package ai.foresignal.object;

import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.evidence.OriginatorRegistry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Classifies evidence for dossier facets (prompt 129): {@code imagery | signals | reports | documents | sensor |
 * social | other}. Imagery kinds come first; otherwise the 119 signal family of the record decides (aviation, AIS,
 * GNSS and internet measurement are signals; conflict, warnings, sanctions, health and cyber are reports; seismic,
 * weather, fire and space are sensor; attention and markets are other), and failing that the 116 originator class of
 * the source (media and agencies report, sensor networks sense, social is social).
 */
@Component
public class EvidenceClasses {

    /** The classes, in display order. */
    public static final List<String> CLASSES = List.of("imagery", "signals", "reports", "documents", "sensor",
            "social", "other");

    static final Set<String> IMAGERY_KINDS = Set.of("satellite-scene", "raster-frame", "satellite-pass");

    static final Map<String, String> BY_FAMILY = Map.ofEntries(
            Map.entry("ais-behaviour", "signals"), Map.entry("aviation-military", "signals"),
            Map.entry("aviation-ops", "signals"), Map.entry("gnss-interference", "signals"),
            Map.entry("internet-measurement", "signals"), Map.entry("outage", "signals"),
            Map.entry("conflict", "reports"), Map.entry("nav-warning", "reports"), Map.entry("sanctions", "reports"),
            Map.entry("health", "reports"), Map.entry("cyber", "reports"), Map.entry("seismic", "sensor"),
            Map.entry("weather-severe", "sensor"), Map.entry("fire", "sensor"), Map.entry("space", "sensor"),
            Map.entry("attention", "other"), Map.entry("markets", "other"), Map.entry("social", "social"));

    static final Map<String, String> BY_ORIGINATOR = Map.of("sensor-network", "sensor", "agency", "reports",
            "aggregator", "reports", "media", "reports", "crowd", "social", "social", "social", "operator", "other",
            "derived", "other");

    private final ObjectProvider<SignalFamilyRegistry> families;
    private final ObjectProvider<OriginatorRegistry> originators;

    /** Creates the component. */
    public EvidenceClasses(ObjectProvider<SignalFamilyRegistry> families,
            ObjectProvider<OriginatorRegistry> originators) {
        this.families = families;
        this.originators = originators;
    }

    /** The evidence class of a record of {@code sourceId} and {@code kind}. */
    public String classify(String sourceId, String kind) {
        if (kind != null && IMAGERY_KINDS.contains(kind)) {
            return "imagery";
        }
        if (kind != null && (kind.contains("filing") || kind.contains("document"))) {
            return "documents";
        }
        SignalFamilyRegistry registry = families.getIfAvailable();
        if (registry != null && sourceId != null) {
            try {
                String family = registry.familyOfRecord(sourceId, kind).orElse(null);
                if (family != null && BY_FAMILY.containsKey(family)) {
                    return BY_FAMILY.get(family);
                }
            } catch (RuntimeException e) {
                // fall through to the originator
            }
        }
        OriginatorRegistry registry2 = originators.getIfAvailable();
        if (registry2 != null && sourceId != null) {
            try {
                String c = registry2.resolve(sourceId).originatorClass();
                if (c != null && BY_ORIGINATOR.containsKey(c)) {
                    return BY_ORIGINATOR.get(c);
                }
            } catch (RuntimeException e) {
                // other
            }
        }
        return "other";
    }

    /** The signal family of a record, or null. */
    public String family(String sourceId, String kind) {
        SignalFamilyRegistry registry = families.getIfAvailable();
        if (registry == null || sourceId == null) {
            return null;
        }
        try {
            return registry.familyOfRecord(sourceId, kind).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Facet counts.
     *
     * @param items {@code [sourceId, kind]} pairs
     * @return {@code {byEvidenceClass, bySource}}
     */
    public Map<String, Object> facets(List<String[]> items) {
        Map<String, Integer> byClass = new TreeMap<>();
        Map<String, Integer> bySource = new TreeMap<>();
        for (String[] item : items) {
            byClass.merge(classify(item[0], item[1]), 1, Integer::sum);
            if (item[0] != null) {
                bySource.merge(item[0], 1, Integer::sum);
            }
        }
        return Map.of("byEvidenceClass", byClass, "bySource", bySource);
    }
}
