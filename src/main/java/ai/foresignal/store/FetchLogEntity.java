package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One fetch, stream flush or push, and what the writer did with it (Core-Data-Model §8).
 *
 * <p>{@code record_version.batch_id} refers to {@link #getId()} with <strong>no foreign
 * key</strong>: versions live in partitions that retention drops wholesale, and fetch-log rows are
 * pruned on their own schedule, so neither side may block the other. Counters default to 0.
 */
@Entity
@Table(
        name = "fetch_log",
        indexes = @Index(name = "fetch_log_source_started_idx", columnList = "source_id, started_at DESC"))
public class FetchLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** The source fetched. */
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    /** What started the fetch. */
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger", length = 16, nullable = false)
    private FetchTrigger trigger;

    /** How the fetch ended. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private FetchStatus status;

    /** When the fetch started. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    /** When the response was received, or null. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "received_at")
    private Instant receivedAt;

    /** The writer's batch timestamp; null unless the writer inserted this row. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "ingested_at")
    private Instant ingestedAt;

    /** The last HTTP status, or null. */
    @Column(name = "http_status")
    private Integer httpStatus;

    /** Upstream requests made by the fetch. */
    @Column(name = "request_count", nullable = false)
    private int requestCount;

    /** The request URL with credentials masked, or null. */
    @Column(name = "url_redacted", columnDefinition = "text")
    private String urlRedacted;

    /** The response ETag, or null. */
    @Column(name = "etag", length = 512)
    private String etag;

    /** Response bytes, or null. */
    @Column(name = "bytes")
    private Long bytes;

    /** Hex SHA-256 of the response body, or null. */
    @Column(name = "body_sha256", length = 64)
    private String bodySha256;

    /** The adapter version that parsed the response, or null. */
    @Column(name = "adapter_version", length = 64)
    private String adapterVersion;

    /**
     * The data license in force for the source, or null. The column keeps the persisted spelling
     * {@code licence} from Core-Data-Model §8.
     */
    @Column(name = "licence", length = 128)
    private String licence;

    /** Who started a manual run or push, or null. */
    @Column(name = "principal", length = 128)
    private String principal;

    /** Records the adapter returned. */
    @Column(name = "records_seen", nullable = false)
    private int recordsSeen;

    /** Records the normalizer rejected. */
    @Column(name = "rejected", nullable = false)
    private int rejected;

    /** Records dropped as LATE. */
    @Column(name = "late", nullable = false)
    private int late;

    /** Records the policy engine skipped. */
    @Column(name = "skipped", nullable = false)
    private int skipped;

    /** Versions inserted. */
    @Column(name = "versions_written", nullable = false)
    private int versionsWritten;

    /** Live rows updated without a version. */
    @Column(name = "live_updates", nullable = false)
    private int liveUpdates;

    /** Touch-only live updates. */
    @Column(name = "touches", nullable = false)
    private int touches;

    /** The first seq the batch wrote, or null. */
    @Column(name = "from_seq")
    private Long fromSeq;

    /** The last seq the batch wrote, or null. */
    @Column(name = "to_seq")
    private Long toSeq;

    /** The coverage the adapter reported, or null. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coverage")
    private CoverageSample coverage;

    /** The redacted error message of a failed fetch, or null. */
    @Column(name = "error", columnDefinition = "text")
    private String error;
    protected FetchLogEntity() {
        // for JPA
    }

    /** Starts a log entry for a fetch of {@code sourceId}. */
    public FetchLogEntity(String sourceId, FetchTrigger trigger, FetchStatus status, Instant startedAt) {
        this.sourceId = sourceId;
        this.trigger = trigger;
        this.status = status;
        this.startedAt = startedAt;
    }

    /** The identity id; also the {@code batch_id} of the versions it wrote. */
    public Long getId() {
        return id;
    }

    /** Returns the source fetched. */
    public String getSourceId() {
        return sourceId;
    }

    /** Sets the source fetched. */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /** Returns what started the fetch. */
    public FetchTrigger getTrigger() {
        return trigger;
    }

    /** Sets what started the fetch. */
    public void setTrigger(FetchTrigger trigger) {
        this.trigger = trigger;
    }

    /** Returns how the fetch ended. */
    public FetchStatus getStatus() {
        return status;
    }

    /** Sets how the fetch ended. */
    public void setStatus(FetchStatus status) {
        this.status = status;
    }

    /** Returns when the fetch started. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets when the fetch started. */
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    /** Returns when the response was received, or null. */
    public Instant getReceivedAt() {
        return receivedAt;
    }

    /** Sets when the response was received, or null. */
    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    /** Returns the writer's batch timestamp; null unless the writer inserted this row. */
    public Instant getIngestedAt() {
        return ingestedAt;
    }

    /** Sets the writer's batch timestamp; null unless the writer inserted this row. */
    public void setIngestedAt(Instant ingestedAt) {
        this.ingestedAt = ingestedAt;
    }

    /** Returns the last HTTP status, or null. */
    public Integer getHttpStatus() {
        return httpStatus;
    }

    /** Sets the last HTTP status, or null. */
    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    /** Returns upstream requests made by the fetch. */
    public int getRequestCount() {
        return requestCount;
    }

    /** Sets upstream requests made by the fetch. */
    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    /** Returns the request URL with credentials masked, or null. */
    public String getUrlRedacted() {
        return urlRedacted;
    }

    /** Sets the request URL with credentials masked, or null. */
    public void setUrlRedacted(String urlRedacted) {
        this.urlRedacted = urlRedacted;
    }

    /** Returns the response ETag, or null. */
    public String getEtag() {
        return etag;
    }

    /** Sets the response ETag, or null. */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /** Returns response bytes, or null. */
    public Long getBytes() {
        return bytes;
    }

    /** Sets response bytes, or null. */
    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    /** Returns hex SHA-256 of the response body, or null. */
    public String getBodySha256() {
        return bodySha256;
    }

    /** Sets hex SHA-256 of the response body, or null. */
    public void setBodySha256(String bodySha256) {
        this.bodySha256 = bodySha256;
    }

    /** Returns the adapter version that parsed the response, or null. */
    public String getAdapterVersion() {
        return adapterVersion;
    }

    /** Sets the adapter version that parsed the response, or null. */
    public void setAdapterVersion(String adapterVersion) {
        this.adapterVersion = adapterVersion;
    }

    /** Returns the data license in force for the source, or null. */
    public String getLicence() {
        return licence;
    }

    /** Sets the data license in force for the source, or null. */
    public void setLicence(String licence) {
        this.licence = licence;
    }

    /** Returns who started a manual run or push, or null. */
    public String getPrincipal() {
        return principal;
    }

    /** Sets who started a manual run or push, or null. */
    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    /** Returns records the adapter returned. */
    public int getRecordsSeen() {
        return recordsSeen;
    }

    /** Sets records the adapter returned. */
    public void setRecordsSeen(int recordsSeen) {
        this.recordsSeen = recordsSeen;
    }

    /** Returns records the normalizer rejected. */
    public int getRejected() {
        return rejected;
    }

    /** Sets records the normalizer rejected. */
    public void setRejected(int rejected) {
        this.rejected = rejected;
    }

    /** Returns records dropped as LATE. */
    public int getLate() {
        return late;
    }

    /** Sets records dropped as LATE. */
    public void setLate(int late) {
        this.late = late;
    }

    /** Returns records the policy engine skipped. */
    public int getSkipped() {
        return skipped;
    }

    /** Sets records the policy engine skipped. */
    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    /** Returns versions inserted. */
    public int getVersionsWritten() {
        return versionsWritten;
    }

    /** Sets versions inserted. */
    public void setVersionsWritten(int versionsWritten) {
        this.versionsWritten = versionsWritten;
    }

    /** Returns live rows updated without a version. */
    public int getLiveUpdates() {
        return liveUpdates;
    }

    /** Sets live rows updated without a version. */
    public void setLiveUpdates(int liveUpdates) {
        this.liveUpdates = liveUpdates;
    }

    /** Returns touch-only live updates. */
    public int getTouches() {
        return touches;
    }

    /** Sets touch-only live updates. */
    public void setTouches(int touches) {
        this.touches = touches;
    }

    /** Returns the first seq the batch wrote, or null. */
    public Long getFromSeq() {
        return fromSeq;
    }

    /** Sets the first seq the batch wrote, or null. */
    public void setFromSeq(Long fromSeq) {
        this.fromSeq = fromSeq;
    }

    /** Returns the last seq the batch wrote, or null. */
    public Long getToSeq() {
        return toSeq;
    }

    /** Sets the last seq the batch wrote, or null. */
    public void setToSeq(Long toSeq) {
        this.toSeq = toSeq;
    }

    /** Returns the coverage the adapter reported, or null. */
    public CoverageSample getCoverage() {
        return coverage;
    }

    /** Sets the coverage the adapter reported, or null. */
    public void setCoverage(CoverageSample coverage) {
        this.coverage = coverage;
    }

    /** Returns the redacted error message of a failed fetch, or null. */
    public String getError() {
        return error;
    }

    /** Sets the redacted error message of a failed fetch, or null. */
    public void setError(String error) {
        this.error = error;
    }
}
