package ai.foresignal.source.adapters.fingrid;

import ai.foresignal.source.series.FrequencyExcursionDetector;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Fingrid dataset 177 (frequency) as detector samples (prompt 093). Pure.
 *
 * <p>The answer is {@code {data: [{startTime, endTime, value}], pagination: {...}}}; the value is the system
 * frequency in Hz.
 */
public final class FingridFrequencyParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private FingridFrequencyParser() {}

    /** The samples in the answer, oldest first. */
    public static List<FrequencyExcursionDetector.Sample> parse(byte[] body) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Fingrid frequency answer is not JSON: " + e.getOriginalMessage(), "",
                    e);
        }
        JsonNode data = root == null ? null : root.get("data");
        if (data == null || !data.isArray()) {
            throw new UpstreamFormatException("Fingrid frequency answer has no data array", "");
        }
        List<FrequencyExcursionDetector.Sample> out = new ArrayList<>();
        for (JsonNode row : data) {
            Instant at = JsonProps.iso(row, "startTime");
            Double hz = JsonProps.number(row, "value");
            if (at != null && hz != null) {
                out.add(new FrequencyExcursionDetector.Sample(at, hz));
            }
        }
        return out;
    }
}
