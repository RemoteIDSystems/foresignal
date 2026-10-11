package ai.foresignal.stream;

/** A handle on one bus subscription; closing it unsubscribes and stops its delivery thread. */
public interface Subscription extends AutoCloseable {

    /** The subscriber's name, used in thread names and metric tags. */
    String name();

    /** Unsubscribes. Idempotent; queued batches are discarded. */
    @Override
    void close();
}
