package ai.foresignal.intel;

import ai.foresignal.derive.DerivedRecords;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.source.SourceRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

/**
 * What is inside an affected area (prompt 101). Counts current reference facilities by kind, the
 * operator's places, live aircraft and vessels, and — when a population pack is installed — an
 * estimated residential population. Every section carries a {@code status} and its attribution;
 * sections the installation cannot answer report {@code not-installed}.
 */
@Service
public class ExposureService {

    /** One facility source and kind contributing an exposure kind. */
    private record Facility(String sourceId, String kind, String exposureKind) {}

    /** The facility sources, per exposure kind (which the OpenAPI {@code Exposure} schema names). */
    private static final List<Facility> FACILITIES = List.of(
            new Facility("wri-power-plants", "power-plant", "power-plant"),
            new Facility("ourairports", "airport", "airport"),
            new Facility("world-port-index", "port", "port"),
            new Facility("submarine-cables", "cable-landing", "cable-landing"),
            new Facility("nuclear-reactors-us", "nuclear-reactor", "nuclear-facility"),
            new Facility("nuclear-plants-wikidata", "nuclear-plant", "nuclear-facility"),
            new Facility("spaceports", "launch-site", "spaceport"),
            new Facility("spaceports", "launch-pad", "spaceport"));

    /** Every exposure facility kind; {@code data-centre} has no source until prompt 104. */
    private static final List<String> KINDS = List.of("power-plant", "airport", "port", "cable-landing",
            "data-centre", "nuclear-facility", "spaceport");

    /** The AIS sources for the live-vessel count. */
    private static final Set<String> VESSEL_SOURCES = Set.of("aisstream", "ais-kystverket", "ais-digitraffic");

    /** The most rows read per facility kind. */
    static final int LIMIT = 200_000;

    private final DerivedRecords records;
    private final PlaceService places;
    private final PopulationPackService population;
    private final SourceRegistry registry;

    /** Creates the service. */
    public ExposureService(DerivedRecords records, PlaceService places, PopulationPackService population,
            SourceRegistry registry) {
        this.records = records;
        this.places = places;
        this.population = population;
        this.registry = registry;
    }

    /** The exposure of an area. */
    public Map<String, Object> exposure(Geometry area, boolean includePopulation) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("facilities", facilities(area));
        out.put("places", places(area));
        out.put("entities", entities(area));
        if (includePopulation) {
            out.put("population", population(area));
        }
        return out;
    }

    private Map<String, Object> facilities(Geometry area) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String kind : KINDS) {
            counts.put(kind, 0);
        }
        boolean anyInstalled = false;
        boolean dataCenterInstalled = false;
        for (Facility facility : FACILITIES) {
            if (!installed(facility.sourceId())) {
                continue;
            }
            anyInstalled = true;
            int count = countIntersecting(Set.of(facility.sourceId()), Set.of(facility.kind()), area);
            counts.merge(facility.exposureKind(), count, Integer::sum);
        }
        if (anyInstalled) {
            dataCenterInstalled = installed("data-centre-osm");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", anyInstalled ? "ok" : "not-installed");
        out.put("counts", counts);
        if (!dataCenterInstalled) {
            out.put("notInstalled", List.of("data-centre"));
        }
        out.put("attribution", "facility sources; see the Source Catalog");
        return out;
    }

    private Map<String, Object> places(Geometry area) {
        List<Map<String, Object>> inside = new ArrayList<>();
        int count = 0;
        for (PlaceView place : places.all()) {
            if (place.area() != null && !place.area().isEmpty() && area.intersects(place.area())) {
                count++;
                inside.add(Map.of("id", place.id(), "name", place.name()));
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", "ok");
        out.put("count", count);
        out.put("places", inside);
        return out;
    }

    private Map<String, Object> entities(Geometry area) {
        int aircraft = countIntersecting(Set.of("derived-aircraft"), Set.of("aircraft"), area);
        int vessels = countIntersecting(VESSEL_SOURCES, Set.of("vessel"), area);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", aircraft + vessels > 0 ? "ok" : "unavailable");
        out.put("aircraft", aircraft);
        out.put("vessels", vessels);
        out.put("watchlist", Map.of("status", "not-installed",
                "reason", "watchlist entity counts arrive with prompt 130"));
        return out;
    }

    private Map<String, Object> population(Geometry area) {
        PopulationPackService.Estimate estimate = population.estimate(area);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", estimate.status());
        out.put("estimate", estimate.estimate());
        out.put("epoch", estimate.epoch());
        out.put("method", estimate.method());
        out.put("note", estimate.note());
        if ("ok".equals(estimate.status())) {
            out.put("attribution", PopulationPackService.ATTRIBUTION);
        }
        return out;
    }

    private int countIntersecting(Set<String> sources, Set<String> kinds, Geometry area) {
        Envelope envelope = area.getEnvelopeInternal();
        int count = 0;
        for (HeadSnapshot row : records.current(sources, kinds, envelope, LIMIT)) {
            if (row.geometry() != null && !row.geometry().isEmpty() && area.intersects(row.geometry())) {
                count++;
            }
        }
        return count;
    }

    private boolean installed(String sourceId) {
        return registry.find(sourceId).isPresent();
    }
}
