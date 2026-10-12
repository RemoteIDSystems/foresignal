package ai.foresignal.inbound.edp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One snapshot in progress ({@code edp_snapshot}, docs/External-Data-Protocol.md §3.2): which pages
 * arrived, when it started, and whether anything has already disqualified it. The ids it carried are
 * staged in {@code edp_snapshot_stage}.
 */
@Entity
@Table(name = "edp_snapshot")
public class EdpSnapshotEntity {

    /** {@code {sourceId}/{snapshotId}}. */
    @Id
    @Column(name = "snapshot_key", length = 200, nullable = false)
    private String key;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "snapshot_id", length = 128, nullable = false)
    private String snapshotId;

    @Column(name = "stream", length = 48, nullable = false)
    private String stream;

    @Column(name = "scope", length = 256)
    private String scope;

    /** The pages received, as a comma-separated list. */
    @Column(name = "pages", columnDefinition = "text", nullable = false)
    private String pages = "";

    /** The final page's number, or null until it arrives. */
    @Column(name = "final_page")
    private Integer finalPage;

    /** Why the snapshot can no longer remove anything, or null while it is still clean. */
    @Column(name = "failed_reason", length = 500)
    private String failedReason;

    /** True when its deletions are held above {@code maxAbsentRatio} for the operator. */
    @Column(name = "held", nullable = false)
    private boolean held;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "first_page_at", nullable = false)
    private Instant firstPageAt;

    /** For JPA. */
    protected EdpSnapshotEntity() {}

    /** A new snapshot. */
    public EdpSnapshotEntity(String sourceId, String snapshotId, String stream, String scope, Instant firstPageAt) {
        this.key = key(sourceId, snapshotId);
        this.sourceId = sourceId;
        this.snapshotId = snapshotId;
        this.stream = stream;
        this.scope = scope;
        this.firstPageAt = firstPageAt;
    }

    /** The primary key of a snapshot. */
    public static String key(String sourceId, String snapshotId) {
        return sourceId + "/" + snapshotId;
    }

    /** The row key. */
    public String getKey() {
        return key;
    }

    /** The source. */
    public String getSourceId() {
        return sourceId;
    }

    /** The provider's snapshot id. */
    public String getSnapshotId() {
        return snapshotId;
    }

    /** The stream. */
    public String getStream() {
        return stream;
    }

    /** The scope, or null for the whole stream. */
    public String getScope() {
        return scope;
    }

    /** The pages received. */
    public java.util.SortedSet<Integer> pages() {
        java.util.SortedSet<Integer> out = new java.util.TreeSet<>();
        for (String part : pages.split(",")) {
            if (!part.isBlank()) {
                out.add(Integer.parseInt(part.strip()));
            }
        }
        return out;
    }

    /** Records that {@code page} arrived. */
    public void addPage(int page) {
        java.util.SortedSet<Integer> all = pages();
        all.add(page);
        StringBuilder out = new StringBuilder();
        for (Integer p : all) {
            if (out.length() > 0) {
                out.append(',');
            }
            out.append(p);
        }
        pages = out.toString();
    }

    /** The final page's number, or null. */
    public Integer getFinalPage() {
        return finalPage;
    }

    /** Sets the final page's number. */
    public void setFinalPage(Integer finalPage) {
        this.finalPage = finalPage;
    }

    /** Why it is disqualified, or null. */
    public String getFailedReason() {
        return failedReason;
    }

    /** Disqualifies it (the first reason is kept). */
    public void fail(String reason) {
        if (failedReason == null) {
            failedReason = reason == null || reason.length() <= 500 ? reason : reason.substring(0, 500);
        }
    }

    /** True when its deletions are held. */
    public boolean isHeld() {
        return held;
    }

    /** Sets the hold. */
    public void setHeld(boolean held) {
        this.held = held;
    }

    /** When its first page arrived. */
    public Instant getFirstPageAt() {
        return firstPageAt;
    }
}
