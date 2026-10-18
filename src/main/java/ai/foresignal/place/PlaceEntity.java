package ai.foresignal.place;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A named place (prompt 036, table {@code place}): a point with a radius, a polygon, or a route with
 * a buffer, generalizing the Weather design's point-only table.
 *
 * <p>{@code area} is derived on every save ({@link PlaceGeometry}): the geography buffer of a point or
 * route, the fixed polygon itself, split at the antimeridian, at most 10,000 vertices. It is what
 * rules, views, filters and the weather AOI use. A GiST index on it is in the Flyway
 * baseline.
 */
@Entity
@Table(name = "place")
public class PlaceEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    /** {@code point}, {@code polygon} or {@code route}. */
    @Column(name = "shape", length = 8, nullable = false)
    private String shape;

    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)", nullable = false)
    private Geometry geom;

    @Column(name = "radius_km")
    private Double radiusKm;

    @Column(name = "buffer_m")
    private Double bufferM;

    @Column(name = "area", columnDefinition = "geometry(MultiPolygon,4326)", nullable = false)
    private MultiPolygon area;

    @Column(name = "timezone", length = 64)
    private String timezone;

    /** {@code user}, {@code airport} or {@code asset}. */
    @Column(name = "kind", length = 16, nullable = false)
    private String kind = "user";

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags = new String[0];

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "products")
    private List<String> products = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes")
    private Map<String, Object> attributes = new LinkedHashMap<>();

    @Column(name = "sensitive", nullable = false, columnDefinition = "boolean default true")
    private boolean sensitive = true;

    @Column(name = "version", nullable = false, columnDefinition = "integer default 1")
    private int version;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlaceEntity() {
        // for JPA
    }

    /** A new place with {@code id}. */
    public PlaceEntity(String id) {
        this.id = id;
    }

    /** The slug id. */
    public String getId() {
        return id;
    }

    /** The display name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** {@code point}, {@code polygon} or {@code route}. */
    public String getShape() {
        return shape;
    }

    /** Sets the shape. */
    public void setShape(String shape) {
        this.shape = shape;
    }

    /** The place's own geometry (never returned to read tokens by MCP short output). */
    public Geometry getGeom() {
        return geom;
    }

    /** Sets the geometry. */
    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    /** The radius of a point place in km, or null. */
    public Double getRadiusKm() {
        return radiusKm;
    }

    /** Sets the radius. */
    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }

    /** The buffer of a route place in meters, or null. */
    public Double getBufferM() {
        return bufferM;
    }

    /** Sets the buffer. */
    public void setBufferM(Double bufferM) {
        this.bufferM = bufferM;
    }

    /** The derived area. */
    public MultiPolygon getArea() {
        return area;
    }

    /** Sets the derived area. */
    public void setArea(MultiPolygon area) {
        this.area = area;
    }

    /** The IANA timezone, or null for the server's. */
    public String getTimezone() {
        return timezone;
    }

    /** Sets the timezone. */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /** {@code user}, {@code airport} or {@code asset}. */
    public String getKind() {
        return kind;
    }

    /** Sets the kind. */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /** The tags. */
    public List<String> getTags() {
        return tags == null ? List.of() : List.of(tags);
    }

    /** Sets the tags. */
    public void setTags(List<String> tags) {
        this.tags = tags == null ? new String[0] : tags.toArray(String[]::new);
    }

    /** Weather product ids for place-weather (prompt 052). */
    public List<String> getProducts() {
        return products == null ? List.of() : List.copyOf(products);
    }

    /** Sets the products. */
    public void setProducts(List<String> products) {
        this.products = products == null ? new ArrayList<>() : new ArrayList<>(products);
    }

    /** Free attributes (for example {@code zip}). */
    public Map<String, Object> getAttributes() {
        return attributes == null ? Map.of() : attributes;
    }

    /** Sets the attributes. */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(attributes);
    }

    /** True when the place is sensitive (default). */
    public boolean isSensitive() {
        return sensitive;
    }

    /** Sets the sensitive flag. */
    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
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

    @Override
    public String toString() {
        return "Place[" + id + ", " + shape + ", v" + version + ", tags=" + Arrays.toString(tags) + "]";
    }
}
