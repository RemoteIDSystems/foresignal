package ai.foresignal.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.GetEvidenceTool;
import ai.foresignal.mcp.tools.GetObservabilityTool;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.evidence.EvidenceTestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 116 endpoints over HTTP and MCP: canonical events (list, filters, detail with originators and
 * groups, 404), record evidence, the private-source rule, the coverage layer (JSON and tiles), manifests and
 * replay (agent only), prompt templates, and the {@code get_evidence} and {@code get_observability} tool shapes.
 */
@IntegrationTest
@Import(EvidenceTestSources.class)
class EvidenceControllersIT {

    private static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;
    @Autowired EvidenceResolver resolver;
    @Autowired SourceVisibility sources;
    @Autowired ManifestService manifests;
    @Autowired GetEvidenceTool evidenceTool;
    @Autowired GetObservabilityTool observabilityTool;

    String read;
    String readPrivate;
    String agent;

    @BeforeEach
    void seed() {
        clock.setInstant(T0.plusSeconds(3_600));
        read = TestTokens.bearer(TestTokens.create(tokens, "evidence-read", Scope.READ));
        readPrivate = TestTokens.bearer(TestTokens.create(tokens, "evidence-read-private", Scope.READ, true));
        agent = TestTokens.bearer(TestTokens.create(tokens, "evidence-agent", Scope.AGENT));
        ingest(EvidenceTestSources.USGS, "us1", "earthquake", T0, 30.0, 38.0, Map.of("mag", 5.6));
        ingest(EvidenceTestSources.EMSC, "em1", "earthquake", T0.plusSeconds(6), 30.1, 38.0, Map.of("mag", 5.5));
        for (int i = 0; i < 6; i++) {
            ingest(EvidenceTestSources.GDELT, "g" + i, "conflict-event", T0.plusSeconds(i * 60L), 44.21, 15.35,
                    Map.of("sourceUrl", "https://example-news.org/" + i));
        }
        resolver.resetState();
        resolver.catchUp();
    }

    @AfterEach
    void restore() {
        sources.setOperatorPrivate(EvidenceTestSources.USGS, false);
        sources.refresh();
    }

    private void ingest(String source, String id, String kind, Instant at, double lon, double lat,
            Map<String, Object> payload) {
        NormalizedRecord.Builder builder = NormalizedRecord.builder(id, kind).observedAt(at).point(lon, lat);
        payload.forEach(builder::put);
        records.ingestAt(source, at.plusSeconds(30), builder.build());
    }

