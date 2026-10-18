package ai.foresignal.raster;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * One stored frame (table {@code raster_frame}, Weather-Imagery-Design §2.3).
 *
 * <p>This entity maps the table (created by the Flyway baseline, validated by Hibernate); the raster code writes and reads it with
 * native SQL ({@link RasterFrameQueries}), because commits are upserts and retention deletes in
 * bulk. The {@code (product_id, valid_time DESC)} index and the GiST index on {@code footprint} are
 * in the Flyway baseline ({@code db/migration/V1__baseline.sql}), since JPA cannot express either.
 *
 * <p>{@code frame_id} is time-based and immutable: {@code 20261010T143240Z} for an observation,
 * {@code 2026101012Z-f006} for a forecast step. {@code sha256} hashes the <em>decoded</em> content,
 * so it is stable across codecs and gives the URL token {@code sha8}.
 */
@Entity
@IdClass(RasterFrameId.class)
@Table(name = "raster_frame", indexes = {
        @Index(name = "raster_frame_product_run_idx", columnList = "product_id, reference_time, forecast_step")})
public class RasterFrameEntity {

    @Id
    @Column(name = "product_id", length = 64, nullable = false)
    private String productId;

    @Id
    @Column(name = "frame_id", length = 32, nullable = false)
    private String frameId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_time", nullable = false)
    private Instant validTime;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "reference_time")
    private Instant referenceTime;

    @Column(name = "forecast_step")
    private Integer forecastStep;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "obs_start")
    private Instant obsStart;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "obs_end")
    private Instant obsEnd;

    @Column(name = "grid_id", length = 64, nullable = false)
    private String gridId;

    @Column(name = "path", length = 512, nullable = false)
    private String path;

    @Column(name = "bytes", nullable = false)
    private long bytes;

    @Column(name = "sha256", length = 64, nullable = false)
    private String sha256;

    @Column(name = "dtype", length = 8, nullable = false)
    private String dtype;

    @Column(name = "codec", nullable = false)
    private int codec;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats")
    private Map<String, Object> stats;

    @Column(name = "footprint", columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon footprint;

    @Column(name = "upstream_key", length = 512)
    private String upstreamKey;

    /** {@code partial} or {@code complete}. */
    @Column(name = "status", length = 8, nullable = false)
    private String status;

    @Column(name = "thinned", nullable = false)
    private boolean thinned;

    /** True on every frame of a forecast run marked complete (prompt 042; not in the design's list). */
    @Column(name = "run_complete", nullable = false, columnDefinition = "boolean default false")
    private boolean runComplete;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at")
    private Instant expiresAt;

    protected RasterFrameEntity() {
        // for JPA
    }

    /** The product. */
    public String getProductId() {
        return productId;
    }

    /** The frame id. */
    public String getFrameId() {
        return frameId;
    }

    /** The valid time. */
    public Instant getValidTime() {
        return validTime;
    }

    /** The forecast run, or null. */
    public Instant getReferenceTime() {
        return referenceTime;
    }

    /** The forecast step in hours, or null. */
    public Integer getForecastStep() {
        return forecastStep;
    }

    /** The relative path of the {@code .fsg} file. */
    public String getPath() {
        return path;
    }

    /** {@code partial} or {@code complete}. */
    public String getStatus() {
        return status;
    }

    /** When retention removes the frame, or null. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** The grid id. */
    public String getGridId() {
        return gridId;
    }

    /** The decoded-content hash. */
    public String getSha256() {
        return sha256;
    }

    /** File bytes. */
    public long getBytes() {
        return bytes;
    }

    /** The stored dtype. */
    public String getDtype() {
        return dtype;
    }

    /** The codec. */
    public int getCodec() {
        return codec;
    }

    /** The statistics. */
    public Map<String, Object> getStats() {
        return stats;
    }

    /** The footprint. */
    public MultiPolygon getFootprint() {
        return footprint;
    }

    /** The upstream key. */
    public String getUpstreamKey() {
        return upstreamKey;
    }

    /** True when kept by ladder thinning. */
    public boolean isThinned() {
        return thinned;
    }

    /** True when the forecast run is complete. */
    public boolean isRunComplete() {
        return runComplete;
    }

    /** When the frame was stored. */
    public Instant getIngestedAt() {
        return ingestedAt;
    }

    /** Start of the observation period, or null. */
    public Instant getObsStart() {
        return obsStart;
    }

    /** End of the observation period, or null. */
    public Instant getObsEnd() {
        return obsEnd;
    }
}
