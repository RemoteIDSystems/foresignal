package ai.foresignal.infra;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The effective dependency rules of a build (prompt 104): the versioned
 * {@code infra/dependency-rules.json} with the operator overrides of {@code app_setting['infra.rules']}
 * merged in. Pure.
 *
 * <p>An override may switch a rule on or off and change its parameters. It may not change a rule's
 * method, relation or label: those state how an edge is known, and letting a setting relabel a
 * proximity guess as {@code confirmed} would break the rule that proximity is never better than
 * {@code possible}. {@link #cap} enforces that ceiling on every label a rule emits.
 */
public final class InfraRules {

    /** The label ranks, weakest first. */
    public static final List<String> LABELS = List.of("speculative", "possible", "likely", "confirmed");

    private InfraRules() {}

    /**
     * One effective rule.
     *
     * @param id the rule id
     * @param enabled whether it runs
     * @param method {@code declared}, {@code topological}, {@code proximity} or {@code inferred}
     * @param relation the edge relation
     * @param label the label the rule emits by default
     * @param params the effective parameters (bundled values with overrides applied)
     */
    public record Rule(String id, boolean enabled, String method, String relation, String label,
            Map<String, Object> params) {

        /** A numeric parameter, or {@code fallback}. */
        public double number(String name, double fallback) {
            return params.get(name) instanceof Number n ? n.doubleValue() : fallback;
        }
    }

    /**
     * The effective rules.
     *
     * @param bundled the parsed {@code dependency-rules.json}
     * @param overrides the parsed {@code app_setting['infra.rules']} (may be empty or null)
     */
    public static List<Rule> effective(Map<String, Object> bundled, Map<String, Object> overrides) {
        Map<String, Map<String, Object>> byId = new LinkedHashMap<>();
        for (Object raw : InfraGraphBuilder.asList(overrides == null ? null : overrides.get("rules"))) {
            Map<String, Object> rule = InfraGraphBuilder.asMap(raw);
            if (rule.get("id") != null) {
                byId.put(String.valueOf(rule.get("id")), rule);
            }
        }
        List<Rule> out = new ArrayList<>();
        for (Object raw : InfraGraphBuilder.asList(bundled.get("rules"))) {
            Map<String, Object> rule = InfraGraphBuilder.asMap(raw);
            String id = String.valueOf(rule.get("id"));
            Map<String, Object> override = byId.getOrDefault(id, Map.of());
            boolean enabled = override.get("enabled") instanceof Boolean b ? b : Boolean.TRUE.equals(rule.get("enabled"));
            Map<String, Object> params = new LinkedHashMap<>(InfraGraphBuilder.asMap(rule.get("params")));
            params.putAll(InfraGraphBuilder.asMap(override.get("params")));
            String method = String.valueOf(rule.get("method"));
            out.add(new Rule(id, enabled, method, String.valueOf(rule.get("relation")),
                    cap(method, String.valueOf(rule.get("label"))), java.util.Collections.unmodifiableMap(params)));
        }
        return out;
    }

    /** The rank of a label (0 = speculative … 3 = confirmed; unknown labels rank 0). */
    public static int rank(String label) {
        int index = LABELS.indexOf(label);
        return Math.max(0, index);
    }

    /** The label of a rank. */
    public static String label(int rank) {
        return LABELS.get(Math.max(0, Math.min(LABELS.size() - 1, rank)));
    }

    /** {@code label}, lowered to the ceiling of {@code method}: proximity is never better than possible. */
    public static String cap(String method, String label) {
        if ("proximity".equals(method) && rank(label) > rank("possible")) {
            return "possible";
        }
        return LABELS.contains(label) ? label : "speculative";
    }
}
