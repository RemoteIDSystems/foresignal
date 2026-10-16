package ai.foresignal.intel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An operator case file (prompt 101). Prompt 130 extends it with owners, members, TLP labels and
 * links, so those columns stay open; this prompt keeps identity, status, summary and tags.
 */
@Entity
@Table(name = "investigation",
        uniqueConstraints = @UniqueConstraint(name = "investigation_display_id_uq", columnNames = "display_id"))
public class InvestigationEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    /** Human-readable id, {@code INV-{yyyy}-{nnnn}}, sequential per year. */
    @Column(name = "display_id", length = 32, nullable = false)
    private String displayId;

    @Column(name = "title", length = 512, nullable = false)
    private String title;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "open";

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", nullable = false, columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> tags = new ArrayList<>();

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA. */
    protected InvestigationEntity() {}

    /** Creates an investigation. */
    public InvestigationEntity(String id, String displayId, String title, Instant createdAt) {
        this.id = id;
        this.displayId = displayId;
        this.title = title;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getDisplayId() {
        return displayId;
    }

    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
