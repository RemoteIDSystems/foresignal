package ai.foresignal.inbound.edp;

import tools.jackson.databind.JsonNode;

/**
 * One EDP item that passed validation.
 *
 * @param index its position in the batch (0-based)
 * @param item the item as sent
 * @param type its {@code type}
 * @param stream its stream; null only for a source-level {@code status} item
 */
public record EdpValidItem(int index, JsonNode item, String type, EdpManifestModel.Stream stream) {

    /** The item's {@code id}, or null (status items). */
    public String id() {
        JsonNode id = item.get("id");
        return id == null || id.isNull() ? null : id.asString();
    }

    /** {@code {stream}:{id}}, the record's external id; null for status items. */
    public String externalId() {
        String id = id();
        return stream == null || id == null ? null : stream.id() + ":" + id;
    }
}
