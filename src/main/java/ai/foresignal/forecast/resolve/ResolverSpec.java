package ai.foresignal.forecast.resolve;

import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.JsonValues;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.security.Ulids;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The machine resolvers' shapes and validators (prompt 122 §3): {@code event-exists}, {@code count-threshold},
 * {@code series-value}, {@code alert-fired}, {@code situation-status}, {@code hypothesis-outcome} and {@code manual}.
 * Each resolver is pure and versioned ({@value #VERSION}, set by the server); this class checks its members and the
 * question types it can answer, and returns a normalized copy with the version.
 *
 * <ul>
 *   <li>{@code event-exists {selector, area?, window}} (binary): yes when the selector finds an occurrence in the
 *       area and window.</li>
 *   <li>{@code count-threshold {selector | ref, area?, window, op?, threshold?}}: the count of occurrences, or the
 *       sum of a {@code rollup:} series, compared with the threshold (binary); without {@code op} and
 *       {@code threshold} the count itself answers a numeric question (prompt 126; a {@code referenceFamily}
 *       selector then counts the events of the family's reference history, not the weeks with events).</li>
 *   <li>{@code series-value {ref, period | window, aggregate, op?, threshold?}}: binary with a threshold, numeric
 *       without.</li>
 *   <li>{@code alert-fired {ruleId, window, area?}} (binary).</li>
 *   <li>{@code situation-status {situationId, status? , minSeverity?}} (binary) at {@code resolve_by}.</li>
 *   <li>{@code hypothesis-outcome {setId}} (binary or multiple): resolved by the operator's confirmation (125).</li>
 *   <li>{@code manual} (any type): resolved through {@code resolveForecastQuestion}.</li>
 * </ul>
 *
 * A geometric area ({@code bbox}, {@code polygon}, {@code placeId}) needs located occurrences, so it is accepted only
 * with the alert, intel-event, situation, change-point and unexplained-move selector forms.
 */
public final class ResolverSpec {

    /** The resolver version. */
    public static final String VERSION = "1.0.0";

    /** The selector forms an event resolver accepts (the inline {@code rule} form is refused). */
    public static final List<String> SELECTOR_FORMS = List.of("alertRuleId", "alertRuleTag", "intelEventType",
            "anomalyRuleId", "situationActivated", "changePoint", "unexplainedMove", "referenceFamily", "timestamps");

    /** The selector forms whose occurrences have a geometry. */
    public static final Set<String> LOCATED_FORMS = Set.of("alertRuleId", "alertRuleTag", "intelEventType",
            "situationActivated", "changePoint", "unexplainedMove");

    /** The comparison operators. */
    public static final List<String> OPS = List.of("gt", "gte", "lt", "lte", "eq");

    /** The series aggregates. */
    public static final List<String> AGGREGATES = List.of("last", "mean", "max", "sum");

    /** The situation statuses. */
    public static final List<String> SITUATION_STATUSES = List.of("emerging", "active", "dormant", "closed");

    /** The severities, lowest first. */
    public static final List<String> SEVERITIES = List.of("info", "warning", "critical");

    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    private static final Pattern MONTH = Pattern.compile("^(\\d{4})-(\\d{2})$");

    private static final Pattern WEEK = Pattern.compile("^(\\d{4})-W(\\d{2})$");

    private static final Pattern DAY = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})$");

    private static final Pattern YEAR = Pattern.compile("^(\\d{4})$");

    private static final Map<String, Set<String>> FIELDS = Map.of(
            "event-exists", Set.of("type", "selector", "area", "window", "version"),
            "count-threshold", Set.of("type", "selector", "ref", "area", "window", "op", "threshold", "version"),
            "series-value", Set.of("type", "ref", "period", "window", "aggregate", "op", "threshold", "version"),
            "alert-fired", Set.of("type", "ruleId", "window", "area", "version"),
            "situation-status", Set.of("type", "situationId", "status", "minSeverity", "version"),
            "hypothesis-outcome", Set.of("type", "setId", "version"),
            "manual", Set.of("type", "version"));

    private ResolverSpec() {}

    /**
     * Validates a resolver for a question type and returns its normalized copy with {@code version}.
     *
     * @param resolver the resolver as sent
     * @param questionType the question's type, or null when unknown
     * @throws ValidationException 400 {@code RESOLVER_INVALID}
     */
    public static Map<String, Object> validate(Map<String, Object> resolver, String questionType) {
        if (resolver == null) {
            throw invalid("resolver", "is required");
        }
        String type = JsonValues.text(resolver, "type");
        if (type == null || !Ledger.RESOLVER_TYPES.contains(type)) {
            throw invalid("resolver.type", "must be one of " + Ledger.RESOLVER_TYPES);
        }
        for (String field : resolver.keySet()) {
            if (!FIELDS.get(type).contains(field)) {
                throw invalid("resolver." + field, "is not a member of a " + type + " resolver");
            }
        }
        Map<String, Object> out = new LinkedHashMap<>(resolver);
        out.put("version", VERSION);
        switch (type) {
            case "event-exists" -> {
                requireType(questionType, type, Ledger.BINARY);
                Map<String, Object> selector = selector(resolver.get("selector"), true);
                area(resolver.get("area"), selector);
                window(resolver.get("window"), true);
            }
            case "count-threshold" -> {
                boolean hasOp = resolver.get("op") != null;
                boolean hasThreshold = resolver.get("threshold") != null;
                if (hasOp != hasThreshold) {
                    throw invalid("resolver.threshold", "op and threshold go together");
                }
                // Prompt 126: without op and threshold the resolver answers a numeric question with the count itself.
                requireType(questionType, hasOp ? type : type + " without a threshold",
                        hasOp ? Ledger.BINARY : Ledger.NUMERIC);
                boolean hasSelector = resolver.get("selector") != null;
                boolean hasRef = resolver.get("ref") != null;
                if (hasSelector == hasRef) {
                    throw invalid("resolver.selector", "give exactly one of selector (occurrences) or ref (a rollup)");
                }
                Map<String, Object> selector = hasSelector ? selector(resolver.get("selector"), true) : null;
                if (hasRef) {
                    SeriesRef ref = seriesRef(resolver.get("ref"));
                    if (ref.type() != SeriesRef.Type.ROLLUP) {
                        throw invalid("resolver.ref", "a count-threshold ref must be a rollup: series");
                    }
                    if (resolver.get("area") != null) {
                        throw invalid("resolver.area", "a rollup ref already names its region");
                    }
                } else {
                    area(resolver.get("area"), selector);
                }
                window(resolver.get("window"), true);
                if (hasOp) {
                    op(resolver.get("op"), true);
                    threshold(resolver.get("threshold"), true);
                }
            }
            case "series-value" -> {
                seriesRef(resolver.get("ref"));
                boolean period = resolver.get("period") != null;
                boolean window = resolver.get("window") != null;
                if (period == window) {
                    throw invalid("resolver.period", "give exactly one of period or window");
                }
                if (period) {
                    periodRange(resolver.get("period").toString());
                } else {
                    window(resolver.get("window"), true);
                    if (((Map<?, ?>) resolver.get("window")).get("end") == null) {
                        throw invalid("resolver.window.end", "a series-value window needs an end");
                    }
                }
                Object aggregate = resolver.get("aggregate");
                if (aggregate == null) {
                    out.put("aggregate", "last");
                } else if (!AGGREGATES.contains(aggregate.toString())) {
                    throw invalid("resolver.aggregate", "must be one of " + AGGREGATES);
                }
                boolean hasOp = resolver.get("op") != null;
                boolean hasThreshold = resolver.get("threshold") != null;
                if (hasOp != hasThreshold) {
                    throw invalid("resolver.threshold", "op and threshold go together");
                }
                if (hasOp) {
                    op(resolver.get("op"), true);
                    threshold(resolver.get("threshold"), false);
                    requireType(questionType, type + " with a threshold", Ledger.BINARY);
                } else {
                    requireType(questionType, type + " without a threshold", Ledger.NUMERIC);
                }
            }
            case "alert-fired" -> {
                requireType(questionType, type, Ledger.BINARY);
                Object rule = resolver.get("ruleId");
                try {
                    UUID.fromString(String.valueOf(rule));
                } catch (IllegalArgumentException e) {
                    throw invalid("resolver.ruleId", "must be a 017 rule id (UUID)");
                }
                area(resolver.get("area"), Map.of("alertRuleId", rule));
                window(resolver.get("window"), true);
            }
            case "situation-status" -> {
                requireType(questionType, type, Ledger.BINARY);
                ulid(resolver.get("situationId"), "resolver.situationId");
                Object status = resolver.get("status");
                Object min = resolver.get("minSeverity");
                if (status == null && min == null) {
                    throw invalid("resolver.status", "give status, minSeverity or both");
                }
                if (status != null && !SITUATION_STATUSES.contains(status.toString())) {
                    throw invalid("resolver.status", "must be one of " + SITUATION_STATUSES);
                }
                if (min != null && !SEVERITIES.contains(min.toString())) {
                    throw invalid("resolver.minSeverity", "must be one of " + SEVERITIES);
                }
            }
            case "hypothesis-outcome" -> {
                if (questionType != null && !Ledger.BINARY.equals(questionType)
                        && !Ledger.MULTIPLE.equals(questionType)) {
                    throw invalid("resolver.type", "a hypothesis-outcome resolver answers binary or multiple questions");
                }
                ulid(resolver.get("setId"), "resolver.setId");
            }
            default -> {
                // manual: any question type.
            }
        }
        return out;
    }

    /** A {@code RESOLVER_INVALID} problem. */
    public static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.RESOLVER_INVALID, "The resolver is not valid: " + field + " "
                + message + ".", List.of(new FieldError(field, message)));
    }

    private static void requireType(String questionType, String what, String needed) {
        if (questionType != null && !needed.equals(questionType)) {
            throw invalid("resolver.type", "a " + what + " resolver answers " + needed + " questions, not "
                    + questionType + " ones");
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> selector(Object value, boolean required) {
        if (value == null) {
            if (required) {
                throw invalid("resolver.selector", "is required");
            }
            return null;
        }
        if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
            throw invalid("resolver.selector", "must be an EventSelector object");
        }
        Map<String, Object> selector = (Map<String, Object>) map;
        if (selector.containsKey("rule")) {
            throw invalid("resolver.selector.rule", "the inline rule form cannot be resolved; use alertRuleId,"
                    + " alertRuleTag or intelEventType");
        }
        long forms = selector.keySet().stream().filter(SELECTOR_FORMS::contains).count();
        if (forms != 1) {
            throw invalid("resolver.selector", "must name exactly one of " + SELECTOR_FORMS);
        }
        Object regions = selector.get("regionCodes");
        if (regions != null) {
            regionCodes(regions, "resolver.selector.regionCodes");
        }
        return selector;
    }

    @SuppressWarnings("unchecked")
    private static void area(Object value, Map<String, Object> selector) {
        if (value == null) {
            return;
        }
        if (!(value instanceof Map<?, ?> raw) || raw.isEmpty()) {
            throw invalid("resolver.area", "must be a ResolverArea object");
        }
        Map<String, Object> area = (Map<String, Object>) raw;
        long forms = area.keySet().stream().filter(k -> List.of("bbox", "polygon", "placeId", "regionCodes")
                .contains(k)).count();
        if (forms != 1 || area.size() != 1) {
            throw invalid("resolver.area", "give exactly one of bbox, polygon, placeId or regionCodes");
        }
        if (area.containsKey("regionCodes")) {
            regionCodes(area.get("regionCodes"), "resolver.area.regionCodes");
            return;
        }
        if (area.containsKey("bbox")) {
            Object bbox = area.get("bbox");
            if (!(bbox instanceof List<?> list) || list.size() != 4
                    || list.stream().anyMatch(v -> !(v instanceof Number))) {
                throw invalid("resolver.area.bbox", "must be [west, south, east, north]");
            }
            double w = ((Number) list.get(0)).doubleValue();
            double s = ((Number) list.get(1)).doubleValue();
            double e = ((Number) list.get(2)).doubleValue();
            double n = ((Number) list.get(3)).doubleValue();
            if (w < -180 || e > 180 || s < -90 || n > 90 || w >= e || s >= n) {
                throw invalid("resolver.area.bbox", "must be [west, south, east, north] in degrees, west < east and"
                        + " south < north");
            }
        } else if (area.containsKey("polygon")) {
            Object polygon = area.get("polygon");
            Object type = polygon instanceof Map<?, ?> m ? m.get("type") : null;
            if (type == null || !List.of("Polygon", "MultiPolygon").contains(type.toString())) {
                throw invalid("resolver.area.polygon", "must be a GeoJSON Polygon or MultiPolygon");
            }
        } else if (area.get("placeId") == null || area.get("placeId").toString().isBlank()) {
            throw invalid("resolver.area.placeId", "must be a 036 place id");
        }
        String form = selector == null ? null : selector.keySet().stream().filter(SELECTOR_FORMS::contains)
                .findFirst().orElse(null);
        if (form != null && !LOCATED_FORMS.contains(form)) {
            throw invalid("resolver.area", "a " + form + " selector has no geometry; use area.regionCodes");
        }
    }

    private static void regionCodes(Object value, String field) {
        if (!(value instanceof List<?> list) || list.isEmpty() || list.size() > 300) {
            throw invalid(field, "must be 1 to 300 040 region codes");
        }
        for (Object code : list) {
            if (code == null || !REGION.matcher(code.toString()).matches()) {
                throw invalid(field, "'" + code + "' is not a 040 region code");
            }
        }
    }

    private static void window(Object value, boolean required) {
        if (value == null) {
            if (required) {
                throw invalid("resolver.window", "is required");
            }
            return;
        }
        if (!(value instanceof Map<?, ?> window) || window.get("start") == null) {
            throw invalid("resolver.window", "must be a TimeRange with a start");
        }
        Instant start;
        try {
            start = JsonValues.instant(window.get("start").toString());
        } catch (RuntimeException e) {
            throw invalid("resolver.window.start", "must be an ISO-8601 date-time");
        }
        if (window.get("end") != null) {
            Instant end;
            try {
                end = JsonValues.instant(window.get("end").toString());
            } catch (RuntimeException e) {
                throw invalid("resolver.window.end", "must be an ISO-8601 date-time");
            }
            if (!end.isAfter(start)) {
                throw invalid("resolver.window.end", "must be after start");
            }
        }
    }

    private static void op(Object value, boolean required) {
        if (value == null) {
            if (required) {
                throw invalid("resolver.op", "is required");
            }
            return;
        }
        if (!OPS.contains(value.toString())) {
            throw invalid("resolver.op", "must be one of " + OPS);
        }
    }

    private static void threshold(Object value, boolean count) {
        if (!(value instanceof Number n) || !Double.isFinite(n.doubleValue())) {
            throw invalid("resolver.threshold", "must be a number");
        }
        if (count && n.doubleValue() < 0) {
            throw invalid("resolver.threshold", "a count threshold is at least 0");
        }
    }

    private static SeriesRef seriesRef(Object value) {
        if (value == null) {
            throw invalid("resolver.ref", "is required");
        }
        try {
            return SeriesRef.parse(value.toString());
        } catch (RuntimeException e) {
            throw invalid("resolver.ref", "is not a series reference (series:, rollup: or rule:)");
        }
    }

    private static void ulid(Object value, String field) {
        if (value == null || !Ulids.isUlid(value.toString())) {
            throw invalid(field, "must be a ULID");
        }
    }

    /**
     * The UTC range of a series period: {@code YYYY}, {@code YYYY-MM}, {@code YYYY-Www} (ISO week) or
     * {@code YYYY-MM-DD}.
     *
     * @return {@code {start, end}} with {@code end} exclusive
     * @throws ValidationException 400 {@code RESOLVER_INVALID}
     */
    public static Instant[] periodRange(String period) {
        try {
            Matcher m;
            if ((m = DAY.matcher(period)).matches()) {
                LocalDate d = LocalDate.parse(period);
                return range(d, d.plusDays(1));
            }
            if ((m = MONTH.matcher(period)).matches()) {
                LocalDate d = LocalDate.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), 1);
                return range(d, d.plusMonths(1));
            }
            if ((m = WEEK.matcher(period)).matches()) {
                int week = Integer.parseInt(m.group(2));
                LocalDate d = LocalDate.of(Integer.parseInt(m.group(1)), 1, 4)
                        .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                        .with(java.time.DayOfWeek.MONDAY);
                if (week < 1 || week > 53) {
                    throw new IllegalArgumentException("week");
                }
                return range(d, d.plusWeeks(1));
            }
            if ((m = YEAR.matcher(period)).matches()) {
                LocalDate d = LocalDate.of(Integer.parseInt(m.group(1)), 1, 1);
                return range(d, d.plusYears(1));
            }
        } catch (RuntimeException e) {
            throw invalid("resolver.period", "'" + period + "' is not a valid period");
        }
        throw invalid("resolver.period", "must be YYYY, YYYY-MM, YYYY-Www or YYYY-MM-DD");
    }

    private static Instant[] range(LocalDate from, LocalDate to) {
        return new Instant[] {from.atStartOfDay(ZoneOffset.UTC).toInstant(), to.atStartOfDay(ZoneOffset.UTC)
                .toInstant()};
    }

    /**
     * The evaluation window of a resolver: its {@code window} (an open end means {@code resolveBy}), or its series
     * {@code period}; null for resolvers without one.
     */
    public static Instant[] window(Map<String, Object> resolver, Instant resolveBy) {
        if (resolver.get("period") != null) {
            return periodRange(resolver.get("period").toString());
        }
        Map<String, Object> window = JsonValues.child(resolver, "window");
        if (window == null || window.get("start") == null) {
            return null;
        }
        Instant start = JsonValues.instant(window.get("start").toString());
        Instant end = window.get("end") == null ? resolveBy : JsonValues.instant(window.get("end").toString());
        return new Instant[] {start, end};
    }

    /** True when {@code value op threshold}. */
    public static boolean compare(double value, String op, double threshold) {
        return switch (op) {
            case "gt" -> value > threshold;
            case "gte" -> value >= threshold;
            case "lt" -> value < threshold;
            case "lte" -> value <= threshold;
            case "eq" -> value == threshold;
            default -> throw new IllegalArgumentException("unknown op " + op);
        };
    }
}
