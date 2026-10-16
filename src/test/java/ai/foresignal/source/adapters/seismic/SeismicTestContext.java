package ai.foresignal.source.adapters.seismic;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.spi.WebSocketOptions;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link StreamContext} for {@link EmscStreamTest} (prompt 089): the FDSN gap-fill {@code get} returns a canned
 * body, and {@code openWebSocket} opens a real JDK WebSocket to the loopback server (test code may build an
 * {@code HttpClient}; only main code is restricted).
 */
final class SeismicTestContext implements StreamContext {

    private final SourceDescriptor descriptor;
    private final Clock clock;
    private final java.util.function.Function<URI, UpstreamResponse> responder;
    private final List<URI> requests = new CopyOnWriteArrayList<>();
    private final Http http = new Http();
    private final Map<String, String> state = new ConcurrentHashMap<>();
    private final AtomicLong reportedBytes = new AtomicLong();

    SeismicTestContext(SourceDescriptor descriptor, Clock clock, byte[] gapFillBody) {
        this(descriptor, clock, uri -> new UpstreamResponse(200, Map.of(), gapFillBody, "application/json", false,
                false));
    }

    /**
     * A context whose gap-fill GETs are answered by {@code responder}, which may throw an
     * {@link IllegalStateException} to simulate a failed request (it is rethrown as an {@link AdapterException}).
     */
    SeismicTestContext(SourceDescriptor descriptor, Clock clock,
            java.util.function.Function<URI, UpstreamResponse> responder) {
        this.descriptor = descriptor;
        this.clock = clock;
        this.responder = responder;
    }

    /** The gap-fill URIs requested, in order. */
    List<URI> requests() {
        return requests;
    }

    /** The adapter state, for assertions. */
    Map<String, String> stateValues() {
        return state;
    }

    int opens() {
        return http.opens.get();
    }

    long reportedBytes() {
        return reportedBytes.get();
    }

    @Override
    public String sourceId() {
        return descriptor.id();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public SourceHttp http() {
        return http;
    }

    @Override
    public ValidatedParams params() {
        return ValidatedParams.empty();
    }

    @Override
    public Map<String, String> credentials() {
        return Map.of();
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public Optional<Instant> lastSuccessAt() {
        return Optional.empty();
    }

    @Override
    public AdapterStateStore state() {
        return new AdapterStateStore() {
            @Override
            public Optional<String> get(String key) {
                return Optional.ofNullable(state.get(key));
            }

            @Override
            public void put(String key, String value) {
                state.put(key, value);
            }

            @Override
            public void remove(String key) {
                state.remove(key);
            }
        };
    }

    @Override
    public Logger log() {
        return LoggerFactory.getLogger("foresignal.source." + descriptor.id());
    }

    @Override
    public void recordReceivedBytes(long bytes) {
        if (bytes > 0) {
            reportedBytes.addAndGet(bytes);
        }
    }

    @Override
    public void connected() {}

    @Override
    public void disconnected(Throwable error) {}

    private final class Http implements SourceHttp {

        private final HttpClient client = HttpClient.newHttpClient();
        private final AtomicInteger opens = new AtomicInteger();
        private final CopyOnWriteArrayList<WebSocketOptions> options = new CopyOnWriteArrayList<>();

        @Override
        public WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers)
                throws AdapterException {
            return openWebSocket(uri, listener, headers, WebSocketOptions.DEFAULT);
        }

        @Override
        public WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers,
                WebSocketOptions opts) throws AdapterException {
            opens.incrementAndGet();
            options.add(opts);
            try {
                return client.newWebSocketBuilder().buildAsync(uri, listener).join();
            } catch (RuntimeException e) {
                throw new AdapterException("could not connect: " + e.getMessage(), e);
            }
        }

        @Override
        public UpstreamResponse get(URI uri) throws AdapterException {
            requests.add(uri);
            try {
                return responder.apply(uri);
            } catch (IllegalStateException e) {
                throw new AdapterException(e.getMessage());
            }
        }

        @Override
        public UpstreamResponse get(URI uri, Map<String, String> headers) throws AdapterException {
            return get(uri);
        }

        @Override
        public UpstreamResponse getConditional(URI uri, String stateKey) throws AdapterException {
            return get(uri);
        }

        @Override
        public UpstreamResponse post(URI uri, String contentType, byte[] body, Map<String, String> headers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UpstreamResponse getRange(URI uri, long start, long end, long maxBytes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DownloadResult download(URI uri, Path target, long maxBytes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public S3Listing listS3(String bucket, String prefix, String startAfter) {
            throw new UnsupportedOperationException();
        }
    }
}
