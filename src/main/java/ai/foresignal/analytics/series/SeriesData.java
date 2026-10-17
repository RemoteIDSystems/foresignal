package ai.foresignal.analytics.series;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A series read through the {@link SeriesReader} (prompt 119): its points, gaps and metadata.
 *
 * @param ref the reference
 * @param from the first bucket start
 * @param to the end (exclusive)
 * @param asOf the knowledge cutoff applied (only points with {@code knownAt ≤ asOf}), or null
 * @param asOfSeq the seq the cutoff corresponds to ({@code S(asOf)}, or the newest seq), for manifests
 * @param frequency the frequency
 * @param unit the unit, or null
 * @param signalFamily the 119 signal family, or null
 * @param seriesFamily the 117 series family ({@code price}, {@code count}, …), or null
 * @param transform the 117 transform hint, or null
 * @param releaseLagDays the typical publication lag in days
 * @param regionCode the 040 region the series covers, or null
 * @param topicId the 117 attention topic, or null
 * @param sources the sources whose data the series rests on (empty for the cross-source rollup)
 * @param localOnly true when a source's egress is {@code LOCAL_ONLY}
 * @param points the points, one per bucket that has data, in time order
 * @param gaps the buckets with no point or no coverage, merged into ranges ({@code [start, end)})
 */
