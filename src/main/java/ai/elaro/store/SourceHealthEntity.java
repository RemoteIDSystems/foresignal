package ai.elaro.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * What happened the last time Elaro talked to a source, and how it has been behaving.
 *
 * <p>This is what makes a broken adapter self-diagnosing: an operator, or a future maintenance
 * agent, can tell a dead upstream from a changed response shape without reading logs.
 */
@Entity
@Table(name = "source_health")
public class SourceHealthEntity {

    /** How many recent run counts {@link #recentRecordCounts} keeps (Architecture §7.1). */
    public static final int RECENT_RECORD_COUNTS_LIMIT = 5;

    @Id
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_error_at")
    private Instant lastErrorAt;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "last_http_status")
    private Integer lastHttpStatus;

    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures;

    @Column(name = "last_records_seen")
    private Integer lastRecordsSeen;

    @Column(name = "last_records_changed")
    private Integer lastRecordsChanged;

    /** Record counts from the last few runs, newest last, used to spot a feed going quiet. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recent_record_counts", nullable = false)
    private List<Integer> recentRecordCounts = new ArrayList<>();

    /** When set, the collector must not fetch this source until this instant. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "backoff_until")
    private Instant backoffUntil;

    /** Adapter-owned scratch state, for example an upstream cursor or ETag. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adapter_state", nullable = false)
    private Map<String, String> adapterState = new LinkedHashMap<>();

    protected SourceHealthEntity() {
        // for JPA
    }

    public SourceHealthEntity(String sourceId) {
        this.sourceId = sourceId;
    }

    /** Appends a run's record count, keeping only the most recent {@value #RECENT_RECORD_COUNTS_LIMIT}. */
    public void recordRun(int recordsSeen) {
        recentRecordCounts.add(recordsSeen);
        while (recentRecordCounts.size() > RECENT_RECORD_COUNTS_LIMIT) {
            recentRecordCounts.remove(0);
        }
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(Instant lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public Instant getLastSuccessAt() {
        return lastSuccessAt;
    }

    public void setLastSuccessAt(Instant lastSuccessAt) {
        this.lastSuccessAt = lastSuccessAt;
    }

    public Instant getLastErrorAt() {
        return lastErrorAt;
    }

    public void setLastErrorAt(Instant lastErrorAt) {
        this.lastErrorAt = lastErrorAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Integer getLastHttpStatus() {
        return lastHttpStatus;
    }

    public void setLastHttpStatus(Integer lastHttpStatus) {
        this.lastHttpStatus = lastHttpStatus;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(int consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }

    public Integer getLastRecordsSeen() {
        return lastRecordsSeen;
    }

    public void setLastRecordsSeen(Integer lastRecordsSeen) {
        this.lastRecordsSeen = lastRecordsSeen;
    }

    public Integer getLastRecordsChanged() {
        return lastRecordsChanged;
    }

    public void setLastRecordsChanged(Integer lastRecordsChanged) {
        this.lastRecordsChanged = lastRecordsChanged;
    }

    public List<Integer> getRecentRecordCounts() {
        return recentRecordCounts;
    }

    public void setRecentRecordCounts(List<Integer> recentRecordCounts) {
        this.recentRecordCounts = recentRecordCounts == null ? new ArrayList<>() : recentRecordCounts;
    }

    public Instant getBackoffUntil() {
        return backoffUntil;
    }

    public void setBackoffUntil(Instant backoffUntil) {
        this.backoffUntil = backoffUntil;
    }

    public Map<String, String> getAdapterState() {
        return adapterState;
    }

    public void setAdapterState(Map<String, String> adapterState) {
        this.adapterState = adapterState == null ? new LinkedHashMap<>() : adapterState;
    }
}
