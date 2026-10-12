package ai.foresignal.notify.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.notify.ActionFixture;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertStore;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Action capabilities against the database (prompt 033): single use under concurrency, expiry,
 * the ack, snooze and mute effects, already-acknowledged results, superseding on an acknowledgement
 * through another path, and audit rows.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class ActionServiceIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final ActionService.Via VIA = new ActionService.Via("http", null, "203.0.113.9");

    @Autowired ActionService actions;
    @Autowired AlertStore alerts;
    @Autowired AlertService alertService;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired TriggerRuleRepository rules;
    @Autowired AuditService audit;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    ActionFixture fixture;
    UUID channel;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fixture = new ActionFixture(jdbc, rules);
        channel = new NotifyFixture(channels, deliveries, limiter).channel(
                "{'name':'Ops','type':'ntfy','config':{'serverUrl':'https://ntfy.example.invalid','topic':'t'}}");
    }

    List<NotificationAction> mint(long alertId, String... list) {
        return actions.mint(alerts.find(alertId).orElseThrow(), channel, 42L, null, List.of(list));
    }

    @Test
    @DisplayName("mint: one capability per action, labels, 12 h expiry, only the hash stored, none for a non-firing alert")
    void mints() {
        UUID rule = fixture.rule("Quakes", T);
        long alert = fixture.alert(rule, "warning", T);
        List<NotificationAction> minted = mint(alert, "ack", "snooze", "mute");
        assertThat(minted).extracting(NotificationAction::action).containsExactly("ack", "snooze", "mute");
        assertThat(minted).extracting(NotificationAction::label).containsExactly("Acknowledge", "Snooze 1h", "Mute 4h");
        assertThat(minted).allSatisfy(a -> {
            assertThat(a.url()).startsWith("http://localhost:8080/v1/actions/eac_").endsWith(a.token());
            assertThat(a.expiresAt()).isEqualTo(T.plus(Duration.ofHours(12)));
        });
        List<String> stored = jdbc.queryForList("select token_hash from action_capability", String.class);
        assertThat(stored).hasSize(3).noneMatch(h -> minted.stream().anyMatch(a -> h.contains(a.token())));
        alertService.acknowledge(alert, AlertService.Transition.NONE, "admin");
        assertThat(mint(alert, "ack")).isEmpty();
    }

    @Test
    @DisplayName("ack sets acknowledged_by action:<type>:<name>; the capability is single use (410 after)")
    void ackOnce() {
        long alert = fixture.alert(fixture.rule("Quakes", T), "warning", T);
        NotificationAction ack = mint(alert, "ack").getFirst();
        ActionService.Result result = actions.execute(ack.token(), VIA);
        assertThat(result.result()).isEqualTo("acknowledged");
        assertThat(result.alertId()).isEqualTo(alert);
        assertThat(fixture.state(alert)).isEqualTo("acknowledged");
        assertThat(fixture.acknowledgedBy(alert)).isEqualTo("action:ntfy:Ops");
        assertThatThrownBy(() -> actions.execute(ack.token(), VIA)).isInstanceOf(ActionExpiredException.class);
        assertThat(jdbc.queryForObject("select result from action_capability where action = 'ack'", String.class))
                .isEqualTo("acknowledged");
    }

    @Test
    @DisplayName("two concurrent executions of one capability: exactly one wins, the other gets ACTION_EXPIRED")
    void concurrentSingleUse() throws Exception {
        long alert = fixture.alert(fixture.rule("Quakes", T), "warning", T);
        String token = mint(alert, "snooze").getFirst().token();
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> outcomes = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 2; i++) {
                outcomes.add(pool.submit(() -> {
                    start.await(5, TimeUnit.SECONDS);
                    try {
                        return actions.execute(token, VIA).result();
                    } catch (ActionExpiredException e) {
                        return "expired";
                    }
                }));
            }
            start.countDown();
            List<String> results = new ArrayList<>();
            for (Future<String> f : outcomes) {
                results.add(f.get(20, TimeUnit.SECONDS));
            }
            assertThat(results).containsExactlyInAnyOrder("snoozed", "expired");
        }
    }

    @Test
    @DisplayName("an expired capability is 410 and changes nothing; forged and unknown tokens too")
    void expiry() {
        long alert = fixture.alert(fixture.rule("Quakes", T), "warning", T);
        String token = mint(alert, "ack").getFirst().token();
        clock.advance(Duration.ofHours(12).plusSeconds(1));
        assertThatThrownBy(() -> actions.execute(token, VIA)).isInstanceOf(ActionExpiredException.class);
        assertThatThrownBy(() -> actions.peek(token)).isInstanceOf(ActionExpiredException.class);
        assertThat(fixture.state(alert)).isEqualTo("firing");
        assertThatThrownBy(() -> actions.execute("eac_" + "A".repeat(22) + "." + "A".repeat(11), VIA))
                .isInstanceOf(ActionExpiredException.class);
        assertThatThrownBy(() -> actions.execute(ActionToken.mint(new byte[32]), VIA))
                .isInstanceOf(ActionExpiredException.class);
    }

    @Test
    @DisplayName("snooze sets the alert snooze (1 h); mute sets the rule's muted_until (4 h)")
    void snoozeAndMute() {
        UUID rule = fixture.rule("Quakes", T);
        long alert = fixture.alert(rule, "warning", T);
        List<NotificationAction> minted = mint(alert, "snooze", "mute");
        assertThat(actions.execute(minted.get(0).token(), VIA).result()).isEqualTo("snoozed");
        assertThat(alerts.find(alert).orElseThrow().snoozedUntil()).isEqualTo(T.plus(Duration.ofHours(1)));
        assertThat(actions.execute(minted.get(1).token(), VIA).result()).isEqualTo("muted");
        assertThat(rules.findById(rule).orElseThrow().getMutedUntil()).isEqualTo(T.plus(Duration.ofHours(4)));
        assertThat(fixture.state(alert)).isEqualTo("firing");
    }

    @Test
    @DisplayName("acting on an acknowledged or resolved alert returns already-<state> with 200 semantics")
    void alreadyActed() {
        long acked = fixture.alert(fixture.rule("A", T), "warning", T);
        long resolved = fixture.alert(fixture.rule("B", T), "warning", T);
        String snoozeAcked = mint(acked, "snooze").getFirst().token();
        String snoozeResolved = mint(resolved, "snooze").getFirst().token();
        jdbc.update("update alert set state = 'acknowledged' where id = ?", acked);
        jdbc.update("update alert set state = 'resolved' where id = ?", resolved);
        assertThat(actions.execute(snoozeAcked, VIA).result()).isEqualTo("already-acknowledged");
        assertThat(actions.execute(snoozeResolved, VIA).result()).isEqualTo("already-resolved");
    }

    @Test
    @DisplayName("an acknowledgement through the API supersedes the alert's open capabilities")
    void supersede() {
        long alert = fixture.alert(fixture.rule("Quakes", T), "warning", T);
        List<NotificationAction> minted = mint(alert, "ack", "snooze");
        alertService.acknowledge(alert, AlertService.Transition.NONE, "token:phone");
        await().atMost(Duration.ofSeconds(10)).until(() -> jdbc.queryForObject(
                "select count(*) from action_capability where result = 'superseded'", Long.class) == 2);
        assertThatThrownBy(() -> actions.execute(minted.getFirst().token(), VIA)).isInstanceOf(ActionExpiredException.class);
        assertThat(actions.supersede(alert)).isZero();
    }

    @Test
    @DisplayName("every execution is audited with actor kind action and the token redacted, refusals included")
    void audited() {
        long alert = fixture.alert(fixture.rule("Quakes", T), "warning", T);
        String token = mint(alert, "ack").getFirst().token();
        actions.execute(token, VIA);
        assertThatThrownBy(() -> actions.execute(token, VIA)).isInstanceOf(ActionExpiredException.class);
        audit.flush();
        List<java.util.Map<String, Object>> rows = jdbc.queryForList(
                "select actor_kind, path, status, target_id, detail::text as detail, remote_ip from audit_event"
                        + " where actor_kind = 'action' order by id");
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("path")).isEqualTo("/v1/actions/eac_…");
        assertThat(rows.get(0).get("status")).isEqualTo(200);
        assertThat(rows.get(0).get("target_id")).isEqualTo(Long.toString(alert));
        assertThat(rows.get(0).get("detail").toString()).contains("acknowledged").doesNotContain(token);
        assertThat(rows.get(1).get("status")).isEqualTo(410);
        assertThat(rows.toString()).doesNotContain(token);
    }

    @Test
    @DisplayName("peek shows the alert and the action without changing anything")
    void peek() {
        long alert = fixture.alert(fixture.rule("Quakes", T), "critical", T);
        NotificationAction mute = mint(alert, "mute").getFirst();
        ActionService.Confirm confirm = actions.peek(mute.token());
        assertThat(confirm.action()).isEqualTo("mute");
        assertThat(confirm.duration()).isEqualTo(Duration.ofHours(4));
        assertThat(confirm.alert().id()).isEqualTo(alert);
        assertThat(jdbc.queryForObject("select count(*) from action_capability where used_at is not null", Long.class))
                .isZero();
    }
}
