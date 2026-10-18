package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.Actors;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.social.SocialViews;
import ai.foresignal.social.narrative.MediaFingerprinter;
import ai.foresignal.social.narrative.NarrativeService;
import ai.foresignal.social.narrative.NarrativeViews;
import ai.foresignal.social.narrative.NarrativeVocabulary;
import ai.foresignal.social.narrative.Pdq;
import ai.foresignal.source.spi.ImageRejectedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Narratives, claims and media fingerprints (OpenAPI tag {@code Social}, prompt 136): narratives with their posts,
 * propagation and timeline (read), merge and split ({@code agent}), claims (read), fingerprints and on-demand image
 * matching (read; matching needs a token). Claims are reports: draw them in a "reported" style, never as observed
 * events.
 */
@RestController
@Tag(name = "Social")
public class NarrativeController {

    private static final String ULID = "^[0-9A-HJKMNP-TV-Z]{26}$";

    private final NarrativeViews views;
    private final NarrativeService service;
    private final MediaFingerprinter media;
    private final ReadCallerResolver readers;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the controller. */
    public NarrativeController(NarrativeViews views, NarrativeService service, MediaFingerprinter media,
            ReadCallerResolver readers, ObjectCallers callers, JsonMapper json) {
        this.views = views;
        this.service = service;
        this.media = media;
        this.readers = readers;
        this.callers = callers;
        this.json = json;
    }

