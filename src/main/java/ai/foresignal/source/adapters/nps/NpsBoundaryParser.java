package ai.foresignal.source.adapters.nps;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The NPS Land Resources Division boundary layer as GeoJSON (prompt 094).
 *
 * <p>{@code externalId} = {@code UNIT_CODE}. Geometry is a (Multi)Polygon simplified to the store's vertex
 * limit with JTS {@code TopologyPreservingSimplifier}; the record then carries
 * {@code quality.extra["geom.simplified"]}.
 */
public final class NpsBoundaryParser {

    /** The record kind. */
    public static final String KIND = "park-boundary";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private NpsBoundaryParser() {}

    /** Parses a GeoJSON FeatureCollection body. */
    public static List<NormalizedRecord> parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("NPS boundary document could not be parsed",
                    new String(body, 0, Math.min(500, body.length), StandardCharsets.UTF_8), e);
        }
        JsonNode features = root == null ? null : root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("NPS boundary document has no features array", "");
        }
        List<NormalizedRecord> out = new ArrayList<>(features.size());
        for (JsonNode feature : features) {
            NormalizedRecord record = map(feature);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    /** One feature, or null without a {@code UNIT_CODE}. */
    public static NormalizedRecord map(JsonNode feature) {
        JsonNode properties = feature.get("properties");
        String unitCode = text(properties, "UNIT_CODE");
        if (unitCode == null) {
            return null;
        }
        FeatureGeometry.Result geometry;
        try {
            geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
        } catch (UpstreamFormatException e) {
            geometry = FeatureGeometry.Result.NONE;
        }
        Quality quality = geometry.simplified()
                ? Quality.none().withExtra("geom.simplified", true)
                : Quality.none();
        return NormalizedRecord.builder(unitCode, KIND)
                .geometry(geometry.geometry())
                .quality(quality)
                .put("unitName", text(properties, "UNIT_NAME"))
                .put("unitType", text(properties, "UNIT_TYPE"))
                .put("parkName", text(properties, "PARKNAME"))
                .put("state", text(properties, "STATE"))
                .put("region", text(properties, "REGION"))
                .put("dateEdit", text(properties, "DATE_EDIT"))
                .put("status", text(properties, "STATUS"))
                .build();
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isContainer()) {
            return null;
        }
        String s = value.asString();
        return s == null || s.isBlank() ? null : s.strip();
    }
}
