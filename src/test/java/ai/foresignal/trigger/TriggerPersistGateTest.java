package ai.foresignal.trigger;

import static ai.foresignal.trigger.TestRules.box;
import static ai.foresignal.trigger.TestRules.point;
import static ai.foresignal.trigger.TestRules.rule;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.GateSnapshot;
import ai.foresignal.ingest.GateSpec;
import ai.foresignal.ingest.GateState;
import ai.foresignal.ingest.JsonValues;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.TriggerMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Geometry;

/** {@link TriggerPersistGate}: the shared truth table, flips, conservatism and specs (prompt 017). */
class TriggerPersistGateTest {

    static final SourceDescriptor QUAKES = SourceDescriptor.builder("quakes").name("Quakes").kind("earthquake")
            .mode(Mode.PUSH).defaultBudget(Budget.of(60, 10_000)).build();

    static final SourceDescriptor SHIPS = SourceDescriptor.builder("ships").name("Ships").kind("vessel")
            .mode(Mode.PUSH).defaultBudget(Budget.of(60, 10_000)).build();

    static Stream<TruthTable.Row> rows() {
        return TruthTable.rows().stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rows")
    @DisplayName("every truth-table row evaluates in Java exactly as the fixture says")
    void truthTable(TruthTable.Row row) {
        RuleCondition condition = row.condition();
        Evaluation evaluation = condition.evaluateValue(JsonValues.at(row.payload(), row.path()));
        assertThat(evaluation.holds()).as("holds").isEqualTo(row.expected());
        assertThat(evaluation == Evaluation.AMBIGUOUS).as("ambiguous").isEqualTo(row.ambiguous());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rows")
    @DisplayName("the gate flips exactly when a truth-table row's outcome differs from an empty payload's")
    void gateAgainstEmptyPayload(TruthTable.Row row) {
        RuleSpec spec = withCondition(row.condition());
        GateSnapshot gate = snapshot(spec);
        Object value = JsonValues.at(row.payload(), row.path());
        Evaluation empty = row.condition().evaluateValue(null);
        boolean expected = row.expected() != empty.holds()
                || ((row.ambiguous() || empty == Evaluation.AMBIGUOUS) && value != null);
        assertThat(gate.flips("quakes", "earthquake", state(row.path(), null, null), state(row.path(), value, null)))
                .isEqualTo(expected);
    }

    private static RuleSpec withCondition(RuleCondition condition) {
        TestRules builder = rule();
        builder.conditions.add(condition);
        return builder.build();
    }

    @Nested
    @DisplayName("flips")
    class Flips {

        final GateSnapshot gate = snapshot(rule().when("mag", ">=", 6).build());

        @Test
        @DisplayName("crossing the threshold flips; staying on one side does not")
        void threshold() {
            assertThat(gate.flips("quakes", "earthquake", state("mag", 5, null), state("mag", 6, null))).isTrue();
            assertThat(gate.flips("quakes", "earthquake", state("mag", 6.5, null), state("mag", 5.9, null))).isTrue();
            assertThat(gate.flips("quakes", "earthquake", state("mag", 6, null), state("mag", 7, null))).isFalse();
            assertThat(gate.flips("quakes", "earthquake", state("mag", 1, null), state("mag", 2, null))).isFalse();
        }

        @Test
        @DisplayName("conservative on type mismatch: flips iff the raw values differ")
        void typeMismatch() {
            assertThat(gate.flips("quakes", "earthquake", state("mag", "5", null), state("mag", "6", null))).isTrue();
            assertThat(gate.flips("quakes", "earthquake", state("mag", "5", null), state("mag", "5", null))).isFalse();
            assertThat(gate.flips("quakes", "earthquake", state("mag", true, null), state("mag", 1, null))).isTrue();
        }

        @Test
        @DisplayName("conservative on missing values: missing to present flips, missing on both sides does not")
        void missing() {
            assertThat(gate.flips("quakes", "earthquake", state("mag", null, null), state("mag", 2, null))).isTrue();
            assertThat(gate.flips("quakes", "earthquake", state("mag", 2, null), state("mag", null, null))).isTrue();
            assertThat(gate.flips("quakes", "earthquake", state("mag", null, null), state("mag", null, null))).isFalse();
        }

        @Test
        @DisplayName("numbers compare numerically, so 6 and 6.0 are the same value")
        void numericEquality() {
            assertThat(TriggerPersistGate.jsonEquals(6L, 6.0)).isTrue();
            assertThat(TriggerPersistGate.jsonEquals(List.of(1, "a"), List.of(1.0, "a"))).isTrue();
            assertThat(TriggerPersistGate.jsonEquals(Map.of("a", 1), Map.of("a", 2))).isFalse();
            assertThat(TriggerPersistGate.jsonEquals(true, 1)).isFalse();
            assertThat(TriggerPersistGate.jsonEquals(null, 0)).isFalse();
        }

        @Test
        @DisplayName("a rule for another source or kind never flips")
        void applicability() {
            GateSnapshot scoped = snapshot(rule().sources("ships").when("speed", ">", 10).build());
            assertThat(scoped.flips("quakes", "earthquake", state("speed", 1, null), state("speed", 20, null))).isFalse();
            assertThat(scoped.flips("ships", "vessel", state("speed", 1, null), state("speed", 20, null))).isTrue();
            GateSnapshot kinds = snapshot(rule().kinds("vessel").when("speed", ">", 10).build());
            assertThat(kinds.flips("ships", "buoy", state("speed", 1, null), state("speed", 20, null))).isFalse();
        }
    }

    @Nested
    @DisplayName("geofences")
    class Fences {

        @Test
        @DisplayName("entering or leaving the fence flips, moving inside does not; no geometry is outside")
        void fence() {
            GateSnapshot gate = snapshot(rule().fence(0, 0, 10, 10).build());
            assertThat(gate.flips("ships", "vessel", geo(point(-1, 5)), geo(point(1, 5)))).isTrue();
            assertThat(gate.flips("ships", "vessel", geo(point(1, 5)), geo(point(2, 5)))).isFalse();
            assertThat(gate.flips("ships", "vessel", geo(point(1, 5)), geo(null))).isTrue();
            assertThat(gate.flips("ships", "vessel", geo(point(10, 5)), geo(point(9, 5))))
                    .as("the boundary counts as inside, as ST_Intersects").isFalse();
        }

        @Test
        @DisplayName("a fence and a condition combine with AND")
        void fenceAndCondition() {
            GateSnapshot gate = snapshot(rule().fence(0, 0, 10, 10).when("speed", ">", 5).build());
            assertThat(gate.flips("ships", "vessel", both(point(1, 1), 1), both(point(1, 1), 9))).isTrue();
            assertThat(gate.flips("ships", "vessel", both(point(-1, 1), 1), both(point(-1, 1), 9))).isFalse();
        }

        @Test
        @DisplayName("a hysteresis rule flips on either band fence")
        void hysteresis() {
            TestRules builder = rule().fence(0, 0, 10, 10).mode(TriggerMode.ENTER)
                    .band(box(1, 1, 9, 9), box(-1, -1, 11, 11), 1000);
            GateSnapshot gate = snapshot(builder.build());
            assertThat(gate.flips("ships", "vessel", geo(point(5, 0.5)), geo(point(5, 2)))).as("into the core").isTrue();
            assertThat(gate.flips("ships", "vessel", geo(point(5, 10.5)), geo(point(5, 12)))).as("out of the outer").isTrue();
            assertThat(gate.flips("ships", "vessel", geo(point(5, 0.5)), geo(point(5, 0.7)))).as("inside the band").isFalse();
        }
    }

    @Nested
    @DisplayName("specs")
    class Specs {

        @Test
        @DisplayName("paths and geometry come from the rules applicable to the source")
        void spec() {
            GateSnapshot gate = snapshot(
                    rule().sources("quakes").when("mag", ">", 5).build(),
                    rule().kinds("vessel").fence(0, 0, 1, 1).when("speed", ">", 1).build());
            assertThat(gate.spec("quakes")).isEqualTo(new GateSpec(java.util.Set.of("mag"), false));
            assertThat(gate.spec("ships")).isEqualTo(new GateSpec(java.util.Set.of("speed"), true));
            assertThat(gate.spec("other")).isEqualTo(GateSpec.NONE);
        }

        @Test
        @DisplayName("an empty snapshot needs nothing and never flips")
        void empty() {
            GateSnapshot gate = snapshot();
            assertThat(gate.spec("quakes").isEmpty()).isTrue();
            assertThat(gate.flips("quakes", "earthquake", state("mag", 1, null), state("mag", 9, null))).isFalse();
        }
    }

    // ------------------------------------------------------------------------- helpers

    static GateSnapshot snapshot(RuleSpec... rules) {
        Map<String, SourceDescriptor> descriptors = Map.of("quakes", QUAKES, "ships", SHIPS);
        return new TriggerPersistGate.Compiled(List.of(rules), descriptors::get);
    }

    static GateState state(String path, Object value, Geometry geometry) {
        Map<String, Object> values = new HashMap<>();
        values.put(path, value);
        return new GateState(values, geometry);
    }

    static GateState geo(Geometry geometry) {
        return new GateState(Map.of(), geometry);
    }

    static GateState both(Geometry geometry, Object speed) {
        return state("speed", speed, geometry);
    }
}