    /** {@code listNarratives}: newest activity first. */
    @GetMapping("/v1/social/narratives")
    @Operation(operationId = "listNarratives", tags = "Social")
    public Map<String, Object> listNarratives(@RequestParam(required = false) String topic,
            @RequestParam(required = false) String situationId, @RequestParam(required = false) String place,
            @RequestParam(required = false) List<String> status, @RequestParam(required = false) List<String> state,
            @RequestParam(required = false) String platform, @RequestParam(required = false) String since,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, NarrativeViews.MAX_LIMIT, NarrativeViews.DEFAULT_LIMIT);
        return views.narratives(blank(topic), blank(situationId), blank(place),
                new ArrayList<>(QueryParams.enumSet("status", status, NarrativeVocabulary.NARRATIVE_STATUSES)),
                new ArrayList<>(QueryParams.enumSet("state", state, NarrativeVocabulary.STATES)),
                SocialViews.network("platform", platform), QueryParams.instant("since", since), cursor, size).toMap();
    }

    /** {@code getNarrative}: 404 {@code UNKNOWN_NARRATIVE}. */
    @GetMapping("/v1/social/narratives/{narrativeId}")
    @Operation(operationId = "getNarrative", tags = "Social")
    public Map<String, Object> getNarrative(@PathVariable String narrativeId, HttpServletRequest request) {
        return views.detail(narrativeId, new ResolveContext(callers.of(request), null));
    }

    /** {@code listNarrativePosts}: member posts newest first, with their link reason and evidence group. */
    @GetMapping("/v1/social/narratives/{narrativeId}/posts")
    @RestrictedRead("posts of sources outside the restriction are left out; spatial restrictions see none")
    @Operation(operationId = "listNarrativePosts", tags = "Social")
    public Map<String, Object> listNarrativePosts(@PathVariable String narrativeId,
            @RequestParam(required = false) String platform, @RequestParam(required = false) String evidenceGroup,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        int size = QueryParams.intInRange("limit", limit, 1, NarrativeViews.MAX_LIMIT, NarrativeViews.DEFAULT_LIMIT);
        ReadCaller caller = readers.resolveScoped(request);
        return views.posts(narrativeId, SocialViews.network("platform", platform), ulid("evidenceGroup", evidenceGroup),
                cursor, size, SocialViews.PostFilter.of(caller.restriction(), readers.excludedSources(caller)))
                .toMap();
    }

    /** {@code getNarrativePropagation}. */
    @GetMapping("/v1/social/narratives/{narrativeId}/propagation")
    @Operation(operationId = "getNarrativePropagation", tags = "Social")
    public Map<String, Object> getNarrativePropagation(@PathVariable String narrativeId) {
        return views.propagation(narrativeId);
    }

    /** {@code getNarrativeTimeline}. */
    @GetMapping("/v1/social/narratives/{narrativeId}/timeline")
    @Operation(operationId = "getNarrativeTimeline", tags = "Social")
    public Map<String, Object> getNarrativeTimeline(@PathVariable String narrativeId,
            @RequestParam(required = false) String interval) {
        String i = QueryParams.oneOf("interval", interval, List.of("PT5M", "PT15M", "PT1H", "P1D"), "PT1H");
        return views.timeline(narrativeId, i);
    }

    /** {@code mergeNarratives} (agent): returns the survivor. */
    @PostMapping("/v1/social/narratives/{narrativeId}/merge")
    @Operation(operationId = "mergeNarratives", tags = "Social")
    public Map<String, Object> mergeNarratives(@PathVariable String narrativeId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        if (body == null || !body.isObject() || body.get("narrativeIds") == null || !body.get("narrativeIds").isArray()
                || body.get("narrativeIds").isEmpty() || body.get("narrativeIds").size() > 50) {
            throw ValidationException.forField("narrativeIds", "is required: 1 to 50 narrative ids");
        }
        List<String> ids = new ArrayList<>();
        for (JsonNode id : body.get("narrativeIds")) {
            ids.add(ulid("narrativeIds", id.asString("")));
        }
        views.require(narrativeId);
        service.merge(narrativeId, ids, Actors.of(request), reason(body));
        return views.detail(narrativeId, new ResolveContext(callers.of(request), null));
    }

    /** {@code splitNarrative} (agent): 201 with the new narrative. */
    @PostMapping("/v1/social/narratives/{narrativeId}/split")
    @Operation(operationId = "splitNarrative", tags = "Social")
    @ApiResponse(responseCode = "201", description = "New narrative")
    public ResponseEntity<Map<String, Object>> splitNarrative(@PathVariable String narrativeId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        if (body == null || !body.isObject() || body.get("postRefs") == null || !body.get("postRefs").isArray()
                || body.get("postRefs").isEmpty() || body.get("postRefs").size() > 10_000) {
            throw ValidationException.forField("postRefs", "is required: 1 to 10000 member post refs");
        }
        List<String> refs = new ArrayList<>();
        for (JsonNode ref : body.get("postRefs")) {
            refs.add(ref.asString(""));
        }
        views.require(narrativeId);
        String id = service.split(narrativeId, refs, Actors.of(request), reason(body));
        return ResponseEntity.created(URI.create("/v1/social/narratives/" + id))
                .body(views.detail(id, new ResolveContext(callers.of(request), null)));
    }

    /** {@code listClaims}: newest first. */
    @GetMapping("/v1/social/claims")
    @Operation(operationId = "listClaims", tags = "Social")
    public Map<String, Object> listClaims(@RequestParam(required = false) String narrativeId,
            @RequestParam(required = false) String situationId, @RequestParam(required = false) String type,
            @RequestParam(required = false) List<String> status, @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String since, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, NarrativeViews.MAX_LIMIT, NarrativeViews.DEFAULT_LIMIT);
        return views.claims(ulid("narrativeId", narrativeId), ulid("situationId", situationId), blank(type),
                new ArrayList<>(QueryParams.enumSet("status", status, NarrativeVocabulary.CLAIM_STATUSES)),
                bbox(bbox), QueryParams.instant("since", since), cursor, size).toMap();
    }

    /** {@code getClaim}: 404 {@code UNKNOWN_CLAIM}. */
    @GetMapping("/v1/social/claims/{claimId}")
    @Operation(operationId = "getClaim", tags = "Social")
    public Map<String, Object> getClaim(@PathVariable String claimId) {
        return views.claimDetail(claimId);
    }

    /** {@code getMediaFingerprint}: 404 {@code UNKNOWN_FINGERPRINT}. */
    @GetMapping("/v1/social/media/{fingerprintId}")
    @Operation(operationId = "getMediaFingerprint", tags = "Social")
    public Map<String, Object> getMediaFingerprint(@PathVariable String fingerprintId, HttpServletRequest request) {
        return views.fingerprintDetail(fingerprintId, new ResolveContext(callers.of(request), null));
    }

    /**
     * {@code matchMedia}: a multipart image upload (through 043 ImageGuard; 415 {@code IMAGE_REJECTED}, 413 when too
     * large) or a JSON PDQ hash. The uploaded image is not stored.
     */
    @PostMapping("/v1/social/media/match")
    @Operation(operationId = "matchMedia", tags = "Social")
    public Map<String, Object> matchMedia(HttpServletRequest request) throws IOException {
        if (request instanceof MultipartHttpServletRequest multipart) {
            MultipartFile file = multipart.getFile("file");
            if (file == null || file.isEmpty()) {
                throw ValidationException.forField("file", "is required: the image to match");
            }
            int maxDistance = QueryParams.intInRange("maxDistance", multipart.getParameter("maxDistance"), 0, 63,
                    NarrativeVocabulary.PDQ_MATCH_DISTANCE);
            int limit = QueryParams.intInRange("limit", multipart.getParameter("limit"), 1, 100, 20);
            Pdq.Hash hash;
            try {
                hash = media.hash(file.getBytes(), file.getContentType() == null
                        || file.getContentType().startsWith("application/octet-stream") ? null : file.getContentType());
            } catch (ImageRejectedException e) {
                HttpStatus status = ImageRejectedException.FILE_TOO_LARGE.equals(e.tag()) ? HttpStatus.CONTENT_TOO_LARGE
                        : HttpStatus.UNSUPPORTED_MEDIA_TYPE;
                throw new ForesignalException(status, ErrorCodes.IMAGE_REJECTED, "The image was rejected: "
                        + e.reason()).withProperty("reason", e.tag());
            }
            return views.match(hash, "upload", true, maxDistance, limit);
        }
        JsonNode body;
        try (InputStream in = request.getInputStream()) {
            byte[] bytes = in.readNBytes(64 * 1024);
            body = bytes.length == 0 ? null : json.readTree(bytes);
        } catch (RuntimeException e) {
            throw ValidationException.forField("body", "must be a MediaMatchRequest object or a multipart upload");
        }
        if (body == null || !body.isObject() || body.get("pdq") == null || !body.get("pdq").isString()
                || !body.get("pdq").asString().matches("^[0-9a-f]{64}$")) {
            throw ValidationException.forField("pdq", "is required: 64 lower-case hex characters");
        }
        int maxDistance = number(body, "maxDistance", 0, 63, NarrativeVocabulary.PDQ_MATCH_DISTANCE);
        int limit = number(body, "limit", 1, 100, 20);
        return views.match(Pdq.parse(body.get("pdq").asString()), "pdq", false, maxDistance, limit);
    }

    private static int number(JsonNode body, String field, int min, int max, int defaultValue) {
        JsonNode v = body.get(field);
        if (v == null || v.isNull()) {
            return defaultValue;
        }
        if (!v.isIntegralNumber() || v.asInt() < min || v.asInt() > max) {
            throw ValidationException.forField(field, "must be an integer from " + min + " to " + max);
        }
        return v.asInt();
    }

    private static String reason(JsonNode body) {
        JsonNode r = body.get("reason");
        if (r == null || r.isNull()) {
            return null;
        }
        String s = r.asString("");
        if (s.length() > 2000) {
            throw ValidationException.forField("reason", "must be at most 2000 characters");
        }
        return s.isBlank() ? null : s;
    }

    private static String ulid(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.strip();
        if (!v.matches(ULID)) {
            throw ValidationException.forField(field, "'" + value + "' is not a ULID");
        }
        return v;
    }

    private static double[] bbox(String bbox) {
        if (bbox == null || bbox.isBlank()) {
            return null;
        }
        String[] parts = bbox.split(",");
        if (parts.length != 4) {
            throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat");
        }
        double[] out = new double[4];
        try {
            for (int i = 0; i < 4; i++) {
                out[i] = Double.parseDouble(parts[i].strip());
            }
        } catch (NumberFormatException e) {
            throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat");
        }
        if (out[0] < -180 || out[2] > 180 || out[1] < -90 || out[3] > 90 || out[0] > out[2] || out[1] > out[3]) {
            throw ValidationException.forField("bbox", "is outside WGS84 or inverted");
        }
        return out;
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
