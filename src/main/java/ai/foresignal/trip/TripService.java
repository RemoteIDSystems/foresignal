package ai.foresignal.trip;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.trigger.GeofenceMaterializer;
import ai.foresignal.trigger.TriggerRuleService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Trips and commutes (prompt 114, the {@code /v1/trips} API).
 *
 * <p><b>The route comes from the client.</b> A trip names a 036 route place (LineString + {@code bufferMeters}) or
 * carries an inline GeoJSON LineString of at most {@value #MAX_VERTICES} vertices that the client's own router
 * produced; Foresignal never calls a routing service. The route is validated ({@code 400 TRIP_ROUTE_INVALID}),
 * split at the antimeridian, and buffered on save into {@code trip.corridor}
 * ({@code ST_Buffer(route::geography, bufferMeters)::geometry}, {@link GeofenceMaterializer#bufferLine}).
 *
 * <p>Pre-trip checks and the in-trip watch are {@link TripWatch}'s; this class owns the definitions and runs.
 */
@Service
public class TripService {

    /** The most vertices of an inline route. */
    public static final int MAX_VERTICES = 5_000;

    /** The default corridor half-width, meters. */
    public static final int DEFAULT_BUFFER = 2_000;

    /** The widest corridor half-width, meters. */
    public static final int MAX_BUFFER = 50_000;

    /** The default lead time. */
    public static final Duration DEFAULT_LEAD = Duration.ofMinutes(60);

    /** The longest trip. */
    public static final Duration MAX_DURATION = Duration.ofDays(7);

    /** The default hazard kinds. */
    public static final List<String> DEFAULT_KINDS = List.of("nws-alert", "road-event", "work-zone", "fire-perimeter",
            "cyclone-cone", "earthquake", "incident");

    private final TripRepository trips;
    private final TripRunRepository runs;
    private final PlaceService placeService;
    private final GeofenceMaterializer geofences;
    private final NotificationChannelRepository channels;
    private final TriggerRuleService rules;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final TransactionTemplate tx;
    private final Clock clock;

    /** Creates the service. */
    public TripService(TripRepository trips, TripRunRepository runs, PlaceService placeService,
            GeofenceMaterializer geofences, NotificationChannelRepository channels, TriggerRuleService rules,
            JdbcTemplate jdbc, JsonMapper json, PlatformTransactionManager transactions, Clock clock) {
        this.trips = trips;
        this.runs = runs;
        this.placeService = placeService;
        this.geofences = geofences;
        this.channels = channels;
        this.rules = rules;
        this.jdbc = jdbc;
        this.json = json;
        this.tx = new TransactionTemplate(transactions);
        this.clock = clock;
    }

    /**
     * A page.
     *
     * @param items the items
     * @param nextCursor the next page's cursor, or null
     */
    public record Page(List<Map<String, Object>> items, String nextCursor) {}

    // ------------------------------------------------------------------------- reads

    /** A page of trips in creation order. */
    public Page list(String cursor, int limit) {
        String after = decode(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        String next = null;
        for (TripEntity t : trips.findAllByOrderByIdAsc()) {
            if (after != null && t.getId().compareTo(after) <= 0) {
                continue;
            }
            if (items.size() == limit) {
                next = encode((String) items.getLast().get("id"));
                break;
            }
            items.add(view(t));
        }
        return new Page(items, next);
    }

    /**
     * A trip.
     *
     * @throws NotFoundException when there is none
     */
    public TripEntity get(String id) {
        return trips.findById(id).orElseThrow(() -> NotFoundException.of("Trip", id));
    }

    /** The schedule of a trip. */
    public TripSchedule schedule(TripEntity trip) {
        return TripSchedule.fromMap(trip.getSchedule(), json);
    }

    /** The OpenAPI {@code Trip} object. */
    public Map<String, Object> view(TripEntity t) {
        GeoJsonWriter writer = new GeoJsonWriter();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", t.getId());
        out.put("name", t.getName());
        out.put("routePlaceId", t.getRoutePlaceId());
        out.put("route", t.getRoutePlaceId() == null ? writer.write(t.getRoute()) : null);
        out.put("bufferMeters", t.getBufferMeters());
        out.put("schedule", t.getSchedule());
        out.put("expectedDuration", ai.foresignal.settings.Durations.format(Duration.ofSeconds(t.getExpectedDurationSeconds())));
        out.put("leadTime", ai.foresignal.settings.Durations.format(Duration.ofSeconds(t.getLeadTimeSeconds())));
        out.put("hazardKinds", t.getHazardKinds());
        out.put("minSeverity", t.getMinSeverity());
        out.put("channelIds", t.getChannelIds());
        out.put("anchor", t.getAnchor());
        out.put("notifyWhenClear", t.isNotifyWhenClear());
        out.put("enabled", t.isEnabled());
        out.put("corridor", writer.write(t.getCorridor()));
        out.put("nextOccurrence", t.isEnabled() ? schedule(t).next(clock.instant()) : null);
        out.put("createdBy", t.getCreatedBy());
        out.put("createdAt", t.getCreatedAt());
        out.put("updatedAt", t.getUpdatedAt());
        return out;
    }

    // ------------------------------------------------------------------------- writes

    /**
     * Creates a trip from a {@code TripInput}.
     *
     * @throws ValidationException {@code 400 TRIP_ROUTE_INVALID} or another validation failure
     * @throws NotFoundException when the route place, a channel or the anchor does not exist
     */
    public TripEntity create(JsonNode body, String actor) {
        Instant now = clock.instant();
        TripEntity trip = new TripEntity(Ulids.next(now));
        apply(trip, body);
        trip.setCreatedBy(actor);
        trip.setCreatedAt(now);
        trip.setUpdatedAt(now);
        return trips.saveAndFlush(trip);
    }

    /**
     * Replaces a trip whole and re-materializes the corridor; an active ephemeral rule is removed (the watch
     * creates a new one for the new definition).
     */
    public TripEntity update(String id, JsonNode body) {
        TripEntity trip = get(id);
        apply(trip, body);
        removeRule(trip);
        trip.setUpdatedAt(clock.instant());
        return trips.saveAndFlush(trip);
    }

    /** Deletes a trip, its runs and any active ephemeral rule. */
    public void delete(String id) {
        TripEntity trip = get(id);
        removeRule(trip);
        tx.executeWithoutResult(status -> {
            runs.deleteByTripId(trip.getId());
            trips.deleteById(trip.getId());
        });
    }

    /** Deletes the trip's ephemeral rule, if any, and clears it on the entity (not saved). */
    void removeRule(TripEntity trip) {
        UUID ruleId = trip.getActiveRuleId();
        if (ruleId != null) {
            try {
                rules.delete(ruleId);
            } catch (NotFoundException e) {
                // Already gone.
            }
        }
        trip.setActiveRuleId(null);
        trip.setActiveOccurrence(null);
        trip.setActiveUntil(null);
    }

    /**
     * Validates a {@code TripInput} without touching the database: name, route (an inline route fully;
     * {@code TRIP_ROUTE_INVALID}), buffer, schedule, durations, kinds, severity, channel id and anchor formats.
     * Whether the route place, channels and anchor exist is checked on save.
     *
     * @throws ValidationException when it is not valid
     */
    public static void validateInput(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw ValidationException.forField("body", "must be a TripInput object");
        }
        String name = string(body, "name");
        if (name == null || name.isBlank() || name.length() > 200) {
            throw ValidationException.forField("name", "is required (1 to 200 characters)");
        }
        boolean inline = body.has("route") && !body.get("route").isNull();
        if ((string(body, "routePlaceId") == null) == !inline) {
            throw routeInvalid("routePlaceId", "give exactly one of routePlaceId or route");
        }
        if (inline) {
            route(body.get("route"));
        }
        if (body.has("bufferMeters") && !body.get("bufferMeters").isNull()) {
            int meters = integer(body, "bufferMeters");
            if (meters < 1 || meters > MAX_BUFFER) {
                throw ValidationException.forField("bufferMeters", "must be from 1 to 50000");
            }
        }
        TripSchedule.parse(body.get("schedule"));
        Duration duration = duration(body, "expectedDuration", null);
        if (duration == null || duration.isZero() || duration.compareTo(MAX_DURATION) > 0) {
            throw ValidationException.forField("expectedDuration", "is required: more than 0 and at most 7d");
        }
        if (duration(body, "leadTime", DEFAULT_LEAD).compareTo(Duration.ofDays(2)) > 0) {
            throw ValidationException.forField("leadTime", "must be at most 2d");
        }
        List<String> kinds = strings(body, "hazardKinds", DEFAULT_KINDS);
        if (kinds.isEmpty() || kinds.size() > 50) {
            throw ValidationException.forField("hazardKinds", "must name 1 to 50 kinds");
        }
        String minSeverity = string(body, "minSeverity");
        if (minSeverity != null && TripHazards.rank(minSeverity) < 0) {
            throw ValidationException.forField("minSeverity", "must be info, warning or critical");
        }
        for (String channel : strings(body, "channelIds", List.of())) {
            try {
                UUID.fromString(channel);
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("channelIds", "must contain channel ids");
            }
        }
        String anchor = string(body, "anchor");
        if (anchor != null && !anchor.matches("^[a-z0-9-]+/.+$")) {
            throw ValidationException.forField("anchor", "must be {sourceId}/{externalId}");
        }
        bool(body, "notifyWhenClear", true);
        bool(body, "enabled", true);
    }

    /** Validates {@code body} and applies it to {@code trip}, materializing the corridor. */
    private void apply(TripEntity trip, JsonNode body) {
        validateInput(body);
        String name = string(body, "name");
        if (name == null || name.isBlank() || name.length() > 200) {
            throw ValidationException.forField("name", "is required (1 to 200 characters)");
        }
        String placeId = string(body, "routePlaceId");
        boolean inline = body.has("route") && !body.get("route").isNull();
        if ((placeId == null) == !inline) {
            throw routeInvalid("routePlaceId", "give exactly one of routePlaceId or route");
        }
        LineString route;
        Integer buffer = body.has("bufferMeters") && !body.get("bufferMeters").isNull() ? integer(body, "bufferMeters") : null;
        if (placeId != null) {
            PlaceView place = placeService.get(placeId.strip());
            if (!"route".equals(place.shape()) || !(place.geometry() instanceof LineString line)) {
                throw routeInvalid("routePlaceId", "names a place that is not a route");
            }
            route = line;
            if (buffer == null && place.bufferM() != null) {
                buffer = (int) Math.round(place.bufferM());
            }
            trip.setRoutePlaceId(place.id());
        } else {
            route = route(body.get("route"));
            trip.setRoutePlaceId(null);
        }
        int meters = buffer == null ? DEFAULT_BUFFER : buffer;
        if (meters < 1 || meters > MAX_BUFFER) {
            throw ValidationException.forField("bufferMeters", "must be from 1 to 50000");
        }
        TripSchedule schedule = TripSchedule.parse(body.get("schedule"));
        Duration duration = duration(body, "expectedDuration", null);
        if (duration == null || duration.isZero() || duration.compareTo(MAX_DURATION) > 0) {
            throw ValidationException.forField("expectedDuration", "is required: more than 0 and at most 7d");
        }
        Duration lead = duration(body, "leadTime", DEFAULT_LEAD);
        if (lead.compareTo(Duration.ofDays(2)) > 0) {
            throw ValidationException.forField("leadTime", "must be at most 2d");
        }
        List<String> kinds = strings(body, "hazardKinds", DEFAULT_KINDS);
        if (kinds.isEmpty() || kinds.size() > 50) {
            throw ValidationException.forField("hazardKinds", "must name 1 to 50 kinds");
        }
        String minSeverity = string(body, "minSeverity");
        if (minSeverity != null && TripHazards.rank(minSeverity) < 0) {
            throw ValidationException.forField("minSeverity", "must be info, warning or critical");
        }
        List<String> channelIds = strings(body, "channelIds", List.of());
        for (String channel : channelIds) {
            UUID uuid;
            try {
                uuid = UUID.fromString(channel);
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("channelIds", "must contain channel ids");
            }
            if (!channels.existsById(uuid)) {
                throw NotFoundException.of("Channel", channel);
            }
        }
        String anchor = string(body, "anchor");
        if (anchor != null) {
            if (!anchor.matches("^[a-z0-9-]+/.+$")) {
                throw ValidationException.forField("anchor", "must be {sourceId}/{externalId}");
            }
            int slash = anchor.indexOf('/');
            Boolean exists = jdbc.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM record_head WHERE source_id = ? AND external_id = ?)", Boolean.class,
                    anchor.substring(0, slash), anchor.substring(slash + 1));
            if (!Boolean.TRUE.equals(exists)) {
                throw NotFoundException.of("Anchor record", anchor);
            }
        }
        Geometry split = RouteMeasure.splitAntimeridian(route);
        MultiPolygon corridor = geofences.bufferLine(split, meters);
        if (corridor.isEmpty()) {
            throw routeInvalid("route", "the corridor is empty after buffering");
        }
        corridor.setSRID(4326);
        route.setSRID(4326);
        trip.setName(name.strip());
        trip.setRoute(route);
        trip.setBufferMeters(meters);
        trip.setSchedule(schedule.toMap());
        trip.setExpectedDurationSeconds(duration.toSeconds());
        trip.setLeadTimeSeconds(lead.toSeconds());
        trip.setHazardKinds(new ArrayList<>(new LinkedHashSet<>(kinds)));
        trip.setMinSeverity(minSeverity);
        trip.setChannelIds(new ArrayList<>(channelIds));
        trip.setAnchor(anchor);
        trip.setNotifyWhenClear(bool(body, "notifyWhenClear", true));
        trip.setEnabled(bool(body, "enabled", true));
        trip.setCorridor(corridor);
    }

    /**
     * An inline route: a GeoJSON LineString of 2 to {@value #MAX_VERTICES} vertices with valid coordinates and a
     * non-zero length.
     *
     * @throws ValidationException {@code 400 TRIP_ROUTE_INVALID}
     */
    public static LineString route(JsonNode node) {
        Geometry geometry;
        try {
            geometry = new GeoJsonReader(MAX_VERTICES, 2).read(node);
        } catch (RuntimeException e) {
            throw routeInvalid("route", "must be a GeoJSON LineString of at most 5,000 vertices: " + e.getMessage());
        }
        if (!(geometry instanceof LineString line)) {
            throw routeInvalid("route", "must be a GeoJSON LineString (the client's own router computes it)");
        }
        if (line.getNumPoints() < 2 || line.getNumPoints() > MAX_VERTICES) {
            throw routeInvalid("route", "must have 2 to 5,000 vertices");
        }
        boolean moves = false;
        for (Coordinate c : line.getCoordinates()) {
            if (!Double.isFinite(c.x) || !Double.isFinite(c.y) || c.x < -180 || c.x > 180 || c.y < -90 || c.y > 90) {
                throw routeInvalid("route", "has a coordinate outside -180..180, -90..90");
            }
            moves |= !c.equals2D(line.getCoordinateN(0));
        }
        if (!moves) {
            throw routeInvalid("route", "has zero length");
        }
        return line;
    }

    static ValidationException routeInvalid(String field, String message) {
        return new ValidationException(ErrorCodes.TRIP_ROUTE_INVALID, "The trip route is not valid.",
                List.of(new FieldError(field, message)));
    }

    // ------------------------------------------------------------------------- runs

    /** A page of a trip's runs, newest first. */
    public Page runs(String tripId, String cursor, int limit) {
        get(tripId);
        String after = decode(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        String next = null;
        boolean skipping = after != null;
        for (TripRunEntity run : runs.findByTripIdOrderByCreatedAtDescIdDesc(tripId)) {
            if (skipping) {
                if (run.getId().equals(after)) {
                    skipping = false;
                }
                continue;
            }
            if (items.size() == limit) {
                next = encode((String) items.getLast().get("id"));
                break;
            }
            items.add(runView(run));
        }
        return new Page(items, next);
    }

    /** The OpenAPI {@code TripRun} object. */
    public static Map<String, Object> runView(TripRunEntity run) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", run.getId());
        out.put("tripId", run.getTripId());
        out.put("occurrence", run.getOccurrence());
        out.put("status", run.getStatus());
        out.put("clear", run.isClear());
        out.put("hazards", run.getHazards());
        out.put("briefing", run.getBriefing());
        out.put("deliveredAt", run.getDeliveredAt());
        out.put("createdAt", run.getCreatedAt());
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    private static String string(JsonNode body, String field) {
        JsonNode n = body.get(field);
        if (n == null || n.isNull()) {
            return null;
        }
        if (!n.isString()) {
            throw ValidationException.forField(field, "must be a string");
        }
        return n.asString();
    }

    private static int integer(JsonNode body, String field) {
        JsonNode n = body.get(field);
        if (!n.isIntegralNumber()) {
            throw ValidationException.forField(field, "must be an integer");
        }
        return n.intValue();
    }

    private static boolean bool(JsonNode body, String field, boolean def) {
        JsonNode n = body.get(field);
        if (n == null || n.isNull()) {
            return def;
        }
        if (!n.isBoolean()) {
            throw ValidationException.forField(field, "must be a boolean");
        }
        return n.booleanValue();
    }

    private static Duration duration(JsonNode body, String field, Duration def) {
        String text = string(body, field);
        if (text == null) {
            return def;
        }
        try {
            return ai.foresignal.settings.Durations.parse(text);
        } catch (RuntimeException e) {
            throw ValidationException.forField(field, "must be an ISO-8601 duration or shorthand such as 60m");
        }
    }

    private static List<String> strings(JsonNode body, String field, List<String> def) {
        JsonNode n = body.get(field);
        if (n == null || n.isNull()) {
            return def;
        }
        if (!n.isArray()) {
            throw ValidationException.forField(field, "must be an array of strings");
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : n) {
            if (!item.isString() || item.asString().isBlank()) {
                throw ValidationException.forField(field, "must be an array of strings");
            }
            out.add(item.asString().strip());
        }
        return out;
    }

    static String encode(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("trip:" + id).getBytes(StandardCharsets.UTF_8));
    }

    static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!text.startsWith("trip:")) {
                throw new IllegalArgumentException();
            }
            return text.substring(5);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                    List.of(new FieldError("cursor", "is not a cursor of this listing")));
        }
    }
}
