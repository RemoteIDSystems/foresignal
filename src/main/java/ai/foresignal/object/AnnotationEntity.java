package ai.foresignal.object;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A note or verdict on any object (table {@code annotation}, prompt 129). Verdicts (confirmed, rejected, needs-review,
 * duplicate-of) are human validation: they feed precision metrics ({@link PrecisionService}) and benchmark-case
 * proposals. {@code target_component} and {@code target_key} record, at creation, which producer made the target
 * (a finding's detector, an intel event's recipe, the situation linker), so precision is computed per producer.
 *
 * <p>Prompt 130 makes notes comment threads: {@code parent_id} names the root of a reply, {@code mentions} holds the
 * handles parsed from {@code @handle} that were notified, {@code resolved} closes a thread and {@code edited_at}
 * records the last edit of the body.
 */
@Entity
@Table(name = "annotation", indexes = {
        @Index(name = "annotation_target_idx", columnList = "target_ref, created_at"),
        @Index(name = "annotation_verdict_idx", columnList = "kind, target_component, created_at"),
        @Index(name = "annotation_parent_idx", columnList = "parent_id, created_at")})
public class AnnotationEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "target_ref", length = 1024, nullable = false)
    private String targetRef;

    @Column(name = "kind", length = 16, nullable = false)
    private String kind;

    @Column(name = "verdict", length = 16)
    private String verdict;

    @Column(name = "duplicate_of_ref", length = 1024)
    private String duplicateOfRef;

    @Column(name = "body", columnDefinition = "text")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", nullable = false)
    private List<String> tags = new ArrayList<>();

    @Column(name = "author", length = 200, nullable = false)
    private String author;

    @Column(name = "private", nullable = false)
    private boolean privateAnnotation;

    @Column(name = "status_applied", nullable = false)
    private boolean statusApplied;

    @Column(name = "target_component", length = 32)
    private String targetComponent;

    @Column(name = "target_key", length = 300)
    private String targetKey;

    @Column(name = "precursor_key", length = 300)
    private String precursorKey;

    @Column(name = "parent_id", length = 26)
    private String parentId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mentions")
    private List<String> mentions = new ArrayList<>();

    @Column(name = "resolved", nullable = false, columnDefinition = "boolean default false")
    private boolean resolved;

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** For JPA. */
    protected AnnotationEntity() {}

    /**
     * A new annotation.
     *
     * @param id its ULID
     * @param targetRef the annotated object
     * @param kind {@code note} or {@code verdict}
     * @param author the author's principal id
     * @param now the creation time
     */
    public AnnotationEntity(String id, String targetRef, String kind, String author, Instant now) {
        this.id = id;
        this.targetRef = targetRef;
        this.kind = kind;
        this.author = author;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The annotated object. */
    public String getTargetRef() {
        return targetRef;
    }

    /** {@code note} or {@code verdict}. */
    public String getKind() {
        return kind;
    }

    /** The verdict, or null for a note. */
    public String getVerdict() {
        return verdict;
    }

    /** Sets the verdict. */
    public void setVerdict(String value) {
        this.verdict = value;
    }

    /** The original of a {@code duplicate-of} verdict, or null. */
    public String getDuplicateOfRef() {
        return duplicateOfRef;
    }

    /** Sets the original of a duplicate. */
    public void setDuplicateOfRef(String value) {
        this.duplicateOfRef = value;
    }

    /** The markdown body, or null. */
    public String getBody() {
        return body;
    }

    /** Sets the body. */
    public void setBody(String value) {
        this.body = value;
    }

    /** Free tags. */
    public List<String> getTags() {
        return tags;
    }

    /** Sets the tags. */
    public void setTags(List<String> value) {
        this.tags = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The author's principal id. */
    public String getAuthor() {
        return author;
    }

    /** True when the annotation (or its target) is private. */
    public boolean isPrivateAnnotation() {
        return privateAnnotation;
    }

    /** Sets the private flag. */
    public void setPrivateAnnotation(boolean value) {
        this.privateAnnotation = value;
    }

    /** True when a rejected verdict with {@code applyStatus} dismissed the target. */
    public boolean isStatusApplied() {
        return statusApplied;
    }

    /** Records that the verdict dismissed its target. */
    public void setStatusApplied(boolean value) {
        this.statusApplied = value;
    }

    /** The producer class of the target ({@code finding}, {@code intel-event}, {@code situation}), or null. */
    public String getTargetComponent() {
        return targetComponent;
    }

    /** Sets the producer class. */
    public void setTargetComponent(String value) {
        this.targetComponent = value;
    }

    /** The producer of the target (detector, recipe or linker id and version), or null. */
    public String getTargetKey() {
        return targetKey;
    }

    /** Sets the producer. */
    public void setTargetKey(String value) {
        this.targetKey = value;
    }

    /** The precursor of a precursor-active finding, or null. */
    public String getPrecursorKey() {
        return precursorKey;
    }

    /** Sets the precursor. */
    public void setPrecursorKey(String value) {
        this.precursorKey = value;
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

    /** When it was deleted, or null. */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /** Soft-deletes it. */
    public void setDeletedAt(Instant value) {
        this.deletedAt = value;
    }

    /** The root annotation of a reply, or null for a root. */
    public String getParentId() {
        return parentId;
    }

    /** Makes this annotation a reply under {@code value}. */
    public void setParentId(String value) {
        this.parentId = value;
    }

    /** The handles mentioned and notified. */
    public List<String> getMentions() {
        return mentions == null ? List.of() : mentions;
    }

    /** Sets the mentioned handles. */
    public void setMentions(List<String> value) {
        this.mentions = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** True when the thread (rooted here) is resolved. */
    public boolean isResolved() {
        return resolved;
    }

    /** Resolves or reopens the thread. */
    public void setResolved(boolean value) {
        this.resolved = value;
    }

    /** When the body was last edited, or null. */
    public Instant getEditedAt() {
        return editedAt;
    }

    /** Records an edit. */
    public void setEditedAt(Instant value) {
        this.editedAt = value;
    }
}
