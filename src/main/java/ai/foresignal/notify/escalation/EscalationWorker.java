package ai.foresignal.notify.escalation;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.DeliveryStore;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.notify.NotificationStore;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.push.PushChannelType;
import ai.foresignal.notify.types.PushoverChannel;
import ai.foresignal.store.EscalationPolicyEntity;
import ai.foresignal.store.EscalationPolicyRepository;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerRuleMutedEvent;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Escalates unacknowledged alerts along their rule's escalation policy (prompt 033).
 *
 * <ul>
 *   <li><b>Start:</b> when an alert of a rule with {@code escalation_policy_id} fires (not suppressed,
 *       rule not muted), an {@code alert_escalation} row is created, due at {@code fired_at} plus the
 *       first step's {@code afterMinutes} (the rule's own routing is the alert's first notification).
 *   <li><b>Steps:</b> every {@code foresignal.escalation.poll-interval} (15 s) due rows are claimed with
 *       {@code FOR UPDATE SKIP LOCKED} and advanced in the same transaction, so two workers never
 *       send a step twice. Unless the alert is acknowledged (always), resolved or snoozed (per
 *       {@code stopOn}), or its rule muted, the step's channels (plus the built-in Push channel with
 *       {@code includePush}) get an {@code alert.escalated} notification titled
 *       {@code [Escalation <n>] …} with fresh action capabilities, after the commit. After the last
 *       step the optional {@code repeat} sends the last step again every {@code everyMinutes} up to
 *       {@code maxTimes}; then the row is {@code exhausted}. Quiet hours apply except to critical
 *       alerts ({@link NotificationService#publishEscalation}).
 *   <li><b>Stop:</b> on the alert bus's ack, resolve and snooze (per {@code stopOn}) and on a rule
 *       mute ({@link TriggerRuleMutedEvent}) the row is {@code stopped} at once, its pending
 *       escalation deliveries are {@code suppressed}, and Pushover emergency receipts of escalation
 *       messages already sent are canceled (018's receipt cancel).
 * </ul>
 * Listeners run after the alert's transaction committed and write in their own statements (027).
 */
@Component
public class EscalationWorker implements AlertListener {

    private static final Logger log = LoggerFactory.getLogger(EscalationWorker.class);

    /** The {@code alert.escalated} subtype. */
    public static final String SUBTYPE = "alert.escalated";

    /**
     * One step to send after the claim commits.
     *
     * @param alert the alert
     * @param number the 1-based escalation number shown in the title
     * @param channels the channels
     */
    record Work(AlertView alert, int number, List<UUID> channels) {}

    private final AlertBus bus;
    private final AlertStore alerts;
    private final TriggerRuleRepository rules;
    private final EscalationPolicyRepository policies;
    private final NotificationService notifications;
    private final NotificationStore store;
    private final DeliveryStore deliveries;
    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final ChannelSender sender;
    private final PushoverChannel pushover;
    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final EscalationProperties properties;
    private final Clock clock;
    private volatile Subscription subscription;

    /** Creates the worker. */
    public EscalationWorker(AlertBus bus, AlertStore alerts, TriggerRuleRepository rules,
            EscalationPolicyRepository policies, NotificationService notifications, NotificationStore store,
            DeliveryStore deliveries, NotificationChannelRepository channels, ChannelService channelService,
            ChannelSender sender, PushoverChannel pushover, NamedParameterJdbcTemplate jdbc,
            PlatformTransactionManager transactions, EscalationProperties properties, Clock clock) {
        this.bus = bus;
        this.alerts = alerts;
        this.rules = rules;
        this.policies = policies;
        this.notifications = notifications;
        this.store = store;
        this.deliveries = deliveries;
        this.channels = channels;
        this.channelService = channelService;
        this.sender = sender;
        this.pushover = pushover;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
        this.properties = properties;
        this.clock = clock;
    }

    /** Subscribes to the alert bus with the notification router. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.NOTIFY_ROUTER)
    public synchronized void start() {
        if (subscription == null) {
            subscription = bus.subscribe("escalation", this, AlertBus.DEFAULT_QUEUE);
        }
    }

    /** Unsubscribes. */
    @PreDestroy
    public synchronized void stop() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    // ------------------------------------------------------------------------- bus

    @Override
    public void onAlert(AlertEvent event) {
        try {
            switch (event.type()) {
                case AlertEvent.FIRED -> {
                    // Exercise alerts (prompt 115) never escalate.
                    if (!event.suppressed() && !event.alert().exercise()) {
                        begin(event.alert());
                    }
                }
                case AlertEvent.ACKNOWLEDGED -> stop(event.alert().id(), "acknowledged", null);
                case AlertEvent.RESOLVED -> stop(event.alert().id(), "resolved", "resolve");
                case AlertEvent.SNOOZED -> stop(event.alert().id(), "snoozed", "snooze");
                default -> { }
            }
        } catch (RuntimeException e) {
            log.warn("Escalation handling of alert {} failed: {}", event.alert().id(), e.toString());
        }
    }

    @Override
    public void onGap() {
        log.warn("The escalation worker missed alert events; the worker re-checks each alert before every step");
    }

    /** Stops the escalations of a muted rule's open alerts. */
    @EventListener
    public void onRuleMuted(TriggerRuleMutedEvent event) {
        List<Long> ids = jdbc.queryForList("""
                select e.alert_id from alert_escalation e join alert a on a.id = e.alert_id
                 where e.state = 'active' and a.rule_id = :rule
                """, new MapSqlParameterSource("rule", event.ruleId()), Long.class);
        for (Long id : ids) {
            stop(id, "muted", null);
        }
    }

    /**
     * Creates the escalation of a new alert when its rule has a policy.
     *
     * @return true when one was created
     */
    public boolean begin(AlertView alert) {
        if (alert.ruleId() == null) {
            return false;
        }
        Optional<TriggerRuleEntity> rule = rules.findById(alert.ruleId());
        Instant now = clock.instant();
        if (rule.isEmpty() || rule.get().getEscalationPolicyId() == null || rule.get().mutedAt(now)) {
            return false;
        }
        Optional<EscalationPolicyEntity> policy = policies.findById(rule.get().getEscalationPolicyId());
        if (policy.isEmpty() || policy.get().getSteps().isEmpty()) {
            return false;
        }
        Instant fired = alert.firedAt() == null ? now : alert.firedAt();
        int first = EscalationPolicyInput.Step.fromMap(policy.get().getSteps().getFirst()).afterMinutes();
        int inserted = jdbc.update("""
                insert into alert_escalation (alert_id, policy_id, next_step, next_at, repeats_done, state, updated_at)
                values (:alert, :policy, 0, :next, 0, 'active', :now) on conflict (alert_id) do nothing
                """, new MapSqlParameterSource("alert", alert.id()).addValue("policy", policy.get().getId())
                .addValue("next", Timestamp.from(fired.plus(Duration.ofMinutes(first))))
                .addValue("now", Timestamp.from(now)));
        return inserted > 0;
    }

    /**
     * Stops the active escalation of {@code alertId} for {@code reason}: the row is {@code stopped},
     * pending escalation deliveries are suppressed and Pushover receipts canceled.
     *
     * @param stopOnKey the {@code stopOn} member that must be true ({@code resolve}, {@code snooze}),
     *     or null when the reason always stops (acknowledgement, mute)
     * @return true when an active escalation was stopped
     */
    public boolean stop(long alertId, String reason, String stopOnKey) {
        if (stopOnKey != null) {
            List<UUID> policyIds = jdbc.queryForList(
                    "select policy_id from alert_escalation where alert_id = :alert and state = 'active'",
                    new MapSqlParameterSource("alert", alertId), UUID.class);
            if (policyIds.isEmpty()) {
                return false;
            }
            Optional<EscalationPolicyEntity> policy = policies.findById(policyIds.getFirst());
            if (policy.isPresent() && Boolean.FALSE.equals(policy.get().getStopOn().get(stopOnKey))) {
                return false;
            }
        }
        int stopped = jdbc.update("""
                update alert_escalation set state = 'stopped', stopped_reason = :reason, next_at = null, updated_at = :now
                 where alert_id = :alert and state = 'active'
                """, new MapSqlParameterSource("alert", alertId).addValue("reason", reason)
                .addValue("now", Timestamp.from(clock.instant())));
        if (stopped == 0) {
            return false;
        }
        deliveries.suppressPendingEscalations(alertId, "escalation stopped: " + reason);
        cancelPushoverReceipts(alertId);
        return true;
    }

    // ------------------------------------------------------------------------- worker

    /** The scheduled poll; does nothing when {@code foresignal.escalation.scheduled} is false. */
    @Scheduled(fixedDelayString = "${foresignal.escalation.poll-interval:15s}",
            initialDelayString = "${foresignal.escalation.poll-interval:15s}")
    public void poll() {
        if (!properties.scheduled()) {
            return;
        }
        try {
            runOnce();
        } catch (RuntimeException e) {
            log.error("Escalation poll failed", e);
        }
    }

    /**
     * Claims and advances due escalations, then sends their steps.
     *
     * @return how many steps were sent
     */
    public int runOnce() {
        Instant now = clock.instant();
        List<Work> work = tx.execute(status -> claim(now));
        int sent = 0;
        for (Work w : work == null ? List.<Work>of() : work) {
            try {
                send(w);
                sent++;
            } catch (RuntimeException e) {
                log.error("Escalation {} of alert {} could not be sent", w.number(), w.alert().id(), e);
            }
        }
        return sent;
    }

    private List<Work> claim(Instant now) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                select alert_id, policy_id, next_step, repeats_done from alert_escalation
                 where state = 'active' and next_at <= :now order by next_at limit :limit for update skip locked
                """, new MapSqlParameterSource("now", Timestamp.from(now)).addValue("limit", properties.batchSize()));
        List<Work> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            long alertId = ((Number) row.get("alert_id")).longValue();
            UUID policyId = (UUID) row.get("policy_id");
            int nextStep = ((Number) row.get("next_step")).intValue();
            int repeatsDone = ((Number) row.get("repeats_done")).intValue();
            Optional<AlertView> alert = alerts.find(alertId);
            Optional<EscalationPolicyEntity> policy = policies.findById(policyId);
            String stop = stopReason(alert, policy, now);
            if (stop != null) {
                update(alertId, "stopped", stop, nextStep, null, repeatsDone, now);
                continue;
            }
            List<EscalationPolicyInput.Step> steps = policy.get().getSteps().stream()
                    .map(EscalationPolicyInput.Step::fromMap).toList();
            EscalationPolicyInput.Repeat repeat = EscalationPolicyInput.Repeat.fromMap(policy.get().getRepeat());
            Instant fired = alert.get().firedAt() == null ? now : alert.get().firedAt();
            EscalationPolicyInput.Step step;
            int number;
            Instant nextAt;
            int newStep = nextStep;
            int newRepeats = repeatsDone;
            if (nextStep < steps.size()) {
                step = steps.get(nextStep);
                number = nextStep + 1;
                newStep = nextStep + 1;
                if (newStep < steps.size()) {
                    nextAt = fired.plus(Duration.ofMinutes(steps.get(newStep).afterMinutes()));
                } else {
                    nextAt = repeat == null ? null : now.plus(Duration.ofMinutes(repeat.everyMinutes()));
                }
            } else if (repeat != null && repeatsDone < repeat.maxTimes() && !steps.isEmpty()) {
                step = steps.getLast();
                number = steps.size();
                newRepeats = repeatsDone + 1;
                nextAt = newRepeats < repeat.maxTimes() ? now.plus(Duration.ofMinutes(repeat.everyMinutes())) : null;
            } else {
                update(alertId, "exhausted", null, nextStep, null, repeatsDone, now);
                continue;
            }
            update(alertId, nextAt == null ? "exhausted" : "active", null, newStep, nextAt, newRepeats, now);
            Set<UUID> targets = new LinkedHashSet<>(step.channelIds());
            if (step.includePush()) {
                targets.add(PushChannelType.BUILT_IN_ID);
            }
            out.add(new Work(alert.get(), number, List.copyOf(targets)));
        }
        return out;
    }

    /** Why an escalation must stop before its next step, or null to continue. */
    String stopReason(Optional<AlertView> alert, Optional<EscalationPolicyEntity> policy, Instant now) {
        if (alert.isEmpty()) {
            return "alert-deleted";
        }
        if (policy.isEmpty()) {
            return "policy-deleted";
        }
        AlertView a = alert.get();
        Map<String, Object> stopOn = policy.get().getStopOn();
        if ("acknowledged".equals(a.state())) {
            return "acknowledged";
        }
        if ("resolved".equals(a.state()) && !Boolean.FALSE.equals(stopOn.get("resolve"))) {
            return "resolved";
        }
        if (a.snoozedUntil() != null && a.snoozedUntil().isAfter(now) && !Boolean.FALSE.equals(stopOn.get("snooze"))) {
            return "snoozed";
        }
        if (a.ruleId() != null && rules.findById(a.ruleId()).map(r -> r.mutedAt(now)).orElse(false)) {
            return "muted";
        }
        return null;
    }

    private void update(long alertId, String state, String reason, int nextStep, Instant nextAt, int repeats,
            Instant now) {
        jdbc.update("""
                update alert_escalation set state = :state, stopped_reason = :reason, next_step = :step,
                       next_at = :next, repeats_done = :repeats, updated_at = :now
                 where alert_id = :alert
                """, new MapSqlParameterSource("state", state).addValue("reason", reason).addValue("step", nextStep)
                .addValue("next", nextAt == null ? null : Timestamp.from(nextAt)).addValue("repeats", repeats)
                .addValue("now", Timestamp.from(now)).addValue("alert", alertId));
    }

    /** Publishes one escalation step. */
    void send(Work w) {
        AlertView a = w.alert();
        Optional<NotificationMessage> fired = store.fired(a.id());
        String baseTitle = fired.map(NotificationMessage::title).orElse(a.message());
        NotificationMessage message = NotificationMessage.builder(NotificationMessage.ALERT, SUBTYPE,
                        "[Escalation " + w.number() + "] " + baseTitle, a.severity())
                .shortTitle("Escalation " + w.number() + ": " + (a.ruleName() == null ? "alert" : a.ruleName()))
                .body(fired.map(NotificationMessage::bodyMarkdown).orElse(a.message()))
                .url(fired.map(NotificationMessage::url).orElse(null))
                .links(fired.map(NotificationMessage::links).orElse(List.of()))
                .data(fired.map(NotificationMessage::data).orElse(null))
                .tags(a.tags())
                .alertId(a.id())
                .ruleId(a.ruleId())
                .build();
        notifications.publishEscalation(message, w.channels(), w.number());
    }

    /** Cancels the Pushover emergency retries of escalation messages already sent for {@code alertId}. */
    private void cancelPushoverReceipts(long alertId) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                select d.provider_message_ref as ref, d.channel_id from notification_delivery d
                  join notification_channel c on c.id = d.channel_id
                 where d.alert_id = :alert and d.escalation_step is not null and d.status = 'DELIVERED'
                   and d.provider_message_ref is not null and c.type = 'pushover'
                """, new MapSqlParameterSource("alert", alertId));
        NotificationMessage cancel = NotificationMessage.builder(NotificationMessage.ALERT, "alert.acknowledged",
                "Escalation stopped", "info").alertId(alertId).build();
        for (Map<String, Object> row : rows) {
            Optional<NotificationChannelEntity> channel = channels.findById((UUID) row.get("channel_id"));
            if (channel.isEmpty()) {
                continue;
            }
            ChannelSettings s = channelService.settingsFor(channel.get(), 0, clock.instant());
            for (PreparedRequest request : pushover.beforeUpdate(cancel, String.valueOf(row.get("ref")), s)) {
                try {
                    ChannelResponse response = sender.send(request);
                    if (!response.success()) {
                        log.warn("Canceling a Pushover receipt of alert {} answered HTTP {}", alertId, response.status());
                    }
                } catch (IOException | RuntimeException e) {
                    log.warn("Canceling a Pushover receipt of alert {} failed: {}", alertId, e.toString());
                }
            }
        }
    }

    /** The {@code Escalation} JSON of {@code alertId}, if it has one. */
    public Optional<Map<String, Object>> view(long alertId) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                select alert_id, policy_id, next_step, next_at, repeats_done, state, stopped_reason, updated_at
                  from alert_escalation where alert_id = :alert
                """, new MapSqlParameterSource("alert", alertId));
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> row = rows.getFirst();
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("alertId", ((Number) row.get("alert_id")).longValue());
        out.put("policyId", String.valueOf(row.get("policy_id")));
        out.put("nextStep", ((Number) row.get("next_step")).intValue());
        out.put("nextAt", instant(row.get("next_at")));
        out.put("repeatsDone", ((Number) row.get("repeats_done")).intValue());
        out.put("state", row.get("state"));
        out.put("stoppedReason", row.get("stopped_reason"));
        out.put("updatedAt", instant(row.get("updated_at")));
        return Optional.of(out);
    }

    private static String instant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp ts) {
            return ts.toInstant().toString();
        }
        if (value instanceof java.time.OffsetDateTime odt) {
            return odt.toInstant().toString();
        }
        return String.valueOf(value);
    }
}
