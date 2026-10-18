package ai.foresignal.chronicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * A raster frame a chronicle captured by reference (table {@code chronicle_frame}, prompt 131): the product and the
 * content-addressed frame key (042). While the row exists the frame is pinned ({@code raster.FramePin}), so raster
 * retention never deletes it; dropping the chronicle's storage releases it. Unique per
 * {@code (chronicle_id, product_id, frame_key)}.
 */
@Entity
@Table(name = "chronicle_frame",
        uniqueConstraints = @UniqueConstraint(name = "chronicle_frame_key",
                columnNames = {"chronicle_id", "product_id", "frame_key"}),
        indexes = @Index(name = "chronicle_frame_pin_idx", columnList = "product_id, frame_key"))
public class ChronicleFrameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chronicle_id", length = 26, nullable = false)
    private String chronicleId;

    @Column(name = "layer_id", length = 26, nullable = false)
    private String layerId;

    @Column(name = "product_id", length = 64, nullable = false)
    private String productId;

    @Column(name = "frame_key", length = 128, nullable = false)
    private String frameKey;

    @Column(name = "valid_time")
    private Instant validTime;

    @Column(name = "seq")
    private Long seq;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    /** For JPA. */
    protected ChronicleFrameEntity() {}

    /** The row id. */
    public Long getId() {
        return id;
    }

    /** The chronicle. */
    public String getChronicleId() {
        return chronicleId;
    }

    /** The capturing layer. */
    public String getLayerId() {
        return layerId;
    }

    /** The raster product. */
    public String getProductId() {
        return productId;
    }

    /** The content-addressed frame key. */
    public String getFrameKey() {
        return frameKey;
    }

    /** The frame's valid time, or null. */
    public Instant getValidTime() {
        return validTime;
    }

    /** The {@code raster-frame} record seq, or null. */
    public Long getSeq() {
        return seq;
    }

    /** When it was captured. */
    public Instant getCapturedAt() {
        return capturedAt;
    }
}
