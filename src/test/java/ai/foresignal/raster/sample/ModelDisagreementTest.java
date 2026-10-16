package ai.foresignal.raster.sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The model-disagreement flag (prompt 105): thresholds, hysteresis, the two-model minimum. */
class ModelDisagreementTest {

    private static Map<String, Map<String, Double>> gust(double gfs, double ifs) {
        Map<String, Map<String, Double>> fields = new LinkedHashMap<>();
        Map<String, Double> models = new LinkedHashMap<>();
        models.put("gfs", gfs);
        models.put("ifs", ifs);
        fields.put("gust", models);
        return fields;
    }

    @Test
    @DisplayName("a gust spread over 8 m/s is flagged with the models")
    void flagsGust() {
        Map<String, Object> block = ModelDisagreement.compute(gust(10, 19.5), Map.of());
        assertThat(block).isNotNull();
        assertThat(String.valueOf(block.get("fields"))).contains("gust");
        assertThat(String.valueOf(block.get("models"))).contains("gfs").contains("ifs");
        assertThat(((Map<?, ?>) block.get("spread")).get("gust")).isEqualTo(9.5);
    }

    @Test
    @DisplayName("a spread under the threshold is not flagged")
    void belowThreshold() {
        assertThat(ModelDisagreement.compute(gust(10, 15), Map.of())).isNull();
    }

    @Test
    @DisplayName("hysteresis keeps a field flagged down to 75% of the threshold")
    void hysteresis() {
        // spread 7 m/s: below 8, but at or above 0.75 * 8 = 6 and previously flagged.
        assertThat(ModelDisagreement.compute(gust(10, 17), Map.of())).isNull();
        assertThat(ModelDisagreement.compute(gust(10, 17), Map.of("gust", 9.0))).isNotNull();
    }

    @Test
    @DisplayName("a field with fewer than two models is ignored")
    void needsTwoModels() {
        Map<String, Map<String, Double>> fields = new LinkedHashMap<>();
        fields.put("gust", Map.of("gfs", 30.0));
        assertThat(ModelDisagreement.compute(fields, Map.of())).isNull();
    }

    @Test
    @DisplayName("precipitation uses max(15 mm, half the mean)")
    void precipThreshold() {
        assertThat(ModelDisagreement.threshold("precip24h", 10)).isEqualTo(15.0);
        assertThat(ModelDisagreement.threshold("precip24h", 100)).isEqualTo(50.0);
        Map<String, Map<String, Double>> fields = new LinkedHashMap<>();
        fields.put("precip24h", Map.of("gfs", 10.0, "ifs", 60.0));
        assertThat(ModelDisagreement.compute(fields, Map.of())).isNotNull();
    }

    @Test
    @DisplayName("temperature uses 4 K")
    void t2mThreshold() {
        assertThat(ModelDisagreement.threshold("t2mMax", 300)).isEqualTo(4.0);
    }

    @Test
    @DisplayName("a model without a value (NaN) neither counts toward the two-model minimum nor is listed")
    void nanIsAbsent() {
        Map<String, Double> gust = new LinkedHashMap<>();
        gust.put("gfs", 10.0);
        gust.put("hrrr", Double.NaN);
        gust.put("ifs", 20.0);
        Map<String, Map<String, Double>> fields = new LinkedHashMap<>();
        fields.put("gust", gust);
        Map<String, Object> block = ModelDisagreement.compute(fields, Map.of());
        assertThat(ModelDisagreement.models(block)).containsExactly("gfs", "ifs");
        Map<String, Double> lonely = new LinkedHashMap<>();
        lonely.put("gfs", 10.0);
        lonely.put("ifs", Double.NaN);
        fields.put("gust", lonely);
        assertThat(ModelDisagreement.compute(fields, Map.of())).isNull();
    }

    @Test
    @DisplayName("AIFS is just another model: GFS, IFS and AIFS temperatures 5 K apart disagree")
    void aifsIsAModel() {
        Map<String, Map<String, Double>> fields = new LinkedHashMap<>();
        fields.put("t2mMax", Map.of("gfs", 300.0, "ifs", 301.0, "aifs", 305.0));
        assertThat(ModelDisagreement.models(ModelDisagreement.compute(fields, Map.of())))
                .containsExactly("aifs", "gfs", "ifs");
    }
}
