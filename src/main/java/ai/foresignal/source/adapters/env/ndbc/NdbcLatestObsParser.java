package ai.foresignal.source.adapters.env.ndbc;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * NDBC's {@code latest_obs.txt} as {@code buoy-observation} records (prompt 078). Pure.
 *
 * <p>Verified 2026-10-13 (104 KB, {@code ETag} and {@code Last-Modified}): a header {@code #STN LAT LON YYYY MM DD
 * hh mm WDIR WSPD GST WVHT DPD APD MWD PRES PTDY ATMP WTMP DEWP VIS TIDE}, a units row starting {@code #text}, then
 * one whitespace-separated row per station; {@code MM} means missing. Columns are found by header name; a missing
 * value is left out.
 *
 * <ul>
 *   <li>{@code externalId = STN}, a point from {@code LAT}/{@code LON} (station {@code 15009} reports {@code 0.000
 *       -3.051}, which is valid: only exactly (0, 0) is refused);
 *   <li>payload {@code name, stationType, owner} from {@code activestations.xml} when listed, and {@link #COLUMNS}
 *       mapped to {@code windDirDeg, windSpeedMs, gustMs, waveHeightM, dominantPeriodS, averagePeriodS,
 *       meanWaveDirDeg, pressureHpa, pressureTendencyHpa, airTempC, waterTempC, dewPointC, visibilityNmi,
 *       tideFt};
 *   <li>{@code observedAt = eventTime} from {@code YYYY MM DD hh mm} (UTC).
 * </ul>
 */
public final class NdbcLatestObsParser {

    /** The record kind. */
    public static final String KIND = "buoy-observation";

    /** The skip reason of a row whose cell count differs from the header's. */
    public static final String BAD_ROW = "bad-row";

    /** The missing-value marker. */
    static final String MISSING = "MM";

    /** Header column to payload key, in payload order. */
    public static final Map<String, String> COLUMNS = columns();

    /** The default {@code maxAge}. */
    public static final Duration DEFAULT_MAX_AGE = Duration.ofHours(6);

    private NdbcLatestObsParser() {}

    private static Map<String, String> columns() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("WDIR", "windDirDeg");
        m.put("WSPD", "windSpeedMs");
        m.put("GST", "gustMs");
        m.put("WVHT", "waveHeightM");
        m.put("DPD", "dominantPeriodS");
        m.put("APD", "averagePeriodS");
        m.put("MWD", "meanWaveDirDeg");
        m.put("PRES", "pressureHpa");
        m.put("PTDY", "pressureTendencyHpa");
        m.put("ATMP", "airTempC");
        m.put("WTMP", "waterTempC");
        m.put("DEWP", "dewPointC");
        m.put("VIS", "visibilityNmi");
        m.put("TIDE", "tideFt");
        return java.util.Collections.unmodifiableMap(m);
    }

    /**
     * Parses the file.
     *
     * @param stations the station table, possibly empty
     * @param now the fetch time
     * @param maxAge the oldest observation kept
     * @throws UpstreamFormatException when the header lacks a required column
     */
    public static EnvSources.Parsed parse(byte[] body, Map<String, NdbcStationsParser.Station> stations, Instant now,
            Duration maxAge) {
        String text = body == null ? "" : new String(body, StandardCharsets.ISO_8859_1);
        String[] lines = text.split("\r?\n");
        Map<String, Integer> header = null;
        EnvSources.Collector collector = new EnvSources.Collector(now, maxAge);
        for (String line : lines) {
            String trimmed = line.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("#")) {
                if (header == null && trimmed.substring(1).strip().startsWith("STN")) {
                    header = header(trimmed.substring(1).strip());
                }
                continue;
            }
            if (header == null) {
                throw new UpstreamFormatException("NDBC latest_obs.txt has no #STN header", trimmed);
            }
            collector.add(row(trimmed.split("\\s+"), header, stations));
        }
        if (header == null) {
            throw new UpstreamFormatException("NDBC latest_obs.txt has no #STN header", "");
        }
        return collector.result();
    }

    private static Map<String, Integer> header(String line) {
        String[] names = line.split("\\s+");
        Map<String, Integer> out = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            // Case matters: the header names the month "MM" and the minute "mm".
            out.putIfAbsent(names[i], i);
        }
        for (String required : List.of("STN", "LAT", "LON", "YYYY", "MM", "DD", "hh", "mm")) {
            if (!out.containsKey(required)) {
                throw new UpstreamFormatException("NDBC latest_obs.txt header has no " + required + " column", line);
            }
        }
        out.put("#COUNT", names.length);
        return out;
    }

    static MappedRecord row(String[] cells, Map<String, Integer> header, Map<String, NdbcStationsParser.Station> stations) {
        if (cells.length != header.get("#COUNT")) {
            return MappedRecord.skipped(BAD_ROW);
        }
        String id = cells[header.get("STN")].toUpperCase(Locale.ROOT);
        Instant at = time(cells, header);
        if (at == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        Double lat = number(cells, header, "LAT");
        Double lon = number(cells, header, "LON");
        if (!EnvSources.validPoint(lat, lon)) {
            return MappedRecord.skipped(EnvSources.MISSING_LOCATION);
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND).observedAt(at).eventTime(at).point(lon, lat);
        NdbcStationsParser.Station station = stations == null ? null : stations.get(id);
        if (station != null) {
            b.put("name", station.name()).put("stationType", station.type()).put("owner", station.owner());
        }
        int values = 0;
        for (Map.Entry<String, String> c : COLUMNS.entrySet()) {
            Double v = number(cells, header, c.getKey());
            if (v != null) {
                b.put(c.getValue(), v);
                values++;
            }
        }
        if (values == 0) {
            return MappedRecord.skipped(EnvSources.NO_VALUES);
        }
        return MappedRecord.of(b.build());
    }

    private static Instant time(String[] cells, Map<String, Integer> header) {
        try {
            return LocalDateTime.of(Integer.parseInt(cells[header.get("YYYY")]),
                    Integer.parseInt(cells[header.get("MM")]), Integer.parseInt(cells[header.get("DD")]),
                    Integer.parseInt(cells[header.get("hh")]), Integer.parseInt(cells[header.get("mm")]))
                    .toInstant(ZoneOffset.UTC);
        } catch (NumberFormatException | DateTimeException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private static Double number(String[] cells, Map<String, Integer> header, String column) {
        Integer at = header.get(column);
        if (at == null || at >= cells.length || MISSING.equals(cells[at])) {
            return null;
        }
        try {
            double d = Double.parseDouble(cells[at]);
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
