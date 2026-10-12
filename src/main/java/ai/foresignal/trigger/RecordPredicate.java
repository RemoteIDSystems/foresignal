package ai.foresignal.trigger;

import ai.foresignal.store.query.PgArrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.io.WKBWriter;

/**
 * One compiled record predicate shared by trigger rules and saved views (prompt 036, task 3): source
 * and kind arrays, the 017 {@link RuleCondition} grammar (with {@code inWatchlist}), and an area.
 *
 * <p>It has one meaning in two places, which the shared operator truth table proves equal:
 *
 * <ul>
 *   <li>{@link #sql}: a boolean SQL fragment rendered by the same renderer {@link RuleSqlBuilder}
 *       uses for rules ({@link #conditionsSql}), over any table alias or payload expression, with
 *       every path and value bound;
 *   <li>{@link #test}/{@link #testConditions}: the same test in Java, with the operators' Java
 *       meaning (the {@link TriggerPersistGate} evaluator), for streams.
 * </ul>
 *
 * <p>The predicate wraps its conditions in {@code COALESCE(…, false)}, so a missing value or one of
 * another JSON type never matches, exactly as {@link Evaluation#AMBIGUOUS} does not hold in Java.
 *
 * @param sources source ids; empty for any
 * @param kinds kinds; empty for any
 * @param conditions the conditions, ANDed
 * @param area the area records must intersect, or null for anywhere (SRID 4326)
 */
public final class RecordPredicate {

