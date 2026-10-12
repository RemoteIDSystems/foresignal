package ai.foresignal.retention;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.store.history.PartitionManager;
import java.util.Comparator;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 3 (Core-Data-Model §13): drops every {@code rv_s_*} leaf whose upper bound is at or below
 * {@code cutoffSeq(now − largest effective STANDARD window)}. Step 2 has already emptied such a leaf;
 * a leaf that still holds rows (a source skipped after a failed delete) is left for the next run.
 */
@Component
public class StandardLeafDropStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(StandardLeafDropStep.class);

    private static final Pattern LEAF = Pattern.compile("rv_s_\\d+");

    private final PartitionManager partitions;

    /** Creates the step. */
    public StandardLeafDropStep(PartitionManager partitions) {
        this.partitions = partitions;
    }

    @Override
    public String name() {
        return "standard-partitions";
    }

    @Override
    public int order() {
        return 300;
    }

    @Override
    public void run(RetentionRun run) {
        // Unclamped: only empty leaves are dropped, so no consumer can lose an unprocessed row here,
        // and clamping would report consumers as lagging against this window for nothing (prompt 022).
        long cutoff = run.rawCutoff(run.largestStandardWindow());
        partitions.refresh();
        for (LeafInfo leaf : partitions.leaves(HistoryTier.STANDARD).stream()
                .sorted(Comparator.comparingLong(LeafInfo::fromSeq)).toList()) {
            if (leaf.toSeq() > cutoff) {
                break;
            }
            if (!LEAF.matcher(leaf.name()).matches()) {
                continue;
            }
            Boolean rows = run.jdbc().getJdbcTemplate()
                    .queryForObject("SELECT EXISTS (SELECT 1 FROM " + leaf.name() + ")", Boolean.class);
            if (Boolean.TRUE.equals(rows)) {
                log.info("Standard partition {} still holds rows; it will be dropped once they are deleted", leaf.name());
                continue;
            }
            try {
                partitions.drop(HistoryTier.STANDARD, leaf.name());
                run.partitionDropped();
            } catch (RuntimeException e) {
                log.warn("Retention could not drop standard partition {}; trying again next run", leaf.name(), e);
                run.error("standard partition " + leaf.name() + ": " + e.getMessage());
                break;
            }
        }
    }
}
