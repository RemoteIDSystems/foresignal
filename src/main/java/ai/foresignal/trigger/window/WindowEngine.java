package ai.foresignal.trigger.window;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.MatchRow;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.RuleSpecs;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Evaluates the enabled windowed rules (prompt 100).
 *
 * <ul>
 *   <li><strong>Count and absence</strong> evaluate the current window {@code (now − window, now]} on
 *       each due step: live rows and versions, everything known now. Missed steps are not replayed.
 *   <li><strong>Anomaly</strong> evaluates completed buckets in order from a durable cursor
 *       ({@code anomaly.cursor.{ruleId}}), including after downtime and, on creation or rebuild, from
 *       the retained history of the last 12 weeks. A bucket is evaluated only once it has settled
 *       ({@link #SETTLE} after its end), from versions and {@code fetch_log} history known by then, so
 *       a live evaluation and a later catch-up of the same bucket give the same result (INT-1, INT-2).
 *       Only the newest bucket moves the window state and may alert; older buckets fill samples and
 *       observations.
 *   <li><strong>Alerts</strong> are owed on clear → active and written once the {@code rule-windows}
 *       version of that state exists, so {@code alert.seq} references it; an alert that cannot be
 *       written is retried on the next tick. Cooldown and {@code maxAlertsPerHour} apply per
 *       {@code (rule, group)}. {@code unknown} and {@code learning} never fire or resolve.
 *   <li><strong>Caps:</strong> a window with more candidate rows than {@link WindowMembership#ROW_CAP}
 *       is {@code unknown}, never a partial count.
 * </ul>
 */
@Service
public class WindowEngine {

    /** The DERIVED source that owns the {@code window-state} and {@code anomaly} records. */
    public static final String SOURCE_ID = "rule-windows";

    /** The window-state record kind. */
    public static final String STATE_KIND = "window-state";

    /** The anomaly record kind. */
    public static final String ANOMALY_KIND = "anomaly";

    /** The system-event subtype an absence rule raises when it cannot be evaluated. */
    public static final String UNKNOWN_SUBTYPE = "system.window_rule_unknown";

    /** How long after a bucket's end it is evaluated, so late versions are in (the decision time). */
    public static final Duration SETTLE = Duration.ofMinutes(2);

    /** The baseline lookback (prompt 100 §4: the last 12 weeks), also the creation backfill. */
    public static final Duration LOOKBACK = Duration.ofDays(84);

    /** How many anomaly buckets one tick evaluates per rule (catch-up is spread over ticks). */
    public static final int MAX_BUCKETS_PER_TICK = 24;

    /** How long an owed alert waits for its {@code rule-windows} version before it is written anyway. */
    public static final Duration PENDING_GRACE = Duration.ofMinutes(5);

    /** Anomaly records are emitted for buckets this recent; older catch-up buckets only fill tables. */
    public static final Duration EMIT_RECENT = Duration.ofHours(24);

    private static final Logger log = LoggerFactory.getLogger(WindowEngine.class);

    private final TriggerRuleRepository rules;
    private final RuleSpecs specs;
    private final WindowMembership membership;
    private final CoverageGate gate;
    private final WindowStateRepository states;
    private final ActivitySampleRepository samples;
    private final AnomalyObservationRepository anomalies;
    private final AlertStore alerts;
    private final ChangeBus changeBus;
    private final ApplicationEventPublisher events;
    private final JsonMapper json;
    private final AppSettingService settings;
    private final JdbcTemplate jdbc;

    /** Creates the engine. */
    public WindowEngine(TriggerRuleRepository rules, RuleSpecs specs, WindowMembership membership, CoverageGate gate,
            WindowStateRepository states, ActivitySampleRepository samples, AnomalyObservationRepository anomalies,
            AlertStore alerts, ChangeBus changeBus, ApplicationEventPublisher events, JsonMapper json,
            AppSettingService settings, JdbcTemplate jdbc) {
        this.rules = rules;
        this.specs = specs;
        this.membership = membership;
        this.gate = gate;
        this.states = states;
        this.samples = samples;
        this.anomalies = anomalies;
        this.alerts = alerts;
        this.changeBus = changeBus;
        this.events = events;
        this.json = json;
        this.settings = settings;
        this.jdbc = jdbc;
    }

    /** Detectability (prompt 116), or null when the observability layer is absent. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.observability.ObservabilityService observability;

    /** The observability family of each kind (prompt 116), or null. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.observability.ObservabilityDeclarations declarations;

    /** Analysis manifests (prompt 116), or null. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.manifest.ManifestService manifests;

    /** Where window-state and anomaly records go (the {@code rule-windows} {@code DerivedContext}). */
    public interface Emitter {

        /** Emits a record. */
        void emit(NormalizedRecord record);

        /** Tombstones a record. */
        void tombstone(String kind, String externalId);
    }

    /**
     * A compiled windowed rule.
     *
     * @param spec the compiled 017 filters
     * @param type the windowed type
     * @param window the window spec
     * @param createdAt when the rule was created (the absence warm-up)
     */
    public record WindowRule(RuleSpec spec, RuleType type, WindowSpec window, Instant createdAt) {

        /** A rule without a known creation time (treated as old enough). */
        public WindowRule(RuleSpec spec, RuleType type, WindowSpec window) {
            this(spec, type, window, null);
        }
    }

    /**
     * The durable anomaly cursor of one rule.
     *
     * @param bucketStart the start (epoch seconds) of the last bucket evaluated, or -1 to backfill
     * @param watermarkSeq the change-bus watermark when it was evaluated
     */
    public record AnomalyCursor(long bucketStart, long watermarkSeq) {}

    /** True when {@code entity} is a windowed rule. */
    public static boolean windowed(TriggerRuleEntity entity) {
        try {
            return RuleType.fromWire(entity.getType()).windowed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Compiles every enabled windowed rule. */
    public List<WindowRule> enabled() {
        List<WindowRule> out = new ArrayList<>();
        for (TriggerRuleEntity entity : rules.findByEnabledTrue()) {
            if (!windowed(entity)) {
                continue;
            }
            try {
                RuleType type = RuleType.fromWire(entity.getType());
                WindowSpec window = WindowSpec.fromStored(entity.getWindowSpec());
                if (window == null) {
                    continue;
                }
                out.add(new WindowRule(specs.compile(entity), type, window, entity.getCreatedAt()));
            } catch (RuntimeException e) {
                log.warn("rule {} could not be compiled: {}", entity.getId(), e.toString());
            }
        }
        return out;
    }

    /** Evaluates every enabled windowed rule at {@code now}. */
    public void evaluate(Instant now, Emitter emitter) {
        long watermark = changeBus.lastPublishedSeq();
        for (WindowRule rule : enabled()) {
            try {
                firePending(rule, now, watermark);
                switch (rule.type()) {
                    case COUNT -> count(rule, now, watermark, emitter);
                    case ABSENCE -> absence(rule, now, watermark, emitter);
                    case ANOMALY -> anomaly(rule, now, watermark, emitter);
                    default -> { }
                }
            } catch (RuntimeException e) {
                log.warn("windowed rule {} failed: {}", rule.spec().id(), e.toString());
            }
        }
    }

    // ------------------------------------------------------------------ count

    private void count(WindowRule rule, Instant now, long watermark, Emitter emitter) {
        if (!due(rule, now)) {
            return;
        }
        Instant from = now.minus(rule.window().window());
        WindowMembership.Membership result = membership.resolve(rule.spec(), rule.window(), from, now, watermark);
        if (result.capped()) {
            unknownEverywhere(rule, capReason(result), watermark, now, emitter);
            return;
        }
        Map<String, Integer> counts = new TreeMap<>(result.counts());
        if (rule.window().groupBy() == null || rule.window().groupBy().isBlank()) {
            counts.putIfAbsent("*", 0);
        }
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            int value = entry.getValue();
            transition(rule, state(rule, entry.getKey()), countActive(rule, value) ? "active" : "clear", (double) value,
                    (double) rule.window().threshold(), null, watermark, now, emitter, null, null);
        }
        for (WindowStateEntity state : states.findByRuleIdOrderByGroupKey(rule.spec().id())) {
            if (counts.containsKey(state.getGroupKey())) {
                continue;
            }
            transition(rule, state, countActive(rule, 0) ? "active" : "clear", 0.0,
                    (double) rule.window().threshold(), null, watermark, now, emitter, null, null);
        }
    }

    private static boolean countActive(WindowRule rule, int value) {
        return ">=".equals(rule.window().op()) ? value >= rule.window().threshold() : value <= rule.window().threshold();
    }

    // ------------------------------------------------------------------ absence

    private void absence(WindowRule rule, Instant now, long watermark, Emitter emitter) {
        if (!due(rule, now)) {
            return;
        }
        Instant from = now.minus(rule.window().window());
        WindowStateEntity state = state(rule, "*");
        String warmUp = warmUp(rule, now);
        if (warmUp != null) {
            transition(rule, state, "learning", null, 0.0, warmUp, watermark, now, emitter, null, null);
            return;
        }
        CoverageGate.Gate gateResult = gate.evaluate(rule.spec().sources(), rule.spec().kinds(), from, now,
                rule.window().window(), rule.window().coverageMin());
        if (!gateResult.ok()) {
            unknown(rule, state, gateResult.reason(), watermark, now, emitter);
            return;
        }
        WindowMembership.Membership result = membership.resolve(rule.spec(), rule.window(), from, now, watermark);
        if (result.capped()) {
            unknown(rule, state, capReason(result), watermark, now, emitter);
            return;
        }
        int total = result.counts().values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            // 116 (OBS-1): an absence needs detectability as well as healthy coverage.
            ai.foresignal.observability.AbsenceCheck detect = detectable(rule, from, now, false);
            if (!detect.ok()) {
                unknown(rule, state, detect.reason(), watermark, now, emitter);
                return;
            }
        }
        transition(rule, state, total == 0 ? "active" : "clear", (double) total, 0.0, null, watermark, now, emitter,
                null, null);
    }

    /**
     * The detectability half of the absence rule (prompt 116, OBS-1) for a rule's involved sources over each of
     * its observability families, at the rule's geofence (everywhere without one): ok only when every family's
     * combined {@code p ≥ absenceMinDetect}; otherwise {@code LOW_DETECTABILITY}. Always ok without the
     * observability layer.
     */
    ai.foresignal.observability.AbsenceCheck detectable(WindowRule rule, Instant from, Instant to, boolean historical) {
        if (observability == null || declarations == null) {
            return new ai.foresignal.observability.AbsenceCheck(true, null, null);
        }
        Set<String> involved = gate.involvedSources(rule.spec().sources(), rule.spec().kinds(), from, to, historical);
        ai.foresignal.observability.Place place = rule.spec().fence() == null
                ? ai.foresignal.observability.Place.GLOBAL
                : ai.foresignal.observability.Place.polygon(rule.spec().fence());
        Set<String> families = new java.util.TreeSet<>();
        for (String kind : rule.spec().kinds() == null ? Set.<String>of() : rule.spec().kinds()) {
            families.add(declarations.familyOfKind(kind));
        }
        if (families.isEmpty()) {
            families.add("*");
        }
        Double lowest = null;
        for (String family : families) {
            ai.foresignal.observability.AbsenceCheck check = "*".equals(family)
                    ? new ai.foresignal.observability.AbsenceCheck(true, null, null)
                    : observability.detectable(involved, family, place, to, null);
            if (check.p() != null) {
                lowest = lowest == null ? check.p() : Math.min(lowest, check.p());
            }
            if (!check.ok()) {
                return check;
            }
        }
        return new ai.foresignal.observability.AbsenceCheck(true, lowest, null);
    }

    /**
     * Why an absence rule may not be evaluated yet, or null: the rule and every involved source must
     * have existed for one full window (prompt 100 §3).
     */
    private String warmUp(WindowRule rule, Instant now) {
        Instant since = now.minus(rule.window().window());
        if (rule.createdAt() != null && rule.createdAt().isAfter(since)) {
            return "warm-up: the rule has not existed for one full window";
        }
        for (String sourceId : gate.involvedSources(rule.spec().sources(), rule.spec().kinds(), since, now, false)) {
            Instant first = gate.firstFetch(sourceId);
            if (first == null || first.isAfter(since)) {
                return "warm-up: source " + sourceId + " has not existed for one full window";
            }
        }
        return null;
    }

    /** {@code unknown} with one system event per transition into it. */
    private void unknown(WindowRule rule, WindowStateEntity state, String reason, long watermark, Instant now,
            Emitter emitter) {
        boolean firstUnknown = !"unknown".equals(state.getState());
        transition(rule, state, "unknown", null, null, reason, watermark, now, emitter, null, null);
        if (firstUnknown) {
            events.publishEvent(new SystemEvent(UNKNOWN_SUBTYPE, "warning",
                    "Windowed rule cannot be evaluated",
                    "Rule '" + rule.spec().name() + "' (" + rule.spec().id() + ") cannot be evaluated: " + reason,
                    "/v1/triggers/" + rule.spec().id() + "/window-state",
                    Map.of("ruleId", rule.spec().id().toString(), "reason", reason)));
        }
    }

    private void unknownEverywhere(WindowRule rule, String reason, long watermark, Instant now, Emitter emitter) {
        List<WindowStateEntity> existing = states.findByRuleIdOrderByGroupKey(rule.spec().id());
        if (existing.isEmpty()) {
            existing = List.of(state(rule, "*"));
        }
        for (WindowStateEntity state : existing) {
            unknown(rule, state, reason, watermark, now, emitter);
        }
    }

    private static String capReason(WindowMembership.Membership result) {
        return "more than " + WindowMembership.ROW_CAP + " candidate rows in the window; the count is unknown";
    }

    // ------------------------------------------------------------------ anomaly

    private void anomaly(WindowRule rule, Instant now, long watermark, Emitter emitter) {
        Duration bucket = rule.window().bucket();
        // The newest bucket whose end has settled: [latest, latest + bucket) with latest + bucket + SETTLE ≤ now.
        Instant latest = floor(now.minus(SETTLE), bucket).minus(bucket);
        AnomalyCursor cursor = cursor(rule.spec().id());
        Instant next;
        if (cursor != null && cursor.bucketStart() >= 0) {
            next = Instant.ofEpochSecond(cursor.bucketStart()).plus(bucket);
        } else if (cursor == null) {
            // No stored cursor: continue after the newest sample, or backfill the lookback on creation.
            next = samples.findFirstByRuleIdOrderByBucketStartDesc(rule.spec().id())
                    .map(s -> s.getBucketStart().plus(bucket))
                    .orElse(floor(now.minus(LOOKBACK), bucket));
        } else {
            next = floor(now.minus(LOOKBACK), bucket); // reset by a rebuild: backfill the lookback
        }
        int done = 0;
        while (!next.isAfter(latest) && done < MAX_BUCKETS_PER_TICK) {
            evaluateBucket(rule, next, next.equals(latest), watermark, now, emitter);
            storeCursor(rule.spec().id(), new AnomalyCursor(next.getEpochSecond(), watermark));
            next = next.plus(bucket);
            done++;
        }
    }

    private void evaluateBucket(WindowRule rule, Instant bucketStart, boolean newest, long watermark, Instant now,
            Emitter emitter) {
        Duration bucket = rule.window().bucket();
        Instant bucketEnd = bucketStart.plus(bucket);
        Instant knownBy = bucketEnd.plus(SETTLE);
        WindowMembership.Membership result = membership.resolve(rule.spec(), rule.window(), bucketStart, bucketEnd,
                watermark, false, knownBy, WindowMembership.ROW_CAP);
        CoverageGate.Gate coverage = gate.evaluateAsOf(rule.spec().sources(), rule.spec().kinds(), bucketStart,
                bucketEnd, bucket, rule.window().coverageMin());
        boolean coverageOk = coverage.ok() && !result.capped();
        double coverageRatio = gate.coverageRatioAsOf(rule.spec().sources(), rule.spec().kinds(), bucketStart, bucketEnd);
        Map<String, long[]> fetchRanges =
                gate.fetchIdRanges(rule.spec().sources(), rule.spec().kinds(), bucketStart, bucketEnd, true);
        Instant lookbackStart = bucketStart.minus(LOOKBACK);
        List<ActivitySampleEntity> history = samples
                .findByRuleIdAndBucketStartGreaterThanEqualOrderByBucketStartAsc(rule.spec().id(), lookbackStart);

        Set<String> groups = new LinkedHashSet<>(result.counts().keySet());
        for (ActivitySampleEntity sample : history) {
            groups.add(sample.getGroupKey());
        }
        if (rule.window().groupBy() == null || rule.window().groupBy().isBlank()) {
            groups.add("*");
        }
        for (String group : new java.util.TreeSet<>(groups)) {
            evaluateGroup(rule, group, bucketStart, bucketEnd, result, coverage, coverageOk, coverageRatio, fetchRanges,
                    history, newest, watermark, now, emitter);
        }
    }

    private void evaluateGroup(WindowRule rule, String group, Instant bucketStart, Instant bucketEnd,
            WindowMembership.Membership result, CoverageGate.Gate coverage, boolean coverageOk, double coverageRatio,
            Map<String, long[]> fetchRanges, List<ActivitySampleEntity> history, boolean newest, long watermark,
            Instant now, Emitter emitter) {
        if (samples.findById(new ActivitySampleEntity.Key(rule.spec().id(), group, bucketStart)).isPresent()) {
            return; // already evaluated: buckets are evaluated once
        }
        int observed = result.counts().getOrDefault(group, 0);
        ActivitySampleEntity sample = new ActivitySampleEntity(rule.spec().id(), group, bucketStart, observed);
        sample.setCoverageOk(coverageOk);
        sample.setCoverageRatio(coverageRatio);
        samples.save(sample);

        int slot = slotOf(bucketStart, rule.window().slotMode());
        List<Integer> slotCounts = new ArrayList<>();
        List<Double> slotCoverages = new ArrayList<>();
        for (ActivitySampleEntity other : history) {
            if (!other.getGroupKey().equals(group) || other.isFlagged() || !other.isCoverageOk()
                    || !other.getBucketStart().isBefore(bucketStart)
                    || slotOf(other.getBucketStart(), rule.window().slotMode()) != slot) {
                continue;
            }
            slotCounts.add(other.getCount());
            slotCoverages.add(other.getCoverageRatio() == null ? 1.0 : other.getCoverageRatio());
        }
        Baseline baseline = Baseline.of(slotCounts, slotCoverages, rule.window().slotMode(), rule.window().bucket());
        AnomalyDetector.Result detector =
                AnomalyDetector.evaluate(observed, baseline, coverageRatio, coverageOk, rule.window());
        boolean lowConfidence = AnomalyDetector.lowConfidence(baseline, rule.window());
        // 116 (OBS-1): a downward anomaly is an absence-like conclusion and needs detectability too.
        ai.foresignal.observability.AbsenceCheck detect = null;
        if ("activity-down".equals(detector.classification())) {
            detect = detectable(rule, bucketStart, bucketEnd, true);
            if (!detect.ok()) {
                detector = lowDetectability(detector);
            }
        }

        AnomalyObservationEntity observation =
                new AnomalyObservationEntity(rule.spec().id(), group, bucketStart, bucketEnd, observed);
        observation.setExpected(detector.expected());
        observation.setExposure(detector.exposure());
        observation.setpValue(detector.pValue());
        observation.setScore(detector.score());
        observation.setDirection(detector.direction());
        observation.setClassification(detector.classification());
        observation.setLowConfidence(lowConfidence);
        observation.setBaseline(baseline.toMap());
        observation.setDetector(detectorMap(rule));
        Map<String, Object> evidence = evidence(result, group, fetchRanges);
        if (!coverage.ok() || result.capped()) {
            evidence.put("coverageReason", result.capped() ? capReason(result) : coverage.reason());
        } else if (detect != null && !detect.ok()) {
            evidence.put("coverageReason", ai.foresignal.observability.AbsenceCheck.LOW_DETECTABILITY);
            evidence.put("pDetect", detect.p());
        }
        observation.setEvidence(evidence);
        observation.setWatermarkSeq(watermark);
        observation.setManifestId(anomalyManifest(rule, group, bucketStart, bucketEnd, history, slot, watermark,
                detect));
        anomalies.save(observation);
        if (detector.fire()) {
            sample.setFlagged(true);
            samples.save(sample);
        }
        if (!bucketEnd.isBefore(now.minus(EMIT_RECENT))) {
            emitter.emit(anomalyRecord(rule, group, bucketStart, detector, baseline, lowConfidence, now));
        }
        if (!newest) {
            return; // catch-up and backfill fill the tables; only the newest bucket moves state or alerts
        }
        String desired = detector.fire() ? "active"
                : "learning".equals(detector.classification()) ? "learning"
                : "insufficient-coverage".equals(detector.classification())
                        || "coverage-drop".equals(detector.classification()) ? "unknown" : "clear";
        String reason = "learning".equals(desired) ? "learning: fewer than minWeeks of coverage-ok history"
                : detect != null && !detect.ok() ? ai.foresignal.observability.AbsenceCheck.LOW_DETECTABILITY
                : "unknown".equals(desired) ? detector.classification()
                        + (coverage.reason() == null ? "" : ": " + coverage.reason()) : null;
        Map<String, Object> context = anomalyContext(rule, detector, baseline, observation, evidence, watermark,
                lowConfidence);
        transition(rule, state(rule, group), desired, (double) observed, detector.expected(), reason, watermark, now,
                emitter, context, anomalyMessage(detector, baseline));
    }

    /**
     * A downward anomaly the sources could not have seen (prompt 116): {@code insufficient-coverage}, never an
     * alert; the state becomes {@code unknown} with reason {@code LOW_DETECTABILITY}.
     */
    static AnomalyDetector.Result lowDetectability(AnomalyDetector.Result detector) {
        return new AnomalyDetector.Result(detector.observed(), detector.expected(), detector.exposure(),
                detector.pValue(), detector.score(), "insufficient-coverage", null, false);
    }

    /** The component name of anomaly manifests. */
    public static final String ANOMALY_COMPONENT = AnomalyDetector.NAME;

    /** The input ref of an activity sample: {@code activity-sample:{ruleId}/{group}/{bucketStart}}. */
    public static String sampleRef(UUID ruleId, String group, Instant bucketStart) {
        return "activity-sample:" + ruleId + "/" + java.net.URLEncoder.encode(group, java.nio.charset.StandardCharsets.UTF_8)
                + "/" + bucketStart;
    }

    /**
     * The manifest of one anomaly evaluation (prompt 116): the detector and its parameters, the window spec,
     * the group and bucket, the detectability decision, and as inputs the bucket's own sample followed by the
     * slot samples the baseline used, in order. {@code ManifestReplay} re-runs the detector from exactly these.
     */
    private String anomalyManifest(WindowRule rule, String group, Instant bucketStart, Instant bucketEnd,
            List<ActivitySampleEntity> history, int slot, long watermark,
            ai.foresignal.observability.AbsenceCheck detect) {
        if (manifests == null) {
            return null;
        }
        List<String> refs = new ArrayList<>();
        refs.add(sampleRef(rule.spec().id(), group, bucketStart));
        for (ActivitySampleEntity other : history) {
            if (!other.getGroupKey().equals(group) || other.isFlagged() || !other.isCoverageOk()
                    || !other.getBucketStart().isBefore(bucketStart)
                    || slotOf(other.getBucketStart(), rule.window().slotMode()) != slot) {
                continue;
            }
            refs.add(sampleRef(rule.spec().id(), group, other.getBucketStart()));
        }
        Map<String, Object> config = new LinkedHashMap<>(detectorMap(rule));
        config.put("ruleId", rule.spec().id().toString());
        config.put("group", group);
        config.put("bucketStart", bucketStart.toString());
        config.put("bucketEnd", bucketEnd.toString());
        config.put("window", rule.window().toMap());
        Map<String, Object> detectability = new LinkedHashMap<>();
        detectability.put("checked", detect != null);
        detectability.put("ok", detect == null || detect.ok());
        detectability.put("p", detect == null ? null : detect.p());
        config.put("detectability", detectability);
        try {
            return manifests.record(ai.foresignal.manifest.ManifestRequest.of(ANOMALY_COMPONENT, AnomalyDetector.VERSION)
                    .config(config).registries("observability.json", "originators.json").asOf(watermark).inputs(refs));
        } catch (RuntimeException e) {
            log.warn("Could not record the anomaly manifest of rule {}: {}", rule.spec().id(), e.toString());
            return null;
        }
    }

    private Map<String, Object> detectorMap(WindowRule rule) {
        return Map.of("name", AnomalyDetector.NAME, "version", AnomalyDetector.VERSION,
                "params", Map.of("bucket", rule.window().bucket().toString(), "slotMode", rule.window().slotMode(),
                        "direction", rule.window().direction(), "sensitivity", rule.window().sensitivity(),
                        "alpha", rule.window().alpha(), "minAbsoluteDelta", rule.window().minAbsoluteDelta(),
                        "minWeeks", rule.window().minWeeks(), "coverageMin", rule.window().coverageMin()));
    }

    /** Up to 20 member references with their real seqs, and the {@code fetch_log} id range per source. */
    private static Map<String, Object> evidence(WindowMembership.Membership result, String group,
            Map<String, long[]> fetchRanges) {
        List<Map<String, Object>> refs = new ArrayList<>();
        for (WindowMembership.Member member : result.samples().getOrDefault(group, List.of())) {
            if (refs.size() >= WindowMembership.SAMPLES_PER_GROUP) {
                break;
            }
            refs.add(Map.of("source", member.sourceId(), "externalId", member.externalId(), "seq", member.seq()));
        }
        List<Map<String, Object>> ranges = new ArrayList<>();
        fetchRanges.forEach((sourceId, range) -> ranges.add(
                Map.of("source", sourceId, "fromId", range[0], "toId", range[1], "rows", range[2])));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("members", refs);
        out.put("fetchLog", ranges);
        return out;
    }

    /** {@code alert.context.anomaly}: the same fields as the stored {@code anomaly_observation}. */
    private static Map<String, Object> anomalyContext(WindowRule rule, AnomalyDetector.Result detector,
            Baseline baseline, AnomalyObservationEntity observation, Map<String, Object> evidence, long watermark,
            boolean lowConfidence) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("observationId", observation.getId());
        out.put("bucketStart", observation.getBucketStart().toString());
        out.put("bucketEnd", observation.getBucketEnd().toString());
        out.put("observed", detector.observed());
        out.put("expected", detector.expected());
        out.put("exposure", detector.exposure());
        if (Double.isFinite(detector.pValue())) {
            out.put("pValue", detector.pValue());
        }
        out.put("score", detector.score());
        if (detector.direction() != null) {
            out.put("direction", detector.direction());
        }
        out.put("classification", detector.classification());
        out.put("lowConfidence", lowConfidence);
        out.put("baseline", baseline.toMap());
        out.put("detector", Map.of("name", AnomalyDetector.NAME, "version", AnomalyDetector.VERSION));
        out.put("evidence", evidence);
        out.put("watermarkSeq", watermark);
        out.put("model", baseline.model());
        return out;
    }

    /** "Unusual activity: 42 observed vs ~11 expected (p≈2e-6 under a Poisson model)". */
    static String anomalyMessage(AnomalyDetector.Result detector, Baseline baseline) {
        String model = "nb".equals(baseline.model()) ? "negative binomial" : "Poisson";
        String p = Double.isFinite(detector.pValue()) ? "p≈" + scientific(detector.pValue()) : "p unavailable";
        return "Unusual activity: " + detector.observed() + " observed vs ~" + Math.round(detector.expected())
                + " expected (" + p + " under a " + model + " model)";
    }

    /** One significant digit in compact scientific form: 2e-6, 3e-1, 1e0. */
    static String scientific(double value) {
        if (value <= 0) {
            return "0";
        }
        int exponent = (int) Math.floor(Math.log10(value));
        long mantissa = Math.round(value / Math.pow(10, exponent));
        if (mantissa == 10) {
            mantissa = 1;
            exponent++;
        }
        return mantissa + "e" + exponent;
    }

    // ------------------------------------------------------------------ transitions and alerts

    private boolean due(WindowRule rule, Instant now) {
        Instant last = null;
        for (WindowStateEntity state : states.findByRuleIdOrderByGroupKey(rule.spec().id())) {
            if (state.getLastEvaluatedAt() != null && (last == null || state.getLastEvaluatedAt().isAfter(last))) {
                last = state.getLastEvaluatedAt();
            }
        }
        return last == null || !now.isBefore(last.plus(rule.window().step()));
    }

    private WindowStateEntity state(WindowRule rule, String group) {
        WindowStateEntity state = states.findByRuleIdAndGroupKey(rule.spec().id(), group);
        return state == null ? new WindowStateEntity(rule.spec().id(), group) : state;
    }

    private void transition(WindowRule rule, WindowStateEntity state, String desired, Double value, Double threshold,
            String reason, long watermark, Instant now, Emitter emitter, Map<String, Object> anomalyContext,
            String message) {
        String previous = state.getState();
        boolean first = state.getSince() == null;
        if (!desired.equals(previous) || first) {
            state.setSince(now);
        }
        if ("active".equals(desired) && !"active".equals(previous)) {
            // An alert is owed unless one is still open from an earlier active period (active → unknown →
            // active is the same episode). It is written once the state's version exists (firePending).
            if (state.getLastAlertId() == null && !state.isAlertPending()) {
                state.setAlertPending(true, now, anomalyContext == null ? null : contextJson(anomalyContext), message);
            }
        } else if ("clear".equals(desired)) {
            // Resolve on → clear. unknown and learning never resolve: an absence rule that loses
            // coverage must not auto-resolve (false all-clear).
            if (state.getLastAlertId() != null) {
                resolve(rule, state.getGroupKey(), now);
                state.setLastAlertId(null);
            }
            state.setAlertPending(false, null, null, null);
        }
        state.setState(desired);
        state.setLastValue(value);
        state.setLastEvaluatedAt(now);
        state.setLastWatermarkSeq(watermark);
        state.setReason(reason);
        states.save(state);
        emitter.emit(stateRecord(rule, state.getGroupKey(), desired, value, threshold, reason, anomalyContext, now));
    }

    private String contextJson(Map<String, Object> anomalyContext) {
        return json.writeValueAsString(Map.of("anomaly", anomalyContext));
    }

    /**
     * Writes the owed alerts of {@code rule}: each once the {@code rule-windows} version of its active
     * state exists (so {@code alert.seq} references it), or after {@link #PENDING_GRACE} without one.
     * A failed write stays owed and is retried on the next tick; cooldown or the hourly cap settles it
     * as suppressed.
     */
    void firePending(WindowRule rule, Instant now, long watermark) {
        for (WindowStateEntity state : states.findByRuleIdOrderByGroupKey(rule.spec().id())) {
            if (!state.isAlertPending()) {
                continue;
            }
            if (!"active".equals(state.getState())) {
                state.setAlertPending(false, null, null, null);
                states.save(state);
                continue;
            }
            String externalId = rule.spec().id() + ":" + state.getGroupKey();
            Map<String, Object> version = activeVersion(externalId);
            boolean overdue = state.getAlertPendingSince() == null
                    || !now.isBefore(state.getAlertPendingSince().plus(PENDING_GRACE));
            if (version == null && !overdue) {
                continue;
            }
            String suppressed = suppressed(rule, externalId, now);
            if (suppressed != null) {
                log.info("windowed rule {} group {}: alert suppressed ({})", rule.spec().id(), state.getGroupKey(),
                        suppressed);
                state.setAlertPending(false, null, null, null);
                states.save(state);
                continue;
            }
            try {
                long seq = version == null ? watermark : ((Number) version.get("seq")).longValue();
                int versionNo = version == null ? 1 : ((Number) version.get("version")).intValue();
                Instant observedAt = version == null || version.get("observed_at") == null ? now
                        : ((Timestamp) version.get("observed_at")).toInstant();
                if (version == null) {
                    log.warn("windowed rule {} group {}: no rule-windows version after {}; alert references the"
                            + " evaluation watermark", rule.spec().id(), state.getGroupKey(), PENDING_GRACE);
                }
                // The rule's own template wins, rendered; RuleSpec.template() is never null (it falls back to the
                // per-record default), so only an explicit messageTemplate may override the pending message.
                String explicit = rule.spec().messageTemplate();
                String message = explicit != null && !explicit.isBlank()
                        ? ai.foresignal.trigger.MessageTemplate.render(explicit, new ai.foresignal.trigger.MessageTemplate
                                .Context(rule.spec().name(), SOURCE_ID, externalId, STATE_KIND, null,
                                        rule.spec().severity().wire(), observedAt, null, null, null))
                        : state.getPendingMessage() != null ? state.getPendingMessage()
                        : "Windowed rule '" + rule.spec().name() + "' is active for group " + state.getGroupKey();
                MatchRow row = new MatchRow(seq, null, SOURCE_ID, externalId, STATE_KIND, versionNo,
                        ai.foresignal.ingest.PersistReason.CHANGE.code(), observedAt, null, null, null, false, null,
                        "{}", "enter");
                String context = state.getPendingContext();
                Long id = alerts.write(writer -> writer.insert(new AlertStore.NewAlert(rule.spec(), row, now, message,
                        null, null, context)).id());
                state.setLastAlertId(id);
                state.setAlertPending(false, null, null, null);
                states.save(state);
            } catch (RuntimeException e) {
                // Still owed: retried on the next tick.
                log.warn("windowed rule {} group {}: alert not written yet ({}); will retry", rule.spec().id(),
                        state.getGroupKey(), e.toString());
            }
        }
    }

    /** The head of the group's {@code rule-windows} record when its persisted state is active, or null. */
    private Map<String, Object> activeVersion(String externalId) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT h.seq, h.version, l.observed_at FROM record_head h"
                + " LEFT JOIN record_live l USING (source_id, external_id)"
                + " WHERE h.source_id = ? AND h.external_id = ? AND NOT h.tombstone AND h.payload->>'state' = 'active'",
                SOURCE_ID, externalId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Why an alert for {@code (rule, group)} must not be written now, or null: cooldown or the hourly cap. */
    private String suppressed(WindowRule rule, String externalId, Instant now) {
        Duration cooldown = rule.spec().cooldown();
        if (cooldown != null && !cooldown.isZero()) {
            List<Timestamp> last = jdbc.queryForList("SELECT max(fired_at) FROM alert WHERE rule_id = ?"
                    + " AND source_id = ? AND external_id = ? AND NOT suppressed AND NOT exercise", Timestamp.class,
                    rule.spec().id(), SOURCE_ID, externalId);
            if (!last.isEmpty() && last.get(0) != null && now.isBefore(last.get(0).toInstant().plus(cooldown))) {
                return "cooldown " + cooldown;
            }
        }
        Integer cap = rule.spec().maxAlertsPerHour();
        if (cap != null) {
            Integer fired = jdbc.queryForObject("SELECT count(*) FROM alert WHERE rule_id = ? AND source_id = ?"
                    + " AND external_id = ? AND NOT suppressed AND NOT exercise AND fired_at > ?", Integer.class, rule.spec().id(),
                    SOURCE_ID, externalId, Timestamp.from(now.minus(Duration.ofHours(1))));
            if (fired != null && fired >= cap) {
                return "maxAlertsPerHour " + cap;
            }
        }
        return null;
    }

    private void resolve(WindowRule rule, String group, Instant now) {
        try {
            String externalId = rule.spec().id() + ":" + group;
            alerts.write(writer -> writer.resolveOpen(rule.spec().id(), SOURCE_ID, externalId, Long.MAX_VALUE, "auto",
                    null, now));
        } catch (RuntimeException e) {
            log.warn("windowed rule {} could not resolve: {}", rule.spec().id(), e.toString());
        }
    }

    private NormalizedRecord stateRecord(WindowRule rule, String group, String state, Double value, Double threshold,
            String reason, Map<String, Object> anomalyContext, Instant observedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ruleId", rule.spec().id().toString());
        payload.put("name", rule.spec().name());
        payload.put("type", rule.type().wire());
        payload.put("groupKey", group);
        payload.put("state", state);
        if (value != null) {
            payload.put("value", value);
        }
        if (threshold != null) {
            payload.put("threshold", threshold);
        }
        if (reason != null) {
            payload.put("reason", reason);
        }
        if (anomalyContext != null) {
            payload.put("anomaly", anomalyContext);
        }
        payload.put("detector", Map.of("name", AnomalyDetector.NAME, "version", AnomalyDetector.VERSION));
        payload.put("automated", true);
        return NormalizedRecord.builder(rule.spec().id() + ":" + group, STATE_KIND)
                .observedAt(observedAt)
                .putAll(payload)
                .build();
    }

    /** The {@code anomaly} record for one evaluated bucket (prompt 100 §2). */
    private NormalizedRecord anomalyRecord(WindowRule rule, String group, Instant bucketStart,
            AnomalyDetector.Result detector, Baseline baseline, boolean lowConfidence, Instant observedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ruleId", rule.spec().id().toString());
        payload.put("name", rule.spec().name());
        payload.put("type", rule.type().wire());
        payload.put("groupKey", group);
        payload.put("bucketStart", bucketStart.toString());
        payload.put("observed", detector.observed());
        payload.put("expected", detector.expected());
        payload.put("exposure", detector.exposure());
        if (Double.isFinite(detector.pValue())) {
            payload.put("pValue", detector.pValue());
        }
        payload.put("score", detector.score());
        if (detector.direction() != null) {
            payload.put("direction", detector.direction());
        }
        payload.put("classification", detector.classification());
        payload.put("lowConfidence", lowConfidence);
        payload.put("baseline", baseline.toMap());
        payload.put("detector", Map.of("name", AnomalyDetector.NAME, "version", AnomalyDetector.VERSION));
        payload.put("automated", true);
        payload.put("notAWarning", true);
        return NormalizedRecord.builder(rule.spec().id() + ":" + group + ":" + bucketStart.getEpochSecond(),
                        ANOMALY_KIND)
                .observedAt(observedAt)
                .putAll(payload)
                .build();
    }

    // ------------------------------------------------------------------ cursor, forget, rebuild, baselines

    /** The setting key of a rule's durable anomaly cursor. */
    public static String cursorKey(UUID ruleId) {
        return SettingKeys.ANOMALY_CURSOR + "." + ruleId;
    }

    /** The rule's anomaly cursor, or null before its first bucket. */
    public AnomalyCursor cursor(UUID ruleId) {
        try {
            return settings.get(cursorKey(ruleId), AnomalyCursor.class).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void storeCursor(UUID ruleId, AnomalyCursor cursor) {
        settings.put(cursorKey(ruleId), cursor);
    }

    /** Removes the state, samples and observations of a deleted rule, and tombstones its records. */
    public void forget(UUID ruleId, Emitter emitter) {
        for (WindowStateEntity state : states.findByRuleIdOrderByGroupKey(ruleId)) {
            emitter.tombstone(STATE_KIND, ruleId + ":" + state.getGroupKey());
        }
        states.deleteByRuleId(ruleId);
        samples.deleteByRuleId(ruleId);
        anomalies.deleteByRuleId(ruleId);
        if (cursor(ruleId) != null) {
            storeCursor(ruleId, new AnomalyCursor(-1, 0));
        }
    }

    /**
     * The outcome of a baseline rebuild.
     *
     * @param samplesCleared samples removed
     * @param weeksAvailable weeks of retained history the backfill can read (at most 12)
     * @param backfillFrom the first bucket the backfill evaluates
     */
    public record Rebuild(long samplesCleared, double weeksAvailable, Instant backfillFrom) {}

    /**
     * Rebuilds an anomaly rule's baseline: removes its samples and observations and resets its cursor,
     * so the following ticks re-evaluate the last 12 weeks of retained history bucket by bucket (the
     * window state and any open alert are kept).
     *
     * @param ruleId the rule
     * @param now the current time
     * @return what was cleared and what the backfill will read
     */
    public Rebuild rebuild(UUID ruleId, Instant now) {
        TriggerRuleEntity entity = rules.findById(ruleId).orElseThrow();
        long cleared = samples.findByRuleIdOrderByBucketStartAsc(ruleId).size();
        samples.deleteByRuleId(ruleId);
        anomalies.deleteByRuleId(ruleId);
        storeCursor(ruleId, new AnomalyCursor(-1, 0));
        WindowSpec window = WindowSpec.fromStored(entity.getWindowSpec());
        Duration bucket = window == null ? Duration.ofHours(1) : window.bucket();
        Instant from = floor(now.minus(LOOKBACK), bucket);
        Instant earliest = earliestRetained(specs.compile(entity), now);
        double weeks = earliest == null ? 0
                : Math.min(LOOKBACK.toDays() / 7.0, Duration.between(earliest.isAfter(from) ? earliest : from, now)
                        .toHours() / (24.0 * 7));
        return new Rebuild(cleared, weeks, from);
    }

    /** The oldest retained version observation of the rule's sources (or its kinds' sources), or null. */
    private Instant earliestRetained(RuleSpec spec, Instant now) {
        Instant earliest = null;
        for (String sourceId : gate.involvedSources(spec.sources(), spec.kinds(), now.minus(LOOKBACK), now, true)) {
            List<Timestamp> rows = jdbc.queryForList("SELECT min(observed_at) FROM record_version WHERE source_id = ?",
                    Timestamp.class, sourceId);
            if (!rows.isEmpty() && rows.get(0) != null) {
                Instant at = rows.get(0).toInstant();
                earliest = earliest == null || at.isBefore(earliest) ? at : earliest;
            }
        }
        return earliest;
    }

    /** The window-state rows of a rule. */
    public List<WindowStateEntity> windowState(UUID ruleId) {
        return states.findByRuleIdOrderByGroupKey(ruleId);
    }

    /**
     * One slot's baseline for the API.
     *
     * @param groupKey the group
     * @param slot the hour-of-week (0–167) or hour-of-day (0–23) slot
     * @param baseline the slot's baseline over coverage-ok, unflagged samples of the lookback
     */
    public record SlotBaseline(String groupKey, int slot, Baseline baseline) {}

    /**
     * The per-slot baselines of an anomaly rule as the evaluator would use them now: coverage-ok,
     * unflagged samples of the last 12 weeks, grouped by the rule's own slot mode and bucket.
     *
     * @param ruleId the rule
     * @param groupKey one group, or null for every group
     * @param window the rule's window spec (slot mode and bucket)
     * @param now the current time
     * @return one entry per group and slot with samples, ordered by group then slot
     */
    public List<SlotBaseline> slotBaselines(UUID ruleId, String groupKey, WindowSpec window, Instant now) {
        String slotMode = window == null ? "hour-of-week" : window.slotMode();
        Duration bucket = window == null ? Duration.ofHours(1) : window.bucket();
        Map<String, Map<Integer, List<ActivitySampleEntity>>> bySlot = new TreeMap<>();
        for (ActivitySampleEntity sample : samples
                .findByRuleIdAndBucketStartGreaterThanEqualOrderByBucketStartAsc(ruleId, now.minus(LOOKBACK))) {
            if ((groupKey != null && !groupKey.equals(sample.getGroupKey())) || sample.isFlagged()
                    || !sample.isCoverageOk()) {
                continue;
            }
            bySlot.computeIfAbsent(sample.getGroupKey(), g -> new TreeMap<>())
                    .computeIfAbsent(slotOf(sample.getBucketStart(), slotMode), s -> new ArrayList<>()).add(sample);
        }
        List<SlotBaseline> out = new ArrayList<>();
        bySlot.forEach((group, slots) -> slots.forEach((slot, list) -> {
            List<Integer> counts = new ArrayList<>();
            List<Double> coverages = new ArrayList<>();
            for (ActivitySampleEntity sample : list) {
                counts.add(sample.getCount());
                coverages.add(sample.getCoverageRatio() == null ? 1.0 : sample.getCoverageRatio());
            }
            out.add(new SlotBaseline(group, slot, Baseline.of(counts, coverages, slotMode, bucket)));
        }));
        return out;
    }

    /** True when the rule has any sample at all (the {@code /baseline} 409 test). */
    public boolean hasSamples(UUID ruleId) {
        return !samples.findByRuleIdOrderByBucketStartAsc(ruleId).isEmpty();
    }

    static Instant floor(Instant at, Duration bucket) {
        long seconds = bucket.toSeconds();
        return Instant.ofEpochSecond(Math.floorDiv(at.getEpochSecond(), seconds) * seconds);
    }

    /** The slot of a bucket start: hour-of-week {@code 0–167} (Monday 00 UTC = 0) or hour-of-day {@code 0–23}. */
    static int slotOf(Instant at, String slotMode) {
        var zoned = at.atZone(ZoneOffset.UTC);
        if ("hour-of-day".equals(slotMode)) {
            return zoned.getHour();
        }
        return (zoned.getDayOfWeek().getValue() - 1) * 24 + zoned.getHour();
    }
}
