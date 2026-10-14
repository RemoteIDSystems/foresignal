package ai.foresignal.source.codec;

import ai.foresignal.source.formats.sonde.RadiosondeRecords;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.support.JsonProps;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Codec {@value #ID} (prompt 077, the name prompts 069 and 086 reserve): radiosonde_auto_rx {@code PAYLOAD_SUMMARY}
 * datagrams as {@code radiosonde} records, through the shared {@link RadiosondeRecords} contract.
 *
 * <p>auto_rx broadcasts one JSON object per UDP datagram (its {@code station.cfg} defaults
 * {@code payload_summary_enabled = True}, {@code payload_summary_port = 55673}). Written from the JSON field list
 * only; auto_rx is GPL-3.0 and no code is copied.
 *
 * <ul>
 *   <li>Only objects with {@code type == "PAYLOAD_SUMMARY"} become records; other types are ignored (counted in
 *       {@link Session#ignored()}). Malformed JSON, a non-object, a summary without {@code callsign} or without a
 *       readable {@code datetime} is skipped and counted ({@link CodecSession#skipped()}).
 *   <li>{@code callsign} (the sonde serial) → {@code externalId}; {@code datetime} (ISO) → {@code observedAt};
 *       {@code latitude, longitude} → point; {@code altitude} → {@code altitudeM}; {@code speed} (km/h) →
 *       {@code horizontalSpeedMs}; {@code heading} → {@code headingDeg}; {@code model} → {@code type};
 *       {@code freq} ({@code 402.500 MHz} or a number) → {@code frequencyMHz}; {@code temp, humidity, pressure} →
 *       {@code tempC, humidityPct, pressureHpa}; {@code station} → the observer, with {@code snr}.
 *   <li>{@code frame}, {@code batt}, {@code sats}, {@code comment} and {@code sdr_device_idx} are not stored.
 *   <li>Prompt 086: a summary without {@code datetime} falls back to {@code time} ({@code HH:MM:SS} UTC), dated on
 *       the receive day nearest to it (so a summary just before midnight received just after it keeps its day); the
 *       session option {@value #STATION_CALLSIGN_PARAM} (the operator's station callsign) replaces auto_rx's
 *       {@code station} as the observer, and the observer's {@code frequencyHz} is {@code freq}.
 * </ul>
 */
@Component
public class AutoRxPayloadCodec implements SocketCodec {

    /** The codec id. */
    public static final String ID = "sonde-udp";

    /** The datagram type that carries telemetry. */
    public static final String PAYLOAD_SUMMARY = "PAYLOAD_SUMMARY";

    /** The session option naming the operator's station, the observer (prompt 086). */
    public static final String STATION_CALLSIGN_PARAM = "stationCallsign";

    private static final Pattern NUMBER = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Override
    public String id() {
        return ID;
    }

    /** One datagram is one frame. */
    @Override
    public Framing framing() {
        return Framing.RAW;
    }

    @Override
    public CodecSession newSession(CodecContext context) {
        String station = context == null ? null : context.params().string(STATION_CALLSIGN_PARAM);
        return new Session(station == null || station.isBlank() ? null : station.strip());
    }

    /** One listener's counters; the codec keeps no other state. */
    public static final class Session implements CodecSession {

        private final String stationCallsign;
        private long skipped;
        private long ignored;

        /** A session that keeps auto_rx's {@code station} as the observer. */
        Session() {
            this(null);
        }

        /**
         * A session.
         *
         * @param stationCallsign the observer that replaces auto_rx's {@code station}, or null
         */
        Session(String stationCallsign) {
            this.stationCallsign = stationCallsign;
        }

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
            if (!PAYLOAD_SUMMARY.equals(JsonProps.text(node, "type"))) {
                ignored++;
                return List.of();
            }
            NormalizedRecord record = summary(node, receivedAt, stationCallsign);
            if (record == null) {
                skipped++;
                return List.of();
            }
            return List.of(record);
        }

        @Override
        public long skipped() {
            return skipped;
        }

        /** Datagrams of other types since the session opened. */
        public long ignored() {
            return ignored;
        }
    }

    /** The record of one {@code PAYLOAD_SUMMARY} object, or null without a callsign or a readable datetime. */
    static NormalizedRecord summary(JsonNode node) {
        return summary(node, null, null);
    }

    /**
     * The record of one {@code PAYLOAD_SUMMARY} object (prompt 086 form), or null without a callsign or a readable
     * time.
     *
     * @param receivedAt the receive time, for a summary that carries only {@code time}; null to require
     *     {@code datetime}
     * @param stationCallsign the observer replacing auto_rx's {@code station}, or null
     */
    static NormalizedRecord summary(JsonNode node, Instant receivedAt, String stationCallsign) {
        String serial = JsonProps.text(node, "callsign");
        Instant at = iso(JsonProps.text(node, "datetime"));
        if (at == null && receivedAt != null) {
            at = timeOfDay(JsonProps.text(node, "time"), receivedAt);
        }
        if (serial == null || at == null) {
            return null;
        }
        Double speedKmh = JsonProps.number(node, "speed");
        Double mhz = frequency(node.get("freq"));
        Long hz = mhz == null || mhz <= 0 ? null : Math.round(mhz * 1_000_000d);
        String observer = stationCallsign != null ? stationCallsign : JsonProps.text(node, "station");
        return RadiosondeRecords.record(new RadiosondeRecords.Observation(serial, at,
                JsonProps.number(node, "latitude"), JsonProps.number(node, "longitude"),
                JsonProps.number(node, "altitude"), JsonProps.text(node, "model"), null, null, null,
                speedKmh == null ? null : speedKmh / 3.6, JsonProps.number(node, "heading"),
                JsonProps.number(node, "temp"), JsonProps.number(node, "humidity"), JsonProps.number(node, "pressure"),
                mhz, observer, JsonProps.number(node, "snr"), null), hz);
    }

    /**
     * {@code HH:MM:SS} (UTC) on the day of {@code receivedAt}, the day before or the day after, whichever is nearest
     * to {@code receivedAt}; null when unreadable.
     */
    static Instant timeOfDay(String text, Instant receivedAt) {
        if (text == null || receivedAt == null) {
            return null;
        }
        java.time.LocalTime time;
        try {
            time = java.time.LocalTime.parse(text.strip());
        } catch (DateTimeParseException e) {
            return null;
        }
        java.time.LocalDate day = receivedAt.atZone(java.time.ZoneOffset.UTC).toLocalDate();
        Instant best = null;
        for (int offset = -1; offset <= 1; offset++) {
            Instant candidate = day.plusDays(offset).atTime(time).toInstant(java.time.ZoneOffset.UTC);
            if (best == null || java.time.Duration.between(candidate, receivedAt).abs()
                    .compareTo(java.time.Duration.between(best, receivedAt).abs()) < 0) {
                best = candidate;
            }
        }
        return best;
    }

    /** {@code 402.500 MHz}, {@code "402.5"} or {@code 402.5} in MHz, or null. */
    static Double frequency(JsonNode freq) {
        if (freq == null || freq.isNull()) {
            return null;
        }
        if (freq.isNumber()) {
            return freq.doubleValue();
        }
        if (!freq.isString()) {
            return null;
        }
        Matcher m = NUMBER.matcher(freq.asString());
        return m.find() ? Double.valueOf(m.group()) : null;
    }

    /** An ISO-8601 time with an offset, or a local ISO time read as UTC; null when unreadable. */
    static Instant iso(String text) {
        if (text == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return java.time.LocalDateTime.parse(text).toInstant(java.time.ZoneOffset.UTC);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
