package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.PrincipalViews;
import ai.foresignal.collab.TagEntity;
import ai.foresignal.collab.TagService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ObjectRef;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Tags (prompt 130, tag {@code Collaboration}): {@code listTags}, {@code createTag}, {@code getTag},
 * {@code updateTag}, {@code deleteTag}, {@code assignTag}, {@code unassignTag}, {@code listTaggedObjects} and
 * {@code listObjectTags}. Every operation needs a token.
 */
@RestController
@Tag(name = "Collaboration")
public class TagsController {

    private final TagService tags;
    private final ObjectCallers callers;

    /** Creates the controller. */
    public TagsController(TagService tags, ObjectCallers callers) {
        this.tags = tags;
        this.callers = callers;
    }

    /** {@code listTags}: the caller's personal tags and the shared tags it sees. */
    @GetMapping("/v1/tags")
    @Operation(operationId = "listTags", tags = "Collaboration")
    public Map<String, Object> listTags(@RequestParam(required = false) String namespace,
            @RequestParam(required = false) String q, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        return tags.list(callers.authenticated(request), blank(namespace), q, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code createTag}. */
    @PostMapping("/v1/tags")
    @Operation(operationId = "createTag", tags = "Collaboration")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createTag(@RequestBody JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        TagEntity t = tags.create(body, caller);
        return ResponseEntity.created(URI.create("/v1/tags/" + t.getId())).body(tags.view(t, caller,
                PrincipalViews.cache()));
    }

    /** {@code getTag}. */
    @GetMapping("/v1/tags/{tagId}")
    @Operation(operationId = "getTag", tags = "Collaboration")
    public Map<String, Object> getTag(@PathVariable String tagId, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return tags.view(tags.get(tagId, caller), caller, PrincipalViews.cache());
    }

    /** {@code updateTag} (owner or editor; JSON merge patch). */
    @PatchMapping(value = "/v1/tags/{tagId}", consumes = "application/merge-patch+json")
    @Operation(operationId = "updateTag", tags = "Collaboration")
    public Map<String, Object> updateTag(@PathVariable String tagId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return tags.view(tags.patch(tagId, body, caller), caller, PrincipalViews.cache());
    }

    /** {@code deleteTag} (owner only). */
    @DeleteMapping("/v1/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteTag", tags = "Collaboration")
    public void deleteTag(@PathVariable String tagId, HttpServletRequest request) {
        tags.delete(tagId, callers.authenticated(request));
    }

    /** {@code assignTag}: up to 500 references; invisible ones are skipped. */
    @PostMapping("/v1/tags/{tagId}/assign")
    @Operation(operationId = "assignTag", tags = "Collaboration")
    public Map<String, Object> assignTag(@PathVariable String tagId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return tags.bulk(tagId, refs(body), true, callers.authenticated(request));
    }

    /** {@code unassignTag}. */
    @PostMapping("/v1/tags/{tagId}/unassign")
    @Operation(operationId = "unassignTag", tags = "Collaboration")
    public Map<String, Object> unassignTag(@PathVariable String tagId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return tags.bulk(tagId, refs(body), false, callers.authenticated(request));
    }

    /** {@code listTaggedObjects}: envelopes the caller sees. */
    @GetMapping("/v1/tags/{tagId}/objects")
    @Operation(operationId = "listTaggedObjects", tags = "Collaboration")
    public Map<String, Object> listTaggedObjects(@PathVariable String tagId,
            @RequestParam(required = false) String type, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        return tags.objects(tagId, blank(type), cursor, QueryParams.intInRange("limit", limit, 1, 500, 100),
                callers.authenticated(request));
    }

    /** {@code listObjectTags}: the caller's visible tags on an object. */
    @GetMapping("/v1/objects/{ref}/tags")
    @Operation(operationId = "listObjectTags", tags = "Collaboration")
    public List<Map<String, Object>> listObjectTags(@PathVariable String ref, HttpServletRequest request) {
        return tags.objectTags(ObjectRef.parse(ref), callers.authenticated(request));
    }

    private static List<String> refs(JsonNode body) {
        CollabInputs.object(body, "TagRefsRequest", Set.of("refs"));
        JsonNode refs = body.get("refs");
        if (refs == null || !refs.isArray()) {
            throw ObjectRef.invalid("refs", "must be an array of references");
        }
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        refs.forEach(r -> out.add(r.isString() ? r.asString() : r.toString()));
        return out;
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
