package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Per-model maximum run ages (prompt 105 post-review): cadence + availability lag + ingest allowance. */
class ModelRunAgeTest {

    @Test
    @DisplayName("each model's age is its own cadence, lag and ingest allowance")
    void perModel() {
        assertThat(ModelRunAge.of("ifs-gust")).isEqualTo(Duration.ofHours(22));
        assertThat(ModelRunAge.of("ifs-precip-24h")).isEqualTo(Duration.ofHours(22));
        assertThat(ModelRunAge.of("aifs-t2m")).isEqualTo(Duration.ofHours(16));
        assertThat(ModelRunAge.of("gfs-apcp")).isEqualTo(Duration.ofMinutes(690));
        assertThat(ModelRunAge.of("gfs-wave")).isEqualTo(Duration.ofHours(12));
        assertThat(ModelRunAge.of("hrrr-smoke")).isEqualTo(Duration.ofMinutes(470));
        assertThat(ModelRunAge.of("nbm-t2m")).isEqualTo(Duration.ofHours(9));
    }

    @Test
    @DisplayName("an unknown model or a null id keeps the previous 12-hour limit")
    void fallback() {
        assertThat(ModelRunAge.of("mystery-field")).isEqualTo(Duration.ofHours(12));
        assertThat(ModelRunAge.of(null)).isEqualTo(Duration.ofHours(12));
        assertThat(ModelRunAge.model("ifs-gust")).isEqualTo("ifs");
        assertThat(ModelRunAge.model("gfs")).isEqualTo("gfs");
    }

    @Test
    @DisplayName("IFS is no longer stale for most of the day: a run stays current until its successor is due and fetched")
    void ifsCoversItsCycle() {
        Duration cadence = Duration.ofHours(12);
        assertThat(ModelRunAge.of("ifs-gust")).isGreaterThan(cadence.plus(Duration.ofHours(7)));
    }
}
