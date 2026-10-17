package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.exercise.ExerciseRunEntity;
import ai.foresignal.exercise.ExerciseService;
import ai.foresignal.exercise.ExerciseViews;
import ai.foresignal.security.CurrentToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Exercise replays (prompt 115, {@code /v1/exercises}; agent scope, always authenticated):
 * {@code createExercise}, {@code listExercises}, {@code getExercise}, {@code cancelExercise} and
 * {@code purgeExerciseAlerts}.
 */
@RestController
@Tag(name = "Exercises")
@RequestMapping("/v1/exercises")
public class ExercisesController {

    private final ExerciseService exercises;

    /** Creates the controller. */
    public ExercisesController(ExerciseService exercises) {
        this.exercises = exercises;
    }

    /** {@code createExercise}: 201 with the started run, or 200 with a dry-run projection. */
    @PostMapping
    @Operation(operationId = "createExercise", tags = "Exercises")
    public ResponseEntity<Map<String, Object>> createExercise(@RequestBody JsonNode body, HttpServletRequest request) {
        ExerciseRunEntity run = exercises.create(body, actor(request));
        boolean dryRun = body != null && body.path("dryRun").asBoolean(false);
        if (dryRun) {
            return ResponseEntity.ok(ExerciseViews.run(run, true));
        }
        return ResponseEntity.created(URI.create("/v1/exercises/" + run.getId())).body(ExerciseViews.run(run, false));
    }

    /** {@code listExercises}: newest first. */
    @GetMapping
    @Operation(operationId = "listExercises", tags = "Exercises")
    public Map<String, Object> listExercises(@RequestParam(required = false) String status,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        List<ExerciseRunEntity> page = exercises.list(status, decode(cursor), size + 1);
        String next = null;
        if (page.size() > size) {
            page = page.subList(0, size);
            next = encode(page.getLast().getId());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.stream().map(r -> ExerciseViews.run(r, false)).toList());
        out.put("nextCursor", next);
        return out;
    }

    /** {@code getExercise}. */
    @GetMapping("/{exerciseId}")
    @Operation(operationId = "getExercise", tags = "Exercises")
    public Map<String, Object> getExercise(@PathVariable String exerciseId) {
        return ExerciseViews.run(exercises.get(exerciseId), false);
    }

    /** {@code cancelExercise}. */
    @PostMapping("/{exerciseId}/cancel")
    @Operation(operationId = "cancelExercise", tags = "Exercises")
    public Map<String, Object> cancelExercise(@PathVariable String exerciseId, HttpServletRequest request) {
        return ExerciseViews.run(exercises.cancel(exerciseId, actor(request)), false);
    }

    /** {@code purgeExerciseAlerts}: 204. */
    @DeleteMapping("/{exerciseId}/alerts")
    @Operation(operationId = "purgeExerciseAlerts", tags = "Exercises")
    public ResponseEntity<Void> purgeExerciseAlerts(@PathVariable String exerciseId, HttpServletRequest request) {
        exercises.purge(exerciseId, actor(request));
        return ResponseEntity.noContent().build();
    }

    static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> "token:" + t.name()).orElse("operator");
    }

    /** An opaque cursor for an id. */
    static String encode(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    /** The id of a cursor, or null for none. */
    static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", "is not a cursor from this listing");
        }
    }
}
