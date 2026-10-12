package ai.foresignal.notify.escalation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.NotificationRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.notify.ActionFixture;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
 * Escalation policies and the escalation worker (prompt 033): steps fire at their times, stop on
 * acknowledgement, resolve, snooze and mute, repeat and exhaustion, two workers never send a step
 * twice, quiet hours hold back non-critical escalations only, and policy CRUD rules.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class EscalationWorkerIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    @Autowired EscalationWorker worker;
    @Autowired ai.foresignal.notify.NotificationRouter router;
    @Autowired EscalationPolicyService policies;
    @Autowired AlertStore alerts;
    @Autowired AlertService alertService;
    @Autowired TriggerRuleService ruleService;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired NotificationRepository notifications;
    @Autowired TriggerRuleRepository rules;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    ActionFixture fixture;
    NotifyFixture notify;
    UUID c1;
    UUID c2;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fixture = new ActionFixture(jdbc, rules);
        notify = new NotifyFixture(channels, deliveries, limiter);
        c1 = notify.channel("{'name':'pager','type':'webhook','config':{'url':'https://hook.example.invalid/1'}}");
        c2 = notify.channel("{'name':'boss','type':'webhook','config':{'url':'https://hook.example.invalid/2'}}");
    }

    UUID policy(String repeat, String stopOn) {
        Map<String, Object> created = policies.create(NotifyFixture.JSON.readTree(("{'name':'Wake','steps':["
                + "{'afterMinutes':5,'channelIds':['" + c1 + "']},{'afterMinutes':15,'channelIds':['" + c2 + "']}]"
                + (repeat == null ? "" : ",'repeat':" + repeat) + (stopOn == null ? "" : ",'stopOn':" + stopOn) + "}")
                .replace('\'', '"')));
        return UUID.fromString((String) created.get("id"));
    }

    long alert(UUID policy, String severity) {
        long id = fixture.alert(fixture.rule("R" + System.nanoTime(), T, policy), severity, T);
        assertThat(worker.begin(alerts.find(id).orElseThrow())).isTrue();
        return id;
    }

    List<NotificationDeliveryEntity> escalations(UUID channel) {
        return notify.deliveries(channel).stream().filter(d -> d.getEscalationStep() != null).toList();
    }

    String state(long alert) {
        return jdbc.queryForObject("select state from alert_escalation where alert_id = ?", String.class, alert);
    }

    @Test
    @DisplayName("steps fire at afterMinutes from fired_at, titled [Escalation n], then repeat and exhaust")
    void stepsRepeatAndExhaust() {
        long alert = alert(policy("{'everyMinutes':10,'maxTimes':2}", null), "warning");
        clock.setInstant(T.plus(Duration.ofMinutes(4)));
        assertThat(worker.runOnce()).isZero();
        clock.setInstant(T.plus(Duration.ofMinutes(5)));
        assertThat(worker.runOnce()).isEqualTo(1);
        assertThat(escalations(c1)).hasSize(1).allSatisfy(d -> assertThat(d.getEscalationStep()).isEqualTo(1));
        long notification = escalations(c1).getFirst().getNotificationId();
        assertThat(notifications.findById(notification).orElseThrow().getTitle()).startsWith("[Escalation 1] ");
        assertThat(notifications.findById(notification).orElseThrow().getSubtype()).isEqualTo("alert.escalated");
        assertThat(escalations(c2)).isEmpty();

        clock.setInstant(T.plus(Duration.ofMinutes(15)));
        assertThat(worker.runOnce()).isEqualTo(1);
        assertThat(escalations(c2)).hasSize(1).allSatisfy(d -> assertThat(d.getEscalationStep()).isEqualTo(2));
        clock.advance(Duration.ofMinutes(10));
        assertThat(worker.runOnce()).isEqualTo(1);
        clock.advance(Duration.ofMinutes(10));
        assertThat(worker.runOnce()).isEqualTo(1);
        assertThat(escalations(c2)).hasSize(3);
        assertThat(state(alert)).isEqualTo("exhausted");
        clock.advance(Duration.ofHours(1));
        assertThat(worker.runOnce()).isZero();
        assertThat(worker.view(alert).orElseThrow().get("repeatsDone")).isEqualTo(2);
    }

    @Test
    @DisplayName("an acknowledgement stops at once and suppresses pending escalation deliveries")
    void stopOnAck() {
        long alert = alert(policy(null, null), "warning");
        clock.setInstant(T.plus(Duration.ofMinutes(5)));
        worker.runOnce();
        assertThat(escalations(c1).getFirst().getStatus()).isEqualTo(DeliveryStatus.PENDING);
        alertService.acknowledge(alert, AlertService.Transition.NONE, "admin");
        await().atMost(Duration.ofSeconds(10)).until(() -> "stopped".equals(state(alert)));
        assertThat(worker.view(alert).orElseThrow().get("stoppedReason")).isEqualTo("acknowledged");
        assertThat(escalations(c1).getFirst().getStatus()).isEqualTo(DeliveryStatus.SUPPRESSED);
        clock.setInstant(T.plus(Duration.ofMinutes(15)));
        assertThat(worker.runOnce()).isZero();
        assertThat(escalations(c2)).isEmpty();
    }

    @Test
    @DisplayName("resolve and snooze stop per stopOn; with stopOn.resolve false a resolved alert keeps escalating")
    void stopOnResolveAndSnooze() {
        long resolved = alert(policy(null, null), "warning");
        assertThat(worker.stop(resolved, "resolved", "resolve")).isTrue();
        long snoozed = alert(policy(null, null), "warning");
        jdbc.update("update alert set snoozed_until = ? where id = ?", java.sql.Timestamp.from(T.plus(Duration.ofHours(1))),
                snoozed);
        long keeps = alert(policy(null, "{'resolve':false,'snooze':true}"), "warning");
        jdbc.update("update alert set state = 'resolved' where id = ?", keeps);
        assertThat(worker.stop(keeps, "resolved", "resolve")).isFalse();
        clock.setInstant(T.plus(Duration.ofMinutes(5)));
        assertThat(worker.runOnce()).isEqualTo(1);
        assertThat(state(resolved)).isEqualTo("stopped");
        assertThat(state(snoozed)).isEqualTo("stopped");
        assertThat(worker.view(snoozed).orElseThrow().get("stoppedReason")).isEqualTo("snoozed");
        assertThat(state(keeps)).isEqualTo("active");
    }

    @Test
    @DisplayName("muting the rule stops escalation of its open alerts; a muted rule's new alerts do not escalate")
    void stopOnMute() {
        UUID policy = policy(null, null);
        long alert = alert(policy, "warning");
        UUID rule = alerts.find(alert).orElseThrow().ruleId();
        ruleService.mute(rule, T.plus(Duration.ofHours(4)));
        assertThat(state(alert)).isEqualTo("stopped");
        assertThat(worker.view(alert).orElseThrow().get("stoppedReason")).isEqualTo("muted");
        long later = fixture.alert(rule, "warning", T);
        assertThat(worker.begin(alerts.find(later).orElseThrow())).isFalse();
        // A muted rule's alerts are recorded and kept in the inbox, but routed to no channel.
        long notification = router.fired(alerts.find(later).orElseThrow()).id();
        assertThat(notifications.findById(notification)).isPresent();
        assertThat(deliveries.findByNotificationIdOrderByIdAsc(notification)).isEmpty();
    }

    @Test
    @DisplayName("two workers claiming at once never send a step twice")
    void noDoubleSend() throws Exception {
        alert(policy(null, null), "warning");
        clock.setInstant(T.plus(Duration.ofMinutes(5)));
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> runs = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 2; i++) {
                runs.add(pool.submit(() -> {
                    start.await(5, TimeUnit.SECONDS);
                    return worker.runOnce();
                }));
            }
            start.countDown();
            int total = 0;
            for (Future<Integer> run : runs) {
                total += run.get(20, TimeUnit.SECONDS);
            }
            assertThat(total).isEqualTo(1);
        }
        assertThat(escalations(c1)).hasSize(1);
    }

    @Test
    @DisplayName("quiet hours defer non-critical escalation deliveries; critical ones go at once")
    void quietHours() {
        UUID quiet = notify.channel("{'name':'quiet','type':'webhook','config':{'url':'https://hook.example.invalid/q'},"
                + "'timezone':'UTC','quietHours':{'start':'00:00','end':'23:59','bypassSeverity':null}}");
        Map<String, Object> created = policies.create(NotifyFixture.JSON.readTree(("{'name':'Q','steps':[{'afterMinutes':1,"
                + "'channelIds':['" + quiet + "']}]}").replace('\'', '"')));
        UUID policy = UUID.fromString((String) created.get("id"));
        alert(policy, "warning");
        alert(policy, "critical");
        clock.setInstant(T.plus(Duration.ofMinutes(1)));
        assertThat(worker.runOnce()).isEqualTo(2);
        List<NotificationDeliveryEntity> rows = escalations(quiet);
        assertThat(rows).hasSize(2);
        assertThat(rows).filteredOn(d -> DeferredReason.QUIET_HOURS.equals(d.getDeferredReason())).hasSize(1);
        assertThat(rows).filteredOn(d -> d.getDeferredReason() == null && d.getNextAttemptAt().equals(clock.instant()))
                .hasSize(1);
    }

    @Test
    @DisplayName("policy CRUD: validation, unknown channels, ruleCount, rule references and 409 POLICY_IN_USE")
    void crud() {
        for (String bad : List.of("{'name':'x','steps':[]}", "{'name':'x','steps':[{'afterMinutes':0,'includePush':true}]}",
                "{'name':'x','steps':[{'afterMinutes':5,'includePush':true},{'afterMinutes':5,'includePush':true}]}",
                "{'name':'x','steps':[{'afterMinutes':5}]}",
                "{'name':'x','steps':[{'afterMinutes':5,'includePush':true}],'repeat':{'everyMinutes':5,'maxTimes':11}}",
                "{'name':'x','steps':[{'afterMinutes':5,'channelIds':['" + UUID.randomUUID() + "']}]}",
                "{'steps':[{'afterMinutes':5,'includePush':true}]}")) {
            assertThatThrownBy(() -> policies.create(NotifyFixture.JSON.readTree(bad.replace('\'', '"'))))
                    .as(bad).isInstanceOf(ValidationException.class);
        }
        UUID policy = policy("{'everyMinutes':10,'maxTimes':3}", "{'resolve':false}");
        Map<String, Object> view = policies.get(policy);
        assertThat(view.get("ruleCount")).isEqualTo(0L);
        assertThat(view.get("stopOn")).isEqualTo(Map.of("resolve", false, "snooze", true));
        UUID rule = ruleService.create(NotifyFixture.JSON.readTree(("{'name':'w','sources':['usgs-earthquakes'],"
                + "'escalationPolicyId':'" + policy + "'}").replace('\'', '"'))).rule().getId();
        assertThat(rules.findById(rule).orElseThrow().getEscalationPolicyId()).isEqualTo(policy);
        assertThat(policies.get(policy).get("ruleCount")).isEqualTo(1L);
        assertThatThrownBy(() -> policies.delete(policy)).isInstanceOf(ConflictException.class)
                .hasMessageContaining("1 trigger rule");
        assertThatThrownBy(() -> ruleService.create(NotifyFixture.JSON.readTree(("{'name':'w2','sources':['usgs-earthquakes'],"
                + "'escalationPolicyId':'" + UUID.randomUUID() + "'}").replace('\'', '"'))))
                .isInstanceOf(ValidationException.class);
        policies.update(policy, NotifyFixture.JSON.readTree(("{'name':'Renamed','steps':[{'afterMinutes':3,'channelIds':['"
                + c1 + "']}]}").replace('\'', '"')));
        assertThat(policies.get(policy).get("name")).isEqualTo("Renamed");
        rules.deleteById(rule);
        policies.delete(policy);
        assertThat(policies.list()).isEmpty();
    }
}
