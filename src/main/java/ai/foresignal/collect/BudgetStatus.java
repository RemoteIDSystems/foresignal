package ai.foresignal.collect;

/**
 * What a source has left to spend.
 *
 * <p>Surfaced by {@code GET /v1/sources/{id}/usage} in prompt 010, so an operator can see the spend
 * rather than infer it.
 *
 * @param sourceId the source
 * @param perMinute the configured per-minute limit
 * @param perDay the configured per-day limit
 * @param usedToday requests already made today, UTC
 * @param remainingToday how many more may be made today
 * @param minuteRemaining how many may be made in the current minute
 * @param mbPerDay the optional megabyte budget, or null
 * @param megabytesUsedToday bytes received today, as megabytes, when a megabyte budget applies
 */
public record BudgetStatus(
        String sourceId,
        int perMinute,
        int perDay,
        long usedToday,
        long remainingToday,
        long minuteRemaining,
        Integer mbPerDay,
        Double megabytesUsedToday) {

    /** True when nothing more may be sent right now. */
    public boolean isExhausted() {
        return remainingToday <= 0 || minuteRemaining <= 0;
    }

    /** True when a megabyte budget applies and is spent. */
    public boolean isByteBudgetExhausted() {
        return mbPerDay != null && megabytesUsedToday != null && megabytesUsedToday >= mbPerDay;
    }
}
