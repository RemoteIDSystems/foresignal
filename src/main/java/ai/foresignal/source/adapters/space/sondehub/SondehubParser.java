package ai.foresignal.source.adapters.space.sondehub;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.formats.sonde.RadiosondeRecords;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * SondeHub v2 {@code /sondes} answers ({@code {serial: {...}}}, the latest telemetry of each sonde) as
 * {@code radiosonde} records (prompt 077), through the shared {@link RadiosondeRecords} contract. Pure.
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code serial} (else the object key); {@code observedAt = datetime}.
 *   <li><strong>Mapping:</strong> {@code alt} → {@code altitudeM}, {@code vel_v} → {@code verticalSpeedMs},
 *       {@code vel_h} → {@code horizontalSpeedMs}, {@code heading} → {@code headingDeg}, {@code temp} →
 *       {@code tempC}, {@code humidity} → {@code humidityPct}, {@code pressure} → {@code pressureHpa},
 *       {@code frequency} → {@code frequencyMHz}; {@code type, subtype, manufacturer} as given.
 *   <li><strong>Observer:</strong> {@code uploader_callsign} with {@code snr} and {@code rssi}.
 *   <li><strong>Never stored:</strong> {@code uploader_position} and {@code uploader_antenna} (receiver locations of
 *       private stations), {@code frame} and {@code batt} (counters and noise), and every other member.
 * </ul>
 */
public final class SondehubParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed answer.
     *
     * @param records one record per sonde
     * @param skipped skip reason to count
     */
    public record Parsed(List<NormalizedRecord> records, Map<String, Integer> skipped) {}

    private SondehubParser() {}

    /**
     * Parses an answer.
     *
     * @throws UpstreamFormatException when the body is not a JSON object
     */
    public static Parsed parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("SondeHub answer is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("SondeHub answer is not a JSON object keyed by serial", "");
        }
        List<NormalizedRecord> out = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        for (Map.Entry<String, JsonNode> entry : root.properties()) {
            MappedRecord m = sonde(entry.getKey(), entry.getValue());
            if (m.accepted()) {
                out.add(m.record());
            } else {
                skipped.merge(m.reason(), 1, Integer::sum);
            }
        }
        return new Parsed(out, skipped);
    }

    /** One sonde (see the class note). */
    public static MappedRecord sonde(String key, JsonNode s) {
        if (s == null || !s.isObject()) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        String serial = JsonProps.text(s, "serial");
        if (serial == null) {
            serial = key == null || key.isBlank() ? null : key.strip();
        }
        if (serial == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant at = SpaceSources.utc(JsonProps.text(s, "datetime"));
        if (at == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        return MappedRecord.of(RadiosondeRecords.record(new RadiosondeRecords.Observation(serial, at,
                JsonProps.number(s, "lat"), JsonProps.number(s, "lon"), JsonProps.number(s, "alt"),
                JsonProps.text(s, "type"), JsonProps.text(s, "subtype"), JsonProps.text(s, "manufacturer"),
                JsonProps.number(s, "vel_v"), JsonProps.number(s, "vel_h"), JsonProps.number(s, "heading"),
                JsonProps.number(s, "temp"), JsonProps.number(s, "humidity"), JsonProps.number(s, "pressure"),
                JsonProps.number(s, "frequency"), JsonProps.text(s, "uploader_callsign"), JsonProps.number(s, "snr"),
                JsonProps.number(s, "rssi"))));
    }
}
