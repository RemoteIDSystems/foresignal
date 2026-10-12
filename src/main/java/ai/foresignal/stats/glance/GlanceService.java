package ai.foresignal.stats.glance;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.briefing.BriefingEntity;
import ai.foresignal.briefing.BriefingLinks;
import ai.foresignal.briefing.BriefingRepository;
import ai.foresignal.briefing.BriefingRunEntity;
import ai.foresignal.briefing.BriefingRunRepository;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.ingest.IngestWriter;
import ai.foresignal.ingest.WriterState;
import ai.foresignal.ingest.WriterStatus;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.settings.ServerSettingsService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.stats.StatsService;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.system.ReadinessCheck;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Assembles the glance (prompt 038).
 *
 * <ul>
 *   <li>{@code status}: the worst of the writer ({@code failing} when stopped or its lock is lost,
 *       {@code degraded} while its clock guard holds batches), readiness ({@code failing} when not
 *       ready) and the source tallies ({@code degraded} when a visible source is failing);
 *   <li>{@code alerts.open}: unresolved and unacknowledged ({@code state = firing}), unsuppressed
 *       alerts per severity; {@code alerts.latest}: the three newest of them;
 *   <li>{@code sources}: healthy, degraded and failing among the sources the caller may see;
 *   <li>{@code briefing}: the newest completed run the caller may see;
 *   <li>{@code activity.versionsLastHour}, {@code lastSeq}, {@code links} (023 {@code clientLinks}) and
 *       the {@link GlanceContributor} sections.
 * </ul>
 *
 * <p><b>Visibility.</b> Without a token (under {@code openReads}) only {@code status},
 * {@code serverTime}, {@code alerts.latest} (already public through {@code /v1/alerts}) and
 * {@code lastSeq} are shown; the counts join them when {@code glance.exposeCountsAnonymously} is set.
 * Private-source alerts follow the 023 gate, and {@code place} narrows the alerts to a 036 place.
 */
@Service
public class GlanceService {

    private static final Logger log = LoggerFactory.getLogger(GlanceService.class);

    /** How many latest alerts are listed. */
    public static final int LATEST = 3;

    /** The run statuses a briefing is shown for. */
    static final List<String> COMPLETED = List.of(BriefingRunEntity.SUCCEEDED, BriefingRunEntity.FALLBACK);

    private final Clock clock;
    private final ReadinessCheck readiness;
    private final ObjectProvider<IngestWriter> writer;
    private final HealthService health;
    private final AlertStore alerts;
    private final BriefingRunRepository runs;
    private final BriefingRepository briefings;
    private final BriefingLinks briefingLinks;
    private final ServerSettingsService server;
    private final StatsService stats;
    private final ChangeBus changeBus;
    private final AppSettingService settings;
    private final ObjectProvider<GlanceContributor> contributors;
    private final GlanceAssembler assembler;

    /** Creates the service. */
    public GlanceService(Clock clock, ReadinessCheck readiness, ObjectProvider<IngestWriter> writer,
            HealthService health, AlertStore alerts, BriefingRunRepository runs, BriefingRepository briefings,
            BriefingLinks briefingLinks, ServerSettingsService server, StatsService stats, ChangeBus changeBus,
            AppSettingService settings, ObjectProvider<GlanceContributor> contributors, JsonMapper json) {
        this.clock = clock;
        this.readiness = readiness;
        this.writer = writer;
        this.health = health;
        this.alerts = alerts;
        this.runs = runs;
        this.briefings = briefings;
        this.briefingLinks = briefingLinks;
        this.server = server;
        this.stats = stats;
        this.changeBus = changeBus;
        this.settings = settings;
        this.contributors = contributors;
        this.assembler = new GlanceAssembler(json);
    }

    /**
     * Who asks.
     *
     * @param caller the caller, with any {@code place} folded into its restriction
     * @param excludedSources the sources the caller may not see
     * @param placeId the {@code place} parameter, or null
     */
    public record Request(ReadCaller caller, Set<String> excludedSources, String placeId) {}

    /** The glance data of {@code request}. */
    public GlanceData data(Request request) {
        Instant now = clock.instant();
        ReadCaller caller = request.caller();
        boolean anonymous = caller.isAnonymous();
        boolean counts = !anonymous || glanceSettings().exposeCountsAnonymously();
        ReadRestriction restriction = caller.restriction();
        long lastSeq = changeBus.lastPublishedSeq();

        AlertQuery open = new AlertQuery(null, null, null, null, false, List.of("firing"), null, null, null, false,
                request.excludedSources(), restriction);
        List<GlanceData.Latest> latest = new ArrayList<>();
        for (AlertView a : alerts.list(open, null, LATEST)) {
            latest.add(new GlanceData.Latest(a.id(), a.severity(), shortTitle(a), a.firedAt(), null));
        }
        List<SourceHealthView> visible = visibleSources(request.excludedSources(), restriction);
        String status = status(visible);

        Map<String, Long> openCounts = counts ? alerts.countBySeverity(open) : null;
        GlanceData.Sources tallies = null;
        Long versions = null;
        if (counts) {
            tallies = new GlanceData.Sources(
                    visible.stream().filter(s -> s.status() == HealthStatus.HEALTHY).count(),
                    visible.stream().filter(s -> s.status() == HealthStatus.DEGRADED).count(),
                    visible.stream().filter(s -> s.status() == HealthStatus.FAILING).count());
            versions = stats.versionsSince(now.minus(Duration.ofHours(1)), request.excludedSources(), restriction);
        }
        GlanceData.Briefing briefing = null;
        Map<String, String> links = null;
        LinkedHashMap<String, Object> sections = null;
        if (!anonymous) {
            Optional<BriefingRunEntity> run = latestRun(restriction);
            if (run.isPresent()) {
                BriefingRunEntity r = run.get();
                briefing = new GlanceData.Briefing(r.getBriefingName(), r.getStartedAt(),
                        briefingLinks.runHtml(r.getBriefingId(), r.getId()));
            }
            links = links(latest, run.orElse(null));
            sections = sections(new GlanceContributor.Context(caller, request.excludedSources(), request.placeId(),
                    now));
        }
        return new GlanceData(now, status, openCounts, latest, tallies, briefing, versions, lastSeq, links, sections);
    }

