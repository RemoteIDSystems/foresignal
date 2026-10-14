package ai.foresignal.source.adapters.submarinecables;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The public Submarine Cable Map GeoJSON (prompt 094): {@code cable-geo.json} and
 * {@code landing-point-geo.json}.
 *
 * <p>Stylised geometry. {@code externalId} is {@code cable:{id}} for a cable and {@code landing:{id}} for a
 * landing point. A feature without an id is skipped.
 */
public final class SubmarineCableParser {

    /** The cable kind. */
    public static final String CABLE_KIND = "cable";

    /** The landing-point kind. */
    public static final String LANDING_KIND = "cable-landing";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private SubmarineCableParser() {}

    /** Parses {@code cable-geo.json}. */
    public static List<NormalizedRecord> parseCables(byte[] body) {
        return parse(body, CABLE_KIND, "cable:");
    }

    /** Parses {@code landing-point-geo.json}. */
    public static List<NormalizedRecord> parseLandings(byte[] body) {
        return parse(body, LANDING_KIND, "landing:");
    }

    private static List<NormalizedRecord> parse(byte[] body, String kind, String idPrefix) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("Submarine Cable Map document could not be parsed",
                    new String(body, 0, Math.min(500, body.length), StandardCharsets.UTF_8), e);
        }
        JsonNode features = root == null ? null : root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("Submarine Cable Map document has no features array", "");
        }
        List<NormalizedRecord> out = new ArrayList<>(features.size());
        for (JsonNode feature : features) {
            JsonNode properties = feature.get("properties");
            String id = text(properties, "id");
            if (id == null) {
                continue;
            }
            NormalizedRecord.Builder b = NormalizedRecord.builder(idPrefix + id, kind)
                    .put("name", text(properties, "name"))
                    .put("color", text(properties, "color"))
                    .put("isTbd", tbd(properties));
            FeatureGeometry.Result geometry;
            try {
                geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
            } catch (UpstreamFormatException e) {
                geometry = FeatureGeometry.Result.NONE;
            }
            out.add(b.geometry(geometry.geometry()).build());
        }
        return out;
    }

    private static Boolean tbd(JsonNode properties) {
        if (properties == null) {
            return null;
        }
        JsonNode node = properties.get("is_tbd");
        if (node == null || node.isNull()) {
            node = properties.get("isTbd");
        }
        return node != null && node.isBoolean() ? node.booleanValue() : null;
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
