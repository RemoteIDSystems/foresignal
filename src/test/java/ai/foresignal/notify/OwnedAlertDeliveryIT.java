package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertView;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Where an alert is delivered by its rule's owner (prompt R2-008): a user's alert to that user's channels only, a
 * shared rule's alert to the instance channels only. Deliveries are only enqueued; nothing is sent.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class OwnedAlertDeliveryIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-0000000000d8");
    static final String ALICE = "01ALICEPRINCIPAL0000000000";
    static final String BOB = "01BOBPRINCIPAL000000000000";
    static final String WEBHOOK = "{'name':'%s','type':'webhook','config':{'url':'http://127.0.0.1:9/%s'}}";

    @Autowired NotificationRouter router;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;

    NotifyFixture fixture;
    UUID instance;
    UUID alicesAlerts;
    UUID alicesPersonal;
    UUID bobsAlerts;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        fixture = new NotifyFixture(channels, deliveries, limiter);
        instance = fixture.channel(WEBHOOK.formatted("instance", "instance"));
        alicesAlerts = owned(fixture.channel(WEBHOOK.formatted("alice", "alice")), ALICE);
        bobsAlerts = owned(fixture.channel(WEBHOOK.formatted("bob", "bob")), BOB);
        alicesPersonal = channels.savePersonal(null,
                NotifyFixture.JSON.readTree(WEBHOOK.formatted("alice-personal", "personal").replace('\'', '"')), ALICE);
    }

    private UUID owned(UUID channel, String owner) {
        jdbc.update("update notification_channel set owner_principal_id = ? where id = ?", owner, channel);
        return channel;
    }

    private static AlertView ownedBy(AlertView a, String owner) {
        return new AlertView(a.id(), a.ruleId(), a.ruleName(), a.seq(), a.sourceId(), a.externalId(), a.kind(),
                a.firedAt(), a.observedAt(), a.message(), a.severity(), a.state(), a.rev(), a.acknowledgedAt(),
                a.acknowledgedBy(), a.resolvedAt(), a.resolvedBy(), a.resolvedSeq(), a.feedback(), a.feedbackAt(),
                a.note(), a.snoozedUntil(), a.suppressed(), a.suppressedReason(), a.tags(), a.recordSnapshot(),
                a.summary(), a.enrichedAt(), a.summaryCitations(), a.sensitive(), a.context(), a.region(), a.exercise(),
                a.exerciseRunId(), owner);
    }

    @Test
    @DisplayName("a user's alert goes to that user's channels, their personal channel included, and nowhere else")
    void ownedAlert() {
        NotificationMessage message = router.fired(ownedBy(NotifyFixture.alert(1, RULE, "Mine", "warning", T), ALICE));
        assertThat(message.data()).containsEntry("ownerPrincipalId", ALICE);
        assertThat(fixture.deliveries(alicesAlerts)).hasSize(1);
        assertThat(fixture.deliveries(alicesPersonal)).hasSize(1);
        assertThat(fixture.deliveries(bobsAlerts)).isEmpty();
        assertThat(fixture.deliveries(instance)).isEmpty();
    }

    @Test
    @DisplayName("a shared rule's alert goes to the instance channels only, as before R2-008")
    void sharedAlert() {
        router.fired(NotifyFixture.alert(2, RULE, "Shared", "warning", T));
        assertThat(fixture.deliveries(instance)).hasSize(1);
        assertThat(fixture.deliveries(alicesAlerts)).isEmpty();
        assertThat(fixture.deliveries(alicesPersonal)).isEmpty();
        assertThat(fixture.deliveries(bobsAlerts)).isEmpty();
    }

    @Test
    @DisplayName("the owner's channel filters still apply: below its minimum severity it receives nothing")
    void ownerChannelFilters() {
        jdbc.update("update notification_channel set min_severity = 'critical' where id = ?", alicesAlerts);
        router.fired(ownedBy(NotifyFixture.alert(3, RULE, "Mine", "warning", T), ALICE));
        assertThat(fixture.deliveries(alicesAlerts)).isEmpty();
        assertThat(fixture.deliveries(alicesPersonal)).hasSize(1);
    }
}
