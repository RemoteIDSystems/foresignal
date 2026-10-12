package ai.foresignal.testsupport.stream;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

/**
 * A minimal SSE reader over {@link HttpClient} for integration tests: it opens a stream, parses
 * {@code id:}, {@code event:} and {@code data:} lines into {@link Event}s on a virtual thread, and
 * hands them out with timeouts. Not an {@code EventSource}: it never reconnects.
 */
public final class SseTestClient implements AutoCloseable {

    /**
     * One received event.
     *
     * @param id the {@code id:} value, or null when the frame had no id line
     * @param event the event name
     * @param data the data (lines joined with {@code \n})
     */
    public record Event(String id, String event, String data) {}

    /** How long a connect waits for the response headers. */
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final int status;
    private final HttpResponse<Stream<String>> response;
    private final BlockingQueue<Event> events = new LinkedBlockingQueue<>();
    private final List<Event> received = new ArrayList<>();
    private volatile boolean ended;

    private SseTestClient(HttpResponse<Stream<String>> response) {
        this.response = response;
        this.status = response.statusCode();
        if (status == 200) {
            Thread.ofVirtual().name("sse-test-reader").start(this::read);
        } else {
            response.body().close();
        }
    }

    /**
     * Connects to {@code baseUrl + pathAndQuery} with {@code Accept: text/event-stream} plus
     * {@code headers}.
     */
    public static SseTestClient connect(String baseUrl, String pathAndQuery, Map<String, String> headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + pathAndQuery))
                .header("Accept", "text/event-stream")
                .GET();
        headers.forEach(request::header);
        // Bounded: a server that never answers fails this connect instead of hanging the whole run
        // (prompt 022, run 1 hung for 10 min in HttpClient.send).
        CompletableFuture<HttpResponse<Stream<String>>> pending =
                HTTP.sendAsync(request.build(), HttpResponse.BodyHandlers.ofLines());
        try {
            return new SseTestClient(pending.get(CONNECT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        } catch (TimeoutException e) {
            pending.cancel(true);
            throw new IllegalStateException("No SSE response headers within " + CONNECT_TIMEOUT + " for " + pathAndQuery, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /** The response status. */
    public int status() {
        return status;
    }

    /** A response header, or null. */
    public String header(String name) {
        return response.headers().firstValue(name).orElse(null);
    }

    /** True once the server ended the stream. */
    public boolean ended() {
        return ended;
    }

    private void read() {
        String id = null;
        String event = null;
        StringBuilder data = null;
        try (Stream<String> lines = response.body()) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (line.isEmpty()) {
                    if (event != null || data != null) {
                        events.add(new Event(id, event == null ? "message" : event, data == null ? "" : data.toString()));
                    }
                    id = null;
                    event = null;
                    data = null;
                } else if (line.startsWith("id:")) {
                    id = value(line, 3);
                } else if (line.startsWith("event:")) {
                    event = value(line, 6);
                } else if (line.startsWith("data:")) {
                    data = data == null ? new StringBuilder() : data.append('\n');
                    data.append(value(line, 5));
                }
            }
        } catch (RuntimeException e) {
            // The test closed the stream.
        } finally {
            ended = true;
        }
    }

    private static String value(String line, int from) {
        String value = line.substring(from);
        return value.startsWith(" ") ? value.substring(1) : value;
    }

    /** The next event, or null after {@code timeout}. */
    public Event next(Duration timeout) {
        try {
            Event event = events.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (event != null) {
                received.add(event);
            }
            return event;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /** The next event named {@code name}, skipping others (heartbeats, live events), or null after {@code timeout}. */
    public Event next(String name, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (true) {
            long left = deadline - System.nanoTime();
            if (left <= 0) {
                return null;
            }
            Event event = next(Duration.ofNanos(left));
            if (event == null) {
                return null;
            }
            if (event.event().equals(name)) {
                return event;
            }
        }
    }

    /** Every event named {@code name} that arrives within {@code window}. */
    public List<Event> collect(String name, Duration window) {
        List<Event> out = new ArrayList<>();
        long deadline = System.nanoTime() + window.toNanos();
        while (true) {
            long left = deadline - System.nanoTime();
            if (left <= 0) {
                return out;
            }
            Event event = next(Duration.ofNanos(left));
            if (event != null && event.event().equals(name)) {
                out.add(event);
            }
        }
    }

    /** Every event received through {@link #next} so far. */
    public List<Event> received() {
        return List.copyOf(received);
    }

    /** Waits up to {@code timeout} for the server to end the stream. */
    public boolean awaitEnd(Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (!ended && System.nanoTime() < deadline) {
            Event event = next(Duration.ofMillis(50));
            if (event == null && ended) {
                break;
            }
        }
        return ended;
    }

    @Override
    public void close() {
        response.body().close();
    }
}
