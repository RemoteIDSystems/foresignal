package ai.foresignal.hypothesis;

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
 * One event of the {@code hypotheses} SSE topic (prompt 125, table {@code hypothesis_log}): {@code set.created},
 * {@code set.revised}, {@code set.band_changed}, {@code set.resolved}, {@code indicator.observed},
 * {@code indicator.absent} or {@code assumption.broken}, with its payload as sent. The id is the topic cursor.
 */
@Entity
@Table(name = "hypothesis_log", indexes = @Index(name = "hypothesis_log_set_idx", columnList = "set_id, at"))
public class HypothesisLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event", length = 32, nullable = false)
    private String event;

    @Column(name = "set_id", length = 26, nullable = false)
    private String setId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload = new LinkedHashMap<>();

    @Column(name = "private", nullable = false)
    private boolean privateEvent;

    @Column(name = "at", nullable = false)
    private Instant at;

    /** For JPA. */
    protected HypothesisLogEntity() {}

    /**
     * Creates a log row.
     *
     * @param event the event name
     * @param setId the set
     * @param payload the payload as sent
     * @param privateEvent whether only callers with private access may see it
     * @param at when it happened
     */
    public HypothesisLogEntity(String event, String setId, Map<String, Object> payload, boolean privateEvent,
            Instant at) {
        this.event = event;
        this.setId = setId;
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

    /** The set. */
    public String getSetId() {
        return setId;
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
