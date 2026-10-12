package ai.foresignal.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A sea area (table {@code ref_sea}, prompt 040) from Natural Earth marine polygons; {@code id} is
 * Natural Earth's {@code ne_id}. Marine Regions IHO areas are not offered: their terms ask that products
 * not be re-hosted.
 */
@Entity
@Table(name = "ref_sea")
public class RefSeaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "geom", columnDefinition = "geometry(MultiPolygon,4326)", nullable = false)
    private MultiPolygon geom;

    /** For JPA. */
    protected RefSeaEntity() {}

    /** The Natural Earth id. */
    public Long getId() {
        return id;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** The area. */
    public MultiPolygon getGeom() {
        return geom;
    }
}
