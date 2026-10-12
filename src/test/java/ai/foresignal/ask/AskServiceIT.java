package ai.foresignal.ask;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.StopReason;
import ai.foresignal.llm.client.TokenUsage;
import ai.foresignal.llm.client.ToolResult;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

/**
 * {@code ask} end to end over HTTP with a scripted fake provider (prompt 028): verified citations with
 * persisted seqs, dropped citations, scope injection, retrieval-only mode, thread follow-ups replaying
 * raw turns unchanged, refusals, budgets, configuration, authentication and private-source egress.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(AskTestSources.class)
class AskServiceIT extends AskItSupport {

    static final String QUAKES = AskTestSources.QUAKES;
    static final String Q1 = QUAKES + ":q1";

    @Autowired MockMvc mvc;

    JsonNode ask(String bearer, String body, int expectedStatus) throws Exception {
        var request = post("/v1/ask").contentType(MediaType.APPLICATION_JSON).content(body);
        if (bearer != null) {
            request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        MvcResult result = mvc.perform(request).andReturn();
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(result.getResponse().getStatus()).as(content).isEqualTo(expectedStatus);
        return content.isEmpty() ? null : JSON.readTree(content);
    }

    JsonNode ask(String bearer, String body) throws Exception {
        return ask(bearer, body, 200);
    }

    static String question(String text) {
        return "{\"question\":\"" + text + "\"}";
    }

    /** The {@code seq} of item {@code id} in the last tool result of {@code request}. */
    static long seqOf(ChatRequest request, String id) {
        for (JsonNode item : lastData(request).path("items")) {
            if (id.equals(item.path("id").asString())) {
                return item.get("seq").asLong();
            }
        }
        throw new AssertionError("no item " + id + " in " + lastData(request));
    }

    @Test
    @DisplayName("tools mode: query_records, then an answer whose citation carries the persisted seq")
    void toolsModeVerifiedCitation() throws Exception {
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        long seq = headSeq(QUAKES, "q1");
        chat().then(toolUse(call("c1", "query_records", "{\"kind\":[\"" + AskTestSources.QUAKE_KIND + "\"]}")))
                .then(request -> text("A quake hit Tokyo Bay [" + Q1 + "@" + seqOf(request, Q1) + "]."));

        JsonNode response = ask(reader, question("Any quakes near Tokyo?"));

        assertThat(response.get("mode").asString()).isEqualTo("tools");
        assertThat(response.get("citations")).hasSize(1);
        JsonNode citation = response.get("citations").get(0);
        assertThat(citation.get("id").asString()).isEqualTo(Q1);
        assertThat(citation.get("seq").asLong()).isEqualTo(seq);
        assertThat(citation.get("title").asString()).isEqualTo("Tokyo Bay");
        assertThat(citation.get("type").asString()).isEqualTo("record");
        assertThat(response.get("warnings").toString()).doesNotContain("dropped").doesNotContain("uncited");
        assertThat(response.at("/usage/toolCalls").asInt()).isEqualTo(1);
        assertThat(response.at("/usage/provider").asString()).isEqualTo(CHAT);
        assertThat(response.get("stopReason").asString()).isEqualTo("end");

        List<ChatRequest> requests = chat().requests();
        assertThat(requests).hasSize(2);
        ChatRequest first = requests.getFirst();
        assertThat(first.system()).doesNotContain("2026-10-12");
        assertThat(((ChatMessage.User) first.messages().getLast()).text()).contains("Today is 2026-10-12");
        assertThat(first.tools()).extracting(t -> t.name())
                .contains("query_records", "get_record", "list_alerts", "propose_watch", "propose_ack",
                        "propose_alert_status", "propose_briefing", "list_briefings", "get_briefing_run", "get_digest")
                .doesNotContain("create_watch", "delete_watch", "acknowledge_alert", "set_alert_status",
                        "update_watch", "create_briefing", "run_briefing");
        ToolResult result = toolResults(requests.get(1)).getFirst();
        assertThat(JSON.readTree(result.content()).get("tool").asString()).isEqualTo("query_records");
    }

    @Test
    @DisplayName("a citation with a wrong @seq and an invented id are dropped with warnings; a bare id resolves")
    void droppedAndResolvedCitations() throws Exception {
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        long seq = headSeq(QUAKES, "q1");
        chat().then(toolUse(call("c1", "query_records", "{}")))
                .then(text("Tokyo Bay shook [" + Q1 + "@999999]. Another one [" + QUAKES + ":nope]. And the bay ["
                        + Q1 + "]."));

        JsonNode response = ask(reader, question("What happened?"));

        assertThat(response.get("warnings").toString())
                .contains("dropped citation [" + Q1 + "@999999]: not returned by any tool")
                .contains("dropped citation [" + QUAKES + ":nope]: not returned by any tool");
        assertThat(response.get("citations")).hasSize(1);
        assertThat(response.get("citations").get(0).get("seq").asLong()).isEqualTo(seq);
    }

    @Test
    @DisplayName("an answer citing nothing after tools returned data is flagged, with its uncited claims")
    void noCitations() throws Exception {
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        chat().then(toolUse(call("c1", "query_records", "{}"))).then(text("A magnitude 5.5 quake hit the bay."));

        JsonNode response = ask(reader, question("What happened?"));

        assertThat(response.get("warnings").toString()).contains("answer contains no citations")
                .contains("uncited claim: A magnitude 5.5 quake hit the bay.");
        JsonNode thread = JSON.readTree(mvc.perform(get("/v1/ask/threads/" + response.get("threadId").asString())
                .header(HttpHeaders.AUTHORIZATION, reader)).andReturn().getResponse().getContentAsString());
        assertThat(thread.at("/messages/1/warnings").toString()).contains("answer contains no citations");
    }

    @Test
    @DisplayName("scope injection: the model cannot widen the bbox, and a disjoint box is refused")
    void scopeInjection() throws Exception {
        quake("tokyo", "Tokyo Bay", 5.5, 139.8, 35.6);
        quake("sf", "San Francisco", 4.0, -122.4, 37.8);
        chat().then(toolUse(call("c1", "query_records", "{\"bbox\":\"-180,-80,180,80\"}")))
                .then(toolUse(call("c2", "query_records", "{\"bbox\":\"-130,30,-110,40\"}")))
                .then(text("Only Tokyo [" + QUAKES + ":tokyo]."));

        JsonNode response = ask(reader, "{\"question\":\"Quakes?\",\"scope\":{\"bbox\":\"130,30,150,40\"}}");

        List<ChatRequest> requests = chat().requests();
        String widened = toolResults(requests.get(1)).getFirst().content();
        assertThat(widened).contains(QUAKES + ":tokyo").doesNotContain("San Francisco");
        ToolResult disjoint = toolResults(requests.get(2)).getLast();
        assertThat(disjoint.isError()).isTrue();
        assertThat(disjoint.content()).contains("outside");
        assertThat(((ChatMessage.User) requests.getFirst().messages().getLast()).text()).contains("130,30,150,40");
        assertThat(response.get("citations")).hasSize(1);
    }

    @Test
    @DisplayName("a scope source limits every tool call; a source outside the scope is refused")
    void scopeSources() throws Exception {
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        chat().then(toolUse(call("c1", "query_records", "{\"source\":[\"test-standard\"]}")))
                .then(text("Nothing."));

        ask(reader, "{\"question\":\"Quakes?\",\"scope\":{\"sources\":[\"" + QUAKES + "\"]}}");

        ToolResult refused = toolResults(chat().requests().get(1)).getFirst();
        assertThat(refused.isError()).isTrue();
        assertThat(refused.content()).contains("limited to sources");
    }

    @Test
    @DisplayName("retrieval-only mode: hybrid search plus recent alerts in one completion, same citation rules")
    void retrievalMode() throws Exception {
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        long seq = headSeq(QUAKES, "q1");
        health.supportsTools(CHAT, false);
        chat().then(text("Tokyo Bay had a quake [" + Q1 + "]."));

        JsonNode response = ask(reader, question("Tokyo"));

        assertThat(response.get("mode").asString()).isEqualTo("retrieval");
        assertThat(response.get("citations").get(0).get("seq").asLong()).isEqualTo(seq);
        ChatRequest request = chat().requests().getFirst();
        assertThat(request.tools()).isEmpty();
        String user = ((ChatMessage.User) request.messages().getLast()).text();
        assertThat(user).contains("{\"tool\":\"retrieval\"").contains("Tokyo Bay").contains("Today is");
    }

    @Test
    @DisplayName("a follow-up replays the stored raw turns byte for byte and appends the new question")
    void followUpReplaysRawTurns() throws Exception {
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        JsonNode rawToolTurn = JSON.readTree("{\"role\":\"assistant\",\"z\":1,\"a\":[3,2,1],"
                + "\"content\":[{\"type\":\"thinking\",\"signature\":\"c2ln==\"}]}");
        chat().then(new ChatResponse("Looking.", List.of(call("c1", "query_records", "{}")), StopReason.TOOL_USE, null,
                        new TokenUsage(10, 5, 0), "fake-chat", rawToolTurn))
                .then(new ChatResponse("Tokyo Bay [" + Q1 + "].", List.of(), StopReason.END, null,
                        new TokenUsage(10, 5, 0), "fake-chat", "final-raw-turn"));
        JsonNode first = ask(reader, question("Quakes?"));
        List<ChatMessage> firstConversation = chat().requests().get(1).messages();

        chat().then(text("Still Tokyo Bay [" + Q1 + "]."));
        ask(reader, "{\"question\":\"And now?\",\"threadId\":\"" + first.get("threadId").asString() + "\"}");

        ChatRequest followUp = chat().requests().get(2);
        List<ChatMessage> replayed = followUp.messages();
        assertThat(replayed).hasSize(firstConversation.size() + 2);
        for (int i = 0; i < firstConversation.size(); i++) {
            assertSameTurn(replayed.get(i), firstConversation.get(i));
        }
        ChatMessage.Assistant finalTurn = (ChatMessage.Assistant) replayed.get(firstConversation.size());
        assertThat(finalTurn.rawTurn()).isEqualTo("final-raw-turn");
        ChatMessage.Assistant toolTurn = (ChatMessage.Assistant) replayed.get(1);
        assertThat(toolTurn.rawTurn().toString()).isEqualTo(rawToolTurn.toString());
        assertThat(((ChatMessage.User) replayed.getLast()).text()).startsWith("And now?");
    }

    private static void assertSameTurn(ChatMessage actual, ChatMessage expected) {
        assertThat(actual.getClass()).isEqualTo(expected.getClass());
        switch (expected) {
            case ChatMessage.User user -> assertThat(((ChatMessage.User) actual).text()).isEqualTo(user.text());
            case ChatMessage.Assistant assistant -> {
                ChatMessage.Assistant a = (ChatMessage.Assistant) actual;
                assertThat(String.valueOf(a.rawTurn())).isEqualTo(String.valueOf(assistant.rawTurn()));
                assertThat(a.text()).isEqualTo(assistant.text());
                assertThat(a.toolCalls()).hasSameSizeAs(assistant.toolCalls());
            }
            case ChatMessage.ToolResults results -> assertThat(((ChatMessage.ToolResults) actual).results())
                    .isEqualTo(results.results());
        }
    }

    @Test
    @DisplayName("a refusal is answered (200) with the category and a warning")
    void refusal() throws Exception {
        chat().then(FakeLlmClient.refusal("cyber"));

        JsonNode response = ask(reader, question("Something the model declines"));

        assertThat(response.get("stopReason").asString()).isEqualTo("refusal");
        assertThat(response.get("refusalCategory").asString()).isEqualTo("cyber");
        assertThat(response.get("answer").asString()).contains("declined").contains("cyber");
        assertThat(response.get("warnings").toString()).contains("the model refused to answer (category cyber)");
    }

    @Test
    @DisplayName("a spent daily budget is 429 LLM_BUDGET_EXCEEDED and stores nothing")
    void budget() throws Exception {
        provider("tiny", "openai-compatible", 1_000L);
        assign("tiny");

        JsonNode problem = ask(reader, question("Anything?"), 429);

        assertThat(problem.get("code").asString()).isEqualTo("LLM_BUDGET_EXCEEDED");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ask_thread", Long.class)).isZero();
    }

    @Test
    @DisplayName("no chat provider, or Ask disabled, is 409 LLM_NOT_CONFIGURED")
    void notConfigured() throws Exception {
        llmSettings.update(new LlmSettings(null, null, true, true, false, 200_000, false));
        assertThat(ask(reader, question("Anything?"), 409).get("code").asString()).isEqualTo("LLM_NOT_CONFIGURED");

        llmSettings.update(new LlmSettings(CHAT, null, false, true, false, 200_000, false));
        assertThat(ask(reader, question("Anything?"), 409).get("code").asString()).isEqualTo("LLM_NOT_CONFIGURED");
        assertThat(chat().requests()).isEmpty();
    }

    @Test
    @DisplayName("Ask is always authenticated: 401 without a token even while open reads are on")
    void alwaysAuthenticated() throws Exception {
        ask(null, question("Anything?"), 401);
        assertThat(mvc.perform(get("/v1/ask/threads")).andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(chat().requests()).isEmpty();
    }

    @Test
    @DisplayName("a private-source record never reaches a non-local provider, even for an agent token; a local one gets it")
    void privateRecordsStayLocal() throws Exception {
        secret("s1", "Hidden Base", 10.0, 50.0);
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        chat().then(toolUse(call("c1", "query_records", "{}"),
                        call("c2", "get_record", "{\"source\":\"" + AskTestSources.SECRET + "\",\"externalId\":\"s1\"}"),
                        call("c3", "query_records", "{\"q\":\"hidden\"}")))
                .then(text("Tokyo Bay only [" + Q1 + "]."));

        ask(agent, question("Everything you know?"));

        assertThat(everythingSent(chat().requests())).doesNotContain("Hidden Base");

        provider(LOCAL, "ollama", 1_000_000L);
        assign(LOCAL);
        fakes.client(LOCAL).then(toolUse(call("c1", "query_records", "{}"))).then(text("Done."));
        ask(agent, question("Everything you know?"));
        assertThat(everythingSent(fakes.client(LOCAL).requests())).contains("Hidden Base");
    }

    @Test
    @DisplayName("threads: a token lists and reads its own; another token gets 404; admin sees all")
    void threadVisibility() throws Exception {
        chat().then(text("No data."));
        JsonNode response = ask(reader, question("First question about quakes"));
        String threadId = response.get("threadId").asString();

        JsonNode page = JSON.readTree(mvc.perform(get("/v1/ask/threads").header(HttpHeaders.AUTHORIZATION, reader))
                .andReturn().getResponse().getContentAsString());
        assertThat(page.get("items")).hasSize(1);
        assertThat(page.at("/items/0/title").asString()).isEqualTo("First question about quakes");
        assertThat(page.at("/items/0/messageCount").asInt()).isEqualTo(2);

        assertThat(mvc.perform(get("/v1/ask/threads/" + threadId).header(HttpHeaders.AUTHORIZATION, otherAgent))
                .andReturn().getResponse().getStatus()).isEqualTo(404);
        JsonNode other = JSON.readTree(mvc.perform(get("/v1/ask/threads").header(HttpHeaders.AUTHORIZATION, otherAgent))
                .andReturn().getResponse().getContentAsString());
        assertThat(other.get("items")).isEmpty();
        assertThat(mvc.perform(get("/v1/ask/threads/" + threadId).header(HttpHeaders.AUTHORIZATION, admin))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
        ask(otherAgent, "{\"question\":\"x\",\"threadId\":\"" + threadId + "\"}", 404);
    }

    @Test
    @DisplayName("validation: unknown members, question length, unknown scope source, placeIds before 036")
    void validation() throws Exception {
        ask(reader, "{\"question\":\"x\",\"allowAction\":true}", 400);
        ask(reader, "{\"question\":\"" + "x".repeat(4001) + "\"}", 400);
        ask(reader, "{\"question\":\"   \"}", 400);
        ask(reader, "{\"question\":\"x\",\"scope\":{\"sources\":[\"no-such-source\"]}}", 400);
        ask(reader, "{\"question\":\"x\",\"scope\":{\"sources\":[\"" + AskTestSources.SECRET + "\"]}}", 400);
        ask(reader, "{\"question\":\"x\",\"scope\":{\"placeIds\":[\"home\"]}}", 400);
        assertThat(chat().requests()).isEmpty();
    }
}
