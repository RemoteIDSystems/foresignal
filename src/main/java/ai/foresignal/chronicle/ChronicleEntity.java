package ai.foresignal.chronicle;

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
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A chronicle (table {@code chronicle}, prompt 131): a named monitoring thread whose layers, while it is active,
 * copy what they match into the chronicle's own storage leaf. Owner, visibility and TLP live in the 130 registry
 * ({@code shareable_object}); the row keeps the capture state: the cursor ({@code chronicle.cursor.{id}}), the capture
 * span in seqs and times, the bytes used against the cap, and the archive state.
 *
 * <p>Updates write only the changed columns ({@link DynamicUpdate}): the capture loop moves the cursor, the bytes used
 * and the cap state with native SQL while the API edits metadata, and neither overwrites the other.
 */
@Entity
@DynamicUpdate
@Table(name = "chronicle", indexes = {
    @Index(name = "chronicle_status_idx", columnList = "status"),
    @Index(name = "chronicle_display_idx", columnList = "display_id", unique = true)
})
public class ChronicleEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "display_id", length = 32, nullable = false)
    private String displayId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "status", length = 16, nullable = false)
    private String status = ChronicleVocabulary.PROPOSED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "area")
    private Map<String, Object> area;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "stopped_at")
    private Instant stoppedAt;

    @Column(name = "capture_from_seq")
    private Long captureFromSeq;

    @Column(name = "capture_to_seq")
    private Long captureToSeq;

    @Column(name = "cursor_seq")
    private Long cursorSeq;

    @Column(name = "last_capture_at")
    private Instant lastCaptureAt;

    @Column(name = "retention", length = 32, nullable = false)
    private String retention = "forever";

    @Column(name = "size_cap_bytes", nullable = false)
    private long sizeCapBytes;

    @Column(name = "bytes_used", nullable = false)
    private long bytesUsed;

    @Column(name = "rows_captured", nullable = false)
    private long rowsCaptured;

    @Column(name = "cap_state", length = 16, nullable = false)
    private String capState = ChronicleVocabulary.CAP_OK;

    @Column(name = "workspace_id", length = 26)
    private String workspaceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "situation_ids", nullable = false)
    private List<String> situationIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hypothesis_set_ids", nullable = false)
    private List<String> hypothesisSetIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "investigation_ids", nullable = false)
    private List<String> investigationIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "topic_ids", nullable = false)
    private List<String> topicIds = new ArrayList<>();

    @Column(name = "origin", length = 16, nullable = false)
    private String origin = ChronicleVocabulary.ORIGIN_OPERATOR;

    @Column(name = "proposal_id", length = 26)
    private String proposalId;

    @Column(name = "auto_activated_at")
    private Instant autoActivatedAt;

    @Column(name = "leaf_created", nullable = false)
    private boolean leafCreated;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "archive")
    private Map<String, Object> archive;

    @Column(name = "history_expired_at")
    private Instant historyExpiredAt;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ChronicleEntity() {}

    /**
     * A new proposed chronicle.
     *
     * @param id its ULID
     * @param displayId its display id ({@code CHR-yyyy-nnnn})
     * @param name its name
     * @param sizeCapBytes its size cap
     * @param now the creation time
     */
    public ChronicleEntity(String id, String displayId, String name, long sizeCapBytes, Instant now) {
        this.id = id;
        this.displayId = displayId;
        this.name = name;
        this.sizeCapBytes = sizeCapBytes;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The display id. */
    public String getDisplayId() {
        return displayId;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String value) {
        this.name = value;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String value) {
        this.description = value;
    }

    /** The lifecycle status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String value) {
        this.status = value;
    }

    /** The stored area, or null. */
    public Map<String, Object> getArea() {
        return area;
    }

    /** Sets the area. */
    public void setArea(Map<String, Object> value) {
        this.area = value == null ? null : new LinkedHashMap<>(value);
    }

    /** When capture first started, or null. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets when capture started. */
    public void setStartedAt(Instant value) {
        this.startedAt = value;
    }

    /** When capture stopped, or null. */
    public Instant getStoppedAt() {
        return stoppedAt;
    }

    /** Sets when capture stopped. */
    public void setStoppedAt(Instant value) {
        this.stoppedAt = value;
    }

    /** The first seq capture covers, or null before activation. */
    public Long getCaptureFromSeq() {
        return captureFromSeq;
    }

    /** Sets the first captured seq. */
    public void setCaptureFromSeq(Long value) {
        this.captureFromSeq = value;
    }

    /** The last seq capture covered once stopped, or null. */
    public Long getCaptureToSeq() {
        return captureToSeq;
    }

    /** Sets the last captured seq. */
    public void setCaptureToSeq(Long value) {
        this.captureToSeq = value;
    }

    /** The durable cursor ({@code chronicle.cursor.{id}}): every seq at or below it was processed; null before. */
    public Long getCursorSeq() {
        return cursorSeq;
    }

    /** Sets the cursor. */
    public void setCursorSeq(Long value) {
        this.cursorSeq = value;
    }

    /** When the last capture pass for this chronicle ran, or null. */
    public Instant getLastCaptureAt() {
        return lastCaptureAt;
    }

    /** Sets when the last capture pass ran. */
    public void setLastCaptureAt(Instant value) {
        this.lastCaptureAt = value;
    }

    /** {@code forever} or an ISO-8601 duration kept after stop. */
    public String getRetention() {
        return retention;
    }

    /** Sets the retention. */
    public void setRetention(String value) {
        this.retention = value;
    }

    /** The size cap in bytes. */
    public long getSizeCapBytes() {
        return sizeCapBytes;
    }

    /** Sets the size cap. */
    public void setSizeCapBytes(long value) {
        this.sizeCapBytes = value;
    }

    /** The estimated bytes captured. */
    public long getBytesUsed() {
        return bytesUsed;
    }

    /** Sets the bytes used. */
    public void setBytesUsed(long value) {
        this.bytesUsed = Math.max(0, value);
    }

    /** The rows captured (versions, series points and rollup rows). */
    public long getRowsCaptured() {
        return rowsCaptured;
    }

    /** Sets the rows captured. */
    public void setRowsCaptured(long value) {
        this.rowsCaptured = Math.max(0, value);
    }

    /** {@code ok}, {@code warning} or {@code exceeded}. */
    public String getCapState() {
        return capState;
    }

    /** Sets the cap state. */
    public void setCapState(String value) {
        this.capState = value;
    }

    /** The chronicle's 129 workspace. */
    public String getWorkspaceId() {
        return workspaceId;
    }

    /** Sets the workspace. */
    public void setWorkspaceId(String value) {
        this.workspaceId = value;
    }

    /** Linked situations. */
    public List<String> getSituationIds() {
        return situationIds == null ? List.of() : situationIds;
    }

    /** Sets the linked situations. */
    public void setSituationIds(List<String> value) {
        this.situationIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** Linked hypothesis sets. */
    public List<String> getHypothesisSetIds() {
        return hypothesisSetIds == null ? List.of() : hypothesisSetIds;
    }

    /** Sets the linked hypothesis sets. */
    public void setHypothesisSetIds(List<String> value) {
        this.hypothesisSetIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** Linked investigations. */
    public List<String> getInvestigationIds() {
        return investigationIds == null ? List.of() : investigationIds;
    }

    /** Sets the linked investigations. */
    public void setInvestigationIds(List<String> value) {
        this.investigationIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** Linked 117 attention topics. */
    public List<String> getTopicIds() {
        return topicIds == null ? List.of() : topicIds;
    }

    /** Sets the linked topics. */
    public void setTopicIds(List<String> value) {
        this.topicIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** {@code operator}, {@code agent} or {@code analyst}. */
    public String getOrigin() {
        return origin;
    }

    /** Sets the origin. */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /** The 102 proposal that proposed it, or null. */
    public String getProposalId() {
        return proposalId;
    }

    /** Sets the proposal. */
    public void setProposalId(String value) {
        this.proposalId = value;
    }

    /** When the analysis cycles activated it on their own, or null. */
    public Instant getAutoActivatedAt() {
        return autoActivatedAt;
    }

    /** Sets when it was auto-activated. */
    public void setAutoActivatedAt(Instant value) {
        this.autoActivatedAt = value;
    }

    /** True once its storage leaf exists. */
    public boolean isLeafCreated() {
        return leafCreated;
    }

    /** Records whether the leaf exists. */
    public void setLeafCreated(boolean value) {
        this.leafCreated = value;
    }

    /** The cold-archive state ({@code state}, {@code exportId}, {@code archivedAt}, {@code bytes}, {@code error}). */
    public Map<String, Object> getArchive() {
        return archive;
    }

    /** Sets the archive state. */
    public void setArchive(Map<String, Object> value) {
        this.archive = value == null ? null : new LinkedHashMap<>(value);
    }

    /** When the chronicle's own retention removed its captured history, or null. */
    public Instant getHistoryExpiredAt() {
        return historyExpiredAt;
    }

    /** Sets when the history expired. */
    public void setHistoryExpiredAt(Instant value) {
        this.historyExpiredAt = value;
    }

    /** The actor that created it. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets the creating actor. */
    public void setCreatedBy(String value) {
        this.createdBy = value;
    }

    /** The optimistic-lock version (the ETag). */
    public int getVersion() {
        return version;
    }

    /** Bumps the version and the update time. */
    public void touch(Instant now) {
        this.version++;
        this.updatedAt = now;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
