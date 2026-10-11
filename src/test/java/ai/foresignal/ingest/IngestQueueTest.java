package ai.foresignal.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.store.FetchTrigger;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IngestQueueTest {

    private static final Instant NOW = Instant.parse("2026-03-01T00:00:00Z");

    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();

    private static FetchLogEntry log() {
        return FetchLogEntry.ok("s", FetchTrigger.MANUAL, NOW, NOW);
    }

    @Test
    @DisplayName("submit enqueues a batch whose future the writer completes")
    void submitEnqueues() throws InterruptedException {
        IngestQueue queue = new IngestQueue(2, meters);
        CompletableFuture<IngestOutcome> future = queue.submit("s", log(), List.of(), Duration.ofMillis(10));
        assertThat(queue.depth()).isEqualTo(1);
        IngestCommand command = queue.poll(Duration.ofMillis(10));
        assertThat(command).isInstanceOf(IngestBatch.class);
        IngestBatch batch = (IngestBatch) command;
        assertThat(batch.sourceId()).isEqualTo("s");
        assertThat(batch.future()).isSameAs(future);
        assertThat(queue.depth()).isZero();
    }

    @Test
    @DisplayName("a full queue blocks submit for the timeout, then fails with back-pressure")
    void backPressure() {
        IngestQueue queue = new IngestQueue(1, meters);
        queue.submit("s", log(), List.of(), Duration.ofMillis(10));
        long start = System.nanoTime();
        assertThatThrownBy(() -> queue.submit("s", log(), List.of(), Duration.ofMillis(200)))
                .isInstanceOf(IngestBackpressureException.class)
                .hasMessageContaining("stayed full");
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isGreaterThanOrEqualTo(Duration.ofMillis(150));
        assertThat(queue.depth()).isEqualTo(1);
    }

    @Test
    @DisplayName("a purge waits for room like a batch")
    void purgeBackPressure() {
        IngestQueue queue = new IngestQueue(1, meters);
        CompletableFuture<PurgeOutcome> first =
                queue.submitPurge(List.of(new PurgeSpec("s", 10, NOW)), Duration.ofMillis(10));
        assertThat(first).isNotDone();
        assertThatThrownBy(() -> queue.submitPurge(List.of(), Duration.ofMillis(20)))
                .isInstanceOf(IngestBackpressureException.class);
    }

    @Test
    @DisplayName("offer never blocks and reports a full queue; drain empties it")
    void offerAndDrain() {
        IngestQueue queue = new IngestQueue(1, meters);
        assertThat(queue.offer(new HeadPurge(List.of(), null))).isTrue();
        assertThat(queue.offer(new HeadPurge(List.of(), null))).isFalse();
        assertThat(queue.drain()).hasSize(1);
        assertThat(queue.depth()).isZero();
        assertThat(queue.capacity()).isEqualTo(1);
    }

    @Test
    @DisplayName("the depth gauge follows the queue")
    void depthGauge() {
        IngestQueue queue = new IngestQueue(4, meters);
        queue.offer(new HeadPurge(List.of(), null));
        queue.offer(new HeadPurge(List.of(), null));
        assertThat(meters.get(IngestQueue.DEPTH_GAUGE).gauge().value()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("the capacity comes from foresignal.ingest.queue-capacity and must be positive")
    void capacity() {
        assertThat(new IngestQueue(IngestProperties.defaults(), meters).capacity()).isEqualTo(64);
        assertThatThrownBy(() -> new IngestQueue(0, meters)).isInstanceOf(IllegalArgumentException.class);
    }
}
