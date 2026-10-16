package ai.foresignal.ingest;

/**
 * What one complete-set reconciliation did (prompt 094, {@link TombstoneAbsent}).
 *
 * @param ran true when the reconciliation wrote tombstones
 * @param discarded true when the shrink guard refused to write anything
 * @param candidates the records the run did not see
 * @param total the dataset's size when the run started (candidates plus the records the run saw)
 * @param tombstoned the tombstones actually written
 * @param reason the health reason when the guard tripped, else null
 */
public record TombstoneOutcome(
        boolean ran, boolean discarded, int candidates, int total, int tombstoned, String reason) {

    /**
     * The start of the health reason the shrink guard records ({@code "dataset shrank by N% — reconciliation
     * skipped"}); the executor clears a degradation with this prefix once a reconciliation runs again.
     */
    public static final String SHRINK_REASON_PREFIX = "dataset shrank by ";

    /** The reconciliation ran and wrote {@code tombstoned} tombstones. */
    public static TombstoneOutcome ran(int candidates, int total, int tombstoned) {
        return new TombstoneOutcome(true, false, candidates, total, tombstoned, null);
    }

    /** The shrink guard tripped; nothing was written. */
    public static TombstoneOutcome discarded(int candidates, int total, String reason) {
        return new TombstoneOutcome(false, true, candidates, total, 0, reason);
    }
}
