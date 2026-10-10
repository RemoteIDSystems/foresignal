package ai.elaro.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RetentionAndCorsSettingsTest {

    @Test
    @DisplayName("retention defaults are 7d records, 90d alerts, 30d usage, no overrides")
    void retentionDefaults() {
        RetentionSettings settings = RetentionSettings.defaults();

        assertThat(settings.defaultRetention()).isEqualTo(Duration.ofDays(7));
        assertThat(settings.alerts()).isEqualTo(Duration.ofDays(90));
        assertThat(settings.usage()).isEqualTo(Duration.ofDays(30));
        assertThat(settings.sources()).isEmpty();
    }

    @Test
    @DisplayName("missing retention fields fall back to the defaults")
    void retentionFillsMissingFields() {
        var settings = new RetentionSettings(null, null, null, null);

        assertThat(settings.defaultRetention()).isEqualTo(RetentionSettings.DEFAULT_RETENTION);
        assertThat(settings.alerts()).isEqualTo(RetentionSettings.DEFAULT_ALERTS);
        assertThat(settings.usage()).isEqualTo(RetentionSettings.DEFAULT_USAGE);
        assertThat(settings.sources()).isEmpty();
    }

    @Test
    void forSourcePrefersTheOverride() {
        var settings =
                new RetentionSettings(
                        Duration.ofDays(7),
                        Duration.ofDays(90),
                        Duration.ofDays(30),
                        Map.of("adsblol-mil", Duration.ofDays(2)));

        assertThat(settings.forSource("adsblol-mil")).isEqualTo(Duration.ofDays(2));
        assertThat(settings.forSource("usgs-earthquakes")).isEqualTo(Duration.ofDays(7));
    }

    @Test
    @DisplayName("the sources map is defensively copied")
    void retentionCopiesSources() {
        var mutable = new LinkedHashMap<String, Duration>();
        mutable.put("a", Duration.ofDays(1));
        var settings = new RetentionSettings(null, null, null, mutable);

        mutable.put("b", Duration.ofDays(2));

        assertThat(settings.sources()).containsOnlyKeys("a");
        assertThatThrownBy(() -> settings.sources().put("c", Duration.ofDays(3)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("CORS defaults open reads and closed writes")
    void corsDefaults() {
        CorsSettings settings = CorsSettings.defaults();

        assertThat(settings.readOrigins()).containsExactly("*");
        assertThat(settings.writeOrigins()).isEmpty();
    }

    @Test
    void corsFillsMissingFields() {
        var settings = new CorsSettings(null, null);

        assertThat(settings.readOrigins()).isEqualTo(CorsSettings.DEFAULT_READ_ORIGINS);
        assertThat(settings.writeOrigins()).isEqualTo(CorsSettings.DEFAULT_WRITE_ORIGINS);
    }

    @Test
    void corsKeepsSuppliedOrigins() {
        var settings =
                new CorsSettings(
                        java.util.List.of("https://globe.example"),
                        java.util.List.of("http://localhost:5173"));

        assertThat(settings.readOrigins()).containsExactly("https://globe.example");
        assertThat(settings.writeOrigins()).containsExactly("http://localhost:5173");
    }

    @Test
    void corsListsAreImmutable() {
        CorsSettings settings = CorsSettings.defaults();

        assertThatThrownBy(() -> settings.readOrigins().add("https://evil.example"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
