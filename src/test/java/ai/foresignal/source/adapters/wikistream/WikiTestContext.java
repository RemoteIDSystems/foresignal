package ai.foresignal.source.adapters.wikistream;

import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link StreamContext} for {@link WikiStreamAdapterTest}: a bound {@link SourceHttp} from the real
 * {@code UpstreamClient} (budget, backoff, User-Agent, redaction) pointed at WireMock, a {@code MutableClock}, an
 * in-memory adapter state and byte accounting.
 */
final class WikiTestContext implements StreamContext {

    private final SourceDescriptor descriptor;
    private final SourceHttp http;
    private final Clock clock;
    private final ValidatedParams params;
    private final Map<String, String> state = new ConcurrentHashMap<>();
    private final AtomicLong reportedBytes = new AtomicLong();
    private final AtomicBoolean connected = new AtomicBoolean();
    private volatile Throwable disconnected;

    WikiTestContext(SourceDescriptor descriptor, SourceHttp http, Clock clock, ValidatedParams params) {
        this.descriptor = descriptor;
        this.http = http;
        this.clock = clock;
        this.params = params;
    }

    long reportedBytes() {
        return reportedBytes.get();
    }

    boolean everConnected() {
        return connected.get();
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
    public void recordReceivedBytes(long bytes) {
        if (bytes > 0) {
            reportedBytes.addAndGet(bytes);
        }
    }

    @Override
    public void connected() {
        connected.set(true);
    }

    @Override
    public void disconnected(Throwable error) {
        disconnected = error;
    }

    @Override
    public Logger log() {
        return LoggerFactory.getLogger("foresignal.source." + descriptor.id());
    }
}
