package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.Actors;
import ai.foresignal.forecast.ForecastEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The on-demand analysis-request endpoints (prompt 155, tag {@code External}): request a forecast of a question
 * now from an external forecaster, and the provider's callback that returns the correlated result.
 */
@RestController
@Tag(name = "External")
public class AnalysisRequestController {

    private final AnalysisRequestService service;

    /** Creates the controller. */
    public AnalysisRequestController(AnalysisRequestService service) {
        this.service = service;
    }

    /**
     * {@code requestAnalysis}: asks the external forecaster {@code forecasterId} to forecast {@code questionId}
     * now. Returns the correlation id; a synchronous provider's forecast is recorded at once.
     */
    @PostMapping("/v1/external/analysis/requests")
    @Operation(operationId = "requestAnalysis", tags = "External")
    public Map<String, Object> requestAnalysis(@RequestBody JsonNode body, HttpServletRequest request) {
        String forecasterId = text(body, "forecasterId");
        String questionId = text(body, "questionId");
        if (forecasterId == null || questionId == null) {
            throw ValidationException.forField("forecasterId", "forecasterId and questionId are required");
        }
        String orientation = text(body, "orientation") == null ? BindingVocabulary.SAME : text(body, "orientation");
        String horizon = text(body, "horizon");
        AnalysisRequestService.Requested requested;
        try {
            requested = service.request(forecasterId, questionId, orientation, horizon, Actors.of(request));
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("correlationId", requested.correlationId());
        out.put("recorded", requested.recorded());
        return out;
    }

    /**
     * {@code analysisCallback}: a provider returns the forecast for a correlation. Records it against the
     * correlated question and forecaster.
     */
    @PostMapping("/v1/external/analysis/callback")
    @Operation(operationId = "analysisCallback", tags = "External")
    public Map<String, Object> analysisCallback(@RequestBody JsonNode body) {
        String correlationId = text(body, "correlationId");
        if (correlationId == null) {
            throw ValidationException.forField("correlationId", "is required");
        }
        Double probability = body.path("probability").isNumber() ? body.get("probability").doubleValue() : null;
        Map<String, Double> quantiles = quantiles(body.get("quantiles"));
        Optional<ForecastEntity> recorded = service.fulfill(correlationId, probability, quantiles);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("correlationId", correlationId);
        out.put("recorded", recorded.isPresent());
        recorded.ifPresent(f -> out.put("forecastId", f.getId()));
        return out;
    }

    private static Map<String, Double> quantiles(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Double> out = new LinkedHashMap<>();
        node.properties().forEach(e -> {
            if (e.getValue().isNumber()) {
                out.put(e.getKey(), e.getValue().doubleValue());
            }
        });
        return out.isEmpty() ? null : out;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() || !value.isValueNode() ? null : value.asString();
    }
}
