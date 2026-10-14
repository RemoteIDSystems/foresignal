package ai.foresignal.source.adapters.ercot;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The ERCOT Public API report envelope as field-named rows (prompt 093). Pure.
 *
 * <p>Every report answers {@code {_meta, report, fields, data}}; the columns differ per report, so the names are
 * read from {@code fields[].name} and the rows are lists aligned with them (objects are accepted too). Never map by
 * position.
 */
public final class ErcotParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private ErcotParser() {}

    /**
     * One report page.
     *
     * @param fields the column names, in order
     * @param rows one map per row, keyed by column name
     */
    public record Envelope(List<String> fields, List<Map<String, Object>> rows) {}

    /** Parses a report page. */
    public static Envelope parse(byte[] body) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("ERCOT answer is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("ERCOT answer is empty or not an object", "");
        }
        JsonNode fieldsNode = root.get("fields");
        if (fieldsNode == null || !fieldsNode.isArray()) {
            throw new UpstreamFormatException("ERCOT answer has no fields array", "");
        }
        List<String> fields = new ArrayList<>();
        for (JsonNode field : fieldsNode) {
            String name = field.isObject() && field.has("name") ? field.get("name").asString() : null;
            fields.add(name);
        }
        JsonNode data = root.get("data");
        if (data == null || !data.isArray()) {
            throw new UpstreamFormatException("ERCOT answer has no data array", "");
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (JsonNode row : data) {
            Map<String, Object> mapped = new LinkedHashMap<>();
            if (row.isArray()) {
                for (int i = 0; i < fields.size() && i < row.size(); i++) {
                    if (fields.get(i) != null) {
                        mapped.put(fields.get(i), value(row.get(i)));
                    }
                }
            } else if (row.isObject()) {
                row.properties().forEach(entry -> mapped.put(entry.getKey(), value(entry.getValue())));
            }
            rows.add(mapped);
        }
        return new Envelope(fields, rows);
    }

    private static Object value(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.doubleValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return node.asString();
    }
}
