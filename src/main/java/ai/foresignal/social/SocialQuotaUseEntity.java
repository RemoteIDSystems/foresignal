package ai.foresignal.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One use of a platform quota (prompt 135, table {@code social_quota_use}): YouTube units, Threads queries, Reddit
 * queries, Instagram hashtags. Rolling windows are summed from these rows ({@link QuotaLedger}); rows older than eight
 * days are pruned.
 */
@Entity
@Table(name = "social_quota_use", indexes = @Index(name = "social_quota_use_idx", columnList = "source_id, unit, at"))
public class SocialQuotaUseEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "unit", length = 16, nullable = false)
    private String unit;

    @Column(name = "use_key", length = 200)
    private String useKey;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "at", nullable = false)
    private Instant at;

    /** For JPA. */
    protected SocialQuotaUseEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
