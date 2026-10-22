package ai.foresignal.intel;

import ai.foresignal.trigger.OperatorRegistry;
import ai.foresignal.trigger.RuleCondition;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A correlation recipe definition (prompt 101), the parsed form of the {@code definition} jsonb.
 *
 * <p>Seed versions come from the recipe's seed kinds/sources/conditions; joins add members within a
 * distance, region or shared entity and a time window; contradictions subtract evidence and add an
 * alternative; the event type, severity map, dedup rule and lifecycle periods complete it.
 */
public record RecipeDefinition(
        String id,
        int version,
        String type,
        Seed seed,
        List<Join> joins,
        int minMembers,
        int minSignalFamilies,
        ActivateAt activateAt,
        List<Contradiction> contradictions,
        Map<String, String> severityMap,
        Dedup dedup,
        Duration quietPeriod,
        Duration reopenWindow) {

    /** The seed selection. */
    public record Seed(List<String> kinds, List<String> sources, List<RuleCondition> conditions) {}

    /** One join. */
    public record Join(List<String> kinds, List<String> sources, List<RuleCondition> conditions, Double withinKm,
            String sameRegion, String sameEntity, Duration before, Duration after, String role, String signalFamily,
            double weight) {}

    /**
     * The activation threshold.
     *
     * @param members the supporting members needed
     * @param confidence the confidence needed, or null
     * @param modalities the distinct sensing modalities needed (prompt 154), or null to not require any
     */
    public record ActivateAt(int members, Double confidence, Integer modalities) {}

    /** A contradiction. */
    public record Contradiction(List<String> kinds, List<String> sources, List<String> healthOf, Double withinKm,
            Duration window, String alternative) {}

    /** The same-event dedup rule. */
    public record Dedup(double radiusKm, Duration window) {}

    /** Parses a definition map (from jsonb or a bundled file). */
    public static RecipeDefinition of(String id, int version, Map<String, Object> map, OperatorRegistry registry) {
        Map<String, Object> seed = asMap(map.get("seed"));
        List<Join> joins = new ArrayList<>();
        for (Object raw : asList(map.get("joins"))) {
            Map<String, Object> join = asMap(raw);
            joins.add(new Join(strings(join.get("kinds")), strings(join.get("sources")),
                    conditions(join.get("conditions"), registry), number(join.get("withinKm")),
                    text(join.get("sameRegion")), text(join.get("sameEntity")),
                    duration(join.get("before")), duration(join.get("after")), text(join.get("role")),
                    text(join.get("signalFamily")), number(join.get("weight")) == null ? 0.5 : number(join.get("weight"))));
        }
        List<Contradiction> contradictions = new ArrayList<>();
        for (Object raw : asList(map.get("contradictions"))) {
            Map<String, Object> contradiction = asMap(raw);
            contradictions.add(new Contradiction(strings(contradiction.get("kinds")), strings(contradiction.get("sources")),
                    strings(contradiction.get("healthOf")), number(contradiction.get("withinKm")),
                    duration(contradiction.get("window")), text(contradiction.get("alternative"))));
        }
        Map<String, Object> activate = asMap(map.get("activateAt"));
        Map<String, Object> dedup = asMap(map.get("dedup"));
        Map<String, String> severityMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : asMap(map.get("severityMap")).entrySet()) {
            severityMap.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return new RecipeDefinition(
                id,
                version,
                text(map.get("type")),
                new Seed(strings(seed.get("kinds")), strings(seed.get("sources")), conditions(seed.get("conditions"), registry)),
                joins,
                integer(map.get("minMembers"), 2),
                integer(map.get("minSignalFamilies"), 1),
                new ActivateAt(integer(activate.get("members"), 2), number(activate.get("confidence")),
                        activate.get("modalities") instanceof Number m ? m.intValue() : null),
                contradictions,
                severityMap,
                new Dedup(dedup.isEmpty() ? 0 : number(dedup.get("radiusKm")) == null ? 0 : number(dedup.get("radiusKm")),
                        duration(dedup.get("window"))),
                duration(map.get("quietPeriod")) == null ? Duration.ofHours(6) : duration(map.get("quietPeriod")),
                duration(map.get("reopenWindow")) == null ? Duration.ofHours(72) : duration(map.get("reopenWindow")));
    }

    /** Every kind the recipe reads (seed + joins + contradictions). */
    public List<String> allKinds() {
        List<String> all = new ArrayList<>(seed.kinds());
        for (Join join : joins) {
            all.addAll(join.kinds());
        }
        for (Contradiction contradiction : contradictions) {
            all.addAll(contradiction.kinds());
        }
        return all;
    }

    // ------------------------------------------------------------------ parsing helpers

    static List<RuleCondition> conditions(Object raw, OperatorRegistry registry) {
        List<RuleCondition> out = new ArrayList<>();
        for (Object item : asList(raw)) {
            Map<String, Object> condition = asMap(item);
            String path = text(condition.get("path"));
            String op = text(condition.get("op"));
            if (path == null || op == null) {
                continue;
            }
            var operator = registry.find(op);
            operator.ifPresent(value -> out.add(new RuleCondition(path, value, condition.get("value"))));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> asMap(Object raw) {
        return raw instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    static List<Object> asList(Object raw) {
        return raw instanceof List<?> list ? new ArrayList<>(list) : List.of();
    }

    static List<String> strings(Object raw) {
        List<String> out = new ArrayList<>();
        for (Object item : asList(raw)) {
            if (item != null) {
                out.add(String.valueOf(item));
            }
        }
        return out;
    }

    static String text(Object raw) {
        return raw == null ? null : String.valueOf(raw);
    }

    static Double number(Object raw) {
        return raw instanceof Number value ? value.doubleValue() : null;
    }

    static int integer(Object raw, int fallback) {
        return raw instanceof Number value ? value.intValue() : fallback;
    }

    static Duration duration(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return ai.foresignal.trigger.window.Durations.parse(String.valueOf(raw));
        } catch (RuntimeException e) {
            return null;
        }
    }
}
