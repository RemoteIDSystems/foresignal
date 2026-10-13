package ai.foresignal.source.spi;

import java.net.http.WebSocket;

/**
 * A WebSocket that knows how many bytes it read off the wire (prompt 063).
 *
 * <p>With {@code permessage-deflate} the messages a listener receives are inflated, so their sizes are not
 * what the connection cost. A connection opened with {@link WebSocketOptions#perMessageDeflate()} implements
 * this interface; a streaming adapter reports the deltas of {@link #wireBytesReceived()} through
 * {@link StreamContext#reportBytes} instead of the message sizes, so the source is metered on compressed
 * bytes. A JDK connection does not implement it, and the adapter reports message sizes as before.
 */
public interface MeteredWebSocket extends WebSocket {

    /** The frame bytes read since the handshake completed, compressed as they arrived. */
    long wireBytesReceived();

    /** True when the server accepted {@code permessage-deflate} in the opening handshake. */
    boolean perMessageDeflateNegotiated();
}
