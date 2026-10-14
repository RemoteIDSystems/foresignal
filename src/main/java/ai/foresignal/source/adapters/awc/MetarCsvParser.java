package ai.foresignal.source.adapters.awc;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Parses the AWC METAR cache file ({@code metars.cache.csv}, prompt 047) into {@code metar} records.
 *
 * <p>A pure function of the bytes and the station filter. Lines before the header that begins
 * {@code raw_text,station_id,} are skipped (older files carried a preamble; the 2026-10-13 file starts
 * with the header). Columns are found by name, except the four repeated
 * {@code sky_cover,cloud_base_ft_agl} pairs, which are read by position. Only the newest
 * {@code observation_time} per station is kept.
 *
 * <p>Payload values are normalized and empty ones left out (no nulls). Verified 2026-10-13 against the
 * live file (5,046 stations):
 * <ul>
 *   <li>a variable wind ({@code VRB02KT}) appears as {@code wind_dir_degrees = 0}, never as
 *       {@code VRB}, so variability is read from the raw report: {@code windDirDeg} is left out and
 *       {@code windVariable: true} is set;
 *   <li>visibility is {@code "10+"} (US) or {@code "6+"} (9999 m elsewhere): the number is kept with
 *       {@code visibilityPlus: true};
 *   <li>a missing flight category is the literal string {@code null}.
 * </ul>
 */
public final class MetarCsvParser {

    /** The record kind. */
    public static final String KIND = "metar";

    /** The header prefix that ends any preamble. */
    public static final String HEADER_PREFIX = "raw_text,station_id,";

    /** The largest inflated file accepted (the live file is about 1.3 MB). */
    static final long MAX_INFLATED_BYTES = 64L * 1024 * 1024;

    private static final Pattern VRB = Pattern.compile("^VRB\\d{2,3}(G\\d{2,3})?(KT|MPS|KMH)$");

    private MetarCsvParser() {}

    /**
     * Which stations to keep.
     *
     * @param envelope {@code [minLon, minLat, maxLon, maxLat]}, or null for the whole world
     * @param stations an allowlist of station ids that overrides the envelope; empty for none
     */
    public record Filter(double[] envelope, Set<String> stations) {

        /** Copies the allowlist. */
        public Filter {
            stations = stations == null ? Set.of() : Set.copyOf(stations);
        }

        /** Every station. */
        public static Filter global() {
            return new Filter(null, Set.of());
        }

        boolean keeps(String station, double lon, double lat) {
            return AwcSupport.keeps(envelope, stations, station, lon, lat);
        }
    }

