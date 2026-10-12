package ai.foresignal.notify;

import static ai.foresignal.testsupport.notify.NotifyFixture.alert;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Notification payloads of private-source alerts (prompt 039, §2 "Alerts"): a channel without
 * {@code privatePayload} receives only the rule name, severity and a link; a channel with
 * {@code privatePayload: true} receives the payload; a fan-out push type (Web Push, APNs, FCM) never
 * does. The source is made private through the operator's tightening, so the built-in USGS source of
 * the fixture alerts serves as the private source.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class PrivatePayloadsIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-0000000000d1");
    static final String SOURCE = "usgs-earthquakes";

    @Autowired NotificationRouter router;
    @Autowired DeliveryWorker worker;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired AppSettingService settings;
    @Autowired SourceVisibility visibility;
    @Autowired MutableClock clock;

    NotifyFixture fixture;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        fixture = new NotifyFixture(channels, deliveries, limiter);
        visibility.setOperatorPrivate(SOURCE, true);
        visibility.refresh();
    }

    @AfterEach
    void restore() {
        visibility.setOperatorPrivate(SOURCE, false);
        visibility.refresh();
    }

    private static NotificationMessage message(String sourceId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alertId", 7L);
        data.put("ruleName", "Family geofence");
        data.put("severity", "warning");
        data.put("sourceId", sourceId);
        data.put("message", "Phone left Home at 12:00");
        data.put("record", Map.of("payload", Map.of("lat", 45.5)));
        return NotificationMessage.builder(NotificationMessage.ALERT, "alert.fired", "Phone left Home", "warning")
                .body("Phone left Home at 12:00 (45.5, -73.6)")
                .url("https://foresignal.example/v1/alerts/7")
                .data(data)
                .alertId(7L)
                .build();
    }

    private static NotificationChannelEntity channel(String type, Map<String, Object> config) {
        NotificationChannelEntity entity = mock(NotificationChannelEntity.class);
        when(entity.getType()).thenReturn(type);
        when(entity.getConfig()).thenReturn(config);
        return entity;
    }

    @Test
    @DisplayName("redact keeps the rule name, severity and link only")
    void redact() {
        NotificationMessage redacted = PrivatePayloads.redact(message(SOURCE));
        assertThat(redacted.title()).isEqualTo("Family geofence (warning)");
        assertThat(redacted.bodyMarkdown()).contains("https://foresignal.example/v1/alerts/7").doesNotContain("Home");
        assertThat(redacted.data()).containsEntry("private", true).containsEntry("ruleName", "Family geofence")
                .doesNotContainKeys("record", "message", "sourceId");
        assertThat(redacted.url()).isEqualTo("https://foresignal.example/v1/alerts/7");
        assertThat(PrivatePayloads.sourceId(message(SOURCE))).isEqualTo(SOURCE);
        assertThat(PrivatePayloads.optedIn(Map.of(PrivatePayloads.CONFIG_KEY, true))).isTrue();
        assertThat(PrivatePayloads.optedIn(Map.of())).isFalse();
        assertThat(PrivatePayloads.optedIn(null)).isFalse();
    }

    @Test
    @DisplayName("per channel: redacted without privatePayload, full with it, always redacted on push")
    void perChannel() {
        NotificationMessage m = message(SOURCE);
        assertThat(worker.privacy(m, channel("webhook", Map.of())).bodyMarkdown()).doesNotContain("Home");
        assertThat(worker.privacy(m, channel("webhook", Map.of(PrivatePayloads.CONFIG_KEY, true))).bodyMarkdown())
                .contains("Home");
        assertThat(worker.privacy(m, channel("webpush", Map.of(PrivatePayloads.CONFIG_KEY, true))).bodyMarkdown())
                .as("push never carries a private payload").doesNotContain("Home");
        NotificationMessage open = message("some-public-source");
        assertThat(worker.privacy(open, channel("webhook", Map.of()))).isSameAs(open);
        assertThat(worker.privacy(open, channel("webpush", Map.of()))).isSameAs(open);
    }

    @Test
    @DisplayName("end to end: a webhook without privatePayload receives no snapshot, message or place")
    void webhookEndToEnd() {
        WIREMOCK.stubFor(post(urlEqualTo("/hook")).willReturn(aResponse().withStatus(200)));
        WIREMOCK.stubFor(post(urlEqualTo("/hook-private")).willReturn(aResponse().withStatus(200)));
        fixture.channel("{'name':'w','type':'webhook','config':{'url':'" + url("/hook") + "'}}");
        fixture.channel("{'name':'wp','type':'webhook','config':{'url':'" + url("/hook-private")
                + "','privatePayload':true}}");

        router.fired(alert(21, RULE, "Family geofence", "warning", T));
        worker.runOnce();

        List<LoggedRequest> plain = WIREMOCK.findAll(postRequestedFor(urlEqualTo("/hook")));
        assertThat(plain).hasSize(1);
        assertThat(plain.getFirst().getBodyAsString()).contains("Family geofence").doesNotContain("Austin")
                .doesNotContain("\"mag\"");
        List<LoggedRequest> opted = WIREMOCK.findAll(postRequestedFor(urlEqualTo("/hook-private")));
        assertThat(opted).hasSize(1);
        assertThat(opted.getFirst().getBodyAsString()).contains("Austin");
    }

    @Test
    @DisplayName("ChannelInputParser rejects a privatePayload that is not a boolean")
    void parserRejectsNonBoolean() {
        List<FieldError> errors = new ArrayList<>();
        ChannelInputParser.actionsConfig(Map.of(PrivatePayloads.CONFIG_KEY, "yes"), errors);
        assertThat(errors).extracting(FieldError::field).containsExactly("config.privatePayload");
        List<FieldError> none = new ArrayList<>();
        ChannelInputParser.actionsConfig(Map.of(PrivatePayloads.CONFIG_KEY, true), none);
        assertThat(none).isEmpty();
    }
}
