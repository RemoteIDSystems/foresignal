package ai.foresignal.backtest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Deterministic tuning suggestions (prompt 103, version 1.0.0). Each suggestion carries a kind, a
 * rationale, a ready {@code TriggerRuleInput} partial patch, an expected effect (null unless
 * simulated) and caveats. Suggestions are reported, never applied automatically.
 */
@Service
public class TuningSuggester {

    /** The suggester version. */
    public static final String VERSION = "1.0.0";

    /** The precision below which a threshold raise is suggested. */
    public static final double LOW_PRECISION = 0.5;

    private final AlertQualityService quality;

    /** Creates the suggester. */
    public TuningSuggester(AlertQualityService quality) {
        this.quality = quality;
    }

    /** The suggestions for a rule since {@code since}. */
    public List<Map<String, Object>> forRule(UUID ruleId, Instant since) {
        Map<String, Object> report = quality.forRule(ruleId, since);
        List<Map<String, Object>> suggestions = new ArrayList<>();
        long duplicates = number(report.get("duplicates"));
        long flapping = number(report.get("flapping"));
        Double precision = report.get("precision") instanceof Number p ? p.doubleValue() : null;
        if (duplicates > 0) {
            suggestions.add(suggestion("add_cooldown",
                    "The rule produced " + duplicates + " duplicate alerts on the same entity within 10 minutes.",
                    Map.of("cooldown", "PT10M"), "Fewer duplicate alerts; the first in each window is kept."));
        }
        if (flapping > 0) {
            suggestions.add(suggestion("switch_to_enter",
                    flapping + " alerts resolved within 10 minutes (flapping).",
                    Map.of("mode", "enter", "autoResolve", Map.of("onInverseEdge", true)),
                    "A single enter alert instead of a fire/clear pair."));
        }
        if (precision != null && precision < LOW_PRECISION) {
            suggestions.add(suggestion("raise_threshold",
                    "Precision is " + Math.round(precision * 100) + "% on labelled alerts.",
                    Map.of("conditions", List.of()),
                    "A stricter condition set raises precision; review the suggested patch."));
        }
        return suggestions;
    }

    private static Map<String, Object> suggestion(String kind, String rationale, Map<String, Object> patch,
            String expectedEffect) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", kind);
        out.put("kind", kind);
        out.put("rationale", rationale);
        out.put("patch", patch);
        out.put("expectedEffect", expectedEffect);
        out.put("caveats", List.of("Deterministic suggestion; not applied automatically. Backtest the patch before"
                + " applying it."));
        return out;
    }

    private static long number(Object raw) {
        return raw instanceof Number n ? n.longValue() : 0;
    }
}
