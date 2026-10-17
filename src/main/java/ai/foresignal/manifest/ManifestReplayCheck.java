package ai.foresignal.manifest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Compares a replayed output with the stored one (prompt 116), pure: numbers equal within
 * {@value #TOLERANCE} (relative) count as equal, maps and lists are compared member by member, and the first
 * {@value #MAX_DIFFERENCES} differing paths make the diff summary.
 */
public final class ManifestReplayCheck {

    /** The relative tolerance of numeric comparisons. */
    public static final double TOLERANCE = 1e-9;

    /** The most differing paths listed. */
    public static final int MAX_DIFFERENCES = 10;

    private ManifestReplayCheck() {}

    /** {@code identical}, or {@code differs} with a summary of the differing paths. */
    public static ReplayOutcome compare(Object stored, Object replayed) {
        List<String> differences = new ArrayList<>();
        diff("$", stored, replayed, differences);
        if (differences.isEmpty()) {
            return ReplayOutcome.identical();
        }
        StringBuilder summary = new StringBuilder();
        int shown = Math.min(differences.size(), MAX_DIFFERENCES);
        for (int i = 0; i < shown; i++) {
            if (i > 0) {
                summary.append("; ");
            }
            summary.append(differences.get(i));
        }
        if (differences.size() > shown) {
            summary.append("; and ").append(differences.size() - shown).append(" more");
        }
        return ReplayOutcome.differs(summary.toString());
    }

    private static void diff(String path, Object a, Object b, List<String> out) {
        if (out.size() > MAX_DIFFERENCES * 10) {
            return;
        }
        if (a instanceof Number na && b instanceof Number nb) {
            double x = na.doubleValue();
            double y = nb.doubleValue();
            if (!(Double.isNaN(x) && Double.isNaN(y)) && Math.abs(x - y) > TOLERANCE * Math.max(1, Math.max(Math.abs(x),
                    Math.abs(y)))) {
                out.add(path + ": " + x + " → " + y);
            }
            return;
        }
        if (a instanceof Map<?, ?> ma && b instanceof Map<?, ?> mb) {
            TreeSet<String> keys = new TreeSet<>();
            ma.keySet().forEach(k -> keys.add(String.valueOf(k)));
            mb.keySet().forEach(k -> keys.add(String.valueOf(k)));
            for (String key : keys) {
                diff(path + "." + key, ma.get(key), mb.get(key), out);
            }
            return;
        }
        if (a instanceof Collection<?> ca && b instanceof Collection<?> cb) {
            List<?> la = new ArrayList<>(ca);
            List<?> lb = new ArrayList<>(cb);
            if (la.size() != lb.size()) {
                out.add(path + ": " + la.size() + " items → " + lb.size() + " items");
                return;
            }
            for (int i = 0; i < la.size(); i++) {
                diff(path + "[" + i + "]", la.get(i), lb.get(i), out);
            }
            return;
        }
        if (!Objects.equals(a == null ? null : String.valueOf(a), b == null ? null : String.valueOf(b))) {
            out.add(path + ": " + a + " → " + b);
        }
    }
}
