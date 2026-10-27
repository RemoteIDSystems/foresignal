package ai.foresignal.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The cap-history members of {@code RetentionSettings} (prompt R2-015): validation, defaults and the clamp. */
class RetentionSettingsCapTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    final SourceRegistry registry = mock(SourceRegistry.class);
    final RetentionResolver resolver = mock(RetentionResolver.class);
    RetentionSettingsService service;

    @BeforeEach
    void setUp() {
        when(registry.descriptors()).thenReturn(List.of());
        when(resolver.settings()).thenReturn(RetentionSettings.defaults());
        service = new RetentionSettingsService(mock(AppSettingService.class), resolver, registry,
                mock(SourceService.class));
    }

    static JsonNode body(String json) {
        return JSON.readTree(json.replace('\'', '"'));
    }

    List<String> fields(String json) {
        try {
            service.parse(body(json));
        } catch (ValidationException e) {
            return e.getErrors().stream().map(FieldError::field).toList();
        }
        throw new AssertionError("expected a 400 for " + json);
    }

    @Test
    @DisplayName("off by default: no cap, overwriteOldest true, every window unchanged")
    void offByDefault() {
        RetentionSettings parsed = service.parse(body("{'default':'400d','trackWindow':'30d'}"));
        assertThat(parsed.capHistory()).isFalse();
        assertThat(parsed.maxDays()).isNull();
        assertThat(parsed.maxDbGb()).isNull();
        assertThat(parsed.overwriteOldest()).isTrue();
        assertThat(parsed.capWindow()).isEmpty();
        assertThat(parsed.capBytes()).isEmpty();
        assertThat(parsed.capped(Duration.ofDays(400))).isEqualTo(Duration.ofDays(400));
        assertThat(parsed.effectiveTrackWindow()).isEqualTo(Duration.ofDays(30));
        assertThat(RetentionSettings.defaults().capHistory()).isFalse();
    }

    @Test
    @DisplayName("a cap that is set but switched off has no effect")
    void setButOff() {
        RetentionSettings parsed = service.parse(body("{'default':'400d','maxDays':30,'maxDbGb':10}"));
        assertThat(parsed.maxDays()).isEqualTo(30);
        assertThat(parsed.capWindow()).isEmpty();
        assertThat(parsed.capBytes()).isEmpty();
        assertThat(parsed.capped(Duration.ofDays(400))).isEqualTo(Duration.ofDays(400));
    }

    @Test
    @DisplayName("on: maxDays clamps longer windows only; maxDbGb is in binary GB")
    void on() {
        RetentionSettings parsed = service.parse(body(
                "{'default':'400d','trackWindow':'3d','capHistory':true,'maxDays':30,'maxDbGb':1.5,"
                        + "'overwriteOldest':false}"));
        assertThat(parsed.capWindow()).contains(Duration.ofDays(30));
        assertThat(parsed.capped(Duration.ofDays(400))).isEqualTo(Duration.ofDays(30));
        assertThat(parsed.capped(Duration.ofDays(2))).as("never lengthens").isEqualTo(Duration.ofDays(2));
        assertThat(parsed.effectiveTrackWindow()).isEqualTo(Duration.ofDays(3));
        assertThat(parsed.capBytes().getAsLong()).isEqualTo((long) (1.5 * 1024 * 1024 * 1024));
        assertThat(parsed.overwriteOldest()).isFalse();
        assertThat(service.parse(body("{'default':'7d','capHistory':true,'maxDbGb':50}")).capWindow()).isEmpty();
    }

    @Test
    @DisplayName("validation: whole days 1-36500, GB above 0, booleans, a switched-on cap needs a bound; one 400")
    void validation() {
        assertThat(fields("{'default':'7d','maxDays':0}")).containsExactly("maxDays");
        assertThat(fields("{'default':'7d','maxDays':1.5}")).containsExactly("maxDays");
        assertThat(fields("{'default':'7d','maxDays':'30d'}")).containsExactly("maxDays");
        assertThat(fields("{'default':'7d','maxDays':36501}")).containsExactly("maxDays");
        assertThat(fields("{'default':'7d','maxDbGb':0}")).containsExactly("maxDbGb");
        assertThat(fields("{'default':'7d','maxDbGb':-1}")).containsExactly("maxDbGb");
        assertThat(fields("{'default':'7d','maxDbGb':'10'}")).containsExactly("maxDbGb");
        assertThat(fields("{'default':'7d','capHistory':'yes'}")).containsExactly("capHistory");
        assertThat(fields("{'default':'7d','overwriteOldest':1}")).containsExactly("overwriteOldest");
        assertThat(fields("{'default':'7d','capHistory':true}")).containsExactly("capHistory");
        assertThat(fields("{'default':'0m','maxDays':0,'maxDbGb':0}"))
                .as("every problem in one 400").containsExactlyInAnyOrder("default", "maxDays", "maxDbGb");
        assertThat(service.parse(body("{'default':'7d','maxDays':1,'capHistory':true}")).capWindow())
                .as("1 day is above the 1h minimum window").contains(Duration.ofDays(1));
    }

    @Test
    @DisplayName("withMaxDays switches the cap on and keeps the size cap")
    void withMaxDays() {
        RetentionSettings parsed = service.parse(body("{'default':'400d','capHistory':true,'maxDbGb':5}"));
        RetentionSettings tightened = parsed.withMaxDays(10);
        assertThat(tightened.capWindow()).contains(Duration.ofDays(10));
        assertThat(tightened.capBytes()).isEqualTo(parsed.capBytes());
        assertThat(tightened.defaultRetention()).isEqualTo(Duration.ofDays(400));
    }

    @Test
    @DisplayName("the cap members round-trip through JSON with the rest")
    void roundTrip() {
        RetentionSettings parsed = service.parse(body("{'default':'7d','capHistory':true,'maxDays':30,'maxDbGb':2}"));
        RetentionSettings back = JSON.readValue(JSON.writeValueAsString(parsed), RetentionSettings.class);
        assertThat(back).isEqualTo(parsed);
        RetentionSettings legacy = JSON.readValue("{\"default\":\"7d\"}", RetentionSettings.class);
        assertThat(legacy.capHistory()).as("settings stored before R2-015 are uncapped").isFalse();
        assertThat(legacy.overwriteOldest()).isTrue();
    }

    @Test
    @DisplayName("the full value a pre-R2-015 instance stored reads back uncapped, not as a mapping failure")
    void storedBeforeCap() {
        String stored = "{'ask':'90d','audit':'90d','inbox':'30d','kinds':{},'stats':{'daily':'1825d','hourly':'35d',"
                + "'regionDaily':'3650d','regionHourly':'1095d'},'usage':'30d','alerts':'90d','default':'7d',"
                + "'sources':{},'briefings':'180d','provenance':'30d','entityLinks':'365d','trackWindow':'3d',"
                + "'notifications':'90d','trackRawWindow':'1d','trackDownsampleInterval':'5m'}";
        RetentionSettings read = JSON.readValue(stored.replace('\'', '"'), RetentionSettings.class);
        assertThat(read.capHistory()).isFalse();
        assertThat(read.overwriteOldest()).isTrue();
        assertThat(read.capWindow()).isEmpty();
        assertThat(read.effectiveTrackWindow()).isEqualTo(Duration.ofDays(3));
    }
}
