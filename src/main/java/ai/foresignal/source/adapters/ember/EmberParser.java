package ai.foresignal.source.adapters.ember;

import ai.foresignal.source.series.SeriesPeriods;
import ai.foresignal.source.series.SeriesPoint;
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
 * Ember monthly answers as series records (prompt 093). Pure.
 *
 * <p>Rows are {@code {entity, entity_code, date (YYYY-MM), series, generation_twh, share_of_generation_pct}} for the
 * generation dataset; the carbon-intensity and demand datasets carry one value per entity and month.
 */
public final class EmberParser {

    /** The generation dataset. */
    public static final String GENERATION = "electricity-generation";

    /** The carbon-intensity dataset. */
    public static final String CARBON_INTENSITY = "carbon-intensity";

    /** The demand dataset. */
    public static final String DEMAND = "electricity-demand";

    /** The datasets this source reads. */
    public static final List<String> DATASETS = List.of(GENERATION, CARBON_INTENSITY, DEMAND);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private EmberParser() {}

    /** One data row's entity code and month, for the options short-circuit. */
    public record Dated(String seriesId, String month) {}

    /**
     * Parses a monthly data answer.
     *
     * @param dataset one of {@link #DATASETS}
     */
    public static List<NormalizedRecord> parse(byte[] body, String dataset) {
        JsonNode data = data(body);
        List<NormalizedRecord> out = new ArrayList<>();
        for (JsonNode row : data) {
            NormalizedRecord record = record(row, dataset);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    static NormalizedRecord record(JsonNode row, String dataset) {
        String entity = JsonProps.text(row, "entity");
        String code = JsonProps.text(row, "entity_code");
        String month = JsonProps.text(row, "date");
        SeriesPeriods.Period bounds = SeriesPeriods.parse("P1M", month);
        if (code == null || month == null || bounds == null) {
            return null;
        }
        String seriesName = entity != null ? entity : code;
        return switch (dataset) {
            case CARBON_INTENSITY -> SeriesPoint.point("ember:" + code + ":carbon-intensity", seriesName,
                    CARBON_INTENSITY, "P1M", month, bounds.start(), bounds.end(),
                    SeriesPoint.number(JsonProps.text(row, "carbon_intensity_g_kwh")), "g/(kW.h)", code, null,
                    "final", bounds.end());
            case DEMAND -> SeriesPoint.point("ember:" + code + ":demand", seriesName, DEMAND, "P1M", month,
                    bounds.start(), bounds.end(), SeriesPoint.number(JsonProps.text(row, "electricity_demand_twh")),
                    "TW.h", code, null, "final", bounds.end());
            default -> {
                String series = JsonProps.text(row, "series");
                Double value = SeriesPoint.number(JsonProps.text(row, "generation_twh"));
                if (series == null || value == null) {
                    yield null;
                }
                Map<String, Object> extra = new LinkedHashMap<>();
                extra.put("sharePct", SeriesPoint.number(JsonProps.text(row, "share_of_generation_pct")));
                yield SeriesPoint.point("ember:" + code + ":" + series, seriesName, GENERATION, "P1M", month,
                        bounds.start(), bounds.end(), value, "TW.h", code, extra, "final", bounds.end());
            }
        };
    }

    /**
     * The newest month in an options {@code /date} answer, or null.
     *
     * <p>The answer lists the months for which data exists; the newest {@code YYYY-MM} anywhere in it is the
     * change marker.
     */
    public static String newestMonth(byte[] body) {
        String text = body == null ? "" : new String(body, java.nio.charset.StandardCharsets.UTF_8);
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d{4}-\\d{2})").matcher(text);
        String newest = null;
        while (matcher.find()) {
            String month = matcher.group(1);
            if (newest == null || month.compareTo(newest) > 0) {
                newest = month;
            }
        }
        return newest;
    }

    private static JsonNode data(byte[] body) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Ember answer is not JSON: " + e.getOriginalMessage(), "", e);
        }
        JsonNode data = root == null ? null : root.get("data");
        if (data == null || !data.isArray()) {
            throw new UpstreamFormatException("Ember answer has no data array", "");
        }
        return data;
    }
}
