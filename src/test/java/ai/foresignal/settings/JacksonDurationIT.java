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
                                + "\"ask\":\"90d\",\"kinds\":{},\"audit\":\"90d\",\"inbox\":\"30d\","
                                + "\"stats\":{\"hourly\":\"35d\",\"daily\":\"1825d\"},"
                                + "\"trackRawWindow\":\"1d\",\"trackDownsampleInterval\":\"5m\"}");
    }

    @Test
    void retentionSettingsRoundTrips() {
        RetentionSettings original = RetentionSettings.defaults();

        String json = jsonMapper.writeValueAsString(original);

        assertThat(jsonMapper.readValue(json, RetentionSettings.class)).isEqualTo(original);
    }

    @Test
    @DisplayName("a disabled trackRawWindow is written as null and read back as disabled; an absent one is the default")
    void disabledRawWindowRoundTrips() {
        RetentionSettings disabled = new RetentionSettings(null, null, null, null, null, null, null, null, null, null,
                null, null, null, RetentionSettings.DOWNSAMPLING_DISABLED, Duration.ofMinutes(2), null);

        String json = jsonMapper.writeValueAsString(disabled);

        assertThat(json).contains("\"trackRawWindow\":null").contains("\"trackDownsampleInterval\":\"2m\"");
        RetentionSettings back = jsonMapper.readValue(json, RetentionSettings.class);
        assertThat(back.downsamplingEnabled()).isFalse();
        assertThat(back).isEqualTo(disabled);
        RetentionSettings absent = jsonMapper.readValue("{\"default\":\"7d\"}", RetentionSettings.class);
        assertThat(absent.trackRawWindow()).isEqualTo(Duration.ofHours(24));
        assertThat(absent.trackDownsampleInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(absent.downsamplingEnabled()).isTrue();
    }

    @Test
    void corsSettingsRoundTrips() {
        CorsSettings original = CorsSettings.defaults();

        String json = jsonMapper.writeValueAsString(original);

        assertThat(json).isEqualTo("{\"readOrigins\":[\"*\"],\"writeOrigins\":[]}");
        assertThat(jsonMapper.readValue(json, CorsSettings.class)).isEqualTo(original);
    }
}
