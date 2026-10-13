package ai.foresignal.source.adapters.gdelt;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Parses one GDELT 2.0 event export into {@code conflict-event} records (prompt 066; pure).
 *
 * <p><b>Format:</b> tab-delimited despite the {@code .CSV} name, one event per line, 61 columns per the GDELT 2.0
 * Event Codebook (indices verified 2026-10 against a live export). A line with another column count is skipped and
 * counted as bad, as is a line whose GlobalEventID, Day or DATEADDED cannot be read; more than 5% bad lines means the
 * format changed, and the whole file is refused with an {@link UpstreamFormatException}. Lines longer than
 * {@value #MAX_LINE_BYTES} bytes are counted as bad without being buffered.
 *
 * <p><b>Filters:</b> the root code is one of {@link Options#rootCodes()}; {@code NumMentions ≥ minMentions}; with
 * {@code requireGeo}, a numeric in-range ActionGeo latitude and longitude and {@code ActionGeo_Type ≥ 1}; and, when
 * {@link Options#countries()} is not empty, an ActionGeo FIPS 10-4 country code in it.
 *
 * <p><b>Records:</b> {@code externalId = GlobalEventID}; {@code observedAt = DATEADDED} (UTC); {@code eventTime = Day}
 * at the start of the UTC day; geometry the ActionGeo point (null only when {@code requireGeo} is off and there is
 * none). Event codes stay strings, so leading zeros survive. Empty strings are left out of the payload.
 *
 * <p><b>Summary:</b> {@link #summary} is built only from codes and names, never from the article, and is the only
 * embedded field. {@code SOURCEURL} is stored as {@code sourceUrl} and never fetched.
 */
public final class GdeltExportParser {

    /** The record kind. */
    public static final String KIND = "conflict-event";

    /** Columns per line (Codebook V2.0). */
    public static final int COLUMNS = 61;

    /** The share of bad lines above which the file is refused. */
    public static final double MAX_BAD_SHARE = 0.05;

    /** The longest line read; GDELT lines are about 1 KB. */
    public static final int MAX_LINE_BYTES = 64 * 1024;

    /** The longest summary, in characters. */
    public static final int MAX_SUMMARY = 200;

    /** The longest {@code sourceUrl} kept, in characters. */
    public static final int MAX_SOURCE_URL = 2048;

    static final int GLOBAL_EVENT_ID = 0;
    static final int DAY = 1;
    static final int ACTOR1_NAME = 6;
    static final int ACTOR1_COUNTRY = 7;
    static final int ACTOR2_NAME = 16;
    static final int ACTOR2_COUNTRY = 17;
    static final int IS_ROOT_EVENT = 25;
    static final int EVENT_CODE = 26;
    static final int EVENT_BASE_CODE = 27;
    static final int EVENT_ROOT_CODE = 28;
    static final int QUAD_CLASS = 29;
    static final int GOLDSTEIN = 30;
    static final int NUM_MENTIONS = 31;
    static final int NUM_SOURCES = 32;
    static final int NUM_ARTICLES = 33;
    static final int AVG_TONE = 34;
    static final int ACTION_GEO_TYPE = 51;
    static final int ACTION_GEO_FULLNAME = 52;
    static final int ACTION_GEO_COUNTRY = 53;
    static final int ACTION_GEO_LAT = 56;
    static final int ACTION_GEO_LONG = 57;
    static final int ACTION_GEO_FEATURE_ID = 58;
    static final int DATE_ADDED = 59;
    static final int SOURCE_URL = 60;

    private static final Pattern DIGITS = Pattern.compile("^\\d{1,19}$");
    private static final Pattern FIPS = Pattern.compile("^[A-Z]{2}$");
    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter ADDED_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);

    private GdeltExportParser() {}

    /**
     * What to keep.
     *
     * @param rootCodes the CAMEO root codes kept, two digits each
     * @param minMentions the smallest {@code NumMentions} kept, at least 1
     * @param requireGeo true to keep only events with an ActionGeo point and {@code ActionGeo_Type ≥ 1}
     * @param countries the ActionGeo FIPS 10-4 country codes kept, uppercase; empty keeps every country
     */
    public record Options(Set<String> rootCodes, int minMentions, boolean requireGeo, Set<String> countries) {

        /** The default root codes, 13 (Threaten) to 20 (Use unconventional mass violence). */
        public static final List<String> DEFAULT_ROOT_CODES = List.of("13", "14", "15", "16", "17", "18", "19", "20");

        /** The defaults: roots 13–20, one mention, a point required, every country. */
        public static final Options DEFAULTS = new Options(Set.copyOf(DEFAULT_ROOT_CODES), 1, true, Set.of());

        /** Normalizes the codes: root codes padded to two digits, countries uppercased, and a minimum of 1. */
        public Options {
            Set<String> roots = new LinkedHashSet<>();
            for (String code : rootCodes == null || rootCodes.isEmpty() ? DEFAULT_ROOT_CODES : rootCodes) {
                String trimmed = code == null ? "" : code.strip();
                roots.add(trimmed.length() == 1 ? "0" + trimmed : trimmed);
            }
            rootCodes = Set.copyOf(roots);
            Set<String> fips = new LinkedHashSet<>();
            for (String code : countries == null ? List.<String>of() : countries) {
                if (code != null && !code.isBlank()) {
                    fips.add(code.strip().toUpperCase(Locale.ROOT));
                }
            }
            countries = Set.copyOf(fips);
            minMentions = Math.max(1, minMentions);
        }
    }

    /**
     * What one file produced.
     *
     * @param records the kept events
     * @param rows the non-empty lines read
     * @param bad lines with the wrong column count, an unreadable id or time, or over {@value #MAX_LINE_BYTES} bytes
     * @param filtered well-formed lines the filters left out
     */
    public record Result(List<NormalizedRecord> records, int rows, int bad, int filtered) {

        /** Copies the records. */
        public Result {
            records = List.copyOf(records);
        }
    }

    /**
     * Parses an export already in memory.
     *
     * @see #parse(InputStream, Options, Function)
     */
    public static Result parse(byte[] tsv, Options options, Function<String, String> fipsToIso) {
        return parse(new ByteArrayInputStream(tsv), options, fipsToIso);
    }

    /**
     * Parses an export as a stream, keeping only the events {@code options} lets through.
     *
     * @param tsv the export's lines (UTF-8); read to the end, not closed
     * @param options the filters
     * @param fipsToIso maps a FIPS 10-4 country code to ISO 3166-1 alpha-2, or returns null; null when there is no
     *     crosswalk, which leaves {@code actionGeoCountryIso} out
     * @return the records and the counts
     * @throws UpstreamFormatException when more than 5% of the lines are bad
     */
    public static Result parse(InputStream tsv, Options options, Function<String, String> fipsToIso) {
        Options opts = options == null ? Options.DEFAULTS : options;
        List<NormalizedRecord> records = new ArrayList<>();
        int rows = 0;
        int bad = 0;
        int filtered = 0;
        String firstBad = null;
        LineReader reader = new LineReader(tsv);
        String line;
        while ((line = reader.next()) != null) {
            if (line.isEmpty()) {
                continue;
            }
            rows++;
            if (line == LineReader.OVERLONG) {
                bad++;
                continue;
            }
            String[] c = line.split("\t", -1);
            Row row = c.length == COLUMNS ? Row.of(c) : null;
            if (row == null) {
                bad++;
                if (firstBad == null) {
                    firstBad = line;
                }
                continue;
            }
            NormalizedRecord record = toRecord(c, row, opts, fipsToIso);
            if (record == null) {
                filtered++;
            } else {
                records.add(record);
            }
        }
        if (rows > 0 && bad > rows * MAX_BAD_SHARE) {
            throw new UpstreamFormatException("GDELT export has " + bad + " malformed lines of " + rows
                    + " (more than 5%); the format may have changed", firstBad);
        }
        return new Result(records, rows, bad, filtered);
    }

    /**
     * The summary of one event: {@code "{actor1Name|Unknown actor} — {cameoRoot} — {actor2Name|} in
     * {actionGeoName}"}, with whitespace collapsed, trimmed, and cut to {@value #MAX_SUMMARY} characters (the last
     * one an ellipsis when cut). {@code " in {actionGeoName}"} is left out when there is no place name.
     *
     * @param actor1Name the first actor's name, or empty
     * @param cameoRoot the root label
     * @param actor2Name the second actor's name, or empty
     * @param actionGeoName the place name, or empty
     * @return the summary
     */
    public static String summary(String actor1Name, String cameoRoot, String actor2Name, String actionGeoName) {
        String actor1 = actor1Name == null || actor1Name.isBlank() ? "Unknown actor" : actor1Name;
        String actor2 = actor2Name == null ? "" : actor2Name;
        String place = actionGeoName == null || actionGeoName.isBlank() ? "" : " in " + actionGeoName;
        String text = WHITESPACE.matcher(actor1 + " — " + cameoRoot + " — " + actor2 + place).replaceAll(" ").strip();
        if (text.length() <= MAX_SUMMARY) {
            return text;
        }
        int end = MAX_SUMMARY - 1;
        if (Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end).stripTrailing() + "…";
    }

    /** The values every well-formed line must have. */
    private record Row(String id, Instant day, Instant added) {

        static Row of(String[] c) {
            String id = c[GLOBAL_EVENT_ID].strip();
            if (!DIGITS.matcher(id).matches()) {
                return null;
            }
            try {
                Instant day = LocalDate.parse(c[DAY].strip(), DAY_FORMAT).atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant added = LocalDateTime.parse(c[DATE_ADDED].strip(), ADDED_FORMAT).toInstant(ZoneOffset.UTC);
                return new Row(id, day, added);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }

    private static NormalizedRecord toRecord(String[] c, Row row, Options opts, Function<String, String> fipsToIso) {
        String rootCode = clean(c[EVENT_ROOT_CODE]);
        String cameoRoot = CameoRoots.label(rootCode);
        if (cameoRoot == null || !opts.rootCodes().contains(rootCode)) {
            return null;
        }
        Integer mentions = integer(c[NUM_MENTIONS]);
        if (mentions == null || mentions < opts.minMentions()) {
            return null;
        }
        String country = clean(c[ACTION_GEO_COUNTRY]).toUpperCase(Locale.ROOT);
        if (!opts.countries().isEmpty() && !opts.countries().contains(country)) {
            return null;
        }
        Integer geoType = integer(c[ACTION_GEO_TYPE]);
        Double lat = number(c[ACTION_GEO_LAT]);
        Double lon = number(c[ACTION_GEO_LONG]);
        boolean hasPoint = lat != null && lon != null && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
        if (opts.requireGeo() && (!hasPoint || geoType == null || geoType < 1)) {
            return null;
        }
        String precision = precision(geoType);

        String actor1Name = clean(c[ACTOR1_NAME]);
        String actor2Name = clean(c[ACTOR2_NAME]);
        String geoName = clean(c[ACTION_GEO_FULLNAME]);
        NormalizedRecord.Builder b = NormalizedRecord.builder(row.id(), KIND)
                .observedAt(row.added())
                .eventTime(row.day());
        if (hasPoint) {
            b.geometry(Geo.point(lon, lat));
        }
        putText(b, "eventCode", clean(c[EVENT_CODE]));
        putText(b, "eventBaseCode", clean(c[EVENT_BASE_CODE]));
        b.put("eventRootCode", rootCode);
        b.put("cameoRoot", cameoRoot);
        b.put("quadClass", integer(c[QUAD_CLASS]));
        b.put("goldstein", number(c[GOLDSTEIN]));
        b.put("numMentions", mentions);
        b.put("numSources", integer(c[NUM_SOURCES]));
        b.put("numArticles", integer(c[NUM_ARTICLES]));
        b.put("avgTone", round2(number(c[AVG_TONE])));
        putText(b, "actor1Name", actor1Name);
        putText(b, "actor1Country", clean(c[ACTOR1_COUNTRY]));
        putText(b, "actor2Name", actor2Name);
        putText(b, "actor2Country", clean(c[ACTOR2_COUNTRY]));
        putText(b, "actionGeoName", geoName);
        putText(b, "actionGeoCountry", country);
        if (fipsToIso != null && FIPS.matcher(country).matches()) {
            putText(b, "actionGeoCountryIso", fipsToIso.apply(country));
        }
        b.put("geoPrecision", precision);
        if (hasPoint && precision != null) {
            b.put("geometryApproximate", precision.equals("country") || precision.equals("adm1"));
        }
        b.put("isRoot", "1".equals(clean(c[IS_ROOT_EVENT])));
        String url = clean(c[SOURCE_URL]);
        if (url.length() <= MAX_SOURCE_URL) {
            putText(b, "sourceUrl", url);
        }
        b.put("summary", summary(actor1Name, cameoRoot, actor2Name, geoName));
        return b.build();
    }

    /** {@code country} for 1, {@code adm1} for 2 and 5, {@code city} for 3 and 4, else null. */
    static String precision(Integer geoType) {
        if (geoType == null) {
            return null;
        }
        return switch (geoType) {
            case 1 -> "country";
            case 2, 5 -> "adm1";
            case 3, 4 -> "city";
            default -> null;
        };
    }

    private static void putText(NormalizedRecord.Builder b, String key, String value) {
        if (value != null && !value.isEmpty()) {
            b.put(key, value);
        }
    }

    /** The field without control characters, whitespace collapsed and trimmed. */
    static String clean(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return WHITESPACE.matcher(CONTROL.matcher(value).replaceAll(" ")).replaceAll(" ").strip();
    }

    private static Integer integer(String value) {
        String v = value == null ? "" : value.strip();
        if (v.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double number(String value) {
        String v = value == null ? "" : value.strip();
        if (v.isEmpty()) {
            return null;
        }
        try {
            double d = Double.parseDouble(v);
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double round2(Double value) {
        return value == null ? null : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Reads {@code \n}-terminated UTF-8 lines without ever buffering more than {@value #MAX_LINE_BYTES} bytes: a
     * longer line is skipped to its end and returned as {@link #OVERLONG}.
     */
    static final class LineReader {

        /** Returned, by identity, for a line over the cap. */
        static final String OVERLONG = new String("\u0000overlong");

        private final InputStream in;
        private final byte[] buffer = new byte[8192];
        private int position;
        private int limit;
        private byte[] line = new byte[1024];

        LineReader(InputStream in) {
            this.in = in;
        }

        /** The next line without its terminator ({@code \r\n} or {@code \n}), or null at the end. */
        String next() {
            int length = 0;
            boolean overlong = false;
            boolean any = false;
            while (true) {
                if (position == limit && !fill()) {
                    if (!any) {
                        return null;
                    }
                    break;
                }
                any = true;
                byte b = buffer[position++];
                if (b == '\n') {
                    break;
                }
                if (overlong) {
                    continue;
                }
                if (length == MAX_LINE_BYTES) {
                    overlong = true;
                    continue;
                }
                if (length == line.length) {
                    line = Arrays.copyOf(line, Math.min(MAX_LINE_BYTES, line.length * 2));
                }
                line[length++] = b;
            }
            if (overlong) {
                return OVERLONG;
            }
            if (length > 0 && line[length - 1] == '\r') {
                length--;
            }
            return new String(line, 0, length, StandardCharsets.UTF_8);
        }

        private boolean fill() {
            try {
                int n = in.read(buffer, 0, buffer.length);
                if (n <= 0) {
                    return false;
                }
                position = 0;
                limit = n;
                return true;
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read the GDELT export", e);
            }
        }
    }
}
