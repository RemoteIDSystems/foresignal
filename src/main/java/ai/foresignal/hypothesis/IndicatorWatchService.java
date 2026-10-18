package ai.foresignal.hypothesis;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.analytics.finding.FindingEventBus;
import ai.foresignal.analytics.finding.FindingListener;
import ai.foresignal.analytics.finding.ForesignalAnalyticsGenerator;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.JsonValues;
import ai.foresignal.intel.ForesignalIntelGenerator;
import ai.foresignal.situation.SituationRecordPublisher;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.RuleValidator;
import ai.foresignal.trigger.TriggerRuleService;
import ai.foresignal.trigger.window.RuleType;
import ai.foresignal.trigger.window.WindowSpecValidator;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Indicators that watch for themselves (prompt 125 §2, Intelligence-Design §6.3).
 *
 * <ul>
 *   <li><b>Watch rules.</b> {@code rule}, {@code series}, {@code intelEvent} and {@code situation} signals each own one
 *       017 trigger rule ({@link #ruleSpec}), kept in sync: editing the indicator replaces the rule, retiring it
 *       disables the rule. The rule is tagged {@value #INDICATOR_TAG}, {@value #ORIGIN_TAG} and
 *       {@code ind-{indicator id}}; it routes to no channel unless a channel names the rule in its {@code ruleIds}
 *       (the notification router honors {@value #ORIGIN_TAG}). A {@code series} signal becomes a record rule on the
 *       series' latest value, a 100 count or anomaly rule on a rollup, or a rule on 119 {@code change-point} findings;
 *       an {@code intelEvent} or {@code situation} signal a rule on the {@code foresignal-intel} records. A signal's
 *       optional {@code emerging: {rule}} is a second, first-stage rule whose alert moves the indicator to
 *       {@code emerging}.</li>
 *   <li><b>Subscriptions.</b> {@code precursor} signals follow 120 {@code precursor-active} findings on the finding
 *       bus; {@code question} signals follow 122 resolutions ({@link HypothesisQuestionHook}).</li>
 *   <li><b>Observation.</b> An alert of a watch rule (or a matching finding) observes the indicator through
 *       {@link HypothesisService#observe}, which records the evidence and calls the updater.</li>
 * </ul>
 */
@Service
public class IndicatorWatchService implements AlertListener, FindingListener {

    private static final Logger log = LoggerFactory.getLogger(IndicatorWatchService.class);

    /** The tag every watch rule carries. */
    public static final String INDICATOR_TAG = "indicator";

    /**
     * The origin tag of watch rules. Prompt 125 names it {@code origin:hypothesis}, but a rule tag must match
     * {@code ^[a-z0-9][a-z0-9_-]{0,31}$} (no colon), as checkpoint 124 found for {@code origin-analyst}.
     */
    public static final String ORIGIN_TAG = "origin-hypothesis";

    /** The tag of a first-stage rule. */
    public static final String EMERGING_TAG = "stage-emerging";

    /** The per-indicator tag prefix ({@code ind-} plus the lower-case ULID: 30 characters). */
    public static final String ID_TAG_PREFIX = "ind-";

    private final TriggerRuleService rules;
    private final RuleValidator validator;
    private final IndicatorRepository indicators;
    private final AnalyticFindingRepository findings;
    private final AlertBus alerts;
    private final FindingEventBus findingBus;
    private final ObjectProvider<HypothesisService> service;
    private final JsonMapper json;
    private volatile Subscription alertSubscription;
    private volatile Subscription findingSubscription;

    /** Creates the service. */
    public IndicatorWatchService(TriggerRuleService rules, RuleValidator validator, IndicatorRepository indicators,
            AnalyticFindingRepository findings, AlertBus alerts, FindingEventBus findingBus,
            ObjectProvider<HypothesisService> service, JsonMapper json) {
        this.rules = rules;
        this.validator = validator;
        this.indicators = indicators;
        this.findings = findings;
        this.alerts = alerts;
        this.findingBus = findingBus;
        this.service = service;
        this.json = json;
    }

    /** Subscribes to the alert and finding buses. */
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void start() {
        if (alertSubscription == null) {
            alertSubscription = alerts.subscribe("hypothesis-indicators", this, AlertBus.DEFAULT_QUEUE);
        }
        if (findingSubscription == null) {
            findingSubscription = findingBus.subscribe("hypothesis-indicators", this, FindingEventBus.DEFAULT_QUEUE);
        }
    }

    /** Unsubscribes. */
    @PreDestroy
    public synchronized void stop() {
        if (alertSubscription != null) {
            alertSubscription.close();
            alertSubscription = null;
        }
        if (findingSubscription != null) {
            findingSubscription.close();
            findingSubscription = null;
        }
    }

    // ------------------------------------------------------------------------- rule specs

    /** The tags of an indicator's watch rule. */
    public static List<String> tags(String indicatorId) {
        return List.of(INDICATOR_TAG, ORIGIN_TAG, ID_TAG_PREFIX + indicatorId.toLowerCase(Locale.ROOT));
    }

    /**
     * The {@code TriggerRuleInput} of an indicator's watch rule, or null when its signal owns none.
     *
     * @param indicatorId the indicator id
     * @param description the description (the rule name)
     * @param signal the signal
     * @throws ValidationException 400 {@code INDICATOR_INVALID} when the signal cannot be watched by a rule
     */
    public static Map<String, Object> ruleSpec(String indicatorId, String description, Map<String, Object> signal) {
        String type = JsonValues.text(signal, "type");
        if (type == null || !HypothesisVocabulary.WATCHED_SIGNALS.contains(type)) {
            return null;
        }
        Map<String, Object> spec = switch (type) {
            case "rule" -> new LinkedHashMap<>(HypothesisInputs.asMap(signal.get("rule")));
            case "series" -> seriesSpec(HypothesisInputs.asMap(signal.get("series")));
            case "intelEvent" -> intelEventSpec(HypothesisInputs.asMap(signal.get("intelEvent")));
            default -> situationSpec(HypothesisInputs.asMap(signal.get("situation")));
        };
        return finish(spec, indicatorId, "Indicator: " + description, false);
    }

    /** The first-stage rule of a two-stage signal ({@code emerging: {rule}}), or null. */
    public static Map<String, Object> emergingSpec(String indicatorId, String description,
            Map<String, Object> signal) {
        Map<String, Object> emerging = JsonValues.child(signal, "emerging");
        Map<String, Object> rule = emerging == null ? null : JsonValues.child(emerging, "rule");
        if (rule == null) {
            return null;
        }
        return finish(new LinkedHashMap<>(rule), indicatorId, "Indicator (emerging): " + description, true);
    }

    private static Map<String, Object> finish(Map<String, Object> spec, String indicatorId, String name,
            boolean emerging) {
        spec.put("name", name.length() > RuleValidator.MAX_NAME ? name.substring(0, RuleValidator.MAX_NAME) : name);
        List<String> tags = new ArrayList<>(tags(indicatorId));
        if (emerging) {
            tags.add(EMERGING_TAG);
        }
        if (spec.get("tags") instanceof List<?> existing) {
            for (Object tag : existing) {
                String value = String.valueOf(tag);
                if (!tags.contains(value) && tags.size() < RuleValidator.MAX_TAGS) {
                    tags.add(value);
                }
            }
        }
        spec.put("tags", tags);
        spec.put("enabled", true);
        spec.putIfAbsent("severity", "info");
        return spec;
    }

    private static Map<String, Object> seriesSpec(Map<String, Object> series) {
        String refText = JsonValues.text(series, "ref");
        SeriesRef ref;
        try {
            ref = SeriesRef.parse(refText);
        } catch (ValidationException e) {
            throw invalid("signal.series.ref", e.getMessage());
        }
        Map<String, Object> spec = new LinkedHashMap<>();
        if (Boolean.TRUE.equals(series.get("changePoint"))) {
            spec.put("sources", List.of(ForesignalAnalyticsGenerator.ID));
            spec.put("kinds", List.of(ForesignalAnalyticsGenerator.KIND));
            spec.put("conditions", List.of(condition("findingType", "==", "change-point")));
            return spec;
        }
        Map<String, Object> condition = JsonValues.child(series, "condition");
        String direction = JsonValues.text(series, "direction");
        switch (ref.type()) {
            case SERIES -> {
                if (condition == null) {
                    throw invalid("signal.series", "a series: reference is watched through a condition or changePoint");
                }
                spec.put("sources", List.of(ref.sourceId()));
                spec.put("kinds", List.of(SeriesPoint.LATEST_KIND));
                spec.put("mode", "enter");
                spec.put("conditions", List.of(condition("series", "==", ref.seriesId()),
                        condition("value", operator(JsonValues.text(condition, "op")),
                                JsonValues.number(condition, "value"))));
            }
            case ROLLUP -> {
                if (!SeriesRef.ALL_SOURCES.equals(ref.sourceId())) {
                    spec.put("sources", List.of(ref.sourceId()));
                }
                spec.put("kinds", List.of(ref.kind()));
                if (ref.region() != null && !"ZZ".equals(ref.region())) {
                    spec.put("geofenceRef", geofenceRef(List.of(ref.region())));
                }
                String length = "hour".equals(ref.resolution()) ? "1h" : "1d";
                Map<String, Object> window = new LinkedHashMap<>();
                if (condition != null) {
                    spec.put("type", "count");
                    double v = JsonValues.number(condition, "value");
                    String op = JsonValues.text(condition, "op");
                    window.put("window", length);
                    window.put("op", "gt".equals(op) || "gte".equals(op) ? ">=" : "<=");
                    long threshold = switch (op) {
                        case "gt" -> (long) Math.floor(v) + 1;
                        case "gte" -> (long) Math.ceil(v);
                        case "lt" -> Math.max(0, (long) Math.ceil(v) - 1);
                        default -> (long) Math.floor(v);
                    };
                    window.put("threshold", Math.max(0, threshold));
                    window.put("distinctBy", "externalId");
                } else {
                    spec.put("type", "anomaly");
                    window.put("window", length);
                    window.put("bucket", "1h");
                    window.put("direction", "any".equals(direction) || direction == null ? "both" : direction);
                }
                spec.put("window", window);
            }
            default -> throw invalid("signal.series", "a rule: or rollup:news/ reference is watched through"
                    + " changePoint: true");
        }
        return spec;
    }

    private static Map<String, Object> intelEventSpec(Map<String, Object> event) {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("sources", List.of(ForesignalIntelGenerator.ID));
        spec.put("kinds", List.of(ForesignalIntelGenerator.KIND));
        spec.put("mode", "enter");
        spec.put("conditions", List.of(condition("type", "==", JsonValues.text(event, "type"))));
        List<String> regions = JsonValues.strings(event, "regionCodes");
        String placeId = JsonValues.text(event, "placeId");
        String bbox = JsonValues.text(event, "bbox");
        if (!regions.isEmpty()) {
            spec.put("geofenceRef", geofenceRef(regions));
        } else if (placeId != null) {
            spec.put("near", Map.of("placeId", placeId));
        } else if (bbox != null) {
            double[] b = HypothesisInputs.bbox(bbox);
            if (b == null) {
                throw invalid("signal.intelEvent.bbox", "must be minLon,minLat,maxLon,maxLat");
            }
            spec.put("geofence", Map.of("type", "Polygon", "coordinates", List.of(List.of(List.of(b[0], b[1]),
                    List.of(b[2], b[1]), List.of(b[2], b[3]), List.of(b[0], b[3]), List.of(b[0], b[1])))));
        }
        return spec;
    }

    private static Map<String, Object> situationSpec(Map<String, Object> situation) {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("sources", List.of(SituationRecordPublisher.SOURCE_ID));
        spec.put("kinds", List.of(SituationRecordPublisher.KIND));
        spec.put("mode", "enter");
        List<Map<String, Object>> conditions = new ArrayList<>();
        String status = JsonValues.text(situation, "status");
        if (status != null) {
            conditions.add(condition("status", "==", status));
        }
        String minSeverity = JsonValues.text(situation, "minSeverity");
        if (minSeverity != null) {
            List<String> levels = List.of("info", "warning", "critical");
            conditions.add(condition("severity", "in", levels.subList(Math.max(0, levels.indexOf(minSeverity)),
                    levels.size())));
        }
        spec.put("conditions", conditions);
        return spec;
    }

    private static Map<String, Object> geofenceRef(List<String> regions) {
        List<String> countries = new ArrayList<>();
        List<String> admin1 = new ArrayList<>();
        for (String code : regions) {
            if (code.contains("-")) {
                admin1.add(code);
            } else {
                countries.add(code);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        if (!countries.isEmpty()) {
            out.put("country", countries);
        }
        if (!admin1.isEmpty()) {
            out.put("admin1", admin1);
        }
        return out;
    }

    private static Map<String, Object> condition(String path, String op, Object value) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("path", path);
        out.put("op", op);
        out.put("value", value);
        return out;
    }

    private static String operator(String op) {
        return switch (op == null ? "" : op) {
            case "gt" -> ">";
            case "gte" -> ">=";
            case "lt" -> "<";
            default -> "<=";
        };
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.INDICATOR_INVALID, "The indicator's signal cannot be watched.",
                List.of(new FieldError(field, message)));
    }

    /**
     * Validates a watch-rule spec with the 017 rule validator and the 100 window validator.
     *
     * @throws ValidationException 400 {@code INDICATOR_INVALID} with the validators' field errors under
     *     {@code signal.rule}
     */
    public void validate(Map<String, Object> spec) {
        if (spec == null) {
            return;
        }
        JsonNode node = json.valueToTree(spec);
        try {
            validator.validate(node);
            RuleType type = WindowSpecValidator.type(node);
            if (type != RuleType.RECORD) {
                WindowSpecValidator.validate(type, node);
            }
        } catch (ValidationException e) {
            List<FieldError> errors = new ArrayList<>();
            for (FieldError error : e.getErrors()) {
                errors.add(new FieldError("signal.rule." + error.field(), error.message()));
            }
            if (errors.isEmpty()) {
                errors.add(new FieldError("signal.rule", e.getMessage()));
            }
            throw new ValidationException(ErrorCodes.INDICATOR_INVALID, "The indicator's watch rule is not valid.",
                    errors);
        } catch (ForesignalException e) {
            throw invalid("signal.rule", e.getMessage());
        }
    }

    /** Validates an indicator's signal by building and validating its watch rules (drafts and creation). */
    public void validateSignal(String indicatorId, String description, Map<String, Object> signal) {
        validate(ruleSpec(indicatorId, description, signal));
        validate(emergingSpec(indicatorId, description, signal));
    }

    // ------------------------------------------------------------------------- sync

    /**
     * Creates, replaces or disables the indicator's watch rules to match it: a retired indicator's rules are disabled
     * (kept, with their alerts), a changed signal replaces them, and a rule the new signal no longer needs is deleted
     * (its alerts are kept).
     */
    public void sync(IndicatorEntity indicator) {
        boolean retired = HypothesisVocabulary.RETIRED.equals(indicator.getState());
        Map<String, Object> spec = ruleSpec(indicator.getId(), indicator.getDescription(), indicator.getSignal());
        indicator.setWatchRuleId(apply(indicator.getWatchRuleId(), spec, retired));
        Map<String, Object> emerging = emergingSpec(indicator.getId(), indicator.getDescription(),
                indicator.getSignal());
        indicator.setEmergingRuleId(apply(indicator.getEmergingRuleId(), emerging, retired));
    }

    /** Disables an indicator's watch rules (kept with their alerts) whatever its state; a final state needs no rule. */
    public void disable(IndicatorEntity indicator) {
        if (indicator.getWatchRuleId() != null) {
            indicator.setWatchRuleId(apply(indicator.getWatchRuleId(), ruleSpec(indicator.getId(),
                    indicator.getDescription(), indicator.getSignal()), true));
        }
        if (indicator.getEmergingRuleId() != null) {
            indicator.setEmergingRuleId(apply(indicator.getEmergingRuleId(), emergingSpec(indicator.getId(),
                    indicator.getDescription(), indicator.getSignal()), true));
        }
    }

    private UUID apply(UUID ruleId, Map<String, Object> spec, boolean disable) {
        if (spec == null) {
            if (ruleId != null) {
                remove(ruleId);
            }
            return null;
        }
        Map<String, Object> body = new LinkedHashMap<>(spec);
        if (disable) {
            body.put("enabled", false);
            if (ruleId == null) {
                return null;
            }
            try {
                rules.update(ruleId, json.valueToTree(body));
                return ruleId;
            } catch (NotFoundException e) {
                return null;
            } catch (ValidationException e) {
                // The rule no longer validates (a source was removed, say): it cannot be kept disabled, so remove it.
                log.info("Watch rule {} no longer validates ({}); removing it", ruleId, e.getMessage());
                remove(ruleId);
                return null;
            }
        }
        validate(body);
        if (ruleId != null) {
            try {
                rules.update(ruleId, json.valueToTree(body));
                return ruleId;
            } catch (NotFoundException e) {
                log.info("Watch rule {} is gone; creating a new one", ruleId);
            }
        }
        return rules.create(json.valueToTree(body)).rule().getId();
    }

    private void remove(UUID ruleId) {
        try {
            rules.delete(ruleId);
        } catch (NotFoundException e) {
            // Already gone.
        }
    }

    // ------------------------------------------------------------------------- intake

    /**
     * A bus dropped events while this listener lagged. Nothing is replayed: a missed alert leaves its indicator
     * waiting, and the indicator's {@code expected_by} judgment still applies.
     */
    @Override
    public void onGap() {
        log.warn("The indicator watch missed alert or finding events (a bus queue overflowed)");
    }

    @Override
    public void onAlert(AlertEvent event) {
        if (!AlertEvent.FIRED.equals(event.type()) || event.alert().exercise() || event.alert().ruleId() == null
                || !event.alert().tags().contains(ORIGIN_TAG)) {
            return;
        }
        AlertView alert = event.alert();
        for (IndicatorEntity indicator : indicators.findByWatchRuleIdOrEmergingRuleId(alert.ruleId(),
                alert.ruleId())) {
            if (!indicator.waiting() || !matches(indicator, alert)) {
                continue;
            }
            boolean emerging = alert.ruleId().equals(indicator.getEmergingRuleId())
                    && !alert.ruleId().equals(indicator.getWatchRuleId());
            if (emerging && !HypothesisVocabulary.PENDING.equals(indicator.getState())) {
                continue;
            }
            Map<String, Object> evidence = new LinkedHashMap<>();
            evidence.put("source", "alert");
            evidence.put("alertId", alert.id());
            evidence.put("ruleId", alert.ruleId().toString());
            evidence.put("sourceId", alert.sourceId());
            evidence.put("externalId", alert.externalId());
            evidence.put("seq", alert.seq());
            evidence.put("ref", "record:" + alert.sourceId() + "/" + encode(alert.externalId()) + "@" + alert.seq());
            evidence.put("at", alert.firedAt() == null ? null : alert.firedAt().toString());
            try {
                service.getObject().observe(indicator.getId(), evidence,
                        emerging ? HypothesisVocabulary.EMERGING : HypothesisVocabulary.OBSERVED);
            } catch (RuntimeException e) {
                log.warn("Indicator {} could not record alert {}: {}", indicator.getId(), alert.id(), e.toString());
            }
        }
    }

    /**
     * True when an alert of the indicator's rule is about what the indicator watches: a situation signal's alert must
     * be about its situation, a change-point signal's finding must name its series.
     */
    boolean matches(IndicatorEntity indicator, AlertView alert) {
        String type = indicator.signalType();
        if ("situation".equals(type)) {
            Map<String, Object> situation = JsonValues.child(indicator.getSignal(), "situation");
            return situation != null && java.util.Objects.equals(JsonValues.text(situation, "situationId"),
                    alert.externalId());
        }
        if ("series".equals(type)) {
            Map<String, Object> series = JsonValues.child(indicator.getSignal(), "series");
            if (series != null && Boolean.TRUE.equals(series.get("changePoint"))) {
                String ref = JsonValues.text(series, "ref");
                AnalyticFindingEntity finding = findings.findById(alert.externalId()).orElse(null);
                return finding != null && finding.getSeriesRefs().contains(ref);
            }
        }
        return true;
    }

    @Override
    public void onEvent(FindingEvent event) {
        if (!FindingEvent.CREATED.equals(event.event()) || event.finding() == null
                || !"precursor-active".equals(String.valueOf(event.finding().get("findingType")))) {
            return;
        }
        AnalyticFindingEntity finding = findings.findById(event.findingId()).orElse(null);
        if (finding == null || finding.getPrecursorId() == null) {
            return;
        }
        for (IndicatorEntity indicator : indicators.findByStateIn(HypothesisVocabulary.WAITING_STATES)) {
            if (!"precursor".equals(indicator.signalType())
                    || !finding.getPrecursorId().equals(JsonValues.text(indicator.getSignal(), "precursorId"))) {
                continue;
            }
            Map<String, Object> evidence = new LinkedHashMap<>();
            evidence.put("source", "finding");
            evidence.put("findingId", finding.getId());
            evidence.put("precursorId", finding.getPrecursorId());
            evidence.put("ref", "finding:" + finding.getId());
            evidence.put("at", finding.getDetectedAt() == null ? null : finding.getDetectedAt().toString());
            try {
                service.getObject().observe(indicator.getId(), evidence, HypothesisVocabulary.OBSERVED);
            } catch (RuntimeException e) {
                log.warn("Indicator {} could not record finding {}: {}", indicator.getId(), finding.getId(),
                        e.toString());
            }
        }
    }

    /**
     * A 122 question resolved: waiting {@code question} signals on it are observed when the outcome is theirs and
     * absent (with full detectability: the resolution is certain) otherwise.
     */
    public void questionResolved(String questionId, String outcome) {
        for (IndicatorEntity indicator : indicators.findByStateIn(HypothesisVocabulary.WAITING_STATES)) {
            Map<String, Object> question = JsonValues.child(indicator.getSignal(), "question");
            if (!"question".equals(indicator.signalType()) || question == null
                    || !questionId.equals(JsonValues.text(question, "questionId"))) {
                continue;
            }
            try {
                if (java.util.Objects.equals(outcome, JsonValues.text(question, "outcome"))) {
                    Map<String, Object> evidence = new LinkedHashMap<>();
                    evidence.put("source", "question");
                    evidence.put("questionId", questionId);
                    evidence.put("outcome", outcome);
                    evidence.put("ref", "question:" + questionId);
                    service.getObject().observe(indicator.getId(), evidence, HypothesisVocabulary.OBSERVED);
                } else {
                    service.getObject().absent(indicator.getId(), 1.0, "question:" + questionId);
                }
            } catch (RuntimeException e) {
                log.warn("Indicator {} could not record the resolution of {}: {}", indicator.getId(), questionId,
                        e.toString());
            }
        }
    }

    private static String encode(String externalId) {
        return externalId == null ? "" : java.net.URLEncoder.encode(externalId,
                java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
