package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One persisted {@code NotificationMessage} (prompt 018).
 *
 * <p>Every message is written here before fan-out, including system events no channel subscribes
 * to; prompt 033 builds the notification inbox on this table. A lifecycle update points at the
 * notification it updates through {@code update_of}.
 */
@Entity
@Table(
        name = "notification",
        indexes = {
            @Index(name = "notification_created_idx", columnList = "created_at"),
            @Index(name = "notification_alert_idx", columnList = "alert_id")
        })
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** {@code alert}, {@code briefing}, {@code system} or {@code test}. */
    @Column(name = "event", nullable = false, length = 32)
    private String event;

    /** For example {@code alert.fired} or {@code system.source_failing}. */
    @Column(name = "subtype", nullable = false, length = 64)
    private String subtype;

    @Column(name = "severity", nullable = false, length = 16)
    private String severity;

    @Column(name = "title", nullable = false, columnDefinition = "text")
    private String title;

    @Column(name = "short_title", length = 40)
    private String shortTitle;

    @Column(name = "body_markdown", columnDefinition = "text")
    private String bodyMarkdown;

    @Column(name = "url", columnDefinition = "text")
    private String url;

    /** {@code [{label, url}]}, at most three. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "links")
    private List<Map<String, Object>> links;

    /** The full event data (the alert JSON for alerts). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data")
    private Map<String, Object> data;

    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "briefing_run_id")
    private UUID briefingRunId;

    @Column(name = "rule_id")
    private UUID ruleId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    /** The notification this one updates, or null. */
    @Column(name = "update_of")
    private Long updateOf;

    /** The install id of the instance that produced the message (loop guard). */
    @Column(name = "origin", length = 64)
    private String origin;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA and for building a new row with the setters. */
    public NotificationEntity() {
        // for JPA
    }

    /** The id, null before the insert. */
    public Long getId() {
        return id;
    }

    /** The event. */
    public String getEvent() {
        return event;
    }

    /** Sets the event. */
    public void setEvent(String event) {
        this.event = event;
    }

    /** The subtype. */
    public String getSubtype() {
        return subtype;
    }

    /** Sets the subtype. */
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    /** The severity wire value. */
    public String getSeverity() {
        return severity;
    }

    /** Sets the severity wire value. */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /** The title. */
    public String getTitle() {
        return title;
    }

    /** Sets the title. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** The short title, or null. */
    public String getShortTitle() {
        return shortTitle;
    }

    /** Sets the short title. */
    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    /** The body in Foresignal markdown, or null. */
    public String getBodyMarkdown() {
        return bodyMarkdown;
    }

    /** Sets the body. */
    public void setBodyMarkdown(String bodyMarkdown) {
        this.bodyMarkdown = bodyMarkdown;
    }

    /** The API link, or null. */
    public String getUrl() {
        return url;
    }

    /** Sets the API link. */
    public void setUrl(String url) {
        this.url = url;
    }

    /** The link buttons, or null. */
    public List<Map<String, Object>> getLinks() {
        return links;
    }

    /** Sets the link buttons. */
    public void setLinks(List<Map<String, Object>> links) {
        this.links = links;
    }

    /** The event data, or null. */
    public Map<String, Object> getData() {
        return data;
    }

    /** Sets the event data. */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    /** The alert, or null. */
    public Long getAlertId() {
        return alertId;
    }

    /** Sets the alert. */
    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    /** The briefing run, or null. */
    public UUID getBriefingRunId() {
        return briefingRunId;
    }

    /** Sets the briefing run. */
    public void setBriefingRunId(UUID briefingRunId) {
        this.briefingRunId = briefingRunId;
    }

    /** The rule, or null. */
    public UUID getRuleId() {
        return ruleId;
    }

    /** Sets the rule. */
    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }

    /** The tags, or null. */
    public String[] getTags() {
        return tags;
    }

    /** Sets the tags. */
    public void setTags(String[] tags) {
        this.tags = tags;
    }

    /** The notification this one updates, or null. */
    public Long getUpdateOf() {
        return updateOf;
    }

    /** Sets the notification this one updates. */
    public void setUpdateOf(Long updateOf) {
        this.updateOf = updateOf;
    }

    /** The origin install id. */
    public String getOrigin() {
        return origin;
    }

    /** Sets the origin install id. */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /** When created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets when created. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
