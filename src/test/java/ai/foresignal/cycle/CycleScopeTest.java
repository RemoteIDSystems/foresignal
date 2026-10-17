package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Cycle scopes (prompt 128): stored form, region containment, filters that apply only where an item has a value. */
class CycleScopeTest {

    @Test
    @DisplayName("a stored scope round-trips, cadence-match included")
    void roundTrip() {
        CycleScope s = CycleScope.of(Map.of("regionCodes", List.of("US"), "hypothesisSetIds", "cadence-match",
                "categories", List.of("conflict", " ")));
        assertThat(s.cadenceMatch()).isTrue();
        assertThat(s.categories()).containsExactly("conflict");
        assertThat(CycleScope.of(s.toMap())).isEqualTo(s);
        assertThat(CycleScope.of(null)).isEqualTo(CycleScope.ALL);
    }

    @Test
    @DisplayName("regions match by containment in either direction; empty item regions pass")
    void regions() {
        CycleScope us = CycleScope.of(Map.of("regionCodes", List.of("US")));
        assertThat(us.admits(List.of("US-TX"), null, null, null, null, List.of(), null, "daily")).isTrue();
        assertThat(us.admits(List.of("MX"), null, null, null, null, List.of(), null, "daily")).isFalse();
        assertThat(us.admits(List.of(), null, null, null, null, List.of(), null, "daily")).isTrue();
        CycleScope tx = CycleScope.of(Map.of("regionCodes", List.of("US-TX")));
        assertThat(tx.admits(List.of("US"), null, null, null, null, List.of(), null, "daily")).isTrue();
    }

    @Test
    @DisplayName("themes, situations, sets, sources and categories filter where the item has a value")
    void filters() {
        CycleScope s = new CycleScope(List.of(), List.of("maritime"), List.of("S1"), List.of("H1"), false,
                List.of("ais"), List.of("conflict"), false);
        assertThat(s.admits(List.of(), "aviation", null, null, null, List.of(), null, "daily")).isFalse();
        assertThat(s.admits(List.of(), null, "S2", null, null, List.of(), null, "daily")).isFalse();
        assertThat(s.admits(List.of(), null, null, "H2", null, List.of(), null, "daily")).isFalse();
        assertThat(s.admits(List.of(), null, null, null, null, List.of("adsb"), null, "daily")).isFalse();
        assertThat(s.admits(List.of(), null, null, null, null, List.of(), "markets", "daily")).isFalse();
        assertThat(s.admits(List.of(), "maritime", "S1", "H1", "daily", List.of("ais"), "conflict", "daily")).isTrue();
        CycleScope focused = CycleScope.focusedOn("S1", List.of("H1"));
        assertThat(focused.admits(List.of("US"), null, null, "H1", null, List.of(), null, "on-demand")).isTrue();
        assertThat(focused.admits(List.of("US"), null, null, null, null, List.of(), null, "on-demand")).isFalse();
    }
}
