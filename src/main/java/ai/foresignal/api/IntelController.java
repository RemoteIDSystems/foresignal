package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.intel.CorrelationEngine;
import ai.foresignal.intel.CorrelationRecipeRepository;
import ai.foresignal.intel.CorrelationRecipeEntity;
import ai.foresignal.intel.IntelCalibrationService;
import ai.foresignal.intel.IntelEventMapper;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.intel.RecipeDefinition;
import ai.foresignal.intel.RecipeRegistry;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Intelligence events and recipes (prompt 101). Reads are scoped {@code read}, event and recipe-test
 * changes {@code agent}, and recipe updates {@code admin}.
 *
 * <p>Every path applies the private-event rule ({@link IntelVisibility}, EGR-1): a private event is
 * listed, read, changed or dry-run only for a caller with private access, and is {@code 404} to anyone
 * else.
 */
@RestController
@Tag(name = "Intelligence")
@RequestMapping("/v1/intel")
public class IntelController {

    /** The most would-be events a dry run returns. */
    static final int MAX_TEST_EVENTS = 100;

    private final IntelEventService events;
    private final IntelEventMapper mapper;
    private final CorrelationRecipeRepository recipes;
    private final RecipeRegistry registry;
    private final CorrelationEngine engine;
    private final IntelVisibility visibility;
    private final IntelCalibrationService calibration;
    private final tools.jackson.databind.json.JsonMapper json;

    /** Creates the controller. */
    public IntelController(IntelEventService events, IntelEventMapper mapper, CorrelationRecipeRepository recipes,
            RecipeRegistry registry, CorrelationEngine engine, IntelVisibility visibility,
            IntelCalibrationService calibration, tools.jackson.databind.json.JsonMapper json) {
        this.events = events;
        this.mapper = mapper;
        this.recipes = recipes;
        this.registry = registry;
        this.engine = engine;
        this.visibility = visibility;
        this.calibration = calibration;
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
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        Instant from = QueryParams.instant("since", since);
        Double min = minConfidence == null ? null : QueryParams.doubleInRange("minConfidence", minConfidence, 0, 1);
        List<Map<String, Object>> items = events.list(type, status, from, min, size, visibility.seesPrivate(request))
                .stream().map(IntelEventMapper::summary).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", null);
        return out;
    }

    /** {@code getIntelEvent}. */
    @GetMapping("/events/{id}")
    @RestrictedRead
    @Operation(operationId = "getIntelEvent", tags = "Intelligence")
    public Map<String, Object> getIntelEvent(@PathVariable String id, HttpServletRequest request) {
        IntelligenceEventEntity event = events.visible(id, visibility.seesPrivate(request));
        return mapper.full(event, events.members(id), events.log(id));
    }

    /** {@code createIntelEvent}. */
    @PostMapping("/events")
    @Operation(operationId = "createIntelEvent", tags = "Intelligence")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createIntelEvent(@RequestBody JsonNode body,
            HttpServletRequest request) {
        String origin = "agent".equals(body.path("origin").asString(null)) ? "agent" : "operator";
        IntelligenceEventEntity event = events.create(body, actor(request), origin, visibility.seesPrivate(request));
        return ResponseEntity.created(URI.create("/v1/intel/events/" + event.getId()))
                .body(mapper.full(event, events.members(event.getId()), events.log(event.getId())));
    }

    /** {@code updateIntelEvent}. */
    @PatchMapping("/events/{id}")
    @Operation(operationId = "updateIntelEvent", tags = "Intelligence")
    public Map<String, Object> updateIntelEvent(@PathVariable String id, @RequestBody JsonNode body,
            HttpServletRequest request) {
        IntelligenceEventEntity event = events.patch(id, body, actor(request), visibility.seesPrivate(request));
        return mapper.full(event, events.members(id), events.log(id));
    }

    /** {@code addIntelEventMember}: 201 with the event. */
    @PostMapping("/events/{id}/members")
    @Operation(operationId = "addIntelEventMember", tags = "Intelligence")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addIntelEventMember(@PathVariable String id, @RequestBody JsonNode body,
            HttpServletRequest request) {
        IntelligenceEventEntity event = events.addMember(id, body, actor(request), visibility.seesPrivate(request));
        return mapper.full(event, events.members(id), events.log(id));
    }

    /** {@code removeIntelEventMember}. */
    @DeleteMapping("/events/{id}/members/{memberId}")
    @Operation(operationId = "removeIntelEventMember", tags = "Intelligence")
    @ApiResponse(responseCode = "204", description = "Removed")
    public ResponseEntity<Void> removeIntelEventMember(@PathVariable String id, @PathVariable long memberId,
            HttpServletRequest request) {
        events.removeMember(id, memberId, actor(request), visibility.seesPrivate(request));
        return ResponseEntity.noContent().build();
    }