    private static final Pattern SIMPLE_COLUMN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)?$");

    private final Set<String> sources;
    private final Set<String> kinds;
    private final List<RuleCondition> conditions;
    private final Geometry area;
    private volatile PreparedGeometry prepared;

    /** Copies the collections. */
    public RecordPredicate(Set<String> sources, Set<String> kinds, List<RuleCondition> conditions, Geometry area) {
        this.sources = sources == null ? Set.of() : Set.copyOf(new TreeSet<>(sources));
        this.kinds = kinds == null ? Set.of() : Set.copyOf(new TreeSet<>(kinds));
        this.conditions = conditions == null ? List.of() : List.copyOf(conditions);
        this.area = area;
    }

    /** Source ids; empty for any. */
    public Set<String> sources() {
        return sources;
    }

    /** Kinds; empty for any. */
    public Set<String> kinds() {
        return kinds;
    }

    /** The conditions, ANDed. */
    public List<RuleCondition> conditions() {
        return conditions;
    }

    /** The area, or null for anywhere. */
    public Geometry area() {
        return area;
    }

    /** A predicate of conditions only. */
    public static RecordPredicate of(List<RuleCondition> conditions) {
        return new RecordPredicate(Set.of(), Set.of(), conditions, null);
    }

    /** True when the predicate narrows nothing. */
    public boolean isEmpty() {
        return sources.isEmpty() && kinds.isEmpty() && conditions.isEmpty() && area == null;
    }

    /** True when it has payload conditions. */
    public boolean hasConditions() {
        return !conditions.isEmpty();
    }

    // ------------------------------------------------------------------------- SQL

    /**
     * Binds one value and returns its placeholder expression ({@code CAST(:name AS type)}). The same
     * {@code key} must bind the same value, so a predicate rendered for two aliases binds once.
     */
    @FunctionalInterface
    public interface Binder {

        /**
         * Binds {@code value} under {@code key}.
         *
         * @param key distinguishes the value within one statement ({@code c0p}, {@code c0v})
         * @param value the JDBC value
         * @param sqlType the SQL type to cast to
         * @return the placeholder expression
         */
        String bind(String key, Object value, String sqlType);
    }

    /**
     * A {@link Binder} over a function that binds a value under a fresh name and returns the bare
     * placeholder ({@code :p3}), for query builders that number their own parameters; each key is
     * bound once.
     */
    public static Binder binder(Function<Object, String> bind) {
        Map<String, String> bound = new HashMap<>();
        return (key, value, sqlType) -> bound.computeIfAbsent(key,
                k -> "CAST(" + bind.apply(value) + " AS " + sqlType + ")");
    }

    /**
     * The whole predicate over explicit columns, or {@code "true"} when it narrows nothing.
     *
     * @param sourceColumn the source id expression
     * @param kindColumn the kind expression
     * @param geomColumn the geometry expression, or null (an area then fails closed)
     * @param payloadExpression the payload expression, or null (conditions then fail closed)
     * @param binder binds values
     */
    public String sql(String sourceColumn, String kindColumn, String geomColumn, String payloadExpression,
            Binder binder) {
        List<String> parts = new ArrayList<>();
        if (!sources.isEmpty()) {
            parts.add(sourceColumn + " = ANY(" + binder.bind("rpS", PgArrays.text(new ArrayList<>(sources)), "text[]")
                    + ")");
        }
        if (!kinds.isEmpty()) {
            parts.add(kindColumn + " = ANY(" + binder.bind("rpK", PgArrays.text(new ArrayList<>(kinds)), "text[]") + ")");
        }
        if (area != null) {
            if (geomColumn == null || area.isEmpty()) {
                parts.add("FALSE");
            } else {
                var e = area.getEnvelopeInternal();
                String envelope = "ST_MakeEnvelope(" + binder.bind("rpX1", e.getMinX(), "double precision") + ", "
                        + binder.bind("rpY1", e.getMinY(), "double precision") + ", "
                        + binder.bind("rpX2", e.getMaxX(), "double precision") + ", "
                        + binder.bind("rpY2", e.getMaxY(), "double precision") + ", 4326)";
                String shape = "ST_GeomFromWKB(" + binder.bind("rpA", new WKBWriter(2).write(area), "bytea") + ", 4326)";
                parts.add("(" + geomColumn + " && " + envelope + " AND ST_Intersects(" + geomColumn + ", " + shape + "))");
            }
        }
        if (!conditions.isEmpty()) {
            parts.add(payloadExpression == null ? "FALSE"
                    : conditionsSql(conditions, sourceColumn, payloadExpression, binder));
        }
        return parts.isEmpty() ? "true" : "(" + String.join(" AND ", parts) + ")";
    }

    /**
     * {@code COALESCE((c0) AND (c1) …, false)} for {@code conditions} over a payload expression, or
     * {@code "true"} when there are none. This is the one condition renderer: {@link RuleSqlBuilder}
     * renders rules with it and views and request filters render through {@link #sql}.
     *
     * @param conditions the conditions
     * @param sourceExpression the record's source id (identity conditions switch on it)
     * @param payloadExpression the record's payload
     * @param binder binds values under {@code c<i><suffix>} keys
     */
    public static String conditionsSql(List<RuleCondition> conditions, String sourceExpression,
            String payloadExpression, Binder binder) {
        if (conditions.isEmpty()) {
            return "true";
        }
        return "COALESCE(" + String.join(" AND ", conditionParts(conditions, sourceExpression, payloadExpression,
                binder)) + ", false)";
    }

    /**
     * Each condition as a parenthesized SQL fragment that may be NULL (no {@code COALESCE}), for
     * callers that AND more parts into one {@code COALESCE} ({@link RuleSqlBuilder}: the fence).
     */
    public static List<String> conditionParts(List<RuleCondition> conditions, String sourceExpression,
            String payloadExpression, Binder binder) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < conditions.size(); i++) {
            RuleCondition condition = conditions.get(i);
            Term term = new Term(i, condition, sourceExpression, payloadExpression, binder);
            parts.add("(" + condition.operator().sql(term, condition.value()) + ")");
        }
        return parts;
    }

    /** The path expressions of condition {@code index}, switching on the source for identity conditions. */
    static final class Term implements SqlTerm {

        private final int index;
        private final RuleCondition condition;
        private final String source;
        private final String payload;
        private final Binder binder;

        Term(int index, RuleCondition condition, String source, String payload, Binder binder) {
            this.index = index;
            this.condition = condition;
            this.source = source;
            this.payload = SIMPLE_COLUMN.matcher(payload).matches() ? payload : "(" + payload + ")";
            this.binder = binder;
        }

        private String lookup(String operator, String nullType) {
            if (!condition.identity()) {
                return payload + " " + operator + " "
                        + binder.bind("c" + index + "p", PgArrays.text(condition.steps()), "text[]");
            }
            Map<String, String> paths = new TreeMap<>(condition.identityPaths());
            if (paths.isEmpty()) {
                return "CAST(NULL AS " + nullType + ")";
            }
            StringBuilder out = new StringBuilder("(CASE ").append(source);
            int k = 0;
            for (Map.Entry<String, String> entry : paths.entrySet()) {
                out.append(" WHEN ").append(binder.bind("c" + index + "s" + k, entry.getKey(), "text"))
                        .append(" THEN ").append(payload).append(' ').append(operator).append(' ')
                        .append(binder.bind("c" + index + "p" + k, PgArrays.text(RuleCondition.steps(entry.getValue())),
                                "text[]"));
                k++;
            }
            return out.append(" END)").toString();
        }

        @Override
        public String json() {
            return lookup("#>", "jsonb");
        }

        @Override
        public String text() {
            return lookup("#>>", "text");
        }

        @Override
        public String typeof() {
            return "jsonb_typeof(" + json() + ")";
        }

        @Override
        public String bind(String suffix, Object value, String sqlType) {
            return binder.bind("c" + index + suffix, value, sqlType);
        }
    }

    // ------------------------------------------------------------------------- Java

    /**
     * The Java test of the whole predicate on a record.
     *
     * @param sourceId the record's source
     * @param kind the record's kind (null fails a kind filter)
     * @param geometry the record's geometry (null fails an area)
     * @param payload the record's payload (null fails conditions)
     */
    public boolean test(String sourceId, String kind, Geometry geometry, Map<String, ?> payload) {
        return testShape(sourceId, kind, geometry) && testConditions(sourceId, payload);
    }

    /** Sources, kinds and area only. */
    public boolean testShape(String sourceId, String kind, Geometry geometry) {
        if (!sources.isEmpty() && !sources.contains(sourceId)) {
            return false;
        }
        if (!kinds.isEmpty() && (kind == null || !kinds.contains(kind))) {
            return false;
        }
        if (area == null) {
            return true;
        }
        return geometry != null && !geometry.isEmpty() && !area.isEmpty() && prepared().intersects(geometry);
    }

    /** The conditions on a record of {@code sourceId}: every one must hold. */
    public boolean testConditions(String sourceId, Map<String, ?> payload) {
        for (RuleCondition condition : conditions) {
            if (!condition.evaluate(sourceId, payload).holds()) {
                return false;
            }
        }
        return true;
    }

    private PreparedGeometry prepared() {
        PreparedGeometry current = prepared;
        if (current == null) {
            current = PreparedGeometryFactory.prepare(area);
            prepared = current;
        }
        return current;
    }
}
