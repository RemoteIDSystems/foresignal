package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.news.NewsSubscriptionService;
import ai.foresignal.security.user.AuthenticatedSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * A user's news subscriptions (prompt NP-003): {@code getMyNewsSubscriptions} and {@code updateMyNewsSubscriptions}
 * ({@code self.sources}, enforced by the token filter). The outlets and languages followed scope the user's editions.
 * Subscriptions belong to a user, so both need a signed-in session; an API token is {@code 401}.
 */
@RestController
@Tag(name = "News")
public class NewsSubscriptionsController {

    private final NewsSubscriptionService subscriptions;

    /** Creates the controller. */
    public NewsSubscriptionsController(NewsSubscriptionService subscriptions) {
        this.subscriptions = subscriptions;
    }

    /** {@code getMyNewsSubscriptions}: the {@code NewsSubscriptionView}. */
    @GetMapping("/v1/me/news-subscriptions")
    @Operation(operationId = "getMyNewsSubscriptions", tags = "News")
    public Map<String, Object> getMyNewsSubscriptions(HttpServletRequest request) {
        return subscriptions.view(session(request).userId());
    }

    /** {@code updateMyNewsSubscriptions}: replaces the subscriptions; an unknown outlet or bad tag is a 400. */
    @PutMapping("/v1/me/news-subscriptions")
    @Operation(operationId = "updateMyNewsSubscriptions", tags = "News")
    public Map<String, Object> updateMyNewsSubscriptions(@RequestBody(required = false) JsonNode body,
            HttpServletRequest request) {
        return subscriptions.replace(session(request).userId(), body);
    }

    private static AuthenticatedSession session(HttpServletRequest request) {
        return AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This operation needs a signed-in session, not an API token."));
    }
}
