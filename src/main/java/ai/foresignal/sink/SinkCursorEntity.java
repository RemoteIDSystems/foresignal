package ai.foresignal.sink;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A sink's durable cursor and delivery counters (prompt 110, table {@code sink_cursor}). */
@Entity
@Table(name = "sink_cursor")
public class SinkCursorEntity {

    /** Running normally. */
    public static final String RUNNING = "RUNNING";

    /** Re-snapshotting after a gap or the purge horizon. */
    public static final String RESYNCING = "RESYNCING";

    /** Failing with backoff. */
    public static final String FAILING = "FAILING";

    /** Disabled or stopped. */
    public static final String STOPPED = "STOPPED";

    @Id
    @Column(name = "sink_id", length = 64, nullable = false)
    private String sinkId;

    @Column(name = "last_seq", nullable = false)
    private long lastSeq;

    @Column(name = "watermark_seq", nullable = false)
    private long watermarkSeq;

    @Column(name = "last_delivered_at")
    private Instant lastDeliveredAt;

    @Column(name = "state", length = 16, nullable = false)
    private String state = STOPPED;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "failing_since")
    private Instant failingSince;

    @Column(name = "resync_count", nullable = false)
    private long resyncCount;

    @Column(name = "delivered", nullable = false)
    private long delivered;

    @Column(name = "dropped", nullable = false)
    private long dropped;

    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'{}'::jsonb")
    @Column(name = "denied", columnDefinition = "jsonb")
    private Map<String, Object> denied = new LinkedHashMap<>();

    /** JPA. */
    protected SinkCursorEntity() {}

    /** Creates a cursor. */
    public SinkCursorEntity(String sinkId) {
        this.sinkId = sinkId;
    }

    public String getSinkId() {
        return sinkId;
    }

    public long getLastSeq() {
        return lastSeq;
    }

    public void setLastSeq(long lastSeq) {
        this.lastSeq = lastSeq;
    }

    public long getWatermarkSeq() {
        return watermarkSeq;
    }

    public void setWatermarkSeq(long watermarkSeq) {
        this.watermarkSeq = watermarkSeq;
    }

    public Instant getLastDeliveredAt() {
        return lastDeliveredAt;
    }

    public void setLastDeliveredAt(Instant lastDeliveredAt) {
        this.lastDeliveredAt = lastDeliveredAt;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Instant getFailingSince() {
        return failingSince;
    }

    public void setFailingSince(Instant failingSince) {
        this.failingSince = failingSince;
    }

    public long getResyncCount() {
        return resyncCount;
    }

    public void setResyncCount(long resyncCount) {
        this.resyncCount = resyncCount;
    }

    public long getDelivered() {
        return delivered;
    }

    public void setDelivered(long delivered) {
        this.delivered = delivered;
    }

    public long getDropped() {
        return dropped;
    }

    public void setDropped(long dropped) {
        this.dropped = dropped;
    }

    public Map<String, Object> getDenied() {
        return denied;
    }

    public void setDenied(Map<String, Object> denied) {
        this.denied = denied == null ? new LinkedHashMap<>() : denied;
    }
}
