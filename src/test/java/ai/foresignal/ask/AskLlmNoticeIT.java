package ai.foresignal.ask;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ToolResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
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
 * Prompt 090: a source's {@code llmNotice} (OpenFEMA's eligibility notice in production) reaches the model in
 * the Ask tool result that carries that source's records, in both the short and the long record forms.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(AskTestSources.class)
class AskLlmNoticeIT extends AskItSupport {

    @Autowired MockMvc mvc;

    private void noticed(String externalId, String place) {
        records.ingest(AskTestSources.NOTICED, NormalizedRecord.builder(externalId, AskTestSources.QUAKE_KIND)
                .observedAt(clock.instant()).put("place", place).point(139.8, 35.6).build());
    }

    private void ask(String question) throws Exception {
        MvcResult result = mvc.perform(post("/v1/ask").header(HttpHeaders.AUTHORIZATION, reader)
                .contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"" + question + "\"}")).andReturn();
        assertThat(result.getResponse().getStatus()).as(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .isEqualTo(200);
    }

    @Test
    @DisplayName("query_records results carry the source's llmNotice on each of its records (short and long)")
    void queryRecordsCarriesNotice() throws Exception {
        noticed("n1", "Declared area");
        chat().then(toolUse(call("c1", "query_records",
                        "{\"source\":[\"" + AskTestSources.NOTICED + "\"]}")))
                .then(toolUse(call("c2", "query_records",
                        "{\"source\":[\"" + AskTestSources.NOTICED + "\"],\"detail\":\"long\"}")))
                .then(text("Done."));

        ask("What was declared?");

        List<ChatRequest> requests = chat().requests();
        assertThat(requests).hasSize(3);
        List<ToolResult> results = toolResults(requests.getLast());
        assertThat(results).hasSize(2);
        for (ToolResult result : results) {
            JsonNode items = JSON.readTree(result.content()).path("data").path("items");
            assertThat(items).as(result.content()).isNotEmpty();
            for (JsonNode item : items) {
                assertThat(item.path("llmNotice").asString()).as(result.content()).isEqualTo(AskTestSources.NOTICE);
            }
        }
    }

    @Test
    @DisplayName("records of a source without a notice carry none")
    void otherSourcesCarryNone() throws Exception {
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        chat().then(toolUse(call("c1", "query_records", "{\"source\":[\"" + AskTestSources.QUAKES + "\"]}")))
                .then(text("Done."));

        ask("Any quakes?");

        JsonNode data = lastData(chat().requests().getLast());
        assertThat(data.path("items")).isNotEmpty();
        assertThat(data.toString()).doesNotContain("llmNotice");
    }
}
