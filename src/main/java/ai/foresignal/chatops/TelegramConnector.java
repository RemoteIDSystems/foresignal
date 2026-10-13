package ai.foresignal.chatops;

import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.Truncator;
import ai.foresignal.notify.action.TelegramUpdateHandler;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.security.Scope;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Telegram side of an interactive channel (prompt 061), through the Bot API only.
 *
 * <ul>
 *   <li><b>Start:</b> {@code getMe} (the bot's username, for {@code /cmd@BotName}); {@code getWebhookInfo}: a
 *       webhook URL with {@code takeOverWebhook} off blocks the connector (health {@code degraded}, "bot has a
 *       webhook; set takeOverWebhook or use a dedicated bot"), otherwise {@code deleteWebhook};
 *       {@code setMyCommands} scoped to each allowed chat with the commands of its scope. On the first start
 *       ({@code chatops_cursor} empty) {@code getUpdates?offset=-1} records the last update + 1, so the
 *       backlog is skipped.
 *   <li><b>Poll:</b> {@code getUpdates} with {@code timeout=50}, the stored {@code offset} and
 *       {@code allowed_updates=["message","callback_query"]}; the next offset is stored after each update.
 *       401 or 403 is a {@link ChatAuthException}; 409 (a webhook, or another poller of the same bot) blocks.
 *   <li><b>Messages:</b> messages older than {@code staleAfter} (2 min, by {@code date}) are ignored;
 *       {@code /cmd@BotName} becomes {@code /cmd}, and a command addressed to another bot is ignored.
 *   <li><b>Buttons:</b> one dispatcher for every {@code callback_query} of the bot, keyed on the data prefix:
 *       {@code cc_} is a chat-command token ({@link CommandSink#onCallback}); {@code ea:} is a prompt 033 signed
 *       action, handed to the {@link TelegramUpdateHandler}s exactly as {@code TelegramCallbackPoller} would.
 *       That poller skips interactive channels, so only one consumer ever calls {@code getUpdates} per bot.
 *   <li><b>Replies:</b> {@code sendMessage} in HTML (018's Telegram formatter), cut to 4,096 characters, with
 *       {@code reply_parameters.message_id}, buttons as an inline keyboard; 20 per minute per chat through the
 *       018 rate limiter.
 * </ul>
 */
public class TelegramConnector extends AbstractChatConnector {

    private static final Logger log = LoggerFactory.getLogger(TelegramConnector.class);

    /** The update types an interactive channel needs. */
    public static final List<String> ALLOWED_UPDATES = List.of("message", "callback_query");

    /** The webhook problem shown as the connector's health reason. */
    public static final String WEBHOOK_REASON = "bot has a webhook; set takeOverWebhook or use a dedicated bot";

    private final List<TelegramUpdateHandler> handlers;
    private final NotificationChannelRepository channels;
    private volatile String botUsername;

    /** Creates the connector. */
    public TelegramConnector(ChannelSender sender, ChatopsCursorStore cursors, ChannelRateLimiter limiter,
            JsonMapper json, Clock clock, ChatopsProperties properties, List<TelegramUpdateHandler> handlers,
            NotificationChannelRepository channels) {
        super(sender, cursors, limiter, json, clock, properties);
        this.handlers = handlers == null ? List.of() : List.copyOf(handlers);
        this.channels = channels;
    }

    @Override
    protected ChannelRateLimit replyLimit() {
        return ChannelRateLimit.perMinute(20);
    }

    @Override
    public boolean supportsButtons() {
        return true;
    }

    /** The bot's username from {@code getMe}, or null before start. */
    public String botUsername() {
        return botUsername;
    }

    @Override
    public void start(ChannelConfig channel, CommandSink commandSink) throws IOException {
        this.config = channel;
        this.sink = commandSink;
        this.stopped = false;
        JsonNode me = call("getMe", Map.of());
        botUsername = me.path("result").path("username").asString("");
        JsonNode webhook = call("getWebhookInfo", Map.of());
        String url = webhook.path("result").path("url").asString("");
        if (!url.isEmpty()) {
            if (!channel.interactive().takeOverWebhook()) {
                throw new ConnectorBlockedException(WEBHOOK_REASON);
            }
            call("deleteWebhook", Map.of("drop_pending_updates", false));
        }
        for (AllowedChat chat : channel.interactive().allowedChats()) {
            state.activate(chat.chatId());
            try {
                call("setMyCommands", Map.of("commands", commands(chat.scope()),
                        "scope", Map.of("type", "chat", "chat_id", chatId(chat.chatId()))));
            } catch (IOException | RuntimeException e) {
                if (e instanceof ChatAuthException auth) {
                    throw auth;
                }
                log.warn("setMyCommands for a chat of channel {} failed: {}", channel.channelId(), redact(e.toString()));
            }
        }
        if (cursors.get(channel.channelId()).isEmpty()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("offset", -1);
            body.put("timeout", 0);
            body.put("allowed_updates", ALLOWED_UPDATES);
            JsonNode result = call("getUpdates", body).path("result");
            long next = 0;
            for (JsonNode update : result) {
                next = Math.max(next, update.path("update_id").asLong() + 1);
            }
            cursors.put(channel.channelId(), Long.toString(next));
        }
        state.polled(clock.instant());
    }

    @Override
    public int poll() throws IOException {
        ChannelConfig channel = config;
        long offset = cursors.get(channel.channelId()).map(TelegramConnector::parseOffset).orElse(0L);
        Map<String, Object> body = new LinkedHashMap<>();
        if (offset > 0) {
            body.put("offset", offset);
        }
        body.put("timeout", Math.max(0, properties.longPoll().toSeconds()));
        body.put("allowed_updates", ALLOWED_UPDATES);
        ChannelResponse response = sendLongPoll(jsonRequest("POST", TelegramChannel.apiMethod(channel.settings(),
                "getUpdates"), null, body), properties.longPoll());
        JsonNode root = check("getUpdates", response);
        int count = 0;
        long next = offset;
        for (JsonNode update : root.path("result")) {
            if (stopped) {
                break;
            }
            count++;
            long id = update.path("update_id").asLong();
            try {
                handle(update);
            } catch (RuntimeException e) {
                log.warn("Telegram update {} of channel {} failed: {}", id, channel.channelId(), redact(e.toString()));
            }
            if (id + 1 > next) {
                next = id + 1;
                cursors.put(channel.channelId(), Long.toString(next));
            }
        }
        state.polled(clock.instant());
        return count;
    }

    /** Dispatches one update: a message to the sink, a press by its data prefix. */
    void handle(JsonNode update) {
        JsonNode message = update.get("message");
        if (message != null && message.isObject()) {
            onMessage(message);
            return;
        }
        JsonNode query = update.get("callback_query");
        if (query != null && query.isObject()) {
            onCallbackQuery(update, query);
        }
    }

    private void onMessage(JsonNode message) {
        String text = message.path("text").asString(null);
        if (text == null || text.isBlank()) {
            return;
        }
        Instant sent = Instant.ofEpochSecond(message.path("date").asLong(0));
        if (sent.isBefore(clock.instant().minus(properties.staleAfter()))) {
            return;
        }
        Optional<String> normalized = normalize(text, botUsername);
        if (normalized.isEmpty()) {
            return;
        }
        JsonNode from = message.path("from");
        if (from.path("is_bot").asBoolean(false)) {
            return;
        }
        ChatRef ref = new ChatRef(config.channelId(), message.path("chat").path("id").asString(""),
                from.has("id") ? from.path("id").asString() : null, from.path("first_name").asString(null),
                message.path("message_id").asString(null));
        sink.onMessage(ref, normalized.get(), sent);
    }

    private void onCallbackQuery(JsonNode update, JsonNode query) {
        String data = query.path("data").asString("");
        String callbackId = query.path("id").asString("");
        if (data.startsWith(PendingTokens.BUTTON_PREFIX)) {
            JsonNode from = query.path("from");
            ChatRef ref = new ChatRef(config.channelId(), query.path("message").path("chat").path("id").asString(""),
                    from.has("id") ? from.path("id").asString() : null, from.path("first_name").asString(null),
                    query.path("message").path("message_id").asString(null));
            sink.onCallback(ref, callbackId, data);
            return;
        }
        if (data.startsWith(TelegramChannel.CALLBACK_PREFIX)) {
            // Prompt 033's signed actions: the same handlers TelegramCallbackPoller would call.
            NotificationChannelEntity entity = channels == null ? null : channels.findById(config.channelId()).orElse(null);
            if (entity != null) {
                for (TelegramUpdateHandler handler : handlers) {
                    if (handler.wants(entity) && handler.handle(entity, config.settings(), update)) {
                        return;
                    }
                }
            }
        }
        answerCallback(callbackId, "This button is not valid any more.");
    }

    /**
     * Normalizes a message: {@code /cmd@BotName rest} becomes {@code /cmd rest}; a command addressed to another
     * bot is empty (ignored); anything else is returned unchanged.
     */
    public static Optional<String> normalize(String text, String botUsername) {
        String line = text.strip();
        if (!line.startsWith("/")) {
            return Optional.of(text);
        }
        int end = 0;
        while (end < line.length() && !Character.isWhitespace(line.charAt(end))) {
            end++;
        }
        String head = line.substring(0, end);
        int at = head.indexOf('@');
        if (at < 0) {
            return Optional.of(line);
        }
        String target = head.substring(at + 1);
        if (botUsername == null || botUsername.isEmpty() || !target.equalsIgnoreCase(botUsername)) {
            return Optional.empty();
        }
        return Optional.of(head.substring(0, at) + line.substring(end));
    }

    @Override
    public void reply(ChatRef chat, ChatReply reply) {
        if (!acquire(chat.chatId())) {
            return;
        }
        String html = Truncator.truncate(MarkdownRenderer.render(reply.markdown(), MarkdownRenderer.Format.TELEGRAM_HTML),
                4096, Truncator.Unit.CHARS, Truncator.Mode.HTML, null);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", chatId(chat.chatId()));
        body.put("text", html.isBlank() ? "…" : html);
        body.put("parse_mode", "HTML");
        body.put("link_preview_options", Map.of("is_disabled", true));
        String replyTo = reply.replyTo();
        if (replyTo != null && replyTo.matches("[0-9]{1,18}")) {
            body.put("reply_parameters", Map.of("message_id", Long.parseLong(replyTo),
                    "allow_sending_without_reply", true));
        }
        if (reply.hasButtons()) {
            List<List<Map<String, Object>>> rows = new ArrayList<>();
            for (List<ChatButton> row : reply.buttons()) {
                List<Map<String, Object>> out = new ArrayList<>();
                for (ChatButton button : row) {
                    out.add(Map.of("text", button.label(), "callback_data", button.token()));
                }
                rows.add(out);
            }
            body.put("reply_markup", Map.of("inline_keyboard", rows));
        }
        try {
            ChannelResponse response = send(jsonRequest("POST", TelegramChannel.apiMethod(config.settings(),
                    "sendMessage"), null, body));
            if (!response.success()) {
                logReplyFailure("sendMessage", response);
            }
        } catch (IOException | RuntimeException e) {
            log.warn("sendMessage on interactive channel {} failed: {}", config.channelId(), redact(e.toString()));
        }
    }

    @Override
    public void answerCallback(String callbackRef, String text) {
        if (callbackRef == null || callbackRef.isEmpty()) {
            return;
        }
        try {
            ChannelResponse response = send(jsonRequest("POST", TelegramChannel.apiMethod(config.settings(),
                    "answerCallbackQuery"), null, Map.of("callback_query_id", callbackRef, "text",
                    text == null ? "" : ChatText.cut(text, 190))));
            if (!response.success()) {
                logReplyFailure("answerCallbackQuery", response);
            }
        } catch (IOException | RuntimeException e) {
            log.warn("answerCallbackQuery on interactive channel {} failed: {}", config.channelId(), redact(e.toString()));
        }
    }

    /** The {@code setMyCommands} list of a chat of {@code scope}. */
    static List<Map<String, String>> commands(Scope scope) {
        List<Map<String, String>> out = new ArrayList<>();
        for (ChatCommand command : ChatCommand.values()) {
            boolean agentOnly = command == ChatCommand.ACK || command == ChatCommand.RESOLVE
                    || command == ChatCommand.SNOOZE || command == ChatCommand.CONFIRM || command == ChatCommand.REJECT;
            if (agentOnly && scope != Scope.AGENT) {
                continue;
            }
            out.add(Map.of("command", command.word(), "description", ChatText.cut(command.help(), 256)));
        }
        return out;
    }

    /** Calls Bot API {@code method} with {@code body} and returns the checked response. */
    private JsonNode call(String method, Map<String, Object> body) throws IOException {
        return check(method, send(jsonRequest("POST", TelegramChannel.apiMethod(config.settings(), method), null, body)));
    }

    private JsonNode check(String method, ChannelResponse response) throws IOException {
        if (authFailure(response.status())) {
            throw new ChatAuthException("Telegram refused the bot token (" + method + " answered HTTP "
                    + response.status() + ")");
        }
        if (response.status() == 409) {
            String description = description(response);
            throw new ConnectorBlockedException(description.toLowerCase(java.util.Locale.ROOT).contains("webhook")
                    ? WEBHOOK_REASON : "another getUpdates consumer uses this bot; use a dedicated bot ("
                            + description + ")");
        }
        if (!response.success()) {
            throw new IOException(method + " answered HTTP " + response.status());
        }
        return json.readTree(response.body() == null || response.body().isEmpty() ? "{}" : response.body());
    }

    private String description(ChannelResponse response) {
        try {
            return ChatText.cut(json.readTree(response.body()).path("description").asString(""), 200);
        } catch (RuntimeException e) {
            return "";
        }
    }

    @Override
    public String describe(Throwable error) {
        return redact(String.valueOf(error));
    }

    private String redact(String text) {
        String token = config == null ? null : config.settings().secret("botToken");
        return token == null || token.isEmpty() || text == null ? text : text.replace(token, "***");
    }

    private static Object chatId(String chatId) {
        try {
            return Long.parseLong(chatId);
        } catch (NumberFormatException e) {
            return chatId;
        }
    }

    private static long parseOffset(String cursor) {
        try {
            return Long.parseLong(cursor.strip());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
