package ai.foresignal.api.error;

import org.springframework.http.HttpStatus;

/**
 * 429: the request would exceed a configured budget.
 *
 * <p>Foresignal refuses rather than borrowing against a budget, which is what makes "never exceeds its
 * spend" a guarantee instead of an aspiration.
 */
public class BudgetExceededProblem extends ForesignalException {

    private static final long serialVersionUID = 1L;

    public BudgetExceededProblem(String detail) {
        super(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.BUDGET_EXCEEDED, detail);
    }
}
