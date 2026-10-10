package ai.elaro.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

class ElaroVersionTest {

    private static BuildProperties buildProperties(String version) {
        Properties properties = new Properties();
        if (version != null) {
            properties.setProperty("version", version);
        }
        return new BuildProperties(properties);
    }

    private static ElaroProperties elaroProperties(String version) {
        return new ElaroProperties(Path.of("./data"), null, false, version);
    }

    @Test
    @DisplayName("build info wins when it is available")
    void prefersBuildProperties() {
        assertThat(ElaroVersion.resolve(buildProperties("1.2.3"), "9.9.9")).isEqualTo("1.2.3");
    }

    @Test
    @DisplayName("falls back to elaro.version when there is no build info")
    void fallsBackToConfiguredVersion() {
        assertThat(ElaroVersion.resolve(null, "0.1.0-SNAPSHOT")).isEqualTo("0.1.0-SNAPSHOT");
    }

    @Test
    @DisplayName("falls back to 'dev' when nothing is available")
    void fallsBackToDev() {
        assertThat(ElaroVersion.resolve(null, null)).isEqualTo(ElaroVersion.FALLBACK);
        assertThat(ElaroVersion.resolve(null, "")).isEqualTo(ElaroVersion.FALLBACK);
        assertThat(ElaroVersion.resolve(null, "   ")).isEqualTo(ElaroVersion.FALLBACK);
    }

    @Test
    @DisplayName("'unknown' is not treated as a version")
    void treatsUnknownAsAbsent() {
        // ElaroProperties normalizes a missing version to "unknown"; surfacing that as the running
        // version would be worse than saying "dev".
        assertThat(ElaroVersion.resolve(null, ElaroProperties.UNKNOWN_VERSION))
                .isEqualTo(ElaroVersion.FALLBACK);
    }

    @Test
    @DisplayName("build info with no version falls through to the property")
    void ignoresBuildPropertiesWithoutVersion() {
        assertThat(ElaroVersion.resolve(buildProperties(null), "0.2.0")).isEqualTo("0.2.0");
    }

    @Test
    @DisplayName("the bean resolves through the ObjectProvider")
    void beanResolvesFromProvider() {
        var version = new ElaroVersion(provider(buildProperties("4.5.6")), elaroProperties("0.0.1"));

        assertThat(version.version()).isEqualTo("4.5.6");
        assertThat(version).hasToString("4.5.6");
    }

    @Test
    @DisplayName("the bean copes with no BuildProperties bean present")
    void beanWorksWithoutBuildProperties() {
        var version = new ElaroVersion(provider(null), elaroProperties("0.1.0-SNAPSHOT"));

        assertThat(version.version()).isEqualTo("0.1.0-SNAPSHOT");
    }

    /** Every ObjectProvider method is a default, so overriding one is enough. */
    private static ObjectProvider<BuildProperties> provider(BuildProperties value) {
        return new ObjectProvider<>() {
            @Override
            public BuildProperties getIfAvailable() {
                return value;
            }
        };
    }
}
