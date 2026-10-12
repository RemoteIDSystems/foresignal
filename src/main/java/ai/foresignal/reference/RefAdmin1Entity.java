package ai.foresignal.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A first-level administrative region (table {@code ref_admin1}, prompt 040) from Natural Earth admin-1.
 * {@code code} is the ISO 3166-2 code ({@code US-TX}), or Natural Earth's {@code adm1_code} where the
 * region has none. {@code gn_code} is the GeoNames admin-1 code ({@code US.TX}, Natural Earth
 * {@code gn_a1_code}), which maps a GeoNames place to its ISO region.
 */
@Entity
@Table(name = "ref_admin1")
public class RefAdmin1Entity {

    @Id
    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @Column(name = "iso2", length = 2)
    private String iso2;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "gn_code", length = 32)
    private String gnCode;

    @Column(name = "geom", columnDefinition = "geometry(MultiPolygon,4326)", nullable = false)
    private MultiPolygon geom;

    @Column(name = "geom_simple", columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geomSimple;

    /** For JPA. */
    protected RefAdmin1Entity() {}

    /** The ISO 3166-2 code (or {@code adm1_code}). */
    public String getCode() {
        return code;
    }

    /** The country. */
    public String getIso2() {
        return iso2;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** The GeoNames admin-1 code, or null. */
    public String getGnCode() {
        return gnCode;
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
