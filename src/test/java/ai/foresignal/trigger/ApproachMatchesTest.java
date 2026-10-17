package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.GateState;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.store.TriggerMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * Edges of rules with {@code approach} conditions (prompt 114): the Java edge rules match the SQL ones (match,
 * enter, exit, REAPPEAR, inverse-edge resolves), velocity is derived from the predecessor when the payload has none,
 * and the persist gate is exact on known state and conservative on ambiguity.
 */
class ApproachMatchesTest {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final ApproachCondition NEAR = new ApproachCondition(null, new double[] {1, 0}, null, 1000, 20);

    /** A judge that matches when the subject's longitude is at least 0.9 and its speed is known. */
    static final ApproachMatches.Judge JUDGE = (c, s, e, g, at, speed, course) -> {
        if (g == null || speed == null || course == null) {
            return Evaluation.AMBIGUOUS;
        }
        return Evaluation.of(((Point) g).getX() >= 0.9);
    };

    static RuleSpec rule(TriggerMode mode, AutoResolve auto) {
        RuleSpec base = TestRules.rule().mode(mode).build();
        RuleSpec withAuto = new RuleSpec(base.id(), base.name(), true, base.sources(), base.kinds(), base.tags(),
                base.conditions(), null, null, null, 0, mode, false, true, base.cooldown(), base.cooldownScope(),
                base.severity(), auto, null, false, null, null, null, null, null, null, false, null, null, false, null);
        return withAuto.withApproaches(List.of(NEAR));
    }

    static MatchRow row(long seq, Long prev, double lon, boolean tombstone, short reason, String payload) {
        String geometry = tombstone ? null : "{\"type\":\"Point\",\"coordinates\":[" + lon + ",0]}";
        return new MatchRow(seq, prev, "src", "e", "vessel", (int) seq, reason, T.plusSeconds(seq * 60), null, null, null,
                tombstone, geometry, tombstone ? "{}" : payload, "match");
    }

    static final String MOVING = "{\"sog\":10,\"cog\":90}";

    static ApproachMatches.Candidate candidate(MatchRow v, Double prevLon, boolean prevTombstone, String prevPayload) {
        String prevGeometry = prevLon == null ? null : "{\"type\":\"Point\",\"coordinates\":[" + prevLon + ",0]}";
        return new ApproachMatches.Candidate(v, true, v.prevSeq(), prevTombstone, prevGeometry, prevPayload,
                v.prevSeq() == null ? null : T.plusSeconds(v.prevSeq() * 60), v.prevSeq() != null && !prevTombstone);
    }

    @Test
    @DisplayName("enter fires on the first version that approaches, after a non-approaching predecessor")
    void enter() {
        RuleSpec r = rule(TriggerMode.ENTER, null);
        List<ApproachMatches.Candidate> rows = List.of(
                candidate(row(1, null, 0.0, false, (short) 1, MOVING), null, false, null),
                candidate(row(2, 1L, 0.95, false, PersistReason.TRIGGER_EDGE.code(), MOVING), 0.0, false, MOVING),
                candidate(row(3, 2L, 0.97, false, PersistReason.TRIGGER_EDGE.code(), MOVING), 0.95, false, MOVING));
        ApproachMatches.Result result = ApproachMatches.evaluate(r, rows, JUDGE);
        assertThat(result.matches()).extracting(MatchRow::seq).containsExactly(2L);
        assertThat(result.matches().getFirst().edge()).isEqualTo("enter");
    }

    @Test
    @DisplayName("a first version that already approaches enters; REAPPEAR enters when reappearAsEnter")
    void firstAndReappear() {
        RuleSpec r = rule(TriggerMode.ENTER, null);
        assertThat(ApproachMatches.evaluate(r, List.of(candidate(row(1, null, 0.95, false, (short) 1, MOVING), null,
                false, null)), JUDGE).matches()).hasSize(1);
        assertThat(ApproachMatches.evaluate(r, List.of(candidate(row(5, 4L, 0.95, false,
                PersistReason.REAPPEAR.code(), MOVING), 0.95, false, MOVING)), JUDGE).matches()).hasSize(1);
    }

    @Test
    @DisplayName("exit fires when the predecessor approached and the version does not (or is a tombstone)")
    void exit() {
        RuleSpec r = rule(TriggerMode.EXIT, null);
        ApproachMatches.Result result = ApproachMatches.evaluate(r, List.of(
                candidate(row(2, 1L, 0.5, false, PersistReason.TRIGGER_EDGE.code(), MOVING), 0.95, false, MOVING),
                candidate(row(4, 3L, 0, true, (short) 4, MOVING), 0.95, false, MOVING)), JUDGE);
        assertThat(result.matches()).extracting(MatchRow::edge).containsExactly("exit", "exit");
    }

