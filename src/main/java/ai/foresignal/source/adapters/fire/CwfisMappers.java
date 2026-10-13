package ai.foresignal.source.adapters.fire;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.JsonNode;

/**
 * The CWFIS GeoServer features as records (prompt 075). Pure. The WFS kit has already put every coordinate
 * longitude first ({@code axisOrder=lonlat}; verified 2026-10-13: the GeoJSON is lon,lat although its {@code crs}
 * reads {@code urn:ogc:def:crs:EPSG::4326}).
 *
 * <ul>
 *   <li>{@link #perimeter} ({@code public:m3_polygons_current}): hotspot-derived burned-area polygons. The GeoServer
 *       feature ids ({@code m3_polygons_current.226354}) are not stable: on the capture day they were unrelated to
 *       {@code firstdate} and the highest ids carried the newest {@code lastdate}, so a row gets a new id when its
 *       polygon is rewritten. The id is therefore {@code firstdate@lat,lon} of the polygon's centroid rounded to two
 *       decimals.
 *   <li>{@link #station} ({@code public:firewx_stns_current}): one point per weather station, id {@code aes}
 *       (else {@code wmo}); the space-padded {@code name} and {@code agency} are trimmed. {@link #stationOrder}
 *       puts surface reports before upper-air ones, so when an id repeats (upper-air and surface entries share an
 *       {@code aes}) the surface station wins.
 *   <li>{@link #hotspot} ({@code public:hotspots_last24hrs}): one point per detection, id
 *       {@code rep_date:lat:lon:satellite} (five decimals), kept for its Canadian FBP System fire-behavior
 *       prediction fields.
 * </ul>
 */
public final class CwfisMappers {

    /** The {@code perimeterSource} of every m3 polygon. */
    public static final String M3 = "m3";

    /** The {@code ua} flag of an upper-air station entry. */
    static final String UPPER_AIR = "@";

    private CwfisMappers() {}

