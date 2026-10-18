package ai.foresignal.chronicle.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * That a principal was shown an opaque link (table {@code chronicle_link_disclosure}, prompt 134): the daily cap of
 * {@value NetworkVocabulary#OPAQUE_LINKS_PER_DAY} new opaque links counts these rows, and a link shown once stays
 * visible. Written with native inserts; Hibernate only owns the table definition.
 */
@Entity
@Table(name = "chronicle_link_disclosure", uniqueConstraints = @UniqueConstraint(
        name = "chronicle_link_disclosure_unique", columnNames = {"principal_id", "link_id"}), indexes = {
        @Index(name = "chronicle_link_disclosure_time_idx", columnList = "principal_id, disclosed_at")})
public class ChronicleLinkDisclosureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "principal_id", length = 200, nullable = false)
    private String principalId;

    @Column(name = "link_id", length = 26, nullable = false)
    private String linkId;

    @Column(name = "disclosed_at", nullable = false)
    private Instant disclosedAt;

    /** For JPA. */
    protected ChronicleLinkDisclosureEntity() {}

    /** The row id. */
    public Long getId() {
        return id;
    }

    /** The principal shown the link. */
    public String getPrincipalId() {
        return principalId;
    }

    /** The link. */
    public String getLinkId() {
        return linkId;
    }

    /** When it was first shown. */
    public Instant getDisclosedAt() {
        return disclosedAt;
    }
}
