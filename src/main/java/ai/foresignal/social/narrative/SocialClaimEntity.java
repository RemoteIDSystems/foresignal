package ai.foresignal.social.narrative;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A claim of a narrative (prompt 136, table {@code social_claim}): what, where, when and who, with support, evidence groups,
 * extraction method and manifest, extraction confidence, 137 status and its Admiralty grade as a 125 evidence item
 * ({@code source = social-claim}). A report, never a fact. Declares the schema; {@link NarrativeStore} reads and writes
 * it.
 */
@Entity
@Table(name = "social_claim", indexes = {
        @Index(name = "social_claim_narrative_idx", columnList = "narrative_id"),
        @Index(name = "social_claim_first_idx", columnList = "first_observed_at"),
        @Index(name = "social_claim_source_idx", columnList = "source_key"),
        @Index(name = "social_claim_key_uq", columnList = "narrative_id, claim_key, extraction", unique = true)})
public class SocialClaimEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "narrative_id", length = 26, nullable = false)
    private String narrativeId;

    @Column(name = "claim_key", length = 600, nullable = false)
    private String claimKey;

    @Column(name = "type", length = 64, nullable = false)
    private String type;

    @Column(name = "signal_family", length = 64)
    private String signalFamily;

    @Column(name = "polarity", length = 16, nullable = false)
    private String polarity;

    @Column(name = "hedged", nullable = false)
    private boolean hedged;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "place")
    private Map<String, Object> place;

    @Column(name = "place_lon")
    private Double placeLon;

    @Column(name = "place_lat")
    private Double placeLat;

    @Column(name = "region_code", length = 16)
    private String regionCode;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "entity_refs", columnDefinition = "text[]")
    private String[] entityRefs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_time")
    private Map<String, Object> eventTime;

    @Column(name = "actor", length = 200)
    private String actor;

    @Column(name = "support_count", nullable = false)
    private int supportCount;

    @Column(name = "evidence_group_count", nullable = false)
    private int evidenceGroupCount;

    @Column(name = "first_observed_at", nullable = false)
    private Instant firstObservedAt;

    @Column(name = "extraction", length = 16, nullable = false)
    private String extraction;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "comparison_id", length = 26)
    private String comparisonId;

    @Column(name = "source_key", length = 300)
    private String sourceKey;

    @Column(name = "source_reliability", length = 1, nullable = false)
    private String sourceReliability;

    @Column(name = "information_credibility", nullable = false)
    private int informationCredibility;

    @Column(name = "tlp", length = 16, nullable = false)
    private String tlp;

    @Column(name = "restatement", length = 240)
    private String restatement;

    @Column(name = "framing_score")
    private Double framingScore;

    @Column(name = "framing_label", length = 16)
    private String framingLabel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reputation", columnDefinition = "jsonb")
    private Map<String, Object> reputation;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected SocialClaimEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
