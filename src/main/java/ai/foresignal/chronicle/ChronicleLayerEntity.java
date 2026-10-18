package ai.foresignal.chronicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One layer of a chronicle (table {@code chronicle_layer}, prompt 131): a selector ({@link ChronicleSelector}), an
 * optional area override ({@link ChronicleArea}) and a capture mode ({@link CaptureMode}). An edit applies from
 * {@code added_at_seq} onward; a removed layer keeps {@code removed_at} and its captured rows unless purged. The
 * capture counters ({@code rows_captured}, {@code bytes_captured}, {@code frame_counter}) are moved by the capture
 * loop with native SQL, so updates write only changed columns.
 */
@Entity
@DynamicUpdate
@Table(name = "chronicle_layer", indexes = @Index(name = "chronicle_layer_chronicle_idx", columnList = "chronicle_id"))
public class ChronicleLayerEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "chronicle_id", length = 26, nullable = false)
    private String chronicleId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selector", nullable = false)
    private Map<String, Object> selector = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "area_override")
    private Map<String, Object> areaOverride;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "capture", nullable = false)
    private Map<String, Object> capture = new LinkedHashMap<>();

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "added_at_seq")
    private Long addedAtSeq;

    @Column(name = "removed_at")
    private Instant removedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "estimate")
    private Map<String, Object> estimate;

    @Column(name = "rows_captured", nullable = false)
    private long rowsCaptured;

    @Column(name = "bytes_captured", nullable = false)
    private long bytesCaptured;

    @Column(name = "frame_counter", nullable = false)
    private long frameCounter;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ChronicleLayerEntity() {}

    /**
     * A new layer.
     *
     * @param id its ULID
     * @param chronicleId its chronicle
     * @param name its name
     * @param now the creation time
     */
    public ChronicleLayerEntity(String id, String chronicleId, String name, Instant now) {
        this.id = id;
        this.chronicleId = chronicleId;
        this.name = name;
        this.addedAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The chronicle. */
    public String getChronicleId() {
        return chronicleId;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String value) {
        this.name = value;
    }

    /** The stored selector. */
    public Map<String, Object> getSelector() {
        return selector;
    }

    /** Sets the selector. */
    public void setSelector(Map<String, Object> value) {
        this.selector = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The stored area override, or null. */
    public Map<String, Object> getAreaOverride() {
        return areaOverride;
    }

    /** Sets the area override. */
    public void setAreaOverride(Map<String, Object> value) {
        this.areaOverride = value == null ? null : new LinkedHashMap<>(value);
    }

    /** The stored capture mode. */
    public Map<String, Object> getCapture() {
        return capture;
    }

    /** Sets the capture mode. */
    public void setCapture(Map<String, Object> value) {
        this.capture = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** True while it captures. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Enables or disables it. */
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    /** When it was added. */
    public Instant getAddedAt() {
        return addedAt;
    }

    /** The seq from which its current definition applies, or null before activation. */
    public Long getAddedAtSeq() {
        return addedAtSeq;
    }

    /** Sets the seq from which its definition applies. */
    public void setAddedAtSeq(Long value) {
        this.addedAtSeq = value;
    }

    /** When it was removed, or null. */
    public Instant getRemovedAt() {
        return removedAt;
    }

    /** Marks it removed. */
    public void setRemovedAt(Instant value) {
        this.removedAt = value;
    }

    /** The projected volume ({@code rowsPerDay}, {@code bytesPerDay}, {@code basis}), or null. */
    public Map<String, Object> getEstimate() {
        return estimate;
    }

    /** Sets the projected volume. */
    public void setEstimate(Map<String, Object> value) {
        this.estimate = value == null ? null : new LinkedHashMap<>(value);
    }

    /** Captured rows. */
    public long getRowsCaptured() {
        return rowsCaptured;
    }

    /** Captured bytes (estimated). */
    public long getBytesCaptured() {
        return bytesCaptured;
    }

    /** Raster frames seen by a {@code frames} layer (every Nth is captured). */
    public long getFrameCounter() {
        return frameCounter;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when it last changed. */
    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }

    /** True while it is part of the chronicle (not removed). */
    public boolean live() {
        return removedAt == null;
    }
}
