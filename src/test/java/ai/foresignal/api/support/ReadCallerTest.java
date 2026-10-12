package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ReadCaller} (prompt 039): the private grant is part of the caller's cache identity. */
class ReadCallerTest {

    @Test
    @DisplayName("canonical() appends +private after the scope when the caller holds the private grant")
    void canonical() {
        assertThat(new ReadCaller(Scope.AGENT, false, true).canonical()).isEqualTo("agent/false");
        assertThat(new ReadCaller(Scope.AGENT, false, true, null, true).canonical()).isEqualTo("agent+private/false");
        assertThat(ReadCaller.anonymous().canonical()).isEqualTo("anon/false");
        assertThat(ReadCaller.anonymous().privateAccess()).isFalse();
    }

    @Test
    @DisplayName("withRestriction keeps the scope, settings and the private grant")
    void withRestriction() {
        ReadCaller granted = new ReadCaller(Scope.READ, true, false, null, true);
        ReadRestriction narrowed = new ReadRestriction(java.util.List.of("a"), null, null, null, null, false, null);
        ReadCaller next = granted.withRestriction(narrowed);
        assertThat(next.privateAccess()).isTrue();
        assertThat(next.scope()).isEqualTo(Scope.READ);
        assertThat(next.exposeObserver()).isTrue();
        assertThat(next.openReads()).isFalse();
        assertThat(next.restriction()).isSameAs(narrowed);
    }
}
