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
 * not passed).
 *
 * <p>Afterwards every TRACK source's floor is the lower bound of the oldest remaining track leaf (or
 * the end of the last dropped leaf when none remains), and each TRACK source becomes a head-purge
 * candidate at that floor.
 */
@Component
public class TrackPartitionStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(TrackPartitionStep.class);

    private final PartitionManager partitions;
    private final SourceRegistry registry;

    /** Creates the step. */
    public TrackPartitionStep(PartitionManager partitions, SourceRegistry registry) {
        this.partitions = partitions;
        this.registry = registry;
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
        long cutoff = run.cutoff(window);
        partitions.refresh();
        long next = 0L;
        for (LeafInfo leaf : sorted(partitions.leaves(HistoryTier.TRACK))) {
            if (leaf.toSeq() > cutoff) {
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
        List<LeafInfo> remaining = sorted(partitions.leaves(HistoryTier.TRACK));
        long floor = remaining.isEmpty() ? next : remaining.getFirst().fromSeq();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.isTrack()) {
                run.floor(descriptor.id(), null, floor);
                run.candidate(new RetentionRun.PurgeCandidate(descriptor.id(), null, floor, window));
            }
        }
    }

    private static List<LeafInfo> sorted(List<LeafInfo> leaves) {
        return leaves.stream().sorted(Comparator.comparingLong(LeafInfo::fromSeq)).toList();
    }
}
