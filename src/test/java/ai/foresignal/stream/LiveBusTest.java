package ai.foresignal.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LiveBusTest {

    private static final Duration WAIT = Duration.ofSeconds(5);

    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    private final LiveBus bus = new LiveBus(meters);
    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    @AfterEach
    void close() {
        subscriptions.forEach(Subscription::close);
    }

    private static LiveBatch batch(String id) {
        return new LiveBatch(id, List.of(), List.of(new Touch(id, "e", null, null, null)));
    }

    @Test
    @DisplayName("subscribers receive live batches in order")
    void delivers() {
        List<String> seen = new CopyOnWriteArrayList<>();
        AtomicInteger dropped = new AtomicInteger();
        subscriptions.add(bus.subscribe("ok", listener(seen, dropped, null), 10));
        bus.publish(batch("a"));
        bus.publish(batch("b"));
        await().atMost(WAIT).untilAsserted(() -> assertThat(seen).containsExactly("a", "b"));
        assertThat(dropped).hasValue(0);
        assertThat(batch("a").isEmpty()).isFalse();
        assertThat(new LiveBatch("x", null, null).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("overflow drops the batch, counts it and calls onLiveDropped")
    void overflow() throws InterruptedException {
        List<String> seen = new CopyOnWriteArrayList<>();
        AtomicInteger dropped = new AtomicInteger();
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch blocked = new CountDownLatch(1);
        subscriptions.add(bus.subscribe("slow", listener(seen, dropped, new CountDownLatch[] {release, blocked}), 1));
        bus.publish(batch("a"));
        assertThat(blocked.await(5, TimeUnit.SECONDS)).isTrue();
        bus.publish(batch("b")); // queued
        bus.publish(batch("c")); // dropped
        bus.publish(batch("d")); // dropped
        assertThat(meters.get(LiveBus.DROPPED_METRIC).tag("subscriber", "slow").counter().count()).isEqualTo(2.0);
        release.countDown();
        await().atMost(WAIT).untilAsserted(() -> assertThat(dropped).hasValue(1));
        assertThat(seen).containsExactly("a", "b");
        assertThat(bus.subscriberCount()).isEqualTo(1);
    }

    private static LiveListener listener(List<String> seen, AtomicInteger dropped, CountDownLatch[] gate) {
        return new LiveListener() {
            @Override
            public void onBatch(LiveBatch batch) {
                if (gate != null && seen.isEmpty()) {
                    gate[1].countDown();
                    try {
                        gate[0].await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                seen.add(batch.sourceId());
            }

            @Override
            public void onLiveDropped() {
                dropped.incrementAndGet();
            }
        };
    }
}
