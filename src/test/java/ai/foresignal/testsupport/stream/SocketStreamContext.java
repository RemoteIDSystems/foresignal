package ai.foresignal.testsupport.stream;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.SocketTransport;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A hand-built {@link StreamContext} for socket source tests (prompt 082; prompt 069's {@code TestStreamContext} with
 * a real descriptor): counts the budget units charged and the bytes reported, refuses once {@link #budgetLeft} is
 * spent, keeps state in memory, and returns whatever {@link SocketTransport} the test installs.
 */
public final class SocketStreamContext implements StreamContext {

    /** Budget units charged so far. */
    public final AtomicInteger charged = new AtomicInteger();

    /** Bytes reported so far. */
    public final AtomicLong bytes = new AtomicLong();

    /** Units left before {@link #chargeRequest} refuses. */
    public final AtomicInteger budgetLeft = new AtomicInteger(Integer.MAX_VALUE);

    private final SourceDescriptor descriptor;
    private final Clock clock;
    private final ValidatedParams params;
    private final Map<String, String> state = new ConcurrentHashMap<>();
    private volatile SocketTransport sockets;
    private volatile Map<String, String> credentials = Map.of();

    /**
     * Creates the context.
     *
     * @param descriptor the source's descriptor
     * @param clock the context clock
     * @param params the source's parameters
     */
    public SocketStreamContext(SourceDescriptor descriptor, Clock clock, ValidatedParams params) {
        this.descriptor = descriptor;
        this.clock = clock;
        this.params = params == null ? ValidatedParams.empty() : params;
    }

    /** Installs the transport {@link #sockets()} returns. */
    public SocketStreamContext withSockets(SocketTransport transport) {
        this.sockets = transport;
        return this;
    }

    /** Installs the credentials {@link #credentials()} returns (prompt 083: a broker's user name and password). */
    public SocketStreamContext withCredentials(Map<String, String> values) {
        this.credentials = values == null ? Map.of() : Map.copyOf(values);
        return this;
    }

    @Override
    public SocketTransport sockets() {
        return sockets == null ? StreamContext.super.sockets() : sockets;
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
        throw new UnsupportedOperationException("no HTTP in socket tests");
    }

    @Override
    public void chargeRequest(String label) throws AdapterException {
        if (budgetLeft.getAndDecrement() <= 0) {
            throw new AdapterException("budget spent");
        }
        charged.incrementAndGet();
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
    public void recordReceivedBytes(long count) {
        if (count > 0) {
            bytes.addAndGet(count);
        }
    }

    @Override
    public void connected() {}

    @Override
    public void disconnected(Throwable error) {}
}
