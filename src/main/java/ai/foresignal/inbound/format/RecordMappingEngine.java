package ai.foresignal.inbound.format;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Instant;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Applies a {@link RecordMapping} to a parsed JSON body (prompt 024). Pure: no I/O, no state.
 */
@Component
public class RecordMappingEngine {

    private final GeoJsonIngestMapper mapper;

    /** Creates the engine. */
    public RecordMappingEngine(GeoJsonIngestMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Maps {@code body}.
     *
     * @throws InvalidInputException when {@code recordsPath} does not point at an array or object
     */
    public ParsedIngest apply(RecordMapping mapping, JsonNode body, SourceDescriptor descriptor) {
        JsonNode records = mapping.recordsPath().isEmpty() ? body : body.at(mapping.recordsPath());
        ParsedIngest.Builder out = ParsedIngest.builder();
        if (records.isArray()) {
            int index = 0;
            for (JsonNode record : records) {
                map(index++, record, mapping, descriptor, out);
            }
            out.items(index);
        } else if (records.isObject()) {
            map(0, records, mapping, descriptor, out);
        } else {
            throw new InvalidInputException("recordsPath '" + mapping.recordsPath() + "' does not point at records.");
        }
        return out.build();
    }

    private void map(int index, JsonNode node, RecordMapping mapping, SourceDescriptor descriptor,
            ParsedIngest.Builder out) {
        if (node == null || !node.isObject()) {
            out.reject(index, null, "INVALID_ITEM", "a record must be an object");
            return;
        }
        String externalId = GeoJsonIngestMapper.idText(node.at(mapping.externalId()));
        if (externalId == null) {
            out.reject(index, null, "EXTERNAL_ID_INVALID", "no external id at " + mapping.externalId());
            return;
        }
        String requested = mapping.kind() == null ? null : GeoJsonIngestMapper.text(node.at(mapping.kind()));
        String kind = GeoJsonIngestMapper.resolveKind(requested, descriptor);
        if (kind == null) {
            out.reject(index, externalId, "UNKNOWN_KIND", "kind is missing or not one of the source's kinds");
            return;
        }
        NormalizedRecord.Builder record = NormalizedRecord.builder(externalId, kind);
        try {
            record.observedAt(observedAt(node, mapping));
            record.geometry(geometry(node, mapping));
        } catch (InvalidInputException e) {
            out.reject(index, externalId, "INVALID_ITEM", e.getMessage());
            return;
        }
        for (RecordMapping.Field field : mapping.payload()) {
            JsonNode value = node.at(field.from());
            if (!value.isMissingNode()) {
                record.put(field.to(), mapper.toJava(value));
            }
        }
        out.add(index, record.build());
    }

    private static Instant observedAt(JsonNode node, RecordMapping mapping) {
        if (mapping.observedAt() == null) {
            return null;
        }
        JsonNode value = node.at(mapping.observedAt());
        return switch (mapping.observedAtFormat()) {
            case "epoch-s" -> IngestTimes.parseEpoch(value, false);
            case "epoch-ms" -> IngestTimes.parseEpoch(value, true);
            default -> value.isNumber() ? IngestTimes.parseEpoch(value, true) : IngestTimes.parse(value);
        };
    }

    private Geometry geometry(JsonNode node, RecordMapping mapping) {
        if (mapping.geometry() != null) {
            JsonNode value = node.at(mapping.geometry());
            return value.isMissingNode() || value.isNull() ? null : mapper.geometry(value);
        }
        if (mapping.lat() == null) {
            return null;
        }
        Double lat = number(node.at(mapping.lat()));
        Double lon = number(node.at(mapping.lon()));
        if (lat == null || lon == null) {
            return null;
        }
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new InvalidInputException("lat/lon are out of range");
        }
        return Geo.factory().createPoint(new Coordinate(lon, lat));
    }

    private static Double number(JsonNode value) {
        if (value == null || value.isMissingNode() || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return Double.isFinite(value.doubleValue()) ? value.doubleValue() : null;
        }
        if (value.isString()) {
            try {
                double parsed = Double.parseDouble(value.asString().strip());
                return Double.isFinite(parsed) ? parsed : null;
            } catch (NumberFormatException e) {
                throw new InvalidInputException("'" + value.asString() + "' is not a number");
            }
        }
        throw new InvalidInputException("a coordinate must be a number");
    }
}
