package ai.foresignal.trigger;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.kinematics.GeoMath;
import ai.foresignal.kinematics.KinematicFields;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.store.TriggerMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.jdbc.core.RowMapper;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Edges of a rule with {@code approach} conditions (prompt 114).
 *
 * <p>An approach condition cannot be rendered in SQL, so for such a rule {@link RuleSqlBuilder#approachCandidates}
 * selects every version in the chunk where the rest of the predicate held for the version or for its predecessor,
 * with the predecessor's geometry, payload and time. This class then evaluates {@code P = rest AND approach} on
 * both sides in Java and applies the same edge definitions {@link RuleSqlBuilder} renders: {@code match},
 * {@code enter} (FIRST, after a tombstone, REAPPEAR when {@code reappearAsEnter}, or {@code NOT P(p)}) and
 * {@code exit}, plus the inverse-edge rows auto-resolve needs. An {@link Evaluation#AMBIGUOUS} approach counts as
 * not holding, as SQL's {@code COALESCE(…, false)} does.
 *
 * <p>The subject's speed and course come from the version's payload ({@link KinematicFields}); when they are
 * absent, the version's are derived from its predecessor's position and time (at least 10 s apart). The
 * predecessor itself is judged only from its own payload.
 */
public final class ApproachMatches {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private ApproachMatches() {}

    /**
     * Judges one approach condition for a subject state; implemented by
     * {@code ai.foresignal.kinematics.ApproachEvaluator.Session#evaluate}.
     */
    @FunctionalInterface
    public interface Judge {

        /**
         * The evaluation.
         *
         * @param condition the condition
         * @param sourceId the subject's source
         * @param externalId the subject's entity
         * @param geometry the subject's geometry, or null
         * @param at the subject's time
         * @param speedMps the subject's speed, or null
         * @param courseDeg the subject's course, or null
         */
        Evaluation evaluate(ApproachCondition condition, String sourceId, String externalId, Geometry geometry,
                Instant at, Double speedMps, Double courseDeg);
    }

    /** A judge that knows nothing: every approach is ambiguous (no evaluator available). */
    public static final Judge UNKNOWN = (c, s, e, g, at, speed, course) -> Evaluation.AMBIGUOUS;

    /**
     * One candidate version with its predecessor.
     *
     * @param row the version, with {@code edge = match}
     * @param restV the rest of the predicate held for the version
     * @param prevSeq the predecessor's seq, or null when it is missing
     * @param prevTombstone the predecessor is a tombstone
     * @param prevGeometry the predecessor's geometry as GeoJSON, or null
     * @param prevPayload the predecessor's payload JSON, or null
     * @param prevObservedAt the predecessor's observed time, or null
     * @param restP the rest of the predicate held for a non-tombstone predecessor
     */
    public record Candidate(MatchRow row, boolean restV, Long prevSeq, boolean prevTombstone, String prevGeometry,
            String prevPayload, Instant prevObservedAt, boolean restP) {}

    /** Reads the columns of {@link RuleSqlBuilder#approachCandidates}. */
    public static final RowMapper<Candidate> MAPPER = ApproachMatches::read;

    private static Candidate read(ResultSet rs, int n) throws SQLException {
        MatchRow row = MatchRow.MAPPER.mapRow(rs, n);
        long pSeq = rs.getLong("p_seq");
        Long prevSeq = rs.wasNull() ? null : pSeq;
        return new Candidate(row, rs.getBoolean("rest_v"), prevSeq, rs.getBoolean("p_tombstone"),
                rs.getString("p_geometry"), rs.getString("p_payload"), MatchRow.instant(rs, "p_observed_at"),
                rs.getBoolean("rest_p"));
    }

    /**
     * The matches and inverse-edge rows of a chunk.
     *
     * @param matches the versions that fire, with their edge, in seq order
     * @param resolves the versions where the entity stops satisfying the rule (for auto-resolve)
     */
    public record Result(List<MatchRow> matches, List<ResolveRow> resolves) {}

    /**
     * Evaluates the candidates of {@code rule}.
     *
     * @param rule a rule with approach conditions, without a hysteresis band
     * @param candidates the candidate rows in seq order
     * @param judge the approach judge
     */
    public static Result evaluate(RuleSpec rule, List<Candidate> candidates, Judge judge) {
        List<MatchRow> matches = new ArrayList<>();
        List<ResolveRow> resolves = new ArrayList<>();
        AutoResolve auto = rule.autoResolve();
        boolean wantResolves = auto != null && auto.needsEdgePass() && rule.mode() != TriggerMode.EXIT;
        for (Candidate c : candidates) {
            MatchRow v = c.row();
            Geometry vGeometry = geometry(v.geometry());
            Geometry pGeometry = geometry(c.prevGeometry());
            Map<String, Object> vPayload = payload(v.payload());
            Map<String, Object> pPayload = payload(c.prevPayload());
            boolean pv = !v.tombstone() && c.restV()
                    && holds(rule, judge, v.sourceId(), v.externalId(), vGeometry, v.observedAt(), vPayload,
                            pGeometry, c.prevObservedAt());
            boolean pp = c.prevSeq() != null && !c.prevTombstone() && c.restP()
                    && holds(rule, judge, v.sourceId(), v.externalId(), pGeometry, c.prevObservedAt(), pPayload,
                            null, null);
            boolean exitEdge = c.prevSeq() != null && !c.prevTombstone() && pp && (v.tombstone() || !pv);
            String edge = switch (rule.mode()) {
                case MATCH -> pv ? "match" : null;
                case ENTER -> pv && (v.prevSeq() == null || c.prevTombstone()
                        || (rule.reappearAsEnter() && v.persistReason() == PersistReason.REAPPEAR.code())
                        || (c.prevSeq() != null && !pp)) ? "enter" : null;
                case EXIT -> exitEdge ? "exit" : null;
            };
            if (edge != null) {
                matches.add(withEdge(v, edge));
            }
            if (wantResolves && exitEdge && (auto.onInverseEdge() || v.tombstone())
                    && (auto.onTombstone() || !v.tombstone())) {
                resolves.add(new ResolveRow(v.seq(), v.sourceId(), v.externalId(), v.tombstone()));
            }
        }
        return new Result(matches, resolves);
    }

    /** True when every approach condition of {@code rule} matches the subject state. */
    static boolean holds(RuleSpec rule, Judge judge, String sourceId, String externalId, Geometry geometry, Instant at,
            Map<String, Object> payload, Geometry previousGeometry, Instant previousAt) {
        Double speed = KinematicFields.speedMps(payload);
        Double course = KinematicFields.courseDeg(payload);
        if ((speed == null || course == null) && geometry != null && previousGeometry != null && at != null
                && previousAt != null && Duration.between(previousAt, at).toSeconds() >= 10) {
            Point a = point(previousGeometry);
            Point b = point(geometry);
            double seconds = Duration.between(previousAt, at).toMillis() / 1000.0;
            if (speed == null) {
                speed = GeoMath.distanceMeters(a.getX(), a.getY(), b.getX(), b.getY()) / seconds;
            }
            if (course == null) {
                course = GeoMath.bearingDegrees(a.getX(), a.getY(), b.getX(), b.getY());
            }
        }
        for (ApproachCondition condition : rule.approaches()) {
            if (judge.evaluate(condition, sourceId, externalId, geometry, at, speed, course) != Evaluation.MATCH) {
                return false;
            }
        }
        return true;
    }

    private static Point point(Geometry geometry) {
        return geometry instanceof Point p ? p : geometry.getInteriorPoint();
    }

    private static MatchRow withEdge(MatchRow v, String edge) {
        return new MatchRow(v.seq(), v.prevSeq(), v.sourceId(), v.externalId(), v.kind(), v.version(), v.persistReason(),
                v.observedAt(), v.eventTime(), v.validFrom(), v.validUntil(), v.tombstone(), v.geometry(), v.payload(),
                edge);
    }

    private static Geometry geometry(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) {
            return null;
        }
        try {
            return new GeoJsonReader(1_000_000, 8).read(geoJson);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Map<String, Object> payload(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }
        try {
            return JSON.readValue(text, MAP);
        } catch (RuntimeException e) {
            return Map.of();
        }
    }
}
