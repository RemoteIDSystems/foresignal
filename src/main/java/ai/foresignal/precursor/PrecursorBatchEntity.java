package ai.foresignal.precursor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One mining or re-test run (prompt 120, table {@code precursor_batch}) with its multiple-testing accounting:
 * candidates generated, tested, capped, survivors and validated, the false-discovery level {@code q} and the method
 * ({@code BH} or {@code BY}).
 */
@Entity
@Table(name = "precursor_batch", indexes = {
    @Index(name = "precursor_batch_started_idx", columnList = "started_at")
})
public class PrecursorBatchEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "job_id", length = 26)
    private String jobId;

    @Column(name = "mode", length = 8, nullable = false)
    private String mode;

    @Column(name = "trigger", length = 16, nullable = false)
    private String trigger;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "running";

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "candidates_generated", nullable = false)
    private int candidatesGenerated;

    @Column(name = "tested", nullable = false)
    private int tested;

    @Column(name = "capped", nullable = false)
    private int capped;

    @Column(name = "insufficient", nullable = false)
    private int insufficient;

    @Column(name = "survivors", nullable = false)
    private int survivors;

    @Column(name = "validated", nullable = false)
    private int validated;

    @Column(name = "q", nullable = false)
    private double q;

    @Column(name = "method", length = 4, nullable = false)
    private String method = "BH";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "windows", columnDefinition = "jsonb")
    private Map<String, Object> windows = new LinkedHashMap<>();

    @Column(name = "error", columnDefinition = "text")
    private String error;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    /** JPA. */
    protected PrecursorBatchEntity() {}

    /** Creates a row. */
    public PrecursorBatchEntity(String id, String mode, String trigger, Instant startedAt) {
        this.id = id;
        this.mode = mode;
        this.trigger = trigger;
        this.startedAt = startedAt;
    }

    /** The id (ULID). */
    public String getId() {
        return id;
    }

    /** The 103 job, or null. */
    public String getJobId() {
        return jobId;
    }

    /** Sets: the 103 job, or null. */
    public void setJobId(String value) {
        this.jobId = value;
    }

    /** {@code mine} or {@code retest}. */
    public String getMode() {
        return mode;
    }

    /** Sets: {@code mine} or {@code retest}. */
    public void setMode(String value) {
        this.mode = value;
    }

    /** {@code schedule}, {@code manual} or {@code cycle}. */
    public String getTrigger() {
        return trigger;
    }

    /** Sets: {@code schedule}, {@code manual} or {@code cycle}. */
    public void setTrigger(String value) {
        this.trigger = value;
    }

    /** {@code running}, {@code done}, {@code failed} or {@code cancelled}. */
    public String getStatus() {
        return status;
    }

    /** Sets: {@code running}, {@code done}, {@code failed} or {@code cancelled}. */
    public void setStatus(String value) {
        this.status = value;
    }

    /** When it started. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets: when it started. */
    public void setStartedAt(Instant value) {
        this.startedAt = value;
    }

    /** When it finished, or null. */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /** Sets: when it finished, or null. */
    public void setFinishedAt(Instant value) {
        this.finishedAt = value;
    }

    /** Candidates generated before the cap. */
    public int getCandidatesGenerated() {
        return candidatesGenerated;
    }

    /** Sets: candidates generated before the cap. */
    public void setCandidatesGenerated(int value) {
        this.candidatesGenerated = value;
    }

    /** Tests that produced a p-value (the FDR family size). */
    public int getTested() {
        return tested;
    }

    /** Sets: tests that produced a p-value (the FDR family size). */
    public void setTested(int value) {
        this.tested = value;
    }

    /** Candidates cut by {@code analysis.precursors.maxTestsPerRun}. */
    public int getCapped() {
        return capped;
    }

    /** Sets: candidates cut by {@code analysis.precursors.maxTestsPerRun}. */
    public void setCapped(int value) {
        this.capped = value;
    }

    /** Candidates skipped as insufficient (short range, coverage gaps, few occurrences). */
    public int getInsufficient() {
        return insufficient;
    }

    /** Sets: candidates skipped as insufficient (short range, coverage gaps, few occurrences). */
    public void setInsufficient(int value) {
        this.insufficient = value;
    }

    /** Tests that survived false-discovery control. */
    public int getSurvivors() {
        return survivors;
    }

    /** Sets: tests that survived false-discovery control. */
    public void setSurvivors(int value) {
        this.survivors = value;
    }

    /** Survivors (or re-tested patterns) validated. */
    public int getValidated() {
        return validated;
    }

    /** Sets: survivors (or re-tested patterns) validated. */
    public void setValidated(int value) {
        this.validated = value;
    }

    /** The false-discovery level. */
    public double getQ() {
        return q;
    }

    /** Sets: the false-discovery level. */
    public void setQ(double value) {
        this.q = value;
    }

    /** {@code BH} or {@code BY}. */
    public String getMethod() {
        return method;
    }

    /** Sets: {@code BH} or {@code BY}. */
    public void setMethod(String value) {
        this.method = value;
    }

    /** The global range and split. */
    public Map<String, Object> getWindows() {
        return windows;
    }

    /** Sets: the global range and split. */
    public void setWindows(Map<String, Object> value) {
        this.windows = value;
    }

    /** The error of a failed batch, or null. */
    public String getError() {
        return error;
    }

    /** Sets: the error of a failed batch, or null. */
    public void setError(String value) {
        this.error = value;
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets: the 116 manifest, or null. */
    public void setManifestId(String value) {
        this.manifestId = value;
    }


}
