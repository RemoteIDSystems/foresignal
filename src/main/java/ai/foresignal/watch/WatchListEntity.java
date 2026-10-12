package ai.foresignal.watch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A watchlist (prompt 036, table {@code watch_list}); its items are {@link WatchListItemEntity} rows. */
@Entity
@Table(name = "watch_list")
public class WatchListEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    /** {@link Identity#wire()}. */
    @Column(name = "identity", length = 16, nullable = false)
    private String identity;

    /** {@link Identity.Normalize#wire()}. */
    @Column(name = "normalize", length = 8, nullable = false)
    private String normalize;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "item_count", nullable = false, columnDefinition = "integer default 0")
    private int itemCount;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WatchListEntity() {
        // for JPA
    }

    /** A new list. */
    public WatchListEntity(UUID id) {
        this.id = id;
    }

    /** The id. */
    public UUID getId() {
        return id;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** The identity wire value. */
    public String getIdentity() {
        return identity;
    }

    /** Sets the identity. */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /** The normalization wire value. */
    public String getNormalize() {
        return normalize;
    }

    /** Sets the normalization. */
    public void setNormalize(String normalize) {
        this.normalize = normalize;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** The number of items. */
    public int getItemCount() {
        return itemCount;
    }

    /** Sets the number of items. */
    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    /** When created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets when created. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when last changed. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
