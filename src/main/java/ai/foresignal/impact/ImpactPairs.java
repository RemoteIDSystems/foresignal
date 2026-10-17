package ai.foresignal.impact;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The built-in impact pairs of the impact projector (prompt 127), read once from
 * {@code src/main/resources/intel/impact-pairs.json}.
 *
 * <ul>
 *   <li>A <b>hazard class</b> names the records that count as one occurrence of a hazard in an admin1 region (per UTC
 *       day) when the conditional rate is computed from history, and the hazard type it applies to.
 *   <li>An <b>impact family</b> names the records that count as the impact: the same selector feeds the
 *       co-occurrence count, the trigger rule behind the ledger question's {@code event-exists} resolver and the
 *       question's detectability scope.
 *   <li>A <b>pair</b> joins a hazard class to an impact family with the lag within which the impact counts.
 * </ul>
 *
 * <p>Conditions use the trigger-rule operators {@code ==}, {@code in} and {@code >=} on top-level payload fields.
 * The file is validated when it loads: unknown references, operators or field names stop startup.
 */
@Component
public class ImpactPairs {

    /** The resource path. */
    public static final String RESOURCE = "intel/impact-pairs.json";

    /** The operators a condition may use. */
    public static final Set<String> OPERATORS = Set.of("==", "in", ">=");

    /** A payload field a condition may name. */
    public static final Pattern PATH = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{0,63}$");

    /**
     * One condition on a payload field.
     *
     * @param path the top-level payload field
     * @param op {@code ==}, {@code in} or {@code >=}
     * @param value a string or number ({@code ==}), a list of strings ({@code in}) or a number ({@code >=})
     */
    public record Condition(String path, String op, Object value) {

