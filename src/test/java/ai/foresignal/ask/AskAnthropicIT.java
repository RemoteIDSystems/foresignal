package ai.foresignal.ask;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.llm.LlmProviderService;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One Ask round trip through the real Anthropic client, with the official SDK pointed at WireMock
 * (prompt 028): the tool loop, the {@code {"tool","data"}} wrapping of the tool result, citation
 * resolution against the persisted seq, and a follow-up that replays the stored Anthropic turn with its
 * thinking signature. No request leaves the machine and no real key is used.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TriggerTestSources.class)
class AskAnthropicIT extends WireMockSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String MESSAGES = "/v1/messages";
    static final String STANDARD = TestIngestSources.STANDARD;

    static final String TOOL_USE = """
            {"id":"msg_1","type":"message","role":"assistant","model":"claude-opus-5-5",
             "content":[{"type":"thinking","thinking":"","signature":"c2lnbmF0dXJlLTE="},
                        {"type":"tool_use","id":"toolu_1","name":"query_records","input":{}}],
             "stop_reason":"tool_use","stop_sequence":null,
             "usage":{"input_tokens":1200,"output_tokens":40,"cache_read_input_tokens":0,"cache_creation_input_tokens":0}}
            """;

    static final String ANSWER = """
            {"id":"msg_2","type":"message","role":"assistant","model":"claude-opus-5-5",
             "content":[{"type":"text","text":"One quake: Test q1 [test-standard:q1]."}],
             "stop_reason":"end_turn","stop_sequence":null,
             "usage":{"input_tokens":1400,"output_tokens":20,"cache_read_input_tokens":1100,"cache_creation_input_tokens":0}}
            """;

    static final String FOLLOW_UP = """
            {"id":"msg_3","type":"message","role":"assistant","model":"claude-opus-5-5",
             "content":[{"type":"text","text":"Nothing new since [test-standard:q1]."}],
             "stop_reason":"end_turn","stop_sequence":null,
             "usage":{"input_tokens":1500,"output_tokens":10,"cache_read_input_tokens":1400,"cache_creation_input_tokens":0}}
            """;

    @Autowired MockMvc mvc;
    @Autowired LlmProviderService providers;
    @Autowired LlmSettingsService llmSettings;
    @Autowired AppSettingService settings;
    @Autowired TokenService tokens;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;

    String reader;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(Instant.parse("2026-10-12T12:00:00Z"));
        reader = TestTokens.bearer(TestTokens.create(tokens, "anthropic-reader", Scope.READ));
        providers.create(JSON.readTree("{\"id\":\"claude\",\"type\":\"anthropic\",\"baseUrl\":\"" + baseUrl()
                + "\",\"apiKey\":\"sk-ant-test-not-a-key\",\"requestsPerMinute\":1000}"));
        llmSettings.update(new LlmSettings("claude", null, true, true, false, 200_000, false));
    }

    JsonNode ask(String body) throws Exception {
        String content = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/v1/ask")
                        .header(HttpHeaders.AUTHORIZATION, reader).contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JSON.readTree(content);
    }

    static List<JsonNode> bodies() {
        List<JsonNode> out = new ArrayList<>();
        for (LoggedRequest request : findAll(postRequestedFor(urlPathEqualTo(MESSAGES)))) {
            out.add(JSON.readTree(request.getBodyAsString()));
        }
        return out;
    }

    @Test
    @DisplayName("tools mode through the Anthropic SDK: wrapped tool result, verified citation, replayed thinking turn")
    void roundTrip() throws Exception {
        records.ingest(STANDARD, NormalizedRecord.builder("q1", TestIngestSources.STANDARD_KIND)
                .observedAt(clock.instant()).put("mag", 5.1).put("place", "Test q1").point(-122.42, 37.77).build());
        Long seq = jdbc.queryForObject("SELECT seq FROM record_head WHERE source_id = ? AND external_id = 'q1'",
                Long.class, STANDARD);
        stubFor(post(urlPathEqualTo(MESSAGES)).inScenario("ask").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(okJson(TOOL_USE)).willSetStateTo("tool"));
        stubFor(post(urlPathEqualTo(MESSAGES)).inScenario("ask").whenScenarioStateIs("tool")
                .willReturn(okJson(ANSWER)).willSetStateTo("answered"));
        stubFor(post(urlPathEqualTo(MESSAGES)).inScenario("ask").whenScenarioStateIs("answered")
                .willReturn(okJson(FOLLOW_UP)));

        JsonNode response = ask("{\"question\":\"Any quakes?\"}");

        assertThat(response.get("mode").asString()).isEqualTo("tools");
        assertThat(response.at("/citations/0/id").asString()).isEqualTo(STANDARD + ":q1");
        assertThat(response.at("/citations/0/seq").asLong()).isEqualTo(seq);
        assertThat(response.at("/usage/model").asString()).isEqualTo("claude-opus-5-5");

        List<JsonNode> bodies = bodies();
        assertThat(bodies).hasSize(2);
        assertThat(bodies.get(0).at("/output_config/effort").asString()).isEqualTo("medium");
        assertThat(bodies.get(0).at("/system/0/text").asString()).doesNotContain("2026-10-12");
        JsonNode toolResult = bodies.get(1).at("/messages/2/content/0");
        assertThat(toolResult.get("type").asString()).isEqualTo("tool_result");
        String resultText = toolResult.get("content").isString() ? toolResult.get("content").asString()
                : toolResult.at("/content/0/text").asString();
        assertThat(JSON.readTree(resultText).get("tool").asString()).isEqualTo("query_records");
        assertThat(resultText).contains(STANDARD + ":q1");

        ask("{\"question\":\"Anything new?\",\"threadId\":\"" + response.get("threadId").asString() + "\"}");

        JsonNode followUp = bodies().get(2);
        assertThat(followUp.at("/messages/1/content/0/type").asString()).isEqualTo("thinking");
        assertThat(followUp.at("/messages/1/content/0/signature").asString()).isEqualTo("c2lnbmF0dXJlLTE=");
        assertThat(followUp.at("/messages/1")).isEqualTo(bodies.get(1).at("/messages/1"));
        assertThat(followUp.get("messages")).hasSize(5);
    }
}
