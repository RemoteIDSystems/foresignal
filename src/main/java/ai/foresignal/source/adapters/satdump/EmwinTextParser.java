package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.adapters.satdump.SatDumpFiles.FormatException;
import ai.foresignal.source.spi.Geo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * The EMWIN text parser (prompt 107): the WMO heading, the AWIPS id, the P-VTEC line, a
 * {@code LAT...LON} polygon and {@code TIME...MOT...LOC} from a product body, plain or ZIP-wrapped.
 *
 * <p>ZIP guards: at most {@value #MAX_ZIP_ENTRIES} entries, at most {@value #MAX_PLAIN_BYTES} plain
 * bytes, and any entry name that is absolute or contains {@code ..} is refused. The text is untrusted:
 * control characters are stripped and the body is capped at {@value #MAX_TEXT_BYTES} bytes.
 */
public final class EmwinTextParser {

    /** The most ZIP entries read. */
    public static final int MAX_ZIP_ENTRIES = 16;

    /** The largest inflated body, bytes (2 MB). */
    public static final int MAX_PLAIN_BYTES = 2 * 1024 * 1024;

    /** The largest kept text, bytes (16 KB). */
    public static final int MAX_TEXT_BYTES = 16 * 1024;

    private static final Pattern WMO = Pattern.compile("^([A-Z]{4}\\d{2}) ([A-Z]{4}) (\\d{6})(?: ([A-Z]{3}))?$");
    private static final Pattern AWIPS = Pattern.compile("^([A-Z]{3})([A-Z]{3})(\\d*)$");
    private static final Pattern VTEC = Pattern.compile(
            "/([OTEX])\\.([A-Z]{3})\\.([A-Z]{4})\\.([A-Z]{2})\\.([A-Z])\\.(\\d{4})\\.(\\d{6})T(\\d{4})Z-(\\d{6})T(\\d{4})Z/");
    private static final Pattern LATLON = Pattern.compile("LAT\\.\\.\\.LON\\s+((?:\\d{4,7}\\s+){3,}\\d{4,7})");
    private static final Pattern TIME_MOT_LOC =
            Pattern.compile("TIME\\.\\.\\.MOT\\.\\.\\.LOC\\s+(\\d{4})Z\\s+(\\d{1,3})DEG\\s+(\\d+)KT");

    private EmwinTextParser() {}

    /**
     * A parsed product.
     *
     * @param wmoHeading the WMO abbreviated heading, or null
     * @param awipsId the AWIPS id, or null
     * @param office the issuing office (from the AWIPS id or VTEC), or null
     * @param category the AWIPS product category (first three letters of the id), or null
     * @param text the body, control-stripped and capped
     * @param warning the warning, when a P-VTEC and polygon are present
     */
    public record Parsed(String wmoHeading, String awipsId, String office, String category, String text, Warning warning) {}

    /**
     * A parsed warning.
     *
     * @param action the VTEC action ({@code NEW}, {@code CON}, {@code EXT}, {@code CAN}, {@code EXP}, …)
     * @param phenomena the two-letter phenomena code
     * @param significance the significance code ({@code W}, {@code A}, {@code Y}, …)
     * @param office the four-letter office
     * @param etn the event tracking number (four digits)
     * @param validFrom the VTEC start
     * @param validUntil the VTEC end
     * @param headline the first non-empty line, capped
     * @param polygon the {@code LAT...LON} polygon, or null
     */
    public record Warning(String action, String phenomena, String significance, String office, String etn,
            Instant validFrom, Instant validUntil, String headline, Geometry polygon) {

        /** The stable externalId {@code {office}.{phen}.{sig}.{etn}}, so an update versions the record. */
        public String externalId() {
            return office + "." + phenomena + "." + significance + "." + etn;
        }

        /**
         * The event tracking number as the integer {@code EmwinWarningPayload.etn} declares ({@code 42} for
         * {@code 0042}); the externalId keeps the four-digit VTEC form.
         *
         * @return the event tracking number
         */
        public int etnNumber() {
            return Integer.parseInt(etn);
        }

        /** True for a cancellation or an expiry (the record is tombstoned). */
        public boolean canceled() {
            return "CAN".equals(action) || "EXP".equals(action);
        }
    }

    /** Parses a body, plain or ZIP-wrapped. */
    public static Parsed parse(byte[] body) throws FormatException {
        byte[] plain = unwrap(body);
        String text = cap(new String(plain, StandardCharsets.UTF_8));
        String[] lines = text.split("\\R");
        String wmoHeading = null;
        String awipsId = null;
        String office = null;
        String category = null;
        for (String raw : lines) {
            String line = raw.strip();
            if (line.isEmpty()) {
                continue;
            }
            Matcher wmo = WMO.matcher(line);
            if (wmo.matches() && wmoHeading == null) {
                wmoHeading = line;
                office = wmo.group(2);
                continue;
            }
            Matcher awips = AWIPS.matcher(line);
            if (awips.matches() && awipsId == null) {
                awipsId = line;
                category = awips.group(1);
                office = awips.group(2);
                break;
            }
        }
        Warning warning = warning(text, office);
        return new Parsed(wmoHeading, awipsId, office, category, text, warning);
    }

    /** The warning of a body, or null when it has no P-VTEC with a polygon. */
    static Warning warning(String text, String defaultOffice) {
        Matcher vtec = VTEC.matcher(text);
        if (!vtec.find()) {
            return null;
        }
        String action = vtec.group(2);
        String office = vtec.group(3);
        String phenomena = vtec.group(4);
        String significance = vtec.group(5);
        String etn = vtec.group(6);
        Instant from = vtecTime(vtec.group(7), vtec.group(8));
        Instant until = vtecTime(vtec.group(9), vtec.group(10));
        Geometry polygon = polygon(text);
        String headline = headline(text);
        if (from != null && until != null && until.isBefore(from)) {
            return null;
        }
        return new Warning(action, phenomena, significance, office, etn, from, until, headline, polygon);
    }

    /**
     * The {@code LAT...LON} polygon, or null.
     *
     * <p>NWS lists the vertices as latitude-longitude pairs, latitude first, both in hundredths of a
     * degree. Longitudes are west and written positive, so they are negated; a longitude of 100° or more
     * is written with five digits ({@code 10123} = 101.23° W), one below 100° with four or a zero-padded
     * five ({@code 9730} or {@code 09730} = 97.30° W).
     */
    static Geometry polygon(String text) {
        Matcher m = LATLON.matcher(text);
        if (!m.find()) {
            return null;
        }
        String[] numbers = m.group(1).strip().split("\\s+");
        if (numbers.length < 6 || numbers.length % 2 != 0) {
            return null;
        }
        List<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i + 1 < numbers.length; i += 2) {
            double lat = parseHundredths(numbers[i]);
            double lon = -parseHundredths(numbers[i + 1]);
            if (Double.isNaN(lon) || Double.isNaN(lat) || lat > 90 || lon < -180) {
                return null;
            }
            coords.add(new Coordinate(lon, lat));
        }
        if (coords.size() < 3) {
            return null;
        }
        coords.add(new Coordinate(coords.get(0)));
        try {
            return Geo.factory().createPolygon(coords.toArray(Coordinate[]::new));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static double parseHundredths(String s) {
        try {
            return Long.parseLong(s) / 100.0;
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /** Unwraps a ZIP body with the guards, or returns the body unchanged. */
    static byte[] unwrap(byte[] body) throws FormatException {
        if (body == null || body.length < 4 || body[0] != 'P' || body[1] != 'K') {
            return body == null ? new byte[0] : body;
        }
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(body))) {
            ZipEntry entry = zip.getNextEntry();
            if (entry == null) {
                throw new FormatException("EMWIN ZIP has no entry");
            }
            if (entry.getName().startsWith("/") || entry.getName().contains("..")) {
                throw new FormatException("EMWIN ZIP entry name is unsafe: " + entry.getName());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int entries = 0;
            int read;
            while (entry != null) {
                entries++;
                if (entries > MAX_ZIP_ENTRIES) {
                    throw new FormatException("EMWIN ZIP has more than " + MAX_ZIP_ENTRIES + " entries");
                }
                while ((read = zip.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                    if (out.size() > MAX_PLAIN_BYTES) {
                        throw new FormatException("EMWIN ZIP inflates over " + MAX_PLAIN_BYTES + " bytes");
                    }
                }
                zip.closeEntry();
                entry = zip.getNextEntry();
            }
            return out.toByteArray();
        } catch (java.io.IOException e) {
            throw new FormatException("EMWIN ZIP could not be read: " + e.getMessage());
        }
    }

    private static String cap(String text) {
        String clean = text.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", " ");
        if (clean.getBytes(StandardCharsets.UTF_8).length <= MAX_TEXT_BYTES) {
            return clean;
        }
        byte[] bytes = clean.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, 0, MAX_TEXT_BYTES, StandardCharsets.UTF_8);
    }

    private static String headline(String text) {
        for (String line : text.split("\\R")) {
            String stripped = line.strip();
            if (!stripped.isEmpty() && !stripped.matches("^[A-Z]{4}\\d{2}.*") && !stripped.matches("^[A-Z]{6}\\d*$")) {
                return stripped.length() > 256 ? stripped.substring(0, 256) : stripped;
            }
        }
        return null;
    }

    static Instant vtecTime(String yymmdd, String hhmm) {
        try {
            // P-VTEC writes its times as yymmddThhnnZ (NWS Directive 10-1703)
            int year = 2000 + Integer.parseInt(yymmdd.substring(0, 2));
            int month = Integer.parseInt(yymmdd.substring(2, 4));
            int day = Integer.parseInt(yymmdd.substring(4, 6));
            int hour = Integer.parseInt(hhmm.substring(0, 2));
            int minute = Integer.parseInt(hhmm.substring(2, 4));
            return java.time.LocalDateTime.of(year, month, day, hour, minute).toInstant(java.time.ZoneOffset.UTC);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * The issue time of a product from its WMO heading's {@code DDHHMM} group (prompt 107), or null when
     * the heading is missing or malformed. The heading carries no month or year, so the latest such time
     * not more than a day after {@code now} is chosen (a product issued on the 31st and read on the 1st
     * belongs to the previous month).
     */
    public static Instant issuedAt(String wmoHeading, Instant now) {
        if (wmoHeading == null || now == null) {
            return null;
        }
        Matcher m = WMO.matcher(wmoHeading.strip());
        if (!m.matches()) {
            return null;
        }
        String ddhhmm = m.group(3);
        int day = Integer.parseInt(ddhhmm.substring(0, 2));
        int hour = Integer.parseInt(ddhhmm.substring(2, 4));
        int minute = Integer.parseInt(ddhhmm.substring(4, 6));
        if (day < 1 || day > 31 || hour > 23 || minute > 59) {
            return null;
        }
        java.time.YearMonth month = java.time.YearMonth.from(now.atZone(java.time.ZoneOffset.UTC));
        for (int back = 0; back < 3; back++) {
            java.time.YearMonth candidate = month.minusMonths(back);
            if (day > candidate.lengthOfMonth()) {
                continue;
            }
            Instant at = candidate.atDay(day).atTime(hour, minute).toInstant(java.time.ZoneOffset.UTC);
            if (!at.isAfter(now.plus(java.time.Duration.ofDays(1)))) {
                return at;
            }
        }
        return null;
    }

    /** True when {@code category} is in the allowlist (case-insensitive). */
    public static boolean categoryAllowed(String category, List<String> allowed) {
        if (category == null) {
            return false;
        }
        String c = category.toUpperCase(Locale.ROOT);
        for (String a : allowed) {
            if (c.equals(a.strip().toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * The {@code TIME...MOT...LOC} motion triple {@code {hhmm, directionDeg, speedKt}} found anywhere in the
     * text (for example {@code TIME...MOT...LOC 2130Z 250DEG 24KT 3250 09730}), or null.
     */
    public static int[] timeMotLoc(String text) {
        Matcher m = TIME_MOT_LOC.matcher(text);
        if (!m.find()) {
            return null;
        }
        try {
            return new int[] {Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
