package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.source.adapters.system.SystemCanaryAdapter;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.Subscription;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

/** The ingest canary's success and failure paths (prompt 019). */
class CanaryServiceTest {

    final IngestQueue queue = mock(IngestQueue.class);
    final ChangeBus bus = mock(ChangeBus.class);
    final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    final CanaryService canary = new CanaryService(queue, bus, events,
            new WatchdogProperties(false, Duration.ofSeconds(5), Duration.ofMillis(200)), meters,
            Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), java.time.ZoneOffset.UTC));

    CanaryServiceTest() {
        when(bus.subscribe(any(), any(), anyInt())).thenReturn(mock(Subscription.class));
    }

    @Test
    @DisplayName("success: the change arrives, latency is recorded and nothing is published")
    void success() {
        doAnswer(invocation -> {
            canary.onBatch(new ChangeBatch(SystemCanaryAdapter.ID, 1, Instant.EPOCH, 5, 5, List.of()));
            return new CompletableFuture<>();
        }).when(queue).submit(eq(SystemCanaryAdapter.ID), any(), anyList(), any());

        assertThat(canary.runOnce()).isTrue();
        assertThat(canary.lastOk()).isTrue();
        assertThat(canary.lastRunAt()).isNotNull();
        assertThat(meters.timer(CanaryService.LATENCY_METRIC).count()).isEqualTo(1);
        verify(events, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("failure: no change within the wait publishes system.canary_failed")
    void failure() {
        when(queue.submit(eq(SystemCanaryAdapter.ID), any(), anyList(), any())).thenReturn(new CompletableFuture<>());

        assertThat(canary.runOnce()).isFalse();
        assertThat(canary.lastOk()).isFalse();
        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue()).isInstanceOf(SystemEvent.class);
        assertThat(((SystemEvent) event.getValue()).subtype()).isEqualTo("system.canary_failed");
    }

    @Test
    @DisplayName("another source's batch does not satisfy the canary")
    void otherSourceIgnored() {
        doAnswer(invocation -> {
            canary.onBatch(new ChangeBatch("other", 1, Instant.EPOCH, 5, 5, List.of()));
            return new CompletableFuture<>();
        }).when(queue).submit(eq(SystemCanaryAdapter.ID), any(), anyList(), any());

        assertThat(canary.runOnce()).isFalse();
        canary.reset();
        assertThat(canary.lastOk()).isNull();
    }
}
