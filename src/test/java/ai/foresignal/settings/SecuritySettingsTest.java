package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class SecuritySettingsTest {

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void defaultsKeepObserversGated() {
        assertThat(SecuritySettings.defaults().openReads()).isTrue();
        assertThat(SecuritySettings.defaults().exposeObserver()).isFalse();
        assertThat(new SecuritySettings(false).exposeObserver()).isFalse();
        assertThat(new SecuritySettings(true, null).exposeObserver()).isFalse();
    }

    @Test
    @DisplayName("a value stored before exposeObserver existed reads as false")
    void legacyJson() {
        SecuritySettings settings = json.readValue("{\"openReads\":false}", SecuritySettings.class);
        assertThat(settings.openReads()).isFalse();
        assertThat(settings.exposeObserver()).isFalse();
    }

    @Test
    void roundTrips() {
        SecuritySettings settings = new SecuritySettings(true, true);
        assertThat(json.readValue(json.writeValueAsString(settings), SecuritySettings.class)).isEqualTo(settings);
    }
}
