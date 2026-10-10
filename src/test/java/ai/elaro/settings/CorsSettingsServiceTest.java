package ai.elaro.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.elaro.api.error.FieldError;
import ai.elaro.api.error.ValidationException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Origin validation, which runs for both the settings API and settings import. */
class CorsSettingsServiceTest {

    private final CorsSettingsService service = new CorsSettingsService(null);

    @ParameterizedTest
    @ValueSource(
            strings = {
                "http://localhost:5173",
                "https://globe.example",
                "https://globe.example:8443",
                "http://192.168.1.10:8080",
                "https://sub.domain.example.com",
            })
    void acceptsValidOrigins(String origin) {
        assertThat(CorsSettingsService.isValidOrigin(origin)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "globe.example",
                "https://globe.example/",
                "https://globe.example/path",
                "https://globe.example?q=1",
                "https://globe.example#frag",
                "https://user:pass@globe.example",
                "not a url",
                "://missing-scheme",
            })
    @DisplayName("rejects anything that is not a bare scheme://host[:port]")
    void rejectsInvalidOrigins(String origin) {
        assertThat(CorsSettingsService.isValidOrigin(origin)).isFalse();
    }

    @Test
    @DisplayName("a wildcard is fine for reads")
    void allowsWildcardForReads() {
        var settings = new CorsSettings(List.of("*"), List.of());

        assertThat(service.validate(settings)).isSameAs(settings);
    }

    @Test
    @DisplayName("a wildcard is refused for writes, with a reason")
    void rejectsWildcardForWrites() {
        var settings = new CorsSettings(List.of("*"), List.of("*"));

        assertThatThrownBy(() -> service.validate(settings))
                .isInstanceOf(ValidationException.class)
                .satisfies(
                        e -> {
                            List<FieldError> errors = ((ValidationException) e).getErrors();
                            assertThat(errors).hasSize(1);
                            assertThat(errors.get(0).field()).isEqualTo("writeOrigins");
                            assertThat(errors.get(0).message()).contains("explicit origin list");
                        });
    }

    @Test
    void acceptsExplicitWriteOrigins() {
        var settings = new CorsSettings(List.of("*"), List.of("http://localhost:5173"));

        assertThat(service.validate(settings)).isSameAs(settings);
    }

    @Test
    @DisplayName("an invalid origin names the field and suggests the right shape")
    void reportsInvalidOrigin() {
        var settings = new CorsSettings(List.of("globe.example"), List.of());

        assertThatThrownBy(() -> service.validate(settings))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("not valid")
                .satisfies(
                        e -> {
                            List<FieldError> errors = ((ValidationException) e).getErrors();
                            assertThat(errors.get(0).field()).isEqualTo("readOrigins");
                            assertThat(errors.get(0).message()).contains("scheme://host[:port]");
                        });
    }

    @Test
    @DisplayName("every bad origin is reported, not just the first")
    void reportsAllErrors() {
        var settings = new CorsSettings(List.of("bad-one", "bad-two"), List.of("*", "also bad"));

        assertThatThrownBy(() -> service.validate(settings))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors()).hasSize(4));
    }

    @Test
    void rejectsBlankOrigin() {
        var settings = new CorsSettings(List.of("   "), List.of());

        assertThatThrownBy(() -> service.validate(settings))
                .isInstanceOf(ValidationException.class)
                .satisfies(
                        e ->
                                assertThat(((ValidationException) e).getErrors().get(0).message())
                                        .contains("blank"));
    }

    @Test
    void rejectsNullSettings() {
        assertThatThrownBy(() -> service.validate(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void acceptsTheDefaults() {
        assertThat(service.validate(CorsSettings.defaults())).isNotNull();
    }
}
