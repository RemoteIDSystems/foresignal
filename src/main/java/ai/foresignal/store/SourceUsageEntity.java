package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Per-minute request accounting for one source.
 *
 * <p>Foresignal is meant to be a good upstream citizen by construction (Executive Summary), so what it
 * spends is measured rather than estimated. {@code refused} counts requests the budget governor
 * declined, which distinguishes "we chose not to ask" from "the upstream failed".
 */
@Entity
@Table(name = "source_usage")
public class SourceUsageEntity {

    @EmbeddedId
    private SourceUsageId id;

    @Column(name = "requests", nullable = false)
    private long requests;

    @Column(name = "bytes", nullable = false)
    private long bytes;

    @Column(name = "errors", nullable = false)
    private long errors;

    @Column(name = "refused", nullable = false)
    private long refused;

    // --- Ingest counters (Core-Data-Model §8) ---
    // Kept on the same per-minute row as the request counters so one query answers "what did this
    // source cost and what did it produce in this minute?".

    @Column(name = "records_seen", nullable = false)
    private long recordsSeen;

    @Column(name = "versions", nullable = false)
    private long versions;

    @Column(name = "live_updates", nullable = false)
    private long liveUpdates;

    @Column(name = "touches", nullable = false)
    private long touches;

    @Column(name = "late", nullable = false)
    private long late;

    @Column(name = "skipped", nullable = false)
    private long skipped;

    @Column(name = "rejected", nullable = false)
    private long rejected;

    @Column(name = "version_bytes", nullable = false)
    private long versionBytes;

    protected SourceUsageEntity() {
        // for JPA
    }

    public SourceUsageEntity(SourceUsageId id) {
        this.id = id;
    }

    public SourceUsageId getId() {
        return id;
    }

    public void setId(SourceUsageId id) {
        this.id = id;
    }

    public long getRequests() {
        return requests;
    }

    public void setRequests(long requests) {
        this.requests = requests;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public long getErrors() {
        return errors;
    }

    public void setErrors(long errors) {
        this.errors = errors;
    }

    public long getRefused() {
        return refused;
    }

    public void setRefused(long refused) {
        this.refused = refused;
    }

    public long getRecordsSeen() {
        return recordsSeen;
    }

    public void setRecordsSeen(long recordsSeen) {
        this.recordsSeen = recordsSeen;
    }

    public long getVersions() {
        return versions;
    }

    public void setVersions(long versions) {
        this.versions = versions;
    }

    public long getLiveUpdates() {
        return liveUpdates;
    }

    public void setLiveUpdates(long liveUpdates) {
        this.liveUpdates = liveUpdates;
    }

    public long getTouches() {
        return touches;
    }

    public void setTouches(long touches) {
        this.touches = touches;
    }

    public long getLate() {
        return late;
    }

    public void setLate(long late) {
        this.late = late;
    }

    public long getSkipped() {
        return skipped;
    }

    public void setSkipped(long skipped) {
        this.skipped = skipped;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public long getVersionBytes() {
        return versionBytes;
    }

    public void setVersionBytes(long versionBytes) {
        this.versionBytes = versionBytes;
    }
}
