package ai.foresignal.baserate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.statistics.distribution.BetaDistribution;

/**
 * The base-rate computation (prompt 121, {@code baserate} {@value #VERSION}): pure, deterministic, from weekly counts.
 *
 * <ol>
 *   <li><strong>One source per region and week.</strong> For every region and ISO week the first source in the
 *       family's priority that covers it is used (conflict: ACLED, then UCDP, then Foresignal; seismic: USGS, then
 *       Foresignal; disaster: GDACS, then Foresignal); without a priority, the source with the longest coverage comes
 *       first. Events of other sources in that region and week are never added, so nothing is counted twice.
 *   <li><strong>Covered weeks.</strong> A week counts when every region of the reference class has a covering source.
 *   <li><strong>Windows</strong> are non-overlapping runs of whole weeks (1, 4 or 13), tiled from the first covered
 *       week; a window with an uncovered week is dropped and tiling restarts after it.
 *   <li><strong>Statistics:</strong> events per window, the fraction of windows with at least one event
 *       ({@code pAtLeastOne}) and its 95% Jeffreys interval (Beta(x + ½, n − x + ½) quantiles; 0 when x = 0, 1 when
 *       x = n).
 *   <li><strong>Conditional rates</strong> keep only the windows preceded, within {@code withinDays} (rounded up to
 *       whole weeks), by at least one antecedent event in covered weeks; the unconditional rate is reported alongside.
 * </ol>
 */
public final class BaseRateCalculator {

    /** The computer's name. */
    public static final String NAME = "baserate";

    /** The computer's version. */
    public static final String VERSION = "1.0.0";

    /** The interval level. */
    public static final double LEVEL = 0.95;

    private BaseRateCalculator() {}

    /**
     * A source's coverage: week ranges for every region, and week ranges per country (an import limited to some
     * countries, such as ACLED's).
     *
     * @param ranges {@code [from, to)} week ranges (Mondays) covering every region
     * @param countryRanges {@code [from, to)} week ranges covering one country (and its admin-1 regions)
     */
    public record Coverage(List<LocalDate[]> ranges, Map<String, List<LocalDate[]>> countryRanges) {

        /** Copies the inputs. */
        public Coverage {
            ranges = ranges == null ? List.of() : List.copyOf(ranges);
            countryRanges = countryRanges == null ? Map.of() : Map.copyOf(countryRanges);
        }

        /** Coverage of every region over the given ranges. */
        public static Coverage global(List<LocalDate[]> ranges) {
            return new Coverage(ranges, Map.of());
        }

        /**
         * This coverage cut off at {@code end}: ranges ending later end there and ranges starting at or after it are
         * dropped, so coverage known only for later weeks changes neither the source order nor any choice before
         * {@code end} (INT-2, rates as of a past time).
         */
        public Coverage clippedAt(LocalDate end) {
            Map<String, List<LocalDate[]>> countries = new HashMap<>();
            countryRanges.forEach((country, list) -> {
                List<LocalDate[]> kept = clip(list, end);
                if (!kept.isEmpty()) {
                    countries.put(country, kept);
                }
            });
            return new Coverage(clip(ranges, end), countries);
        }

        private static List<LocalDate[]> clip(List<LocalDate[]> list, LocalDate end) {
            List<LocalDate[]> out = new ArrayList<>();
            for (LocalDate[] r : list) {
                if (r[0].isBefore(end)) {
                    out.add(new LocalDate[] {r[0], r[1].isAfter(end) ? end : r[1]});
                }
            }
            return out;
        }

        /** True when the source covers {@code region} in the week starting {@code week}. */
        public boolean covers(String region, LocalDate week) {
            if (within(ranges, week)) {
                return true;
            }
            List<LocalDate[]> country = countryRanges.get(region.length() > 2 ? region.substring(0, 2) : region);
            return country != null && within(country, week);
        }

        private static boolean within(List<LocalDate[]> list, LocalDate week) {
            for (LocalDate[] r : list) {
                if (!week.isBefore(r[0]) && week.isBefore(r[1])) {
                    return true;
                }
            }
            return false;
        }

        /** The weeks covered: the global ranges plus the longest country's ranges (for ordering). */
        public long weeks() {
            long country = 0;
            for (List<LocalDate[]> list : countryRanges.values()) {
                country = Math.max(country, length(list));
            }
            return length(ranges) + country;
        }

        private static long length(List<LocalDate[]> list) {
            long out = 0;
            for (LocalDate[] r : list) {
                out += Math.max(0, java.time.temporal.ChronoUnit.WEEKS.between(r[0], r[1]));
            }
            return out;
        }

        /** The first covered week, or null. */
        public LocalDate first() {
            LocalDate out = null;
            for (LocalDate[] r : ranges) {
                out = out == null || r[0].isBefore(out) ? r[0] : out;
            }
            for (List<LocalDate[]> list : countryRanges.values()) {
                for (LocalDate[] r : list) {
                    out = out == null || r[0].isBefore(out) ? r[0] : out;
                }
            }
            return out;
        }
    }

