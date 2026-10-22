package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The tunable corroboration policy (prompt 154): defaults, a PUT/GET round-trip and validation. */
class IntelCorroborationServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private IntelCorroborationService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        AppSettingService settings = mock(AppSettingService.class);
        Map<String, Object> store = new HashMap<>();
        when(settings.get(eq(SettingKeys.INTEL_CORROBORATION), any())).thenAnswer(
                i -> Optional.ofNullable(store.get(SettingKeys.INTEL_CORROBORATION)));
        doAnswer(i -> {
            store.put(i.getArgument(0), i.getArgument(1));
            return null;
        }).when(settings).put(anyString(), any());
        doAnswer(i -> {
            store.remove(i.getArgument(0));
            return null;
        }).when(settings).remove(anyString());
        service = new IntelCorroborationService(settings, JSON);
    }

    @Test
    @DisplayName("without a setting every gate is its historical default")
    void defaults() {
        assertThat(service.quality()).isEqualTo(EventScorer.Quality.DEFAULT);
        assertThat(service.situationActivation()).isEqualTo(IntelCorroborationService.Activation.DEFAULT);
        IntelCorroborationService.AutoCorroborate auto = service.autoCorroborate();
        assertThat(auto.enabled()).isTrue();
        assertThat(auto.withinKm()).isEqualTo(50);
        assertThat(auto.window()).isEqualTo(Duration.ofHours(24));
        assertThat(auto.maxMembers()).isEqualTo(10);
    }

    @Test
    @DisplayName("a PUT replaces the policy and the typed accessors reflect it")
    void putThenGet() {
        service.put(json("{\"eventQuality\": {\"strongFamilies\": 2, \"requireModalities\": 2},"
                + " \"situationActivation\": {\"minFamilies\": 3, \"minGroups\": 1},"
                + " \"autoCorroborate\": {\"withinKm\": 25, \"window\": \"PT6H\", \"maxMembers\": 3}}"));
        assertThat(service.quality().strongFamilies()).isEqualTo(2);
        assertThat(service.quality().requireModalities()).isEqualTo(2);
        assertThat(service.quality().strongGroups()).isEqualTo(EventScorer.Quality.DEFAULT.strongGroups());
        assertThat(service.situationActivation()).isEqualTo(new IntelCorroborationService.Activation(3, 1));
        IntelCorroborationService.AutoCorroborate auto = service.autoCorroborate();
        assertThat(auto.withinKm()).isEqualTo(25);
        assertThat(auto.window()).isEqualTo(Duration.ofHours(6));
        assertThat(auto.maxMembers()).isEqualTo(3);
        assertThat(service.get()).containsKey("eventQuality");
    }

    @Test
    @DisplayName("an unknown member is rejected")
    void rejectsUnknownMember() {
        assertThatThrownBy(() -> service.put(json("{\"nope\": 1}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("an out-of-range threshold is rejected")
    void rejectsOutOfRange() {
        assertThatThrownBy(() -> service.put(json("{\"eventQuality\": {\"strongFamilies\": 0}}")))
                .isInstanceOf(ValidationException.class);
    }

    private static JsonNode json(String text) {
        return JSON.readTree(text);
    }
}
