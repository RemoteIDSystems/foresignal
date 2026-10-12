package ai.foresignal.ask;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One message of an Ask thread ({@code ask_message}, Architecture §7.1 and §21).
 *
 * <p>{@code content} is {@code {text, turns}}: the text shown to people (the question, or the
 * verified answer) and the conversation turns the message added, encoded by
 * {@link ai.foresignal.llm.client.RawTurnCodec} with the provider's raw turns kept byte for byte. A
 * follow-up question replays every stored turn of the thread, in order, then appends its own: history
 * is append-only. An assistant message also stores its verified {@code citations}, its
 * {@code warnings} (shown again whenever the thread is read), {@code usage}, {@code mode} and stop
 * reason.
 */
@Entity
@Table(name = "ask_message", indexes = @Index(name = "ask_message_thread_idx", columnList = "thread_id, position"))
public class AskMessageEntity {

    /** {@code role} of a question. */
    public static final String USER = "user";

    /** {@code role} of an answer. */
    public static final String ASSISTANT = "assistant";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "thread_id", nullable = false)
    private UUID threadId;

    /** The message's place in the thread, from 0. */
    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "role", nullable = false, length = 16)
    private String role;

    /** {@code {text, turns}}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false)
    private Map<String, Object> content;

    /** The verified {@code AskCitation}s of an answer. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "citations")
    private List<Map<String, Object>> citations;

    /** Dropped citations, uncited claims and other warnings of an answer. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "warnings")
    private List<String> warnings;

    /** {@code AskResponse.usage} of an answer. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "usage")
    private Map<String, Object> usage;

    /** {@code tools} or {@code retrieval}, for an answer. */
    @Column(name = "mode", length = 16)
    private String mode;

    @Column(name = "stop_reason", length = 16)
    private String stopReason;

    @Column(name = "refusal_category", length = 64)
    private String refusalCategory;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected AskMessageEntity() {}

    /**
     * A new message.
     *
     * @param threadId the thread
     * @param position its place in the thread
     * @param role {@value #USER} or {@value #ASSISTANT}
     * @param content {@code {text, turns}}
     * @param createdAt when it was written
     */
    public AskMessageEntity(UUID threadId, int position, String role, Map<String, Object> content, Instant createdAt) {
        this.threadId = threadId;
        this.position = position;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    /** The row id. */
    public Long getId() {
        return id;
    }

    /** The thread. */
    public UUID getThreadId() {
        return threadId;
    }

    /** The place in the thread. */
    public int getPosition() {
        return position;
    }

    /** The role. */
    public String getRole() {
        return role;
    }

    /** {@code {text, turns}}. */
    public Map<String, Object> getContent() {
        return content;
    }

    /** The citations, or null. */
    public List<Map<String, Object>> getCitations() {
        return citations;
    }

    /** Sets the citations. */
    public void setCitations(List<Map<String, Object>> citations) {
        this.citations = citations;
    }

    /** The warnings, or null. */
    public List<String> getWarnings() {
        return warnings;
    }

    /** Sets the warnings. */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    /** The usage, or null. */
    public Map<String, Object> getUsage() {
        return usage;
    }

    /** Sets the usage. */
    public void setUsage(Map<String, Object> usage) {
        this.usage = usage;
    }

    /** The mode, or null. */
    public String getMode() {
        return mode;
    }

    /** Sets the mode. */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /** The stop reason, or null. */
    public String getStopReason() {
        return stopReason;
    }

    /** Sets the stop reason. */
    public void setStopReason(String stopReason) {
        this.stopReason = stopReason;
    }

    /** The refusal category, or null. */
    public String getRefusalCategory() {
        return refusalCategory;
    }

    /** Sets the refusal category. */
    public void setRefusalCategory(String refusalCategory) {
        this.refusalCategory = refusalCategory;
    }

    /** When it was written. */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
