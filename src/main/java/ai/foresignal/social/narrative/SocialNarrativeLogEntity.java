package ai.foresignal.social.narrative;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One narrative history entry (prompt 136, table {@code social_narrative_log}): creation, merges and splits (118 rules),
 * state, status and title changes. Declares the schema; {@link NarrativeStore} writes it.
 */
@Entity
@Table(name = "social_narrative_log", indexes = {
        @Index(name = "social_narrative_log_narrative_idx", columnList = "narrative_id, at")})
public class SocialNarrativeLogEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "narrative_id", length = 26, nullable = false)
    private String narrativeId;

    @Column(name = "at", nullable = false)
    private Instant at;

    @Column(name = "action", length = 16, nullable = false)
    private String action;

    @Column(name = "actor", length = 128, nullable = false)
    private String actor;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "related_ids", columnDefinition = "text[]")
    private String[] relatedIds;

    @Column(name = "from_value", length = 300)
    private String fromValue;

    @Column(name = "to_value", length = 300)
    private String toValue;

    @Column(name = "reason", length = 2000)
    private String reason;

    /** For JPA. */
    protected SocialNarrativeLogEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
