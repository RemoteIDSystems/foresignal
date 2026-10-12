package ai.foresignal.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A GeoNames first-level region name (table {@code gazetteer_admin1}, prompt 040, from
 * {@code admin1CodesASCII.txt}; CC BY 4.0). {@code code} is {@code <ISO2>.<admin1 code>}, for example
 * {@code US.TX}.
 */
@Entity
@Table(name = "gazetteer_admin1")
public class GazetteerAdmin1Entity {

    @Id
    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "ascii_name", length = 200)
    private String asciiName;

    @Column(name = "geonameid")
    private Long geonameid;

    /** For JPA. */
    protected GazetteerAdmin1Entity() {}

    /** The code. */
    public String getCode() {
        return code;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** The ASCII name, or null. */
    public String getAsciiName() {
        return asciiName;
    }

    /** The GeoNames id, or null. */
    public Long getGeonameid() {
        return geonameid;
    }
}
