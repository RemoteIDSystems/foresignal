package ai.foresignal.raster.sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code /v1/sample} disagreement block (prompt 105; AIFS joins per prompt 106). */
class SampleDisagreementTest {

    private static Map<String, Object> value(String productId, double v) {
        return Map.of("productId", productId, "value", v, "status", "ok");
    }

    @Test
    @DisplayName("aifs-t2m is an extra model next to gfs-t2m and ifs-t2m")
    void aifsJoins() {
        Map<String, Object> block = SampleService.disagreement(List.of(value("gfs-t2m", 300), value("ifs-t2m", 301),
                value("aifs-t2m", 306)));
        assertThat(block).isNotNull();
        assertThat(block.get("models")).isEqualTo(List.of("aifs", "gfs", "ifs"));
        assertThat(SampleService.disagreementField("aifs-t2m")).isEqualTo("t2mMax");
    }

    @Test
    @DisplayName("run totals are not 24-hour amounts and stay out")
    void runTotalsStayOut() {
        assertThat(SampleService.disagreementField("aifs-tp")).isNull();
        assertThat(SampleService.disagreementField("gfs-apcp")).isNull();
        assertThat(SampleService.disagreementField("ifs-precip-24h")).isEqualTo("precip24h");
    }
}
