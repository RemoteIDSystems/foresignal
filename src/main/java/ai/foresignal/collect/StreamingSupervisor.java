package ai.foresignal.collect;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.store.FetchStatus;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * Owns the connections of the enabled STREAMING sources (Architecture §5.5).
 *
 * <p>{@link #reconcile} starts and stops streams to match the set the {@link CollectorScheduler}
 * computes. Each stream runs on its own virtual thread, which calls {@code adapter.start} with a
 * {@link DefaultStreamContext} and the source's {@link PolicyAwareRecordSink}, then waits for the
 * connection to end: the handle's {@code onClose} callback, the context's {@code disconnected}
 * callback, or {@link StreamHandle#isRunning()} turning false, polled once a second for handles that
 * implement neither.
 *
 * <p><strong>Reconnecting.</strong> The ladder is 5 s, 15 s, 60 s, 300 s, then 900 s repeating, and
 * starts again once a connection has held for 10 minutes (all configurable). Each attempt consumes
 * budget through {@code SourceHttp.openWebSocket}; a refused budget waits until the next minute
 * instead of spinning, a source in backoff waits out its backoff, and a
 * {@link CredentialsRejectedException} retries hourly.
 *
 * <p>Connection changes go to {@link HealthTracker}; each flush that the writer completes is recorded
 * with {@link HealthTracker#recordStreamFlush}, which sets {@code last_data_at}. A flush the writer
 * failed gets a FAILED {@code fetch_log} row here, so every flush leaves exactly one row.
 * {@link #stop} closes the handle and flushes what was buffered.
 */
@Component
public class StreamingSupervisor implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(StreamingSupervisor.class);

    private static final Duration POLL = Duration.ofSeconds(1);

    private final SourceRegistry registry;
    private final SourceService sources;
    private final FetchContextFactory contexts;
    private final HealthTracker health;
    private final IngestQueue queue;
    private final FetchLogWriter fetchLog;
    private final ForesignalVersion version;
    private final MeterRegistry meters;
    private final CollectorProperties properties;
    private final Clock clock;

    private final ConcurrentHashMap<String, Runner> runners = new ConcurrentHashMap<>();

    /** Creates the supervisor. */
    public StreamingSupervisor(
            SourceRegistry registry,
            SourceService sources,
            FetchContextFactory contexts,
            HealthTracker health,
            IngestQueue queue,
            FetchLogWriter fetchLog,
            ForesignalVersion version,
            MeterRegistry meters,
            CollectorProperties properties,
            Clock clock) {
        this.registry = registry;
        this.sources = sources;
        this.contexts = contexts;
        this.health = health;
        this.queue = queue;
        this.fetchLog = fetchLog;
        this.version = version;
        this.meters = meters;
        this.properties = properties;
        this.clock = clock;
    }

    /** Starts the streams in {@code desired} that are not running and stops the rest. */
    public synchronized void reconcile(Set<String> desired) {
        for (String id : new ArrayList<>(runners.keySet())) {
            if (!desired.contains(id)) {
                stop(id, properties.shutdownWait());
            }
        }
        for (String id : desired) {
            if (!runners.containsKey(id)) {
                try {
                    start(id);
                } catch (RuntimeException e) {
                    log.error("Could not start the stream of {}", id, e);
                }
            }
        }
    }

    /**
     * Starts {@code sourceId}'s stream. Idempotent.
     *
     * @throws IllegalArgumentException when the source is not a streaming source
     */
    public synchronized void start(String sourceId) {
        if (runners.containsKey(sourceId)) {
            return;
        }
        SourceRegistry.Registration registration = registry.get(sourceId);
        SourceDescriptor descriptor = registration.descriptor();
        SourceAdapter adapter = registration.adapterIfPresent().orElse(null);
        if (descriptor.mode() != Mode.STREAMING || !(adapter instanceof StreamingAdapter streaming)) {
            throw new IllegalArgumentException(sourceId + " is not a streaming source");
        }
        PolicyAwareRecordSink sink = new PolicyAwareRecordSink(
                descriptor,
                policyMode(descriptor),
                queue::submit,
                new HealthFlushListener(sourceId),
                clock,
                properties.stream(),
                version.version(),
                meters);
        sink.start();
        Runner runner = new Runner(descriptor, streaming, sink);
        runners.put(sourceId, runner);
        runner.thread = Thread.ofVirtual().name("foresignal-stream-" + sourceId).start(runner::loop);
        log.info("Started the stream of {}", sourceId);
    }

    /**
     * Stops {@code sourceId}'s stream: closes the handle and flushes what was buffered.
     *
     * @return true when the final flush completed within {@code timeout}; true also when the stream
     *     was not running
     */
    public synchronized boolean stop(String sourceId, Duration timeout) {
        Runner runner = runners.remove(sourceId);
        if (runner == null) {
            return true;
        }
        long deadline = System.nanoTime() + timeout.toNanos();
        runner.shutdown();
        try {
            runner.thread.join(Math.max(1, Math.min(TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()), 5_000)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean flushed = runner.sink.flushAndClose(Duration.ofNanos(Math.max(1, deadline - System.nanoTime())));
        if (!flushed) {
            log.warn("{}: the final stream flush did not complete within {}", sourceId, timeout);
        }
        log.info("Stopped the stream of {}", sourceId);
        return flushed;
    }

    /** Stops every stream, flushing each, within {@code timeout} overall. */
    public void stopAll(Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        for (String id : new ArrayList<>(runners.keySet())) {
            stop(id, Duration.ofNanos(Math.max(1, deadline - System.nanoTime())));
        }
    }

    /** The sources whose streams are running, ordered. */
    public Set<String> running() {
        return new TreeSet<>(runners.keySet());
    }

    /** How many streams are connected right now. */
    public int activeStreams() {
        return (int) runners.values().stream().filter(r -> r.connected).count();
    }

    /** True while {@code sourceId}'s stream is connected. */
    public boolean isConnected(String sourceId) {
        Runner runner = runners.get(sourceId);
        return runner != null && runner.connected;
    }

    /** The sink of a running stream, or null. */
    PolicyAwareRecordSink sink(String sourceId) {
        Runner runner = runners.get(sourceId);
        return runner == null ? null : runner.sink;
    }

    /** The reconnect attempts since the ladder last reset, or -1 when not running. */
    int attempts(String sourceId) {
        Runner runner = runners.get(sourceId);
        return runner == null ? -1 : runner.attempt;
    }

    /** Stops every stream on shutdown. */
    @Override
    public void destroy() {
        stopAll(properties.shutdownWait());
    }

    private IngestPolicy.PolicyMode policyMode(SourceDescriptor descriptor) {
        try {
            return sources.effectiveIngestPolicy(descriptor.id()).mode();
        } catch (RuntimeException e) {
            log.warn("Could not read {}'s ingest policy; using its default", descriptor.id(), e);
            return descriptor.defaultIngestPolicy().mode();
        }
    }

    /**
     * How long to wait before the next connection attempt.
     *
     * @param attempt reconnects since the ladder last reset
     * @param failure why the last connection ended, or null
     */
    Duration nextDelay(String sourceId, int attempt, Throwable failure) {
        if (FetchExecutor.causeOf(failure, CredentialsRejectedException.class) != null) {
            return properties.stream().credentialsRetry();
        }
        if (FetchExecutor.causeOf(failure, BudgetExceededException.class) != null) {
            Instant now = clock.instant();
            Instant nextMinute = now.truncatedTo(ChronoUnit.MINUTES).plus(Duration.ofMinutes(1));
            return Duration.between(now, nextMinute).plusMillis(100);
        }
        InBackoffException inBackoff = FetchExecutor.causeOf(failure, InBackoffException.class);
        if (inBackoff != null && inBackoff.getUntil() != null) {
            Duration wait = Duration.between(clock.instant(), inBackoff.getUntil());
            return wait.compareTo(Duration.ofSeconds(1)) < 0 ? Duration.ofSeconds(1) : wait;
        }
        return properties.stream().reconnectDelay(attempt);
    }

    /** Records completed flushes in health, and writes the row of a flush the writer failed. */
    private final class HealthFlushListener implements PolicyAwareRecordSink.FlushListener {

        private final String sourceId;

        HealthFlushListener(String sourceId) {
            this.sourceId = sourceId;
        }

        @Override
        public void flushed(FetchLogEntry entry, int records, IngestOutcome outcome) {
            health.recordStreamFlush(sourceId, outcome.seen(), outcome.persisted());
        }

        @Override
        public void failed(FetchLogEntry entry, int records, Throwable error) {
            String message = HealthTracker.truncate("ingest failed: " + health.errorMessage(sourceId, error));
            log.warn("{}: a stream flush of {} records was not ingested: {}", sourceId, records, message);
            try {
                fetchLog.write(
                        new FetchLogEntry(
                                entry.sourceId(), entry.trigger(), FetchStatus.FAILED, entry.startedAt(),
                                entry.receivedAt(), null, 0, null, null, null, null, entry.adapterVersion(),
                                entry.licence(), null, entry.coverage(), message),
                        records);
            } catch (RuntimeException e) {
                log.warn("{}: could not record the failed stream flush", sourceId, e);
            }
        }
    }

    /** One stream's connect, wait, reconnect loop. */
    private final class Runner {

        private final String sourceId;
        private final SourceDescriptor descriptor;
        private final StreamingAdapter adapter;
        private final PolicyAwareRecordSink sink;
        private final Object wake = new Object();

        private volatile boolean stopped;
        private volatile boolean connected;
        private volatile long connectedSinceNanos;
        private volatile StreamHandle handle;
        private volatile CompletableFuture<Throwable> closed = new CompletableFuture<>();
        private volatile int attempt;
        private Thread thread;

        Runner(SourceDescriptor descriptor, StreamingAdapter adapter, PolicyAwareRecordSink sink) {
            this.sourceId = descriptor.id();
            this.descriptor = descriptor;
            this.adapter = adapter;
            this.sink = sink;
        }

        void loop() {
            while (!stopped) {
                CompletableFuture<Throwable> signal = new CompletableFuture<>();
                closed = signal;
                Throwable failure = null;
                try {
                    DefaultStreamContext ctx = contexts.forStream(descriptor, null, new DefaultStreamContext.Callbacks() {
                        @Override
                        public void connected() {
                            if (!signal.isDone()) {
                                markConnected();
                            }
                        }

                        @Override
                        public void disconnected(Throwable error) {
                            signal.complete(error);
                        }
                    });
                    StreamHandle started = adapter.start(ctx, sink);
                    if (started == null) {
                        throw new IllegalStateException(sourceId + "'s adapter returned no stream handle");
                    }
                    handle = started;
                    if (stopped) {
                        closeQuietly(started);
                        break;
                    }
                    started.onClose(signal::complete);
                    if (!signal.isDone()) {
                        markConnected();
                    }
                    failure = awaitClose(signal, started);
                } catch (Exception e) {
                    failure = e;
                }
                boolean wasConnected = connected;
                long connectedNanos = wasConnected ? System.nanoTime() - connectedSinceNanos : 0;
                connected = false;
                closeQuietly(handle);
                handle = null;
                if (stopped) {
                    if (wasConnected) {
                        // A deliberate close is not a failure.
                        health.recordStreamDisconnected(sourceId, null);
                    }
                    break;
                }
                health.recordStreamDisconnected(sourceId, failure);
                if (connectedNanos >= properties.stream().ladderResetAfter().toNanos()) {
                    attempt = 0;
                }
                Duration delay = nextDelay(sourceId, attempt, failure);
                boolean ladderStep = FetchExecutor.causeOf(failure, CredentialsRejectedException.class) == null
                        && FetchExecutor.causeOf(failure, BudgetExceededException.class) == null
                        && FetchExecutor.causeOf(failure, InBackoffException.class) == null;
                if (ladderStep) {
                    attempt++;
                }
                log.info("{}: stream disconnected ({}); reconnecting in {}", sourceId,
                        failure == null ? "closed" : failure.getClass().getSimpleName(), delay);
                sleep(delay);
            }
            connected = false;
        }

        private Throwable awaitClose(CompletableFuture<Throwable> signal, StreamHandle started) throws Exception {
            while (!stopped) {
                try {
                    return signal.get(POLL.toMillis(), TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    if (!started.isRunning()) {
                        signal.complete(null);
                    }
                }
            }
            return null;
        }

        private void markConnected() {
            if (!connected) {
                connected = true;
                connectedSinceNanos = System.nanoTime();
                health.recordStreamConnected(sourceId);
            }
        }

        private void sleep(Duration delay) {
            long deadline = System.nanoTime() + delay.toNanos();
            synchronized (wake) {
                while (!stopped) {
                    long remaining = deadline - System.nanoTime();
                    if (remaining <= 0) {
                        return;
                    }
                    try {
                        TimeUnit.NANOSECONDS.timedWait(wake, remaining);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }

        void shutdown() {
            stopped = true;
            closed.complete(null);
            closeQuietly(handle);
            synchronized (wake) {
                wake.notifyAll();
            }
        }

        private void closeQuietly(StreamHandle h) {
            if (h == null) {
                return;
            }
            try {
                h.close();
            } catch (RuntimeException e) {
                log.debug("{}: closing the stream handle failed", sourceId, e);
            }
        }
    }
}
