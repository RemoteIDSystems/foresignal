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

/**
 * One item pinned into an investigation (prompt 101): an event, alert, record, anomaly, place,
 * briefing run or note, with a snapshot taken when it was added so the evidence survives retention.
 */
@Entity
@Table(name = "investigation_item")
public class InvestigationItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "investigation_id", length = 26, nullable = false)
    private String investigationId;

    @Column(name = "item_type", length = 24, nullable = false)
    private String itemType;

    @Column(name = "ref", length = 256, nullable = false)
    private String ref;

    @Column(name = "seq")
    private Long seq;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot")
    private Map<String, Object> snapshot;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "added_by", length = 128)
    private String addedBy;

    /** JPA. */
    protected InvestigationItemEntity() {}

    /** Creates an item. */
    public InvestigationItemEntity(String investigationId, String itemType, String ref, Instant addedAt) {
        this.investigationId = investigationId;
        this.itemType = itemType;
        this.ref = ref;
        this.addedAt = addedAt;
    }

    public Long getId() {
        return id;
    }

    public String getInvestigationId() {
        return investigationId;
    }

    public String getItemType() {
        return itemType;
    }

    public String getRef() {
        return ref;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public Map<String, Object> getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Map<String, Object> snapshot) {
        this.snapshot = snapshot;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }
}
