package ai.foresignal.cycle;

import ai.foresignal.analyst.AnalystProfileRepository;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.briefing.BriefingValidator;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.situation.SituationService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates, reads, replaces and deletes analysis cycles (prompt 128). On first start (no cycle and no run yet) it
 * creates the three defaults, <strong>disabled</strong>: "Daily brief" (06:00), "Weekly assessment" (Monday 07:00)
 * and "Monthly outlook" (the 1st, 08:00), in the operator's timezone from the 018 {@code server} setting. Saving a
 * cycle recomputes {@code next_run_at} from now; a disabled cycle has none.
 */
@Service
public class CycleService {

    private static final Logger log = LoggerFactory.getLogger(CycleService.class);

    /** How long a {@code running_since} claim is honored. */
    public static final Duration STALE = Duration.ofHours(2);

    private final AnalysisCycleRepository cycles;
    private final CycleRunRepository runs;
    private final AnalystProfileRepository profiles;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the service. */
    public CycleService(AnalysisCycleRepository cycles, CycleRunRepository runs, AnalystProfileRepository profiles,
            AppSettingService settings, Clock clock) {
        this.cycles = cycles;
        this.runs = runs;
        this.profiles = profiles;
        this.settings = settings;
        this.clock = clock;
    }

    /** Creates the disabled defaults on first start. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.CYCLES)
    public void ensureDefaults() {
        try {
            createDefaults();
        } catch (DataIntegrityViolationException e) {
            // Another node created them first.
        } catch (RuntimeException e) {
            log.warn("Could not create the default analysis cycles: {}", e.toString());
        }
    }

    /** Creates the three defaults when no cycle and no run exist; returns how many were created. */
    @Transactional
    public int createDefaults() {
        if (cycles.count() > 0 || runs.count() > 0) {
            return 0;
        }
        String zone = operatorTimezone();
        Instant now = clock.instant();
        cycles.save(builtIn("Daily brief", CycleVocabulary.DAILY, "0 6 * * *", zone, now));
        cycles.save(builtIn("Weekly assessment", CycleVocabulary.WEEKLY, "0 7 * * 1", zone, now.plusMillis(1)));
        cycles.save(builtIn("Monthly outlook", CycleVocabulary.MONTHLY, "0 8 1 * *", zone, now.plusMillis(2)));
        return 3;
    }

    private AnalysisCycleEntity builtIn(String name, String cadence, String cron, String zone, Instant now) {
        AnalysisCycleEntity c = new AnalysisCycleEntity(Ulids.next(now), name, cadence, now);
        c.setSchedule(Map.of("cron", cron, "timezone", zone));
        c.setScope(CycleScope.ALL.toMap());
        c.setRoles(CycleVocabulary.ROLES);
        c.setNotifyMinPriority(CycleVocabulary.priority(cadence));
        c.setBuiltIn(true);
        c.setEnabled(false);
        return c;
    }

    /** The operator timezone (018 {@code server.timezone}), else UTC. */
    String operatorTimezone() {
        try {
            ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class,
                    ServerSettings::defaults);
            String zone = server == null ? null : server.timezone();
            if (zone != null && !zone.isBlank()) {
                BriefingValidator.zone(zone);
                return zone.strip();
            }
        } catch (RuntimeException e) {
            log.debug("The server timezone is unusable: {}", e.toString());
        }
        return "UTC";
    }

    /** Every cycle by id, paged by offset. */
    public Page list(String cursor, int limit) {
        int offset = SituationService.offset(cursor);
        List<AnalysisCycleEntity> all = cycles.findAllByOrderByIdAsc();
        int end = Math.min(all.size(), offset + limit);
        List<AnalysisCycleEntity> page = offset >= all.size() ? List.of() : new ArrayList<>(all.subList(offset, end));
        return new Page(page, end < all.size() ? SituationService.offsetCursor(end) : null);
    }

    /**
     * A page of cycles.
     *
     * @param items the cycles
     * @param nextCursor the next cursor, or null
     */
    public record Page(List<AnalysisCycleEntity> items, String nextCursor) {}

    /**
     * A cycle.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_CYCLE}
     */
    public AnalysisCycleEntity get(String id) {
        return cycles.findById(id == null ? "" : id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_CYCLE,
                "No analysis cycle '" + id + "'."));
    }

    /**
     * Creates a cycle.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_ANALYST_PROFILE}
     */
    @Transactional
    public AnalysisCycleEntity create(CycleInputs.CycleDraft draft) {
        checkProfile(draft.analystProfileId());
        Instant now = clock.instant();
        AnalysisCycleEntity c = new AnalysisCycleEntity(Ulids.next(now), draft.name(), draft.cadence(), now);
        apply(c, draft, now);
        return cycles.save(c);
    }

    /**
     * Replaces a cycle and recomputes its next run.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_CYCLE} or {@code UNKNOWN_ANALYST_PROFILE}
     */
    @Transactional
    public AnalysisCycleEntity replace(String id, CycleInputs.CycleDraft draft) {
        AnalysisCycleEntity c = get(id);
        checkProfile(draft.analystProfileId());
        apply(c, draft, clock.instant());
        return cycles.save(c);
    }

    /**
     * Deletes a cycle; its runs and assessments are kept.
     *
     * @throws ConflictException 409 {@code CYCLE_RUN_IN_PROGRESS}
     */
    @Transactional
    public void delete(String id) {
        AnalysisCycleEntity c = get(id);
        if (running(c, clock.instant())) {
            throw new ConflictException(ErrorCodes.CYCLE_RUN_IN_PROGRESS, "Analysis cycle " + id
                    + " has a run in progress.");
        }
        cycles.delete(c);
    }

    /** True when the cycle holds a fresh running claim. */
    public static boolean running(AnalysisCycleEntity c, Instant now) {
        return c.getRunningSince() != null && c.getRunningSince().isAfter(now.minus(STALE));
    }

    private void apply(AnalysisCycleEntity c, CycleInputs.CycleDraft d, Instant now) {
        c.setName(d.name());
        c.setCadence(d.cadence());
        c.setSchedule(CycleInputs.schedule(d));
        c.setScope(d.scope().toMap());
        c.setAnalystProfileId(d.analystProfileId());
        c.setRoles(d.roles());
        c.setForecasterProviders(d.forecasterProviders());
        c.setSamples(d.samples());
        c.setMaxQuestions(d.maxQuestions());
        c.setMaxTokensPerCycle(d.maxTokensPerCycle());
        c.setChannelIds(d.channelIds());
        c.setNotifyMinPriority(d.notifyMinPriority());
        c.setEnabled(d.enabled());
        c.setNextRunAt(d.enabled() ? next(c, now) : null);
        c.setUpdatedAt(now);
    }

    /** The cycle's next fire time after {@code after}, or null when the schedule is unusable. */
    public static Instant next(AnalysisCycleEntity c, Instant after) {
        try {
            Object cron = c.getSchedule().get("cron");
            Object zone = c.getSchedule().get("timezone");
            return BriefingValidator.nextRun(String.valueOf(cron), zone == null ? "UTC" : String.valueOf(zone), after);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void checkProfile(String profileId) {
        if (profileId != null && !profiles.existsById(profileId)) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_ANALYST_PROFILE, "No analyst profile '" + profileId + "'.");
        }
    }
}
