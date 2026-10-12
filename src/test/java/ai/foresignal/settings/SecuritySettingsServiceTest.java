package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Parsing of {@code SecuritySettings} bodies (prompts 023 and 035). */
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
    @DisplayName("035: metricsOpen and the anonymous profile are accepted; excludePrivate stays on")
    void parsesPerimeterMembers() {
        SecuritySettings closed = parse("{\"openReads\":true,\"metricsOpen\":false}");
        assertThat(closed.metricsOpen()).isFalse();
        assertThat(parse("{\"openReads\":true}").metricsOpen()).isTrue();
        SecuritySettings profiled = parse("{\"openReads\":true,\"anonymous\":{\"kinds\":[\"earthquake\"],"
                + "\"maxRequestsPerMinute\":30,\"bbox\":[-10,-10,10,10]}}");
        assertThat(profiled.anonymous().kinds()).containsExactly("earthquake");
        assertThat(profiled.anonymousUnrestricted()).isFalse();
        assertThat(profiled.effectiveAnonymous().excludePrivate()).isTrue();
        assertThat(profiled.effectiveAnonymous().maxRequestsPerMinute()).isEqualTo(30);
        SecuritySettings empty = parse("{\"openReads\":true,\"anonymous\":{}}");
        assertThat(empty.anonymous()).isNull();
        assertThat(empty.anonymousUnrestricted()).isTrue();
        assertThat(empty.effectiveAnonymous().maxRequestsPerMinute())
                .isEqualTo(SecuritySettings.DEFAULT_ANONYMOUS_RPM);
    }

    @Test
    @DisplayName("missing or mistyped members, unknown members and later members are refused")
    void refuses() {
        for (String bad : new String[] {"{}", "[]", "{\"openReads\":\"yes\"}", "{\"openReads\":true,\"exposeObserver\":1}",
            "{\"openReads\":true,\"metricsOpen\":\"no\"}", "{\"openReads\":true,\"anonymous\":[]}",
            "{\"openReads\":true,\"anonymous\":{\"excludePrivate\":false}}",
            "{\"openReads\":true,\"anonymous\":{\"placeIds\":[\"home\"]}}",
            "{\"openReads\":true,\"exposeMlat\":true}",
            "{\"openReads\":true,\"colour\":\"red\"}"}) {
            assertThatThrownBy(() -> parse(bad)).as(bad).isInstanceOf(ValidationException.class);
        }
    }
}
