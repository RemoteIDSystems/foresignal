package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.pol.PolService;
import ai.foresignal.pol.PolSubjectEntity;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
 * Pattern of life (prompt 114, {@code /v1/pol}; agent scope). Subjects on private sources are admin-only: other
 * callers neither see nor change them, and cannot opt them in ({@code 403}).
 */
@RestController
@Tag(name = "PatternOfLife")
@RequestMapping("/v1/pol")
public class PolController {

    private final PolService pol;

    /** Creates the controller. */
    public PolController(PolService pol) {
        this.pol = pol;
    }

    static boolean admin(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.scope() == Scope.ADMIN).orElse(false);
    }

    static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> "token:" + t.name()).orElse("operator");
    }

    /** {@code listPolSubjects}. */
    @GetMapping("/subjects")
    @Operation(operationId = "listPolSubjects", tags = "PatternOfLife")
    public Map<String, Object> listPolSubjects(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit, HttpServletRequest request) {
        return page(pol.list(cursor, limit(limit), admin(request)));
    }

    /** {@code createPolSubject}. */
    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createPolSubject", tags = "PatternOfLife")
    public ResponseEntity<Map<String, Object>> createPolSubject(@RequestBody JsonNode body, HttpServletRequest request) {
        PolSubjectEntity s = pol.create(body, actor(request), admin(request));
        return ResponseEntity.created(URI.create("/v1/pol/subjects/" + s.getId())).body(pol.view(s));
    }

    /** {@code deletePolSubject}. */
    @DeleteMapping("/subjects/{subjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deletePolSubject", tags = "PatternOfLife")
    public ResponseEntity<Void> deletePolSubject(@PathVariable String subjectId, HttpServletRequest request) {
        pol.delete(subjectId, admin(request));
        return ResponseEntity.noContent().build();
    }

    /** {@code listPolPlaces}. */
    @GetMapping("/subjects/{subjectId}/places")
    @Operation(operationId = "listPolPlaces", tags = "PatternOfLife")
    public Map<String, Object> listPolPlaces(@PathVariable String subjectId, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit, HttpServletRequest request) {
        return page(pol.places(subjectId, cursor, limit(limit), admin(request)));
    }

    /** {@code updatePolPlace}. */
    @PutMapping("/places/{placeId}")
    @Operation(operationId = "updatePolPlace", tags = "PatternOfLife")
    public Map<String, Object> updatePolPlace(@PathVariable String placeId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return pol.label(placeId, body, admin(request));
    }

    /** {@code listPolStays}. */
    @GetMapping("/subjects/{subjectId}/stays")
    @Operation(operationId = "listPolStays", tags = "PatternOfLife")
    public Map<String, Object> listPolStays(@PathVariable String subjectId, @RequestParam(required = false) String from,
            @RequestParam(required = false) String to, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit, HttpServletRequest request) {
        return page(pol.stays(subjectId, instant(from, "from"), instant(to, "to"), cursor, limit(limit),
                admin(request)));
    }

    /** {@code recomputePolSubject}. */
    @PostMapping("/subjects/{subjectId}/recompute")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "recomputePolSubject", tags = "PatternOfLife")
    public ResponseEntity<Map<String, Object>> recomputePolSubject(@PathVariable String subjectId,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(pol.view(pol.recompute(subjectId, admin(request))));
    }

    static Map<String, Object> page(PolService.Page page) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    static int limit(Integer limit) {
        if (limit == null) {
            return 100;
        }
        if (limit < 1 || limit > 500) {
            throw ValidationException.forField("limit", "must be from 1 to 500");
        }
        return limit;
    }

    static Instant instant(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value.strip());
        } catch (DateTimeParseException e) {
            try {
                return java.time.OffsetDateTime.parse(value.strip()).toInstant();
            } catch (DateTimeParseException e2) {
                throw ValidationException.forField(field, "must be an RFC 3339 date-time");
            }
        }
    }
}
