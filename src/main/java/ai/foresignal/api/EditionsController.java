package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpCallerResolver;
import ai.foresignal.security.user.AuthenticatedSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The caller's editions under {@code /v1/me/editions}: an edition is a briefing the signed-in user owns
 * ({@code Briefing.ownerUserId}).
 *
 * <ul>
 *   <li>{@code listMyEditions} (prompt NP-003): the user's editions, newest first.
 *   <li>{@code createMyEdition}, {@code updateMyEdition}, {@code deleteMyEdition}, {@code runMyEdition} (prompt
 *       NP-008): a signed-in user holding {@code self.editions} owns and manages their own edition without agent
 *       scope. These reuse {@link BriefingService}, forcing {@code ownerUserId} to the caller; an edition the caller
 *       does not own is {@code 404}, as for one that does not exist.
 * </ul>
 *
 * <p>Editions belong to a user, so every operation here needs a signed-in session; an API token is {@code 401}.
 * Writes run as the session user through the same audited pipeline as the briefing operations.
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
    private final McpCallerResolver callers;

    /** Creates the controller. */
    public EditionsController(BriefingService briefings, McpCallerResolver callers) {
        this.briefings = briefings;
        this.callers = callers;
    }

    /** {@code listMyEditions}: {@code {items: EditionSummary[], nextCursor}}. */
    @GetMapping("/v1/me/editions")
    @Operation(operationId = "listMyEditions", tags = "Briefings")
    public Map<String, Object> listMyEditions(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        AuthenticatedSession session = requireSession(request);
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        return briefings.listMine(session.userId(), cursor, size);
    }

    /** {@code createMyEdition}: 201 with {@code Location}; {@code ownerUserId} is forced to the caller (NP-008). */
    @PostMapping("/v1/me/editions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createMyEdition", tags = "Briefings")
    public ResponseEntity<Map<String, Object>> createMyEdition(@RequestBody JsonNode body, HttpServletRequest request) {
        requireSession(request);
        Map<String, Object> created = briefings.create(body, callers.resolve(request));
        return ResponseEntity.created(URI.create("/v1/briefings/" + created.get("id"))).body(created);
    }

    /** {@code updateMyEdition}: a full replace of an edition the caller owns; 404 otherwise (NP-008). */
    @PutMapping("/v1/me/editions/{briefingId}")
    @Operation(operationId = "updateMyEdition", tags = "Briefings")
    public Map<String, Object> updateMyEdition(@PathVariable String briefingId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        requireSession(request);
        return briefings.update(id(briefingId), body, callers.resolve(request));
    }

    /** {@code deleteMyEdition}: 204; 404 unless the caller owns it (NP-008). */
    @DeleteMapping("/v1/me/editions/{briefingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteMyEdition", tags = "Briefings")
    public void deleteMyEdition(@PathVariable String briefingId, HttpServletRequest request) {
        requireSession(request);
        McpCaller caller = callers.resolve(request);
        briefings.delete(id(briefingId), briefings.viewer(caller));
    }

    /** {@code runMyEdition}: runs an edition the caller owns now (202); 404 otherwise (NP-008). */
    @PostMapping("/v1/me/editions/{briefingId}/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "runMyEdition", tags = "Briefings")
    public Map<String, Object> runMyEdition(@PathVariable String briefingId, HttpServletRequest request) {
        requireSession(request);
        McpCaller caller = callers.resolve(request);
        UUID id = id(briefingId);
        briefings.requireVisible(id, caller.restriction(), briefings.viewer(caller));
        return briefings.runNow(id);
    }

    /** The request's session, or {@code 401}: these operations are never reachable with an API token. */
    private static AuthenticatedSession requireSession(HttpServletRequest request) {
        return AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This operation needs a signed-in session, not an API token."));
    }

    /** The briefing id, or {@code 404} when it is not a UUID (an unknown edition). */
    private static UUID id(String briefingId) {
        try {
            return UUID.fromString(briefingId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Briefing", briefingId);
        }
    }
}
