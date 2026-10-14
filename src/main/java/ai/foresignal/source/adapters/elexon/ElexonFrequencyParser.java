package ai.foresignal.source.adapters.elexon;

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
 * Elexon BMRS {@code /system/frequency} as detector samples (prompt 093). Pure.
 *
 * <p>The answer is {@code {metadata: {datasets: [FREQ]}, data: [{measurementTime, frequency}]}}, one 15-second sample
 * per entry, about two minutes behind real time (verified 2026-10).
 */
public final class ElexonFrequencyParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private ElexonFrequencyParser() {}

    /** The samples in the answer, oldest first. */
    public static List<FrequencyExcursionDetector.Sample> parse(byte[] body) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Elexon frequency answer is not JSON: " + e.getOriginalMessage(), "",
                    e);
        }
        JsonNode data = root == null ? null : root.get("data");
        if (data == null || !data.isArray()) {
            throw new UpstreamFormatException("Elexon frequency answer has no data array", "");
        }
        List<FrequencyExcursionDetector.Sample> out = new ArrayList<>();
        for (JsonNode row : data) {
            Instant at = JsonProps.iso(row, "measurementTime");
            Double hz = JsonProps.number(row, "frequency");
            if (at != null && hz != null) {
                out.add(new FrequencyExcursionDetector.Sample(at, hz));
            }
        }
        return out;
    }
}
