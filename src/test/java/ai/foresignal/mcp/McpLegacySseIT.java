package ai.foresignal.mcp;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.mcp.McpTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.ProtocolVersions;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The legacy HTTP+SSE transport behind {@code foresignal.mcp.legacy-sse.enabled} (prompt 025): with
 * the flag on, the SDK's SSE client connects at {@code /mcp/sse}, lists tools by scope and calls
 * {@code query_records}, sharing the streamable transport's tools and authentication. The flag-off
 * case ({@code /mcp/sse} is 404) is {@link McpServerIT#legacyOff}, which runs with the default.
 */
@WebIntegrationTest
@Import(McpTestSources.class)
@TestPropertySource(properties = "foresignal.mcp.legacy-sse.enabled=true")
class McpLegacySseIT {

    private static final Instant T = Instant.parse("2026-10-12T12:00:00Z");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @LocalServerPort int port;
    @Autowired MutableClock clock;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;
    @Autowired TestRecords records;
    @Autowired McpSessions sessions;

    private McpSyncClient client;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            try {
                client.closeGracefully();
            } catch (RuntimeException e) {
                // already closed
            }
        }
    }

    private McpSyncClient connect(String bearer) {
        HttpClientSseClientTransport.Builder transport = HttpClientSseClientTransport.builder("http://localhost:" + port)
                .sseEndpoint("/mcp/sse")
                .connectTimeout(Duration.ofSeconds(5));
        if (bearer != null) {
            transport.httpRequestCustomizer((builder, method, uri, body, context) -> builder.header("Authorization", bearer));
        }
        client = McpClient.sync(transport.build()).requestTimeout(TIMEOUT).initializationTimeout(TIMEOUT).build();
        client.initialize();
        return client;
    }

    @Test
    @DisplayName("with the flag on, an SSE client connects, lists tools by scope and calls query_records")
    void connectListCall() {
        records.ingest(STANDARD, NormalizedRecord.builder("s", STANDARD_KIND).observedAt(T).put("mag", 4.2)
                .point(10, 20).build());
        String reader = TestTokens.bearer(TestTokens.create(tokens, "sse-reader", Scope.READ));
        McpSyncClient sse = connect(reader);

        assertThat(sse.getCurrentInitializationResult().protocolVersion()).isEqualTo(ProtocolVersions.MCP_2024_11_05);
        assertThat(sse.getCurrentInitializationResult().serverInfo().name()).isEqualTo("foresignal");
        assertThat(sessions.count(McpSessions.LEGACY_SSE)).isPositive();
        List<String> names = sse.listTools().tools().stream().map(McpSchema.Tool::name).toList();
        assertThat(names).contains("query_records", "test_cost").doesNotContain("create_watch");

        McpSchema.CallToolResult result = sse.callTool(new McpSchema.CallToolRequest("query_records",
                Map.of("source", List.of(STANDARD))));
        assertThat(result.isError()).isFalse();
        JsonNode out = JSON.valueToTree(result.structuredContent());
        assertThat(out.get("items").get(0).get("id").asString()).isEqualTo(STANDARD + ":s");
        assertThat(out.get("items").get(0).has("seq")).isTrue();
    }

    @Test
    @DisplayName("without a token under openReads the SSE client sees read tools only")
    void anonymous() {
        List<String> names = connect(null).listTools().tools().stream().map(McpSchema.Tool::name).toList();
        assertThat(names).contains("query_records").doesNotContain("test_cost", "set_alert_status");
    }

    @Test
    @DisplayName("/v1/system reports the legacy transport")
    void systemReportsLegacy() throws Exception {
        String reader = TestTokens.bearer(TestTokens.create(tokens, "sse-system", Scope.READ));
        HttpResponse<String> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
                .send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v1/system"))
                        .timeout(TIMEOUT).header("Authorization", reader).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
        JsonNode mcp = JSON.readTree(response.body()).get("mcp");
        assertThat(mcp.get("transports").get("legacySse").asBoolean()).isTrue();
        assertThat(mcp.get("transports").get("streamable").asBoolean()).isTrue();
    }
}
