package ai.foresignal.social.correlation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One revision of a comparison (prompt 137, table {@code social_comparison_revision}): every status change, and every
 * change of the matching physical evidence, until the comparison is final. Declares the schema; {@link
 * ComparisonStore} reads and writes it.
 */
@Entity
@Table(name = "social_comparison_revision", indexes = {
        @Index(name = "social_comparison_revision_uq", columnList = "comparison_id, revision", unique = true)})
public class SocialComparisonRevisionEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "comparison_id", length = 26, nullable = false)
    private String comparisonId;

    @Column(name = "revision", nullable = false)
    private int revision;

    @Column(name = "at", nullable = false)
    private Instant at;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "previous_status", length = 32)
    private String previousStatus;

    @Column(name = "reason", length = 1000, nullable = false)
    private String reason;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "physical_evidence_refs", columnDefinition = "text[]")
    private String[] physicalEvidenceRefs;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    /** For JPA. */
    protected SocialComparisonRevisionEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
