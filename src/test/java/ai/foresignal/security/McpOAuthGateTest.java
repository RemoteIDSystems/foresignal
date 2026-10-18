package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link McpOAuthGate} (prompt 141): which bearers are treated as identity-provider JWTs, and the outcomes. */
class McpOAuthGateTest {

    @Test
    @DisplayName("only header.payload.signature starting eyJ looks like a JWT; Foresignal tokens and keys never do")
    void looksLikeJwt() {
        assertThat(McpOAuthGate.looksLikeJwt("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ4In0.c2ln")).isTrue();
        assertThat(McpOAuthGate.looksLikeJwt("eyJhbGciOiJub25lIn0.eyJzdWIiOiJ4In0.")).isTrue();
        assertThat(McpOAuthGate.looksLikeJwt("fsr_0123456789abcdef")).isFalse();
        assertThat(McpOAuthGate.looksLikeJwt("eik_abc.def.ghi")).isFalse();
        assertThat(McpOAuthGate.looksLikeJwt("eyJa.b.c.d.e")).as("JWE (five parts)").isFalse();
        assertThat(McpOAuthGate.looksLikeJwt("eyJa..c")).isFalse();
        assertThat(McpOAuthGate.looksLikeJwt("eyJabc")).isFalse();
        assertThat(McpOAuthGate.looksLikeJwt(null)).isFalse();
    }

    @Test
    @DisplayName("Accepted requires the token, issuer and subject and copies the scopes")
    void accepted() {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "oauth:x", Scope.READ, null, null, null,
                false);
        McpOAuthGate.Accepted accepted = new McpOAuthGate.Accepted(token, "https://idp", "x", null);
        assertThat(accepted.idpScopes()).isEmpty();
        assertThat(new McpOAuthGate.Accepted(token, "https://idp", "x", Set.of("a")).idpScopes()).containsExactly("a");
        assertThatThrownBy(() -> new McpOAuthGate.Accepted(null, "https://idp", "x", Set.of()))
                .isInstanceOf(NullPointerException.class);
    }
}
