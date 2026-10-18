package ai.foresignal.media.outlet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * The operator's settings of one rating provider (prompt 133, table {@code media_provider}): enabled, the admin's
 * license acknowledgement, a recorded redistribution permission, the encrypted MBFC key and the last import. The
 * provider's license, egress and default state are code ({@link MediaProviders}).
 */
@Entity
@Table(name = "media_provider")
public class MediaProviderEntity {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "licence_acknowledged", nullable = false)
    private boolean licenseAcknowledged;

    @Column(name = "acknowledged_by", length = 200)
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "permission_recorded", nullable = false)
    private boolean permissionRecorded;

    @Column(name = "api_key_ciphertext")
    private byte[] apiKeyCiphertext;

    @Column(name = "api_key_nonce")
    private byte[] apiKeyNonce;

    @Column(name = "last_import_at")
    private Instant lastImportAt;

    @Column(name = "last_import_status", length = 16)
    private String lastImportStatus;

    @Column(name = "last_import_message", length = 1000)
    private String lastImportMessage;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected MediaProviderEntity() {}

    /** The provider id. */
    public String getId() {
        return id;
    }
}
