package ai.foresignal.collect;

import ai.foresignal.source.HealthStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * The monthly-cap health rule (OpenAPI {@code Budget.monthlyCapUsd}, 135): a paid source whose spend this UTC
 * calendar month has reached its money cap is {@code degraded} with the reason, because {@link BudgetGovernor}
 * refuses its every request until the next month begins.
 */
@Component
public class MonthlyCapHealthRule implements HealthRule {

    private final BudgetGovernor budgets;

    /** Creates the rule. */
    public MonthlyCapHealthRule(BudgetGovernor budgets) {
        this.budgets = budgets;
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        if (!budgets.budget(context.sourceId()).hasMonthlyCap()) {
            return Optional.empty();
        }
        BudgetStatus budget = budgets.status(context.sourceId());
        if (budget == null || !budget.isMonthlyCapReached()) {
            return Optional.empty();
        }
        YearMonth next = YearMonth.from(context.now().atZone(ZoneOffset.UTC)).plusMonths(1);
        return Optional.of(new HealthVerdict(HealthStatus.DEGRADED, reason(budget, next), null));
    }

    /** {@code monthly cap of $25 spent ($25.0012); paused until 2026-11-01 UTC}. */
    static String reason(BudgetStatus budget, YearMonth next) {
        return "monthly cap of $" + budget.monthlyCapUsd().stripTrailingZeros().toPlainString() + " spent ($"
                + BigDecimal.valueOf(budget.spentUsdThisMonth()).setScale(4, RoundingMode.HALF_UP).toPlainString()
                + "); paused until " + next.atDay(1) + " UTC";
    }
}
