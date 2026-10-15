package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-lightning-holdover} cell identity (prompt 099). */
class LightningHoldoverTest {

    @Test
    @DisplayName("candidates are keyed by res-6 cell and UTC day")
    void ids() {
        long cell = LightningHoldoverGenerator.cell(-95.5, 32.5);
        assertThat(LightningHoldoverGenerator.id(cell, "2026-10-15")).startsWith("h3:").endsWith(":2026-10-15");
        assertThat(LightningHoldoverGenerator.cell(-95.5, 32.5)).isEqualTo(cell);
    }
}
