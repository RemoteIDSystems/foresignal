package ai.foresignal.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChangeBusTest {

    private static final Duration WAIT = Duration.ofSeconds(5);

    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    private final ChangeBus bus = new ChangeBus(() -> 100L, meters);
    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    @AfterEach
    void close() {
        subscriptions.forEach(Subscription::close);
    }

    private static ChangeBatch batch(long from, long to) {
        return new ChangeBatch("s", from, Instant.EPOCH, from, to, List.of());
    }

    /** Records batches and gaps. */
    static final class Recorder implements ChangeListener {
        final List<Long> toSeqs = new CopyOnWriteArrayList<>();
        final List<Long> gaps = new CopyOnWriteArrayList<>();
        final CountDownLatch release;
        final CountDownLatch started = new CountDownLatch(1);

        Recorder(CountDownLatch release) {
            this.release = release;
        }

        @Override
        public void onBatch(ChangeBatch batch) {
            started.countDown();
            if (release != null) {
                try {
                    release.await(WAIT.toMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            toSeqs.add(batch.toSeq());
        }

        @Override
        public void onGap(long lastDeliveredSeq) {
            gaps.add(lastDeliveredSeq);
        }
    }

    private Recorder subscribe(String name, int capacity, CountDownLatch release) {
        Recorder recorder = new Recorder(release);
        subscriptions.add(bus.subscribe(name, recorder, capacity));
        return recorder;
    }

    @Test
    @DisplayName("every subscriber receives every batch in publish order, and the watermark advances")
    void fanOut() {
        Recorder a = subscribe("a", 10, null);
        Recorder b = subscribe("b", 10, null);
        assertThat(bus.lastPublishedSeq()).isEqualTo(100L);
        bus.publish(batch(101, 103));
        bus.publish(batch(104, 104));
        await().atMost(WAIT).untilAsserted(() -> {
            assertThat(a.toSeqs).containsExactly(103L, 104L);
            assertThat(b.toSeqs).containsExactly(103L, 104L);
        });
        assertThat(bus.lastPublishedSeq()).isEqualTo(104L);
        assertThat(a.gaps).isEmpty();
    }

    @Test
    @DisplayName("a slow subscriber does not delay a fast one, and publish never blocks")
    void slowSubscriberIsolation() {
        CountDownLatch release = new CountDownLatch(1);
        Recorder slow = subscribe("slow", 100, release);
        Recorder fast = subscribe("fast", 100, null);
        long start = System.nanoTime();
        for (long seq = 101; seq <= 150; seq++) {
            bus.publish(batch(seq, seq));
        }
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(1));
        await().atMost(WAIT).untilAsserted(() -> assertThat(fast.toSeqs).hasSize(50));
        assertThat(slow.toSeqs).isEmpty();
        release.countDown();
        await().atMost(WAIT).untilAsserted(() -> assertThat(slow.toSeqs).hasSize(50));
    }

    @Test
    @DisplayName("overflow drops batches, marks the subscriber lagging and calls onGap once with the last delivered seq")
    void overflowGap() {
        CountDownLatch release = new CountDownLatch(1);
        Recorder recorder = subscribe("lagging", 2, release);
        bus.publish(batch(101, 101));
        // The delivery thread takes 101 and blocks in onBatch; 102 and 103 fill the queue.
        await().atMost(WAIT).until(() -> recorder.started.getCount() == 0);
        bus.publish(batch(102, 102));
        bus.publish(batch(103, 103));
        bus.publish(batch(104, 104)); // overflow: dropped, now lagging
        bus.publish(batch(105, 105)); // dropped while lagging
        release.countDown();
        await().atMost(WAIT).untilAsserted(() -> assertThat(recorder.gaps).containsExactly(103L));
        assertThat(recorder.toSeqs).containsExactly(101L, 102L, 103L);
        assertThat(meters.get(ChangeBus.DROPPED_METRIC).tag("subscriber", "lagging").counter().count()).isEqualTo(2.0);

        // Recovered: later batches are delivered again, with no further gap.
        bus.publish(batch(106, 106));
        await().atMost(WAIT).untilAsserted(() -> assertThat(recorder.toSeqs).containsExactly(101L, 102L, 103L, 106L));
        assertThat(recorder.gaps).containsExactly(103L);
        assertThat(bus.lastPublishedSeq()).isEqualTo(106L);
    }

    @Test
    @DisplayName("closeAll closes every subscription and ends its delivery thread (checkpoint 124)")
    void closeAllEndsDeliveryThreads() {
        AtomicReference<Thread> delivery = new AtomicReference<>();
        subscriptions.add(bus.subscribe("left-open", new ChangeListener() {
            @Override
            public void onBatch(ChangeBatch batch) {
                delivery.set(Thread.currentThread());
            }

            @Override
            public void onGap(long lastDeliveredSeq) {
            }
        }, 10));
        subscribe("second", 10, null);
        bus.publish(batch(101, 101));
        await().atMost(WAIT).until(() -> delivery.get() != null);

        bus.closeAll();

        assertThat(bus.subscriberCount()).isZero();
        await().atMost(WAIT).until(() -> !delivery.get().isAlive());
    }

    @Test
    @DisplayName("an unsubscribed listener receives nothing more")
    void unsubscribe() {
        Recorder recorder = subscribe("gone", 10, null);
        bus.publish(batch(101, 101));
        await().atMost(WAIT).untilAsserted(() -> assertThat(recorder.toSeqs).containsExactly(101L));
        subscriptions.get(0).close();
        subscriptions.get(0).close();
        assertThat(bus.subscriberCount()).isZero();
        bus.publish(batch(102, 102));
        sleep(300);
        assertThat(recorder.toSeqs).containsExactly(101L);
    }

    @Test
    @DisplayName("the watermark is read lazily once and can be reset")
    void watermark() {
        AtomicInteger reads = new AtomicInteger();
        ChangeBus lazy = new ChangeBus(() -> {
            reads.incrementAndGet();
            return 7L;
        }, meters);
        assertThat(reads).hasValue(0);
        assertThat(lazy.lastPublishedSeq()).isEqualTo(7L);
        assertThat(lazy.lastPublishedSeq()).isEqualTo(7L);
        assertThat(reads).hasValue(1);
        lazy.publish(batch(5, 6));
        assertThat(lazy.lastPublishedSeq()).isEqualTo(7L);
        lazy.resetWatermark(0);
        assertThat(lazy.lastPublishedSeq()).isZero();
    }

    @Test
    @DisplayName("a failing listener keeps receiving later batches")
    void failingListener() {
        List<Long> seen = new CopyOnWriteArrayList<>();
        subscriptions.add(bus.subscribe("failing", new ChangeListener() {
            @Override
            public void onBatch(ChangeBatch batch) {
                seen.add(batch.toSeq());
                throw new IllegalStateException("boom");
            }

            @Override
            public void onGap(long lastDeliveredSeq) {}
        }, 10));
        bus.publish(batch(101, 101));
        bus.publish(batch(102, 102));
        await().atMost(WAIT).untilAsserted(() -> assertThat(seen).containsExactly(101L, 102L));
    }

    @Test
    @DisplayName("a capacity below one is refused")
    void capacityValidated() {
        assertThatThrownBy(() -> bus.subscribe("x", new Recorder(null), 0)).isInstanceOf(IllegalArgumentException.class);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
