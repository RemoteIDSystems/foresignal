package ai.foresignal.mapping;

import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * What a value of one item is read from (prompt 059).
 *
 * @param item the current item: the document, or one element of {@code select}
 * @param pairs the flattened name/value pairs of the item ({@code $pairs.<key>}); empty without {@code pairs}
 * @param dialect the JSONPath dialect
 */
public record MappingScope(JsonNode item, Map<String, JsonNode> pairs, MappingJsonPath dialect) {

    /** Copies the pairs. */
    public MappingScope {
        pairs = pairs == null ? Map.of() : Map.copyOf(pairs);
    }
}
