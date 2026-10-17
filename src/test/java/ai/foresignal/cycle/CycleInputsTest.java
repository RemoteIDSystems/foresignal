package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@code AnalysisCycleInput} and {@code SituationAssessmentRequest} validation (prompt 128). */
class CycleInputsTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static final String WEEKLY = "{\"name\":\"Weekly\",\"cadence\":\"weekly\",\"schedule\":{\"cron\":\"0 7 * * 1\","
            + "\"timezone\":\"Europe/London\"},\"scope\":{\"regionCodes\":[\"YE\",\"US-TX\"],"
            + "\"hypothesisSetIds\":\"cadence-match\"},\"roles\":[\"proposer\",\"analyst\"],\"samples\":5}";

    @Test
    @DisplayName("a valid cycle parses with cadence defaults and roles in run order")
    void valid() {
        CycleInputs.CycleDraft d = CycleInputs.cycle(JSON.readTree(WEEKLY));
        assertThat(d.cadence()).isEqualTo("weekly");
        assertThat(d.roles()).containsExactly("analyst", "proposer");
        assertThat(d.samples()).isEqualTo(5);
        assertThat(d.maxQuestions()).isEqualTo(40);
        assertThat(d.maxTokensPerCycle()).isEqualTo(1_200_000L);
        assertThat(d.notifyMinPriority()).isEqualTo("high");
        assertThat(d.enabled()).isTrue();
        assertThat(d.scope().cadenceMatch()).isTrue();
        assertThat(d.scope().regionCodes()).containsExactly("YE", "US-TX");
        assertThat(d.scope().toMap()).containsEntry("hypothesisSetIds", "cadence-match");
        assertThat(CycleInputs.schedule(d)).containsEntry("cron", "0 7 * * 1");
    }

    @Test
    @DisplayName("invalid cron, timezone, roles, samples, scope and unknown fields are 400 with every error")
    void invalid() {
        String bad = "{\"name\":\"\",\"cadence\":\"hourly\",\"schedule\":{\"cron\":\"* *\",\"timezone\":\"Mars/Base\"},"
                + "\"roles\":[\"analyst\",\"analyst\",\"oracle\"],\"samples\":9,\"scope\":{\"regionCodes\":[\"yemen\"],"
                + "\"hypothesisSetIds\":\"all\",\"bogus\":1},\"extra\":true,\"notifyMinPriority\":\"urgent\","
                + "\"maxTokensPerCycle\":-1,\"enabled\":\"yes\"}";
        assertThatThrownBy(() -> CycleInputs.cycle(JSON.readTree(bad))).isInstanceOfSatisfying(
                ValidationException.class, e -> assertThat(e.getErrors()).extracting(f -> f.field()).contains("name",
                        "cadence", "schedule.cron", "schedule.timezone", "roles", "samples", "scope.regionCodes",
                        "scope.hypothesisSetIds", "scope.bogus", "extra", "notifyMinPriority", "maxTokensPerCycle",
                        "enabled"));
        assertThatThrownBy(() -> CycleInputs.cycle(JSON.readTree("[]"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> CycleInputs.cycle(JSON.readTree("{\"name\":\"x\",\"cadence\":\"daily\"}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("an on-demand request defaults to the analyst and red team")
    void situation() {
        assertThat(CycleInputs.situation(null).roles()).containsExactly("analyst", "red-team");
        CycleInputs.SituationRequest r = CycleInputs.situation(JSON.readTree("{\"roles\":[\"forecaster\"],"
                + "\"effort\":\"low\"}"));
        assertThat(r.roles()).containsExactly("forecaster");
        assertThat(r.effort()).isEqualTo("low");
        assertThatThrownBy(() -> CycleInputs.situation(JSON.readTree("{\"effort\":\"max\",\"x\":1}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> CycleInputs.situation(JSON.readTree("3"))).isInstanceOf(ValidationException.class);
        assertThat(CycleInputs.situation(JSON.readTree("{\"roles\":[]}")).roles()).isEqualTo(List.of());
    }
}
