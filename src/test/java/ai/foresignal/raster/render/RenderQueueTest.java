package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ForesignalException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * The render queue of prompt 045: priority order, per-client fairness, single-flight, and the 1.5 s
 * bounded wait that sheds with {@code 503 RENDER_BUSY} and {@code Retry-After: 1}.
 */
@Timeout(30)
class RenderQueueTest {

    private final List<RenderQueue> queues = new ArrayList<>();

    private RenderQueue queue(int threads, Duration wait) {
        RenderQueue q = new RenderQueue(threads, wait, Clock.systemUTC());
        queues.add(q);
        return q;
    }

    @AfterEach
    void stop() {
        queues.forEach(RenderQueue::destroy);
    }

    /** Occupies the single worker until {@code release} counts down. */
    private static CompletableFuture<byte[]> block(RenderQueue q, CountDownLatch started, CountDownLatch release) {
        return q.submit("blocker", RenderQueue.Priority.INTERACTIVE, "blocker", () -> {
            started.countDown();
            await(release);
            return new byte[0];
        });
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("warming runs before interactive, which runs before prefetch")
    void priorityOrder() throws Exception {
        RenderQueue q = queue(1, Duration.ofSeconds(10));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        block(q, started, release);
        await(started);
        List<String> order = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture<byte[]> prefetch = q.submit("p", RenderQueue.Priority.PREFETCH, "a", () -> mark(order, "prefetch"));
        CompletableFuture<byte[]> interactive = q.submit("i", RenderQueue.Priority.INTERACTIVE, "a",
                () -> mark(order, "interactive"));
        CompletableFuture<byte[]> warm = q.submit("w", RenderQueue.Priority.WARM, "warm", () -> mark(order, "warm"));
        assertThat(q.depth()).isEqualTo(3);
        release.countDown();
        CompletableFuture.allOf(prefetch, interactive, warm).get(10, TimeUnit.SECONDS);
        assertThat(order).containsExactly("warm", "interactive", "prefetch");
    }

    @Test
    @DisplayName("warming does not starve a waiting client: after one warming render the interactive one goes next")
    void warmingDoesNotStarveInteractive() throws Exception {
        RenderQueue q = queue(1, Duration.ofSeconds(10));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        block(q, started, release);
        await(started);
        List<String> order = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<byte[]>> all = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            String id = "w" + i;
            all.add(q.submit(id, RenderQueue.Priority.WARM, "warm", () -> mark(order, id)));
        }
        all.add(q.submit("i", RenderQueue.Priority.INTERACTIVE, "token:a", () -> mark(order, "i")));
        release.countDown();
        CompletableFuture.allOf(all.toArray(CompletableFuture[]::new)).get(10, TimeUnit.SECONDS);
        assertThat(order.get(0)).as("the newest frame still goes first").isEqualTo("w0");
        assertThat(order.indexOf("i")).as("the client waits behind one warming render, not forty").isEqualTo(1);
    }

    @Test
    @DisplayName("a client asking for a tile queued for warming promotes it instead of waiting for the warming batch")
    void interactivePromotesQueuedWarming() throws Exception {
        RenderQueue q = queue(1, Duration.ofSeconds(10));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        block(q, started, release);
        await(started);
        List<String> order = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<byte[]>> all = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            String id = "w" + i;
            all.add(q.submit(id, RenderQueue.Priority.WARM, "warm", () -> mark(order, id)));
        }
        CompletableFuture<byte[]> client = q.submit("w39", RenderQueue.Priority.INTERACTIVE, "token:a",
                () -> mark(order, "never"));
        assertThat(q.depth()).as("promotion moves the render, it does not add one").isEqualTo(40);
        release.countDown();
        CompletableFuture.allOf(all.toArray(CompletableFuture[]::new)).get(10, TimeUnit.SECONDS);
        client.get(10, TimeUnit.SECONDS);
        assertThat(order).hasSize(40).doesNotContain("never");
        assertThat(order.indexOf("w39")).isEqualTo(1);
    }

    @Test
    @DisplayName("clients take turns within a priority: a burst from one client does not starve another")
    void fairness() throws Exception {
        RenderQueue q = queue(1, Duration.ofSeconds(10));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        block(q, started, release);
        await(started);
        List<String> order = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<byte[]>> all = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            String id = "a" + i;
            all.add(q.submit(id, RenderQueue.Priority.INTERACTIVE, "token:a", () -> mark(order, id)));
        }
        all.add(q.submit("b0", RenderQueue.Priority.INTERACTIVE, "ip:10.0.0.2", () -> mark(order, "b0")));
        release.countDown();
        CompletableFuture.allOf(all.toArray(CompletableFuture[]::new)).get(10, TimeUnit.SECONDS);
        assertThat(order.indexOf("b0")).as("b0 runs second, not after a's whole burst").isEqualTo(1);
    }

    @Test
    @DisplayName("concurrent requests for one key share one render")
    void singleFlight() throws Exception {
        RenderQueue q = queue(2, Duration.ofSeconds(10));
        AtomicInteger renders = new AtomicInteger();
        CountDownLatch gate = new CountDownLatch(1);
        List<CompletableFuture<byte[]>> callers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            callers.add(CompletableFuture.supplyAsync(() -> q.render("same", RenderQueue.Priority.INTERACTIVE, "c", () -> {
                renders.incrementAndGet();
                await(gate);
                return new byte[] {7};
            })));
        }
        Thread.sleep(200);
        gate.countDown();
        for (CompletableFuture<byte[]> c : callers) {
            assertThat(c.get(10, TimeUnit.SECONDS)).containsExactly(7);
        }
        assertThat(renders.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("a caller waits at most 1.5 s, then 503 RENDER_BUSY with Retry-After: 1; the busy count rises")
    void boundedWait() throws Exception {
        RenderQueue q = queue(1, RenderQueue.MAX_WAIT);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        block(q, started, release);
        await(started);
        AtomicInteger ran = new AtomicInteger();
        long t0 = System.nanoTime();
        assertThatThrownBy(() -> q.render("late", RenderQueue.Priority.INTERACTIVE, "c", () -> {
            ran.incrementAndGet();
            return new byte[0];
        })).isInstanceOfSatisfying(ForesignalException.class, e -> {
            assertThat(e.getCode()).isEqualTo("RENDER_BUSY");
            assertThat(e.getStatus().value()).isEqualTo(503);
            assertThat(e.getHeaders()).containsEntry("Retry-After", "1");
        });
        long waitedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
        assertThat(waitedMs).isBetween(1400L, 3000L);
        assertThat(q.busyLastHour()).isEqualTo(1);
        assertThat(q.depth()).as("the abandoned render left the queue").isZero();
        release.countDown();
        Thread.sleep(200);
        assertThat(ran.get()).as("a render nobody waits for is dropped").isZero();
        assertThat(q.active()).isZero();
        assertThat(q.poolSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("a render failure reaches the caller")
    void failure() {
        RenderQueue q = queue(1, Duration.ofSeconds(5));
        assertThatThrownBy(() -> q.render("x", RenderQueue.Priority.INTERACTIVE, "c", () -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class).hasMessage("boom");
    }

    private static byte[] mark(List<String> order, String name) {
        order.add(name);
        return new byte[0];
    }
}
