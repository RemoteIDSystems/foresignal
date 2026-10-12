package ai.foresignal.ask;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * One Ask thread ({@code ask_thread}, Architecture §7.1 and §21): a conversation of questions and
 * answers that follow-up questions continue.
 *
 * <p>A thread belongs to the token that created it ({@code token_id}; null for an anonymous caller).
 * Only that token, or an {@code admin} token, lists and reads it.
 */
@Entity
@Table(
        name = "ask_thread",
        indexes = {
            @Index(name = "ask_thread_token_idx", columnList = "token_id, updated_at"),
            @Index(name = "ask_thread_updated_idx", columnList = "updated_at")
        })
public class AskThreadEntity {

    /** The longest title: the first 80 characters of the first question. */
    public static final int TITLE_LENGTH = 80;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = TITLE_LENGTH)
    private String title;

    /** The creating token, or null for an anonymous caller. */
    @Column(name = "token_id")
    private UUID tokenId;

    /** The creating token's principal, or null. */
    @Column(name = "principal_id", length = 64)
    private String principalId;

    @Column(name = "message_count", nullable = false)
    private int messageCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected AskThreadEntity() {}

    /**
     * A new thread.
     *
     * @param id the thread id
     * @param title its title
     * @param tokenId the creating token, or null
     * @param principalId the token's principal, or null
     * @param now the creation time
     */
    public AskThreadEntity(UUID id, String title, UUID tokenId, String principalId, Instant now) {
        this.id = id;
        this.title = title;
        this.tokenId = tokenId;
        this.principalId = principalId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The title of a thread whose first question is {@code question}: its first 80 characters. */
    public static String titleOf(String question) {
        String flat = question.strip().replaceAll("\\s+", " ");
        return flat.length() <= TITLE_LENGTH ? flat : flat.substring(0, TITLE_LENGTH);
    }

    /** The thread id. */
    public UUID getId() {
        return id;
    }

    /** The title. */
    public String getTitle() {
        return title;
    }

    /** The creating token, or null. */
    public UUID getTokenId() {
        return tokenId;
    }

    /** The creating token's principal, or null. */
    public String getPrincipalId() {
        return principalId;
    }

    /** Messages in the thread. */
    public int getMessageCount() {
        return messageCount;
    }

    /** Sets the message count. */
    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When a message was last added. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the last activity time. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
