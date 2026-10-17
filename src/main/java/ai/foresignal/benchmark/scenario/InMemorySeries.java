package ai.foresignal.benchmark.scenario;

import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.analytics.series.SeriesValue;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.ObservabilityService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A synthetic series held in memory (prompt 123), read the way the 119 {@link SeriesReader} reads a stored one: each
 * bucket keeps every published version with its {@code knownAt}, and {@link #read} returns, per bucket, the newest
 * version known by {@code asOf} (no look-ahead), with the bucket's coverage flag. It stands in for the database-backed
 * reader in the harness's pure mode, so detectors see exactly the {@link SeriesData} shape they see live.
 */
public final class InMemorySeries {

    /**
     * One published value.
     *
     * @param value the value
     * @param knownAt when it became known
     */
    public record Version(double value, Instant knownAt) {}

    private final SeriesRef ref;
    private final SeriesFrequency frequency;
    private final Instant start;
    private final String seriesFamily;
    private final List<List<Version>> versions;
    private final double[] coverage;
    private final double[] detectability;

    /**
     * Creates an empty series.
     *
     * @param ref the reference it is read as (a rollup reference for counts)
     * @param frequency the bucket frequency
     * @param start the first bucket start
     * @param buckets the number of buckets
     * @param seriesFamily the 117 series family ({@code count}, {@code price}, …), or null
     */
    public InMemorySeries(SeriesRef ref, SeriesFrequency frequency, Instant start, int buckets, String seriesFamily) {
        this.ref = ref;
        this.frequency = frequency;
        this.start = frequency.floor(start);
        this.seriesFamily = seriesFamily;
        this.versions = new ArrayList<>(buckets);
        this.coverage = new double[buckets];
        this.detectability = new double[buckets];
        for (int i = 0; i < buckets; i++) {
            versions.add(new ArrayList<>());
            coverage[i] = 1.0;
            detectability[i] = 1.0;
        }
    }

    /** The reference. */
    public SeriesRef ref() {
        return ref;
    }

    /** The frequency. */
    public SeriesFrequency frequency() {
        return frequency;
    }

    /** The number of buckets. */
    public int size() {
        return versions.size();
    }

    /** The start of bucket {@code i}. */
    public Instant startOf(int i) {
        return frequency.plus(start, i);
    }

    /** The end of bucket {@code i}. */
    public Instant endOf(int i) {
        return frequency.plus(start, i + 1);
    }

    /** Publishes (or revises) the value of bucket {@code i}, known from {@code knownAt}. */
    public void publish(int i, double value, Instant knownAt) {
        versions.get(i).add(new Version(value, knownAt));
    }

    /** Sets the coverage ratio of bucket {@code i} (1 by default; below 0.8 the bucket is uncovered). */
    public void coverage(int i, double ratio) {
        coverage[i] = ratio;
    }

    /** Sets the detection probability of bucket {@code i} (1 by default): reporting continues, but thinly. */
    public void detectability(int i, double p) {
        detectability[i] = p;
    }

    /** The detection probability of bucket {@code i}. */
    public double detectabilityOf(int i) {
        return detectability[i];
    }

    /** The coverage ratio of bucket {@code i}. */
    public double coverageOf(int i) {
        return coverage[i];
    }

    /** Every version of bucket {@code i}, in publication order. */
    public List<Version> versions(int i) {
        return List.copyOf(versions.get(i));
    }

    /** The newest value of bucket {@code i} known by {@code asOf} (any time when null), or {@code NaN}. */
    public double valueAsOf(int i, Instant asOf) {
        Version best = null;
        for (Version v : versions.get(i)) {
            if (asOf == null || !v.knownAt().isAfter(asOf)) {
                if (best == null || v.knownAt().isAfter(best.knownAt())) {
                    best = v;
                }
            }
        }
        return best == null ? Double.NaN : best.value();
    }

    /** The bucket index of an instant (may be outside {@code [0, size)}). */
    public int indexOf(Instant t) {
        Instant floor = frequency.floor(t);
        long seconds = floor.getEpochSecond() - start.getEpochSecond();
        return (int) Math.floorDiv(seconds, frequency.approximateWidth().toSeconds());
    }

    /**
     * Reads buckets {@code [from, to)} as the 119 reader does: the newest version per bucket with
     * {@code knownAt ≤ asOf}, a bucket without one or with coverage below {@link SeriesReader#COVERAGE_MIN} is a gap.
     *
     * @param from the first bucket index (clamped to 0)
     * @param to the end index, exclusive (clamped to the size)
     * @param asOf the knowledge cutoff, or null for every version
     * @return the series data
     */
    public SeriesData read(int from, int to, Instant asOf) {
        int a = Math.max(0, from);
        int b = Math.min(size(), to);
        List<SeriesValue> points = new ArrayList<>();
        List<Instant[]> gaps = new ArrayList<>();
        Instant gapStart = null;
        for (int i = a; i < b; i++) {
            Version best = null;
            for (Version v : versions.get(i)) {
                if (asOf == null || !v.knownAt().isAfter(asOf)) {
                    if (best == null || v.knownAt().isAfter(best.knownAt())) {
                        best = v;
                    }
                }
            }
            boolean covered = coverage[i] >= SeriesReader.COVERAGE_MIN;
            if (best != null) {
                points.add(new SeriesValue(startOf(i), endOf(i), best.value(), best.knownAt(), covered, null, null,
                        null));
            }
            boolean gap = best == null || !covered;
            if (gap && gapStart == null) {
                gapStart = startOf(i);
            } else if (!gap && gapStart != null) {
                gaps.add(new Instant[] {gapStart, startOf(i)});
                gapStart = null;
            }
        }
        if (gapStart != null) {
            gaps.add(new Instant[] {gapStart, endOf(b - 1)});
        }
        return new SeriesData(ref, startOf(a), startOf(b), asOf, 0L, frequency, null, null, seriesFamily, null, 0,
                ref.regionCode(), null, Set.of(), false, points, gaps);
    }

    /**
     * Whether a decline over buckets {@code [from, to)} could have been seen: the stand-in for the 116
     * {@code ObservabilityService.canSeeAbsence} in the harness's pure mode, from the planted coverage and detection
     * probability. Not ok when a bucket is uncovered or its detection probability is below the 116 default
     * {@code absenceMinDetect} ({@value ObservabilityService#DEFAULT_ABSENCE_MIN_DETECT}).
     */
    public AbsenceCheck canSeeDecline(int from, int to) {
        double minCoverage = 1.0;
        double minDetect = 1.0;
        for (int i = Math.max(0, from); i < Math.min(size(), to); i++) {
            minCoverage = Math.min(minCoverage, coverage[i]);
            minDetect = Math.min(minDetect, detectability[i]);
        }
        if (minCoverage < SeriesReader.COVERAGE_MIN) {
            return new AbsenceCheck(false, null, "coverage below " + SeriesReader.COVERAGE_MIN);
        }
        return minDetect >= ObservabilityService.DEFAULT_ABSENCE_MIN_DETECT ? new AbsenceCheck(true, minDetect, null)
                : new AbsenceCheck(false, minDetect, AbsenceCheck.LOW_DETECTABILITY);
    }
}
