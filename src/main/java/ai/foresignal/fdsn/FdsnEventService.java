package ai.foresignal.fdsn;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.NearParser;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Answers FDSN event queries from current {@code earthquake} records (prompt 141).
 *
 * <p>The time window, the rectangle and a circle that fits {@link NearParser#MAX_RADIUS_KM} are pushed into the
 * normal current-state query ({@code timeField=event}, expired records included because past earthquakes are what the
 * service is for), which also applies the caller's excluded sources and restriction. Magnitudes, the exact circle in
 * degrees, the ordering and the limit are applied here. At most {@value #MAX_SCAN} records are read per request.
 */
@Component
public class FdsnEventService {

    /** The record kind served. */
    public static final String KIND = "earthquake";

    /** The most current records one query reads. */
    public static final int MAX_SCAN = 200_000;

    /** The page size used while scanning. */
    static final int PAGE = 5000;

    /** Kilometers per degree of great-circle arc (mean Earth radius 6371.0088 km). */
    static final double KM_PER_DEGREE = 111.19508;

    /** The QuakeML 1.2 BED {@code EventType} enumeration. */
    static final Set<String> EVENT_TYPES = Set.of("not existing", "not reported", "earthquake",
            "anthropogenic event", "collapse", "cavity collapse", "mine collapse", "building collapse", "explosion",
            "accidental explosion", "chemical explosion", "controlled explosion", "experimental explosion",
            "industrial explosion", "mining explosion", "quarry blast", "road cut", "blasting levee",
            "nuclear explosion", "induced or triggered event", "rock burst", "reservoir loading", "fluid injection",
            "fluid extraction", "crash", "plane crash", "train crash", "boat crash", "other event",
            "atmospheric event", "sonic boom", "sonic blast", "acoustic noise", "thunder", "avalanche",
            "snow avalanche", "debris avalanche", "hydroacoustic event", "ice quake", "slide", "landslide",
            "rockslide", "meteorite", "volcanic eruption");

    private final RecordQueryService queries;
    private final JsonMapper json;

    /** Creates the service. */
    public FdsnEventService(RecordQueryService queries, JsonMapper json) {
        this.queries = queries;
        this.json = json;
    }

    /**
     * The events matching {@code query} that the caller may see, ordered and limited.
     *
     * @param excludedSources sources the caller may not read
     * @param restriction the caller's read restriction
     */
    public List<FdsnEvent> query(FdsnEventQuery query, Set<String> excludedSources, ReadRestriction restriction) {
        BboxFilter box = null;
        if (query.hasBox() && query.minLatitude() < query.maxLatitude()) {
            box = BboxParser.parse(query.minLongitude() + "," + query.minLatitude() + "," + query.maxLongitude() + ","
                    + query.maxLatitude());
        }
        NearFilter near = null;
        if (query.hasCircle()) {
            double km = query.maxRadius() * KM_PER_DEGREE * 1.01;
            if (km > 0 && km <= NearParser.MAX_RADIUS_KM) {
                near = new NearFilter(query.longitude(), query.latitude(), km);
            }
        }
        Instant until = query.end() == null ? null : query.end().plusMillis(1);
        List<FdsnEvent> events = new ArrayList<>();
        String cursor = null;
        int scanned = 0;
        do {
            CurrentQuery page = new CurrentQuery(List.of(), List.of(KIND), List.of(), box, near, query.start(), until,
                    TimeField.EVENT, null, true, ListOrder.ID, null, cursor, PAGE, FieldSelection.ALL,
                    GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false, excludedSources, restriction);
            Slices.RecordSlice slice = queries.list(page);
            for (RecordRow row : slice.rows()) {
                FdsnEvent event = event(row);
                if (event != null && matches(event, query)) {
                    events.add(event);
                }
            }
            scanned += slice.rows().size();
            cursor = slice.nextCursor();
        } while (cursor != null && scanned < MAX_SCAN);
        events.sort(comparator(query.orderBy()));
        return events.size() > query.limit() ? List.copyOf(events.subList(0, query.limit())) : events;
    }

    /** The ordering of {@code order}; ties fall back to newest first, then id, so the output is stable. */
    static Comparator<FdsnEvent> comparator(FdsnEventQuery.OrderBy order) {
        Comparator<FdsnEvent> newest = Comparator.comparing(FdsnEvent::time).reversed();
        Comparator<FdsnEvent> stable = Comparator.comparing(FdsnEvent::sourceId).thenComparing(FdsnEvent::externalId);
        Comparator<FdsnEvent> magnitude = Comparator.comparing(e -> e.magnitude() == null ? Double.NEGATIVE_INFINITY
                : e.magnitude());
        return switch (order) {
            case TIME -> newest.thenComparing(stable);
            case TIME_ASC -> Comparator.comparing(FdsnEvent::time).thenComparing(stable);
            case MAGNITUDE -> magnitude.reversed().thenComparing(newest).thenComparing(stable);
            case MAGNITUDE_ASC -> magnitude.thenComparing(newest).thenComparing(stable);
        };
    }

    /** True when {@code event} satisfies the exact filters (time, rectangle, circle, magnitudes). */
    static boolean matches(FdsnEvent event, FdsnEventQuery query) {
        if (query.start() != null && event.time().isBefore(query.start())) {
            return false;
        }
        if (query.end() != null && event.time().isAfter(query.end())) {
            return false;
        }
        if (event.latitude() < query.minLatitude() || event.latitude() > query.maxLatitude()) {
            return false;
        }
        if (!longitudeInside(event.longitude(), query.minLongitude(), query.maxLongitude())) {
            return false;
        }
        if (query.hasCircle()) {
            double degrees = arcDegrees(query.latitude(), query.longitude(), event.latitude(), event.longitude());
            if (degrees < query.minRadius() || degrees > query.maxRadius()) {
                return false;
            }
        }
        if (query.minMagnitude() != null && (event.magnitude() == null || event.magnitude() < query.minMagnitude())) {
            return false;
        }
        return query.maxMagnitude() == null || (event.magnitude() != null && event.magnitude() <= query.maxMagnitude());
    }

    /** True when {@code lon} lies in {@code [min, max]}, or in the antimeridian-crossing span when min > max. */
    static boolean longitudeInside(double lon, double min, double max) {
        return min <= max ? lon >= min && lon <= max : lon >= min || lon <= max;
    }

    /** The great-circle arc between two points, degrees (haversine). */
    static double arcDegrees(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return Math.toDegrees(2 * Math.asin(Math.min(1, Math.sqrt(a))));
    }

    /** The event of a current record, or null when it has no origin time or no point. */
    FdsnEvent event(RecordRow row) {
        if (row.tombstone() || row.eventTime() == null || row.geometry() == null) {
            return null;
        }
        JsonNode geometry = read(row.geometry());
        if (geometry == null || !"Point".equals(geometry.path("type").asString())) {
            return null;
        }
        JsonNode coordinates = geometry.get("coordinates");
        if (coordinates == null || !coordinates.isArray() || coordinates.size() < 2) {
            return null;
        }
        JsonNode payload = row.payload() == null ? null : read(row.payload());
        if (payload == null) {
            payload = json.createObjectNode();
        }
        Double depth = number(payload, "depthKm");
        if (depth == null) {
            depth = number(payload, "depth");
        }
        String author = text(payload, "auth");
        if (author == null) {
            author = text(payload, "net");
        }
        String catalog = text(payload, "sourceCatalog");
        if (catalog == null) {
            catalog = text(payload, "net");
        }
        String type = text(payload, "type");
        String eventType = type != null && EVENT_TYPES.contains(type.toLowerCase(Locale.ROOT))
                ? type.toLowerCase(Locale.ROOT) : "earthquake";
        String place = text(payload, "place");
        if (place == null) {
            place = row.title();
        }
        return new FdsnEvent(row.sourceId(), row.externalId(), row.eventTime(), coordinates.get(1).asDouble(),
                coordinates.get(0).asDouble(), depth, number(payload, "mag"), text(payload, "magType"), place,
                author, catalog, eventType, row.observedAt());
    }

    private JsonNode read(String text) {
        try {
            return json.readTree(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Double number(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            double d = value.asDouble();
            return Double.isFinite(d) ? d : null;
        }
        try {
            double d = Double.parseDouble(value.asString().strip());
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isContainer()) {
            return null;
        }
        String text = value.asString();
        return text == null || text.isBlank() ? null : text.strip();
    }
}