        /** The condition as a trigger-rule condition object. */
        public Map<String, Object> asRuleCondition() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("path", path);
            out.put("op", op);
            out.put("value", value);
            return out;
        }
    }

    /**
     * The records a hazard class or impact family names.
     *
     * @param sources source ids
     * @param kinds kinds
     * @param conditions payload conditions (all must hold)
     */
    public record Selector(List<String> sources, List<String> kinds, List<Condition> conditions) {

        /** Copies the lists. */
        public Selector {
            sources = List.copyOf(sources);
            kinds = List.copyOf(kinds);
            conditions = List.copyOf(conditions);
        }
    }

    /**
     * A hazard class.
     *
     * @param id the class id (for example {@code tropical-wind-34})
     * @param hazardType the hazard type it applies to
     * @param label the human label
     * @param minCategory the flood-relevant category of a {@code river-flood} class, or null
     * @param selector the records that count as an occurrence
     */
    public record HazardClass(String id, String hazardType, String label, String minCategory, Selector selector) {}

    /**
     * An impact family.
     *
     * @param id the family id (for example {@code outage})
     * @param label the human label used in question titles
     * @param selector the records that count as the impact
     * @param note where the records come from
     */
    public record ImpactFamily(String id, String label, Selector selector, String note) {}

    /**
     * A built-in pair.
     *
     * @param hazardClass the hazard class id
     * @param impactFamily the impact family id
     * @param lagHours the hours after the hazard within which the impact counts
     */
    public record Pair(String hazardClass, String impactFamily, int lagHours) {}

    private final String version;
    private final Map<String, HazardClass> hazardClasses;
    private final Map<String, ImpactFamily> families;
    private final List<Pair> pairs;

    /** Loads the built-in file. */
    public ImpactPairs() {
        this(read());
    }

    /**
     * Loads a pairs document (tests).
     *
     * @param root the parsed document
     * @throws IllegalStateException when the document is not valid
     */
    public ImpactPairs(JsonNode root) {
        Objects.requireNonNull(root, "root");
        this.version = root.path("version").asString("1.0.0");
        Map<String, HazardClass> classes = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> e : root.path("hazardClasses").properties()) {
            JsonNode n = e.getValue();
            classes.put(e.getKey(), new HazardClass(e.getKey(), text(n, "hazardType", e.getKey()),
                    text(n, "label", e.getKey()), text(n, "minCategory", null), selector(e.getKey(), n)));
        }
        Map<String, ImpactFamily> fams = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> e : root.path("impactFamilies").properties()) {
            JsonNode n = e.getValue();
            fams.put(e.getKey(), new ImpactFamily(e.getKey(), text(n, "label", e.getKey()), selector(e.getKey(), n),
                    text(n, "note", null)));
        }
        List<Pair> list = new ArrayList<>();
        for (JsonNode n : root.path("pairs")) {
            Pair pair = new Pair(text(n, "hazardClass", null), text(n, "impactFamily", null),
                    n.path("lagHours").asInt(0));
            if (!classes.containsKey(pair.hazardClass()) || !fams.containsKey(pair.impactFamily())
                    || pair.lagHours() < 1 || pair.lagHours() > 24 * 30) {
                throw new IllegalStateException(RESOURCE + ": invalid pair " + pair);
            }
            list.add(pair);
        }
        this.hazardClasses = Map.copyOf(classes);
        this.families = Map.copyOf(fams);
        this.pairs = List.copyOf(list);
    }

    /** The file's version. */
    public String version() {
        return version;
    }

    /** Every pair, in file order. */
    public List<Pair> pairs() {
        return pairs;
    }

    /** The pairs whose hazard class applies to {@code hazardType}, in file order. */
    public List<Pair> pairsFor(String hazardType) {
        List<Pair> out = new ArrayList<>();
        for (Pair pair : pairs) {
            HazardClass c = hazardClasses.get(pair.hazardClass());
            if (c != null && c.hazardType().equals(hazardType)) {
                out.add(pair);
            }
        }
        return out;
    }

    /** The hazard class, or null. */
    public HazardClass hazardClass(String id) {
        return id == null ? null : hazardClasses.get(id);
    }

    /** The impact family, or null. */
    public ImpactFamily family(String id) {
        return id == null ? null : families.get(id);
    }

    /** Every impact family. */
    public Collection<ImpactFamily> families() {
        return families.values();
    }

    private static Selector selector(String owner, JsonNode n) {
        List<String> sources = strings(n.path("sources"));
        List<String> kinds = strings(n.path("kinds"));
        if (sources.isEmpty() || kinds.isEmpty()) {
            throw new IllegalStateException(RESOURCE + ": " + owner + " needs sources and kinds");
        }
        List<Condition> conditions = new ArrayList<>();
        for (JsonNode c : n.path("conditions")) {
            String path = text(c, "path", null);
            String op = text(c, "op", null);
            JsonNode value = c.get("value");
            if (path == null || !PATH.matcher(path).matches() || op == null || !OPERATORS.contains(op)
                    || value == null) {
                throw new IllegalStateException(RESOURCE + ": " + owner + " has an invalid condition " + c);
            }
            Object v;
            if ("in".equals(op)) {
                if (!value.isArray() || value.isEmpty()) {
                    throw new IllegalStateException(RESOURCE + ": " + owner + ": 'in' needs a list");
                }
                v = strings(value);
            } else if (">=".equals(op)) {
                if (!value.isNumber()) {
                    throw new IllegalStateException(RESOURCE + ": " + owner + ": '>=' needs a number");
                }
                v = value.doubleValue();
            } else {
                v = value.isNumber() ? (Object) value.numberValue() : value.asString();
            }
            conditions.add(new Condition(path, op, v));
        }
        return new Selector(sources, kinds, conditions);
    }

    private static List<String> strings(JsonNode array) {
        List<String> out = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode v : array) {
                out.add(v.asString());
            }
        }
        return out;
    }

    private static String text(JsonNode n, String field, String fallback) {
        JsonNode v = n == null ? null : n.get(field);
        return v == null || v.isNull() ? fallback : v.asString();
    }

    private static JsonNode read() {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            return JsonMapper.builder().build().readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + RESOURCE, e);
        }
    }
}
