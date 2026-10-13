package ai.foresignal.source.codec;

import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.formats.readsb.ReadsbAircraftMapper;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * The SBS-1 (BaseStation) text format, codec {@code sbs1} (prompt 069): what readsb and dump1090 serve on
 * {@code --net-sbs-port} (conventionally 30003; readsb leaves it off unless the option is set).
 *
 * <p>One line per message, 22 comma-separated fields:
 * {@code MSG,type,session,aircraftId,hex,flightId,dateGen,timeGen,dateLog,timeLog,callsign,altitude,
 * groundSpeed,track,lat,lon,verticalRate,squawk,alert,emergency,spi,isOnGround}. Types: 1 identification,
 * 2 surface position, 3 airborne position, 4 velocity, 5 surveillance altitude, 6 surveillance id, 7
 * air-to-air, 8 all-call reply. A blank field means "not in this message".
 *
 * <ul>
 *   <li><strong>Merge (adapter rule 7):</strong> each message updates a per-hex snapshot, and every
 *       message emits that whole snapshot with the readsb mapper's payload keys: {@code hex, flight,
 *       squawk, emergency, onGround, alt_baro} (feet to meters), {@code gs} (knots), {@code track},
 *       {@code baro_rate} (ft/min to m/s) and {@code posSource: other} (readsb's name for data received
 *       as BaseStation, whose source is unknown). Alert and SPI bits are not part of that allowlist.
 *   <li><strong>Emergency:</strong> the flag alone does not say which emergency, so a set flag maps a 7500,
 *       7600 or 7700 squawk to {@code unlawful}, {@code nordo} or {@code general}, and anything else to
 *       {@code general}; a cleared flag removes it.
 *   <li><strong>Time:</strong> the message dates and times are the decoder's local wall clock with no zone,
 *       so they are never parsed. Records carry no {@code observedAt}; the source declares
 *       {@code observedAtMode} RECEIVED and the framework imputes it.
 *   <li><strong>Eviction:</strong> a hex unseen for {@link #EVICT_AFTER} is forgotten.
 *   <li><strong>Malformed lines:</strong> fewer than 22 fields, an unknown type, a bad hex or a bad number
 *       is skipped and counted. Lines that are not {@code MSG} ({@code SEL, ID, AIR, STA, CLK}) are
 *       ignored.
 * </ul>
 */
@Component
public class Sbs1Codec implements SocketCodec {

    /** The codec id. */
    public static final String ID = "sbs1";

    /** How long a hex stays in a session without a message. */
    public static final Duration EVICT_AFTER = Duration.ofMinutes(5);

    /** The fields of a BaseStation line. */
    public static final int FIELD_COUNT = 22;

    /** The {@code posSource} of every SBS record. */
    public static final String POS_SOURCE = "other";

    /** A hex: six hex digits, optionally after {@code ~} (a non-ICAO address). */
    static final Pattern HEX = Pattern.compile("^~?[0-9a-f]{6}$");

    static final int TYPE = 1;
    static final int HEX_FIELD = 4;
    static final int CALLSIGN = 10;
    static final int ALTITUDE = 11;
    static final int GROUND_SPEED = 12;
    static final int TRACK = 13;
    static final int LAT = 14;
    static final int LON = 15;
    static final int VERTICAL_RATE = 16;
    static final int SQUAWK = 17;
    static final int EMERGENCY = 19;
    static final int ON_GROUND = 21;

    /** How often a session looks for hexes to evict. */
    static final Duration EVICT_SCAN = Duration.ofSeconds(10);

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Framing framing() {
        return Framing.LINES;
    }

    @Override
    public CodecSession newSession(CodecContext context) {
        return new Session(context);
    }

    /** A malformed line. */
    static final class MalformedLine extends Exception {

        private static final long serialVersionUID = 1L;

        MalformedLine(String message) {
            super(message, null, false, false);
        }
    }

    /** One connection's per-hex snapshots. */
    static final class Session implements CodecSession {

        private final CodecContext context;
        private final Map<String, Snapshot> aircraft = new LinkedHashMap<>();
        private long skipped;
        private Instant lastScan;

        Session(CodecContext context) {
            this.context = context;
        }

        @Override
        public List<NormalizedRecord> onFrame(byte[] frame, Instant receivedAt) {
            evict(receivedAt);
            String line = new String(frame, StandardCharsets.US_ASCII).trim();
            if (!line.startsWith("MSG")) {
                return List.of();
            }
            try {
                return List.of(apply(line.split(",", -1), receivedAt));
            } catch (MalformedLine e) {
                skipped++;
                return List.of();
            }
        }

        @Override
        public long skipped() {
            return skipped;
        }

        /** The number of hexes held. */
        int size() {
            return aircraft.size();
        }

        private NormalizedRecord apply(String[] f, Instant receivedAt) throws MalformedLine {
            if (f.length < FIELD_COUNT) {
                throw new MalformedLine("expected " + FIELD_COUNT + " fields, got " + f.length);
            }
            int type = integer(f[TYPE], "type");
            if (type < 1 || type > 8) {
                throw new MalformedLine("unknown message type " + type);
            }
            String hex = f[HEX_FIELD].trim().toLowerCase(Locale.ROOT);
            if (!HEX.matcher(hex).matches()) {
                throw new MalformedLine("bad hex '" + hex + "'");
            }
            // Parse every field before touching the snapshot, so a bad line changes nothing.
            String callsign = blank(f[CALLSIGN]) ? null : f[CALLSIGN].trim();
            Double altitude = optionalNumber(f[ALTITUDE], "altitude");
            Double gs = optionalNumber(f[GROUND_SPEED], "groundSpeed");
            Double track = optionalNumber(f[TRACK], "track");
            Double lat = optionalNumber(f[LAT], "lat");
            Double lon = optionalNumber(f[LON], "lon");
            Double rate = optionalNumber(f[VERTICAL_RATE], "verticalRate");
            String squawk = blank(f[SQUAWK]) ? null : f[SQUAWK].trim();
            Boolean emergency = flag(f[EMERGENCY], "emergency");
            Boolean onGround = flag(f[ON_GROUND], "isOnGround");

            Snapshot s = aircraft.computeIfAbsent(hex, h -> new Snapshot());
            s.lastSeen = receivedAt;
            if (callsign != null && !callsign.isEmpty()) {
                s.flight = callsign;
            }
            if (squawk != null) {
                s.squawk = squawk;
            }
            if (type == 2) {
                s.onGround = true;
            }
            if (onGround != null) {
                s.onGround = onGround;
            }
            if (altitude != null) {
                s.altBaro = AircraftFields.feetToMeters(altitude);
            }
            if (gs != null) {
                s.gs = AircraftFields.tenths(gs);
            }
            if (track != null) {
                s.track = AircraftFields.tenths(track);
            }
            if (rate != null) {
                s.baroRate = AircraftFields.fpmToMps(rate);
            }
            if (ReadsbAircraftMapper.validPosition(lat, lon)) {
                s.lat = lat;
                s.lon = lon;
            }
            if (emergency != null) {
                s.emergency = emergency;
            }
            return s.record(hex, context.sourceId());
        }

        private void evict(Instant now) {
            if (lastScan != null && Duration.between(lastScan, now).compareTo(EVICT_SCAN) < 0) {
                return;
            }
            lastScan = now;
            Iterator<Snapshot> it = aircraft.values().iterator();
            while (it.hasNext()) {
                if (Duration.between(it.next().lastSeen, now).compareTo(EVICT_AFTER) > 0) {
                    it.remove();
                }
            }
        }
    }

    /** What is known about one hex. */
    static final class Snapshot {

        Instant lastSeen;
        String flight;
        String squawk;
        Boolean onGround;
        Integer altBaro;
        Double gs;
        Double track;
        Double baroRate;
        Double lat;
        Double lon;
        boolean emergency;

        NormalizedRecord record(String hex, String sourceId) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put(AircraftFields.HEX, hex);
            if (flight != null) {
                payload.put(AircraftFields.FLIGHT, flight);
            }
            if (squawk != null) {
                payload.put(AircraftFields.SQUAWK, squawk);
            }
            if (emergency) {
                payload.put(AircraftFields.EMERGENCY, emergencyFor(squawk));
            }
            if (onGround != null) {
                payload.put(AircraftFields.ON_GROUND, onGround);
            }
            if (altBaro != null && !Boolean.TRUE.equals(onGround)) {
                payload.put(AircraftFields.ALT_BARO, altBaro);
            }
            if (gs != null) {
                payload.put(AircraftFields.GROUND_SPEED, gs);
            }
            if (track != null) {
                payload.put(AircraftFields.TRACK, track);
            }
            if (baroRate != null) {
                payload.put(AircraftFields.BARO_RATE, baroRate);
            }
            payload.put(AircraftFields.POS_SOURCE, POS_SOURCE);
            NormalizedRecord.Builder builder = NormalizedRecord.builder(hex, AircraftFields.KIND)
                    .putAll(payload)
                    .quality(Quality.positionSource(PositionSource.UNKNOWN))
                    .observers(List.of(Observer.of(sourceId)));
            if (lat != null && lon != null) {
                builder.point(lon, lat);
            }
            return builder.build();
        }
    }

    /** The ADS-B emergency name for a set emergency flag. */
    static String emergencyFor(String squawk) {
        if (squawk == null) {
            return "general";
        }
        return switch (squawk) {
            case "7500" -> "unlawful";
            case "7600" -> "nordo";
            default -> "general";
        };
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static int integer(String value, String name) throws MalformedLine {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new MalformedLine("bad " + name + " '" + value + "'");
        }
    }

    private static Double optionalNumber(String value, String name) throws MalformedLine {
        if (blank(value)) {
            return null;
        }
        try {
            double d = Double.parseDouble(value.trim());
            if (!Double.isFinite(d)) {
                throw new MalformedLine("bad " + name + " '" + value + "'");
            }
            return d;
        } catch (NumberFormatException e) {
            throw new MalformedLine("bad " + name + " '" + value + "'");
        }
    }

    /** {@code -1} or {@code 1} true, {@code 0} false, blank absent. */
    private static Boolean flag(String value, String name) throws MalformedLine {
        if (blank(value)) {
            return null;
        }
        return switch (value.trim()) {
            case "-1", "1" -> Boolean.TRUE;
            case "0" -> Boolean.FALSE;
            default -> throw new MalformedLine("bad " + name + " flag '" + value + "'");
        };
    }
}
