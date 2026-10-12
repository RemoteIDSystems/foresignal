package ai.foresignal.trigger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Loads {@code trigger/operator-truth-table.json}, shared by {@code RuleSqlBuilderIT} and {@code TriggerPersistGateTest}. */
final class TruthTable {

    static final String RESOURCE = "/trigger/operator-truth-table.json";

    static final JsonMapper JSON = JsonMapper.builder().build();

    private TruthTable() {}

    /**
     * One row.
     *
     * @param path the condition path
     * @param op the operator
     * @param value the raw condition value
     * @param payloadJson the payload as JSON text
     * @param payload the payload as a map (JSON nulls kept)
     * @param expected whether the condition holds
     * @param ambiguous whether Java reports AMBIGUOUS (SQL NULL)
     */
    record Row(String path, String op, Object value, String payloadJson, Map<String, Object> payload,
            boolean expected, boolean ambiguous) {

        /** The condition, compiled with the built-in registry. */
        RuleCondition condition() {
            Operator operator = OperatorRegistry.builtIn().find(op).orElseThrow();
            return new RuleCondition(path, operator, operator.normalize(JsonTrees.toJava(JSON.valueToTree(value))));
        }

        @Override
        public String toString() {
            return path + " " + op + " " + value + " on " + payloadJson;
        }
    }

    /** Every row. */
    static List<Row> rows() {
        try (InputStream in = TruthTable.class.getResourceAsStream(RESOURCE)) {
            JsonNode root = JSON.readTree(in);
            List<Row> rows = new ArrayList<>();
            for (JsonNode node : root.get("rows")) {
                Map<String, Object> payload = JSON.readValue(node.get("payload").toString(), new TypeReference<>() {});
                rows.add(new Row(
                        node.get("path").asString(),
                        node.get("op").asString(),
                        JSON.treeToValue(node.get("value"), Object.class),
                        node.get("payload").toString(),
                        payload,
                        node.get("expected").asBoolean(),
                        node.get("ambiguous").asBoolean()));
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * One geofence-preset row (prompt 040).
     *
     * @param ref the preset
     * @param lon the point's longitude
     * @param lat the point's latitude
     * @param expected whether the point intersects the preset's union
     */
    record GeofenceRefRow(GeofenceRef ref, double lon, double lat, boolean expected) {

        @Override
        public String toString() {
            return ref.canonical() + " at " + lon + "," + lat;
        }
    }

    /** Every geofence-preset row. */
    static List<GeofenceRefRow> geofenceRefRows() {
        JsonNode root = root();
        List<GeofenceRefRow> rows = new ArrayList<>();
        for (JsonNode node : root.get("geofenceRefRows")) {
            JsonNode ref = node.get("geofenceRef");
            rows.add(new GeofenceRefRow(new GeofenceRef(strings(ref.get("country")), strings(ref.get("admin1")),
                    strings(ref.get("county"))), node.get("point").get(0).asDouble(), node.get("point").get(1).asDouble(),
                    node.get("expected").asBoolean()));
        }
        return rows;
    }

    /** The preset regions' boxes {@code [minLon, minLat, maxLon, maxLat]}, by member then code. */
    static Map<String, Map<String, double[]>> geofenceRefRegions() {
        JsonNode regions = root().get("geofenceRefRegions");
        Map<String, Map<String, double[]>> out = new java.util.LinkedHashMap<>();
        for (String member : regions.propertyNames()) {
            Map<String, double[]> boxes = new java.util.LinkedHashMap<>();
            for (String code : regions.get(member).propertyNames()) {
                JsonNode box = regions.get(member).get(code);
                boxes.put(code, new double[] {box.get(0).asDouble(), box.get(1).asDouble(), box.get(2).asDouble(),
                    box.get(3).asDouble()});
            }
            out.put(member, boxes);
        }
        return out;
    }

    private static JsonNode root() {
        try (InputStream in = TruthTable.class.getResourceAsStream(RESOURCE)) {
            return JSON.readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> strings(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null) {
            for (JsonNode item : node) {
                out.add(item.asString());
            }
        }
        return out;
    }
}
