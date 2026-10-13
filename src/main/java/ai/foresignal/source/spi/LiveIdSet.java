package ai.foresignal.source.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Remembers which external ids a complete upstream snapshot contained, so an adapter can tombstone
 * the ones that disappear (prompt 047; Adapter Guide, "Tombstone only from a complete, successful
 * response").
 *
 * <p>The framework never tombstones absent records ({@link FetchResult#complete()} is informational),
 * because a truncated response would otherwise delete the world. An adapter that knows its response
 * is the full current set calls {@link #absentSince} <strong>only after</strong> the response was
 * received in full and parsed without error; a failed, truncated or older response must never reach
 * it, which is what keeps stale data from reading as an all-clear.
 *
 * <p>The ids live in the adapter state under {@code live:{scope}}, newline-separated. The ids that
 * went absent are kept for one more run under {@code gone:{scope}} and reported again, so a tombstone
 * lost because the ingest of that run failed is retried once; a second tombstone for an entity that
 * is already tombstoned is skipped by the ingest decision table (Core-Data-Model §5, row 2). The
 * scope separates independent snapshots of one source, for example NWS alerts per {@code area}.
 */
public final class LiveIdSet {

    /** Prefix of the key holding the ids of the last complete snapshot. */
    public static final String LIVE_PREFIX = "live:";

    /** Prefix of the key holding the ids that went absent in the last snapshot. */
    public static final String GONE_PREFIX = "gone:";

    private LiveIdSet() {}

    /**
     * Compares {@code current} with the last complete snapshot of {@code scope}, remembers
     * {@code current}, and returns the ids to tombstone.
     *
     * @param state the adapter state
     * @param scope which snapshot this is; blank means the source's only one
     * @param current every id in the new complete snapshot
     * @return the previously live (or just-gone) ids missing from {@code current}, sorted
     */
    public static List<String> absentSince(AdapterStateStore state, String scope, Collection<String> current) {
        String key = scope == null || scope.isBlank() ? "default" : scope;
        Set<String> now = new LinkedHashSet<>(current);
        Set<String> previous = read(state, LIVE_PREFIX + key);
        Set<String> retry = read(state, GONE_PREFIX + key);
        Set<String> newlyGone = new TreeSet<>(previous);
        newlyGone.removeAll(now);
        Set<String> absent = new TreeSet<>(newlyGone);
        absent.addAll(retry);
        absent.removeAll(now);
        write(state, LIVE_PREFIX + key, now);
        write(state, GONE_PREFIX + key, newlyGone);
        return List.copyOf(absent);
    }

    /** The ids stored under {@code key}, empty when none are. */
    public static Set<String> read(AdapterStateStore state, String key) {
        String value = state.get(key).orElse("");
        Set<String> out = new LinkedHashSet<>();
        if (!value.isEmpty()) {
            Arrays.stream(value.split("\n")).filter(s -> !s.isBlank()).forEach(out::add);
        }
        return out;
    }

    private static void write(AdapterStateStore state, String key, Set<String> ids) {
        String value = String.join("\n", new TreeSet<>(ids));
        if (value.isEmpty()) {
            if (state.get(key).isPresent()) {
                state.remove(key);
            }
        } else if (!value.equals(state.get(key).orElse(null))) {
            state.put(key, value);
        }
    }
}
