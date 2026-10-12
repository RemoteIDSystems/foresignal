package ai.foresignal.notify.action;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.notify.ActionFixture;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertStore;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Telegram action buttons end to end against a WireMock Bot API (prompt 033): the keyboard sent with
 * a firing alert, {@code getUpdates} with {@code allowed_updates}, allowed versus disallowed
 * pressers, {@code answerCallbackQuery}, the edit that removes the keyboard, and the persisted offset.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class TelegramCallbackPollerIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired TelegramCallbackPoller poller;
    @Autowired NotificationRouter router;
    @Autowired DeliveryWorker worker;
    @Autowired AlertStore alerts;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired TriggerRuleRepository rules;
    @Autowired AppSettingService settings;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    ActionFixture fixture;
    NotifyFixture notify;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        fixture = new ActionFixture(jdbc, rules);
        notify = new NotifyFixture(channels, deliveries, limiter);
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/sendMessage"))
                .willReturn(okJson("{\"ok\":true,\"result\":{\"message_id\":77}}")));
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/answerCallbackQuery")).willReturn(okJson("{\"ok\":true,\"result\":true}")));
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/editMessageText")).willReturn(okJson("{\"ok\":true,\"result\":{}}")));
    }

    UUID channel(String allowed) {
        return notify.channel("{'name':'t','type':'telegram','config':{'chatId':'-100','apiBaseUrl':'" + baseUrl()
                + "','allowedUserIds':" + allowed + "},'secrets':{'botToken':'TOKEN'}}");
    }

    /** Fires an alert, sends it, and returns the callback_data of its Acknowledge button. */
    String sendAlert(long alert) {
        router.fired(alerts.find(alert).orElseThrow());
        worker.runOnce();
        List<LoggedRequest> sent = WIREMOCK.findAll(postRequestedFor(urlEqualTo("/botTOKEN/sendMessage")));
        JsonNode body = JSON.readTree(sent.getLast().getBodyAsString());
        return body.at("/reply_markup/inline_keyboard/0/0/callback_data").asString();
    }

    static String update(long id, long from, String data) {
        return "{\"ok\":true,\"result\":[{\"update_id\":" + id + ",\"callback_query\":{\"id\":\"cb" + id
                + "\",\"from\":{\"id\":" + from + ",\"is_bot\":false,\"first_name\":\"Ana\"},"
                + "\"message\":{\"message_id\":77,\"chat\":{\"id\":-100,\"type\":\"group\"},\"text\":\"Quake\"},"
                + "\"chat_instance\":\"x\",\"data\":\"" + data + "\"}}]}";
    }

    @Test
    @DisplayName("an allowed press acknowledges the alert, answers the query, edits the message and stores the offset")
    void allowedPress() throws Exception {
        UUID channel = channel("[42]");
        long alert = fixture.alert(fixture.rule("Quakes", T), "warning", T);
        String data = sendAlert(alert);
        assertThat(data).startsWith("ea:eac_");
        assertThat(data.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getUpdates")).willReturn(okJson(update(500, 42, data))));

        assertThat(poller.pollOnce(channel, 0)).isEqualTo(1);

        assertThat(fixture.state(alert)).isEqualTo("acknowledged");
        assertThat(fixture.acknowledgedBy(alert)).isEqualTo("action:telegram:t");
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/botTOKEN/getUpdates"))
                .withRequestBody(matchingJsonPath("$.allowed_updates[0]", equalTo("callback_query")))
                .withRequestBody(matchingJsonPath("$.timeout", equalTo("0"))));
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/botTOKEN/answerCallbackQuery"))
                .withRequestBody(matchingJsonPath("$.callback_query_id", equalTo("cb500")))
                .withRequestBody(matchingJsonPath("$.text", equalTo("✅ Acknowledged"))));
        List<LoggedRequest> edits = WIREMOCK.findAll(postRequestedFor(urlEqualTo("/botTOKEN/editMessageText")));
        assertThat(edits).hasSize(1);
        JsonNode edit = JSON.readTree(edits.getFirst().getBodyAsString());
        assertThat(edit.get("message_id").asLong()).isEqualTo(77);
        assertThat(edit.get("text").asString()).contains("Acknowledged by Ana");
        assertThat(edit.at("/reply_markup/inline_keyboard")).isEmpty();
        assertThat(settings.get(TelegramCallbackPoller.offsetKey(channel), Long.class)).contains(501L);

        // The next poll sends the stored offset; a second press of the used button is refused politely.
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getUpdates")).willReturn(okJson(update(501, 42, data))));
        poller.pollOnce(channel, 0);
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/botTOKEN/getUpdates"))
                .withRequestBody(matchingJsonPath("$.offset", equalTo("501"))));
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/botTOKEN/answerCallbackQuery"))
                .withRequestBody(matchingJsonPath("$.callback_query_id", equalTo("cb501")))
                .withRequestBody(matchingJsonPath("$.show_alert", equalTo("true"))));
        assertThat(settings.get(TelegramCallbackPoller.offsetKey(channel), Long.class)).contains(502L);
    }

    @Test
    @DisplayName("a presser outside allowedUserIds is refused and the alert stays firing")
    void disallowedPress() throws Exception {
        UUID channel = channel("[42]");
        long alert = fixture.alert(fixture.rule("Quakes", T), "warning", T);
        String data = sendAlert(alert);
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getUpdates")).willReturn(okJson(update(7, 13, data))));
        poller.pollOnce(channel, 0);
        assertThat(fixture.state(alert)).isEqualTo("firing");
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/botTOKEN/answerCallbackQuery"))
                .withRequestBody(matchingJsonPath("$.show_alert", equalTo("true"))));
        WIREMOCK.verify(0, postRequestedFor(urlEqualTo("/botTOKEN/editMessageText")));
    }

    @Test
    @DisplayName("with no allowedUserIds any member of the channel's chat may press; a button from another chat may not")
    void chatMembers() throws Exception {
        UUID channel = channel("[]");
        long alert = fixture.alert(fixture.rule("Quakes", T), "warning", T);
        String data = sendAlert(alert);
        String otherChat = update(9, 99, data).replace("\"id\":-100", "\"id\":-555");
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getUpdates")).willReturn(okJson(otherChat)));
        poller.pollOnce(channel, 0);
        assertThat(fixture.state(alert)).isEqualTo("firing");
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getUpdates")).willReturn(okJson(update(10, 99, data))));
        poller.pollOnce(channel, 0);
        assertThat(fixture.state(alert)).isEqualTo("acknowledged");
    }

    @Test
    @DisplayName("updates that are not action buttons are skipped, and the offset still advances")
    void otherUpdates() throws Exception {
        UUID channel = channel("[]");
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getUpdates")).willReturn(okJson(
                "{\"ok\":true,\"result\":[{\"update_id\":3,\"callback_query\":{\"id\":\"c\",\"from\":{\"id\":1},"
                        + "\"data\":\"something-else\"}}]}")));
        assertThat(poller.pollOnce(channel, 0)).isEqualTo(1);
        assertThat(settings.get(TelegramCallbackPoller.offsetKey(channel), Long.class)).contains(4L);
        WIREMOCK.verify(0, postRequestedFor(urlEqualTo("/botTOKEN/answerCallbackQuery")));
    }
}
