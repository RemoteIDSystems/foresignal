package ai.foresignal.social.correlation;

import ai.foresignal.settings.Durations;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

/**
 * The family rules of the comparator (prompt 137, {@code intel/social/social-compare.json}, merged with the operator's
 * {@code social.compare.familyWindows}): window, distance tolerance, publication lag, whether admin-1 places suffice,
 * the canonical-event and finding families that count as physical evidence, and the negating records. Immutable.
 */
public final class CompareRules {

    /** The bundled resource. */
    public static final String RESOURCE = "intel/social/social-compare.json";

    /**
     * A record kind whose payload field value explicitly negates a claim of the family.
     *
     * @param kind the record kind
     * @param field the payload field
     * @param values the values that mean "normal" or "nothing happened"
     */
    public record Negation(String kind, String field, List<String> values) {

        /** Copies the list. */
        public Negation {
            values = values == null ? List.of() : List.copyOf(values);
        }
    }

    /**
     * The rule of one signal family.
     *
     * @param family the 119 signal family
     * @param before the window before the claim time
     * @param after the window after the claim time
     * @param toleranceKm the distance within which evidence matches
     * @param publicationLag how long after the window late evidence still revises
     * @param largeArea true when admin-1 claim places are precise enough
     * @param canonicalFamilies the 116 canonical families counted as evidence
     * @param findingFamilies the signal families of non-social findings counted as evidence
     * @param negations the negating records
     */
    public record FamilyRule(String family, Duration before, Duration after, double toleranceKm,
            Duration publicationLag, boolean largeArea, List<String> canonicalFamilies, List<String> findingFamilies,
            List<Negation> negations) {

        /** Copies the lists. */
        public FamilyRule {
            canonicalFamilies = canonicalFamilies == null ? List.of() : List.copyOf(canonicalFamilies);
            findingFamilies = findingFamilies == null ? List.of() : List.copyOf(findingFamilies);
            negations = negations == null ? List.of() : List.copyOf(negations);
        }

        /** The {@code familyWindows} entry view. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("before", Durations.format(before));
            out.put("after", Durations.format(after));
            out.put("toleranceKm", toleranceKm);
            out.put("publicationLag", Durations.format(publicationLag));
            return out;
        }
    }

    private static volatile CompareRules bundled;

    private final String version;
    private final Map<String, Object> defaults;
    private final Map<String, Map<String, Object>> families;

    private CompareRules(String version, Map<String, Object> defaults, Map<String, Map<String, Object>> families) {
        this.version = version;
        this.defaults = Map.copyOf(defaults);
        this.families = Map.copyOf(families);
    }

    /** The bundled rules (loaded once). */
    public static CompareRules bundled() {
        CompareRules current = bundled;
        if (current == null) {
            try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
                current = parse(JsonMapper.builder().build().readValue(in, Map.class));
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read " + RESOURCE, e);
            }
            bundled = current;
        }
        return current;
    }

    /** Parses a rules document. */
    @SuppressWarnings("unchecked")
    public static CompareRules parse(Map<?, ?> doc) {
        Map<String, Object> defaults = new LinkedHashMap<>();
        if (doc.get("defaults") instanceof Map<?, ?> d) {
            d.forEach((k, v) -> defaults.put(String.valueOf(k), v));
        }
        Map<String, Map<String, Object>> families = new LinkedHashMap<>();
        if (doc.get("families") instanceof Map<?, ?> f) {
            for (Map.Entry<?, ?> e : f.entrySet()) {
                if (e.getValue() instanceof Map<?, ?> m) {
                    families.put(String.valueOf(e.getKey()), new LinkedHashMap<>((Map<String, Object>) m));
                }
            }
        }
        return new CompareRules(doc.get("version") == null ? "1.0.0" : doc.get("version").toString(), defaults,
                families);
    }

    /** The rules version. */
    public String version() {
        return version;
    }

    /** The families with a bundled row. */
    public List<String> families() {
        return new ArrayList<>(families.keySet());
    }

    /**
     * The rule of a family: the bundled row over the defaults, then the operator's window (before, after,
     * toleranceKm, publicationLag) and the operator's default window when the family has no row of its own.
     *
     * @param family the signal family
     * @param defaultBefore the operator's default before (null for the bundled one)
     * @param defaultAfter the operator's default after (null for the bundled one)
     * @param override the operator's {@code familyWindows} entry, or null
     */
    public FamilyRule rule(String family, Duration defaultBefore, Duration defaultAfter, Map<?, ?> override) {
        Map<String, Object> row = families.getOrDefault(family, Map.of());
        Duration before = duration(row.get("before"), defaultBefore != null ? defaultBefore
                : duration(defaults.get("before"), Duration.ofHours(6)));
        Duration after = duration(row.get("after"), defaultAfter != null ? defaultAfter
                : duration(defaults.get("after"), Duration.ofHours(24)));
        double tolerance = number(row.get("toleranceKm"), number(defaults.get("toleranceKm"), 50));
        Duration lag = duration(row.get("publicationLag"), duration(defaults.get("publicationLag"),
                Duration.ofHours(24)));
        boolean largeArea = row.get("largeArea") instanceof Boolean b ? b
                : Boolean.TRUE.equals(defaults.get("largeArea"));
        if (override != null) {
            before = duration(override.get("before"), before);
            after = duration(override.get("after"), after);
            tolerance = number(override.get("toleranceKm"), tolerance);
            lag = duration(override.get("publicationLag"), lag);
        }
        List<String> canonical = strings(row.get("canonicalFamilies"));
        List<String> findings = row.containsKey("findingFamilies") ? strings(row.get("findingFamilies"))
                : family == null ? List.of() : List.of(family);
        List<Negation> negations = new ArrayList<>();
        if (row.get("negations") instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> n && n.get("kind") != null && n.get("field") != null) {
                    negations.add(new Negation(n.get("kind").toString(), n.get("field").toString(),
                            strings(n.get("values"))));
                }
            }
        }
        return new FamilyRule(family, before, after, tolerance, lag, largeArea, canonical, findings, negations);
    }

    static Duration duration(Object value, Duration fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Durations.parse(value.toString());
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    static double number(Object value, double fallback) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return value == null ? fallback : Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object o : list) {
                if (o != null && !o.toString().isBlank()) {
                    out.add(o.toString().strip());
                }
            }
        }
        return out;
    }
}
