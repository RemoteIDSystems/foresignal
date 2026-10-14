package ai.foresignal.source.formats.acars;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * ACARS, VDL Mode 2 and HFDL messages as {@value #KIND} metadata records (prompt 086), pure and allowlist-only.
 *
 * <p><strong>Message text is never read into the record.</strong> Interception-disclosure laws (US 18 U.S.C. §2511, the
 * UK Wireless Telegraphy Act 2006 s.48 and their equivalents) make redistributing the content of these messages
 * unlawful, so the mapper copies only the {@link #PAYLOAD_FIELDS} allowlist out of the decoder's JSON. {@code text}
 * and {@code msg_text} are only tested for presence ({@code hasText}); their values are never copied, stored, logged,
 * embedded or forwarded, and nothing enables them.
 *
 * <p>Three decoder shapes, recognized by their top-level object (acars_router passes each decoder's JSON through):
 * <ul>
 *   <li><strong>acarsdec</strong>: top-level {@code timestamp} (epoch seconds), {@code station_id}, {@code channel},
 *       {@code freq} (MHz), {@code level} (dB), {@code error}, {@code mode}, {@code label}, {@code block_id},
 *       {@code ack}, {@code tail}, {@code flight}, {@code msgno}, {@code text};
 *   <li><strong>dumpvdl2</strong>: {@code vdl2.t.sec/usec}, {@code vdl2.freq} (Hz), {@code vdl2.sig_level} (dBFS),
 *       {@code vdl2.avlc.src.addr} (the aircraft's ICAO address when {@code src.type} is an aircraft, else the
 *       ground station), {@code vdl2.avlc.acars.{reg, flight, label, mode, blk_id, msg_num, msg_text}};
 *   <li><strong>dumphfdl</strong>: {@code hfdl.t.sec/usec}, {@code hfdl.freq} (Hz), {@code hfdl.sig_level},
 *       {@code hfdl.lpdu.src}/{@code dst} (aircraft {@code ac_info.icao}, ground station {@code id}),
 *       {@code hfdl.lpdu.hfnpdu.acars…} and the decoded position report {@code hfdl.lpdu.hfnpdu.pos.{lat, lon}}.
 * </ul>
 *
 * <p>Geometry only from a decoded HFDL position report; every other record has none. {@code externalId} =
 * {@code protocol|station|epochMillis|tail|msgNum}; {@code observedAt} and {@code eventTime} are the decoder's time.
 */
public final class AcarsMetadataMapper {

    /** The record kind. */
    public static final String KIND = "acars-message";

    /** The only payload keys a record can carry. */
    public static final List<String> PAYLOAD_FIELDS = List.of("protocol", "tail", "flight", "label", "mode", "blockId",
            "msgNum", "freqMhz", "signalDbm", "icaoHex", "groundStation", "hasText");

    /** acarsdec's protocol name. */
    public static final String ACARS = "acars";

    /** dumpvdl2's protocol name. */
    public static final String VDL2 = "vdl2";

    /** dumphfdl's protocol name. */
    public static final String HFDL = "hfdl";

    private AcarsMetadataMapper() {}

    /**
     * The metadata record of one decoder message, or null when it has no usable time or is not one of the three
     * shapes.
     *
     * @param message one decoder JSON object
     * @param stationId the operator's station (the observer), or null to use the decoder's own station id
     */
    public static NormalizedRecord map(JsonNode message, String stationId) {
        if (message == null || !message.isObject()) {
            return null;
        }
        Fields f;
        if (message.get("vdl2") instanceof JsonNode vdl2 && vdl2.isObject()) {
            f = vdl2(vdl2);
        } else if (message.get("hfdl") instanceof JsonNode hfdl && hfdl.isObject()) {
            f = hfdl(hfdl);
        } else if (message.has("timestamp")) {
            f = acarsdec(message);
        } else {
            return null;
        }
        if (f == null || f.time == null) {
            return null;
        }
        String station = blank(stationId) != null ? stationId.strip() : f.decoderStation;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("protocol", f.protocol);
        put(payload, "tail", f.tail);
        put(payload, "flight", f.flight);
        put(payload, "label", f.label);
        put(payload, "mode", f.mode);
        put(payload, "blockId", f.blockId);
        put(payload, "msgNum", f.msgNum);
        put(payload, "freqMhz", f.freqMhz == null ? null : round(f.freqMhz, 4));
        put(payload, "signalDbm", f.signal == null ? null : round(f.signal, 1));
        put(payload, "icaoHex", f.icaoHex);
        put(payload, "groundStation", f.groundStation);
        payload.put("hasText", f.hasText);
        String externalId = f.protocol + "|" + (station == null ? "" : station) + "|" + f.time.toEpochMilli() + "|"
                + (f.tail == null ? "" : f.tail) + "|" + (f.msgNum == null ? "" : f.msgNum);
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, KIND)
                .observedAt(f.time)
                .eventTime(f.time)
                .putAll(payload);
        if (f.lat != null && f.lon != null) {
            b.point(f.lon, f.lat);
        }
        if (station != null) {
            Long hz = f.freqMhz == null ? null : Math.round(f.freqMhz * 1_000_000d);
            b.observers(List.of(new Observer(station, hz, f.signal, null, null)));
        }
        return b.build();
    }

    /** What the three shapes have in common, before the payload is built. */
    private static final class Fields {
        String protocol;
        Instant time;
        String decoderStation;
        String tail;
        String flight;
        String label;
        String mode;
        String blockId;
        String msgNum;
        Double freqMhz;
        Double signal;
        String icaoHex;
        String groundStation;
        boolean hasText;
        Double lat;
        Double lon;
    }

    private static Fields acarsdec(JsonNode m) {
        Fields f = new Fields();
        f.protocol = ACARS;
        Double ts = number(m.get("timestamp"));
        f.time = ts == null ? null : epoch(ts);
        f.decoderStation = text(m.get("station_id"));
        f.tail = tail(text(m.get("tail")));
        f.flight = text(m.get("flight"));
        f.label = text(m.get("label"));
        f.mode = text(m.get("mode"));
        f.blockId = text(m.get("block_id"));
        f.msgNum = text(m.get("msgno"));
        f.freqMhz = number(m.get("freq"));
        f.signal = number(m.get("level"));
        f.hasText = present(m.get("text"));
        return f;
    }

    private static Fields vdl2(JsonNode v) {
        Fields f = new Fields();
        f.protocol = VDL2;
        f.time = secUsec(v.get("t"));
        f.decoderStation = text(v.get("station"));
        Double hz = number(v.get("freq"));
        f.freqMhz = hz == null ? null : hz / 1_000_000d;
        f.signal = number(v.get("sig_level"));
        JsonNode avlc = v.path("avlc");
        JsonNode src = avlc.path("src");
        JsonNode dst = avlc.path("dst");
        if (isAircraft(src)) {
            f.icaoHex = hex(text(src.get("addr")));
            f.groundStation = hex(text(dst.get("addr")));
        } else if (src.isObject()) {
            f.groundStation = hex(text(src.get("addr")));
            f.icaoHex = isAircraft(dst) ? hex(text(dst.get("addr"))) : null;
        }
        acars(f, avlc.path("acars"));
        return f;
    }

    private static Fields hfdl(JsonNode h) {
        Fields f = new Fields();
        f.protocol = HFDL;
        f.time = secUsec(h.get("t"));
        f.decoderStation = text(h.get("station"));
        Double hz = number(h.get("freq"));
        f.freqMhz = hz == null ? null : hz / 1_000_000d;
        f.signal = number(h.get("sig_level"));
        JsonNode lpdu = h.path("lpdu");
        JsonNode src = lpdu.path("src");
        JsonNode dst = lpdu.path("dst");
        JsonNode aircraft = isAircraft(src) ? src : isAircraft(dst) ? dst : null;
        JsonNode ground = isAircraft(src) ? dst : src;
        if (aircraft != null) {
            f.icaoHex = hex(text(aircraft.path("ac_info").get("icao")));
        }
        if (ground != null && ground.isObject() && !isAircraft(ground)) {
            String id = text(ground.get("id"));
            if (id == null && ground.get("id") != null && ground.get("id").isNumber()) {
                id = Integer.toString(ground.get("id").intValue());
            }
            f.groundStation = id;
        }
        JsonNode hfnpdu = lpdu.path("hfnpdu");
        acars(f, hfnpdu.path("acars"));
        JsonNode pos = hfnpdu.path("pos");
        Double lat = number(pos.get("lat"));
        Double lon = number(pos.get("lon"));
        if (lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180 && !(lat == 0 && lon == 0)) {
            f.lat = round(lat, 5);
            f.lon = round(lon, 5);
        }
        return f;
    }

    /** The ACARS block inside a VDL2 or HFDL frame; {@code msg_text} is only tested for presence. */
    private static void acars(Fields f, JsonNode a) {
        if (a == null || !a.isObject()) {
            return;
        }
        f.tail = tail(text(a.get("reg")));
        f.flight = text(a.get("flight"));
        f.label = text(a.get("label"));
        f.mode = text(a.get("mode"));
        f.blockId = text(a.get("blk_id"));
        f.msgNum = text(a.get("msg_num"));
        f.hasText = present(a.get("msg_text"));
    }

    private static boolean isAircraft(JsonNode endpoint) {
        String type = endpoint == null ? null : text(endpoint.get("type"));
        return type != null && type.toLowerCase(Locale.ROOT).startsWith("aircraft");
    }

    /** {@code {sec, usec}} as an instant, or null. */
    static Instant secUsec(JsonNode t) {
        if (t == null || !t.isObject()) {
            return null;
        }
        Double sec = number(t.get("sec"));
        if (sec == null) {
            return null;
        }
        Double usec = number(t.get("usec"));
        return Instant.ofEpochSecond(sec.longValue(), usec == null ? 0 : (long) (usec * 1_000));
    }

    private static Instant epoch(double seconds) {
        long whole = (long) Math.floor(seconds);
        long nanos = Math.round((seconds - whole) * 1_000_000d) * 1_000L;
        return Instant.ofEpochSecond(whole, nanos);
    }

    /** A registration without the leading dots acarsdec and dumpvdl2 pad it with. */
    static String tail(String reg) {
        if (reg == null) {
            return null;
        }
        String t = reg.replaceFirst("^\\.+", "").strip();
        return t.isEmpty() ? null : t;
    }

    private static String hex(String addr) {
        if (addr == null) {
            return null;
        }
        String h = addr.strip().toUpperCase(Locale.ROOT);
        return h.matches("[0-9A-F]{1,6}") ? h : null;
    }

    private static boolean present(JsonNode node) {
        return node != null && node.isString() && !node.stringValue().isBlank();
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.isIntegralNumber() ? Long.toString(node.longValue()) : null;
        }
        if (!node.isString()) {
            return null;
        }
        return blank(node.stringValue());
    }

    private static String blank(String s) {
        if (s == null) {
            return null;
        }
        String t = s.strip();
        return t.isEmpty() ? null : (t.length() > 32 ? t.substring(0, 32) : t);
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
