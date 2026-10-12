package ai.foresignal.security.visibility;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link Viewer} (prompt 039): who a record would be shown to. */
class ViewerTest {

    @Test
    @DisplayName("anonymous never has the grant; admin sees private; factories read tokens and principals")
    void factories() {
        assertThat(new Viewer(null, true, null).privateAccess()).isFalse();
        assertThat(Viewer.anonymous().canSeePrivate()).isFalse();
        assertThat(Viewer.admin().canSeePrivate()).isTrue();
        assertThat(Viewer.admin().isAdmin()).isTrue();
        assertThat(Viewer.of(Optional.empty())).isEqualTo(Viewer.anonymous());
        CurrentToken token = new CurrentToken(new AuthenticatedToken(UUID.randomUUID(), "t", Scope.AGENT, null, null,
                "p", false, true), false);
        Viewer agent = Viewer.of(Optional.of(token));
        assertThat(agent.canSeePrivate()).isTrue();
        assertThat(agent.isAdmin()).isFalse();
        assertThat(Viewer.of((RequestPrincipal) null)).isEqualTo(Viewer.anonymous());
        assertThat(Viewer.of(RequestPrincipal.anonymous(ReadRestriction.NONE))).isEqualTo(Viewer.anonymous());
        assertThat(Viewer.of(new RequestPrincipal(RequestPrincipal.Kind.FEED_KEY, UUID.randomUUID(), "k", Scope.READ,
                null, true)).canSeePrivate()).isTrue();
    }
}
