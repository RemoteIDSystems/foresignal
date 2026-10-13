package ai.foresignal.weather;

import ai.foresignal.ask.CitableExtractor;
import ai.foresignal.ask.CitationSet;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Makes weather tool results citable in Ask (prompt 056): every object in a weather tool's output that carries a
 * {@code citationId} starting with {@code wx:} becomes a citable weather value (product, frame key, run, step, valid
 * time, age), and every object of such a result whose {@code status} is not {@code ok} is recorded as a weather issue
 * for {@link WeatherAnswerGuard}. The weather tools are {@value #TOOLS_TEXT}.
 */
@Component
public class WeatherCitationExtractor implements CitableExtractor {

    /** The weather tools, as text for documentation. */
    static final String TOOLS_TEXT = "sample_weather, weather_forecast, area_weather_stats and get_map_image";

    /** The weather tools. */
    public static final Set<String> TOOLS = Set.of("sample_weather", "weather_forecast", "area_weather_stats",
            "get_map_image");

    /** The deepest object visited. */
    static final int MAX_DEPTH = 8;

    @Override
    public void extract(String tool, JsonNode output, CitationSet citable) {
        if (output == null) {
            return;
        }
        boolean weatherTool = TOOLS.contains(tool);
        visit(output, citable, weatherTool, 0);
    }

    private static void visit(JsonNode node, CitationSet citable, boolean weatherTool, int depth) {
        if (node == null || depth > MAX_DEPTH) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                visit(item, citable, weatherTool, depth + 1);
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }
        String citationId = text(node, "citationId");
        String productId = text(node, "productId");
        if (productId == null) {
            productId = text(node, "provider");
        }
        if (citationId != null && citationId.startsWith(WeatherCitations.PREFIX)) {
            citable.addWeather(new CitationSet.Weather(citationId, title(node, productId), productId,
                    text(node, "frameKey"), text(node, "run"), node.hasNonNull("step") && node.get("step").isNumber()
                            ? node.get("step").asInt() : null, text(node, "validTime"),
                    node.hasNonNull("ageMinutes") && node.get("ageMinutes").isNumber()
                            ? node.get("ageMinutes").asDouble() : null));
        }
        String status = text(node, "status");
        if (weatherTool && status != null && !WeatherSections.OK.equals(status) && productId != null) {
            citable.addWeatherIssue(new CitationSet.WeatherIssue(productId, status, text(node, "statusText")));
        }
        for (var entry : node.properties()) {
            JsonNode child = entry.getValue();
            if (child.isObject() || child.isArray()) {
                visit(child, citable, weatherTool, depth + 1);
            }
        }
    }

    private static String title(JsonNode node, String productId) {
        String label = text(node, "label");
        String valid = text(node, "validTime");
        StringBuilder out = new StringBuilder(productId == null ? "weather" : productId);
        if (label != null) {
            out.append(": ").append(label);
        }
        if (valid != null) {
            out.append(" (valid ").append(valid).append(')');
        }
        return out.toString();
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isValueNode() ? null : value.asString();
    }
}
