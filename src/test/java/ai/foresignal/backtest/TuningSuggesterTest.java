package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.store.TriggerCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The deterministic tuning suggester (prompt 103). */
class TuningSuggesterTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static List<Map<String, Object>> suggest(Map<String, Object> report, List<TriggerCondition> conditions,
            boolean geofence, List<AlertQualityService.Labeled> labeled) {
        return TuningSuggester.suggest(new TuningSuggester.Inputs(report, conditions, geofence, false, null, labeled),
                JSON);
    }

    private static AlertQualityService.Labeled alert(String feedbackClass, double mag, double lon, double lat) {
        return new AlertQualityService.Labeled(feedbackClass, "{\"kind\":\"quake\",\"geometry\":{\"type\":\"Point\","
                + "\"coordinates\":[" + lon + "," + lat + "]},\"payload\":{\"properties\":{\"mag\":" + mag + "}}}");
    }

    @Test
    @DisplayName("duplicates suggest a cooldown of the duplicate window")
    void cooldown() {
        var suggestions = suggest(Map.of("duplicates", 4L, "flapping", 0L, "duplicateWindow", "PT20M"), List.of(),
                false, List.of());
        assertThat(suggestions).anyMatch(s -> s.get("kind").equals("add_cooldown")
                && s.get("patch").equals(Map.of("cooldown", "PT20M")));
    }

    @Test
    @DisplayName("flapping suggests enter mode")
    void flapping() {
        var suggestions = suggest(Map.of("duplicates", 0L, "flapping", 3L), List.of(), false, List.of());
        assertThat(suggestions).anyMatch(s -> s.get("kind").equals("switch_to_enter"));
    }

    @Test
    @DisplayName("the threshold scan keeps every condition and moves one value to remove negatives")
    @SuppressWarnings("unchecked")
    void thresholdScan() {
        List<TriggerCondition> conditions = List.of(new TriggerCondition("properties.mag", ">=", 4.0),
                new TriggerCondition("properties.type", "==", "earthquake"));
        List<AlertQualityService.Labeled> labeled = new ArrayList<>();
        for (double mag : new double[] {5.0, 5.5, 6.0, 6.5, 7.0, 5.2, 5.8, 6.1, 6.3, 6.9}) {
            labeled.add(alert("positive", mag, 0, 0));
        }
        for (double mag : new double[] {4.0, 4.2, 4.5, 4.8, 4.9}) {
            labeled.add(alert("negative", mag, 0, 0));
        }
        var suggestions = suggest(Map.of("precision", 0.66), conditions, false, labeled);
        Map<String, Object> raise = suggestions.stream().filter(s -> s.get("kind").equals("raise_threshold"))
                .findFirst().orElseThrow();
        List<Map<String, Object>> patched = (List<Map<String, Object>>) ((Map<String, Object>) raise.get("patch"))
                .get("conditions");
        assertThat(patched).hasSize(2);
        assertThat(patched.get(0)).containsEntry("path", "properties.mag").containsEntry("op", ">=")
                .containsEntry("value", 5L);
        assertThat(patched.get(1)).containsEntry("path", "properties.type").containsEntry("value", "earthquake");
        assertThat((Map<String, Object>) raise.get("expectedEffect")).containsEntry("removedNegatives", 5)
                .containsEntry("keptPositives", 10);
    }

    @Test
    @DisplayName("a threshold that would drop more than 10% of positives is not suggested")
    void thresholdKeepsPositives() {
        List<TriggerCondition> conditions = List.of(new TriggerCondition("properties.mag", ">", 4.0));
        List<AlertQualityService.Labeled> labeled = List.of(alert("positive", 4.5, 0, 0), alert("positive", 6, 0, 0),
                alert("negative", 5.0, 0, 0), alert("negative", 5.5, 0, 0));
        var suggestions = suggest(Map.of("precision", 0.5), conditions, false, labeled);
        assertThat(suggestions).noneMatch(s -> s.get("kind").equals("raise_threshold"));
    }

    @Test
    @DisplayName("low precision without labeled values never produces an empty condition list")
    @SuppressWarnings("unchecked")
    void neverEmptyConditions() {
        var suggestions = suggest(Map.of("duplicates", 0L, "flapping", 0L, "precision", 0.3),
                List.of(new TriggerCondition("properties.mag", ">=", 4.0)), false, List.of());
        assertThat(suggestions).allSatisfy(s -> {
            Object conditions = ((Map<String, Object>) s.get("patch")).get("conditions");
            assertThat(conditions == null || !((List<?>) conditions).isEmpty()).isTrue();
        });
    }

    @Test
    @DisplayName("negatives far outside the positives suggest shrinking the geofence")
    void geofence() {
        List<AlertQualityService.Labeled> labeled = List.of(alert("positive", 5, -97.0, 31.0),
                alert("positive", 5, -96.0, 31.0), alert("positive", 5, -96.5, 32.0), alert("positive", 5, -96.5, 31.5),
                alert("negative", 5, -90.0, 40.0), alert("negative", 5, -80.0, 25.0), alert("negative", 5, -100.0, 45.0));
        var suggestions = suggest(Map.of(), List.of(), true, labeled);
        assertThat(suggestions).anyMatch(s -> s.get("kind").equals("shrink_geofence")
                && ((Map<?, ?>) s.get("patch")).get("geofence") instanceof Map<?, ?> fence
                && "Polygon".equals(fence.get("type")));
        assertThat(suggest(Map.of(), List.of(), false, labeled)).noneMatch(s -> s.get("kind").equals("shrink_geofence"));
    }

    @Test
    @DisplayName("an anomaly rule with low precision gets a lower sensitivity")
    void sensitivity() {
        var suggestions = TuningSuggester.suggest(new TuningSuggester.Inputs(Map.of("precision", 0.2), List.of(), false,
                true, Map.of("sensitivity", "high", "bucket", "PT1H"), List.of()), JSON);
        assertThat(suggestions).anyMatch(s -> s.get("kind").equals("lower_anomaly_sensitivity")
                && ((Map<?, ?>) ((Map<?, ?>) s.get("patch")).get("window")).get("sensitivity").equals("medium"));
    }

    @Test
    @DisplayName("a clean rule has no suggestions, and every suggestion carries caveats")
    void clean() {
        assertThat(suggest(Map.of("duplicates", 0L, "flapping", 0L, "precision", 0.9), List.of(), false, List.of()))
                .isEmpty();
        assertThat(suggest(Map.of("duplicates", 2L, "flapping", 2L), List.of(), false, List.of()))
                .allMatch(s -> s.containsKey("caveats") && s.containsKey("patch"));
    }
}
