package ai.foresignal.api;

import ai.foresignal.intel.InvestigationItemEntity;
import ai.foresignal.intel.InvestigationEntity;
import ai.foresignal.intel.InvestigationService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.security.CurrentToken;
import jakarta.servlet.http.HttpServletRequest;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Investigations (prompt 101): case files, items with snapshots, and JSON/Markdown/GeoJSON export.
 * Items built from private sources are added, listed, removed and exported only for callers with
 * private access ({@link IntelVisibility}, EGR-1).
 */
@RestController
@Tag(name = "Intelligence")
@RequestMapping("/v1/investigations")
public class InvestigationsController {

    private final InvestigationService investigations;
    private final IntelVisibility visibility;

    /** Creates the controller. */
    public InvestigationsController(InvestigationService investigations, IntelVisibility visibility) {
        this.investigations = investigations;
        this.visibility = visibility;
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> "token:" + t.name()).orElse("operator");
    }

    /** {@code listInvestigations}. */
    @GetMapping
    @RestrictedRead
    @Operation(operationId = "listInvestigations", tags = "Intelligence")
    public Map<String, Object> listInvestigations(@RequestParam(required = false) String status) {
        List<Map<String, Object>> items = investigations.list(status).stream()
                .map(InvestigationsController::view).toList();
        return Map.of("items", items);
    }

    /** {@code getInvestigation}. */
    @GetMapping("/{id}")
    @RestrictedRead
    @Operation(operationId = "getInvestigation", tags = "Intelligence")
    public Map<String, Object> getInvestigation(@PathVariable String id, HttpServletRequest request) {
        Map<String, Object> out = new LinkedHashMap<>(view(investigations.get(id)));
        out.put("items", investigations.items(id, visibility.seesPrivate(request)).stream()
                .map(InvestigationsController::item).toList());
        return out;
    }

    /** {@code createInvestigation}. */
    @PostMapping
    @Operation(operationId = "createInvestigation", tags = "Intelligence")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createInvestigation(@RequestBody JsonNode body,
            HttpServletRequest request) {
        InvestigationEntity entity = investigations.create(body, actor(request));
        return ResponseEntity.created(URI.create("/v1/investigations/" + entity.getId())).body(view(entity));
    }

    /** {@code updateInvestigation}. */
    @PatchMapping("/{id}")
    @Operation(operationId = "updateInvestigation", tags = "Intelligence")
    public Map<String, Object> updateInvestigation(@PathVariable String id, @RequestBody JsonNode body) {
        return view(investigations.patch(id, body));
    }

    /** {@code deleteInvestigation}. */
    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteInvestigation", tags = "Intelligence")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> deleteInvestigation(@PathVariable String id) {
        investigations.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** {@code addInvestigationItem}: 201 with the item. */
    @PostMapping("/{id}/items")
    @Operation(operationId = "addInvestigationItem", tags = "Intelligence")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addInvestigationItem(@PathVariable String id, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return item(investigations.addItem(id, body, actor(request), visibility.seesPrivate(request)));
    }

    /** {@code removeInvestigationItem}. */
    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(operationId = "removeInvestigationItem", tags = "Intelligence")
    @ApiResponse(responseCode = "204", description = "Removed")
    public ResponseEntity<Void> removeInvestigationItem(@PathVariable String id, @PathVariable long itemId,
            HttpServletRequest request) {
        investigations.removeItem(id, itemId, visibility.seesPrivate(request));
        return ResponseEntity.noContent().build();
    }

    /** {@code exportInvestigation}. */
    @GetMapping(value = "/{id}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @RestrictedRead
    @Operation(operationId = "exportInvestigation", tags = "Intelligence")
    public ResponseEntity<String> exportInvestigation(@PathVariable String id,
            @RequestParam(required = false, defaultValue = "json") String format, HttpServletRequest request) {
        String body = investigations.export(id, format, visibility.seesPrivate(request));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(InvestigationService.contentType(format)))
                .body(body);
    }

    private static Map<String, Object> view(InvestigationEntity entity) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", entity.getId());
        out.put("displayId", entity.getDisplayId());
        out.put("title", entity.getTitle());
        out.put("status", entity.getStatus());
        out.put("summary", entity.getSummary());
        out.put("tags", entity.getTags());
        out.put("createdBy", entity.getCreatedBy());
        out.put("createdAt", entity.getCreatedAt());
        out.put("updatedAt", entity.getUpdatedAt());
        return out;
    }

    private static Map<String, Object> item(InvestigationItemEntity entity) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", entity.getId());
        out.put("itemType", entity.getItemType());
        out.put("ref", entity.getRef());
        out.put("seq", entity.getSeq());
        out.put("note", entity.getNote());
        out.put("snapshot", entity.getSnapshot());
        out.put("addedAt", entity.getAddedAt());
        out.put("addedBy", entity.getAddedBy());
        return out;
    }
}
