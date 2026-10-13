package ai.foresignal.collect;

import ai.foresignal.source.spi.SocketTransport;
import ai.foresignal.source.spi.StreamContext;

/** What a stream context answers when an optional service is not installed (prompt 069). */
final class StreamContextDefaults {

    private StreamContextDefaults() {}

    /**
     * The answer of {@link StreamContext#sockets()} without a transport: the SPI default, which throws
     * {@link UnsupportedOperationException}.
     */
    static SocketTransport noSockets(StreamContext ctx) {
        throw new UnsupportedOperationException("No socket transport is available to " + ctx.sourceId()
                + " in this context.");
    }
}
