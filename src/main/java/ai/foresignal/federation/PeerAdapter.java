package ai.foresignal.federation;

import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;

/**
 * The streaming adapter of a {@code PULL} peer's {@code peer-{id}} source (prompt 113): each start runs one
 * {@link PeerSession} pass, and the streaming supervisor (010) reconnects it.
 */
public final class PeerAdapter implements StreamingAdapter {

    private final SourceDescriptor descriptor;
    private final String peerId;
    private final PeerRuntime runtime;

    /** Creates the adapter. */
    public PeerAdapter(SourceDescriptor descriptor, String peerId, PeerRuntime runtime) {
        this.descriptor = descriptor;
        this.peerId = peerId;
        this.runtime = runtime;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The peer. */
    public String peerId() {
        return peerId;
    }

    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) {
        return runtime.startSession(peerId, ctx, sink);
    }
}
