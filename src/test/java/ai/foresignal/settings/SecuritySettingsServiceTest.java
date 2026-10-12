package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Parsing of {@code SecuritySettings} bodies (prompt 023). */
class SecuritySettingsServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static SecuritySettings parse(String text) {
        return SecuritySettingsService.parse(JSON.readTree(text));
    }

    @Test
    @DisplayName("openReads is required; exposeObserver defaults to false")
    void parses() {
        assertThat(parse("{\"openReads\":false}")).isEqualTo(new SecuritySettings(false, false));
        assertThat(parse("{\"openReads\":true,\"exposeObserver\":true}")).isEqualTo(new SecuritySettings(true, true));
        assertThat(parse("{\"openReads\":true,\"metricsOpen\":null}")).isEqualTo(new SecuritySettings(true, false));
    }

    @Test
    @DisplayName("missing or mistyped members, unknown members and later members are refused")
    void refuses() {
        for (String bad : new String[] {"{}", "[]", "{\"openReads\":\"yes\"}", "{\"openReads\":true,\"exposeObserver\":1}",
            "{\"openReads\":true,\"metricsOpen\":false}", "{\"openReads\":true,\"anonymous\":{}}",
            "{\"openReads\":true,\"colour\":\"red\"}"}) {
            assertThatThrownBy(() -> parse(bad)).as(bad).isInstanceOf(ValidationException.class);
        }
    }
}
