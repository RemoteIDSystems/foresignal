package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Handles derived from token names (prompt 023). */
class PrincipalServiceTest {

    @Test
    @DisplayName("handles are lower case, hyphenated, at most 32 characters and match the spec pattern")
    void handles() {
        assertThat(PrincipalService.handleFrom("OpenClaw Agent")).isEqualTo("openclaw-agent");
        assertThat(PrincipalService.handleFrom("Café phone!")).isEqualTo("cafe-phone");
        assertThat(PrincipalService.handleFrom("--x")).isEqualTo("token-x");
        assertThat(PrincipalService.handleFrom("")).isEqualTo("token");
        assertThat(PrincipalService.handleFrom(null)).isEqualTo("token");
        assertThat(PrincipalService.handleFrom("a".repeat(50))).hasSize(32);
        for (String name : new String[] {"OpenClaw Agent", "Café phone!", "--x", "", "a".repeat(50), "a b-c.d_e"}) {
            assertThat(PrincipalService.handleFrom(name)).matches(PrincipalService.HANDLE);
        }
    }
}
