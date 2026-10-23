package ai.foresignal.collect;

import java.math.BigDecimal;

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
 * @param monthlyCapUsd the optional money cap per UTC calendar month, or null
 * @param spentUsdThisMonth US dollars spent this month, when a money cap applies and a ledger knows the source
 */
public record BudgetStatus(
        String sourceId,
        int perMinute,
        int perDay,
        long usedToday,
        long remainingToday,
        long minuteRemaining,
        Integer mbPerDay,
        Double megabytesUsedToday,
        BigDecimal monthlyCapUsd,
        Double spentUsdThisMonth) {

    /** A status with no money cap. */
    public BudgetStatus(String sourceId, int perMinute, int perDay, long usedToday, long remainingToday,
            long minuteRemaining, Integer mbPerDay, Double megabytesUsedToday) {
        this(sourceId, perMinute, perDay, usedToday, remainingToday, minuteRemaining, mbPerDay, megabytesUsedToday,
                null, null);
    }

    /** True when nothing more may be sent right now. */
    public boolean isExhausted() {
        return remainingToday <= 0 || minuteRemaining <= 0 || isMonthlyCapReached();
    }

    /** True when a money cap applies and this month's spend has reached it. */
    public boolean isMonthlyCapReached() {
        return monthlyCapUsd != null && spentUsdThisMonth != null
                && BigDecimal.valueOf(spentUsdThisMonth).compareTo(monthlyCapUsd) >= 0;
    }

    /** True when a megabyte budget applies and is spent. */
    public boolean isByteBudgetExhausted() {
        return mbPerDay != null && megabytesUsedToday != null && megabytesUsedToday >= mbPerDay;
    }
}
