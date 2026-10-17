package ai.foresignal.object;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** JSON merge patch (RFC 7386): objects merge recursively, {@code null} removes a member, anything else replaces. */
public final class JsonMergePatch {

    private JsonMergePatch() {}

    /**
     * Applies {@code patch} to {@code target}.
     *
     * @param target the document (not modified)
     * @param patch the merge patch
     * @return the patched document
     */
    public static JsonNode apply(JsonNode target, JsonNode patch) {
        if (patch == null || !patch.isObject()) {
            return patch == null ? target : patch.deepCopy();
        }
        ObjectNode result = target != null && target.isObject() ? (ObjectNode) target.deepCopy()
                : tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        for (String name : patch.propertyNames()) {
            JsonNode value = patch.get(name);
            if (value == null || value.isNull()) {
                result.remove(name);
            } else {
                result.set(name, apply(result.get(name), value));
            }
        }
        return result;
    }
}
