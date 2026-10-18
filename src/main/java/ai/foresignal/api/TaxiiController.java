package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.stix.StixMapper;
import ai.foresignal.stix.StixService;
import ai.foresignal.stix.TaxiiCollection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * A read-only TAXII 2.1 server (prompt 141; OpenAPI tag {@code Standards}), {@code agent} scope: discovery at
 * {@code /taxii2/}, one API root {@code /taxii2/foresignal/}, and the collections of {@link TaxiiCollection} with
 * their objects.
 *
 * <ul>
 *   <li>Every response is {@value #MEDIA_TYPE}.
 *   <li>Objects: {@code added_after} (strictly after, on the version's ingest time), {@code limit} (default
 *       {@value StixService#DEFAULT_PAGE}, at most {@value StixService#MAX_PAGE}) and {@code next} (the token of the
 *       previous envelope; repeat the same {@code added_after} with it). The envelope says {@code more} and carries
 *       {@code next}; {@code X-TAXII-Date-Added-First} and {@code -Last} give the first and last date added on the
 *       page. Records refused by the egress policy are left out and counted in {@code Foresignal-Licence-Filtered}.
 *   <li>No write endpoints exist: {@code can_write} is false and the API root's {@code max_content_length} is 0.
 * </ul>
 */
@RestController
@Tag(name = "Standards")
public class TaxiiController {

    /** The TAXII 2.1 media type. */
    public static final String MEDIA_TYPE = "application/taxii+json;version=2.1";

    /** The API root path. */
    public static final String API_ROOT = "/taxii2/foresignal/";

    /** The {@code X-TAXII-Date-Added-First} header. */
    public static final String DATE_ADDED_FIRST = "X-TAXII-Date-Added-First";

    /** The {@code X-TAXII-Date-Added-Last} header. */
    public static final String DATE_ADDED_LAST = "X-TAXII-Date-Added-Last";

    private static final MediaType TAXII = MediaType.parseMediaType(MEDIA_TYPE);

    private final StixService stix;
    private final ReadCallerResolver callers;
    private final ProductUrls urls;
    private final JsonMapper json;

    /** Creates the controller. */
    public TaxiiController(StixService stix, ReadCallerResolver callers, ProductUrls urls, JsonMapper json) {
        this.stix = stix;
        this.callers = callers;
        this.urls = urls;
        this.json = json;
    }

    /** {@code getTaxiiDiscovery}. */
    @GetMapping("/taxii2/")
    @Operation(operationId = "getTaxiiDiscovery", tags = "Standards")
    public ResponseEntity<String> discovery(HttpServletRequest request) {
        String root = urls.base(request) + API_ROOT;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", "Foresignal TAXII 2.1");
        body.put("description", "Read-only STIX 2.1 vulnerabilities and indicators from Foresignal's cyber sources.");
        body.put("default", root);
        body.put("api_roots", List.of(root));
        return taxii(body);
    }

    /** {@code getTaxiiApiRoot}. */
    @GetMapping(API_ROOT)
    @Operation(operationId = "getTaxiiApiRoot", tags = "Standards")
    public ResponseEntity<String> apiRoot() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", "Foresignal");
        body.put("description", "Read-only collections of STIX 2.1 objects derived from Foresignal records.");
        body.put("versions", List.of(MEDIA_TYPE));
        body.put("max_content_length", 0);
        return taxii(body);
    }

    /** {@code listTaxiiCollections}. */
    @GetMapping(API_ROOT + "collections/")
    @Operation(operationId = "listTaxiiCollections", tags = "Standards")
    public ResponseEntity<String> collections() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TaxiiCollection collection : TaxiiCollection.values()) {
            list.add(collection(collection));
        }
        return taxii(Map.of("collections", list));
    }

    /** {@code getTaxiiCollection}; 404 for an unknown id. */
    @GetMapping(API_ROOT + "collections/{collectionId}/")
    @Operation(operationId = "getTaxiiCollection", tags = "Standards")
    public ResponseEntity<String> getCollection(@PathVariable String collectionId) {
        return taxii(collection(find(collectionId)));
    }

    /** {@code getTaxiiCollectionObjects}: one envelope of STIX objects. */
    @GetMapping(API_ROOT + "collections/{collectionId}/objects/")
    @Operation(operationId = "getTaxiiCollectionObjects", tags = "Standards")
    @RestrictedRead("versions are read through RecordQueryService.history with the caller's restriction and excluded sources")
    public ResponseEntity<String> objects(
            @PathVariable String collectionId,
            @RequestParam(name = "added_after", required = false) String addedAfter,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) String next,
            HttpServletRequest request) {
        TaxiiCollection collection = find(collectionId);
        int size = QueryParams.intInRange("limit", limit, 1, StixService.MAX_PAGE, StixService.DEFAULT_PAGE);
        ReadCaller caller = callers.resolve(request);
        StixService.Page page = stix.objects(collection, QueryParams.instant("added_after", addedAfter), size,
                next == null || next.isBlank() ? null : next.strip(), callers.excludedSources(caller),
                caller.restriction(), Viewer.of(CurrentToken.of(request)));
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("more", page.more());
        if (page.next() != null) {
            envelope.put("next", page.next());
        }
        envelope.put("objects", page.objects());
        HttpHeaders headers = new HttpHeaders();
        if (page.firstAdded() != null) {
            headers.set(DATE_ADDED_FIRST, StixMapper.timestamp(page.firstAdded()));
            headers.set(DATE_ADDED_LAST, StixMapper.timestamp(page.lastAdded()));
        }
        if (page.refused() > 0) {
            headers.set(ai.foresignal.provenance.LicencePolicyService.FILTERED_HEADER, Integer.toString(page.refused()));
        }
        headers.setCacheControl("private, no-store");
        return ResponseEntity.ok().headers(headers).contentType(TAXII).body(json.writeValueAsString(envelope));
    }

    private static TaxiiCollection find(String id) {
        return TaxiiCollection.byId(id).orElseThrow(() -> NotFoundException.of("Collection", id));
    }

    /** The TAXII collection resource of {@code collection}. */
    static Map<String, Object> collection(TaxiiCollection collection) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", collection.id());
        out.put("title", collection.title());
        out.put("description", collection.description());
        out.put("can_read", true);
        out.put("can_write", false);
        out.put("media_types", List.of(StixMapper.STIX_MEDIA_TYPE));
        return out;
    }

    private ResponseEntity<String> taxii(Object body) {
        return ResponseEntity.ok().contentType(TAXII).body(json.writeValueAsString(body));
    }
}
