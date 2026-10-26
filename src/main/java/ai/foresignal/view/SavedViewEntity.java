package ai.foresignal.view;

import ai.foresignal.store.TriggerCondition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A saved view (prompt 036, table {@code saved_view}): a named record predicate (sources, kinds,
 * places, a bbox and an area, conditions in the 017 grammar with {@code inWatchlist}) with its time
 * axis, expiry choice and field projection, rendered by records, tiles and SSE.
 */
@Entity
@Table(name = "saved_view", uniqueConstraints = @UniqueConstraint(name = "saved_view_slug_key", columnNames = "slug"))
public class SavedViewEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "slug", length = 64, nullable = false)
    private String slug;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "sources", columnDefinition = "text[]")
    private String[] sources = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "kinds", columnDefinition = "text[]")
    private String[] kinds = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "place_ids", columnDefinition = "text[]")
    private String[] placeIds = new String[0];

    /** {@code [minLon, minLat, maxLon, maxLat]}, or null. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bbox")
    private List<Double> bbox;

    @Column(name = "area", columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon area;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions", nullable = false)
    private List<TriggerCondition> conditions = new ArrayList<>();

    @Column(name = "time_field", length = 16, nullable = false)
    private String timeField = "effective";

    @Column(name = "include_expired", nullable = false, columnDefinition = "boolean default false")
    private boolean includeExpired;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fields")
    private List<String> fields = new ArrayList<>();

    @Column(name = "version", nullable = false, columnDefinition = "integer default 1")
    private int version;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** The owning user's principal (prompt R2-012), or null for a shared view. */
    @Column(name = "owner_principal_id", length = 64)
    private String ownerPrincipalId;

    protected SavedViewEntity() {
        // for JPA
    }

    /** A new view. */
    public SavedViewEntity(UUID id) {
        this.id = id;
    }

    /** The id. */
    public UUID getId() {
        return id;
    }

    /** The unique slug. */
    public String getSlug() {
        return slug;
    }

    /** Sets the slug. */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Source ids; empty for all. */
    public List<String> getSources() {
        return sources == null ? List.of() : List.of(sources);
    }

    /** Sets the sources. */
    public void setSources(List<String> sources) {
        this.sources = sources == null ? new String[0] : sources.toArray(String[]::new);
    }

    /** Kinds; empty for all. */
    public List<String> getKinds() {
        return kinds == null ? List.of() : List.of(kinds);
    }

    /** Sets the kinds. */
    public void setKinds(List<String> kinds) {
        this.kinds = kinds == null ? new String[0] : kinds.toArray(String[]::new);
    }

    /** Place ids whose areas bound the view; empty for none. */
    public List<String> getPlaceIds() {
        return placeIds == null ? List.of() : List.of(placeIds);
    }

    /** Sets the place ids. */
    public void setPlaceIds(List<String> placeIds) {
        this.placeIds = placeIds == null ? new String[0] : placeIds.toArray(String[]::new);
    }

    /** The bbox, or null. */
    public List<Double> getBbox() {
        return bbox;
    }

    /** Sets the bbox. */
    public void setBbox(List<Double> bbox) {
        this.bbox = bbox == null ? null : new ArrayList<>(bbox);
    }

    /** The area, or null. */
    public MultiPolygon getArea() {
        return area;
    }

    /** Sets the area. */
    public void setArea(MultiPolygon area) {
        this.area = area;
    }

    /** The conditions. */
    public List<TriggerCondition> getConditions() {
        return conditions == null ? List.of() : conditions;
    }

    /** Sets the conditions. */
    public void setConditions(List<TriggerCondition> conditions) {
        this.conditions = conditions == null ? new ArrayList<>() : new ArrayList<>(conditions);
    }

    /** The time axis. */
    public String getTimeField() {
        return timeField;
    }

    /** Sets the time axis. */
    public void setTimeField(String timeField) {
        this.timeField = timeField;
    }

    /** True to include expired records. */
    public boolean isIncludeExpired() {
        return includeExpired;
    }

    /** Sets whether expired records are included. */
    public void setIncludeExpired(boolean includeExpired) {
        this.includeExpired = includeExpired;
    }

    /** The projection; empty for every field. */
    public List<String> getFields() {
        return fields == null ? List.of() : List.copyOf(fields);
    }

    /** Sets the projection. */
    public void setFields(List<String> fields) {
        this.fields = fields == null ? new ArrayList<>() : new ArrayList<>(fields);
    }

    /** The version, bumped on every save. */
    public int getVersion() {
        return version;
    }

    /** Sets the version. */
    public void setVersion(int version) {
        this.version = version;
    }

    /** When created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets when created. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When last saved. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when last saved. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** The owning user's principal (prompt R2-012), or null for a shared view managed by an admin. */
    public String getOwnerPrincipalId() {
        return ownerPrincipalId;
    }

    /** Sets the owner principal; null makes the view shared. */
    public void setOwnerPrincipalId(String value) {
        this.ownerPrincipalId = value;
    }
}
