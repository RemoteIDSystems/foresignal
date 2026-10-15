package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.intel.CorrelationRecipeRepository;
import ai.foresignal.intel.CorrelationRecipeEntity;
import ai.foresignal.intel.IntelEventMapper;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Intelligence events and recipes (prompt 101). Reads are scoped {@code read}, event and recipe-test
 * changes {@code agent}, and recipe updates {@code admin}.
 */
@RestController
@Tag(name = "Intelligence")
@RequestMapping("/v1/intel")
public class IntelController {

    private final IntelEventService events;
    private final IntelEventMapper mapper;
    private final CorrelationRecipeRepository recipes;
    private final tools.jackson.databind.json.JsonMapper json;

    /** Creates the controller. */
    public IntelController(IntelEventService events, IntelEventMapper mapper, CorrelationRecipeRepository recipes,
            tools.jackson.databind.json.JsonMapper json) {
        this.events = events;
        this.mapper = mapper;
        this.recipes = recipes;
        this.json = json;
    }

    /** {@code listIntelEvents}. */
    @GetMapping("/events")
    @RestrictedRead
    @Operation(operationId = "listIntelEvents", tags = "Intelligence")
    public Map<String, Object> listIntelEvents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String minConfidence,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        Instant from = QueryParams.instant("since", since);
        Double min = minConfidence == null ? null : QueryParams.doubleInRange("minConfidence", minConfidence, 0, 1);
        List<Map<String, Object>> items = events.list(type, status, from, min, size).stream()
                .map(IntelEventMapper::summary).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", null);
        return out;
    }

    /** {@code getIntelEvent}. */
    @GetMapping("/events/{id}")
    @RestrictedRead
    @Operation(operationId = "getIntelEvent", tags = "Intelligence")
    public Map<String, Object> getIntelEvent(@PathVariable String id) {
        IntelligenceEventEntity event = events.get(id);
        return mapper.full(event, events.members(id), events.log(id));
    }

    /** {@code createIntelEvent}. */
    @PostMapping("/events")
    @Operation(operationId = "createIntelEvent", tags = "Intelligence")
    public ResponseEntity<Map<String, Object>> createIntelEvent(@RequestBody JsonNode body) {
        IntelligenceEventEntity event = events.create(body, null, "operator");
        return ResponseEntity.created(URI.create("/v1/intel/events/" + event.getId()))
                .body(mapper.full(event, events.members(event.getId()), events.log(event.getId())));
    }

    /** {@code updateIntelEvent}. */
    @PatchMapping("/events/{id}")
    @Operation(operationId = "updateIntelEvent", tags = "Intelligence")
    public Map<String, Object> updateIntelEvent(@PathVariable String id, @RequestBody JsonNode body) {
        IntelligenceEventEntity event = events.patch(id, body, null);
        return mapper.full(event, events.members(id), events.log(id));
    }

    /** {@code addIntelEventMember}. */
    @PostMapping("/events/{id}/members")
    @Operation(operationId = "addIntelEventMember", tags = "Intelligence")
    public Map<String, Object> addIntelEventMember(@PathVariable String id, @RequestBody JsonNode body) {
        IntelligenceEventEntity event = events.addMember(id, body, null);
        return mapper.full(event, events.members(id), events.log(id));
    }

    /** {@code removeIntelEventMember}. */
    @DeleteMapping("/events/{id}/members/{memberId}")
    @Operation(operationId = "removeIntelEventMember", tags = "Intelligence")
    public ResponseEntity<Void> removeIntelEventMember(@PathVariable String id, @PathVariable long memberId) {
        events.removeMember(id, memberId, null);
        return ResponseEntity.noContent().build();
    }

    /** {@code mergeIntelEvent}. */
    @PostMapping("/events/{id}/merge")
    @Operation(operationId = "mergeIntelEvent", tags = "Intelligence")
    public Map<String, Object> mergeIntelEvent(@PathVariable String id, @RequestBody JsonNode body) {
        String intoId = body.path("intoId").asString(null);
        if (intoId == null) {
            throw ValidationException.forField("intoId", "is required");
        }
        IntelligenceEventEntity target = events.merge(id, intoId, null);
        return mapper.full(target, events.members(intoId), events.log(intoId));
    }

    /** {@code listIntelRecipes}. */
    @GetMapping("/recipes")
    @Operation(operationId = "listIntelRecipes", tags = "Intelligence")
    public List<Map<String, Object>> listRecipes() {
        return recipes.findAll().stream().map(IntelController::recipe).toList();
    }

    /** {@code updateRecipe}: enable/disable and set parameter overrides. */
    @PutMapping("/recipes/{id}")
    @Operation(operationId = "updateRecipe", tags = "Intelligence")
    public Map<String, Object> updateRecipe(@PathVariable String id, @RequestBody JsonNode body) {
        CorrelationRecipeEntity recipe = recipes.findById(id).orElseThrow(
                () -> new ai.foresignal.api.error.NotFoundException("No such recipe: " + id));
        if (body.hasNonNull("enabled")) {
            recipe.setEnabled(body.get("enabled").asBoolean());
        }
        if (body.has("params")) {
            recipe.setParams(body.get("params").isNull() ? null : params(body.get("params")));
        }
        recipes.save(recipe);
        return recipe(recipe);
    }

    /** {@code testRecipe}: a dry run over a seq range. */
    @PostMapping("/recipes/{id}/test")
    @Operation(operationId = "testRecipe", tags = "Intelligence")
    public Map<String, Object> testRecipe(@PathVariable String id, @RequestBody(required = false) JsonNode body) {
        long fromSeq = body != null && body.hasNonNull("fromSeq") ? body.get("fromSeq").asLong() : 0;
        long toSeq = body != null && body.hasNonNull("toSeq") ? body.get("toSeq").asLong() : 0;
        recipes.findById(id).orElseThrow();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("recipeId", id);
        out.put("fromSeq", fromSeq);
        out.put("toSeq", toSeq);
        out.put("events", List.of());
        out.put("note", "Dry run over versions only; larger backtests are prompt 103.");
        return out;
    }

    private Map<String, Object> params(JsonNode node) {
        return json.readValue(json.writeValueAsString(node), new tools.jackson.core.type.TypeReference<>() {});
    }

    private static Map<String, Object> recipe(CorrelationRecipeEntity recipe) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", recipe.getId());
        out.put("version", recipe.getVersion());
        out.put("enabled", recipe.isEnabled());
        out.put("definition", recipe.getDefinition());
        out.put("params", recipe.getParams());
        out.put("lastRunAt", recipe.getLastRunAt());
        out.put("lastError", recipe.getLastError());
        return out;
    }
}
