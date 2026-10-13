package ai.foresignal.source.spi;

/**
 * How {@link SourceHttp#openWebSocket(java.net.URI, java.net.http.WebSocket.Listener, java.util.Map,
 * WebSocketOptions)} opens a connection (prompt 063).
 *
 * <p>The JDK's {@code java.net.http.WebSocket} cannot negotiate WebSocket extensions. When
 * {@link #perMessageDeflate} is true the connection is opened by a client that offers
 * {@code permessage-deflate} (RFC 7692) instead; the budget, the User-Agent and the metering are the same
 * either way. aisstream.io limits the bandwidth of uncompressed connections, so its adapter asks for it.
 *
 * @param perMessageDeflate true to offer {@code permessage-deflate} in the opening handshake
 */
public record WebSocketOptions(boolean perMessageDeflate) {

    /** The JDK client with no extensions: what {@code openWebSocket(uri, listener, headers)} does. */
    public static final WebSocketOptions DEFAULT = new WebSocketOptions(false);

    /** Offers {@code permessage-deflate}. */
    public static final WebSocketOptions DEFLATE = new WebSocketOptions(true);
}
