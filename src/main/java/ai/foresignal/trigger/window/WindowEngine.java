package ai.foresignal.trigger.window;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.MatchRow;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.RuleSpecs;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Evaluates the enabled windowed rules (prompt 100). Count and absence rules evaluate the current
 * window on each scheduler tick; anomaly rules fill completed buckets from a durable cursor. Every
 * result is a pure function of stored versions, live rows, {@code fetch_log} and the rule.
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

    /** Creates the engine. */
    public WindowEngine(TriggerRuleRepository rules, RuleSpecs specs, WindowMembership membership, CoverageGate gate,
            WindowStateRepository states, ActivitySampleRepository samples, AnomalyObservationRepository anomalies,
            AlertStore alerts, ChangeBus changeBus, ApplicationEventPublisher events, JsonMapper json) {
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
    }

    /** Where window-state and anomaly records go (the {@code rule-windows} {@code DerivedContext}). */
    public interface Emitter {

        /** Emits a record. */
        void emit(NormalizedRecord record);

        /** Tombstones a record. */
        void tombstone(String kind, String externalId);
    }

    /** A compiled windowed rule. */
    public record WindowRule(RuleSpec spec, RuleType type, WindowSpec window) {}

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
                out.add(new WindowRule(specs.compile(entity), type, window));
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
        Instant from = now.minus(rule.window().window());
        WindowMembership.Membership result = membership.resolve(rule.spec(), rule.window(), from, now, watermark);
        boolean eligible = due(rule, now);
        for (Map.Entry<String, Integer> entry : result.counts().entrySet()) {
            String group = entry.getKey();
            int value = entry.getValue();
            boolean active = ">=".equals(rule.window().op())
                    ? value >= rule.window().threshold()
                    : value <= rule.window().threshold();
            WindowStateEntity state = state(rule, group);
            if (eligible) {
                transition(rule, state, active ? "active" : "clear", (double) value,
                        (double) rule.window().threshold(), null, watermark, now, emitter);
            }
        }
        for (WindowStateEntity state : states.findByRuleIdOrderByGroupKey(rule.spec().id())) {
            if (result.counts().containsKey(state.getGroupKey()) || !eligible) {
                continue;
            }
            boolean active = ">=".equals(rule.window().op())
                    ? 0 >= rule.window().threshold()
                    : 0 <= rule.window().threshold();
            transition(rule, state, active ? "active" : "clear", 0.0, (double) rule.window().threshold(), null,
                    watermark, now, emitter);
        }
    }

    // ------------------------------------------------------------------ absence

    private void absence(WindowRule rule, Instant now, long watermark, Emitter emitter) {
        Instant from = now.minus(rule.window().window());
        String group = "*";
        WindowStateEntity state = state(rule, group);
        if (!due(rule, now)) {
            return;
        }
        CoverageGate.Gate gateResult =
                gate.evaluate(rule.spec().sources(), from, now, rule.window().window(), rule.window().coverageMin());
        if (!gateResult.ok()) {
            boolean firstUnknown = !"unknown".equals(state.getState());
            transition(rule, state, "unknown", null, null, gateResult.reason(), watermark, now, emitter);
            if (firstUnknown) {
                events.publishEvent(new SystemEvent(UNKNOWN_SUBTYPE, "warning",
                        "Absence rule cannot be evaluated",
                        "Rule '" + rule.spec().name() + "' (" + rule.spec().id() + ") cannot be evaluated: "
                                + gateResult.reason(),
                        "/v1/triggers/" + rule.spec().id() + "/window-state",
                        Map.of("ruleId", rule.spec().id().toString(), "reason", gateResult.reason())));
            }
            return;
        }
        WindowMembership.Membership result = membership.resolve(rule.spec(), rule.window(), from, now, watermark);
        int total = result.counts().values().stream().mapToInt(Integer::intValue).sum();
        transition(rule, state, total == 0 ? "active" : "clear", (double) total, 0.0, null, watermark, now, emitter);
    }

    // ------------------------------------------------------------------ anomaly

    private void anomaly(WindowRule rule, Instant now, long watermark, Emitter emitter) {
        Instant bucketStart = floor(now.minus(rule.window().bucket()), rule.window().bucket());
        Instant bucketEnd = bucketStart.plus(rule.window().bucket());
        WindowMembership.Membership result =
                membership.resolve(rule.spec(), rule.window(), bucketStart, bucketEnd, watermark);
        Map<String, Integer> counts = result.counts();
        java.util.Set<String> groups = new java.util.LinkedHashSet<>(counts.keySet());
        for (ActivitySampleEntity sample : samples.findByRuleIdOrderByBucketStartAsc(rule.spec().id())) {
            if (sample.getBucketStart().equals(bucketStart)) {
                groups.add(sample.getGroupKey());
            }
        }
        for (String group : groups) {
            evaluateBucket(rule, group, bucketStart, bucketEnd, counts.getOrDefault(group, 0), result, watermark, now,
                    emitter);
        }
    }

    private void evaluateBucket(WindowRule rule, String group, Instant bucketStart, Instant bucketEnd, int observed,
            WindowMembership.Membership result, long watermark, Instant now, Emitter emitter) {
        ActivitySampleEntity sample = samples.findById(
                new ActivitySampleEntity.Key(rule.spec().id(), group, bucketStart)).orElse(null);
        if (sample != null) {
            return; // already evaluated (the cursor filled it)
        }
        CoverageGate.Gate coverage =
                gate.evaluate(rule.spec().sources(), bucketStart, bucketEnd, rule.window().bucket(),
                        rule.window().coverageMin());
        boolean coverageOk = coverage.ok();
        // The real per-bucket ratio (prompt 100 §4): min over sources of (OK share) x (receivers factor).
        // This makes the coverage-drop classification reachable and expected = mean x ratio.
        double coverageRatio = gate.coverageRatio(rule.spec().sources(), bucketStart, bucketEnd);
        sample = new ActivitySampleEntity(rule.spec().id(), group, bucketStart, observed);
        sample.setCoverageOk(coverageOk);
        sample.setCoverageRatio(coverageRatio);
        samples.save(sample);

        List<Integer> slotCounts = new ArrayList<>();
        List<Double> slotCoverages = new ArrayList<>();
        int slot = slotOf(bucketStart, rule.window().slotMode());
        // The baseline looks back 12 weeks of coverage-ok samples (prompt 100 §4); "prune after 26
        // weeks" is a separate retention concern, not this lookback.
        Instant lookbackStart = bucketStart.minus(Duration.ofDays(84));
        for (ActivitySampleEntity other : samples.findByRuleIdAndGroupKeyOrderByBucketStartAsc(rule.spec().id(), group)) {
            if (other.getBucketStart().isBefore(lookbackStart) || other.isFlagged() || !other.isCoverageOk()
                    || other.getBucketStart().equals(bucketStart) || slotOf(other.getBucketStart(),
                            rule.window().slotMode()) != slot) {
                continue;
            }
            slotCounts.add(other.getCount());
            slotCoverages.add(other.getCoverageRatio() == null ? 1.0 : other.getCoverageRatio());
        }
        Baseline baseline = Baseline.of(slotCounts, slotCoverages, rule.window().slotMode(), rule.window().bucket());
        AnomalyDetector.Result detector =
                AnomalyDetector.evaluate(observed, baseline, coverageRatio, coverageOk, rule.window());
        AnomalyObservationEntity observation =
                new AnomalyObservationEntity(rule.spec().id(), group, bucketStart, bucketEnd, observed);
        observation.setExpected(detector.expected());
        observation.setExposure(detector.exposure());
        observation.setpValue(detector.pValue());
        observation.setScore(detector.score());
        observation.setDirection(detector.direction());
        observation.setClassification(detector.classification());
        boolean lowConfidence = "hour-of-day".equals(rule.window().slotMode()) && baseline.weeks() < 14;
        observation.setLowConfidence(lowConfidence);
        observation.setBaseline(baseline.toMap());
        observation.setDetector(Map.of("name", AnomalyDetector.NAME, "version", AnomalyDetector.VERSION,
                "params", Map.of("bucket", rule.window().bucket().toString(), "slotMode", rule.window().slotMode(),
                        "sensitivity", rule.window().sensitivity(), "minAbsoluteDelta", rule.window().minAbsoluteDelta(),
                        "minWeeks", rule.window().minWeeks())));
        observation.setEvidence(evidence(result, group, rule.spec().id()));
        observation.setWatermarkSeq(watermark);
        anomalies.save(observation);
        emitter.emit(anomalyRecord(rule, group, bucketStart, detector, baseline, lowConfidence, now));
        if (detector.fire()) {
            sample.setFlagged(true);
            samples.save(sample);
        }
        String desired = detector.fire() ? "active"
                : "learning".equals(detector.classification()) ? "learning"
                : "insufficient-coverage".equals(detector.classification())
                        || "coverage-drop".equals(detector.classification()) ? "unknown" : "clear";
        WindowStateEntity state = state(rule, group);
        transition(rule, state, desired, (double) observed, baseline.mean(), detector.classification(), watermark,
                now, emitter, anomalyContext(detector, baseline), observation.getId());
    }

    private Map<String, Object> evidence(WindowMembership.Membership result, String group, UUID ruleId) {
        List<WindowMembership.Member> members = result.samples().getOrDefault(group, List.of());
        List<Map<String, Object>> refs = new ArrayList<>();
        for (WindowMembership.Member member : members) {
            if (refs.size() >= 20) {
                break;
            }
            refs.add(Map.of("source", member.sourceId(), "externalId", member.externalId(), "seq", member.seq()));
        }
        return Map.of("members", refs);
    }

    private Map<String, Object> anomalyContext(AnomalyDetector.Result detector, Baseline baseline) {
        return Map.of("observed", detector.observed(), "expected", detector.expected(), "pValue", detector.pValue(),
                "score", detector.score(), "model", baseline.model());
    }

    // ------------------------------------------------------------------ transitions

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
            String reason, long watermark, Instant now, Emitter emitter) {
        transition(rule, state, desired, value, threshold, reason, watermark, now, emitter, null, null);
    }

    private void transition(WindowRule rule, WindowStateEntity state, String desired, Double value, Double threshold,
            String reason, long watermark, Instant now, Emitter emitter, Map<String, Object> anomalyContext,
            Long anomalyId) {
        String previous = state.getState();
        boolean first = state.getSince() == null;
        if (!desired.equals(previous)) {
            state.setSince(now);
            if ("active".equals(desired) && !"active".equals(previous)) {
                Long alertId = fire(rule, state.getGroupKey(), reason, now, anomalyContext);
                state.setLastAlertId(alertId);
            } else if ("active".equals(previous) && "clear".equals(desired)) {
                // Resolve only on active -> clear. active -> unknown/learning leaves the alert
                // firing: an absence rule that loses coverage must not auto-resolve (false all-clear).
                resolve(rule, state.getGroupKey(), now);
                state.setLastAlertId(null);
            }
        } else if (first) {
            state.setSince(now);
        }
        state.setState(desired);
        state.setLastValue(value);
        state.setLastEvaluatedAt(now);
        state.setLastWatermarkSeq(watermark);
        state.setReason(reason);
        states.save(state);
        emitter.emit(stateRecord(rule, state.getGroupKey(), desired, value, threshold, reason, anomalyContext, now));
    }

    private NormalizedRecord stateRecord(WindowRule rule, String group, String state, Double value, Double threshold,
            String reason, Map<String, Object> anomalyContext, Instant observedAt) {
        java.util.LinkedHashMap<String, Object> payload = new java.util.LinkedHashMap<>();
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
        java.util.LinkedHashMap<String, Object> payload = new java.util.LinkedHashMap<>();
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

    private Long fire(WindowRule rule, String group, String reason, Instant now, Map<String, Object> anomalyContext) {
        try {
            if (rule.spec().maxAlertsPerHour() != null
                    && alerts.firedSince(rule.spec().id(), now.minus(Duration.ofHours(1))).size()
                            >= rule.spec().maxAlertsPerHour()) {
                return null;
            }
            String message = rule.spec().template() != null ? rule.spec().template()
                    : "Windowed rule '" + rule.spec().name() + "' is active for group " + group
                            + (reason == null ? "" : " (" + reason + ")");
            String context = anomalyContext == null ? null : json.writeValueAsString(Map.of("anomaly", anomalyContext));
            String externalId = rule.spec().id() + ":" + group;
            long seq = changeBus.lastPublishedSeq();
            MatchRow row = new MatchRow(seq, null, SOURCE_ID, externalId, STATE_KIND, 1,
                    ai.foresignal.ingest.PersistReason.CHANGE.code(), now, null, null, null, false, null, "{}", "enter");
            return alerts.write(writer -> writer.insert(new AlertStore.NewAlert(rule.spec(), row, now, message, null,
                    null, context)).id());
        } catch (RuntimeException e) {
            log.warn("windowed rule {} could not fire: {}", rule.spec().id(), e.toString());
            return null;
        }
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

    /** Removes the state, samples and observations of a deleted rule, and tombstones its records. */
    public void forget(UUID ruleId, Emitter emitter) {
        for (WindowStateEntity state : states.findByRuleIdOrderByGroupKey(ruleId)) {
            emitter.tombstone(STATE_KIND, ruleId + ":" + state.getGroupKey());
        }
        states.deleteByRuleId(ruleId);
        samples.deleteByRuleId(ruleId);
        anomalies.deleteByRuleId(ruleId);
    }

    /** The window-state rows of a rule. */
    public List<WindowStateEntity> windowState(UUID ruleId) {
        return states.findByRuleIdOrderByGroupKey(ruleId);
    }

    /** The baselines of a rule, one per group, or a {@link NotFoundException}-free empty list. */
    public List<BaselineRow> baselines(UUID ruleId, String groupKey) {
        List<BaselineRow> out = new ArrayList<>();
        Map<String, List<ActivitySampleEntity>> byGroup = new java.util.LinkedHashMap<>();
        for (ActivitySampleEntity sample : samples.findByRuleIdOrderByBucketStartAsc(ruleId)) {
            if (groupKey == null || groupKey.equals(sample.getGroupKey())) {
                byGroup.computeIfAbsent(sample.getGroupKey(), k -> new ArrayList<>()).add(sample);
            }
        }
        for (Map.Entry<String, List<ActivitySampleEntity>> entry : byGroup.entrySet()) {
            List<Integer> counts = new ArrayList<>();
            List<Double> coverages = new ArrayList<>();
            for (ActivitySampleEntity sample : entry.getValue()) {
                counts.add(sample.getCount());
                coverages.add(sample.getCoverageRatio() == null ? 1.0 : sample.getCoverageRatio());
            }
            Baseline baseline = Baseline.of(counts, coverages, "hour-of-week", Duration.ofHours(1));
            out.add(new BaselineRow(entry.getKey(), baseline, entry.getValue().size()));
        }
        return out;
    }

    /** One group's baseline for the API. */
    public record BaselineRow(String groupKey, Baseline baseline, int samples) {}

    private static Instant floor(Instant at, Duration bucket) {
        long seconds = bucket.toSeconds();
        return Instant.ofEpochSecond(Math.floorDiv(at.getEpochSecond(), seconds) * seconds);
    }

    private static int slotOf(Instant at, String slotMode) {
        var zoned = at.atZone(ZoneOffset.UTC);
        if ("hour-of-day".equals(slotMode)) {
            return zoned.getHour();
        }
        return zoned.getDayOfWeek().getValue() * 24 + zoned.getHour();
    }
}
