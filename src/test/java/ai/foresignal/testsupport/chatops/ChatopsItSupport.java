package ai.foresignal.testsupport.chatops;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import ai.foresignal.chatops.ChannelConfig;
import ai.foresignal.chatops.ChatConnector;
import ai.foresignal.chatops.ChatConnectorFactory;
import ai.foresignal.chatops.ChatRateLimiter;
import ai.foresignal.chatops.ChatSession;
import ai.foresignal.chatops.ChatopsCursorStore;
import ai.foresignal.chatops.ChatopsManager;
import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.notify.ActionFixture;
import ai.foresignal.testsupport.notify.NotifyFixture;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared fixtures of the interactive-channel integration tests (prompt 061): channels created through the real
 * {@link ChannelService}, connectors from the real {@link ChatConnectorFactory} wired to the
 * {@link ChatopsManager}'s router-backed sink (synchronous in tests), a WireMock Bot API or homeserver, and
 * alerts inserted by {@link ActionFixture}. No test reaches a real chat platform.
 */
public abstract class ChatopsItSupport extends WireMockSupport {

    /** The test clock's instant. */
    public static final Instant T = Instant.parse("2026-10-13T12:00:00Z");

    /** A JSON mapper for reading recorded requests. */
    protected static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired protected ChannelService channels;
    @Autowired protected NotificationChannelRepository channelRows;
    @Autowired protected NotificationDeliveryRepository deliveries;
    @Autowired protected ChannelRateLimiter channelLimiter;
    @Autowired protected ChatRateLimiter chatLimiter;
    @Autowired protected ChatConnectorFactory factory;
    @Autowired protected ChatopsManager manager;
    @Autowired protected ChatopsCursorStore cursors;
    @Autowired protected AppSettingService settings;
    @Autowired protected AuditService audit;
    @Autowired protected TriggerRuleRepository rules;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected MutableClock clock;

    protected NotifyFixture notify;
    protected ActionFixture alerts;

    /** Fresh clock, limiters and fixtures. */
    @BeforeEach
    void setUpChatops() {
        clock.setInstant(T);
        settings.invalidateCache();
        channelLimiter.reset();
        chatLimiter.reset();
        notify = new NotifyFixture(channels, deliveries, channelLimiter);
        alerts = new ActionFixture(jdbc, rules);
    }

    /**
     * An interactive Telegram channel on the WireMock Bot API (token {@code TOKEN}) with {@code allowedChats} (JSON
     * with single quotes) and extra config members ({@code ,'k':v} or empty).
     */
    protected UUID telegram(String allowedChats, String extra) {
        return notify.channel("{'name':'ops','type':'telegram','config':{'chatId':'1','apiBaseUrl':'" + baseUrl()
                + "','interactive':true,'allowedChats':" + allowedChats + extra + "},'secrets':{'botToken':'TOKEN'},"
                + "'events':['alert']}");
    }

    /** An interactive Matrix channel on the WireMock homeserver with {@code allowedChats}. */
    protected UUID matrix(String allowedChats, String extra) {
        return notify.channel("{'name':'room','type':'matrix','config':{'homeserverUrl':'" + baseUrl()
                + "','roomId':'!ops:example.org','interactive':true,'allowedChats':" + allowedChats + extra
                + "},'secrets':{'accessToken':'MXTOKEN'},'events':['alert']}");
    }

    /** The stored channel's config. */
    protected ChannelConfig config(UUID channel) {
        return manager.configOf(channelRows.findById(channel).orElseThrow());
    }

    /** A fresh connector of {@code channel} with its router-backed session. */
    protected ChatSession session(UUID channel) {
        ChannelConfig config = config(channel);
        ChatConnector connector = factory.create(config.type());
        return new ChatSession(config, connector);
    }

    /** Starts the session's connector with the manager's router-backed sink. */
    protected void start(ChatSession session) throws Exception {
        session.connector().start(session.config(), manager.sink(session));
    }

    /** Stubs the Bot API methods every start calls, with or without a webhook set. */
    protected static void stubTelegramStart(String webhookUrl) {
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getMe"))
                .willReturn(okJson("{\"ok\":true,\"result\":{\"id\":9,\"is_bot\":true,\"username\":\"FsBot\"}}")));
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getWebhookInfo"))
                .willReturn(okJson("{\"ok\":true,\"result\":{\"url\":\"" + webhookUrl + "\",\"pending_update_count\":0}}")));
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/deleteWebhook")).willReturn(okJson("{\"ok\":true,\"result\":true}")));
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/setMyCommands")).willReturn(okJson("{\"ok\":true,\"result\":true}")));
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/sendMessage"))
                .willReturn(okJson("{\"ok\":true,\"result\":{\"message_id\":500}}")));
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/answerCallbackQuery")).willReturn(okJson("{\"ok\":true,\"result\":true}")));
    }

    /** Stubs {@code getUpdates} to answer {@code result} (a JSON array). */
    protected static void updates(String result) {
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getUpdates")).willReturn(okJson("{\"ok\":true,\"result\":" + result + "}")));
    }

    /** A Telegram message update. */
    protected static String message(long updateId, long chatId, long fromId, String text, Instant date) {
        return "{\"update_id\":" + updateId + ",\"message\":{\"message_id\":" + (updateId + 1000) + ",\"date\":"
                + date.getEpochSecond() + ",\"chat\":{\"id\":" + chatId + ",\"type\":\"" + (chatId < 0 ? "group" : "private")
                + "\"},\"from\":{\"id\":" + fromId + ",\"is_bot\":false,\"first_name\":\"Ana\"},\"text\":"
                + JSON.writeValueAsString(text) + "}}";
    }

    /** A Telegram callback_query update. */
    protected static String press(long updateId, long chatId, long fromId, String data) {
        return "{\"update_id\":" + updateId + ",\"callback_query\":{\"id\":\"cb" + updateId + "\",\"from\":{\"id\":" + fromId
                + ",\"is_bot\":false,\"first_name\":\"Ana\"},\"message\":{\"message_id\":77,\"chat\":{\"id\":" + chatId
                + "}},\"chat_instance\":\"x\",\"data\":\"" + data + "\"}}";
    }

    /** The bodies of every {@code sendMessage} so far. */
    protected static List<JsonNode> sent() {
        return WIREMOCK.findAll(postRequestedFor(urlEqualTo("/botTOKEN/sendMessage"))).stream()
                .map(r -> JSON.readTree(r.getBodyAsString())).toList();
    }

    /** The bodies of every {@code answerCallbackQuery} so far. */
    protected static List<JsonNode> answers() {
        return WIREMOCK.findAll(postRequestedFor(urlEqualTo("/botTOKEN/answerCallbackQuery"))).stream()
                .map(r -> JSON.readTree(r.getBodyAsString())).toList();
    }
}
