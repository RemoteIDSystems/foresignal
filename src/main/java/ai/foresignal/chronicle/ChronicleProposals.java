package ai.foresignal.chronicle;

import ai.foresignal.analyst.AnalystActions;
import ai.foresignal.analyst.AnalystExecutionService;
import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.analyst.AnalystProfileRepository;
import ai.foresignal.analyst.AnalystProposalEntity;
import ai.foresignal.analyst.AnalystProposalRepository;
import ai.foresignal.analyst.AnalystRunEntity;
import ai.foresignal.analyst.ChronicleProposalHandler;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.benchmark.BenchmarkGate;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.cycle.AnalysisCycleEntity;
import ai.foresignal.cycle.AnalysisCycleRepository;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.situation.Severities;
import ai.foresignal.situation.SituationEvent;
import ai.foresignal.situation.SituationEventBus;
import ai.foresignal.situation.SituationListener;
import ai.foresignal.stream.Subscription;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Chronicle proposals (prompt 131, Intelligence-Design §12.3): the {@code propose_chronicle} type of the 102 pipeline
 * and the 128 proposer ({@link ChronicleProposalHandler}), and the deterministic proposer that suggests a chronicle
 * when a situation turns active with severity warning or above, or when a hypothesis set opens on a theme no set of the
 * last 30 days had.
 *
 * <p>A proposed chronicle is created at once with status {@code proposed} (no capture), owned by the analyst profile's
 * owner (else the instance owner), and its owner gets {@code chronicle.proposed} in the inbox. Accepting it
 * ({@code POST /v1/chronicles/{id}/activate}, or confirming the 102 proposal) activates it. When the profile allows
 * {@code propose_chronicle}, it may activate on its own: at most {@code chronicle.autoActivatePerDay} (2) per UTC day,
 * each with its size cap lowered to {@code chronicle.autoActivateCapGb} (5 GB); beyond the daily cap the proposal
 * fails with the reason and the chronicle stays proposed for a person to accept.
 *
 * <p>The deterministic proposer uses the first enabled analysis cycle's profile for the 102 proposal (without one,
 * only the proposed chronicle is created). It never proposes twice for one situation.
 */
