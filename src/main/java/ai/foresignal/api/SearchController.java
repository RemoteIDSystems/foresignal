package ai.foresignal.api;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.search.ReindexService;
import ai.foresignal.search.SearchRequest;
import ai.foresignal.search.SearchService;
import ai.foresignal.search.SearchStatusService;
import ai.foresignal.source.SourceRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The {@code Search} operations (prompt 027): {@code search}, {@code getSearchStatus},
 * {@code reindexSearch} and {@code cancelReindex}.
 *
 * <p>{@code GET /v1/search} needs a {@code read} token even under open reads, because semantic and
 * hybrid queries spend the embedding budget ({@code AccessPolicy}); the status is a plain read; the
 * reindex operations are {@code admin}.
 */
@RestController
@Tag(name = "Search")
public class SearchController {

    /** The longest query. */
    static final int MAX_QUERY_CHARS = 1_000;

    private final SearchService search;
    private final SearchStatusService status;
    private final ReindexService reindex;
    private final ReadCallerResolver callers;
    private final SourceRegistry registry;
    private final ai.foresignal.stream.ChangeBus changeBus;

    /** Creates the controller. */
    public SearchController(SearchService search, SearchStatusService status, ReindexService reindex,
            ReadCallerResolver callers, SourceRegistry registry, ai.foresignal.stream.ChangeBus changeBus) {
        this.search = search;
        this.status = status;
        this.reindex = reindex;
        this.callers = callers;
        this.registry = registry;
        this.changeBus = changeBus;
    }

    /** {@code search}: text, semantic or hybrid search over records, briefings and alerts. */
    @GetMapping("/v1/search")
    @Operation(operationId = "search", tags = "Search")
    @ai.foresignal.security.restrict.RestrictedRead
    public Map<String, Object> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) List<String> docType,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(required = false) String timeField,
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        List<FieldError> errors = new ArrayList<>();
        if (q == null || q.isBlank()) {
            errors.add(new FieldError("q", "is required"));
        } else if (q.length() > MAX_QUERY_CHARS) {
            errors.add(new FieldError("q", "must be at most " + MAX_QUERY_CHARS + " characters"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The search is not valid.", errors);
        }
        String modeValue = QueryParams.oneOf("mode", mode, List.of("hybrid", "semantic", "text"), "hybrid");
        Set<String> types = QueryParams.enumSet("docType", docType, SearchRequest.DOC_TYPES);
        String axis = QueryParams.oneOf("timeField", timeField, List.of("effective", "observed"), "effective");
        int size = QueryParams.intInRange("limit", limit, 1, SearchRequest.MAX_LIMIT, SearchRequest.DEFAULT_LIMIT);
        Instant sinceAt = QueryParams.instant("since", since);
        Instant untilAt = QueryParams.instant("until", until);
        if (sinceAt != null && untilAt != null && !sinceAt.isBefore(untilAt)) {
            throw ValidationException.forField("until", "must be after since");
        }
        Set<String> excluded = callers.excludedSources(request);
        Set<String> hidden = callers.hiddenSources(callers.resolve(request));
        List<String> sources = QueryParams.multi("source", source);
        for (String id : sources) {
            registry.descriptor(id);
            if (hidden.contains(id)) {
                throw NotFoundException.of("Source", id);
            }
        }
        SearchRequest query = new SearchRequest(q.strip(), SearchRequest.Mode.of(modeValue), types, sources,
                QueryParams.multi("kind", kind), BboxParser.parse(bbox), sinceAt, untilAt, "observed".equals(axis),
                size);
        // place=, view= and inWatchlist= narrow the caller's restriction (prompt 036).
        if (QueryParams.bool("count", request.getParameter("count"), false)) {
            // count=true (prompt 038): {count, exact, watermarkSeq} and no items.
            if (query.mode() == SearchRequest.Mode.SEMANTIC) {
                throw ValidationException.forField("count", "count=true counts text matches; it cannot be combined"
                        + " with mode=semantic");
            }
            return search.count(query, excluded, callers.resolveScoped(request).restriction(),
                    changeBus.lastPublishedSeq()).toMap();
        }
        return search.search(query, excluded, callers.resolveScoped(request).restriction());
    }

    /** {@code getSearchStatus}. */
    @GetMapping("/v1/search/status")
    @Operation(operationId = "getSearchStatus", tags = "Search")
    public Map<String, Object> getSearchStatus() {
        return status.status();
    }

    /** {@code reindexSearch}: rebuilds the inactive slot with the assigned model; 202 with the status. */
    @PostMapping("/v1/search/reindex")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "reindexSearch", tags = "Search")
    public Map<String, Object> reindexSearch() {
        reindex.requestReindex();
        return status.status();
    }

    /** {@code cancelReindex}: clears a running reindex and its slot; idempotent. */
    @DeleteMapping("/v1/search/reindex")
    @Operation(operationId = "cancelReindex", tags = "Search")
    public Map<String, Object> cancelReindex() {
        reindex.cancel();
        return status.status();
    }
}