    /**
     * The weekly counts of one family: region → source → week → count.
     *
     * @param counts the counts
     */
    public record Counts(Map<String, Map<String, Map<LocalDate, Long>>> counts) {

        /** Copies nothing; the map is read only by the calculator. */
        public Counts {
            counts = counts == null ? Map.of() : counts;
        }

        /** The count of one region, source and week (0 when absent). */
        public long get(String region, String source, LocalDate week) {
            Map<String, Map<LocalDate, Long>> bySource = counts.get(region);
            if (bySource == null) {
                return 0;
            }
            Map<LocalDate, Long> weeks = bySource.get(source);
            Long n = weeks == null ? null : weeks.get(week);
            return n == null ? 0 : n;
        }
    }

    /**
     * One computed rate.
     *
     * @param ratePerWindow mean events per window
     * @param pAtLeastOne the fraction of windows with at least one event
     * @param lower the interval's lower bound
     * @param upper the interval's upper bound
     * @param nWindows the windows
     * @param nEvents the events in them
     * @param periodFrom the first day of the first window, or null without windows
     * @param periodTo the last day of the last window, or null
     * @param sourceMix events per source over the windows
     * @param sourcesUsed the sources chosen for at least one region-week
     * @param unconditional the unconditional rate beside a conditional one, or null
     */
    public record Result(double ratePerWindow, double pAtLeastOne, double lower, double upper, int nWindows,
            long nEvents, LocalDate periodFrom, LocalDate periodTo, Map<String, Long> sourceMix,
            Set<String> sourcesUsed, Result unconditional) {}

    /**
     * Inputs of one computation.
     *
     * @param regions the regions of the reference class
     * @param counts the family's weekly counts
     * @param coverage each source's coverage
     * @param priority the family's source priority, or null for longest coverage first
     * @param windowWeeks the window in weeks
     * @param end the first week not to use (the current, incomplete week)
     * @param antecedent the antecedent's counts for a conditional rate, or null
     * @param antecedentPriority the antecedent family's priority, or null
     * @param withinDays the condition's days
     */
    public record Inputs(Collection<String> regions, Counts counts, Map<String, Coverage> coverage,
            List<String> priority, int windowWeeks, LocalDate end, Counts antecedent, List<String> antecedentPriority,
            int withinDays) {}

    /** Computes a rate. */
    public static Result compute(Inputs in) {
        List<String> regions = new ArrayList<>(new TreeSet<>(in.regions()));
        Map<String, Long> mix = new TreeMap<>();
        Set<String> used = new TreeSet<>();
        TreeMap<LocalDate, Long> weekly = weekly(regions, in.counts(), in.coverage(), order(in.priority(),
                in.coverage()), in.end(), mix, used, true);
        List<LocalDate[]> windows = windows(weekly, in.windowWeeks());
        Result unconditional = stats(windows, weekly, in.windowWeeks(), in.counts(), regions, in.coverage(),
                order(in.priority(), in.coverage()), mix, used);
        if (in.antecedent() == null) {
            return unconditional;
        }
        TreeMap<LocalDate, Long> antecedent = weekly(regions, in.antecedent(), in.coverage(),
                order(in.antecedentPriority(), in.coverage()), in.end(), new TreeMap<>(), new TreeSet<>(), false);
        int lookback = Math.max(1, (int) Math.ceil(in.withinDays() / 7.0));
        List<LocalDate[]> conditioned = new ArrayList<>();
        for (LocalDate[] w : windows) {
            boolean occurred = false;
            boolean covered = true;
            for (int i = 1; i <= lookback; i++) {
                Long n = antecedent.get(w[0].minusWeeks(i));
                if (n == null) {
                    covered = false;
                    break;
                }
                if (n > 0) {
                    occurred = true;
                }
            }
            if (covered && occurred) {
                conditioned.add(w);
            }
        }
        Map<String, Long> conditionedMix = new TreeMap<>();
        Result conditional = stats(conditioned, weekly, in.windowWeeks(), in.counts(), regions, in.coverage(),
                order(in.priority(), in.coverage()), conditionedMix, new TreeSet<>());
        return new Result(conditional.ratePerWindow(), conditional.pAtLeastOne(), conditional.lower(),
                conditional.upper(), conditional.nWindows(), conditional.nEvents(), conditional.periodFrom(),
                conditional.periodTo(), conditional.sourceMix(), used, unconditional);
    }

    /** The source order: the priority (known sources first, then any other by coverage), or longest coverage first. */
    static List<String> order(List<String> priority, Map<String, Coverage> coverage) {
        List<String> out = new ArrayList<>();
        if (priority != null) {
            for (String s : priority) {
                if (coverage.containsKey(s)) {
                    out.add(s);
                }
            }
        }
        List<String> rest = new ArrayList<>(coverage.keySet());
        rest.removeAll(out);
        rest.sort(Comparator.comparingLong((String s) -> -coverage.get(s).weeks()).thenComparing(s -> s));
        out.addAll(rest);
        return out;
    }

