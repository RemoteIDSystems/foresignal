package ai.foresignal.source.adapters.mesh;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the community Meshtastic map's node list (prompt 083), {@code {"nodes":[...]}} of about 29 MB, as a stream: the
 * document is never built as one tree, only one node object at a time.
 *
 * <p>Fields (verified 2026-10): {@code node_id} (decimal, unsigned), {@code long_name}, {@code short_name},
 * {@code hardware_model} and {@code hardware_model_name}, {@code role} and {@code role_name},
 * {@code firmware_version}, {@code position_precision}, {@code latitude} and {@code longitude} (integers × 1e-7, as
 * {@code latitude_i} in the firmware), {@code battery_level}, {@code voltage}, {@code channel_utilization},
 * {@code air_util_tx} and {@code uptime_seconds} (numbers, often as strings), {@code position_updated_at} and
 * {@code updated_at} (ISO-8601). {@code observedAt} is {@code updated_at}, else {@code position_updated_at}; a node
 * with neither, or without a usable {@code node_id}, is skipped and counted.
 */
public final class MeshtasticMapParser {

    /**
     * Reads one node tree at a time from a parser inside the {@code nodes} array: Jackson 3's default
     * {@code FAIL_ON_TRAILING_TOKENS} would refuse a tree read that leaves the next node behind it.
     */
    private static final JsonMapper JSON = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    private MeshtasticMapParser() {}

    /**
     * What one parse produced.
     *
     * @param records the records handed over
     * @param skipped the nodes skipped
     */
    public record Counts(int records, int skipped) {}

    /**
     * Streams the node list, handing each record to {@code consumer}.
     *
     * @throws UpstreamFormatException when the body is not a JSON object with a {@code nodes} array
     */
    public static Counts parse(InputStream in, Consumer<NormalizedRecord> consumer) {
        int records = 0;
        int skipped = 0;
        boolean sawNodes = false;
        try (JsonParser parser = JSON.createParser(in)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new UpstreamFormatException("meshtastic-map: the body is not a JSON object", "");
            }
            while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
                String name = parser.currentName();
                JsonToken value = parser.nextToken();
                if ("nodes".equals(name) && value == JsonToken.START_ARRAY) {
                    sawNodes = true;
                    JsonToken token;
                    while ((token = parser.nextToken()) != JsonToken.END_ARRAY && token != null) {
                        if (token != JsonToken.START_OBJECT) {
                            parser.skipChildren();
                            skipped++;
                            continue;
                        }
                        NormalizedRecord record = map(JSON.readTree(parser));
                        if (record == null) {
                            skipped++;
                        } else {
                            consumer.accept(record);
                            records++;
                        }
                    }
                } else {
                    parser.skipChildren();
                }
            }
        } catch (JacksonException e) {
            throw new UpstreamFormatException("meshtastic-map: the node list does not parse: " + e.getOriginalMessage(),
                    "");
        }
        if (!sawNodes) {
            throw new UpstreamFormatException("meshtastic-map: the body has no nodes array", "");
        }
        return new Counts(records, skipped);
    }

    /** The record of one node object, or null when it is skipped. */
    static NormalizedRecord map(JsonNode node) {
        Long number = MeshtasticJsonParser.number(node.get("node_id"));
        if (number == null || number < 0 || number > 0xFFFF_FFFFL) {
            return null;
        }
        Instant updated = instant(node.get("updated_at"));
        Instant positionUpdated = instant(node.get("position_updated_at"));
        Instant observedAt = updated != null ? updated : positionUpdated;
        if (observedAt == null) {
            return null;
        }
        String hardware = MeshtasticJsonParser.text(node.get("hardware_model_name"));
        if (hardware == null) {
            Long model = MeshtasticJsonParser.number(node.get("hardware_model"));
            hardware = model == null ? null : Long.toString(model);
        }
        String role = MeshtasticJsonParser.text(node.get("role_name"));
        if (role == null) {
            role = MeshRecords.meshtasticRole(MeshtasticJsonParser.integer(node.get("role")));
        }
        MeshNodeState state = new MeshNodeState(MeshRecords.meshtasticNodeId(number), MeshRecords.MESHTASTIC)
                .identity(MeshtasticJsonParser.text(node.get("long_name")),
                        MeshtasticJsonParser.text(node.get("short_name")), role, hardware,
                        MeshtasticJsonParser.text(node.get("firmware_version")), null)
                .telemetry(battery(node.get("battery_level")), round(MeshtasticJsonParser.decimal(node.get("voltage")), 3),
                        round(MeshtasticJsonParser.decimal(node.get("channel_utilization")), 2),
                        round(MeshtasticJsonParser.decimal(node.get("air_util_tx")), 2),
                        MeshtasticJsonParser.number(node.get("uptime_seconds")))
                .heard(observedAt);
        Long latI = MeshtasticJsonParser.number(node.get("latitude"));
        Long lonI = MeshtasticJsonParser.number(node.get("longitude"));
        if (latI != null && lonI != null && !(latI == 0 && lonI == 0)) {
            double lat = latI / 1e7;
            double lon = lonI / 1e7;
            if (Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
                Integer bits = MeshtasticJsonParser.integer(node.get("position_precision"));
                state.position(lat, lon, bits == null || bits <= 0 ? null : bits);
            }
        }
        return MeshRecords.node(state, observedAt, null);
    }

    private static Integer battery(JsonNode node) {
        Long value = MeshtasticJsonParser.number(node);
        return value == null || value < 0 || value > 255 ? null : (int) (long) value;
    }

    private static Double round(Double value, int decimals) {
        if (value == null) {
            return null;
        }
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }

    private static Instant instant(JsonNode node) {
        String text = MeshtasticJsonParser.text(node);
        if (text == null) {
            return null;
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
