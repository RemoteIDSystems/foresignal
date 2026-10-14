package ai.foresignal.source.adapters.gvp;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Smithsonian Global Volcanism Program WFS GeoJSON (prompt 094): Holocene volcanoes.
 *
 * <p>{@code externalId} = {@code Volcano_Number}; a point from the feature geometry. {@code lastEruptionYear}
 * is negative for BCE and omitted when the upstream has none; the geologic summary is trimmed to 2 KB.
 */
public final class GvpWfsParser {

    /** The record kind. */
    public static final String KIND = "volcano";

    /** The longest summary kept, characters. */
    public static final int MAX_SUMMARY = 2_048;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private GvpWfsParser() {}

    /** Parses the WFS GeoJSON FeatureCollection. */
    public static List<NormalizedRecord> parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("GVP WFS document could not be parsed",
                    new String(body, 0, Math.min(500, body.length), StandardCharsets.UTF_8), e);
        }
        JsonNode features = root == null ? null : root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("GVP WFS document has no features array", "");
        }
        List<NormalizedRecord> out = new ArrayList<>(features.size());
        for (JsonNode feature : features) {
            JsonNode p = feature.get("properties");
            String number = text(p, "Volcano_Number");
            if (number == null) {
                continue;
            }
            String summary = text(p, "Geologic_Summary");
            if (summary != null && summary.length() > MAX_SUMMARY) {
                summary = summary.substring(0, MAX_SUMMARY);
            }
            NormalizedRecord.Builder b = NormalizedRecord.builder(number, KIND)
                    .put("name", text(p, "Volcano_Name"))
                    .put("landform", text(p, "Volcano_Landform"))
                    .put("primaryType", primaryType(p))
                    .put("lastEruptionYear", year(p.get("Last_Eruption_Year")))
                    .put("country", text(p, "Country"))
                    .put("region", text(p, "Region"))
                    .put("subregion", text(p, "Subregion"))
                    .put("elevationM", number(p.get("Elevation")))
                    .put("tectonicSetting", text(p, "Tectonic_Setting"))
                    .put("evidenceCategory", text(p, "Evidence_Category"))
                    .put("rockType", text(p, "Major_Rock_Type"))
                    .put("summary", summary);
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

    private static String primaryType(JsonNode p) {
        String type = text(p, "Primary_Volcano_Type");
        return type != null ? type : text(p, "Volcano_Type");
    }

    private static Long year(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        if (node.isIntegralNumber()) {
            return node.longValue();
        }
        Double value = number(node);
        return value == null || value != Math.rint(value) ? null : value.longValue();
    }

    private static Double number(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        if (node.isNumber()) {
            double value = node.doubleValue();
            return Double.isFinite(value) ? value : null;
        }
        try {
            double value = Double.parseDouble(node.asString().strip());
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
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
