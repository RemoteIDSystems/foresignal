package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class QueryParamsTest {

    @Test
    void multiJoinsRepeatedAndCommaSeparatedValues() {
        assertThat(QueryParams.multi("kind", List.of("a, b", "c", "a", " "))).containsExactly("a", "b", "c");
        assertThat(QueryParams.multi("kind", null)).isEmpty();
        List<String> withNull = new ArrayList<>();
        withNull.add(null);
        assertThat(QueryParams.multi("kind", withNull)).isEmpty();
    }

    @Test
    void multiCapsTheCount() {
        List<String> values = new ArrayList<>();
        for (int i = 0; i <= QueryParams.MAX_VALUES; i++) {
            values.add("id" + i);
        }
        assertThatThrownBy(() -> QueryParams.multi("externalId", values)).isInstanceOf(ValidationException.class);
        assertThat(QueryParams.multi("externalId", values.subList(0, 100))).hasSize(100);
    }

    @Test
    void intInRange() {
        assertThat(QueryParams.intInRange("limit", null, 1, 10, 5)).isEqualTo(5);
        assertThat(QueryParams.intInRange("limit", "10", 1, 10, 5)).isEqualTo(10);
        assertThatThrownBy(() -> QueryParams.intInRange("limit", "0", 1, 10, 5)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> QueryParams.intInRange("limit", "11", 1, 10, 5)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> QueryParams.intInRange("limit", "x", 1, 10, 5)).isInstanceOf(ValidationException.class);
    }

    @Test
    void nonNegativeLong() {
        assertThat(QueryParams.nonNegativeLong("after", null)).isNull();
        assertThat(QueryParams.nonNegativeLong("after", "0")).isZero();
        assertThatThrownBy(() -> QueryParams.nonNegativeLong("after", "-1")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> QueryParams.nonNegativeLong("after", "1.5")).isInstanceOf(ValidationException.class);
    }

    @Test
    void doubleInRange() {
        assertThat(QueryParams.doubleInRange("simplify", null, 0, 10)).isNull();
        assertThat(QueryParams.doubleInRange("simplify", "2.5", 0, 10)).isEqualTo(2.5);
        assertThatThrownBy(() -> QueryParams.doubleInRange("simplify", "11", 0, 10)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> QueryParams.doubleInRange("simplify", "NaN", 0, 10)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> QueryParams.doubleInRange("simplify", "x", 0, 10)).isInstanceOf(ValidationException.class);
    }

    @Test
    void bool() {
        assertThat(QueryParams.bool("payload", null, true)).isTrue();
        assertThat(QueryParams.bool("payload", "TRUE", false)).isTrue();
        assertThat(QueryParams.bool("payload", "false", true)).isFalse();
        assertThatThrownBy(() -> QueryParams.bool("payload", "yes", false)).isInstanceOf(ValidationException.class);
    }

    @Test
    void instant() {
        assertThat(QueryParams.instant("since", null)).isNull();
        assertThat(QueryParams.instant("since", "2026-03-01T00:00:00Z")).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
        assertThat(QueryParams.instant("since", "2026-03-01T01:00:00+01:00"))
                .isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
        assertThatThrownBy(() -> QueryParams.instant("since", "yesterday")).isInstanceOf(ValidationException.class);
    }

    @Test
    void oneOfAndEnumSet() {
        assertThat(QueryParams.oneOf("order", "ID", List.of("observed", "id"), null)).isEqualTo("id");
        assertThat(QueryParams.oneOf("order", null, List.of("observed", "id"), "observed")).isEqualTo("observed");
        assertThatThrownBy(() -> QueryParams.oneOf("order", "x", List.of("observed", "id"), null))
                .isInstanceOf(ValidationException.class);
        assertThat(QueryParams.enumSet("include", List.of("observer,Region"), List.of("observer", "region")))
                .containsExactlyInAnyOrder("observer", "region");
        assertThatThrownBy(() -> QueryParams.enumSet("include", List.of("x"), List.of("observer")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void timeField() {
        assertThat(TimeField.parse(null, TimeField.OBSERVED)).isEqualTo(TimeField.OBSERVED);
        assertThat(TimeField.parse("Ingested", TimeField.OBSERVED)).isEqualTo(TimeField.INGESTED);
        assertThat(TimeField.EFFECTIVE.wireValue()).isEqualTo("effective");
        assertThatThrownBy(() -> TimeField.parse("valid", TimeField.OBSERVED)).isInstanceOf(ValidationException.class);
    }
}
