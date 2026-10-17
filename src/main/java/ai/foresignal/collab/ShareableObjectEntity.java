package ai.foresignal.collab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * The ownership record of one work product (table {@code shareable_object}, prompt 130): owner, visibility and TLP
 * label, keyed by the canonical object reference. Every shareable type registers here, so {@link AccessResolver}
 * reads one table whatever the type; the type's own table keeps its domain data. Objects created before prompt 130
 * are registered lazily from their {@link ShareableSource} defaults.
 */
@Entity
@Table(name = "shareable_object", indexes = {
        @Index(name = "shareable_object_owner_idx", columnList = "owner, updated_at"),
        @Index(name = "shareable_object_visibility_idx", columnList = "visibility, updated_at")})
public class ShareableObjectEntity {

    @Id
    @Column(name = "object_ref", length = 128, nullable = false)
    private String objectRef;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "owner", length = 64, nullable = false)
    private String owner;

    @Column(name = "visibility", length = 16, nullable = false)
    private String visibility;

    @Column(name = "tlp", length = 16, nullable = false)
    private String tlp;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ShareableObjectEntity() {}

    /**
     * A new registration.
     *
     * @param objectRef the canonical reference
     * @param type the shareable type
     * @param owner the owner's principal id
     * @param visibility the visibility
     * @param tlp the TLP label (wire value)
     * @param now the creation time
     */
    public ShareableObjectEntity(String objectRef, String type, String owner, Visibility visibility, String tlp,
            Instant now) {
        this.objectRef = objectRef;
        this.type = type;
        this.owner = owner;
        this.visibility = visibility.wireValue();
        this.tlp = tlp;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The canonical reference. */
    public String getObjectRef() {
        return objectRef;
    }

    /** The shareable type. */
    public String getType() {
        return type;
    }

    /** The owner's principal id. */
    public String getOwner() {
        return owner;
    }

    /** Transfers ownership. */
    public void setOwner(String value) {
        this.owner = value;
    }

    /** The visibility. */
    public Visibility getVisibility() {
        return Visibility.fromWire(visibility);
    }

    /** Sets the visibility. */
    public void setVisibility(Visibility value) {
        this.visibility = value.wireValue();
    }

    /** The TLP label (wire value). */
    public String getTlp() {
        return tlp;
    }

    /** Sets the TLP label. */
    public void setTlp(String value) {
        this.tlp = value;
    }

    /** When it was registered. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the change time. */
    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }
}
