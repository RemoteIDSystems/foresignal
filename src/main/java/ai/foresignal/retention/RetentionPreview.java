package ai.foresignal.retention;

import java.time.Instant;
import java.util.List;

/**
 * {@code GET /v1/settings/retention/preview} (prompt R2-015); OpenAPI schema {@code RetentionPreview}: what the next
 * retention run would prune under the current policy, computed without deleting anything.
 *
 * @param items one entry per subsystem and kind with something to prune or a cutoff in force
 * @param totalAffected the sum of every item's count
 * @param totalBytesEstimate the sum of the items' byte estimates, or null when none has one
 * @param dbCap the database-size cap, or null when it is off
 * @param generatedAt when the preview was computed
 */
public record RetentionPreview(List<Item> items, long totalAffected, Long totalBytesEstimate, DbCap dbCap,
        Instant generatedAt) {

    public RetentionPreview {
        items = items == null ? List.of() : List.copyOf(items);
    }

    /**
     * What one subsystem would prune; OpenAPI schema {@code RetentionPreviewItem}.
     *
     * @param subsystem {@code records} (STANDARD history), {@code track} (TRACK history leaves), {@code frames},
     *     {@code media} or {@code chronicles}
     * @param kind the record kind of a {@code records} item, or null
     * @param cutoff the oldest data kept, or null when no single cutoff applies
     * @param affectedCount the rows, frames, media or chronicles that would go
     * @param bytesEstimate an estimate of the bytes they hold, or null
     */
    public record Item(String subsystem, String kind, Instant cutoff, long affectedCount, Long bytesEstimate) {}

    /**
     * The database-size cap as the next run would find it.
     *
     * @param capBytes the cap
     * @param sizeBytes the database size now
     * @param exceeded whether the database is over the cap
     * @param windowDays the history window the cap has stepped down to, or null for the configured windows
     * @param overwriteOldest whether the run would step the window down further while the database keeps growing
     */
    public record DbCap(long capBytes, long sizeBytes, boolean exceeded, Integer windowDays,
            boolean overwriteOldest) {}
}
