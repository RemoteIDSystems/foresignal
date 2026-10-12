package ai.foresignal.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A US county (table {@code ref_admin2}, prompt 040) from Natural Earth admin-2 counties. {@code fips5} is
 * the five-digit FIPS code; {@code state_code} the state's ISO 3166-2 code ({@code US-WA}).
 */
@Entity
@Table(name = "ref_admin2")
public class RefAdmin2Entity {

    @Id
    @Column(name = "fips5", length = 5, nullable = false)
    private String fips5;

    @Column(name = "state_code", length = 8)
    private String stateCode;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "geom", columnDefinition = "geometry(MultiPolygon,4326)", nullable = false)
    private MultiPolygon geom;

    /** For JPA. */
    protected RefAdmin2Entity() {}

    /** The FIPS5 code. */
    public String getFips5() {
        return fips5;
    }

    /** The state's ISO 3166-2 code, or null. */
    public String getStateCode() {
        return stateCode;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** The boundary. */
    public MultiPolygon getGeom() {
        return geom;
    }
}
