package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.stream.Subscription;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link AlertBus}: ordered async delivery, overflow and gaps (prompt 017). */
class AlertBusTest {

    static AlertEvent event(long id) {
        AlertView view = new AlertView(id, AlertModelTest.RULE, "r", id, "s", "e", "k", AlertModelTest.T, AlertModelTest.T,
                "m", "warning", "firing", id, null, null, null, null, null, null, null, null, null, false, null, List.of(), null);
        return AlertEvent.of(AlertEvent.FIRED, view);
    }

    @Test
    @DisplayName("every listener receives every event in order on its own thread")
    void delivery() {
        AlertBus bus = new AlertBus(new SimpleMeterRegistry());
        List<Long> a = new CopyOnWriteArrayList<>();
        List<Long> b = new CopyOnWriteArrayList<>();
        Subscription sa = bus.subscribe("a", e -> a.add(e.alert().id()), 16);
        Subscription sb = bus.subscribe("b", e -> b.add(e.alert().id()), 16);
        for (long i = 1; i <= 5; i++) {
            bus.publish(event(i));
        }
        await().atMost(Duration.ofSeconds(5)).until(() -> a.size() == 5 && b.size() == 5);
        assertThat(a).containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(bus.listenerCount()).isEqualTo(2);
        sa.close();
        sa.close();
        sb.close();
        assertThat(bus.listenerCount()).isZero();
    }

    @Test
    @DisplayName("a full queue drops events until it drains, then reports one gap; a throwing listener survives")
    void overflow() throws InterruptedException {
        AlertBus bus = new AlertBus(new SimpleMeterRegistry());
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger gaps = new AtomicInteger();
        List<Long> seen = new CopyOnWriteArrayList<>();
        bus.subscribe("slow", new AlertListener() {
            @Override
            public void onAlert(AlertEvent e) {
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                seen.add(e.alert().id());
                if (e.alert().id() == 2) {
                    throw new IllegalStateException("boom");
                }
            }

            @Override
            public void onGap() {
                gaps.incrementAndGet();
            }
        }, 1);
        for (long i = 1; i <= 10; i++) {
            bus.publish(event(i));
        }
        release.countDown();
        await().atMost(Duration.ofSeconds(5)).until(() -> gaps.get() == 1);
        bus.publish(event(11));
        await().atMost(Duration.ofSeconds(5)).until(() -> seen.contains(11L));
        assertThat(seen.size()).isLessThan(11);
    }
}
