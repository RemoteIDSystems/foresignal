package ai.foresignal.retention;

import java.util.List;

/**
 * Supplies {@link RetentionConsumer}s whose number changes at run time (prompt 131): one durable cursor per active
 * chronicle ({@code chronicle.cursor.{id}}). {@link RetentionJob} asks every provider at the start of each run and
 * clamps by the consumers it returns exactly as by the consumer beans, including the release-after-2×-window rule
 * with its alarm.
 */
public interface RetentionConsumerProvider {

    /** The consumers in force now; empty when there are none. */
    List<RetentionConsumer> retentionConsumers();
}
