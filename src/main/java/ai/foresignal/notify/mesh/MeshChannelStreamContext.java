package ai.foresignal.notify.mesh;

import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * The stream context behind a {@code meshcore} channel's socket transport (prompt 084), when the channel rather than
 * the {@code meshcore-local} source is the first owner of 083's companion link. A channel is not a source: it has no
 * descriptor, no budget and no HTTP; a connect costs nothing (the link's own reconnect ladder, 5 s to 5 min, bounds
 * connects), and the socket counters are tagged with {@link #sourceId()}.
 */
final class MeshChannelStreamContext implements StreamContext {

    private final String id;
    private final Clock clock;

    /**
     * Creates the context.
     *
     * @param id the pseudo source id of the socket counters ({@code channel-meshcore-<id>})
     * @param clock the receive-time clock
     */
    MeshChannelStreamContext(String id, Clock clock) {
        this.id = id;
        this.clock = clock;
    }

    @Override
    public String sourceId() {
        return id;
    }

    @Override
    public SourceDescriptor descriptor() {
        return null;
    }

    @Override
    public SourceHttp http() {
        throw new UnsupportedOperationException("A mesh channel has no HTTP client.");
    }

    @Override
    public ValidatedParams params() {
        return null;
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
        throw new UnsupportedOperationException("A mesh channel has no adapter state.");
    }

    @Override
    public void chargeRequest(String label) {
        // A channel's companion connects are not budgeted; the link's reconnect ladder bounds them.
    }

    @Override
    public void recordReceivedBytes(long bytes) {
        // Not metered per channel.
    }

    @Override
    public void connected() {
        // The link logs its own connection changes.
    }

    @Override
    public void disconnected(Throwable error) {
        // The link reconnects on its own.
    }
}
