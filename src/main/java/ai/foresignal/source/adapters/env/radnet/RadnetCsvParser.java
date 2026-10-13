package ai.foresignal.source.adapters.env.radnet;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * One EPA RadNet station-year CSV ({@code …/csv/{year}/fixed/{STATE}/{CITY}}) reduced to its newest row (prompt
 * 078). Pure.
 *
 * <p>Verified 2026-10-13 on Denver (600 KB, {@code Content-Type: text/text}, no validators): a header
 * {@code LOCATION_NAME, SAMPLE COLLECTION TIME, DOSE EQUIVALENT RATE (nSv/h), GAMMA COUNT RATE R02 (CPM) … R09
 * (CPM), STATUS} and one row per hour; the dose column is often empty. Columns are found by name.
 *
 * <p><strong>Time base:</strong> {@code SAMPLE COLLECTION TIME} ({@code MM/dd/yyyy HH:mm:ss}) is read as
 * <strong>UTC</strong>. The newest rows of Honolulu, Anchorage, Denver, San Francisco, Boston and San Juan read
 * 19:48–20:41 when fetched at 21:34 UTC on 2026-10-13; as local standard time several would lie hours in the future.
 *
 * <p>Record: kind {@value #KIND}, {@code externalId = STATE/CITY}, the station's bundled point, payload {@code
 * doseRateNsvH} (when present), {@code grossCpm} (the sum of the bands present), {@code bands} ({@code R02}…{@code
 * R09} to CPM), {@code status} ({@code APPROVED|PRELIMINARY}); {@code observedAt = eventTime} = the collection time.
 */
public final class RadnetCsvParser {

    /** The record kind. */
    public static final String KIND = "radiation";

    /** The time column. */
    static final String TIME_COLUMN = "SAMPLE COLLECTION TIME";

    /** The dose column. */
    static final String DOSE_COLUMN = "DOSE EQUIVALENT RATE (nSv/h)";

    /** The status column. */
    static final String STATUS_COLUMN = "STATUS";

    /** The gamma bands, in order. */
    public static final List<String> BANDS = List.of("R02", "R03", "R04", "R05", "R06", "R07", "R08", "R09");

    /** {@code 10/13/2026 20:41:00}. */
    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm:ss", Locale.ROOT);

    /**
     * The newest row of a file.
     *
     * @param time the collection time (UTC)
     * @param doseRateNsvH the dose equivalent rate, or null when the column is empty
     * @param bands band to CPM, for the bands present
     * @param status the status, upper case, or null
     */
    public record Row(Instant time, Double doseRateNsvH, Map<String, Double> bands, String status) {

        /** Copies the bands, keeping their order. */
        public Row {
            bands = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(bands));
        }

        /** The sum of the bands present, or null when none is. */
        public Double grossCpm() {
            if (bands.isEmpty()) {
                return null;
            }
            double sum = 0;
            for (double v : bands.values()) {
                sum += v;
            }
            return EnvSources.round(sum, 2);
        }
    }

    private RadnetCsvParser() {}

    /**
     * The newest data row of a CSV, by collection time.
     *
     * @return the row, or empty when the file holds only its header (a station-year with no data yet)
     * @throws UpstreamFormatException when the header lacks the time column
     */
    public static Optional<Row> newest(byte[] body) {
        String text = body == null ? "" : new String(body, StandardCharsets.ISO_8859_1);
        String[] lines = text.split("\r?\n");
        int first = 0;
        while (first < lines.length && lines[first].isBlank()) {
            first++;
        }
        if (first >= lines.length) {
            throw new UpstreamFormatException("RadNet CSV is empty", "");
        }
        List<String> header = split(lines[first]);
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            columns.put(header.get(i).strip().toUpperCase(Locale.ROOT), i);
        }
        Integer timeAt = columns.get(TIME_COLUMN);
        if (timeAt == null) {
            throw new UpstreamFormatException("RadNet CSV has no '" + TIME_COLUMN + "' column", lines[first]);
        }
        Integer doseAt = columns.get(DOSE_COLUMN.toUpperCase(Locale.ROOT));
        Integer statusAt = columns.get(STATUS_COLUMN);
        Map<String, Integer> bandAt = new LinkedHashMap<>();
        for (String band : BANDS) {
            Integer at = columns.get("GAMMA COUNT RATE " + band + " (CPM)");
            if (at != null) {
                bandAt.put(band, at);
            }
        }
        Row newest = null;
        for (int n = first + 1; n < lines.length; n++) {
            if (lines[n].isBlank()) {
                continue;
            }
            List<String> cells = split(lines[n]);
            Instant time = time(cell(cells, timeAt));
            if (time == null || (newest != null && !time.isAfter(newest.time()))) {
                continue;
            }
            Map<String, Double> bands = new LinkedHashMap<>();
            bandAt.forEach((band, at) -> {
                Double v = number(cell(cells, at));
                if (v != null) {
                    bands.put(band, v);
                }
            });
            String status = cell(cells, statusAt);
            newest = new Row(time, number(cell(cells, doseAt)), bands,
                    status == null ? null : status.toUpperCase(Locale.ROOT));
        }
        return Optional.ofNullable(newest);
    }

    /** The record of {@code station}'s newest row. */
    public static MappedRecord record(RadnetStations.Station station, Row row) {
        if (row.bands().isEmpty() && row.doseRateNsvH() == null) {
            return MappedRecord.skipped(EnvSources.NO_VALUES);
        }
        return MappedRecord.of(NormalizedRecord.builder(station.id(), KIND)
                .observedAt(row.time())
                .eventTime(row.time())
                .point(station.lon(), station.lat())
                .put("doseRateNsvH", row.doseRateNsvH())
                .put("grossCpm", row.grossCpm())
                .put("bands", row.bands().isEmpty() ? null : row.bands())
                .put("status", row.status())
                .build());
    }

    /** {@code 10/13/2026 20:41:00} as a UTC instant, or null. */
    static Instant time(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.strip(), TIME).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String cell(List<String> cells, Integer at) {
        if (at == null || at >= cells.size()) {
            return null;
        }
        String s = cells.get(at).strip();
        return s.isEmpty() ? null : s;
    }

    private static Double number(String text) {
        if (text == null) {
            return null;
        }
        try {
            double d = Double.parseDouble(text);
            return Double.isFinite(d) && d >= 0 ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** One CSV line split on commas, honoring double-quoted cells. */
    static List<String> split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                out.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        out.add(cell.toString());
        return out;
    }
}