    /**
     * An m3 burned-area polygon as a {@code fire-perimeter} record: {@code observedAt = lastdate},
     * {@code eventTime = firstdate}; payload {@code hotspotCount, firstDetected, lastDetected, areaHa,
     * perimeterSource}, plus the simplification flags when the polygon had more than 9,500 vertices.
     *
     * @param feature a GeoJSON Feature, longitude first
     */
    public static MappedRecord perimeter(JsonNode feature) {
        JsonNode p = feature == null ? null : feature.get("properties");
        Instant first = JsonProps.iso(p, "firstdate");
        Instant last = JsonProps.iso(p, "lastdate");
        if (last == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        FeatureGeometry.Result geometry;
        try {
            geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
        } catch (UpstreamFormatException e) {
            return MappedRecord.skipped(MappedRecord.BAD_GEOMETRY);
        }
        if (first == null || geometry.geometry() == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Point centroid = geometry.geometry().getCentroid();
        String id = first + "@" + fixed(centroid.getY(), 2) + "," + fixed(centroid.getX(), 2);
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, NifcPerimeterMapper.KIND)
                .observedAt(last)
                .eventTime(first)
                .geometry(geometry.geometry())
                .put("hotspotCount", JsonProps.integer(p, "hcount"))
                .put("firstDetected", first.toString())
                .put("lastDetected", last.toString())
                .put("areaHa", JsonProps.number(p, "area"))
                .put("perimeterSource", M3);
        if (geometry.simplified()) {
            b.put(FeatureGeometry.SIMPLIFIED_KEY, true).put(FeatureGeometry.TOLERANCE_KEY, geometry.toleranceDeg());
        }
        return MappedRecord.of(b.build());
    }

    /**
     * A fire-weather station report as a {@code fire-weather} record: {@code observedAt = eventTime = rep_date};
     * payload {@code name, province, agency, elevationM, tempC, rhPct, windKph, gustKph, windDirDeg, precipMm,
     * ffmc, dmc, dc, isi, bui, fwi, dsr}.
     *
     * @param feature a GeoJSON Feature, longitude first
     */
    public static MappedRecord station(JsonNode feature) {
        JsonNode p = feature == null ? null : feature.get("properties");
        String id = JsonProps.text(p, "aes");
        if (id == null) {
            Long wmo = JsonProps.integer(p, "wmo");
            id = wmo == null ? null : "wmo:" + wmo;
        }
        if (id == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant reported = JsonProps.iso(p, "rep_date");
        if (reported == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, CwfisLayer.FIRE_WEATHER)
                .observedAt(reported)
                .eventTime(reported)
                .geometry(point(feature))
                .put("name", JsonProps.text(p, "name"))
                .put("province", JsonProps.text(p, "prov"))
                .put("agency", JsonProps.text(p, "agency"))
                .put("elevationM", JsonProps.number(p, "elev"))
                .put("tempC", JsonProps.number(p, "temp"))
                .put("rhPct", JsonProps.number(p, "rh"))
                .put("windKph", JsonProps.number(p, "ws"))
                .put("gustKph", JsonProps.number(p, "wg"))
                .put("windDirDeg", JsonProps.number(p, "wdir"))
                .put("precipMm", JsonProps.number(p, "precip"));
        for (String index : List.of("ffmc", "dmc", "dc", "isi", "bui", "fwi", "dsr")) {
            b.put(index, JsonProps.number(p, index));
        }
        return MappedRecord.of(b.build());
    }

    /**
     * The station features with surface reports first, otherwise in document order, so the surface station wins a
     * repeated id.
     *
     * @param features the page features
     */
    public static List<JsonNode> stationOrder(Iterable<JsonNode> features) {
        List<JsonNode> surface = new ArrayList<>();
        List<JsonNode> upperAir = new ArrayList<>();
        for (JsonNode f : features) {
            (UPPER_AIR.equals(JsonProps.text(f.get("properties"), "ua")) ? upperAir : surface).add(f);
        }
        surface.addAll(upperAir);
        return surface;
    }

    /**
     * A hotspot as a {@code fire-detection} record: {@code observedAt = eventTime = rep_date}; payload {@code hfi,
     * ros, fuel, cfb, sfc, tfc, frp, sensor, satellite, agency}.
     *
     * @param feature a GeoJSON Feature, longitude first
     */
    public static MappedRecord hotspot(JsonNode feature) {
        JsonNode p = feature == null ? null : feature.get("properties");
        Instant reported = JsonProps.iso(p, "rep_date");
        if (reported == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        Double lat = JsonProps.number(p, "lat");
        Double lon = JsonProps.number(p, "lon");
        Point point = point(feature);
        if (lat == null || lon == null) {
            lat = point == null ? null : point.getY();
            lon = point == null ? null : point.getX();
        }
        String satellite = JsonProps.text(p, "satellite");
        if (lat == null || lon == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        String id = reported + ":" + fixed(lat, 5) + ":" + fixed(lon, 5) + ":" + (satellite == null ? "" : satellite);
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, CwfisLayer.FIRE_DETECTION)
                .observedAt(reported)
                .eventTime(reported)
                .geometry(point != null ? point : Geo.point(lon, lat));
        for (String key : List.of("hfi", "ros", "cfb", "sfc", "tfc", "frp")) {
            b.put(key, JsonProps.number(p, key));
        }
        b.put("fuel", JsonProps.text(p, "fuel"))
                .put("sensor", JsonProps.text(p, "sensor"))
                .put("satellite", satellite)
                .put("agency", JsonProps.text(p, "agency"));
        return MappedRecord.of(b.build());
    }

    /** The feature's point geometry, or null when it has none or it is not a valid point. */
    static Point point(JsonNode feature) {
        try {
            FeatureGeometry.Result g = FeatureGeometry.fromGeoJson(feature.get("geometry"));
            return g.geometry() instanceof Point point ? point : null;
        } catch (UpstreamFormatException e) {
            return null;
        }
    }

    /** {@code value} with exactly {@code decimals} decimals, a dot as separator. */
    static String fixed(double value, int decimals) {
        String s = String.format(Locale.ROOT, "%." + decimals + "f", value);
        return s.startsWith("-") && s.matches("-0\\.0+") ? s.substring(1) : s;
    }
}
