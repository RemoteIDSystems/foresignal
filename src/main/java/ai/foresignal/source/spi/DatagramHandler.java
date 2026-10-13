package ai.foresignal.source.spi;

import java.net.InetSocketAddress;
import java.time.Instant;

/**
 * Receives the datagrams of a UDP listener opened through {@link SocketTransport#udp} (prompt 069).
 *
 * <p>Only datagrams from an allowed sender reach it; the others are dropped and counted before anything
 * reads them. Called on the listener's virtual thread, one datagram at a time; it may block.
 */
@FunctionalInterface
public interface DatagramHandler {

    /**
     * Handles one datagram.
     *
     * @param datagram the datagram's bytes, at most {@value SocketTransport#MAX_DATAGRAM_BYTES}; owned by
     *     the handler
     * @param sender the sender's address, inside the allowlist
     * @param receivedAt when the transport read it, from the context clock
     * @throws Exception to close the listener; the stream then reports the failure and the supervisor
     *     restarts it on its ladder
     */
    void onDatagram(byte[] datagram, InetSocketAddress sender, Instant receivedAt) throws Exception;
}
