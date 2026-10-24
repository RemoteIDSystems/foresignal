package ai.foresignal.collect;

import ai.foresignal.source.HealthStatus;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * The budget-outlook health rule (prompt 106; Weather-Imagery-Design §2.5): a source whose projected
 * day's download, at the rate seen so far today, exceeds {@value #WARN_FRACTION} × its {@code mbPerDay}
 * is {@code degraded} with an explicit reason, so the operator sees an over-budget product before the
 * byte budget is actually refused.
 *
 * <p>The projection is {@code usedToday × 24 / hoursElapsedInTheUtcDay}, compared with
 * {@code 1.2 × mbPerDay}. It applies only when a byte budget is configured, at least
 * {@value #MIN_HOURS} of the UTC day has elapsed, and some bytes have been used.
 */
@Component
public class BudgetOutlookHealthRule implements HealthRule {

    /** The multiplier over {@code mbPerDay} at which health degrades. */
    public static final double WARN_FRACTION = 1.2;

    /** The fewest hours of the UTC day before a projection is trusted. */
    public static final double MIN_HOURS = 1.0;

    private final BudgetGovernor budgets;

    /** Creates the rule. */
    public BudgetOutlookHealthRule(BudgetGovernor budgets) {
        this.budgets = budgets;
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        if (!budgets.budget(context.sourceId()).hasByteBudget()) {
            return Optional.empty();
        }
        BudgetStatus budget = budgets.status(context.sourceId());
        if (budget == null || budget.mbPerDay() == null || budget.mbPerDay() <= 0 || budget.megabytesUsedToday() == null
                || budget.megabytesUsedToday() <= 0) {
            return Optional.empty();
        }
        double elapsed = context.now().atZone(ZoneOffset.UTC).toLocalTime().toSecondOfDay() / 3600.0;
        if (elapsed < MIN_HOURS) {
            return Optional.empty();
        }
        double projected = budget.megabytesUsedToday() * 24.0 / elapsed;
        double limit = WARN_FRACTION * budget.mbPerDay();
        if (projected > limit) {
            return Optional.of(new HealthVerdict(HealthStatus.DEGRADED,
                    "projected download " + Math.round(projected) + " MB/day exceeds " + Math.round(limit)
                            + " MB/day (" + budget.mbPerDay() + " MB budget)",
                    null));
        }
        return Optional.empty();
    }
}
