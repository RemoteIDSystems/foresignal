package ai.foresignal.social.narrative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.RecordRef;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.GetClaimsTool;
import ai.foresignal.mcp.tools.GetNarrativeTool;
import ai.foresignal.mcp.tools.ListNarrativesTool;
import ai.foresignal.mcp.tools.MatchMediaTool;
import ai.foresignal.news.TitleEmbeddings;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.social.NarrativeTestSupport;
import ai.foresignal.testsupport.social.NarrativeTestSupport.Spec;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The narrative endpoints (prompt 136): narratives, posts, propagation, timeline, claims and fingerprints (read),
 * merge and split (agent), image matching (read with a token; multipart upload through ImageGuard or a PDQ hash), their
 * Problem codes ({@code UNKNOWN_NARRATIVE}, {@code UNKNOWN_CLAIM}, {@code UNKNOWN_FINGERPRINT},
 * {@code IMAGE_REJECTED}, {@code VALIDATION_FAILED}), the {@code narrative} and {@code claim} envelopes of contract 1.5,
 * and the MCP tools {@code list_narratives}, {@code get_narrative}, {@code get_claims} and {@code match_media}.
 */
@IntegrationTest
class NarrativeControllersIT {

    static final Instant T0 = Instant.now().truncatedTo(ChronoUnit.HOURS).minus(Duration.ofHours(6));

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired NarrativeClusterer clusterer;
    @Autowired NarrativeStore store;
    @Autowired MediaFingerprinter fingerprinter;
    @Autowired CanonicalRules rules;
    @Autowired ListNarrativesTool listTool;
    @Autowired GetNarrativeTool getTool;
    @Autowired GetClaimsTool claimsTool;
    @Autowired MatchMediaTool matchTool;
    @MockitoBean TitleEmbeddings embeddings;

    String read;
    String agent;
    String narrativeA;
    String narrativeB;
    long seq = 50_000;

