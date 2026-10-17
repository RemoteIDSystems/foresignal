package ai.foresignal.federation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One federated origin record (prompt 113, table {@code federation_origin}): which local peer source owns it, under
 * which local external id, and the newest origin seq applied. Written only by {@link OriginLedger}'s native upsert;
 * the entity exists so Hibernate manages the table.
 */
@Entity
@Table(name = "federation_origin")
public class FederationOriginEntity {

    @Id
    @Column(name = "origin_ref", length = 1024, nullable = false)
    private String originRef;

    @Column(name = "owner_source_id", length = 64, nullable = false)
    private String ownerSourceId;

    @Column(name = "local_external_id", length = 256, nullable = false)
    private String localExternalId;

    @Column(name = "origin_seq", nullable = false)
    private long originSeq;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected FederationOriginEntity() {}

    /** The origin's global reference. */
    public String getOriginRef() {
        return originRef;
    }

    /** The owning local source. */
    public String getOwnerSourceId() {
        return ownerSourceId;
    }

    /** The local external id. */
    public String getLocalExternalId() {
        return localExternalId;
    }

    /** The newest origin seq applied. */
    public long getOriginSeq() {
        return originSeq;
    }

    /** When the row last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
