package ai.foresignal.source.spi;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A read-only view of other sources' current records and health, for an adapter that reacts to them
 * (prompt 107: the EMWIN warnings fallback compares against {@code nws-alerts} and checks its health).
 *
 * <p>Adapters depend only on SPI types (Architecture §3), so this reaches them through this interface;
 * a service bean implements it. It never causes an upstream fetch.
 */
public interface CurrentRecords {

    /**
     * One current head.
     *
     * @param sourceId the source
     * @param externalId the entity id
     * @param kind the record kind
     * @param observedAt the observation time
     * @param payload the current payload
     */
    record Row(String sourceId, String externalId, String kind, Instant observedAt, Map<String, Object> payload) {}

    /** The current, live rows of the given sources and kinds, newest first, capped at {@code limit}. */
    List<Row> current(List<String> sources, List<String> kinds, int limit);

    /** The wire value of a source's health ({@code healthy}, {@code degraded}, {@code failing}, …). */
    String health(String sourceId);

    /** True when a source is not {@code failing} and not {@code disabled}. */
    default boolean available(String sourceId) {
        String status = health(sourceId);
        return !"FAILING".equalsIgnoreCase(status) && !"DISABLED".equalsIgnoreCase(status);
    }
}
