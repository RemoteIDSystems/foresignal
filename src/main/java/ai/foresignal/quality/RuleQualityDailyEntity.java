package ai.foresignal.quality;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One rule's alert-quality trend for one UTC day (prompt 115, table {@code rule_quality_daily}, retained 400 days).
 * The nightly job writes the alert, feedback and signal columns; the trigger engine adds to the suppression
 * counters as it drops matches. Hibernate maps the table (Flyway creates it); {@link RuleQualityDailyStore} writes it with native SQL.
 */
@Entity
@Table(name = "rule_quality_daily",
        uniqueConstraints = @UniqueConstraint(name = "rule_quality_daily_rule_day_key", columnNames = {"rule_id", "day"}))
public class RuleQualityDailyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "fired", nullable = false, columnDefinition = "integer default 0")
    private int fired;

    @Column(name = "useful", nullable = false, columnDefinition = "integer default 0")
    private int useful;

    @Column(name = "noise", nullable = false, columnDefinition = "integer default 0")
    private int noise;

    @Column(name = "late", nullable = false, columnDefinition = "integer default 0")
    private int late;

    @Column(name = "false_positive", nullable = false, columnDefinition = "integer default 0")
    private int falsePositive;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "signals")
    private Map<String, Object> signals;

    @Column(name = "median_ack_seconds")
    private Double medianAckSeconds;

    @Column(name = "flaps", nullable = false, columnDefinition = "integer default 0")
    private int flaps;

    @Column(name = "suppressed_by_cooldown", nullable = false, columnDefinition = "integer default 0")
    private int suppressedByCooldown;

    @Column(name = "suppressed_by_rate_cap", nullable = false, columnDefinition = "integer default 0")
    private int suppressedByRateCap;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected RuleQualityDailyEntity() {
        // for JPA
    }

    /** The id. */
    public Long getId() {
        return id;
    }

    /** The rule. */
    public UUID getRuleId() {
        return ruleId;
    }

    /** The UTC day. */
    public LocalDate getDay() {
        return day;
    }

    /** Alerts fired that day. */
    public int getFired() {
        return fired;
    }

    /** {@code useful} feedback. */
    public int getUseful() {
        return useful;
    }

    /** {@code noise} feedback. */
    public int getNoise() {
        return noise;
    }

    /** {@code late} feedback. */
    public int getLate() {
        return late;
    }

    /** {@code false_positive} feedback. */
    public int getFalsePositive() {
        return falsePositive;
    }

    /** Signal counts by name. */
    public Map<String, Object> getSignals() {
        return signals;
    }

    /** The median time to acknowledge, seconds, or null. */
    public Double getMedianAckSeconds() {
        return medianAckSeconds;
    }

    /** Alerts resolved by the inverse edge within 10 min. */
    public int getFlaps() {
        return flaps;
    }

    /** Matches dropped by the cooldown. */
    public int getSuppressedByCooldown() {
        return suppressedByCooldown;
    }

    /** Matches dropped by the rate cap. */
    public int getSuppressedByRateCap() {
        return suppressedByRateCap;
    }

    /** When the row last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
