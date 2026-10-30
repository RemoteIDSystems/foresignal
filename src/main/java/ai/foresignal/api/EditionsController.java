package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.security.user.AuthenticatedSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code listMyEditions} (prompt NP-003): the editions the signed-in user owns, newest first. An edition is a briefing
 * with {@code ownerUserId}; it is created, changed, run and deleted through the briefing operations. Editions belong to
 * a user, so this needs a signed-in session; an API token is {@code 401}.
 *
 * <p>Left out in edge mode, with the briefings (prompt 113).
 */
@RestController
@Tag(name = "Briefings")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class EditionsController {

    /** The default page size. */
    static final int DEFAULT_LIMIT = 50;

    /** The largest page size. */
    static final int MAX_LIMIT = 200;

    private final BriefingService briefings;

    /** Creates the controller. */
    public EditionsController(BriefingService briefings) {
        this.briefings = briefings;
    }

    /** {@code listMyEditions}: {@code {items: EditionSummary[], nextCursor}}. */
    @GetMapping("/v1/me/editions")
    @Operation(operationId = "listMyEditions", tags = "Briefings")
    public Map<String, Object> listMyEditions(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        AuthenticatedSession session = AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(
                HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                "This operation needs a signed-in session, not an API token."));
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        return briefings.listMine(session.userId(), cursor, size);
    }
}
