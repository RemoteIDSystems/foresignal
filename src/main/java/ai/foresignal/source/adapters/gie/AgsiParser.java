package ai.foresignal.source.adapters.gie;

import ai.foresignal.source.series.SeriesPeriods;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One GIE AGSI {@code GET /api} answer as gas-storage records (prompt 093). Pure.
 *
 * <p>AGSI returns HTTP 200 with a body like {@code {"error":"access denied",...}} for a missing or invalid key, so
 * {@link #checkAuth} inspects the body rather than the status line. Each row is one area and gas day: the value is
 * {@code full} (%), the extras are the raw storage, injection, withdrawal, working-volume, trend and consumption
 * numbers, and the status is {@code E} (estimated), {@code C} (confirmed) or {@code N} (no data, skipped).
 */
public final class AgsiParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private AgsiParser() {}

    /**
     * Fails the fetch when AGSI answered its HTTP-200 access-denied body.
     *
     * @throws CredentialsRejectedException when the body carries an error
     * @throws UpstreamFormatException when the body is not JSON
     */
    public static void checkAuth(byte[] body) throws CredentialsRejectedException {
        JsonNode root = root(body);
        JsonNode error = root.get("error");
        if (error != null && !error.isNull()) {
            String message = JsonProps.text(root, "message");
            throw new CredentialsRejectedException("GIE AGSI refused the key: "
                    + (message != null ? message : error.asString()));
        }
    }

    /** Every row of the answer, one per area and gas day, with status N skipped. */
    public static List<NormalizedRecord> parse(byte[] body) {
        JsonNode root = root(body);
        JsonNode data = root.get("data");
        if (data == null || !data.isArray()) {
            throw new UpstreamFormatException("AGSI answer has no data array", "");
        }
        List<NormalizedRecord> out = new ArrayList<>();
        for (JsonNode row : data) {
            NormalizedRecord record = record(row);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    static NormalizedRecord record(JsonNode row) {
        String code = JsonProps.text(row, "code");
        String period = JsonProps.text(row, "gasDayStart");
        SeriesPeriods.Period bounds = SeriesPeriods.parse("P1D", period);
        Double full = SeriesPoint.number(JsonProps.text(row, "full"));
        if (code == null || period == null || bounds == null || full == null) {
            return null;
        }
        String status = status(JsonProps.text(row, "status"));
        if (status == null) {
            return null;
        }
        String name = JsonProps.text(row, "name");
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("gasInStorageTWh", SeriesPoint.number(JsonProps.text(row, "gasInStorage")));
        extra.put("injectionGWhD", SeriesPoint.number(JsonProps.text(row, "injection")));
        extra.put("withdrawalGWhD", SeriesPoint.number(JsonProps.text(row, "withdrawal")));
        extra.put("netWithdrawalGWhD", SeriesPoint.number(JsonProps.text(row, "netWithdrawal")));
        extra.put("workingGasVolumeTWh", SeriesPoint.number(JsonProps.text(row, "workingGasVolume")));
        extra.put("trend", SeriesPoint.number(JsonProps.text(row, "trend")));
        extra.put("consumptionFull", SeriesPoint.number(JsonProps.text(row, "consumptionFull")));
        return SeriesPoint.point("agsi:" + code, name != null ? name : code, "agsi/storage", "P1D", period,
                bounds.start(), bounds.end(), full, "%", code, extra, status, bounds.end());
    }

    /** AGSI's status letter as the series status; null for {@code N} (no data). */
    static String status(String code) {
        if (code == null) {
            return "final";
        }
        return switch (code.strip().toUpperCase()) {
            case "C" -> "final";
            case "E" -> "estimated";
            case "N", "" -> null;
            default -> "final";
        };
    }

    private static JsonNode root(byte[] body) {
        try {
            JsonNode root = body == null || body.length == 0 ? null : JSON.readTree(body);
            if (root == null || !root.isObject()) {
                throw new UpstreamFormatException("AGSI answer is empty or not an object", "");
            }
            return root;
        } catch (JacksonException e) {
            throw new UpstreamFormatException("AGSI answer is not JSON: " + e.getOriginalMessage(), "", e);
        }
    }
}
