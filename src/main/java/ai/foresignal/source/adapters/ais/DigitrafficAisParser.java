package ai.foresignal.source.adapters.ais;

import ai.foresignal.codec.ais.AisMessage;
import ai.foresignal.codec.ais.VesselStateMerger;
import ai.foresignal.codec.ais.VesselUpdate;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Digitraffic marine JSON to source-neutral vessel updates (prompt 085), pure. Shapes verified on 2026-10-13 against
 * {@code wss://meri.digitraffic.fi:443/mqtt} and {@code GET /api/ais/v1/vessels}:
 *
 * <ul>
 *   <li><b>Location</b> ({@code vessels-v2/{mmsi}/location}): {@code {time (s), sog, cog, navStat, rot, posAcc, raim,
 *       heading, lon, lat}}; AIS sentinels as on the radio (SOG 102.3, COG 360, heading 511, ROT −128, status 15,
 *       latitude 91, longitude 181).
 *   <li><b>Metadata</b> ({@code vessels-v2/{mmsi}/metadata}): {@code {timestamp (ms), destination, name, draught,
 *       eta, posType, refA..refD, callSign, imo, type}}. {@code draught} (the wire name; the draft) is in tenths of a meter and {@code eta} is the
 *       AIS bit field (month bits 19–16, day 15–11, hour 10–6, minute 5–0), both as on the radio.
 *   <li><b>Vessel list</b> ({@code /api/ais/v1/vessels}): the metadata fields under the REST names
 *       {@code referencePointA..D}, {@code shipType} and {@code mmsi}.
 *   <li><b>Status</b> ({@code vessels-v2/status}): {@code {updated (s)}}.
 * </ul>
 *
 * <p>MMSIs come from the topic (or the list's {@code mmsi}) and must lie in 100000000–999999999. The class
 * ({@code A|B}) is not reported by Digitraffic and stays absent.
 */
final class DigitrafficAisParser {

    /** The topic prefix of every vessel topic. */
    static final String PREFIX = "vessels-v2/";

    /** The status topic. */
    static final String STATUS_TOPIC = "vessels-v2/status";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private DigitrafficAisParser() {}

    /** What a topic carries. */
    enum TopicKind {
        LOCATION, METADATA, STATUS, OTHER
    }

    /** The kind of {@code topic}. */
    static TopicKind kind(String topic) {
        if (STATUS_TOPIC.equals(topic)) {
            return TopicKind.STATUS;
        }
        if (topic != null && topic.startsWith(PREFIX)) {
            if (topic.endsWith("/location")) {
                return TopicKind.LOCATION;
            }
            if (topic.endsWith("/metadata")) {
                return TopicKind.METADATA;
            }
        }
        return TopicKind.OTHER;
    }

    /** The nine-digit MMSI of a {@code vessels-v2/{mmsi}/…} topic, or null when absent or invalid. */
    static String mmsiFromTopic(String topic) {
        if (topic == null || !topic.startsWith(PREFIX)) {
            return null;
        }
        String[] parts = topic.split("/");
        if (parts.length != 3) {
            return null;
        }
        try {
            return VesselStateMerger.mmsiText(Long.parseLong(parts[1]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * A location message.
     *
     * @return the position, or null when the JSON is unusable or has no time
     */
    static VesselUpdate.Position location(String mmsi, byte[] payload) {
        JsonNode node = read(payload);
        if (node == null || !node.isObject()) {
            return null;
        }
        Long time = JsonProps.integer(node, "time");
        if (time == null) {
            return null;
        }
        Double lat = JsonProps.number(node, "lat");
        Double lon = JsonProps.number(node, "lon");
        if (lat != null && (lat < -90 || lat > 90)) {
            lat = null;
        }
        if (lon != null && (lon < -180 || lon > 180)) {
            lon = null;
        }
        if (lat == null || lon == null) {
            lat = null;
            lon = null;
        }
        Double sog = JsonProps.number(node, "sog");
        if (sog != null && (sog < 0 || sog >= 102.3)) {
            sog = null;
        }
        Double cog = JsonProps.number(node, "cog");
        if (cog != null && (cog < 0 || cog >= 360)) {
            cog = null;
        }
        Integer heading = integer(node, "heading");
        if (heading != null && (heading < 0 || heading > 359)) {
            heading = null;
        }
        Integer navStatus = integer(node, "navStat");
        if (navStatus != null && (navStatus < 0 || navStatus > 14)) {
            navStatus = null;
        }
        Integer rot = integer(node, "rot");
        if (rot != null && (rot < -127 || rot > 127)) {
            rot = null;
        }
        JsonNode posAcc = node.get("posAcc");
        Boolean accuracy = posAcc != null && posAcc.isBoolean() ? posAcc.booleanValue() : null;
        return new VesselUpdate.Position(mmsi, Instant.ofEpochSecond(time), null, lat, lon, sog, cog, heading,
                navStatus, rot, accuracy, false);
    }

    /**
     * A metadata message (the whole voyage block, so absent voyage values clear).
     *
     * @return the static data, or null when the JSON is unusable or has no timestamp
     */
    static VesselUpdate.Static metadata(String mmsi, byte[] payload) {
        JsonNode node = read(payload);
        return node == null || !node.isObject() ? null : staticData(mmsi, node, "refA", "refB", "refC", "refD", "type");
    }

    /**
     * The REST vessel list.
     *
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    static List<VesselUpdate.Static> vesselList(byte[] body) {
        JsonNode root = read(body);
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("Digitraffic vessel list is not a JSON array", null);
        }
        List<VesselUpdate.Static> out = new ArrayList<>(root.size());
        for (JsonNode entry : root) {
            Long mmsi = JsonProps.integer(entry, "mmsi");
            String text = mmsi == null ? null : VesselStateMerger.mmsiText(mmsi);
            if (text == null) {
                continue;
            }
            VesselUpdate.Static s = staticData(text, entry, "referencePointA", "referencePointB", "referencePointC",
                    "referencePointD", "shipType");
            if (s != null) {
                out.add(s);
            }
        }
        return out;
    }

    /** The {@code updated} time of a status message, or null. */
    static Instant statusUpdated(byte[] payload) {
        JsonNode node = read(payload);
        Long updated = node == null ? null : JsonProps.integer(node, "updated");
        return updated == null ? null : Instant.ofEpochSecond(updated);
    }

    /** The ETA parts of Digitraffic's AIS bit field, or null when any part is unavailable. */
    static AisMessage.Eta eta(Long field) {
        if (field == null) {
            return null;
        }
        long v = field;
        return AisMessage.Eta.ofOrNull((int) ((v >> 16) & 0xF), (int) ((v >> 11) & 0x1F), (int) ((v >> 6) & 0x1F),
                (int) (v & 0x3F));
    }

    /** Text trimmed of {@code @} padding (cut at the first {@code @}) and spaces; null when nothing is left. */
    static String aisText(String value) {
        if (value == null) {
            return null;
        }
        int at = value.indexOf('@');
        String t = (at >= 0 ? value.substring(0, at) : value).strip();
        return t.isEmpty() ? null : t;
    }

    private static VesselUpdate.Static staticData(String mmsi, JsonNode node, String a, String b, String c, String d,
            String typeField) {
        Long timestamp = JsonProps.integer(node, "timestamp");
        if (timestamp == null) {
            return null;
        }
        Integer draftTenths = positive(node, "draught");
        AisMessage.Dimension dims = AisMessage.Dimension.ofOrNull(nonNegative(node, a), nonNegative(node, b),
                nonNegative(node, c), nonNegative(node, d));
        return new VesselUpdate.Static(mmsi, Instant.ofEpochMilli(timestamp), null, true,
                aisText(JsonProps.text(node, "name")), aisText(JsonProps.text(node, "callSign")), positive(node, "imo"),
                positive(node, typeField), dims, eta(JsonProps.integer(node, "eta")),
                draftTenths == null ? null : draftTenths / 10.0, aisText(JsonProps.text(node, "destination")));
    }

    private static Integer integer(JsonNode node, String field) {
        Long v = JsonProps.integer(node, field);
        return v == null || v > Integer.MAX_VALUE || v < Integer.MIN_VALUE ? null : v.intValue();
    }

    private static Integer positive(JsonNode node, String field) {
        Integer v = integer(node, field);
        return v == null || v <= 0 ? null : v;
    }

    private static int nonNegative(JsonNode node, String field) {
        Integer v = integer(node, field);
        return v == null || v < 0 ? 0 : v;
    }

    private static JsonNode read(byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }
        try {
            return JSON.readTree(body);
        } catch (JacksonException e) {
            return null;
        }
    }
}
