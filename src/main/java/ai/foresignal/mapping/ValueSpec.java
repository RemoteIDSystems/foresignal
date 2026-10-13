package ai.foresignal.mapping;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.MissingNode;

/**
 * One mapping value (OpenAPI {@code MappingValueSpec}, prompt 059): a JSONPath, a {@code $pairs.<key>} lookup, a
 * substitution-only template or a constant. Resolving never throws: a value that is not there is a
 * {@link MissingNode}.
 */
public sealed interface ValueSpec {

    /**
     * The value in {@code scope}.
     *
     * @return the value, or a {@link MissingNode} when absent
     */
    JsonNode resolve(MappingScope scope);

    /** True when {@code value} is absent: missing, null or an empty indefinite result. */
    static boolean absent(JsonNode value) {
        return value == null || value.isMissingNode() || value.isNull();
    }

    /**
     * A Jayway JSONPath, relative to the current item.
     *
     * @param text the path as written
     * @param path the compiled path
     */
    record Path(String text, JsonPath path) implements ValueSpec {
        @Override
        public JsonNode resolve(MappingScope scope) {
            return scope.dialect().read(path, scope.item());
        }
    }

    /**
     * A flattened pair: {@code $pairs.<key>}.
     *
     * @param key the pair's name
     */
    record Pair(String key) implements ValueSpec {
        @Override
        public JsonNode resolve(MappingScope scope) {
            JsonNode value = scope.pairs().get(key);
            return value == null ? MissingNode.getInstance() : value;
        }
    }

    /**
     * Substitution only: literal text with {@code {{path}}} placeholders. Absent when any placeholder is absent.
     *
     * @param text the template as written
     * @param parts literal {@link String}s and {@link ValueSpec}s, in order
     */
    record Template(String text, List<Object> parts) implements ValueSpec {

        /** Copies the parts. */
        public Template {
            parts = List.copyOf(parts);
        }

        @Override
        public JsonNode resolve(MappingScope scope) {
            StringBuilder out = new StringBuilder();
            for (Object part : parts) {
                if (part instanceof String literal) {
                    out.append(literal);
                } else {
                    JsonNode value = ((ValueSpec) part).resolve(scope);
                    if (absent(value) || !value.isValueNode()) {
                        return MissingNode.getInstance();
                    }
                    out.append(value.asString());
                }
            }
            return JsonNodeFactory.instance.stringNode(out.toString());
        }
    }

    /**
     * A constant.
     *
     * @param value the constant
     */
    record Const(JsonNode value) implements ValueSpec {
        @Override
        public JsonNode resolve(MappingScope scope) {
            return value == null ? MissingNode.getInstance() : value;
        }
    }
}
