package ai.foresignal.retention;

import java.util.OptionalLong;

/**
 * A durable consumer of {@code record_version} whose cursor holds retention back (the consumer
 * clamp, Core-Data-Model §13, invariant RET-2).
 *
 * <p>Every retention cutoff is clamped to {@code min(cursorSeq) + 1}, so retention never deletes a
 * version a consumer has not processed. When a cursor is older than {@code now − 2 × window}, the
 * clamp is released for that run, {@code foresignal.retention.consumer_lag} is incremented and a
 * {@code system} notification is published.
 *
 * <p>Register one by declaring a bean: the trigger engine's {@code trigger.cursor} now, the
 * embedding cursor in prompt 027, and the intelligence block's durable cursors (116–134) later.
 */
public interface RetentionConsumer {

    /** A stable name, for example {@code trigger.cursor}; reported in {@code consumerClamp.consumer}. */
    String name();

    /**
     * The last seq this consumer has fully processed, or empty when it cannot say (it then does not
     * clamp this run).
     */
    OptionalLong cursorSeq();
}
