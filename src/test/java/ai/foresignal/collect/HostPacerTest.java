package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * {@link HostPacer} (prompt 080): one request in flight per host, the spacing between request starts honored
 * across every caller of a host, hosts independent of each other, and a bounded wait that hands out nothing.
 *
 * <p>The spacing tests drive a fake monotonic clock whose sleeper advances it, so they are exact and instant; the
 * concurrency tests use real threads and the real clock with short durations.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class HostPacerTest {

    /** A monotonic clock that only moves when the pacer sleeps (or the test advances it). */
    private static final class FakeTime {
        private final AtomicLong nanos = new AtomicLong(1_000_000_000L);
        private final List<Long> sleeps = new ArrayList<>();

        long now() {
            return nanos.get();
        }

        void sleep(long n) {
            sleeps.add(n);
            nanos.addAndGet(n);
        }

        void advance(Duration d) {
            nanos.addAndGet(d.toNanos());
        }
    }

    private static HostPacer fake(FakeTime time, Duration maxWait) {
        return new HostPacer(maxWait, time::now, time::sleep);
    }

    @Test
    @DisplayName("the first request to a host starts at once; the next waits out the spacing from the first start")
    void spacing() throws Exception {
        FakeTime time = new FakeTime();
        HostPacer pacer = fake(time, Duration.ofSeconds(30));
        Duration spacing = Duration.ofMillis(1100);

        pacer.acquire("opendata.adsb.fi", spacing).orElseThrow().close();
        assertThat(time.sleeps).isEmpty();

        time.advance(Duration.ofMillis(300));
        pacer.acquire("opendata.adsb.fi", spacing).orElseThrow().close();
        assertThat(time.sleeps).containsExactly(Duration.ofMillis(800).toNanos());

        // A request that comes after the spacing has passed does not wait.
        time.advance(Duration.ofSeconds(2));
        pacer.acquire("opendata.adsb.fi", spacing).orElseThrow().close();
        assertThat(time.sleeps).hasSize(1);
    }

    @Test
    @DisplayName("the spacing is per host, shared by every caller of it, and the host name is case-insensitive")
    void perHost() throws Exception {
        FakeTime time = new FakeTime();
        HostPacer pacer = fake(time, Duration.ofSeconds(30));
        Duration spacing = Duration.ofMillis(1100);

        // adsbfi-mil, then adsbfi-region-1 on the same host: the second waits.
        pacer.acquire("opendata.adsb.fi", spacing).orElseThrow().close();
        pacer.acquire("OPENDATA.ADSB.FI", spacing).orElseThrow().close();
        assertThat(time.sleeps).containsExactly(spacing.toNanos());

        // Another host has its own gate and does not wait.
        pacer.acquire("api.airplanes.live", spacing).orElseThrow().close();
        assertThat(time.sleeps).hasSize(1);
    }

    @Test
    @DisplayName("a wait that would end after the bounded wait hands out nothing and leaves the gate free")
    void boundedSpacingWait() throws Exception {
        FakeTime time = new FakeTime();
        HostPacer pacer = fake(time, Duration.ofSeconds(1));

        pacer.acquire("h", Duration.ofSeconds(5)).orElseThrow().close();
        assertThat(pacer.acquire("h", Duration.ofSeconds(5))).isEmpty();
        assertThat(time.sleeps).isEmpty();

        // The refused caller did not keep the gate.
        time.advance(Duration.ofSeconds(5));
        assertThat(pacer.acquire("h", Duration.ofSeconds(5))).isPresent();
    }

    @Test
    @DisplayName("closing a permit twice releases the gate once")
    void idempotentClose() throws Exception {
        HostPacer pacer = new HostPacer(Duration.ofMillis(200));
        HostPacer.Permit permit = pacer.acquire("h", null).orElseThrow();
        permit.close();
        permit.close();

        HostPacer.Permit second = pacer.acquire("h", null).orElseThrow();
        // Were the gate released twice, a third caller would get in while the second holds it.
        assertThat(pacer.acquire("h", null)).isEmpty();
        second.close();
    }

    @Test
    @DisplayName("while one request holds the gate, another to the host waits until it is released")
    void singleInFlight() throws Exception {
        HostPacer pacer = new HostPacer(Duration.ofSeconds(10));
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger maxInFlight = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    try (HostPacer.Permit permit = pacer.acquire("opendata.adsb.fi", Duration.ZERO).orElseThrow()) {
                        int now = inFlight.incrementAndGet();
                        maxInFlight.accumulateAndGet(now, Math::max);
                        Thread.sleep(50);
                        inFlight.decrementAndGet();
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
        assertThat(maxInFlight.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("a request that cannot get the gate within the bounded wait gets nothing")
    void boundedGateWait() throws Exception {
        HostPacer pacer = new HostPacer(Duration.ofMillis(200));
        HostPacer.Permit held = pacer.acquire("h", null).orElseThrow();
        long started = System.nanoTime();

        Optional<HostPacer.Permit> refused = pacer.acquire("h", null);

        long waitedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        assertThat(refused).isEmpty();
        assertThat(waitedMs).isBetween(150L, 5_000L);
        held.close();
        assertThat(pacer.acquire("h", null)).isPresent();
    }

    @Test
    @DisplayName("spacing holds across threads on the real clock: starts are at least the spacing apart")
    void realSpacingAcrossThreads() throws Exception {
        HostPacer pacer = new HostPacer(Duration.ofSeconds(10));
        Duration spacing = Duration.ofMillis(150);
        List<Long> starts = java.util.Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                futures.add(pool.submit(() -> {
                    try (HostPacer.Permit permit = pacer.acquire("h", spacing).orElseThrow()) {
                        starts.add(System.nanoTime());
                    }
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
        List<Long> sorted = starts.stream().sorted().toList();
        for (int i = 1; i < sorted.size(); i++) {
            assertThat(TimeUnit.NANOSECONDS.toMillis(sorted.get(i) - sorted.get(i - 1)))
                    .as("gap before start %d", i).isGreaterThanOrEqualTo(140L);
        }
    }
}
