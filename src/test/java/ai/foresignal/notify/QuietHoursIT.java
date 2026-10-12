package ai.foresignal.notify;

import static ai.foresignal.testsupport.notify.NotifyFixture.alert;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.NotificationSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.DeliveryKind;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/** Quiet hours, the global pause, backlog folding and grouping (prompt 018), driven by a {@link MutableClock}. */
@IntegrationTest
@Import(NotifyTestConfig.class)
class QuietHoursIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-0000000000b1");

    @Autowired NotificationRouter router;
    @Autowired DeliveryWorker worker;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;

    NotifyFixture fixture;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        fixture = new NotifyFixture(channels, deliveries, limiter);
        WIREMOCK.stubFor(post(urlEqualTo("/hook")).willReturn(ok()));
    }

    UUID channel(String extra) {
        return fixture.channel("{'name':'w','type':'webhook','events':['alert','system'],'timezone':'UTC',"
                + "'config':{'url':'" + url("/hook") + "'}" + (extra.isEmpty() ? "" : "," + extra) + "}");
    }

    JsonNode lastBody() {
        List<LoggedRequest> requests = WIREMOCK.findAll(postRequestedFor(urlEqualTo("/hook")));
        return NotifyFixture.JSON.readTree(requests.getLast().getBodyAsString());
    }

    @Test
    @DisplayName("inside quiet hours a warning is deferred to the window end; critical bypasses")
    void deferral() {
        UUID c = channel("'quietHours':{'start':'11:00','end':'13:00'}");
        router.fired(alert(1, RULE, "Quakes", "warning", T));
        router.fired(alert(2, RULE, "Quakes", "critical", T));
        List<NotificationDeliveryEntity> rows = fixture.deliveries(c);
        assertThat(rows.get(0).getNextAttemptAt()).isEqualTo(Instant.parse("2026-05-01T13:00:00Z"));
        assertThat(rows.get(0).getDeferredReason()).isEqualTo(DeferredReason.QUIET_HOURS);
        assertThat(rows.get(1).getNextAttemptAt()).isEqualTo(T);
        assertThat(rows.get(1).getDeferredReason()).isNull();

        worker.runOnce();
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/hook")));
        assertThat(lastBody().get("message").get("severity").asString()).isEqualTo("critical");
    }

    @Test
    @DisplayName("several deferred alerts are released at the window end as one digest, newest first")
    void digestAtWindowEnd() {
        UUID c = channel("'quietHours':{'start':'11:00','end':'13:00','bypassSeverity':null}");
        for (long id = 1; id <= 3; id++) {
            router.fired(alert(id, RULE, "Quakes", "warning", T));
        }
        assertThat(worker.runOnce()).isZero();
        clock.setInstant(Instant.parse("2026-05-01T13:00:01Z"));
        worker.runOnce();

        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/hook")));
        JsonNode message = lastBody().get("message");
        assertThat(message.get("subtype").asString()).isEqualTo("alert.digest");
        assertThat(message.get("title").asString()).isEqualTo("[warning] Quakes: 3 alerts");
        assertThat(message.get("bodyMarkdown").asString()).startsWith("- **[warning]** \\[warning\\] Quakes");
        List<NotificationDeliveryEntity> rows = fixture.deliveries(c);
        assertThat(rows).allSatisfy(d -> assertThat(d.getStatus()).isEqualTo(DeliveryStatus.DELIVERED));
        NotificationDeliveryEntity digest = rows.stream().filter(d -> DeliveryKind.DIGEST.equals(d.getKind())).findFirst().orElseThrow();
        assertThat(rows.stream().filter(d -> d.getFoldedInto() != null))
                .hasSize(2).allSatisfy(d -> assertThat(d.getNote()).isEqualTo("folded into delivery " + digest.getId()));
    }

    @Test
    @DisplayName("the global pause defers everything below its bypass and releases one digest per channel")
    void globalPause() {
        UUID c = channel("");
        settings.put(SettingKeys.NOTIFICATIONS, new NotificationSettings(Instant.parse("2026-05-01T14:00:00Z"), "critical"));
        router.fired(alert(1, RULE, "Quakes", "warning", T));
        router.fired(alert(2, RULE, "Quakes", "info", T));
        router.fired(alert(3, RULE, "Quakes", "critical", T));
        assertThat(fixture.pending(c)).filteredOn(d -> DeferredReason.PAUSED.equals(d.getDeferredReason())).hasSize(2);

        worker.runOnce();
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/hook")));

        clock.setInstant(Instant.parse("2026-05-01T14:00:00Z"));
        worker.runOnce();
        WIREMOCK.verify(2, postRequestedFor(urlEqualTo("/hook")));
        assertThat(lastBody().get("message").get("title").asString()).isEqualTo("[warning] Quakes: 2 alerts");
        assertThat(fixture.pending(c)).isEmpty();
    }

    @Test
    @DisplayName("pending sends above the backlog cap are folded into one digest (+N more alerts)")
    void backlogFolding() {
        UUID c = channel("'backlogCap':3");
        settings.put(SettingKeys.NOTIFICATIONS, new NotificationSettings(Instant.parse("2026-05-01T14:00:00Z"), null));
        for (long id = 1; id <= 5; id++) {
            router.fired(alert(id, RULE, "Quakes", "warning", T));
        }
        List<NotificationDeliveryEntity> pending = fixture.pending(c);
        assertThat(pending).hasSize(3);
        NotificationDeliveryEntity digest = pending.getFirst();
        assertThat(digest.getKind()).isEqualTo(DeliveryKind.DIGEST);
        assertThat(fixture.deliveries(c).stream().filter(d -> d.getFoldedInto() != null))
                .hasSize(2).allSatisfy(d -> {
                    assertThat(d.getFoldedInto()).isEqualTo(digest.getId());
                    assertThat(d.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
                    assertThat(d.getNote()).startsWith("folded into delivery ");
                });
    }

    @Test
    @DisplayName("lifecycle updates and system events are never folded into a digest")
    void updatesNotFolded() {
        UUID c = channel("'backlogCap':1");
        settings.put(SettingKeys.NOTIFICATIONS, new NotificationSettings(Instant.parse("2026-05-01T14:00:00Z"), null));
        var a = alert(1, RULE, "Quakes", "warning", T);
        router.fired(a);
        router.lifecycle("resolved", NotifyFixture.resolved(a, T, "admin"));
        router.onRuleStorm(new ai.foresignal.trigger.RuleStormEvent(RULE, "Quakes", T, 10, 1, false));
        assertThat(fixture.deliveries(c)).allSatisfy(d -> assertThat(d.getFoldedInto()).isNull());
        assertThat(fixture.pending(c)).hasSize(3);
    }

    @Test
    @DisplayName("grouping holds the first alert of a rule for the window and sends one grouped message")
    void grouping() {
        UUID c = channel("'groupWindowSeconds':60");
        for (long id = 1; id <= 3; id++) {
            router.fired(alert(id, RULE, "Quakes", "warning", T.plusSeconds(id)));
            clock.advanceSeconds(5);
        }
        router.fired(alert(9, UUID.randomUUID(), "Other", "info", T));
        List<NotificationDeliveryEntity> rows = fixture.deliveries(c);
        assertThat(rows.getFirst().getDeferredReason()).isEqualTo(DeferredReason.GROUPING);
        assertThat(rows.getFirst().getNextAttemptAt()).isEqualTo(T.plusSeconds(60));
        assertThat(rows.stream().filter(d -> d.getFoldedInto() != null)).hasSize(2);

        assertThat(worker.runOnce()).isZero();
        clock.setInstant(T.plusSeconds(61));
        worker.runOnce();
        // The other rule's holder started its own window later (at T+15 s), so only the grouped message is due.
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/hook")).withHeader("X-Foresignal-Event", equalTo("alert")));
        assertThat(lastBody().get("message").get("title").asString()).isEqualTo("[warning] Quakes: 3 alerts");
    }
}
