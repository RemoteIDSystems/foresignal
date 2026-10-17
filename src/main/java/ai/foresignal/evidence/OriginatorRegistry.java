package ai.foresignal.evidence;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The originator registry (prompt 116): maps every source to the {@link Originator} its observations trace
 * back to, from three layers, the later winning:
 *
 * <ol>
 *   <li>the bundled {@code intel/originators.json} (built from {@code docs/Source-Catalog.md}): originators,
 *       exact source mappings, family patterns ending in {@code *} (the longest matching prefix wins) and
 *       upstream reference fields;
 *   <li>the operator's setting {@value #SETTING}, an object of the same shape that adds or replaces
 *       originators, mappings, patterns and upstream references;
 *   <li>a descriptor's own {@code originator} and {@code upstreamRefFields}.
 * </ol>
 *
 * <p>A source nothing maps is its own originator with class {@code aggregator} (never silently merged with
 * another source), and the sources in that state are listed in one warning at startup.
 */
@Component
public class OriginatorRegistry {

    private static final Logger log = LoggerFactory.getLogger(OriginatorRegistry.class);

    /** The bundled registry resource. */
    public static final String RESOURCE = "intel/originators.json";

    /** The operator setting that extends the registry. */
    public static final String SETTING = ai.foresignal.settings.SettingKeys.INTEL_ORIGINATORS;

    /** The placeholder meaning "the source id itself". */
    static final String SOURCE_PLACEHOLDER = "$source";

    /** How deep {@link #aggregates} follows republication chains. */
    static final int MAX_AGGREGATION_DEPTH = 4;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final JsonMapper json;
    private final AppSettingService settings;
    private final ObjectProvider<SourceRegistry> sources;
    private final Model bundled;

    private volatile Object lastOverride;
    private volatile Model merged;

    /** Creates the registry and loads the bundled file. */
    public OriginatorRegistry(JsonMapper json, AppSettingService settings, ObjectProvider<SourceRegistry> sources) {
        this.json = json;
        this.settings = settings;
        this.sources = sources;
        this.bundled = Model.parse(readBundled(json));
        this.merged = bundled;
    }

    /** A registry over a parsed document only (tests): no settings and no descriptors. */
    public static OriginatorRegistry of(Map<String, Object> document, JsonMapper json) {
        return new OriginatorRegistry(json, document);
    }

    private OriginatorRegistry(JsonMapper json, Map<String, Object> document) {
        this.json = json;
        this.settings = null;
        this.sources = null;
        this.bundled = Model.parse(document);
        this.merged = bundled;
    }

