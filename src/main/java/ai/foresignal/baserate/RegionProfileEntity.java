package ai.foresignal.baserate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * The reporting density of one region (prompt 121, table {@code region_profile}): its weekly text-reported event count
 * divided by the median of countries of similar population, and its decile among all countries (1 = least reported),
 * used for {@code LOW_REPORTING_DENSITY}.
 */
@Entity
@Table(name = "region_profile")
public class RegionProfileEntity {

    @Id
    @Column(name = "region_code", length = 16, nullable = false)
    private String regionCode;

    @Column(name = "reporting_density", nullable = false)
    private double reportingDensity;

    @Column(name = "density_decile", nullable = false)
    private int densityDecile;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    /** For JPA. */
    protected RegionProfileEntity() {}

    /**
     * Creates a profile.
     *
     * @param regionCode the region
     * @param reportingDensity the density
     * @param densityDecile the decile, 1–10
     * @param computedAt when it was computed
     */
    public RegionProfileEntity(String regionCode, double reportingDensity, int densityDecile, Instant computedAt) {
        this.regionCode = regionCode;
        this.reportingDensity = reportingDensity;
        this.densityDecile = densityDecile;
        this.computedAt = computedAt;
    }

    /** The region. */
    public String getRegionCode() {
        return regionCode;
    }

    /** The density. */
    public double getReportingDensity() {
        return reportingDensity;
    }

    /** The decile, 1–10. */
    public int getDensityDecile() {
        return densityDecile;
    }

    /** When it was computed. */
    public Instant getComputedAt() {
        return computedAt;
    }
}
