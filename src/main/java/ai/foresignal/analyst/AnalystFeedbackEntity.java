package ai.foresignal.analyst;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Operator feedback on a note or proposal (prompt 102). */
@Entity
@Table(name = "analyst_feedback")
public class AnalystFeedbackEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "target", length = 16, nullable = false)
    private String target;

    @Column(name = "target_id", length = 26, nullable = false)
    private String targetId;

    @Column(name = "rating", length = 16, nullable = false)
    private String rating;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @Column(name = "by", length = 128, nullable = false)
    private String by;

    @Column(name = "at", nullable = false)
    private Instant at;

    /** JPA. */
    protected AnalystFeedbackEntity() {}

    /** Creates feedback. */
    public AnalystFeedbackEntity(String id, String target, String targetId, String rating, String by, Instant at) {
        this.id = id;
        this.target = target;
        this.targetId = targetId;
        this.rating = rating;
        this.by = by;
        this.at = at;
    }

    public String getId() {
        return id;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBy() {
        return by;
    }

    public Instant getAt() {
        return at;
    }
}
