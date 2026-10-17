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
 * An operator case file (prompt 101). Prompt 130 adds the statuses {@code active} and {@code archived} and the links
 * to everything an analyst works with: a 129 workspace, 125 hypothesis sets, 122 questions, and the chronicles (131)
 * and reports (132) of later prompts. Its owner, visibility, access list and TLP label live in
 * {@code shareable_object} and {@code object_acl} ({@code ai.foresignal.collab.AccessResolver}).
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

    @Column(name = "workspace_id", length = 26)
    private String workspaceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hypothesis_set_ids")
    private List<String> hypothesisSetIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "question_ids")
    private List<String> questionIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chronicle_ids")
    private List<String> chronicleIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_ids")
    private List<String> reportIds = new ArrayList<>();

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

    /** The linked 129 workspace, or null (prompt 130). */
    public String getWorkspaceId() {
        return workspaceId;
    }

    /** Links a workspace. */
    public void setWorkspaceId(String value) {
        this.workspaceId = value;
    }

    /** The linked 125 hypothesis sets (prompt 130). */
    public List<String> getHypothesisSetIds() {
        return hypothesisSetIds == null ? List.of() : hypothesisSetIds;
    }

    /** Links hypothesis sets. */
    public void setHypothesisSetIds(List<String> value) {
        this.hypothesisSetIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The linked 122 forecast questions (prompt 130). */
    public List<String> getQuestionIds() {
        return questionIds == null ? List.of() : questionIds;
    }

    /** Links questions. */
    public void setQuestionIds(List<String> value) {
        this.questionIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The linked chronicles (prompt 131). */
    public List<String> getChronicleIds() {
        return chronicleIds == null ? List.of() : chronicleIds;
    }

    /** Links chronicles. */
    public void setChronicleIds(List<String> value) {
        this.chronicleIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The linked reports (prompt 132). */
    public List<String> getReportIds() {
        return reportIds == null ? List.of() : reportIds;
    }

    /** Links reports. */
    public void setReportIds(List<String> value) {
        this.reportIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }
}
