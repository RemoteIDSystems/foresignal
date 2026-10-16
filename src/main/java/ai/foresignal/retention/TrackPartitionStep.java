package ai.foresignal.retention;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.store.history.PartitionManager;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 1 (Core-Data-Model §13): TRACK history is removed only by dropping whole {@code rv_t_*}
 * leaves whose upper bound is at or below {@code cutoffSeq(now − trackWindow)}, through
 * {@link PartitionManager#drop} (which takes the manager's lock and refuses leaves the allocator has
 * not passed). The leaves and the clamped cutoff come from {@link RetentionPlanner#track}, the same
 * plan the 111 hold copy and cold archive wrote out before this step.
 *
 * <p>Leaves drop in order and the step stops at the first leaf it may not drop: one that a hold copy
 * or archive write of this run could not cover ({@link RetentionRun#leafBlocked}, prompt 111), or
 * one whose drop fails. Afterwards every TRACK source's floor is the lower bound of the oldest
 * remaining track leaf (or the end of the last dropped leaf when none remains), and each TRACK source
 * becomes a head-purge candidate at that floor.
 */
@Component
public class TrackPartitionStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(TrackPartitionStep.class);

    private final PartitionManager partitions;
    private final SourceRegistry registry;
    private final RetentionPlanner planner;

    /** Creates the step. */
    public TrackPartitionStep(PartitionManager partitions, SourceRegistry registry, RetentionPlanner planner) {
        this.partitions = partitions;
        this.registry = registry;
        this.planner = planner;
    }

    @Override
    public String name() {
        return "track-partitions";
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public void run(RetentionRun run) {
        Duration window = run.settings().trackWindow();
        RetentionPlanner.TrackPlan plan = planner.track(run);
        long next = 0L;
        for (LeafInfo leaf : plan.droppable()) {
            if (run.leafBlocked(leaf.name())) {
                log.warn("Retention keeps track partition {} this run: a hold copy or archive write of its rows failed",
                        leaf.name());
                break;
            }
            try {
                partitions.drop(HistoryTier.TRACK, leaf.name());
                run.partitionDropped();
                next = leaf.toSeq();
            } catch (RuntimeException e) {
                log.warn("Retention could not drop track partition {}; trying again next run", leaf.name(), e);
                run.error("track partition " + leaf.name() + ": " + e.getMessage());
                break;
            }
        }
        partitions.refresh();
        List<LeafInfo> remaining = partitions.leaves(HistoryTier.TRACK).stream()
                .sorted(Comparator.comparingLong(LeafInfo::fromSeq)).toList();
        long floor = remaining.isEmpty() ? next : remaining.getFirst().fromSeq();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.isTrack()) {
                run.floor(descriptor.id(), null, floor);
                run.candidate(new RetentionRun.PurgeCandidate(descriptor.id(), null, floor, window));
            }
        }
    }
}
