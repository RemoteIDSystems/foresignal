package ai.foresignal.sink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.net.InetAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** SERVE bind and peer rules (prompt 110; Gap §4a). */
class ServeGuardTest {

    private final ServeGuard guard = new ServeGuard();

    @Test
    @DisplayName("loopback and RFC 1918 binds are allowed, a public bind is refused")
    void bind() {
        assertThat(guard.resolveBind("127.0.0.1", false, false).isLoopbackAddress()).isTrue();
        assertThat(guard.resolveBind("192.168.1.10", false, false)).isNotNull();
        assertThatThrownBy(() -> guard.resolveBind("8.8.8.8", false, false)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("0.0.0.0 needs allowAllInterfaces")
    void allInterfaces() {
        assertThatThrownBy(() -> guard.resolveBind("0.0.0.0", false, false)).isInstanceOf(ValidationException.class);
        assertThat(guard.resolveBind("0.0.0.0", true, false).isAnyLocalAddress()).isTrue();
    }

    @Test
    @DisplayName("100.64/10 needs allowTailnet; a public peer is never allowed")
    void peers() throws Exception {
        InetAddress tailnet = InetAddress.getByName("100.64.0.1");
        assertThat(guard.allowedPeer(tailnet, false)).isFalse();
        assertThat(guard.allowedPeer(tailnet, true)).isTrue();
        assertThat(guard.allowedPeer(InetAddress.getByName("8.8.8.8"), true)).isFalse();
        assertThat(guard.allowedPeer(InetAddress.getByName("127.0.0.1"), false)).isTrue();
        assertThat(guard.allowedPeer(InetAddress.getByName("10.0.0.5"), false)).isTrue();
    }
}
