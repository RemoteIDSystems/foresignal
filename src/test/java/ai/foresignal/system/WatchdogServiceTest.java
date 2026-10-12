package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.settings.WatchdogSettings;
import ai.foresignal.settings.WatchdogSettingsService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The watchdog's choice of URL (prompt 019). */
class WatchdogServiceTest {

    final WatchdogSettingsService settings = mock(WatchdogSettingsService.class);
    final ReadinessCheck readiness = mock(ReadinessCheck.class);
    final CanaryService canary = mock(CanaryService.class);
    final HeartbeatPinger pinger = mock(HeartbeatPinger.class);
    final WatchdogService watchdog = new WatchdogService(settings, readiness, canary, pinger,
            new WatchdogProperties(false, Duration.ofSeconds(5), Duration.ofSeconds(30)), new SimpleMeterRegistry(),
            Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC));

    @Test
    @DisplayName("a failed canary sends the fail URL even while ready")
    void canaryFailure() throws Exception {
        when(settings.get()).thenReturn(new WatchdogSettings("https://hc.invalid/ok", "https://hc.invalid/fail",
                Duration.ofSeconds(60), "GET", new WatchdogSettings.Canary(true, Duration.ofHours(1))));
        when(readiness.ready()).thenReturn(true);
        when(canary.runOnce()).thenReturn(false);
        when(canary.lastOk()).thenReturn(false);
        when(pinger.ping(anyString(), anyString(), anyInt())).thenReturn(200);

        assertThat(watchdog.tick()).isEqualTo("fail");
        verify(canary).runOnce();
        verify(pinger).ping(eq("https://hc.invalid/fail"), eq("GET"), eq(5000));
    }

    @Test
    @DisplayName("not ready without a fail URL pings nothing")
    void notReadyNoFailUrl() throws Exception {
        when(settings.get()).thenReturn(new WatchdogSettings("https://hc.invalid/ok", null, Duration.ofSeconds(60),
                "GET", WatchdogSettings.Canary.defaults()));
        when(readiness.ready()).thenReturn(false);

        assertThat(watchdog.tick()).isNull();
        verify(canary).reset();
    }

    @Test
    @DisplayName("a ping that throws is counted, not propagated")
    void pingThrows() throws Exception {
        when(settings.get()).thenReturn(new WatchdogSettings("https://hc.invalid/ok", null, Duration.ofSeconds(60),
                "GET", WatchdogSettings.Canary.defaults()));
        when(readiness.ready()).thenReturn(true);
        when(pinger.ping(anyString(), anyString(), anyInt())).thenThrow(new java.io.IOException("refused"));

        assertThat(watchdog.tick()).isEqualTo("heartbeat");
    }
}
