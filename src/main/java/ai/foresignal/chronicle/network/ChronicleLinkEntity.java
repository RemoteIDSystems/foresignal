package ai.foresignal.chronicle.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A relationship between two chronicles (table {@code chronicle_link}, prompt 134). {@code a} is always a local
 * chronicle id; {@code b} is a local chronicle id (then {@code a < b}) or {@code remote:{instanceId}/chronicle:{sketchId}}.
 * It keeps the per-key-type scores (Jaccard, containment, shared estimate; exact for local pairs), the dimensions that
 * passed the minimum overlap, the strength band, the temporal relation and capture-span overlap, the situations both
 * local chronicles link, the exact shared key count of a local pair, and whether an owner dismissed it.
 */
@Entity
@Table(name = "chronicle_link", uniqueConstraints = @UniqueConstraint(name = "chronicle_link_pair",
        columnNames = {"a", "b"}), indexes = {
        @Index(name = "chronicle_link_a_idx", columnList = "a"),
        @Index(name = "chronicle_link_b_idx", columnList = "b")})
public class ChronicleLinkEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "a", length = 26, nullable = false)
    private String a;

    @Column(name = "b", length = 300, nullable = false)
    private String b;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scores", nullable = false)
    private Map<String, Object> scores = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dimensions", nullable = false)
    private List<String> dimensions = new ArrayList<>();

    @Column(name = "temporal", length = 16, nullable = false)
    private String temporal = NetworkVocabulary.CONCURRENT;

    @Column(name = "time_overlap", length = 16, nullable = false)
    private String timeOverlap = NetworkVocabulary.OVERLAP_NONE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shared_situations", nullable = false)
    private List<String> sharedSituations = new ArrayList<>();

    @Column(name = "shared_key_count", nullable = false)
    private long sharedKeyCount;

    @Column(name = "strength", length = 8, nullable = false)
    private String strength;

    @Column(name = "salt_id", length = 64, nullable = false)
    private String saltId;

    @Column(name = "first_found", nullable = false)
    private Instant firstFound;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Column(name = "status", length = 16, nullable = false)
    private String status = NetworkVocabulary.ACTIVE;

    @Column(name = "dismissed_by", length = 200)
    private String dismissedBy;

    /** For JPA. */
    protected ChronicleLinkEntity() {}

    /**
     * A new link.
     *
     * @param id its ULID
     * @param a the local chronicle
     * @param b the other chronicle (local or remote node)
     * @param saltId the salt its scores were computed under
     * @param strength the strength band
     * @param now when it was found
     */
    public ChronicleLinkEntity(String id, String a, String b, String saltId, String strength, Instant now) {
        this.id = id;
        this.a = a;
        this.b = b;
        this.saltId = saltId;
        this.strength = strength;
        this.firstFound = now;
        this.lastUpdated = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The local chronicle. */
    public String getA() {
        return a;
    }

    /** The other chronicle: a local id or a remote node. */
    public String getB() {
        return b;
    }

    /** True when {@code b} is a remote sketch. */
    public boolean isRemote() {
        return NetworkVocabulary.isRemote(b);
    }

    /** The per-key-type scores. */
    public Map<String, Object> getScores() {
        return scores == null ? Map.of() : scores;
    }

    /** Sets the scores. */
    public void setScores(Map<String, Object> value) {
        this.scores = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The dimensions that passed the minimum overlap. */
    public List<String> getDimensions() {
        return dimensions == null ? List.of() : dimensions;
    }

    /** Sets the dimensions. */
    public void setDimensions(List<String> value) {
        this.dimensions = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** {@code a-precedes-b}, {@code b-precedes-a} or {@code concurrent}. */
    public String getTemporal() {
        return temporal;
    }

    /** Sets the temporal relation. */
    public void setTemporal(String value) {
        this.temporal = value;
    }

    /** {@code none}, {@code partial} or {@code full}. */
    public String getTimeOverlap() {
        return timeOverlap;
    }

    /** Sets the time overlap. */
    public void setTimeOverlap(String value) {
        this.timeOverlap = value;
    }

    /** The situations both local chronicles link. */
    public List<String> getSharedSituations() {
        return sharedSituations == null ? List.of() : sharedSituations;
    }

    /** Sets the shared situations. */
    public void setSharedSituations(List<String> value) {
        this.sharedSituations = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The exact shared key count of a local pair, or the rounded estimate of a remote one. */
    public long getSharedKeyCount() {
        return sharedKeyCount;
    }

    /** Sets the shared key count. */
    public void setSharedKeyCount(long value) {
        this.sharedKeyCount = Math.max(0, value);
    }

    /** The strength band. */
    public String getStrength() {
        return strength;
    }

    /** Sets the strength band. */
    public void setStrength(String value) {
        this.strength = value;
    }

    /** The salt the scores were computed under. */
    public String getSaltId() {
        return saltId;
    }

    /** Sets the salt. */
    public void setSaltId(String value) {
        this.saltId = value;
    }

    /** When it was first found. */
    public Instant getFirstFound() {
        return firstFound;
    }

    /** When its scores last changed. */
    public Instant getLastUpdated() {
        return lastUpdated;
    }

    /** Sets when it last changed. */
    public void setLastUpdated(Instant value) {
        this.lastUpdated = value;
    }

    /** {@code active} or {@code dismissed}. */
    public String getStatus() {
        return status;
    }

    /** True while no owner dismissed it. */
    public boolean isActive() {
        return NetworkVocabulary.ACTIVE.equals(status);
    }

    /** The principal that dismissed it, or null. */
    public String getDismissedBy() {
        return dismissedBy;
    }

    /** Dismisses the link. */
    public void dismiss(String principalId, Instant now) {
        this.status = NetworkVocabulary.DISMISSED;
        this.dismissedBy = principalId;
        this.lastUpdated = now;
    }

    /** The other side of the link from {@code chronicleId}'s point of view, or null when it is not a side. */
    public String other(String chronicleId) {
        if (a.equals(chronicleId)) {
            return b;
        }
        return b.equals(chronicleId) ? a : null;
    }
}
