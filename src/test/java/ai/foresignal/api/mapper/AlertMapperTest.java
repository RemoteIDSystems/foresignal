package ai.foresignal.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link AlertMapper#summaryOnly}: the trimmed {@code context} of an alert list item still satisfies the contract's
 * {@code WeatherContext}, whose {@code status} is required.
 */
class AlertMapperTest {

    @Test
    @DisplayName("a list item's weather context keeps status beside summary and drops the sections")
    void summaryOnlyKeepsStatus() {
        Map<String, Object> context = AlertMapper.summaryOnly(
                "{\"weather\":{\"status\":\"ok\",\"at\":\"2026-10-23T17:00:00Z\",\"radar\":{\"status\":\"ok\"},"
                        + "\"summary\":\"Radar max 0 dBZ within 10 km\"}}");
        assertThat(context).isEqualTo(Map.of("weather",
                Map.of("status", "ok", "summary", "Radar max 0 dBZ within 10 km")));
    }

    @Test
    @DisplayName("a stored weather context without a status reads as unavailable; none without a summary is null")
    void missingStatusIsUnavailable() {
        assertThat(AlertMapper.summaryOnly("{\"weather\":{\"summary\":\"s\"}}"))
                .isEqualTo(Map.of("weather", Map.of("status", "unavailable", "summary", "s")));
        assertThat(AlertMapper.summaryOnly("{\"weather\":{\"status\":\"ok\"}}")).isNull();
        assertThat(AlertMapper.summaryOnly(null)).isNull();
    }
}
