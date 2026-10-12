package ai.foresignal.ask;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One action a model proposed in an Ask answer ({@code ask_action}, Architecture §7.1 and §21;
 * invariant EGR-3).
 *
 * <p>Created {@value #PENDING}. It becomes {@value #EXECUTED} or {@value #FAILED} when an
 * {@code agent} token confirms it (or at once with {@code allowActions} and agent scope), with the
 * outcome in {@code result}, or {@value #REJECTED} when rejected. It belongs to the token that asked
 * ({@code token_id}): another non-admin token cannot see, confirm or reject it.
 */
@Entity
@Table(
        name = "ask_action",
        indexes = {
            @Index(name = "ask_action_thread_idx", columnList = "thread_id"),
            @Index(name = "ask_action_token_idx", columnList = "token_id")
        })
public class AskActionEntity {

    /** Waiting for confirmation. */
    public static final String PENDING = "pending";

    /** Confirmed and executed. */
    public static final String EXECUTED = "executed";

    /** Rejected; never executed. */
    public static final String REJECTED = "rejected";

    /** Confirmed, but the execution failed (the reason is in {@code result}). */
    public static final String FAILED = "failed";

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "thread_id", nullable = false)
    private UUID threadId;

    /** The assistant message that proposed it. */
    @Column(name = "message_id")
    private Long messageId;

    /** The asking token, or null for an anonymous caller. */
    @Column(name = "token_id")
    private UUID tokenId;

    /** {@code AskAction.type}: {@code create_watch}, {@code update_watch}, {@code acknowledge_alert}, … */
    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    /** The validated payload, as the REST operation takes it. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private Map<String, Object> payload;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /** The executed operation's result, or {@code {error}} for a failure. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result")
    private Map<String, Object> result;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "executed_at")
    private Instant executedAt;

    /** Who confirmed, rejected or (with allowActions) executed it. */
    @Column(name = "decided_by", length = 256)
    private String decidedBy;

    /** For JPA. */
    protected AskActionEntity() {}

    /**
     * A new pending action.
     *
     * @param id the action id
     * @param threadId the thread
     * @param tokenId the asking token, or null
     * @param type the action type
     * @param description what it does, for people
     * @param payload the validated payload
     * @param createdAt when it was proposed
     */
    public AskActionEntity(UUID id, UUID threadId, UUID tokenId, String type, String description,
            Map<String, Object> payload, Instant createdAt) {
        this.id = id;
        this.threadId = threadId;
        this.tokenId = tokenId;
        this.type = type;
        this.description = description;
        this.payload = payload;
        this.status = PENDING;
        this.createdAt = createdAt;
    }

    /** The action id. */
    public UUID getId() {
        return id;
    }

    /** The thread. */
    public UUID getThreadId() {
        return threadId;
    }

    /** The proposing message, or null. */
    public Long getMessageId() {
        return messageId;
    }

    /** Sets the proposing message. */
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    /** The asking token, or null. */
    public UUID getTokenId() {
        return tokenId;
    }

    /** The type. */
    public String getType() {
        return type;
    }

    /** The description. */
    public String getDescription() {
        return description;
    }

    /** The payload. */
    public Map<String, Object> getPayload() {
        return payload;
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The result, or null. */
    public Map<String, Object> getResult() {
        return result;
    }

    /** Sets the result. */
    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    /** When it was proposed. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it was executed, rejected or failed, or null. */
    public Instant getExecutedAt() {
        return executedAt;
    }

    /** Sets the decision time. */
    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }

    /** Who decided, or null. */
    public String getDecidedBy() {
        return decidedBy;
    }

    /** Sets who decided. */
    public void setDecidedBy(String decidedBy) {
        this.decidedBy = decidedBy;
    }
}
