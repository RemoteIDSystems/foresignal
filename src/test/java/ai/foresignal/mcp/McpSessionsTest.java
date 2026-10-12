package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import java.time.Clock;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Session binding (prompt 025). */
class McpSessionsTest {

    private final McpSessions sessions = new McpSessions(Clock.systemUTC());

    private static McpCaller caller(Scope scope) {
        AuthenticatedToken token = scope == null ? null : new AuthenticatedToken(UUID.randomUUID(), "t", scope,
                TokenRestrictions.NONE, null, "p", scope == Scope.ADMIN);
        return new McpCaller(token, new ReadCaller(scope, false, true), Set.of(), null);
    }

    @Test
    @DisplayName("a session is bound to the caller of the request that opened it")
    void binding() {
        McpCaller reader = caller(Scope.READ);
        McpCaller other = caller(Scope.READ);
        sessions.callAs(reader, () -> {
            sessions.bind("s1", McpSessions.STREAMABLE);
            return null;
        });
        assertThat(sessions.matches("s1", reader)).isTrue();
        assertThat(sessions.matches("s1", other)).isFalse();
        assertThat(sessions.matches("s1", caller(null))).isFalse();
        assertThat(sessions.matches("missing", reader)).isFalse();
        assertThat(sessions.matches(null, reader)).isFalse();
        assertThat(sessions.binding("s1")).get().extracting(McpSessions.Binding::transport)
                .isEqualTo(McpSessions.STREAMABLE);
    }

    @Test
    @DisplayName("a session opened outside a request is bound to nobody (fails closed)")
    void unbound() {
        sessions.bind("s2", McpSessions.LEGACY_SSE);
        assertThat(sessions.binding("s2")).get().extracting(McpSessions.Binding::callerKey)
                .isEqualTo(McpSessions.UNBOUND);
        assertThat(sessions.matches("s2", caller(null))).isFalse();
    }

    @Test
    @DisplayName("scopes nest and restore; sessions are counted per transport and removed")
    void scopesAndCounts() {
        McpCaller outer = caller(Scope.AGENT);
        McpCaller inner = caller(null);
        try (McpSessions.CallerScope a = sessions.open(outer)) {
            try (McpSessions.CallerScope b = sessions.open(inner)) {
                sessions.bind("in", McpSessions.LEGACY_SSE);
            }
            sessions.bind("out", McpSessions.STREAMABLE);
        }
        assertThat(sessions.matches("in", inner)).isTrue();
        assertThat(sessions.matches("out", outer)).isTrue();
        assertThat(sessions.count()).isEqualTo(2);
        assertThat(sessions.count(McpSessions.STREAMABLE)).isEqualTo(1);
        sessions.remove("out");
        sessions.remove(null);
        assertThat(sessions.count()).isEqualTo(1);
        sessions.clear();
        assertThat(sessions.count()).isZero();
    }

    @Test
    @DisplayName("caller keys and actors: token id, anonymous, the bootstrap token as admin")
    void callerKeys() {
        McpCaller anonymous = caller(null);
        assertThat(anonymous.key()).isEqualTo(McpCaller.ANONYMOUS_KEY);
        assertThat(anonymous.anonymous()).isTrue();
        assertThat(anonymous.currentToken()).isEmpty();
        McpCaller reader = caller(Scope.READ);
        assertThat(reader.key()).isEqualTo("token:" + reader.token().id());
        assertThat(reader.actor()).isEqualTo("token:t");
        assertThat(reader.currentToken()).isPresent();
        assertThat(caller(Scope.ADMIN).actor()).isEqualTo("admin");
    }
}
