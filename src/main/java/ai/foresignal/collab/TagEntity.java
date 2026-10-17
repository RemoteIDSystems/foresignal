package ai.foresignal.collab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A personal or shared tag (table {@code tag}, prompt 130). A personal tag is visible only to its owner; a shared tag
 * follows its access list (default {@code instance}). Owner, visibility and TLP are in {@code shareable_object}.
 */
@Entity
@Table(name = "tag", indexes = @Index(name = "tag_name_idx", columnList = "namespace, name"))
public class TagEntity {

    /** The personal namespace. */
    public static final String PERSONAL = "personal";

    /** The shared namespace. */
    public static final String SHARED = "shared";

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "namespace", length = 16, nullable = false)
    private String namespace;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "created_by", length = 64, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected TagEntity() {}

    /**
     * A new tag.
     *
     * @param id its ULID
     * @param name its name
     * @param namespace {@code personal} or {@code shared}
     * @param createdBy the creating principal
     * @param now the creation time
     */
    public TagEntity(String id, String name, String namespace, String createdBy, Instant now) {
        this.id = id;
        this.name = name;
        this.namespace = namespace;
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

    /** {@code personal} or {@code shared}. */
    public String getNamespace() {
        return namespace;
    }

    /** True for a personal tag. */
    public boolean isPersonal() {
        return PERSONAL.equals(namespace);
    }

    /** The color {@code #rrggbb}, or null. */
    public String getColor() {
        return color;
    }

    /** Sets the color. */
    public void setColor(String value) {
        this.color = value;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String value) {
        this.description = value;
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
