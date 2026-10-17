package ai.foresignal.trip;

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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A trip or commute corridor watch (prompt 114, table {@code trip}).
 *
 * <p>The route comes from the client's own router; Foresignal never routes. {@code corridor} is
 * {@code ST_Buffer(route::geography, buffer_m)::geometry}, split at the antimeridian and materialized on every save;
 * its GiST index is created by {@code SpatialIndexInitializer}. {@code active_rule_id} names the ephemeral trigger
 * rule watching the corridor during an occurrence's window.
 */
@Entity
@Table(name = "trip")
public class TripEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "route_place_id", length = 64)
    private String routePlaceId;

    @Column(name = "route", columnDefinition = "geometry(Geometry,4326)", nullable = false)
    private Geometry route;

    @Column(name = "buffer_m", nullable = false)
    private int bufferMeters;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule")
    private Map<String, Object> schedule = new LinkedHashMap<>();

    @Column(name = "expected_duration_seconds", nullable = false)
    private long expectedDurationSeconds;

    @Column(name = "lead_time_seconds", nullable = false)
    private long leadTimeSeconds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hazard_kinds")
    private List<String> hazardKinds = new ArrayList<>();

    @Column(name = "min_severity", length = 16)
    private String minSeverity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channel_ids")
    private List<String> channelIds = new ArrayList<>();

    @Column(name = "anchor", length = 330)
    private String anchor;

    @Column(name = "notify_when_clear", nullable = false)
    private boolean notifyWhenClear = true;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "corridor", columnDefinition = "geometry(MultiPolygon,4326)", nullable = false)
    private MultiPolygon corridor;

    @Column(name = "active_rule_id")
    private UUID activeRuleId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "active_occurrence")
    private Instant activeOccurrence;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "active_until")
    private Instant activeUntil;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected TripEntity() {
        // for JPA
    }

    /** Creates a row with its id. */
    public TripEntity(String id) {
        this.id = id;
    }

    /** The ULID id. */
    public String getId() {
        return id;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** Sets: the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** The 036 route place the route came from, or null for an inline route. */
    public String getRoutePlaceId() {
        return routePlaceId;
    }

    /** Sets: the 036 route place the route came from, or null for an inline route. */
    public void setRoutePlaceId(String routePlaceId) {
        this.routePlaceId = routePlaceId;
    }

    /** The route LineString as given (or copied from the route place). */
    public Geometry getRoute() {
        return route;
    }

    /** Sets: the route LineString as given (or copied from the route place). */
    public void setRoute(Geometry route) {
        this.route = route;
    }

    /** The corridor half-width in meters. */
    public int getBufferMeters() {
        return bufferMeters;
    }

    /** Sets: the corridor half-width in meters. */
    public void setBufferMeters(int bufferMeters) {
        this.bufferMeters = bufferMeters;
    }

    /** The stored {@code TripSchedule}. */
    public Map<String, Object> getSchedule() {
        return schedule;
    }

    /** Sets: the stored {@code TripSchedule}. */
    public void setSchedule(Map<String, Object> schedule) {
        this.schedule = schedule;
    }

    /** The expected duration in seconds. */
    public long getExpectedDurationSeconds() {
        return expectedDurationSeconds;
    }

    /** Sets: the expected duration in seconds. */
    public void setExpectedDurationSeconds(long expectedDurationSeconds) {
        this.expectedDurationSeconds = expectedDurationSeconds;
    }

    /** The pre-trip lead time in seconds. */
    public long getLeadTimeSeconds() {
        return leadTimeSeconds;
    }

    /** Sets: the pre-trip lead time in seconds. */
    public void setLeadTimeSeconds(long leadTimeSeconds) {
        this.leadTimeSeconds = leadTimeSeconds;
    }

    /** The hazard kinds watched. */
    public List<String> getHazardKinds() {
        return hazardKinds;
    }

    /** Sets: the hazard kinds watched. */
    public void setHazardKinds(List<String> hazardKinds) {
        this.hazardKinds = hazardKinds;
    }

    /** The lowest hazard severity reported, or null for every severity. */
    public String getMinSeverity() {
        return minSeverity;
    }

    /** Sets: the lowest hazard severity reported, or null for every severity. */
    public void setMinSeverity(String minSeverity) {
        this.minSeverity = minSeverity;
    }

    /** The notification channels of the briefing (empty: every channel subscribed to briefings). */
    public List<String> getChannelIds() {
        return channelIds;
    }

    /** Sets: the notification channels of the briefing (empty: every channel subscribed to briefings). */
    public void setChannelIds(List<String> channelIds) {
        this.channelIds = channelIds;
    }

    /** The moving-anchor record {@code {sourceId}/{externalId}}, or null. */
    public String getAnchor() {
        return anchor;
    }

    /** Sets: the moving-anchor record {@code {sourceId}/{externalId}}, or null. */
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    /** True when a "route clear" briefing is delivered too. */
    public boolean isNotifyWhenClear() {
        return notifyWhenClear;
    }

    /** Sets: true when a "route clear" briefing is delivered too. */
    public void setNotifyWhenClear(boolean notifyWhenClear) {
        this.notifyWhenClear = notifyWhenClear;
    }

    /** True when the trip is watched. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets: true when the trip is watched. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** The materialized corridor. */
    public MultiPolygon getCorridor() {
        return corridor;
    }

    /** Sets: the materialized corridor. */
    public void setCorridor(MultiPolygon corridor) {
        this.corridor = corridor;
    }

    /** The ephemeral in-trip rule, or null. */
    public UUID getActiveRuleId() {
        return activeRuleId;
    }

    /** Sets: the ephemeral in-trip rule, or null. */
    public void setActiveRuleId(UUID activeRuleId) {
        this.activeRuleId = activeRuleId;
    }

    /** The occurrence the ephemeral rule watches, or null. */
    public Instant getActiveOccurrence() {
        return activeOccurrence;
    }

    /** Sets: the occurrence the ephemeral rule watches, or null. */
    public void setActiveOccurrence(Instant activeOccurrence) {
        this.activeOccurrence = activeOccurrence;
    }

    /** The end of the ephemeral rule's window, or null. */
    public Instant getActiveUntil() {
        return activeUntil;
    }

    /** Sets: the end of the ephemeral rule's window, or null. */
    public void setActiveUntil(Instant activeUntil) {
        this.activeUntil = activeUntil;
    }

    /** Who created the trip. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets: who created the trip. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** When created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets: when created. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When last saved. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets: when last saved. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
