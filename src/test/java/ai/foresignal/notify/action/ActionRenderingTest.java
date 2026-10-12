package ai.foresignal.notify.action;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.push.PushMessage;
import ai.foresignal.notify.push.PushPayloadBudget;
import ai.foresignal.notify.push.PushPayloads;
import ai.foresignal.notify.types.MatrixChannel;
import ai.foresignal.notify.types.NtfyChannel;
import ai.foresignal.notify.types.PushoverChannel;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.notify.types.WebhookChannel;
import ai.foresignal.store.NotificationChannelEntity;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * How each channel type renders action capabilities (prompt 033), pinned against exact expected
 * values: the ntfy {@code Actions} header, the Web Push {@code actions} and {@code confirmUrl}, the
 * Telegram keyboard (≤ 64-byte {@code callback_data}), the Matrix body links, the Pushover URL and the
 * webhook {@code actions} array.
 */
class ActionRenderingTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant EXPIRES = Instant.parse("2026-05-02T00:00:00Z");
    static final String ACK_TOKEN = "eac_AAAAAAAAAAAAAAAAAAAAAA.BBBBBBBBBBB";
    static final String SNOOZE_TOKEN = "eac_CCCCCCCCCCCCCCCCCCCCCC.DDDDDDDDDDD";
    static final String MUTE_TOKEN = "eac_EEEEEEEEEEEEEEEEEEEEEE.FFFFFFFFFFF";
    static final String BASE = "https://fs.example/v1/actions/";

    static final List<NotificationAction> ACTIONS = List.of(
            new NotificationAction("ack", "Acknowledge", BASE + ACK_TOKEN, ACK_TOKEN, null, EXPIRES),
            new NotificationAction("snooze", "Snooze 1h", BASE + SNOOZE_TOKEN, SNOOZE_TOKEN, "1h", EXPIRES),
            new NotificationAction("mute", "Mute 4h", BASE + MUTE_TOKEN, MUTE_TOKEN, "4h", EXPIRES));

    static ChannelSettings settings(Map<String, Object> config, Map<String, String> secrets) {
        return NotifyFixtures.settings(config, secrets).withActions(ACTIONS);
    }

    static JsonNode json(PreparedRequest request) {
        return JSON.readTree(((PreparedRequest.Bytes) request.body()).text());
    }

    @Test
    @DisplayName("ntfy: two one-tap http POST actions with clear=true, then view Open; no more than three")
    void ntfy() {
        PreparedRequest request = new NtfyChannel().render(NotifyFixtures.message(),
                settings(Map.of("topic", "alerts"), Map.of()));
        assertThat(request.headers().get("Actions")).isEqualTo(
                "http, Acknowledge, " + BASE + ACK_TOKEN + ", method=POST, headers.Accept=application/json, clear=true; "
                        + "http, Snooze 1h, " + BASE + SNOOZE_TOKEN
                        + ", method=POST, headers.Accept=application/json, clear=true; "
                        + "view, Open, " + NotifyFixtures.URL);
        assertThat(request.headers().get("Actions").split(";")).hasSize(3);
        PreparedRequest plain = new NtfyChannel().render(NotifyFixtures.message(),
                NotifyFixtures.settings(Map.of("topic", "alerts"), Map.of()));
        assertThat(plain.headers().get("Actions")).doesNotContain("http,");
    }

    @Test
    @DisplayName("Telegram: one row of buttons with callback_data ea:<token> (≤ 64 bytes); an edit without actions clears it")
    void telegram() {
        Map<String, String> secrets = Map.of("botToken", "123456:TESTTOKEN");
        JsonNode body = json(new TelegramChannel().render(NotifyFixtures.message(), settings(Map.of("chatId", "-1"),
                secrets)));
        JsonNode row = body.at("/reply_markup/inline_keyboard/0");
        assertThat(row).hasSize(3);
        assertThat(row.get(0).get("text").asString()).isEqualTo("Acknowledge");
        assertThat(row.get(0).get("callback_data").asString()).isEqualTo("ea:" + ACK_TOKEN);
        for (JsonNode button : row) {
            assertThat(button.get("callback_data").asString().getBytes(StandardCharsets.UTF_8).length)
                    .isBetween(1, 64);
        }
        JsonNode edit = json(new TelegramChannel().renderUpdate(NotifyFixtures.update("alert.acknowledged"), "77",
                NotifyFixtures.settings(Map.of("chatId", "-1"), secrets)).orElseThrow());
        assertThat(edit.at("/reply_markup/inline_keyboard").isArray()).isTrue();
        assertThat(edit.at("/reply_markup/inline_keyboard")).isEmpty();
        JsonNode none = json(new TelegramChannel().render(NotifyFixtures.message(),
                NotifyFixtures.settings(Map.of("chatId", "-1"), secrets)));
        assertThat(none.has("reply_markup")).isFalse();
    }

    @Test
    @DisplayName("Matrix and other button-less types: confirm-page links appended to the body")
    void links() {
        NotificationChannelEntity matrix = new NotificationChannelEntity(UUID.randomUUID(), "m", "matrix");
        NotificationMessage linked = ActionMinter.withLinks(NotifyFixtures.message(), matrix, ACTIONS);
        assertThat(linked.bodyMarkdown()).endsWith("[Acknowledge](" + BASE + ACK_TOKEN + ") · [Snooze 1h](" + BASE
                + SNOOZE_TOKEN + ") · [Mute 4h](" + BASE + MUTE_TOKEN + ")");
        JsonNode content = json(new MatrixChannel().render(linked, NotifyFixtures.settings(
                Map.of("homeserverUrl", "https://matrix.example.invalid", "roomId", "!r:example.invalid"),
                Map.of("accessToken", "syt"))));
        assertThat(content.get("formatted_body").asString()).contains("<a href=\"" + BASE + ACK_TOKEN + "\">Acknowledge</a>");

        NotificationChannelEntity slack = new NotificationChannelEntity(UUID.randomUUID(), "s", "slack");
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("actions", Map.of("enabled", true, "links", false));
        slack.setConfig(config);
        assertThat(ActionMinter.withLinks(NotifyFixtures.message(), slack, ACTIONS).bodyMarkdown())
                .isEqualTo(NotifyFixtures.message().bodyMarkdown());
        NotificationChannelEntity ntfy = new NotificationChannelEntity(UUID.randomUUID(), "n", "ntfy");
        assertThat(ActionMinter.withLinks(NotifyFixtures.message(), ntfy, ACTIONS).bodyMarkdown())
                .doesNotContain(ACK_TOKEN);
    }

    @Test
    @DisplayName("webhook: an actions array [{action, label, url, expiresAt}] in the default JSON body")
    void webhook() {
        JsonNode body = json(new WebhookChannel().render(NotifyFixtures.message(),
                settings(Map.of("url", "https://hook.example.invalid/x"), Map.of())));
        assertThat(body.get("actions")).hasSize(3);
        assertThat(body.at("/actions/0/action").asString()).isEqualTo("ack");
        assertThat(body.at("/actions/0/label").asString()).isEqualTo("Acknowledge");
        assertThat(body.at("/actions/0/url").asString()).isEqualTo(BASE + ACK_TOKEN);
        assertThat(body.at("/actions/0/expiresAt").asString()).isEqualTo(EXPIRES.toString());
        JsonNode plain = json(new WebhookChannel().render(NotifyFixtures.message(),
                NotifyFixtures.settings(Map.of("url", "https://hook.example.invalid/x"), Map.of())));
        assertThat(plain.has("actions")).isFalse();
    }

    @Test
    @DisplayName("Pushover: the supplementary URL is the acknowledge confirm page")
    void pushover() {
        PreparedRequest request = new PushoverChannel().render(NotifyFixtures.message("critical", "body"),
                settings(Map.of(), Map.of("appToken", "a", "userKey", "u")));
        Map<String, String> form = ((PreparedRequest.Form) request.body()).fields();
        assertThat(form.get("url")).isEqualTo(BASE + ACK_TOKEN);
        assertThat(form.get("url_title")).isEqualTo("Acknowledge");
        assertThat(form.get("priority")).isEqualTo("2");
    }

    @Test
    @DisplayName("Web Push: at most two actions {action, title, url} plus confirmUrl; declarative navigates to the confirm page")
    void webPush() {
        PushMessage message = new PushMessage("alert", 9L, 7L, null, "warning", "Quake", "Quake", "body",
                "https://fs.example/v1/alerts/7", null, null, List.of(), "foresignal-alert-7", null, false, 0L, null, null,
                null, PushMessage.ACTIONABLE_CATEGORY, List.of(
                        new PushMessage.Action("ack", "Acknowledge", BASE + ACK_TOKEN),
                        new PushMessage.Action("snooze", "Snooze 1h", BASE + SNOOZE_TOKEN),
                        new PushMessage.Action("mute", "Mute 4h", BASE + MUTE_TOKEN)), BASE + ACK_TOKEN);
        assertThat(message.actions()).hasSize(2);
        JsonNode payload = JSON.readTree(new PushPayloadBudget(JSON).fit(message, PushPayloads.FORESIGNAL, "en"));
        assertThat(payload.get("actions")).hasSize(2);
        assertThat(payload.at("/actions/0/action").asString()).isEqualTo("ack");
        assertThat(payload.at("/actions/0/title").asString()).isEqualTo("Acknowledge");
        assertThat(payload.at("/actions/1/url").asString()).isEqualTo(BASE + SNOOZE_TOKEN);
        assertThat(payload.get("confirmUrl").asString()).isEqualTo(BASE + ACK_TOKEN);
        JsonNode declarative = JSON.readTree(new PushPayloadBudget(JSON).fit(message, PushPayloads.DECLARATIVE, "en"));
        assertThat(declarative.at("/notification/navigate").asString()).isEqualTo(BASE + ACK_TOKEN);
        assertThat(declarative.at("/notification/data").has("actions")).isFalse();
        PushMessage roundTrip = PushMessage.fromMap(message.toMap());
        assertThat(roundTrip.actions()).isEqualTo(message.actions());
        assertThat(roundTrip.confirmUrl()).isEqualTo(message.confirmUrl());
    }

    @Test
    @DisplayName("ActionsConfig: on by default for ntfy, webpush, telegram, matrix and pushover; off for others; list filtered")
    void config() {
        assertThat(ActionsConfig.of("ntfy", Map.of()).enabled()).isTrue();
        assertThat(ActionsConfig.of("email", Map.of()).enabled()).isFalse();
        assertThat(ActionsConfig.of("email", Map.of("actions", Map.of("enabled", true))).enabled()).isTrue();
        assertThat(ActionsConfig.of("telegram", Map.of("actions", Map.of("enabled", false))).enabled()).isFalse();
        assertThat(ActionsConfig.of("ntfy", Map.of("actions", Map.of("list", List.of("snooze", "bogus", "ack", "ack"))))
                .list()).containsExactly("snooze", "ack");
        assertThat(ActionsConfig.of("matrix", Map.of()).reactions()).isFalse();
        assertThat(ActionsConfig.of("matrix", Map.of()).links()).isTrue();
    }
}
