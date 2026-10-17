package ai.foresignal.object;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.observability.ObservabilityDeclarations;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.observability.Place;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.adapters.satdump.SatDumpDescriptor;
import ai.foresignal.source.adapters.satdump.StationLocation;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.space.CelestrakElementsService;
import ai.foresignal.space.Observer;
import ai.foresignal.space.PassPredictor;
import ai.foresignal.space.PredictedPass;
import ai.foresignal.store.SourceSettingsEntity;
import ai.foresignal.store.SourceSettingsRepository;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Sensor discovery ({@code listSensors}, prompt 129): what can observe an area in a window, as one list of
 * {@code sensor} envelopes. Read-only: Foresignal cannot task public satellites or third-party sensors; local
 * receivers are configured through their sources.
 *
 * <ul>
 *   <li>{@code source}: enabled sources whose 116 observability for the area is above {@value #MIN_P_DETECT}
 *       (with {@code pDetect}, method and families);</li>
 *   <li>{@code receiver}: local receivers (069) whose last reported position is within {@value #RECEIVER_RANGE_KM}
 *       km (the nominal ADS-B radio horizon);</li>
 *   <li>{@code station}: SatDump ground stations (049) within {@value #STATION_RANGE_KM} km (the reception radius of a
 *       low-Earth-orbit pass at 10° elevation);</li>
 *   <li>{@code satellite}: predicted passes (064 Orekit) of the curated satellites ({@link SatelliteCapabilities}) over
 *       the area's center, at least 10° above the horizon;</li>
 *   <li>{@code scene} and {@code raster-product}: STAC scene footprints (096) in the window and raster products (042)
 *       intersecting the area, with links.</li>
 * </ul>
 */
@Service
public class SensorDiscoveryService {

    /** The lowest detection probability that lists a source. */
    public static final double MIN_P_DETECT = 0.2;

    /** Nominal local receiver range, km. */
    public static final double RECEIVER_RANGE_KM = 400;

    /** Nominal SatDump station reception radius, km. */
    public static final double STATION_RANGE_KM = 2500;

    /** Longest discovery window. */
    public static final Duration MAX_WINDOW = Duration.ofDays(7);

    /** Lowest pass elevation, degrees. */
    static final double MIN_ELEVATION_DEG = 10;

    /** Sensor kinds. */
    public static final List<String> KINDS = ObjectRef.SENSOR_KINDS;

    /** The note every result carries. */
    public static final String NOTE = "Foresignal cannot task public satellites or third-party sensors";

    private static final Logger log = LoggerFactory.getLogger(SensorDiscoveryService.class);
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};
    private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    private final ObjectProvider<ObservabilityService> observability;
    private final ObjectProvider<ObservabilityDeclarations> declarations;
    private final SourceRegistry registry;
    private final ObjectProvider<SourceService> sourceService;
    private final SourceSettingsRepository settings;
    private final RecordQueryService records;
    private final ObjectProvider<CelestrakElementsService> elements;
    private final SatelliteCapabilities capabilities;
    private final ObjectProvider<ProductCatalogService> products;
    private final ObjectProvider<PlaceService> places;
    private final JsonMapper json;
    private final Clock clock;
    private final GeoJsonReader reader = new GeoJsonReader();

    /** Creates the service. */
    public SensorDiscoveryService(ObjectProvider<ObservabilityService> observability,
            ObjectProvider<ObservabilityDeclarations> declarations, SourceRegistry registry,
            ObjectProvider<SourceService> sourceService, SourceSettingsRepository settings, RecordQueryService records,
            ObjectProvider<CelestrakElementsService> elements, SatelliteCapabilities capabilities,
            ObjectProvider<ProductCatalogService> products, ObjectProvider<PlaceService> places, JsonMapper json,
            Clock clock) {
        this.observability = observability;
        this.declarations = declarations;
        this.registry = registry;
        this.sourceService = sourceService;
        this.settings = settings;
        this.records = records;
        this.elements = elements;
        this.capabilities = capabilities;
        this.products = products;
        this.places = places;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A discovery request.
     *
     * @param bbox the area as a bbox, or null
     * @param placeId the area as a place, or null
     * @param from the window start
     * @param to the window end
     * @param kinds the sensor kinds to list (empty for all)
     * @param limit the most sensors returned
     */
    public record Request(BboxFilter bbox, String placeId, Instant from, Instant to, Set<String> kinds, int limit) {}

    /**
     * Discovers the sensors for {@code request}.
     *
     * @param request the area, window and kinds
     * @param caller the caller ({@code placeId} needs a token)
     * @return the {@code SensorDiscoveryResult} view
     */
    public Map<String, Object> discover(Request request, ObjectCaller caller) {
        Geometry area = area(request, caller);
        Instant from = request.from() == null ? clock.instant() : request.from();
        Instant to = request.to() == null ? from.plus(Duration.ofDays(1)) : request.to();
        List<FieldError> errors = new ArrayList<>();
        if (!to.isAfter(from)) {
            errors.add(new FieldError("to", "must be after from"));
        }
        if (Duration.between(from, to).compareTo(MAX_WINDOW) > 0) {
            errors.add(new FieldError("to", "the window is at most 7 days"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The discovery window is not valid.", errors);
        }
        Set<String> kinds = request.kinds() == null || request.kinds().isEmpty() ? new LinkedHashSet<>(KINDS)
                : request.kinds();
        Set<String> enabled = new LinkedHashSet<>();
        for (SourceSettingsEntity s : settings.findByEnabledTrue()) {
            if (caller.canRead(s.getSourceId())) {
                enabled.add(s.getSourceId());
            }
        }
        List<Map<String, Object>> items = new ArrayList<>();
        if (kinds.contains("source")) {
            items.addAll(safe("source", () -> sources(area, from, enabled)));
        }
        if (kinds.contains("receiver")) {
            items.addAll(safe("receiver", () -> receivers(area, enabled, caller)));
        }
        if (kinds.contains("station")) {
            items.addAll(safe("station", () -> stations(area, enabled)));
        }
        if (kinds.contains("satellite")) {
            items.addAll(safe("satellite", () -> satellites(area, from, to)));
        }
        if (kinds.contains("scene")) {
            items.addAll(safe("scene", () -> scenes(area, from, to, caller)));
        }
        if (kinds.contains("raster-product")) {
            items.addAll(safe("raster-product", () -> rasterProducts(area, caller)));
        }
        boolean truncated = items.size() > request.limit();
        Map<String, Object> out = new LinkedHashMap<>();
        Envelope e = area.getEnvelopeInternal();
        out.put("bbox", List.of(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()));
        out.put("placeId", request.placeId());
        out.put("from", from);
        out.put("to", to);
        out.put("items", truncated ? items.subList(0, request.limit()) : items);
        out.put("truncated", truncated);
        out.put("note", NOTE);
        return out;
    }

    private Geometry area(Request request, ObjectCaller caller) {
        if (request.placeId() != null && !request.placeId().isBlank()) {
            if (caller.anonymous()) {
                throw new ai.foresignal.api.error.ForesignalException(org.springframework.http.HttpStatus.UNAUTHORIZED,
                        ErrorCodes.UNAUTHORIZED, "placeId needs a token (places are never anonymous).");
            }
            PlaceService service = places.getIfAvailable();
            if (service == null) {
                throw new ai.foresignal.api.error.NotFoundException(ErrorCodes.UNKNOWN_PLACE, "Places are not available.");
            }
            PlaceView place = service.get(request.placeId());
            Geometry g = place.area() != null ? place.area() : place.geometry();
            if (g == null) {
                throw ValidationException.forField("placeId", "the place has no area");
            }
            return g;
        }
        if (request.bbox() == null || request.bbox().envelopes().isEmpty()) {
            throw ValidationException.forField("bbox", "bbox or placeId is required");
        }
        Geometry union = null;
        for (Envelope e : request.bbox().envelopes()) {
            Geometry g = GEOMETRY.toGeometry(e);
            union = union == null ? g : union.union(g);
        }
        return union;
    }

    // ------------------------------------------------------------------------- sources

    List<Map<String, Object>> sources(Geometry area, Instant at, Set<String> enabled) {
        ObservabilityService service = observability.getIfAvailable();
        ObservabilityDeclarations declared = declarations.getIfAvailable();
        if (service == null || declared == null) {
            return List.of();
        }
        Set<String> families = new TreeSet<>(declared.eventFamilies());
        families.addAll(declared.trackFamilies());
        Map<String, Double> best = new LinkedHashMap<>();
        Map<String, Set<String>> methods = new LinkedHashMap<>();
        Map<String, Set<String>> byFamily = new LinkedHashMap<>();
        Place place = Place.polygon(area);
        for (String family : families) {
            ObservabilityService.Detectability d;
            try {
                d = service.pDetect(family, place, at, null);
            } catch (RuntimeException e) {
                continue;
            }
            if (d == null || d.estimates() == null) {
                continue;
            }
            for (ObservabilityService.SourceEstimate s : d.estimates()) {
                if (!enabled.contains(s.sourceId()) || s.p() <= MIN_P_DETECT) {
                    continue;
                }
                best.merge(s.sourceId(), s.p(), Math::max);
                methods.computeIfAbsent(s.sourceId(), k -> new TreeSet<>()).add(s.method());
                byFamily.computeIfAbsent(s.sourceId(), k -> new TreeSet<>()).add(family);
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        best.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()
                .thenComparing(Map.Entry.comparingByKey())).forEach(e -> {
                    Map<String, Object> sensor = base("source", e.getKey(), sourceName(e.getKey()),
                            "Observes " + String.join(", ", byFamily.get(e.getKey())) + " here (p_detect "
                                    + Math.round(e.getValue() * 100) + "%)", List.of(e.getKey()));
                    sensor.put("pDetect", e.getValue());
                    sensor.put("method", String.join(", ", methods.get(e.getKey())));
                    sensor.put("families", List.copyOf(byFamily.get(e.getKey())));
                    out.add(sensor);
                });
        return out;
    }

    // ------------------------------------------------------------------------- receivers and stations

    List<Map<String, Object>> receivers(Geometry area, Set<String> enabled, ObjectCaller caller) {
        List<Map<String, Object>> out = new ArrayList<>();
        Point center = area.getCentroid();
        for (String id : enabled) {
            if (!(id.startsWith("local-") || id.startsWith("station-readsb"))) {
                continue;
            }
            Optional<double[]> position = lastPosition(id, caller);
            if (position.isEmpty()) {
                continue;
            }
            double km = distanceToArea(area, center, position.get());
            if (km <= RECEIVER_RANGE_KM) {
                Map<String, Object> sensor = base("receiver", id, sourceName(id),
                        "Local receiver " + Math.round(km) + " km away", List.of(id));
                sensor.put("rangeKm", RECEIVER_RANGE_KM);
                sensor.put("geometry", Map.of("type", "Point", "coordinates", List.of(position.get()[0],
                        position.get()[1])));
                out.add(sensor);
            }
        }
        return out;
    }

    List<Map<String, Object>> stations(Geometry area, Set<String> enabled) {
        List<Map<String, Object>> out = new ArrayList<>();
        SourceService service = sourceService.getIfAvailable();
        if (service == null) {
            return out;
        }
        Point center = area.getCentroid();
        for (String id : enabled) {
            if (!id.startsWith(SatDumpDescriptor.PREFIX)) {
                continue;
            }
            double[] position;
            try {
                StationLocation location = StationLocation.parse(service.effectiveParams(id)
                        .string(SatDumpDescriptor.LOCATION));
                position = stationPosition(location);
            } catch (RuntimeException e) {
                continue;
            }
            if (position == null) {
                continue;
            }
            double km = distanceToArea(area, center, position);
            if (km <= STATION_RANGE_KM) {
                Map<String, Object> sensor = base("station", id, sourceName(id),
                        "SatDump ground station " + Math.round(km) + " km away", List.of(id));
                sensor.put("rangeKm", STATION_RANGE_KM);
                sensor.put("geometry", Map.of("type", "Point", "coordinates", List.of(position[0], position[1])));
                out.add(sensor);
            }
        }
        return out;
    }

    private double[] stationPosition(StationLocation location) {
        if (location.lat() != null && location.lon() != null) {
            return new double[] {location.lon(), location.lat()};
        }
        PlaceService service = places.getIfAvailable();
        if (location.placeId() != null && service != null) {
            return service.find(location.placeId()).map(p -> p.geometry() == null ? null
                    : new double[] {p.geometry().getCentroid().getX(), p.geometry().getCentroid().getY()}).orElse(null);
        }
        return null;
    }

    private Optional<double[]> lastPosition(String sourceId, ObjectCaller caller) {
        CurrentQuery q = new CurrentQuery(List.of(sourceId), null, null, null, null, null, null, TimeField.EFFECTIVE,
                null, true, ListOrder.OBSERVED, null, null, 5, FieldSelection.ALL, GeometryOptions.DEFAULT,
                GeometryEncoding.GEOJSON, false, caller.excludedSources());
        for (RecordRow row : records.list(q).rows()) {
            if (row.geometry() == null || row.geometry().isBlank()) {
                continue;
            }
            try {
                Point p = reader.read(row.geometry()).getCentroid();
                return Optional.of(new double[] {p.getX(), p.getY()});
            } catch (RuntimeException e) {
                // try the next row
            }
        }
        return Optional.empty();
    }

    /** Distance from a point to the area: 0 inside, else to the area's center. */
    static double distanceToArea(Geometry area, Point center, double[] lonLat) {
        Point p = GEOMETRY.createPoint(new Coordinate(lonLat[0], lonLat[1]));
        if (area.intersects(p)) {
            return 0;
        }
        return Geo.haversineKm(lonLat[1], lonLat[0], center.getY(), center.getX());
    }

    // ------------------------------------------------------------------------- satellites

    List<Map<String, Object>> satellites(Geometry area, Instant from, Instant to) {
        CelestrakElementsService service = elements.getIfAvailable();
        List<Map<String, Object>> out = new ArrayList<>();
        if (service == null) {
            return out;
        }
        Point center = area.getCentroid();
        Observer observer = new Observer(center.getY(), center.getX(), 0);
        Duration horizon = Duration.between(from, to);
        for (SatelliteCapabilities.Capability c : capabilities.all()) {
            var omm = service.byNorad(c.norad());
            if (omm.isEmpty()) {
                continue;
            }
            List<PredictedPass> passes;
            try {
                passes = PassPredictor.predict(omm.get(), observer, from, horizon, MIN_ELEVATION_DEG);
            } catch (RuntimeException e) {
                continue;
            }
            if (passes.isEmpty()) {
                continue;
            }
            Map<String, Object> sensor = base("satellite", Long.toString(c.norad()), c.name(),
                    passes.size() + " pass" + (passes.size() == 1 ? "" : "es") + " above "
                            + Math.round(MIN_ELEVATION_DEG) + "° in the window", List.of());
            List<Map<String, Object>> views = new ArrayList<>();
            for (PredictedPass p : passes.subList(0, Math.min(20, passes.size()))) {
                Map<String, Object> v = new LinkedHashMap<>();
                v.put("aos", p.aos());
                v.put("tca", p.tca());
                v.put("los", p.los());
                v.put("maxElevationDeg", Math.round(p.maxElevationDeg() * 10) / 10.0);
                views.add(v);
            }
            sensor.put("passes", views);
            sensor.put("capabilities", c.view());
            Map<String, Object> time = new LinkedHashMap<>();
            time.put("start", passes.get(0).aos());
            time.put("end", passes.get(passes.size() - 1).los());
            time.put("observedAt", null);
            sensor.put("time", time);
            out.add(sensor);
        }
        out.sort(Comparator.comparing(s -> String.valueOf(((Map<?, ?>) s.get("time")).get("start"))));
        return out;
    }

    // ------------------------------------------------------------------------- imagery

    List<Map<String, Object>> scenes(Geometry area, Instant from, Instant to, ObjectCaller caller) {
        Envelope e = area.getEnvelopeInternal();
        CurrentQuery q = new CurrentQuery(null, List.of("satellite-scene"), null, new BboxFilter(List.of(e)), null,
                from, to, TimeField.EFFECTIVE, null, true, ListOrder.OBSERVED, null, null, 50, FieldSelection.ALL,
                GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false, caller.excludedSources());
        List<Map<String, Object>> out = new ArrayList<>();
        for (RecordRow row : records.list(q).rows()) {
            Map<String, Object> payload = row.payload() == null ? Map.of() : json.readValue(row.payload(), MAP);
            String id = row.sourceId() + "/" + ObjectRef.encode(row.externalId());
            Map<String, Object> sensor = base("scene", id, row.title() == null ? row.externalId() : row.title(),
                    payload.get("platform") == null ? "Satellite scene" : "Scene from " + payload.get("platform"),
                    List.of(row.sourceId()));
            Geometry footprint = null;
            try {
                footprint = row.geometry() == null ? null : reader.read(row.geometry());
            } catch (RuntimeException ex) {
                // no footprint
            }
            EnvelopeGeometry.Shape shape = EnvelopeGeometry.of(footprint);
            sensor.put("footprint", shape == null ? null : shape.geoJson());
            sensor.put("geometry", shape == null ? null : shape.geoJson());
            sensor.put("bbox", shape == null ? null : shape.bbox());
            sensor.put("acquiredAt", row.observedAt());
            Map<String, Object> links = new LinkedHashMap<>();
            links.put("self", "/v1/records/" + row.sourceId() + "/" + ObjectRef.encode(row.externalId()));
            links.put("related", "/v1/objects/" + ObjectRef.record(row.sourceId(), row.externalId(), null)
                    + "/related");
            if (payload.get("thumbnailUrl") != null) {
                links.put("thumbnail", String.valueOf(payload.get("thumbnailUrl")));
            }
            if (payload.get("stacItemUrl") != null) {
                links.put("stacItem", String.valueOf(payload.get("stacItemUrl")));
            }
            sensor.put("links", links);
            out.add(sensor);
        }
        return out;
    }

    List<Map<String, Object>> rasterProducts(Geometry area, ObjectCaller caller) {
        ProductCatalogService service = products.getIfAvailable();
        if (service == null) {
            return List.of();
        }
        Envelope e = area.getEnvelopeInternal();
        Map<String, Object> page = service.listProducts(new ProductCatalogService.ProductQuery(null, null,
                new BboxFilter(List.of(e)), null, null, true), caller.excludedSources(), null, 50, "");
        List<Map<String, Object>> out = new ArrayList<>();
        if (page.get("items") instanceof List<?> items) {
            for (Object o : items) {
                if (!(o instanceof Map<?, ?> p) || p.get("id") == null) {
                    continue;
                }
                String id = String.valueOf(p.get("id"));
                String source = p.get("source") == null ? null : String.valueOf(p.get("source"));
                Map<String, Object> sensor = base("raster-product", id, p.get("title") == null ? id
                        : String.valueOf(p.get("title")), p.get("category") == null ? "Raster product"
                                : "Raster product (" + p.get("category") + ")",
                        source == null ? List.of() : List.of(source));
                Map<String, Object> links = new LinkedHashMap<>();
                links.put("self", "/v1/products/" + id);
                links.put("related", "/v1/objects/sensor:raster-product:" + id + "/related");
                links.put("frames", "/v1/products/" + id + "/frames");
                sensor.put("links", links);
                out.add(sensor);
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- envelopes

    /** A sensor envelope with the common fields. */
    Map<String, Object> base(String kind, String id, String title, String summary, List<String> sourceIds) {
        ObjectRef ref = ObjectRef.of(ObjectTypes.SENSOR, kind + ":" + id);
        ObjectEnvelope envelope = ObjectEnvelope.builder(ref).title(title).summary(summary)
                .provenance(sourceIds, null, null).self(selfOf(kind, id)).build();
        Map<String, Object> out = new LinkedHashMap<>(json.convertValue(envelope, MAP));
        out.put("sensorKind", kind);
        out.put("pDetect", null);
        out.put("method", null);
        out.put("families", List.of());
        out.put("rangeKm", null);
        out.put("passes", List.of());
        out.put("capabilities", null);
        out.put("footprint", null);
        out.put("acquiredAt", null);
        return out;
    }

    static String selfOf(String kind, String id) {
        return switch (kind) {
            case "source", "receiver", "station" -> "/v1/sources/" + id;
            case "satellite" -> "/compat/celestrak/gp.php?CATNR=" + id + "&FORMAT=json";
            case "raster-product" -> "/v1/products/" + id;
            default -> "/v1/sensors";
        };
    }

    private String sourceName(String id) {
        return registry.find(id).map(r -> r.descriptor().name()).orElse(id);
    }

    private List<Map<String, Object>> safe(String kind, java.util.function.Supplier<List<Map<String, Object>>> call) {
        try {
            return call.get();
        } catch (RuntimeException e) {
            log.warn("Sensor discovery of {} failed: {}", kind, e.toString());
            return List.of();
        }
    }

    // ------------------------------------------------------------------------- resolver

    /**
     * The envelope of {@code sensor:{kind}:{id}}: a source, receiver or station is its source; a satellite is a
     * curated or catalogued satellite; a scene is its record; a raster product is its catalog entry.
     */
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        String kind = ref.sensorKind();
        String id = ref.sensorId();
        return switch (kind) {
            case "source", "receiver", "station" -> context.caller().canRead(id) ? registry.find(id).map(r ->
                    ObjectEnvelope.builder(ref).title(r.descriptor().name()).summary(r.descriptor().description())
                            .provenance(List.of(id), null, null).self(selfOf(kind, id)).build()) : Optional.empty();
            case "satellite" -> {
                long norad;
                try {
                    norad = Long.parseLong(id);
                } catch (NumberFormatException e) {
                    yield Optional.empty();
                }
                SatelliteCapabilities.Capability c = capabilities.byNorad(norad);
                CelestrakElementsService service = elements.getIfAvailable();
                String name = c != null ? c.name() : service == null ? null
                        : service.byNorad(norad).map(o -> o.objectName()).orElse(null);
                yield name == null ? Optional.empty() : Optional.of(ObjectEnvelope.builder(ref).title(name)
                        .summary(c == null ? null : c.operator() + ": " + String.join(", ", c.sensorTypes()))
                        .self(selfOf(kind, id)).build());
            }
            case "scene" -> {
                int slash = id.indexOf('/');
                if (slash <= 0) {
                    yield Optional.empty();
                }
                String source = id.substring(0, slash);
                String external = ObjectRef.decode(id.substring(slash + 1));
                yield records.get(source, external, false, FieldSelection.ALL, GeometryOptions.DEFAULT,
                        context.caller().excludedSources()).map(row -> {
                            ObjectEnvelope.Builder b = ObjectEnvelope.builder(ref)
                                    .title(row.title() == null ? row.externalId() : row.title())
                                    .time(row.observedAt(), null, row.observedAt())
                                    .provenance(List.of(source), null, row.seq())
                                    .self("/v1/records/" + source + "/" + ObjectRef.encode(external));
                            try {
                                if (row.geometry() != null) {
                                    b.geometry(reader.read(row.geometry()));
                                }
                            } catch (RuntimeException e) {
                                // no footprint
                            }
                            return b.build();
                        });
            }
            case "raster-product" -> Optional.of(ObjectEnvelope.builder(ref).title(id).summary("Raster product")
                    .self(selfOf(kind, id)).build());
            default -> Optional.empty();
        };
    }
}
