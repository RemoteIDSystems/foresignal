package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.export.HoldEntity;
import ai.foresignal.export.HoldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/** Holds (OpenAPI tag {@code Archive}, prompt 111; admin): create, list, read held versions, delete. */
@RestController
@Tag(name = "Archive")
public class HoldsController {

    private final HoldService holds;

    /** Creates the controller. */
    public HoldsController(HoldService holds) {
        this.holds = holds;
    }

    /** {@code listHolds}: holds with their held rows and bytes, newest first. */
    @GetMapping("/v1/holds")
    @Operation(operationId = "listHolds", tags = "Archive")
    public Map<String, Object> listHolds(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        HoldService.Page page = holds.list(cursor, BulkExportsController.parseLimit(limit));
        List<Map<String, Object>> items = page.items().stream().map(holds::view).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code createHold}: create a hold, 201. */
    @PostMapping("/v1/holds")
    @Operation(operationId = "createHold", tags = "Archive")
    public ResponseEntity<Map<String, Object>> createHold(@RequestBody JsonNode body, HttpServletRequest request) {
        HoldEntity hold = holds.create(body, ExportCallers.tokenId(request));
        return ResponseEntity.created(URI.create("/v1/holds/" + hold.getId())).body(holds.view(hold));
    }

    /** {@code deleteHold}: delete a hold and its copies, 204. */
    @DeleteMapping("/v1/holds/{holdId}")
    @Operation(operationId = "deleteHold", tags = "Archive")
    public ResponseEntity<Void> deleteHold(@PathVariable String holdId) {
        holds.delete(holdId);
        return ResponseEntity.noContent().build();
    }

    /** {@code listHoldVersions}: the versions a hold copied, keyset by seq. */
    @GetMapping("/v1/holds/{holdId}/versions")
    @Operation(operationId = "listHoldVersions", tags = "Archive")
    public Map<String, Object> listHoldVersions(@PathVariable String holdId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        Long after = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                after = Long.parseLong(cursor.strip());
            } catch (NumberFormatException e) {
                throw ValidationException.forField("cursor", "is not a cursor of this listing");
            }
        }
        return holds.versions(holdId, after, BulkExportsController.parseLimit(limit));
    }
}
