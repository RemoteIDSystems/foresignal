package ai.foresignal.ingest;

/**
 * One record the normalizer refused.
 *
 * @param index the record's position in the batch as submitted
 * @param externalId its external id, possibly blank
 * @param reason why
 * @param detail a human-readable explanation for logs; not stored
 */
public record Rejection(int index, String externalId, RejectionReason reason, String detail) {}
