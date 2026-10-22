package ai.foresignal.situation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The tunable situation activation rule (prompt 154): the 2-families / 2-groups gate is now config. */
class SituationActivationThresholdTest {

    @Test
    @DisplayName("the default rule is two families and two independent groups")
    void defaults() {
        assertThat(SituationLifecycle.meetsActivation(2, 2)).isTrue();
        assertThat(SituationLifecycle.meetsActivation(1, 2)).isFalse();
        assertThat(SituationLifecycle.meetsActivation(2, 1)).isFalse();
    }

    @Test
    @DisplayName("custom thresholds raise or lower the bar")
    void custom() {
        assertThat(SituationLifecycle.meetsActivation(2, 2, 3, 2)).isFalse();
        assertThat(SituationLifecycle.meetsActivation(3, 2, 3, 2)).isTrue();
        assertThat(SituationLifecycle.meetsActivation(2, 1, 2, 1)).isTrue();
    }

    @Test
    @DisplayName("an emerging situation activates only once the configured thresholds are met")
    void afterMembers() {
        assertThat(SituationLifecycle.afterMembers("emerging", 2, 2, true, 3, 2))
                .isEqualTo(SituationLifecycle.EMERGING);
        assertThat(SituationLifecycle.afterMembers("emerging", 3, 2, true, 3, 2))
                .isEqualTo(SituationLifecycle.ACTIVE);
    }
}
