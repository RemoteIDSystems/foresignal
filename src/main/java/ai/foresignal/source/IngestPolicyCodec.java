package ai.foresignal.source;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.spi.IngestPolicy;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts an {@link IngestPolicy} to and from its one JSON shape: the shape stored in
 * {@code source_settings.ingest_policy} and the {@code IngestPolicy} schema of the API
 * (Core-Data-Model §5.1).
 *
 * <p>A thin wrapper, but it keeps the shape in one place and turns a malformed policy into a clear
 * message rather than a Jackson stack trace.
 *
 * <p><strong>{@code keyframe: "none"}.</strong> A policy with no keyframe (allowed only for a
 * source without a TTL) is written as the string {@code "none"}, as §5.1 and the OpenAPI schema
 * spell it. Reading accepts {@code "none"}, an explicit JSON {@code null} and an absent key, so rows
 * written before prompt 013 (which stored {@code null}) still read back unchanged.
 */
@Component
public class IngestPolicyCodec {

    /** The keyframe value meaning "no keyframe". */
    public static final String KEYFRAME_NONE = "none";

    static final String KEYFRAME = "keyframe";

    private final JsonMapper jsonMapper;

    /** Creates the codec on Boot's mapper, which writes durations in shorthand. */
    public IngestPolicyCodec(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /** The policy as JSON, or null for a null policy. */
    public String toJson(IngestPolicy policy) {
        if (policy == null) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsString(toTree(policy));
        } catch (JacksonException e) {
            throw new InvalidInputException(
                    "Could not serialize the ingest policy: " + e.getOriginalMessage(), e);
        }
    }

    /** The policy parsed from {@code json}, or null when there is none. */
    public IngestPolicy fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        JsonNode tree;
        try {
            tree = jsonMapper.readTree(json);
        } catch (JacksonException e) {
            throw unreadable(e);
        }
        return fromTree(tree);
    }

    /**
     * The policy as a JSON tree, with a missing keyframe written as {@code "none"}; null for a null
     * policy.
     */
    public ObjectNode toTree(IngestPolicy policy) {
        if (policy == null) {
            return null;
        }
        try {
            ObjectNode tree = jsonMapper.valueToTree(policy);
            if (policy.keyframe() == null) {
                tree.put(KEYFRAME, KEYFRAME_NONE);
            }
            return tree;
        } catch (JacksonException e) {
            throw new InvalidInputException(
                    "Could not serialize the ingest policy: " + e.getOriginalMessage(), e);
        }
    }

    /** The policy read from a JSON tree, or null for a missing or JSON-null tree. */
    public IngestPolicy fromTree(JsonNode tree) {
        if (tree == null || tree.isNull() || tree.isMissingNode()) {
            return null;
        }
        if (!tree.isObject()) {
            throw new InvalidInputException(
                    "Could not read the ingest policy. Expected {\"mode\":\"ALL|SAMPLED|LATEST\", ...}.");
        }
        ObjectNode copy = ((ObjectNode) tree).deepCopy();
        JsonNode keyframe = copy.get(KEYFRAME);
        if (keyframe != null && keyframe.isString() && KEYFRAME_NONE.equalsIgnoreCase(keyframe.stringValue().trim())) {
            copy.remove(KEYFRAME);
        }
        try {
            return jsonMapper.treeToValue(copy, IngestPolicy.class);
        } catch (JacksonException e) {
            throw unreadable(e);
        }
    }

    private static InvalidInputException unreadable(JacksonException e) {
        return new InvalidInputException(
                "Could not read the ingest policy. Expected"
                        + " {\"mode\":\"ALL|SAMPLED|LATEST\", ...}, but got: "
                        + e.getOriginalMessage(),
                e);
    }
}
