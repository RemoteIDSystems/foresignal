package ai.foresignal.source.adapters.internet;

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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A {@link StreamContext} for {@link RisLiveAdapterTest}: params and a loopback WebSocket. */
final class RisTestContext implements StreamContext {

    private final SourceDescriptor descriptor;
    private final Clock clock;
    private final ValidatedParams params;
    private final Map<String, String> state = new ConcurrentHashMap<>();
    private final AtomicLong reportedBytes = new AtomicLong();

    RisTestContext(SourceDescriptor descriptor, Clock clock, ValidatedParams params) {
        this.descriptor = descriptor;
        this.clock = clock;
        this.params = params;
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
        return new Http();
    }

    @Override
    public ValidatedParams params() {
        return params;
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

    private static final class Http implements SourceHttp {

        private final HttpClient client = HttpClient.newHttpClient();

        @Override
        public WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers)
                throws AdapterException {
            return openWebSocket(uri, listener, headers, WebSocketOptions.DEFAULT);
        }

        @Override
        public WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers,
                WebSocketOptions opts) throws AdapterException {
            try {
                return client.newWebSocketBuilder().buildAsync(uri, listener).join();
            } catch (RuntimeException e) {
                throw new AdapterException("could not connect: " + e.getMessage(), e);
            }
        }

        @Override
        public UpstreamResponse get(URI uri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UpstreamResponse get(URI uri, Map<String, String> headers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UpstreamResponse getConditional(URI uri, String stateKey) {
            throw new UnsupportedOperationException();
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
