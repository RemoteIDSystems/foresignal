package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.media.outlet.MediaProviders;
import ai.foresignal.media.outlet.MediaVocabulary;
import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.media.outlet.OutletViews;
import ai.foresignal.media.outlet.RatingImportService;
import ai.foresignal.news.NewsViews;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.EgressPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The media outlet registry and rating providers (OpenAPI tag {@code Media}, prompt 133). Outlets are {@code read}
 * scope; edits, providers and imports are {@code admin}. Ratings come only from declared providers: the registry edit
 * never touches a rating, and enabling a provider or importing its ratings needs an admin's license acknowledgement.
 */
@RestController
@Tag(name = "Media")
public class MediaOutletsController {

    /** The largest ratings upload, bytes. */
    static final int MAX_IMPORT_BYTES = 20 * 1024 * 1024;

    private final NewsViews views;
    private final OutletRegistry outlets;
    private final MediaProviders providers;
    private final RatingImportService imports;
    private final JsonMapper json;

    /** Creates the controller. */
    public MediaOutletsController(NewsViews views, OutletRegistry outlets, MediaProviders providers,
            RatingImportService imports, JsonMapper json) {
        this.views = views;
        this.outlets = outlets;
        this.providers = providers;
        this.imports = imports;
        this.json = json;
    }

    /** {@code listMediaOutlets}: keyset on id; consensus per dimension. */
    @GetMapping("/v1/media/outlets")
    @Operation(operationId = "listMediaOutlets", tags = "Media")
    public Map<String, Object> listMediaOutlets(@RequestParam(required = false) String q,
            @RequestParam(required = false) String country, @RequestParam(required = false) String type,
            @RequestParam(required = false) String bucket, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        String t = type == null || type.isBlank() ? null
                : QueryParams.oneOf("type", type.strip(), MediaVocabulary.OUTLET_TYPES, null);
        String b = bucket == null || bucket.isBlank() ? null
                : QueryParams.oneOf("bucket", bucket.strip(), MediaVocabulary.BUCKETS, null);
        int size = QueryParams.intInRange("limit", limit, 1, OutletRegistry.MAX_LIMIT, OutletRegistry.DEFAULT_LIMIT);
        return views.outlets(q, country, t, b, cursor, size, EgressPath.API_READ).toMap();
    }

    /** {@code getMediaOutlet}: ratings per provider with license and date; 404 {@code UNKNOWN_OUTLET}. */
    @GetMapping("/v1/media/outlets/{outletId}")
    @Operation(operationId = "getMediaOutlet", tags = "Media")
    public Map<String, Object> getMediaOutlet(@PathVariable String outletId) {
        return views.outlet(outletId, EgressPath.API_READ);
    }

    /** {@code updateMediaOutlet}: JSON merge patch (admin); ratings cannot be edited here. */
    @PatchMapping("/v1/media/outlets/{outletId}")
    @Operation(operationId = "updateMediaOutlet", tags = "Media")
    public Map<String, Object> updateMediaOutlet(@PathVariable String outletId,
            @RequestBody(required = false) JsonNode body) {
        OutletRegistry.Outlet o = outlets.patch(outletId, body);
        return OutletViews.outlet(o, outlets.ratings(o.id()), outlets.consensus(o.id()), true);
    }

