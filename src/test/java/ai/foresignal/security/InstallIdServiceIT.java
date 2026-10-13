package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class InstallIdServiceIT {

    @Autowired private AppSettingService settings;
    @Autowired private ForesignalVersion version;

    @BeforeEach
    void clearCachedSettings() {
        // The table is truncated for us, but the settings cache is a singleton field.
        settings.invalidateCache();
    }

    private InstallIdService freshService() {
        var service = new InstallIdService(settings, version);
        service.afterPropertiesSet();
        return service;
    }

    @Test
    @DisplayName("the id is 8 lowercase alphanumerics")
    void generatesWellFormedId() {
        assertThat(freshService().installId()).hasSize(8).matches("[a-z0-9]{8}");
    }

    @Test
    @DisplayName("the id is stable across calls")
    void idIsStableAcrossCalls() {
        InstallIdService service = freshService();

        String first = service.installId();

        assertThat(service.installId()).isEqualTo(first);
        assertThat(service.installId()).isEqualTo(first);
    }

    @Test
    @DisplayName("the id is persisted, so a restart keeps it")
    void idSurvivesRestart() {
        String first = freshService().installId();
        settings.invalidateCache();

        assertThat(freshService().installId()).isEqualTo(first);
        assertThat(settings.get(SettingKeys.INSTALL_ID, String.class)).contains(first);
    }

    @Test
    @DisplayName("the User-Agent identifies Foresignal, its version and this install")
    void userAgentFormat() {
        InstallIdService service = freshService();

        String userAgent = service.userAgent();

        assertThat(userAgent)
                .isEqualTo(
                        "Foresignal/" + version.version() + " (+https://foresignal.ai; install=" + service.installId() + ")")
                .matches("Foresignal/\\S+ \\(\\+https://foresignal\\.ai; install=[a-z0-9]{8}\\)");
    }

    @Test
    @DisplayName("the User-Agent does not change between calls")
    void userAgentIsStable() {
        InstallIdService service = freshService();

        assertThat(service.userAgent()).isEqualTo(service.userAgent());
    }

    @Test
    @DisplayName("an id already in settings is used rather than replaced")
    void adoptsAnExistingId() {
        settings.put(SettingKeys.INSTALL_ID, "abcd1234");

        assertThat(freshService().installId()).isEqualTo("abcd1234");
    }

    @Test
    @DisplayName("a blank stored id is replaced rather than used")
    void replacesBlankStoredId() {
        settings.put(SettingKeys.INSTALL_ID, "   ");

        assertThat(freshService().installId()).hasSize(8).isNotBlank();
    }

    @Test
    @DisplayName("two installations get different ids")
    void idsDifferBetweenInstallations() {
        String first = freshService().installId();
        settings.put(SettingKeys.INSTALL_ID, "");
        settings.invalidateCache();

        // A second installation starting from empty settings would generate its own.
        String second = freshService().installId();

        assertThat(second).hasSize(8);
        assertThat(first).hasSize(8);
    }

    @Test
    @DisplayName("server.contactEmail is appended to the User-Agent as a contact (prompt 047)")
    void userAgentContact() {
        settings.put(SettingKeys.SERVER, new ai.foresignal.settings.ServerSettings(null, null, null, null, " ops@example.org "));
        InstallIdService service = freshService();

        assertThat(service.userAgent()).isEqualTo("Foresignal/" + version.version() + " (+https://foresignal.ai; install="
                + service.installId() + "; contact=ops@example.org)");
    }
}
