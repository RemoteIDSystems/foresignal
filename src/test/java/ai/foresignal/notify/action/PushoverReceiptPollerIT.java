package ai.foresignal.notify.action;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.notify.ActionFixture;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertStore;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Pushover emergency receipts against a WireMock API (prompt 033): the stored receipt is polled, an
 * acknowledged receipt becomes a Foresignal acknowledgement by {@code pushover:<device>}, and an
 * unacknowledged or expired one changes nothing.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class PushoverReceiptPollerIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    @Autowired PushoverReceiptPoller poller;
    @Autowired NotificationRouter router;
    @Autowired DeliveryWorker worker;
    @Autowired AlertStore alerts;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired TriggerRuleRepository rules;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    ActionFixture fixture;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fixture = new ActionFixture(jdbc, rules);
        new NotifyFixture(channels, deliveries, limiter).channel("{'name':'po','type':'pushover','config':{'apiBaseUrl':'"
                + baseUrl() + "'},'secrets':{'appToken':'APP','userKey':'USER'}}");
    }

    long sendCritical(String receipt) {
        WIREMOCK.stubFor(post(urlEqualTo("/1/messages.json"))
                .willReturn(okJson("{\"status\":1,\"receipt\":\"" + receipt + "\",\"request\":\"x\"}")));
        long alert = fixture.alert(fixture.rule("Quakes " + receipt, T), "critical", T);
        router.fired(alerts.find(alert).orElseThrow());
        worker.runOnce();
        return alert;
    }

    @Test
    @DisplayName("an acknowledged receipt acknowledges the alert as pushover:<acknowledged_by_device>")
    void acknowledged() {
        long alert = sendCritical("rAck1");
        WIREMOCK.stubFor(get(urlEqualTo("/1/receipts/rAck1.json?token=APP")).willReturn(okJson(
                "{\"status\":1,\"acknowledged\":1,\"acknowledged_at\":1777636900,\"acknowledged_by\":\"USER\","
                        + "\"acknowledged_by_device\":\"iphone\",\"expired\":0,\"request\":\"x\"}")));
        clock.advance(Duration.ofMinutes(1));
        assertThat(poller.pollOnce()).isEqualTo(1);
        assertThat(fixture.state(alert)).isEqualTo("acknowledged");
        assertThat(fixture.acknowledgedBy(alert)).isEqualTo("pushover:iphone");
        // Acknowledged: the receipt is not polled again.
        assertThat(poller.pollOnce()).isZero();
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo("/1/receipts/rAck1.json?token=APP")));
    }

    @Test
    @DisplayName("an unacknowledged receipt is polled again; an expired one stops being polled; neither acknowledges")
    void pendingAndExpired() {
        long alert = sendCritical("rWait1");
        WIREMOCK.stubFor(get(urlEqualTo("/1/receipts/rWait1.json?token=APP")).willReturn(okJson(
                "{\"status\":1,\"acknowledged\":0,\"expired\":0,\"request\":\"x\"}")));
        assertThat(poller.pollOnce()).isZero();
        assertThat(poller.pollOnce()).isZero();
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo("/1/receipts/rWait1.json?token=APP")));
        WIREMOCK.stubFor(get(urlEqualTo("/1/receipts/rWait1.json?token=APP")).willReturn(okJson(
                "{\"status\":1,\"acknowledged\":0,\"expired\":1,\"request\":\"x\"}")));
        poller.pollOnce();
        poller.pollOnce();
        WIREMOCK.verify(3, getRequestedFor(urlEqualTo("/1/receipts/rWait1.json?token=APP")));
        assertThat(fixture.state(alert)).isEqualTo("firing");
    }

    @Test
    @DisplayName("receipts older than the emergency window are not polled")
    void window() {
        sendCritical("rOld1");
        clock.advance(Duration.ofHours(PushoverReceiptPoller.WINDOW_HOURS).plusMinutes(1));
        assertThat(poller.pollOnce()).isZero();
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo("/1/receipts/rOld1.json?token=APP")));
    }
}
