package ai.foresignal.analytics.finding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One change to a finding (prompt 119, table {@code analytic_finding_log}): who ({@code actor}: a detector, a token id
 * or {@code operator}) did what ({@code created}, {@code superseded}, {@code dismissed}, {@code linked}). The id is the
 * {@code analytics} SSE cursor; {@code event} is the stream event it announced.
 */
@Entity
@Table(name = "analytic_finding_log", indexes = @Index(name = "analytic_finding_log_finding_idx",
        columnList = "finding_id, at"))
public class FindingLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "finding_id", length = 26, nullable = false)
    private String findingId;

    @Column(name = "at", nullable = false)
    private Instant at;

    @Column(name = "actor", length = 200)
    private String actor;

    @Column(name = "action", length = 32, nullable = false)
    private String action;

    @Column(name = "event", length = 32)
    private String event;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    /** JPA. */
    protected FindingLogEntity() {}

    /** Creates a log entry. */
    public FindingLogEntity(String findingId, Instant at, String actor, String action, String event, String reason) {
        this.findingId = findingId;
        this.at = at;
        this.actor = actor;
        this.action = action;
        this.event = event;
        this.reason = reason;
    }

    /** The id (the SSE cursor). */
    public Long getId() {
        return id;
    }

    /** The finding. */
    public String getFindingId() {
        return findingId;
    }

    /** When. */
    public Instant getAt() {
        return at;
    }

    /** Who. */
    public String getActor() {
        return actor;
    }

    /** What. */
    public String getAction() {
        return action;
    }

    /** The stream event announced, or null. */
    public String getEvent() {
        return event;
    }

    /** The reason, or null. */
    public String getReason() {
        return reason;
    }
}
