package ai.foresignal.source.spi;

import java.util.function.Consumer;

/**
 * A running stream, closed when the source is disabled or Foresignal shuts down.
 *
 * <p>{@link #close()} must be idempotent and must not block indefinitely: the supervisor closes
 * handles during shutdown and while reconciling settings changes.
 */
public interface StreamHandle extends AutoCloseable {

    @Override
    void close();

    /** True while the stream is connected and delivering. */
    default boolean isRunning() {
        return true;
    }

    /**
     * Registers a callback for when the stream ends on its own: the upstream closed it, or its read
     * loop failed.
     *
     * <p>The callback receives the failure, or null for a clean close. It is not required to run
     * for a {@link #close()} the supervisor itself made. The default does nothing, in which case
     * the supervisor falls back to polling {@link #isRunning()}; adapters should implement it so a
     * drop is noticed immediately.
     */
    default void onClose(Consumer<Throwable> callback) {}

    /**
     * Applies changed parameters to the running stream without reconnecting (prompt 063).
     *
     * <p>The supervisor calls it when an operator changes the source's parameters while the stream runs. An
     * adapter whose upstream can change a subscription in place (aisstream resends its subscription, which
     * replaces the previous one) applies {@code params} and returns true. Returning false asks the
     * supervisor to restart the stream with the new parameters, which costs a connection attempt. The
     * default returns false.
     *
     * @param params the new effective parameters
     * @return true when applied in place, false to be restarted
     */
    default boolean update(ValidatedParams params) {
        return false;
    }
}
