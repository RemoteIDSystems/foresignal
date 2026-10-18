package ai.foresignal.inbound.edp.binding;

import ai.foresignal.inbound.edp.EdpManifestModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * One stream of a registered provider, as a binding needs it (prompt 138): the stream's manifest entry and the
 * provider-level provenance (originator and what it aggregates).
 *
 * @param sourceId the provider ({@code ext-…})
 * @param manifestSha256 the SHA-256 of the manifest in force
 * @param manifest the manifest
 * @param stream the stream
 */
public record BoundStream(String sourceId, String manifestSha256, EdpManifestModel manifest,
        EdpManifestModel.Stream stream) {

    /** The stream id. */
    public String streamId() {
        return stream.id();
    }

    /** The stream's record kind. */
    public String kind() {
        return stream.kind();
    }

    /** The item type the stream carries. */
    public String itemType() {
        return stream.itemType();
    }

    /** The declared document type, or null. */
    public String documentType() {
        return stream.documentType();
    }

    /** True when the manifest declares the stream a complete record of its scope ({@code complete: true}). */
    public boolean complete() {
        JsonNode complete = stream.raw() == null ? null : stream.raw().get("complete");
        return complete != null && complete.isBoolean() && complete.asBoolean();
    }

    /** The stream's {@code coverage} object, or null. */
    public JsonNode coverage() {
        JsonNode coverage = stream.raw() == null ? null : stream.raw().get("coverage");
        return coverage == null || !coverage.isObject() ? null : coverage;
    }

    /** A release lag the stream declares ({@code releaseLagDays}), or null. */
    public Double releaseLagDays() {
        JsonNode lag = stream.raw() == null ? null : stream.raw().get("releaseLagDays");
        return lag != null && lag.isNumber() ? lag.asDouble() : null;
    }

    /** The stream's {@code suggestedBindings}: each {@code {target, config}}. */
    public List<Map<String, Object>> suggestedBindings() {
        List<Map<String, Object>> out = new ArrayList<>();
        JsonNode suggested = stream.raw() == null ? null : stream.raw().get("suggestedBindings");
        if (suggested == null || !suggested.isArray()) {
            return out;
        }
        for (JsonNode s : suggested) {
            if (s.isObject() && s.get("target") != null && s.get("target").isString()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("target", s.get("target").asString());
                entry.put("config", s.get("config"));
                out.add(entry);
            }
        }
        return out;
    }
}
