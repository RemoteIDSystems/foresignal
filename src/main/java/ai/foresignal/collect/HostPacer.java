package ai.foresignal.collect;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

/**
 * A per-host request gate (prompt 080): at most one request in flight to a host, and at least a minimum
 * spacing between the starts of two requests to it.
 *
 * <p>Community aircraft services allow one request per second per client, and adsb.fi restricts an IP address
 * that keeps making invalid requests. Several sources share one host there ({@code adsbfi-mil} and the four
 * {@code adsbfi-region-*} slots all call {@code opendata.adsb.fi}), so a per-source budget alone cannot keep
 * them serial: {@code UpstreamClient} holds one pacer, and every request of a source whose descriptor sets
 * {@code hostPacing} waits here, after the backoff check and before the budget is consumed, so a request that
 * never leaves costs no budget.
 *
 * <p>The wait is bounded ({@link #DEFAULT_MAX_WAIT} by default). A request that cannot start within it is not
 * sent: {@link #acquire} returns empty and {@code BoundSourceHttp} throws {@link HostPacingTimeoutException},
 * which the fetch executor reports as a skip (reason {@value FetchOutcome#SKIP_HOST_PACING}), never as a
 * failure.
 *
 * <p>Time is measured with a monotonic nanosecond clock, not the application {@code Clock}: tests freeze the
 * latter, and pacing is about real elapsed time between real requests.
 */
public final class HostPacer {

    /** The longest a request waits for its host's gate before it is skipped. */
    public static final Duration DEFAULT_MAX_WAIT = Duration.ofSeconds(30);

    /** Sleeps for a number of nanoseconds; replaceable in tests. */
    @FunctionalInterface
    interface Sleeper {

        /**
         * Sleeps.
         *
         * @param nanos how long, at least 1
         * @throws InterruptedException when the thread is interrupted
         */
        void sleep(long nanos) throws InterruptedException;
    }

    /** A held gate; closing it lets the next request to the host start (after the spacing). */
    public interface Permit extends AutoCloseable {

        /** Releases the gate. Idempotent. */
        @Override
        void close();
    }

    /** One host's gate. */
    private static final class Gate {
        private final Semaphore inFlight = new Semaphore(1, true);
        private long lastStartNanos;
        private boolean started;
    }

    private final Map<String, Gate> gates = new ConcurrentHashMap<>();
    private final Duration maxWait;
    private final LongSupplier nanoTime;
    private final Sleeper sleeper;

    /** A pacer with the {@link #DEFAULT_MAX_WAIT} and the system's monotonic clock. */
    public HostPacer() {
        this(DEFAULT_MAX_WAIT, System::nanoTime, HostPacer::sleepNanos);
    }

    /** A pacer with {@code maxWait} and the system's monotonic clock. */
    public HostPacer(Duration maxWait) {
        this(maxWait, System::nanoTime, HostPacer::sleepNanos);
    }

    /**
     * A pacer with its own time source (tests).
     *
     * @param maxWait the longest wait for a gate
     * @param nanoTime a monotonic nanosecond clock
     * @param sleeper how to wait
     */
    HostPacer(Duration maxWait, LongSupplier nanoTime, Sleeper sleeper) {
        this.maxWait = maxWait == null || maxWait.isNegative() ? DEFAULT_MAX_WAIT : maxWait;
        this.nanoTime = nanoTime;
        this.sleeper = sleeper;
    }

    /** The longest a request waits for its host's gate. */
    public Duration maxWait() {
        return maxWait;
    }

    /**
     * Waits until a request to {@code host} may start: no other request to it in flight, and {@code spacing}
     * since the previous start.
     *
     * @param host the upstream host, compared case-insensitively
     * @param spacing the minimum spacing between request starts; null or zero means only one in flight
     * @return the held gate, which the caller closes when the response has been read; empty when the gate
     *     could not be had within {@link #maxWait()} (nothing was held, nothing may be sent)
     * @throws InterruptedException when the waiting thread is interrupted (nothing is held)
     */
    public Optional<Permit> acquire(String host, Duration spacing) throws InterruptedException {
        String key = host == null ? "" : host.toLowerCase(Locale.ROOT);
        Gate gate = gates.computeIfAbsent(key, k -> new Gate());
        long start = nanoTime.getAsLong();
        long deadline = start + maxWait.toNanos();
        if (!gate.inFlight.tryAcquire(maxWait.toNanos(), TimeUnit.NANOSECONDS)) {
            return Optional.empty();
        }
        boolean handedOut = false;
        try {
            long spacingNanos = spacing == null || spacing.isNegative() ? 0 : spacing.toNanos();
            long now = nanoTime.getAsLong();
            long wait = gate.started ? gate.lastStartNanos + spacingNanos - now : 0;
            if (wait > 0) {
                if (now + wait > deadline) {
                    return Optional.empty();
                }
                sleeper.sleep(wait);
            }
            gate.lastStartNanos = nanoTime.getAsLong();
            gate.started = true;
            handedOut = true;
            AtomicBoolean released = new AtomicBoolean();
            return Optional.of(() -> {
                if (released.compareAndSet(false, true)) {
                    gate.inFlight.release();
                }
            });
        } finally {
            if (!handedOut) {
                gate.inFlight.release();
            }
        }
    }

    private static void sleepNanos(long nanos) throws InterruptedException {
        TimeUnit.NANOSECONDS.sleep(nanos);
    }
}
