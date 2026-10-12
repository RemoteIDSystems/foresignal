package ai.foresignal.briefing;

import static ai.foresignal.testsupport.llm.FakeLlmClient.refusal;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * LLM-mode briefings with the scripted {@code FakeLlmClient} (prompt 029): the context sent, private
 * records withheld from a non-local provider (invariant EGR-1), citations verified with their warnings
 * kept, and a clean fallback to the digest on failure, refusal or a missing provider.
 */
@IntegrationTest
@Import(BriefingTestSources.class)
class LlmBriefingIT extends BriefingItSupport {

    static final String Q1 = BriefingTestSources.QUAKES + ":q1";

    UUID llmBriefing(McpCaller owner) {
        Map<String, Object> created = briefings.create(json(digestBody("Quake brief", "0 7 * * *",
                "\"mode\":\"llm\",\"instructions\":\"Lead with the strongest quake.\"")), owner);
        return id(created);
    }

    static String userText(ChatRequest request) {
        return ((ChatMessage.User) request.messages().getLast()).text();
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> citations(Map<String, Object> run) {
        return (List<Map<String, Object>>) run.get("citations");
    }

    @Test
    @DisplayName("the context carries stats, notable records with cite keys and the instructions; the citation is verified")
    void contextAndVerifiedCitation() {
        useChatProvider();
        quake("q1", "Tokyo Bay", 6.1);
        long seq = headSeq(BriefingTestSources.QUAKES, "q1");
        chat().then(request -> text("Strongest: Tokyo Bay [" + Q1 + "@" + seq + "]."));

        Map<String, Object> run = briefings.runNow(llmBriefing(admin()));

        assertThat(run.get("status")).isEqualTo("succeeded");
        assertThat(run.get("mode")).isEqualTo("llm");
        assertThat(run.get("contentMarkdown").toString()).contains("Strongest: Tokyo Bay");
        assertThat(citations(run)).singleElement().satisfies(c -> {
            assertThat(c.get("id")).isEqualTo(Q1);
            assertThat(((Number) c.get("seq")).longValue()).isEqualTo(seq);
            assertThat(c.get("type")).isEqualTo("record");
        });
        assertThat(run.get("warnings").toString()).doesNotContain("dropped");
        assertThat(((Map<?, ?>) run.get("usage")).get("provider")).isEqualTo(CHAT);

        ChatRequest request = chat().requests().getLast();
        assertThat(request.system()).isEqualTo(BriefingPrompts.SYSTEM).doesNotContain("2026");
        assertThat(request.purpose()).isEqualTo(Purpose.BRIEFING);
        assertThat(request.effort()).isEqualTo(Effort.MEDIUM);
        assertThat(request.tools()).isEmpty();
        assertThat(userText(request)).contains("Lead with the strongest quake.")
                .contains("[" + Q1 + "@" + seq + "]").contains("\"counts\"").contains("Tokyo Bay").contains("<data>");
    }

    @Test
    @DisplayName("private records never reach a non-local provider, even for an admin-owned briefing")
    void privateWithheldFromNonLocal() {
        useChatProvider();
        quake("q1", "Tokyo Bay", 6.1);
        secretQuake("s1", "Hidden Lake", 7.0);
        chat().then(text("A quiet day."));

        briefings.runNow(llmBriefing(admin()));

        String sent = everythingSent(chat().requests());
        assertThat(sent).contains("Tokyo Bay").doesNotContain("Hidden Lake").doesNotContain(BriefingTestSources.SECRET);
    }

    @Test
    @DisplayName("a local provider may receive private records of an admin-owned briefing, never of an agent-owned one")
    void localProvider() {
        provider(LOCAL, "ollama", 1_000_000L);
        assign(LOCAL);
        secretQuake("s1", "Hidden Lake", 7.0);
        fakes.client(LOCAL).otherwise(request -> text("Noted."));

        briefings.runNow(llmBriefing(admin()));
        assertThat(everythingSent(fakes.client(LOCAL).requests())).contains("Hidden Lake");

        int before = fakes.client(LOCAL).requests().size();
        briefings.runNow(llmBriefing(agentCaller()));
        assertThat(everythingSent(fakes.client(LOCAL).requests().subList(before,
                fakes.client(LOCAL).requests().size()))).doesNotContain("Hidden Lake");
    }

    @Test
    @DisplayName("a citation no record supports is dropped and the warning kept")
    void verifierWarningsKept() {
        useChatProvider();
        quake("q1", "Tokyo Bay", 6.1);
        chat().then(text("A big one hit Osaka [" + BriefingTestSources.QUAKES + ":nope@1]."));

        Map<String, Object> run = briefings.runNow(llmBriefing(admin()));

        assertThat(run.get("status")).isEqualTo("succeeded");
        assertThat(citations(run)).isEmpty();
        assertThat(run.get("warnings").toString()).contains("dropped citation [" + BriefingTestSources.QUAKES + ":nope@1]");
        BriefingRunEntity stored = runRepository.findById(UUID.fromString(run.get("id").toString())).orElseThrow();
        assertThat(stored.getWarnings().toString()).contains("dropped citation");
    }

    @Test
    @DisplayName("a provider failure falls back to the digest with status fallback and the error noted")
    void fallbackOnFailure() {
        useChatProvider();
        quake("q1", "Tokyo Bay", 6.1);
        chat().then(request -> {
            throw new LlmProviderException(LlmProviderException.Kind.OVERLOADED, CHAT, "provider down");
        });

        Map<String, Object> run = briefings.runNow(llmBriefing(admin()));

        assertThat(run.get("status")).isEqualTo("fallback");
        assertThat(run.get("error").toString()).contains("provider down");
        assertThat(run.get("contentMarkdown").toString()).contains("## Activity").contains("Tokyo Bay");
        assertThat(run.get("warnings").toString()).contains("fell back to the digest");
        assertThat(citations(run)).isNotEmpty();
        assertThat(briefingRepository.findAll().getFirst().getLastRunStatus()).isEqualTo("fallback");
    }

    @Test
    @DisplayName("a refusal falls back to the digest")
    void fallbackOnRefusal() {
        useChatProvider();
        quake("q1", "Tokyo Bay", 6.1);
        chat().then(refusal("cyber"));

        Map<String, Object> run = briefings.runNow(llmBriefing(admin()));

        assertThat(run.get("status")).isEqualTo("fallback");
        assertThat(run.get("error").toString()).contains("refused").contains("cyber");
        assertThat(run.get("contentMarkdown").toString()).contains("# Quake brief");
    }

    @Test
    @DisplayName("without a chat provider, LLM mode renders the digest as a fallback")
    void fallbackWithoutProvider() {
        quake("q1", "Tokyo Bay", 6.1);

        Map<String, Object> run = briefings.runNow(llmBriefing(admin()));

        assertThat(run.get("status")).isEqualTo("fallback");
        assertThat(run.get("error").toString()).contains("no chat provider");
        assertThat(chat().requests()).isEmpty();
    }
}
