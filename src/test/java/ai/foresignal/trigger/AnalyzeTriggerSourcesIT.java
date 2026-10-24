package ai.foresignal.trigger;

import static ai.foresignal.testsupport.llm.FakeLlmClient.refusal;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.PromptTemplates;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * {@code analyzeTriggerSources} (the alert builder's advisory source-relevance check) with a scripted fake chat
 * provider: every requested source comes back once with a verdict and a reason, a clearly unrelated source is flagged
 * with the model's reason, the model sees catalog content only as a data block, a private source is withheld from a
 * remote provider and comes back unrated, nothing is changed, and the usual refusals apply.
 */
@IntegrationTest
@Import(AskTestSources.class)
class AnalyzeTriggerSourcesIT extends AskItSupport {

    static final String PATH = "/v1/triggers/analyze-sources";

    /** A satellite catalog: nothing to do with earthquakes. */
    static final String SATELLITES = "celestrak-gp";

    @Autowired MockMvc mvc;

    MockHttpServletResponse call(String token, String body) throws Exception {
        var request = post(PATH).contentType(MediaType.APPLICATION_JSON).content(body);
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, token);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    JsonNode analyze(String... sourceIds) throws Exception {
        MockHttpServletResponse response = call(agent, "{\"sourceIds\":" + JSON.writeValueAsString(sourceIds) + "}");
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        return JSON.readTree(response.getContentAsString());
    }

    static List<String> ids(JsonNode analysis) {
        List<String> out = new ArrayList<>();
        analysis.get("sources").forEach(s -> out.add(s.get("sourceId").asString()));
        return out;
    }

    static JsonNode source(JsonNode analysis, String id) {
        for (JsonNode s : analysis.get("sources")) {
            if (id.equals(s.get("sourceId").asString())) {
                return s;
            }
        }
        throw new AssertionError(id + " is missing from " + analysis);
    }

    static String userText(ChatRequest request) {
        return ((ChatMessage.User) request.messages().getFirst()).text();
    }

    @Test
    @DisplayName("each source gets a verdict and a reason; the unrelated satellite catalog is flagged with its reason")
    void flagsTheUnrelatedSource() throws Exception {
        chat().then(text("{\"summary\":\"Earthquake reports.\",\"sources\":["
                + "{\"sourceId\":\"" + AskTestSources.QUAKES + "\",\"verdict\":\"relevant\",\"reason\":\"Reports"
                + " earthquakes like the others.\"},"
                + "{\"sourceId\":\"" + AskTestSources.NOTICED + "\",\"verdict\":\"relevant\",\"reason\":\"Shares the"
                + " quake kind.\"},"
                + "{\"sourceId\":\"" + SATELLITES + "\",\"verdict\":\"not-meaningful\",\"reason\":\"Satellite orbits"
                + " share no kind or subject with earthquake reports.\"}]}"));
        long rulesBefore = jdbc.queryForObject("select count(*) from trigger_rule", Long.class);

        JsonNode analysis = analyze(AskTestSources.QUAKES, AskTestSources.NOTICED, SATELLITES);

        assertThat(ids(analysis)).containsExactly(AskTestSources.QUAKES, AskTestSources.NOTICED, SATELLITES);
        for (JsonNode s : analysis.get("sources")) {
            assertThat(s.get("verdict").asString()).isIn("relevant", "marginal", "not-meaningful");
            assertThat(s.get("reason").asString()).isNotBlank();
        }
        JsonNode flagged = source(analysis, SATELLITES);
        assertThat(flagged.get("verdict").asString()).isEqualTo("not-meaningful");
        assertThat(flagged.get("reason").asString()).contains("Satellite orbits");
        assertThat(source(analysis, AskTestSources.QUAKES).get("verdict").asString()).isEqualTo("relevant");
        assertThat(analysis.get("advisory").asBoolean()).isTrue();
        assertThat(analysis.get("summary").asString()).isEqualTo("Earthquake reports.");
        assertThat(analysis.get("model").asString()).isEqualTo("fake-chat");
        assertThat(analysis.get("shared").get("kinds").toString()).contains(AskTestSources.QUAKE_KIND);
        assertThat(analysis.get("template").get("templateId").asString()).isEqualTo("source-relevance-system");

        ChatRequest sent = chat().requests().getFirst();
        assertThat(sent.system()).isEqualTo(PromptTemplates.text(PromptTemplates.SOURCE_RELEVANCE,
                PromptTemplates.V1));
        assertThat(sent.purpose()).isEqualTo(Purpose.ANALYST);
        JsonNode block = JSON.readTree(userText(sent));
        assertThat(block.get("tool").asString()).isEqualTo("dataset_sources");
        assertThat(block.get("data").get("sources").toString()).contains(SATELLITES, AskTestSources.QUAKES,
                AskTestSources.QUAKE_KIND);
        // Advice only: no rule is created and the request changes nothing.
        assertThat(jdbc.queryForObject("select count(*) from trigger_rule", Long.class)).isEqualTo(rulesBefore);
    }

    @Test
    @DisplayName("a private source is not sent to a remote provider and comes back unrated, with the reason")
    void privateSourceWithheld() throws Exception {
        chat().then(text("{\"sources\":[{\"sourceId\":\"" + AskTestSources.QUAKES + "\",\"verdict\":\"relevant\","
                + "\"reason\":\"Quakes.\"},{\"sourceId\":\"" + AskTestSources.NOTICED + "\",\"verdict\":\"marginal\","
                + "\"reason\":\"Quakes with a notice.\"}]}"));

        JsonNode analysis = analyze(AskTestSources.QUAKES, AskTestSources.SECRET, AskTestSources.NOTICED);

        assertThat(ids(analysis)).containsExactly(AskTestSources.QUAKES, AskTestSources.SECRET, AskTestSources.NOTICED);
        JsonNode secret = source(analysis, AskTestSources.SECRET);
        assertThat(secret.get("verdict").asString()).isEqualTo("unrated");
        assertThat(secret.get("reason").asString()).contains("egress policy");
        assertThat(source(analysis, AskTestSources.NOTICED).get("verdict").asString()).isEqualTo("marginal");
        assertThat(userText(chat().requests().getFirst())).doesNotContain(AskTestSources.SECRET);
    }

    @Test
    @DisplayName("a source the model leaves out, or rates with another value, is never dropped: it comes back unrated")
    void nothingDropped() throws Exception {
        chat().then(text("Here you go: {\"sources\":[{\"sourceId\":\"" + AskTestSources.QUAKES + "\",\"verdict\":"
                + "\"essential\",\"reason\":\"Best.\"},{\"sourceId\":\"invented-source\",\"verdict\":\"relevant\","
                + "\"reason\":\"Made up.\"}]}"));

        JsonNode analysis = analyze(AskTestSources.QUAKES, SATELLITES);

        assertThat(ids(analysis)).containsExactly(AskTestSources.QUAKES, SATELLITES);
        assertThat(source(analysis, AskTestSources.QUAKES).get("verdict").asString()).isEqualTo("unrated");
        assertThat(source(analysis, SATELLITES).get("reason").asString()).isNotBlank();
        assertThat(analysis.get("summary").isNull()).isTrue();
    }

    @Test
    @DisplayName("a refusal leaves every source unrated with the reason")
    void refusalUnrated() throws Exception {
        chat().then(refusal("policy"));

        JsonNode analysis = analyze(AskTestSources.QUAKES, SATELLITES);

        for (JsonNode s : analysis.get("sources")) {
            assertThat(s.get("verdict").asString()).isEqualTo("unrated");
            assertThat(s.get("reason").asString()).contains("declined");
        }
    }

    @Test
    @DisplayName("without a chat provider the check answers 409 LLM_NOT_CONFIGURED")
    void notConfigured() throws Exception {
        llmSettings.update(new LlmSettings(null, null, true, true, false, 200_000, false));

        MockHttpServletResponse response = call(agent, "{\"sourceIds\":[\"" + AskTestSources.QUAKES + "\",\""
                + SATELLITES + "\"]}");

        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.getContentAsString()).contains("LLM_NOT_CONFIGURED");
        assertThat(chat().requests()).isEmpty();
    }

    @Test
    @DisplayName("an agent token is always needed, and the body is 2 to 50 distinct registered source ids")
    void scopesAndValidation() throws Exception {
        String body = "{\"sourceIds\":[\"" + AskTestSources.QUAKES + "\",\"" + SATELLITES + "\"]}";
        assertThat(call(null, body).getStatus()).isEqualTo(401);
        assertThat(call(reader, body).getStatus()).isEqualTo(403);
        assertThat(call(agent, "{\"sourceIds\":[\"" + AskTestSources.QUAKES + "\"]}").getStatus()).isEqualTo(400);
        MockHttpServletResponse unknown = call(agent, "{\"sourceIds\":[\"" + AskTestSources.QUAKES
                + "\",\"no-such-source\"]}");
        assertThat(unknown.getStatus()).isEqualTo(400);
        assertThat(unknown.getContentAsString()).contains("sourceIds[1]");
        assertThat(call(agent, "{\"sourceIds\":[\"" + AskTestSources.QUAKES + "\",\"" + AskTestSources.QUAKES
                + "\"]}").getStatus()).isEqualTo(400);
        assertThat(call(agent, "{\"sourceIds\":[\"" + AskTestSources.QUAKES + "\",\"" + SATELLITES
                + "\"],\"enable\":true}").getStatus()).isEqualTo(400);
        assertThat(chat().requests()).isEmpty();
    }
}
