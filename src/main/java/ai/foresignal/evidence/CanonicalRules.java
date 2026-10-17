package ai.foresignal.evidence;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The canonical-event rules (prompt 116, {@code intel/canonical-rules.json}): which event-class kinds the
 * {@code EvidenceResolver} links, the family each belongs to, and how observations of a family are matched.
 * Families are tried in file order; an input may restrict a kind with a {@code where} clause (GDACS
 * {@code disaster} records with {@code eventType} {@code EQ} are earthquakes).
 */
@Component
public class CanonicalRules {

    /** The bundled rules resource. */
    public static final String RESOURCE = "intel/canonical-rules.json";

    /** The default {@code evidence.minLinkConfidence}. */
    public static final double DEFAULT_MIN_LINK_CONFIDENCE = 0.5;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final List<FamilyRule> families;
    private final Map<String, String> hazardTypes;
    private final double minLinkConfidence;
    private final String version;

    /** Loads the bundled rules. */
    @Autowired
    public CanonicalRules(JsonMapper json) {
        this(read(json));
    }

    /** Rules from a parsed document (tests). */
    public CanonicalRules(Map<String, Object> document) {
        List<FamilyRule> out = new ArrayList<>();
        for (Object item : list(document.get("families"))) {
            if (item instanceof Map<?, ?> m) {
                out.add(FamilyRule.parse(m));
            }
        }
        this.families = List.copyOf(out);
        Map<String, String> hazards = new LinkedHashMap<>();
        if (document.get("hazardTypes") instanceof Map<?, ?> h) {
            h.forEach((k, v) -> hazards.put(String.valueOf(k).toLowerCase(Locale.ROOT), String.valueOf(v)));
        }
        this.hazardTypes = Map.copyOf(hazards);
        Object min = document.get("minLinkConfidence");
        this.minLinkConfidence = min instanceof Number n ? n.doubleValue() : DEFAULT_MIN_LINK_CONFIDENCE;
        this.version = document.get("version") == null ? "1.0.0" : String.valueOf(document.get("version"));
    }

