package ai.foresignal.chatops;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.chatops.ChatopsItSupport;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/**
 * The Telegram connector against a WireMock Bot API (prompt 061): the backlog skipped on first start, the offset
 * persisted across a restart, a foreign webhook (degraded, or taken over), stale messages, {@code /cmd@Bot}, the
 * button flow (single use, expiry, the wrong user), silence towards unknown chats, and a refused token stopping
 * the connector with a system event.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class TelegramConnectorIT extends ChatopsItSupport {

    static final String AGENT_PRIVATE = "[{'chatId':'123','scope':'agent'}]";
    static final String AGENT_GROUP = "[{'chatId':'-100','scope':'agent','userIds':['42','43']}]";

    @Test
    @DisplayName("the first start stores the last update + 1 and replies to none of the backlog")
    void backlogSkipped() throws Exception {
        stubTelegramStart("");
        updates("[" + message(40, 123, 123, "/help", T) + "," + message(41, 123, 123, "/status", T) + "]");
        UUID channel = telegram(AGENT_PRIVATE, "");
        ChatSession session = session(channel);

        start(session);

        assertThat(cursors.get(channel)).contains("42");
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/botTOKEN/getUpdates"))
                .withRequestBody(matchingJsonPath("$.offset", equalTo("-1")))
                .withRequestBody(matchingJsonPath("$.timeout", equalTo("0"))));
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/botTOKEN/setMyCommands"))
                .withRequestBody(matchingJsonPath("$.scope.type", equalTo("chat")))
                .withRequestBody(matchingJsonPath("$.scope.chat_id", equalTo("123"))));
        assertThat(sent()).isEmpty();
        assertThat(((TelegramConnector) session.connector()).botUsername()).isEqualTo("FsBot");
    }

    @Test
    @DisplayName("the offset is stored after each update and used again after a restart")
    void offsetPersisted() throws Exception {
        stubTelegramStart("");
        updates("[]");
        UUID channel = telegram(AGENT_PRIVATE, "");
        ChatSession first = session(channel);
        start(first);
        assertThat(cursors.get(channel)).contains("0");

        updates("[" + message(50, 123, 123, "/help", T) + "]");
        assertThat(first.connector().poll()).isEqualTo(1);
        assertThat(cursors.get(channel)).contains("51");
        assertThat(sent()).hasSize(1);
        JsonNode reply = sent().getFirst();
        assertThat(reply.get("chat_id").asLong()).isEqualTo(123);
        assertThat(reply.get("parse_mode").asString()).isEqualTo("HTML");
        assertThat(reply.at("/reply_parameters/message_id").asLong()).isEqualTo(1050);
        assertThat(reply.get("text").asString()).contains("Foresignal commands");
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/botTOKEN/getUpdates"))
                .withRequestBody(matchingJsonPath("$.allowed_updates[0]", equalTo("message")))
                .withRequestBody(matchingJsonPath("$.allowed_updates[1]", equalTo("callback_query"))));

        // A restart: no offset=-1 skip again, and the stored offset is sent.
        WIREMOCK.resetRequests();
        updates("[]");
        ChatSession second = session(channel);
        start(second);
        second.connector().poll();
        WIREMOCK.verify(0, postRequestedFor(urlEqualTo("/botTOKEN/getUpdates"))
                .withRequestBody(matchingJsonPath("$.offset", equalTo("-1"))));
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/botTOKEN/getUpdates"))
                .withRequestBody(matchingJsonPath("$.offset", equalTo("51"))));
    }

    @Test
    @DisplayName("a foreign webhook blocks the connector unless takeOverWebhook deletes it; 409 blocks too")
    void webhook() throws Exception {
        stubTelegramStart("https://elsewhere.example/hook");
        updates("[]");
        UUID channel = telegram(AGENT_PRIVATE, "");
        ChatSession session = session(channel);
        assertThatThrownBy(() -> start(session)).isInstanceOf(ConnectorBlockedException.class)
                .hasMessage(TelegramConnector.WEBHOOK_REASON);
        WIREMOCK.verify(0, postRequestedFor(urlEqualTo("/botTOKEN/deleteWebhook")));

        UUID takeover = telegram(AGENT_PRIVATE, ",'takeOverWebhook':true");
        start(session(takeover));
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/botTOKEN/deleteWebhook")));

        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getUpdates")).willReturn(aResponse().withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"ok\":false,\"error_code\":409,\"description\":\"Conflict: can't use getUpdates method while webhook is active\"}")));
        ChatSession blocked = session(takeover);
        start(blocked);
        assertThatThrownBy(() -> blocked.connector().poll()).isInstanceOf(ConnectorBlockedException.class)
                .hasMessage(TelegramConnector.WEBHOOK_REASON);
    }

    @Test
    @DisplayName("messages older than two minutes are ignored")
    void staleIgnored() throws Exception {
        stubTelegramStart("");
        updates("[]");
        UUID channel = telegram(AGENT_PRIVATE, "");
        ChatSession session = session(channel);
        start(session);
        updates("[" + message(60, 123, 123, "/help", T.minus(Duration.ofMinutes(3))) + "]");
        assertThat(session.connector().poll()).isEqualTo(1);
        assertThat(sent()).isEmpty();
        assertThat(cursors.get(channel)).contains("61");
    }

    @Test
    @DisplayName("/cmd@FsBot is this bot's command; /cmd@OtherBot is ignored")
    void botSuffix() throws Exception {
        assertThat(TelegramConnector.normalize("/alerts@FsBot 3", "FsBot")).contains("/alerts 3");
        assertThat(TelegramConnector.normalize("/alerts@fsbot", "FsBot")).contains("/alerts");
        assertThat(TelegramConnector.normalize("/alerts@OtherBot", "FsBot")).isEmpty();
        assertThat(TelegramConnector.normalize("hello @FsBot", "FsBot")).contains("hello @FsBot");

        stubTelegramStart("");
        updates("[]");
        ChatSession session = session(telegram(AGENT_PRIVATE, ""));
        start(session);
        updates("[" + message(70, 123, 123, "/help@OtherBot", T) + "]");
        session.connector().poll();
        assertThat(sent()).isEmpty();
        updates("[" + message(71, 123, 123, "/help@FsBot", T) + "]");
        session.connector().poll();
        assertThat(sent()).hasSize(1);
    }

    @Test
    @DisplayName("a button acknowledges once, only for the user it was shown to, and expires after an hour")
    void callbackFlow() throws Exception {
        stubTelegramStart("");
        updates("[]");
        UUID channel = telegram(AGENT_GROUP, "");
        long alert = alerts.alert(alerts.rule("Quakes", T), "warning", T.minusSeconds(60));
        ChatSession session = session(channel);
        start(session);

        updates("[" + message(80, -100, 42, "/alerts", T) + "]");
        session.connector().poll();
        JsonNode listing = sent().getLast();
        assertThat(listing.get("text").asString()).contains(Long.toString(alert)).contains("Quakes");
        String ack = listing.at("/reply_markup/inline_keyboard/0/0/callback_data").asString();
        assertThat(listing.at("/reply_markup/inline_keyboard/0/0/text").asString()).isEqualTo("Ack " + alert);
        assertThat(ack).startsWith(PendingTokens.BUTTON_PREFIX);
        assertThat(ack.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);

        // Another allowed member presses it: refused, nothing changes.
        updates("[" + press(81, -100, 43, ack) + "]");
        session.connector().poll();
        assertThat(alerts.state(alert)).isEqualTo("firing");
        assertThat(answers().getLast().get("text").asString()).contains("someone else");

        // The user it was shown to: acknowledged as the chat principal.
        updates("[" + press(82, -100, 42, ack) + "]");
        session.connector().poll();
        assertThat(alerts.state(alert)).isEqualTo("acknowledged");
        assertThat(alerts.acknowledgedBy(alert)).isEqualTo("chat:" + channel + ":-100:42");
        assertThat(answers().getLast().get("callback_query_id").asString()).isEqualTo("cb82");

        // Single use.
        updates("[" + press(83, -100, 42, ack) + "]");
        session.connector().poll();
        assertThat(answers().getLast().get("text").asString()).contains("already used");

        // Expiry: a fresh button pressed after an hour.
        long second = alerts.alert(alerts.rule("Floods", T), "critical", T);
        updates("[" + message(84, -100, 42, "/alerts", T) + "]");
        session.connector().poll();
        String resolve = sent().getLast().at("/reply_markup/inline_keyboard/0/1/callback_data").asString();
        clock.setInstant(T.plus(Duration.ofMinutes(61)));
        updates("[" + press(85, -100, 42, resolve) + "]");
        session.connector().poll();
        assertThat(answers().getLast().get("text").asString()).contains("expired");
        assertThat(alerts.state(second)).isEqualTo("firing");
    }

    @Test
    @DisplayName("a chat that is not allowlisted gets no reply and changes nothing")
    void unknownChatSilent() throws Exception {
        stubTelegramStart("");
        updates("[]");
        UUID channel = telegram(AGENT_PRIVATE, "");
        long alert = alerts.alert(alerts.rule("Quakes", T), "warning", T);
        ChatSession session = session(channel);
        start(session);
        updates("[" + message(90, 999, 999, "/alerts", T) + "," + message(91, 999, 999, "/ack " + alert, T) + "]");
        assertThat(session.connector().poll()).isEqualTo(2);
        assertThat(sent()).isEmpty();
        assertThat(alerts.state(alert)).isEqualTo("firing");
    }

    @Test
    @DisplayName("401 from the Bot API stops the connector, sets failing and raises a system event")
    void unauthorizedStops() throws Exception {
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getUpdates")).willReturn(aResponse().withStatus(401)
                .withHeader("Content-Type", "application/json").withBody("{\"ok\":false,\"error_code\":401}")));
        stubTelegramStart("");
        UUID channel = telegram(AGENT_PRIVATE, "");
        ChatSession session = session(channel);
        cursors.put(channel, "5");
        start(session);
        assertThatThrownBy(() -> session.connector().poll()).isInstanceOf(ChatAuthException.class);

        // The supervisor: getMe refused.
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN/getMe")).willReturn(aResponse().withStatus(401)
                .withHeader("Content-Type", "application/json").withBody("{\"ok\":false,\"error_code\":401}")));
        try {
            manager.sync(channel);
            Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                Map<String, Object> status = manager.status(channelRows.findById(channel).orElseThrow());
                assertThat(status.get("status")).isEqualTo(HealthStatus.FAILING.value());
                assertThat(status.get("connected")).isEqualTo(false);
            });
            Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(jdbc.queryForObject(
                    "select count(*) from notification where subtype = ?", Long.class, ChatopsManager.AUTH_FAILED_EVENT))
                    .isEqualTo(1));
            List<String> errors = jdbc.queryForList("select title from notification where subtype = ?", String.class,
                    ChatopsManager.AUTH_FAILED_EVENT);
            assertThat(errors.getFirst()).contains("ops").doesNotContain("TOKEN");
        } finally {
            channels.delete(channel);
            manager.sync(channel);
        }
    }
}
