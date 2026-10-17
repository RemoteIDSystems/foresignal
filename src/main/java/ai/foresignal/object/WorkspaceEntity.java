package ai.foresignal.object;

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
 * A workspace (table {@code workspace}, prompt 129): a shared investigation focus with a small, versioned
 * {@code context} (docs/Client-Contract.md §5) synchronized to every connected client, and an opaque client
 * {@code layout} that Foresignal stores but never interprets.
 */
@Entity
@Table(name = "workspace", indexes = @Index(name = "workspace_owner_idx", columnList = "owner, id"))
public class WorkspaceEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "owner", length = 200, nullable = false)
    private String owner;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "members", nullable = false)
    private Map<String, String> members = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context", nullable = false)
    private Map<String, Object> context = new LinkedHashMap<>();

    @Column(name = "context_version", nullable = false)
    private long contextVersion = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout", nullable = false)
    private Map<String, Object> layout = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "saved_view_ids", nullable = false)
    private List<String> savedViewIds = new ArrayList<>();

    @Column(name = "investigation_id", length = 64)
    private String investigationId;

    @Column(name = "private", nullable = false)
    private boolean privateWorkspace;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected WorkspaceEntity() {}

    /**
     * A new workspace at context version 1.
     *
     * @param id its ULID
     * @param name its name
     * @param owner the owner's principal id
     * @param now the creation time
     */
    public WorkspaceEntity(String id, String name, String owner, Instant now) {
        this.id = id;
        this.name = name;
        this.owner = owner;
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

    /** The owner's principal id. */
    public String getOwner() {
        return owner;
    }

    /** Members: principal id → {@code viewer} or {@code editor}. */
    public Map<String, String> getMembers() {
        return members;
    }

    /** Sets the members. */
    public void setMembers(Map<String, String> value) {
        this.members = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The shared context. */
    public Map<String, Object> getContext() {
        return context;
    }

    /** Sets the shared context. */
    public void setContext(Map<String, Object> value) {
        this.context = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The context version (also the {@code If-Match} value for context patches). */
    public long getContextVersion() {
        return contextVersion;
    }

    /** Sets the context version. */
    public void setContextVersion(long value) {
        this.contextVersion = value;
    }

    /** The opaque client layout. */
    public Map<String, Object> getLayout() {
        return layout;
    }

    /** Sets the layout. */
    public void setLayout(Map<String, Object> value) {
        this.layout = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** Saved views (036) attached to the workspace. */
    public List<String> getSavedViewIds() {
        return savedViewIds;
    }

    /** Sets the saved views. */
    public void setSavedViewIds(List<String> value) {
        this.savedViewIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The linked investigation (101), or null. */
    public String getInvestigationId() {
        return investigationId;
    }

    /** Sets the investigation. */
    public void setInvestigationId(String value) {
        this.investigationId = value;
    }

    /** True for a private workspace. */
    public boolean isPrivateWorkspace() {
        return privateWorkspace;
    }

    /** Sets the private flag. */
    public void setPrivateWorkspace(boolean value) {
        this.privateWorkspace = value;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it was last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the change time. */
    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }
}
