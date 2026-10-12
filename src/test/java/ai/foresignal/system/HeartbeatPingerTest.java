package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Watchdog URLs never reach the log whole (prompt 019). */
class HeartbeatPingerTest {

    @Test
    @DisplayName("redaction keeps scheme, host and port and drops the path and query")
    void redact() {
        assertThat(HeartbeatPinger.redact("https://hc-ping.com/0f3a-uuid?x=1")).isEqualTo("https://hc-ping.com/…");
        assertThat(HeartbeatPinger.redact("http://kuma.lan:3001/api/push/abc")).isEqualTo("http://kuma.lan:3001/…");
        assertThat(HeartbeatPinger.redact(null)).isNull();
        assertThat(HeartbeatPinger.redact("::not a url")).isEqualTo("<invalid url>");
    }
}