    /**
     * The weekly totals over every covered week (absent weeks are not covered), choosing one source per region and
     * week.
     */
    static TreeMap<LocalDate, Long> weekly(List<String> regions, Counts counts, Map<String, Coverage> coverage,
            List<String> order, LocalDate end, Map<String, Long> mix, Set<String> used, boolean recordMix) {
        TreeMap<LocalDate, Long> out = new TreeMap<>();
        LocalDate start = null;
        for (String s : order) {
            LocalDate first = coverage.get(s).first();
            if (first != null && (start == null || first.isBefore(start))) {
                start = first;
            }
        }
        if (start == null || regions.isEmpty()) {
            return out;
        }
        Map<String, Long> weekMix = new HashMap<>();
        for (LocalDate week = start; week.isBefore(end); week = week.plusWeeks(1)) {
            long total = 0;
            boolean covered = true;
            weekMix.clear();
            for (String region : regions) {
                String chosen = null;
                for (String s : order) {
                    if (coverage.get(s).covers(region, week)) {
                        chosen = s;
                        break;
                    }
                }
                if (chosen == null) {
                    covered = false;
                    break;
                }
                long n = counts.get(region, chosen, week);
                total += n;
                weekMix.merge(chosen, n, Long::sum);
            }
            if (covered) {
                out.put(week, total);
                if (recordMix) {
                    used.addAll(weekMix.keySet());
                }
            }
        }
        return out;
    }

    /** The windows: runs of {@code size} consecutive covered weeks, tiled from the first covered week. */
    static List<LocalDate[]> windows(TreeMap<LocalDate, Long> weekly, int size) {
        List<LocalDate[]> out = new ArrayList<>();
        if (weekly.isEmpty()) {
            return out;
        }
        LocalDate week = weekly.firstKey();
        LocalDate last = weekly.lastKey();
        while (!week.isAfter(last)) {
            boolean complete = true;
            LocalDate gap = null;
            for (int i = 0; i < size; i++) {
                if (!weekly.containsKey(week.plusWeeks(i))) {
                    complete = false;
                    gap = week.plusWeeks(i);
                    break;
                }
            }
            if (complete) {
                out.add(new LocalDate[] {week, week.plusWeeks(size)});
                week = week.plusWeeks(size);
            } else {
                LocalDate next = weekly.higherKey(gap);
                if (next == null) {
                    break;
                }
                week = next;
            }
        }
        return out;
    }

    private static Result stats(List<LocalDate[]> windows, TreeMap<LocalDate, Long> weekly, int size, Counts counts,
            List<String> regions, Map<String, Coverage> coverage, List<String> order, Map<String, Long> mix,
            Set<String> used) {
        long events = 0;
        int withEvent = 0;
        for (LocalDate[] w : windows) {
            long n = 0;
            for (int i = 0; i < size; i++) {
                LocalDate week = w[0].plusWeeks(i);
                n += weekly.getOrDefault(week, 0L);
                for (String region : regions) {
                    for (String s : order) {
                        if (coverage.get(s).covers(region, week)) {
                            long c = counts.get(region, s, week);
                            if (c > 0) {
                                mix.merge(s, c, Long::sum);
                            }
                            used.add(s);
                            break;
                        }
                    }
                }
            }
            events += n;
            if (n > 0) {
                withEvent++;
            }
        }
        int nWindows = windows.size();
        double[] interval = jeffreys(withEvent, nWindows);
        Map<String, Long> sortedMix = new LinkedHashMap<>(new TreeMap<>(mix));
        return new Result(nWindows == 0 ? 0 : (double) events / nWindows, nWindows == 0 ? 0 : (double) withEvent
                / nWindows, interval[0], interval[1], nWindows, events, windows.isEmpty() ? null : windows.get(0)[0],
                windows.isEmpty() ? null : windows.get(windows.size() - 1)[1].minusDays(1), sortedMix,
                new TreeSet<>(used), null);
    }

    /**
     * The 95% Jeffreys interval of {@code x / n}: the 2.5% and 97.5% quantiles of Beta(x + ½, n − x + ½), with the
     * lower bound 0 when x = 0 and the upper bound 1 when x = n; [0, 1] when n = 0.
     */
    public static double[] jeffreys(long x, long n) {
        if (n <= 0) {
            return new double[] {0, 1};
        }
        BetaDistribution beta = BetaDistribution.of(x + 0.5, n - x + 0.5);
        double alpha = (1 - LEVEL) / 2;
        double lower = x == 0 ? 0 : beta.inverseCumulativeProbability(alpha);
        double upper = x == n ? 1 : beta.inverseCumulativeProbability(1 - alpha);
        return new double[] {lower, upper};
    }
}
