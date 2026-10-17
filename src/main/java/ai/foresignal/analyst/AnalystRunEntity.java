package ai.foresignal.analyst;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One analyst run's evaluation log (prompt 102). */
@Entity
@Table(name = "analyst_run")
public class AnalystRunEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "profile_id", length = 26, nullable = false)
    private String profileId;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "queued";

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cursor_from")
    private Map<String, Object> cursorFrom;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cursor_to")
    private Map<String, Object> cursorTo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_counts")
    private Map<String, Object> inputCounts;

    @Column(name = "provider", length = 64)
    private String provider;

    @Column(name = "model", length = 128)
    private String model;

    @Column(name = "effort", length = 8)
    private String effort;

    @Column(name = "tokens_in", nullable = false)
    private long tokensIn;

    @Column(name = "tokens_out", nullable = false)
    private long tokensOut;

    @Column(name = "cached_tokens", nullable = false)
    private long cachedTokens;

    @Column(name = "tool_calls", nullable = false)
    private int toolCalls;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "notes", nullable = false)
    private int notes;

    @Column(name = "proposals", nullable = false)
    private int proposals;

    @Column(name = "auto_executed", nullable = false)
    private int autoExecuted;

    @Column(name = "citations_dropped", nullable = false)
    private int citationsDropped;

    @Column(name = "claims_downgraded", nullable = false)
    private int claimsDowngraded;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "warnings", nullable = false, columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> warnings = new ArrayList<>();

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @Column(name = "prompt_hash", length = 64)
    private String promptHash;

    /** The analysis manifest of this output (prompt 116), or null for outputs written before it. */
    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "detector_version", length = 32, nullable = false)
    private String detectorVersion = "analyst-1.0.0";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content")
    private Map<String, Object> content;

    /** JPA. */
    protected AnalystRunEntity() {}

    /** Creates a run. */
    public AnalystRunEntity(String id, String profileId, Instant startedAt) {
        this.id = id;
        this.profileId = profileId;
        this.startedAt = startedAt;
    }

    public String getId() {
        return id;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Map<String, Object> getCursorFrom() {
        return cursorFrom;
    }

    public void setCursorFrom(Map<String, Object> cursorFrom) {
        this.cursorFrom = cursorFrom;
    }

    public Map<String, Object> getCursorTo() {
        return cursorTo;
    }

    public void setCursorTo(Map<String, Object> cursorTo) {
        this.cursorTo = cursorTo;
    }

    public Map<String, Object> getInputCounts() {
        return inputCounts;
    }

    public void setInputCounts(Map<String, Object> inputCounts) {
        this.inputCounts = inputCounts;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getEffort() {
        return effort;
    }

    public void setEffort(String effort) {
        this.effort = effort;
    }

    public long getTokensIn() {
        return tokensIn;
    }

    public void setTokensIn(long tokensIn) {
        this.tokensIn = tokensIn;
    }

    public long getTokensOut() {
        return tokensOut;
    }

    public void setTokensOut(long tokensOut) {
        this.tokensOut = tokensOut;
    }

    public long getCachedTokens() {
        return cachedTokens;
    }

    public void setCachedTokens(long cachedTokens) {
        this.cachedTokens = cachedTokens;
    }

    public int getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(int toolCalls) {
        this.toolCalls = toolCalls;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public int getNotes() {
        return notes;
    }

    public void setNotes(int notes) {
        this.notes = notes;
    }

    public int getProposals() {
        return proposals;
    }

    public void setProposals(int proposals) {
        this.proposals = proposals;
    }

    public int getAutoExecuted() {
        return autoExecuted;
    }

    public void setAutoExecuted(int autoExecuted) {
        this.autoExecuted = autoExecuted;
    }

    public int getCitationsDropped() {
        return citationsDropped;
    }

    public void setCitationsDropped(int citationsDropped) {
        this.citationsDropped = citationsDropped;
    }

    public int getClaimsDowngraded() {
        return claimsDowngraded;
    }

    public void setClaimsDowngraded(int claimsDowngraded) {
        this.claimsDowngraded = claimsDowngraded;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings == null ? new ArrayList<>() : warnings;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPromptHash() {
        return promptHash;
    }

    public void setPromptHash(String promptHash) {
        this.promptHash = promptHash;
    }

    public String getDetectorVersion() {
        return detectorVersion;
    }

    public void setDetectorVersion(String detectorVersion) {
        this.detectorVersion = detectorVersion;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    /** The analysis manifest of this output (prompt 116), or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the analysis manifest (prompt 116). */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }
}
