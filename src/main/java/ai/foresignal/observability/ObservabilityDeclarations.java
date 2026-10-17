package ai.foresignal.observability;

import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.DeclaredObservability;
import ai.foresignal.source.spi.SourceDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Declared observability (prompt 116): a descriptor's {@code observability} field, else the entry of
 * {@code intel/observability.json}; and the observability family of each kind (the canonical-event families
 * of {@code canonical-rules.json}, the track families of {@code trackKinds}, else the kind itself).
 */
@Component
public class ObservabilityDeclarations {

    /** The bundled declarations resource. */
    public static final String RESOURCE = "intel/observability.json";

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final CanonicalRules rules;
    private final ObjectProvider<SourceRegistry> sources;
    private final Map<String, DeclaredObservability> bundled;
    private final Map<String, String> trackKinds;

    /** Loads the bundled declarations. */
    public ObservabilityDeclarations(JsonMapper json, CanonicalRules rules, ObjectProvider<SourceRegistry> sources) {
        this(read(json), rules, sources);
    }

    /** Declarations from a parsed document (tests). */
    public ObservabilityDeclarations(Map<String, Object> document, CanonicalRules rules,
            ObjectProvider<SourceRegistry> sources) {
        this.rules = rules;
        this.sources = sources;
        Map<String, DeclaredObservability> out = new LinkedHashMap<>();
        if (document.get("sources") instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                if (v instanceof Map<?, ?> m) {
                    out.put(String.valueOf(k), parse(m));
                }
            });
        }
        this.bundled = Map.copyOf(out);
        Map<String, String> tracks = new LinkedHashMap<>();
        if (document.get("trackKinds") instanceof Map<?, ?> t) {
            t.forEach((k, v) -> tracks.put(String.valueOf(k), String.valueOf(v)));
        }
        this.trackKinds = Map.copyOf(tracks);
    }

    private static Map<String, Object> read(JsonMapper json) {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            return json.readValue(in, MAP);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + RESOURCE, e);
        }
    }

    /** The declaration in force for {@code descriptor}: its own, else the bundled one, else null. */
    public DeclaredObservability declared(SourceDescriptor descriptor) {
        if (descriptor.observability() != null) {
            return descriptor.observability();
        }
        return bundled.get(descriptor.id());
    }

    /** The declaration in force for {@code sourceId}, or null. */
    public DeclaredObservability declared(String sourceId) {
        SourceRegistry registry = sources == null ? null : sources.getIfAvailable();
        if (registry != null && registry.contains(sourceId)) {
            return declared(registry.descriptor(sourceId));
        }
        return bundled.get(sourceId);
    }

    /** The observability family of {@code kind}. */
    public String familyOfKind(String kind) {
        String family = rules.familyOfKind(kind);
        if (family != null) {
            return family;
        }
        return trackKinds.getOrDefault(kind, kind);
    }

    /** True when {@code family} is a track family (presence proxy). */
    public boolean trackFamily(String family) {
        return trackKinds.containsValue(family);
    }

    /** The track families. */
    public Set<String> trackFamilies() {
        return new LinkedHashSet<>(trackKinds.values());
    }

    /** The kinds of {@code family}. */
    public Set<String> kindsOf(String family) {
        Set<String> out = new LinkedHashSet<>(rules.kindsOf(family));
        trackKinds.forEach((kind, f) -> {
            if (f.equals(family)) {
                out.add(kind);
            }
        });
        if (out.isEmpty()) {
            out.add(family);
        }
        return out;
    }

    /** The canonical-event families (capture–recapture). */
    public List<String> eventFamilies() {
        List<String> out = new ArrayList<>();
        for (CanonicalRules.FamilyRule rule : rules.families()) {
            out.add(rule.family());
        }
        return out;
    }

    static DeclaredObservability parse(Map<?, ?> map) {
        Map<String, DeclaredObservability.KindObservability> kinds = new LinkedHashMap<>();
        if (map.get("kinds") instanceof Map<?, ?> k) {
            k.forEach((kind, value) -> {
                if (value instanceof Map<?, ?> m) {
                    DeclaredObservability.Area area = null;
                    if (m.get("area") instanceof Map<?, ?> a) {
                        List<Double> bbox = null;
                        if (a.get("bbox") instanceof Collection<?> b) {
                            bbox = new ArrayList<>();
                            for (Object o : b) {
                                bbox.add(o instanceof Number n ? n.doubleValue() : 0.0);
                            }
                        }
                        area = new DeclaredObservability.Area(a.get("type") == null ? null : String.valueOf(a.get("type")),
                                bbox, strings(a.get("regionCodes")));
                    }
                    List<DeclaredObservability.Threshold> thresholds = new ArrayList<>();
                    if (m.get("thresholds") instanceof Collection<?> list) {
                        for (Object item : list) {
                            if (item instanceof Map<?, ?> t && t.get("field") != null && t.get("min") instanceof Number n) {
                                thresholds.add(new DeclaredObservability.Threshold(String.valueOf(t.get("field")),
                                        n.doubleValue(), t.get("area") == null ? null : String.valueOf(t.get("area"))));
                            }
                        }
                    }
                    kinds.put(String.valueOf(kind), new DeclaredObservability.KindObservability(area, thresholds,
                            m.get("typicalLatency") == null ? null : String.valueOf(m.get("typicalLatency")),
                            m.get("notes") == null ? null : String.valueOf(m.get("notes"))));
                }
            });
        }
        return new DeclaredObservability(kinds);
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof Collection<?> c) {
            for (Object o : c) {
                if (o != null) {
                    out.add(String.valueOf(o));
                }
            }
        }
        return out;
    }
}
