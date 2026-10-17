package ai.foresignal.cycle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One run of an analysis cycle or an on-demand situation assessment (prompt 128, table {@code cycle_run}): the
 * period, the complete deterministic packet, the outcome of every LLM role, the tokens spent, the assessment written
 * and, for a monthly run, the maintenance it triggered.
 */
@Entity
@Table(name = "cycle_run", indexes = {
    @Index(name = "cycle_run_cycle_idx", columnList = "cycle_id, id"),
    @Index(name = "cycle_run_situation_idx", columnList = "situation_id, id")
})
public class CycleRunEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "cycle_id", length = 26)
    private String cycleId;

    @Column(name = "situation_id", length = 26)
    private String situationId;

    @Column(name = "cadence", length = 16, nullable = false)
    private String cadence;

    @Column(name = "run_trigger", length = 16, nullable = false)
    private String trigger;

    @Column(name = "status", length = 16, nullable = false)
    private String status = CycleVocabulary.QUEUED;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Column(name = "previous_run_id", length = 26)
    private String previousRunId;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "packet")
    private Map<String, Object> packet;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roles", nullable = false)
    private List<Map<String, Object>> roles = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "requested_roles")
    private List<String> requestedRoles;

    @Column(name = "requested_effort", length = 8)
    private String requestedEffort;

    @Column(name = "tokens_used", nullable = false)
    private long tokensUsed;

    @Column(name = "assessment_id", length = 26)
    private String assessmentId;

    @Column(name = "maintenance_id", length = 26)
    private String maintenanceId;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "warnings", nullable = false)
    private List<String> warnings = new ArrayList<>();

    @Column(name = "error", columnDefinition = "text")
    private String error;

    /** For JPA. */
    protected CycleRunEntity() {}

    /** Creates a queued run. */
    public CycleRunEntity(String id, String cycleId, String situationId, String cadence, String trigger,
            Instant periodStart, Instant periodEnd, Instant createdAt) {
        this.id = id;
        this.cycleId = cycleId;
        this.situationId = situationId;
        this.cadence = cadence;
        this.trigger = trigger;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.createdAt = createdAt;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The cycle, or null for an on-demand situation assessment. */
    public String getCycleId() {
        return cycleId;
    }

    /** The situation of an on-demand assessment, or null. */
    public String getSituationId() {
        return situationId;
    }

    /** The cadence. */
    public String getCadence() {
        return cadence;
    }

    /** The trigger: schedule, missed, manual or situation. */
    public String getTrigger() {
        return trigger;
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The period start (the previous run's end, or the cadence's lookback). */
    public Instant getPeriodStart() {
        return periodStart;
    }

    /** Sets the period start. */
    public void setPeriodStart(Instant periodStart) {
        this.periodStart = periodStart;
    }

    /** The period end. */
    public Instant getPeriodEnd() {
        return periodEnd;
    }

    /** Sets the period end. */
    public void setPeriodEnd(Instant periodEnd) {
        this.periodEnd = periodEnd;
    }

    /** The previous successful run of the same cycle, or null. */
    public String getPreviousRunId() {
        return previousRunId;
    }

    /** Sets the previous run. */
    public void setPreviousRunId(String previousRunId) {
        this.previousRunId = previousRunId;
    }

    /** When the run started, or null while queued. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets the start. */
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    /** When the run finished, or null. */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /** Sets the finish. */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    /** Creation time. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** The complete packet ({@code CyclePacket}), or null before it is built. */
    public Map<String, Object> getPacket() {
        return packet;
    }

    /** Sets the packet. */
    public void setPacket(Map<String, Object> packet) {
        this.packet = packet;
    }

    /** The per-role outcomes. */
    public List<Map<String, Object>> getRoles() {
        return roles;
    }

    /** Sets the role outcomes. */
    public void setRoles(List<Map<String, Object>> roles) {
        this.roles = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
    }

    /** The roles an on-demand request asked for, or null for the cycle's roles. */
    public List<String> getRequestedRoles() {
        return requestedRoles;
    }

    /** Sets the requested roles. */
    public void setRequestedRoles(List<String> requestedRoles) {
        this.requestedRoles = requestedRoles == null ? null : new ArrayList<>(requestedRoles);
    }

    /** The effort an on-demand request asked for, or null. */
    public String getRequestedEffort() {
        return requestedEffort;
    }

    /** Sets the requested effort. */
    public void setRequestedEffort(String requestedEffort) {
        this.requestedEffort = requestedEffort;
    }

    /** Tokens spent by every role. */
    public long getTokensUsed() {
        return tokensUsed;
    }

    /** Sets the tokens spent. */
    public void setTokensUsed(long tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    /** The assessment written, or null. */
    public String getAssessmentId() {
        return assessmentId;
    }

    /** Sets the assessment. */
    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }

    /** The maintenance run triggered (monthly), or null. */
    public String getMaintenanceId() {
        return maintenanceId;
    }

    /** Sets the maintenance run. */
    public void setMaintenanceId(String maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    /** The run's 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** Warnings. */
    public List<String> getWarnings() {
        return warnings;
    }

    /** Sets the warnings. */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings == null ? new ArrayList<>() : new ArrayList<>(warnings);
    }

    /** The failure reason, or null. */
    public String getError() {
        return error;
    }

    /** Sets the failure reason. */
    public void setError(String error) {
        this.error = error;
    }
}
