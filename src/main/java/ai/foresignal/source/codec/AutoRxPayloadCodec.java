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
 *   <li>{@code frame}, {@code batt}, {@code sats} and {@code time} are not stored.
 * </ul>
 */
@Component
public class AutoRxPayloadCodec implements SocketCodec {

    /** The codec id. */
    public static final String ID = "sonde-udp";

    /** The datagram type that carries telemetry. */
    public static final String PAYLOAD_SUMMARY = "PAYLOAD_SUMMARY";

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
        return new Session();
    }

    /** One listener's counters; the codec keeps no other state. */
    public static final class Session implements CodecSession {

        private long skipped;
        private long ignored;

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
            NormalizedRecord record = summary(node);
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
        String serial = JsonProps.text(node, "callsign");
        Instant at = iso(JsonProps.text(node, "datetime"));
        if (serial == null || at == null) {
            return null;
        }
        Double speedKmh = JsonProps.number(node, "speed");
        return RadiosondeRecords.record(new RadiosondeRecords.Observation(serial, at,
                JsonProps.number(node, "latitude"), JsonProps.number(node, "longitude"),
                JsonProps.number(node, "altitude"), JsonProps.text(node, "model"), null, null, null,
                speedKmh == null ? null : speedKmh / 3.6, JsonProps.number(node, "heading"),
                JsonProps.number(node, "temp"), JsonProps.number(node, "humidity"), JsonProps.number(node, "pressure"),
                frequency(node.get("freq")), JsonProps.text(node, "station"), JsonProps.number(node, "snr"), null));
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
