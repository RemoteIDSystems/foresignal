package ai.foresignal.chatops;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One pending chat confirmation ({@code chatops_pending}, prompt 061): a Telegram button's opaque
 * {@code callback_data} token ({@code cc_…}) or a Matrix 6-character code for {@code /confirm} and
 * {@code /reject}. It lasts one hour, is single-use ({@code used_at}) and is bound to the chat and the user it
 * was shown to. Declared for Hibernate to own the table; {@link PendingTokens} reads and writes it with SQL.
 */
@Entity
@Table(name = "chatops_pending", indexes = @Index(name = "chatops_pending_expires_idx", columnList = "expires_at"))
public class ChatopsPendingEntity {

    @Id
    @Column(name = "token", nullable = false, length = 64)
    private String token;

    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @Column(name = "chat_id", nullable = false, length = 255)
    private String chatId;

    @Column(name = "user_id", length = 255)
    private String userId;

    @Column(name = "action", nullable = false, length = 32)
    private String action;

    @Column(name = "ref", nullable = false, length = 128)
    private String ref;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "used_at")
    private Instant usedAt;

    /** For JPA. */
    protected ChatopsPendingEntity() {}

    /** The token or code. */
    public String getToken() {
        return token;
    }

    /** The channel. */
    public UUID getChannelId() {
        return channelId;
    }

    /** The chat it was shown in. */
    public String getChatId() {
        return chatId;
    }

    /** The user it was shown to, or null. */
    public String getUserId() {
        return userId;
    }

    /** What a press does. */
    public String getAction() {
        return action;
    }

    /** What it acts on (an alert id, an Ask action id). */
    public String getRef() {
        return ref;
    }

    /** When it expires. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** When it was used, or null. */
    public Instant getUsedAt() {
        return usedAt;
    }
}
