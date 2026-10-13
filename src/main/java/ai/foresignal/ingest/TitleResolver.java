package ai.foresignal.ingest;

import java.util.List;
import java.util.Map;

/**
 * Resolves a record's title (Gap-Analysis §3 "GeoJSON {@code title}", stored as
 * {@code record_head.title}).
 *
 * <p>The descriptor's {@code titleField} value if present, else the first present of
 * {@code title}, {@code name}, {@code callsign}, {@code place}, {@code r}, {@code hex} and {@code mmsi} (prompt
 * 062: an aircraft without a callsign is titled by its registration, else its ICAO address; prompt 063: a vessel
 * without a name or call sign by its MMSI); stringified, trimmed and cut to
 * 256 characters; null when none yields text. The one resolver, reused by tiles (016), MCP (025)
 * and Ask (028). {@code kindDisplay.titleTemplate} is a client hint and is not used here.
 */
public final class TitleResolver {

    /** The longest title kept. */
    public static final int MAX_TITLE_LENGTH = 256;

    /** The fallback keys, in order. */
    public static final List<String> FALLBACK_FIELDS = List.of("title", "name", "callsign", "place", "r", "hex", "mmsi");

    private TitleResolver() {}

    /** The title of {@code payload} under {@code profile}'s {@code titleField}. */
    public static String resolve(SourceIngestProfile profile, Map<String, ?> payload) {
        return resolve(profile == null ? null : profile.titleField(), payload);
    }

    /** The title of {@code payload}, preferring {@code titleField} when it is set. */
    public static String resolve(String titleField, Map<String, ?> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        if (titleField != null) {
            String title = clean(payload.get(titleField));
            if (title != null) {
                return title;
            }
        }
        for (String field : FALLBACK_FIELDS) {
            String title = clean(payload.get(field));
            if (title != null) {
                return title;
            }
        }
        return null;
    }

    private static String clean(Object value) {
        String text = JsonValues.text(value);
        if (text == null) {
            return null;
        }
        String trimmed = text.strip();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() <= MAX_TITLE_LENGTH ? trimmed : trimmed.substring(0, MAX_TITLE_LENGTH).strip();
    }
}
