package ai.foresignal.source.adapters.mesh;

import java.time.Instant;
import java.util.Arrays;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the Meshtastic firmware's MQTT JSON output (prompt 083). Pure: no I/O, no clock, no state.
 *
 * <ul>
 *   <li><strong>Topics:</strong> only {@code <root>/2/json/<channel>/<gatewayId>} is parsed. A message on
 *       {@code <root>/2/e/…} or {@code <root>/2/map/…} carries a protobuf; it is dropped by its topic without its bytes
 *       ever being looked at ({@link Drop#PROTOBUF_TOPIC}). Any other topic is {@link Drop#NOT_JSON_TOPIC}.
 *   <li><strong>Envelope:</strong> {@code id, channel, from, to, type, sender, timestamp, payload, rssi, snr,
 *       hops_away}. Node numbers are read as unsigned 32-bit values: firmware from 2.2.0 sends them unsigned (values of
 *       2^31 and more), older firmware signed. A {@code !xxxxxxxx} string is accepted too; any other string id is
 *       malformed (third-party republishers on the public broker send such variants).
 *   <li><strong>Types:</strong> {@code nodeinfo} ({@code longname, shortname, hardware, role}), {@code position}
 *       ({@code latitude_i}/{@code longitude_i} × 1e-7, {@code altitude}, {@code precision_bits}), {@code telemetry}
 *       (device metrics) and {@code text}. Any other type is {@link Drop#UNKNOWN_TYPE}, for the caller to count.
 * </ul>
 */
public final class MeshtasticJsonParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** The largest message body read; a longer one is malformed (the firmware's JSON is well under 1 KB). */
    public static final int MAX_BODY_BYTES = 16 * 1024;

    private MeshtasticJsonParser() {}

    /** Why a message produced no packet. */
    public enum Drop {
        /** A protobuf topic ({@code /2/e/} or {@code /2/map/}), dropped without parsing. */
        PROTOBUF_TOPIC,
        /** A topic that is not {@code <root>/2/json/<channel>/<gatewayId>}. */
        NOT_JSON_TOPIC,
        /** A body that is not a JSON object with a sender and a type, or is too long. */
        MALFORMED,
        /** A type other than nodeinfo, position, telemetry and text. */
        UNKNOWN_TYPE
    }

    /**
     * The outcome of one message: a packet, or why there is none.
     *
     * @param packet the packet, or null
     * @param drop why there is none, or null
     */
    public record Result(MeshtasticPacket packet, Drop drop) {

        static Result of(MeshtasticPacket packet) {
            return new Result(packet, null);
        }

        static Result dropped(Drop drop) {
            return new Result(null, drop);
        }
    }

    /**
     * The topic's place in the Meshtastic tree.
     *
     * @param kind {@code json}, {@code e}, {@code map}, or null when the topic has no {@code /2/} segment
     * @param channel the channel segment of a JSON topic, else null
     * @param gateway the gateway segment of a JSON topic, else null
     */
    record TopicParts(String kind, String channel, String gateway) {}

    /** Splits a topic: the segment after the first {@code 2} below the root, and for JSON the channel and gateway. */
    static TopicParts topic(String topic) {
        if (topic == null) {
            return new TopicParts(null, null, null);
        }
        String[] segments = topic.split("/", -1);
        for (int i = 1; i + 1 < segments.length; i++) {
            if ("2".equals(segments[i])) {
                String kind = segments[i + 1];
                if ("json".equals(kind)) {
                    String[] rest = Arrays.copyOfRange(segments, i + 2, segments.length);
                    if (rest.length == 2 && !rest[0].isEmpty() && !rest[1].isEmpty()) {
                        return new TopicParts(kind, rest[0], rest[1]);
                    }
                    return new TopicParts("json-invalid", null, null);
                }
                return new TopicParts(kind, null, null);
            }
        }
        return new TopicParts(null, null, null);
    }

    /** True for a protobuf topic, which must never be parsed. */
    public static boolean isProtobufTopic(String topic) {
        String kind = topic(topic).kind();
        return "e".equals(kind) || "map".equals(kind);
    }

    /** Parses one message. */
    public static Result parse(String topic, byte[] body) {
        TopicParts parts = topic(topic);
        if ("e".equals(parts.kind()) || "map".equals(parts.kind())) {
            return Result.dropped(Drop.PROTOBUF_TOPIC);
        }
        if (!"json".equals(parts.kind())) {
            return Result.dropped(Drop.NOT_JSON_TOPIC);
        }
        if (body == null || body.length == 0 || body.length > MAX_BODY_BYTES) {
            return Result.dropped(Drop.MALFORMED);
        }
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            return Result.dropped(Drop.MALFORMED);
        }
        if (root == null || !root.isObject()) {
            return Result.dropped(Drop.MALFORMED);
        }
        Long from = nodeNumber(root.get("from"));
        String type = text(root.get("type"));
        if (from == null || type == null) {
            return Result.dropped(Drop.MALFORMED);
        }
        JsonNode p = root.path("payload");
        MeshtasticPacket.Payload payload = switch (type) {
            case "nodeinfo" -> new MeshtasticPacket.NodeInfo(text(p.get("longname")), text(p.get("shortname")),
                    integer(p.get("hardware")), integer(p.get("role")));
            case "position" -> position(p);
            case "telemetry" -> new MeshtasticPacket.Telemetry(integer(p.get("battery_level")), decimal(p.get("voltage")),
                    decimal(p.get("channel_utilization")), decimal(p.get("air_util_tx")), number(p.get("uptime_seconds")));
            case "text" -> {
                String text = p.isString() ? p.asString() : text(p.get("text"));
                yield text == null ? null : new MeshtasticPacket.Text(text);
            }
            default -> null;
        };
        if (payload == null) {
            return Result.dropped(switch (type) {
                case "nodeinfo", "position", "telemetry", "text" -> Drop.MALFORMED;
                default -> Drop.UNKNOWN_TYPE;
            });
        }
        Long to = nodeNumber(root.get("to"));
        Long ts = number(root.get("timestamp"));
        return Result.of(new MeshtasticPacket(topic, parts.channel(), parts.gateway(), number(root.get("id")),
                integer(root.get("channel")), MeshRecords.meshtasticNodeId(from),
                to == null ? null : MeshRecords.meshtasticNodeId(to), type,
                ts == null || ts <= 0 ? null : Instant.ofEpochSecond(ts), nonZero(decimal(root.get("rssi"))),
                nonZero(decimal(root.get("snr"))), integer(root.get("hops_away")), payload));
    }

    private static MeshtasticPacket.Payload position(JsonNode p) {
        Long latI = number(p.get("latitude_i"));
        Long lonI = number(p.get("longitude_i"));
        Double lat = latI == null ? null : (int) (long) latI / 1e7;
        Double lon = lonI == null ? null : (int) (long) lonI / 1e7;
        if (lat != null && (Math.abs(lat) > 90 || Math.abs(lon == null ? 0 : lon) > 180
                || (latI == 0 && lonI != null && lonI == 0))) {
            lat = null;
            lon = null;
        }
        return new MeshtasticPacket.Position(lat, lon, integer(p.get("altitude")), integer(p.get("precision_bits")));
    }

    /**
     * A node number: a JSON integer (signed or unsigned), or a {@code !xxxxxxxx} string; anything else (a decimal or
     * bare hex string, which third-party republishers send and which cannot be told apart) is null.
     */
    static Long nodeNumber(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isIntegralNumber()) {
            return node.asLong();
        }
        if (node.isString()) {
            String text = node.asString().strip();
            if (text.length() == 9 && text.charAt(0) == '!') {
                try {
                    return Long.parseLong(text.substring(1), 16);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static Double nonZero(Double value) {
        return value == null || value == 0.0 ? null : value;
    }

    /** A JSON integer (or integer string) as a long, or null. */
    static Long number(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isIntegralNumber()) {
            return node.asLong();
        }
        if (node.isNumber()) {
            return (long) node.asDouble();
        }
        if (node.isString()) {
            try {
                return Long.parseLong(node.asString().strip());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** A JSON integer as an int, or null. */
    static Integer integer(JsonNode node) {
        Long value = number(node);
        return value == null ? null : (int) (long) value;
    }

    /** A JSON number (or numeric string) as a double, or null. */
    static Double decimal(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isNumber()) {
            double v = node.asDouble();
            return Double.isFinite(v) ? v : null;
        }
        if (node.isString()) {
            try {
                double v = Double.parseDouble(node.asString().strip());
                return Double.isFinite(v) ? v : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** A JSON string, stripped of control characters, or null when absent or blank. */
    static String text(JsonNode node) {
        if (node == null || !node.isString()) {
            return null;
        }
        StringBuilder out = new StringBuilder();
        node.asString().codePoints().filter(cp -> !Character.isISOControl(cp)).forEach(out::appendCodePoint);
        String value = out.toString().strip();
        return value.isEmpty() ? null : value;
    }
}
