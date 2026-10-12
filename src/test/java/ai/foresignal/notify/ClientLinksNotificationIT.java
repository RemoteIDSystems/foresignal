package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** 018's alert links gain "Open in app" from {@code clientLinks} once the operator sets it (prompt 023). */
@IntegrationTest
class ClientLinksNotificationIT {

    @Autowired private NotificationRouter router;
    @Autowired private AppSettingService settings;

    @BeforeEach
    void clean() {
        settings.invalidateCache();
    }

    @Test
    @DisplayName("without clientLinks an alert has only its API link")
    void apiLinkOnly() {
        assertThat(router.alertLinks(7, "http://x/v1/alerts/7")).extracting(NotificationMessage.Link::label)
                .containsExactly("Open alert");
    }

    @Test
    @DisplayName("with clientLinks the app link fills the alert template")
    void appLink() {
        settings.put(SettingKeys.SERVER, new ServerSettings("https://fs.example", null,
                new ClientLinks("https://fs.example/app/alerts/{id}", null, null, null)));

        assertThat(router.alertLinks(7, "https://fs.example/v1/alerts/7"))
                .extracting(NotificationMessage.Link::url)
                .containsExactly("https://fs.example/v1/alerts/7", "https://fs.example/app/alerts/7");
    }
}
