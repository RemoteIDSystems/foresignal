package ai.foresignal.retention;

import java.time.Duration;
import java.util.Set;

/**
 * What retention has done, as read clients need to know it (Core-Data-Model §13, Conventions
 * "RetentionState.purgeSeq").
 *
 * <p>Defined by prompt 014; {@link RetentionStateStore} (prompt 019) is the implementation, which the
 * retention job republishes after every run.
 */
public interface RetentionState {

    /**
     * The global seq allocated when retention last purged heads of {@code sourceId}, that is when
     * entities disappeared without a tombstone; 0 when it never has.
     *
     * <p>Advances <strong>only</strong> on head purges. Routine version deletes and partition drops
     * do not advance it, or every client would have to resync after each hourly retention run. A
     * change-feed client whose {@code after} is below it gets {@code 410 RESYNC_REQUIRED}.
     */
    long purgeSeq(String sourceId);

    /**
     * The resume floor over {@code sources} (all sources when empty): the largest {@code floorSeq} of
     * the set, so a replay from {@code after} is complete only when {@code after ≥} it; 0 when
     * retention has removed nothing yet.
     */
    long lowestRetainedSeq(Set<String> sources);

    /** The retention window of {@code sourceId}: {@code retention.trackWindow} for TRACK, else the effective window. */
    Duration window(String sourceId);
}
