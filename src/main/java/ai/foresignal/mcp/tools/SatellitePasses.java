package ai.foresignal.mcp.tools;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.RecordRow;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared lookups of the satellite pass tools (prompt 049): which {@code satdump-*} sources a caller may
 * read, how a pass id names its source, and a pass's status and thumbnail from its payload.
 */
@Component
public class SatellitePasses {

    /** The pass record kind. */
    static final String KIND = "satellite-pass";

    /** The id prefix of a SatDump station source. */
    static final String PREFIX = "satdump-";

    private final SourceRegistry registry;
    private final JsonMapper json;

    /** Creates the helper. */
    public SatellitePasses(SourceRegistry registry, JsonMapper json) {
        this.registry = registry;
        this.json = json;
    }

    /**
     * The station sources to read: one station's, or every visible one.
     *
     * @throws NotFoundException when the named station is unknown or hidden from the caller
     */
    List<String> sources(String station, Set<String> excluded) {
        if (station != null && !station.isBlank()) {
            String id = PREFIX + station.trim();
            if (!registry.contains(id) || excluded.contains(id)) {
                throw NotFoundException.of("Source", id);
            }
            return List.of(id);
        }
        List<String> out = new ArrayList<>();
        for (String id : registry.ids()) {
            if (id.startsWith(PREFIX) && !excluded.contains(id)) {
                out.add(id);
            }
        }
        out.sort(null);
        return out;
    }

    /**
     * The source and external id of a pass id: {@code {station}:{satKey}:{minute}} (the record's
     * {@code externalId}) or {@code satdump-{station}:{externalId}} (an item id).
     *
     * @return {@code {sourceId, externalId}}
     * @throws NotFoundException when the id has neither shape
     */
    static String[] split(String id) {
        String trimmed = id == null ? "" : id.trim();
        if (trimmed.startsWith(PREFIX)) {
            int colon = trimmed.indexOf(':');
            if (colon > PREFIX.length() && colon < trimmed.length() - 1) {
                return new String[] {trimmed.substring(0, colon), trimmed.substring(colon + 1)};
            }
        }
        int colon = trimmed.indexOf(':');
        if (colon <= 0) {
            throw new NotFoundException("No satellite pass '" + id + "'. Pass ids look like home:meteor-m2-3:29467234;"
                    + " list them with list_satellite_passes.");
        }
        return new String[] {PREFIX + trimmed.substring(0, colon), trimmed};
    }

    /** The payload of a row, or an empty object. */
    JsonNode payload(RecordRow row) {
        if (row.payload() == null) {
            return json.createObjectNode();
        }
        try {
            return json.readTree(row.payload());
        } catch (JacksonException e) {
            return json.createObjectNode();
        }
    }

    /** The pass's status, or null. */
    String status(RecordRow row) {
        JsonNode status = payload(row).get("status");
        return status == null || !status.isString() ? null : status.asString();
    }

    /** The first 256 px thumbnail media id of the pass's products, or null. */
    String thumbnail(RecordRow row) {
        for (JsonNode product : payload(row).path("products")) {
            JsonNode id = product.get("thumbMediaId");
            if (id != null && id.isString() && !id.asString().isBlank()) {
                return id.asString();
            }
        }
        return null;
    }
}
