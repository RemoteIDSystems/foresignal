package ai.foresignal.source.adapters.eia;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The EIA v2 JSON envelope shared by every route (prompt 093). Pure.
 *
 * <p>The shape is {@code {response: {total, dateFormat, frequency, data[], warnings[]}}}; each {@code data} entry is
 * {@code {period, series|respondent|fueltype, value (string), units|value-units}}.
 *
 * <p>A {@code warnings} entry naming an "incomplete return" means the 5,000-row cap was hit; the window must be
 * narrowed rather than silently truncated, so it throws {@link UpstreamFormatException}.
 */
public final class EiaParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private EiaParser() {}

    /**
     * One EIA answer's data rows.
     *
     * @param data the {@code response.data} entries
     * @param frequency the answer's frequency code, for example {@code daily} or {@code hourly}
     * @param total the upstream's row count
     */
    public record Envelope(List<JsonNode> data, String frequency, long total) {}

    /**
     * Parses an EIA envelope.
     *
     * @throws UpstreamFormatException when the body is not an EIA answer or the two-hour window was truncated
     */
    public static Envelope parse(byte[] body) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("EIA answer is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("EIA answer is empty or not an object", "");
        }
        if (root.has("error")) {
            throw new UpstreamFormatException("EIA answered an error: " + root.get("error"), "");
        }
        JsonNode response = root.get("response");
        if (response == null || !response.isObject()) {
            throw new UpstreamFormatException("EIA answer has no response object", "");
        }
        checkWarnings(response.get("warnings"));
        JsonNode data = response.get("data");
        if (data == null || !data.isArray()) {
            throw new UpstreamFormatException("EIA answer has no data array", "");
        }
        List<JsonNode> rows = new ArrayList<>(data.size());
        data.forEach(rows::add);
        long total = JsonProps.integer(response, "total") == null ? rows.size()
                : JsonProps.integer(response, "total");
        return new Envelope(rows, JsonProps.text(response, "frequency"), total);
    }

    private static void checkWarnings(JsonNode warnings) {
        if (warnings == null || !warnings.isArray()) {
            return;
        }
        for (JsonNode warning : warnings) {
            String text = warning.isString() ? warning.stringValue() : JsonProps.text(warning, "warning");
            if (text != null && text.toLowerCase().contains("incomplete return")) {
                throw new UpstreamFormatException(
                        "EIA hit its 5,000-row cap (incomplete return): narrow the window or request fewer rows",
                        text);
            }
        }
    }

    /** The {@code value} of one data row as a finite number, or null (EIA sends it as a string). */
    public static Double value(JsonNode row) {
        return ai.foresignal.source.series.SeriesPoint.number(JsonProps.text(row, "value"));
    }

    /** The unit of one data row: {@code value-units} (EIA-930) or {@code units} (petroleum). */
    public static String units(JsonNode row) {
        String valueUnits = JsonProps.text(row, "value-units");
        return valueUnits != null ? valueUnits : JsonProps.text(row, "units");
    }
}
