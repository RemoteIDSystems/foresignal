package ai.foresignal.source.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ReconnectPolicy} and {@link HostPort} (prompt 082). */
class ReconnectPolicyTest {

    private static final ReconnectPolicy APRS = new ReconnectPolicy(Duration.ofSeconds(30), Duration.ofMinutes(15), 0.2);

    @Test
    @DisplayName("the base doubles from 30 s and stops at 15 min")
    void base() {
        assertThat(APRS.base(0)).isEqualTo(Duration.ofSeconds(30));
        assertThat(APRS.base(1)).isEqualTo(Duration.ofSeconds(60));
        assertThat(APRS.base(4)).isEqualTo(Duration.ofSeconds(480));
        assertThat(APRS.base(5)).isEqualTo(Duration.ofMinutes(15));
        assertThat(APRS.base(500)).isEqualTo(Duration.ofMinutes(15));
        assertThat(APRS.base(-3)).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("jitter stays within ±20% of the base at the extremes of the random value")
    void jitterBounds() {
        for (int attempt = 0; attempt < 8; attempt++) {
            long base = APRS.base(attempt).toMillis();
            assertThat(APRS.delay(attempt, () -> 0.0).toMillis()).isEqualTo(Math.round(base * 0.8));
            assertThat(APRS.delay(attempt, () -> 0.999999).toMillis()).isBetween(Math.round(base * 1.19), Math.round(base * 1.2));
            assertThat(APRS.delay(attempt, () -> 0.5).toMillis()).isEqualTo(base);
        }
        assertThat(ReconnectPolicy.exponential(Duration.ofSeconds(5), Duration.ofMinutes(5)).delay(3, () -> 0.0))
                .isEqualTo(Duration.ofSeconds(40));
    }

    @Test
    @DisplayName("bad bounds are refused")
    void bounds() {
        assertThatThrownBy(() -> new ReconnectPolicy(Duration.ZERO, Duration.ofSeconds(1), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReconnectPolicy(Duration.ofSeconds(2), Duration.ofSeconds(1), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReconnectPolicy(Duration.ofSeconds(1), Duration.ofSeconds(1), 0.6))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("HostPort parses host:port, lower-cases the host and refuses malformed input")
    void hostPort() {
        assertThat(HostPort.parse("Rotate.APRS2.net:14580")).isEqualTo(new HostPort("rotate.aprs2.net", 14580));
        assertThat(HostPort.parse("rotate.aprs2.net:14580").toString()).isEqualTo("rotate.aprs2.net:14580");
        for (String bad : new String[] {"rotate.aprs2.net", ":14580", "host:", "host:x", "host:0", "ho st:1", "a_b:1"}) {
            assertThatThrownBy(() -> HostPort.parse(bad)).as(bad).isInstanceOf(IllegalArgumentException.class);
        }
        SourceDescriptor d = SourceDescriptor.builder("x").kind("k").allowedHost("rotate.aprs2.net:14580").build();
        assertThat(d.allowsHost(HostPort.parse("rotate.aprs2.net:14580"))).isTrue();
        assertThat(d.allowsHost(HostPort.parse("rotate.aprs2.net:10152"))).isFalse();
        assertThat(d.allowsHost(null)).isFalse();
    }
}
