package ai.foresignal.system;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.WatchdogSettings;
import ai.foresignal.settings.WatchdogSettingsService;
import ai.foresignal.store.history.PartitionManager;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.TestPartitions;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

/** The off-box watchdog and the ingest canary against a WireMock push monitor (prompt 019). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class WatchdogIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    @Autowired WatchdogService watchdog;
    @Autowired WatchdogSettingsService settings;
    @Autowired AppSettingService appSettings;
    @Autowired CanaryService canary;
    @Autowired MutableClock clock;
    @Autowired ApplicationContext context;
    @Autowired PartitionManager partitions;
    @Autowired MeterRegistry meters;

    @BeforeEach
    void setUp() {
        appSettings.invalidateCache();
        clock.setInstant(T);
        TestPartitions.restore(partitions);
        watchdog.resetSchedule();
    }

    void configure(String method, boolean canaryEnabled) {
        settings.put(new WatchdogSettings(url("/ping/ok"), url("/ping/fail"), Duration.ofSeconds(60), method,
                new WatchdogSettings.Canary(canaryEnabled, Duration.ofHours(1))));
        watchdog.resetSchedule();
    }

    @Test
    @DisplayName("pings the heartbeat URL while ready, once per interval")
    void heartbeatWhileReady() {
        WIREMOCK.stubFor(get("/ping/ok").willReturn(ok()));
        configure("GET", false);

        assertThat(watchdog.tick()).isEqualTo("heartbeat");
        assertThat(watchdog.tick()).isNull();
        clock.advance(Duration.ofSeconds(61));
        assertThat(watchdog.tick()).isEqualTo("heartbeat");

        WIREMOCK.verify(2, getRequestedFor(urlEqualTo("/ping/ok")));
    }

    @Test
    @DisplayName("pings the fail URL while not ready")
    void failUrlWhenNotReady() {
        WIREMOCK.stubFor(get("/ping/fail").willReturn(ok()));
        configure("GET", false);
        AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC);
        try {
            assertThat(watchdog.tick()).isEqualTo("fail");
        } finally {
            AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
        }
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo("/ping/fail")));
    }

    @Test
    @DisplayName("POST is honored and redirects are not followed")
    void postWithoutRedirects() {
        WIREMOCK.stubFor(post("/ping/ok").willReturn(aResponse().withStatus(302).withHeader("Location", url("/elsewhere"))));
        configure("POST", false);
        double failedBefore = meters.counter("foresignal.watchdog.pings", "kind", "heartbeat", "result", "failed").count();

        assertThat(watchdog.tick()).isEqualTo("heartbeat");

        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/ping/ok")));
        assertThat(meters.counter("foresignal.watchdog.pings", "kind", "heartbeat", "result", "failed").count())
                .isEqualTo(failedBefore + 1);
    }

    @Test
    @DisplayName("with the canary on, a canary success keeps the heartbeat and records its latency")
    void canarySuccess() {
        WIREMOCK.stubFor(get("/ping/ok").willReturn(ok()));
        configure("GET", true);
        long before = meters.timer(CanaryService.LATENCY_METRIC).count();

        assertThat(watchdog.tick()).isEqualTo("heartbeat");

        assertThat(canary.lastOk()).isTrue();
        assertThat(meters.timer(CanaryService.LATENCY_METRIC).count()).isEqualTo(before + 1);
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo("/ping/ok")));
    }

    @Test
    @DisplayName("nothing is pinged without URLs")
    void unconfigured() {
        settings.put(WatchdogSettings.defaults());
        assertThat(watchdog.tick()).isNull();
    }
}
