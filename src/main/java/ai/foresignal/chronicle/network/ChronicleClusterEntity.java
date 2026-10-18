package ai.foresignal.chronicle.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A connected component of medium-or-stronger active chronicle links (table {@code chronicle_cluster}, prompt 134):
 * its members (local chronicle ids and remote nodes). Recomputed by every discovery run; an id is kept when the new
 * component overlaps the old one most.
 */
@Entity
@Table(name = "chronicle_cluster")
public class ChronicleClusterEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "members", nullable = false)
    private List<String> members = new ArrayList<>();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ChronicleClusterEntity() {}

    /**
     * A cluster.
     *
     * @param id its ULID
     * @param members its members
     * @param now when it was computed
     */
    public ChronicleClusterEntity(String id, List<String> members, Instant now) {
        this.id = id;
        this.members = new ArrayList<>(members);
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The members. */
    public List<String> getMembers() {
        return members == null ? List.of() : members;
    }

    /** Sets the members. */
    public void setMembers(List<String> value, Instant now) {
        this.members = value == null ? new ArrayList<>() : new ArrayList<>(value);
        this.updatedAt = now;
    }

    /** When it was last recomputed with a change. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
