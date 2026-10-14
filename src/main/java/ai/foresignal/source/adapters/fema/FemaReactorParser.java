package ai.foresignal.source.adapters.fema;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The FEMA Nuclear Plant Power Reactor Status layer as GeoJSON (prompt 094).
 *
 * <p>{@code externalId} = {@code reactor:{license_number}}. A {@code powerPct} change is a version.
 */
public final class FemaReactorParser {

    /** The record kind. */
    public static final String KIND = "nuclear-reactor";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private FemaReactorParser() {}

    /** Parses a GeoJSON FeatureCollection. */
    public static List<NormalizedRecord> parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("FEMA reactor document could not be parsed",
                    new String(body, 0, Math.min(500, body.length), StandardCharsets.UTF_8), e);
        }
        JsonNode features = root == null ? null : root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("FEMA reactor document has no features array", "");
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

    /** One feature, or null without a license number. */
    public static NormalizedRecord map(JsonNode feature) {
        JsonNode p = feature.get("properties");
        String license = text(p, "license_number", "LICENSE_NUMBER", "licenseNumber");
        if (license == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder("reactor:" + license, KIND)
                .put("reactorName", text(p, "reactor_name", "REACTOR_NAME", "reactorName"))
                .put("powerPlant", text(p, "power_plant", "POWER_PLANT", "powerPlant"))
                .put("state", text(p, "state", "STATE"))
                .put("reactorType", text(p, "reactor_type", "REACTOR_TYPE", "reactorType"))
                .put("status", text(p, "status", "STATUS"))
                .put("powerPct", number(first(p, "power_pct", "POWER_PCT", "powerPct")))
                .put("reportDate", text(p, "report_date", "REPORT_DATE", "reportDate"))
                .put("mwe", number(first(p, "mwe", "MWE")))
                .put("ownerOperator", text(p, "owner_operator", "OWNER_OPERATOR", "ownerOperator"))
                .put("nrcRegion", text(p, "nrc_region", "NRC_REGION", "nrcRegion"))
                .put("eiaPlantCode", text(p, "eia_plant_code", "EIA_PLANT_CODE", "eiaPlantCode"));
        FeatureGeometry.Result geometry;
        try {
            geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
        } catch (UpstreamFormatException e) {
            geometry = FeatureGeometry.Result.NONE;
        }
        return b.geometry(geometry.geometry()).build();
    }

    private static JsonNode first(JsonNode node, String... names) {
        if (node == null) {
            return null;
        }
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull() && !value.isContainer()) {
                return value;
            }
        }
        return null;
    }

    private static String text(JsonNode node, String... names) {
        JsonNode value = first(node, names);
        if (value == null) {
            return null;
        }
        String s = value.asString();
        return s == null || s.isBlank() ? null : s.strip();
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
}
