package ai.foresignal.collab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A named, shareable search (table {@code saved_search}, prompt 130): a {@code SavedSearchQuery} run over object
 * envelopes, whose new matches can reach the owner's inbox at most hourly. {@code last_match_refs} remembers the
 * previous result so only new matches notify. Owner, visibility and TLP are in {@code shareable_object}.
 */
@Entity
@Table(name = "saved_search", indexes = @Index(name = "saved_search_notify_idx",
        columnList = "notify, last_notified_at"))
public class SavedSearchEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "query", nullable = false)
    private Map<String, Object> query = new LinkedHashMap<>();

    @Column(name = "notify", nullable = false)
    private boolean notify;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "last_notified_at")
    private Instant lastNotifiedAt;

    @Column(name = "last_match_count")
    private Integer lastMatchCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "last_match_refs")
    private List<String> lastMatchRefs = new ArrayList<>();

    @Column(name = "created_by", length = 64, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected SavedSearchEntity() {}

    /**
     * A new saved search.
     *
     * @param id its ULID
     * @param name its name
     * @param createdBy the creating principal
     * @param now the creation time
     */
    public SavedSearchEntity(String id, String name, String createdBy, Instant now) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String value) {
        this.name = value;
    }

    /** The {@code SavedSearchQuery}. */
    public Map<String, Object> getQuery() {
        return query;
    }

    /** Sets the query. */
    public void setQuery(Map<String, Object> value) {
        this.query = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** True when new matches notify the owner. */
    public boolean isNotify() {
        return notify;
    }

    /** Turns match notifications on or off. */
    public void setNotify(boolean value) {
        this.notify = value;
    }

    /** When it last ran for notifications, or null. */
    public Instant getLastRunAt() {
        return lastRunAt;
    }

    /** Sets the last run. */
    public void setLastRunAt(Instant value) {
        this.lastRunAt = value;
    }

    /** When it last notified, or null. */
    public Instant getLastNotifiedAt() {
        return lastNotifiedAt;
    }

    /** Sets the last notification. */
    public void setLastNotifiedAt(Instant value) {
        this.lastNotifiedAt = value;
    }

    /** The match count of the last run, or null. */
    public Integer getLastMatchCount() {
        return lastMatchCount;
    }

    /** Sets the last match count. */
    public void setLastMatchCount(Integer value) {
        this.lastMatchCount = value;
    }

    /** The matches of the last run. */
    public List<String> getLastMatchRefs() {
        return lastMatchRefs == null ? List.of() : lastMatchRefs;
    }

    /** Sets the last matches. */
    public void setLastMatchRefs(List<String> value) {
        this.lastMatchRefs = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The creating principal. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the change time. */
    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }
}
