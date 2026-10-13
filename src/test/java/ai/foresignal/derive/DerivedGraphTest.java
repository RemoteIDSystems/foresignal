package ai.foresignal.derive;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.wx.WxAreasGenerator;
import ai.foresignal.source.adapters.wx.WxCellsGenerator;
import ai.foresignal.source.adapters.wx.WxPlacesGenerator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The derived-input graph's cycle guard (prompt 052). */
class DerivedGraphTest {

    private static SourceDescriptor derived(String id, String kind) {
        return SourceDescriptor.builder(id).name(id).kind(kind).licence("derived").mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1)).build();
    }

    @Test
    @DisplayName("the shipped weather generators form no cycle (wx-cells feeds wx-places)")
    void weatherGraphIsAcyclic() {
        Map<SourceDescriptor, DerivedInputs> nodes = new LinkedHashMap<>();
        WxPlacesGenerator places = new WxPlacesGenerator();
        WxCellsGenerator cells = new WxCellsGenerator("small");
        WxAreasGenerator areas = new WxAreasGenerator();
        nodes.put(places.descriptor(), places.inputs(ValidatedParams.empty()));
        nodes.put(cells.descriptor(), cells.inputs(ValidatedParams.empty()));
        nodes.put(areas.descriptor(), areas.inputs(ValidatedParams.empty()));
        assertThat(DerivedGraph.cycle(nodes)).isEmpty();
    }

    @Test
    @DisplayName("a two-node cycle is found and named")
    void twoNodeCycle() {
        Map<SourceDescriptor, DerivedInputs> nodes = new LinkedHashMap<>();
        nodes.put(derived("a", "kind-a"), new DerivedInputs(Set.of(), Set.of("kind-b"), Set.of(), null, false));
        nodes.put(derived("b", "kind-b"), new DerivedInputs(Set.of("a"), Set.of(), Set.of(), null, false));
        Optional<List<String>> cycle = DerivedGraph.cycle(nodes);
        assertThat(cycle).isPresent();
        assertThat(cycle.get()).containsSubsequence("a", "b").endsWith(cycle.get().get(0));
    }

    @Test
    @DisplayName("a generator subscribed to its own kind is a cycle")
    void selfLoop() {
        Map<SourceDescriptor, DerivedInputs> nodes = Map.of(derived("self", "kind-s"),
                new DerivedInputs(Set.of(), Set.of("kind-s"), Set.of(), Duration.ofMinutes(1), false));
        assertThat(DerivedGraph.cycle(nodes)).contains(List.of("self", "self"));
    }

    @Test
    @DisplayName("a chain and frame-only inputs are not cycles")
    void chain() {
        Map<SourceDescriptor, DerivedInputs> nodes = new LinkedHashMap<>();
        nodes.put(derived("a", "kind-a"), new DerivedInputs(Set.of(), Set.of(), Set.of("mrms-reflectivity"), null,
                false));
        nodes.put(derived("b", "kind-b"), new DerivedInputs(Set.of("a"), Set.of("kind-a"), Set.of(), null, false));
        nodes.put(derived("c", "kind-c"), new DerivedInputs(Set.of(), Set.of("kind-b"), Set.of(), null, false));
        assertThat(DerivedGraph.cycle(nodes)).isEmpty();
    }

    @Test
    @DisplayName("change matching: sources and kinds are ANDed; empty sets match all; nothing without either")
    void matching() {
        DerivedInputs byKind = new DerivedInputs(Set.of(), Set.of("radar-cell"), Set.of(), null, false);
        assertThat(byKind.matchesChange("any", "radar-cell")).isTrue();
        assertThat(byKind.matchesChange("any", "other")).isFalse();
        DerivedInputs both = new DerivedInputs(Set.of("wx-cells"), Set.of("radar-cell"), Set.of(), null, false);
        assertThat(both.matchesChange("wx-cells", "radar-cell")).isTrue();
        assertThat(both.matchesChange("other", "radar-cell")).isFalse();
        assertThat(DerivedInputs.none().wantsChanges()).isFalse();
        assertThat(DerivedInputs.none().matchesChange("a", "b")).isFalse();
        assertThat(both.matchesProduct("mrms-reflectivity")).isFalse();
    }
}
