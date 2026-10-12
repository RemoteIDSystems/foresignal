package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

/**
 * Confirms the shorthand module is actually registered on the application's mapper.
 *
 * <p>{@link DurationShorthandModuleTest} covers the module's behaviour; this covers the wiring,
 * which is the part that silently breaks if the Boot customizer is wrong.
 */
@IntegrationTest
class JacksonDurationIT {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("the application mapper writes durations as shorthand")
    void writesShorthand() {
        assertThat(jsonMapper.writeValueAsString(Duration.ofDays(7))).isEqualTo("\"7d\"");
    }

    @Test
    @DisplayName("the application mapper reads shorthand")
    void readsShorthand() {
        assertThat(jsonMapper.readValue("\"12h\"", Duration.class)).isEqualTo(Duration.ofHours(12));
    }

    @Test
    void readsIsoToo() {
        assertThat(jsonMapper.readValue("\"PT15M\"", Duration.class))
                .isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("RetentionSettings serializes every window in shorthand, in the schema's member names")
    void retentionSettingsMatchesContract() {
        var settings =
                new RetentionSettings(
                        Duration.ofDays(7),
                        Duration.ofDays(90),
                        Duration.ofDays(30),
                        Map.of("usgs-earthquakes", Duration.ofDays(3650)),
                        Duration.ofDays(3));

        String json = jsonMapper.writeValueAsString(settings);

        assertThat(json)
                .isEqualTo(
                        "{\"default\":\"7d\",\"alerts\":\"90d\",\"usage\":\"30d\","
                                + "\"sources\":{\"usgs-earthquakes\":\"3650d\"},\"trackWindow\":\"3d\","
                                + "\"provenance\":\"30d\",\"notifications\":\"90d\",\"briefings\":\"180d\","
                                + "\"ask\":\"90d\",\"kinds\":{}}");
    }

    @Test
    void retentionSettingsRoundTrips() {
        RetentionSettings original = RetentionSettings.defaults();

        String json = jsonMapper.writeValueAsString(original);

        assertThat(jsonMapper.readValue(json, RetentionSettings.class)).isEqualTo(original);
    }

    @Test
    void corsSettingsRoundTrips() {
        CorsSettings original = CorsSettings.defaults();

        String json = jsonMapper.writeValueAsString(original);

        assertThat(json).isEqualTo("{\"readOrigins\":[\"*\"],\"writeOrigins\":[]}");
        assertThat(jsonMapper.readValue(json, CorsSettings.class)).isEqualTo(original);
    }
}
