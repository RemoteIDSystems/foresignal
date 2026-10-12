package ai.foresignal.security.restrict;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.security.TestTokens;
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
 * Ask's tool loop runs with the caller's restriction (prompt 035): the scripted fake provider asks
 * for {@code query_records} without filters, and the tool result it receives holds only the record
 * inside the token's area; a citation of the outside record is dropped.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(AskTestSources.class)
class AskRestrictionIT extends AskItSupport {

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("the tool loop sees only records inside the restriction and drops outside citations")
    void toolLoopClamped() throws Exception {
        quake("tokyo", "Tokyo Bay", 5.5, 139.8, 35.6);
        quake("lima", "Lima", 6.0, -77.0, -12.0);
        String boxed = TestTokens.bearer(TestTokens.create(tokens, "ask-boxed", Scope.READ,
                new TokenRestrictions(null, null, List.of(130.0, 30.0, 145.0, 40.0), null), null));
        String lima = AskTestSources.QUAKES + ":lima";
        chat().then(toolUse(call("c1", "query_records", "{}")))
                .then(request -> text("Two quakes [" + lima + "@1]."));

        MvcResult result = mvc.perform(post("/v1/ask").contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"Any quakes?\"}").header(HttpHeaders.AUTHORIZATION, boxed)).andReturn();
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(result.getResponse().getStatus()).as(content).isEqualTo(200);

        List<ChatRequest> requests = chat().requests();
        assertThat(requests).hasSizeGreaterThanOrEqualTo(2);
        JsonNode data = lastData(requests.get(1));
        assertThat(data.toString()).contains("tokyo").doesNotContain("lima");
        assertThat(everythingSent(requests)).doesNotContain("Lima");
        JsonNode response = JSON.readTree(content);
        for (JsonNode citation : response.path("citations")) {
            assertThat(citation.path("id").asString()).isNotEqualTo(lima);
        }
    }
}
