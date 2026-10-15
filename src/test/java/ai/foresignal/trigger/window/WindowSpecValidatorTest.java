package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code type} and {@code window} validation (prompt 100). */
class WindowSpecValidatorTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static JsonNode body(String json) {
        return JSON.readTree(json);
    }

    @Test
    @DisplayName("a record rule carries no window and defaults its type")
    void record() {
        assertThat(WindowSpecValidator.type(body("{}"))).isEqualTo(RuleType.RECORD);
        assertThat(WindowSpecValidator.validate(RuleType.RECORD, body("{}"))).isNull();
    }

    @Test
    @DisplayName("a count rule needs an op and a threshold")
    void count() {
        WindowSpec spec = WindowSpecValidator.validate(RuleType.COUNT,
                body("{\"type\":\"count\",\"window\":{\"window\":\"30m\",\"op\":\">=\",\"threshold\":5}}"));
        assertThat(spec.window()).isEqualTo(java.time.Duration.ofMinutes(30));
        assertThat(spec.step()).isEqualTo(java.time.Duration.ofMinutes(5));
        assertThat(spec.op()).isEqualTo(">=");
        assertThat(spec.threshold()).isEqualTo(5);
    }

    @Test
    @DisplayName("an invalid count window is 400 WINDOW_RULE_INVALID")
    void countInvalid() {
        assertThatThrownBy(() -> WindowSpecValidator.validate(RuleType.COUNT, body("{\"window\":{\"window\":\"30m\"}}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a window shorter than a minute or longer than 7 days is rejected")
    void windowBounds() {
        assertThatThrownBy(() -> WindowSpecValidator.validate(RuleType.COUNT,
                body("{\"window\":{\"window\":\"30s\",\"op\":\">=\",\"threshold\":1}}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> WindowSpecValidator.validate(RuleType.COUNT,
                body("{\"window\":{\"window\":\"8d\",\"op\":\">=\",\"threshold\":1}}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("anomaly fields take their defaults and validate the enums")
    void anomaly() {
        WindowSpec spec = WindowSpecValidator.validate(RuleType.ANOMALY,
                body("{\"window\":{\"window\":\"1h\",\"sensitivity\":\"high\",\"minWeeks\":10}}"));
        assertThat(spec.sensitivity()).isEqualTo("high");
        assertThat(spec.alpha()).isEqualTo(1e-2);
        assertThat(spec.minWeeks()).isEqualTo(10);
        assertThat(spec.bucket()).isEqualTo(java.time.Duration.ofHours(1));
        assertThat(spec.coverageMin()).isEqualTo(0.8);
        assertThatThrownBy(() -> WindowSpecValidator.validate(RuleType.ANOMALY,
                body("{\"window\":{\"window\":\"1h\",\"direction\":\"sideways\"}}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> WindowSpecValidator.validate(RuleType.ANOMALY,
                body("{\"window\":{\"window\":\"1h\",\"minWeeks\":4}}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("onlyNew, mode and reappearAsEnter are rejected for windowed rules")
    void singleVersionRejected() {
        assertThatThrownBy(() -> WindowSpecValidator.validate(RuleType.COUNT,
                body("{\"onlyNew\":true,\"window\":{\"window\":\"30m\",\"op\":\">=\",\"threshold\":1}}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> WindowSpecValidator.validate(RuleType.COUNT,
                body("{\"mode\":\"enter\",\"window\":{\"window\":\"30m\",\"op\":\">=\",\"threshold\":1}}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> WindowSpecValidator.validate(RuleType.COUNT,
                body("{\"reappearAsEnter\":true,\"window\":{\"window\":\"30m\",\"op\":\">=\",\"threshold\":1}}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("groupBy accepts the supported forms and the cap is 500")
    void groupBy() {
        assertThat(WindowSpecValidator.validGroupBy("h3:5")).isTrue();
        assertThat(WindowSpecValidator.validGroupBy("h3:9")).isFalse();
        assertThat(WindowSpecValidator.validGroupBy("source")).isTrue();
        assertThat(WindowSpecValidator.validGroupBy("shipType")).isTrue();
        assertThatThrownBy(() -> WindowSpecValidator.validate(RuleType.COUNT,
                body("{\"window\":{\"window\":\"30m\",\"op\":\">=\",\"threshold\":1,\"maxGroups\":1000}}")))
                .isInstanceOf(ValidationException.class);
    }
}
