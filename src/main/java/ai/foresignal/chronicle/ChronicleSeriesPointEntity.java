package ai.foresignal.chronicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * One captured series point (table {@code chronicle_series_point}, prompt 131): a {@code series:} point copied when
 * its record version is captured, or a {@code rollup:} / {@code rule:} bucket sampled from the 119 reader. A revised
 * point replaces the stored one (newest known value wins). Written by native upsert; unique per
 * {@code (chronicle_id, ref, period_start)}.
 */
@Entity
@Table(name = "chronicle_series_point", uniqueConstraints = @UniqueConstraint(name = "chronicle_series_point_key",
        columnNames = {"chronicle_id", "ref", "period_start"}))
public class ChronicleSeriesPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chronicle_id", length = 26, nullable = false)
    private String chronicleId;

    @Column(name = "layer_id", length = 26, nullable = false)
    private String layerId;

    @Column(name = "ref", length = 600, nullable = false)
    private String ref;

    @Column(name = "period", length = 64)
    private String period;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end")
    private Instant periodEnd;

    @Column(name = "value")
    private Double value;

    @Column(name = "unit", length = 64)
    private String unit;

    @Column(name = "status", length = 16)
    private String status;

    @Column(name = "point_version")
    private Integer pointVersion;

    @Column(name = "known_at")
    private Instant knownAt;

    @Column(name = "coverage_ok")
    private Boolean coverageOk;

    @Column(name = "seq")
    private Long seq;

    @Column(name = "backfilled", nullable = false)
    private boolean backfilled;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    /** For JPA. */
    protected ChronicleSeriesPointEntity() {}

    /** The row id. */
    public Long getId() {
        return id;
    }

    /** The chronicle. */
    public String getChronicleId() {
        return chronicleId;
    }

    /** The capturing layer. */
    public String getLayerId() {
        return layerId;
    }

    /** The series ref. */
    public String getRef() {
        return ref;
    }

    /** The upstream period label, or null. */
    public String getPeriod() {
        return period;
    }

    /** The period start. */
    public Instant getPeriodStart() {
        return periodStart;
    }

    /** The period end, or null. */
    public Instant getPeriodEnd() {
        return periodEnd;
    }

    /** The value, or null. */
    public Double getValue() {
        return value;
    }

    /** The unit, or null. */
    public String getUnit() {
        return unit;
    }

    /** The point status, or null. */
    public String getStatus() {
        return status;
    }

    /** The record version of the point, or null. */
    public Integer getPointVersion() {
        return pointVersion;
    }

    /** When the value became known, or null. */
    public Instant getKnownAt() {
        return knownAt;
    }

    /** Whether the involved sources covered the period, or null. */
    public Boolean getCoverageOk() {
        return coverageOk;
    }

    /** The record seq that supplied it, or null for sampled buckets. */
    public Long getSeq() {
        return seq;
    }

    /** True when written by a backfill. */
    public boolean isBackfilled() {
        return backfilled;
    }

    /** When it was captured. */
    public Instant getCapturedAt() {
        return capturedAt;
    }
}
