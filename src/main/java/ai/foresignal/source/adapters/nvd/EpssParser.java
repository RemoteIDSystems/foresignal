package ai.foresignal.source.adapters.nvd;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One FIRST.org EPSS API response ({@code {status, total, limit, data:[{cve, epss, percentile, date}]}}, verified
 * 2026-10-13) as {@code cve-epss} records (prompt 076). Pure.
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code "epss:" + cve}, so the bands of a CVE never collide with its
 *       {@code vulnerability} record in the same source.
 *   <li><strong>Time:</strong> {@code observedAt = date}, the model date at 00:00Z (upstream; fixed per model run).
 *   <li><strong>Payload:</strong> {@code cveId, epssBand, percentileBand} ({@link EpssBands}) and {@code modelDate}
 *       (volatile, so a new model run that keeps both bands is a touch). The raw {@code epss} and {@code percentile}
 *       values are never stored.
 * </ul>
 * Ids asked for but absent from the response (a CVE too new to be scored) are reported in {@link Result#missing()};
 * rows for ids that were not asked for are ignored.
 */
public final class EpssParser {

    /** The record kind. */
    public static final String KIND = "cve-epss";

    /** The externalId prefix. */
    public static final String ID_PREFIX = "epss:";

    private static final Pattern CVE = Pattern.compile("^CVE-\\d{4}-\\d{4,}$");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed response.
     *
     * @param records one record per scored CVE
     * @param missing requested ids without a score
     * @param malformed rows that could not be read
     */
    public record Result(List<NormalizedRecord> records, List<String> missing, int malformed) {}

    private EpssParser() {}

    /**
     * Parses one response.
     *
     * @param body the API response
     * @param requested the ids asked for; null accepts every row
     * @throws UpstreamFormatException when the body is not an EPSS response with status OK
     */
    public static Result parse(byte[] body, Collection<String> requested) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("EPSS response is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isObject() || !root.path("data").isArray()
                || !"OK".equalsIgnoreCase(JsonProps.text(root, "status"))) {
            throw new UpstreamFormatException("EPSS response has no data array or its status is not OK", "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        int malformed = 0;
        Set<String> asked = requested == null ? null : Set.copyOf(requested);
        for (JsonNode row : root.get("data")) {
            NormalizedRecord record = row(row);
            if (record == null) {
                malformed++;
            } else if (asked != null && !asked.contains((String) record.payload().get("cveId"))) {
                continue;
            } else if (seen.add((String) record.payload().get("cveId"))) {
                records.add(record);
            }
        }
        List<String> missing = new ArrayList<>();
        if (requested != null) {
            for (String id : requested) {
                if (!seen.contains(id)) {
                    missing.add(id);
                }
            }
        }
        return new Result(records, missing, malformed);
    }

    /**
     * One data row as a record, or null when the id, a score or the date cannot be read.
     *
     * @param row a {@code data} element
     */
    public static NormalizedRecord row(JsonNode row) {
        String cve = JsonProps.text(row, "cve");
        BigDecimal epss = decimal(JsonProps.text(row, "epss"));
        BigDecimal percentile = decimal(JsonProps.text(row, "percentile"));
        String date = JsonProps.text(row, "date");
        Instant modelDate = date(date);
        if (cve == null || !CVE.matcher(cve).matches() || epss == null || percentile == null || modelDate == null) {
            return null;
        }
        return NormalizedRecord.builder(ID_PREFIX + cve, KIND).observedAt(modelDate)
                .put("cveId", cve)
                .put("epssBand", EpssBands.epssBand(epss))
                .put("percentileBand", EpssBands.percentileBand(percentile))
                .put("modelDate", date)
                .build();
    }

    /** A decimal in 0–1, or null. */
    static BigDecimal decimal(String value) {
        if (value == null) {
            return null;
        }
        try {
            BigDecimal d = new BigDecimal(value);
            return d.signum() < 0 || d.compareTo(BigDecimal.ONE) > 0 ? null : d;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Instant date(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
