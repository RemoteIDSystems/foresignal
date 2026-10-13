package ai.foresignal.source.adapters.firms;

// Origin: header-driven columns, the unpadded acq_time, isLikelyCsv and the VIIRS-versus-MODIS confidence, after
// God's Eye View (MIT, Copyright (c) 2026 Bilawal Sidhu): src/data/firmsCsv.js (isLikelyCsv, parseFirmsCsv,
// acquisitionMsUtc) and src/data/firmsAdapt.js (normalizeConfidence). See THIRD_PARTY_NOTICES.md.

import ai.foresignal.source.formats.csv.CsvReader;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Parses a FIRMS area-API CSV body into {@code fire-detection} records (prompt 065). Pure: no network, no clock.
 *
 * <p><b>Columns</b> are found by header name, never by position, because the published attribute order and the
 * live order are not confirmed to match. VIIRS files carry {@code bright_ti4, bright_ti5} and a confidence of
 * {@code l|n|h}; MODIS files carry {@code brightness, bright_t31}, a confidence of 0–100 and optionally
 * {@code type}. Only {@code latitude, longitude, acq_date, acq_time} are required.
 *
 * <p><b>Time:</b> {@code acq_time} is an unpadded UTC {@code HHMM}, so {@code "45"} is 00:45 and {@code "5"} is
 * 00:05; {@code observedAt = eventTime = acq_date + acq_time}.
 *
 * <p><b>Record:</b> {@code externalId} {@code {SOURCE}:{acq_date}:{HHMM}:{lat 4dp}:{lon 4dp}}, geometry
 * {@code Point(longitude, latitude)}. Payload {@code source, satellite, instrument} (from the product name:
 * {@code VIIRS}, {@code MODIS} or {@code OLI}), {@code frpMw} (the {@code frp} column, MW; named as in
 * {@code goes-fdc} and {@code hms-fire} so one rule reads all three), {@code brightK, bright2K, scanKm, trackKm,
 * daynight, version}, {@code confidence} (raw: a letter or an integer), {@code confidenceClass}
 * ({@code low|nominal|high}) and {@code type} (MODIS, when present). Rows with invalid coordinates are skipped
 * and counted apart from other malformed rows; a repeated id keeps its first row. No cross-satellite dedup: each
 * satellite's detection is an independent observation.
 */
public final class FirmsCsvParser {

    /** The record kind, shared with {@code goes-fdc} and {@code hms-fire}. */
    public static final String KIND = "fire-detection";

    /** Header fields that must all be present for a body to count as FIRMS CSV (lower case). */
    public static final List<String> REQUIRED_HEADER = List.of("latitude", "longitude", "acq_date", "acq_time");

    /** The text FIRMS answers a bad key with (verified 2026-10: {@code Invalid MAP_KEY.}, HTTP 400). */
    static final String INVALID_KEY_MARKER = "invalid map_key";

    /** At most this many characters are inspected to classify a body. */
    static final int SNIFF_CHARS = 4096;

    private static final Pattern TIME = Pattern.compile("^\\d{1,4}$");
    private static final Pattern INTEGER = Pattern.compile("^-?\\d+$");

    private FirmsCsvParser() {}

    /**
     * The parsed body.
     *
     * @param records one per distinct id whose acquisition fell inside the requested range
     * @param rows data rows seen
     * @param skipped malformed rows (bad date or time, unreadable number)
     * @param badCoordinates rows whose latitude or longitude was missing, not finite or out of range
     * @param outside readable rows left out because their acquisition was outside the requested range
     */
    public record Result(List<NormalizedRecord> records, int rows, int skipped, int badCoordinates, int outside) {}

