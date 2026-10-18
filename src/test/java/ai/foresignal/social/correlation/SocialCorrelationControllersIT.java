package ai.foresignal.social.correlation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.CompareClaimTool;
import ai.foresignal.mcp.tools.GetSocialLeadTimesTool;
import ai.foresignal.mcp.tools.RunSocialCueTool;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.social.narrative.NarrativeStore;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.social.CorrelationTestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The social-versus-physical endpoints (prompt 137): comparisons and their revisions, cues, lead times and the social
 * settings, with their scopes and Problem codes ({@code UNKNOWN_COMPARISON}, {@code UNKNOWN_CUE},
 * {@code PLATFORM_DISABLED}, {@code VALIDATION_FAILED}), and the MCP tools {@code compare_claim},
 * {@code get_social_lead_times} and {@code run_social_cue}.
 */
@IntegrationTest
class SocialCorrelationControllersIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");
    static final Instant T0 = NOW.minus(Duration.ofDays(3));
    static final String NARRATIVE = "01JA8Q3V7S2K9W4T6Y1B5N0M3C";
    static final String CLAIM = "01JA8Q3V7S2K9W4T6Y1B5N0M3D";

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired MutableClock clock;
    @Autowired NarrativeStore narratives;
    @Autowired EvidenceStore evidence;
    @Autowired JdbcTemplate jdbc;
    @Autowired ComparisonService comparisons;
    @Autowired ComparisonStore store;
    @Autowired CompareClaimTool compareTool;
    @Autowired GetSocialLeadTimesTool leadTool;
    @Autowired RunSocialCueTool cueTool;

    String read;
    String agent;
    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        read = TestTokens.bearer(TestTokens.create(tokens, "social-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "social-agent", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "social-admin", Scope.ADMIN));
        CorrelationTestSupport.narrative(narratives, NARRATIVE, T0, 3, List.of(), 6.0);
        CorrelationTestSupport.claim(narratives, CLAIM, NARRATIVE, "explosion", "conflict", T0, 3);
        CorrelationTestSupport.canonicalEvent(evidence, jdbc, "conflict", "acled-events", "acled", "aggregator",
                T0.plus(Duration.ofMinutes(30)), CorrelationTestSupport.LON + 0.01, CorrelationTestSupport.LAT, "YE");
        comparisons.compareNarrative(NARRATIVE);
    }

    private MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    private JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("comparisons: list with filters, detail with revisions, 404 UNKNOWN_COMPARISON, 400 on a bad filter")
    void comparisons() throws Exception {
        JsonNode page = body(call(read, get("/v1/social/comparisons?narrativeId=" + NARRATIVE)));
        assertThat(page.get("items")).hasSize(1);
        JsonNode c = page.get("items").get(0);
        assertThat(c.get("status").asString()).isEqualTo("corroborated");
        assertThat(c.get("ref").asString()).startsWith("social-comparison:");
        assertThat(c.get("foundBy").asString()).isEqualTo("organic");
        assertThat(c.get("leadTimes").get("leadOverPhysicalSeconds").asDouble()).isEqualTo(1800.0);
        assertThat(c.get("comparator").get("name").asString()).isEqualTo("SocialPhysicalComparator");
        assertThat(body(call(read, get("/v1/social/comparisons?status=unobserved"))).get("items")).isEmpty();
        assertThat(body(call(read, get("/v1/social/comparisons?region=YE&family=conflict"))).get("items")).hasSize(1);
        JsonNode detail = body(call(read, get("/v1/social/comparisons/" + c.get("id").asString())));
        assertThat(detail.get("revisions")).hasSize(1);
        assertThat(detail.get("revisions").get(0).get("status").asString()).isEqualTo("corroborated");
        MockHttpServletResponse missing = call(read, get("/v1/social/comparisons/01JA8Q3V7S2K9W4T6Y1B5N0M9Z"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_COMPARISON");
        assertThat(call(read, get("/v1/social/comparisons?status=bogus")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/social/comparisons?region=yemen")).getStatus()).isEqualTo(400);
        assertThat(narratives.claim(CLAIM).orElseThrow().status()).isEqualTo("corroborated");
        assertThat(narratives.narrative(NARRATIVE).orElseThrow().status()).isEqualTo("corroborated");
    }

    @Test
    @DisplayName("cues: list, 404 UNKNOWN_CUE, POST needs agent, validates, and is 409 when no platform is enabled")
    void cues() throws Exception {
        assertThat(body(call(read, get("/v1/social/cues"))).get("items")).isEmpty();
        MockHttpServletResponse missing = call(read, get("/v1/social/cues/01JA8Q3V7S2K9W4T6Y1B5N0M9Z"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_CUE");
        String cue = "{\"regionCode\":\"YE\",\"terms\":[\"port\"],\"window\":{\"start\":\"" + T0 + "\"}}";
        assertThat(call(read, post("/v1/social/cues").contentType(MediaType.APPLICATION_JSON).content(cue))
                .getStatus()).isEqualTo(403);
        assertThat(call(null, post("/v1/social/cues").contentType(MediaType.APPLICATION_JSON).content(cue))
                .getStatus()).isEqualTo(401);
        MockHttpServletResponse invalid = call(agent, post("/v1/social/cues").contentType(MediaType.APPLICATION_JSON)
                .content("{\"terms\":[\"port\"]}"));
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(body(invalid).get("code").asString()).isEqualTo("VALIDATION_FAILED");
        MockHttpServletResponse disabled = call(agent, post("/v1/social/cues").contentType(MediaType.APPLICATION_JSON)
                .content(cue));
        assertThat(disabled.getStatus()).isEqualTo(409);
        assertThat(body(disabled).get("code").asString()).isEqualTo("PLATFORM_DISABLED");
    }

    @Test
    @DisplayName("lead times: grouped by claim type, platform, region and foundBy with the training cut-off")
    void leadTimes() throws Exception {
        JsonNode stats = body(call(read, get("/v1/social/lead-times?claimType=explosion")));
        assertThat(stats.get("trainingCutoff").asString()).isNotBlank();
        assertThat(stats.get("groups")).hasSize(1);
        JsonNode g = stats.get("groups").get(0);
        assertThat(g.get("platform").asString()).isEqualTo("bluesky");
        assertThat(g.get("foundBy").asString()).isEqualTo("organic");
        assertThat(g.get("overPhysical").get("n").asInt()).isEqualTo(1);
        assertThat(call(read, get("/v1/social/lead-times?foundBy=nobody")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("settings: admin only; defaults; a PUT validates and replaces")
    void settings() throws Exception {
        assertThat(call(read, get("/v1/settings/social")).getStatus()).isEqualTo(403);
        JsonNode doc = body(call(admin, get("/v1/settings/social")));
        assertThat(doc.get("compare").get("minGroups").asInt()).isEqualTo(3);
        assertThat(doc.get("cues").get("maxPerDay").asInt()).isEqualTo(50);
        assertThat(doc.get("attentionGap").get("percentile").asDouble()).isEqualTo(10.0);
        assertThat(doc.get("compare").get("familyWindows").has("conflict")).isTrue();
        MockHttpServletResponse bad = call(admin, put("/v1/settings/social").contentType(MediaType.APPLICATION_JSON)
                .content("{\"attentionGap\":{\"percentile\":80},\"cues\":{\"platforms\":[\"myspace\"]}}"));
        assertThat(bad.getStatus()).isEqualTo(400);
        assertThat(body(bad).get("errors").size()).isGreaterThanOrEqualTo(2);
        JsonNode updated = body(call(admin, put("/v1/settings/social").contentType(MediaType.APPLICATION_JSON)
                .content("{\"compare\":{\"minGroups\":5,\"familyWindows\":{\"conflict\":{\"after\":\"48h\"}}},"
                        + "\"cues\":{\"maxPerDay\":10},\"trackRecord\":{\"minAssessed\":20,\"overrides\":[{\"platform\":"
                        + "\"bluesky\",\"channel\":\"news\",\"reliability\":\"C\"}]}}")));
        assertThat(updated.get("compare").get("minGroups").asInt()).isEqualTo(5);
        assertThat(updated.get("compare").get("familyWindows").get("conflict").get("after").asString()).isEqualTo("2d");
        assertThat(updated.get("cues").get("maxPerDay").asInt()).isEqualTo(10);
        assertThat(updated.get("trackRecord").get("overrides").get(0).get("channel").asString()).isEqualTo("news");
    }

    @Test
    @DisplayName("MCP: compare_claim and get_social_lead_times are read tools; run_social_cue is agent and cost-bearing")
    void mcp() throws Exception {
        ToolContext context = new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(),
                "127.0.0.1"), "session", McpSessions.STREAMABLE);
        ToolResult byClaim = compareTool.call(context, json.readTree("{\"claimId\":\"" + CLAIM + "\"}"));
        assertThat(byClaim.error()).isFalse();
        assertThat(byClaim.structured().get("items")).hasSize(1);
        assertThat(compareTool.call(context, json.readTree("{\"narrativeId\":\"" + NARRATIVE + "\"}")).structured()
                .get("items")).hasSize(1);
        assertThat(compareTool.call(context, json.readTree("{}")).error()).isTrue();
        ToolResult lead = leadTool.call(context, json.readTree("{\"region\":\"YE\"}"));
        assertThat(lead.error()).isFalse();
        assertThat(lead.structured().get("groups")).hasSize(1);
        assertThat(cueTool.call(context, json.readTree("{\"regionCode\":\"YE\",\"windowStart\":\"" + T0 + "\"}"))
                .error()).isTrue();
        assertThat(compareTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(leadTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(cueTool.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(cueTool.costBearing()).isTrue();
        assertThat(ai.foresignal.analyst.AnalystRunner.readToolNames()).contains("compare_claim",
                "get_social_lead_times");
    }
}
