package ai.foresignal.backtest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * The precursor tester (prompt 103, version 1.0.0): does X tend to precede Y within T? It builds the
 * deduplicated antecedent occurrences A and the consequent occurrences C over a range, finds each
 * antecedent's first consequent in {@code [t+minLag, t+maxLag]} satisfying the spatial rule, and
 * reports the hit rate with a Wilson interval, lag statistics and a deterministic circular-shift base
 * rate and permutation p-value. It also runs the reverse test and flags co-occurrence.
 */
public final class PrecursorTester {

    public static final String VERSION = "1.0.0";
    public static final int SHIFTS = 200;
    public static final int MIN_ANTECEDENTS = 10;
    public static final double COVERAGE_GAP_LIMIT = 0.30;

    private PrecursorTester() {}

    /** An occurrence with time and optional place. */
    public record Occurrence(Instant time, Double lon, Double lat, String region, String entity) {}

    /** The spatial rule. */
    public record Spatial(String kind, Double withinKm) {

        public static Spatial none() {
            return new Spatial("none", null);
        }

        public static Spatial sameEntity() {
            return new Spatial("sameEntity", null);
        }

        public static Spatial withinKm(double km) {
            return new Spatial("withinKm", km);
        }

        public static Spatial sameRegion() {
            return new Spatial("sameRegion", null);
        }
    }

    /** The result. */
    public record Result(long nAntecedent, long hits, double hitRate, double[] hitRateInterval, long lagMin,
            long lagP25, long lagMedian, long lagP75, long lagMax, double lagMeanMinutes, double baseRate,
            double lift, double pValue, String verdict, boolean coOccurrence, boolean insufficient, String note) {}

    /** A resolved request. */
    public record Request(List<Occurrence> antecedents, List<Occurrence> consequents, Duration minLag,
            Duration maxLag, Spatial spatial, Duration dedupWindow, Instant rangeStart, Instant rangeEnd,
            double coverageGapFraction, String seedText) {}

    /** Runs the precursor test. */
    public static Result test(Request request) {
        List<Occurrence> a = dedup(sort(request.antecedents()), request.dedupWindow());
        List<Occurrence> c = sort(request.consequents());
        long nA = a.size();
        boolean insufficient = nA < MIN_ANTECEDENTS || request.coverageGapFraction() > COVERAGE_GAP_LIMIT;
        List<Long> lags = new ArrayList<>();
        int hits = 0;
        for (Occurrence antecedent : a) {
            Occurrence match = firstConsequent(antecedent, c, request);
            if (match != null) {
                hits++;
                lags.add(Duration.between(antecedent.time(), match.time()).toMinutes());
            }
        }
        double hitRate = nA == 0 ? 0 : (double) hits / nA;
        long seed = request.seedText().hashCode();
        double[] shifted = new double[SHIFTS];
        int atLeast = 0;
        long span = Math.max(1, Duration.between(request.rangeStart(), request.rangeEnd()).toMinutes());
        long low = request.maxLag().toMinutes();
        long high = Math.max(low + 1, span - low);
        Random random = new Random(seed);
        for (int i = 0; i < SHIFTS; i++) {
            long shift = low + (long) (random.nextDouble() * (high - low));
            int shiftedHits = 0;
            for (Occurrence occurrence : a) {
                Occurrence moved = new Occurrence(occurrence.time().plus(Duration.ofMinutes(shift)), occurrence.lon(),
                        occurrence.lat(), occurrence.region(), occurrence.entity());
                if (firstConsequent(moved, c, request) != null) {
                    shiftedHits++;
                }
            }
            shifted[i] = nA == 0 ? 0 : (double) shiftedHits / nA;
            if (shifted[i] >= hitRate) {
                atLeast++;
            }
        }
        double meanShifted = 0;
        for (double value : shifted) {
            meanShifted += value;
        }
        meanShifted /= SHIFTS;
        double baseRate = meanShifted;
        double lift = meanShifted == 0 ? (hitRate > 0 ? Double.POSITIVE_INFINITY : 0) : hitRate / meanShifted;
        double pValue = (1.0 + atLeast) / (SHIFTS + 1.0);
        boolean coOccurrence = !insufficient && pValue <= 0.05 && lift >= 2.0 && reverseStrong(request, c, a);
        String verdict = verdict(insufficient, hitRate, lift, pValue);
        return new Result(nA, hits, hitRate, Wilson.interval(hits, nA), lags.isEmpty() ? 0 : min(lags),
                percentile(lags, 0.25), percentile(lags, 0.5), percentile(lags, 0.75), lags.isEmpty() ? 0 : max(lags),
                lags.isEmpty() ? 0 : lags.stream().mapToLong(Long::longValue).average().orElse(0), baseRate, lift,
                pValue, verdict, coOccurrence, insufficient, "association in stored data; not causation");
    }

