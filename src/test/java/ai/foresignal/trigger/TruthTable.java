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
}
