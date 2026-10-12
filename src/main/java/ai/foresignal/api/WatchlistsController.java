package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.watch.WatchlistMapper;
import ai.foresignal.watch.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 * Watchlists (OpenAPI tag {@code Watchlists}, prompt 036): CRUD, items, CSV/JSON import and CSV
 * export. Every operation needs the {@code agent} scope; every write is audited by the token filter.
 */
@RestController
@Tag(name = "Watchlists")
public class WatchlistsController {

    /** The CSV media type of imports and exports. */
    public static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    static final int MAX_LIMIT = 500;

    private final WatchlistService watchlists;
    private final WatchlistMapper mapper;

    /** Creates the controller. */
    public WatchlistsController(WatchlistService watchlists, WatchlistMapper mapper) {
        this.watchlists = watchlists;
        this.mapper = mapper;
    }

    /** {@code listWatchlists}. */
    @GetMapping("/v1/watchlists")
    @Operation(operationId = "listWatchlists", tags = "Watchlists")
    public Map<String, Object> listWatchlists(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        return mapper.page(watchlists.list(cursor, QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, 100)));
    }

    /** {@code createWatchlist}: 201. */
    @PostMapping("/v1/watchlists")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createWatchlist", tags = "Watchlists")
    public Map<String, Object> createWatchlist(@RequestBody JsonNode body) {
        return mapper.watchlist(watchlists.create(body));
    }

    /** {@code getWatchlist}: 404 {@code UNKNOWN_WATCHLIST}. */
    @GetMapping("/v1/watchlists/{watchlistId}")
    @Operation(operationId = "getWatchlist", tags = "Watchlists")
    public Map<String, Object> getWatchlist(@PathVariable String watchlistId) {
        return mapper.watchlist(watchlists.get(watchlistId));
    }

    /** {@code updateWatchlist}: name, normalization (empty lists only) and description. */
    @PutMapping("/v1/watchlists/{watchlistId}")
    @Operation(operationId = "updateWatchlist", tags = "Watchlists")
    public Map<String, Object> updateWatchlist(@PathVariable String watchlistId, @RequestBody JsonNode body) {
        return mapper.watchlist(watchlists.update(WatchlistService.parseId(watchlistId), body));
    }

    /** {@code deleteWatchlist}: 204; rules referencing it stop matching. */
    @DeleteMapping("/v1/watchlists/{watchlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteWatchlist", tags = "Watchlists")
    public void deleteWatchlist(@PathVariable String watchlistId) {
        watchlists.delete(WatchlistService.parseId(watchlistId));
    }

    /** {@code listWatchlistItems}: keyset by value. */
    @GetMapping("/v1/watchlists/{watchlistId}/items")
    @Operation(operationId = "listWatchlistItems", tags = "Watchlists")
    public Map<String, Object> listWatchlistItems(@PathVariable String watchlistId,
            @RequestParam(required = false) String q, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        UUID id = WatchlistService.parseId(watchlistId);
        return mapper.itemPage(watchlists.items(id, q, cursor, QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, 100)));
    }

    /** {@code addWatchlistItems}. */
    @PostMapping("/v1/watchlists/{watchlistId}/items")
    @Operation(operationId = "addWatchlistItems", tags = "Watchlists")
    public Map<String, Object> addWatchlistItems(@PathVariable String watchlistId, @RequestBody JsonNode body) {
        return mapper.result(watchlists.add(WatchlistService.parseId(watchlistId), body));
    }

    /** {@code removeWatchlistItems}: {@code ?value=…&value=…}. */
    @DeleteMapping("/v1/watchlists/{watchlistId}/items")
    @Operation(operationId = "removeWatchlistItems", tags = "Watchlists")
    public Map<String, Object> removeWatchlistItems(@PathVariable String watchlistId,
            @RequestParam(name = "value", required = false) List<String> values) {
        UUID id = WatchlistService.parseId(watchlistId);
        if (values == null || values.isEmpty()) {
            throw ai.foresignal.api.error.ValidationException.forField("value", "at least one value is required");
        }
        return mapper.result(watchlists.remove(id, values));
    }

    /**
     * {@code importWatchlist}: {@code text/csv} or {@code application/json}; {@code mode=merge|replace};
     * {@code dryRun=true} changes nothing. 413 above 5 MB, 415 for another media type.
     */
    @PostMapping("/v1/watchlists/{watchlistId}/import")
    @Operation(operationId = "importWatchlist", tags = "Watchlists")
    public Map<String, Object> importWatchlist(@PathVariable String watchlistId,
            @RequestParam(required = false) String mode, @RequestParam(required = false) String dryRun,
            HttpServletRequest request) throws IOException {
        UUID id = WatchlistService.parseId(watchlistId);
        String m = QueryParams.oneOf("mode", mode, List.of("merge", "replace"), "merge");
        boolean dry = QueryParams.bool("dryRun", dryRun, false);
        MediaType type;
        try {
            type = request.getContentType() == null ? null : MediaType.parseMediaType(request.getContentType());
        } catch (RuntimeException e) {
            type = null;
        }
        boolean csv;
        if (type != null && type.isCompatibleWith(MediaType.parseMediaType("text/csv"))) {
            csv = true;
        } else if (type != null && type.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            csv = false;
        } else {
            throw new ForesignalException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCodes.UNSUPPORTED_MEDIA_TYPE,
                    "A watchlist import is text/csv or application/json.");
        }
        if (request.getContentLengthLong() > WatchlistService.MAX_IMPORT_BYTES) {
            throw new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE,
                    "A watchlist import may be at most 5 MB.");
        }
        byte[] body;
        try (InputStream in = request.getInputStream()) {
            body = in.readNBytes(WatchlistService.MAX_IMPORT_BYTES + 1);
        }
        return mapper.result(watchlists.importItems(id, csv, body, m, dry));
    }

    /** {@code exportWatchlist}: CSV with the header {@code value,label,notes,tags}. */
    @GetMapping("/v1/watchlists/{watchlistId}/export")
    @Operation(operationId = "exportWatchlist", tags = "Watchlists")
    public ResponseEntity<String> exportWatchlist(@PathVariable String watchlistId) {
        UUID id = WatchlistService.parseId(watchlistId);
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"watchlist-" + id + ".csv\"")
                .body(watchlists.export(id));
    }
}
