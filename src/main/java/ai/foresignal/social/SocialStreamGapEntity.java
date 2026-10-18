package ai.foresignal.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A gap in a complete social stream (prompt 135, table {@code social_stream_gap}): from the drop to the reconnect
 * ({@code gap_to} null while still disconnected), whether the gap was replayed (Jetstream cursor resume or Network
 * Replay) and why it happened. An hour overlapping an unreplayed gap is not complete.
 */
@Entity
@Table(name = "social_stream_gap", indexes = @Index(name = "social_stream_gap_source_idx",
        columnList = "source_id, gap_from"))
public class SocialStreamGapEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "gap_from", nullable = false)
    private Instant gapFrom;

    @Column(name = "gap_to")
    private Instant gapTo;

    @Column(name = "replayed", nullable = false)
    private boolean replayed;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected SocialStreamGapEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
