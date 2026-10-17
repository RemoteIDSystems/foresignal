package ai.foresignal.observability;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One time-versioned detection estimate (prompt 116, table {@code observability_estimate}): a source's
 * probability of detecting {@code family} in a region or H3 res-4 cell (or everywhere when both are null) and
 * threshold band, estimated over {@code [period_start, period_end]}. Rows are never updated; each run adds
 * new ones, so a retrospective question selects the estimate that was valid then (OBS-2).
 */
@Entity
@Table(name = "observability_estimate", indexes = {
        @Index(name = "observability_estimate_lookup_idx", columnList = "family, source_id, period_end"),
        @Index(name = "observability_estimate_cell_idx", columnList = "family, cell")})
public class ObservabilityEstimateEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "family", length = 64, nullable = false)
    private String family;

    @Column(name = "region_code", length = 16)
    private String regionCode;

    @Column(name = "cell", length = 16)
    private String cell;

    @Column(name = "band", length = 32)
    private String band;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Column(name = "p_detect", nullable = false)
    private double pDetect;

    @Column(name = "lo", nullable = false)
    private double lo;

    @Column(name = "hi", nullable = false)
    private double hi;

    @Column(name = "n", nullable = false)
    private long n;

    @Column(name = "method", length = 32, nullable = false)
    private String method;

    @Column(name = "confidence", length = 16, nullable = false)
    private String confidence;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    /** For JPA. */
    protected ObservabilityEstimateEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }

    /** The source. */
    public String getSourceId() {
        return sourceId;
    }

    /** The family. */
    public String getFamily() {
        return family;
    }

    /** The region code, or null. */
    public String getRegionCode() {
        return regionCode;
    }

    /** The H3 res-4 cell address, or null. */
    public String getCell() {
        return cell;
    }

    /** The threshold band, or null. */
    public String getBand() {
        return band;
    }

    /** The estimation period's start. */
    public Instant getPeriodStart() {
        return periodStart;
    }

    /** The estimation period's end. */
    public Instant getPeriodEnd() {
        return periodEnd;
    }

    /** The detection probability. */
    public double getPDetect() {
        return pDetect;
    }

    /** The interval's lower bound. */
    public double getLo() {
        return lo;
    }

    /** The interval's upper bound. */
    public double getHi() {
        return hi;
    }

    /** The sample size. */
    public long getN() {
        return n;
    }

    /** The method. */
    public String getMethod() {
        return method;
    }

    /** {@code low}, {@code moderate} or {@code high}. */
    public String getConfidence() {
        return confidence;
    }

    /** When computed. */
    public Instant getComputedAt() {
        return computedAt;
    }

    /** The manifest of the run that computed it, or null. */
    public String getManifestId() {
        return manifestId;
    }
}
