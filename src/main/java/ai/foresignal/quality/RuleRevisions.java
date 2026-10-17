package ai.foresignal.quality;

import ai.foresignal.api.mapper.TriggerMapper;
import ai.foresignal.trigger.TriggerRuleService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * A rule's definition as a {@code TriggerRuleInput} and its revision hash (prompt 115).
 *
 * <p>The input is the {@code TriggerRule} object without its read-only members (ids, timestamps, counters,
 * snooze and mute state, errors) and without null members, so it round-trips through {@code updateTrigger}. The
 * revision is the SHA-256 of its canonical JSON (keys sorted at every depth): a snooze, a mute or a new alert does
 * not change it; any edit of the definition does.
 */
@Component
public class RuleRevisions {

    /** The {@code TriggerRule} members that are not part of the definition. */
    public static final Set<String> READ_ONLY = Set.of("id", "createdAt", "updatedAt", "lastFiredAt", "alertCount",
            "openAlertCount", "suppressedCount", "lastStormAt", "feedbackCounts", "lastError", "lastErrorAt",
            "snoozedUntil", "mutedUntil");

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final TriggerRuleService rules;
    private final TriggerMapper mapper;
    private final JsonMapper json;

    /** Creates the helper. */
    public RuleRevisions(TriggerRuleService rules, TriggerMapper mapper, JsonMapper json) {
        this.rules = rules;
        this.mapper = mapper;
        this.json = json;
    }

    /**
     * The current definition of {@code ruleId} as a {@code TriggerRuleInput}.
     *
     * @throws ai.foresignal.api.error.NotFoundException when the rule does not exist
     */
    public Map<String, Object> input(UUID ruleId) {
        return input(rules.get(ruleId));
    }

    /** The definition of {@code view} as a {@code TriggerRuleInput}. */
    public Map<String, Object> input(TriggerRuleService.RuleView view) {
        Map<String, Object> plain = json.readValue(json.writeValueAsString(mapper.rule(view)), MAP);
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : plain.entrySet()) {
            if (!READ_ONLY.contains(entry.getKey()) && entry.getValue() != null) {
                out.put(entry.getKey(), entry.getValue());
            }
        }
        return out;
    }

    /** The current revision of {@code ruleId}, or null when the rule does not exist. */
    public String revision(UUID ruleId) {
        try {
            return revision(input(ruleId));
        } catch (ai.foresignal.api.error.NotFoundException e) {
            return null;
        }
    }

    /** The revision of a definition. */
    public String revision(Map<String, Object> input) {
        return sha256(canonical(input));
    }

    /** {@code input} with {@code patch} merged in at the top level (a rule update replaces whole members). */
    public static Map<String, Object> merge(Map<String, Object> input, Map<String, Object> patch) {
        Map<String, Object> out = new LinkedHashMap<>(input);
        if (patch != null) {
            out.putAll(patch);
        }
        out.values().removeIf(java.util.Objects::isNull);
        return out;
    }

    /** The canonical JSON of {@code value}: object keys sorted at every depth. */
    public String canonical(Object value) {
        return json.writeValueAsString(sorted(value));
    }

    /** The SHA-256 of {@code text} in lowercase hex. */
    public static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object sorted(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new TreeMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                out.put(String.valueOf(entry.getKey()), sorted(entry.getValue()));
            }
            return out;
        }
        if (value instanceof List<?> list) {
            List<Object> out = new ArrayList<>(list.size());
            for (Object item : list) {
                out.add(sorted(item));
            }
            return out;
        }
        return value;
    }
}
