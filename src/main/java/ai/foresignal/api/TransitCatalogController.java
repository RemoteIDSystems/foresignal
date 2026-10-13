package ai.foresignal.api;

import ai.foresignal.api.dto.TransitCatalogEntryDto;
import ai.foresignal.api.dto.TransitCatalogPage;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.source.adapters.transit.MobilityCatalog;
import ai.foresignal.source.adapters.transit.TransitFamily;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Mobility Database catalog search (OpenAPI tag {@code Transit}, prompt 070): {@code searchTransitCatalog},
 * {@code admin}.
 *
 * <p>Read-only: it lists rows of the vendored snapshot ({@link MobilityCatalog}) and whether each is already enabled.
 * There is no POST. A feed is enabled only through {@code foresignal.transit.catalog-feeds} and a restart, so no
 * transit URL is ever accepted from an API caller.
 */
@RestController
@Tag(name = "Transit")
public class TransitCatalogController {

    static final int DEFAULT_LIMIT = 100;
    static final int MAX_LIMIT = 500;
    static final String CURSOR_PREFIX = "t1.";

    private final MobilityCatalog catalog;
    private final TransitFamily family;

    /** Creates the controller. */
    public TransitCatalogController(MobilityCatalog catalog, TransitFamily family) {
        this.catalog = catalog;
        this.family = family;
    }

    /**
     * {@code searchTransitCatalog}: snapshot rows matching every filter, by id.
     *
     * @param country an ISO 3166-1 alpha-2 code
     * @param q a case-insensitive substring of the provider or the id
     * @param entityType {@code vp}, {@code tu} or {@code sa}
     * @param cursor a previous page's {@code nextCursor}
     * @param limit the page size, 1 to 500 (default 100)
     */
    @GetMapping("/v1/transit/catalog")
    @Operation(operationId = "searchTransitCatalog", tags = "Transit")
    public TransitCatalogPage searchTransitCatalog(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        if (country != null && !country.isBlank() && !country.strip().matches("^[A-Za-z]{2}$")) {
            throw ValidationException.forField("country", "must be an ISO 3166-1 alpha-2 code such as US or NO");
        }
        String type = QueryParams.oneOf("entityType", entityType, MobilityCatalog.ENTITY_TYPES, null);
        int max = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        String after = decode(cursor);
        Map<String, List<String>> enabled = family.enabledCatalogFeeds();
        List<TransitCatalogEntryDto> items = new ArrayList<>();
        String last = null;
        boolean more = false;
        for (MobilityCatalog.Entry entry : catalog.search(country, q, type)) {
            if (after != null && entry.id().compareTo(after) <= 0) {
                continue;
            }
            if (items.size() == max) {
                more = true;
                break;
            }
            items.add(dto(entry, enabled.get(entry.id())));
            last = entry.id();
        }
        return new TransitCatalogPage(items, more && last != null ? encode(last) : null);
    }

    static TransitCatalogEntryDto dto(MobilityCatalog.Entry entry, List<String> sourceIds) {
        List<String> ids = sourceIds == null ? List.of() : List.copyOf(sourceIds);
        return new TransitCatalogEntryDto(entry.id(), entry.provider(), entry.countryCode(), entry.entityType(),
                entry.directDownloadUrl().toString(), entry.licenseUrl(), entry.staticReference(), !ids.isEmpty(),
                ids.isEmpty() ? null : ids.get(0), ids);
    }

    static String encode(String id) {
        return CURSOR_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            if (!cursor.startsWith(CURSOR_PREFIX)) {
                throw new IllegalArgumentException("prefix");
            }
            String id = new String(Base64.getUrlDecoder().decode(cursor.substring(CURSOR_PREFIX.length())),
                    StandardCharsets.UTF_8);
            if (id.isBlank() || id.length() > 200) {
                throw new IllegalArgumentException("shape");
            }
            return id;
        } catch (IllegalArgumentException e) {
            throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_CURSOR,
                    "The cursor is not one this endpoint issued.");
        }
    }
}
