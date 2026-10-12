package ai.foresignal.raster;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Polygon;

/**
 * One native grid (table {@code raster_grid}, Weather-Imagery-Design §2.3). The id is the SHA-256
 * of the canonical definition JSON, so frames on the same grid share a row ({@link GridStore}).
 */
@Entity
@Table(name = "raster_grid")
public class RasterGridEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "type", length = 16, nullable = false)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "definition", nullable = false)
    private Map<String, Object> definition = new LinkedHashMap<>();

    @Column(name = "nx", nullable = false)
    private int nx;

    @Column(name = "ny", nullable = false)
    private int ny;

    @Column(name = "bbox", columnDefinition = "geometry(Polygon,4326)", nullable = false)
    private Polygon bbox;

    protected RasterGridEntity() {
        // for JPA
    }

    /** A new grid row. */
    public RasterGridEntity(String id, String type, Map<String, Object> definition, int nx, int ny, Polygon bbox) {
        this.id = id;
        this.type = type;
        this.definition = definition;
        this.nx = nx;
        this.ny = ny;
        this.bbox = bbox;
    }

    /** The definition hash. */
    public String getId() {
        return id;
    }

    /** The grid type. */
    public String getType() {
        return type;
    }

    /** The canonical definition. */
    public Map<String, Object> getDefinition() {
        return definition;
    }

    /** Columns. */
    public int getNx() {
        return nx;
    }

    /** Rows. */
    public int getNy() {
        return ny;
    }

    /** The bounding box. */
    public Polygon getBbox() {
        return bbox;
    }
}
