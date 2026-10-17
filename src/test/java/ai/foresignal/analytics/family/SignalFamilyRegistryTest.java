package ai.foresignal.analytics.family;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.intel.SignalFamilies;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The signal-family registry (prompt 119): bundled families and rules, 101 aliases, operator rows first. */
class SignalFamilyRegistryTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static SignalFamilyRegistry registry(Map<String, Object> operator) {
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.get(eq(SettingKeys.INTEL_SIGNAL_FAMILIES), eq(Map.class)))
                .thenReturn((Optional) Optional.ofNullable(operator));
        return new SignalFamilyRegistry(settings, JsonMapper.builder().build());
    }

    @Test
    @DisplayName("the seventeen families of the prompt are bundled")
    void families() {
        assertThat(registry(null).ids()).contains("ais-behaviour", "aviation-military", "aviation-ops", "conflict",
                "outage", "internet-measurement", "gnss-interference", "nav-warning", "attention", "seismic", "fire",
                "weather-severe", "cyber", "markets", "sanctions", "health", "space").hasSize(17);
    }

    @Test
    @DisplayName("every 101 signal_family string resolves through the registry")
    void reconciles101() {
        SignalFamilyRegistry r = registry(null);
        for (String name : SignalFamilies.ALL) {
            assertThat(r.canonical(name)).as(name).isPresent();
        }
        assertThat(r.canonical("aviation-behaviour")).contains("aviation-ops");
        assertThat(r.canonical("aviation-restriction")).contains("aviation-ops");
        assertThat(r.canonical("weather-hazard")).contains("weather-severe");
        assertThat(r.canonical("grid-frequency")).contains("outage");
        assertThat(r.storedNames("aviation-ops")).contains("aviation-ops", "aviation-behaviour", "aviation-restriction");
        assertThat(r.canonical("activity-anomaly")).as("a 118 fallback label is not a family").isEmpty();
    }

    @Test
    @DisplayName("sources, kinds, event types, finding types and rule tags map to families")
    void rules() {
        SignalFamilyRegistry r = registry(null);
        assertThat(r.familyOfRecord("usgs-earthquakes", "earthquake")).contains("seismic");
        assertThat(r.familyOfRecord("gdelt-timelines", "series-point")).contains("attention");
        assertThat(r.familyOfRecord("cftc-cot", "series-point")).contains("markets");
        assertThat(r.familyOfRecord(null, "conflict-event")).contains("conflict");
        assertThat(r.familyOfRecord("imf-portwatch-chokepoints", "chokepoint-transit")).contains("ais-behaviour");
        assertThat(r.familyOf(SignalFamilyRegistry.Query.intelEvent("maritime-disruption"))).contains("ais-behaviour");
        assertThat(r.familyOf(SignalFamilyRegistry.Query.finding("pre-event-signal"))).contains("markets");
        assertThat(r.familyOfTags(List.of("unknown", "Jamming"))).contains("gnss-interference");
        assertThat(r.familyOfRecord("nobody", "nothing")).isEmpty();
    }

    @Test
    @DisplayName("operator rows from intel.signalFamilies are tried first and may add families")
    void operatorRows() {
        SignalFamilyRegistry r = registry(Map.of(
                "families", List.of(Map.of("id", "rail", "label", "Rail operations")),
                "rules", List.of(Map.of("match", Map.of("kind", "train"), "family", "rail"),
                        Map.of("match", Map.of("kind", "earthquake"), "family", "space"))));
        assertThat(r.ids()).contains("rail");
        assertThat(r.familyOfRecord(null, "train")).contains("rail");
        assertThat(r.familyOfRecord(null, "earthquake")).as("operator rows win").contains("space");
    }
}
