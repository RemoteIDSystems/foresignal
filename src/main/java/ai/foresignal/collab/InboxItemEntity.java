package ai.foresignal.collab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One item of a principal's personal inbox (table {@code principal_inbox}, prompt 130): a mention, share, reply,
 * saved-search match or another event addressed to that principal. Its id is the cursor of the {@code me} SSE topic.
 */
@Entity
@Table(name = "principal_inbox", indexes = @Index(name = "principal_inbox_principal_idx",
        columnList = "principal_id, id"))
public class InboxItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "principal_id", length = 64, nullable = false)
    private String principalId;

    @Column(name = "event", length = 64, nullable = false)
    private String event;

    @Column(name = "category", length = 32, nullable = false)
    private String category;

    @Column(name = "severity", length = 16, nullable = false)
    private String severity = "info";

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "text")
    private String body;

    @Column(name = "object_ref", length = 128)
    private String objectRef;

    @Column(name = "actor_id", length = 64)
    private String actorId;

    @Column(name = "tlp", length = 16)
    private String tlp;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "payload")
    private java.util.Map<String, Object> payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    /** For JPA. */
    protected InboxItemEntity() {}

    /**
     * A new item.
     *
     * @param principalId the addressee
     * @param event the event, e.g. {@code mention}
     * @param category the inbox category, e.g. {@code mention}
     * @param title the title
     * @param now the creation time
     */
    public InboxItemEntity(String principalId, String event, String category, String title, Instant now) {
        this.principalId = principalId;
        this.event = event;
        this.category = category;
        this.title = title.length() > 500 ? title.substring(0, 499) + "…" : title;
        this.createdAt = now;
    }

    /** The id (the {@code me} topic cursor). */
    public Long getId() {
        return id;
    }

    /** The addressee. */
    public String getPrincipalId() {
        return principalId;
    }

    /** The event. */
    public String getEvent() {
        return event;
    }

    /** The category. */
    public String getCategory() {
        return category;
    }

    /** The severity. */
    public String getSeverity() {
        return severity;
    }

    /** Sets the severity. */
    public void setSeverity(String value) {
        this.severity = value;
    }

    /** The title. */
    public String getTitle() {
        return title;
    }

    /** The markdown body, or null. */
    public String getBody() {
        return body;
    }

    /** Sets the body. */
    public void setBody(String value) {
        this.body = value;
    }

    /** The object the item is about, or null. */
    public String getObjectRef() {
        return objectRef;
    }

    /** Sets the object. */
    public void setObjectRef(String value) {
        this.objectRef = value;
    }

    /** The acting principal, or null. */
    public String getActorId() {
        return actorId;
    }

    /** Sets the actor. */
    public void setActorId(String value) {
        this.actorId = value;
    }

    /** The TLP label of the object, or null. */
    public String getTlp() {
        return tlp;
    }

    /** Sets the TLP label. */
    public void setTlp(String value) {
        this.tlp = value;
    }

    /** The SSE payload sent on the {@code me} topic. */
    public java.util.Map<String, Object> getPayload() {
        return payload;
    }

    /** Sets the SSE payload. */
    public void setPayload(java.util.Map<String, Object> value) {
        this.payload = value;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it was read, or null. */
    public Instant getReadAt() {
        return readAt;
    }

    /** Marks it read. */
    public void setReadAt(Instant value) {
        this.readAt = value;
    }
}
