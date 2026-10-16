package ai.foresignal.export;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A legal-style hold (prompt 111, table {@code hold}): a named reason to keep the records of some
 * sources and kinds, within a time or seq range, through retention. {@link HoldCopyStep} copies the
 * rows it covers into {@code record_version_held} before retention removes them.
 *
 * <p>{@code floorsAtCreate} records each covered source's retention floor when the hold was created:
 * versions below it were already gone and can never be held, which the snapshot coverage check uses.
 */
@Entity
@Table(name = "hold")
public class HoldEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "reason", length = 2000, nullable = false)
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sources")
    private List<String> sources;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "kinds")
    private List<String> kinds;

    @Column(name = "from_time")
    private Instant from;

    @Column(name = "to_time")
    private Instant to;

    @Column(name = "from_seq")
    private Long fromSeq;

    @Column(name = "to_seq")
    private Long toSeq;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floors_at_create")
    private Map<String, Long> floorsAtCreate;

    /** JPA. */
    protected HoldEntity() {}

    /** Creates a hold. */
    public HoldEntity(String id, String name, String reason, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** The display name. */
    public String getName() {
        return name;
    }

    /** Why the records are held. */
    public String getReason() {
        return reason;
    }

    /** The held sources; empty means every source. */
    public List<String> getSources() {
        return sources == null ? List.of() : sources;
    }

    /** Sets the held sources. */
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    /** The held kinds; empty means every kind. */
    public List<String> getKinds() {
        return kinds == null ? List.of() : kinds;
    }

    /** Sets the held kinds. */
    public void setKinds(List<String> kinds) {
        this.kinds = kinds;
    }

    /** The lower bound on ingestion time, or null. */
    public Instant getFrom() {
        return from;
    }

    /** Sets the lower time bound. */
    public void setFrom(Instant from) {
        this.from = from;
    }

    /** The upper bound on ingestion time, or null (open). */
    public Instant getTo() {
        return to;
    }

    /** Sets the upper time bound. */
    public void setTo(Instant to) {
        this.to = to;
    }

    /** The inclusive lower seq bound given at creation, or null. */
    public Long getFromSeq() {
        return fromSeq;
    }

    /** Sets the lower seq bound. */
    public void setFromSeq(Long fromSeq) {
        this.fromSeq = fromSeq;
    }

    /** The inclusive upper seq bound given at creation, or null (open). */
    public Long getToSeq() {
        return toSeq;
    }

    /** Sets the upper seq bound. */
    public void setToSeq(Long toSeq) {
        this.toSeq = toSeq;
    }

    /** When the hold lapses, or null (never). */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** Sets the expiry. */
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /** When the hold was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** The token id that created the hold. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets the creating token id. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** Each covered source's retention floor at creation. */
    public Map<String, Long> getFloorsAtCreate() {
        return floorsAtCreate == null ? Map.of() : floorsAtCreate;
    }

    /** Sets the floors at creation. */
    public void setFloorsAtCreate(Map<String, Long> floorsAtCreate) {
        this.floorsAtCreate = floorsAtCreate;
    }

    /** True when the hold has expired at {@code now}. */
    public boolean expired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    /** True when the hold covers records of {@code sourceId}. */
    public boolean coversSource(String sourceId) {
        return getSources().isEmpty() || getSources().contains(sourceId);
    }
}
