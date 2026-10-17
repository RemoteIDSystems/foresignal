package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.trip.TripEntity;
import ai.foresignal.trip.TripService;
import ai.foresignal.trip.TripWatch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
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
 * Trips and commutes (prompt 114, {@code /v1/trips}; agent scope). The route always comes from the client's own
 * router; Foresignal buffers and watches the geometry it is given and never calls a routing service.
 */
@RestController
@Tag(name = "Trips")
@RequestMapping("/v1/trips")
public class TripsController {

    private final TripService trips;
    private final TripWatch watch;

    /** Creates the controller. */
    public TripsController(TripService trips, TripWatch watch) {
        this.trips = trips;
        this.watch = watch;
    }

    /** {@code listTrips}. */
    @GetMapping
    @Operation(operationId = "listTrips", tags = "Trips")
    public Map<String, Object> listTrips(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        TripService.Page page = trips.list(cursor, PolController.limit(limit));
        return page(page);
    }

    /** {@code createTrip}. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createTrip", tags = "Trips")
    public ResponseEntity<Map<String, Object>> createTrip(@RequestBody JsonNode body, HttpServletRequest request) {
        String actor = CurrentToken.of(request).map(t -> "token:" + t.name()).orElse("operator");
        TripEntity trip = trips.create(body, actor);
        return ResponseEntity.created(URI.create("/v1/trips/" + trip.getId())).body(trips.view(trip));
    }

    /** {@code getTrip}. */
    @GetMapping("/{tripId}")
    @Operation(operationId = "getTrip", tags = "Trips")
    public Map<String, Object> getTrip(@PathVariable String tripId) {
        return trips.view(trips.get(tripId));
    }

    /** {@code updateTrip}. */
    @PutMapping("/{tripId}")
    @Operation(operationId = "updateTrip", tags = "Trips")
    public Map<String, Object> updateTrip(@PathVariable String tripId, @RequestBody JsonNode body) {
        return trips.view(trips.update(tripId, body));
    }

    /** {@code deleteTrip}. */
    @DeleteMapping("/{tripId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteTrip", tags = "Trips")
    public ResponseEntity<Void> deleteTrip(@PathVariable String tripId) {
        trips.delete(tripId);
        return ResponseEntity.noContent().build();
    }

    /** {@code checkTrip}: the pre-trip check now; {@code dryRun=true} returns it without delivering. */
    @PostMapping("/{tripId}/check")
    @RestrictedRead
    @Operation(operationId = "checkTrip", tags = "Trips")
    public Map<String, Object> checkTrip(@PathVariable String tripId, @RequestBody(required = false) JsonNode body) {
        CheckRequest check = parseCheck(body);
        return TripService.runView(watch.check(tripId, check.dryRun(), check.occurrence()));
    }

    /**
     * A parsed {@code TripCheckRequest}.
     *
     * @param dryRun return the briefing without delivering
     * @param occurrence the occurrence, or null for the next one
     */
    public record CheckRequest(boolean dryRun, Instant occurrence) {}

    /**
     * Parses a {@code TripCheckRequest} (an absent body is {@code dryRun=false}, the next occurrence).
     *
     * @throws ValidationException when it is not valid
     */
    public static CheckRequest parseCheck(JsonNode body) {
        boolean dryRun = false;
        Instant occurrence = null;
        if (body != null && !body.isNull()) {
            if (!body.isObject()) {
                throw ValidationException.forField("body", "must be a TripCheckRequest object");
            }
            JsonNode dry = body.get("dryRun");
            if (dry != null && !dry.isNull()) {
                if (!dry.isBoolean()) {
                    throw ValidationException.forField("dryRun", "must be a boolean");
                }
                dryRun = dry.booleanValue();
            }
            JsonNode at = body.get("occurrence");
            if (at != null && !at.isNull()) {
                try {
                    occurrence = OffsetDateTime.parse(at.asString()).toInstant();
                } catch (DateTimeParseException e) {
                    throw ValidationException.forField("occurrence", "must be an RFC 3339 date-time");
                }
            }
        }
        return new CheckRequest(dryRun, occurrence);
    }

    /** {@code listTripRuns}. */
    @GetMapping("/{tripId}/runs")
    @Operation(operationId = "listTripRuns", tags = "Trips")
    public Map<String, Object> listTripRuns(@PathVariable String tripId, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        return page(trips.runs(tripId, cursor, PolController.limit(limit)));
    }

    private static Map<String, Object> page(TripService.Page page) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("items", page.items());
        out.put("nextCursor", page.nextCursor());
        return out;
    }
}