    private static Map<String, Object> readBundled(JsonMapper json) {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            return json.readValue(in, MAP);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + RESOURCE, e);
        }
    }

    /** Lists the sources that fall back to the default originator, once, when the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    public void warnUnknown() {
        Set<String> unknown = unknownSources();
        if (!unknown.isEmpty()) {
            log.warn("{} source(s) have no originator in {} or the {} setting and count as their own originator"
                    + " (class aggregator): {}", unknown.size(), RESOURCE, SETTING, unknown);
        }
    }

    /** The registered sources that resolve to the default originator, sorted. */
    public Set<String> unknownSources() {
        Set<String> out = new TreeSet<>();
        SourceRegistry registry = sources == null ? null : sources.getIfAvailable();
        if (registry == null) {
            return out;
        }
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.originator() == null && mapping(model(), descriptor.id()) == null) {
                out.add(descriptor.id());
            }
        }
        return out;
    }

    /** True when {@code sourceId} has an explicit mapping (descriptor, setting or bundled file). */
    public boolean mapped(String sourceId) {
        SourceDescriptor descriptor = descriptor(sourceId);
        return (descriptor != null && descriptor.originator() != null) || mapping(model(), sourceId) != null;
    }

    /**
     * The originator of {@code sourceId}: the descriptor's {@code originator}, else the setting's or bundled
     * mapping, else {@link Originator#defaultFor(String) the source itself}.
     */
    public Originator resolve(String sourceId) {
        if (sourceId == null) {
            return Originator.defaultFor("unknown");
        }
        Model model = model();
        SourceDescriptor descriptor = descriptor(sourceId);
        if (descriptor != null && descriptor.originator() != null && !descriptor.originator().isBlank()) {
            Originator declared = model.originators.get(descriptor.originator());
            return declared != null ? declared
                    : new Originator(descriptor.originator(), Originator.DEFAULT_CLASS, null, List.of(), null, List.of());
        }
        Mapping mapping = mapping(model, sourceId);
        if (mapping == null) {
            return Originator.defaultFor(sourceId);
        }
        String id = SOURCE_PLACEHOLDER.equals(mapping.originator()) ? sourceId : mapping.originator();
        Originator known = model.originators.get(id);
        if (known != null) {
            return known;
        }
        return new Originator(id, mapping.originatorClass(), null, List.of(), mapping.notes(), List.of());
    }

    /** The originator with id {@code id}, when the registry declares it. */
    public Optional<Originator> originator(String id) {
        return Optional.ofNullable(id == null ? null : model().originators.get(id));
    }

    /** Every declared originator, sorted by id. */
    public List<Originator> originators() {
        List<Originator> out = new ArrayList<>(model().originators.values());
        out.sort(Comparator.comparing(Originator::id));
        return out;
    }

    /**
     * The upstream references of {@code sourceId}: the descriptor's {@code upstreamRefFields} when it declares
     * any, else the setting's or bundled entry (exact id, else the longest matching pattern).
     */
    public UpstreamRefs upstreamRefs(String sourceId) {
        SourceDescriptor descriptor = descriptor(sourceId);
        if (descriptor != null && !descriptor.upstreamRefFields().isEmpty()) {
            return UpstreamRefs.parse(descriptor.upstreamRefFields());
        }
        return UpstreamRefs.parse(lookup(model().upstreamRefs, sourceId));
    }

    /** The upstream reference fields in force for {@code descriptor}, in the descriptor notation. */
    public List<String> upstreamRefFields(SourceDescriptor descriptor) {
        if (!descriptor.upstreamRefFields().isEmpty()) {
            return descriptor.upstreamRefFields();
        }
        List<String> fields = lookup(model().upstreamRefs, descriptor.id());
        return fields == null ? List.of() : fields;
    }

    /**
     * True when {@code a} republishes {@code b}: {@code a.aggregates} names {@code b}'s id or independence group,
     * directly or through a chain of at most {@value #MAX_AGGREGATION_DEPTH} declared originators.
     */
    public boolean aggregates(Originator a, Originator b) {
        return aggregates(a, b, 0, new HashSet<>());
    }

    private boolean aggregates(Originator a, Originator b, int depth, Set<String> seen) {
        if (a == null || b == null || depth > MAX_AGGREGATION_DEPTH || !seen.add(a.id())) {
            return false;
        }
        for (String target : a.aggregates()) {
            if (target.equals(b.id()) || target.equals(b.independenceGroup())) {
                return true;
            }
            Originator next = model().originators.get(target);
            if (next != null && aggregates(next, b, depth + 1, seen)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True when observations of {@code a} and {@code b} may count as independent: different roots and neither
     * republishes the other.
     */
    public boolean independent(Originator a, Originator b) {
        return !Objects.equals(a.root(), b.root()) && !aggregates(a, b) && !aggregates(b, a);
    }

    /**
     * The originator a citation names: a URL or domain matched against the declared {@code domains} (the
     * domain or a subdomain of it), or a name matched against originator ids (case-insensitive).
     */
    public Optional<Originator> byCitation(String citation) {
        String normalized = normalizeCitation(citation);
        if (normalized == null) {
            return Optional.empty();
        }
        Model model = model();
        Originator byId = model.originators.get(normalized);
        if (byId != null) {
            return Optional.of(byId);
        }
        for (Originator originator : model.originators.values()) {
            if (originator.id().equalsIgnoreCase(normalized)) {
                return Optional.of(originator);
            }
            for (String domain : originator.domains()) {
                String d = domain.toLowerCase(Locale.ROOT);
                if (normalized.equals(d) || normalized.endsWith("." + d)) {
                    return Optional.of(originator);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * A citation as a comparable key: the lower-case host of a URL without {@code www.}, or the trimmed lower-case
     * text; null for nothing.
     */
    public static String normalizeCitation(String citation) {
        if (citation == null || citation.isBlank()) {
            return null;
        }
        String text = citation.strip();
        if (text.contains("://")) {
            try {
                String host = URI.create(text.replace(" ", "%20")).getHost();
                if (host != null) {
                    text = host;
                }
            } catch (IllegalArgumentException e) {
                // keep the text as it is
            }
        }
        text = text.toLowerCase(Locale.ROOT);
        if (text.startsWith("www.")) {
            text = text.substring(4);
        }
        return text.isEmpty() ? null : text;
    }

    // ------------------------------------------------------------------------- model

    private SourceDescriptor descriptor(String sourceId) {
        SourceRegistry registry = sources == null ? null : sources.getIfAvailable();
        if (registry == null || sourceId == null || !registry.contains(sourceId)) {
            return null;
        }
        try {
            return registry.descriptor(sourceId);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The bundled model merged with the operator's setting, rebuilt when the setting changes. */
    Model model() {
        if (settings == null) {
            return merged;
        }
        Object override;
        try {
            override = settings.get(SETTING, Map.class).orElse(null);
        } catch (RuntimeException e) {
            override = null;
        }
        if (!Objects.equals(override, lastOverride)) {
            Model next = bundled;
            if (override instanceof Map<?, ?> map) {
                try {
                    next = bundled.with(Model.parse(json.convertValue(map, MAP)));
                } catch (RuntimeException e) {
                    log.warn("The {} setting is not a valid originator registry and is ignored: {}", SETTING,
                            e.toString());
                }
            }
            merged = next;
            lastOverride = override;
        }
        return merged;
    }

    private static Mapping mapping(Model model, String sourceId) {
        Mapping exact = model.sources.get(sourceId);
        if (exact != null) {
            return exact;
        }
        String best = null;
        for (String pattern : model.patterns.keySet()) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            if (sourceId.startsWith(prefix) && (best == null || prefix.length() > best.length() - 1)) {
                best = pattern;
            }
        }
        return best == null ? null : model.patterns.get(best);
    }

    private static List<String> lookup(Map<String, List<String>> map, String sourceId) {
        List<String> exact = map.get(sourceId);
        if (exact != null) {
            return exact;
        }
        String best = null;
        for (String key : map.keySet()) {
            if (key.endsWith("*") && sourceId.startsWith(key.substring(0, key.length() - 1))
                    && (best == null || key.length() > best.length())) {
                best = key;
            }
        }
        return best == null ? null : map.get(best);
    }

    /**
     * One source mapping.
     *
     * @param originator the originator id, or {@value #SOURCE_PLACEHOLDER}
     * @param originatorClass the class for an originator the registry does not declare
     * @param notes free text
     */
    record Mapping(String originator, String originatorClass, String notes) {}

    /** The parsed registry. */
    static final class Model {

        final Map<String, Originator> originators;
        final Map<String, Mapping> sources;
        final Map<String, Mapping> patterns;
        final Map<String, List<String>> upstreamRefs;

        Model(Map<String, Originator> originators, Map<String, Mapping> sources, Map<String, Mapping> patterns,
                Map<String, List<String>> upstreamRefs) {
            this.originators = originators;
            this.sources = sources;
            this.patterns = patterns;
            this.upstreamRefs = upstreamRefs;
        }

        /** This model with {@code other}'s entries added, {@code other} winning on equal keys. */
        Model with(Model other) {
            Map<String, Originator> o = new LinkedHashMap<>(originators);
            o.putAll(other.originators);
            Map<String, Mapping> s = new LinkedHashMap<>(sources);
            s.putAll(other.sources);
            Map<String, Mapping> p = new LinkedHashMap<>(patterns);
            p.putAll(other.patterns);
            Map<String, List<String>> u = new LinkedHashMap<>(upstreamRefs);
            u.putAll(other.upstreamRefs);
            return new Model(o, s, p, u);
        }

        static Model parse(Map<String, Object> document) {
            Map<String, Originator> originators = new LinkedHashMap<>();
            for (Object item : list(document.get("originators"))) {
                if (item instanceof Map<?, ?> m && m.get("id") != null) {
                    Originator originator = new Originator(String.valueOf(m.get("id")), text(m.get("class")),
                            text(m.get("independenceGroup")), strings(m.get("aggregates")), text(m.get("notes")),
                            strings(m.get("domains")));
                    originators.put(originator.id(), originator);
                }
            }
            Map<String, Mapping> sources = new LinkedHashMap<>();
            Map<String, Mapping> patterns = new LinkedHashMap<>();
            readMappings(document.get("sources"), sources, patterns);
            readMappings(document.get("patterns"), sources, patterns);
            Map<String, List<String>> upstream = new LinkedHashMap<>();
            if (document.get("upstreamRefs") instanceof Map<?, ?> refs) {
                refs.forEach((k, v) -> upstream.put(String.valueOf(k), strings(v)));
            }
            return new Model(originators, sources, patterns, upstream);
        }

        private static void readMappings(Object value, Map<String, Mapping> sources, Map<String, Mapping> patterns) {
            if (!(value instanceof Map<?, ?> map)) {
                return;
            }
            map.forEach((k, v) -> {
                String key = String.valueOf(k);
                Mapping mapping;
                if (v instanceof Map<?, ?> m) {
                    mapping = new Mapping(text(m.get("originator")), text(m.get("class")), text(m.get("notes")));
                } else {
                    mapping = new Mapping(text(v), null, null);
                }
                if (mapping.originator() == null) {
                    return;
                }
                if (key.endsWith("*")) {
                    patterns.put(key, mapping);
                } else {
                    sources.put(key, mapping);
                }
            });
        }

        private static List<?> list(Object value) {
            return value instanceof List<?> l ? l : List.of();
        }

        private static List<String> strings(Object value) {
            List<String> out = new ArrayList<>();
            if (value instanceof Collection<?> c) {
                for (Object item : c) {
                    if (item != null) {
                        out.add(String.valueOf(item));
                    }
                }
            } else if (value != null) {
                out.add(String.valueOf(value));
            }
            return out;
        }

        private static String text(Object value) {
            return value == null || String.valueOf(value).isBlank() ? null : String.valueOf(value);
        }
    }
}
