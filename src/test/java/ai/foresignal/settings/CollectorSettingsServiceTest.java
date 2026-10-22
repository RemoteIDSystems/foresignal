package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@code CollectorSettings} parsing, clamping and validation (prompt 150). */
class CollectorSettingsServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static CollectorSettings parse(String json) {
        return CollectorSettingsService.parse(JSON.readTree(json.replace('\'', '"')));
    }

    static List<String> fields(String json) {
        try {
            parse(json);
        } catch (ValidationException e) {
            assertThat(e.getCode()).isEqualTo("VALIDATION_FAILED");
            return e.getErrors().stream().map(FieldError::field).toList();
        }
        throw new AssertionError("expected a validation failure for " + json);
    }

    @Test
    @DisplayName("the default is two concurrent pulls; an empty body or a null value is the default")
    void defaults() {
        assertThat(CollectorSettings.defaults().maxConcurrentPulls()).isEqualTo(2);
        assertThat(parse("{}")).isEqualTo(CollectorSettings.defaults());
        assertThat(parse("{'maxConcurrentPulls':null}")).isEqualTo(CollectorSettings.defaults());
    }

    @Test
    @DisplayName("a value inside 1..16 is kept; outside it is clamped")
    void clamps() {
        assertThat(parse("{'maxConcurrentPulls':6}").maxConcurrentPulls()).isEqualTo(6);
        assertThat(parse("{'maxConcurrentPulls':0}").maxConcurrentPulls()).isEqualTo(1);
        assertThat(parse("{'maxConcurrentPulls':-3}").maxConcurrentPulls()).isEqualTo(1);
        assertThat(parse("{'maxConcurrentPulls':40}").maxConcurrentPulls()).isEqualTo(16);
        // A stored value read back is clamped by the record too.
        assertThat(new CollectorSettings(99).maxConcurrentPulls()).isEqualTo(16);
    }

    @Test
    @DisplayName("a non-integer value and an unknown member are each a field error")
    void rejects() {
        assertThat(fields("{'maxConcurrentPulls':'four'}")).containsExactly("maxConcurrentPulls");
        assertThat(fields("{'maxConcurrentPulls':2.5}")).containsExactly("maxConcurrentPulls");
        assertThat(fields("{'maxConcurrentPulls':99999999999}")).containsExactly("maxConcurrentPulls");
        assertThat(fields("{'pulls':4}")).containsExactly("pulls");
    }
}