@Component
public class ChronicleProposals implements ChronicleProposalHandler, SituationListener, DisposableBean,
        IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(ChronicleProposals.class);

    /** The window in which a hypothesis-set theme counts as known. */
    static final Duration THEME_WINDOW = Duration.ofDays(30);

    private final ChronicleService chronicles;
    private final ChronicleSuggester suggester;
    private final ChronicleNotices notices;
    private final ChronicleRepository repository;
    private final AnalystProposalRepository proposals;
    private final AnalystProfileRepository profiles;
    private final ObjectProvider<AnalystExecutionService> execution;
    private final ObjectProvider<AnalysisCycleRepository> cycles;
    private final ObjectProvider<BenchmarkGate> benchmarks;
    private final SituationEventBus situations;
    private final TokenService tokens;
    private final PrincipalService principals;
    private final AppSettingService settings;
    private final ChronicleProperties properties;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private volatile Subscription subscription;

    /** Creates the component. */
    public ChronicleProposals(ChronicleService chronicles, ChronicleSuggester suggester, ChronicleNotices notices,
            ChronicleRepository repository, AnalystProposalRepository proposals, AnalystProfileRepository profiles,
            ObjectProvider<AnalystExecutionService> execution, ObjectProvider<AnalysisCycleRepository> cycles,
            ObjectProvider<BenchmarkGate> benchmarks, SituationEventBus situations, TokenService tokens,
            PrincipalService principals, AppSettingService settings, ChronicleProperties properties, JdbcTemplate jdbc,
            JsonMapper json, Clock clock) {
        this.chronicles = chronicles;
        this.suggester = suggester;
        this.notices = notices;
        this.repository = repository;
        this.proposals = proposals;
        this.profiles = profiles;
        this.execution = execution;
        this.cycles = cycles;
        this.benchmarks = benchmarks;
        this.situations = situations;
        this.tokens = tokens;
        this.principals = principals;
        this.settings = settings;
        this.properties = properties;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /** Subscribes to situation events once the application is ready (unless capture does not start on its own). */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.CHRONICLES)
    public synchronized void start() {
        if (properties.autoStart() && subscription == null) {
            subscription = situations.subscribe("chronicle-proposer", this, SituationEventBus.DEFAULT_QUEUE);
        }
    }

    /** Closes the subscription. */
    @Override
    public synchronized void destroy() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    /** Nothing cached: the set-theme poll position lives in the app settings. */
    @Override
    public void resetState() {
        // Nothing to forget.
    }

    // ------------------------------------------------------------------------- 102 handler

    @Override
    public String validate(Map<String, Object> payload) {
        Object situationId = payload.get("situationId");
        Object area = payload.get("area");
        if (situationId == null && !(area instanceof Map<?, ?>)) {
            return "a chronicle proposal needs a situationId or an area";
        }
        try {
            if (situationId != null) {
                suggester.fromSituation(String.valueOf(situationId));
            } else {
                ChronicleArea a = ChronicleArea.parse(json.valueToTree(area), "area", null);
                if (a == null || a.isEmpty()) {
                    return "the area is empty";
                }
            }
            return null;
        } catch (ForesignalException e) {
            return e.getMessage();
        }
    }

    @Override
    public void created(AnalystProposalEntity proposal) {
        Map<String, Object> payload = new LinkedHashMap<>(proposal.getPayload() == null ? Map.of()
                : proposal.getPayload());
        Map<String, Object> suggestion = suggestionFor(payload);
        @SuppressWarnings("unchecked")
        Map<String, Object> draft = new LinkedHashMap<>((Map<String, Object>) suggestion.get("draft"));
        if (payload.get("name") instanceof String n && !n.isBlank()) {
            draft.put("name", n.length() > 200 ? n.substring(0, 200) : n);
        }
        AnalystProfileEntity profile = profiles.findById(proposal.getProfileId()).orElse(null);
        ChronicleEntity c = createProposed(draft, ownerOf(profile), proposal.getId(), proposal.getRationale());
        payload.put("chronicleId", c.getId());
        payload.put("draft", draft);
        proposal.setPayload(payload);
    }

    private Map<String, Object> suggestionFor(Map<String, Object> payload) {
        if (payload.get("situationId") != null) {
            return suggester.fromSituation(String.valueOf(payload.get("situationId")));
        }
        ChronicleArea area = ChronicleArea.parse(json.valueToTree(payload.get("area")), "area", null);
        return suggester.fromArea(area, payload.get("topic") == null ? null : String.valueOf(payload.get("topic")),
                payload.get("name") == null ? null : String.valueOf(payload.get("name")));
    }

    @Override
    public String execute(AnalystProposalEntity proposal, boolean auto, String actor) {
        Object id = proposal.getPayload() == null ? null : proposal.getPayload().get("chronicleId");
        if (id == null) {
            created(proposal);
            id = proposal.getPayload().get("chronicleId");
        }
        String chronicleId = String.valueOf(id);
        ChronicleEntity c = chronicles.get(chronicleId);
        if (auto) {
            long cap = autoCapBytes();
            long today = repository.countByAutoActivatedAtGreaterThanEqual(clock.instant().truncatedTo(ChronoUnit.DAYS));
            if (today >= autoPerDay()) {
                throw new ConflictException(ErrorCodes.CHRONICLE_STATE_INVALID, "The daily cap of " + autoPerDay()
                        + " automatic chronicle activations is reached; the chronicle stays proposed for a person to"
                        + " accept.");
            }
            if (c.getSizeCapBytes() > cap) {
                jdbc.update("UPDATE chronicle SET size_cap_bytes = ? WHERE id = ?", cap, c.getId());
            }
        }
        ChronicleEntity active = chronicles.activate(c.getId(), null, ObjectCaller.system(), auto);
        return ChronicleService.ref(active.getId());
    }

    // ------------------------------------------------------------------------- deterministic proposer

    @Override
    public void onEvent(SituationEvent event) {
        try {
            if (qualifies(event)) {
                proposeForSituation(event.situationId(), "The situation turned active with severity "
                        + severity(event) + ".");
            }
        } catch (RuntimeException e) {
            log.warn("Chronicle proposal for situation {} failed: {}", event.situationId(), e.toString());
        }
    }

    /**
     * True when a situation event should propose a chronicle: activation at severity warning or above, or an
     * escalation of an active situation from below warning to warning or above.
     */
    static boolean qualifies(SituationEvent event) {
        if (event == null || event.situationId() == null || event.privateSituation()) {
            return false;
        }
        String severity = severity(event);
        boolean high = Severities.rank(severity) >= Severities.rank("warning");
        if (SituationEvent.ACTIVATED.equals(event.event())) {
            return high;
        }
        if (SituationEvent.ESCALATED.equals(event.event())) {
            Object status = event.situation() == null ? null : event.situation().get("status");
            boolean wasLow = event.previousSeverity() == null
                    || Severities.rank(event.previousSeverity()) < Severities.rank("warning");
            return high && wasLow && "active".equals(status);
        }
        return false;
    }

    private static String severity(SituationEvent event) {
        Object s = event.situation() == null ? null : event.situation().get("severity");
        return s == null ? "info" : String.valueOf(s);
    }

    /**
     * Proposes a chronicle for a situation unless one already links it.
     *
     * @param situationId the situation
     * @param rationale why
     * @return the proposed chronicle, or empty when one already exists
     */
    public Optional<ChronicleEntity> proposeForSituation(String situationId, String rationale) {
        Long existing = jdbc.queryForObject("SELECT count(*) FROM chronicle WHERE situation_ids @> to_jsonb(?::text)",
                Long.class, situationId);
        if (existing != null && existing > 0) {
            return Optional.empty();
        }
        Map<String, Object> suggestion = suggester.fromSituation(situationId);
        return Optional.of(propose(suggestion, Map.of("situationId", situationId), rationale));
    }

    /**
     * Proposes a chronicle from a suggestion: a 102 proposal of the first enabled cycle's profile (when there is one)
     * and the proposed chronicle, auto-executed when the profile allows it.
     */
    ChronicleEntity propose(Map<String, Object> suggestion, Map<String, Object> request, String rationale) {
        @SuppressWarnings("unchecked")
        Map<String, Object> draft = (Map<String, Object>) suggestion.get("draft");
        AnalystProfileEntity profile = cycleProfile();
        if (profile == null) {
            return createProposed(draft, ownerOf(null), null, rationale);
        }
        Instant now = clock.instant();
        AnalystProposalEntity proposal = new AnalystProposalEntity(Ulids.next(now), Ulids.next(now), profile.getId(),
                AnalystActions.PROPOSE_CHRONICLE, now, now.plus(Duration.ofDays(7)));
        Map<String, Object> payload = new LinkedHashMap<>(request);
        payload.put("draft", draft);
        proposal.setPayload(payload);
        proposal.setRationale(rationale);
        proposal.setCitations(List.of());
        proposals.save(proposal);
        ChronicleEntity c = createProposed(draft, ownerOf(profile), proposal.getId(), rationale);
        payload.put("chronicleId", c.getId());
        proposal.setPayload(payload);
        AnalystProposalEntity saved = proposals.save(proposal);
        AnalystExecutionService exec = execution.getIfAvailable();
        if (exec != null && profile.isAllowActions() && profile.getAllowedActions().contains(
                AnalystActions.PROPOSE_CHRONICLE)) {
            BenchmarkGate gate = benchmarks.getIfAvailable();
            if (gate == null || gate.proposerAutoExecutionAllowed(null)) {
                exec.autoExecute(profile, new AnalystRunEntity(saved.getRunId(), profile.getId(), now),
                        List.of(saved), null);
            }
        }
        return repository.findById(c.getId()).orElse(c);
    }

    /** Creates the proposed chronicle and notifies its owner. */
    ChronicleEntity createProposed(Map<String, Object> draft, String owner, String proposalId, String rationale) {
        Map<String, Object> input = new LinkedHashMap<>(draft);
        input.remove("activate");
        ObjectCaller caller = new ObjectCaller(owner, "analyst:chronicle-proposer", Scope.AGENT, true, Set.of());
        ChronicleEntity c = chronicles.create(json.valueToTree(input), caller, ChronicleVocabulary.ORIGIN_ANALYST,
                proposalId);
        notices.proposed(c, rationale);
        return c;
    }

    /** The owner of a profile's proposals: its owner token's principal, else the instance owner. */
    String ownerOf(AnalystProfileEntity profile) {
        if (profile != null && profile.getOwnerTokenId() != null) {
            try {
                Optional<String> principal = tokens.current(UUID.fromString(profile.getOwnerTokenId()))
                        .map(t -> t.principalId());
                if (principal.isPresent() && principal.get() != null) {
                    return principal.get();
                }
            } catch (RuntimeException e) {
                log.debug("No owner token for profile {}: {}", profile.getId(), e.toString());
            }
        }
        return principals.owner().getId();
    }

    private AnalystProfileEntity cycleProfile() {
        AnalysisCycleRepository repo = cycles.getIfAvailable();
        if (repo == null) {
            return null;
        }
        for (AnalysisCycleEntity cycle : repo.findAllByOrderByIdAsc()) {
            if (cycle.isEnabled() && cycle.getAnalystProfileId() != null) {
                Optional<AnalystProfileEntity> p = profiles.findById(cycle.getAnalystProfileId());
                if (p.isPresent()) {
                    return p.get();
                }
            }
        }
        return null;
    }

    private long autoPerDay() {
        try {
            return settings.get(SettingKeys.CHRONICLE_AUTO_ACTIVATE_PER_DAY, Double.class).map(Double::longValue)
                    .filter(v -> v >= 0).orElse(2L);
        } catch (RuntimeException e) {
            return 2L;
        }
    }

    private long autoCapBytes() {
        double gb = 5;
        try {
            gb = settings.get(SettingKeys.CHRONICLE_AUTO_ACTIVATE_CAP_GB, Double.class).filter(v -> v > 0).orElse(5.0);
        } catch (RuntimeException e) {
            gb = 5;
        }
        return (long) (gb * ChronicleSizes.GB);
    }

    // ------------------------------------------------------------------------- hypothesis sets on new themes

    /** Polls new hypothesis sets every five minutes (when capture starts on its own). */
    @Scheduled(fixedDelayString = "${foresignal.chronicles.theme-poll:5m}", initialDelayString = "2m")
    public void scheduledThemes() {
        if (properties.autoStart()) {
            try {
                pollNewThemes();
            } catch (RuntimeException e) {
                log.warn("Chronicle theme proposal poll failed: {}", e.toString());
            }
        }
    }

    /**
     * Proposes a chronicle for every hypothesis set created since the last poll whose themes no other set of the last
     * 30 days had: for its situation when it has one, else for its regions.
     *
     * @return the chronicles proposed
     */
    public synchronized int pollNewThemes() {
        Map<String, Object> cursors = new LinkedHashMap<>();
        settings.get(SettingKeys.CHRONICLE_THREAD_CURSORS, Map.class).ifPresent(m -> ((Map<?, ?>) m)
                .forEach((k, v) -> cursors.put(String.valueOf(k), v)));
        Instant now = clock.instant();
        Instant since = cursors.get("setsCreatedAt") instanceof String s ? Instant.parse(s) : null;
        int proposed = 0;
        if (since != null) {
            List<Map<String, Object>> sets = jdbc.queryForList("SELECT id, question, situation_id, themes, region_codes,"
                    + " private FROM hypothesis_set WHERE created_at > ? AND created_at <= ? ORDER BY created_at",
                    Timestamp.from(since), Timestamp.from(now));
            for (Map<String, Object> set : sets) {
                if (Boolean.TRUE.equals(set.get("private"))) {
                    continue;
                }
                List<String> themes = strings(set.get("themes"));
                if (themes.isEmpty() || knownTheme(String.valueOf(set.get("id")), themes, now)) {
                    continue;
                }
                String rationale = "A hypothesis set opened on a new theme " + themes + ": " + set.get("question");
                try {
                    if (set.get("situation_id") != null) {
                        if (proposeForSituation(String.valueOf(set.get("situation_id")), rationale).isPresent()) {
                            proposed++;
                        }
                    } else {
                        List<String> regions = strings(set.get("region_codes"));
                        if (regions.isEmpty()) {
                            continue;
                        }
                        ChronicleArea area = new ChronicleArea(null, List.of(), regions, null);
                        Map<String, Object> suggestion = suggester.fromArea(area, themes.getFirst(),
                                String.valueOf(set.get("question")));
                        @SuppressWarnings("unchecked")
                        Map<String, Object> draft = (Map<String, Object>) suggestion.get("draft");
                        draft.put("hypothesisSetIds", List.of(String.valueOf(set.get("id"))));
                        propose(suggestion, Map.of("area", area.toMap(), "topic", themes.getFirst()), rationale);
                        proposed++;
                    }
                } catch (RuntimeException e) {
                    log.warn("Chronicle proposal for set {} failed: {}", set.get("id"), e.toString());
                }
            }
        }
        Map<String, Object> latest = new LinkedHashMap<>();
        settings.get(SettingKeys.CHRONICLE_THREAD_CURSORS, Map.class).ifPresent(m -> ((Map<?, ?>) m)
                .forEach((k, v) -> latest.put(String.valueOf(k), v)));
        latest.put("setsCreatedAt", now.toString());
        settings.put(SettingKeys.CHRONICLE_THREAD_CURSORS, latest);
        return proposed;
    }

    private boolean knownTheme(String setId, List<String> themes, Instant now) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM hypothesis_set WHERE id <> ? AND created_at >= ?"
                + " AND themes && ?::text[]", Long.class, setId, Timestamp.from(now.minus(THEME_WINDOW)),
                themes.toArray(String[]::new));
        return n != null && n > 0;
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof java.sql.Array array) {
            try {
                Object[] items = (Object[]) array.getArray();
                for (Object o : items) {
                    if (o != null) {
                        out.add(o.toString());
                    }
                }
            } catch (java.sql.SQLException e) {
                return out;
            }
        } else if (value instanceof String[] items) {
            out.addAll(List.of(items));
        } else if (value instanceof java.util.Collection<?> c) {
            c.forEach(o -> out.add(String.valueOf(o)));
        }
        return out;
    }
}
