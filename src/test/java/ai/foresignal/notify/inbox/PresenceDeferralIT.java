package ai.foresignal.notify.inbox;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.notify.ActionFixture;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertStore;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Presence-aware deferral with a {@link MutableClock} (prompt 033): a non-critical alert waits while
 * a desktop is active, is suppressed when acknowledged, snoozed or read on a desktop before release,
 * is sent otherwise; critical alerts and system events go at once, and stale presence fails open.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class PresenceDeferralIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    @Autowired PresenceService presence;
    @Autowired InboxService inbox;
    @Autowired NotificationRouter router;
    @Autowired NotificationService notifications;
    @Autowired DeliveryWorker worker;
    @Autowired AlertStore alerts;
    @Autowired AlertService alertService;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired TriggerRuleRepository rules;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    ActionFixture fixture;
    NotifyFixture notify;
    UUID phone;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fixture = new ActionFixture(jdbc, rules);
        notify = new NotifyFixture(channels, deliveries, limiter);
        WIREMOCK.stubFor(post(urlEqualTo("/phone")).willReturn(ok()));
        phone = notify.channel("{'name':'phone','type':'webhook','config':{'url':'" + url("/phone")
                + "'},'events':['alert','system'],'deferWhileActiveSeconds':120}");
    }

    void desktopActive(int ttl) {
        presence.update("desk", new PresenceService.Input("desktop", "active", ttl));
    }

    long fire(String severity) {
        long alert = fixture.alert(fixture.rule("R" + System.nanoTime(), T), severity, T);
        router.fired(alerts.find(alert).orElseThrow());
        return alert;
    }

    NotificationDeliveryEntity only() {
        List<NotificationDeliveryEntity> rows = notify.deliveries(phone);
        assertThat(rows).hasSize(1);
        return rows.getFirst();
    }

    @Test
    @DisplayName("deferred while a desktop is active; sent after the deferral when still unread and firing")
    void deferredThenSent() {
        desktopActive(300);
        fire("warning");
        NotificationDeliveryEntity d = only();
        assertThat(d.getDeferredReason()).isEqualTo(DeferredReason.PRESENCE);
        assertThat(d.getNextAttemptAt()).isEqualTo(T.plusSeconds(120));
        worker.runOnce();
        WIREMOCK.verify(0, postRequestedFor(urlEqualTo("/phone")));
        clock.advance(Duration.ofSeconds(121));
        worker.runOnce();
        assertThat(only().getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/phone")));
    }

    @Test
    @DisplayName("acknowledged before release: suppressed, nothing sent, visible with a note")
    void ackedSuppressed() {
        desktopActive(300);
        long alert = fire("warning");
        alertService.acknowledge(alert, AlertService.Transition.NONE, "token:desktop");
        clock.advance(Duration.ofSeconds(121));
        worker.runOnce();
        NotificationDeliveryEntity d = notify.deliveries(phone).getFirst();
        assertThat(d.getStatus()).isEqualTo(DeliveryStatus.SUPPRESSED);
        assertThat(d.getNote()).contains("acknowledged");
        WIREMOCK.verify(0, postRequestedFor(urlEqualTo("/phone")));
    }

    @Test
    @DisplayName("snoozed before release: suppressed")
    void snoozedSuppressed() {
        desktopActive(300);
        long alert = fire("warning");
        alertService.snooze(alert, T.plus(Duration.ofHours(1)));
        clock.advance(Duration.ofSeconds(121));
        worker.runOnce();
        assertThat(notify.deliveries(phone).getFirst().getStatus()).isEqualTo(DeliveryStatus.SUPPRESSED);
    }

    @Test
    @DisplayName("read on the desktop before release: suppressed; read on a phone does not count")
    void readSuppressed() {
        desktopActive(300);
        presence.update("phone-1", new PresenceService.Input("mobile", "active", 300));
        fire("warning");
        long id = only().getNotificationId();
        inbox.markRead("phone-1", new InboxService.ReadRequest(id, List.of()));
        assertThat(inbox.readOnDesktop(id)).isFalse();
        inbox.markRead("desk", new InboxService.ReadRequest(null, List.of(id)));
        assertThat(inbox.readOnDesktop(id)).isTrue();
        clock.advance(Duration.ofSeconds(121));
        worker.runOnce();
        assertThat(only().getStatus()).isEqualTo(DeliveryStatus.SUPPRESSED);
        assertThat(only().getNote()).contains("read on a desktop");
    }

    @Test
    @DisplayName("critical alerts and system events are never deferred")
    void criticalAndSystemImmediate() {
        desktopActive(300);
        fire("critical");
        notifications.publish(NotificationMessage.builder(NotificationMessage.SYSTEM, "system.source_failing",
                "Source failing", "warning").build(), null);
        assertThat(notify.deliveries(phone)).hasSize(2).allSatisfy(d -> {
            assertThat(d.getDeferredReason()).isNull();
            assertThat(d.getNextAttemptAt()).isEqualTo(T);
        });
    }

    @Test
    @DisplayName("stale or expired presence fails open; idle or background desktops and active phones do not defer")
    void failOpen() {
        desktopActive(60);
        clock.advance(Duration.ofSeconds(61));
        presence.update("lap", new PresenceService.Input("desktop", "idle", 300));
        presence.update("tab", new PresenceService.Input("desktop", "background", 300));
        presence.update("pho", new PresenceService.Input("mobile", "active", 300));
        fire("warning");
        assertThat(only().getDeferredReason()).isNull();
        assertThat(presence.desktopActive(clock.instant())).isFalse();
    }

    @Test
    @DisplayName("presence parsing: kind, state and ttl ≤ 300 are validated")
    void parse() {
        PresenceService.Input input = PresenceService.parse(NotifyFixture.JSON.readTree("{\"kind\":\"desktop\",\"state\":\"active\"}"));
        assertThat(input.ttlSeconds()).isEqualTo(PresenceService.DEFAULT_TTL);
        for (String bad : List.of("{\"kind\":\"tv\",\"state\":\"active\"}", "{\"kind\":\"web\",\"state\":\"away\"}",
                "{\"kind\":\"web\",\"state\":\"idle\",\"ttlSeconds\":301}", "{\"kind\":\"web\",\"state\":\"idle\",\"x\":1}")) {
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> PresenceService.parse(NotifyFixture.JSON.readTree(bad)))
                    .as(bad).isInstanceOf(ai.foresignal.api.error.ValidationException.class);
        }
        assertThat(PresenceService.kindOf("macos")).isEqualTo("desktop");
        assertThat(PresenceService.kindOf("android")).isEqualTo("mobile");
        assertThat(PresenceService.kindOf(null)).isEqualTo("web");
    }
}
