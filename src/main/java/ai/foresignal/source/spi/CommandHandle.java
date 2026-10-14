package ai.foresignal.source.spi;

import java.io.IOException;

/**
 * The handle of a TCP connection opened with {@link SocketOptions#commandWriter(CommandGuard)} (prompt 083): the only
 * socket handle with a write method, and every write passes the connection's {@link CommandGuard} first.
 *
 * <p>Every other handle stays receive-only (prompt 082): {@link SocketTransport#tcp} returns a plain
 * {@link StreamHandle} unless the options carry a guard.
 */
public interface CommandHandle extends StreamHandle {

    /**
     * Writes one frame after the guard allowed it.
     *
     * @param frame the exact bytes to write, envelope included
     * @throws CommandGuard.CommandRefusedException when the guard refuses it; nothing is written
     * @throws IOException when the connection is closed or the write fails
     */
    void send(byte[] frame) throws IOException;
}
