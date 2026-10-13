package ai.foresignal.source.adapters.fire;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.support.MappedRecord;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import tools.jackson.databind.JsonNode;

/**
 * One CWFIS GeoServer feature type ingested as a source (prompt 075).
 *
 * @param id the source id
 * @param name the human-readable name
 * @param description what the source holds
 * @param typeName the WFS feature type
 * @param kind the record kind
 * @param interval the default poll interval
 * @param minInterval the floor on the poll interval
 * @param budget the default request budget
 * @param maxPages the page cap (1000 features per page)
 * @param recordTtl the record TTL, or null for none
 * @param tombstones true when features missing from two consecutive complete fetches are tombstoned
 * @param retention the raw retention of the kind
 * @param filters extra GetFeature parameters ({@code sortBy} where GeoServer needs a sort order to page)
 * @param mapper the pure per-feature mapper
 * @param reorder orders the features before mapping (which entry wins a repeated id)
 */
public record CwfisLayer(String id, String name, String description, String typeName, String kind,
        Duration interval, Duration minInterval, Budget budget, int maxPages, Duration recordTtl, boolean tombstones,
        Duration retention, Map<String, String> filters, Function<JsonNode, MappedRecord> mapper,
        Function<List<JsonNode>, List<JsonNode>> reorder) {

    /** The WFS endpoint. */
    public static final String ENDPOINT = "https://cwfis.cfs.nrcan.gc.ca/geoserver/public/wfs";

    /** The kind of a fire-weather station report. */
    public static final String FIRE_WEATHER = "fire-weather";

    /** The kind of a hotspot. */
    public static final String FIRE_DETECTION = "fire-detection";

    /** Features per page. */
    public static final int PAGE_SIZE = 1000;

    /** The license. */
    public static final String LICENSE = "Open Government Licence – Canada";

    /** The required attribution. */
    public static final String ATTRIBUTION = "Natural Resources Canada, Canadian Wildland Fire Information System";

    /**
     * {@code [minLon, minLat, maxLon, maxLat]} of North America, for the WFS kit's axis-order sanity check (CWFIS
     * hotspots include US detections).
     */
    public static final List<Double> NORTH_AMERICA = List.of(-180.0, 14.0, -50.0, 84.0);

    /** The three reviewed layers. {@code public:fdr_current_shp} (fire-danger class polygons) is not ingested. */
    public static final List<CwfisLayer> LAYERS = List.of(
            new CwfisLayer("cwfis-perimeters", "CWFIS burned-area polygons (Canada)",
                    "Hotspot-derived burned-area polygons of the current season from the Canadian Wildland Fire"
                            + " Information System (M3 algorithm): these are NOT agency-mapped fire perimeters but"
                            + " polygons drawn around clustered satellite hotspots. Hotspot count, first and last"
                            + " detection and area in hectares. A polygon missing from two consecutive complete"
                            + " fetches is tombstoned.",
                    "public:m3_polygons_current", NifcPerimeterMapper.KIND, Duration.ofHours(3), Duration.ofHours(1),
                    Budget.of(10, 200), 5, null, true, Duration.ofDays(1825), Map.of(), CwfisMappers::perimeter,
                    list -> list),
            new CwfisLayer("cwfis-fwi-stations", "CWFIS fire-weather stations (Canada)",
                    "Daily fire-weather observations and Canadian Forest Fire Weather Index System codes (FFMC, DMC,"
                            + " DC, ISI, BUI, FWI, DSR) for about a thousand stations, from the Canadian Wildland"
                            + " Fire Information System. Reports expire 36 hours after their report time.",
                    "public:firewx_stns_current", FIRE_WEATHER, Duration.ofHours(3), Duration.ofHours(1),
                    Budget.of(10, 200), 5, Duration.ofHours(36), false, Duration.ofDays(365),
                    Map.of("sortBy", "aes"), CwfisMappers::station, CwfisMappers::stationOrder),
            new CwfisLayer("cwfis-hotspots", "CWFIS hotspots with fire-behavior predictions",
                    "Satellite hotspots of the last 24 hours from the Canadian Wildland Fire Information System,"
                            + " kept for their Canadian FBP System fire-behavior predictions (head fire intensity, rate of spread,"
                            + " fuel type, crown fraction burned, fuel consumption, FRP). The detections duplicate"
                            + " FIRMS: nasa-firms (prompt 065) is the primary hotspot source. Detections expire 24"
                            + " hours after their report time.",
                    "public:hotspots_last24hrs", FIRE_DETECTION, Duration.ofHours(1), Duration.ofMinutes(30),
                    Budget.of(30, 1000), 50, Duration.ofHours(24), false, Duration.ofDays(7), Map.of(),
                    CwfisMappers::hotspot, list -> list));

    /** Copies the filters. */
    public CwfisLayer {
        filters = filters == null ? Map.of() : Map.copyOf(filters);
    }

    /**
     * The reviewed layer of {@code id}.
     *
     * @throws IllegalArgumentException when there is none
     */
    public static CwfisLayer reviewed(String id) {
        return LAYERS.stream().filter(l -> l.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No reviewed CWFIS layer " + id));
    }
}
