package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.GateSpec;
import ai.foresignal.ingest.GateState;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.TriggerMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * The persist gate with prompt 036's conditions (no Spring): identity conditions read the path of the
 * source being judged, and an anchored rule compares both sides against the current anchor, treats
 * the uncertainty band conservatively and is suspended without an anchor.
 */
class GateIdentityAnchorTest {

    static final SourceDescriptor A = SourceDescriptor.builder("a").name("A").kind("plane").mode(Mode.PUSH)
            .defaultBudget(Budget.of(60, 10_000)).entityKey("icao24", "hex").build();
    static final SourceDescriptor B = SourceDescriptor.builder("b").name("B").kind("plane").mode(Mode.PUSH)
            .defaultBudget(Budget.of(60, 10_000)).entityKey("icao24", "icao").build();

    /** An identity operator over a fixed set, standing in for {@code inWatchlist}. */
    static final class Listed implements IdentityOperator {

        final Set<String> values;

        Listed(Set<String> values) {
            this.values = values;
        }

        @Override
        public String name() {
            return "inWatchlist";
        }

        @Override
        public Object normalize(Object value) {
            return value;
        }

        @Override
        public String sql(SqlTerm term, Object value) {
            return "true";
        }

        @Override
        public Evaluation evaluate(Object actual, Object value) {
            return actual instanceof String s ? Evaluation.of(values.contains(s)) : Evaluation.AMBIGUOUS;
        }

        @Override
        public Map<String, String> identityPaths(Object value, Collection<String> sources) {
            return Map.of("a", "hex", "b", "icao");
        }

        @Override
        public List<String> sourcesWithoutIdentity(Object value, Collection<String> sources) {
            return List.of();
        }

        @Override
        public boolean requiresPath(Object value) {
            return false;
        }

        @Override
        public boolean known(Object value) {
            return true;
        }
    }

    static GateState state(Map<String, Object> values, Geometry geometry) {
        return new GateState(values, geometry);
    }

    static Point point(double lon, double lat) {
        return TestRules.GEO.createPoint(new Coordinate(lon, lat));
    }

    @Test
    @DisplayName("an identity condition gathers each source's own path and judges it per source")
    void identity() {
        Listed op = new Listed(Set.of("A1B2C3"));
        TestRules builder = TestRules.rule();
        builder.conditions.add(new RuleCondition(null, op, "list", op.identityPaths("list", List.of())));
        TriggerPersistGate.Compiled gate = new TriggerPersistGate.Compiled(List.of(builder.build()),
                id -> "a".equals(id) ? A : B);
        GateSpec specA = gate.spec("a");
        GateSpec specB = gate.spec("b");
        assertThat(specA.paths()).containsExactly("hex");
        assertThat(specB.paths()).containsExactly("icao");

        assertThat(gate.flips("a", "plane", state(Map.of("hex", "FFFFFF"), null), state(Map.of("hex", "A1B2C3"), null)))
                .isTrue();
        assertThat(gate.flips("b", "plane", state(Map.of("icao", "A1B2C3"), null), state(Map.of("icao", "A1B2C3"), null)))
                .isFalse();
        assertThat(gate.flips("c", "plane", state(Map.of(), null), state(Map.of("hex", "A1B2C3"), null)))
                .as("a source without the identity never flips").isFalse();
    }

    @Test
    @DisplayName("an anchored rule flips on crossing the radius around the current anchor, is conservative in the band, and is suspended without an anchor")
    void anchored() {
        NearAnchor anchor = new NearAnchor("phone", "p", 50, null, 200);
        RuleSpec rule = TestRules.rule().mode(TriggerMode.ENTER).build().withAnchor(anchor);
        assertThat(rule.anchored()).isTrue();
        assertThat(rule.spatial()).isTrue();
        TriggerPersistGate.Compiled gate = new TriggerPersistGate.Compiled(List.of(rule), id -> A, a -> point(0, 0));
        assertThat(gate.spec("a").needsGeometry()).isTrue();
        assertThat(gate.flips("a", "plane", state(Map.of(), point(1, 0)), state(Map.of(), point(0.1, 0)))).isTrue();
        assertThat(gate.flips("a", "plane", state(Map.of(), point(1, 0)), state(Map.of(), point(1.1, 0)))).isFalse();
        assertThat(gate.flips("a", "plane", state(Map.of(), point(0.4497, 0)), state(Map.of(), point(0.4497, 0))))
                .as("ambiguous but unchanged: no flip").isFalse();
        assertThat(gate.flips("a", "plane", state(Map.of(), point(0.4497, 0)), state(Map.of(), point(0.4498, 0))))
                .as("ambiguous and moved: persisted").isTrue();

        TriggerPersistGate.Compiled stale = new TriggerPersistGate.Compiled(List.of(rule), id -> A, a -> null);
        assertThat(stale.flips("a", "plane", state(Map.of(), point(1, 0)), state(Map.of(), point(0.1, 0)))).isFalse();
    }

    @Test
    @DisplayName("geometryEquals treats absent geometries alike")
    void geometryEquals() {
        assertThat(TriggerPersistGate.geometryEquals(null, null)).isTrue();
        assertThat(TriggerPersistGate.geometryEquals(point(0, 0), point(0, 0))).isTrue();
        assertThat(TriggerPersistGate.geometryEquals(point(0, 0), null)).isFalse();
    }
}
