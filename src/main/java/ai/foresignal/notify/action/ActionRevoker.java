package ai.foresignal.notify.action;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Revokes the open action capabilities of an alert once it is acknowledged or resolved by any path
 * (REST, MCP, Ask, an action, Pushover, a reaction, automatic resolution): they are marked used with
 * result {@code superseded} (prompt 033). Runs on its own {@link AlertBus} thread, after the alert's
 * transaction committed, and writes in its own statement.
 */
@Component
public class ActionRevoker implements AlertListener {

    private static final Logger log = LoggerFactory.getLogger(ActionRevoker.class);

    private final AlertBus bus;
    private final ActionService actions;
    private volatile Subscription subscription;

    /** Creates the revoker. */
    public ActionRevoker(AlertBus bus, ActionService actions) {
        this.bus = bus;
        this.actions = actions;
    }

    /** Subscribes to the alert bus with the notification router. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.NOTIFY_ROUTER)
    public synchronized void start() {
        if (subscription == null) {
            subscription = bus.subscribe("actions", this, AlertBus.DEFAULT_QUEUE);
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
        if (AlertEvent.ACKNOWLEDGED.equals(event.type()) || AlertEvent.RESOLVED.equals(event.type())) {
            try {
                actions.supersede(event.alert().id());
            } catch (RuntimeException e) {
                log.warn("Could not revoke the action capabilities of alert {}: {}", event.alert().id(), e.toString());
            }
        }
    }

    @Override
    public void onGap() {
        log.warn("The action revoker missed alert events; capabilities of those alerts expire on their own"
                + " and execute as already-acknowledged or already-resolved");
    }
}
