package ai.foresignal.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.MutableClock;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The {@code env.foresignal_log} host import (prompt 141): level mapping, sanitizing, truncation and both rate limits.
 */
class PluginLogTest {

    static PluginLimits limits(int perCall, int perMinute, int maxBytes) {
        return new PluginLimits(1024, Duration.ofSeconds(5), 1024, 1024, 10, perCall, perMinute, maxBytes);
    }

    @Test
    @DisplayName("levels clamp to 0..3: negative is debug, 3 and above are error")
    void levels() {
        assertThat(PluginLog.normalize(-5)).isZero();
        assertThat(PluginLog.normalize(0)).isZero();
        assertThat(PluginLog.normalize(1)).isEqualTo(1);
        assertThat(PluginLog.normalize(2)).isEqualTo(2);
        assertThat(PluginLog.normalize(3)).isEqualTo(3);
        assertThat(PluginLog.normalize(99)).isEqualTo(3);
    }

    @Test
    @DisplayName("control characters and line separators become spaces, so a plugin cannot forge log lines")
    void sanitize() {
        String forged = "ok\r\n2026-10-18 ERROR fake\u0000 x";
        assertThat(PluginLog.sanitize(forged.getBytes(StandardCharsets.UTF_8), 1024))
                .isEqualTo("ok  2026-10-18 ERROR fake  x");
        assertThat(PluginLog.sanitize("abcdef".getBytes(StandardCharsets.UTF_8), 3)).isEqualTo("abc");
    }

    @Test
    @DisplayName("per-call and per-minute limits; bad pointers count as dropped")
    void rateLimits() {
        MutableClock clock = new MutableClock();
        List<String> lines = new ArrayList<>();
        PluginLog log = new PluginLog("p", limits(2, 3, 16), clock, (level, message) -> lines.add(message));

        PluginLog.Call first = log.startCall();
        first.log(1, "a".getBytes());
        first.log(1, "b".getBytes());
        first.log(1, "c".getBytes());
        first.log(1, null);
        first.finish();
        assertThat(first.kept()).isEqualTo(2);
        assertThat(first.dropped()).isEqualTo(2);

        PluginLog.Call second = log.startCall();
        second.log(1, "d".getBytes());
        second.log(1, "e".getBytes());
        assertThat(second.kept()).as("the minute allows 3").isEqualTo(1);
        assertThat(lines).containsExactly("a", "b", "d");

        clock.advance(Duration.ofMinutes(1));
        PluginLog.Call third = log.startCall();
        third.log(2, "f".getBytes());
        assertThat(third.kept()).isEqualTo(1);
        assertThat(lines).containsExactly("a", "b", "d", "f");
    }

    @Test
    @DisplayName("the default sink writes to the per-plugin logger without failing")
    void loggerSink() {
        PluginLog.Sink sink = PluginLog.loggerSink("example");
        for (int level = 0; level <= 3; level++) {
            sink.write(level, "message " + level);
        }
        PluginLog log = new PluginLog("example", PluginLimits.DEFAULTS, null, null);
        PluginLog.Call call = log.startCall();
        call.log(1, "hello".getBytes());
        assertThat(call.kept()).isEqualTo(1);
    }
}
