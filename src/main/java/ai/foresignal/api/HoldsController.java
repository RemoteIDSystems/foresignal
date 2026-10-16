package ai.foresignal.api;

import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.export.HoldEntity;
import ai.foresignal.export.HoldService;
import ai.foresignal.security.restrict.RequestPrincipal;
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

/** Legal holds (OpenAPI tag {@code Archive}, prompt 111): create, list, read versions, delete. */
@RestController
@Tag(name = "Archive")
public class HoldsController {

    private final HoldService holds;
    private final ReadCallerResolver callers;

    /** Creates the controller. */
    public HoldsController(HoldService holds, ReadCallerResolver callers) {
        this.holds = holds;
        this.callers = callers;
    }

    /** {@code listHolds}: every hold. */
    @GetMapping("/v1/holds")
    @Operation(operationId = "listHolds", tags = "Archive")
    public Map<String, Object> listHolds() {
        List<Map<String, Object>> items = holds.list().stream().map(holds::view).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", null);
        return out;
    }

    /** {@code createHold}: create a hold. */
    @PostMapping("/v1/holds")
    @Operation(operationId = "createHold", tags = "Archive")
    public ResponseEntity<Map<String, Object>> createHold(@RequestBody JsonNode body, HttpServletRequest request) {
        HoldEntity hold = holds.create(body, tokenId(request));
        return ResponseEntity.created(URI.create("/v1/holds/" + hold.getId())).body(holds.view(hold));
    }

    /** {@code deleteHold}: delete a hold and its copies. */
    @DeleteMapping("/v1/holds/{holdId}")
    @Operation(operationId = "deleteHold", tags = "Archive")
    public ResponseEntity<Void> deleteHold(@PathVariable String holdId) {
        holds.delete(holdId);
        return ResponseEntity.noContent().build();
    }

    /** {@code listHoldVersions}: the held versions, keyset by seq. */
    @GetMapping("/v1/holds/{holdId}/versions")
    @Operation(operationId = "listHoldVersions", tags = "Archive")
    public Map<String, Object> listHoldVersions(@PathVariable String holdId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        Long after = cursor == null || cursor.isBlank() ? null : Long.parseLong(cursor);
        int size = limit == null || limit.isBlank() ? 100 : Integer.parseInt(limit);
        return holds.versions(holdId, after, size);
    }

    private static String tokenId(HttpServletRequest request) {
        return RequestPrincipal.of(request).map(p -> p.id() == null ? null : p.id().toString()).orElse(null);
    }
}