public record SeriesData(SeriesRef ref, Instant from, Instant to, Instant asOf, long asOfSeq, SeriesFrequency frequency,
        String unit, String signalFamily, String seriesFamily, String transform, double releaseLagDays,
        String regionCode, String topicId, Set<String> sources, boolean localOnly, List<SeriesValue> points,
        List<Instant[]> gaps) {

    /** Copies the collections. */
    public SeriesData {
        sources = sources == null ? Set.of() : Set.copyOf(sources);
        points = points == null ? List.of() : List.copyOf(points);
        gaps = gaps == null ? List.of() : List.copyOf(gaps);
    }

    /** True for a count series (rollups, rule samples and the {@code count} family). */
    public boolean counts() {
        return ref.type() != SeriesRef.Type.SERIES || "count".equals(seriesFamily);
    }

    /**
     * The series on its regular bucket grid {@code [from, to)}.
     *
     * @return the grid; irregular series give their points in order
     */
    public Grid grid() {
        if (!frequency.regular()) {
            int n = points.size();
            Instant[] starts = new Instant[n];
            double[] values = new double[n];
            Instant[] known = new Instant[n];
            boolean[] gap = new boolean[n];
            for (int i = 0; i < n; i++) {
                SeriesValue p = points.get(i);
                starts[i] = p.periodStart();
                values[i] = p.usable() ? p.value() : Double.NaN;
                known[i] = p.knownAt();
                gap[i] = !p.usable();
            }
            return new Grid(starts, values, known, gap);
        }
        Map<Instant, SeriesValue> byStart = new LinkedHashMap<>();
        for (SeriesValue p : points) {
            byStart.put(frequency.floor(p.periodStart()), p);
        }
        List<Instant> starts = new ArrayList<>();
        for (Instant t = frequency.floor(from); t.isBefore(to); t = frequency.next(t)) {
            starts.add(t);
        }
        int n = starts.size();
        double[] values = new double[n];
        Instant[] known = new Instant[n];
        boolean[] gap = new boolean[n];
        for (int i = 0; i < n; i++) {
            SeriesValue p = byStart.get(starts.get(i));
            boolean usable = p != null && p.usable();
            values[i] = usable ? p.value() : Double.NaN;
            known[i] = p == null ? null : p.knownAt();
            gap[i] = !usable;
        }
        return new Grid(starts.toArray(new Instant[0]), values, known, gap);
    }

    /**
     * A series on its bucket grid.
     *
     * @param starts the bucket starts
     * @param values the values ({@code NaN} where missing or uncovered)
     * @param knownAt when each value became known (null where missing)
     * @param gap true where missing or uncovered
     */
    public record Grid(Instant[] starts, double[] values, Instant[] knownAt, boolean[] gap) {

        /** The number of buckets. */
        public int size() {
            return starts.length;
        }

        /** The index of the bucket starting at {@code start}, or −1. */
        public int indexOf(Instant start) {
            for (int i = 0; i < starts.length; i++) {
                if (starts[i].equals(start)) {
                    return i;
                }
            }
            return -1;
        }

        /** This grid with every value whose {@code knownAt} is after {@code decision} turned into a gap (INT-2). */
        public Grid knownBy(Instant decision) {
            if (decision == null) {
                return this;
            }
            double[] v = values.clone();
            boolean[] g = gap.clone();
            for (int i = 0; i < v.length; i++) {
                if (knownAt[i] == null || knownAt[i].isAfter(decision)) {
                    v[i] = Double.NaN;
                    g[i] = true;
                }
            }
            return new Grid(starts, v, knownAt, g);
        }
    }

    /**
     * The series aggregated onto a coarser frequency (hour to day, day to week): counts are summed, other series
     * averaged. A coarse bucket of counts is covered only when every fine bucket inside it is; a level needs at least
     * one covered value. Its {@code knownAt} is the latest of its values'.
     */
    public SeriesData resample(SeriesFrequency target) {
        if (target == frequency || !frequency.regular() || !target.regular()) {
            return this;
        }
        Grid grid = grid();
        List<SeriesValue> out = new ArrayList<>();
        int i = 0;
        Instant start = target.floor(from);
        while (start.isBefore(to)) {
            Instant end = target.next(start);
            double sum = 0;
            boolean ok = true;
            int seen = 0;
            int used = 0;
            Instant known = null;
            while (i < grid.size() && grid.starts()[i].isBefore(end)) {
                if (!grid.starts()[i].isBefore(start)) {
                    seen++;
                    if (grid.gap()[i]) {
                        ok = !counts() && ok;
                    } else {
                        sum += grid.values()[i];
                        used++;
                    }
                    Instant k = grid.knownAt()[i];
                    if (k != null && (known == null || k.isAfter(known))) {
                        known = k;
                    }
                }
                i++;
            }
            if (seen > 0 && ok && used > 0) {
                out.add(new SeriesValue(start, end, counts() ? sum : sum / used, known, true, null, null, null));
            }
            start = end;
        }
        List<Instant[]> gaps = SeriesReader.gapsOf(target, target.floor(from), to, out);
        return new SeriesData(ref, target.floor(from), to, asOf, asOfSeq, target, unit, signalFamily, seriesFamily,
                transform, releaseLagDays, regionCode, topicId, sources, localOnly, out, gaps);
    }

    /**
     * The {@code SeriesData} JSON of {@code GET /v1/analytics/series/data}.
     *
     * @param withheld true to leave raw values out ({@code LOCAL_ONLY} source, caller without local egress)
     */
    public Map<String, Object> view(boolean withheld) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", ref.format());
        out.put("from", from.toString());
        out.put("to", to.toString());
        out.put("asOf", asOf == null ? null : asOf.toString());
        out.put("frequency", frequency.wire());
        out.put("unit", unit);
        out.put("family", signalFamily != null ? signalFamily : seriesFamily);
        out.put("valuesWithheld", withheld);
        List<Map<String, Object>> pts = new ArrayList<>();
        for (SeriesValue p : points) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("period", p.period() != null ? p.period() : p.periodStart().toString());
            m.put("periodStart", p.periodStart().toString());
            m.put("periodEnd", p.periodEnd() == null ? null : p.periodEnd().toString());
            m.put("value", withheld ? null : p.value());
            m.put("unit", unit == null ? "" : unit);
            m.put("status", p.status() == null ? "final" : p.status());
            if (p.version() != null) {
                m.put("version", p.version());
            }
            m.put("knownAt", p.knownAt() == null ? null : p.knownAt().toString());
            m.put("coverageOk", p.coverageOk());
            pts.add(m);
        }
        out.put("points", pts);
        List<Map<String, Object>> gapList = new ArrayList<>();
        for (Instant[] g : gaps) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("start", g[0].toString());
            m.put("end", g[1] == null ? null : g[1].toString());
            gapList.add(m);
        }
        out.put("gaps", gapList);
        return out;
    }
}
