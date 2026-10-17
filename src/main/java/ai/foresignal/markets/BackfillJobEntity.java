package ai.foresignal.markets;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One history backfill job (prompt 117, table {@code history_backfill_job}, OpenAPI {@code BackfillJob}): a
 * resumable, budget-limited backward read of one source's history down to {@code from}. The source's own runs do
 * the reading; this row tracks the request and the progress read back from the source's adapter state.
 */
@Entity
@Table(name = "history_backfill_job")
public class BackfillJobEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Column(name = "from_date", nullable = false)
    private LocalDate from;

    @Column(name = "cursor_date")
    private LocalDate cursor;

    @Column(name = "pages_fetched", nullable = false)
    private int pagesFetched;

    @Column(name = "versions_written", nullable = false)
    private long versionsWritten;

    @Column(name = "baseline_heads", nullable = false)
    private long baselineHeads;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "started_at")
    private Instant startedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "error", length = 1000)
    private String error;

    /** For JPA. */
    protected BackfillJobEntity() {
        // for JPA
    }

    /**
     * A new queued job.
     *
     * @param id the ULID
     * @param sourceId the source
     * @param from the oldest date to fetch
     * @param createdAt when it was requested
     */
    public BackfillJobEntity(String id, String sourceId, LocalDate from, Instant createdAt) {
        this.id = id;
        this.sourceId = sourceId;
        this.from = from;
        this.createdAt = createdAt;
        this.status = "queued";
    }

    /** The ULID id. */
    public String getId() {
        return id;
    }

    /** The source being backfilled. */
    public String getSourceId() {
        return sourceId;
    }

    /** {@code queued}, {@code running}, {@code done}, {@code failed} or {@code cancelled}. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The oldest date to fetch. */
    public LocalDate getFrom() {
        return from;
    }

    /** The oldest report date fetched so far, or null. */
    public LocalDate getCursor() {
        return cursor;
    }

    /** Sets the oldest report date fetched so far. */
    public void setCursor(LocalDate cursor) {
        this.cursor = cursor;
    }

    /** The backfill pages read. */
    public int getPagesFetched() {
        return pagesFetched;
    }

    /** Sets the backfill pages read. */
    public void setPagesFetched(int pagesFetched) {
        this.pagesFetched = pagesFetched;
    }

    /** The new series points the backfill added (heads above the baseline). */
    public long getVersionsWritten() {
        return versionsWritten;
    }

    /** Sets the new series points the backfill added. */
    public void setVersionsWritten(long versionsWritten) {
        this.versionsWritten = versionsWritten;
    }

    /** The source's {@code series-point} heads when the job started. */
    public long getBaselineHeads() {
        return baselineHeads;
    }

    /** Sets the source's {@code series-point} heads when the job started. */
    public void setBaselineHeads(long baselineHeads) {
        this.baselineHeads = baselineHeads;
    }

    /** The token id that requested it, or null. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets the token id that requested it. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** When it was requested. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it started, or null. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets when it started. */
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    /** When it finished, or null. */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /** Sets when it finished. */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    /** Why it failed, or null. */
    public String getError() {
        return error;
    }

    /** Sets why it failed. */
    public void setError(String error) {
        this.error = error == null || error.length() <= 1000 ? error : error.substring(0, 1000);
    }

    /** The OpenAPI {@code BackfillJob} view. */
    public Map<String, Object> view() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        out.put("sourceId", sourceId);
        out.put("status", status);
        out.put("from", from.toString());
        out.put("cursor", cursor == null ? null : cursor.toString());
        out.put("pagesFetched", pagesFetched);
        out.put("versionsWritten", versionsWritten);
        out.put("createdAt", createdAt.toString());
        out.put("startedAt", startedAt == null ? null : startedAt.toString());
        out.put("finishedAt", finishedAt == null ? null : finishedAt.toString());
        out.put("error", error);
        return out;
    }
}
