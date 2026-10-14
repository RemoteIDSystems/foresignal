package ai.foresignal.source.adapters.deepstate;

import ai.foresignal.source.adapters.conflict.ConflictSources;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.databind.JsonNode;

/**
 * Reads one DeepStateMap daily file of the {@code cyterat/deepstate-map-data} mirror (prompt 081): a
 * FeatureCollection (CRS84) with one feature, empty properties and a MultiPolygon of the occupied area.
 *
 * <p>The record is the single entity {@value #EXTERNAL_ID} of kind {@value #KIND}: the geometry as published
 * (simplified only above {@value FeatureGeometry#MAX_VERTICES} vertices, then with {@code geometrySimplified} and
 * {@code simplifyToleranceDeg}); payload {@code mapDate} ({@code yyyy-MM-dd}), {@code areaKm2} (the area on the WGS84
 * ellipsoid, 0.1 km²), {@code areaChangeKm2} (against the previous map, when known) and {@code ref} (the mirror ref
 * read); {@code observedAt = eventTime} = the map date at {@value #MAP_HOUR}:00 UTC, when the mirror publishes it.
 */
public final class DeepStateParser {

    /** The kind. */
    public static final String KIND = "frontline";

    /** The single entity. */
    public static final String EXTERNAL_ID = "occupied-area";

    /** The UTC hour at which a day's map is published. */
    public static final int MAP_HOUR = 3;

    /**
     * One read map.
     *
     * @param geometry the occupied area, SRID 4326
     * @param simplified true when it was simplified to fit the vertex limit
     * @param toleranceDeg the simplification tolerance, or null
     * @param areaKm2 the area, km², rounded to 0.1
     */
    public record MapFile(Geometry geometry, boolean simplified, Double toleranceDeg, double areaKm2) {}

    private DeepStateParser() {}

    /**
     * Reads a daily file.
     *
     * @throws UpstreamFormatException when it is not JSON, has no feature with a polygon, or the geometry is
     *     malformed
     */
    public static MapFile parse(byte[] body) {
        JsonNode root = ConflictSources.json(body, "DeepState map");
        JsonNode geometryNode = null;
        if ("FeatureCollection".equals(ConflictSources.text(root.get("type")))) {
            JsonNode features = root.get("features");
            if (features != null && features.isArray()) {
                for (JsonNode feature : features) {
                    JsonNode g = feature.get("geometry");
                    if (g != null && g.isObject()) {
                        geometryNode = g;
                        break;
                    }
                }
            }
        } else if ("Feature".equals(ConflictSources.text(root.get("type")))) {
            geometryNode = root.get("geometry");
        }
        FeatureGeometry.Result result = FeatureGeometry.fromGeoJson(geometryNode);
        Geometry geometry = result.geometry();
        if (geometry == null || !(geometry.getGeometryType().endsWith("Polygon"))) {
            throw new UpstreamFormatException("DeepState map has no polygon feature", "");
        }
        return new MapFile(geometry, result.simplified(), result.toleranceDeg(), round1(GeodesicArea.km2(geometry)));
    }

    /**
     * The record of a map.
     *
     * @param map the read map
     * @param mapDate the date of the file
     * @param ref the mirror ref read ({@code main} or a commit)
     * @param areaChangeKm2 the change against the previous map, or null when there is none
     */
    public static NormalizedRecord record(MapFile map, LocalDate mapDate, String ref, Double areaChangeKm2) {
        Instant at = observedAt(mapDate);
        NormalizedRecord.Builder b = NormalizedRecord.builder(EXTERNAL_ID, KIND)
                .observedAt(at)
                .eventTime(at)
                .geometry(map.geometry())
                .put("mapDate", mapDate.toString())
                .put("areaKm2", map.areaKm2())
                .put("areaChangeKm2", areaChangeKm2 == null ? null : round1(areaChangeKm2))
                .put("ref", ref);
        if (map.simplified()) {
            b.put(FeatureGeometry.SIMPLIFIED_KEY, true).put(FeatureGeometry.TOLERANCE_KEY, map.toleranceDeg());
        }
        return b.build();
    }

    /** The map date at {@value #MAP_HOUR}:00 UTC. */
    public static Instant observedAt(LocalDate mapDate) {
        return mapDate.atTime(LocalTime.of(MAP_HOUR, 0)).toInstant(ZoneOffset.UTC);
    }

    /** {@code value} rounded half-up to one decimal, without {@code -0.0}. */
    static double round1(double value) {
        double rounded = BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
        return rounded == 0.0 ? 0.0 : rounded;
    }
}