    /** {@code listMediaProviders} (admin). */
    @GetMapping("/v1/media/providers")
    @Operation(operationId = "listMediaProviders", tags = "Media")
    public List<Map<String, Object>> listMediaProviders() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (MediaProviders.State s : providers.list()) {
            out.add(s.view());
        }
        return out;
    }

    /** {@code getMediaProvider} (admin). */
    @GetMapping("/v1/media/providers/{providerId}")
    @Operation(operationId = "getMediaProvider", tags = "Media")
    public Map<String, Object> getMediaProvider(@PathVariable String providerId) {
        return providers.get(providerId).view();
    }

    /**
     * {@code updateMediaProvider} (admin): enable or disable, acknowledge the license, record permission, store the
     * MBFC key; 409 {@code PROVIDER_LICENCE_NOT_ACKNOWLEDGED} when enabling without acknowledgement. A change of the
     * enabled set or of an egress recomputes every consensus.
     */
    @PutMapping("/v1/media/providers/{providerId}")
    @Operation(operationId = "updateMediaProvider", tags = "Media")
    public Map<String, Object> updateMediaProvider(@PathVariable String providerId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        MediaProviders.State before = providers.get(providerId);
        MediaProviders.Input input = input(body);
        MediaProviders.State after = providers.update(providerId, input, actor(request));
        if (before.enabled() != after.enabled() || !before.egress().equals(after.egress())) {
            imports.recomputeAll();
        }
        return after.view();
    }

    /**
     * {@code importMediaRatings} (admin): a provider import ({@code wikidata}, {@code partisan-web}) or operator rows
     * ({@code operator}, {@code allsides}, {@code mbfc}) as a multipart file, JSON {@code {rows}} or CSV text.
     */
    @PostMapping("/v1/media/ratings/import")
    @Operation(operationId = "importMediaRatings", tags = "Media")
    public Map<String, Object> importMediaRatings(@RequestParam(required = false) String provider,
            HttpServletRequest request) throws IOException {
        String p = provider == null || provider.isBlank() ? MediaVocabulary.OPERATOR : provider.strip();
        return imports.importRatings(p, body(request), actor(request));
    }

    private RatingImportService.Body body(HttpServletRequest request) throws IOException {
        String type = request.getContentType() == null ? "" : request.getContentType().toLowerCase(Locale.ROOT);
        if (request instanceof MultipartHttpServletRequest multipart) {
            MultipartFile file = multipart.getFile("file");
            if (file == null || file.isEmpty()) {
                throw invalid("file", "is required: the CSV or JSON ratings file");
            }
            if (file.getSize() > MAX_IMPORT_BYTES) {
                throw invalid("file", "is larger than " + MAX_IMPORT_BYTES + " bytes");
            }
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
            boolean jsonFile = name.endsWith(".json") || (file.getContentType() != null
                    && file.getContentType().toLowerCase(Locale.ROOT).contains("json"));
            return jsonFile ? new RatingImportService.Body(parse(text), null) : new RatingImportService.Body(null, text);
        }
        byte[] bytes = read(request.getInputStream());
        if (bytes.length == 0) {
            return RatingImportService.Body.NONE;
        }
        String text = new String(bytes, StandardCharsets.UTF_8);
        if (type.contains("json")) {
            return new RatingImportService.Body(parse(text), null);
        }
        if (type.contains("csv") || type.startsWith("text/")) {
            return new RatingImportService.Body(null, text);
        }
        throw invalid("body", "must be multipart/form-data, application/json or text/csv");
    }

    private JsonNode parse(String text) {
        try {
            return json.readTree(text);
        } catch (RuntimeException e) {
            throw invalid("body", "is not valid JSON");
        }
    }

    private static byte[] read(InputStream in) throws IOException {
        byte[] bytes = in.readNBytes(MAX_IMPORT_BYTES + 1);
        if (bytes.length > MAX_IMPORT_BYTES) {
            throw invalid("body", "is larger than " + MAX_IMPORT_BYTES + " bytes");
        }
        return bytes;
    }

    private static MediaProviders.Input input(JsonNode body) {
        if (body == null || body.isNull()) {
            return new MediaProviders.Input(null, null, null, null);
        }
        if (!body.isObject()) {
            throw ValidationException.forField("body", "must be a JSON object");
        }
        Set<String> allowed = Set.of("enabled", "licenceAcknowledged", "permissionRecorded", "apiKey");
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!allowed.contains(name)) {
                errors.add(new FieldError(name, "is not a provider setting"));
            }
        }
        for (String name : List.of("enabled", "licenceAcknowledged", "permissionRecorded")) {
            if (body.has(name) && !body.get(name).isBoolean()) {
                errors.add(new FieldError(name, "must be a boolean"));
            }
        }
        if (body.has("apiKey") && !body.get("apiKey").isString()) {
            errors.add(new FieldError("apiKey", "must be a string"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The provider update is not valid.", errors);
        }
        return new MediaProviders.Input(bool(body, "enabled"), bool(body, "licenceAcknowledged"),
                bool(body, "permissionRecorded"), body.has("apiKey") ? body.get("apiKey").asString() : null);
    }

    private static Boolean bool(JsonNode body, String name) {
        return body.has(name) ? body.get(name).asBoolean() : null;
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(c -> "token:" + c.id()).orElse("admin");
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.RATINGS_IMPORT_INVALID, "The ratings import is not valid: " + message
                + ".", List.of(new FieldError(field, message)));
    }
}
