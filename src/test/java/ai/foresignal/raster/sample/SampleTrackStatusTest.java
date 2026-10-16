package ai.foresignal.raster.sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Track-sample per-point statuses (prompt 106). */
class SampleTrackStatusTest {

    private static Map<String, Object> value(String status, String validTime) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productId", "mrms-reflectivity");
        m.put("status", status);
        if (validTime != null) {
            m.put("validTime", validTime);
        }
        return m;
    }

    @Test
    @DisplayName("maxGap marks a value stale when its frame is older")
    void maxGap() {
        Map<String, Object> stale = value("ok", "2026-10-15T00:00:00Z");
        SampleService.applyMaxGap(stale, Duration.ofMinutes(10), Instant.parse("2026-10-15T01:00:00Z"));
        assertThat(stale.get("status")).isEqualTo("stale");
        assertThat(stale.get("statusText")).isEqualTo(SampleStatus.STALE.defaultText());
    }

    @Test
    @DisplayName("maxGap leaves a fresh value ok")
    void fresh() {
        Map<String, Object> fresh = value("ok", "2026-10-15T00:59:00Z");
        SampleService.applyMaxGap(fresh, Duration.ofMinutes(10), Instant.parse("2026-10-15T01:00:00Z"));
        assertThat(fresh.get("status")).isEqualTo("ok");
    }

    @Test
    @DisplayName("a non-ok value is not touched by maxGap")
    void notOkUntouched() {
        Map<String, Object> v = value("no-coverage", "2026-10-15T00:00:00Z");
        SampleService.applyMaxGap(v, Duration.ofMinutes(10), Instant.parse("2026-10-15T01:00:00Z"));
        assertThat(v.get("status")).isEqualTo("no-coverage");
    }

    @Test
    @DisplayName("the worst status wins")
    void worst() {
        assertThat(SampleService.worst(List.of(value("ok", null), value("stale", null)))).isEqualTo("stale");
        assertThat(SampleService.worst(List.of(value("stale", null), value("unavailable", null))))
                .isEqualTo("unavailable");
        assertThat(SampleService.worst(List.of(value("ok", null)))).isEqualTo("ok");
    }
}
