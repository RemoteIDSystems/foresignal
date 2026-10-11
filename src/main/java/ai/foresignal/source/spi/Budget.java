package ai.foresignal.source.spi;

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
 * @param perMinute requests per minute
 * @param perDay requests per UTC day
 * @param mbPerDay optional megabytes per UTC day; null means no byte budget
 */
public record Budget(int perMinute, int perDay, Integer mbPerDay) {

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
}
