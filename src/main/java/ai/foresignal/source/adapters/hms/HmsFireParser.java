package ai.foresignal.source.adapters.hms;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses an HMS fire points text file into {@code fire-detection} records (prompt 048).
 *
 * <p>The file is a CSV whose header is {@code Lon, Lat, YearDay, Time, Satellite, Method, Ecosystem, FRP},
 * every field padded with spaces (verified on {@code hms_fire20261012.txt}: 16,777 points, 55 with FRP
 * {@code -999.000}). Columns are found by header name, so a reordering is harmless; a missing column is
 * a format error.
 *
 * <p>Record: {@code externalId} {@code hms:{yearDay}:{hhmm}:{lat 4dp}:{lon 4dp}}; {@code observedAt} =
 * {@code eventTime} = YearDay + Time (UTC); payload {@code satellite}, {@code method}, {@code ecosystem},
 * {@code frpMw} (2 significant figures, left out when −999) and {@code source: "HMS"}. A repeated id keeps
 * its first row; a malformed row is skipped and counted.
 */
public final class HmsFireParser {

    /** The record kind (shared with {@code goes-fdc}). */
    public static final String KIND = "fire-detection";

    /** The columns read. */
    static final List<String> COLUMNS = List.of("lon", "lat", "yearday", "time", "satellite", "method", "ecosystem",
            "frp");

    private HmsFireParser() {}

    /**
     * The parsed file.
     *
     * @param records one per distinct point
     * @param rows data rows seen
     * @param skipped malformed rows
     */
    public record Result(List<NormalizedRecord> records, int rows, int skipped) {}

    /**
     * Parses the file.
     *
     * @throws UpstreamFormatException when the header is missing or lacks a column
     */
    public static Result parse(byte[] body) {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("The HMS fire file is empty", null);
        }
        String text = new String(body, StandardCharsets.UTF_8);
        String[] lines = text.split("\r?\n");
        int headerLine = -1;
        Map<String, Integer> columns = new LinkedHashMap<>();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                continue;
            }
            String[] cells = lines[i].split(",");
            for (int c = 0; c < cells.length; c++) {
                columns.put(cells[c].trim().toLowerCase(Locale.ROOT), c);
            }
            headerLine = i;
            break;
        }
        if (headerLine < 0) {
            throw new UpstreamFormatException("The HMS fire file has no header", null);
        }
        for (String column : COLUMNS) {
            if (!columns.containsKey(column)) {
                throw new UpstreamFormatException("The HMS fire file has no '" + column + "' column",
                        lines[headerLine]);
            }
        }
        Map<String, NormalizedRecord> records = new LinkedHashMap<>();
        int rows = 0;
        int skipped = 0;
        for (int i = headerLine + 1; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                continue;
            }
            rows++;
            NormalizedRecord record = row(lines[i].split(","), columns);
            if (record == null) {
                skipped++;
            } else {
                records.putIfAbsent(record.externalId(), record);
            }
        }
        return new Result(List.copyOf(records.values()), rows, skipped);
    }

    /** The record of one row, or null when it is malformed. */
    static NormalizedRecord row(String[] cells, Map<String, Integer> columns) {
        try {
            double lon = Double.parseDouble(cell(cells, columns, "lon"));
            double lat = Double.parseDouble(cell(cells, columns, "lat"));
            String yearDay = cell(cells, columns, "yearday");
            String time = cell(cells, columns, "time");
            if (yearDay.length() != 7 || time.length() > 4 || Math.abs(lat) > 90 || Math.abs(lon) > 180
                    || Double.isNaN(lat) || Double.isNaN(lon)) {
                return null;
            }
            String hhmm = String.format(Locale.ROOT, "%04d", Integer.parseInt(time));
            Instant at = HmsKmlParser.julian(yearDay, hhmm);
            if (at == null) {
                return null;
            }
            String frpText = cell(cells, columns, "frp");
            Double frp = null;
            if (!frpText.isEmpty()) {
                double value = Double.parseDouble(frpText);
                if (value >= 0) {
                    frp = value == 0 ? 0.0 : new BigDecimal(value).round(new MathContext(2, RoundingMode.HALF_UP))
                            .doubleValue();
                }
            }
            String ecosystemText = cell(cells, columns, "ecosystem");
            Integer ecosystem = ecosystemText.isEmpty() ? null : Integer.valueOf(ecosystemText);
            String id = String.format(Locale.ROOT, "hms:%s:%s:%.4f:%.4f", yearDay, hhmm, lat, lon);
            return NormalizedRecord.builder(id, KIND)
                    .observedAt(at)
                    .eventTime(at)
                    .point(lon, lat)
                    .put("satellite", blankToNull(cell(cells, columns, "satellite")))
                    .put("method", blankToNull(cell(cells, columns, "method")))
                    .put("ecosystem", ecosystem)
                    .put("frpMw", frp)
                    .put("source", "HMS")
                    .build();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String cell(String[] cells, Map<String, Integer> columns, String name) {
        int index = columns.get(name);
        return index < cells.length ? cells[index].trim() : "";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
