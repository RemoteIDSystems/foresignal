package ai.foresignal.api;

import ai.foresignal.access.TileKeyService;
import ai.foresignal.api.dto.TileKeyDto;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.settings.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code listTileKeys}, {@code createTileKey} and {@code revokeTileKey} (agent, prompt 050): read-only
 * {@code fsk_} keys for header-less map clients, used as {@code /v1/k/{key}/…}. Creation returns the
 * key and its {@code urlBase} once; listings never carry the key.
 */
@RestController
@Tag(name = "Tokens")
public class TileKeysController {

    static final int MAX_LIMIT = 500;
    static final int DEFAULT_LIMIT = 100;

    private final TileKeyService tileKeys;
    private final AppSettingService settings;

    /** Creates the controller. */
    public TileKeysController(TileKeyService tileKeys, AppSettingService settings) {
        this.tileKeys = tileKeys;
        this.settings = settings;
    }

    /** {@code listTileKeys}: newest first, without values. */
    @GetMapping("/v1/tile-keys")
    @Operation(operationId = "listTileKeys", tags = "Tokens")
    public TileKeyDto.Page listTileKeys(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        TileKeyService.Page page = tileKeys.page(cursor, size);
        return new TileKeyDto.Page(page.items().stream().map(TileKeyDto::of).toList(), page.nextCursor());
    }

    /** {@code createTileKey}: 201 with the key and its URL base, shown once. */
    @PostMapping("/v1/tile-keys")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createTileKey", tags = "Tokens")
    public TileKeyDto createTileKey(@RequestBody JsonNode body, HttpServletRequest request) {
        FeedKeyService.CreatedKey created = tileKeys.create(body);
        String base = FeedKeysController.baseUrl(settings, request);
        return TileKeyDto.of(created.entity(), created.key(), TileKeyService.urlBase(base, created.key()));
    }

    /** {@code revokeTileKey}: 204; the key answers 401 INVALID_TILE_KEY from then on. */
    @DeleteMapping("/v1/tile-keys/{tileKeyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "revokeTileKey", tags = "Tokens")
    public void revokeTileKey(@PathVariable String tileKeyId) {
        UUID id;
        try {
            id = UUID.fromString(tileKeyId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Tile key", tileKeyId);
        }
        tileKeys.revoke(id);
    }
}
