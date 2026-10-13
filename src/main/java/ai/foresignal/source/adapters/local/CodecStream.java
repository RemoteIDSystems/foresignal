package ai.foresignal.source.adapters.local;

import ai.foresignal.source.codec.CodecContext;
import ai.foresignal.source.codec.CodecSession;
import ai.foresignal.source.codec.SocketCodec;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;

/**
 * Runs a {@link SocketCodec} over a TCP connection from {@link StreamContext#sockets()} (prompt 069): the
 * shared body of the socket sources.
 *
 * <p>One {@link CodecSession} per connection, so per-connection state (SBS's per-hex snapshots) starts empty
 * after every reconnect; the codec's {@link SocketCodec#onConnect()} bytes are written when the connection
 * opens. Records go to the sink as each frame completes them, and the sink may block (back-pressure).
 */
final class CodecStream {

    private CodecStream() {}

    /**
     * Connects to the {@code tcp://} endpoint in {@code endpointParam} and decodes with {@code codec}.
     *
     * @throws AdapterException when the budget is spent or the connection cannot be opened
     */
    static StreamHandle start(StreamContext ctx, RecordSink sink, SocketCodec codec, String endpointParam,
            SocketOptions options) throws AdapterException {
        LanEndpoint endpoint = LanEndpoint.fromParams(ctx.params(), endpointParam);
        CodecSession session = codec.newSession(new CodecContext(ctx.sourceId(), ctx.params()));
        SocketOptions opts = options == null ? SocketOptions.defaults() : options;
        byte[] hello = codec.onConnect();
        if (hello != null) {
            opts = opts.withOnConnect(hello);
        }
        return ctx.sockets().tcp(endpoint, opts, codec.framing(),
                (frame, receivedAt) -> sink.acceptAll(session.onFrame(frame, receivedAt)));
    }
}
