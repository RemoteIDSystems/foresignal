package ai.foresignal.analyst;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A scheduled analyst profile (prompt 102): what to watch, how hard to work, what it may propose and
 * where notes go. The owner token is re-checked at execution time.
 */
@Entity
@Table(name = "analyst_profile")
public class AnalystProfileEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule", nullable = false)
    private Map<String, Object> schedule = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scope")
    private Map<String, Object> scope;

    @Column(name = "lookback_seconds")
    private Integer lookbackSeconds;

    @Column(name = "instructions", columnDefinition = "text")
    private String instructions;

    @Column(name = "provider_id", length = 64)
    private String providerId;

    @Column(name = "effort", length = 8, nullable = false)
    private String effort = "low";

    @Column(name = "max_runs_per_day", nullable = false)
    private int maxRunsPerDay = 48;

    @Column(name = "max_tokens_per_run", nullable = false)
    private int maxTokensPerRun = 60_000;

    @Column(name = "max_tool_calls", nullable = false)
    private int maxToolCalls = 16;

    @Column(name = "max_wall_seconds", nullable = false)
    private int maxWallSeconds = 180;

    @Column(name = "allow_actions", nullable = false)
    private boolean allowActions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_actions", nullable = false, columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> allowedActions = new ArrayList<>();

    @Column(name = "max_auto_actions_per_day", nullable = false)
    private int maxAutoActionsPerDay = 5;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channel_ids", nullable = false, columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> channelIds = new ArrayList<>();

    @Column(name = "notify_min_priority", length = 8, nullable = false)
    private String notifyMinPriority = "normal";

    @Column(name = "owner_token_id", length = 64)
    private String ownerTokenId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cursor")
    private Map<String, Object> cursor;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "running_since")
    private Instant runningSince;

    @Column(name = "last_budget_event_on")
    private LocalDate lastBudgetEventOn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA. */
    protected AnalystProfileEntity() {}

    /** Creates a profile. */
    public AnalystProfileEntity(String id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> getSchedule() {
        return schedule;
    }

    public void setSchedule(Map<String, Object> schedule) {
        this.schedule = schedule;
    }

    public Map<String, Object> getScope() {
        return scope;
    }

    public void setScope(Map<String, Object> scope) {
        this.scope = scope;
    }

    public Integer getLookbackSeconds() {
        return lookbackSeconds;
    }

    public void setLookbackSeconds(Integer lookbackSeconds) {
        this.lookbackSeconds = lookbackSeconds;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getEffort() {
        return effort;
    }

    public void setEffort(String effort) {
        this.effort = effort;
    }

    public int getMaxRunsPerDay() {
        return maxRunsPerDay;
    }

    public void setMaxRunsPerDay(int maxRunsPerDay) {
        this.maxRunsPerDay = maxRunsPerDay;
    }

    public int getMaxTokensPerRun() {
        return maxTokensPerRun;
    }

    public void setMaxTokensPerRun(int maxTokensPerRun) {
        this.maxTokensPerRun = maxTokensPerRun;
    }

    public int getMaxToolCalls() {
        return maxToolCalls;
    }

    public void setMaxToolCalls(int maxToolCalls) {
        this.maxToolCalls = maxToolCalls;
    }

    public int getMaxWallSeconds() {
        return maxWallSeconds;
    }

    public void setMaxWallSeconds(int maxWallSeconds) {
        this.maxWallSeconds = maxWallSeconds;
    }

    public boolean isAllowActions() {
        return allowActions;
    }

    public void setAllowActions(boolean allowActions) {
        this.allowActions = allowActions;
    }

    public List<String> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(List<String> allowedActions) {
        this.allowedActions = allowedActions == null ? new ArrayList<>() : allowedActions;
    }

    public int getMaxAutoActionsPerDay() {
        return maxAutoActionsPerDay;
    }

    public void setMaxAutoActionsPerDay(int maxAutoActionsPerDay) {
        this.maxAutoActionsPerDay = maxAutoActionsPerDay;
    }

    public List<String> getChannelIds() {
        return channelIds;
    }

    public void setChannelIds(List<String> channelIds) {
        this.channelIds = channelIds == null ? new ArrayList<>() : channelIds;
    }

    public String getNotifyMinPriority() {
        return notifyMinPriority;
    }

    public void setNotifyMinPriority(String notifyMinPriority) {
        this.notifyMinPriority = notifyMinPriority;
    }

    public String getOwnerTokenId() {
        return ownerTokenId;
    }

    public void setOwnerTokenId(String ownerTokenId) {
        this.ownerTokenId = ownerTokenId;
    }

    public Map<String, Object> getCursor() {
        return cursor;
    }

    public void setCursor(Map<String, Object> cursor) {
        this.cursor = cursor;
    }

    public Instant getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(Instant nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public Instant getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(Instant lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public Instant getRunningSince() {
        return runningSince;
    }

    public void setRunningSince(Instant runningSince) {
        this.runningSince = runningSince;
    }

    public LocalDate getLastBudgetEventOn() {
        return lastBudgetEventOn;
    }

    public void setLastBudgetEventOn(LocalDate lastBudgetEventOn) {
        this.lastBudgetEventOn = lastBudgetEventOn;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
