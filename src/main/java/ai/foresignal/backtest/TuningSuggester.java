package ai.foresignal.backtest;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import ai.foresignal.store.TriggerCondition;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.trigger.TriggerRuleService;
import ai.foresignal.trigger.window.RuleType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Deterministic tuning suggestions (prompt 103 §6, version 1.0.0). Each suggestion carries an id, a
 * kind, a rationale, a ready {@code TriggerRuleInput} partial patch, an expected effect and caveats.
 * Suggestions are reported, never applied automatically; applying one is an ordinary rule update.
 *
 * <ul>
 *   <li><strong>add-cooldown</strong> when the rule produced duplicates.
 *   <li><strong>switch-to-enter</strong> when alerts flapped.
 *   <li><strong>raise-threshold / lower-threshold</strong>: for each numeric condition
 *       ({@code > >= < <=}), the candidate values are the labeled alerts' snapshot values at the
 *       condition's path; the suggester picks the stricter value that removes the most negatives
 *       while keeping at least {@value #KEEP_POSITIVES_PERCENT}% of the positives (ties go to the
 *       value closest to the current one). The patch is the rule's <em>whole</em> condition list
 *       with that one value changed, since a rule update replaces the list.
 *   <li><strong>shrink-geofence</strong>: when at least {@value #GEOFENCE_NEGATIVE_PERCENT}% of the
 *       negatives fall outside the convex hull of the positives buffered by 10% of the hull's
 *       larger extent, the patch is that buffered hull as the new geofence.
 *   <li><strong>lower-anomaly-sensitivity</strong>: an anomaly rule (prompt 100) with low precision.
 * </ul>
 *
 * <p>{@code expectedEffect} is the effect on the labeled alerts for the threshold and geofence
 * suggestions, else null; a measured effect comes from a backtest of the patched variant, submitted
 * when the caller asks for simulation.
 */
@Service
public class TuningSuggester {

    /** The suggester version. */
    public static final String VERSION = "1.0.0";

    /** The precision below which a lower anomaly sensitivity is suggested. */
    public static final double LOW_PRECISION = 0.5;

    /** The share of positives a threshold change must keep, percent. */
    public static final int KEEP_POSITIVES_PERCENT = 90;

    /** The share of negatives outside the buffered positive hull that suggests a smaller geofence, percent. */
    public static final int GEOFENCE_NEGATIVE_PERCENT = 70;

    /** The buffer of the positive hull, as a fraction of its larger extent. */
    public static final double HULL_BUFFER = 0.10;

    /** The fewest positives with a geometry for a geofence suggestion. */
    public static final int MIN_GEOFENCE_POSITIVES = 3;

    private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    private final AlertQualityService quality;
    private final TriggerRuleService rules;
    private final JsonMapper json;

    /** Creates the suggester. */
    public TuningSuggester(AlertQualityService quality, TriggerRuleService rules, JsonMapper json) {
        this.quality = quality;
        this.rules = rules;
        this.json = json;
    }

    /**
     * What the suggestions are computed from.
     *
     * @param report the {@link AlertQualityService#forRule} report
     * @param conditions the rule's conditions
     * @param hasGeofence true when the rule has an explicit geofence
     * @param anomaly true for an anomaly rule
     * @param window the anomaly rule's stored window specification, or null
     * @param labeled the labeled alerts
     */
    public record Inputs(Map<String, Object> report, List<TriggerCondition> conditions, boolean hasGeofence,
            boolean anomaly, Map<String, Object> window, List<AlertQualityService.Labeled> labeled) {}

    /** The suggestions for a rule since {@code since}. */
    public List<Map<String, Object>> forRule(UUID ruleId, Instant since) {
        Map<String, Object> report = quality.forRule(ruleId, since);
        TriggerRuleEntity rule = rules.load(ruleId);
        boolean anomaly;
        try {
            anomaly = RuleType.fromWire(rule.getType()) == RuleType.ANOMALY;
        } catch (IllegalArgumentException e) {
            anomaly = false;
        }
        return suggest(new Inputs(report, rule.getConditions() == null ? List.of() : rule.getConditions(),
                rule.getGeofence() != null, anomaly, rule.getWindowSpec(), quality.labeled(ruleId, since)), json);
    }

    /** The suggestions from {@code inputs}. Deterministic: the same inputs give the same list. */
    public static List<Map<String, Object>> suggest(Inputs inputs, JsonMapper json) {
        Map<String, Object> report = inputs.report();
        List<Map<String, Object>> suggestions = new ArrayList<>();
        long duplicates = number(report.get("duplicates"));
        long flapping = number(report.get("flapping"));
        Double precision = report.get("precision") instanceof Number p ? p.doubleValue() : null;
        if (duplicates > 0) {
            Object window = report.getOrDefault("duplicateWindow", "PT10M");
            suggestions.add(suggestion("add-cooldown", "add-cooldown",
                    "The rule produced " + duplicates + " duplicate alerts on the same entity within " + window + ".",
                    Map.of("cooldown", String.valueOf(window)), null));
        }
        if (flapping > 0) {
            suggestions.add(suggestion("switch-to-enter", "switch-to-enter",
                    flapping + " alerts resolved within 10 minutes (flapping); a single enter alert replaces each"
                            + " fire/clear pair.",
                    Map.of("mode", "enter", "autoResolve", Map.of("onInverseEdge", true)), null));
        }
        List<Labeled> labeled = parse(inputs.labeled(), json);
        for (int i = 0; i < inputs.conditions().size(); i++) {
            Map<String, Object> threshold = threshold(inputs.conditions(), i, labeled);
            if (threshold != null) {
                suggestions.add(threshold);
            }
        }
        if (inputs.hasGeofence()) {
            Map<String, Object> geofence = geofence(labeled);
            if (geofence != null) {
                suggestions.add(geofence);
            }
        }
        if (inputs.anomaly() && precision != null && precision < LOW_PRECISION && inputs.window() != null) {
            String current = String.valueOf(inputs.window().getOrDefault("sensitivity", "medium"));
            String lower = "high".equals(current) ? "medium" : "medium".equals(current) ? "low" : null;
            if (lower != null) {
                Map<String, Object> window = new LinkedHashMap<>(inputs.window());
                window.put("sensitivity", lower);
                suggestions.add(suggestion("lower-anomaly-sensitivity", "lower-anomaly-sensitivity",
                        "Precision is " + Math.round(precision * 100) + "% on labeled alerts; sensitivity " + current
                                + " → " + lower + ".",
                        Map.of("window", window), null));
            }
        }
        return suggestions;
    }

    /** A labeled alert with its parsed payload and point. */
    record Labeled(boolean positive, boolean negative, JsonNode payload, Point point) {}

    private static List<Labeled> parse(List<AlertQualityService.Labeled> raw, JsonMapper json) {
        List<Labeled> out = new ArrayList<>();
        GeoJsonReader reader = new GeoJsonReader();
        for (AlertQualityService.Labeled alert : raw == null ? List.<AlertQualityService.Labeled>of() : raw) {
            boolean positive = "positive".equals(alert.feedbackClass());
            boolean negative = "negative".equals(alert.feedbackClass());
            if ((!positive && !negative) || alert.recordSnapshot() == null) {
                continue;
            }
            try {
                JsonNode snapshot = json.readTree(alert.recordSnapshot());
                Point point = null;
                JsonNode geometry = snapshot.get("geometry");
                if (geometry != null && geometry.isObject()) {
                    Geometry parsed = reader.read(geometry);
                    point = parsed == null || parsed.isEmpty() ? null : parsed.getCentroid();
                }
                out.add(new Labeled(positive, negative, snapshot.get("payload"), point));
            } catch (RuntimeException e) {
                // An unreadable snapshot is skipped.
            }
        }
        return out;
    }

    /** The threshold suggestion for condition {@code index}, or null. */
    static Map<String, Object> threshold(List<TriggerCondition> conditions, int index, List<Labeled> labeled) {
        TriggerCondition condition = conditions.get(index);
        String op = condition.op();
        boolean above = ">".equals(op) || ">=".equals(op);
        boolean below = "<".equals(op) || "<=".equals(op);
        if ((!above && !below) || !(condition.value() instanceof Number) && !numeric(condition.value())) {
            return null;
        }
        double current = Double.parseDouble(String.valueOf(condition.value()));
        List<double[]> values = new ArrayList<>(); // [value, positive ? 1 : 0]
        int positives = 0;
        int negatives = 0;
        for (Labeled alert : labeled) {
            Double value = valueAt(alert.payload(), condition.path());
            if (value == null) {
                continue;
            }
            values.add(new double[] {value, alert.positive() ? 1 : 0});
            if (alert.positive()) {
                positives++;
            } else {
                negatives++;
            }
        }
        if (positives == 0 || negatives == 0) {
            return null;
        }
        TreeSet<Double> candidates = new TreeSet<>();
        for (double[] value : values) {
            if (above ? value[0] > current : value[0] < current) {
                candidates.add(value[0]);
            }
        }
        Double best = null;
        int bestRemoved = 0;
        int bestKept = 0;
        for (double candidate : candidates) {
            int keptPositives = 0;
            int removedNegatives = 0;
            for (double[] value : values) {
                boolean kept = keeps(op, value[0], candidate);
                if (value[1] == 1 && kept) {
                    keptPositives++;
                } else if (value[1] == 0 && !kept) {
                    removedNegatives++;
                }
            }
            if (keptPositives * 100 < KEEP_POSITIVES_PERCENT * positives) {
                continue;
            }
            boolean closer = best != null && Math.abs(candidate - current) < Math.abs(best - current);
            if (removedNegatives > bestRemoved || (removedNegatives == bestRemoved && removedNegatives > 0 && closer)) {
                best = candidate;
                bestRemoved = removedNegatives;
                bestKept = keptPositives;
            }
        }
        if (best == null || bestRemoved == 0) {
            return null;
        }
        List<Map<String, Object>> patched = new ArrayList<>();
        for (int i = 0; i < conditions.size(); i++) {
            TriggerCondition c = conditions.get(i);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("path", c.path());
            entry.put("op", c.op());
            entry.put("value", i == index ? plain(best) : c.value());
            patched.add(entry);
        }
        String kind = above ? "raise-threshold" : "lower-threshold";
        Map<String, Object> effect = new LinkedHashMap<>();
        effect.put("removedNegatives", bestRemoved);
        effect.put("negatives", negatives);
        effect.put("keptPositives", bestKept);
        effect.put("positives", positives);
        effect.put("basis", "labeled alerts");
        return suggestion(kind + ":" + condition.path(), kind,
                "Moving " + condition.path() + " " + op + " " + condition.value() + " to " + plain(best) + " removes "
                        + bestRemoved + " of " + negatives + " negative alerts and keeps " + bestKept + " of "
                        + positives + " positives.",
                Map.of("conditions", patched), effect);
    }

    /** True when {@code value op threshold} holds. */
    static boolean keeps(String op, double value, double threshold) {
        return switch (op) {
            case ">" -> value > threshold;
            case ">=" -> value >= threshold;
            case "<" -> value < threshold;
            case "<=" -> value <= threshold;
            default -> true;
        };
    }

    /** The shrink-geofence suggestion, or null. */
    static Map<String, Object> geofence(List<Labeled> labeled) {
        List<Coordinate> positives = new ArrayList<>();
        List<Point> negatives = new ArrayList<>();
        for (Labeled alert : labeled) {
            if (alert.point() == null) {
                continue;
            }
            if (alert.positive()) {
                positives.add(alert.point().getCoordinate());
            } else {
                negatives.add(alert.point());
            }
        }
        if (positives.size() < MIN_GEOFENCE_POSITIVES || negatives.isEmpty()) {
            return null;
        }
        Geometry hull = GEOMETRY.createMultiPointFromCoords(positives.toArray(new Coordinate[0])).convexHull();
        Envelope envelope = hull.getEnvelopeInternal();
        double extent = Math.max(envelope.getWidth(), envelope.getHeight());
        if (extent <= 0) {
            return null;
        }
        Geometry buffered = hull.buffer(extent * HULL_BUFFER);
        int outside = 0;
        for (Point negative : negatives) {
            if (!buffered.covers(negative)) {
                outside++;
            }
        }
        if (outside * 100 < GEOFENCE_NEGATIVE_PERCENT * negatives.size()) {
            return null;
        }
        Map<String, Object> effect = new LinkedHashMap<>();
        effect.put("removedNegatives", outside);
        effect.put("negatives", negatives.size());
        effect.put("keptPositives", positives.size());
        effect.put("positives", positives.size());
        effect.put("basis", "labeled alerts");
        return suggestion("shrink-geofence", "shrink-geofence",
                outside + " of " + negatives.size() + " negative alerts fall outside the area of the positives"
                        + " (convex hull buffered by 10%).",
                Map.of("geofence", new GeoJsonWriter().write(buffered)), effect);
    }

    /** The number at a dotted path of the payload, or null. */
    static Double valueAt(JsonNode payload, String path) {
        if (payload == null || path == null) {
            return null;
        }
        JsonNode node = payload;
        for (String part : path.split("\\.")) {
            node = node == null ? null : node.get(part);
        }
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asDouble();
        }
        return numeric(node.asString()) ? Double.parseDouble(node.asString()) : null;
    }

    private static boolean numeric(Object value) {
        if (value == null) {
            return false;
        }
        try {
            Double.parseDouble(String.valueOf(value));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** A threshold value without a trailing {@code .0}. */
    private static Object plain(double value) {
        BigDecimal decimal = BigDecimal.valueOf(value).stripTrailingZeros();
        return decimal.scale() <= 0 ? (Object) decimal.longValueExact() : (Object) decimal.doubleValue();
    }

    private static Map<String, Object> suggestion(String id, String kind, String rationale, Map<String, Object> patch,
            Map<String, Object> expectedEffect) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        out.put("kind", kind);
        out.put("rationale", rationale);
        out.put("patch", patch);
        out.put("expectedEffect", expectedEffect);
        Map<String, Object> caveat = new LinkedHashMap<>();
        caveat.put("code", BacktestCaveat.NOT_GROUND_TRUTH);
        caveat.put("text", "Deterministic suggestion (tuning " + VERSION + ") from labeled alerts; not applied"
                + " automatically. Backtest the patch before applying it.");
        out.put("caveats", List.of(caveat));
        return out;
    }

    private static long number(Object raw) {
        return raw instanceof Number n ? n.longValue() : 0;
    }
}
