package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One push of one notification delivery to one target (prompt 031).
 *
 * <p>The built-in Push channel's {@code notification_delivery} row fans out into one row per
 * matching target, so each target keeps its own retry state and a retry never re-sends to targets
 * that already succeeded. {@code status} is {@code pending}, {@code sent}, {@code gone} or
 * {@code failed}; {@code last_error} is at most 500 characters with the endpoint redacted;
 * {@code content} is the transport-neutral {@code PushMessage} taken at fan-out time, and
 * {@code downgraded} records a quiet-hours downgrade ({@code Urgency: very-low}, silent).
 */
@Entity
@Table(
        name = "push_dispatch",
        indexes = {
            @Index(name = "push_dispatch_due_idx", columnList = "status, next_attempt_at"),
            @Index(name = "push_dispatch_delivery_idx", columnList = "delivery_id"),
            @Index(name = "push_dispatch_target_idx", columnList = "target_type, target_id")
        })
public class PushDispatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "delivery_id", nullable = false)
    private long deliveryId;

    @Column(name = "target_type", nullable = false, length = 32)
    private String targetType;

    @Column(name = "target_id", nullable = false, length = 64)
    private String targetId;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "attempts", nullable = false, columnDefinition = "integer default 0")
    private int attempts;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "last_status")
    private Integer lastStatus;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "downgraded", nullable = false, columnDefinition = "boolean default false")
    private boolean downgraded;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> content = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "leased_until")
    private Instant leasedUntil;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "sent_at")
    private Instant sentAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected PushDispatchEntity() {}

    /** A new pending dispatch. */
    public PushDispatchEntity(long deliveryId, String targetType, String targetId, Map<String, Object> content,
            Instant createdAt) {
        this.deliveryId = deliveryId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.content = content;
        this.createdAt = createdAt;
        this.nextAttemptAt = createdAt;
        this.status = "pending";
    }

    /** The {@code id} column. */
    public Long getId() {
        return id;
    }

    /** The {@code delivery_id} column. */
    public long getDeliveryId() {
        return deliveryId;
    }

    /** Sets the {@code delivery_id} column. */
    public void setDeliveryId(long value) {
        this.deliveryId = value;
    }

    /** The {@code target_type} column. */
    public String getTargetType() {
        return targetType;
    }

    /** Sets the {@code target_type} column. */
    public void setTargetType(String value) {
        this.targetType = value;
    }

    /** The {@code target_id} column. */
    public String getTargetId() {
        return targetId;
    }

    /** Sets the {@code target_id} column. */
    public void setTargetId(String value) {
        this.targetId = value;
    }

    /** The {@code status} column. */
    public String getStatus() {
        return status;
    }

    /** Sets the {@code status} column. */
    public void setStatus(String value) {
        this.status = value;
    }

    /** The {@code attempts} column. */
    public int getAttempts() {
        return attempts;
    }

    /** Sets the {@code attempts} column. */
    public void setAttempts(int value) {
        this.attempts = value;
    }

    /** The {@code next_attempt_at} column. */
    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    /** Sets the {@code next_attempt_at} column. */
    public void setNextAttemptAt(Instant value) {
        this.nextAttemptAt = value;
    }

    /** The {@code last_status} column. */
    public Integer getLastStatus() {
        return lastStatus;
    }

    /** Sets the {@code last_status} column. */
    public void setLastStatus(Integer value) {
        this.lastStatus = value;
    }

    /** The {@code last_error} column. */
    public String getLastError() {
        return lastError;
    }

    /** Sets the {@code last_error} column. */
    public void setLastError(String value) {
        this.lastError = value;
    }

    /** The {@code downgraded} column. */
    public boolean isDowngraded() {
        return downgraded;
    }

    /** Sets the {@code downgraded} column. */
    public void setDowngraded(boolean value) {
        this.downgraded = value;
    }

    /** The {@code content} column. */
    public Map<String, Object> getContent() {
        return content;
    }

    /** Sets the {@code content} column. */
    public void setContent(Map<String, Object> value) {
        this.content = value;
    }

    /** The {@code leased_until} column. */
    public Instant getLeasedUntil() {
        return leasedUntil;
    }

    /** Sets the {@code leased_until} column. */
    public void setLeasedUntil(Instant value) {
        this.leasedUntil = value;
    }

    /** The {@code sent_at} column. */
    public Instant getSentAt() {
        return sentAt;
    }

    /** Sets the {@code sent_at} column. */
    public void setSentAt(Instant value) {
        this.sentAt = value;
    }

    /** The {@code created_at} column. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets the {@code created_at} column. */
    public void setCreatedAt(Instant value) {
        this.createdAt = value;
    }
}
