package ai.foresignal.system;

/**
 * One resource reading (prompt R2-014); OpenAPI schema {@code ResourceGauge}. Every amount is null when it cannot be
 * read.
 *
 * @param totalBytes the total
 * @param usedBytes the amount in use
 * @param freeBytes the amount free
 * @param usedPercent {@code usedBytes / totalBytes} as a percent
 * @param status {@code ok}, {@code warn} or {@code critical} against the admin's thresholds
 */
public record ResourceGauge(Long totalBytes, Long usedBytes, Long freeBytes, Double usedPercent, String status) {

    /** A reading that could not be taken. */
    public static final ResourceGauge UNKNOWN = new ResourceGauge(null, null, null, null, "ok");

    /**
     * A reading from a total and a free amount.
     *
     * @param total the total, in bytes
     * @param free the free amount, in bytes
     * @return the gauge, status {@code ok}
     */
    public static ResourceGauge of(long total, long free) {
        long boundedFree = Math.max(0, Math.min(free, total));
        long used = total - boundedFree;
        Double percent = total > 0 ? 100.0 * used / total : null;
        return new ResourceGauge(total, used, boundedFree, percent, "ok");
    }

    /**
     * The free share of {@code gauge}, as a percent.
     *
     * @param gauge the gauge
     * @return the free percent, or null when unknown
     */
    public static Double freePercent(ResourceGauge gauge) {
        return gauge.usedPercent() == null ? null : 100.0 - gauge.usedPercent();
    }

    /**
     * This reading with {@code value} as its status.
     *
     * @param value the status
     * @return the gauge
     */
    public ResourceGauge withStatus(String value) {
        return new ResourceGauge(totalBytes, usedBytes, freeBytes, usedPercent, value);
    }
}
