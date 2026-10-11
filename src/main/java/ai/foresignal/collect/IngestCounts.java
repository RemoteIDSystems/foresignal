package ai.foresignal.collect;

/**
 * What one ingest batch did, for the per-minute usage row (Core-Data-Model §8).
 *
 * <p>Separate counters rather than one "records" number, because the distinctions are the whole
 * point of the ingest policies: 10,000 observations producing 200 versions and 9,800 touches is a
 * healthy sampled source, while the same counts with 9,800 rejections is a broken adapter.
 *
 * @param recordsSeen observations received
 * @param versions new {@code record_version} rows written
 * @param liveUpdates live-row updates with no new version
 * @param touches rows whose timestamp alone was refreshed
 * @param late observations dropped for arriving out of order
 * @param skipped observations deliberately not persisted by the policy
 * @param rejected observations refused for violating a limit
 * @param versionBytes approximate bytes of version payload written
 */
public record IngestCounts(
        long recordsSeen,
        long versions,
        long liveUpdates,
        long touches,
        long late,
        long skipped,
        long rejected,
        long versionBytes) {

    public static final IngestCounts ZERO = new IngestCounts(0, 0, 0, 0, 0, 0, 0, 0);

    public static IngestCounts seen(long recordsSeen) {
        return new IngestCounts(recordsSeen, 0, 0, 0, 0, 0, 0, 0);
    }

    /** Sums two sets of counts, for accumulating across batches in one minute. */
    public IngestCounts plus(IngestCounts other) {
        return new IngestCounts(
                recordsSeen + other.recordsSeen,
                versions + other.versions,
                liveUpdates + other.liveUpdates,
                touches + other.touches,
                late + other.late,
                skipped + other.skipped,
                rejected + other.rejected,
                versionBytes + other.versionBytes);
    }

    public boolean isZero() {
        return equals(ZERO);
    }
}