    private MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    private JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString());
    }

    private String seismicId() throws Exception {
        JsonNode page = body(call(read, get("/v1/evidence/events").param("family", "seismic")));
        return page.get("items").get(0).get("id").asString();
    }

    @Test
    @DisplayName("listCanonicalEvents: families, flags filter and the evidence-group counts")
    void list() throws Exception {
        MockHttpServletResponse response = call(read, get("/v1/evidence/events"));
        assertThat(response.getStatus()).isEqualTo(200);
        JsonNode items = body(response).get("items");
        assertThat(items).hasSize(2);
        JsonNode echo = body(call(read, get("/v1/evidence/events").param("flags", "ECHO"))).get("items");
        assertThat(echo).hasSize(1);
        assertThat(echo.get(0).get("family").asString()).isEqualTo("conflict");
        assertThat(echo.get(0).get("evidenceGroupCount").asInt()).isEqualTo(1);
        JsonNode seismic = body(call(read, get("/v1/evidence/events").param("family", "seismic"))).get("items");
        assertThat(seismic.get(0).get("evidenceGroupCount").asInt()).isEqualTo(2);
        assertThat(seismic.get(0).get("ref").asString()).startsWith("canonical-event:");
        assertThat(call(read, get("/v1/evidence/events").param("flags", "BOGUS")).getStatus()).isEqualTo(400);
        JsonNode first = body(call(read, get("/v1/evidence/events").param("limit", "1")));
        assertThat(first.get("nextCursor").isNull()).isFalse();
        JsonNode second = body(call(read, get("/v1/evidence/events").param("limit", "1")
                .param("cursor", first.get("nextCursor").asString())));
        assertThat(second.get("items")).hasSize(1);
        assertThat(second.get("items").get(0).get("id").asString())
                .isNotEqualTo(first.get("items").get(0).get("id").asString());
    }

    @Test
    @DisplayName("getCanonicalEvent: observations with originators and link reasons, groups; unknown is 404")
    void detail() throws Exception {
        JsonNode event = body(call(read, get("/v1/evidence/events/" + seismicId())));
        assertThat(event.get("observations")).hasSize(2);
        JsonNode observation = event.get("observations").get(0);
        assertThat(observation.get("originator").get("id").asString()).isIn("usgs-anss", "emsc");
        assertThat(observation.get("linkReason").get("rule").asString()).isNotBlank();
        assertThat(observation.get("recordRef").asString()).startsWith("record:");
        assertThat(event.get("evidenceGroups")).hasSize(2);
        MockHttpServletResponse missing = call(read, get("/v1/evidence/events/01JAAAAAAAAAAAAAAAAAAAAAAA"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(missing.getContentAsString()).contains("UNKNOWN_CANONICAL_EVENT");
    }

    @Test
    @DisplayName("getRecordEvidence: originator, links and canonical events; an unknown record is 404")
    void recordEvidence() throws Exception {
        JsonNode body = body(call(read, get("/v1/evidence/of").param("source", EvidenceTestSources.EMSC)
                .param("externalId", "em1")));
        assertThat(body.get("originator").get("id").asString()).isEqualTo("emsc");
        assertThat(body.get("links")).hasSize(1);
        assertThat(body.get("canonicalEvents")).hasSize(1);
        assertThat(call(read, get("/v1/evidence/of").param("source", EvidenceTestSources.EMSC)
                .param("externalId", "nope")).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("a private source's observations are hidden from readers without the private grant")
    void privateSources() throws Exception {
        String id = seismicId();
        sources.setOperatorPrivate(EvidenceTestSources.USGS, true);
        sources.refresh();
        JsonNode hidden = body(call(read, get("/v1/evidence/events/" + id)));
        assertThat(hidden.get("observations")).hasSize(1);
        assertThat(call(read, get("/v1/evidence/of").param("source", EvidenceTestSources.USGS)
                .param("externalId", "us1")).getStatus()).isEqualTo(404);
        JsonNode visible = body(call(readPrivate, get("/v1/evidence/events/" + id)));
        assertThat(visible.get("observations")).hasSize(2);
    }

    @Test
    @DisplayName("the coverage layer: per region and per cell, absenceMinDetect, and vector tiles")
    void coverageLayer() throws Exception {
        MockHttpServletResponse global = call(read, get("/v1/observability").param("family", "seismic"));
        assertThat(global.getStatus()).isEqualTo(200);
        JsonNode body = body(global);
        assertThat(body.get("absenceMinDetect").asDouble()).isEqualTo(0.8);
        assertThat(body.get("items").get(0).has("p")).isTrue();
        assertThat(body.get("items").get(0).has("absenceOk")).isTrue();
        JsonNode cells = body(call(read, get("/v1/observability").param("family", "seismic")
                .param("bbox", "29,37,31,39").param("limit", "5").param("include", "estimates")));
        assertThat(cells.get("items")).hasSize(5);
        assertThat(cells.get("items").get(0).get("cell").asString()).isNotBlank();
        assertThat(cells.get("nextCursor").isNull()).isFalse();
        assertThat(call(read, get("/v1/observability").param("family", "seismic").param("bbox", "1,2,3"))
                .getStatus()).isEqualTo(400);
        MockHttpServletResponse tile = call(read, get("/v1/observability/tiles/5/18/12.mvt").param("family", "seismic"));
        assertThat(tile.getStatus()).isEqualTo(200);
        assertThat(tile.getContentType()).startsWith("application/vnd.mapbox-vector-tile");
        assertThat(call(read, get("/v1/observability/tiles/2/9/0.mvt").param("family", "seismic")).getStatus())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("manifests are agent-only; replay answers 202 with the queued job")
    void manifestsScopes() throws Exception {
        String id = manifests.record(ManifestRequest.of("event-scorer", "1.1.0").config(Map.of("members", java.util.List.of(),
                "contradictions", 0)));
        assertThat(call(read, get("/v1/manifests/" + id)).getStatus()).isEqualTo(403);
        MockHttpServletResponse ok = call(agent, get("/v1/manifests/" + id));
        assertThat(ok.getStatus()).isEqualTo(200);
        assertThat(body(ok).get("component").asString()).isEqualTo("event-scorer");
        assertThat(body(ok).get("contentHash").asString()).hasSize(64);
        assertThat(call(agent, get("/v1/manifests/01JAAAAAAAAAAAAAAAAAAAAAAA")).getContentAsString())
                .contains("UNKNOWN_MANIFEST");
        MockHttpServletResponse replay = call(agent, post("/v1/manifests/" + id + "/replay"));
        assertThat(replay.getStatus()).isEqualTo(202);
        assertThat(body(replay).get("jobId").asString()).isNotBlank();
        assertThat(call(read, post("/v1/manifests/" + id + "/replay")).getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("prompt templates: JSON with text and hash, Markdown on request, 404 for unknown")
    void promptTemplates() throws Exception {
        JsonNode template = body(call(read, get("/v1/prompt-templates/ask-system")));
        assertThat(template.get("version").asString()).isEqualTo("1.0.0");
        assertThat(template.get("text").asString()).startsWith("You are Ask Foresignal");
        assertThat(template.get("sha256").asString()).hasSize(64);
        MockHttpServletResponse markdown = call(read, get("/v1/prompt-templates/ask-system")
                .header(HttpHeaders.ACCEPT, "text/markdown"));
        assertThat(markdown.getContentType()).startsWith("text/markdown");
        assertThat(call(read, get("/v1/prompt-templates/nope")).getStatus()).isEqualTo(404);
    }

    private static ToolContext readContext() {
        return new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(), "127.0.0.1"),
                "session", McpSessions.STREAMABLE);
    }

    @Test
    @DisplayName("MCP get_evidence and get_observability return the API shapes")
    void mcpShapes() throws Exception {
        ToolResult list = evidenceTool.call(readContext(), json.readTree("{\"family\":\"conflict\"}"));
        assertThat(list.error()).isFalse();
        assertThat(list.structured().get("items").get(0).get("qualityFlags").toString()).contains("ECHO");
        ToolResult record = evidenceTool.call(readContext(), json.readTree("{\"source\":\""
                + EvidenceTestSources.USGS + "\",\"externalId\":\"us1\"}"));
        assertThat(record.structured().get("originator").get("id").asString()).isEqualTo("usgs-anss");
        assertThat(evidenceTool.call(readContext(), json.readTree("{\"source\":\"x\"}")).error()).isTrue();
        ToolResult detectability = observabilityTool.call(readContext(),
                json.readTree("{\"family\":\"seismic\",\"lon\":30.0,\"lat\":38.0}"));
        assertThat(detectability.error()).isFalse();
        assertThat(detectability.structured().has("absenceOk")).isTrue();
        assertThat(evidenceTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(observabilityTool.readOnlyHint()).isTrue();
    }
}
