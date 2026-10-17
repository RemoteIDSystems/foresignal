package ai.foresignal.source.spi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a source declares it can observe, per kind (prompt 116): where, above which thresholds and how fast.
 * Carried by the optional descriptor field {@code observability}; cataloged sources get theirs from
 * {@code intel/observability.json}. The {@code ObservabilityService} uses a declaration as the low-confidence
 * {@code declared} fallback when no empirical estimate exists.
 *
 * @param kinds the declaration of each kind
 */
public record DeclaredObservability(Map<String, KindObservability> kinds) {

    /** Copies the map, keeping its order. */
    public DeclaredObservability {
        kinds = kinds == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(kinds));
    }

    /** The declaration of {@code kind}, or null when the source declares nothing for it. */
    public KindObservability kind(String kind) {
        return kinds.get(kind);
    }

    /**
     * The declared footprint of one kind.
     *
     * @param area where the source can observe
     * @param thresholds the detection thresholds, e.g. {@code {field: mag, min: 2.5, area: US}}
     * @param typicalLatency the typical publication latency (ISO-8601 or shorthand such as {@code 15m}), or null
     * @param notes free text, or null
     */
    public record KindObservability(Area area, List<Threshold> thresholds, String typicalLatency, String notes) {

        /** Fills the defaults: a global area and no thresholds. */
        public KindObservability {
            area = area == null ? Area.GLOBAL : area;
            thresholds = thresholds == null ? List.of() : List.copyOf(thresholds);
        }
    }

    /**
     * Where a source can observe.
     *
     * @param type {@code global}, {@code bbox} or {@code regions}
     * @param bbox {@code [minLon, minLat, maxLon, maxLat]} for {@code bbox}, else null
     * @param regionCodes 040 region codes for {@code regions}, else empty
     */
    public record Area(String type, List<Double> bbox, List<String> regionCodes) {

        /** The whole world. */
        public static final Area GLOBAL = new Area("global", null, List.of());

        /** Fills the defaults. */
        public Area {
            type = type == null || type.isBlank() ? "global" : type;
            bbox = bbox == null ? null : List.copyOf(bbox);
            regionCodes = regionCodes == null ? List.of() : List.copyOf(regionCodes);
        }
    }

    /**
     * A detection threshold: the source reports values of {@code field} at or above {@code min} inside
     * {@code area} ({@code global} or a 040 region code).
     *
     * @param field the payload field, e.g. {@code mag}
     * @param min the smallest value reported
     * @param area {@code global} (default) or a region code
     */
    public record Threshold(String field, double min, String area) {

        /** A missing area is global. */
        public Threshold {
            area = area == null || area.isBlank() ? "global" : area;
        }
    }
}
