package ai.foresignal.notify;

import static ai.foresignal.testsupport.notify.NotifyFixture.alert;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.SourceHealthChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.DeliveryKind;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.NotificationRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.RuleStormEvent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;

/** Routing of alerts and system events into notifications and deliveries (prompt 018). */
@IntegrationTest
@Import(NotifyTestConfig.class)
class NotificationRouterIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final UUID R1 = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    static final UUID R2 = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
    static final String SLACK = "'secrets':{'webhookUrl':'https://hooks.example.invalid/services/T/B/X'}";

    @Autowired NotificationRouter router;
    @Autowired NotificationService service;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationRepository notifications;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired NotificationChannelRepository channelRows;
    @Autowired AppSettingService settings;
    @Autowired ApplicationEventPublisher events;
    @Autowired MutableClock clock;

    NotifyFixture fixture;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        fixture = new NotifyFixture(channels, deliveries, limiter);
    }

    UUID slack(String extra) {
        return fixture.channel("{'name':'s','type':'slack'," + SLACK + (extra.isEmpty() ? "" : "," + extra) + "}");
    }

    @Test
    @DisplayName("rule, severity and event filters pick the channels; disabled channels get nothing")
    void filters() {
        UUID all = slack("");
        UUID onlyR2 = slack("'ruleIds':['" + R2 + "']");
        UUID criticalOnly = slack("'minSeverity':'critical'");
        UUID systemOnly = slack("'events':['system']");
        UUID disabled = slack("'enabled':false");

        router.fired(alert(1, R1, "Quakes", "warning", T));

        assertThat(fixture.deliveries(all)).singleElement().satisfies(d -> {
            assertThat(d.getKind()).isEqualTo(DeliveryKind.SEND);
            assertThat(d.getStatus()).isEqualTo(DeliveryStatus.PENDING);
            assertThat(d.getAlertId()).isEqualTo(1L);
            assertThat(d.getNextAttemptAt()).isEqualTo(T);
        });
        assertThat(fixture.deliveries(onlyR2)).isEmpty();
        assertThat(fixture.deliveries(criticalOnly)).isEmpty();
        assertThat(fixture.deliveries(systemOnly)).isEmpty();
        assertThat(fixture.deliveries(disabled)).isEmpty();

        router.fired(alert(2, R2, "Other", "critical", T));
        assertThat(fixture.deliveries(onlyR2)).hasSize(1);
        assertThat(fixture.deliveries(criticalOnly)).hasSize(1);
    }

    @Test
    @DisplayName("every message gets a notification row, including a system event no channel takes")
    void notificationRows() {
        NotificationMessage fired = router.fired(alert(3, R1, "Quakes", "warning", T));
        assertThat(fired.id()).isNotNull();
        var row = notifications.findById(fired.id()).orElseThrow();
        assertThat(row.getSubtype()).isEqualTo("alert.fired");
        assertThat(row.getTitle()).isEqualTo("[warning] Quakes");
        assertThat(row.getBodyMarkdown()).contains("M5.3 near Austin").contains("usgs-earthquakes/us3");
        assertThat(row.getUrl()).isEqualTo("http://localhost:8080/v1/alerts/3");
        assertThat(row.getAlertId()).isEqualTo(3L);
        assertThat(row.getRuleId()).isEqualTo(R1);
        assertThat(row.getOrigin()).isNotBlank();
        assertThat(row.getData()).containsEntry("ruleName", "Quakes");

        events.publishEvent(new SourceHealthChangedEvent("usgs-earthquakes", HealthStatus.HEALTHY, HealthStatus.FAILING,
                "5 consecutive failures", T));
        assertThat(notifications.findAll()).anySatisfy(n -> {
            assertThat(n.getSubtype()).isEqualTo("system.source_failing");
            assertThat(n.getEvent()).isEqualTo("system");
        });
        assertThat(deliveries.findAll()).isEmpty();
    }

    @Test
    @DisplayName("suppressed and 'updated' alert events notify nobody")
    void suppressed() {
        slack("");
        router.onAlert(new AlertEvent(AlertEvent.FIRED, alert(4, R1, "Quakes", "warning", T), true));
        router.onAlert(new AlertEvent(AlertEvent.UPDATED, alert(4, R1, "Quakes", "warning", T), false));
        assertThat(notifications.findAll()).isEmpty();
    }

    @Test
    @DisplayName("a lifecycle update enqueues 'update' where an edit is possible, else 'followup'")
    void lifecycleUpdates() {
        String discordSecrets = "'secrets':{'webhookUrl':'https://discord.example.invalid/api/webhooks/1/T'}";
        UUID discord = fixture.channel("{'name':'d','type':'discord'," + discordSecrets + "}");
        UUID discordNoEdit = fixture.channel("{'name':'d2','type':'discord','editInPlace':false," + discordSecrets + "}");
        UUID discordNoRef = fixture.channel("{'name':'d3','type':'discord'," + discordSecrets + "}");
        UUID slackChannel = slack("");
        UUID addedLater;

        var a = alert(5, R1, "Quakes", "warning", T);
        NotificationMessage fired = router.fired(a);
        for (UUID c : List.of(discord, discordNoEdit, slackChannel)) {
            NotificationDeliveryEntity d = fixture.deliveries(c).getFirst();
            d.setStatus(DeliveryStatus.DELIVERED);
            d.setProviderMessageRef("msg-" + c.toString().substring(0, 4));
            deliveries.save(d);
        }
        addedLater = slack("");

        NotificationMessage update = router.lifecycle(AlertEvent.RESOLVED, NotifyFixture.resolved(a, T.plusSeconds(120), "admin"));

        assertThat(update.updateOf()).isEqualTo(fired.id());
        assertThat(update.subtype()).isEqualTo("alert.resolved");
        assertThat(update.title()).startsWith("✅ Resolved 12:02 UTC — [warning] Quakes");
        assertThat(update.bodyMarkdown()).startsWith("Resolved by admin.").contains("~~");
        assertThat(fixture.deliveries(discord).getLast().getKind()).isEqualTo(DeliveryKind.UPDATE);
        assertThat(fixture.deliveries(discordNoEdit).getLast().getKind()).isEqualTo(DeliveryKind.FOLLOWUP);
        assertThat(fixture.deliveries(discordNoRef).getLast().getKind()).isEqualTo(DeliveryKind.FOLLOWUP);
        assertThat(fixture.deliveries(slackChannel).getLast().getKind()).isEqualTo(DeliveryKind.FOLLOWUP);
        assertThat(fixture.deliveries(addedLater)).isEmpty();
    }

    @Test
    @DisplayName("an alert of an enrich rule waits wait_for_enrichment_seconds")
    void enrichmentWait() {
        UUID waiting = slack("'waitForEnrichmentSeconds':20");
        UUID immediate = slack("");
        service.publish(NotificationMessage.builder("alert", "alert.fired", "t", "warning").ruleId(R1).alertId(9L)
                .data(Map.of("enrich", true)).build(), null);
        assertThat(fixture.deliveries(waiting)).singleElement().satisfies(d -> {
            assertThat(d.getNextAttemptAt()).isEqualTo(T.plusSeconds(20));
            assertThat(d.getDeferredReason()).isEqualTo(DeferredReason.ENRICHMENT);
        });
        assertThat(fixture.deliveries(immediate).getFirst().getDeferredReason()).isNull();
        service.publish(NotificationMessage.builder("alert", "alert.fired", "t", "warning").ruleId(R1).alertId(10L)
                .data(Map.of("enrich", false)).build(), null);
        assertThat(fixture.deliveries(waiting).getLast().getNextAttemptAt()).isEqualTo(T);
    }

    @Test
    @DisplayName("a delivery failure is never sent to the failing channel, and a failure about a failure is not routed")
    void systemEventsSkipTheFailingChannel() {
        UUID failing = slack("'events':['alert','system']");
        UUID other = slack("'events':['system']");
        router.fired(alert(6, R1, "Quakes", "warning", T));
        NotificationDeliveryEntity failed = fixture.deliveries(failing).getFirst();

        router.deliveryFailed(channelRows.findById(failing).orElseThrow(), failed, "HTTP 500");

        assertThat(fixture.deliveries(failing)).hasSize(1);
        assertThat(fixture.deliveries(other)).singleElement().satisfies(d -> assertThat(d.getEvent()).isEqualTo("system"));
        NotificationDeliveryEntity cascade = fixture.deliveries(other).getFirst();
        router.deliveryFailed(channelRows.findById(other).orElseThrow(), cascade, "HTTP 500");
        assertThat(fixture.deliveries(failing)).hasSize(1);
        assertThat(notifications.findAll().stream().filter(n -> n.getSubtype().equals("system.delivery_failed"))).hasSize(2);
    }

    @Test
    @DisplayName("a rule storm and a source turning failing become system notifications; recovering does not")
    void systemEvents() {
        UUID system = slack("'events':['system']");
        events.publishEvent(new RuleStormEvent(R1, "Quakes", T, 120, 3, true));
        events.publishEvent(new SourceHealthChangedEvent("usgs-earthquakes", HealthStatus.FAILING, HealthStatus.HEALTHY, null, T));
        events.publishEvent(new SourceHealthChangedEvent("usgs-earthquakes", HealthStatus.DEGRADED, HealthStatus.FAILING, "x", T));
        assertThat(fixture.deliveries(system)).hasSize(2);
        assertThat(notifications.findAll()).extracting(n -> n.getSubtype())
                .containsExactlyInAnyOrder("system.rule_storm", "system.source_failing");
    }
}
