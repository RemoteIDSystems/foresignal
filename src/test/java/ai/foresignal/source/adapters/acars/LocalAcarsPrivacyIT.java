package ai.foresignal.source.adapters.acars;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.McpToolRegistry;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.formats.acars.AcarsMetadataMapper;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import io.modelcontextprotocol.spec.McpSchema;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code local-acars-{n}} is private by descriptor (prompt 086, rule of prompt 039): under {@code openReads} an anonymous
 * caller reads none of its {@code acars-message} records (list, direct read, tiles), MCP {@code query_records} and
 * {@code search} return nothing to a caller without the private grant, a remote LLM (the Ask tools) never receives it,
 * it is never embedded, federated or sent to a sink, and no setting can make it public or change its egress.
 */
class LocalAcarsPrivacyIT extends WeatherIngestSupport {

    static final String ID = LocalAcarsAdapter.ID_PREFIX + "1";
    static final Instant AT = Instant.parse("2026-10-13T14:01:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired EgressPolicy egress;
    @Autowired IngestQueue queue;
    @Autowired McpToolRegistry mcp;
    @Autowired SourceVisibility visibility;

    private List<NormalizedRecord> seed() {
        clock.setInstant(AT);
        List<NormalizedRecord> records = List.of(
                AcarsMetadataMapper.map(JSON.readTree(AdapterFixtures.text("local-rf", "acarsdec.json")), "home"),
                AcarsMetadataMapper.map(JSON.readTree(AdapterFixtures.text("local-rf", "dumpvdl2.json")), "home"),
                AcarsMetadataMapper.map(JSON.readTree(AdapterFixtures.text("local-rf", "dumphfdl.json")), "home"));
        IngestOutcome outcome = new TestRecords(queue, clock).ingestAt(ID, AT, records.toArray(NormalizedRecord[]::new));
        assertThat(outcome.persisted()).isEqualTo(3);
        return records;
    }

    @Test
    @DisplayName("the descriptor is private, LOCAL_ONLY and not overridable; every egress path refuses it")
    void egressPaths() {
        SourceDescriptor d = sources.list().stream().filter(s -> s.id().equals(ID)).findFirst().orElseThrow().descriptor();
        assertThat(d.privateSource()).isTrue();
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.egressOverridable()).isFalse();
        assertThat(d.embedFields()).isEmpty();
        assertThat(egress.isPrivate(ID)).isTrue();
        assertThat(egress.hiddenSources(Viewer.anonymous())).contains(ID);
        assertThat(egress.visible(ID, Viewer.anonymous())).isFalse();
        assertThat(egress.allowsEmbedding(ID)).isFalse();
        assertThat(egress.allows(EgressPath.SINK, ID, Viewer.admin())).isFalse();
        assertThat(egress.allows(EgressPath.FEDERATION, ID, Viewer.admin())).isFalse();
        assertThat(egress.allows(EgressPath.SHARE, ID, Viewer.admin())).isFalse();
        assertThat(egress.llmExcludedSources(new EgressPolicy.LlmDestination("remote", "openai", false)))
                .as("the Ask tools never give it to a remote model").contains(ID);
    }

