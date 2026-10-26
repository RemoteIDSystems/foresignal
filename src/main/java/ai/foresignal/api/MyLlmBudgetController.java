package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.llm.LlmUserBudgetService;
import ai.foresignal.security.user.AuthenticatedSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getMyLlmBudget} (prompt R2-011, {@code self.llm}): the signed-in user's LLM spend this UTC month on the shared
 * providers and their personal ones. A budget belongs to a user, so an API token gets {@code 401}.
 */
@RestController
@Tag(name = "LLM")
public class MyLlmBudgetController {

    private final LlmUserBudgetService budgets;

    /**
     * Creates the controller.
     *
     * @param budgets the per-user budget service
     */
    public MyLlmBudgetController(LlmUserBudgetService budgets) {
        this.budgets = budgets;
    }

    /** {@code getMyLlmBudget}. */
    @GetMapping("/v1/me/llm/budget")
    @Operation(operationId = "getMyLlmBudget", tags = "LLM")
    public Map<String, Object> getMyLlmBudget(HttpServletRequest request) {
        AuthenticatedSession session = AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(
                HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                "This operation needs a signed-in session, not an API token."));
        return budgets.budget(session.principalId());
    }
}
