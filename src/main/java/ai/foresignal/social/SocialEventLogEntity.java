package ai.foresignal.social;

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
 * One id-bearing event of the {@code social} SSE topic (prompt 135, table {@code social_event_log}):
 * {@code social.velocity} now; 136 and 137 add theirs. The id is the topic cursor (key {@value SocialTopic#CURSOR_KEY}),
 * so a reconnecting client replays what it missed. The id-less {@code social.platform.state} is not logged.
 */
@Entity
@Table(name = "social_event_log")
public class SocialEventLogEntity {

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
    protected SocialEventLogEntity() {}

    /**
     * A log row.
     *
     * @param event the event name
     * @param payload the payload as sent
     * @param privateEvent true when only callers with private access may see it
     * @param at when it happened
     */
    public SocialEventLogEntity(String event, Map<String, Object> payload, boolean privateEvent, Instant at) {
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
