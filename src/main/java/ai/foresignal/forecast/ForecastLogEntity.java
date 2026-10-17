package ai.foresignal.forecast;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One event of the {@code forecasts} SSE topic (prompt 122, table {@code forecast_log}): {@code question.opened},
 * {@code question.closed}, {@code question.resolved}, {@code question.annulled} or {@code forecast.submitted}, with
 * its payload as sent. The id is the topic cursor, so a reconnecting client replays what it missed.
 */
@Entity
@Table(name = "forecast_log", indexes = @Index(name = "forecast_log_question_idx", columnList = "question_id, at"))
public class ForecastLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event", length = 32, nullable = false)
    private String event;

    @Column(name = "question_id", length = 26, nullable = false)
    private String questionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload = new LinkedHashMap<>();

    @Column(name = "private", nullable = false)
    private boolean privateEvent;

    @Column(name = "at", nullable = false)
    private Instant at;

    /** For JPA. */
    protected ForecastLogEntity() {}

    /**
     * Creates a log row.
     *
     * @param event the event name
     * @param questionId the question
     * @param payload the payload as sent
     * @param privateEvent whether only callers with private access may see it
     * @param at when it happened
     */
    public ForecastLogEntity(String event, String questionId, Map<String, Object> payload, boolean privateEvent,
            Instant at) {
        this.event = event;
        this.questionId = questionId;
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

    /** The question. */
    public String getQuestionId() {
        return questionId;
    }

    /** The payload. */
    public Map<String, Object> getPayload() {
        return payload;
    }

    /** Whether it is private. */
    public boolean isPrivateEvent() {
        return privateEvent;
    }

    /** When it happened. */
    public Instant getAt() {
        return at;
    }
}
