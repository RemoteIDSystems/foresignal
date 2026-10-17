package ai.foresignal.quality;

import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import ai.foresignal.trigger.TriggerRuleSnoozedEvent;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Records implicit signals (prompt 115) from alert lifecycle events ({@link AlertSignals}) and rule snoozes. A
 * failure is logged and never affects the alert lifecycle.
 */
@Component
public class AlertSignalRecorder implements AlertListener {

    private static final Logger log = LoggerFactory.getLogger(AlertSignalRecorder.class);

    private final AlertBus bus;
    private final AlertSignalStore store;
    private final Clock clock;
    private volatile Subscription subscription;

    /** Creates the recorder. */
    public AlertSignalRecorder(AlertBus bus, AlertSignalStore store, Clock clock) {
        this.bus = bus;
        this.store = store;
        this.clock = clock;
    }

    /** Subscribes to the alert bus. */
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void start() {
        if (subscription == null) {
            subscription = bus.subscribe("alert-signals", this, AlertBus.DEFAULT_QUEUE);
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

    @Override
    public void onAlert(AlertEvent event) {
        AlertSignals.of(event).ifPresent(signal -> {
            try {
                store.record(event.alert().id(), signal.type(), signal.at() == null ? clock.instant() : signal.at());
            } catch (RuntimeException e) {
                log.warn("Could not record signal {} of alert {}: {}", signal.type(), event.alert().id(), e.toString());
            }
        });
    }

    /** A rule snooze counts as {@code SNOOZED} for the rule's open alerts. */
    @EventListener
    public void onRuleSnoozed(TriggerRuleSnoozedEvent event) {
        try {
            Instant at = event.at() == null ? clock.instant() : event.at();
            store.recordRuleSnoozed(event.ruleId(), at);
        } catch (RuntimeException e) {
            log.warn("Could not record the snooze of rule {}: {}", event.ruleId(), e.toString());
        }
    }
}
