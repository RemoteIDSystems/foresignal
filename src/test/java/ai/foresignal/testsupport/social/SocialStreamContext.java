package ai.foresignal.testsupport.social;

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
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link StreamContext} for the social streaming tests (prompt 135): {@code openWebSocket} opens a real JDK
 * WebSocket to a loopback server and records the URI and headers asked for; GETs and POSTs are answered by a
 * responder; the adapter state and credentials are in-memory maps.
 */
public final class SocialStreamContext implements StreamContext {

    private final SourceDescriptor descriptor;
    private final Clock clock;
    private final Function<URI, UpstreamResponse> responder;
    private final Map<String, String> state = new ConcurrentHashMap<>();
    private final Map<String, String> credentials = new ConcurrentHashMap<>();
    private final List<URI> opened = new CopyOnWriteArrayList<>();
    private final List<Map<String, String>> openHeaders = new CopyOnWriteArrayList<>();
    private final List<URI> requests = new CopyOnWriteArrayList<>();
    private final AtomicInteger connectedCalls = new AtomicInteger();
    private final Http http = new Http();
    private volatile ValidatedParams params = ValidatedParams.empty();

    /**
     * A context.
     *
     * @param descriptor the source descriptor
     * @param clock the clock
     * @param responder answers GETs and POSTs; may be null when none is expected
     */
    public SocialStreamContext(SourceDescriptor descriptor, Clock clock, Function<URI, UpstreamResponse> responder) {
        this.descriptor = descriptor;
        this.clock = clock;
        this.responder = responder;
    }

    /**
     * A responder that sends each GET to the real URI (a WireMock stub) with a JDK client and returns its answer.
     *
     * @return the responder
     */
    public static Function<URI, UpstreamResponse> forwarding() {
        HttpClient client = HttpClient.newHttpClient();
        return uri -> {
            try {
                java.net.http.HttpResponse<byte[]> r = client.send(java.net.http.HttpRequest.newBuilder(uri).GET()
                        .build(), java.net.http.HttpResponse.BodyHandlers.ofByteArray());
                Map<String, List<String>> headers = new java.util.HashMap<>();
                r.headers().map().forEach((k, v) -> headers.put(k.toLowerCase(java.util.Locale.ROOT), v));
                return new UpstreamResponse(r.statusCode(), headers, r.body(),
                        r.headers().firstValue("content-type").orElse(null), false, false);
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        };
    }

    /** The adapter state, for set-up and assertions. */
    public Map<String, String> stateValues() {
        return state;
    }

    /** The credentials, for set-up. */
    public Map<String, String> credentialValues() {
        return credentials;
    }

    /** The WebSocket URIs opened, in order. */
    public List<URI> opened() {
        return opened;
    }

    /** The headers of each WebSocket opened, in order. */
    public List<Map<String, String>> openHeaders() {
        return openHeaders;
    }

    /** The GET and POST URIs requested, in order. */
    public List<URI> requests() {
        return requests;
    }

    /** How often the adapter reported a connection. */
    public int connectedCalls() {
        return connectedCalls.get();
    }

    /** Sets the parameters. */
    public void params(Map<String, Object> values) {
        this.params = ValidatedParams.of(values);
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
        return params;
    }

    @Override
    public Map<String, String> credentials() {
        return credentials;
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
    public void recordReceivedBytes(long bytes) {}

    @Override
    public void connected() {
        connectedCalls.incrementAndGet();
    }

    @Override
    public void disconnected(Throwable error) {}

    private final class Http implements SourceHttp {

        private final HttpClient client = HttpClient.newHttpClient();

        @Override
        public WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers)
                throws AdapterException {
            return openWebSocket(uri, listener, headers, WebSocketOptions.DEFAULT);
        }

        @Override
        public WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers,
                WebSocketOptions opts) throws AdapterException {
            opened.add(uri);
            openHeaders.add(headers == null ? Map.of() : Map.copyOf(headers));
            try {
                return client.newWebSocketBuilder().buildAsync(uri, listener).join();
            } catch (RuntimeException e) {
                throw new AdapterException("could not connect: " + e.getMessage(), e);
            }
        }

        @Override
        public UpstreamResponse get(URI uri) throws AdapterException {
            requests.add(uri);
            if (responder == null) {
                throw new AdapterException("no request was expected: " + uri);
            }
            return responder.apply(uri);
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
        public UpstreamResponse post(URI uri, String contentType, byte[] body, Map<String, String> headers)
                throws AdapterException {
            return get(uri);
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
