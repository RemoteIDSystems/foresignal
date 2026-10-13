package ai.foresignal.source.kit.websub;

import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A fetch context whose client only answers hub {@code POST}s (prompt 073 WebSub tests): every POST is recorded with
 * its decoded form, and answered with {@link #status}. Any other request fails the test.
 */
final class StubHubContext implements FetchContext {

    /** One recorded hub request. */
    record HubRequest(URI uri, Map<String, String> form) {}

    final List<HubRequest> requests = new ArrayList<>();
    final Map<String, String> state = new HashMap<>();
    int status = 202;

    private final String sourceId;
    private final SourceDescriptor descriptor;
    private final Clock clock;

    StubHubContext(String sourceId, SourceDescriptor descriptor, Clock clock) {
        this.sourceId = sourceId;
        this.descriptor = descriptor;
        this.clock = clock;
    }

    /** The form of the last hub request. */
    Map<String, String> lastForm() {
        return requests.get(requests.size() - 1).form();
    }

    @Override
    public String sourceId() {
        return sourceId;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public SourceHttp http() {
        return new SourceHttp() {
            @Override
            public UpstreamResponse get(URI uri) {
                throw new AssertionError("unexpected GET " + uri);
            }

            @Override
            public UpstreamResponse get(URI uri, Map<String, String> headers) {
                throw new AssertionError("unexpected GET " + uri);
            }

            @Override
            public UpstreamResponse getConditional(URI uri, String stateKey) {
                throw new AssertionError("unexpected GET " + uri);
            }

            @Override
            public UpstreamResponse post(URI uri, String contentType, byte[] body, Map<String, String> headers) {
                Map<String, String> form = new LinkedHashMap<>();
                for (String pair : new String(body, StandardCharsets.UTF_8).split("&")) {
                    int eq = pair.indexOf('=');
                    form.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
                            URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
                }
                requests.add(new HubRequest(uri, form));
                return new UpstreamResponse(status, Map.of(), new byte[0], null, false, false);
            }

            @Override
            public UpstreamResponse getRange(URI uri, long start, long end, long maxBytes) {
                throw new AssertionError("unexpected range GET");
            }

            @Override
            public DownloadResult download(URI uri, Path target, long maxBytes) {
                throw new AssertionError("unexpected download");
            }

            @Override
            public S3Listing listS3(String bucket, String prefix, String startAfter) {
                throw new AssertionError("unexpected S3 listing");
            }

            @Override
            public WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers) {
                throw new AssertionError("unexpected WebSocket");
            }
        };
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
}
