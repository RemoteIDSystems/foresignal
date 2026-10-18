package ai.foresignal.social.correlation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A cue-driven social search (prompt 137, table {@code social_cue}): its trigger, the exact queries per platform, the
 * window, status, results per platform, the narratives its posts joined, cost and manifest. Declares the schema;
 * {@link ComparisonStore} reads and writes it.
 */
@Entity
@Table(name = "social_cue", indexes = {
        @Index(name = "social_cue_created_idx", columnList = "created_at"),
        @Index(name = "social_cue_status_idx", columnList = "status"),
        @Index(name = "social_cue_trigger_idx", columnList = "trigger_ref")})
public class SocialCueEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "trigger_type", length = 32, nullable = false)
    private String triggerType;

    @Column(name = "trigger_ref", length = 600)
    private String triggerRef;

    @Column(name = "backend", length = 32, nullable = false)
    private String backend;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "queries")
    private List<Object> queries;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "platforms", columnDefinition = "text[]")
    private String[] platforms;

    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    @Column(name = "window_end")
    private Instant windowEnd;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Column(name = "status_reason", length = 500)
    private String statusReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "results")
    private List<Object> results;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "narrative_ids", columnDefinition = "text[]")
    private String[] narrativeIds;

    @Column(name = "cost_usd", precision = 12, scale = 4, nullable = false)
    private BigDecimal costUsd;

    @Column(name = "max_cost_usd", precision = 12, scale = 4, nullable = false)
    private BigDecimal maxCostUsd;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input")
    private Map<String, Object> input;

    @Column(name = "created_by", length = 200, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    /** For JPA. */
    protected SocialCueEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
