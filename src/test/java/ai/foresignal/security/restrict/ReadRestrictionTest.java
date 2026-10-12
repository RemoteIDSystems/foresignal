package ai.foresignal.security.restrict;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.TokenRestrictions;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ReadRestriction} (prompt 035). */
class ReadRestrictionTest {

    private static final Map<String, Object> SQUARE = Map.of("type", "Polygon", "coordinates",
            List.of(List.of(List.of(0, 0), List.of(20, 0), List.of(20, 20), List.of(0, 20), List.of(0, 0))));

    @Test
    @DisplayName("built from a stored restriction, with the excludePrivate default")
    void fromStored() {
        ReadRestriction r = ReadRestriction.of(new TokenRestrictions(List.of("b", "a"), null, null, null), true);
        assertThat(r.sources()).containsExactlyInAnyOrder("a", "b");
        assertThat(r.excludePrivate()).isTrue();
        assertThat(r.hasDataRestrictions()).isTrue();
        assertThat(ReadRestriction.of(null, false).isUnrestricted()).isTrue();
        TokenRestrictions explicit = new TokenRestrictions(null, null, null, null, null, Boolean.FALSE, 60);
        assertThat(ReadRestriction.of(explicit, true).excludePrivate()).isFalse();
        assertThat(ReadRestriction.of(explicit, true).maxRequestsPerMinute()).isEqualTo(60);
    }

    @Test
    @DisplayName("bbox and area together restrict to their intersection")
    void regionIntersection() {
        ReadRestriction r = new ReadRestriction(null, null, List.of(10.0, 10.0, 30.0, 30.0), ReadRestriction.area(SQUARE),
                null, false, null);
        var e = r.regionEnvelope();
        assertThat(e.getMinX()).isEqualTo(10.0);
        assertThat(e.getMaxX()).isEqualTo(20.0);
        assertThat(new ReadRestriction(null, null, null, null, null, false, null).region()).isNull();
    }

    @Test
    @DisplayName("within: narrower sources, kinds and region are within; wider or unrestricted are not")
    void within() {
        ReadRestriction outer = new ReadRestriction(List.of("a", "b"), null, List.of(0.0, 0.0, 50.0, 50.0), null, null,
                true, null);
        ReadRestriction inner = new ReadRestriction(List.of("a"), List.of("k"), List.of(1.0, 1.0, 2.0, 2.0), null, null,
                true, null);
        assertThat(inner.within(outer)).isTrue();
        assertThat(outer.within(inner)).isFalse();
        assertThat(ReadRestriction.NONE.within(outer)).isFalse();
        assertThat(ReadRestriction.NONE.within(ReadRestriction.NONE)).isTrue();
        assertThat(outer.within(null)).isTrue();
    }

    @Test
    @DisplayName("canonical text differs between restrictions and is empty for none")
    void canonical() {
        assertThat(ReadRestriction.NONE.canonical()).isEmpty();
        ReadRestriction a = new ReadRestriction(List.of("a"), null, null, null, null, true, null);
        ReadRestriction b = new ReadRestriction(List.of("b"), null, null, null, null, true, null);
        assertThat(a.canonical()).isNotEqualTo(b.canonical());
        assertThat(a).isEqualTo(new ReadRestriction(List.of("a"), null, null, null, null, true, null));
        assertThat(a.hashCode()).isEqualTo(new ReadRestriction(List.of("a"), null, null, null, null, true, null).hashCode());
    }

    @Test
    @DisplayName("the stored form round-trips the data members and is null for none")
    void storedForm() {
        assertThat(ReadRestriction.NONE.toStored()).isNull();
        ReadRestriction r = new ReadRestriction(List.of("a"), List.of("k"), null, ReadRestriction.area(SQUARE), null,
                true, null);
        TokenRestrictions stored = r.toStored();
        assertThat(stored.sources()).containsExactly("a");
        assertThat(stored.area()).containsEntry("type", "Polygon");
        assertThat(ReadRestriction.of(stored, false).region().equalsTopo(r.region())).isTrue();
    }

    @Test
    @DisplayName("withers replace one member")
    void withers() {
        ReadRestriction r = new ReadRestriction(List.of("a"), null, null, null, List.of("/v1/records"), false, null);
        assertThat(r.withExcludePrivate(true).excludePrivate()).isTrue();
        assertThat(r.withExcludePrivate(false)).isSameAs(r);
        assertThat(r.withEndpoints(null).endpoints()).isNull();
        assertThat(r.allowsSource("a")).isTrue();
        assertThat(r.allowsSource("b")).isFalse();
        assertThat(r.allowsKind("anything")).isTrue();
    }
}
