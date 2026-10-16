package ai.foresignal.analyst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.trigger.Severity;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** An analyst profile's scope (prompt 102 §1): parsing, validation and the filters the runner applies. */
class AnalystScopeTest {

    @Test
    @DisplayName("an empty scope admits everything")
    void empty() {
        AnalystScope scope = AnalystScope.of(null);
        assertThat(scope.severityAllowed("info")).isTrue();
        assertThat(scope.tagsAllowed(List.of())).isTrue();
        assertThat(scope.hasArea()).isFalse();
    }

    @Test
    @DisplayName("minSeverity uses the alert severities, not note priorities")
    void minSeverity() {
        AnalystScope scope = AnalystScope.of(Map.of("minSeverity", "warning"));
        assertThat(scope.minSeverity()).isEqualTo(Severity.WARNING);
        assertThat(scope.severityAllowed("info")).isFalse();
        assertThat(scope.severityAllowed("critical")).isTrue();
        assertThatThrownBy(() -> AnalystScope.of(Map.of("minSeverity", "normal")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("rule tags match any; bbox and places make an area; malformed members are 400")
    void tagsAndArea() {
        AnalystScope scope = AnalystScope.of(Map.of("ruleTags", List.of("ports", "grid"), "bbox",
                List.of(-10, 40, 10, 60), "places", List.of("home")));
        assertThat(scope.tagsAllowed(List.of("grid"))).isTrue();
        assertThat(scope.tagsAllowed(List.of("other"))).isFalse();
        assertThat(scope.hasArea()).isTrue();
        assertThat(scope.bbox().getMinX()).isEqualTo(-10);
        assertThatThrownBy(() -> AnalystScope.of(Map.of("bbox", List.of(1, 2, 3))))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> AnalystScope.of(Map.of("sources", "not-a-list")))
                .isInstanceOf(ValidationException.class);
    }
}
