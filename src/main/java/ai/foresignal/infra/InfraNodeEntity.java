package ai.foresignal.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * One infrastructure facility (prompt 104): a view of a stored record (094 dataset or OSM), never
 * edited by hand. Typed by the {@code facility_type} taxonomy.
 */
@Entity
@Table(name = "infra_node")
public class InfraNodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "build_id", nullable = false)
    private long buildId;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    @Column(name = "facility_type", length = 32, nullable = false)
    private String facilityType;

    @Column(name = "name", length = 512)
    private String name;

    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    @Column(name = "point", columnDefinition = "geometry(Point,4326)")
    private Point point;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes")
    private Map<String, Object> attributes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "region_tags", nullable = false, columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> regionTags = new java.util.ArrayList<>();

    @Column(name = "licence", length = 128)
    private String licence;

    @Column(name = "built_at", nullable = false)
    private Instant builtAt;

    /** JPA. */
    protected InfraNodeEntity() {}

    /** Creates a node. */
    public InfraNodeEntity(long buildId, String sourceId, String externalId, String facilityType, Instant builtAt) {
        this.buildId = buildId;
        this.sourceId = sourceId;
        this.externalId = externalId;
        this.facilityType = facilityType;
        this.builtAt = builtAt;
    }

    public Long getId() {
        return id;
    }

    public long getBuildId() {
        return buildId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getFacilityType() {
        return facilityType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public List<String> getRegionTags() {
        return regionTags;
    }

    public void setRegionTags(List<String> regionTags) {
        this.regionTags = regionTags == null ? new java.util.ArrayList<>() : regionTags;
    }

    public String getLicence() {
        return licence;
    }

    public void setLicence(String licence) {
        this.licence = licence;
    }

    public Instant getBuiltAt() {
        return builtAt;
    }
}
