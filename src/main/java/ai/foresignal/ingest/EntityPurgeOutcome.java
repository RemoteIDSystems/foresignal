package ai.foresignal.ingest;

/**
 * What an {@link EntityPurge} did on the writer.
 *
 * @param found false when the entity had no head (nothing was written)
 * @param tombstoneSeq the seq of the tombstone version, or 0 when not found
 * @param versionsDeleted how many older versions were deleted
 * @param kind the entity's kind, or null when not found
 */
public record EntityPurgeOutcome(boolean found, long tombstoneSeq, long versionsDeleted, String kind) {

    /** The outcome for an entity without a head. */
    public static EntityPurgeOutcome notFound() {
        return new EntityPurgeOutcome(false, 0L, 0L, null);
    }
}
