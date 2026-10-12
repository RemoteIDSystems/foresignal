package ai.foresignal.trigger;

import ai.foresignal.store.TriggerCondition;
import ai.foresignal.store.TriggerRuleEntity;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Component;

/** Compiles stored rules into {@link RuleSpec}s, resolving operators through the registry. */
@Component
public class RuleSpecs {

    private final OperatorRegistry operators;

    /** Creates the compiler. */
    public RuleSpecs(OperatorRegistry operators) {
        this.operators = operators;
    }

    /**
     * Compiles {@code rule}.
     *
     * @throws IllegalStateException when a stored condition names an operator that is no longer
     *     registered or a value no longer validates (the rule is then reported as broken)
     */
    public RuleSpec compile(TriggerRuleEntity rule) {
        List<RuleCondition> conditions = new ArrayList<>();
        for (TriggerCondition condition : rule.getConditions()) {
            Operator op = operators.find(condition.op()).orElseThrow(
                    () -> new IllegalStateException("Unknown operator '" + condition.op() + "' in rule " + rule.getId()));
            Object value;
            try {
                value = op.normalize(condition.value());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                        "Condition on '" + condition.path() + "' of rule " + rule.getId() + " " + e.getMessage(), e);
            }
            conditions.add(new RuleCondition(condition.path(), op, value));
        }
        return new RuleSpec(
                rule.getId(),
                rule.getName(),
                rule.isEnabled(),
                new LinkedHashSet<>(rule.getSources()),
                new LinkedHashSet<>(rule.getKinds()),
                rule.getTags(),
                conditions,
                rule.getGeofence(),
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
                rule.isEnrich());
    }
}
