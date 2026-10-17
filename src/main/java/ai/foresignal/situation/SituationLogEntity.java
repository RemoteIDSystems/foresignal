package ai.foresignal.situation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One change to a situation (prompt 118): who ({@code actor}: a token id, {@code linker:<version>} or
 * {@code analyst:<profile>}) did what ({@code created}, {@code member_added}, {@code member_removed},
 * {@code status}, {@code severity}, {@code merged}, {@code split}, {@code renamed}, {@code theme}, {@code brief}),
 * with the state before and after. The id is the {@code situations} SSE cursor.
 */
@Entity
@Table(name = "situation_log", indexes = @Index(name = "situation_log_situation_idx", columnList = "situation_id, at"))
public class SituationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "situation_id", length = 26, nullable = false)
    private String situationId;

    @Column(name = "at", nullable = false)
    private Instant at;

    @Column(name = "actor", length = 200)
    private String actor;

    @Column(name = "action", length = 32, nullable = false)
    private String action;

    /** The SSE event this change announced ({@code situation.updated} …), or null when none. */
    @Column(name = "event", length = 32)
    private String event;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before")
    private Map<String, Object> before;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after")
    private Map<String, Object> after;

    /** JPA. */
    protected SituationLogEntity() {}

    /** Creates a log entry. */
    public SituationLogEntity(String situationId, Instant at, String actor, String action) {
        this.situationId = situationId;
        this.at = at;
        this.actor = actor;
        this.action = action;
    }

    /** The id (the SSE cursor). */
    public Long getId() {
        return id;
    }

    /** The situation. */
    public String getSituationId() {
        return situationId;
    }

    /** When it happened. */
    public Instant getAt() {
        return at;
    }

    /** Who did it. */
    public String getActor() {
        return actor;
    }

    /** What was done. */
    public String getAction() {
        return action;
    }

    /** The SSE event announced, or null. */
    public String getEvent() {
        return event;
    }

    /** Sets the SSE event announced. */
    public void setEvent(String event) {
        this.event = event;
    }

    /** The state before, or null. */
    public Map<String, Object> getBefore() {
        return before;
    }

    /** Sets the state before. */
    public void setBefore(Map<String, Object> before) {
        this.before = before;
    }

    /** The state after, or null. */
    public Map<String, Object> getAfter() {
        return after;
    }

    /** Sets the state after. */
    public void setAfter(Map<String, Object> after) {
        this.after = after;
    }
}
