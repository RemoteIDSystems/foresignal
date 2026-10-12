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
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One notification's delivery to one channel, with its retry and deferral state (Architecture
 * §7.1, prompt 018).
 *
 * <p>Beyond the Architecture columns: {@code notification_id}, {@code kind}
 * ({@link DeliveryKind}), {@code provider_message_ref} (the destination's message id, receipt or
 * event id, used to edit or cancel later), {@code deferred_reason} ({@link DeferredReason}) and
 * {@code note}; plus {@code edited}, {@code folded_into} (the digest delivery that carries a folded
 * row), {@code leased_until} (a worker's claim) and {@code retry_after_counted_at} (when a 429 last
 * counted as an attempt).
 */
@Entity
@Table(
        name = "notification_delivery",
        indexes = {
            @Index(name = "notification_delivery_due_idx", columnList = "status, next_attempt_at"),
            @Index(name = "notification_delivery_channel_idx", columnList = "channel_id, id DESC"),
            @Index(name = "notification_delivery_notification_idx", columnList = "notification_id")
        })
public class NotificationDeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @Column(name = "notification_id", nullable = false)
    private long notificationId;

    @Column(name = "event", nullable = false, length = 32)
    private String event;

    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "briefing_run_id")
    private UUID briefingRunId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "kind", length = 16, nullable = false)
    private String kind = DeliveryKind.SEND;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "provider_message_ref", length = 512)
    private String providerMessageRef;

    @Column(name = "deferred_reason", length = 32)
    private String deferredReason;

    @Column(name = "note", length = 200)
    private String note;

    @Column(name = "edited", nullable = false, columnDefinition = "boolean default false")
    private boolean edited;

    @Column(name = "folded_into")
    private Long foldedInto;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "leased_until")
    private Instant leasedUntil;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "retry_after_counted_at")
    private Instant retryAfterCountedAt;

    /** The escalation step that created this delivery (prompt 033), or null. */
    @Column(name = "escalation_step")
    private Integer escalationStep;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected NotificationDeliveryEntity() {
        // for JPA
    }

    /** A pending delivery of {@code notificationId} to {@code channelId}, due at {@code nextAttemptAt}. */
    public NotificationDeliveryEntity(UUID channelId, long notificationId, String event, Instant nextAttemptAt) {
        this.channelId = channelId;
        this.notificationId = notificationId;
        this.event = event;
        this.nextAttemptAt = nextAttemptAt;
        this.createdAt = nextAttemptAt;
    }

    /** The id, null before the insert. */
    public Long getId() {
        return id;
    }

    /** The channel. */
    public UUID getChannelId() {
        return channelId;
    }

    /** The notification. */
    public long getNotificationId() {
        return notificationId;
    }

    /** Sets the notification. */
    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }

    /** The event. */
    public String getEvent() {
        return event;
    }

    /** The alert, or null. */
    public Long getAlertId() {
        return alertId;
    }

    /** Sets the alert. */
    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    /** The briefing run, or null. */
    public UUID getBriefingRunId() {
        return briefingRunId;
    }

    /** Sets the briefing run. */
    public void setBriefingRunId(UUID briefingRunId) {
        this.briefingRunId = briefingRunId;
    }

    /** The status. */
    public DeliveryStatus getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    /** The kind ({@link DeliveryKind}). */
    public String getKind() {
        return kind;
    }

    /** Sets the kind. */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /** Attempts so far. */
    public int getAttempts() {
        return attempts;
    }

    /** Sets the attempts. */
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    /** The last error, redacted, or null. */
    public String getLastError() {
        return lastError;
    }

    /** Sets the last error. */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /** When it is next due, or null when settled. */
    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    /** Sets when it is next due. */
    public void setNextAttemptAt(Instant nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    /** When delivered, or null. */
    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    /** Sets when delivered. */
    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    /** The destination's message reference, or null. */
    public String getProviderMessageRef() {
        return providerMessageRef;
    }

    /** Sets the destination's message reference. */
    public void setProviderMessageRef(String providerMessageRef) {
        this.providerMessageRef = providerMessageRef;
    }

    /** Why it is deferred ({@link DeferredReason}), or null. */
    public String getDeferredReason() {
        return deferredReason;
    }

    /** Sets why it is deferred. */
    public void setDeferredReason(String deferredReason) {
        this.deferredReason = deferredReason;
    }

    /** A short note, or null. */
    public String getNote() {
        return note;
    }

    /** Sets the note. */
    public void setNote(String note) {
        this.note = note;
    }

    /** True when an update was delivered by editing the original message. */
    public boolean isEdited() {
        return edited;
    }

    /** Sets the edited flag. */
    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    /** The digest delivery this one was folded into, or null. */
    public Long getFoldedInto() {
        return foldedInto;
    }

    /** Sets the digest delivery this one was folded into. */
    public void setFoldedInto(Long foldedInto) {
        this.foldedInto = foldedInto;
    }

    /** Until when a worker holds it, or null. */
    public Instant getLeasedUntil() {
        return leasedUntil;
    }

    /** Sets the lease. */
    public void setLeasedUntil(Instant leasedUntil) {
        this.leasedUntil = leasedUntil;
    }

    /** When a 429 last counted as an attempt, or null. */
    public Instant getRetryAfterCountedAt() {
        return retryAfterCountedAt;
    }

    /** Sets when a 429 last counted as an attempt. */
    public void setRetryAfterCountedAt(Instant retryAfterCountedAt) {
        this.retryAfterCountedAt = retryAfterCountedAt;
    }

    /** When enqueued. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets when enqueued. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** The escalation step that created this delivery (prompt 033), or null. */
    public Integer getEscalationStep() {
        return escalationStep;
    }

    /** Sets the escalation step. */
    public void setEscalationStep(Integer escalationStep) {
        this.escalationStep = escalationStep;
    }
}
