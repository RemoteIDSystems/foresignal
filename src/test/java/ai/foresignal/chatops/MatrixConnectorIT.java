package ai.foresignal.chatops;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.chatops.ChatopsItSupport;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/**
 * The Matrix connector against a WireMock homeserver (prompt 061): {@code since} persisted, the first sync
 * discarded, the bot's own events and edits ignored, an encrypted room (and a crowded room without
 * {@code userIds}) ignored with health {@code degraded}, the rich-reply shape, and {@code /confirm} codes.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class MatrixConnectorIT extends ChatopsItSupport {

    static final String ROOM = "!ops:example.org";
    static final String BOT = "@fsbot:example.org";
    static final String SYNC = "/_matrix/client/v3/sync";

    @BeforeEach
    void stubs() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/_matrix/client/v3/account/whoami"))
                .willReturn(okJson("{\"user_id\":\"" + BOT + "\"}")));
        WIREMOCK.stubFor(get(urlPathMatching("/_matrix/client/v3/rooms/.*/state/m.room.encryption"))
                .willReturn(aResponse().withStatus(404).withHeader("Content-Type", "application/json")
                        .withBody("{\"errcode\":\"M_NOT_FOUND\"}")));
        WIREMOCK.stubFor(get(urlPathMatching("/_matrix/client/v3/rooms/.*/joined_members"))
                .willReturn(okJson("{\"joined\":{\"" + BOT + "\":{},\"@ana:example.org\":{}}}")));
        WIREMOCK.stubFor(put(urlPathMatching("/_matrix/client/v3/rooms/.*/send/m.room.message/.*"))
                .willReturn(okJson("{\"event_id\":\"$reply\"}")));
        sync(null, "s1", event("$old", "@ana:example.org", "/help", false));
    }

    static void sync(String since, String next, String... events) {
        WIREMOCK.stubFor(get(urlPathEqualTo(SYNC)).withQueryParam("since", since == null ? absent() : equalTo(since))
                .willReturn(okJson("{\"next_batch\":\"" + next + "\",\"rooms\":{\"join\":{\"" + ROOM
                        + "\":{\"timeline\":{\"events\":[" + String.join(",", events) + "]}}}}}")));
    }

    static String event(String id, String sender, String body, boolean edit) {
        String relates = edit ? ",\"m.new_content\":{\"msgtype\":\"m.text\",\"body\":\"" + body
                + "\"},\"m.relates_to\":{\"rel_type\":\"m.replace\",\"event_id\":\"$x\"}" : "";
        return "{\"type\":\"m.room.message\",\"event_id\":\"" + id + "\",\"sender\":\"" + sender
                + "\",\"origin_server_ts\":" + T.toEpochMilli() + ",\"content\":{\"msgtype\":\"m.text\",\"body\":"
                + JSON.writeValueAsString(body) + relates + "}}";
    }

    static List<JsonNode> replies() {
        return WIREMOCK.findAll(putRequestedFor(urlPathMatching("/_matrix/client/v3/rooms/.*/send/m.room.message/.*")))
                .stream().map(r -> JSON.readTree(r.getBodyAsString())).toList();
    }

    @Test
    @DisplayName("the first sync is discarded, next_batch is stored and sent as since after a restart")
    void sincePersisted() throws Exception {
        UUID channel = matrix("[{'chatId':'" + ROOM + "','scope':'agent'}]", "");
        ChatSession session = session(channel);
        start(session);
        assertThat(cursors.get(channel)).contains("s1");
        assertThat(replies()).isEmpty();

        sync("s1", "s2", event("$e1", "@ana:example.org", "/help", false));
        assertThat(session.connector().poll()).isEqualTo(1);
        assertThat(cursors.get(channel)).contains("s2");
        assertThat(replies()).hasSize(1);
        LoggedRequest syncRequest = WIREMOCK.findAll(getRequestedFor(urlPathEqualTo(SYNC))).getLast();
        JsonNode filter = JSON.readTree(syncRequest.queryParameter("filter").firstValue());
        assertThat(filter.at("/room/rooms/0").asString()).isEqualTo(ROOM);
        assertThat(filter.at("/room/timeline/types/0").asString()).isEqualTo("m.room.message");
        assertThat(filter.at("/room/timeline/limit").asInt()).isEqualTo(20);
        assertThat(filter.at("/room/state/lazy_load_members").asBoolean()).isTrue();
        assertThat(filter.at("/presence/types")).isEmpty();
        assertThat(syncRequest.queryParameter("timeout").firstValue()).isNotEqualTo("0");

        WIREMOCK.resetRequests();
        sync("s2", "s3");
        ChatSession restarted = session(channel);
        start(restarted);
        restarted.connector().poll();
        assertThat(WIREMOCK.findAll(getRequestedFor(urlPathEqualTo(SYNC)))).allSatisfy(r ->
                assertThat(r.queryParameter("since").firstValue()).isEqualTo("s2"));
        assertThat(cursors.get(channel)).contains("s3");
    }

    @Test
    @DisplayName("the bot's own events and edits are ignored")
    void ownEventsAndEdits() throws Exception {
        UUID channel = matrix("[{'chatId':'" + ROOM + "','scope':'agent'}]", "");
        ChatSession session = session(channel);
        start(session);
        sync("s1", "s2", event("$mine", BOT, "/help", false), event("$edit", "@ana:example.org", "/help", true));
        assertThat(session.connector().poll()).isZero();
        assertThat(replies()).isEmpty();
    }

    @Test
    @DisplayName("an encrypted room is ignored and the connector is degraded; so is a crowded room without userIds")
    void encryptedRoom() throws Exception {
        WIREMOCK.stubFor(get(urlPathMatching("/_matrix/client/v3/rooms/.*/state/m.room.encryption"))
                .willReturn(okJson("{\"algorithm\":\"m.megolm.v1.aes-sha2\"}")));
        UUID channel = matrix("[{'chatId':'" + ROOM + "','scope':'agent'}]", "");
        ChatSession session = session(channel);
        start(session);
        assertThat(session.connector().state().active(ROOM)).isFalse();
        assertThat(session.connector().state().status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(session.connector().state().statusReason()).isEqualTo(MatrixConnector.ENCRYPTED_REASON);
        sync("s1", "s2", event("$e", "@ana:example.org", "/help", false));
        session.connector().poll();
        assertThat(replies()).isEmpty();

        WIREMOCK.stubFor(get(urlPathMatching("/_matrix/client/v3/rooms/.*/state/m.room.encryption"))
                .willReturn(aResponse().withStatus(404).withBody("{}")));
        WIREMOCK.stubFor(get(urlPathMatching("/_matrix/client/v3/rooms/.*/joined_members"))
                .willReturn(okJson("{\"joined\":{\"" + BOT + "\":{},\"@a:x\":{},\"@b:x\":{}}}")));
        ChatSession crowded = session(matrix("[{'chatId':'" + ROOM + "','scope':'read'}]", ""));
        start(crowded);
        assertThat(crowded.connector().state().inactiveChats()).containsEntry(ROOM, MatrixConnector.MEMBERS_REASON);
    }

    @Test
    @DisplayName("replies are rich replies with formatted_body and a deterministic transaction id")
    void richReply() throws Exception {
        UUID channel = matrix("[{'chatId':'" + ROOM + "','scope':'agent'}]", "");
        ChatSession session = session(channel);
        start(session);
        sync("s1", "s2", event("$cmd", "@ana:example.org", "/help", false));
        session.connector().poll();
        JsonNode reply = replies().getFirst();
        assertThat(reply.get("msgtype").asString()).isEqualTo("m.text");
        assertThat(reply.get("format").asString()).isEqualTo("org.matrix.custom.html");
        assertThat(reply.get("formatted_body").asString()).contains("<strong>Foresignal commands</strong>");
        assertThat(reply.get("body").asString()).contains("Foresignal commands").doesNotContain("**");
        assertThat(reply.at("/m.relates_to/m.in_reply_to/event_id").asString()).isEqualTo("$cmd");
        String path = WIREMOCK.findAll(putRequestedFor(urlPathMatching(".*/send/m.room.message/.*"))).getFirst().getUrl();
        String txn = path.substring(path.lastIndexOf('/') + 1);
        assertThat(txn).startsWith("fscc").hasSize(36);
        assertThat(MatrixConnector.txnId(channel.toString(), ROOM, "$cmd", "x", 0))
                .isEqualTo(MatrixConnector.txnId(channel.toString(), ROOM, "$cmd", "x", 0));
        assertThat(MatrixConnector.content(ChatReply.text("**a**", null))).doesNotContainKey("m.relates_to");
    }

    @Test
    @DisplayName("/confirm codes are bound to the user, case-insensitive and single-use")
    void confirmCodes() throws Exception {
        UUID channel = matrix("[{'chatId':'" + ROOM + "','scope':'agent','userIds':['@ana:example.org','@bob:example.org']}]", "");
        ChatSession session = session(channel);
        start(session);
        PendingTokens tokens = new PendingTokens(jdbc, clock);
        String code = tokens.code(new ChatRef(channel, ROOM, "@ana:example.org", null, null), PendingTokens.ASK_DECIDE,
                UUID.randomUUID().toString());
        assertThat(code).hasSize(PendingTokens.CODE_LENGTH).matches("[" + PendingTokens.CODE_ALPHABET + "]+");

        sync("s1", "s2", event("$b", "@bob:example.org", "/confirm " + code, false));
        session.connector().poll();
        assertThat(replies().getLast().get("body").asString()).contains("someone else");

        sync("s2", "s3", event("$a", "@ana:example.org", "/confirm " + code.toLowerCase(java.util.Locale.ROOT), false));
        session.connector().poll();
        assertThat(replies().getLast().get("body").asString()).doesNotContain("someone else").doesNotContain("already used");

        sync("s3", "s4", event("$a2", "@ana:example.org", "/reject " + code, false));
        session.connector().poll();
        assertThat(replies().getLast().get("body").asString()).contains("already used");
        Map<String, Object> row = jdbc.queryForMap("select used_at from chatops_pending where token = ?", code);
        assertThat(row.get("used_at")).isNotNull();
    }
}