    /** {@code mergeIntelEvent}. */
    @PostMapping("/events/{id}/merge")
    @Operation(operationId = "mergeIntelEvent", tags = "Intelligence")
    public Map<String, Object> mergeIntelEvent(@PathVariable String id, @RequestBody JsonNode body,
            HttpServletRequest request) {
        String intoId = body.path("intoId").asString(null);
        if (intoId == null) {
            throw ValidationException.forField("intoId", "is required");
        }
        IntelligenceEventEntity target = events.merge(id, intoId, actor(request), visibility.seesPrivate(request));
        return mapper.full(target, events.members(intoId), events.log(intoId));
    }

    /**
     * {@code getIntelCalibration}: how calibrated intelligence confidence is (prompt 151). Returns the calibration
     * state of every scope, each with whether it is calibrated, the resolved sample size, the Brier score, the 10-bin
     * reliability table and the CORP recalibration curve.
     */
    @GetMapping("/calibration")
    @RestrictedRead
    @Operation(operationId = "getIntelCalibration", tags = "Intelligence")
    public Map<String, Object> getIntelCalibration() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scopes", calibration.all());
        return out;
    }

    /** {@code getIntelCalibrationScope}: the calibration of one scope ({@code intel-event}, {@code precursor-active}). */
    @GetMapping("/calibration/{scope}")
    @RestrictedRead
    @Operation(operationId = "getIntelCalibrationScope", tags = "Intelligence")
    public Map<String, Object> getIntelCalibrationScope(@PathVariable String scope) {
        return calibration.reliability(scope);
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
                () -> new NotFoundException("No such recipe: " + id));
        if (body.hasNonNull("enabled")) {
            recipe.setEnabled(body.get("enabled").asBoolean());
        }
        if (body.has("params")) {
            recipe.setParams(body.get("params").isNull() ? null : params(body.get("params")));
        }
        recipes.save(recipe);
        return recipe(recipe);
    }

    /**
     * {@code testRecipe}: dry-runs the recipe over {@code (fromSeq, toSeq]} into memory and returns the
     * would-be events; nothing is written. An unknown recipe is {@code 404}; a range wider than
     * {@value CorrelationEngine#MAX_TEST_RANGE} seqs is {@code 400} (larger runs are 103 backtests).
     */
    @PostMapping("/recipes/{id}/test")
    @Operation(operationId = "testRecipe", tags = "Intelligence")
    public Map<String, Object> testRecipe(@PathVariable String id, @RequestBody(required = false) JsonNode body,
            HttpServletRequest request) {
        RecipeDefinition definition = registry.definition(id)
                .orElseThrow(() -> new NotFoundException("No such recipe: " + id));
        if (body == null || !body.path("fromSeq").isIntegralNumber() || !body.path("toSeq").isIntegralNumber()) {
            throw ValidationException.forField("fromSeq", "fromSeq and toSeq are required integers");
        }
        long fromSeq = body.get("fromSeq").asLong();
        long toSeq = body.get("toSeq").asLong();
        if (fromSeq < 0 || toSeq <= fromSeq) {
            throw ValidationException.forField("toSeq", "must be greater than fromSeq (and fromSeq ≥ 0)");
        }
        if (toSeq - fromSeq > CorrelationEngine.MAX_TEST_RANGE) {
            throw ValidationException.forField("toSeq",
                    "a dry run covers at most " + CorrelationEngine.MAX_TEST_RANGE + " seqs; use a 103 backtest");
        }
        List<CorrelationEngine.Candidate> candidates = engine.dryRun(definition, fromSeq, toSeq,
                visibility.seesPrivate(request));
        List<Map<String, Object>> would = new ArrayList<>();
        for (CorrelationEngine.Candidate candidate : candidates) {
            if (would.size() >= MAX_TEST_EVENTS) {
                break;
            }
            would.add(candidate.view());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("recipeId", id);
        out.put("recipeVersion", definition.version());
        out.put("fromSeq", fromSeq);
        out.put("toSeq", toSeq);
        out.put("events", would);
        out.put("truncated", candidates.size() > MAX_TEST_EVENTS);
        return out;
    }

    private Map<String, Object> params(JsonNode node) {
        return json.readValue(json.writeValueAsString(node), new tools.jackson.core.type.TypeReference<>() {});
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> "token:" + t.name()).orElse("operator");
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
