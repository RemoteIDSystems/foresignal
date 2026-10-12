package ai.foresignal.briefing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One run of a briefing ({@code briefing_run}, Architecture §7.1 and §22, prompt 029).
 *
 * <p>A run is stored {@code running} when it starts and completed in place as {@code succeeded},
 * {@code fallback} (LLM mode rendered the digest after a model failure or refusal) or {@code failed}.
 * It keeps the briefing's name and timezone at the time, so it stays readable after the briefing is
 * changed or deleted (runs are kept until the {@code briefings} retention removes them).
 */
@Entity
@Table(name = "briefing_run", indexes = {
        @Index(name = "briefing_run_briefing_idx", columnList = "briefing_id, started_at"),
        @Index(name = "briefing_run_started_idx", columnList = "started_at")})
public class BriefingRunEntity {

    /** {@code status} while running. */
    public static final String RUNNING = "running";

    /** {@code status} of a completed run. */
    public static final String SUCCEEDED = "succeeded";

    /** {@code status} of an LLM run that fell back to the digest. */
    public static final String FALLBACK = "fallback";

    /** {@code status} of a run that produced nothing. */
    public static final String FAILED = "failed";

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "briefing_id", nullable = false)
    private UUID briefingId;

    @Column(name = "briefing_name", nullable = false, length = 200)
    private String briefingName;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "mode", nullable = false, length = 16)
    private String mode;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "period_from", nullable = false)
    private Instant periodFrom;

    @Column(name = "period_to", nullable = false)
    private Instant periodTo;

    @Column(name = "content_markdown", nullable = false, columnDefinition = "text")
    private String contentMarkdown = "";

    /** {@code AskCitation} objects. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "citations")
    private List<Map<String, Object>> citations = new ArrayList<>();

    /** {@code DigestStats}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats")
    private Map<String, Object> stats;

    @Column(name = "error", columnDefinition = "text")
    private String error;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "usage")
    private Map<String, Object> usage;

    @Column(name = "deliveries", nullable = false)
    private int deliveries;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "warnings")
    private List<String> warnings = new ArrayList<>();

    /** For JPA. */
    protected BriefingRunEntity() {}

    /** A new run. */
    public BriefingRunEntity(UUID id, UUID briefingId, String briefingName, String mode, String timezone,
            Instant startedAt, Instant periodFrom, Instant periodTo) {
        this.id = id;
        this.briefingId = briefingId;
        this.briefingName = briefingName;
        this.mode = mode;
        this.timezone = timezone;
        this.startedAt = startedAt;
        this.periodFrom = periodFrom;
        this.periodTo = periodTo;
        this.status = RUNNING;
    }

    /** The id. */
    public UUID getId() {
        return id;
    }

    /** The briefing. */
    public UUID getBriefingId() {
        return briefingId;
    }

    /** The briefing's name when the run started. */
    public String getBriefingName() {
        return briefingName;
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** {@code digest} or {@code llm}. */
    public String getMode() {
        return mode;
    }

    /** The briefing's timezone when the run started. */
    public String getTimezone() {
        return timezone;
    }

    /** When the run started. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** When it finished, or null. */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /** Sets when it finished. */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    /** The period start. */
    public Instant getPeriodFrom() {
        return periodFrom;
    }

    /** The period end. */
    public Instant getPeriodTo() {
        return periodTo;
    }

    /** The markdown. */
    public String getContentMarkdown() {
        return contentMarkdown;
    }

    /** Sets the markdown. */
    public void setContentMarkdown(String contentMarkdown) {
        this.contentMarkdown = contentMarkdown == null ? "" : contentMarkdown;
    }

    /** The citations. */
    public List<Map<String, Object>> getCitations() {
        return citations;
    }

    /** Sets the citations. */
    public void setCitations(List<Map<String, Object>> citations) {
        this.citations = citations == null ? new ArrayList<>() : citations;
    }

    /** The statistics, or null. */
    public Map<String, Object> getStats() {
        return stats;
    }

    /** Sets the statistics. */
    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }

    /** The error, or null. */
    public String getError() {
        return error;
    }

    /** Sets the error. */
    public void setError(String error) {
        this.error = error;
    }

    /** The LLM usage, or null. */
    public Map<String, Object> getUsage() {
        return usage;
    }

    /** Sets the LLM usage. */
    public void setUsage(Map<String, Object> usage) {
        this.usage = usage;
    }

    /** Deliveries enqueued for the run's notification. */
    public int getDeliveries() {
        return deliveries;
    }

    /** Sets the delivery count. */
    public void setDeliveries(int deliveries) {
        this.deliveries = deliveries;
    }

    /** Citation-verifier warnings and fallback notes. */
    public List<String> getWarnings() {
        return warnings;
    }

    /** Sets the warnings. */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings == null ? new ArrayList<>() : warnings;
    }
}
