package ai.foresignal.chatops;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Where an interactive channel's connector continues reading ({@code chatops_cursor}, prompt 061): the
 * Telegram {@code getUpdates} offset or the Matrix {@code next_batch}, so a restart neither re-processes nor
 * drops updates. Declared for Hibernate to own the table; {@link ChatopsCursorStore} reads and writes it with
 * SQL.
 */
@Entity
@Table(name = "chatops_cursor")
public class ChatopsCursorEntity {

    @Id
    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @Column(name = "cursor", nullable = false, columnDefinition = "text")
    private String cursor;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ChatopsCursorEntity() {}

    /** The channel. */
    public UUID getChannelId() {
        return channelId;
    }

    /** The cursor. */
    public String getCursor() {
        return cursor;
    }

    /** When it was stored. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
