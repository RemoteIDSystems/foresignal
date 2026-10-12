package ai.foresignal.reference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import java.util.TreeMap;

/**
 * Region tagging options, {@code app_setting['regionTagging']} (prompt 040), edited per source through
 * {@code updateSource} ({@code regionTagging}). Follows 038's precedent of keeping per-source flags in an
 * app setting rather than a {@code source_settings} column.
 *
 * @param modes per-source mode wire values; a missing source has its tier's default
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegionTaggingSettings(Map<String, String> modes) {

    /** Copies the map (sorted, for stable output). */
    public RegionTaggingSettings {
        modes = modes == null ? Map.of() : java.util.Collections.unmodifiableMap(new TreeMap<>(modes));
    }

    /** Nothing overridden. */
    public static RegionTaggingSettings defaults() {
        return new RegionTaggingSettings(null);
    }

    /** The operator's mode of {@code sourceId}, or null for the default. */
    public RegionTagMode override(String sourceId) {
        String value = modes.get(sourceId);
        return value == null ? null : RegionTagMode.fromWire(value);
    }

    /** This value with {@code sourceId}'s mode replaced; null leaves it unchanged. */
    public RegionTaggingSettings with(String sourceId, RegionTagMode mode) {
        if (mode == null) {
            return this;
        }
        Map<String, String> next = new TreeMap<>(modes);
        next.put(sourceId, mode.wire());
        return new RegionTaggingSettings(next);
    }
}
