package ai.foresignal.source.adapters.spc;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses SPC daily storm report CSVs ({@code today.csv}, {@code yesterday.csv}; prompt 053). Pure functions.
 *
 * <p>Verified 2026-10-13: one file holds three sections, each starting with its own header —
 * {@code Time,F_Scale,…} (tornado), {@code Time,Speed,…} (wind) and {@code Time,Size,…} (hail) — with the
 * columns {@code Time,<magnitude>,Location,County,State,Lat,Lon,Comments}; a header-only file (no reports yet)
 * is valid. Comments end with the office in parentheses ({@code (RAH)}), are truncated upstream, and may hold
 * commas inside quotes; an unquoted comment that still splits is joined back.
 *
 * <p><b>Units</b>: hail {@code Size} is hundredths of an inch ({@code 175} = 1.75 in, as the SPC report pages
 * state). Wind {@code Speed} is miles per hour: the 2026-10-12 file has {@code 1215,73,22 SE Meadview,…} with the
 * comment "measured a gust of 73 mph"; {@code UNK} is a damage report without a measured speed. Tornado
 * {@code F_Scale} is a rating such as {@code EF1}, usually {@code UNK} until surveyed. {@code UNK} magnitudes are
 * left out.
 *
 * <p><b>Time</b>: {@code HHMM} UTC within the SPC convective day (12 UTC to 12 UTC): a row at or after 1200 is on
 * the convective date, a row before 1200 on the next calendar date.
 *
 * <p><b>Identity</b>: reports have no upstream id, so the externalId is the first 12 hex digits of SHA-256 over
 * {@code type|convDate|HHMM|lat|lon|location}, stable across re-fetches of today's and yesterday's files.
 */
public final class SpcStormReportsParser {

    /** The record kind. */
    public static final String KIND = "storm-report";

    /** The longest comment kept. */
    public static final int MAX_COMMENT = 500;

    private static final Pattern WFO = Pattern.compile("\\(([A-Z]{3})\\)\\s*$");
    private static final Pattern TIME = Pattern.compile("^\\d{4}$");

    private SpcStormReportsParser() {}

    /**
     * One parsed file.
     *
     * @param records the reports, in file order
     * @param skipped rows that could not be read (bad time or coordinates)
     */
    public record ParseResult(List<NormalizedRecord> records, int skipped) {}

    /**
     * Parses a storm report CSV.
     *
     * @param csv the file
     * @param convDate the SPC convective date the file covers
     * @throws UpstreamFormatException when the file has rows but no section header
     */
    public static ParseResult parse(byte[] csv, LocalDate convDate) {
        String text = new String(csv, StandardCharsets.UTF_8);
        List<NormalizedRecord> out = new ArrayList<>();
        int skipped = 0;
        String type = null;
        String magnitudeColumn = null;
        for (String raw : text.split("\r?\n")) {
            String line = raw.strip();
            if (line.isEmpty()) {
                continue;
            }
            List<String> fields = split(line);
            if (!fields.isEmpty() && "Time".equals(fields.get(0))) {
                magnitudeColumn = fields.size() > 1 ? fields.get(1) : "";
                type = switch (magnitudeColumn) {
                    case "F_Scale" -> "tornado";
                    case "Speed" -> "wind";
                    case "Size" -> "hail";
                    default -> throw new UpstreamFormatException("SPC storm reports: unknown section " + magnitudeColumn,
                            line);
                };
                continue;
            }
            if (type == null) {
                throw new UpstreamFormatException("SPC storm reports: a row before any section header", line);
            }
            NormalizedRecord record = row(type, fields, convDate);
            if (record == null) {
                skipped++;
            } else {
                out.add(record);
            }
        }
        return new ParseResult(out, skipped);
    }

