package ai.foresignal.forecast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link ForecastInputs} (prompt 122): question bodies, forecasts of every shape against their question, manual
 * resolutions, forecaster updates and evaluation requests, with the Problem codes the API answers.
 */
class ForecastInputsTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    static JsonNode json(String text) {
        return JSON.readTree(text);
    }

    static final String QUESTION = "{\"title\":\"Will it happen?\",\"type\":\"binary\",\"resolutionCriteria\":\"Yes if"
            + " it happens.\",\"resolver\":{\"type\":\"manual\"},\"closesAt\":\"2026-11-01T00:00:00Z\","
            + "\"resolveBy\":\"2026-11-02T00:00:00Z\",\"category\":\"conflict\"}";

    static ForecastQuestionEntity question(String type, List<String> labels) {
        ForecastQuestionEntity q = new ForecastQuestionEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", NOW);
        q.setType(type);
        q.setOutcomes(labels.isEmpty() ? Map.of() : Map.of("labels", labels));
        return q;
    }

    @Test
    @DisplayName("a question: defaults, validation of times, labels and links, and RESOLVER_INVALID")
    void questions() {
        ForecastInputs.QuestionDraft d = ForecastInputs.question(json(QUESTION), NOW);
        assertThat(d.opensAt()).isEqualTo(NOW);
        assertThat(d.resolver()).containsEntry("version", "1.0.0");
        assertThat(d.privateQuestion()).isFalse();
        assertThatThrownBy(() -> ForecastInputs.question(json(QUESTION.replace("2026-11-01", "2026-10-01")), NOW))
                .isInstanceOf(ValidationException.class).hasMessageContaining("not valid");
        assertThatThrownBy(() -> ForecastInputs.question(json(QUESTION.replace("\"binary\"", "\"multiple\"")), NOW))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ForecastInputs.question(json(QUESTION.replace("{\"type\":\"manual\"}",
                "{\"type\":\"oracle\"}")), NOW)).isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.RESOLVER_INVALID));
        assertThatThrownBy(() -> ForecastInputs.question(json(QUESTION.replace("\"category\":\"conflict\"",
                "\"category\":\"conflict\",\"links\":{\"situationId\":\"nope\"}")), NOW))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ForecastInputs.question(json(QUESTION.replace("\"category\":\"conflict\"",
                "\"category\":\"conflict\",\"color\":\"red\"")), NOW)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("forecasts: exactly one form of the question's type, in range, summing to one, monotone")
    void forecasts() {
        ForecastQuestionEntity binary = question(Ledger.BINARY, List.of());
        assertThat(ForecastInputs.forecast(json("{\"probability\":0.3}"), binary).value().probability()).isEqualTo(0.3);
        invalid(() -> ForecastInputs.forecast(json("{\"probability\":1.3}"), binary));
        invalid(() -> ForecastInputs.forecast(json("{\"probability\":0.3,\"quantiles\":{\"p50\":1}}"), binary));
        invalid(() -> ForecastInputs.forecast(json("{}"), binary));
        ForecastQuestionEntity multiple = question(Ledger.MULTIPLE, List.of("a", "b"));
        assertThat(ForecastInputs.forecast(json("{\"distribution\":{\"a\":0.25,\"b\":0.75}}"), multiple).value()
                .distribution()).containsEntry("b", 0.75);
        invalid(() -> ForecastInputs.forecast(json("{\"distribution\":{\"a\":0.25,\"b\":0.7}}"), multiple));
        invalid(() -> ForecastInputs.forecast(json("{\"distribution\":{\"a\":0.25,\"c\":0.75}}"), multiple));
        ForecastQuestionEntity numeric = question(Ledger.NUMERIC, List.of());
        String q = "{\"quantiles\":{\"p05\":1,\"p25\":2,\"p50\":3,\"p75\":4,\"p95\":5}}";
        assertThat(ForecastInputs.forecast(json(q), numeric).value().quantiles()).hasSize(5);
        invalid(() -> ForecastInputs.forecast(json(q.replace("\"p75\":4", "\"p75\":2.5")), numeric));
        invalid(() -> ForecastInputs.forecast(json(q.replace(",\"p95\":5", "")), numeric));
        invalid(() -> ForecastInputs.forecast(json("{\"probability\":0.3,\"citations\":[{\"ref\":\"x\"}]}"), binary));
        invalid(() -> ForecastInputs.forecast(json("{\"probability\":0.3,\"forecasterId\":\"Bad Id\"}"), binary));
        assertThat(ForecastInputs.forecast(json("{\"probability\":0.3,\"citations\":[{\"ref\":\"alert:1\"}],"
                + "\"forecasterId\":\"operator:ada\"}"), binary).forecasterId()).isEqualTo("operator:ada");
    }

    @Test
    @DisplayName("manual resolutions fit the question; evidence is required")
    void resolutions() {
        ForecastQuestionEntity binary = question(Ledger.BINARY, List.of());
        assertThat(ForecastInputs.resolution(json("{\"outcome\":\"yes\",\"evidence\":[\"alert:1\"]}"), binary)
                .outcome()).isEqualTo("yes");
        invalid(() -> ForecastInputs.resolution(json("{\"outcome\":\"maybe\",\"evidence\":[\"alert:1\"]}"), binary));
        assertThatThrownBy(() -> ForecastInputs.resolution(json("{\"outcome\":\"yes\",\"evidence\":[]}"), binary))
                .isInstanceOf(ValidationException.class);
        ForecastQuestionEntity numeric = question(Ledger.NUMERIC, List.of());
        assertThat(ForecastInputs.resolution(json("{\"value\":12.5,\"evidence\":[\"series:a/b\"]}"), numeric).value())
                .isEqualTo(12.5);
        invalid(() -> ForecastInputs.resolution(json("{\"outcome\":\"yes\",\"evidence\":[\"series:a/b\"]}"), numeric));
    }

    @Test
    @DisplayName("forecaster updates, annulments, patches and evaluation requests")
    void others() {
        assertThat(ForecastInputs.forecasterUpdate(json("{\"active\":false}"))).containsEntry("active", false);
        assertThatThrownBy(() -> ForecastInputs.forecasterUpdate(json("{\"config\":{}}")))
                .isInstanceOf(ValidationException.class);
        assertThat(ForecastInputs.annulReason(json("{\"reason\":\"duplicate\"}"))).isEqualTo("duplicate");
        assertThat(ForecastInputs.patch(json("{\"title\":\"New\",\"closesAt\":\"2026-11-01T00:00:00Z\"}"),
                Ledger.BINARY)).containsKeys("title", "closesAt");
        ForecastInputs.EvaluationRequest r = ForecastInputs.evaluation(json("{\"forecasterId\":\"base-rate\","
                + "\"from\":\"2026-01-01T00:00:00Z\",\"to\":\"2026-02-01T00:00:00Z\",\"maxQuestions\":10}"));
        assertThat(r.maxQuestions()).isEqualTo(10);
        assertThatThrownBy(() -> ForecastInputs.evaluation(json("{\"forecasterId\":\"base-rate\","
                + "\"from\":\"2026-02-01T00:00:00Z\",\"to\":\"2026-01-01T00:00:00Z\"}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("an external forecaster registration id matches the contract pattern; the slash form is refused")
    void externalForecasterId() {
        assertThat(ForecastInputs.externalForecasterRegistration(json("{\"id\":\"edp:ext-markets:kalshi\"}")))
                .containsEntry("id", "edp:ext-markets:kalshi");
        assertThatThrownBy(() -> ForecastInputs.externalForecasterRegistration(json("{\"id\":\"edp:ext-markets/kalshi\"}")))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(f -> f.field()).containsExactly("id"));
    }

    static void invalid(Runnable call) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ValidationException.class,
                e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.FORECAST_INVALID));
    }
}
