package ai.foresignal.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * One calendar month (UTC) of a paid social adapter's money spend (prompt 135, table {@code social_spend}): the 005
 * budget in money, beside the request counts the source budget keeps. {@code id} is {@code {sourceId}|{yyyy-MM}}.
 * Written by SQL ({@link XSpend}).
 */
@Entity
@Table(name = "social_spend")
public class SocialSpendEntity {

    @Id
    @Column(name = "id", length = 80, nullable = false)
    private String id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "month", length = 7, nullable = false)
    private String month;

    @Column(name = "spent_usd", precision = 14, scale = 6, nullable = false)
    private BigDecimal spentUsd;

    @Column(name = "calls", nullable = false)
    private long calls;

    @Column(name = "refused", nullable = false)
    private long refused;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected SocialSpendEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
