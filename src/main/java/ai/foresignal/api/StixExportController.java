package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.provenance.LicencePolicyService;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.stix.StixMapper;
import ai.foresignal.stix.StixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code exportStix} (prompt 141; OpenAPI tag {@code Standards}): a STIX 2.1 bundle of the cyber records, read-only,
 * {@code agent} scope.
 *
 * <p>{@code kind} (repeatable or comma-separated) narrows to {@code vulnerability}, {@code malware-url} and
 * {@code malware-c2} (default all three); any other kind is a 400. {@code since} bounds the observed time. Every
 * record passes {@code EgressPolicy} on the {@code EXPORT} path ({@link StixService}); the number refused is reported
 * in {@code Foresignal-Licence-Filtered}. The body is {@code application/stix+json;version=2.1}, or
 * {@code application/json} for a client that asks only for that.
 */
@RestController
@Tag(name = "Standards")
public class StixExportController {

    private final StixService stix;
    private final ReadCallerResolver callers;
    private final JsonMapper json;

    /** Creates the controller. */
    public StixExportController(StixService stix, ReadCallerResolver callers, JsonMapper json) {
        this.stix = stix;
        this.callers = callers;
        this.json = json;
    }

    /** {@code exportStix}. */
    @GetMapping("/v1/export/stix")
    @Operation(operationId = "exportStix", tags = "Standards")
    @RestrictedRead("records are read through RecordQueryService with the caller's restriction and excluded sources")
    public ResponseEntity<String> exportStix(
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String since,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            HttpServletRequest request) {
        List<String> kinds = kinds(QueryParams.multi("kind", kind));
        Instant sinceAt = QueryParams.instant("since", since);
        ReadCaller caller = callers.resolve(request);
        StixService.Bundle bundle = stix.export(kinds, sinceAt, callers.excludedSources(caller), caller.restriction(),
                Viewer.of(CurrentToken.of(request)));
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("private, no-store");
        if (bundle.refused() > 0) {
            headers.set(LicencePolicyService.FILTERED_HEADER, Integer.toString(bundle.refused()));
        }
        return ResponseEntity.ok().headers(headers).contentType(mediaType(accept))
                .body(json.writeValueAsString(bundle.bundle()));
    }

    /** The requested kinds, each a STIX-mapped kind; every mapped kind when none is given. */
    static List<String> kinds(List<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return List.of(StixMapper.VULNERABILITY_KIND, StixMapper.URL_KIND, StixMapper.C2_KIND);
        }
        List<String> out = new ArrayList<>();
        for (String kind : requested) {
            if (!StixMapper.KINDS.contains(kind)) {
                throw ValidationException.forField("kind", "'" + kind + "' has no STIX mapping; use "
                        + StixMapper.VULNERABILITY_KIND + ", " + StixMapper.URL_KIND + " or " + StixMapper.C2_KIND);
            }
            if (!out.contains(kind)) {
                out.add(kind);
            }
        }
        return out;
    }

    /** {@code application/json} when the client asks for it and not for STIX; the STIX media type otherwise. */
    static MediaType mediaType(String accept) {
        if (accept != null && accept.contains("application/json") && !accept.contains("stix")) {
            return MediaType.APPLICATION_JSON;
        }
        return MediaType.parseMediaType(StixMapper.STIX_MEDIA_TYPE);
    }
}
