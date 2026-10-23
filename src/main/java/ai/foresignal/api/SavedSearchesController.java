package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.collab.SavedSearchEntity;
import ai.foresignal.collab.SavedSearchService;
import ai.foresignal.object.ObjectCallers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Saved searches (prompt 130, tag {@code Collaboration}): {@code listSavedSearches}, {@code createSavedSearch},
 * {@code getSavedSearch}, {@code updateSavedSearch}, {@code deleteSavedSearch} and {@code runSavedSearch}. Every
 * operation needs a token.
 */
@RestController
@Tag(name = "Collaboration")
public class SavedSearchesController {

    private final SavedSearchService searches;
    private final ObjectCallers callers;

    /** Creates the controller. */
    public SavedSearchesController(SavedSearchService searches, ObjectCallers callers) {
        this.searches = searches;
        this.callers = callers;
    }

    /** {@code listSavedSearches}. */
    @GetMapping("/v1/saved-searches")
    @Operation(operationId = "listSavedSearches", tags = "Collaboration")
    public Map<String, Object> listSavedSearches(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        return searches.list(callers.authenticated(request), cursor, QueryParams.intInRange("limit", limit, 1, 200,
                50));
    }

    /** {@code createSavedSearch}. */
    @PostMapping("/v1/saved-searches")
    @Operation(operationId = "createSavedSearch", tags = "Collaboration")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createSavedSearch(@RequestBody JsonNode body,
            HttpServletRequest request) {
        SavedSearchEntity s = searches.create(body, callers.authenticated(request));
        return ResponseEntity.created(URI.create("/v1/saved-searches/" + s.getId())).body(searches.view(s));
    }

    /** {@code getSavedSearch}. */
    @GetMapping("/v1/saved-searches/{savedSearchId}")
    @Operation(operationId = "getSavedSearch", tags = "Collaboration")
    public Map<String, Object> getSavedSearch(@PathVariable String savedSearchId, HttpServletRequest request) {
        return searches.view(searches.get(savedSearchId, callers.authenticated(request)));
    }

    /** {@code updateSavedSearch} (owner or editor; JSON merge patch). */
    @PatchMapping(value = "/v1/saved-searches/{savedSearchId}", consumes = "application/merge-patch+json")
    @Operation(operationId = "updateSavedSearch", tags = "Collaboration")
    public Map<String, Object> updateSavedSearch(@PathVariable String savedSearchId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return searches.view(searches.patch(savedSearchId, body, callers.authenticated(request)));
    }

    /** {@code deleteSavedSearch} (owner only). */
    @DeleteMapping("/v1/saved-searches/{savedSearchId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteSavedSearch", tags = "Collaboration")
    public void deleteSavedSearch(@PathVariable String savedSearchId, HttpServletRequest request) {
        searches.delete(savedSearchId, callers.authenticated(request));
    }

    /** {@code runSavedSearch}: runs it now and returns the visible envelopes. */
    @GetMapping("/v1/saved-searches/{savedSearchId}/results")
    @Operation(operationId = "runSavedSearch", tags = "Collaboration")
    public Map<String, Object> runSavedSearch(@PathVariable String savedSearchId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        return searches.results(savedSearchId, callers.authenticated(request), cursor,
                QueryParams.intInRange("limit", limit, 1, 100, 25));
    }
}
