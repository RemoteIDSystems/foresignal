package ai.foresignal.watch;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One watchlist item (prompt 036, table {@code watch_list_item}): a normalized value, unique per
 * list. Flyway creates the table and Hibernate validates it; {@link WatchlistService} reads and writes the rows with native SQL in
 * bulk.
 */
@Entity
@Table(name = "watch_list_item")
public class WatchListItemEntity {

    /** The primary key: list and normalized value. */
    @Embeddable
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "list_id", nullable = false)
        private UUID listId;

        @Column(name = "value", length = 256, nullable = false)
        private String value;

        protected Key() {
            // for JPA
        }

        /** A key. */
        public Key(UUID listId, String value) {
            this.listId = listId;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(listId, k.listId) && Objects.equals(value, k.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listId, value);
        }
    }

    @EmbeddedId
    private Key id;

    @Column(name = "label", length = 200)
    private String label;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    /** {@code manual}, {@code csv} or {@code json}. */
    @Column(name = "origin", length = 8, nullable = false)
    private String origin;

    protected WatchListItemEntity() {
        // for JPA
    }

    /** The key. */
    public Key getId() {
        return id;
    }

    /** The label, or null. */
    public String getLabel() {
        return label;
    }

    /** The notes, or null. */
    public String getNotes() {
        return notes;
    }

    /** When added. */
    public Instant getAddedAt() {
        return addedAt;
    }

    /** Where it came from. */
    public String getOrigin() {
        return origin;
    }
}
