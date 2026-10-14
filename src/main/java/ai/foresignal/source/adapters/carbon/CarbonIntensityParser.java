package ai.foresignal.source.adapters.carbon;

import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The GB Carbon Intensity API answers as series records (prompt 093). Pure.
 *
 * <p>{@code /intensity} and {@code /intensity/pt24h} carry national half-hours ({@code intensity.forecast} and
 * {@code intensity.actual}); {@code /regional} carries per-region forecasts (no actual); {@code /generation} carries
 * the generation-mix percentages.
 */
public final class CarbonIntensityParser {

    /** The national series id. */
    public static final String NATIONAL = "carbon:gb:national";

    /** The generation-mix series id. */
    public static final String GENMIX = "genmix:gb";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private CarbonIntensityParser() {}

    /** The national points of {@code /intensity} or {@code /intensity/pt24h}. */
    public static List<NormalizedRecord> national(byte[] body) {
        JsonNode data = array(body);
        List<NormalizedRecord> out = new ArrayList<>();
        for (JsonNode entry : data) {
            Instant from = JsonProps.iso(entry, "from");
            Instant to = JsonProps.iso(entry, "to");
            if (from == null || to == null) {
                continue;
            }
            JsonNode intensity = entry.get("intensity");
            Double forecast = JsonProps.number(intensity, "forecast");
            Double actual = JsonProps.number(intensity, "actual");
            Double value = actual != null ? actual : forecast;
            if (value == null) {
                continue;
            }
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("forecast", forecast);
            extra.put("index", JsonProps.text(intensity, "index"));
            out.add(SeriesPoint.point(NATIONAL, "GB national carbon intensity", "carbon-intensity", "PT30M",
                    from.toString(), from, to, value, "g/(kW.h)", "GB", extra,
                    actual != null ? "final" : "forecast", from));
        }
        return out;
    }

    /** The per-region points of {@code /regional} (forecast only). */
    public static List<NormalizedRecord> regional(byte[] body) {
        JsonNode data = array(body);
        List<NormalizedRecord> out = new ArrayList<>();
        for (JsonNode entry : data) {
            Instant from = JsonProps.iso(entry, "from");
            Instant to = JsonProps.iso(entry, "to");
            JsonNode regions = entry.get("regions");
            if (from == null || to == null || regions == null || !regions.isArray()) {
                continue;
            }
            for (JsonNode region : regions) {
                Long regionId = JsonProps.integer(region, "regionid");
                Double forecast = JsonProps.number(region.get("intensity"), "forecast");
                if (regionId == null || forecast == null) {
                    continue;
                }
                String shortname = JsonProps.text(region, "shortname");
                Map<String, Object> extra = new LinkedHashMap<>();
                extra.put("shortname", shortname);
                out.add(SeriesPoint.point("carbon:gb:region:" + regionId,
                        shortname != null ? shortname : "Region " + regionId, "carbon-intensity", "PT30M",
                        from.toString(), from, to, forecast, "g/(kW.h)", "GB", extra, "forecast", from));
            }
        }
        return out;
    }

    /** The single generation-mix point of {@code /generation}: the fuel percentages in {@code extra}. */
    public static List<NormalizedRecord> generation(byte[] body) {
        JsonNode root = object(body);
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new UpstreamFormatException("Carbon Intensity answer has no data object", "");
        }
        Instant from = JsonProps.iso(data, "from");
        Instant to = JsonProps.iso(data, "to");
        JsonNode mix = data.get("generationmix");
        if (from == null || to == null || mix == null || !mix.isArray()) {
            return List.of();
        }
        Map<String, Object> extra = new LinkedHashMap<>();
        for (JsonNode fuel : mix) {
            String name = JsonProps.text(fuel, "fuel");
            Double percent = JsonProps.number(fuel, "perc");
            if (name != null && percent != null) {
                extra.put(name, percent);
            }
        }
        return List.of(SeriesPoint.point(GENMIX, "GB generation mix", "generation-mix", "PT30M", from.toString(),
                from, to, null, null, "GB", extra, "final", from));
    }

    private static JsonNode array(byte[] body) {
        JsonNode data = object(body).get("data");
        if (data == null || !data.isArray()) {
            throw new UpstreamFormatException("Carbon Intensity answer has no data array", "");
        }
        return data;
    }

    private static JsonNode object(byte[] body) {
        try {
            JsonNode root = body == null || body.length == 0 ? null : JSON.readTree(body);
            if (root == null || !root.isObject()) {
                throw new UpstreamFormatException("Carbon Intensity answer is empty or not an object", "");
            }
            return root;
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Carbon Intensity answer is not JSON: " + e.getOriginalMessage(), "",
                    e);
        }
    }
}
