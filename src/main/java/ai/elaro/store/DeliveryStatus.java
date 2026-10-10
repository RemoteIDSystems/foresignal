package ai.elaro.store;

/** Lifecycle of one webhook delivery attempt chain. */
public enum DeliveryStatus {
    /** Queued, or waiting for its next retry. */
    PENDING,
    /** The endpoint accepted it. */
    DELIVERED,
    /** Retries are exhausted. */
    FAILED
}
