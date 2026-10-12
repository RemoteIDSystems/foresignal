package ai.foresignal.llm;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import java.time.Duration;
import org.springframework.http.HttpStatus;

/**
 * 429 {@code LLM_BUDGET_EXCEEDED}: the call would exceed the provider's per-minute request budget or
 * its daily token budget. The call was not sent. {@code Retry-After} says when the budget refills.
 */
public class LlmBudgetExceededException extends ForesignalException {

    private static final long serialVersionUID = 1L;

    private final String providerId;
    private final transient Duration retryAfter;

    /** Creates the exception. */
    public LlmBudgetExceededException(String providerId, String detail, Duration retryAfter) {
        super(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.LLM_BUDGET_EXCEEDED, detail);
        this.providerId = providerId;
        this.retryAfter = retryAfter;
        withHeader("Retry-After", Long.toString(Math.max(1, (retryAfter.toMillis() + 999) / 1000)));
    }

    /** The provider whose budget refused. */
    public String providerId() {
        return providerId;
    }

    /** When the budget refills. */
    public Duration retryAfter() {
        return retryAfter;
    }
}
