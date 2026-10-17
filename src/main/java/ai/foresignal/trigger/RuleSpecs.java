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

    private final OperatorRegistry operators;
    private final RegionFences fences;

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
                new LinkedHashSet<>(rule.getSources()),
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
}
