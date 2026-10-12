package ai.foresignal.stream.sse;

/** Receives a topic's replayed events, in cursor order, on the client's sender thread. */
@FunctionalInterface
public interface ReplaySink {

    /**
     * Sends one replayed event.
     *
     * @return false when the replay cap is reached: the event was not sent and the topic stops and
     *     returns {@link ReplayOutcome.CapReached}
     */
    boolean emit(FifoEvent event);
}
