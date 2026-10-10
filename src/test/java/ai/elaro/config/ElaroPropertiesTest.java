package ai.elaro.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class ElaroPropertiesTest {

    private static ElaroProperties bind(MapConfigurationPropertySource source) {
        return new Binder(source).bind("elaro", ElaroProperties.class).get();
    }

    @Test
    @DisplayName("binds every property from configuration")
    void bindsSuppliedValues() {
        var source = new MapConfigurationPropertySource();
        source.put("elaro.data-dir", "/var/lib/elaro");
        source.put("elaro.reset-admin-token", "true");
        source.put("elaro.version", "1.2.3");

        ElaroProperties properties = bind(source);

        assertThat(properties.dataDir()).isEqualTo(Path.of("/var/lib/elaro"));
        assertThat(properties.resetAdminToken()).isTrue();
        assertThat(properties.version()).isEqualTo("1.2.3");
    }

    @Test
    @DisplayName("falls back to ./data when elaro.data-dir is absent")
    void appliesDefaultDataDir() {
        var source = new MapConfigurationPropertySource();
        source.put("elaro.version", "1.2.3");

        ElaroProperties properties = bind(source);

        assertThat(properties.dataDir()).isEqualTo(ElaroProperties.DEFAULT_DATA_DIR);
        assertThat(properties.dataDir()).isEqualTo(Path.of("./data"));
    }

    @Test
    @DisplayName("reset-admin-token defaults to false")
    void resetAdminTokenDefaultsToFalse() {
        var source = new MapConfigurationPropertySource();
        source.put("elaro.version", "1.2.3");

        assertThat(bind(source).resetAdminToken()).isFalse();
    }

    @Test
    @DisplayName("falls back to 'unknown' when no build version is available")
    void appliesDefaultVersionWhenAbsent() {
        var source = new MapConfigurationPropertySource();
        source.put("elaro.data-dir", "/var/lib/elaro");

        assertThat(bind(source).version()).isEqualTo(ElaroProperties.UNKNOWN_VERSION);
    }

    @Test
    @DisplayName("treats a blank version as unknown")
    void treatsBlankVersionAsUnknown() {
        var source = new MapConfigurationPropertySource();
        source.put("elaro.version", "   ");

        assertThat(bind(source).version()).isEqualTo(ElaroProperties.UNKNOWN_VERSION);
    }

    @Test
    @DisplayName("applies the same defaults when constructed directly")
    void appliesDefaultsWhenConstructedDirectly() {
        var properties = new ElaroProperties(null, null, false, null);

        assertThat(properties.dataDir()).isEqualTo(ElaroProperties.DEFAULT_DATA_DIR);
        assertThat(properties.version()).isEqualTo(ElaroProperties.UNKNOWN_VERSION);
        assertThat(properties.resetAdminToken()).isFalse();
    }

    @Test
    @DisplayName("the inbox dir defaults to a subdirectory of the data dir")
    void inboxDirDefaultsUnderDataDir() {
        var source = new MapConfigurationPropertySource();
        source.put("elaro.data-dir", "/var/lib/elaro");

        assertThat(bind(source).inboxDir()).isEqualTo(Path.of("/var/lib/elaro/inbox"));
    }

    @Test
    @DisplayName("an explicit inbox dir is kept, wherever it points")
    void keepsExplicitInboxDir() {
        var source = new MapConfigurationPropertySource();
        source.put("elaro.data-dir", "/var/lib/elaro");
        source.put("elaro.inbox-dir", "/mnt/satdump/inbox");

        assertThat(bind(source).inboxDir()).isEqualTo(Path.of("/mnt/satdump/inbox"));
    }

    @Test
    @DisplayName("the inbox dir follows the default data dir when neither is set")
    void inboxDirFollowsDefaultDataDir() {
        var source = new MapConfigurationPropertySource();
        source.put("elaro.version", "1.0.0");

        assertThat(bind(source).inboxDir())
                .isEqualTo(ElaroProperties.DEFAULT_DATA_DIR.resolve(ElaroProperties.DEFAULT_INBOX_SUBDIR));
    }

    @Test
    @DisplayName("keeps a relative data dir as given")
    void keepsRelativeDataDir() {
        var source = new MapConfigurationPropertySource();
        source.put("elaro.data-dir", "custom-data");

        assertThat(bind(source).dataDir()).isEqualTo(Path.of("custom-data"));
    }
}
