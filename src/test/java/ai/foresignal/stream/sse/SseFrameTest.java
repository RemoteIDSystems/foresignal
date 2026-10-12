package ai.foresignal.stream.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Wire form of frames, limits defaults and principals. */
class SseFrameTest {

    @Test
    @DisplayName("an id-bearing frame writes id, event and data; a control frame writes no id line")
    void wire() {
        assertThat(new SseFrame("5", "change", "{}").wire()).isEqualTo("id:5\nevent:change\ndata:{}\n\n");
        assertThat(SseFrame.control("heartbeat", "{}").wire()).isEqualTo("event:heartbeat\ndata:{}\n\n");
        assertThatThrownBy(() -> SseFrame.control("x", "a\nb")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SseFrame("1\n2", "x", "{}")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("limits default to 100, 1,000, 10,000, 15 s, 1 s, 50,000, 8 and 4; partial values keep defaults")
    void limits() {
        StreamLimits d = StreamLimits.defaults();
        assertThat(d).isEqualTo(new StreamLimits(100, 1000, 10_000, 15, 1000, 50_000, 8, 4));
        StreamLimits partial = new StreamLimits(5, 0, -1, 0, 0, 0, 2, 0);
        assertThat(partial.maxClients()).isEqualTo(5);
        assertThat(partial.clientBuffer()).isEqualTo(1000);
        assertThat(partial.replayCap()).isEqualTo(10_000);
        assertThat(partial.perPrincipalLimit(StreamPrincipal.anonymous("1.2.3.4"))).isEqualTo(4);
        assertThat(partial.perPrincipalLimit(new StreamPrincipal("token:abc", ai.foresignal.security.Scope.READ, null)))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("anonymous principals key on the address and count as read")
    void principals() {
        StreamPrincipal anon = StreamPrincipal.anonymous("10.0.0.2");
        assertThat(anon.id()).isEqualTo("anon:10.0.0.2");
        assertThat(anon.anonymous()).isTrue();
        assertThat(anon.allows(ai.foresignal.security.Scope.READ)).isTrue();
        assertThat(anon.allows(ai.foresignal.security.Scope.AGENT)).isFalse();
        StreamPrincipal admin = new StreamPrincipal("token:x", ai.foresignal.security.Scope.ADMIN, "10.0.0.2");
        assertThat(admin.allows(ai.foresignal.security.Scope.AGENT)).isTrue();
        assertThat(StreamPrincipal.anonymous(null).id()).isEqualTo("anon:unknown");
    }
}
