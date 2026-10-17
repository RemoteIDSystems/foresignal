package ai.foresignal.impact;

import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.trigger.TriggerRuleService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The trigger rules that resolve impact questions (prompt 127 §4): one 017 record rule per impact family, with the
 * family's sources, kinds and conditions ({@link ImpactPairs}). An impact question's {@code event-exists} resolver
 * selects the rule's alerts ({@code selector.alertRuleId}) in the question's admin1 region ({@code area.regionCodes}),
 * so the question resolves from Foresignal's own sources, and its detectability scope is the rule's sources and the
 * family of its first kind (122).
 *
 * <p>The rule id is derived from the family ({@code UUID.nameUUIDFromBytes("foresignal:impact-rule:{family}")}), so
 * the rule is created once and found again after a restart; an existing rule (perhaps edited or muted by the operator)
 * is left as it is. The rules are tagged {@value #TAG}, {@value #ORIGIN_TAG} and {@code impact-{family}}; the
 * notification router sends {@value #ORIGIN_TAG} alerts only to channels that name the rule.
 */
@Component
public class ImpactRules {

    /** The tag of every impact rule. */
    public static final String TAG = "impact";

    /** The origin tag the notification router honors. */
    public static final String ORIGIN_TAG = "origin-impact";

    private final ImpactPairs pairs;
    private final TriggerRuleService rules;
    private final TriggerRuleRepository repository;
    private final JsonMapper json;
    private final Map<String, UUID> ensured = new ConcurrentHashMap<>();

    /** Creates the component. */
    public ImpactRules(ImpactPairs pairs, TriggerRuleService rules, TriggerRuleRepository repository,
            JsonMapper json) {
        this.pairs = pairs;
        this.rules = rules;
        this.repository = repository;
        this.json = json;
    }

    /** The rule id of a family. */
    public static UUID ruleId(String family) {
        return UUID.nameUUIDFromBytes(("foresignal:impact-rule:" + family).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * The family's rule, created when missing.
     *
     * @param family an impact family id
     * @return the rule id
     * @throws IllegalArgumentException for an unknown family
     */
    public UUID ensure(String family) {
        UUID known = ensured.get(family);
        if (known != null && repository.existsById(known)) {
            return known;
        }
        ImpactPairs.ImpactFamily f = pairs.family(family);
        if (f == null) {
            throw new IllegalArgumentException("unknown impact family " + family);
        }
        UUID id = ruleId(family);
        if (!repository.existsById(id)) {
            rules.upsert(id, json.valueToTree(spec(f)));
        }
        ensured.put(family, id);
        return id;
    }

    /** Forgets the ensured rules (tests truncate the rule table). */
    public void reset() {
        ensured.clear();
    }

    /** The {@code TriggerRuleInput} of a family's rule. */
    public static Map<String, Object> spec(ImpactPairs.ImpactFamily family) {
        Map<String, Object> spec = new LinkedHashMap<>();
        String name = "Impact family: " + family.label();
        spec.put("name", name.length() > 200 ? name.substring(0, 200) : name);
        spec.put("sources", family.selector().sources());
        spec.put("kinds", family.selector().kinds());
        spec.put("mode", family.selector().conditions().isEmpty() ? "match" : "enter");
        List<Map<String, Object>> conditions = new ArrayList<>();
        for (ImpactPairs.Condition c : family.selector().conditions()) {
            conditions.add(c.asRuleCondition());
        }
        spec.put("conditions", conditions);
        String familyTag = ("impact-" + family.id()).replaceAll("[^a-z0-9_-]", "-");
        spec.put("tags", List.of(TAG, ORIGIN_TAG, familyTag.length() > 32 ? familyTag.substring(0, 32) : familyTag));
        spec.put("severity", "info");
        spec.put("enabled", true);
        return spec;
    }
}
