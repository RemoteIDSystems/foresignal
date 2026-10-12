package ai.foresignal.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Point;

/**
 * A populated place from a GeoNames cities dump (table {@code gazetteer_place}, prompt 040; CC BY 4.0).
 * {@code geog} is a {@code geography(Point)} with a GiST index, so the nearest place is a KNN
 * {@code ORDER BY geog <-> :point} in metres.
 */
@Entity
@Table(name = "gazetteer_place")
public class GazetteerPlaceEntity {

    @Id
    @Column(name = "geonameid", nullable = false)
    private Long geonameid;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "ascii_name", length = 200)
    private String asciiName;

    @Column(name = "iso2", length = 2)
    private String iso2;

    @Column(name = "admin1_code", length = 20)
    private String admin1Code;

    @Column(name = "feature_code", length = 10)
    private String featureCode;

    @Column(name = "population")
    private Long population;

    @Column(name = "timezone", length = 40)
    private String timezone;

    @Column(name = "geog", columnDefinition = "geography(Point,4326)", nullable = false)
    private Point geog;

    /** For JPA. */
    protected GazetteerPlaceEntity() {}

    /** The GeoNames id. */
    public Long getGeonameid() {
        return geonameid;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** The ASCII name, or null. */
    public String getAsciiName() {
        return asciiName;
    }

    /** The country, or null. */
    public String getIso2() {
        return iso2;
    }

    /** The GeoNames admin-1 code, or null. */
    public String getAdmin1Code() {
        return admin1Code;
    }

    /** The GeoNames feature code. */
    public String getFeatureCode() {
        return featureCode;
    }

    /** The population, or null. */
    public Long getPopulation() {
        return population;
    }

    /** The IANA timezone, or null. */
    public String getTimezone() {
        return timezone;
    }

    /** The position. */
    public Point getGeog() {
        return geog;
    }
}
