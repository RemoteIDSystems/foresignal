package ai.foresignal.source.spi;

import java.math.BigDecimal;

/**
 * How many upstream requests a source may spend.
 *
 * <p>Foresignal refuses rather than borrowing against a budget, which is what turns "a good upstream
 * citizen" into a property of the system instead of an intention.
 *
 * <p>Raster and imagery sources can also cap bytes: one range read of a model grid can be larger
 * than a day of a vector feed, so a request count alone would not bound the cost
 * (Weather-Imagery-Design §2.2).
 *
 * <p>Paid sources can also cap money: {@code monthlyCapUsd} is the most a source may spend in a UTC calendar month,
 * as its {@code SourceSpendLookup} reports it (OpenAPI {@code Budget.monthlyCapUsd}, 135). Once the month's spend
 * reaches it, every request is refused until the next month begins.
 *
 * @param perMinute requests per minute
 * @param perDay requests per UTC day
 * @param mbPerDay optional megabytes per UTC day; null means no byte budget
 * @param monthlyCapUsd optional US dollars per UTC calendar month; null means no money cap
 */
public record Budget(int perMinute, int perDay, Integer mbPerDay, BigDecimal monthlyCapUsd) {

    /** A budget with no money cap. */
    public Budget(int perMinute, int perDay, Integer mbPerDay) {
        this(perMinute, perDay, mbPerDay, null);
    }

    public static Budget of(int perMinute, int perDay) {
        return new Budget(perMinute, perDay, null);
    }

    /** A budget that also caps bytes. */
    public static Budget of(int perMinute, int perDay, int mbPerDay) {
        return new Budget(perMinute, perDay, mbPerDay);
    }

    /** True when this budget caps bytes as well as requests. */
    public boolean hasByteBudget() {
        return mbPerDay != null && mbPerDay > 0;
    }

    /** The byte budget in bytes, or null. */
    public Long bytesPerDay() {
        return hasByteBudget() ? mbPerDay * 1024L * 1024L : null;
    }

    /** True when this budget caps money per month. */
    public boolean hasMonthlyCap() {
        return monthlyCapUsd != null;
    }

    /** This budget with {@code cap} as its monthly money cap (null for none). */
    public Budget withMonthlyCapUsd(BigDecimal cap) {
        return new Budget(perMinute, perDay, mbPerDay, cap);
    }
}
