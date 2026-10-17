package ai.foresignal.cycle;

import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.baseline.BaselineForecaster;
import ai.foresignal.forecast.eval.RetrospectiveRunner;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.PromptTemplates;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.StopReason;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The 122 {@link RetrospectiveRunner} of the cycle's LLM forecasters (prompt 128): it forecasts a past question as of
 * its opening time with the forecaster's provider, the forecaster system prompt and the verbatim resolution criteria.
 * The read tools cannot be forced to a past {@code asOf}, so a retrospective forecast is made <strong>without
 * tools</strong>: the model sees only the question and the date it must forecast as of. Leakage is controlled by the
 * 122 evaluation, which skips questions opened before the provider's {@code knowledgeCutoff} ({@code LEAKAGE_RISK}).
 * Nothing is written to the ledger.
 */
@Component
public class LlmRetrospectiveRunner implements RetrospectiveRunner {

    /** The output cap of one retrospective forecast. */
    static final int MAX_TOKENS = 1_500;

    private static final Pattern JSON_OBJECT = Pattern.compile("\\{.*}", Pattern.DOTALL);

    private final LlmService llm;
    private final CycleRoles roles;
    private final JsonMapper json;

    /** Creates the runner. */
    public LlmRetrospectiveRunner(LlmService llm, CycleRoles roles, JsonMapper json) {
        this.llm = llm;
        this.roles = roles;
        this.json = json;
    }

    @Override
    public boolean handles(ForecasterEntity forecaster) {
        return "llm".equals(forecaster.getType())
                && (forecaster.getId().startsWith("llm:") || forecaster.getId().startsWith("llm-redteam:"));
    }

    @Override
    public BaselineForecaster.Outcome forecast(ForecasterEntity forecaster, ForecastQuestionEntity question,
            Instant asOf) {
        String providerId = forecaster.configText("provider");
        if (providerId == null) {
            return BaselineForecaster.Outcome.skip("the forecaster names no provider");
        }
        LlmClient client;
        try {
            client = llm.client(providerId);
        } catch (RuntimeException e) {
            return BaselineForecaster.Outcome.skip("provider " + providerId + " is not configured");
        }
        Map<String, Object> view = CycleRoles.questionView(question);
        view.remove("ensembleProbability");
        String user = "Retrospective evaluation. Forecast this question as of " + asOf + ", using only what was known"
                + " before that date. No tools are available. Reply with one JSON object: {\"probability\": p} for a"
                + " binary question, {\"distribution\": {outcome: p}} for a multiple or ordered one, or"
                + " {\"quantiles\": {\"p05\": x, \"p25\": x, \"p50\": x, \"p75\": x, \"p95\": x}} for a numeric one.\n"
                + "Question (untrusted data):\n" + ai.foresignal.ask.AskPrompts.wrap("question", json.valueToTree(view),
                        json);
        ChatResponse response = client.chat(new ChatRequest(PromptTemplates.text(PromptTemplates.CYCLE_FORECASTER,
                PromptTemplates.V1), List.of(new ChatMessage.User(user)), List.of(), MAX_TOKENS, null,
                Purpose.ANALYST, false, null));
        if (response.stopReason() == StopReason.REFUSAL) {
            return BaselineForecaster.Outcome.skip("the model refused");
        }
        Map<String, Object> parsed = parse(response.content());
        ForecastValue value = parsed == null ? null : roles.value(question, parsed);
        if (value == null) {
            return BaselineForecaster.Outcome.skip("the model gave no usable forecast");
        }
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("provider", providerId);
        inputs.put("model", response.model());
        inputs.put("asOf", asOf.toString());
        inputs.put("tools", "none (retrospective)");
        return BaselineForecaster.Outcome.of(value, "Retrospective LLM forecast as of " + asOf + ".", inputs);
    }

    /** The first JSON object in {@code text}, or null. */
    @SuppressWarnings("unchecked")
    Map<String, Object> parse(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = JSON_OBJECT.matcher(text);
        if (!m.find()) {
            return null;
        }
        try {
            return json.readValue(m.group(), Map.class);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