    @Test
    @DisplayName("openReads: anonymous list, direct read and the kind list never show acars-message")
    void anonymousReads() throws Exception {
        List<NormalizedRecord> records = seed();
        String admin = "Bearer " + adminTokens.rotate();
        security(admin, true);
        try {
            MockHttpServletResponse list = mvc.perform(get("/v1/records").param("kind", AcarsMetadataMapper.KIND))
                    .andReturn().getResponse();
            assertThat(list.getStatus()).isLessThan(500);
            assertThat(list.getContentAsString()).doesNotContain(ID).doesNotContain("XX0123");
            String path = "/v1/records/" + ID + "/" + URLEncoder.encode(records.getFirst().externalId(),
                    StandardCharsets.UTF_8);
            assertThat(mvc.perform(get(path)).andReturn().getResponse().getStatus()).isEqualTo(404);
            assertThat(mvc.perform(get("/v1/kinds")).andReturn().getResponse().getContentAsString())
                    .doesNotContain(AcarsMetadataMapper.KIND);
            assertThat(mvc.perform(get("/v1/tiles/" + ID + "/0/0/0.mvt")).andReturn().getResponse().getStatus())
                    .as("no tile of a private source for an anonymous caller").isEqualTo(404);
            assertThat(mvc.perform(get("/v1/tiles/" + AcarsMetadataMapper.KIND + "/0/0/0.mvt")).andReturn().getResponse()
                    .getStatus()).as("nor of its kind").isEqualTo(404);
            assertThat(mvc.perform(get("/v1/tiles.json")).andReturn().getResponse().getContentAsString())
                    .doesNotContain(ID);
            assertThat(mvc.perform(get("/v1/records").param("kind", AcarsMetadataMapper.KIND)
                    .header(HttpHeaders.AUTHORIZATION, admin)).andReturn().getResponse().getContentAsString())
                    .as("the operator still sees them").contains("XX0123");
        } finally {
            security(admin, false);
        }
    }

    @Test
    @DisplayName("MCP query_records and search return nothing of acars-message to callers without the private grant")
    void mcpTools() {
        seed();
        Set<String> anonymousExcluded = visibility.hiddenFor(null, false, TokenRestrictions.NONE);
        assertThat(anonymousExcluded).contains(ID);
        ToolContext anonymous = new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), anonymousExcluded,
                "127.0.0.1"), "session", McpSessions.STREAMABLE);
        McpSchema.CallToolResult query = mcp.call("query_records", Map.of("kind", AcarsMetadataMapper.KIND, "limit", 50),
                anonymous);
        assertThat(String.valueOf(query.structuredContent())).doesNotContain(ID).doesNotContain("XX0123");

        AuthenticatedToken reader = new AuthenticatedToken(UUID.randomUUID(), "acars-reader", Scope.AGENT,
                TokenRestrictions.NONE, null, "principal-1", false, false);
        Set<String> readerExcluded = visibility.hiddenFor(Scope.AGENT, false, TokenRestrictions.NONE);
        assertThat(readerExcluded).contains(ID);
        ToolContext agent = new ToolContext(new McpCaller(reader, new ReadCaller(Scope.AGENT, false, true, null, false),
                readerExcluded, "10.0.0.2"), "session", McpSessions.STREAMABLE);
        McpSchema.CallToolResult search = mcp.call("search", Map.of("q", "XX0123", "kind", List.of(AcarsMetadataMapper.KIND)),
                agent);
        assertThat(String.valueOf(search.structuredContent()) + search.content()).doesNotContain(ID)
                .doesNotContain("XX0123 H1");
    }

    @Test
    @DisplayName("settings cannot make it public or change its egress")
    void settingsCannotMakeItPublic() throws Exception {
        String admin = "Bearer " + adminTokens.rotate();
        MockHttpServletResponse makePublic = mvc.perform(put("/v1/sources/" + ID).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"private\":false}")).andReturn().getResponse();
        assertThat(makePublic.getStatus()).isEqualTo(400);
        MockHttpServletResponse open = mvc.perform(put("/v1/sources/" + ID).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"egress\":\"OPEN\"}")).andReturn().getResponse();
        assertThat(open.getStatus()).isEqualTo(400);
        assertThat(open.getContentAsString()).contains("EGRESS_FIXED");
        assertThat(egress.isPrivate(ID)).isTrue();
    }

    private void security(String admin, boolean openReads) throws Exception {
        MockHttpServletResponse r = mvc.perform(put("/v1/settings/security").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"openReads\":" + openReads + "}")).andReturn()
                .getResponse();
        assertThat(r.getStatus()).as(r.getContentAsString()).isLessThan(300);
    }
}
