package ai.foresignal.source.adapters.sanctions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The delisting shrink guard (prompt 091, FR-15). */
class DelistingGuardTest {

    @Test
    @DisplayName("a 20% shrink is implausible; a small change is not")
    void implausible() {
        assertThat(DelistingGuard.implausible(100, 80)).isTrue();
        assertThat(DelistingGuard.implausible(100, 95)).isFalse();
        assertThat(DelistingGuard.implausible(0, 0)).isFalse();
    }

    @Test
    @DisplayName("gone() reports the ids present before and absent now")
    void gone() {
        assertThat(DelistingGuard.gone(List.of("a", "b", "c"), List.of("a", "c"))).containsExactly("b");
        assertThat(DelistingGuard.gone(List.of(), List.of("a"))).isEmpty();
        assertThat(DelistingGuard.gone(List.of("a"), List.of())).containsExactly("a");
    }
}
