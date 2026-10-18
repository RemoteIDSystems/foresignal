package ai.foresignal.analyst;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One analyst note (prompt 102): a markdown body with typed, cited claims. */
@Entity
@Table(name = "analyst_note")
public class AnalystNoteEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "profile_id", length = 26, nullable = false)
    private String profileId;

    @Column(name = "run_id", length = 26, nullable = false)
    private String runId;

    @Column(name = "title", length = 512, nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "text", nullable = false)
    private String body;

    @Column(name = "priority", length = 8, nullable = false)
    private String priority = "normal";

    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'[]'::jsonb")
    @Column(name = "claims", nullable = false, columnDefinition = "jsonb")
    private List<java.util.Map<String, Object>> claims = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'[]'::jsonb")
    @Column(name = "citations", nullable = false, columnDefinition = "jsonb")
    private List<String> citations = new ArrayList<>();

    @Column(name = "private", nullable = false)
    private boolean privateNote;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "new";

    @Column(name = "model", length = 128)
    private String model;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** JPA. */
    protected AnalystNoteEntity() {}

    /** Creates a note. */
    public AnalystNoteEntity(String id, String profileId, String runId, String title, Instant createdAt) {
        this.id = id;
        this.profileId = profileId;
        this.runId = runId;
        this.title = title;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getRunId() {
        return runId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<java.util.Map<String, Object>> getClaims() {
        return claims;
    }

    public void setClaims(List<java.util.Map<String, Object>> claims) {
        this.claims = claims == null ? new ArrayList<>() : claims;
    }

    public List<String> getCitations() {
        return citations;
    }

    public void setCitations(List<String> citations) {
        this.citations = citations == null ? new ArrayList<>() : citations;
    }

    public boolean isPrivateNote() {
        return privateNote;
    }

    public void setPrivateNote(boolean privateNote) {
        this.privateNote = privateNote;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
