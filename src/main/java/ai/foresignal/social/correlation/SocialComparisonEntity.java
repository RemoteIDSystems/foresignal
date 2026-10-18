package ai.foresignal.social.correlation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A social–physical comparison (prompt 137, table {@code social_comparison}): one claim tested against independent
 * physical evidence in its place and window, with detectability, lead times, status, the 122 ledger question and the
 * current revision. Declares the schema; {@link ComparisonStore} reads and writes it with SQL. Comparisons are
 * intelligence objects: they are kept when the claim's posts expire.
 */
@Entity
@Table(name = "social_comparison", indexes = {
        @Index(name = "social_comparison_claim_uq", columnList = "claim_id", unique = true),
        @Index(name = "social_comparison_narrative_idx", columnList = "narrative_id"),
        @Index(name = "social_comparison_status_idx", columnList = "status, final_at"),
        @Index(name = "social_comparison_evaluated_idx", columnList = "evaluated_at")})
public class SocialComparisonEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "claim_id", length = 26, nullable = false)
    private String claimId;

    @Column(name = "narrative_id", length = 26, nullable = false)
    private String narrativeId;

    @Column(name = "claim_type", length = 64, nullable = false)
    private String claimType;

    @Column(name = "signal_family", length = 64, nullable = false)
    private String signalFamily;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "place")
    private Map<String, Object> place;

    @Column(name = "place_lon")
    private Double placeLon;

    @Column(name = "place_lat")
    private Double placeLat;

    @Column(name = "region_code", length = 16)
    private String regionCode;

    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    @Column(name = "window_end", nullable = false)
    private Instant windowEnd;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "physical_evidence")
    private List<Object> physicalEvidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detectability")
    private Map<String, Object> detectability;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lead_times")
    private Map<String, Object> leadTimes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contradiction")
    private Map<String, Object> contradiction;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "caveats")
    private List<Object> caveats;

    @Column(name = "found_by", length = 16, nullable = false)
    private String foundBy;

    @Column(name = "cue_id", length = 26)
    private String cueId;

    @Column(name = "trigger_ref", length = 600)
    private String triggerRef;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "platforms", columnDefinition = "text[]")
    private String[] platforms;

    @Column(name = "evidence_group_count", nullable = false)
    private int evidenceGroupCount;

    @Column(name = "question_id", length = 26)
    private String questionId;

    @Column(name = "revision", nullable = false)
    private int revision;

    @Column(name = "final_at", nullable = false)
    private Instant finalAt;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected SocialComparisonEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
