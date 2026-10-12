package ai.foresignal.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A country boundary (table {@code ref_country}, prompt 040), loaded from Natural Earth admin-0 by the
 * {@link ReferenceDataLoader}. Features sharing an ISO2 code (France and Clipperton, for example) are
 * merged. {@code geom_simple} is {@code ST_SimplifyPreserveTopology(geom, 0.01)}. Both have GiST indexes
 * ({@code SpatialIndexInitializer}). Written only by the loader's staging swap.
 */
@Entity
@Table(name = "ref_country")
public class RefCountryEntity {

    @Id
    @Column(name = "iso2", length = 2, nullable = false)
    private String iso2;

    @Column(name = "iso3", length = 3)
    private String iso3;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "geom", columnDefinition = "geometry(MultiPolygon,4326)", nullable = false)
    private MultiPolygon geom;

    @Column(name = "geom_simple", columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geomSimple;

    /** For JPA. */
    protected RefCountryEntity() {}

    /** The ISO2 code. */
    public String getIso2() {
        return iso2;
    }

    /** The ISO3 code, or null. */
    public String getIso3() {
        return iso3;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** The boundary. */
    public MultiPolygon getGeom() {
        return geom;
    }

    /** The simplified boundary, or null. */
    public MultiPolygon getGeomSimple() {
        return geomSimple;
    }
}
