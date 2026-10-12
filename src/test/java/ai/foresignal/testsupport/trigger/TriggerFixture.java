package ai.foresignal.testsupport.trigger;

import static org.awaitility.Awaitility.await;

import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared steps of the trigger integration tests: create rules from JSON, wait for the engine to catch
 * up with the change bus, read alerts, and collect the alert bus's events. Records are seeded only
 * through {@code TestRecords}.
 */
public final class TriggerFixture implements AutoCloseable {

    /** How long the engine may take. */
    public static final Duration WAIT = Duration.ofSeconds(10);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final TriggerRuleService rules;
    private final TriggerEngine engine;
    private final ChangeBus changeBus;
    private final AlertStore alerts;
    private final List<AlertEvent> events = new CopyOnWriteArrayList<>();
    private final Subscription subscription;

    /** Creates the fixture and subscribes to the alert bus. */
    public TriggerFixture(TriggerRuleService rules, TriggerEngine engine, ChangeBus changeBus, AlertStore alerts, AlertBus bus) {
        this.rules = rules;
        this.engine = engine;
        this.changeBus = changeBus;
        this.alerts = alerts;
        this.subscription = bus.subscribe("trigger-fixture-" + UUID.randomUUID(), events::add, 10_000);
    }

    /** Creates a rule from JSON (single quotes allowed) and returns its id. */
    public UUID rule(String json) {
        return rules.create(JSON.readTree(json.replace('\'', '"'))).rule().getId();
    }

    /** Waits until the engine has evaluated everything the bus has published. */
    public void settle() {
        await().atMost(WAIT).until(() -> engine.cursor() >= changeBus.lastPublishedSeq());
    }

    /** Every alert of {@code ruleId}, suppressed included, oldest first. */
    public List<AlertView> alerts(UUID ruleId) {
        settle();
        List<AlertView> list = new ArrayList<>(alerts.list(
                new AlertQuery(Set.of(ruleId), null, null, null, null, null, null, null, null, true, null), null, 10_000));
        list.sort((a, b) -> Long.compare(a.id(), b.id()));
        return list;
    }

    /** Every alert of {@code ruleId} that is not suppressed, oldest first. */
    public List<AlertView> routed(UUID ruleId) {
        return alerts(ruleId).stream().filter(a -> !a.suppressed()).toList();
    }

    /** The events received so far. */
    public List<AlertEvent> events() {
        return List.copyOf(events);
    }

    /** Waits for an event of {@code type} for alert {@code alertId}. */
    public AlertEvent awaitEvent(String type, long alertId) {
        await().atMost(WAIT).until(() -> events.stream().anyMatch(e -> e.type().equals(type) && e.alert().id() == alertId));
        return events.stream().filter(e -> e.type().equals(type) && e.alert().id() == alertId).findFirst().orElseThrow();
    }

    @Override
    public void close() {
        subscription.close();
    }
}
