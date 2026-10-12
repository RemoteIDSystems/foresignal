package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A rule evaluated against persisted versions (Architecture §8, Core-Data-Model §11, prompt 017).
 *
 * <p>A source that is subscribed but not displayed can still announce itself, which is the point
 * of separating alerting from display. The columns hold the rule exactly as {@code RuleValidator}
 * normalized it: the source and kind filters as arrays, the geofence materialized as a MultiPolygon
 * (lines buffered, {@code near} buffered, fixed and split at the antimeridian), and, for edge rules
 * with a hysteresis band, the shrunk and grown fences the engine compares against.
 */
@Entity
@Table(name = "trigger_rule")
public class TriggerRuleEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /** Source ids the rule watches; empty means any source. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sources", nullable = false, columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> sources = new ArrayList<>();

    /** Kinds the rule watches; empty means any kind. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "kinds", nullable = false, columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> kinds = new ArrayList<>();

    /** Tags copied to every alert of the rule. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", nullable = false, columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> tags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions", nullable = false)
    private List<TriggerCondition> conditions = new ArrayList<>();

    /** Null means the rule is not geographically constrained. */
    @Column(name = "geofence", columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geofence;

    /** The original line of a buffered route geofence, as GeoJSON, for display only. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geofence_route")
    private Map<String, Object> geofenceRoute;

    /** The corridor half-width of a route geofence, in meters. */
    @Column(name = "buffer_meters")
    private Double bufferMeters;

    /** The edge band of an enter/exit geofence or near rule, in meters; 0 disables it. */
    @Column(name = "hysteresis_meters", nullable = false, columnDefinition = "double precision default 0")
    private double hysteresisMeters;

    /** The fence shrunk by the hysteresis band (an enter edge must be inside it). */
    @Column(name = "geofence_inner", columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geofenceInner;

    /** The fence grown by the hysteresis band (an exit edge must be outside it). */
    @Column(name = "geofence_outer", columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geofenceOuter;

    /**
     * The {@code near} clause as given: {@code point} and {@code radiusKm}, {@code placeIds} with
     * {@code radiusKm} and the {@code placeVersions} the fence was materialized from (prompt 036),
     * or a moving {@code anchor}.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "near")
    private Map<String, Object> near;

    /** Where {@code geofence} came from (prompt 036): {@code inline} or {@code near}; null without a fence. */
    @Column(name = "geofence_origin", length = 8)
    private String geofenceOrigin;

    /**
     * A region preset used as the geofence (prompt 040: {@code country}, {@code admin1}, {@code county}
     * code lists), or null. Its geometry is resolved from the reference tables when the rule is compiled,
     * never stored in {@code geofence}.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geofence_ref")
    private Map<String, Object> geofenceRef;

    // The default is declared with @ColumnDefault rather than inside columnDefinition. With
    // "varchar(8) default 'match'" as the definition, Hibernate's schema update saw a type mismatch
    // on every start and issued "alter column mode set data type varchar(8) default 'match'", which
    // PostgreSQL rejects as a syntax error (checkpoint 012).
    @Convert(converter = TriggerModeConverter.class)
    @Column(name = "mode", nullable = false, length = 8)
    @ColumnDefault("'match'")
    private TriggerMode mode = TriggerMode.MATCH;

    /** Fire only for a record's first version, ignoring later revisions. */
    @Column(name = "only_new", nullable = false)
    private boolean onlyNew;

    /** Suppress repeat alerts within this many seconds of {@code observed_at}; 0 disables it. */
    @Column(name = "cooldown_seconds", nullable = false, columnDefinition = "integer default 0")
    private int cooldownSeconds;

    /** {@code record} (per entity) or {@code rule} (per rule). */
    @Column(name = "cooldown_scope", nullable = false, length = 8)
    @ColumnDefault("'record'")
    private String cooldownScope = "record";

    /** {@code info}, {@code warning} or {@code critical}. */
    @Column(name = "severity", nullable = false, length = 16)
    @ColumnDefault("'warning'")
    private String severity = "warning";

    /** Whether prompt 030 enriches the rule's alerts. */
    @Column(name = "enrich", nullable = false, columnDefinition = "boolean default false")
    private boolean enrich;

    /** The enrichment radius (prompt 030). */
    @Column(name = "enrich_radius_km", nullable = false, columnDefinition = "double precision default 100")
    private double enrichRadiusKm = 100;

    /** The enrichment window in seconds (prompt 030). */
    @Column(name = "enrich_window_seconds", nullable = false, columnDefinition = "bigint default 86400")
    private long enrichWindowSeconds = 86_400;

    /**
     * Automatic resolution: {@code onInverseEdge}, {@code onTombstone}, {@code onExpiry} (booleans)
     * and {@code staleAfterSeconds} (number or null). Null means off.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "auto_resolve")
    private Map<String, Object> autoResolve;

    /** The storm cap; null follows {@code triggers.defaultMaxAlertsPerHour}, 0 is unlimited. */
    @Column(name = "max_alerts_per_hour")
    private Integer maxAlertsPerHour;

    @Column(name = "auto_disable_on_storm", nullable = false, columnDefinition = "boolean default false")
    private boolean autoDisableOnStorm;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "active_from")
    private Instant activeFrom;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "active_until")
    private Instant activeUntil;

    /** {@code {timezone, windows: [{days, start, end}]}}, or null for always. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "active_window")
    private Map<String, Object> activeWindow;

    /** Fire only on versions whose {@code valid_from} is within this many seconds; null disables. */
    @Column(name = "forecast_horizon_seconds")
    private Long forecastHorizonSeconds;

    @Column(name = "message_template", columnDefinition = "text")
    private String messageTemplate;

    /**
     * Whether an entity reappearing after a tombstone counts as entering the rule (Core-Data-Model
     * §11). The API defaults it to true for rules with a geofence; the column default is false.
     */
    @Column(name = "reappear_as_enter", nullable = false, columnDefinition = "boolean default false")
    private boolean reappearAsEnter;

    /** Until when the rule's matches are recorded as suppressed ({@code rule_snoozed}). */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "snoozed_until")
    private Instant snoozedUntil;

    /** Until when the rule is muted (prompt 033): alerts are recorded but not routed, escalations stop. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "muted_until")
    private Instant mutedUntil;

    /** The escalation policy of the rule's alerts (prompt 033), or null. */
    @Column(name = "escalation_policy_id")
    private UUID escalationPolicyId;

    /** Matches the storm cap dropped. */
    @Column(name = "suppressed_count", nullable = false, columnDefinition = "bigint default 0")
    private long suppressedCount;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_storm_at")
    private Instant lastStormAt;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_error_at")
    private Instant lastErrorAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TriggerRuleEntity() {
        // for JPA
    }

    /**
     * A new rule with defaults.
     *
     * @param id the rule id
     * @param name the rule name
     */
    public TriggerRuleEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    /** The rule id. */
    public UUID getId() {
        return id;
    }

    /** Sets the rule id. */
    public void setId(UUID id) {
        this.id = id;
    }

    /** The rule name. */
    public String getName() {
        return name;
    }

    /** Sets the rule name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Whether the rule is evaluated. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets whether the rule is evaluated. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** The watched sources; empty means any. */
    public List<String> getSources() {
        return sources;
    }

    /** Sets the watched sources. */
    public void setSources(List<String> sources) {
        this.sources = sources == null ? new ArrayList<>() : new ArrayList<>(sources);
    }

    /** The watched kinds; empty means any. */
    public List<String> getKinds() {
        return kinds;
    }

    /** Sets the watched kinds. */
    public void setKinds(List<String> kinds) {
        this.kinds = kinds == null ? new ArrayList<>() : new ArrayList<>(kinds);
    }

    /** The tags copied to alerts. */
    public List<String> getTags() {
        return tags;
    }

    /** Sets the tags. */
    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    /** The payload conditions, ANDed. */
    public List<TriggerCondition> getConditions() {
        return conditions;
    }

    /** Sets the payload conditions. */
    public void setConditions(List<TriggerCondition> conditions) {
        this.conditions = conditions == null ? new ArrayList<>() : conditions;
    }

    /** The materialized geofence, or null. */
    public MultiPolygon getGeofence() {
        return geofence;
    }

    /** Sets the materialized geofence. */
    public void setGeofence(MultiPolygon geofence) {
        this.geofence = geofence;
    }

    /** The original route line as GeoJSON, or null. */
    public Map<String, Object> getGeofenceRoute() {
        return geofenceRoute;
    }

    /** Sets the original route line. */
    public void setGeofenceRoute(Map<String, Object> geofenceRoute) {
        this.geofenceRoute = geofenceRoute;
    }

    /** The route corridor half-width in meters, or null. */
    public Double getBufferMeters() {
        return bufferMeters;
    }

    /** Sets the route corridor half-width. */
    public void setBufferMeters(Double bufferMeters) {
        this.bufferMeters = bufferMeters;
    }

    /** The hysteresis band in meters. */
    public double getHysteresisMeters() {
        return hysteresisMeters;
    }

    /** Sets the hysteresis band. */
    public void setHysteresisMeters(double hysteresisMeters) {
        this.hysteresisMeters = hysteresisMeters;
    }

    /** The fence shrunk by the band, or null without hysteresis. */
    public MultiPolygon getGeofenceInner() {
        return geofenceInner;
    }

    /** Sets the shrunk fence. */
    public void setGeofenceInner(MultiPolygon geofenceInner) {
        this.geofenceInner = geofenceInner;
    }

    /** The fence grown by the band, or null without hysteresis. */
    public MultiPolygon getGeofenceOuter() {
        return geofenceOuter;
    }

    /** Sets the grown fence. */
    public void setGeofenceOuter(MultiPolygon geofenceOuter) {
        this.geofenceOuter = geofenceOuter;
    }

    /** The near clause as given, or null. */
    public Map<String, Object> getNear() {
        return near;
    }

    /** Sets the near clause. */
    public void setNear(Map<String, Object> near) {
        this.near = near;
    }

    /** The geofence preset (prompt 040), or null. */
    public Map<String, Object> getGeofenceRef() {
        return geofenceRef;
    }

    /** Sets the geofence preset. */
    public void setGeofenceRef(Map<String, Object> geofenceRef) {
        this.geofenceRef = geofenceRef;
    }

    /** {@code inline}, {@code near} or null (prompt 036). */
    public String getGeofenceOrigin() {
        return geofenceOrigin;
    }

    /** Sets where the geofence came from. */
    public void setGeofenceOrigin(String geofenceOrigin) {
        this.geofenceOrigin = geofenceOrigin;
    }

    /** The edge mode. */
    public TriggerMode getMode() {
        return mode;
    }

    /** Sets the edge mode; null means match. */
    public void setMode(TriggerMode mode) {
        this.mode = mode == null ? TriggerMode.MATCH : mode;
    }

    /** Whether only first versions fire. */
    public boolean isOnlyNew() {
        return onlyNew;
    }

    /** Sets whether only first versions fire. */
    public void setOnlyNew(boolean onlyNew) {
        this.onlyNew = onlyNew;
    }

    /** The cooldown in seconds. */
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    /** Sets the cooldown in seconds. */
    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    /** {@code record} or {@code rule}. */
    public String getCooldownScope() {
        return cooldownScope;
    }

    /** Sets the cooldown scope. */
    public void setCooldownScope(String cooldownScope) {
        this.cooldownScope = cooldownScope == null ? "record" : cooldownScope;
    }

    /** The alert severity. */
    public String getSeverity() {
        return severity;
    }

    /** Sets the alert severity. */
    public void setSeverity(String severity) {
        this.severity = severity == null ? "warning" : severity;
    }

    /** Whether alerts are enriched (prompt 030). */
    public boolean isEnrich() {
        return enrich;
    }

    /** Sets whether alerts are enriched. */
    public void setEnrich(boolean enrich) {
        this.enrich = enrich;
    }

    /** The enrichment radius in kilometers. */
    public double getEnrichRadiusKm() {
        return enrichRadiusKm;
    }

    /** Sets the enrichment radius. */
    public void setEnrichRadiusKm(double enrichRadiusKm) {
        this.enrichRadiusKm = enrichRadiusKm;
    }

    /** The enrichment window in seconds. */
    public long getEnrichWindowSeconds() {
        return enrichWindowSeconds;
    }

    /** Sets the enrichment window. */
    public void setEnrichWindowSeconds(long enrichWindowSeconds) {
        this.enrichWindowSeconds = enrichWindowSeconds;
    }

    /** The auto-resolve settings, or null when off. */
    public Map<String, Object> getAutoResolve() {
        return autoResolve;
    }

    /** Sets the auto-resolve settings. */
    public void setAutoResolve(Map<String, Object> autoResolve) {
        this.autoResolve = autoResolve;
    }

    /** The storm cap, or null for the default. */
    public Integer getMaxAlertsPerHour() {
        return maxAlertsPerHour;
    }

    /** Sets the storm cap. */
    public void setMaxAlertsPerHour(Integer maxAlertsPerHour) {
        this.maxAlertsPerHour = maxAlertsPerHour;
    }

    /** Whether a storm disables the rule. */
    public boolean isAutoDisableOnStorm() {
        return autoDisableOnStorm;
    }

    /** Sets whether a storm disables the rule. */
    public void setAutoDisableOnStorm(boolean autoDisableOnStorm) {
        this.autoDisableOnStorm = autoDisableOnStorm;
    }

    /** The start of the active period, or null. */
    public Instant getActiveFrom() {
        return activeFrom;
    }

    /** Sets the start of the active period. */
    public void setActiveFrom(Instant activeFrom) {
        this.activeFrom = activeFrom;
    }

    /** The end of the active period, or null. */
    public Instant getActiveUntil() {
        return activeUntil;
    }

    /** Sets the end of the active period. */
    public void setActiveUntil(Instant activeUntil) {
        this.activeUntil = activeUntil;
    }

    /** The recurring active window, or null. */
    public Map<String, Object> getActiveWindow() {
        return activeWindow;
    }

    /** Sets the recurring active window. */
    public void setActiveWindow(Map<String, Object> activeWindow) {
        this.activeWindow = activeWindow;
    }

    /** The forecast horizon in seconds, or null. */
    public Long getForecastHorizonSeconds() {
        return forecastHorizonSeconds;
    }

    /** Sets the forecast horizon. */
    public void setForecastHorizonSeconds(Long forecastHorizonSeconds) {
        this.forecastHorizonSeconds = forecastHorizonSeconds;
    }

    /** The message template, or null for the default. */
    public String getMessageTemplate() {
        return messageTemplate;
    }

    /** Sets the message template. */
    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    /** Whether a reappearance after a tombstone counts as an enter. */
    public boolean isReappearAsEnter() {
        return reappearAsEnter;
    }

    /** Sets whether a reappearance after a tombstone counts as an enter. */
    public void setReappearAsEnter(boolean reappearAsEnter) {
        this.reappearAsEnter = reappearAsEnter;
    }

    /** The rule snooze expiry, or null. */
    public Instant getSnoozedUntil() {
        return snoozedUntil;
    }

    /** Sets the rule snooze expiry. */
    public void setSnoozedUntil(Instant snoozedUntil) {
        this.snoozedUntil = snoozedUntil;
    }

    /** Matches the storm cap dropped. */
    public long getSuppressedCount() {
        return suppressedCount;
    }

    /** Sets the storm-suppressed count. */
    public void setSuppressedCount(long suppressedCount) {
        this.suppressedCount = suppressedCount;
    }

    /** When the storm cap last dropped a match, or null. */
    public Instant getLastStormAt() {
        return lastStormAt;
    }

    /** Sets when the storm cap last dropped a match. */
    public void setLastStormAt(Instant lastStormAt) {
        this.lastStormAt = lastStormAt;
    }

    /** The last evaluation error, or null. */
    public String getLastError() {
        return lastError;
    }

    /** Sets the last evaluation error. */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /** When the last evaluation error happened, or null. */
    public Instant getLastErrorAt() {
        return lastErrorAt;
    }

    /** Sets when the last evaluation error happened. */
    public void setLastErrorAt(Instant lastErrorAt) {
        this.lastErrorAt = lastErrorAt;
    }

    /** When the rule was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets when the rule was created. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When the rule was last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when the rule was last changed. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Until when the rule is muted (prompt 033), or null. */
    public Instant getMutedUntil() {
        return mutedUntil;
    }

    /** Sets the mute expiry. */
    public void setMutedUntil(Instant mutedUntil) {
        this.mutedUntil = mutedUntil;
    }

    /** True when the rule is muted at {@code now}. */
    public boolean mutedAt(Instant now) {
        return mutedUntil != null && mutedUntil.isAfter(now);
    }

    /** The escalation policy (prompt 033), or null. */
    public UUID getEscalationPolicyId() {
        return escalationPolicyId;
    }

    /** Sets the escalation policy. */
    public void setEscalationPolicyId(UUID escalationPolicyId) {
        this.escalationPolicyId = escalationPolicyId;
    }
}
