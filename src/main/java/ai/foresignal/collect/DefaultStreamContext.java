package ai.foresignal.collect;

import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.SocketTransport;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The {@link StreamContext} the streaming supervisor hands an adapter for one connection.
 *
 * <p>Adds the stream's own reporting to {@link DefaultFetchContext}: received bytes go to the
 * {@link UsageMeter} without counting a request, and the connection callbacks go to the
 * supervisor, which records them in health and schedules reconnects. A probe gets the same context
 * with callbacks that do nothing, because a probe never changes health.
 */
public class DefaultStreamContext extends DefaultFetchContext implements StreamContext {

    /** What the supervisor wants to hear about a connection. */
    public interface Callbacks {

        /** The stream is connected and delivering. */
        void connected();

        /** The stream dropped; {@code error} is null for a clean close. */
        void disconnected(Throwable error);

        /** Callbacks that ignore everything, for probes. */
        static Callbacks none() {
            return new Callbacks() {
                @Override
                public void connected() {}

                @Override
                public void disconnected(Throwable error) {}
            };
        }
    }

    private final UsageMeter usage;
    private final Callbacks callbacks;
    private java.util.function.Function<DefaultStreamContext, SocketTransport> socketFactory;
    private SocketTransport sockets;

    /** Creates a stream context; see {@link DefaultFetchContext} for the shared parameters. */
    public DefaultStreamContext(
            SourceDescriptor descriptor,
            UpstreamClient upstream,
            RequestRecorder recorder,
            Supplier<ValidatedParams> params,
            CredentialsAccess credentials,
            Clock clock,
            Supplier<Optional<Instant>> lastSuccessAt,
            AdapterStateStore state,
            UsageMeter usage,
            Callbacks callbacks) {
        super(descriptor, upstream, recorder, params, credentials, clock, lastSuccessAt, state);
        this.usage = usage;
        this.callbacks = callbacks == null ? Callbacks.none() : callbacks;
    }

    @Override
    public void recordReceivedBytes(long bytes) {
        if (bytes > 0 && usage != null) {
            usage.recordBytes(sourceId(), bytes);
        }
    }

    /**
     * Installs the socket transport behind {@link #sockets()} (prompt 069); called by
     * {@code FetchContextFactory}.
     *
     * @param factory builds the transport for this context, on first use
     */
    public void socketTransport(java.util.function.Function<DefaultStreamContext, SocketTransport> factory) {
        this.socketFactory = factory;
    }

    /** The socket transport of this context, built on first use (prompt 069). */
    @Override
    public synchronized SocketTransport sockets() {
        if (sockets == null) {
            if (socketFactory == null) {
                return StreamContextDefaults.noSockets(this);
            }
            sockets = socketFactory.apply(this);
        }
        return sockets;
    }

    @Override
    public void connected() {
        callbacks.connected();
    }

    @Override
    public void disconnected(Throwable error) {
        callbacks.disconnected(error);
    }
}
