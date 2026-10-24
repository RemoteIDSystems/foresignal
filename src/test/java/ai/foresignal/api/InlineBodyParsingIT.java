package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.inbound.InboundIt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * The bodies of {@code ownershipChain} (158), {@code requestAnalysis} and {@code analysisCallback} (155), which their
 * controllers and services read inline, through HTTP: a body missing what it requires is {@code 400} naming the field,
 * and the shape the Postman collection sends gets past the parser (no controller test sent these bodies before).
 */
@IntegrationTest
@AutoConfigureMockMvc
class InlineBodyParsingIT {

    static final String ULID = "01JA8Q3V7S2K9W4T6Y1B5N0M3C";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;

    String admin;
    InboundIt it;

    @BeforeEach
    void setUp() {
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
    }

    private MockHttpServletResponse send(String path, String json) throws Exception {
        return it.perform(post(path).header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                .content(json.replace('\'', '"')));
    }

    private static void refused(MockHttpServletResponse response, String field) throws Exception {
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(400);
        JsonNode problem = InboundIt.body(response);
        assertThat(problem.path("errors").findValuesAsString("field")).contains(field);
    }

    @Test
    @DisplayName("ownershipChain: refs are required and depth is 1 to 6; a well-formed unknown seed is 404")
    void ownershipChain() throws Exception {
        refused(send("/v1/graph/ownership-chain", "{}"), "refs");
        refused(send("/v1/graph/ownership-chain", "{'refs':['entity:" + ULID + "'],'depth':9}"), "depth");

        MockHttpServletResponse unknown = send("/v1/graph/ownership-chain",
                "{'refs':['entity:" + ULID + "'],'depth':3,'direction':'both','types':['owns','officer-of'],"
                        + "'minConfidence':0.5}");
        assertThat(unknown.getStatus()).as(unknown.getContentAsString()).isEqualTo(404);
        assertThat(InboundIt.body(unknown).path("code").asString()).isEqualTo("UNKNOWN_OBJECT");
    }

    @Test
    @DisplayName("requestAnalysis: forecasterId and questionId are required; a well-formed body reaches the service")
    void requestAnalysis() throws Exception {
        refused(send("/v1/external/analysis/requests", "{}"), "forecasterId");
        refused(send("/v1/external/analysis/requests", "{'forecasterId':'edp:ext-timesfm/transits'}"),
                "forecasterId");

        MockHttpServletResponse unknown = send("/v1/external/analysis/requests",
                "{'forecasterId':'edp:ext-timesfm/transits','questionId':'" + ULID + "','orientation':'same',"
                        + "'horizon':'P7D'}");
        // No such forecaster here: the service refuses it, which a parse failure never reaches.
        assertThat(unknown.getStatus()).as(unknown.getContentAsString()).isEqualTo(400);
        assertThat(unknown.getContentAsString()).doesNotContain("forecasterId and questionId are required");
    }

    @Test
    @DisplayName("analysisCallback: correlationId is required; an unknown correlation is answered, not recorded")
    void analysisCallback() throws Exception {
        refused(send("/v1/external/analysis/callback", "{'probability':0.6}"), "correlationId");

        MockHttpServletResponse unknown = send("/v1/external/analysis/callback",
                "{'correlationId':'" + ULID + "','probability':0.61,'quantiles':{'0.1':10,'0.5':12,'0.9':15}}");
        assertThat(unknown.getStatus()).as(unknown.getContentAsString()).isEqualTo(200);
        JsonNode out = InboundIt.body(unknown);
        assertThat(out.path("correlationId").asString()).isEqualTo(ULID);
        assertThat(out.path("recorded").asBoolean(true)).isFalse();
    }
}
