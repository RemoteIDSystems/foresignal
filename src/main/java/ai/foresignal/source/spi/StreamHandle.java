package ai.foresignal.source.spi;

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
}
