package ai.foresignal.source.adapters.satdump;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A DCP (Data Collection Platform) location (prompt 107): the DCS address to a position, loaded from the
 * operator's {@code dcp_locations.csv}. Hibernate manages the table ({@code ddl-auto=update}).
 */
@Entity
@Table(name = "dcp_location")
public class DcpLocationEntity {

    @Id
    @Column(name = "address", length = 32, nullable = false)
    private String address;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lon")
    private Double lon;

    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "agency", length = 64)
    private String agency;

    @Column(name = "source", length = 64)
    private String source;

    @Column(name = "updated_at")
    private Instant updatedAt;

    /** For Hibernate. */
    protected DcpLocationEntity() {}

    /** Creates a row. */
    public DcpLocationEntity(String address, Double lat, Double lon, String name, String agency, String source,
            Instant updatedAt) {
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.agency = agency;
        this.source = source;
        this.updatedAt = updatedAt;
    }

    public String getAddress() {
        return address;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getName() {
        return name;
    }

    public String getAgency() {
        return agency;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
