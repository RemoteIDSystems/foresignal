package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Behaviour of the Duration module itself, independent of Spring wiring. */
class DurationShorthandModuleTest {

    private final JsonMapper mapper =
            JsonMapper.builder().addModule(new DurationShorthandModule()).build();

    record Holder(Duration interval) {}

    @Test
    void writesShorthand() {
        assertThat(mapper.writeValueAsString(new Holder(Duration.ofDays(7))))
                .isEqualTo("{\"interval\":\"7d\"}");
    }

    @Test
    void readsShorthand() {
        assertThat(mapper.readValue("{\"interval\":\"15m\"}", Holder.class).interval())
                .isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("ISO-8601 input is still accepted")
    void readsIso() {
        assertThat(mapper.readValue("{\"interval\":\"PT15M\"}", Holder.class).interval())
                .isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("a bare number is read as seconds")
    void readsBareNumberAsSeconds() {
        assertThat(mapper.readValue("{\"interval\":90}", Holder.class).interval())
                .isEqualTo(Duration.ofSeconds(90));
    }

    @Test
    void rejectsNegativeBareNumber() {
        assertThatThrownBy(() -> mapper.readValue("{\"interval\":-5}", Holder.class))
                .hasMessageContaining("negative");
    }

    @Test
    void rejectsGarbageString() {
        assertThatThrownBy(() -> mapper.readValue("{\"interval\":\"nonsense\"}", Holder.class))
                .hasMessageContaining("nonsense");
    }

    @Test
    void roundTrips() {
        Holder original = new Holder(Duration.ofHours(12));

        String json = mapper.writeValueAsString(original);

        assertThat(mapper.readValue(json, Holder.class)).isEqualTo(original);
    }

    @Test
    @DisplayName("durations inside a map value are converted too")
    void convertsMapValues() {
        String json = mapper.writeValueAsString(Map.of("usgs-earthquakes", Duration.ofDays(3650)));

        assertThat(json).isEqualTo("{\"usgs-earthquakes\":\"3650d\"}");
    }

    @Test
    @DisplayName("RetentionSettings serializes to the shape in the OpenAPI document")
    void serializesRetentionSettings() {
        var settings =
                new RetentionSettings(
                        Duration.ofDays(7),
                        Duration.ofDays(90),
                        Duration.ofDays(30),
                        Map.of("usgs-earthquakes", Duration.ofDays(3650)),
                        Duration.ofDays(3));

        String json = mapper.writeValueAsString(settings);

        assertThat(json)
                .contains("\"default\":\"7d\"")
                .contains("\"alerts\":\"90d\"")
                .contains("\"usage\":\"30d\"")
                .contains("\"usgs-earthquakes\":\"3650d\"");
    }

    @Test
    void readsRetentionSettingsBack() {
        String json =
                """
                {"default":"7d","alerts":"90d","usage":"30d","sources":{"usgs-earthquakes":"3650d"}}
                """;

        RetentionSettings settings = mapper.readValue(json, RetentionSettings.class);

        assertThat(settings.defaultRetention()).isEqualTo(Duration.ofDays(7));
        assertThat(settings.alerts()).isEqualTo(Duration.ofDays(90));
        assertThat(settings.usage()).isEqualTo(Duration.ofDays(30));
        assertThat(settings.forSource("usgs-earthquakes")).isEqualTo(Duration.ofDays(3650));
        assertThat(settings.forSource("anything-else")).isEqualTo(Duration.ofDays(7));
    }
}
