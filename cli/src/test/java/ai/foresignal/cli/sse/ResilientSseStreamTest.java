package ai.foresignal.cli.sse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ai.foresignal.cli.config.Connection;
import ai.foresignal.cli.http.ApiClient;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Timeouts derived from the heartbeat interval. */
class ResilientSseStreamTest {

    private static ResilientSseStream stream(Duration heartbeat) {
        ApiClient api = new ApiClient(new Connection("http://127.0.0.1:1", null), HttpClient.newHttpClient(),
                Duration.ofSeconds(1), JsonMapper.builder().build());
        return new ResilientSseStream(api, heartbeat, new Backoff(new SplittableRandom(1)), d -> { },
                new PrintWriter(new StringWriter()), 0);
    }

    @Test
    void theWatchdogIsTwoAndAHalfHeartbeats() {
        assertEquals(Duration.ofMillis(37500), stream(Duration.ofSeconds(15)).watchdog());
        assertEquals(Duration.ofMillis(500), stream(Duration.ofMillis(200)).watchdog());
    }

    @Test
    void theHeaderTimeoutIsTheWatchdogButAtLeastTenSeconds() {
        assertEquals(Duration.ofSeconds(10), stream(Duration.ofMillis(200)).headerTimeout());
        assertEquals(Duration.ofMillis(37500), stream(Duration.ofSeconds(15)).headerTimeout());
    }
}