    @BeforeEach
    void setUp() {
        when(embeddings.vectors(any())).thenReturn(new TitleEmbeddings.TitleVectors(null, Map.of()));
        fingerprinter.reset();
        read = TestTokens.bearer(TestTokens.create(tokens, "narrative-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "narrative-agent", Scope.AGENT));
        narrativeA = add(Spec.text("a1", "alice", "Explosion and fire at the port of Aden", T0, List.of("red-sea")));
        add(new Spec("bluesky", "a2", "bob", null, T0.plusSeconds(60), "a1", null, List.of(), List.of(), List.of()));
        add(new Spec("x", "a3", "carol", "Explosion and fire at the port of Aden, huge smoke", T0.plusSeconds(120),
                null, null, List.of(), List.of(), List.of("red-sea")));
        narrativeB = add(Spec.text("b1", "dave", "Flooding closes the coastal road", T0.plusSeconds(300),
                List.of("hormuz")));
    }

    private String add(Spec spec) {
        clusterer.process(NarrativeTestSupport.rule(rules), NarrativeTestSupport.post(++seq, spec));
        return store.member(NarrativeTestSupport.SOURCE, spec.platform() + ":" + spec.postId()).orElseThrow()
                .narrativeId();
    }

    private MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    private MockHttpServletResponse upload(String bearer,
            org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder request)
            throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, bearer)).andReturn().getResponse();
    }

    private JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("list, filters, detail, posts, propagation and timeline; 404 UNKNOWN_NARRATIVE")
    void narratives() throws Exception {
        JsonNode list = body(call(read, get("/v1/social/narratives")));
        assertThat(list.get("items")).hasSize(2);
        assertThat(list.get("items").get(0).get("ref").asString()).startsWith("narrative:");
        JsonNode topic = body(call(read, get("/v1/social/narratives").param("topic", "red-sea")));
        assertThat(topic.get("items")).hasSize(1);
        assertThat(topic.get("items").get(0).get("id").asString()).isEqualTo(narrativeA);
        JsonNode xOnly = body(call(read, get("/v1/social/narratives").param("platform", "x")));
        assertThat(xOnly.get("items")).hasSize(1);
        JsonNode page = body(call(read, get("/v1/social/narratives").param("limit", "1")));
        assertThat(page.get("nextCursor").isNull()).isFalse();
        JsonNode next = body(call(read, get("/v1/social/narratives").param("limit", "1")
                .param("cursor", page.get("nextCursor").asString())));
        assertThat(next.get("items")).hasSize(1);
        assertThat(call(read, get("/v1/social/narratives").param("status", "maybe")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/social/narratives").param("cursor", "%%%")).getStatus()).isEqualTo(400);

        JsonNode detail = body(call(read, get("/v1/social/narratives/" + narrativeA)));
        assertThat(detail.get("postCount").asInt()).isEqualTo(3);
        assertThat(detail.get("evidenceGroupCount").asInt()).isEqualTo(2);
        assertThat(detail.get("status").asString()).isEqualTo("unassessed");
        assertThat(detail.get("tlp").asString()).isEqualTo("CLEAR");
        assertThat(detail.get("envelope").get("ref").asString()).isEqualTo("narrative:" + narrativeA);
        assertThat(detail.get("claims").size()).isGreaterThanOrEqualTo(2);
        assertThat(detail.get("history").get(0).get("action").asString()).isEqualTo("created");
        assertThat(detail.get("clusterer").get("name").asString()).isEqualTo("NarrativeClusterer");
        assertThat(detail.get("platforms")).hasSize(2);
        MockHttpServletResponse missing = call(read, get("/v1/social/narratives/01JA8Q3V7S2K9W4T6Y1B5N0M3C"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_NARRATIVE");

        JsonNode posts = body(call(read, get("/v1/social/narratives/" + narrativeA + "/posts")));
        assertThat(posts.get("items")).hasSize(3);
        assertThat(posts.get("items").get(0).get("post").get("narrativeIds").get(0).asString()).isEqualTo(narrativeA);
        assertThat(body(call(read, get("/v1/social/narratives/" + narrativeA + "/posts").param("platform", "x")))
                .get("items")).hasSize(1);

        JsonNode graph = body(call(read, get("/v1/social/narratives/" + narrativeA + "/propagation")));
        assertThat(graph.get("earliestByPlatform")).hasSize(2);
        assertThat(graph.get("tracer").get("name").asString()).isEqualTo("PropagationTracer");
        assertThat(graph.get("summarised").asBoolean()).isFalse();
        assertThat(graph.get("provenanceChain").get(0).get("productRef").asString())
                .isEqualTo("narrative:" + narrativeA);

        JsonNode timeline = body(call(read, get("/v1/social/narratives/" + narrativeA + "/timeline")
                .param("interval", "PT15M")));
        assertThat(timeline.get("interval").asString()).isEqualTo("PT15M");
        assertThat(timeline.get("series")).hasSize(2);
        assertThat(call(read, get("/v1/social/narratives/" + narrativeA + "/timeline").param("interval", "PT2H"))
                .getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("merge and split need agent scope; split returns 201; bad bodies are 400")
    void mergeAndSplit() throws Exception {
        String mergeBody = "{\"narrativeIds\":[\"" + narrativeB + "\"],\"reason\":\"same event\"}";
        assertThat(call(read, post("/v1/social/narratives/" + narrativeA + "/merge")
                .contentType(MediaType.APPLICATION_JSON).content(mergeBody)).getStatus()).isEqualTo(403);
        assertThat(call(agent, post("/v1/social/narratives/" + narrativeA + "/merge")
                .contentType(MediaType.APPLICATION_JSON).content("{}")).getStatus()).isEqualTo(400);
        MockHttpServletResponse merged = call(agent, post("/v1/social/narratives/" + narrativeA + "/merge")
                .contentType(MediaType.APPLICATION_JSON).content(mergeBody));
        assertThat(merged.getStatus()).isEqualTo(200);
        assertThat(body(merged).get("postCount").asInt()).isEqualTo(4);
        assertThat(body(call(read, get("/v1/social/narratives/" + narrativeB))).get("mergedInto").asString())
                .isEqualTo(narrativeA);
        String ref = RecordRef.format(NarrativeTestSupport.SOURCE, "bluesky:b1");
        MockHttpServletResponse split = call(agent, post("/v1/social/narratives/" + narrativeA + "/split")
                .contentType(MediaType.APPLICATION_JSON).content("{\"postRefs\":[\"" + ref + "\"]}"));
        assertThat(split.getStatus()).isEqualTo(201);
        assertThat(split.getHeader("Location")).startsWith("/v1/social/narratives/");
        assertThat(body(split).get("postCount").asInt()).isEqualTo(1);
        MockHttpServletResponse notMember = call(agent, post("/v1/social/narratives/" + narrativeA + "/split")
                .contentType(MediaType.APPLICATION_JSON).content("{\"postRefs\":[\"record:x/x%3Anope\"]}"));
        assertThat(notMember.getStatus()).isEqualTo(400);
        assertThat(body(notMember).get("code").asString()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("claims: list, filters, one claim with grade; 404 UNKNOWN_CLAIM")
    void claims() throws Exception {
        JsonNode list = body(call(read, get("/v1/social/claims").param("narrativeId", narrativeA)));
        assertThat(list.get("items").size()).isGreaterThanOrEqualTo(2);
        JsonNode first = list.get("items").get(0);
        assertThat(first.get("grade").asString()).isEqualTo("F6");
        assertThat(first.get("extraction").asString()).isEqualTo("deterministic");
        String id = first.get("id").asString();
        assertThat(body(call(read, get("/v1/social/claims/" + id))).get("ref").asString()).isEqualTo("claim:" + id);
        assertThat(body(call(read, get("/v1/social/claims").param("type", "explosion"))).get("items")).hasSize(1);
        assertThat(call(read, get("/v1/social/claims").param("bbox", "1,2,3")).getStatus()).isEqualTo(400);
        MockHttpServletResponse missing = call(read, get("/v1/social/claims/01JA8Q3V7S2K9W4T6Y1B5N0M3C"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_CLAIM");
        JsonNode envelope = body(call(read, get("/v1/objects/claim:" + id)));
        assertThat(envelope.get("title").asString()).startsWith("Reported: ");
        JsonNode narrativeEnvelope = body(call(read, get("/v1/objects/narrative:" + narrativeA)));
        assertThat(narrativeEnvelope.get("ref").asString()).isEqualTo("narrative:" + narrativeA);
        JsonNode contract = body(call(read, get("/v1/contract")));
        assertThat(contract.get("objectTypes").toString()).contains("narrative").contains("claim");
        assertThat(contract.get("contractVersion").asString()).isEqualTo(ObjectTypes.CONTRACT_VERSION);
    }

    private static byte[] png() throws Exception {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 200; x++) {
                int v = ((x / 25 + y / 25) % 2 == 0) ? 20 : 230;
                img.setRGB(x, y, v << 16 | v << 8 | v);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    @Test
    @DisplayName("media: match by upload or PDQ with the REFERENCE_WINDOW caveat; token required; 415 IMAGE_REJECTED")
    void media() throws Exception {
        String fp = fingerprinter.process(new NarrativeStore.QueueItem("q", "https://cdn.example/p.png",
                NarrativeTestSupport.SOURCE, "bluesky:a1", "bluesky:a1", "bluesky", narrativeA, T0, 0), png());
        JsonNode fingerprint = body(call(read, get("/v1/social/media/" + fp)));
        assertThat(fingerprint.get("useCount").asInt()).isEqualTo(1);
        assertThat(fingerprint.get("narrativeIds").get(0).asString()).isEqualTo(narrativeA);
        assertThat(fingerprint.get("uses")).hasSize(1);
        MockHttpServletResponse unknown = call(read, get("/v1/social/media/01JA8Q3V7S2K9W4T6Y1B5N0M3C"));
        assertThat(unknown.getStatus()).isEqualTo(404);
        assertThat(body(unknown).get("code").asString()).isEqualTo("UNKNOWN_FINGERPRINT");

        String pdq = fingerprint.get("pdq").asString();
        assertThat(call(null, post("/v1/social/media/match").contentType(MediaType.APPLICATION_JSON)
                .content("{\"pdq\":\"" + pdq + "\"}")).getStatus()).isEqualTo(401);
        JsonNode byHash = body(call(read, post("/v1/social/media/match").contentType(MediaType.APPLICATION_JSON)
                .content("{\"pdq\":\"" + pdq + "\"}")));
        assertThat(byHash.get("matches").get(0).get("distance").asInt()).isZero();
        assertThat(byHash.get("query").get("source").asString()).isEqualTo("pdq");
        assertThat(byHash.get("caveats").get(0).get("code").asString()).isEqualTo("REFERENCE_WINDOW");
        assertThat(byHash.get("referenceWindowStart").isNull()).isFalse();

        JsonNode byUpload = body(upload(read, multipart("/v1/social/media/match")
                .file(new MockMultipartFile("file", "p.png", "image/png", png()))));
        assertThat(byUpload.get("query").get("source").asString()).isEqualTo("upload");
        assertThat(byUpload.get("matches").get(0).get("fingerprint").get("id").asString()).isEqualTo(fp);

        MockHttpServletResponse rejected = upload(read, multipart("/v1/social/media/match")
                .file(new MockMultipartFile("file", "x.txt", "text/plain", "not an image at all".getBytes(
                        StandardCharsets.UTF_8))));
        assertThat(rejected.getStatus()).isEqualTo(415);
        assertThat(body(rejected).get("code").asString()).isEqualTo("IMAGE_REJECTED");
        assertThat(call(read, post("/v1/social/media/match").contentType(MediaType.APPLICATION_JSON)
                .content("{\"pdq\":\"xyz\"}")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("MCP: list_narratives, get_narrative, get_claims and match_media are read tools with these shapes")
    void mcp() throws Exception {
        ToolContext context = new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(),
                "127.0.0.1"), "session", McpSessions.STREAMABLE);
        ToolResult list = listTool.call(context, json.readTree("{\"topic\":\"red-sea\"}"));
        assertThat(list.error()).isFalse();
        assertThat(list.structured().get("items")).hasSize(1);
        ToolResult one = getTool.call(context, json.readTree("{\"narrativeId\":\"narrative:" + narrativeA + "\"}"));
        assertThat(one.error()).isFalse();
        assertThat(one.structured().get("claims").size()).isGreaterThanOrEqualTo(2);
        assertThat(getTool.call(context, json.readTree("{}")).error()).isTrue();
        ToolResult claims = claimsTool.call(context, json.readTree("{\"narrativeId\":\"" + narrativeA + "\"}"));
        assertThat(claims.error()).isFalse();
        ToolResult match = matchTool.call(context, json.readTree("{\"pdq\":\"" + "0".repeat(64) + "\"}"));
        assertThat(match.error()).isFalse();
        assertThat(match.structured().get("caveats").get(0).get("code").asString()).isEqualTo("REFERENCE_WINDOW");
        assertThat(matchTool.call(context, json.readTree("{\"pdq\":\"zz\"}")).error()).isTrue();
        for (var tool : List.of(listTool, getTool, claimsTool, matchTool)) {
            assertThat(tool.requiredScope()).isEqualTo(Scope.READ);
            assertThat(tool.readOnlyHint()).isTrue();
        }
        assertThat(ai.foresignal.analyst.AnalystRunner.readToolNames()).contains("list_narratives", "get_narrative",
                "get_claims", "match_media");
    }
}
