package ai.foresignal.federation;

import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;

/**
 * The streaming adapter of a {@code PULL} peer's {@code peer-{id}-trk} source (prompt 113). It opens no connection
 * of its own: while it runs, the peer's one session hands TRACK items to this source's sink, so they get the
 * TRACK tier, TTL and sampling policy. A TRACK item that arrives while it is stopped is written straight to the
 * writer.
 */
public final class PeerTrackAdapter implements StreamingAdapter {

    private final SourceDescriptor descriptor;
    private final String peerId;
    private final PeerRuntime runtime;

    /** Creates the adapter. */
    public PeerTrackAdapter(SourceDescriptor descriptor, String peerId, PeerRuntime runtime) {
        this.descriptor = descriptor;
        this.peerId = peerId;
        this.runtime = runtime;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) {
        return runtime.startTrack(peerId, sink, ctx);
    }
}
