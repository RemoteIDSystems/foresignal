package ai.foresignal.trigger;

import ai.foresignal.store.TriggerCondition;
import ai.foresignal.store.TriggerRuleEntity;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Compiles stored rules into {@link RuleSpec}s, resolving operators through the registry. Prompt 036:
 * a condition without a path is an identity condition ({@link IdentityOperator}), resolved per source
 * from the descriptors' {@code entityKeys}; a rule with such an operator is sensitive; a stored
 * {@code near.anchor} becomes the rule's {@link NearAnchor}. Prompt 040: a {@code geofenceRef} preset is
 * resolved through {@link RegionFences} into the fence. Prompt 114: a stored condition with the operator
 * {@code approach} becomes an {@link ApproachCondition} of the rule.
 */
@Component
public class RuleSpecs {

    /** A source id no record has, used to fail a dangling source group closed (prompt SG-002). */
    public static final String NO_SOURCE_SENTINEL = "sg:no-members";

    private final OperatorRegistry operators;
    private final RegionFences fences;

    /** Expands a rule's {@code sourceGroupId} into its member sources (prompt SG-002); null in unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.source.group.SourceGroupLookup sourceGroups;

    /** Creates a compiler without region presets (a rule with a {@code geofenceRef} is then broken). */
    public RuleSpecs(OperatorRegistry operators) {
        this(operators, null);
    }

    /** Creates the compiler; {@code fences} resolves {@code geofenceRef} presets (prompt 040). */
    @org.springframework.beans.factory.annotation.Autowired
    public RuleSpecs(OperatorRegistry operators, RegionFences fences) {
        this.operators = operators;
        this.fences = fences;
    }

    /**
     * Compiles {@code rule}.
     *
     * @throws IllegalStateException when a stored condition names an operator that is no longer
     *     registered or a value no longer validates (the rule is then reported as broken)
     */
    public RuleSpec compile(TriggerRuleEntity rule) {
        List<RuleCondition> conditions = new ArrayList<>();
        List<ApproachCondition> approaches = new ArrayList<>();
        boolean sensitive = false;
        for (TriggerCondition condition : rule.getConditions()) {
            if (ApproachCondition.OP.equals(condition.op())) {
                // Prompt 114: an approach condition is evaluated in Java, never rendered in SQL.
                try {
                    approaches.add(ApproachCondition.fromValue(condition.value()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("Approach condition of rule " + rule.getId() + ": " + e.getMessage(), e);
                }
                continue;
            }
            Operator op = operators.find(condition.op()).orElseThrow(
                    () -> new IllegalStateException("Unknown operator '" + condition.op() + "' in rule " + rule.getId()));
            Object value;
            try {
                value = op.normalize(condition.value());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                        "Condition on '" + condition.path() + "' of rule " + rule.getId() + " " + e.getMessage(), e);
            }
            if (condition.path() == null) {
                if (!(op instanceof IdentityOperator identity)) {
                    throw new IllegalStateException("Condition without a path in rule " + rule.getId());
                }
                conditions.add(new RuleCondition(null, op, value, identity.identityPaths(value, rule.getSources())));
            } else {
                conditions.add(new RuleCondition(condition.path(), op, value));
            }
            sensitive |= op instanceof IdentityOperator;
        }
        // Prompt 040: a geofence preset is resolved into its union now, so the gate tests the same area the
        // SQL's subquery computes; missing reference data makes the rule broken until it loads.
        GeofenceRef ref = GeofenceRef.fromMap(rule.getGeofenceRef());
        org.locationtech.jts.geom.MultiPolygon fence = rule.getGeofence();
        if (ref != null) {
            if (fences == null) {
                throw new IllegalStateException("Rule " + rule.getId() + " has a geofenceRef but region presets are"
                        + " not available (REFERENCE_DATA_MISSING)");
            }
            fence = fences.union(ref);
        }
        return new RuleSpec(
                rule.getId(),
                rule.getName(),
                rule.isEnabled(),
                sources(rule),
                new LinkedHashSet<>(rule.getKinds()),
                rule.getTags(),
                conditions,
                fence,
                rule.getGeofenceInner(),
                rule.getGeofenceOuter(),
                rule.getHysteresisMeters(),
                rule.getMode(),
                rule.isOnlyNew(),
                rule.isReappearAsEnter(),
                Duration.ofSeconds(rule.getCooldownSeconds()),
                CooldownScope.fromWire(rule.getCooldownScope()),
                Severity.fromWire(rule.getSeverity()),
                AutoResolve.fromStorage(rule.getAutoResolve()),
                rule.getMaxAlertsPerHour(),
                rule.isAutoDisableOnStorm(),
                rule.getActiveFrom(),
                rule.getActiveUntil(),
                ActiveWindow.fromMap(rule.getActiveWindow()),
                rule.getForecastHorizonSeconds() == null ? null : Duration.ofSeconds(rule.getForecastHorizonSeconds()),
                rule.getMessageTemplate(),
                rule.getSnoozedUntil(),
                rule.isEnrich(),
                NearAnchor.fromMap(rule.getNear()),
                rule.getNear(),
                sensitive,
                ref,
                approaches);
    }

    /**
     * The rule's watched source set (prompt SG-002): its own sources, unioned with the member sources of its
     * {@code sourceGroupId}. A dangling or empty group fails the rule closed — the set becomes a single source id no
     * record has ({@link #NO_SOURCE_SENTINEL}) rather than the empty "match any" set — so a deleted group never widens
     * the rule to every source. Without a {@code sourceGroupId} the set is just the rule's sources.
     */
    private LinkedHashSet<String> sources(TriggerRuleEntity rule) {
        LinkedHashSet<String> sources = new LinkedHashSet<>(rule.getSources());
        String groupId = rule.getSourceGroupId();
        if (groupId == null) {
            return sources;
        }
        List<String> members = sourceGroups == null ? List.of()
                : sourceGroups.memberSourcesForPrincipal(groupId, rule.getOwnerPrincipalId()).orElse(List.of());
        if (members.isEmpty()) {
            LinkedHashSet<String> closed = new LinkedHashSet<>();
            closed.add(NO_SOURCE_SENTINEL);
            return closed;
        }
        sources.addAll(members);
        return sources;
    }
}
