package ai.foresignal.doctor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One persisted adapter-doctor report (prompt 140, table {@code doctor_report}), retained 90 days by
 * {@link DoctorReportRetentionStep}. Holds findings, the structure diff (paths, types and capped examples only), the
 * probe summary, the fixture compared against and the LLM output; never a raw upstream body.
 */
@Entity
@Table(name = "doctor_report", indexes = {
        @Index(name = "doctor_report_source_idx", columnList = "source_id, created_at"),
        @Index(name = "doctor_report_created_idx", columnList = "created_at")})
public class DoctorReportEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "trigger", length = 8, nullable = false)
    private String trigger;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "findings", nullable = false)
    private List<Map<String, Object>> findings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "diff")
    private Map<String, Object> diff;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "llm")
    private Map<String, Object> llm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "probe")
    private Map<String, Object> probe;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fixture")
    private Map<String, Object> fixture;

    @Column(name = "adapter_version", length = 64)
    private String adapterVersion;

    /** For JPA. */
    protected DoctorReportEntity() {}

    /** A report row. */
    public DoctorReportEntity(String id, String sourceId, Instant createdAt, String trigger, String status) {
        this.id = id;
        this.sourceId = sourceId;
        this.createdAt = createdAt;
        this.trigger = trigger;
        this.status = status;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** The source. */
    public String getSourceId() {
        return sourceId;
    }

    /** When the run started. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** {@code MANUAL}, {@code AUTO} or {@code MCP}. */
    public String getTrigger() {
        return trigger;
    }

    /** {@code OK}, {@code DEGRADED_INFO} or {@code BROKEN}. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The findings as JSON objects. */
    public List<Map<String, Object>> getFindings() {
        return findings;
    }

    /** Sets the findings. */
    public void setFindings(List<Map<String, Object>> findings) {
        this.findings = findings;
    }

    /** The structure diff, or null. */
    public Map<String, Object> getDiff() {
        return diff;
    }

    /** Sets the diff. */
    public void setDiff(Map<String, Object> diff) {
        this.diff = diff;
    }

    /** The LLM block, or null. */
    public Map<String, Object> getLlm() {
        return llm;
    }

    /** Sets the LLM block. */
    public void setLlm(Map<String, Object> llm) {
        this.llm = llm;
    }

    /** The probe summary, or null. */
    public Map<String, Object> getProbe() {
        return probe;
    }

    /** Sets the probe summary. */
    public void setProbe(Map<String, Object> probe) {
        this.probe = probe;
    }

    /** The fixture compared against, or null. */
    public Map<String, Object> getFixture() {
        return fixture;
    }

    /** Sets the fixture. */
    public void setFixture(Map<String, Object> fixture) {
        this.fixture = fixture;
    }

    /** The adapter (Foresignal) version that produced the report. */
    public String getAdapterVersion() {
        return adapterVersion;
    }

    /** Sets the adapter version. */
    public void setAdapterVersion(String adapterVersion) {
        this.adapterVersion = adapterVersion;
    }
}
