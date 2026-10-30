package ai.foresignal.briefing;

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
import java.util.UUID;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One scheduled briefing definition ({@code briefing}, Architecture §7.1 and §22, prompt 029).
 *
 * <p>The schedule is stored as the operator wrote it (5 fields); {@code next_run_at} is persisted on
 * create, update and after every run, so a restart neither replays missed runs nor loses the next
 * one. {@code running_since} marks a run in progress (at most one per briefing, across instances);
 * a mark older than the stale-run limit is treated as abandoned. {@code owner_scope} is the scope of
 * the token that created or last replaced the briefing: an {@code admin}-owned briefing may list
 * records of private sources, any other lists them only as counts. {@code owner_user_id} (NP-003) makes the briefing
 * a user's edition: only that user, or an instance credential, sees and runs it.
 *
 * <p>Updates write only changed columns ({@link DynamicUpdate}), so replacing a definition never
 * overwrites the run mark or last-run columns that a run in progress maintains with plain SQL.
 */
@Entity
@DynamicUpdate
@Table(name = "briefing", indexes = @Index(name = "briefing_next_run_idx", columnList = "enabled, next_run_at"))
public class BriefingEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /** The 5-field cron. */
    @Column(name = "schedule", nullable = false, length = 200)
    private String schedule;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone = "UTC";

    /** {@code BriefingScope}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scope", nullable = false)
    private Map<String, Object> scope = new LinkedHashMap<>();

    /** The lookback as written (for example {@code 24h}), or null for "since the last run". */
    @Column(name = "lookback", length = 32)
    private String lookback;

    @Column(name = "mode", nullable = false, length = 16)
    private String mode = "digest";

    @Column(name = "instructions", columnDefinition = "text")
    private String instructions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channel_ids", nullable = false)
    private List<String> channelIds = new ArrayList<>();

    @Column(name = "max_items_per_kind", nullable = false)
    private int maxItemsPerKind = 5;

    /** The audio option (prompt 141): {@code {voice, format}}, or null for no audio. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "audio", columnDefinition = "jsonb")
    private Map<String, Object> audio;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "last_run_status", length = 16)
    private String lastRunStatus;

    @Column(name = "running_since")
    private Instant runningSince;

    @Column(name = "owner_token_id")
    private UUID ownerTokenId;

    @Column(name = "owner_scope", length = 16)
    private String ownerScope;

    /** The v2.0 user who owns it as an edition (NP-003), or null for an instance briefing. */
    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    /** The BCP 47 language the edition is written and spoken in (NP-003), or null. */
    @Column(name = "output_language", length = 35)
    private String outputLanguage;

    /** The edition's delivery targets (NP-004), as stored {@code DeliveryTarget} objects; null for none. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "delivery_targets", columnDefinition = "jsonb")
    private List<Map<String, Object>> deliveryTargets;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "restriction_snapshot", columnDefinition = "jsonb")
    private ai.foresignal.security.TokenRestrictions restrictionSnapshot;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected BriefingEntity() {}

    /** The audio option (prompt 141): {@code {voice, format}}, or null for no audio. */
    public Map<String, Object> getAudio() {
        return audio;
    }

    /** Sets the audio option (prompt 141). */
    public void setAudio(Map<String, Object> audio) {
        this.audio = audio;
    }

    /** A new briefing with {@code id}. */
    public BriefingEntity(UUID id) {
        this.id = id;
    }

    /**
     * The read restriction of the token that created or last replaced the briefing (prompt 035), or
     * null when it had none; every run stays inside it.
     */
    public ai.foresignal.security.TokenRestrictions getRestrictionSnapshot() {
        return restrictionSnapshot;
    }

    /** Stores the creating token's read restriction (null for none). */
    public void setRestrictionSnapshot(ai.foresignal.security.TokenRestrictions restrictionSnapshot) {
        this.restrictionSnapshot = restrictionSnapshot;
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

    /** True when the scheduler runs it. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets the enabled flag. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** The 5-field cron. */
    public String getSchedule() {
        return schedule;
    }

    /** Sets the 5-field cron. */
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    /** The IANA timezone. */
    public String getTimezone() {
        return timezone;
    }

    /** Sets the timezone. */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /** The scope document. */
    public Map<String, Object> getScope() {
        return scope;
    }

    /** Sets the scope document. */
    public void setScope(Map<String, Object> scope) {
        this.scope = scope == null ? new LinkedHashMap<>() : scope;
    }

    /** The lookback as written, or null. */
    public String getLookback() {
        return lookback;
    }

    /** Sets the lookback. */
    public void setLookback(String lookback) {
        this.lookback = lookback;
    }

    /** {@code digest} or {@code llm}. */
    public String getMode() {
        return mode;
    }

    /** Sets the mode. */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /** The LLM instructions, or null. */
    public String getInstructions() {
        return instructions;
    }

    /** Sets the instructions. */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /** The channel ids (empty: every channel subscribed to {@code briefing}). */
    public List<String> getChannelIds() {
        return channelIds;
    }

    /** Sets the channel ids. */
    public void setChannelIds(List<String> channelIds) {
        this.channelIds = channelIds == null ? new ArrayList<>() : channelIds;
    }

    /** Notable records per kind. */
    public int getMaxItemsPerKind() {
        return maxItemsPerKind;
    }

    /** Sets notable records per kind. */
    public void setMaxItemsPerKind(int maxItemsPerKind) {
        this.maxItemsPerKind = maxItemsPerKind;
    }

    /** The next fire time, or null when disabled. */
    public Instant getNextRunAt() {
        return nextRunAt;
    }

    /** Sets the next fire time. */
    public void setNextRunAt(Instant nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    /** When the last run started, or null. */
    public Instant getLastRunAt() {
        return lastRunAt;
    }

    /** Sets when the last run started. */
    public void setLastRunAt(Instant lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    /** The last run's status, or null. */
    public String getLastRunStatus() {
        return lastRunStatus;
    }

    /** Sets the last run's status. */
    public void setLastRunStatus(String lastRunStatus) {
        this.lastRunStatus = lastRunStatus;
    }

    /** When the run in progress started, or null. */
    public Instant getRunningSince() {
        return runningSince;
    }

    /** Sets the run-in-progress mark. */
    public void setRunningSince(Instant runningSince) {
        this.runningSince = runningSince;
    }

    /** The token that created or last replaced it, or null. */
    public UUID getOwnerTokenId() {
        return ownerTokenId;
    }

    /** Sets the owner token. */
    public void setOwnerTokenId(UUID ownerTokenId) {
        this.ownerTokenId = ownerTokenId;
    }

    /** The owner's scope wire value, or null. */
    public String getOwnerScope() {
        return ownerScope;
    }

    /** Sets the owner's scope. */
    public void setOwnerScope(String ownerScope) {
        this.ownerScope = ownerScope;
    }

    /** The user who owns it as an edition (NP-003), or null for an instance briefing. */
    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    /** Sets the owning user (NP-003). */
    public void setOwnerUserId(UUID ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    /** The edition's output language as written (NP-003), or null. */
    public String getOutputLanguage() {
        return outputLanguage;
    }

    /** Sets the output language (NP-003). */
    public void setOutputLanguage(String outputLanguage) {
        this.outputLanguage = outputLanguage;
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

    /** The edition's delivery targets (NP-004), or null when it has none. */
    public List<Map<String, Object>> getDeliveryTargets() {
        return deliveryTargets;
    }

    /** Sets the edition's delivery targets (NP-004); null or empty for none. */
    public void setDeliveryTargets(List<Map<String, Object>> deliveryTargets) {
        this.deliveryTargets = deliveryTargets;
    }
}
