package ai.foresignal.analytics.family;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The signal-family registry (prompt 119, Intelligence-Design §5.4): one place that says which family a source, a
 * record kind, an intelligence event type, a finding type or an alert or anomaly rule tag belongs to.
 *
 * <p>Families: {@code ais-behaviour}, {@code aviation-military}, {@code aviation-ops}, {@code conflict},
 * {@code outage}, {@code internet-measurement}, {@code gnss-interference}, {@code nav-warning}, {@code attention},
 * {@code seismic}, {@code fire}, {@code weather-severe}, {@code cyber}, {@code markets}, {@code sanctions},
 * {@code health} and {@code space}, plus any the operator adds. Names that earlier prompts stored (101's
 * {@code aviation-behaviour}, {@code aviation-restriction}, {@code weather-hazard} and {@code grid-frequency}) are
 * aliases that {@link #canonical(String)} resolves; stored rows are never rewritten.
 *
 * <p>Rules are tried in order, operator rows from {@code intel.signalFamilies} first; the first rule whose every
 * match key equals the query wins. The bundled file is read once; operator rows are read on every call (they are
 * small and app settings are cached).
 */
@Component
public class SignalFamilyRegistry {

    /** The bundled registry. */
    public static final String RESOURCE = "intel/signal-families.json";

    /** The match keys a rule may use. */
    public static final List<String> MATCH_KEYS = List.of("sourceId", "kind", "intelEventType", "findingType",
            "ruleTag");

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    /**
     * One family.
     *
     * @param id the family id
     * @param label a human label
     * @param aliases names earlier prompts used for it
     */
    public record Family(String id, String label, List<String> aliases) {

        /** Copies the aliases. */
        public Family {
            aliases = aliases == null ? List.of() : List.copyOf(aliases);
        }
    }

    /**
     * One rule: every non-null key must equal the query's value.
     *
     * @param match the match keys and values
     * @param family the family
     */
    public record Rule(Map<String, String> match, String family) {

        /** Copies the match. */
        public Rule {
            match = match == null ? Map.of() : Map.copyOf(match);
        }

        /** True when every match key equals the query's value (and there is at least one key). */
        boolean matches(Query query) {
            if (match.isEmpty()) {
                return false;
            }
            for (Map.Entry<String, String> e : match.entrySet()) {
                if (!Objects.equals(e.getValue(), query.get(e.getKey()))) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * What to classify; any member may be null.
     *
     * @param sourceId the source
     * @param kind the record kind
     * @param intelEventType the 101 intelligence event type
     * @param findingType the 119 finding type
     * @param ruleTag one alert or anomaly rule tag
     */
    public record Query(String sourceId, String kind, String intelEventType, String findingType, String ruleTag) {

        /** A query by source and kind. */
        public static Query record(String sourceId, String kind) {
            return new Query(sourceId, kind, null, null, null);
        }

        /** A query by intelligence event type. */
        public static Query intelEvent(String type) {
            return new Query(null, null, type, null, null);
        }

        /** A query by finding type. */
        public static Query finding(String type) {
            return new Query(null, null, null, type, null);
        }

        /** A query by rule tag. */
        public static Query tag(String tag) {
            return new Query(null, null, null, null, tag);
        }

        String get(String key) {
            return switch (key) {
                case "sourceId" -> sourceId;
                case "kind" -> kind;
                case "intelEventType" -> intelEventType;
                case "findingType" -> findingType;
                case "ruleTag" -> ruleTag;
                default -> null;
            };
        }
    }

    private final AppSettingService settings;
    private final JsonMapper json;
    private final List<Family> families;
    private final List<Rule> rules;
    private final String version;

    /** Creates the registry from the bundled file. */
    public SignalFamilyRegistry(AppSettingService settings, JsonMapper json) {
        this.settings = settings;
        this.json = json;
        Map<String, Object> bundled = load(json);
        this.version = String.valueOf(bundled.getOrDefault("version", "1.0.0"));
        this.families = parseFamilies(bundled.get("families"));
        this.rules = parseRules(bundled.get("rules"));
    }

    /** The bundled file's version. */
    public String version() {
        return version;
    }

    /** Every family, operator families after the bundled ones. */
    public List<Family> families() {
        Map<String, Family> out = new LinkedHashMap<>();
        for (Family f : families) {
            out.put(f.id(), f);
        }
        for (Family f : parseFamilies(operator().get("families"))) {
            out.putIfAbsent(f.id(), f);
        }
        return List.copyOf(out.values());
    }

    /** Every family id. */
    public Set<String> ids() {
        Set<String> out = new LinkedHashSet<>();
        for (Family f : families()) {
            out.add(f.id());
        }
        return out;
    }

    /** True when {@code name} is a family id or an alias of one. */
    public boolean known(String name) {
        return canonical(name).isPresent();
    }

    /** The family id {@code name} names (itself, or the family it is an alias of), case-insensitive. */
    public Optional<String> canonical(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String n = name.strip().toLowerCase(Locale.ROOT);
        for (Family f : families()) {
            if (f.id().equals(n) || f.aliases().contains(n)) {
                return Optional.of(f.id());
            }
        }
        return Optional.empty();
    }

    /** The names (id and aliases) a family is stored under, for queries over older rows. */
    public Set<String> storedNames(String family) {
        Set<String> out = new LinkedHashSet<>();
        String id = canonical(family).orElse(family);
        out.add(id);
        for (Family f : families()) {
            if (f.id().equals(id)) {
                out.addAll(f.aliases());
            }
        }
        return out;
    }

    /** The canonical families of several stored names, unknown names left out. */
    public Set<String> canonicalAll(Collection<String> names) {
        Set<String> out = new LinkedHashSet<>();
        if (names != null) {
            for (String name : names) {
                canonical(name).ifPresent(out::add);
            }
        }
        return out;
    }

    /** The family of {@code query}: operator rules first, then the bundled ones. */
    public Optional<String> familyOf(Query query) {
        for (Rule rule : parseRules(operator().get("rules"))) {
            if (rule.matches(query)) {
                return canonical(rule.family()).or(() -> Optional.of(rule.family()));
            }
        }
        for (Rule rule : rules) {
            if (rule.matches(query)) {
                return Optional.of(rule.family());
            }
        }
        return Optional.empty();
    }

    /** The family of a record: by source first (a source rule names the source alone), then by kind. */
    public Optional<String> familyOfRecord(String sourceId, String kind) {
        Optional<String> exact = familyOf(Query.record(sourceId, kind));
        if (exact.isPresent()) {
            return exact;
        }
        Optional<String> bySource = familyOf(new Query(sourceId, null, null, null, null));
        return bySource.isPresent() ? bySource : familyOf(new Query(null, kind, null, null, null));
    }

    /** The family of the first of {@code tags} that has one. */
    public Optional<String> familyOfTags(Collection<String> tags) {
        if (tags != null) {
            for (String tag : tags) {
                Optional<String> family = familyOf(Query.tag(tag == null ? null : tag.toLowerCase(Locale.ROOT)));
                if (family.isPresent()) {
                    return family;
                }
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------------------- parsing

    @SuppressWarnings("unchecked")
    private Map<String, Object> operator() {
        try {
            Map<String, Object> value = settings.get(SettingKeys.INTEL_SIGNAL_FAMILIES, Map.class).orElse(null);
            return value == null ? Map.of() : value;
        } catch (RuntimeException e) {
            return Map.of();
        }
    }

    private static Map<String, Object> load(JsonMapper json) {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            return json.readValue(in, MAP);
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }

    static List<Family> parseFamilies(Object value) {
        List<Family> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> m && m.get("id") instanceof String id && !id.isBlank()) {
                    List<String> aliases = new ArrayList<>();
                    if (m.get("aliases") instanceof List<?> a) {
                        for (Object alias : a) {
                            if (alias instanceof String s && !s.isBlank()) {
                                aliases.add(s.strip().toLowerCase(Locale.ROOT));
                            }
                        }
                    }
                    Object label = m.get("label");
                    out.add(new Family(id.strip().toLowerCase(Locale.ROOT), label == null ? id : label.toString(),
                            aliases));
                }
            }
        }
        return out;
    }

    static List<Rule> parseRules(Object value) {
        List<Rule> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> m && m.get("family") instanceof String family
                        && m.get("match") instanceof Map<?, ?> match) {
                    Map<String, String> keys = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> e : match.entrySet()) {
                        if (e.getKey() instanceof String k && MATCH_KEYS.contains(k) && e.getValue() != null) {
                            keys.put(k, e.getValue().toString());
                        }
                    }
                    if (!keys.isEmpty()) {
                        out.add(new Rule(keys, family.strip().toLowerCase(Locale.ROOT)));
                    }
                }
            }
        }
        return out;
    }
}
