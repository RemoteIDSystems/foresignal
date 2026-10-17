package ai.foresignal.cycle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One event of the {@code assessments} SSE topic (prompt 128, table {@code assessment_log}):
 * {@code assessment.published} or {@code cycle.run_failed}, with its payload as sent. The id is the topic cursor, so
 * a reconnecting client replays what it missed.
 */
@Entity
@Table(name = "assessment_log")
public class AssessmentLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event", length = 32, nullable = false)
    private String event;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload = new LinkedHashMap<>();

    @Column(name = "private", nullable = false)
    private boolean privateEvent;

    @Column(name = "at", nullable = false)
    private Instant at;

    /** For JPA. */
    protected AssessmentLogEntity() {}

    /** Creates a log row. */
    public AssessmentLogEntity(String event, Map<String, Object> payload, boolean privateEvent, Instant at) {
        this.event = event;
        this.payload = new LinkedHashMap<>(payload);
        this.privateEvent = privateEvent;
        this.at = at;
    }

    /** The cursor. */
    public Long getId() {
        return id;
    }

    /** The event name. */
    public String getEvent() {
        return event;
    }

    /** The payload as sent. */
    public Map<String, Object> getPayload() {
        return payload;
    }

    /** True when only callers with private access may see it. */
    public boolean isPrivateEvent() {
        return privateEvent;
    }

    /** When it happened. */
    public Instant getAt() {
        return at;
    }
}
