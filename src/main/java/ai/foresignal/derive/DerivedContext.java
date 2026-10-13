package ai.foresignal.derive;

import ai.foresignal.raster.sample.RasterReader;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What a {@link DerivedGenerator} is given for one invocation (prompt 052).
 *
 * <p>Everything it emits during the invocation goes to the ingest writer as <strong>one</strong> batch
 * when the invocation returns, with one {@code fetch_log} row ({@code principal=derived}). The context
 * is valid only during the call that received it.
 */
public interface DerivedContext {

    /** The derived source. */
    String sourceId();

    /** The source's validated parameters, defaults applied. */
    ValidatedParams params();

    /**
     * Emits one record version.
     *
     * <p>Skipped when its payload and geometry equal the entity's previous emit (emit-on-change).
     * {@code observedAt} is raised to the entity's previous emit time when it is earlier, so a staleness
     * change seen on a tick never makes a later frame LATE (invariant ING-4).
     *
     * @param record the record; its kind must be one of the source's kinds
     */
    void emit(NormalizedRecord record);

    /**
     * Tombstones an entity: only when the object disappeared or its subject was deleted, never because
     * input is stale (Weather-Imagery-Design §7.1). Skipped when the entity is already tombstoned or
     * never existed.
     *
     * @param kind the entity's kind
     * @param externalId the entity
     */
    void tombstone(String kind, String externalId);

    /** Per-source scratch state that survives restarts. */
    AdapterStateStore state();

    /** Read-only raster access: products, frames, point and area sampling, the GLM ring. */
    RasterReader rasters();

    /** Read-only current records ({@code record_head ⋈ record_live}). */
    DerivedRecords records();

    /** The operator's places and the places AOI. */
    DerivedPlaces places();

    /** The health status of another source (for "failing input" statuses); {@code IDLE} when unknown. */
    HealthStatus sourceStatus(String sourceId);

    /** Inject this rather than calling {@code Instant.now()}, so tests control time. */
    Clock clock();

    /** Convenience for {@code clock().instant()}. */
    default Instant now() {
        return clock().instant();
    }

    /** The logger of this source, named {@code foresignal.source.<id>}. */
    default Logger log() {
        return LoggerFactory.getLogger("foresignal.source." + sourceId());
    }
}
