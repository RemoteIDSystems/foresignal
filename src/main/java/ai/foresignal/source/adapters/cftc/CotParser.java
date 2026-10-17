package ai.foresignal.source.adapters.cftc;

import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Turns CFTC Public Reporting rows into {@code series-point} records (prompt 117). Pure: no network, no state.
 *
 * <p>One point per (contract, report, measure, report date): {@code seriesId = cot.{code}.{report}.{measure}},
 * {@code externalId = {seriesId}:{reportDate}}, {@code eventTime} = the report's "as of" date (a Tuesday),
 * {@code observedAt} = the fetch time (the release is the following Friday, so the point was not known on its
 * period: release lags are data, Intelligence-Design §4.3). Net = long − short; {@code mm_net_pct_oi} = managed
 * money net ÷ open interest × 100. A measure whose columns are missing or not numeric is left out of that row; the
 * row's other measures are kept.
 */
public final class CotParser {

    /** The frequency code of every COT series: weekly. */
    public static final String FREQUENCY = "P1W";

    /** The UCUM unit of a position or open interest. */
    public static final String CONTRACTS_UNIT = "{contracts}";

    /** The UCUM unit of a share of open interest. */
    public static final String PERCENT_UNIT = "%";

    /** The release lag: Tuesday positions are published on Friday. */
    public static final double RELEASE_LAG_DAYS = 3;

    /** The report date column. */
    static final String DATE_COLUMN = "report_date_as_yyyy_mm_dd";

    /** The contract market code column. */
    static final String CODE_COLUMN = "cftc_contract_market_code";

    /** The market and exchange name column. */
    static final String NAME_COLUMN = "market_and_exchange_names";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private CotParser() {}

    /**
     * The rows of one Socrata page.
     *
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static JsonNode rows(byte[] body) {
        if (body == null || body.length == 0) {
            return JSON.createArrayNode();
        }
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("cftc-cot: the response is not JSON: " + e.getOriginalMessage(), null,
                    e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("cftc-cot: the response is not a JSON array", null);
        }
        return root;
    }

    /**
     * The points of {@code rows}.
     *
     * @param report the report the rows belong to
     * @param rows the Socrata rows
     * @param contracts the contract market codes to keep; empty keeps every row
     * @param observedAt the fetch time
     */
    public static List<NormalizedRecord> parse(CotReport report, JsonNode rows, Set<String> contracts,
            Instant observedAt) {
        List<NormalizedRecord> out = new ArrayList<>();
        if (rows == null) {
            return out;
        }
        for (JsonNode row : rows) {
            if (row == null || !row.isObject()) {
                continue;
            }
            String code = JsonProps.text(row, CODE_COLUMN);
            LocalDate date = reportDate(JsonProps.text(row, DATE_COLUMN));
            if (code == null || date == null || (contracts != null && !contracts.isEmpty()
                    && !contracts.contains(code))) {
                continue;
            }
            String market = JsonProps.text(row, NAME_COLUMN);
            Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            for (CotReport.Measure measure : report.measures()) {
                Double value = value(row, measure);
                if (value == null) {
                    continue;
                }
                String seriesId = seriesId(code, report, measure);
                Map<String, Object> extra = new LinkedHashMap<>();
                extra.put("contractCode", code);
                extra.put("report", report.wire());
                extra.put("measure", measure.id());
                extra.put("futOnlyOrCombined", JsonProps.text(row, "futonly_or_combined"));
                String name = (market == null ? code : market) + " - " + report.wire() + " " + measure.label();
                out.add(SeriesPoint.point(seriesId, name, report.wire(), FREQUENCY, date.toString(), start, end,
                        value, measure.kind() == CotReport.Kind.NET_PCT_OI ? PERCENT_UNIT : CONTRACTS_UNIT,
                        market, extra, "final", observedAt));
            }
        }
        return out;
    }

    /** {@code cot.{code}.{report}.{measure}}. */
    public static String seriesId(String code, CotReport report, CotReport.Measure measure) {
        return "cot." + code + "." + report.wire() + "." + measure.id();
    }

    /** The series metadata of a COT point: open interest or position, difference, a 3-day release lag. */
    public static SeriesMeta meta(NormalizedRecord point) {
        Object extra = point.payload().get("extra");
        boolean oi = extra instanceof Map<?, ?> map && "oi".equals(map.get("measure"));
        return SeriesMeta.of(oi ? SeriesMeta.OPEN_INTEREST : SeriesMeta.POSITION, SeriesMeta.DIFFERENCE,
                RELEASE_LAG_DAYS);
    }

    /** The value of {@code measure} in {@code row}, or null when a column it needs is missing or not numeric. */
    static Double value(JsonNode row, CotReport.Measure measure) {
        Double first = firstNumber(row, measure.longColumns());
        if (first == null) {
            return null;
        }
        if (measure.kind() == CotReport.Kind.LEVEL) {
            return first;
        }
        Double shorts = firstNumber(row, measure.shortColumns());
        if (shorts == null) {
            return null;
        }
        double net = first - shorts;
        if (measure.kind() == CotReport.Kind.NET) {
            return net;
        }
        Double oi = JsonProps.number(row, CotReport.OPEN_INTEREST);
        if (oi == null || oi <= 0) {
            return null;
        }
        return Math.round(net / oi * 100.0 * 1000.0) / 1000.0;
    }

    private static Double firstNumber(JsonNode row, List<String> columns) {
        for (String column : columns) {
            Double value = JsonProps.number(row, column);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /** The {@code YYYY-MM-DD} date of a Socrata floating timestamp ({@code 2026-10-13T00:00:00.000}), or null. */
    static LocalDate reportDate(String text) {
        if (text == null || text.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(text.substring(0, 10));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** The newest report date among {@code rows}, or null when none has one. */
    public static LocalDate newest(JsonNode rows) {
        LocalDate newest = null;
        if (rows != null) {
            for (JsonNode row : rows) {
                LocalDate date = reportDate(JsonProps.text(row, DATE_COLUMN));
                if (date != null && (newest == null || date.isAfter(newest))) {
                    newest = date;
                }
            }
        }
        return newest;
    }

    /** The oldest report date among {@code rows}, or null when none has one. */
    public static LocalDate oldest(JsonNode rows) {
        LocalDate oldest = null;
        if (rows != null) {
            for (JsonNode row : rows) {
                LocalDate date = reportDate(JsonProps.text(row, DATE_COLUMN));
                if (date != null && (oldest == null || date.isBefore(oldest))) {
                    oldest = date;
                }
            }
        }
        return oldest;
    }

    /** How many distinct report dates {@code rows} hold. */
    public static int distinctDates(JsonNode rows) {
        Set<LocalDate> dates = new java.util.HashSet<>();
        if (rows != null) {
            for (JsonNode row : rows) {
                LocalDate date = reportDate(JsonProps.text(row, DATE_COLUMN));
                if (date != null) {
                    dates.add(date);
                }
            }
        }
        return dates.size();
    }
}