    private static Map<String, Object> read(JsonMapper json) {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            return json.readValue(in, MAP);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + RESOURCE, e);
        }
    }

    /** The family rules, in order. */
    public List<FamilyRule> families() {
        return families;
    }

    /** The rule of {@code family}, or null. */
    public FamilyRule family(String family) {
        for (FamilyRule rule : families) {
            if (rule.family().equals(family)) {
                return rule;
            }
        }
        return null;
    }

    /** Every kind any family links. */
    public Set<String> kinds() {
        Set<String> out = new LinkedHashSet<>();
        for (FamilyRule rule : families) {
            for (Input input : rule.inputs()) {
                out.add(input.kind());
            }
        }
        return out;
    }

    /** The kinds of {@code family}, or empty. */
    public Set<String> kindsOf(String family) {
        FamilyRule rule = family(family);
        Set<String> out = new LinkedHashSet<>();
        if (rule != null) {
            for (Input input : rule.inputs()) {
                out.add(input.kind());
            }
        }
        return out;
    }

    /**
     * The family a version of {@code kind} with {@code payload} belongs to: the first family with a matching
     * input, or null when the kind is not event-class.
     */
    public FamilyRule familyOf(String kind, Map<String, Object> payload) {
        for (FamilyRule rule : families) {
            for (Input input : rule.inputs()) {
                if (input.kind().equals(kind) && input.matches(payload)) {
                    return rule;
                }
            }
        }
        return null;
    }

    /** The family a kind belongs to regardless of {@code where} clauses (the first listing it), or null. */
    public String familyOfKind(String kind) {
        for (FamilyRule rule : families) {
            for (Input input : rule.inputs()) {
                if (input.kind().equals(kind) && input.where() == null) {
                    return rule.family();
                }
            }
        }
        return null;
    }

    /** A hazard type normalized through the synonym table, or null when unknown. */
    public String hazard(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return hazardTypes.get(raw.strip().toLowerCase(Locale.ROOT));
    }

    /** {@code evidence.minLinkConfidence} as bundled. */
    public double minLinkConfidence() {
        return minLinkConfidence;
    }

    /** The rules file version. */
    public String version() {
        return version;
    }

    /**
     * One input of a family.
     *
     * @param kind the record kind
     * @param where an optional restriction, or null
     */
    public record Input(String kind, Where where) {

        /** True when {@code payload} satisfies {@link #where()}. */
        public boolean matches(Map<String, Object> payload) {
            if (where == null) {
                return true;
            }
            Object value = payload == null ? null : payload.get(where.field());
            return value != null && where.in().contains(String.valueOf(value));
        }
    }

    /**
     * A restriction: {@code payload[field]} is one of {@code in}.
     *
     * @param field the payload field
     * @param in the accepted values
     */
    public record Where(String field, Set<String> in) {}

    /**
     * An attribute compared by proximity rules (the earthquake magnitude).
     *
     * @param name the attribute name in link reasons
     * @param fields the payload fields tried in order
     * @param maxDelta the largest difference that still matches
     */
    public record Attribute(String name, List<String> fields, double maxDelta) {}

    /**
     * Threshold bands of a family ({@code M2.5-3}, {@code M3-3.5}, …, {@code M7+}).
     *
     * @param prefix the band label prefix
     * @param start the lowest band's lower bound
     * @param step the band width
     * @param end the lower bound of the open top band
     */
    public record Bands(String prefix, double start, double step, double end) {

        /** The band label of {@code value}, or null below {@link #start()} or for no value. */
        public String bandOf(Double value) {
            if (value == null || !Double.isFinite(value) || value < start) {
                return null;
            }
            if (value >= end) {
                return prefix + format(end) + "+";
            }
            double lower = start + Math.floor((value - start) / step) * step;
            return prefix + format(lower) + "-" + format(lower + step);
        }

        /** The lower bound a band label names, or null when it does not parse. */
        public static Double lowerBound(String band) {
            if (band == null) {
                return null;
            }
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("([0-9]+(?:\\.[0-9]+)?)").matcher(band);
            return m.find() ? Double.valueOf(m.group(1)) : null;
        }

        private static String format(double value) {
            return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
        }
    }

    /**
     * One family's matching rule.
     *
     * @param family the family, e.g. {@code seismic}
     * @param inputs the kinds it links
     * @param match {@code proximity}, {@code cell-day}, {@code outage}, {@code hazard} or {@code identifier}
     * @param maxDtSeconds the time tolerance, seconds (0 when none)
     * @param maxKm the distance tolerance, km (0 when none)
     * @param attribute the compared attribute, or null
     * @param bands the threshold bands, or null
     * @param cellDegrees the cell size of {@code cell-day}, degrees
     * @param countryFields the country fields of {@code outage}
     * @param asnFields the ASN fields of {@code outage}
     * @param startFields interval start fields of {@code outage}
     * @param endFields interval end fields of {@code outage}
     * @param typeFields hazard type fields of {@code hazard}
     */
    public record FamilyRule(String family, List<Input> inputs, String match, double maxDtSeconds, double maxKm,
            Attribute attribute, Bands bands, double cellDegrees, List<String> countryFields, List<String> asnFields,
            List<String> startFields, List<String> endFields, List<String> typeFields) {

        /** The time tolerance used for {@code TIMESTAMP_CONFLICT} (3 × this), seconds; one day when undeclared. */
        public double timeTolerance() {
            if ("cell-day".equals(match)) {
                return 86_400;
            }
            return maxDtSeconds > 0 ? maxDtSeconds : 86_400;
        }

        static FamilyRule parse(Map<?, ?> m) {
            List<Input> inputs = new ArrayList<>();
            for (Object item : list(m.get("inputs"))) {
                if (item instanceof Map<?, ?> in && in.get("kind") != null) {
                    Where where = null;
                    if (in.get("where") instanceof Map<?, ?> w && w.get("field") != null) {
                        where = new Where(String.valueOf(w.get("field")), new LinkedHashSet<>(strings(w.get("in"))));
                    }
                    inputs.add(new Input(String.valueOf(in.get("kind")), where));
                }
            }
            Attribute attribute = null;
            if (m.get("attribute") instanceof Map<?, ?> a) {
                attribute = new Attribute(String.valueOf(a.get("name")), strings(a.get("fields")),
                        number(a.get("maxDelta"), 0));
            }
            Bands bands = null;
            if (m.get("bands") instanceof Map<?, ?> b) {
                bands = new Bands(b.get("prefix") == null ? "" : String.valueOf(b.get("prefix")),
                        number(b.get("start"), 0), number(b.get("step"), 1), number(b.get("end"), 10));
            }
            return new FamilyRule(String.valueOf(m.get("family")), List.copyOf(inputs),
                    m.get("match") == null ? "proximity" : String.valueOf(m.get("match")),
                    number(m.get("maxDtSeconds"), 0), number(m.get("maxKm"), 0), attribute, bands,
                    number(m.get("cellDegrees"), 0.1), strings(m.get("countryFields")), strings(m.get("asnFields")),
                    strings(m.get("startFields")), strings(m.get("endFields")), strings(m.get("typeFields")));
        }
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
        }
        return out;
    }

    private static double number(Object value, double fallback) {
        return value instanceof Number n ? n.doubleValue() : fallback;
    }
}
