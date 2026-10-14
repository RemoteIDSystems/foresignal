package ai.foresignal.source.adapters.sanctions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The delisting shrink guard (prompt 091, FR-15): a list whose new entry count is below
 * {@value #MIN_RETAINED_RATIO} of the previous run's is treated as an upstream fault, so no tombstones are
 * written and the run is degraded with reason {@code LIST_SHRINK_GUARD}.
 */
public final class DelistingGuard {

    /** The smallest fraction of the previous count that is still trusted. */
    public static final double MIN_RETAINED_RATIO = 0.9;

    /** The degraded reason recorded when the guard fires. */
    public static final String SHRINK_REASON = "LIST_SHRINK_GUARD";

    private DelistingGuard() {}

    /** True when {@code current} is an implausible shrink of {@code previous}. */
    public static boolean implausible(int previous, int current) {
        return previous > 0 && current < previous * MIN_RETAINED_RATIO;
    }

    /** The ids of {@code previous} that are not in {@code current}, sorted. */
    public static List<String> gone(Collection<String> previous, Collection<String> current) {
        Set<String> currentSet = current == null ? Set.of() : Set.copyOf(current);
        Set<String> gone = new TreeSet<>();
        if (previous != null) {
            for (String id : previous) {
                if (!currentSet.contains(id)) {
                    gone.add(id);
                }
            }
        }
        return new ArrayList<>(gone);
    }

    /** A stable, sorted copy of {@code ids}. */
    public static List<String> sorted(Collection<String> ids) {
        List<String> out = new ArrayList<>(ids == null ? List.of() : ids);
        Collections.sort(out);
        return out;
    }
}
