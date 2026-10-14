package ai.foresignal.source.adapters.maritime;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * Parses NGA's active broadcast warnings ({@code GET https://msi.nga.mil/api/publications/broadcast-warn?status=active
 * &output=json}, prompt 088) into {@code maritime-warning} records. Pure.
 *
 * <p>The document is {@code {"broadcast-warn":[{msgYear, msgNumber, navArea, subregion, text, status, issueDate,
 * authority, cancelDate, cancelNavArea, cancelMsgYear, cancelMsgNumber, number, year, area}]}} (verified 2026-10-14:
 * 386 warnings, every one {@code status} "A"). NAVAREA codes: {@code P} HYDROPAC, {@code A} HYDROLANT, {@code C}
 * HYDROARC, {@code 4} NAVAREA IV, {@code 12} NAVAREA XII.
 *
 * <p>Record: externalId {@code navArea/msgYear/msgNumber} ({@code 4/2024/517}), {@code observedAt} = {@code issueDate}
 * ({@code DDHHMMZ MON YYYY}, UTC), geometry from {@link MsiCoordinateExtractor}, payload {@code navArea, subregion,
 * msgYear, msgNumber, authority, title} (the first line of the text, at most {@value #TITLE_MAX} characters),
 * {@code text} (at most {@value #TEXT_MAX} characters, untrusted upstream text), {@code cancels} (the warnings the
 * text cancels, as externalIds) and {@code geometryFromText} (false when the text holds no usable position).
 */
public final class NgaMsiParser {

    /** The record kind. */
    public static final String KIND = "maritime-warning";

    /** The longest stored text, characters (the record string limit is 8 KB). */
    public static final int TEXT_MAX = 8000;

    /** The longest title, characters. */
    public static final int TITLE_MAX = 120;

    /** {@code 081653Z MAY 2024}. */
    static final DateTimeFormatter ISSUE_DATE = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendPattern("ddHHmm'Z' MMM yyyy").toFormatter(Locale.ENGLISH).withZone(ZoneOffset.UTC);

    /** The NAVAREA name in a cancel line to its {@code navArea} code. */
    static final Map<String, String> AREA_CODES = Map.of("HYDROPAC", "P", "HYDROLANT", "A", "HYDROARC", "C",
            "NAVAREA IV", "4", "NAVAREA XII", "12");

    /** {@code CANCEL HYDROLANT 927/24, 928/24} (the list continues across line breaks). */
    static final Pattern CANCEL = Pattern.compile("CANCEL\\s+(HYDROPAC|HYDROLANT|HYDROARC|NAVAREA\\s+XII|NAVAREA\\s+IV)"
            + "\\s+((?:\\d{1,4}/\\d{2,4}(?:\\(\\d+\\))?(?:\\s*,\\s*|\\s+AND\\s+(?=\\d))?)+)");

    /** One number of a cancel list. */
    static final Pattern NUMBER = Pattern.compile("(\\d{1,4})/(\\d{2,4})");

    /** The id characters allowed. */
    static final Pattern ID_PART = Pattern.compile("^[0-9A-Za-z]{1,8}$");

    /**
     * What one document gave.
     *
     * @param records the warnings
     * @param ids every externalId of the document, for the absence tombstones
     * @param skipped entries without a usable id or issue date
     * @param withoutGeometry warnings whose text held no usable position
     */
    public record Result(List<NormalizedRecord> records, List<String> ids, int skipped, int withoutGeometry) {

        /** Copies the lists. */
        public Result {
            records = List.copyOf(records);
            ids = List.copyOf(ids);
        }
    }

    private NgaMsiParser() {}

    /**
     * Parses one document.
     *
     * @param root the parsed JSON
     * @return the records
     * @throws UpstreamFormatException when the document has no {@code broadcast-warn} array
     */
    public static Result parse(JsonNode root) {
        JsonNode list = root == null ? null : root.get("broadcast-warn");
        if (list == null || !list.isArray()) {
            throw new UpstreamFormatException("NGA broadcast warnings have no broadcast-warn array", "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Set<String> ids = new LinkedHashSet<>();
        int skipped = 0;
        int withoutGeometry = 0;
        for (JsonNode w : list) {
            String id = externalId(w);
            Instant issued = issueDate(MaritimeSources.text(w.get("issueDate")));
            if (id == null || issued == null || !ids.add(id)) {
                skipped++;
                continue;
            }
            String text = MaritimeSources.text(w.get("text"));
            MsiCoordinateExtractor.Result geometry = MsiCoordinateExtractor.extract(text);
            if (geometry.geometry() == null) {
                withoutGeometry++;
            }
            List<String> cancels = cancels(text);
            records.add(NormalizedRecord.builder(id, KIND)
                    .observedAt(issued)
                    .geometry(geometry.geometry())
                    .put("navArea", MaritimeSources.text(w.get("navArea")))
                    .put("subregion", MaritimeSources.text(w.get("subregion")))
                    .put("msgYear", MaritimeSources.integer(w.get("msgYear")))
                    .put("msgNumber", MaritimeSources.integer(w.get("msgNumber")))
                    .put("authority", MaritimeSources.text(w.get("authority")))
                    .put("title", title(text))
                    .put("text", text == null ? null : cap(text, TEXT_MAX))
                    .put("cancels", cancels.isEmpty() ? null : cancels)
                    .put("geometryFromText", geometry.geometry() != null)
                    .build());
        }
        return new Result(records, List.copyOf(ids), skipped, withoutGeometry);
    }

    /** {@code navArea/msgYear/msgNumber}, or null when a part is missing or odd. */
    static String externalId(JsonNode w) {
        String area = MaritimeSources.text(w.get("navArea"));
        Long year = MaritimeSources.integer(w.get("msgYear"));
        Long number = MaritimeSources.integer(w.get("msgNumber"));
        if (area == null || year == null || number == null || !ID_PART.matcher(area).matches() || year < 1900
                || number < 0) {
            return null;
        }
        return area + "/" + year + "/" + number;
    }

    /**
     * The issue date {@code DDHHMMZ MON YYYY} as UTC.
     *
     * @param text the value, possibly null
     * @return the instant, or null when it does not parse
     */
    public static Instant issueDate(String text) {
        if (text == null) {
            return null;
        }
        try {
            return ZonedDateTime.parse(text.strip(), ISSUE_DATE).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** The first non-blank line of {@code text}, at most {@value #TITLE_MAX} characters, or null. */
    static String title(String text) {
        if (text == null) {
            return null;
        }
        for (String line : text.split("\\R")) {
            if (!line.isBlank()) {
                return cap(line.strip(), TITLE_MAX);
            }
        }
        return null;
    }

    /**
     * The warnings a text cancels, as externalIds ({@code CANCEL HYDROLANT 927/24} → {@code A/2024/927}); "CANCEL THIS
     * MSG" is not a reference.
     *
     * @param text the warning text, possibly null
     * @return the ids, in order, without repeats
     */
    static List<String> cancels(String text) {
        if (text == null) {
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        Matcher m = CANCEL.matcher(text.toUpperCase(Locale.ROOT));
        while (m.find()) {
            String code = AREA_CODES.get(m.group(1).replaceAll("\\s+", " "));
            Matcher n = NUMBER.matcher(m.group(2));
            while (n.find()) {
                int year = Integer.parseInt(n.group(2));
                if (year < 100) {
                    year += 2000;
                }
                out.add(code + "/" + year + "/" + Integer.parseInt(n.group(1)));
            }
        }
        return List.copyOf(out);
    }

    private static String cap(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }
}
