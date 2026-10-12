package ai.foresignal.trigger;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.store.TriggerMode;
import ai.foresignal.store.query.PgArrays;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Renders one parameterized statement per rule over persisted versions {@code seq ∈ (:fromSeq,
 * :toSeq]} (prompt 017, Core-Data-Model §11).
 *
 * <p>The predicate {@code P(a)} (conditions and geofence) is rendered for the version {@code v} and,
 * in edge modes, for its predecessor {@code p}, joined on seq alone
 * ({@code LEFT JOIN record_version p ON p.seq = v.prev_seq}, which prunes to one leaf per tier). It
 * is wrapped in {@code COALESCE(…, false)}, so a missing value or one of another JSON type is simply
 * "not matching" and {@code NOT P(p)} never turns NULL.
 *
 * <ul>
 *   <li>{@code match}: {@code NOT v.tombstone AND P(v)}.
 *   <li>{@code enter}: {@code NOT v.tombstone AND P(v) AND (v.prev_seq IS NULL OR p.tombstone OR
 *       (:reappearAsEnter AND v.persist_reason = 2) OR (p.seq IS NOT NULL AND NOT P(p)))}.
 *   <li>{@code exit}: {@code p.seq IS NOT NULL AND NOT p.tombstone AND P(p) AND (v.tombstone OR NOT
 *       P(v))}.
 *   <li>With a hysteresis band, both edges are selected, the enter edge against the shrunk fence and
 *       the exit edge against the grown one, and the {@code edge} column tells them apart;
 *       {@link AlertPass} keeps them alternating.
 * </ul>
 *
 * <p>{@code HEARTBEAT} versions are excluded ({@code v.persist_reason <> 8}). A missing predecessor
 * (removed by retention) fires nothing except through FIRST or REAPPEAR, and expiry itself is not an
 * exit. The cooldown seed is {@code NOT EXISTS} an unsuppressed alert of the rule (and, with record
 * scope, the entity) whose {@code observed_at} is within the cooldown of {@code v.observed_at};
 * {@link AlertPass} then applies the exact per-entity cooldown in seq order.
 *
 * <p>Operators come from the registry; every path and value is a bound parameter. Conditions are
 * rendered by {@link RecordPredicate#conditionParts}, the renderer saved views share (prompt 036).
 *
 * <p><b>Moving anchors</b> (prompt 036): an anchored rule's spatial part is
 * {@code x.geom && ST_Expand(:anchor, :dLon, :dLat) AND ST_DWithin(x.geom::geography,
 * :anchor::geography, :m)} ({@code ST_Intersects(x.geom, :anchor)} with radius 0), where
 * {@code :anchor} is the anchor's <em>current</em> position, used identically for {@code v} and
 * {@code p}. Without a current position (a stale anchor) the predicate is {@code FALSE}, which
 * suspends the rule.
 */
public final class RuleSqlBuilder {

    /** Rows per statement are always ordered by seq. */
    static final String ORDER = " ORDER BY v.seq";

    static final String COLUMNS =
            "v.seq, v.prev_seq, v.source_id, v.external_id, v.kind, v.version, v.persist_reason, v.observed_at,"
                    + " v.event_time, v.valid_from, v.valid_until, v.tombstone, ST_AsGeoJSON(v.geom) AS geometry,"
                    + " v.payload::text AS payload";

    static final String EDGE_JOIN = " LEFT JOIN record_version p ON p.seq = v.prev_seq";

    private RuleSqlBuilder() {}

    /**
     * A rendered statement.
     *
     * @param sql the SQL with named parameters
     * @param params the bound parameters
     */
    public record RuleSql(String sql, MapSqlParameterSource params) {

        /** The bound values by name. */
        public Map<String, Object> values() {
            return params.getValues();
        }
    }

    /** Which fence a predicate tests. */
    enum FenceSide {
        MAIN("fence"),
        INNER("fenceInner"),
        OUTER("fenceOuter");

        final String param;

        FenceSide(String param) {
            this.param = param;
        }
    }

    /**
     * The match statement of {@code rule}: one row per version that fires, with the columns of
     * {@link MatchRow}.
     *
     * @param rule the rule
     * @param fromSeq exclusive lower bound
     * @param toSeq inclusive upper bound
     * @param evalTime the evaluation time ({@code forecastHorizon})
     * @param cooldownSeed whether to add the cooldown {@code NOT EXISTS} (false for backtests)
     */
    public static RuleSql matches(RuleSpec rule, long fromSeq, long toSeq, Instant evalTime, boolean cooldownSeed) {
        return matches(rule, fromSeq, toSeq, evalTime, cooldownSeed, null);
    }

    /**
     * {@link #matches(RuleSpec, long, long, Instant, boolean)} for an anchored rule, with the anchor's
     * current position ({@code null} suspends the rule: nothing matches).
     */
    public static RuleSql matches(RuleSpec rule, long fromSeq, long toSeq, Instant evalTime, boolean cooldownSeed,
            Point anchor) {
        Params b = new Params();
        b.anchor = anchor;
        List<String> where = baseWhere(rule, fromSeq, toSeq, b);
        if (rule.onlyNew()) {
            where.add("v.version = 1");
        }
        if (rule.forecastHorizon() != null) {
            b.put("evalTime", OffsetDateTime.ofInstant(evalTime, ZoneOffset.UTC));
            b.put("horizon", (double) rule.forecastHorizon().toSeconds());
            where.add("(v.valid_from IS NULL OR v.valid_from <= CAST(:evalTime AS timestamptz)"
                    + " + make_interval(secs => CAST(:horizon AS double precision)))");
        }
        String seed = cooldownSeed && !rule.cooldown().isZero() ? cooldownSeed(rule, b) : null;
        String edge;
        boolean join = rule.mode() != TriggerMode.MATCH;
        if (rule.hysteresis()) {
            String enter = enter(rule, FenceSide.INNER, b);
            String exit = exit(rule, FenceSide.OUTER, b);
            String enterSeeded = rule.mode() == TriggerMode.ENTER && seed != null ? enter + " AND " + seed : enter;
            String exitSeeded = rule.mode() == TriggerMode.EXIT && seed != null ? exit + " AND " + seed : exit;
            where.add("((" + enterSeeded + ") OR (" + exitSeeded + "))");
            edge = "CASE WHEN " + enter + " THEN 'enter' WHEN " + exit + " THEN 'exit' END";
        } else {
            switch (rule.mode()) {
                case ENTER -> {
                    where.add(enter(rule, FenceSide.MAIN, b));
                    edge = "'enter'";
                }
                case EXIT -> {
                    where.add(exit(rule, FenceSide.MAIN, b));
                    edge = "'exit'";
                }
                default -> {
                    where.add("NOT v.tombstone AND " + predicate(rule, "v", FenceSide.MAIN, b));
                    edge = "'match'";
                }
            }
            if (seed != null) {
                where.add(seed);
            }
        }
        String sql = "SELECT " + COLUMNS + ", " + edge + " AS edge FROM record_version v"
                + (join ? EDGE_JOIN : "") + " WHERE " + String.join(" AND ", where) + ORDER;
        return new RuleSql(sql, b.source);
    }

    /**
     * The auto-resolve statement of {@code rule}: versions where the entity stops satisfying the rule
     * ({@code P(p) AND (v.tombstone OR NOT P(v))}), restricted to tombstones when only
     * {@code onTombstone} is set and to non-tombstones when only {@code onInverseEdge} is. With a
     * hysteresis band, {@code P} uses the grown fence. Columns: {@code seq, source_id, external_id,
     * tombstone}.
     */
    public static RuleSql inverse(RuleSpec rule, long fromSeq, long toSeq) {
        return inverse(rule, fromSeq, toSeq, null);
    }

    /** {@link #inverse(RuleSpec, long, long)} for an anchored rule, with the anchor's current position. */
    public static RuleSql inverse(RuleSpec rule, long fromSeq, long toSeq, Point anchor) {
        AutoResolve auto = rule.autoResolve();
        Params b = new Params();
        b.anchor = anchor;
        List<String> where = baseWhere(rule, fromSeq, toSeq, b);
        FenceSide side = rule.hysteresis() ? FenceSide.OUTER : FenceSide.MAIN;
        where.add("NOT p.tombstone AND " + predicate(rule, "p", side, b)
                + " AND (v.tombstone OR NOT " + predicate(rule, "v", side, b) + ")");
        if (auto != null && auto.onTombstone() && !auto.onInverseEdge()) {
            where.add("v.tombstone");
        } else if (auto != null && auto.onInverseEdge() && !auto.onTombstone()) {
            where.add("NOT v.tombstone");
        }
        String sql = "SELECT v.seq, v.source_id, v.external_id, v.tombstone FROM record_version v"
                + " JOIN record_version p ON p.seq = v.prev_seq WHERE " + String.join(" AND ", where) + ORDER;
        return new RuleSql(sql, b.source);
    }

    // ------------------------------------------------------------------------- parts

    private static List<String> baseWhere(RuleSpec rule, long fromSeq, long toSeq, Params b) {
        List<String> where = new ArrayList<>();
        b.put("fromSeq", fromSeq);
        b.put("toSeq", toSeq);
        where.add("v.seq > :fromSeq");
        where.add("v.seq <= :toSeq");
        where.add("v.persist_reason <> " + PersistReason.HEARTBEAT.code());
        if (!rule.sources().isEmpty()) {
            b.put("sources", PgArrays.text(rule.sources().stream().sorted().toList()));
            where.add("v.source_id = ANY(CAST(:sources AS text[]))");
        }
        if (!rule.kinds().isEmpty()) {
            b.put("kinds", PgArrays.text(rule.kinds().stream().sorted().toList()));
            where.add("v.kind = ANY(CAST(:kinds AS text[]))");
        }
        return where;
    }

    static String enter(RuleSpec rule, FenceSide side, Params b) {
        b.put("reappearAsEnter", rule.reappearAsEnter());
        return "NOT v.tombstone AND " + predicate(rule, "v", side, b)
                + " AND (v.prev_seq IS NULL OR p.tombstone OR (CAST(:reappearAsEnter AS boolean) AND v.persist_reason = " + PersistReason.REAPPEAR.code() + ")"
                + " OR (p.seq IS NOT NULL AND NOT " + predicate(rule, "p", side, b) + "))";
    }

    static String exit(RuleSpec rule, FenceSide side, Params b) {
        return "p.seq IS NOT NULL AND NOT p.tombstone AND " + predicate(rule, "p", side, b)
                + " AND (v.tombstone OR NOT " + predicate(rule, "v", side, b) + ")";
    }

    /** {@code COALESCE(conditions AND fence, false)} for {@code alias}; {@code true} when empty. */
    static String predicate(RuleSpec rule, String alias, FenceSide side, Params b) {
        List<String> parts = new ArrayList<>(RecordPredicate.conditionParts(rule.conditions(), alias + ".source_id",
                alias + ".payload", b.binder()));
        if (rule.anchored()) {
            parts.add(anchorClause(rule.anchor(), alias + ".geom", b));
        }
        MultiPolygon fence = switch (side) {
            case MAIN -> rule.fence();
            case INNER -> rule.fenceInner();
            case OUTER -> rule.fenceOuter();
        };
        if (fence != null) {
            b.put(side.param, new WKBWriter(2).write(fence));
            parts.add("(" + alias + ".geom IS NOT NULL AND ST_Intersects(" + alias + ".geom, ST_GeomFromWKB(CAST(:"
                    + side.param + " AS bytea), 4326)))");
        }
        if (parts.isEmpty()) {
            return "true";
        }
        return "COALESCE(" + String.join(" AND ", parts) + ", false)";
    }

    static String cooldownSeed(RuleSpec rule, Params b) {
        b.put("rule", rule.id());
        b.put("cooldown", (double) rule.cooldown().toSeconds());
        String entity = rule.cooldownScope() == CooldownScope.RECORD
                ? " AND a.source_id = v.source_id AND a.external_id = v.external_id"
                : "";
        return "NOT EXISTS (SELECT 1 FROM alert a WHERE a.rule_id = :rule" + entity
                + " AND NOT a.suppressed AND a.observed_at > v.observed_at"
                + " - make_interval(secs => CAST(:cooldown AS double precision)))";
    }

    /**
     * The anchor clause over {@code geom}: within the radius of the anchor's current position (or
     * containing it, with radius 0); {@code FALSE} without a position.
     */
    static String anchorClause(NearAnchor anchor, String geom, Params b) {
        if (b.anchor == null) {
            return "FALSE";
        }
        b.put("anchor", new WKBWriter(2).write(b.anchor));
        String point = "ST_GeomFromWKB(CAST(:anchor AS bytea), 4326)";
        if (anchor.inside()) {
            return "(" + geom + " IS NOT NULL AND ST_Intersects(" + geom + ", " + point + "))";
        }
        double meters = anchor.radiusMeters();
        double[] degrees = expandDegrees(b.anchor.getY(), meters);
        b.put("anchorDLon", degrees[0]);
        b.put("anchorDLat", degrees[1]);
        b.put("anchorM", meters);
        return "(" + geom + " IS NOT NULL AND " + geom + " && ST_Expand(" + point
                + ", CAST(:anchorDLon AS double precision), CAST(:anchorDLat AS double precision))"
                + " AND ST_DWithin(" + geom + "::geography, " + point + "::geography, CAST(:anchorM AS double precision)))";
    }

    /**
     * The degree prefilter of a radius around latitude {@code lat}: {@code [dLon, dLat]}, with
     * {@code dLat = m / 111,320} and {@code dLon = m / 111,320 / cos(lat)}, padded by 1% and clamped
     * to the globe.
     */
    public static double[] expandDegrees(double lat, double meters) {
        double dLat = Math.min(180, meters / 111_320.0 * 1.01 + 1e-6);
        double cos = Math.cos(Math.toRadians(Math.min(89.9, Math.abs(lat) + dLat)));
        double dLon = cos <= 1e-6 ? 360 : Math.min(360, meters / 111_320.0 / cos * 1.01 + 1e-6);
        return new double[] {dLon, dLat};
    }

    /** The parameters of one statement; a name bound twice keeps its first value. */
    static final class Params {

        final MapSqlParameterSource source = new MapSqlParameterSource();

        /** The anchor's current position for an anchored rule, or null. */
        Point anchor;

        void put(String name, Object value) {
            if (!source.hasValue(name)) {
                source.addValue(name, value);
            }
        }

        /** Binds condition values under their own names ({@code c0p}, {@code c0v}). */
        RecordPredicate.Binder binder() {
            return (key, value, sqlType) -> {
                put(key, value);
                return "CAST(:" + key + " AS " + sqlType + ")";
            };
        }
    }
}
