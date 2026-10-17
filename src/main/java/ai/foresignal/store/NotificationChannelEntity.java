package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One notification destination (Architecture §7.1 and §17, prompt 018).
 *
 * <p>Non-secret settings live in {@code config}; secret settings are one JSON object encrypted with
 * {@code CredentialCipher} into {@code secret_ciphertext} / {@code secret_nonce} and never returned
 * by the API. Prompt 018 adds the timezone, quiet hours, grouping window, backlog cap and the
 * edit-in-place switch to the Architecture columns.
 */
@Entity
@Table(name = "notification_channel")
public class NotificationChannelEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", nullable = false, columnDefinition = "jsonb default '{}'::jsonb")
    private Map<String, Object> config = new LinkedHashMap<>();

    @Column(name = "secret_ciphertext")
    private byte[] secretCiphertext;

    @Column(name = "secret_nonce")
    private byte[] secretNonce;

    /** Subset of {@code alert}, {@code briefing}, {@code system}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "events", nullable = false, columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> events = new ArrayList<>();

    /** Rules feeding the channel; null means every rule. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule_ids")
    private List<UUID> ruleIds;

    @Column(name = "min_severity", nullable = false, length = 16)
    private String minSeverity = "info";

    @Column(name = "title_template", columnDefinition = "text")
    private String titleTemplate;

    @Column(name = "body_template", columnDefinition = "text")
    private String bodyTemplate;

    @Column(name = "wait_for_enrichment_seconds", nullable = false, columnDefinition = "integer default 0")
    private int waitForEnrichmentSeconds;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** IANA zone for quiet hours and {@code |local}; null follows the {@code server} setting. */
    @Column(name = "timezone", length = 64)
    private String timezone;

    /** {@code {start, end, days, bypassSeverity}}, or null for no quiet hours. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quiet_hours")
    private Map<String, Object> quietHours;

    @Column(name = "group_window_seconds", nullable = false, columnDefinition = "integer default 0")
    private int groupWindowSeconds;

    @Column(name = "backlog_cap", nullable = false, columnDefinition = "integer default 20")
    private int backlogCap = 20;

    @Column(name = "edit_in_place", nullable = false, columnDefinition = "boolean default true")
    private boolean editInPlace = true;

    /** Presence deferral of non-critical alerts while a desktop is active (prompt 033), 0–600 s. */
    @Column(name = "defer_while_active_seconds", nullable = false, columnDefinition = "integer default 0")
    private int deferWhileActiveSeconds;

    /** Whether the channel receives exercise alerts (prompt 115); off by default. */
    @Column(name = "accept_exercise", nullable = false, columnDefinition = "boolean default false")
    private boolean acceptExercise;

    /**
     * The principal that owns a personal channel (prompt 130), or null for an instance channel. Personal channels
     * take only that principal's personal notifications, never the instance's alert, briefing or system fan-out.
     */
    @Column(name = "owner_principal_id", length = 64)
    private String ownerPrincipalId;

    protected NotificationChannelEntity() {
        // for JPA
    }

    /** A channel with an id, a name and a type; everything else at its default. */
    public NotificationChannelEntity(UUID id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    /** The type id. */
    public String getType() {
        return type;
    }

    /** Sets the type id. */
    public void setType(String type) {
        this.type = type;
    }

    /** The non-secret settings. */
    public Map<String, Object> getConfig() {
        return config;
    }

    /** Sets the non-secret settings. */
    public void setConfig(Map<String, Object> config) {
        this.config = config == null ? new LinkedHashMap<>() : config;
    }

    /** The encrypted secret settings, or null. */
    public byte[] getSecretCiphertext() {
        return secretCiphertext;
    }

    /** Sets the encrypted secret settings. */
    public void setSecretCiphertext(byte[] secretCiphertext) {
        this.secretCiphertext = secretCiphertext;
    }

    /** The nonce of the secret settings, or null. */
    public byte[] getSecretNonce() {
        return secretNonce;
    }

    /** Sets the nonce of the secret settings. */
    public void setSecretNonce(byte[] secretNonce) {
        this.secretNonce = secretNonce;
    }

    /** The subscribed events. */
    public List<String> getEvents() {
        return events;
    }

    /** Sets the subscribed events. */
    public void setEvents(List<String> events) {
        this.events = events == null ? new ArrayList<>() : events;
    }

    /** The rule filter, or null for every rule. */
    public List<UUID> getRuleIds() {
        return ruleIds;
    }

    /** Sets the rule filter. */
    public void setRuleIds(List<UUID> ruleIds) {
        this.ruleIds = ruleIds;
    }

    /** The minimum severity wire value. */
    public String getMinSeverity() {
        return minSeverity;
    }

    /** Sets the minimum severity wire value. */
    public void setMinSeverity(String minSeverity) {
        this.minSeverity = minSeverity;
    }

    /** The title template, or null. */
    public String getTitleTemplate() {
        return titleTemplate;
    }

    /** Sets the title template. */
    public void setTitleTemplate(String titleTemplate) {
        this.titleTemplate = titleTemplate;
    }

    /** The body template, or null. */
    public String getBodyTemplate() {
        return bodyTemplate;
    }

    /** Sets the body template. */
    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    /** Seconds an alert waits for enrichment. */
    public int getWaitForEnrichmentSeconds() {
        return waitForEnrichmentSeconds;
    }

    /** Sets the enrichment wait. */
    public void setWaitForEnrichmentSeconds(int waitForEnrichmentSeconds) {
        this.waitForEnrichmentSeconds = waitForEnrichmentSeconds;
    }

    /** Whether the channel receives messages. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets whether the channel receives messages. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    /** The IANA timezone, or null. */
    public String getTimezone() {
        return timezone;
    }

    /** Sets the IANA timezone. */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /** The quiet hours as stored, or null. */
    public Map<String, Object> getQuietHours() {
        return quietHours;
    }

    /** Sets the quiet hours. */
    public void setQuietHours(Map<String, Object> quietHours) {
        this.quietHours = quietHours;
    }

    /** The grouping window in seconds; 0 disables grouping. */
    public int getGroupWindowSeconds() {
        return groupWindowSeconds;
    }

    /** Sets the grouping window. */
    public void setGroupWindowSeconds(int groupWindowSeconds) {
        this.groupWindowSeconds = groupWindowSeconds;
    }

    /** Pending sends above this are folded into a digest. */
    public int getBacklogCap() {
        return backlogCap;
    }

    /** Sets the backlog cap. */
    public void setBacklogCap(int backlogCap) {
        this.backlogCap = backlogCap;
    }

    /** Whether lifecycle updates edit the original message where the type allows it. */
    public boolean isEditInPlace() {
        return editInPlace;
    }

    /** Sets the edit-in-place switch. */
    public void setEditInPlace(boolean editInPlace) {
        this.editInPlace = editInPlace;
    }

    /** The presence deferral in seconds (prompt 033); 0 for none. */
    public int getDeferWhileActiveSeconds() {
        return deferWhileActiveSeconds;
    }

    /** Sets the presence deferral. */
    public void setDeferWhileActiveSeconds(int deferWhileActiveSeconds) {
        this.deferWhileActiveSeconds = deferWhileActiveSeconds;
    }

    /** Whether the channel receives exercise alerts (prompt 115). */
    public boolean isAcceptExercise() {
        return acceptExercise;
    }

    /** Sets whether the channel receives exercise alerts (prompt 115). */
    public void setAcceptExercise(boolean acceptExercise) {
        this.acceptExercise = acceptExercise;
    }

    /** The owning principal of a personal channel (prompt 130), or null. */
    public String getOwnerPrincipalId() {
        return ownerPrincipalId;
    }

    /** Makes the channel personal to {@code value}. */
    public void setOwnerPrincipalId(String value) {
        this.ownerPrincipalId = value;
    }
}
