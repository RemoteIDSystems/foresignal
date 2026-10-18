package ai.foresignal.cli.sse;

import ai.foresignal.cli.http.ApiClient;
import ai.foresignal.cli.http.ApiException;
import ai.foresignal.cli.http.NetworkException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A long-lived SSE subscription that survives disconnects:
 *
 * <ul>
 *   <li>each attempt asks a {@link Supplier} for its {@link StreamRequest}, so the caller resumes from its own last
 *       id ({@code Last-Event-ID});
 *   <li>a heartbeat watchdog reconnects when neither an event nor a heartbeat arrives within 2.5× the server's
 *       heartbeat interval (and an attempt whose response headers do not arrive within that time, but at least
 *       10 s, counts as failed);
 *   <li>a dropped connection, a network failure, {@code 429} or a {@code 5xx} reconnects after a jittered exponential
 *       backoff (1 s → 60 s) that honors the server's {@code retry:} and {@code Retry-After} as floors;
 *   <li>any other error status ends the subscription with an {@link ApiException}.
 * </ul>
 *
 * <p>The body is read on a virtual thread into a bounded queue so that the watchdog can time out a stalled read.
 */
public final class ResilientSseStream {

    /** What the subscription does after an event. */
    public enum Action {
        /** Keep reading. */
        CONTINUE,
        /** Close this connection and open the next one at once, without backoff (the server asked for it). */
        RECONNECT_NOW,
        /** End the subscription. */
        STOP
    }

    /** Receives the events. */
    @FunctionalInterface
    public interface Handler {

        /**
         * Handles one event.
         *
         * @param event the event
         * @return what to do next
         */
        Action onEvent(SseEvent event);
    }

    /** The multiple of the heartbeat interval after which a silent stream is considered dead. */
    public static final double WATCHDOG_FACTOR = 2.5;

    private static final Object END = new Object();

    private final ApiClient api;
    private final Duration heartbeat;
    private final Backoff backoff;
    private final Sleeper sleeper;
    private final PrintWriter err;
    private final int maxRetries;
    private Long serverRetryMillis;
    private int failures;

    /**
     * Creates a subscription.
     *
     * @param api        the API client
     * @param heartbeat  the server's heartbeat interval (Foresignal's default {@code heartbeatSeconds} is 15)
     * @param backoff    the reconnect backoff
     * @param sleeper    how to wait
     * @param err        where reconnect notices go
     * @param maxRetries consecutive failed attempts allowed before giving up with a {@link NetworkException}; a
     *                   negative value retries forever
     */
    public ResilientSseStream(ApiClient api, Duration heartbeat, Backoff backoff, Sleeper sleeper, PrintWriter err,
            int maxRetries) {
        this.api = api;
        this.heartbeat = heartbeat;
        this.backoff = backoff;
        this.sleeper = sleeper;
        this.err = err;
        this.maxRetries = maxRetries;
    }

    /**
     * The watchdog timeout.
     *
     * @return 2.5× the heartbeat interval
     */
    public Duration watchdog() {
        return Duration.ofMillis(Math.max(1, Math.round(heartbeat.toMillis() * WATCHDOG_FACTOR)));
    }

    /**
     * How long a connection attempt may wait for the response headers: the watchdog, but at least 10 s.
     *
     * @return the header timeout
     */
    Duration headerTimeout() {
        Duration watchdog = watchdog();
        return watchdog.compareTo(Duration.ofSeconds(10)) < 0 ? Duration.ofSeconds(10) : watchdog;
    }

    /**
     * Runs until the handler returns {@link Action#STOP}.
     *
     * @param requests builds each connection attempt
     * @param handler  receives the events
     * @throws ApiException     on a non-retryable error status, or the last {@code 429}/{@code 5xx} once
     *                          {@code maxRetries} consecutive attempts failed
     * @throws NetworkException when {@code maxRetries} consecutive attempts failed otherwise, or when interrupted
     */
    public void run(Supplier<StreamRequest> requests, Handler handler) {
        while (true) {
            StreamRequest request = requests.get();
            HttpResponse<InputStream> response;
            try {
                response = api.open("GET", request.path(), request.query(), null, request.headers(),
                        "text/event-stream", headerTimeout());
            } catch (NetworkException e) {
                retryAfterFailure(e.getMessage(), null, e);
                continue;
            }
            int status = response.statusCode();
            if (status != 200) {
                byte[] body = drain(response.body());
                ApiException problem = api.problem(status, response.headers(), body);
                if (status == 429 || status >= 500) {
                    retryAfterFailure(problem.getMessage(), problem.retryAfter(), problem);
                    continue;
                }
                throw problem;
            }
            Outcome outcome = pump(response.body(), handler);
            switch (outcome) {
                case STOP -> {
                    return;
                }
                case RECONNECT_NOW -> {
                    failures = 0;
                    backoff.reset();
                }
                case DROPPED -> retryAfterFailure("the stream ended", null, null);
                case SILENT -> retryAfterFailure("no event or heartbeat within " + watchdog().toMillis() + " ms",
                        null, null);
                default -> throw new IllegalStateException("Unknown outcome " + outcome);
            }
        }
    }

    private enum Outcome { STOP, RECONNECT_NOW, DROPPED, SILENT }

    private Outcome pump(InputStream body, Handler handler) {
        BlockingQueue<Object> queue = new LinkedBlockingQueue<>(1024);
        Thread reader = Thread.ofVirtual().name("foresignal-sse-reader").start(() -> {
            try (SseReader sse = new SseReader(body)) {
                SseEvent event;
                while ((event = sse.next()) != null) {
                    queue.put(event);
                }
                queue.put(END);
            } catch (IOException e) {
                queue.offer(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        boolean delivered = false;
        try {
            while (true) {
                Object next;
                try {
                    next = queue.poll(watchdog().toMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new NetworkException("Interrupted", e);
                }
                if (next == null) {
                    return Outcome.SILENT;
                }
                if (next == END || next instanceof IOException) {
                    return Outcome.DROPPED;
                }
                SseEvent event = (SseEvent) next;
                if (event.retry() != null) {
                    serverRetryMillis = event.retry();
                }
                if (!delivered) {
                    delivered = true;
                    failures = 0;
                    backoff.reset();
                }
                Action action = handler.onEvent(event);
                if (action == Action.STOP) {
                    return Outcome.STOP;
                }
                if (action == Action.RECONNECT_NOW) {
                    return Outcome.RECONNECT_NOW;
                }
            }
        } finally {
            reader.interrupt();
            try {
                body.close();
            } catch (IOException e) {
                // Closing a dead connection; nothing to do.
            }
        }
    }

    private void retryAfterFailure(String why, Duration retryAfter, RuntimeException cause) {
        failures++;
        if (maxRetries >= 0 && failures > maxRetries) {
            if (cause != null) {
                throw cause;
            }
            throw new NetworkException("Giving up after " + (failures - 1) + " reconnect attempts: " + why, null);
        }
        Duration floor = retryAfter;
        if (serverRetryMillis != null) {
            Duration retry = Duration.ofMillis(serverRetryMillis);
            if (floor == null || retry.compareTo(floor) > 0) {
                floor = retry;
            }
        }
        Duration delay = backoff.next(floor);
        err.println("stream: " + why + "; reconnecting in " + delay.toMillis() + " ms");
        err.flush();
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Interrupted", e);
        }
    }

    private static byte[] drain(InputStream in) {
        try (in) {
            return in.readNBytes(64 * 1024);
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
