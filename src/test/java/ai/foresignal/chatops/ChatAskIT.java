package ai.foresignal.chatops;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.chatops.RecordingConnector;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

/**
 * {@code /ask} from a chat with a scripted fake provider (prompt 061): a cited answer with record links, proposed
 * actions as Confirm/Reject buttons (Telegram) or codes (Matrix) only for agent chats, the per-chat Ask limit, and
 * the 039 rule that private records never reach a non-local provider, even for a chat with {@code privateAccess}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(AskTestSources.class)
class ChatAskIT extends AskItSupport {

    static final String Q1 = AskTestSources.QUAKES + ":q1";
    static final String WATCH_RULE = "{\"rule\":{\"name\":\"Big quakes\",\"sources\":[\"" + AskTestSources.QUAKES
            + "\"],\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":6}]},\"reason\":\"You asked.\"}";

    @Autowired CommandRouter router;
    @Autowired ChatRateLimiter chatLimiter;

    final UUID channel = UUID.randomUUID();

    @BeforeEach
    void resetLimits() {
        chatLimiter.reset();
    }

    ChatSession session(RecordingConnector connector, int askPerHour, AllowedChat chat) {
        ChannelSettings s = new ChannelSettings(channel, "ops", Map.of(), Map.of(), ZoneOffset.UTC, 0, T, "x", "ua");
        return new ChatSession(new ChannelConfig(channel, "ops", connector.supportsButtons() ? "telegram" : "matrix", s,
                new InteractiveConfig(true, List.of(chat), askPerHour, 30, false, false)), connector);
    }

    static AllowedChat chat(String id, Scope scope, boolean privateAccess) {
        return new AllowedChat(id, null, scope, List.of(), privateAccess);
    }

    ChatOutcome ask(ChatSession session, String text) {
        String chatId = session.config().interactive().allowedChats().getFirst().chatId();
        return router.onMessage(session, new ChatRef(channel, chatId, chatId, "Ana", "10"), text).orElseThrow();
    }

    long watches() {
        Long n = jdbc.queryForObject("SELECT count(*) FROM trigger_rule", Long.class);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("the answer comes back with its citations as record links")
    void citedAnswer() {
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        chat().then(toolUse(call("c1", "query_records", "{\"kind\":[\"" + AskTestSources.QUAKE_KIND + "\"]}")))
                .then(text("A quake hit Tokyo Bay [" + Q1 + "]."));
        RecordingConnector connector = new RecordingConnector(true);

        ChatOutcome outcome = ask(session(connector, 10, chat("1", Scope.READ, false)), "/ask Any quakes near Tokyo?");

        assertThat(outcome.result()).isEqualTo(ChatOutcome.OK);
        String reply = connector.lastText();
        assertThat(reply).contains("A quake hit Tokyo Bay").contains("**Sources**")
                .contains("[Tokyo Bay](").contains("/v1/records/" + AskTestSources.QUAKES + "/q1");
        assertThat(chat().requests()).isNotEmpty();
        // The same chat continues its thread; /ask new starts another.
        chat().then(text("Still quiet."));
        ask(session(connector, 10, chat("1", Scope.READ, false)), "/ask And now?");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ask_thread", Long.class)).isEqualTo(1);
        ask(session(connector, 10, chat("1", Scope.READ, false)), "/ask new");
        chat().then(text("Fresh."));
        ask(session(connector, 10, chat("1", Scope.READ, false)), "/ask Hello again");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ask_thread", Long.class)).isEqualTo(2);
    }

    @Test
    @DisplayName("proposed actions get Confirm/Reject buttons only in agent chats; a press executes the action")
    void proposedActionButtons() {
        RecordingConnector readConnector = new RecordingConnector(true);
        chat().then(toolUse(call("p1", "propose_watch", WATCH_RULE))).then(text("I proposed a watch."));
        ask(session(readConnector, 10, chat("1", Scope.READ, false)), "/ask Tell me about big quakes");
        assertThat(readConnector.lastText()).contains("Big quakes").contains("needs an agent chat");
        assertThat(readConnector.replies().getLast().reply().buttons()).isEmpty();

        RecordingConnector agentConnector = new RecordingConnector(true);
        ChatSession agentSession = session(agentConnector, 10, chat("2", Scope.AGENT, false));
        chat().then(toolUse(call("p1", "propose_watch", WATCH_RULE))).then(text("I proposed a watch."));
        ask(agentSession, "/ask Tell me about big quakes");
        List<ChatButton> row = agentConnector.replies().getLast().reply().buttons().getFirst();
        assertThat(row).extracting(ChatButton::label).containsExactly("Confirm", "Reject");
        assertThat(watches()).isZero();

        ChatOutcome confirmed = router.onCallback(agentSession, new ChatRef(channel, "2", "2", "Ana", "11"), "cb1",
                row.getFirst().token()).orElseThrow();
        assertThat(confirmed.result()).isEqualTo(ChatOutcome.OK);
        assertThat(confirmed.reply().markdown()).contains("executed");
        assertThat(watches()).isEqualTo(1);
        assertThat(agentConnector.answers()).hasSize(1);
    }

    @Test
    @DisplayName("Matrix agent chats confirm with a code")
    void matrixCodes() {
        RecordingConnector connector = new RecordingConnector(false);
        ChatSession session = session(connector, 10, chat("!room:example.org", Scope.AGENT, false));
        chat().then(toolUse(call("p1", "propose_watch", WATCH_RULE))).then(text("I proposed a watch."));
        ask(session, "/ask Tell me about big quakes");
        Matcher code = Pattern.compile("/confirm ([A-Z0-9]{6})").matcher(connector.lastText());
        assertThat(code.find()).isTrue();

        ChatOutcome outcome = ask(session, "/confirm " + code.group(1));

        assertThat(outcome.result()).isEqualTo(ChatOutcome.OK);
        assertThat(watches()).isEqualTo(1);
    }

    @Test
    @DisplayName("the per-chat Ask limit refuses the next question with a retry time")
    void askRateLimit() {
        RecordingConnector connector = new RecordingConnector(true);
        ChatSession session = session(connector, 1, chat("3", Scope.READ, false));
        chat().then(text("No data."));
        assertThat(ask(session, "/ask first").result()).isEqualTo(ChatOutcome.OK);
        ChatOutcome limited = ask(session, "/ask second");
        assertThat(limited.result()).isEqualTo(ChatOutcome.RATE_LIMITED);
        assertThat(limited.reply().markdown()).startsWith("Ask is rate limited, retry in");
        assertThat(chat().requests()).hasSize(1);
    }

    @Test
    @DisplayName("a private record never reaches a non-local provider, even from a chat with privateAccess")
    void privateEgress() {
        secret("s1", "Hidden Base", 10.0, 50.0);
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        chat().then(toolUse(call("c1", "query_records", "{}"),
                        call("c2", "get_record", "{\"source\":\"" + AskTestSources.SECRET + "\",\"externalId\":\"s1\"}")))
                .then(text("Tokyo Bay only [" + Q1 + "]."));
        RecordingConnector connector = new RecordingConnector(true);

        ask(session(connector, 10, chat("4", Scope.AGENT, true)), "/ask Everything you know?");

        assertThat(everythingSent(chat().requests())).doesNotContain("Hidden Base");
        assertThat(connector.lastText()).doesNotContain("Hidden Base");
    }
}
