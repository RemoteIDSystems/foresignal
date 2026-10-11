package ai.foresignal.collect;

import java.util.List;

/**
 * The collector's state, for {@code GET /v1/system} (prompt 019).
 *
 * @param running whether the scheduler is running
 * @param scheduledSources the polling sources with a schedule, ordered
 * @param activeFetches fetches running right now, scheduled or manual
 * @param activeStreams streams connected right now
 */
public record SchedulerState(boolean running, List<String> scheduledSources, int activeFetches, int activeStreams) {

    /** Copies the list. */
    public SchedulerState {
        scheduledSources = scheduledSources == null ? List.of() : List.copyOf(scheduledSources);
    }
}