    static boolean reverseStrong(Request request, List<Occurrence> consequents, List<Occurrence> antecedents) {
        Request reversed = new Request(consequents, antecedents, request.minLag(), request.maxLag(),
                request.spatial(), request.dedupWindow(), request.rangeStart(), request.rangeEnd(),
                request.coverageGapFraction(), request.seedText() + ":reverse");
        Result reverse = test(reversed);
        return reverse.pValue() <= 0.05 && reverse.lift() >= 2.0;
    }

    static String verdict(boolean insufficient, double hitRate, double lift, double pValue) {
        if (insufficient) {
            return "insufficient";
        }
        if (pValue <= 0.05 && lift >= 2.0) {
            return "notable";
        }
        if (pValue <= 0.10 && lift >= 1.5) {
            return "weak";
        }
        return "no-evidence-of-precedence";
    }

    private static Occurrence firstConsequent(Occurrence antecedent, List<Occurrence> consequents, Request request) {
        Instant earliest = antecedent.time().plus(request.minLag());
        Instant latest = antecedent.time().plus(request.maxLag());
        for (Occurrence consequent : consequents) {
            if (consequent.time().isBefore(earliest)) {
                continue;
            }
            if (consequent.time().isAfter(latest) || consequent.time().isAfter(request.rangeEnd())) {
                break;
            }
            if (spatially(antecedent, consequent, request.spatial())) {
                return consequent;
            }
        }
        return null;
    }

    /** True when an antecedent and consequent satisfy the spatial rule. */
    public static boolean spatially(Occurrence a, Occurrence c, Spatial spatial) {
        if (spatial == null || "none".equals(spatial.kind())) {
            return true;
        }
        return switch (spatial.kind()) {
            case "sameEntity" -> a.entity() != null && a.entity().equals(c.entity());
            case "sameRegion" -> a.region() != null && a.region().equals(c.region());
            case "withinKm" -> {
                if (a.lon() == null || c.lon() == null || a.lat() == null || c.lat() == null) {
                    yield false;
                }
                yield ai.foresignal.source.spi.Geo.haversineKm(a.lat(), a.lon(), c.lat(), c.lon()) <= spatial.withinKm();
            }
            default -> true;
        };
    }

    static List<Occurrence> sort(List<Occurrence> occurrences) {
        List<Occurrence> out = new ArrayList<>(occurrences == null ? List.of() : occurrences);
        out.sort(Comparator.comparing(Occurrence::time));
        return out;
    }

    static List<Occurrence> dedup(List<Occurrence> occurrences, Duration window) {
        if (occurrences.isEmpty() || window == null || window.isZero()) {
            return occurrences;
        }
        List<Occurrence> out = new ArrayList<>();
        Instant lastKept = null;
        for (Occurrence occurrence : occurrences) {
            if (lastKept == null || !occurrence.time().isBefore(lastKept.plus(window))) {
                out.add(occurrence);
                lastKept = occurrence.time();
            }
        }
        return out;
    }

    private static long min(List<Long> values) {
        return values.stream().mapToLong(Long::longValue).min().orElse(0);
    }

    private static long max(List<Long> values) {
        return values.stream().mapToLong(Long::longValue).max().orElse(0);
    }

    static long percentile(List<Long> values, double percentile) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Long> sorted = new ArrayList<>(values);
        sorted.sort(Long::compareTo);
        int index = (int) Math.round(percentile * (sorted.size() - 1));
        return sorted.get(Math.max(0, Math.min(sorted.size() - 1, index)));
    }
}
