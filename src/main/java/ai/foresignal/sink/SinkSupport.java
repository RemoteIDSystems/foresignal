package ai.foresignal.sink;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Helpers shared by the JSON-shaped sink types (prompt 110). */
public final class SinkSupport {

    private SinkSupport() {}

    /** Sanitizes an MQTT topic segment to {@code [A-Za-z0-9_-]}. */
    public static String segment(String value) {
        return value == null ? "_" : value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    /** The {@code {kind, observedAt, validUntil, version, geometry, properties, attribution}} payload. */
    public static Map<String, Object> statePayload(SinkRecord record) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kind", record.kind());
        out.put("observedAt", record.observedAt() == null ? null : record.observedAt().toString());
        out.put("validUntil", record.validUntil() == null ? null : record.validUntil().toString());
        out.put("version", record.version());
        out.put("geometry", geometry(record));
        out.put("properties", record.properties() == null ? Map.of() : record.properties());
        out.put("attribution", record.attribution());
        return out;
    }

    /** The GeoJSON geometry of a record, or null. */
    public static Map<String, Object> geometry(SinkRecord record) {
        if (record.geometry() == null || record.geometry().isEmpty()) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "Point");
        out.put("coordinates", java.util.List.of(record.lon(), record.lat()));
        return out;
    }

    /** The aircraft hex (identity) of a record. */
    public static String hex(SinkRecord record) {
        String hex = record.string("hex");
        return hex == null ? record.externalId() : hex;
    }

    /** The readsb {@code type} from a {@code posSource}. */
    public static String readsbType(SinkRecord record) {
        String pos = record.string("posSource");
        if (pos == null) {
            return "other";
        }
        return switch (pos.toLowerCase(java.util.Locale.ROOT)) {
            case "adsb", "adsr", "adsc" -> "adsb_icao";
            case "mlat" -> "mlat";
            case "tisb", "tisb_icao" -> "tisb_icao";
            default -> "other";
        };
    }

    /** The MMSI of a vessel record, or null. */
    public static String mmsi(SinkRecord record) {
        String mmsi = record.string("mmsi");
        if (mmsi == null) {
            mmsi = record.externalId();
        }
        return mmsi != null && mmsi.matches("\\d{9}") ? mmsi : null;
    }

    /** A timestamp or null. */
    public static Instant time(SinkRecord record) {
        return record.observedAt() == null ? Instant.now() : record.observedAt();
    }
}