    /** One report, or null when its time or coordinates cannot be read. */
    static NormalizedRecord row(String type, List<String> fields, LocalDate convDate) {
        if (fields.size() < 7) {
            return null;
        }
        String hhmm = fields.get(0).strip();
        if (!TIME.matcher(hhmm).matches()) {
            return null;
        }
        int hour = Integer.parseInt(hhmm.substring(0, 2));
        int minute = Integer.parseInt(hhmm.substring(2));
        if (hour > 23 || minute > 59) {
            return null;
        }
        double lat;
        double lon;
        try {
            lat = Double.parseDouble(fields.get(5).strip());
            lon = Double.parseDouble(fields.get(6).strip());
        } catch (NumberFormatException e) {
            return null;
        }
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            return null;
        }
        Instant time = reportTime(convDate, hour, minute);
        String location = fields.get(2).strip();
        String county = fields.get(3).strip();
        String state = fields.get(4).strip();
        String comments = fields.size() > 7 ? String.join(",", fields.subList(7, fields.size())).strip() : "";
        String magnitudeRaw = fields.get(1).strip();

        NormalizedRecord.Builder b = NormalizedRecord.builder(
                        externalId(type, convDate, hhmm, fields.get(5).strip(), fields.get(6).strip(), location), KIND)
                .observedAt(time)
                .eventTime(time)
                .geometry(Geo.point(lon, lat))
                .put("type", type);
        Object magnitude = magnitude(type, magnitudeRaw);
        if (magnitude != null) {
            b.put("magnitude", magnitude);
            b.put("magnitudeUnits", units(type));
        }
        b.put("location", location);
        if (!county.isEmpty()) {
            b.put("county", county);
        }
        if (!state.isEmpty()) {
            b.put("state", state);
        }
        Matcher wfo = WFO.matcher(comments);
        if (wfo.find()) {
            b.put("wfo", wfo.group(1));
        }
        if (!comments.isEmpty()) {
            b.put("comments", comments.length() > MAX_COMMENT ? comments.substring(0, MAX_COMMENT) : comments);
        }
        b.put("title", title(type, magnitude, location, state));
        return b.build();
    }

    /** The report time: {@code HHMM} at or after 1200 on the convective date, before 1200 on the next date. */
    static Instant reportTime(LocalDate convDate, int hour, int minute) {
        LocalDate date = hour < 12 ? convDate.plusDays(1) : convDate;
        return date.atTime(LocalTime.of(hour, minute)).toInstant(ZoneOffset.UTC);
    }

    /** The magnitude: tornado rating string, wind mph or hail inches as numbers; null for {@code UNK} or blank. */
    static Object magnitude(String type, String raw) {
        if (raw == null || raw.isBlank() || "UNK".equalsIgnoreCase(raw.strip())) {
            return null;
        }
        String v = raw.strip();
        if ("tornado".equals(type)) {
            return v.toUpperCase(Locale.ROOT);
        }
        try {
            BigDecimal number = new BigDecimal(v);
            if ("hail".equals(type)) {
                return number.movePointLeft(2).stripTrailingZeros().doubleValue();
            }
            return number.intValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** The magnitude units of a type. */
    static String units(String type) {
        return switch (type) {
            case "tornado" -> "EF";
            case "wind" -> "mph";
            default -> "in";
        };
    }

    /** {@code Hail 1.75 in — 5 WNW Oliver, GA}. */
    static String title(String type, Object magnitude, String location, String state) {
        String label = switch (type) {
            case "tornado" -> "Tornado";
            case "wind" -> "Wind";
            default -> "Hail";
        };
        StringBuilder s = new StringBuilder(label);
        if (magnitude != null) {
            s.append(' ');
            if (magnitude instanceof Double d) {
                s.append(BigDecimal.valueOf(d).stripTrailingZeros().toPlainString());
            } else {
                s.append(magnitude);
            }
            if (!"tornado".equals(type)) {
                s.append(' ').append(units(type));
            }
        }
        s.append(" — ").append(location);
        if (state != null && !state.isEmpty()) {
            s.append(", ").append(state);
        }
        return s.toString();
    }

    /** The first 12 hex digits of SHA-256 over {@code type|convDate|HHMM|lat|lon|location}. */
    public static String externalId(String type, LocalDate convDate, String hhmm, String lat, String lon, String location) {
        String key = type + "|" + convDate + "|" + hhmm + "|" + lat + "|" + lon + "|" + location;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /**
     * Splits one CSV line: commas separate fields, double quotes group (a doubled quote is a literal quote).
     */
    static List<String> split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                out.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        out.add(field.toString());
        return out;
    }
}
