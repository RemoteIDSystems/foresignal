package ai.foresignal.notify.action;

import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.store.DeliveryKind;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.trigger.AlertState;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Decides which deliveries carry action capabilities and mints them at render time (prompt 033).
 *
 * <p>Actions are attached only to {@code alert} events in state {@code firing}: the first send of an
 * {@code alert.fired} or {@code alert.escalated} notification, and the enrichment edit of an
 * {@code alert.fired} message (so the buttons survive it). Lifecycle updates and digests never carry
 * them, which also removes Telegram's keyboard when an acknowledgement edits the message. The
 * channel's {@code config.actions} ({@link ActionsConfig}) must be enabled.
 *
 * <p>Types without buttons get confirm-page links appended to the body ({@link #withLinks}) when
 * {@code actions.links} is on; {@code ntfy}, {@code telegram}, {@code pushover}, {@code webpush}
 * and {@code webhook} render the actions themselves.
 */
@Component
public class ActionMinter {

    private static final Logger log = LoggerFactory.getLogger(ActionMinter.class);

    /** Types that render actions natively rather than as body links. */
    public static final Set<String> NATIVE_TYPES = Set.of("ntfy", "telegram", "pushover", "webpush", "webhook");

    /** Subtypes that carry actions. */
    static final Set<String> ACTIONABLE = Set.of("alert.fired", "alert.escalated");

    private final ActionService actions;
    private final AlertStore alerts;

    /** Creates the minter. */
    public ActionMinter(ActionService actions, AlertStore alerts) {
        this.actions = actions;
        this.alerts = alerts;
    }

    /**
     * The capabilities of delivery {@code d} of {@code stored} on {@code channel}; empty when the
     * delivery does not carry actions. A failure to mint is logged and sends without actions.
     */
    public List<NotificationAction> forDelivery(NotificationDeliveryEntity d, NotificationMessage stored,
            NotificationChannelEntity channel) {
        if (!NotificationMessage.ALERT.equals(stored.event()) || stored.alertId() == null
                || !ACTIONABLE.contains(stored.subtype())) {
            return List.of();
        }
        boolean firstSend = DeliveryKind.SEND.equals(d.getKind());
        boolean enrichmentEdit = DeliveryKind.UPDATE.equals(d.getKind()) && stored.updateOf() == null;
        if (!firstSend && !enrichmentEdit) {
            return List.of();
        }
        ActionsConfig config = ActionsConfig.of(channel.getType(), channel.getConfig());
        if (!config.enabled() || config.list().isEmpty()) {
            return List.of();
        }
        try {
            Optional<AlertView> alert = alerts.find(stored.alertId());
            if (alert.isEmpty() || !AlertState.FIRING.wire().equals(alert.get().state())) {
                return List.of();
            }
            return actions.mint(alert.get(), channel.getId(), d.getId(), null, config.list());
        } catch (RuntimeException e) {
            log.warn("Could not mint action capabilities for delivery {}: {}", d.getId(), e.toString());
            return List.of();
        }
    }

    /**
     * {@code message} with confirm-page links ("Acknowledge · Snooze 1h · Mute 4h") appended to its
     * body, for a type without buttons whose {@code actions.links} is on; otherwise unchanged.
     */
    public static NotificationMessage withLinks(NotificationMessage message, NotificationChannelEntity channel,
            List<NotificationAction> actions) {
        if (actions.isEmpty() || NATIVE_TYPES.contains(channel.getType())
                || !ActionsConfig.of(channel.getType(), channel.getConfig()).links()) {
            return message;
        }
        StringBuilder links = new StringBuilder();
        for (NotificationAction action : actions) {
            if (!links.isEmpty()) {
                links.append(" · ");
            }
            links.append('[').append(MarkdownRenderer.escapeMarkdown(action.label())).append("](")
                    .append(action.url()).append(')');
        }
        String body = message.bodyMarkdown() == null || message.bodyMarkdown().isBlank()
                ? links.toString() : message.bodyMarkdown() + "\n\n" + links;
        return message.withText(message.title(), body);
    }
}
