package ai.foresignal.intel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One change to an intelligence event (prompt 101): who did what, before and after. */
@Entity
@Table(name = "intelligence_event_log")
public class IntelEventLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_id", length = 26, nullable = false)
    private String eventId;

    @Column(name = "at", nullable = false)
    private Instant at;

    @Column(name = "actor", length = 128)
    private String actor;

    @Column(name = "action", length = 32, nullable = false)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before")
    private Map<String, Object> before;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after")
    private Map<String, Object> after;

    /** JPA. */
    protected IntelEventLogEntity() {}

    /** Creates a log entry. */
    public IntelEventLogEntity(String eventId, Instant at, String actor, String action) {
        this.eventId = eventId;
        this.at = at;
        this.actor = actor;
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getAt() {
        return at;
    }

    public String getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public Map<String, Object> getBefore() {
        return before;
    }

    public void setBefore(Map<String, Object> before) {
        this.before = before;
    }

    public Map<String, Object> getAfter() {
        return after;
    }

    public void setAfter(Map<String, Object> after) {
        this.after = after;
    }
}
