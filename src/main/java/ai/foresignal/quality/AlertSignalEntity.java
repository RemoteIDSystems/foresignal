package ai.foresignal.quality;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

/**
 * One implicit signal of one alert (prompt 115, table {@code alert_signal}): at most one row per alert and type.
 * Hibernate owns the table; {@link AlertSignalStore} writes it with native SQL.
 */
@Entity
@Table(name = "alert_signal",
        uniqueConstraints = @UniqueConstraint(name = "alert_signal_alert_type_key", columnNames = {"alert_id", "type"}),
        indexes = @Index(name = "alert_signal_rule_at_idx", columnList = "rule_id, at"))
public class AlertSignalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "alert_id", nullable = false)
    private long alertId;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "type", length = 16, nullable = false)
    private String type;

    @Column(name = "at", nullable = false)
    private Instant at;

    protected AlertSignalEntity() {
        // for JPA
    }

    /** The id. */
    public Long getId() {
        return id;
    }

    /** The alert. */
    public long getAlertId() {
        return alertId;
    }

    /** The alert's rule, or null. */
    public UUID getRuleId() {
        return ruleId;
    }

    /** The {@link AlertSignalType} name. */
    public String getType() {
        return type;
    }

    /** When the signal happened. */
    public Instant getAt() {
        return at;
    }
}