    /** {@code bytes} inflated when they are gzip, as they are on the wire. */
    public static byte[] inflate(byte[] bytes) {
        if (bytes == null || bytes.length < 2 || (bytes[0] & 0xff) != 0x1f || (bytes[1] & 0xff) != 0x8b) {
            return bytes;
        }
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] out = in.readNBytes((int) MAX_INFLATED_BYTES + 1);
            if (out.length > MAX_INFLATED_BYTES) {
                throw new UpstreamFormatException("AWC METAR file inflates past " + MAX_INFLATED_BYTES + " bytes", null);
            }
            return out;
        } catch (IOException e) {
            throw new UpstreamFormatException("AWC METAR file is not readable gzip: " + e.getMessage(), null, e);
        }
    }

    /**
     * Parses a file.
     *
     * @param csv the file, gzip or plain
     * @param filter which stations to keep
     * @return the newest report per kept station, and counts
     * @throws UpstreamFormatException when there is no header line
     */
    public static ParseResult parse(byte[] csv, Filter filter) {
        byte[] plain = inflate(csv);
        if (plain == null || plain.length == 0) {
            throw new UpstreamFormatException("AWC METAR file is empty", "");
        }
        Map<String, Report> newest = new LinkedHashMap<>();
        int preamble = 0;
        int rows = 0;
        int malformed = 0;
        int filtered = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(plain),
                StandardCharsets.UTF_8))) {
            Columns columns = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (columns == null) {
                    if (line.startsWith(HEADER_PREFIX)) {
                        columns = Columns.of(split(line));
                    } else {
                        preamble++;
                    }
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                rows++;
                Report report = Report.of(split(line), columns);
                if (report == null) {
                    malformed++;
                    continue;
                }
                if (!filter.keeps(report.station(), report.lon(), report.lat())) {
                    filtered++;
                    continue;
                }
                Report previous = newest.get(report.station());
                if (previous == null || report.observed().isAfter(previous.observed())) {
                    newest.put(report.station(), report);
                }
            }
            if (columns == null) {
                throw new UpstreamFormatException("AWC METAR file has no header line beginning '" + HEADER_PREFIX + "'",
                        new String(plain, 0, Math.min(plain.length, 2000), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        List<NormalizedRecord> records = new ArrayList<>(newest.size());
        newest.values().forEach(r -> records.add(r.record()));
        return new ParseResult(List.copyOf(records), new ParseDiagnostics(preamble, rows, records.size(), malformed, filtered));
    }

    /** Splits one CSV line, honoring double quotes and doubled quotes inside them. */
    static List<String> split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                out.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        out.add(cell.toString());
        return out;
    }

    /** True when the raw report's wind group is variable ({@code VRB05KT}). */
    static boolean variableWind(String raw) {
        if (raw == null) {
            return false;
        }
        for (String token : raw.split("\\s+")) {
            if (VRB.matcher(token).matches()) {
                return true;
            }
        }
        return false;
    }

    /** Column positions. */
    record Columns(Map<String, Integer> byName, List<Integer> skyCover, List<Integer> cloudBase) {

        static Columns of(List<String> header) {
            Map<String, Integer> byName = new HashMap<>();
            List<Integer> cover = new ArrayList<>();
            List<Integer> base = new ArrayList<>();
            for (int i = 0; i < header.size(); i++) {
                String name = header.get(i).strip();
                if (name.equals("sky_cover")) {
                    cover.add(i);
                } else if (name.equals("cloud_base_ft_agl")) {
                    base.add(i);
                } else {
                    byName.putIfAbsent(name, i);
                }
            }
            return new Columns(byName, cover, base);
        }

        String get(List<String> row, String name) {
            Integer i = byName.get(name);
            return i == null || i >= row.size() ? "" : row.get(i).strip();
        }
    }

    /** One parsed row. */
    private record Report(String station, Instant observed, double lon, double lat, NormalizedRecord record) {

        static Report of(List<String> row, Columns c) {
            String station = c.get(row, "station_id");
            Instant observed = instant(c.get(row, "observation_time"));
            Double lat = number(c.get(row, "latitude"));
            Double lon = number(c.get(row, "longitude"));
            if (station.isEmpty() || observed == null || lat == null || lon == null || Math.abs(lat) > 90
                    || Math.abs(lon) > 180) {
                return null;
            }
            String raw = c.get(row, "raw_text");
            NormalizedRecord.Builder b = NormalizedRecord.builder(station, KIND)
                    .observedAt(observed)
                    .eventTime(observed)
                    .point(lon, lat);
            b.put("raw", raw.isEmpty() ? null : raw);
            b.put("stationId", station);
            String type = c.get(row, "metar_type");
            b.put("metarType", type.isEmpty() ? null : type);
            b.put("tempC", number(c.get(row, "temp_c")));
            b.put("dewpointC", number(c.get(row, "dewpoint_c")));
            String dir = c.get(row, "wind_dir_degrees");
            if (dir.equalsIgnoreCase("VRB") || variableWind(raw)) {
                b.put("windVariable", true);
            } else {
                b.put("windDirDeg", integer(dir));
            }
            b.put("windKt", integer(c.get(row, "wind_speed_kt")));
            b.put("gustKt", integer(c.get(row, "wind_gust_kt")));
            String visibility = c.get(row, "visibility_statute_mi");
            if (visibility.endsWith("+")) {
                b.put("visibilitySm", number(visibility.substring(0, visibility.length() - 1)));
                b.put("visibilityPlus", true);
            } else {
                b.put("visibilitySm", number(visibility));
            }
            b.put("altimInHg", number(c.get(row, "altim_in_hg")));
            b.put("slpMb", number(c.get(row, "sea_level_pressure_mb")));
            String wx = c.get(row, "wx_string");
            b.put("wx", wx.isEmpty() ? null : wx);
            List<Map<String, Object>> sky = new ArrayList<>();
            for (int i = 0; i < c.skyCover().size(); i++) {
                int coverAt = c.skyCover().get(i);
                String cover = coverAt < row.size() ? row.get(coverAt).strip() : "";
                if (cover.isEmpty()) {
                    continue;
                }
                Map<String, Object> layer = new LinkedHashMap<>();
                layer.put("cover", cover);
                if (i < c.cloudBase().size()) {
                    int baseAt = c.cloudBase().get(i);
                    Long base = baseAt < row.size() ? integer(row.get(baseAt).strip()) : null;
                    if (base != null) {
                        layer.put("baseFtAgl", base);
                    }
                }
                sky.add(layer);
            }
            b.put("sky", sky.isEmpty() ? null : sky);
            String category = c.get(row, "flight_category");
            b.put("flightCategory", category.isEmpty() || category.equalsIgnoreCase("null") ? null : category);
            b.put("elevationM", number(c.get(row, "elevation_m")));
            return new Report(station, observed, lon, lat, b.build());
        }
    }

    private static Instant instant(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Double number(String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            double d = Double.parseDouble(value);
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long integer(String value) {
        Double d = number(value);
        return d == null ? null : Math.round(d);
    }

    /**
     * One parsed file.
     *
     * @param records the newest report per kept station
     * @param diagnostics counts
     */
    public record ParseResult(List<NormalizedRecord> records, ParseDiagnostics diagnostics) {}

    /**
     * What one parse read.
     *
     * @param preambleLines lines skipped before the header
     * @param rows data rows read
     * @param stations stations kept
     * @param malformed rows without a station, time or position
     * @param filtered rows outside the scope or allowlist
     */
    public record ParseDiagnostics(int preambleLines, int rows, int stations, int malformed, int filtered) {}
}