    @Test
    @DisplayName("match fires on every approaching version; inverse edges resolve under autoResolve")
    void matchAndResolves() {
        RuleSpec r = rule(TriggerMode.MATCH, AutoResolve.ALL);
        ApproachMatches.Result result = ApproachMatches.evaluate(r, List.of(
                candidate(row(2, 1L, 0.95, false, PersistReason.TRIGGER_EDGE.code(), MOVING), 0.0, false, MOVING),
                candidate(row(3, 2L, 0.5, false, PersistReason.TRIGGER_EDGE.code(), MOVING), 0.95, false, MOVING)),
                JUDGE);
        assertThat(result.matches()).extracting(MatchRow::seq).containsExactly(2L);
        assertThat(result.resolves()).singleElement().satisfies(rr -> {
            assertThat(rr.seq()).isEqualTo(3L);
            assertThat(rr.resolvedBy()).isEqualTo("inverse_edge");
        });
    }

    @Test
    @DisplayName("without speed in the payload, the version's velocity is derived from its predecessor")
    void derivedVelocity() {
        RuleSpec r = rule(TriggerMode.ENTER, null);
        ApproachMatches.Result result = ApproachMatches.evaluate(r, List.of(
                candidate(row(2, 1L, 0.95, false, PersistReason.TRIGGER_EDGE.code(), "{}"), 0.94, false, "{}")),
                JUDGE);
        assertThat(result.matches()).as("the predecessor itself is ambiguous, so not approaching").hasSize(1);
        assertThat(ApproachMatches.evaluate(r, List.of(candidate(row(2, 1L, 0.95, false, (short) 9, "{}"), null, false,
                "{}")), JUDGE).matches()).as("no predecessor position: ambiguous, no match").isEmpty();
    }

    @Test
    @DisplayName("an unknown judge never matches")
    void unknownJudge() {
        RuleSpec r = rule(TriggerMode.MATCH, null);
        assertThat(ApproachMatches.evaluate(r, List.of(candidate(row(1, null, 0.95, false, (short) 1, MOVING), null,
                false, null)), ApproachMatches.UNKNOWN).matches()).isEmpty();
    }

    @Test
    @DisplayName("the gate: exact on known state, conservative on ambiguity, quiet when nothing changed")
    void gate() {
        RuleSpec r = rule(TriggerMode.ENTER, null);
        TriggerPersistGate.ApproachGate approach = (c, s, g, v) -> {
            Double[] k = ai.foresignal.kinematics.KinematicFields.fromPathValues(v);
            if (g == null || k[0] == null) {
                return Evaluation.AMBIGUOUS;
            }
            return Evaluation.of(((Point) g).getX() >= 0.9);
        };
        TriggerPersistGate.Compiled gate = new TriggerPersistGate.Compiled(List.of(r), id -> null, a -> null, approach);
        assertThat(gate.spec("src").paths()).contains("sog", "cog", "gs", "track");
        assertThat(gate.spec("src").needsGeometry()).isTrue();
        Map<String, Object> moving = new HashMap<>(Map.of("sog", 10, "cog", 90));
        assertThat(gate.flips("src", "vessel", state(0.0, moving), state(0.95, moving))).isTrue();
        assertThat(gate.flips("src", "vessel", state(0.95, moving), state(0.96, moving))).isFalse();
        Map<String, Object> unknown = new HashMap<>();
        assertThat(gate.flips("src", "vessel", state(0.1, unknown), state(0.2, unknown))).as("ambiguous and moved")
                .isTrue();
        assertThat(gate.flips("src", "vessel", state(0.1, unknown), state(0.1, unknown))).as("ambiguous, unchanged")
                .isFalse();
    }

    static GateState state(double lon, Map<String, Object> values) {
        Point p = TestRules.GEO.createPoint(new Coordinate(lon, 0));
        return new GateState(values, p);
    }

    @Test
    @DisplayName("the stored value round-trips; exactly one target is required")
    void storedValue() {
        ApproachCondition c = ApproachCondition.fromValue(NEAR.toValue());
        assertThat(c).isEqualTo(NEAR);
        assertThat(c.toWire()).containsEntry("type", "approach");
        assertThat(ApproachCondition.fromValue(new ApproachCondition("home", null, null, 5, 1).toValue()).placeId())
                .isEqualTo("home");
        assertThat(ApproachCondition.fromValue(new ApproachCondition(null, null, "a/b", 5, 1).toValue()).record())
                .isEqualTo("a/b");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new ApproachCondition("p", new double[] {0, 0}, null,
                1, 1)).isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ApproachCondition.fromValue("x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(NEAR.horizonSeconds()).isEqualTo(1200.0);
    }
}
