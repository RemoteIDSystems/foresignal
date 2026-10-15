package ai.foresignal.infra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Dependency-rule invariants (prompt 104): proximity is never better than possible. */
class DependencyRulesTest {

    @Test
    @DisplayName("the label ranks order speculative < possible < likely < confirmed")
    void labelRanks() {
        assertThat(InfraImpactService.LABEL_RANK.get("speculative")).isLessThan(
                InfraImpactService.LABEL_RANK.get("possible"));
        assertThat(InfraImpactService.LABEL_RANK.get("possible")).isLessThan(
                InfraImpactService.LABEL_RANK.get("likely"));
        assertThat(InfraImpactService.LABEL_RANK.get("likely")).isLessThan(
                InfraImpactService.LABEL_RANK.get("confirmed"));
    }

    @Test
    @DisplayName("the label of a rank round-trips")
    void labelOf() {
        assertThat(InfraImpactService.label(1)).isEqualTo("possible");
        assertThat(InfraImpactService.label(3)).isEqualTo("confirmed");
    }
}