    /** The JSON glance of {@code request} (at most 2,048 bytes). */
    public String json(Request request) {
        return assembler.json(data(request));
    }

    /** The text glance of {@code request} (at most 8 lines of 40 ASCII characters). */
    public String text(Request request) {
        return GlanceAssembler.text(data(request));
    }

    // ------------------------------------------------------------------------- parts

    private GlanceSettings glanceSettings() {
        return settings.getOrDefault(SettingKeys.GLANCE, GlanceSettings.class, GlanceSettings::defaults);
    }

    private static String shortTitle(AlertView a) {
        String title = a.ruleName() != null && !a.ruleName().isBlank() ? a.ruleName() : a.message();
        return GlanceAssembler.cut(title == null ? "Alert " + a.id() : title, GlanceAssembler.MAX_TITLE);
    }

    private List<SourceHealthView> visibleSources(Set<String> excluded, ReadRestriction restriction) {
        List<SourceHealthView> out = new ArrayList<>();
        try {
            for (SourceHealthView view : health.all(Optional.empty())) {
                if (!excluded.contains(view.sourceId()) && restriction.allowsSource(view.sourceId())) {
                    out.add(view);
                }
            }
        } catch (RuntimeException e) {
            log.debug("Source health unavailable for the glance", e);
        }
        return out;
    }

    /** The worst of the writer, readiness and the visible sources. */
    String status(List<SourceHealthView> sources) {
        IngestWriter w = writer.getIfAvailable();
        WriterStatus ws = w == null ? null : w.status();
        boolean ready;
        try {
            ready = readiness.ready();
        } catch (RuntimeException e) {
            ready = false;
        }
        return status(ws == null ? null : ws.state(), ready,
                sources.stream().anyMatch(s -> s.status() == HealthStatus.FAILING));
    }

    /** {@code failing} when not ready or the writer cannot write; {@code degraded} on a clock hold or a failing source. */
    static String status(WriterState writerState, boolean ready, boolean failingSource) {
        if (!ready || writerState == WriterState.STOPPED || writerState == WriterState.LOCK_LOST) {
            return "failing";
        }
        if (writerState == WriterState.CLOCK_BEHIND || failingSource) {
            return "degraded";
        }
        return "ok";
    }

    private Optional<BriefingRunEntity> latestRun(ReadRestriction restriction) {
        for (BriefingRunEntity run : runs.findTop10ByStatusInOrderByStartedAtDescIdDesc(COMPLETED)) {
            Optional<BriefingEntity> briefing = briefings.findById(run.getBriefingId());
            if (briefing.isPresent() && BriefingService.visibleTo(briefing.get(), restriction)) {
                return Optional.of(run);
            }
        }
        return Optional.empty();
    }

    private Map<String, String> links(List<GlanceData.Latest> latest, BriefingRunEntity run) {
        ClientLinks templates = server.get().effectiveClientLinks();
        Map<String, String> out = new LinkedHashMap<>();
        if (!latest.isEmpty()) {
            out.put("latestAlert", ClientLinks.fill(templates.alert(), Map.of("id", Long.toString(latest.getFirst().id()))));
        }
        if (run != null) {
            out.put("briefing", ClientLinks.fill(templates.briefingRun(),
                    Map.of("briefingId", run.getBriefingId().toString(), "id", run.getId().toString())));
        }
        return out;
    }

    private LinkedHashMap<String, Object> sections(GlanceContributor.Context context) {
        LinkedHashMap<String, Object> out = new LinkedHashMap<>();
        contributors.orderedStream().forEach(contributor -> {
            try {
                contributor.contribute(context).ifPresent(value -> {
                    if (assembler.size(value) <= contributor.budgetBytes()) {
                        out.put(contributor.key(), value);
                    } else {
                        log.debug("Glance section {} exceeds its {} byte budget; skipped", contributor.key(),
                                contributor.budgetBytes());
                    }
                });
            } catch (RuntimeException e) {
                log.warn("Glance contributor {} failed; its section is skipped", contributor.key(), e);
            }
        });
        return out;
    }
}