    /**
     * True when {@code text} contains FIRMS's bad-key message, case-insensitively.
     *
     * @param text a body or an error message; null is false
     */
    public static boolean isInvalidKey(String text) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(INVALID_KEY_MARKER);
    }

    /**
     * The port of {@code isLikelyCsv}: the first non-whitespace character is not {@code <}, and the first line
     * names every {@link #REQUIRED_HEADER} field.
     *
     * @param text the start of a body; null is false
     */
    public static boolean isLikelyCsv(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.stripLeading();
        if (trimmed.startsWith("﻿")) {
            trimmed = trimmed.substring(1).stripLeading();
        }
        if (trimmed.isEmpty() || trimmed.charAt(0) == '<') {
            return false;
        }
        int end = trimmed.indexOf('\n');
        String header = (end < 0 ? trimmed : trimmed.substring(0, end)).strip().toLowerCase(Locale.ROOT);
        List<String> fields = new ArrayList<>();
        for (String field : header.split(",")) {
            fields.add(field.strip());
        }
        return fields.containsAll(REQUIRED_HEADER);
    }

    /**
     * The first {@value #SNIFF_CHARS} characters of {@code body}, for {@link #isLikelyCsv} and
     * {@link #isInvalidKey}.
     */
    public static String head(byte[] body) {
        int length = Math.min(body == null ? 0 : body.length, SNIFF_CHARS);
        return length == 0 ? "" : new String(body, 0, length, StandardCharsets.UTF_8);
    }

    /**
     * Parses every row.
     *
     * @param source the FIRMS product, for example {@code VIIRS_NOAA20_NRT}
     * @param text the body
     * @throws UpstreamFormatException when the body has no FIRMS header
     */
    public static Result parse(String source, String text) {
        return parse(source, new BufferedReader(new StringReader(text == null ? "" : text)), null, null);
    }

    /**
     * Parses the rows acquired in {@code [from, to]}, streaming from the bytes so a large world file is never
     * decoded into one string.
     *
     * @param source the FIRMS product
     * @param body the body, UTF-8
     * @param from the earliest acquisition kept, or null for no lower bound
     * @param to the latest acquisition kept, or null for no upper bound
     * @throws UpstreamFormatException when the body has no FIRMS header
     */
    public static Result parse(String source, byte[] body, Instant from, Instant to) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(body == null ? new byte[0] : body), StandardCharsets.UTF_8));
        return parse(source, reader, from, to);
    }

    private static Result parse(String source, BufferedReader reader, Instant from, Instant to) {
        CsvReader csv = new CsvReader(reader);
        List<String> header = csv.next();
        while (header != null && header.size() == 1 && header.get(0).isBlank()) {
            header = csv.next();
        }
        if (header == null) {
            throw new UpstreamFormatException("The FIRMS " + source + " response is empty", null);
        }
        Map<String, Integer> index = CsvReader.index(header);
        for (String column : REQUIRED_HEADER) {
            if (!index.containsKey(column.toUpperCase(Locale.ROOT))) {
                throw new UpstreamFormatException("The FIRMS " + source + " response has no '" + column + "' column",
                        String.join(",", header));
            }
        }
        Map<String, NormalizedRecord> records = new LinkedHashMap<>();
        int rows = 0;
        int skipped = 0;
        int badCoordinates = 0;
        int outside = 0;
        List<String> row;
        while ((row = nextRow(csv)) != null) {
            if (row.size() == 1 && row.get(0).isBlank()) {
                continue;
            }
            rows++;
            Double lat = number(CsvReader.get(row, index, "LATITUDE"));
            Double lon = number(CsvReader.get(row, index, "LONGITUDE"));
            if (!validCoordinates(lat, lon)) {
                badCoordinates++;
                continue;
            }
            String date = CsvReader.get(row, index, "ACQ_DATE");
            String time = CsvReader.get(row, index, "ACQ_TIME");
            Instant at = acquisition(date, time);
            if (at == null) {
                skipped++;
                continue;
            }
            if ((from != null && at.isBefore(from)) || (to != null && at.isAfter(to))) {
                outside++;
                continue;
            }
            try {
                NormalizedRecord record = record(source, row, index, lat, lon, date, time, at);
                records.putIfAbsent(record.externalId(), record);
            } catch (RuntimeException e) {
                skipped++;
            }
        }
        return new Result(List.copyOf(records.values()), rows, skipped, badCoordinates, outside);
    }

    /** The next row, or null at the end; a row the reader cannot finish ends the file. */
    private static List<String> nextRow(CsvReader csv) {
        try {
            return csv.next();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static NormalizedRecord record(String source, List<String> row, Map<String, Integer> index, double lat,
            double lon, String date, String time, Instant at) {
        String confidence = CsvReader.get(row, index, "CONFIDENCE");
        String type = CsvReader.get(row, index, "TYPE");
        return NormalizedRecord.builder(externalId(source, date, time, lat, lon), KIND)
                .observedAt(at)
                .eventTime(at)
                .point(lon, lat)
                .put("source", source)
                .put("satellite", CsvReader.get(row, index, "SATELLITE"))
                .put("instrument", instrument(source))
                .put("frpMw", number(CsvReader.get(row, index, "FRP")))
                .put("brightK", number(first(row, index, "BRIGHT_TI4", "BRIGHTNESS")))
                .put("bright2K", number(first(row, index, "BRIGHT_TI5", "BRIGHT_T31")))
                .put("scanKm", number(CsvReader.get(row, index, "SCAN")))
                .put("trackKm", number(CsvReader.get(row, index, "TRACK")))
                .put("daynight", CsvReader.get(row, index, "DAYNIGHT"))
                .put("version", CsvReader.get(row, index, "VERSION"))
                .put("confidence", rawConfidence(confidence))
                .put("confidenceClass", confidenceClass(confidence))
                .put("type", type != null && INTEGER.matcher(type).matches() ? Integer.valueOf(type) : null)
                .build();
    }

    /**
     * The stable id of one detection.
     *
     * @param source the FIRMS product
     * @param date {@code acq_date}, {@code yyyy-MM-dd}
     * @param time {@code acq_time}, unpadded {@code HHMM}
     */
    public static String externalId(String source, String date, String time, double lat, double lon) {
        return String.format(Locale.ROOT, "%s:%s:%s:%.4f:%.4f", source, date, pad(time), lat, lon);
    }

    /**
     * {@code acq_date} plus {@code acq_time} as a UTC instant, or null when either is unreadable.
     *
     * @param date {@code yyyy-MM-dd}
     * @param time unpadded {@code HHMM}: {@code "1006"} is 10:06, {@code "45"} is 00:45, {@code "0"} is 00:00
     */
    public static Instant acquisition(String date, String time) {
        if (date == null || time == null || !TIME.matcher(time.strip()).matches()) {
            return null;
        }
        String hhmm = pad(time);
        try {
            int hours = Integer.parseInt(hhmm.substring(0, 2));
            int minutes = Integer.parseInt(hhmm.substring(2, 4));
            return LocalDate.parse(date.strip()).atTime(LocalTime.of(hours, minutes)).toInstant(ZoneOffset.UTC);
        } catch (DateTimeException | NumberFormatException e) {
            return null;
        }
    }

    /**
     * The confidence class: VIIRS letters {@code l|n|h} (or the words) map directly; a MODIS integer is
     * {@code low} below 30, {@code nominal} from 30 to 79 and {@code high} from 80. Anything else is null.
     *
     * @param raw the {@code confidence} cell
     */
    public static String confidenceClass(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.strip().toLowerCase(Locale.ROOT);
        if (INTEGER.matcher(text).matches()) {
            int value = Integer.parseInt(text);
            if (value < 30) {
                return "low";
            }
            return value < 80 ? "nominal" : "high";
        }
        return switch (text) {
            case "l", "low" -> "low";
            case "n", "nominal" -> "nominal";
            case "h", "high" -> "high";
            default -> null;
        };
    }

    /**
     * The instrument of a FIRMS product: {@code VIIRS}, {@code MODIS} or {@code OLI} (Landsat). The attribute page
     * lists no {@code instrument} column, so it is derived from the product name; null for an unknown product.
     */
    public static String instrument(String source) {
        if (source == null) {
            return null;
        }
        String upper = source.toUpperCase(Locale.ROOT);
        if (upper.startsWith("VIIRS")) {
            return "VIIRS";
        }
        if (upper.startsWith("MODIS")) {
            return "MODIS";
        }
        return upper.startsWith("LANDSAT") ? "OLI" : null;
    }

    /** The raw confidence: an integer when the cell is one (MODIS), else the text (VIIRS letters). */
    static Object rawConfidence(String raw) {
        if (raw == null) {
            return null;
        }
        return INTEGER.matcher(raw).matches() ? Integer.valueOf(raw) : raw;
    }

    private static boolean validCoordinates(Double lat, Double lon) {
        return lat != null && lon != null && Double.isFinite(lat) && Double.isFinite(lon) && Math.abs(lat) <= 90
                && Math.abs(lon) <= 180;
    }

    private static String first(List<String> row, Map<String, Integer> index, String... columns) {
        for (String column : columns) {
            String value = CsvReader.get(row, index, column);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Double number(String text) {
        if (text == null) {
            return null;
        }
        try {
            double value = Double.parseDouble(text);
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String pad(String time) {
        String t = time.strip();
        return t.length() >= 4 ? t : "0".repeat(4 - t.length()) + t;
    }
}
