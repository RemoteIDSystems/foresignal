package ai.foresignal.export;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A legal hold: a named reason to keep records of some sources through retention (prompt 111). */
@Entity
@Table(name = "hold")
public class HoldEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "reason", length = 500, nullable = false)
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

    /** JPA. */
    protected HoldEntity() {}

    /** Creates a hold. */
    public HoldEntity(String id, String name, String reason, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getReason() {
        return reason;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public List<String> getKinds() {
        return kinds;
    }

    public void setKinds(List<String> kinds) {
        this.kinds = kinds;
    }

    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }

    public Long getFromSeq() {
        return fromSeq;
    }

    public void setFromSeq(Long fromSeq) {
        this.fromSeq = fromSeq;
    }

    public Long getToSeq() {
        return toSeq;
    }

    public void setToSeq(Long toSeq) {
        this.toSeq = toSeq;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** True when the hold has expired. */
    public boolean expired(Instant now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }
}
