package ai.foresignal.analyst;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One analyst proposal (prompt 102): a watch, event, investigation item or event annotation. */
@Entity
@Table(name = "analyst_proposal")
public class AnalystProposalEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "run_id", length = 26, nullable = false)
    private String runId;

    @Column(name = "profile_id", length = 26, nullable = false)
    private String profileId;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private Map<String, Object> payload = new LinkedHashMap<>();

    @Column(name = "rationale", columnDefinition = "text")
    private String rationale;

    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'[]'::jsonb")
    @Column(name = "citations", nullable = false, columnDefinition = "jsonb")
    private List<String> citations = new ArrayList<>();

    @Column(name = "status", length = 16, nullable = false)
    private String status = "pending";

    @Column(name = "auto_executed", nullable = false)
    private boolean autoExecuted;

    /** True when the run that proposed it read private evidence (039); listed only to private-access callers. */
    @Column(name = "private", nullable = false, columnDefinition = "boolean default false")
    private boolean privateProposal;

    @Column(name = "executed_ref", length = 128)
    private String executedRef;

    @Column(name = "decided_by", length = 128)
    private String decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** JPA. */
    protected AnalystProposalEntity() {}

    /** Creates a proposal. */
    public AnalystProposalEntity(String id, String runId, String profileId, String type, Instant createdAt,
            Instant expiresAt) {
        this.id = id;
        this.runId = runId;
        this.profileId = profileId;
        this.type = type;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public String getRunId() {
        return runId;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload == null ? new LinkedHashMap<>() : payload;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public List<String> getCitations() {
        return citations;
    }

    public void setCitations(List<String> citations) {
        this.citations = citations == null ? new ArrayList<>() : citations;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAutoExecuted() {
        return autoExecuted;
    }

    /** True when the proposing run read private evidence. */
    public boolean isPrivateProposal() {
        return privateProposal;
    }

    /** Marks the proposal private (or not). */
    public void setPrivateProposal(boolean privateProposal) {
        this.privateProposal = privateProposal;
    }

    public void setAutoExecuted(boolean autoExecuted) {
        this.autoExecuted = autoExecuted;
    }

    public String getExecutedRef() {
        return executedRef;
    }

    public void setExecutedRef(String executedRef) {
        this.executedRef = executedRef;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(String decidedBy) {
        this.decidedBy = decidedBy;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
