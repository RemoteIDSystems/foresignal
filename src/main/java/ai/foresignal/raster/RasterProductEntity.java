package ai.foresignal.raster;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One catalog product (table {@code raster_product}, Weather-Imagery-Design §2.3).
 *
 * <p>The {@link ProductRegistry} upserts a row per product at startup and on every dynamic
 * registration; the in-memory registry stays the source of truth for reads, and the row is its
 * durable mirror (with the product's {@code spec} as JSON).
 */
@Entity
@Table(name = "raster_product")
public class RasterProductEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "category", length = 16)
    private String category;

    @Column(name = "kind", length = 16, nullable = false)
    private String kind;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spec", nullable = false)
    private Map<String, Object> spec = new LinkedHashMap<>();

    @Column(name = "dynamic", nullable = false)
    private boolean dynamic;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RasterProductEntity() {
        // for JPA
    }

    /** A new row for product {@code id}. */
    public RasterProductEntity(String id) {
        this.id = id;
    }

    /** The product id. */
    public String getId() {
        return id;
    }

    /** The owning source. */
    public String getSourceId() {
        return sourceId;
    }

    /** Sets the owning source. */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /** The category. */
    public String getCategory() {
        return category;
    }

    /** Sets the category. */
    public void setCategory(String category) {
        this.category = category;
    }

    /** The product kind. */
    public String getKind() {
        return kind;
    }

    /** Sets the product kind. */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /** The spec as JSON. */
    public Map<String, Object> getSpec() {
        return spec;
    }

    /** Sets the spec. */
    public void setSpec(Map<String, Object> spec) {
        this.spec = spec;
    }

    /** True for a runtime-registered product. */
    public boolean isDynamic() {
        return dynamic;
    }

    /** Sets the dynamic flag. */
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    /** When the row was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets the creation time. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When the row was last written. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the update time. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
