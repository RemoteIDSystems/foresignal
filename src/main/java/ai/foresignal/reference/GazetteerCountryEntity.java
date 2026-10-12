package ai.foresignal.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A GeoNames country (table {@code gazetteer_country}, prompt 040, from {@code countryInfo.txt}; CC BY
 * 4.0): the names GeoNames uses, for countries the bundled table does not name.
 */
@Entity
@Table(name = "gazetteer_country")
public class GazetteerCountryEntity {

    @Id
    @Column(name = "iso2", length = 2, nullable = false)
    private String iso2;

    @Column(name = "iso3", length = 3)
    private String iso3;

    @Column(name = "iso_numeric", length = 3)
    private String isoNumeric;

    @Column(name = "fips", length = 2)
    private String fips;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "capital", length = 200)
    private String capital;

    @Column(name = "population")
    private Long population;

    @Column(name = "continent", length = 2)
    private String continent;

    @Column(name = "geonameid")
    private Long geonameid;

    /** For JPA. */
    protected GazetteerCountryEntity() {}

    /** The ISO2 code. */
    public String getIso2() {
        return iso2;
    }

    /** The ISO3 code, or null. */
    public String getIso3() {
        return iso3;
    }

    /** The numeric code, or null. */
    public String getIsoNumeric() {
        return isoNumeric;
    }

    /** The FIPS code, or null. */
    public String getFips() {
        return fips;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** The capital, or null. */
    public String getCapital() {
        return capital;
    }

    /** The population, or null. */
    public Long getPopulation() {
        return population;
    }

    /** The continent code, or null. */
    public String getContinent() {
        return continent;
    }

    /** The GeoNames id, or null. */
    public Long getGeonameid() {
        return geonameid;
    }
}
