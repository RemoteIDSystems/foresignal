package ai.foresignal.chatops;

import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.PreparedRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Matrix side of an interactive channel (prompt 061), through the client-server API v3 with the channel's
 * bot account (never a user session).
 *
 * <ul>
 *   <li><b>Start:</b> {@code GET /account/whoami} (the bot's own MXID). Each allowed room is checked: an
 *       {@code m.room.encryption} state event means the room is ignored with "encrypted rooms are not
 *       supported" (health {@code degraded}); a room with more than two joined members and no {@code userIds}
 *       is ignored too, since any member could command. On the first start a {@code /sync} with
 *       {@code timeout=0} records {@code next_batch} and discards the timeline.
 *   <li><b>Poll:</b> {@code /sync} with {@code since}, {@code timeout} and the filter
 *       {@code {room: {rooms: [allowed], timeline: {types: ["m.room.message"], limit: 20}, state:
 *       {lazy_load_members: true}}, presence: {types: []}, account_data: {types: []}}}; {@code next_batch} is
 *       stored after the batch. The bot's own events and edits ({@code m.replace}) are ignored, and so are
 *       messages older than {@code staleAfter}.
 *   <li><b>Replies:</b> rich replies ({@code m.relates_to.m.in_reply_to}) with {@code formatted_body} from
 *       018's Matrix formatter, sent with a deterministic transaction id so a retried send is idempotent.
 *       There are no buttons: confirmations use {@code /confirm <code>} and {@code /reject <code>}.
 * </ul>
 */
public class MatrixConnector extends AbstractChatConnector {

    private static final Logger log = LoggerFactory.getLogger(MatrixConnector.class);

    /** The reason an encrypted room is ignored. */
    public static final String ENCRYPTED_REASON = "encrypted rooms are not supported";

    /** The reason a room with more than two members and no userIds is ignored. */
    public static final String MEMBERS_REASON = "room has more than two members; list userIds";

    /** The HTML format id. */
    static final String FORMAT = "org.matrix.custom.html";

    private volatile String self;

    /** Creates the connector. */
    public MatrixConnector(ChannelSender sender, ChatopsCursorStore cursors, ChannelRateLimiter limiter,
            JsonMapper json, Clock clock, ChatopsProperties properties) {
        super(sender, cursors, limiter, json, clock, properties);
    }

    @Override
    protected ChannelRateLimit replyLimit() {
        return ChannelRateLimit.perSecond(5);
    }

    @Override
    public boolean supportsButtons() {
        return false;
    }

    /** The bot's MXID from {@code whoami}, or null before start. */
    public String self() {
        return self;
    }

    @Override
    public void start(ChannelConfig channel, CommandSink commandSink) throws IOException {
        this.config = channel;
        this.sink = commandSink;
        this.stopped = false;
        self = check("whoami", send(get("/account/whoami"))).path("user_id").asString("");
        for (AllowedChat chat : channel.interactive().allowedChats()) {
            state.activate(chat.chatId());
            ChannelResponse encryption = send(get("/rooms/" + encode(chat.chatId()) + "/state/m.room.encryption"));
            if (encryption.status() == 401) {
                throw new ChatAuthException("The homeserver refused the access token (HTTP 401)");
            }
            if (encryption.success()) {
                state.deactivate(chat.chatId(), ENCRYPTED_REASON);
                continue;
            }
            if (encryption.status() == 403) {
                state.deactivate(chat.chatId(), "the bot is not a member of this room");
                continue;
            }
            if (encryption.status() != 404) {
                throw new IOException("room state answered HTTP " + encryption.status());
            }
            if (chat.userIds().isEmpty()) {
                JsonNode members = check("joined_members", send(get("/rooms/" + encode(chat.chatId()) + "/joined_members")));
                if (members.path("joined").size() > 2) {
                    state.deactivate(chat.chatId(), MEMBERS_REASON);
                }
            }
        }
        if (cursors.get(channel.channelId()).isEmpty()) {
            JsonNode first = check("sync", send(syncRequest(null, 0)));
            String next = first.path("next_batch").asString("");
            if (!next.isEmpty()) {
                cursors.put(channel.channelId(), next);
            }
        }
        state.polled(clock.instant());
    }

    @Override
    public int poll() throws IOException {
        ChannelConfig channel = config;
        Optional<String> since = cursors.get(channel.channelId());
        long timeout = since.isEmpty() ? 0 : properties.longPoll().toMillis();
        JsonNode body = check("sync", sendLongPoll(syncRequest(since.orElse(null), timeout), properties.longPoll()));
        int count = 0;
        if (since.isPresent()) {
            JsonNode join = body.path("rooms").path("join");
            for (AllowedChat chat : channel.interactive().allowedChats()) {
                if (!state.active(chat.chatId())) {
                    continue;
                }
                for (JsonNode event : join.path(chat.chatId()).path("timeline").path("events")) {
                    if (stopped) {
                        break;
                    }
                    try {
                        if (handle(chat.chatId(), event)) {
                            count++;
                        }
                    } catch (RuntimeException e) {
                        log.warn("Matrix event of channel {} failed: {}", channel.channelId(), e.toString());
                    }
                }
            }
        }
        String next = body.path("next_batch").asString("");
        if (!next.isEmpty()) {
            cursors.put(channel.channelId(), next);
        }
        state.polled(clock.instant());
        return count;
    }

    /** Hands one timeline event to the sink; false when it is ignored. */
    boolean handle(String roomId, JsonNode event) {
        if (!"m.room.message".equals(event.path("type").asString(""))) {
            return false;
        }
        String sender = event.path("sender").asString("");
        if (sender.isEmpty() || sender.equals(self)) {
            return false;
        }
        JsonNode content = event.path("content");
        if ("m.replace".equals(content.path("m.relates_to").path("rel_type").asString("")) || content.has("m.new_content")) {
            return false;
        }
        if (!"m.text".equals(content.path("msgtype").asString(""))) {
            return false;
        }
        String text = content.path("body").asString("");
        if (text.isBlank()) {
            return false;
        }
        Instant sent = Instant.ofEpochMilli(event.path("origin_server_ts").asLong(0));
        if (sent.isBefore(clock.instant().minus(properties.staleAfter()))) {
            return false;
        }
        sink.onMessage(new ChatRef(config.channelId(), roomId, sender, sender, event.path("event_id").asString(null)),
                text, sent);
        return true;
    }

    @Override
    public void reply(ChatRef chat, ChatReply reply) {
        if (!acquire(chat.chatId())) {
            return;
        }
        Map<String, Object> content = content(reply);
        String txn = txnId(config.channelId().toString(), chat.chatId(), reply.replyTo(), reply.markdown(),
                reply.replyTo() == null ? clock.instant().toEpochMilli() : 0);
        try {
            ChannelResponse response = send(jsonRequest("PUT", base() + "/rooms/" + encode(chat.chatId())
                    + "/send/m.room.message/" + txn, auth(), content));
            if (!response.success()) {
                logReplyFailure("m.room.message", response);
            }
        } catch (IOException | RuntimeException e) {
            logReplyFailure("m.room.message", e);
        }
    }

    /** Matrix has no buttons; nothing to answer. */
    @Override
    public void answerCallback(String callbackRef, String text) {
        // No buttons on Matrix: confirmations are text commands.
    }

    /** The {@code m.room.message} content of {@code reply}: a rich reply with {@code formatted_body}. */
    static Map<String, Object> content(ChatReply reply) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("msgtype", "m.text");
        content.put("body", MarkdownRenderer.render(reply.markdown(), MarkdownRenderer.Format.PLAIN));
        content.put("format", FORMAT);
        content.put("formatted_body", MarkdownRenderer.render(reply.markdown(), MarkdownRenderer.Format.MATRIX_HTML));
        if (reply.replyTo() != null && !reply.replyTo().isBlank()) {
            content.put("m.relates_to", Map.of("m.in_reply_to", Map.of("event_id", reply.replyTo())));
        }
        return content;
    }

    /**
     * A deterministic transaction id: the same reply to the same event always gets the same id, so a retried
     * send is idempotent on the homeserver. Follow-ups without an event mix in {@code salt}.
     */
    static String txnId(String channelId, String roomId, String replyTo, String markdown, long salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = channelId + "|" + roomId + "|" + replyTo + "|" + markdown + "|" + salt;
            return "fscc" + HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)), 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** The {@code /sync} filter of the allowed rooms. */
    static Map<String, Object> filter(List<String> rooms) {
        Map<String, Object> timeline = new LinkedHashMap<>();
        timeline.put("types", List.of("m.room.message"));
        timeline.put("limit", 20);
        Map<String, Object> room = new LinkedHashMap<>();
        room.put("rooms", List.copyOf(rooms));
        room.put("timeline", timeline);
        room.put("state", Map.of("lazy_load_members", true));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("room", room);
        out.put("presence", Map.of("types", List.of()));
        out.put("account_data", Map.of("types", List.of()));
        return out;
    }

    private PreparedRequest syncRequest(String since, long timeoutMillis) {
        List<String> rooms = new ArrayList<>();
        for (AllowedChat chat : config.interactive().allowedChats()) {
            rooms.add(chat.chatId());
        }
        String url = base() + "/sync?filter=" + encode(json.writeValueAsString(filter(rooms))) + "&timeout="
                + timeoutMillis + (since == null ? "" : "&since=" + encode(since));
        return get(url.substring(base().length()));
    }

    private PreparedRequest get(String path) {
        return new PreparedRequest("GET", base() + path, auth(), PreparedRequest.Bytes.text("application/json", ""));
    }

    private JsonNode check(String what, ChannelResponse response) throws IOException {
        if (authFailure(response.status())) {
            throw new ChatAuthException("The homeserver refused the access token (" + what + " answered HTTP "
                    + response.status() + ")");
        }
        if (!response.success()) {
            throw new IOException(what + " answered HTTP " + response.status());
        }
        return json.readTree(response.body() == null || response.body().isEmpty() ? "{}" : response.body());
    }

    private String base() {
        return ChannelSettings.trimSlash(config.settings().config("homeserverUrl", "")) + "/_matrix/client/v3";
    }

    private Map<String, String> auth() {
        return Map.of("Authorization", "Bearer " + config.settings().secret("accessToken"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
