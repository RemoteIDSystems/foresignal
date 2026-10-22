package ai.foresignal.inbound.edp.binding;

import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.baseline.BaselineForecaster;
import ai.foresignal.forecast.eval.RetrospectiveRunner;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Backtests an external forecaster (prompt 155) by issuing a synchronous as-of analysis request for each
 * question, so {@code type=external} forecasters are no longer skipped by the retrospective evaluator. The
 * forecaster's provider must declare {@code transport.analysisRequest} and answer the as-of request
 * synchronously; a provider that only pushes or answers asynchronously cannot be backtested and each question
 * is skipped.
 */
@Component
public class ExternalRetrospectiveRunner implements RetrospectiveRunner {

    private final AnalysisRequestService analysis;

    /** Creates the runner. */
    public ExternalRetrospectiveRunner(AnalysisRequestService analysis) {
        this.analysis = analysis;
    }

    @Override
    public boolean handles(ForecasterEntity forecaster) {
        return "external".equals(forecaster.getType());
    }

    @Override
    public BaselineForecaster.Outcome forecast(ForecasterEntity forecaster, ForecastQuestionEntity question,
            Instant asOf) {
        String sourceId = string(forecaster.getConfig().get("sourceId"));
        String streamId = string(forecaster.getConfig().get("streamId"));
        if (sourceId == null || streamId == null) {
            String[] parsed = AnalysisRequestService.parse(forecaster.getId());
            sourceId = parsed[0];
            streamId = parsed[1];
        }
        ForecastInputs.ForecastValue value = analysis.forecastSyncAsOf(sourceId, streamId, question, asOf);
        if (value == null) {
            return BaselineForecaster.Outcome.skip("the external forecaster " + forecaster.getId()
                    + " returned no usable on-demand forecast as of " + asOf);
        }
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("forecaster", forecaster.getId());
        inputs.put("asOf", asOf.toString());
        inputs.put("via", "analysisRequest");
        return BaselineForecaster.Outcome.of(value, "on-demand analysis request as of " + asOf, inputs);
    }

    private static String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
