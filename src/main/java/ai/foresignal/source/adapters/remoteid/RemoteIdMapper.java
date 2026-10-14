package ai.foresignal.source.adapters.remoteid;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * DragonSync's MQTT drone payload as a {@value #KIND} record (prompt 086), pure; written from DragonSync's
 * {@code docs/mqtt-schema.md} field table (Apache-2.0), no code copied.
 *
 * <ul>
 *   <li><strong>Topics:</strong> only the drone state topics are parsed, {@code <root>/drones} (aggregate) and
 *       {@code <root>/drone/<id>} (per drone, identical JSON). Every other topic, in particular
 *       {@code <root>/drone/<id>/pilot_attrs} and {@code home_attrs}, is dropped by its name before parsing.
 *   <li><strong>DJI DroneID is dropped</strong> (proprietary, not ASTM F3411 / ASD-STAN Remote ID; Gap-Analysis §9).
 *       DragonSync marks it by an empty {@code transport} (its schema: "Empty for OcuSync — receiver doesn't tag link
 *       layer") and a {@code description} starting with {@code DJI} ({@code DJI O4 (Decrypted)}). A message with an
 *       empty transport, or a {@code DJI…} description on a link that is not a Remote ID link, is
 *       {@link Drop#DJI}. Remote ID links are Wi-Fi beacon or NAN and Bluetooth ({@code WiFi-…}, {@code BT…},
 *       {@code BLE…}); any other transport (SiK or mLRS telemetry, {@code ISM-FHSS}) is {@link Drop#NOT_REMOTE_ID}.
 *   <li><strong>Pilot and home positions are never read</strong> ({@code pilot_lat/lon}, {@code home_lat/lon}),
 *       unless {@code keepOperatorPosition} is set, which the adapter allows only on a private source; then the pilot
 *       position becomes the payload's {@code operatorPoint} {@code {lat, lon}}, never the geometry. Home positions are
 *       never kept.
 *   <li><strong>Fields:</strong> {@code id} ({@code drone-} prefix removed) → externalId {@code rid:<id>};
 *       {@code lat, lon} (0, 0 means no fix) → point; {@code alt} → {@code alt} (HAE, m); {@code height} → {@code agl};
 *       {@code speed}, {@code vspeed} (m/s); {@code direction} → {@code course}; {@code ua_type} → {@code uaType};
 *       {@code id_type} → {@code idType}; {@code operator_id} → {@code operatorId}; observer {@code seen_by} with
 *       {@code rssi} and {@code freq_mhz} (as Hz).
 *   <li><strong>Time:</strong> {@code observed_at} (the kit's Unix time) is {@code observedAt}; a message without it
 *       is {@link Drop#UNTIMED}. {@code rid_timestamp} and {@code timestamp} are not UTC and are never read.
 * </ul>
 */
public final class RemoteIdMapper {

    /** The record kind. */
    public static final String KIND = "drone";

    /** The externalId prefix. */
    public static final String ID_PREFIX = "rid:";

    /** The altitudes accepted, in meters; RID's "unknown" is -1000 and DJI reports 65531. */
    static final double MIN_ALT = -999.0;

    /** The highest altitude accepted. */
    static final double MAX_ALT = 20_000.0;

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Pattern ID = Pattern.compile("^[A-Za-z0-9._:-]{1,64}$");

    /** Why a message became no record. */
    public enum Drop {
        /** A DJI DroneID detection. */
        DJI,
        /** A link that is not Remote ID. */
        NOT_REMOTE_ID,
        /** A topic other than a drone state topic. */
        TOPIC,
        /** Not a JSON object, or no usable id. */
        MALFORMED,
        /** No {@code observed_at}. */
        UNTIMED;

        /** The metric tag. */
        public String tag() {
            return name().toLowerCase(Locale.ROOT).replace('_', '-');
        }
    }

    /**
     * The outcome of one message: a record, or the reason there is none.
     *
     * @param record the record, or null
     * @param drop the reason, or null with a record
     */
    public record Result(NormalizedRecord record, Drop drop) {

        static Result of(NormalizedRecord record) {
            return new Result(record, null);
        }

        static Result dropped(Drop drop) {
            return new Result(null, drop);
        }
    }

    private RemoteIdMapper() {}

    /** True for {@code <root>/drones} and {@code <root>/drone/<id>}. */
    public static boolean isDroneTopic(String topic) {
        if (topic == null) {
            return false;
        }
        String[] parts = topic.split("/", -1);
        if (parts.length >= 2 && parts[parts.length - 1].equals("drones")) {
            return true;
        }
        return parts.length >= 3 && parts[parts.length - 2].equals("drone") && !parts[parts.length - 1].isEmpty();
    }

    /**
     * Maps one MQTT message.
     *
     * @param topic the topic
     * @param payload the message bytes
     * @param keepOperatorPosition true only on a private source: keep the pilot position as {@code operatorPoint}
     */
    public static Result map(String topic, byte[] payload, boolean keepOperatorPosition) {
        if (!isDroneTopic(topic)) {
            return Result.dropped(Drop.TOPIC);
        }
        JsonNode node;
        try {
            node = payload == null ? null : JSON.readTree(payload);
        } catch (JacksonException e) {
            return Result.dropped(Drop.MALFORMED);
        }
        if (node == null || !node.isObject()) {
            return Result.dropped(Drop.MALFORMED);
        }
        return map(node, keepOperatorPosition);
    }

    /** Maps one drone payload object. */
    public static Result map(JsonNode node, boolean keepOperatorPosition) {
        String transport = text(node.get("transport"));
        String description = text(node.get("description"));
        boolean ridLink = isRemoteIdTransport(transport);
        if (transport == null || (!ridLink && description != null
                && description.toUpperCase(Locale.ROOT).startsWith("DJI"))) {
            return Result.dropped(Drop.DJI);
        }
        if (!ridLink) {
            return Result.dropped(Drop.NOT_REMOTE_ID);
        }
        String id = text(node.get("id"));
        if (id != null && id.startsWith("drone-")) {
            id = id.substring("drone-".length());
        }
        if (id == null || !ID.matcher(id).matches()) {
            return Result.dropped(Drop.MALFORMED);
        }
        Double observed = number(node.get("observed_at"));
        if (observed == null || observed <= 0) {
            return Result.dropped(Drop.UNTIMED);
        }
        Instant observedAt = Instant.ofEpochMilli(Math.round(observed * 1000));
        Map<String, Object> payload = new LinkedHashMap<>();
        Double alt = number(node.get("alt"));
        put(payload, "alt", alt == null || alt < MIN_ALT || alt > MAX_ALT ? null : round(alt, 1));
        Double agl = number(node.get("height"));
        put(payload, "agl", agl == null || agl < MIN_ALT || agl > MAX_ALT ? null : round(agl, 1));
        Double speed = number(node.get("speed"));
        put(payload, "speed", speed == null || speed < 0 || speed > 250 ? null : round(speed, 1));
        Double vspeed = number(node.get("vspeed"));
        put(payload, "vspeed", vspeed == null || Math.abs(vspeed) > 100 ? null : round(vspeed, 1));
        Double course = number(node.get("direction"));
        put(payload, "course", course == null || course < 0 || course > 360 ? null : round(course % 360, 0));
        JsonNode uaType = node.get("ua_type");
        put(payload, "uaType", uaType != null && uaType.isIntegralNumber() ? uaType.intValue() : null);
        put(payload, "idType", text(node.get("id_type")));
        put(payload, "operatorId", text(node.get("operator_id")));
        if (keepOperatorPosition) {
            Double plat = number(node.get("pilot_lat"));
            Double plon = number(node.get("pilot_lon"));
            if (validFix(plat, plon)) {
                payload.put("operatorPoint", Map.of("lat", round(plat, 6), "lon", round(plon, 6)));
            }
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(ID_PREFIX + id, KIND)
                .observedAt(observedAt)
                .putAll(payload);
        Double lat = number(node.get("lat"));
        Double lon = number(node.get("lon"));
        if (validFix(lat, lon)) {
            b.point(lon, lat);
            b.quality(Quality.positionSource(PositionSource.REPORTED));
        }
        String seenBy = text(node.get("seen_by"));
        if (seenBy != null) {
            Double freqMhz = number(node.get("freq_mhz"));
            Long hz = freqMhz == null || freqMhz <= 0 ? null : Math.round(freqMhz * 1_000_000d);
            b.observers(List.of(new Observer(seenBy, hz, number(node.get("rssi")), null, null)));
        }
        return Result.of(b.build());
    }

    /** Wi-Fi beacon or NAN and Bluetooth links carry ASTM F3411 / ASD-STAN Remote ID. */
    static boolean isRemoteIdTransport(String transport) {
        if (transport == null) {
            return false;
        }
        String t = transport.toUpperCase(Locale.ROOT);
        return t.startsWith("WIFI") || t.startsWith("BT") || t.startsWith("BLE");
    }

    private static boolean validFix(Double lat, Double lon) {
        return lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180 && !(lat == 0 && lon == 0);
    }

    private static String text(JsonNode node) {
        if (node == null || !node.isString()) {
            return null;
        }
        String t = node.stringValue().strip();
        return t.isEmpty() ? null : (t.length() > 64 ? t.substring(0, 64) : t);
    }

    private static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double v = node.doubleValue();
        return Double.isFinite(v) ? v : null;
    }

    private static double round(double v, int places) {
        double r = BigDecimal.valueOf(v).setScale(places, RoundingMode.HALF_UP).doubleValue();
        return r == 0.0 ? 0.0 : r;
    }

    private static void put(Map<String, Object> payload, String key, Object value) {
        if (value != null) {
            payload.put(key, value);
        }
    }
}
