package ai.foresignal.source.codec;

import ai.foresignal.source.formats.readsb.ReadsbAircraftMapper;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * gpsd's JSON protocol, codec {@code gpsd} (prompt 069; https://gpsd.gitlab.io/gpsd/gpsd_json.html).
 *
 * <p>On connect the transport writes {@link #WATCH}; gpsd answers with a {@code VERSION} object, whose
 * {@code proto_major} must be {@value #PROTO_MAJOR}, and then streams one JSON object per line.
 *
 * <ul>
 *   <li><strong>TPV</strong> (time-position-velocity) emits one {@code gnss-fix} record per {@code device}:
 *       {@code mode} (0 unknown, 1 no fix, 2 2D, 3 3D), {@code time} as {@code observedAt}, {@code lat},
 *       {@code lon} (a point when {@code mode ≥ 2}), {@code altHAE} (only with a 3D fix), {@code eph},
 *       {@code epv}, {@code ept}, {@code speed} (m/s) and {@code track}. The deprecated {@code alt} is
 *       ignored. A TPV without {@code time} uses {@code receivedAt} and sets
 *       {@code quality.extra["gpsd.time_imputed"]}.
 *   <li><strong>SKY</strong> merges {@code nSat}, {@code uSat} and {@code hdop} into the device's state
 *       (counted from {@code satellites[]} when an older gpsd sends no counts) and re-emits once a TPV has
 *       been seen.
 *   <li><strong>Significant fields</strong> computed here: {@code fixQuality} ({@code none|2d|3d}) and
 *       {@code navDegraded}, true when {@code mode < 3 || uSat < 4 || eph > 50} (terms with an unknown
 *       value are left out).
 *   <li>Other classes ({@code DEVICES, WATCH, GST, ATT, PPS, TOFF}) are ignored; an unparsable line is
 *       skipped and counted. A {@code VERSION} with another major is an {@link UpstreamFormatException},
 *       which closes the connection.
 * </ul>
 *
 * <p>Values are rounded so a stationary receiver does not write a new version per second: meters and
 * speeds to 0.1, {@code ept} to 0.001 s, {@code hdop} to 0.01, coordinates as sent (the private source's
 * precision cap applies at ingest).
 */
@Component
public class GpsdCodec implements SocketCodec {

    /** The codec id. */
    public static final String ID = "gpsd";

    /** The record kind. */
    public static final String KIND = "gnss-fix";

    /** The supported protocol major version. */
    public static final int PROTO_MAJOR = 3;

    /** The watch command written on connect. */
    public static final String WATCH = "?WATCH={\"enable\":true,\"json\":true};\n";

    /** The {@code eph} above which navigation counts as degraded, in meters. */
    public static final double EPH_DEGRADED_METERS = 50.0;

    /** The {@code uSat} below which navigation counts as degraded. */
    public static final int MIN_USED_SATELLITES = 4;

    /** {@code quality.extra} key set when a TPV had no {@code time}. */
    public static final String TIME_IMPUTED_EXTRA = "gpsd.time_imputed";

    /** The device id used when an object names none. */
    static final String DEFAULT_DEVICE = "gpsd";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Framing framing() {
        return Framing.LINES;
    }

    @Override
    public byte[] onConnect() {
        return WATCH.getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public CodecSession newSession(CodecContext context) {
        return new Session();
    }

    /** One connection's per-device state. */
    static final class Session implements CodecSession {

        private final Map<String, DeviceState> devices = new LinkedHashMap<>();
        private long skipped;
        private Integer protoMajor;

        @Override
        public List<NormalizedRecord> onFrame(byte[] frame, Instant receivedAt) {
            JsonNode node;
            try {
                node = JSON.readTree(frame);
            } catch (JacksonException e) {
                skipped++;
                return List.of();
            }
            if (node == null || !node.isObject()) {
                skipped++;
                return List.of();
            }
            String type = text(node.get("class"));
            if (type == null) {
                skipped++;
                return List.of();
            }
            return switch (type) {
                case "VERSION" -> {
                    version(node);
                    yield List.of();
                }
                case "TPV" -> tpv(node, receivedAt);
                case "SKY" -> sky(node);
                default -> List.of();
            };
        }

        @Override
        public long skipped() {
            return skipped;
        }

        /** The {@code proto_major} of the VERSION seen, or null before one arrived. */
        Integer protoMajor() {
            return protoMajor;
        }

        private void version(JsonNode node) {
            JsonNode major = node.get("proto_major");
            if (major == null || !major.isIntegralNumber()) {
                throw new UpstreamFormatException("gpsd VERSION has no proto_major", node.toString());
            }
            protoMajor = major.intValue();
            if (protoMajor != PROTO_MAJOR) {
                throw new UpstreamFormatException("gpsd speaks protocol " + protoMajor + "; only " + PROTO_MAJOR
                        + ".x is supported", node.toString());
            }
        }

        private List<NormalizedRecord> tpv(JsonNode node, Instant receivedAt) {
            DeviceState d = devices.computeIfAbsent(device(node), k -> new DeviceState());
            Integer mode = integer(node.get("mode"));
            d.mode = mode == null ? 0 : Math.max(0, Math.min(3, mode));
            d.time = null;
            String time = text(node.get("time"));
            if (time != null) {
                try {
                    d.time = Instant.parse(time);
                } catch (DateTimeParseException e) {
                    d.time = null;
                }
            }
            d.receivedAt = receivedAt;
            Double lat = number(node.get("lat"));
            Double lon = number(node.get("lon"));
            boolean fix = d.mode >= 2 && ReadsbAircraftMapper.validPosition(lat, lon);
            d.lat = fix ? lat : null;
            d.lon = fix ? lon : null;
            d.altHae = d.mode == 3 ? round(number(node.get("altHAE")), 1) : null;
            d.eph = round(number(node.get("eph")), 1);
            d.epv = round(number(node.get("epv")), 1);
            d.ept = round(number(node.get("ept")), 3);
            d.speed = d.mode >= 2 ? round(number(node.get("speed")), 1) : null;
            d.track = d.mode >= 2 ? round(number(node.get("track")), 1) : null;
            d.tpvSeen = true;
            return List.of(d.record(device(node)));
        }

        private List<NormalizedRecord> sky(JsonNode node) {
            String id = device(node);
            DeviceState d = devices.computeIfAbsent(id, k -> new DeviceState());
            Integer nSat = integer(node.get("nSat"));
            Integer uSat = integer(node.get("uSat"));
            JsonNode satellites = node.get("satellites");
            if ((nSat == null || uSat == null) && satellites != null && satellites.isArray()) {
                int seen = 0;
                int used = 0;
                for (JsonNode sat : satellites) {
                    seen++;
                    JsonNode u = sat.get("used");
                    if (u != null && u.isBoolean() && u.booleanValue()) {
                        used++;
                    }
                }
                nSat = nSat == null ? seen : nSat;
                uSat = uSat == null ? used : uSat;
            }
            if (nSat != null) {
                d.nSat = nSat;
            }
            if (uSat != null) {
                d.uSat = uSat;
            }
            Double hdop = round(number(node.get("hdop")), 2);
            if (hdop != null) {
                d.hdop = hdop;
            }
            return d.tpvSeen ? List.of(d.record(id)) : List.of();
        }
    }

    /** What is known about one device. */
    static final class DeviceState {

        int mode;
        Instant time;
        Instant receivedAt;
        Double lat;
        Double lon;
        Double altHae;
        Double eph;
        Double epv;
        Double ept;
        Double speed;
        Double track;
        Integer nSat;
        Integer uSat;
        Double hdop;
        boolean tpvSeen;

        NormalizedRecord record(String device) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("mode", mode);
            payload.put("fixQuality", fixQuality(mode));
            payload.put("navDegraded", navDegraded(mode, uSat, eph));
            put(payload, "altHAE", altHae);
            put(payload, "eph", eph);
            put(payload, "epv", epv);
            put(payload, "ept", ept);
            put(payload, "speed", speed);
            put(payload, "track", track);
            put(payload, "nSat", nSat);
            put(payload, "uSat", uSat);
            put(payload, "hdop", hdop);
            Map<String, Object> extra = new LinkedHashMap<>();
            Instant observedAt = time;
            if (observedAt == null) {
                observedAt = receivedAt;
                extra.put(TIME_IMPUTED_EXTRA, true);
            }
            NormalizedRecord.Builder builder = NormalizedRecord.builder(device, KIND)
                    .observedAt(observedAt)
                    .putAll(payload)
                    .quality(new Quality(eph, null, lat == null ? null : PositionSource.REPORTED, extra));
            if (lat != null && lon != null) {
                builder.point(lon, lat);
            }
            return builder.build();
        }
    }

    /** {@code none} below a 2D fix, else {@code 2d} or {@code 3d}. */
    public static String fixQuality(int mode) {
        return mode >= 3 ? "3d" : mode == 2 ? "2d" : "none";
    }

    /**
     * True when {@code mode < 3}, {@code uSat < 4} or {@code eph > 50}; a null {@code uSat} or {@code eph}
     * leaves its term out.
     */
    public static boolean navDegraded(int mode, Integer uSat, Double eph) {
        return mode < 3 || (uSat != null && uSat < MIN_USED_SATELLITES) || (eph != null && eph > EPH_DEGRADED_METERS);
    }

    private static void put(Map<String, Object> payload, String key, Object value) {
        if (value != null) {
            payload.put(key, value);
        }
    }

    private static String device(JsonNode node) {
        String device = text(node.get("device"));
        return device == null ? DEFAULT_DEVICE : device;
    }

    private static String text(JsonNode node) {
        if (node == null || !node.isString()) {
            return null;
        }
        String value = node.stringValue().trim();
        return value.isEmpty() ? null : value;
    }

    private static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    private static Integer integer(JsonNode node) {
        Double value = number(node);
        return value == null || value != Math.rint(value) ? null : (int) Math.round(value);
    }

    /** {@code value} rounded half-up to {@code places} decimals, or null. */
    static Double round(Double value, int places) {
        if (value == null) {
            return null;
        }
        double rounded = BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
        return rounded == 0.0 ? 0.0 : rounded;
    }
}
