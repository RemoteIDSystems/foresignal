package ai.foresignal.notify.action;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationStore;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Executes Telegram action buttons (prompt 033): a {@code callback_query} whose {@code data} is
 * {@code ea:<token>}.
 *
 * <ol>
 *   <li>The presser must be allowed: {@code from.id} in the channel's {@code allowedUserIds}, or,
 *       when that list is empty, the button must sit in the channel's own chat ({@code chatId}),
 *       whose members are the ones who see it.
 *   <li>The capability is executed ({@link ActionService#execute}, via {@code telegram:<first_name>}).
 *   <li>{@code answerCallbackQuery} reports the result (Telegram shows a progress bar until it is
 *       answered).
 *   <li>The message is edited with the 018 {@code renderUpdate} to "Acknowledged by &lt;first_name&gt;
 *       — …", and the keyboard removed.
 * </ol>
 */
@Component
public class ActionCallbackHandler implements TelegramUpdateHandler {

    private static final Logger log = LoggerFactory.getLogger(ActionCallbackHandler.class);

    private final ActionService actions;
    private final ChannelSender sender;
    private final NotificationDeliveryRepository deliveries;
    private final NotificationStore notifications;
    private final TelegramChannel type;
    private final JsonMapper json;

    /** Creates the handler. */
    public ActionCallbackHandler(ActionService actions, ChannelSender sender, NotificationDeliveryRepository deliveries,
            NotificationStore notifications, TelegramChannel type, JsonMapper json) {
        this.actions = actions;
        this.sender = sender;
        this.deliveries = deliveries;
        this.notifications = notifications;
        this.type = type;
        this.json = json;
    }

    @Override
    public Set<String> updateTypes() {
        return Set.of("callback_query");
    }

    @Override
    public boolean wants(NotificationChannelEntity channel) {
        return TelegramChannel.ID.equals(channel.getType())
                && ActionsConfig.of(channel.getType(), channel.getConfig()).enabled();
    }

    @Override
    public boolean handle(NotificationChannelEntity channel, ChannelSettings s, JsonNode update) {
        JsonNode query = update.get("callback_query");
        if (query == null || !query.path("data").asString("").startsWith(TelegramChannel.CALLBACK_PREFIX)) {
            return false;
        }
        String callbackId = query.path("id").asString();
        String token = query.path("data").asString().substring(TelegramChannel.CALLBACK_PREFIX.length());
        String firstName = query.path("from").path("first_name").asString("someone");
        if (!allowed(channel, s, query)) {
            answer(s, callbackId, "You are not allowed to act on these alerts.", true);
            return true;
        }
        ActionService.Result result;
        try {
            result = actions.execute(token, new ActionService.Via("telegram", firstName, null));
        } catch (ActionExpiredException e) {
            answer(s, callbackId, "This action expired or was already used.", true);
            return true;
        }
        String label = label(result.result());
        answer(s, callbackId, label, false);
        edit(s, query, result, label + " by " + firstName);
        return true;
    }

    /** True when the presser may act: listed in {@code allowedUserIds}, or any member of {@code chatId}. */
    static boolean allowed(NotificationChannelEntity channel, ChannelSettings s, JsonNode query) {
        Object list = channel.getConfig() == null ? null : channel.getConfig().get("allowedUserIds");
        long from = query.path("from").path("id").asLong(Long.MIN_VALUE);
        if (list instanceof List<?> ids && !ids.isEmpty()) {
            return ids.stream().anyMatch(id -> id instanceof Number n && n.longValue() == from);
        }
        String chat = query.path("message").path("chat").path("id").asString("");
        return !chat.isEmpty() && chat.equals(s.config("chatId", "").strip());
    }

    /** The human result: "Acknowledged", "Snoozed", "Muted", "Already acknowledged", … */
    static String label(String result) {
        return switch (result) {
            case "acknowledged" -> "✅ Acknowledged";
            case "snoozed" -> "Snoozed";
            case "muted" -> "Muted";
            default -> {
                String text = result.replace('-', ' ');
                yield Character.toUpperCase(text.charAt(0)) + text.substring(1);
            }
        };
    }

    private void answer(ChannelSettings s, String callbackId, String text, boolean alert) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("callback_query_id", callbackId);
        body.put("text", text);
        body.put("show_alert", alert);
        send(TelegramChannel.apiMethod(s, "answerCallbackQuery"), body);
    }

    /** Edits the pressed message with the 018 update rendering, which also removes the keyboard. */
    private void edit(ChannelSettings s, JsonNode query, ActionService.Result result, String banner) {
        String messageId = query.path("message").path("message_id").asString("");
        if (messageId.isEmpty()) {
            return;
        }
        Optional<NotificationMessage> original = Optional.ofNullable(result.deliveryId())
                .flatMap(deliveries::findById)
                .flatMap(d -> notifications.find(d.getNotificationId()));
        NotificationMessage base = original.orElseGet(() -> NotificationMessage.builder(NotificationMessage.ALERT,
                "alert.acknowledged", "Alert " + result.alertId(), "info").build());
        NotificationMessage edited = base.withText(banner + " — " + base.title(), base.bodyMarkdown());
        type.renderUpdate(edited, messageId, s.withActions(List.of())).ifPresent(request -> {
            try {
                ChannelResponse response = sender.send(request);
                if (!response.success()) {
                    log.warn("Editing Telegram message after an action answered HTTP {}", response.status());
                }
            } catch (IOException | RuntimeException e) {
                log.warn("Editing Telegram message after an action failed: {}", e.toString());
            }
        });
    }

    private void send(String url, Object body) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        try {
            ChannelResponse response = sender.send(new PreparedRequest("POST", url, headers,
                    PreparedRequest.Bytes.text("application/json", json.writeValueAsString(body))));
            if (!response.success()) {
                log.warn("Telegram answered HTTP {} to an action reply", response.status());
            }
        } catch (IOException | RuntimeException e) {
            log.warn("Telegram action reply failed: {}", e.toString());
        }
    }
}
