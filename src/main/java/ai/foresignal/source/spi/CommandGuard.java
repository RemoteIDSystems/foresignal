package ai.foresignal.source.spi;

/**
 * The allowlist a command-writing socket checks before every write (prompt 083).
 *
 * <p>Only {@link SocketOptions#commandWriter(CommandGuard)} connections can write after connecting, and only through
 * {@link CommandHandle#send}, which hands every frame to {@link #check} first. A guard is code-reviewed protocol knowledge
 * (prompt 083's {@code MeshCoreCommandGuard}), never operator input.
 */
@FunctionalInterface
public interface CommandGuard {

    /**
     * Refuses a frame that may not be written.
     *
     * @param frame the exact bytes about to be written, envelope included
     * @throws CommandRefusedException when the frame is not allowed; nothing is written
     */
    void check(byte[] frame) throws CommandRefusedException;

    /** A command a {@link CommandGuard} refused; nothing was written. */
    final class CommandRefusedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /** Creates the exception with the reason. */
        public CommandRefusedException(String message) {
            super(message);
        }
    }
}
