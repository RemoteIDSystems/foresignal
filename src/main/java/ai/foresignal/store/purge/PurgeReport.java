package ai.foresignal.store.purge;

/**
 * OpenAPI {@code PurgeReport}: the outcome of an entity purge (prompt 039). Messages already delivered
 * to external channels cannot be recalled.
 *
 * @param tombstoneSeq the seq of the tombstone version
 * @param versionsDeleted how many older versions were deleted (both tiers)
 * @param alertsRedacted alerts whose snapshot and message were redacted
 * @param alertsDeleted alerts deleted ({@code purgeAlerts})
 * @param embeddingsDeleted {@code embedding_document} rows deleted (the record's and its alerts')
 * @param notificationsRedacted persisted notifications whose bodies were redacted
 * @param citingDocuments documents citing the entity, counted but not edited
 */
public record PurgeReport(
        long tombstoneSeq,
        long versionsDeleted,
        int alertsRedacted,
        int alertsDeleted,
        int embeddingsDeleted,
        int notificationsRedacted,
        CitingDocuments citingDocuments) {

    /**
     * Ask messages and briefing runs whose citations reference the entity.
     *
     * @param askMessages Ask answers citing it
     * @param briefingRuns briefing runs citing it
     */
    public record CitingDocuments(int askMessages, int briefingRuns) {}
}
