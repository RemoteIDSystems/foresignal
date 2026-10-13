package ai.foresignal.raster.render;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.raster.RasterSettings;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * The bounded, fair render queue (prompt 045; Weather-Imagery-Design §0.1, §6.3).
 *
 * <ul>
 *   <li>A fixed pool of platform threads ({@code foresignal.raster.render-pool}, from the profile)
 *       renders.
 *   <li>Priority: {@link Priority#WARM} (newest-frame warming) first, then
 *       {@link Priority#INTERACTIVE}, then {@link Priority#PREFETCH} ({@code Foresignal-Prefetch: 1}
 *       or {@code ?prefetch=1}). Warming never starves a waiting client (checkpoint 057): after a
 *       warming render, a queued interactive render goes next, so the two alternate while both
 *       wait. A frame queues up to {@link TileWarmer#MAX_TILES} warming renders, which alone could
 *       outlast the 1.5 s wait on a small pool.
 *   <li>A client that asks for a tile already queued for warming promotes that render to
 *       {@link Priority#INTERACTIVE}, so it is not served from the back of the warming batch.
 *   <li>Within a priority, clients take turns (round-robin per client key: the token id, else the
 *       tile key, else the client IP), so one busy client cannot starve the others.
 *   <li>Single-flight: concurrent requests for the same cache key share one render.
 *   <li>A caller waits at most {@link #MAX_WAIT} (1.5 s); then {@code 503 RENDER_BUSY} with
 *       {@code Retry-After: 1}, and a render nobody waits for any more is dropped if it has not
 *       started.
 * </ul>
 * The depth, active renders and {@code RENDER_BUSY} responses in the last hour feed
 * {@code /v1/system} {@code rasters.renderQueue} and {@code renderBusyLastHour}.
 */
@Component
public class RenderQueue implements DisposableBean {

    /** The longest a caller waits for its render. */
    public static final Duration MAX_WAIT = Duration.ofMillis(1500);

    private static final Logger log = LoggerFactory.getLogger(RenderQueue.class);

    /** Render priority, highest first. */
    public enum Priority {
        /** Warming the newest frame's AOI tiles. */
        WARM,
        /** A client waiting for a tile or image. */
        INTERACTIVE,
        /** A client prefetching animation frames. */
        PREFETCH
    }

    private final class Job {
        final Object key;
        final String client;
        Priority priority;
        String queuedFor;
        final Supplier<byte[]> work;
        final CompletableFuture<byte[]> future = new CompletableFuture<>();
        final AtomicInteger waiters = new AtomicInteger();
        volatile boolean started;

        Job(Object key, String client, Priority priority, Supplier<byte[]> work) {
            this.key = key;
            this.client = client;
            this.priority = priority;
            this.queuedFor = client;
            this.work = work;
        }
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition ready = lock.newCondition();
    private final List<LinkedHashMap<String, Deque<Job>>> levels = new ArrayList<>();
    private final Map<Object, Job> inflight = new ConcurrentHashMap<>();
    private final Deque<Instant> busy = new ArrayDeque<>();
    private final AtomicInteger active = new AtomicInteger();
    private final List<Thread> workers = new ArrayList<>();
    private final Duration maxWait;
    private final Clock clock;
    private final int poolSize;
    private int depth;
    private boolean lastWasWarm;
    private volatile boolean stopped;

    /** Creates the queue with the profile's pool size. */
    @Autowired
    public RenderQueue(RasterSettings settings, Clock clock) {
        this(settings.renderPool(), MAX_WAIT, clock);
    }

    /** Creates a queue of {@code threads} workers that waits at most {@code maxWait} (tests). */
    public RenderQueue(int threads, Duration maxWait, Clock clock) {
        this.poolSize = Math.max(1, threads);
        this.maxWait = maxWait;
        this.clock = clock;
        for (int k = 0; k < Priority.values().length; k++) {
            levels.add(new LinkedHashMap<>());
        }
        for (int k = 0; k < poolSize; k++) {
            Thread t = Thread.ofPlatform().name("raster-render-" + k).daemon(true).unstarted(this::workLoop);
            workers.add(t);
            t.start();
        }
    }

    /**
     * Renders {@code key}, waiting at most {@link #MAX_WAIT}.
     *
     * @param key the render's cache key (single-flight)
     * @param priority its priority
     * @param client the fairness key
     * @param work the render, which also fills the caches
     * @return the rendered bytes
     * @throws ForesignalException {@code 503 RENDER_BUSY} with {@code Retry-After: 1} after the wait
     */
    public byte[] render(Object key, Priority priority, String client, Supplier<byte[]> work) {
        return render(key, priority, client, work, maxWait);
    }

    /**
     * Renders {@code key}, waiting at most {@code wait} (prompt 055: a static map has a 2-s budget).
     *
     * @throws ForesignalException {@code 503 RENDER_BUSY} with {@code Retry-After: 1} after the wait
     */
    public byte[] render(Object key, Priority priority, String client, Supplier<byte[]> work, Duration wait) {
        Job job = join(key, priority, client, work);
        job.waiters.incrementAndGet();
        try {
            byte[] out = job.future.get(wait.toMillis(), TimeUnit.MILLISECONDS);
            job.waiters.decrementAndGet();
            return out;
        } catch (TimeoutException e) {
            abandon(job);
            throw busy();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            abandon(job);
            throw busy();
        } catch (CancellationException e) {
            job.waiters.decrementAndGet();
            throw busy();
        } catch (ExecutionException e) {
            job.waiters.decrementAndGet();
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException("Render failed: " + e.getCause(), e.getCause());
        }
    }

    /**
     * Queues {@code key} without waiting (warming).
     *
     * @return the render's future
     */
    public CompletableFuture<byte[]> submit(Object key, Priority priority, String client, Supplier<byte[]> work) {
        return join(key, priority, client, work).future;
    }

    private Job join(Object key, Priority priority, String client, Supplier<byte[]> work) {
        if (stopped) {
            throw busy();
        }
        Job[] created = new Job[1];
        Job job = inflight.computeIfAbsent(key, k -> {
            created[0] = new Job(k, client == null ? "?" : client, priority, work);
            return created[0];
        });
        if (created[0] == job) {
            enqueue(job);
        } else if (priority == Priority.INTERACTIVE && job.priority == Priority.WARM) {
            promote(job, client == null ? "?" : client);
        }
        return job;
    }

    /** Moves a queued warming render to the interactive level for {@code client}. */
    private void promote(Job job, String client) {
        lock.lock();
        try {
            if (job.started || job.priority != Priority.WARM) {
                return;
            }
            Deque<Job> queue = levels.get(job.priority.ordinal()).get(job.queuedFor);
            if (queue == null || !queue.remove(job)) {
                return;
            }
            if (queue.isEmpty()) {
                levels.get(job.priority.ordinal()).remove(job.queuedFor);
            }
            job.priority = Priority.INTERACTIVE;
            job.queuedFor = client;
            levels.get(job.priority.ordinal()).computeIfAbsent(client, c -> new ArrayDeque<>()).addLast(job);
        } finally {
            lock.unlock();
        }
    }

    private void enqueue(Job job) {
        lock.lock();
        try {
            levels.get(job.priority.ordinal()).computeIfAbsent(job.queuedFor, c -> new ArrayDeque<>()).addLast(job);
            depth++;
            ready.signal();
        } finally {
            lock.unlock();
        }
    }

    /** Drops a job nobody waits for, if it has not started. */
    private void abandon(Job job) {
        if (job.waiters.decrementAndGet() > 0 || job.started) {
            return;
        }
        lock.lock();
        try {
            if (job.started) {
                return;
            }
            Deque<Job> queue = levels.get(job.priority.ordinal()).get(job.queuedFor);
            if (queue != null && queue.remove(job)) {
                depth--;
                if (queue.isEmpty()) {
                    levels.get(job.priority.ordinal()).remove(job.queuedFor);
                }
            }
        } finally {
            lock.unlock();
        }
        inflight.remove(job.key, job);
        job.future.cancel(false);
    }

    private ForesignalException busy() {
        Instant now = clock.instant();
        synchronized (busy) {
            busy.addLast(now);
            prune(now);
        }
        return new ForesignalException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCodes.RENDER_BUSY,
                "The render queue is busy; retry in a second.").withHeader("Retry-After", "1");
    }

    private void prune(Instant now) {
        Instant cutoff = now.minus(Duration.ofHours(1));
        while (!busy.isEmpty() && busy.peekFirst().isBefore(cutoff)) {
            busy.removeFirst();
        }
    }

    /**
     * The next job: the highest non-empty priority, the next client in turn; except that right after a
     * warming render a waiting interactive render goes first, so warming cannot starve clients.
     */
    private Job take() throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                if (stopped) {
                    return null;
                }
                Job job = null;
                if (lastWasWarm) {
                    job = poll(levels.get(Priority.INTERACTIVE.ordinal()));
                }
                for (int k = 0; job == null && k < levels.size(); k++) {
                    job = poll(levels.get(k));
                }
                if (job != null) {
                    depth--;
                    job.started = true;
                    lastWasWarm = job.priority == Priority.WARM;
                    return job;
                }
                ready.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /** The first client's next job at one level, rotating that client to the back; null when empty. */
    private static Job poll(LinkedHashMap<String, Deque<Job>> level) {
        while (true) {
            Iterator<Map.Entry<String, Deque<Job>>> it = level.entrySet().iterator();
            if (!it.hasNext()) {
                return null;
            }
            Map.Entry<String, Deque<Job>> first = it.next();
            Job job = first.getValue().pollFirst();
            String client = first.getKey();
            Deque<Job> rest = first.getValue();
            it.remove();
            if (!rest.isEmpty()) {
                level.put(client, rest);
            }
            if (job != null) {
                return job;
            }
        }
    }

    private void workLoop() {
        while (!stopped) {
            Job job;
            try {
                job = take();
            } catch (InterruptedException e) {
                return;
            }
            if (job == null) {
                return;
            }
            active.incrementAndGet();
            try {
                job.future.complete(job.work.get());
            } catch (RuntimeException | Error e) {
                job.future.completeExceptionally(e);
                if (job.waiters.get() == 0) {
                    log.debug("Unattended render {} failed", job.key, e);
                }
            } finally {
                active.decrementAndGet();
                inflight.remove(job.key, job);
            }
        }
    }

    /** Renders waiting to start. */
    public int depth() {
        lock.lock();
        try {
            return depth;
        } finally {
            lock.unlock();
        }
    }

    /** Renders running. */
    public int active() {
        return active.get();
    }

    /** The worker count. */
    public int poolSize() {
        return poolSize;
    }

    /** {@code RENDER_BUSY} responses in the last hour. */
    public int busyLastHour() {
        synchronized (busy) {
            prune(clock.instant());
            return busy.size();
        }
    }

    /** Stops the workers. */
    @Override
    public void destroy() {
        stopped = true;
        lock.lock();
        try {
            ready.signalAll();
        } finally {
            lock.unlock();
        }
        workers.forEach(Thread::interrupt);
    }
}
