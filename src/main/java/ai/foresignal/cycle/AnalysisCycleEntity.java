package ai.foresignal.cycle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An analysis cycle (prompt 128, table {@code analysis_cycle}): a cadence, a 5-field cron with an IANA timezone, a
 * scope, an optional 102 analyst profile (model, effort, budgets, {@code allowActions}, owner), the LLM roles to run,
 * the forecaster providers and samples, the question and token caps, the delivery channels and the persisted
 * {@code next_run_at}. {@code running_since} is the one-run-per-cycle claim.
 */
@Entity
@Table(name = "analysis_cycle")
public class AnalysisCycleEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "cadence", length = 16, nullable = false)
    private String cadence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule", nullable = false)
    private Map<String, Object> schedule = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scope", nullable = false)
    private Map<String, Object> scope = new LinkedHashMap<>();

    @Column(name = "analyst_profile_id", length = 26)
    private String analystProfileId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roles", nullable = false)
    private List<String> roles = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "forecaster_providers", nullable = false)
    private List<String> forecasterProviders = new ArrayList<>();

    @Column(name = "samples", nullable = false)
    private int samples = CycleVocabulary.DEFAULT_SAMPLES;

    @Column(name = "max_questions", nullable = false)
    private int maxQuestions;

    @Column(name = "max_tokens_per_cycle", nullable = false)
    private long maxTokensPerCycle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channel_ids", nullable = false)
    private List<String> channelIds = new ArrayList<>();

    @Column(name = "notify_min_priority", length = 8, nullable = false)
    private String notifyMinPriority = "normal";

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "built_in", nullable = false)
    private boolean builtIn;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Column(name = "last_run_id", length = 26)
    private String lastRunId;

    @Column(name = "running_since")
    private Instant runningSince;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected AnalysisCycleEntity() {}

    /** Creates a cycle. */
    public AnalysisCycleEntity(String id, String name, String cadence, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.cadence = cadence;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.maxQuestions = CycleVocabulary.defaultMaxQuestions(cadence);
        this.maxTokensPerCycle = CycleVocabulary.defaultMaxTokens(cadence);
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
    public void setName(String name) {
        this.name = name;
    }

    /** The cadence. */
    public String getCadence() {
        return cadence;
    }

    /** Sets the cadence. */
    public void setCadence(String cadence) {
        this.cadence = cadence;
    }

    /** The schedule ({@code cron}, {@code timezone}). */
    public Map<String, Object> getSchedule() {
        return schedule;
    }

    /** Sets the schedule. */
    public void setSchedule(Map<String, Object> schedule) {
        this.schedule = schedule == null ? new LinkedHashMap<>() : new LinkedHashMap<>(schedule);
    }

    /** The scope ({@code CycleScope}). */
    public Map<String, Object> getScope() {
        return scope;
    }

    /** Sets the scope. */
    public void setScope(Map<String, Object> scope) {
        this.scope = scope == null ? new LinkedHashMap<>() : new LinkedHashMap<>(scope);
    }

    /** The 102 analyst profile, or null. */
    public String getAnalystProfileId() {
        return analystProfileId;
    }

    /** Sets the analyst profile. */
    public void setAnalystProfileId(String analystProfileId) {
        this.analystProfileId = analystProfileId;
    }

    /** The LLM roles. */
    public List<String> getRoles() {
        return roles;
    }

    /** Sets the roles. */
    public void setRoles(List<String> roles) {
        this.roles = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
    }

    /** The forecaster providers (empty: the profile's provider). */
    public List<String> getForecasterProviders() {
        return forecasterProviders;
    }

    /** Sets the forecaster providers. */
    public void setForecasterProviders(List<String> forecasterProviders) {
        this.forecasterProviders = forecasterProviders == null ? new ArrayList<>()
                : new ArrayList<>(forecasterProviders);
    }

    /** Forecaster samples per question. */
    public int getSamples() {
        return samples;
    }

    /** Sets the samples. */
    public void setSamples(int samples) {
        this.samples = samples;
    }

    /** The most questions the forecaster role takes. */
    public int getMaxQuestions() {
        return maxQuestions;
    }

    /** Sets the question cap. */
    public void setMaxQuestions(int maxQuestions) {
        this.maxQuestions = maxQuestions;
    }

    /** The token cap across all roles of one run. */
    public long getMaxTokensPerCycle() {
        return maxTokensPerCycle;
    }

    /** Sets the token cap. */
    public void setMaxTokensPerCycle(long maxTokensPerCycle) {
        this.maxTokensPerCycle = maxTokensPerCycle;
    }

    /** The delivery channels (empty: every channel subscribed to {@code assessment.published}). */
    public List<String> getChannelIds() {
        return channelIds;
    }

    /** Sets the channels. */
    public void setChannelIds(List<String> channelIds) {
        this.channelIds = channelIds == null ? new ArrayList<>() : new ArrayList<>(channelIds);
    }

    /** The lowest priority sent to channels. */
    public String getNotifyMinPriority() {
        return notifyMinPriority;
    }

    /** Sets the minimum priority. */
    public void setNotifyMinPriority(String notifyMinPriority) {
        this.notifyMinPriority = notifyMinPriority;
    }

    /** Whether the cycle is scheduled. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Enables or disables the cycle. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** True for the three defaults created on first start. */
    public boolean isBuiltIn() {
        return builtIn;
    }

    /** Marks a default. */
    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    /** The next scheduled run, or null while disabled. */
    public Instant getNextRunAt() {
        return nextRunAt;
    }

    /** Sets the next run. */
    public void setNextRunAt(Instant nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    /** The last run, or null. */
    public String getLastRunId() {
        return lastRunId;
    }

    /** Sets the last run. */
    public void setLastRunId(String lastRunId) {
        this.lastRunId = lastRunId;
    }

    /** When the running claim was taken, or null. */
    public Instant getRunningSince() {
        return runningSince;
    }

    /** Sets the running claim. */
    public void setRunningSince(Instant runningSince) {
        this.runningSince = runningSince;
    }

    /** Creation time. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Last update. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the last update. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
