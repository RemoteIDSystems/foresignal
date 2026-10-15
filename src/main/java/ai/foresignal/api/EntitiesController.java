package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.entity.EntityService;
import ai.foresignal.store.query.RecordRow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.databind.JsonNode;

/**
 * The entity registry API (prompt 098): search, resolve and read canonical entities, their records
 * and their keys; admin key assertion, retraction and merges.
 */
@RestController
@Tag(name = "Entities")
public class EntitiesController {

    static final int MAX_LIMIT = 200;

    private final EntityService entities;
    private final ReadCallerResolver callers;

    /** Creates the controller. */
    public EntitiesController(EntityService entities, ReadCallerResolver callers) {
        this.entities = entities;
        this.callers = callers;
    }

    @GetMapping("/v1/entities")
    @Operation(operationId = "listEntities", tags = "Entities")
    public Map<String, Object> listEntities(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) String after,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        String entityType = type == null || type.isBlank() ? null : type.strip();
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, 100);
        Instant when = at == null || at.isBlank() ? null : Instant.parse(at);
        EntityService.EntityPageResult page = entities.page(
                q, entityType, key, when, size, after, callers.hiddenSources(caller));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    @GetMapping("/v1/entities/resolve")
    @Operation(operationId = "resolveEntity", tags = "Entities")
    public Map<String, Object> resolveEntity(
            @RequestParam String key,
            @RequestParam(required = false) String at,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        Instant when = at == null || at.isBlank() ? null : Instant.parse(at);
        Map<String, Object> result = entities.resolve(key, when, callers.hiddenSources(caller));
        if (result == null) {
            throw notFound(key);
        }
        return result;
    }

    @GetMapping("/v1/entities/{entityId}")
    @Operation(operationId = "getEntity", tags = "Entities")
    public Map<String, Object> getEntity(@PathVariable String entityId, HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        Map<String, Object> result = entities.get(entityId, callers.hiddenSources(caller));
        if (result == null) {
            throw notFound(entityId);
        }
        return result;
    }

    @GetMapping("/v1/entities/{entityId}/records")
    @Operation(operationId = "listEntityRecords", tags = "Entities")
    public Map<String, Object> listEntityRecords(
            @PathVariable String entityId,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String includeClosed,
            @RequestParam(required = false) String format,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        Instant when = at == null || at.isBlank() ? null : Instant.parse(at);
        boolean closed = QueryParams.bool("includeClosed", includeClosed, false);
        Map<String, Object> result = entities.records(
                entityId, when, closed, callers.hiddenSources(caller), caller.restriction());
        if (result == null) {
            throw notFound(entityId);
        }
        return result;
    }

    @PostMapping("/v1/entities/{entityId}/keys")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "addEntityKey", tags = "Entities")
    public Map<String, Object> addEntityKey(
            @PathVariable String entityId, @RequestBody JsonNode body, HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        String keyType = requireText(body, "keyType");
        String keyValue = requireText(body, "keyValue");
        Float confidence = body.hasNonNull("confidence") ? (float) body.get("confidence").asDouble() : null;
        Instant validFrom = body.hasNonNull("validFrom") ? Instant.parse(body.get("validFrom").asString()) : null;
        Instant validTo = body.hasNonNull("validTo") ? Instant.parse(body.get("validTo").asString()) : null;
        boolean force = body.hasNonNull("force") && body.get("force").asBoolean();
        Map<String, Object> result = entities.addKey(
                entityId, keyType, keyValue, confidence, validFrom, validTo, force, operatorRef(caller));
        if (result == null) {
            throw notFound(entityId);
        }
        return result;
    }

    @DeleteMapping("/v1/entities/{entityId}/keys/{keyId}")
    @Operation(operationId = "retractEntityKey", tags = "Entities")
    public Map<String, Object> retractEntityKey(
            @PathVariable String entityId, @PathVariable long keyId, HttpServletRequest request) {
        callers.resolveScoped(request);
        Map<String, Object> result = entities.retractKey(entityId, keyId);
        if (result == null) {
            throw notFound(entityId);
        }
        return result;
    }

    @PostMapping("/v1/entities/{entityId}/merge")
    @Operation(operationId = "mergeEntities", tags = "Entities")
    public Map<String, Object> mergeEntities(
            @PathVariable String entityId, @RequestBody JsonNode body, HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        String otherId = requireText(body, "otherId");
        Map<String, Object> result = entities.merge(entityId, otherId, callers.hiddenSources(caller));
        if (result == null) {
            throw notFound(entityId);
        }
        return result;
    }

    private static ai.foresignal.api.error.ForesignalException notFound(String id) {
        return new ai.foresignal.api.error.ForesignalException(
                HttpStatus.NOT_FOUND, ai.foresignal.api.error.ErrorCodes.ENTITY_NOT_FOUND,
                "Entity '" + id + "' was not found.");
    }

    private static String requireText(JsonNode body, String field) {
        if (body == null || !body.hasNonNull(field) || body.get(field).asString().isBlank()) {
            throw new EntityService.ForesignalKeyInvalid("'" + field + "' is required");
        }
        return body.get(field).asString();
    }

    private static String operatorRef(ReadCaller caller) {
        return caller.scope() == null ? "operator" : "operator:" + caller.scope().wireValue();
    }
}
