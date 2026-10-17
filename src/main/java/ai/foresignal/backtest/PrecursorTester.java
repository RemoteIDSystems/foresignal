package ai.foresignal.backtest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The precursor tester (prompt 103, version 1.0.0): does X tend to precede Y within T? It builds the
 * deduplicated antecedent occurrences A and the consequent occurrences C over a range, finds each
 * antecedent's first consequent in {@code [t+minLag, t+maxLag]} satisfying the spatial rule, and
 * reports the hit rate with a Wilson interval, lag statistics and a deterministic circular-shift base
 * rate and permutation p-value. It also runs the reverse test (roles swapped) and flags
 * co-occurrence when the reverse is as strong as the forward direction.
 *
 * <p>Pure and deterministic: the same request always gives the same result, including the
 * permutation statistics, because the shifts are drawn from a generator seeded with a SHA-256 hash
 * of the request's seed text (the definition hash and the range).
 *
 * <p><strong>Circular shift.</strong> Each shifted copy of A moves every antecedent by the same
 * offset {@code s} and wraps it around the range: {@code t' = start + ((t - start + s) mod span)}.
 * Wrapping keeps every shifted antecedent inside the range, so the shifted copies have the same
 * number of opportunities to find a consequent as the observed series.
 */
public final class PrecursorTester {

    /** The tester version. */
    public static final String VERSION = "1.0.0";

    /** The number of circular time-shifted copies of A. */
    public static final int SHIFTS = 200;

    /** Below this many (deduplicated) antecedents the verdict is {@code insufficient}. */
    public static final int MIN_ANTECEDENTS = 10;

    /** Coverage gaps covering more than this fraction of the range make the verdict {@code insufficient}. */
    public static final double COVERAGE_GAP_LIMIT = 0.30;

    /** The p-value at or below which (with {@link #NOTABLE_LIFT}) the verdict is {@code notable}. */
    public static final double NOTABLE_P = 0.05;

    /** The lift at or above which (with {@link #NOTABLE_P}) the verdict is {@code notable}. */
    public static final double NOTABLE_LIFT = 2.0;

    /** The p-value at or below which (with {@link #WEAK_LIFT}) the verdict is {@code weak}. */
    public static final double WEAK_P = 0.10;

    /** The lift at or above which (with {@link #WEAK_P}) the verdict is {@code weak}. */
    public static final double WEAK_LIFT = 1.5;

    /** The statement every result carries. */
    public static final String NOTE = "association in stored data; not causation";

    private PrecursorTester() {}

    /**
     * An occurrence with its time and optional place, region and entities.
     *
     * @param time when it happened
     * @param lon the longitude, or null
     * @param lat the latitude, or null
     * @param country the ISO 3166-1 alpha-2 country, or null
     * @param admin1 the first-level region (ISO 3166-2 or the Natural Earth code), or null
     * @param entities the entity keys it involves (for {@code sameEntity}); never null
     */
    public record Occurrence(Instant time, Double lon, Double lat, String country, String admin1,
            Set<String> entities) {

        /** Normalizes the entity set. */
        public Occurrence {
            entities = entities == null ? Set.of() : Set.copyOf(entities);
        }

        /**
         * An occurrence with a single region (the country) and a single entity.
         *
         * @param time when it happened
         * @param lon the longitude, or null
         * @param lat the latitude, or null
         * @param region the country, or null
         * @param entity the entity key, or null
         */
        public Occurrence(Instant time, Double lon, Double lat, String region, String entity) {
            this(time, lon, lat, region, null, entity == null ? Set.of() : Set.of(entity));
        }

        /** The same occurrence at another time. */
        Occurrence at(Instant other) {
            return new Occurrence(other, lon, lat, country, admin1, entities);
        }
    }

    /**
     * The spatial rule.
     *
     * @param kind {@code none}, {@code sameEntity}, {@code withinKm} or {@code sameRegion}
     * @param withinKm the distance for {@code withinKm}, else null
     * @param regionLevel {@code country} or {@code admin1} for {@code sameRegion}, else null
     */
    public record Spatial(String kind, Double withinKm, String regionLevel) {

        /** No spatial constraint. */
        public static Spatial none() {
            return new Spatial("none", null, null);
        }

        /** The antecedent and consequent share at least one entity. */
        public static Spatial sameEntity() {
            return new Spatial("sameEntity", null, null);
        }

        /** The antecedent and consequent are at most {@code km} apart. */
        public static Spatial withinKm(double km) {
            return new Spatial("withinKm", km, null);
        }

        /** The antecedent and consequent are in the same country. */
        public static Spatial sameRegion() {
            return sameRegion("country");
        }

        /** The antecedent and consequent are in the same country or first-level region. */
        public static Spatial sameRegion(String level) {
            return new Spatial("sameRegion", null, "admin1".equals(level) ? "admin1" : "country");
        }
    }

    /**
     * The statistics of one direction (antecedent → consequent).
     *
     * @param nAntecedent the deduplicated antecedents
     * @param nConsequent the consequents
     * @param hits the antecedents with a consequent in the lag window
     * @param hitRate {@code hits / nAntecedent}
     * @param baseRate the mean hit rate of the shifted copies
     * @param lift {@code hitRate / baseRate}
     * @param pValue {@code (1 + #shifted ≥ observed) / (SHIFTS + 1)}
     * @param lags the lags of the hits, minutes
     */
    public record Direction(long nAntecedent, long nConsequent, long hits, double hitRate, double baseRate,
            double lift, double pValue, List<Long> lags) {

        /** True when this direction is notable on its own (enough antecedents, low p, high lift). */
        public boolean strong() {
            return nAntecedent >= MIN_ANTECEDENTS && pValue <= NOTABLE_P && lift >= NOTABLE_LIFT;
        }
    }

    /**
     * The result.
     *
     * @param nAntecedent the deduplicated antecedents
     * @param hits the hits
     * @param hitRate the hit rate
     * @param hitRateInterval the Wilson 95% interval {@code [lower, upper]}
     * @param lagMin the shortest lag, minutes
     * @param lagP25 the 25th percentile lag, minutes
     * @param lagMedian the median lag, minutes
     * @param lagP75 the 75th percentile lag, minutes
     * @param lagMax the longest lag, minutes
     * @param lagMeanMinutes the mean lag, minutes
     * @param baseRate the mean hit rate of the shifted copies
     * @param lift the lift
     * @param pValue the permutation p-value
     * @param verdict {@code insufficient}, {@code no-evidence-of-precedence}, {@code weak} or {@code notable}
     * @param coOccurrence true when the reverse direction is as strong ("co-occurrence, not precedence")
     * @param insufficient true when the sample or the coverage is too small
     * @param note the association statement
     * @param nConsequent the consequents
     * @param reverse the reverse direction's statistics
     * @param seed the generator seed derived from the seed text
     */
    public record Result(long nAntecedent, long hits, double hitRate, double[] hitRateInterval, long lagMin,
            long lagP25, long lagMedian, long lagP75, long lagMax, double lagMeanMinutes, double baseRate,
            double lift, double pValue, String verdict, boolean coOccurrence, boolean insufficient, String note,
            long nConsequent, Direction reverse, long seed) {}

    /**
     * A resolved request.
     *
     * @param antecedents the antecedent occurrences (deduplicated here)
     * @param consequents the consequent occurrences
     * @param minLag the shortest lag that counts
     * @param maxLag the longest lag that counts
     * @param spatial the spatial rule
     * @param dedupWindow antecedents closer than this merge; null or zero keeps every one
     * @param rangeStart the start of the range
     * @param rangeEnd the end of the range
     * @param coverageGapFraction the largest fraction of the range covered by coverage gaps of one source
     * @param seedText the seed text (the definition hash and the range)
     */
    public record Request(List<Occurrence> antecedents, List<Occurrence> consequents, Duration minLag,
            Duration maxLag, Spatial spatial, Duration dedupWindow, Instant rangeStart, Instant rangeEnd,
            double coverageGapFraction, String seedText) {}

    /** The occurrences of {@code list} inside {@code [rangeStart, rangeEnd)} of the request. */
    static List<Occurrence> inRange(List<Occurrence> list, Request request) {
        List<Occurrence> out = new ArrayList<>(list.size());
        for (Occurrence o : list) {
            if (!o.time().isBefore(request.rangeStart()) && o.time().isBefore(request.rangeEnd())) {
                out.add(o);
            }
        }
        return out;
    }

    /** Runs the precursor test in both directions with {@link #SHIFTS} shifted copies. Never recursive. */
    public static Result test(Request request) {
        return test(request, SHIFTS);
    }

    /**
     * Runs the precursor test in both directions with {@code shifts} circular time-shifted copies (prompt 120: mining
     * uses more shifts than the one-off test so that p-values can survive false-discovery control in a batch).
     *
     * @param request the resolved request
     * @param shifts the number of shifted copies (at least 1)
     * @return the result; its p-value is {@code (1 + #shifted ≥ observed) / (shifts + 1)}
     */
    public static Result test(Request request, int shifts) {
        int n = Math.max(1, shifts);
        long seed = seed(request.seedText());
        // Only occurrences inside [rangeStart, rangeEnd) take part (checkpoint 124, INT-2): the circular shift is
        // defined over that span, and an occurrence after the range end must never reach a decision about it. The
        // 103 service and the 120 miner already pass windowed lists, so this changes nothing for them.
        List<Occurrence> antecedents = inRange(request.antecedents(), request);
        List<Occurrence> consequents = inRange(request.consequents(), request);
        Direction forward = direction(antecedents, consequents, request, seed, n);
        Direction reverse = direction(consequents, antecedents, request, seed(request.seedText() + ":reverse"), n);
        boolean insufficient = forward.nAntecedent() < MIN_ANTECEDENTS
                || request.coverageGapFraction() > COVERAGE_GAP_LIMIT;
        boolean coOccurrence = !insufficient && forward.strong() && reverse.strong();
        String verdict = verdict(insufficient, forward.hitRate(), forward.lift(), forward.pValue());
        List<Long> lags = forward.lags();
        return new Result(forward.nAntecedent(), forward.hits(), forward.hitRate(),
                Wilson.interval(forward.hits(), forward.nAntecedent()),
                lags.isEmpty() ? 0 : Collections.min(lags), percentile(lags, 0.25), percentile(lags, 0.5),
                percentile(lags, 0.75), lags.isEmpty() ? 0 : Collections.max(lags),
                lags.isEmpty() ? 0 : lags.stream().mapToLong(Long::longValue).average().orElse(0),
                forward.baseRate(), forward.lift(), forward.pValue(), verdict, coOccurrence, insufficient, NOTE,
                forward.nConsequent(), reverse, seed);
    }

    /**
     * The statistics of one direction: observed hits, then {@link #SHIFTS} circular time-shifted
     * copies of the antecedents for the base rate and the permutation p-value.
     */
    static Direction direction(List<Occurrence> rawAntecedents, List<Occurrence> rawConsequents, Request request,
            long seed) {
        return direction(rawAntecedents, rawConsequents, request, seed, SHIFTS);
    }

    /** {@link #direction(List, List, Request, long)} with {@code shifts} shifted copies. */
    static Direction direction(List<Occurrence> rawAntecedents, List<Occurrence> rawConsequents, Request request,
            long seed, int shifts) {
        List<Occurrence> a = dedup(sort(rawAntecedents), request.dedupWindow());
        Consequents c = new Consequents(sort(rawConsequents));
        long nA = a.size();
        List<Long> lags = new ArrayList<>();
        int hits = 0;
        for (Occurrence antecedent : a) {
            Occurrence match = c.first(antecedent, request);
            if (match != null) {
                hits++;
                lags.add(Duration.between(antecedent.time(), match.time()).toMinutes());
            }
        }
        double hitRate = nA == 0 ? 0 : (double) hits / nA;
        long spanSeconds = Math.max(1, Duration.between(request.rangeStart(), request.rangeEnd()).toSeconds());
        long low = Math.min(request.maxLag().toSeconds(), spanSeconds);
        long high = Math.max(low + 1, spanSeconds - low);
        Random random = new Random(seed);
        double sumShifted = 0;
        int atLeast = 0;
        for (int i = 0; i < shifts; i++) {
            long shift = low + (long) (random.nextDouble() * (high - low));
            int shiftedHits = 0;
            for (Occurrence occurrence : a) {
                Occurrence moved = occurrence.at(circularShift(occurrence.time(), shift, request.rangeStart(),
                        spanSeconds));
                if (c.first(moved, request) != null) {
                    shiftedHits++;
                }
            }
            double rate = nA == 0 ? 0 : (double) shiftedHits / nA;
            sumShifted += rate;
            if (rate >= hitRate) {
                atLeast++;
            }
        }
        double baseRate = sumShifted / shifts;
        double lift = baseRate == 0 ? (hitRate > 0 ? Double.POSITIVE_INFINITY : 0) : hitRate / baseRate;
        double pValue = (1.0 + atLeast) / (shifts + 1.0);
        return new Direction(nA, c.size(), hits, hitRate, baseRate, lift, pValue, List.copyOf(lags));
    }

    /**
     * Moves {@code time} by {@code shiftSeconds} and wraps it around the range:
     * {@code start + ((time - start + shift) mod span)}. Times outside the range are wrapped too.
     */
    static Instant circularShift(Instant time, long shiftSeconds, Instant start, long spanSeconds) {
        long offset = Duration.between(start, time).toSeconds();
        return start.plusSeconds(Math.floorMod(offset + shiftSeconds, Math.max(1, spanSeconds)));
    }

    /** The verdict label from the documented thresholds. */
    static String verdict(boolean insufficient, double hitRate, double lift, double pValue) {
        if (insufficient) {
            return "insufficient";
        }
        if (pValue <= NOTABLE_P && lift >= NOTABLE_LIFT) {
            return "notable";
        }
        if (pValue <= WEAK_P && lift >= WEAK_LIFT) {
            return "weak";
        }
        return "no-evidence-of-precedence";
    }

    /** True when an antecedent and consequent satisfy the spatial rule. */
    public static boolean spatially(Occurrence a, Occurrence c, Spatial spatial) {
        if (spatial == null || "none".equals(spatial.kind())) {
            return true;
        }
        return switch (spatial.kind()) {
            case "sameEntity" -> !Collections.disjoint(a.entities(), c.entities());
            case "sameRegion" -> {
                String left = "admin1".equals(spatial.regionLevel()) ? a.admin1() : a.country();
                String right = "admin1".equals(spatial.regionLevel()) ? c.admin1() : c.country();
                yield left != null && left.equals(right);
            }
            case "withinKm" -> {
                if (a.lon() == null || c.lon() == null || a.lat() == null || c.lat() == null) {
                    yield false;
                }
                yield ai.foresignal.source.spi.Geo.haversineKm(a.lat(), a.lon(), c.lat(), c.lon()) <= spatial.withinKm();
            }
            default -> true;
        };
    }

    /** The occurrences sorted by time (nulls dropped). */
    static List<Occurrence> sort(List<Occurrence> occurrences) {
        List<Occurrence> out = new ArrayList<>();
        if (occurrences != null) {
            for (Occurrence occurrence : occurrences) {
                if (occurrence != null && occurrence.time() != null) {
                    out.add(occurrence);
                }
            }
        }
        out.sort(Comparator.comparing(Occurrence::time));
        return out;
    }

    /** Merges occurrences closer than {@code window} to the last kept one. */
    static List<Occurrence> dedup(List<Occurrence> occurrences, Duration window) {
        if (occurrences.isEmpty() || window == null || window.isZero() || window.isNegative()) {
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

    /** The nearest-rank percentile of {@code values} (0 when empty). */
    static long percentile(List<Long> values, double percentile) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Long> sorted = new ArrayList<>(values);
        sorted.sort(Long::compareTo);
        int index = (int) Math.round(percentile * (sorted.size() - 1));
        return sorted.get(Math.max(0, Math.min(sorted.size() - 1, index)));
    }

    /** A deterministic 64-bit seed: the first eight bytes of the SHA-256 of {@code text}. */
    static long seed(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.valueOf(text).getBytes(StandardCharsets.UTF_8));
            long seed = 0;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (digest[i] & 0xffL);
            }
            return seed;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** The consequents sorted by time, with a binary search for the first one in a lag window. */
    private static final class Consequents {

        private final List<Occurrence> sorted;
        private final long[] epochSeconds;

        Consequents(List<Occurrence> sorted) {
            this.sorted = sorted;
            this.epochSeconds = new long[sorted.size()];
            for (int i = 0; i < sorted.size(); i++) {
                epochSeconds[i] = sorted.get(i).time().getEpochSecond();
            }
        }

        int size() {
            return sorted.size();
        }

        /** The first consequent in {@code [t + minLag, min(t + maxLag, rangeEnd)]} satisfying the spatial rule. */
        Occurrence first(Occurrence antecedent, Request request) {
            Instant earliest = antecedent.time().plus(request.minLag());
            Instant latest = antecedent.time().plus(request.maxLag());
            int index = lowerBound(earliest.getEpochSecond());
            for (int i = index; i < sorted.size(); i++) {
                Occurrence consequent = sorted.get(i);
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

        /** The first index whose epoch second is at least {@code value}. */
        private int lowerBound(long value) {
            int lo = 0;
            int hi = epochSeconds.length;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (epochSeconds[mid] < value) {
                    lo = mid + 1;
                } else {
                    hi = mid;
                }
            }
            return lo;
        }
    }
}
