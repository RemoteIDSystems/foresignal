package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@link Snoozes} (prompt 017). */
class SnoozesTest {

    static final Instant NOW = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    static Instant expiry(String body, Duration max) {
        return Snoozes.expiry(JSON.readTree(body), NOW, max);
    }

    @Test
    @DisplayName("until or for, exactly one, with an expiry in the future")
    void forms() {
        assertThat(expiry("{\"for\":\"2h\"}", null)).isEqualTo(NOW.plusSeconds(7200));
        assertThat(expiry("{\"until\":\"2026-05-02T00:00:00Z\"}", null)).isEqualTo(Instant.parse("2026-05-02T00:00:00Z"));
        assertThatThrownBy(() -> expiry("{}", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> expiry("{\"for\":\"1h\",\"until\":\"2026-05-02T00:00:00Z\"}", null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> expiry("{\"until\":\"2026-04-01T00:00:00Z\"}", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> expiry("{\"until\":\"tomorrow\"}", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> expiry("{\"for\":\"0s\"}", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> expiry("{\"for\":5}", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> expiry("[]", null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("rule snoozes are capped at 7 days")
    void cap() {
        assertThat(expiry("{\"for\":\"7d\"}", Snoozes.MAX_RULE_SNOOZE)).isEqualTo(NOW.plus(Duration.ofDays(7)));
        assertThatThrownBy(() -> expiry("{\"for\":\"8d\"}", Snoozes.MAX_RULE_SNOOZE)).isInstanceOf(ValidationException.class);
    }
}
