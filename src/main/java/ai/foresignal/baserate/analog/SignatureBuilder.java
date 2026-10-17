package ai.foresignal.baserate.analog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds situation and region-period signatures (prompt 121, {@code situation-signature} {@value #VERSION}): pure
 * and deterministic. Features are named, so signatures computed with different family registries still compare on
 * the names they share.
 *
 * <ul>
 *   <li>{@code family:{f}}: the count of family {@code f} over the first N days (N = min(7, age)), divided by the
 *       family's global weekly standard deviation (floored at 1);
 *   <li>{@code slope}: the escalation slope, the least-squares slope of log(1 + members per day) over those days;
 *   <li>{@code theme:{t}}: theme one-hot;
 *   <li>{@code continent:{c}} and {@code income:{b}}: the region class one-hot (040 continent, 117 income band);
 *   <li>{@code entity:{type}}: log(1 + entities of each 098 type);
 *   <li>{@code severity:max}, {@code severity:last} (rank ÷ 2, info 0 … critical 1) and {@code severity:elevated}
 *       (the share of days at warning or above).
 * </ul>
 */
public final class SignatureBuilder {

    /** The signature version. */
    public static final String VERSION = "1.0.0";

    /** The signature's name. */
    public static final String NAME = "situation-signature";

    /** The longest window, in days. */
    public static final int MAX_DAYS = 7;

    /** The entity types (098). */
    public static final List<String> ENTITY_TYPES = List.of("aircraft", "vessel", "satellite", "station");

    /** The severity ranks. */
    public static final List<String> SEVERITIES = List.of("info", "warning", "critical");

    private SignatureBuilder() {}

    /**
     * A signature: named features.
     *
     * @param names the feature names
     * @param values the values, in the same order
     */
    public record Signature(List<String> names, double[] values) {

        /** Copies the inputs. */
        public Signature {
            names = List.copyOf(names);
            values = values.clone();
        }

        /** The value of a feature (0 when absent). */
        public double get(String name) {
            int i = names.indexOf(name);
            return i < 0 ? 0 : values[i];
        }

        /** The features as a map. */
        public Map<String, Double> asMap() {
            Map<String, Double> out = new TreeMap<>();
            for (int i = 0; i < names.size(); i++) {
                out.put(names.get(i), values[i]);
            }
            return out;
        }

        /** True when every value is 0. */
        public boolean zero() {
            for (double v : values) {
                if (v != 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * The inputs of one signature.
     *
     * @param familyCounts members (or events) per family over the window
     * @param familySd each family's global weekly standard deviation
     * @param dailyCounts members per day over the window, oldest first
     * @param theme the theme, or null
     * @param continent the continent, or null
     * @param incomeBand the income band, or null
     * @param entityCounts entities per 098 type
     * @param severities the severity of each day so far, oldest first
     */
    public record Inputs(Map<String, Long> familyCounts, Map<String, Double> familySd, long[] dailyCounts, String theme,
            String continent, String incomeBand, Map<String, Long> entityCounts, List<String> severities) {}

    /** Builds a signature. */
    public static Signature build(Inputs in) {
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Map.Entry<String, Long> e : new TreeMap<>(in.familyCounts()).entrySet()) {
            double sd = Math.max(1.0, in.familySd() == null ? 1.0 : in.familySd().getOrDefault(e.getKey(), 1.0));
            names.add("family:" + e.getKey());
            values.add(e.getValue() / sd);
        }
        names.add("slope");
        values.add(slope(in.dailyCounts()));
        if (in.theme() != null) {
            names.add("theme:" + in.theme());
            values.add(1.0);
        }
        if (in.continent() != null) {
            names.add("continent:" + in.continent());
            values.add(1.0);
        }
        if (in.incomeBand() != null) {
            names.add("income:" + in.incomeBand());
            values.add(1.0);
        }
        for (String type : ENTITY_TYPES) {
            long n = in.entityCounts() == null ? 0 : in.entityCounts().getOrDefault(type, 0L);
            if (n > 0) {
                names.add("entity:" + type);
                values.add(Math.log1p(n));
            }
        }
        List<String> severities = in.severities() == null ? List.of() : in.severities();
        if (!severities.isEmpty()) {
            int max = 0;
            int elevated = 0;
            for (String s : severities) {
                int rank = Math.max(0, SEVERITIES.indexOf(s));
                max = Math.max(max, rank);
                if (rank >= 1) {
                    elevated++;
                }
            }
            names.add("severity:max");
            values.add(max / 2.0);
            names.add("severity:last");
            values.add(Math.max(0, SEVERITIES.indexOf(severities.get(severities.size() - 1))) / 2.0);
            names.add("severity:elevated");
            values.add((double) elevated / severities.size());
        }
        double[] out = new double[values.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = values.get(i);
        }
        return new Signature(names, out);
    }

    /** The least-squares slope of log(1 + c) against the day index; 0 for fewer than two days. */
    public static double slope(long[] daily) {
        if (daily == null || daily.length < 2) {
            return 0;
        }
        int n = daily.length;
        double meanX = (n - 1) / 2.0;
        double meanY = 0;
        for (long c : daily) {
            meanY += Math.log1p(Math.max(0, c));
        }
        meanY /= n;
        double num = 0;
        double den = 0;
        for (int i = 0; i < n; i++) {
            double dx = i - meanX;
            num += dx * (Math.log1p(Math.max(0, daily[i])) - meanY);
            den += dx * dx;
        }
        return den == 0 ? 0 : num / den;
    }

    /** The cosine similarity of two signatures over the union of their feature names; 0 when either is zero. */
    public static double cosine(Signature a, Signature b) {
        Map<String, double[]> pairs = new HashMap<>();
        for (int i = 0; i < a.names().size(); i++) {
            pairs.computeIfAbsent(a.names().get(i), k -> new double[2])[0] = a.values()[i];
        }
        for (int i = 0; i < b.names().size(); i++) {
            pairs.computeIfAbsent(b.names().get(i), k -> new double[2])[1] = b.values()[i];
        }
        double dot = 0;
        double na = 0;
        double nb = 0;
        for (double[] p : pairs.values()) {
            dot += p[0] * p[1];
            na += p[0] * p[0];
            nb += p[1] * p[1];
        }
        return na == 0 || nb == 0 ? 0 : dot / Math.sqrt(na * nb);
    }
}
