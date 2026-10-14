package ai.foresignal.source.adapters.seismic;

import ai.foresignal.source.spi.NormalizedRecord;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses the FDSN event {@code format=text} response (prompt 089): a comment header of
 * {@code #EventID|Time|Latitude|Longitude|Depth/km|Author|Catalog|Contributor|ContributorID|MagType|Magnitude|
 * MagAuthor|EventLocationName} (GEOFON and INGV append {@code EventType}), then one pipe-delimited row per event.
 *
 * <p>Columns are found by header name, case-insensitively, so the {@code Depth/km} vs {@code Depth/Km} spelling
 * difference does not matter. An empty field is null. The text format carries no update time, so {@code observedAt}
 * is left null and the source declares {@link ai.foresignal.source.spi.ObservedAtMode#RECEIVED}.
 */
public final class FdsnTextParser {

    /** The fields a row needs to become a record. */
    private static final List<String> REQUIRED = List.of("eventid", "time", "latitude", "longitude");

    private FdsnTextParser() {}

    /**
     * The records of one text response.
     *
     * @return the records; empty when the body has no rows
     */
    public static List<NormalizedRecord> parse(byte[] body) {
        String text = new String(body == null ? new byte[0] : body, StandardCharsets.UTF_8);
        List<NormalizedRecord> out = new ArrayList<>();
        Map<String, Integer> columns = null;
        for (String line : text.split("\r?\n")) {
            String trimmed = line.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("#")) {
                if (columns == null) {
                    columns = header(trimmed.substring(1));
                }
                continue;
            }
            if (columns == null) {
                continue;
            }
            NormalizedRecord record = row(trimmed, columns);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private static Map<String, Integer> header(String line) {
        Map<String, Integer> columns = new LinkedHashMap<>();
        String[] names = line.split("\\|", -1);
        for (int i = 0; i < names.length; i++) {
            columns.putIfAbsent(normalize(names[i]), i);
        }
        return columns;
    }

    private static NormalizedRecord row(String line, Map<String, Integer> columns) {
        String[] values = line.split("\\|", -1);
        for (String required : REQUIRED) {
            Integer index = columns.get(required);
            if (index == null || index >= values.length || values[index].isBlank()) {
                return null;
            }
        }
        String externalId = value(values, columns, "eventid");
        Instant time = parseTime(value(values, columns, "time"));
        if (externalId == null || time == null) {
            return null;
        }
        Double lat = number(value(values, columns, "latitude"));
        Double lon = number(value(values, columns, "longitude"));
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, SeismicRecords.KIND).eventTime(time);
        if (lat != null && lon != null) {
            b.point(lon, lat);
        }
        b.put("mag", number(value(values, columns, "magnitude")));
        b.put("magType", value(values, columns, "magtype"));
        b.put("place", value(values, columns, "eventlocationname"));
        b.put("depthKm", number(value(values, columns, "depth")));
        b.put("auth", value(values, columns, "author"));
        b.put("sourceCatalog", value(values, columns, "catalog"));
        b.put("evtype", value(values, columns, "eventtype"));
        return b.build();
    }

    private static String value(String[] values, Map<String, Integer> columns, String prefix) {
        for (Map.Entry<String, Integer> entry : columns.entrySet()) {
            if (entry.getKey().startsWith(prefix) && entry.getValue() < values.length) {
                String value = values[entry.getValue()].strip();
                return value.isEmpty() ? null : value;
            }
        }
        return null;
    }

    private static String normalize(String name) {
        return name.strip().toLowerCase(Locale.ROOT).replace("/", "").replace(" ", "").replace("-", "");
    }

    private static Double number(String value) {
        if (value == null || value.isBlank() || "--".equals(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** FDSN text times: an ISO offset, a zoned instant, or (GEOFON) a bare UTC date-time. */
    static Instant parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.strip();
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            return LocalDateTime.parse(text).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
