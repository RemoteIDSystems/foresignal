package ai.foresignal.hypothesis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A key assumption behind a hypothesis set (prompt 125, table {@code key_assumption}): the text, the linchpin flag, its
 * rationale, what would make it wrong, its status ({@code solid}, {@code caveated}, {@code unsupported},
 * {@code broken}), the linked indicators and when it is next due for review. A broken linchpin flags the set for review
 * and lowers confidence one step.
 */
@Entity
@Table(name = "key_assumption", indexes = @Index(name = "key_assumption_set_idx", columnList = "set_id"))
public class KeyAssumptionEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "set_id", length = 26, nullable = false)
    private String setId;

    @Column(name = "text", length = 2000, nullable = false)
    private String text;

    @Column(name = "linchpin", nullable = false)
    private boolean linchpin;

    @Column(name = "rationale", length = 4000)
    private String rationale;

    @Column(name = "would_be_wrong_if", length = 4000)
    private String wouldBeWrongIf;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "solid";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "indicator_ids", columnDefinition = "jsonb", nullable = false)
    private List<String> indicatorIds = new ArrayList<>();

    @Column(name = "review_due")
    private Instant reviewDue;

    @Column(name = "updated_by", length = 128)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected KeyAssumptionEntity() {}

    /**
     * Creates an assumption.
     *
     * @param id the ULID
     * @param setId the set
     * @param now the creation time
     */
    public KeyAssumptionEntity(String id, String setId, Instant now) {
        this.id = id;
        this.setId = setId;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The set. */
    public String getSetId() {
        return setId;
    }

    /** The text. */
    public String getText() {
        return text;
    }

    /** Sets the text. */
    public void setText(String value) {
        this.text = value;
    }

    /** True for a linchpin assumption. */
    public boolean isLinchpin() {
        return linchpin;
    }

    /** Sets the linchpin flag. */
    public void setLinchpin(boolean value) {
        this.linchpin = value;
    }

    /** The rationale, or null. */
    public String getRationale() {
        return rationale;
    }

    /** Sets the rationale. */
    public void setRationale(String value) {
        this.rationale = value;
    }

    /** What would make it wrong, or null. */
    public String getWouldBeWrongIf() {
        return wouldBeWrongIf;
    }

    /** Sets what would make it wrong. */
    public void setWouldBeWrongIf(String value) {
        this.wouldBeWrongIf = value;
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String value) {
        this.status = value;
    }

    /** True when the assumption is a broken linchpin. */
    public boolean brokenLinchpin() {
        return linchpin && "broken".equals(status);
    }

    /** The linked indicators. */
    public List<String> getIndicatorIds() {
        return indicatorIds;
    }

    /** Sets the linked indicators. */
    public void setIndicatorIds(List<String> value) {
        this.indicatorIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** When it is next due for review, or null. */
    public Instant getReviewDue() {
        return reviewDue;
    }

    /** Sets when it is next due for review. */
    public void setReviewDue(Instant value) {
        this.reviewDue = value;
    }

    /** Who last changed it, or null. */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /** Sets who last changed it. */
    public void setUpdatedBy(String value) {
        this.updatedBy = value;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when it last changed. */
    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }
}
