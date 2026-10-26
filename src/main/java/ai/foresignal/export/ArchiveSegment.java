package ai.foresignal.export;

import java.time.Instant;
import java.util.List;

/**
 * One row of {@code archive_manifest} (prompt R2-016): an archive segment, the history one archive run moved from the
 * hot database to a cold tier.
 *
 * @param id the ULID
 * @param status {@code pending}, {@code running}, {@code verifying}, {@code complete}, {@code purged}, {@code failed},
 *     {@code restoring} or {@code restored}
 * @param trigger {@code manual} ({@code runArchive}), {@code scheduled} or {@code cap} (the retention size cap)
 * @param domain what was archived: {@code record_version}
 * @param target the tier: {@code local}, {@code s3} or {@code glacier}
 * @param location the segment's directory or {@code s3://bucket/prefix}
 * @param cutoff the archive-run cutoff, or null when the run took only what retention removes anyway
 * @param sources the sources in the segment
 * @param fromSeq the lowest seq archived, or null when empty
 * @param toSeq the highest seq archived, or null when empty
 * @param fromTime the earliest {@code ingested_at} archived, or null
 * @param toTime the latest {@code ingested_at} archived, or null
 * @param sizeBytes the compressed size of every file
 * @param itemCount the rows archived
 * @param checksum the SHA-256 over every file's {@code key:sha256} line, in key order
 * @param files the files
 * @param purgedFromHot true once no archived row is left in {@code record_version}
 * @param error why it failed, or a note on what it could not do
 * @param createdAt when the run was asked for
 * @param updatedAt the last change
 * @param completedAt when every file was verified on the tier
 * @param purgedAt when the purge pass finished
 * @param restoredAt when the last restore finished
 */
public record ArchiveSegment(String id, String status, String trigger, String domain, String target, String location,
        Instant cutoff, List<String> sources, Long fromSeq, Long toSeq, Instant fromTime, Instant toTime,
        Long sizeBytes, Long itemCount, String checksum, List<SegmentFile> files, boolean purgedFromHot, String error,
        Instant createdAt, Instant updatedAt, Instant completedAt, Instant purgedAt, Instant restoredAt) {

    public static final String PENDING = "pending";
    public static final String RUNNING = "running";
    public static final String VERIFYING = "verifying";
    public static final String COMPLETE = "complete";
    public static final String PURGED = "purged";
    public static final String FAILED = "failed";
    public static final String RESTORING = "restoring";
    public static final String RESTORED = "restored";

    /** Every status, for the {@code status} filter. */
    public static final List<String> STATUSES = List.of(PENDING, RUNNING, VERIFYING, COMPLETE, PURGED, FAILED,
            RESTORING, RESTORED);

    /** Fills defaults. */
    public ArchiveSegment {
        sources = sources == null ? List.of() : List.copyOf(sources);
        files = files == null ? List.of() : List.copyOf(files);
    }

    /**
     * One file of a segment: every archived row of one source.
     *
     * @param name the file name ({@code {tier}-{sourceId}.ndjson.gz})
     * @param key the key in the tier ({@code segments/{id}/{name}})
     * @param sourceId the source
     * @param tier {@code standard} or {@code track}
     * @param fromSeq the lowest seq
     * @param toSeq the highest seq
     * @param fromTime the earliest {@code ingested_at}
     * @param toTime the latest {@code ingested_at}
     * @param rows the rows
     * @param bytes the compressed size
     * @param sha256 the SHA-256 of the compressed file
     * @param location the stored copy ({@link ArchiveRef#location})
     * @param etag the object's ETag (object storage), or null
     */
    public record SegmentFile(String name, String key, String sourceId, String tier, long fromSeq, long toSeq,
            Instant fromTime, Instant toTime, long rows, long bytes, String sha256, String location, String etag) {

        /** The stored copy as the tier sees it. */
        public ArchiveRef ref(String target) {
            return new ArchiveRef(target, key, location, sha256, bytes, etag);
        }
    }

    /** A copy with {@code status} and {@code error} changed at {@code now}. */
    public ArchiveSegment withStatus(String newStatus, String newError, Instant now) {
        return new ArchiveSegment(id, newStatus, trigger, domain, target, location, cutoff, sources, fromSeq, toSeq,
                fromTime, toTime, sizeBytes, itemCount, checksum, files, purgedFromHot, newError, createdAt, now,
                completedAt, purgedAt, restoredAt);
    }

    /** True while a run or restore is working on it. */
    public boolean busy() {
        return PENDING.equals(status) || RUNNING.equals(status) || VERIFYING.equals(status)
                || RESTORING.equals(status);
    }
}
