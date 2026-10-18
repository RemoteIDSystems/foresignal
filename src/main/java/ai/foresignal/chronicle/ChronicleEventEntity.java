package ai.foresignal.chronicle;

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
 * One item of a chronicle's thread (table {@code chronicle_event}, prompt 131): a notable change (an alert, a finding,
 * a situation change, a hypothesis band change, a forecast resolution, a first appearance, a layer edit, a capture
 * gap, a cap warning or fallback, a status change or a backfill) with a JSON snapshot of the object it is about, so the
 * thread survives the retention of the object's owner. The id is the thread cursor and the {@code chronicle} SSE
 * topic's cursor; rows are never updated.
 */
@Entity
@Table(name = "chronicle_event", indexes = {
    @Index(name = "chronicle_event_chronicle_idx", columnList = "chronicle_id, id"),
    @Index(name = "chronicle_event_at_idx", columnList = "chronicle_id, at")
})
public class ChronicleEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chronicle_id", length = 26, nullable = false)
    private String chronicleId;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "at", nullable = false)
    private Instant at;

    @Column(name = "seq")
    private Long seq;

    @Column(name = "layer_id", length = 26)
    private String layerId;

    @Column(name = "title", length = 512, nullable = false)
    private String title;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "ref", length = 600)
    private String ref;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot")
    private Map<String, Object> snapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gap")
    private Map<String, Object> gap;

    @Column(name = "backfilled", nullable = false)
    private boolean backfilled;

    @Column(name = "private", nullable = false)
    private boolean privateItem;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected ChronicleEventEntity() {}

    /**
     * A new thread item.
     *
     * @param chronicleId the chronicle
     * @param type one of {@link ChronicleVocabulary#ITEM_TYPES}
     * @param at when it happened
     * @param title the title (cut to 512 characters)
     * @param now when it was written
     */
    public ChronicleEventEntity(String chronicleId, String type, Instant at, String title, Instant now) {
        this.chronicleId = chronicleId;
        this.type = type;
        this.at = at;
        this.title = title == null ? type : title.length() > 512 ? title.substring(0, 512) : title;
        this.createdAt = now;
    }

    /** The id (the thread and SSE cursor). */
    public Long getId() {
        return id;
    }

    /** The chronicle. */
    public String getChronicleId() {
        return chronicleId;
    }

    /** The item type. */
    public String getType() {
        return type;
    }

    /** When it happened. */
    public Instant getAt() {
        return at;
    }

    /** The record seq it is about, or null. */
    public Long getSeq() {
        return seq;
    }

    /** Sets the seq. */
    public void setSeq(Long value) {
        this.seq = value;
    }

    /** The layer it is about, or null. */
    public String getLayerId() {
        return layerId;
    }

    /** Sets the layer. */
    public void setLayerId(String value) {
        this.layerId = value;
    }

    /** The title. */
    public String getTitle() {
        return title;
    }

    /** The summary, or null. */
    public String getSummary() {
        return summary;
    }

    /** Sets the summary. */
    public void setSummary(String value) {
        this.summary = value;
    }

    /** The object reference it is about, or null. */
    public String getRef() {
        return ref;
    }

    /** Sets the object reference. */
    public void setRef(String value) {
        this.ref = value;
    }

    /** The snapshot of the object at the time, or null. */
    public Map<String, Object> getSnapshot() {
        return snapshot;
    }

    /** Sets the snapshot. */
    public void setSnapshot(Map<String, Object> value) {
        this.snapshot = value == null ? null : new LinkedHashMap<>(value);
    }

    /** The capture gap ({@code from}, {@code to}, {@code reason}) of a {@code capture-gap} item, or null. */
    public Map<String, Object> getGap() {
        return gap;
    }

    /** Sets the gap. */
    public void setGap(Map<String, Object> value) {
        this.gap = value == null ? null : new LinkedHashMap<>(value);
    }

    /** True for an item written by a backfill. */
    public boolean isBackfilled() {
        return backfilled;
    }

    /** Marks it backfilled. */
    public void setBackfilled(boolean value) {
        this.backfilled = value;
    }

    /** True when it is about a private source's data (EGR-1): shown only to callers with private access. */
    public boolean isPrivateItem() {
        return privateItem;
    }

    /** Marks it private. */
    public void setPrivateItem(boolean value) {
        this.privateItem = value;
    }

    /** When it was written. */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
