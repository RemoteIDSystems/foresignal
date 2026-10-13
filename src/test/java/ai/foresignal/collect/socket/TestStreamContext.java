package ai.foresignal.collect.socket;

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
 * A hand-built {@link StreamContext} for the socket transport tests (prompt 069): counts the budget units
 * charged and the bytes reported, refuses once {@link #budgetLeft} is spent, and keeps state in memory.
 */
final class TestStreamContext implements StreamContext {

    final AtomicInteger charged = new AtomicInteger();
    final AtomicLong bytes = new AtomicLong();
    final AtomicInteger budgetLeft = new AtomicInteger(Integer.MAX_VALUE);
    private final SourceDescriptor descriptor;
    private final Clock clock;
    private final ValidatedParams params;
    private final Map<String, String> state = new ConcurrentHashMap<>();
    private SocketTransport sockets;

    TestStreamContext(String sourceId, Clock clock) {
        this(sourceId, clock, ValidatedParams.empty());
    }

    TestStreamContext(String sourceId, Clock clock, ValidatedParams params) {
        this.descriptor = SourceDescriptor.builder(sourceId).kind("thing").defaultBudget(60, 1000).build();
        this.clock = clock;
        this.params = params;
    }

    /** Installs the transport {@link #sockets()} returns. */
    TestStreamContext withSockets(SocketTransport transport) {
        this.sockets = transport;
        return this;
    }

    @Override
    public SocketTransport sockets() {
        if (sockets == null) {
            return StreamContext.super.sockets();
        }
        return sockets;
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
        throw new UnsupportedOperationException("no HTTP in the socket tests");
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
