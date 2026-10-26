package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.SourceHealthChangedEvent;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ResourceThresholds;
import ai.foresignal.settings.ResourceThresholdsService;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/**
 * The operational alerts that share the resource alerts' path (prompt R2-014): a source-failure storm and a sustained
 * collector backlog each raise one alert and clear with one notice, edge-triggered.
 */
@IntegrationTest
@RecordApplicationEvents
class OperationalAlertIT {

    static final Instant T0 = Instant.parse("2026-10-26T12:00:00Z");

    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private ApplicationEvents events;
    @Autowired private AppSettingService settings;
    @Autowired private MutableClock clock;
    @Autowired private ResourceThresholdsService thresholds;
    @Autowired private OperationalAlertService alerts;
    @Autowired private SourceStormMonitor storm;
    @Autowired private CollectorBacklogMonitor backlog;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        settings.invalidateCache();
        alerts.reset();
        storm.reset();
    }

    private void thresholds(ResourceThresholds.SourceStorm s, ResourceThresholds.CollectorBacklog b) {
        ResourceThresholds d = ResourceThresholds.defaults();
        thresholds.put(new ResourceThresholds(d.disk(), d.memory(), List.of(), s, b));
    }

    private long count(String subtype) {
        return events.stream(SystemEvent.class).filter(e -> e.subtype().equals(subtype)).count();
    }

    private void health(String source, HealthStatus previous, HealthStatus current) {
        publisher.publishEvent(new SourceHealthChangedEvent(source, previous, current, "test", clock.instant()));
    }

    @Test
    @DisplayName("a source-failure storm raises once at the threshold and clears once below it")
    void sourceStorm() {
        thresholds(new ResourceThresholds.SourceStorm(2), ResourceThresholds.defaults().collectorBacklog());

        health("storm-a", HealthStatus.HEALTHY, HealthStatus.FAILING);
        assertThat(count("system.source_storm")).isZero();
        health("storm-b", HealthStatus.HEALTHY, HealthStatus.FAILING);
        assertThat(count("system.source_storm")).isEqualTo(1);
        SystemEvent raised = events.stream(SystemEvent.class)
                .filter(e -> e.subtype().equals("system.source_storm")).findFirst().orElseThrow();
        assertThat(raised.data().get("failingSources")).isEqualTo(2);

        health("storm-c", HealthStatus.HEALTHY, HealthStatus.FAILING);
        assertThat(count("system.source_storm")).as("still in the storm: no re-fire").isEqualTo(1);

        health("storm-a", HealthStatus.FAILING, HealthStatus.HEALTHY);
        assertThat(count("system.source_storm_cleared")).as("two still failing").isZero();
        health("storm-b", HealthStatus.FAILING, HealthStatus.HEALTHY);
        assertThat(count("system.source_storm_cleared")).isEqualTo(1);
        health("storm-c", HealthStatus.FAILING, HealthStatus.HEALTHY);
        assertThat(count("system.source_storm_cleared")).isEqualTo(1);
        assertThat(storm.failing()).isEmpty();
    }

    @Test
    @DisplayName("a null storm threshold never raises")
    void stormOff() {
        thresholds(new ResourceThresholds.SourceStorm(null), ResourceThresholds.defaults().collectorBacklog());
        for (int i = 0; i < 10; i++) {
            health("off-" + i, HealthStatus.HEALTHY, HealthStatus.FAILING);
        }
        assertThat(count("system.source_storm")).isZero();
    }

    @Test
    @DisplayName("a trigger lag sustained past the window raises once and clears once")
    void lagBacklog() {
        thresholds(ResourceThresholds.defaults().sourceStorm(),
                new ResourceThresholds.CollectorBacklog(100L, null, 60));

        assertThat(backlog.evaluate(T0, 500, 0)).isEqualTo("ok");
        assertThat(backlog.evaluate(T0.plusSeconds(30), 500, 0)).as("not sustained yet").isEqualTo("ok");
        assertThat(count("system.collector_backlog")).isZero();
        assertThat(backlog.evaluate(T0.plusSeconds(60), 500, 0)).isEqualTo("warn");
        assertThat(count("system.collector_backlog")).isEqualTo(1);
        assertThat(backlog.evaluate(T0.plusSeconds(90), 800, 0)).isEqualTo("warn");
        assertThat(count("system.collector_backlog")).as("no re-fire").isEqualTo(1);

        assertThat(backlog.evaluate(T0.plusSeconds(120), 10, 0)).isEqualTo("ok");
        assertThat(count("system.collector_backlog_cleared")).isEqualTo(1);
        backlog.evaluate(T0.plusSeconds(150), 10, 0);
        assertThat(count("system.collector_backlog_cleared")).isEqualTo(1);
    }

    @Test
    @DisplayName("a brief spike that drops before the window ends raises nothing")
    void briefSpike() {
        thresholds(ResourceThresholds.defaults().sourceStorm(),
                new ResourceThresholds.CollectorBacklog(100L, null, 60));
        backlog.evaluate(T0, 500, 0);
        backlog.evaluate(T0.plusSeconds(45), 10, 0);
        backlog.evaluate(T0.plusSeconds(70), 500, 0);
        backlog.evaluate(T0.plusSeconds(100), 500, 0);
        assertThat(count("system.collector_backlog")).as("the window restarted at 70 s").isZero();
        assertThat(count("system.collector_backlog_cleared")).isZero();
    }

    @Test
    @DisplayName("waiting pulls over their bound raise the same backlog alert")
    void waitingBacklog() {
        thresholds(ResourceThresholds.defaults().sourceStorm(),
                new ResourceThresholds.CollectorBacklog(null, 5, 30));
        backlog.evaluate(T0, 0, 20);
        backlog.evaluate(T0.plusSeconds(30), 0, 20);
        assertThat(count("system.collector_backlog")).isEqualTo(1);
        backlog.evaluate(T0.plusSeconds(60), 0, 2);
        assertThat(count("system.collector_backlog_cleared")).isEqualTo(1);
    }
}
