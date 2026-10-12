package ai.foresignal.trigger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/** Converts Jackson trees from request bodies into plain JSON-like Java values. */
public final class JsonTrees {

    private JsonTrees() {}

    /**
     * {@code node} as Java: objects as {@code LinkedHashMap}, arrays as {@code List}, numbers as
     * {@code BigDecimal}, strings, booleans, and null for JSON null or a missing node.
     */
    public static Object toJava(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (String name : node.propertyNames()) {
                map.put(name, toJava(node.get(name)));
            }
            return map;
        }
        if (node.isArray()) {
            List<Object> list = new ArrayList<>(node.size());
            for (JsonNode item : node) {
                list.add(toJava(item));
            }
            return list;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isString()) {
            return node.asString();
        }
        return node.toString();
    }

    /** True when {@code body} has {@code field} with a value other than JSON null. */
    public static boolean present(JsonNode body, String field) {
        JsonNode node = body.get(field);
        return node != null && !node.isNull() && !node.isMissingNode();
    }
}
