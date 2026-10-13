/**
 * The LAN-only socket transport of prompt 069: TCP client connections and UDP listeners for streaming
 * adapters, reached only through {@link ai.foresignal.source.spi.StreamContext#sockets()}.
 *
 * <ul>
 *   <li>{@link ai.foresignal.collect.socket.DefaultSocketTransport}: one source's transport, budgeted and
 *       metered through its stream context;
 *   <li>{@link ai.foresignal.collect.socket.SocketTransportFactory}: builds a transport per stream context;
 *   <li>{@link ai.foresignal.collect.socket.LineFramer}: cuts a byte stream into LF or CRLF lines of at
 *       most 64 KB;
 *   <li>{@link ai.foresignal.collect.socket.SocketMetrics}: the {@code foresignal.socket.*} counters.
 * </ul>
 *
 * <p>Foresignal never opens a TCP server socket for a source; inbound TCP serving is prompt 110's SERVE
 * sinks.
 */
package ai.foresignal.collect.socket;
