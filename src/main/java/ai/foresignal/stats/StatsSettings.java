package ai.foresignal.stats;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stats options, {@code app_setting['stats']} (prompt 038), edited per source through
 * {@code updateSource} ({@code statsEnabled}, {@code statsIncludePrivate}).
 *
 * @param enabled per-source {@code statsEnabled}; a missing source is enabled (exclude noisy sources
 *     by setting false)
 * @param includePrivate per-source {@code stats.includePrivate[sourceId]}; private sources are rolled up
 *     only when true
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StatsSettings(Map<String, Boolean> enabled, Map<String, Boolean> includePrivate) {

    /** Copies the maps (sorted, for stable output). */
    public StatsSettings {
        enabled = enabled == null ? Map.of() : java.util.Collections.unmodifiableMap(new TreeMap<>(enabled));
        includePrivate = includePrivate == null ? Map.of()
                : java.util.Collections.unmodifiableMap(new TreeMap<>(includePrivate));
    }

    /** Nothing overridden. */
    public static StatsSettings defaults() {
        return new StatsSettings(null, null);
    }

    /** {@code statsEnabled} of {@code sourceId} (default true). */
    public boolean enabled(String sourceId) {
        return enabled.getOrDefault(sourceId, true);
    }

    /** {@code statsIncludePrivate} of {@code sourceId} (default false). */
    public boolean includePrivate(String sourceId) {
        return includePrivate.getOrDefault(sourceId, false);
    }

    /** This value with the source's flags replaced; a null flag leaves it unchanged. */
    public StatsSettings with(String sourceId, Boolean statsEnabled, Boolean statsIncludePrivate) {
        Map<String, Boolean> e = new TreeMap<>(enabled);
        Map<String, Boolean> p = new TreeMap<>(includePrivate);
        if (statsEnabled != null) {
            if (statsEnabled) {
                e.remove(sourceId);
            } else {
                e.put(sourceId, false);
            }
        }
        if (statsIncludePrivate != null) {
            if (statsIncludePrivate) {
                p.put(sourceId, true);
            } else {
                p.remove(sourceId);
            }
        }
        return new StatsSettings(e, p);
    }
}
