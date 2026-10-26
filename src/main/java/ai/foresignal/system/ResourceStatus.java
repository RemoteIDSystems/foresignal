package ai.foresignal.system;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The host-resource reading of {@code GET /v1/system/resources} (prompt R2-014); OpenAPI schema
 * {@code ResourceStatus}.
 *
 * @param status the worst of the gauges: {@code ok}, {@code warn} or {@code critical}
 * @param disk the fullest of the instance's data volumes
 * @param memory physical memory where the JVM can read it, otherwise the heap
 * @param database {@code sizeBytes}, the database size, or null when it cannot be read
 * @param warnings one human-readable message per breached gauge
 * @param checkedAt when the reading was taken
 */
public record ResourceStatus(String status, ResourceGauge disk, ResourceGauge memory, Map<String, Object> database,
        List<String> warnings, Instant checkedAt) {

    /**
     * The more severe of two levels.
     *
     * @param a a level
     * @param b another level
     * @return {@code critical}, else {@code warn}, else {@code ok}
     */
    public static String worst(String a, String b) {
        return rank(a) >= rank(b) ? a : b;
    }

    /**
     * The severity rank of a level.
     *
     * @param level {@code ok}, {@code warn} or {@code critical}
     * @return 0, 1 or 2
     */
    public static int rank(String level) {
        return switch (level == null ? "ok" : level) {
            case "critical" -> 2;
            case "warn" -> 1;
            default -> 0;
        };
    }
}
