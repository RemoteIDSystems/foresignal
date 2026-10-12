package ai.foresignal.mcp;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.mcp.McpTestSources;
import ai.foresignal.testsupport.mcp.McpTestTools;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The MCP server over a real socket with the SDK's own streamable HTTP client (prompt 025): initialize,
 * tool visibility by caller, records with persisted seqs, history reasons, watches and the alert
 * lifecycle as an agent, scope refusals, the cost-bearing rule, audit rows and session binding.
 * Every client and HTTP call has a timeout.
 */
@WebIntegrationTest
@Import(McpTestSources.class)
class McpServerIT {

    private static final Instant T = Instant.parse("2026-10-12T12:00:00Z");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String INITIALIZE = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":"
            + "{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},\"clientInfo\":{\"name\":\"it\",\"version\":\"1\"}}}";

    @LocalServerPort int port;
    @Autowired MutableClock clock;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;
    @Autowired TestRecords records;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired ChangeBus changeBus;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;
    @Autowired AuditService audit;
    @Autowired JdbcTemplate jdbc;
    @Autowired McpToolRegistry registry;
    @Autowired McpSessions sessions;

    private final List<McpSyncClient> clients = new ArrayList<>();
    private String reader;
    private String agent;
    /** An agent token with the 039 private grant. */
    private String privateAgent;
    private TriggerFixture fixture;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        reader = TestTokens.bearer(TestTokens.create(tokens, "mcp-reader", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "mcp-agent", Scope.AGENT));
        privateAgent = TestTokens.bearer(TestTokens.create(tokens, "mcp-private-agent", Scope.AGENT, true));
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
        audit.flush();
        jdbc.update("DELETE FROM audit_event");
    }

    @AfterEach
    void tearDown() {
        for (McpSyncClient client : clients) {
            try {
                client.closeGracefully();
            } catch (RuntimeException e) {
                // the server may already have refused the session
            }
        }
        fixture.close();
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private String base() {
        return "http://localhost:" + port;
    }

    /** An initialized SDK client presenting {@code bearer} (null for none). */
    private McpSyncClient client(String bearer) {
        HttpClientStreamableHttpTransport.Builder transport = HttpClientStreamableHttpTransport.builder(base())
                .endpoint("/mcp")
                .connectTimeout(Duration.ofSeconds(5));
        if (bearer != null) {
            transport.httpRequestCustomizer((builder, method, uri, body, context) -> builder.header("Authorization", bearer));
        }
        McpSyncClient client = McpClient.sync(transport.build())
                .requestTimeout(TIMEOUT)
                .initializationTimeout(TIMEOUT)
                .build();
        clients.add(client);
        client.initialize();
        return client;
    }

    private static List<String> toolNames(McpSyncClient client) {
        return client.listTools().tools().stream().map(McpSchema.Tool::name).toList();
    }

    private static McpSchema.CallToolResult call(McpSyncClient client, String tool, Map<String, Object> args) {
        return client.callTool(new McpSchema.CallToolRequest(tool, args));
    }

    private static String text(McpSchema.CallToolResult result) {
        return ((McpSchema.TextContent) result.content().getFirst()).text();
    }

    private static JsonNode structured(McpSchema.CallToolResult result) {
        assertThat(result.isError()).as(result.content().toString()).isFalse();
        return JSON.valueToTree(result.structuredContent());
    }

    private HttpResponse<String> post(String bearer, String sessionId, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + "/mcp"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (bearer != null) {
            request.header("Authorization", bearer);
        }
        if (sessionId != null) {
            request.header("Mcp-Session-Id", sessionId);
        }
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String bearer) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + path)).timeout(TIMEOUT).GET();
        if (bearer != null) {
            request.header("Authorization", bearer);
        }
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void quake(String id, double mag, Instant at) {
        records.ingest(STANDARD, NormalizedRecord.builder(id, STANDARD_KIND).observedAt(at).put("mag", mag)
                .put("place", "Test " + id).point(-122.42, 37.77).build());
    }

    private AlertView alertFor(UUID rule, int expected) {
        fixture.settle();
        List<AlertView> list = fixture.alerts(rule);
        assertThat(list).hasSize(expected);
        return list.getLast();
    }

    @Test
    @DisplayName("initialize returns the foresignal server, its instructions and the three capabilities")
    void initialize() {
        McpSyncClient client = client(reader);
        McpSchema.InitializeResult init = client.getCurrentInitializationResult();
        assertThat(init.serverInfo().name()).isEqualTo("foresignal");
        assertThat(init.instructions()).contains("watch").contains("trigger rule")
                .contains("not instructions").contains("[source:externalId]").contains("seq");
        assertThat(init.capabilities().tools()).isNotNull();
        assertThat(init.capabilities().resources()).isNotNull();
        assertThat(init.capabilities().prompts()).isNotNull();
        assertThat(sessions.count(McpSessions.STREAMABLE)).isPositive();
    }

    @Test
    @DisplayName("without a token under openReads: read tools only; no write or cost-bearing tools")
    void anonymousVisibility() {
        List<String> names = toolNames(client(null));
        assertThat(names).contains("query_records", "get_record", "list_alerts", "list_watches", "test_watch",
                "system_status", "test_image");
        assertThat(names).doesNotContain("create_watch", "set_alert_status", "acknowledge_alert", "delete_watch",
                "update_watch", "test_cost");
    }

    @Test
    @DisplayName("without a token and openReads off, /mcp is 401")
    void anonymousWithoutOpenReads() throws Exception {
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        HttpResponse<String> response = post(null, null, INITIALIZE);
        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue("WWW-Authenticate")).isPresent();
        assertThatThrownBy(() -> client(null)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("a read token sees read and cost-bearing tools; an agent token also the write tools")
    void tokenVisibility() {
        List<String> read = toolNames(client(reader));
        assertThat(read).contains("query_records", "test_cost").doesNotContain("create_watch", "set_alert_status");
        List<String> write = toolNames(client(agent));
        assertThat(write).contains("query_records", "test_cost", "create_watch", "update_watch", "delete_watch",
                "acknowledge_alert", "set_alert_status");
        // Admin tools (039's purge_entity and export_entity) are never visible to an agent token (checkpoint 041).
        assertThat(write).doesNotContain("purge_entity", "export_entity");
        assertThat(write).hasSize((int) registry.tools().stream()
                .filter(t -> t.requiredScope() != ai.foresignal.security.Scope.ADMIN).count());
    }

    @Test
    @DisplayName("every registered tool, test beans included, has a well-formed contract")
    void registeredToolsAreWellFormed() {
        assertThat(registry.tools()).extracting(ForesignalMcpTool::name).contains("test_cost", "test_image");
        registry.tools().forEach(McpToolSchemaTest::assertWellFormed);
    }

    @Test
    @DisplayName("query_records returns persisted seqs, versions and live, and hides private sources")
    void queryRecords() {
        quake("a", 5.5, T);
        records.ingest(PRIVATE, NormalizedRecord.builder("p", PRIVATE_KIND).observedAt(T).point(1, 1).build());
        long seq = jdbc.queryForObject("SELECT seq FROM record_head WHERE source_id = ? AND external_id = 'a'",
                Long.class, STANDARD);

        JsonNode out = structured(call(client(reader), "query_records", Map.of("source", List.of(STANDARD))));
        assertThat(out.get("total").asLong()).isEqualTo(1);
        assertThat(out.get("truncated").asBoolean()).isFalse();
        assertThat(out.has("watermarkSeq")).isTrue();
        JsonNode item = out.get("items").get(0);
        assertThat(item.get("id").asString()).isEqualTo(STANDARD + ":a");
        assertThat(item.get("seq").asLong()).isEqualTo(seq);
        assertThat(item.get("version").asInt()).isEqualTo(1);
        assertThat(item.get("live").asBoolean()).isFalse();
        assertThat(item.get("lon").asDouble()).isEqualTo(-122.42);
        assertThat(item.get("fields").get("mag").asDouble()).isEqualTo(5.5);

        JsonNode all = structured(call(client(reader), "query_records", Map.of("limit", 200)));
        assertThat(all.get("items").toString()).doesNotContain(PRIVATE);
        McpSchema.CallToolResult hidden = call(client(reader), "query_records", Map.of("source", List.of(PRIVATE)));
        assertThat(hidden.isError()).isTrue();
        assertThat(text(hidden)).contains("does not exist");
        // Prompt 039: tools filter per token; a plain agent token is hidden from, the private grant sees.
        assertThat(structured(call(client(agent), "query_records", Map.of("limit", 200))).get("items").toString())
                .doesNotContain(PRIVATE);
        assertThat(structured(call(client(privateAgent), "query_records", Map.of("limit", 200))).get("items")
                .toString()).contains(PRIVATE);

        JsonNode longForm = structured(call(client(reader), "get_record",
                Map.of("source", STANDARD, "externalId", "a", "detail", "long")));
        assertThat(longForm.get("payload").get("mag").asDouble()).isEqualTo(5.5);
        assertThat(longForm.get("seq").asLong()).isEqualTo(seq);
    }

    @Test
    @DisplayName("get_record_history lists versions with their persist reasons")
    void history() {
        quake("h", 4.0, T);
        quake("h", 4.6, T.plusSeconds(60));
        JsonNode out = structured(call(client(reader), "get_record_history",
                Map.of("source", STANDARD, "externalId", "h")));
        assertThat(out.get("items").size()).isEqualTo(2);
        assertThat(out.get("items").get(0).get("persistReason").asString()).isEqualTo("FIRST");
        // A payload change on test-standard changes the static hash: SIGNIFICANT (Core-Data-Model §5 row 6 precedes
        // the ALL row 7; IngestWriterIT asserts the same), not CHANGE (checkpoint 041).
        assertThat(out.get("items").get(1).get("persistReason").asString()).isEqualTo("SIGNIFICANT");
        assertThat(out.get("items").get(1).get("version").asInt()).isEqualTo(2);
    }

    @Test
    @DisplayName("get_changes follows the feed after a seq")
    void changes() {
        quake("c1", 3.0, T);
        quake("c2", 3.1, T);
        JsonNode out = structured(call(client(reader), "get_changes", Map.of("after", 0, "source", List.of(STANDARD))));
        assertThat(out.get("items").size()).isEqualTo(2);
        long last = out.get("lastSeq").asLong();
        JsonNode next = structured(call(client(reader), "get_changes", Map.of("after", last)));
        assertThat(next.get("items").size()).isZero();
    }

    @Test
    @DisplayName("create_watch as an agent creates a validated rule; invalid rules report field errors")
    void createWatch() {
        McpSyncClient client = client(agent);
        JsonNode watch = structured(call(client, "create_watch", Map.of("rule", Map.of(
                "name", "Big quakes", "sources", List.of(STANDARD),
                "conditions", List.of(Map.of("path", "mag", "op", ">=", "value", 6))))));
        UUID id = UUID.fromString(watch.get("id").asString());
        assertThat(rules.get(id).rule().getName()).isEqualTo("Big quakes");

        McpSchema.CallToolResult invalid = call(client, "create_watch", Map.of("rule", Map.of(
                "name", "Bad", "conditions", List.of(Map.of("path", "mag", "op", "~", "value", 1)))));
        assertThat(invalid.isError()).isTrue();
        assertThat(text(invalid)).contains("conditions");

        JsonNode listed = structured(call(client, "list_watches", Map.of()));
        assertThat(listed.get("items").toString()).contains("Big quakes");
        JsonNode deleted = structured(call(client, "delete_watch", Map.of("watchId", id.toString())));
        assertThat(deleted.get("deleted").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("set_alert_status resolves and snoozes alerts through the lifecycle")
    void alertLifecycle() {
        UUID rule = fixture.rule("{'name':'M5','sources':['" + STANDARD + "'],"
                + "'conditions':[{'path':'mag','op':'>=','value':5}]}");
        quake("q1", 5.5, T);
        AlertView first = alertFor(rule, 1);
        McpSyncClient client = client(agent);

        JsonNode resolved = structured(call(client, "set_alert_status",
                Map.of("alertId", first.id(), "action", "resolve", "feedback", "useful", "note", "handled")));
        assertThat(resolved.get("state").asString()).isEqualTo("resolved");
        assertThat(resolved.get("resolvedBy").asString()).isEqualTo("token:mcp-agent");
        assertThat(resolved.get("feedback").asString()).isEqualTo("useful");

        quake("q2", 6.5, T);
        AlertView second = alertFor(rule, 2);
        JsonNode snoozed = structured(call(client, "set_alert_status",
                Map.of("alertId", second.id(), "action", "snooze", "for", "2h")));
        assertThat(Instant.parse(snoozed.get("snoozedUntil").asString())).isEqualTo(T.plus(Duration.ofHours(2)));
        JsonNode unsnoozed = structured(call(client, "set_alert_status",
                Map.of("alertId", second.id(), "action", "unsnooze")));
        assertThat(unsnoozed.has("snoozedUntil")).isFalse();

        McpSchema.CallToolResult bad = call(client, "set_alert_status",
                Map.of("alertId", second.id(), "action", "snooze"));
        assertThat(bad.isError()).as("a snooze needs until or for").isTrue();

        JsonNode acked = structured(call(client, "acknowledge_alert", Map.of("alertId", second.id())));
        assertThat(acked.get("state").asString()).isEqualTo("acknowledged");

        JsonNode withRecord = structured(call(client(reader), "get_alert", Map.of("alertId", second.id())));
        assertThat(withRecord.get("recordFrom").asString()).isEqualTo("version");
        assertThat(withRecord.get("record").get("id").asString()).isEqualTo(STANDARD + ":q2");
        JsonNode listed = structured(call(client(reader), "list_alerts", Map.of("status", List.of("resolved"))));
        assertThat(listed.get("items").size()).isEqualTo(1);
    }

    @Test
    @DisplayName("a read token calling write tools gets a tool error naming the agent scope")
    void readTokenRefused() {
        McpSyncClient client = client(reader);
        McpSchema.CallToolResult create = call(client, "create_watch", Map.of("rule", Map.of("name", "x",
                "sources", List.of(STANDARD))));
        assertThat(create.isError()).isTrue();
        assertThat(text(create)).contains("agent scope");
        McpSchema.CallToolResult status = call(client, "set_alert_status", Map.of("alertId", 1, "action", "resolve"));
        assertThat(status.isError()).isTrue();
        assertThat(text(status)).contains("agent scope");
        assertThat(rules.list(null, null)).isEmpty();
    }

    @Test
    @DisplayName("a cost-bearing tool without a token is refused under openReads, and works with one")
    void costBearingNeedsToken() {
        McpSchema.CallToolResult anonymous = call(client(null), "test_cost", Map.of("text", "hi"));
        assertThat(anonymous.isError()).isTrue();
        assertThat(text(anonymous)).contains("always requires a token");
        assertThat(structured(call(client(reader), "test_cost", Map.of("text", "hi"))).get("echo").asString())
                .isEqualTo("hi");
    }

    @Test
    @DisplayName("an image tool returns image content with its caption")
    void imageContent() {
        McpSchema.CallToolResult result = call(client(reader), "test_image", Map.of());
        assertThat(result.content().getFirst()).isInstanceOfSatisfying(McpSchema.ImageContent.class,
                image -> assertThat(image.data()).isEqualTo(McpTestTools.PIXEL_PNG));
        assertThat(((McpSchema.TextContent) result.content().get(1)).text()).isEqualTo("A transparent pixel.");
    }

    @Test
    @DisplayName("tool calls are audited with argument names only, never values")
    void auditRows() {
        McpSyncClient client = client(reader);
        call(client, "query_records", Map.of("q", "zebra-secret", "limit", 5));
        call(client, "create_watch", Map.of("rule", Map.of("name", "x")));
        audit.flush();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM audit_event WHERE target_type = 'mcp_tool' ORDER BY id");
        assertThat(rows).hasSize(2);
        Map<String, Object> query = rows.get(0);
        assertThat(query.get("target_id")).isEqualTo("query_records");
        assertThat(query.get("token_name")).isEqualTo("mcp-reader");
        assertThat(query.get("scope")).isEqualTo("read");
        assertThat(query.get("method")).isEqualTo("MCP");
        String detail = query.get("detail").toString();
        assertThat(detail).contains("\"limit\"").contains("\"q\"").contains("durationMs").contains("\"ok\"")
                .doesNotContain("zebra-secret");
        assertThat(rows.get(1).get("detail").toString()).contains("denied");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE path = '/mcp' AND target_type IS NULL",
                Long.class)).as("no per-message HTTP audit rows").isZero();
    }

    @Test
    @DisplayName("a session opened with one token is refused with another")
    void sessionBinding() throws Exception {
        HttpResponse<String> init = post(reader, null, INITIALIZE);
        assertThat(init.statusCode()).as(init.body()).isEqualTo(200);
        String sessionId = init.headers().firstValue("Mcp-Session-Id").orElseThrow();
        post(reader, sessionId, "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");

        HttpResponse<String> other = post(agent, sessionId, "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}");
        assertThat(other.statusCode()).isEqualTo(403);
        assertThat(other.body()).contains("different token");
        HttpResponse<String> anonymous = post(null, sessionId, "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/list\"}");
        assertThat(anonymous.statusCode()).isEqualTo(403);

        HttpResponse<String> same = post(reader, sessionId, "{\"jsonrpc\":\"2.0\",\"id\":4,\"method\":\"tools/list\"}");
        assertThat(same.statusCode()).isEqualTo(200);
        assertThat(same.body()).contains("query_records").doesNotContain("create_watch");
    }

    @Test
    @DisplayName("a foreign browser Origin is refused")
    void foreignOrigin() throws Exception {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(base() + "/mcp"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("Origin", "https://evil.example")
                .header("Authorization", reader)
                .POST(HttpRequest.BodyPublishers.ofString(INITIALIZE))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).contains("Origin");
    }

    @Test
    @DisplayName("resources and the situation-report prompt are served")
    void resourcesAndPrompt() {
        quake("r", 4.4, T);
        McpSyncClient client = client(reader);
        assertThat(client.listResources().resources()).extracting(McpSchema.Resource::uri)
                .contains("foresignal://sources", "foresignal://kinds");
        McpSchema.ReadResourceResult kinds = client.readResource(new McpSchema.ReadResourceRequest("foresignal://kinds"));
        assertThat(((McpSchema.TextResourceContents) kinds.contents().getFirst()).text()).contains(STANDARD_KIND);
        McpSchema.ReadResourceResult sources =
                client.readResource(new McpSchema.ReadResourceRequest("foresignal://sources"));
        assertThat(((McpSchema.TextResourceContents) sources.contents().getFirst()).text()).contains(STANDARD)
                .doesNotContain(PRIVATE);

        assertThat(client.listPrompts().prompts()).extracting(McpSchema.Prompt::name).contains("situation-report");
        McpSchema.GetPromptResult prompt = client.getPrompt(new McpSchema.GetPromptRequest("situation-report",
                Map.of("bbox", "-125,24,-66,50", "since", "2026-10-12T00:00:00Z")));
        String text = ((McpSchema.TextContent) prompt.messages().getFirst().content()).text();
        assertThat(text).contains("query_records").contains("list_alerts").contains("source_health")
                .contains("seq").contains("-125,24,-66,50");
    }

    @Test
    @DisplayName("system_status works, and /v1/system reports the mcp block")
    void systemStatus() throws Exception {
        JsonNode status = structured(call(client(reader), "system_status", Map.of()));
        assertThat(status.has("version")).isTrue();
        assertThat(status.get("mcp").get("enabled").asBoolean()).isTrue();

        HttpResponse<String> system = get("/v1/system", reader);
        JsonNode mcp = JSON.readTree(system.body()).get("mcp");
        assertThat(mcp.get("enabled").asBoolean()).isTrue();
        assertThat(mcp.get("transports").get("streamable").asBoolean()).isTrue();
        assertThat(mcp.get("transports").get("legacySse").asBoolean()).isFalse();
        assertThat(mcp.get("tools").asInt()).isEqualTo(registry.size()).isGreaterThanOrEqualTo(17);
        assertThat(mcp.get("sessions").asInt()).isPositive();
        assertThat(JSON.readTree(system.body()).get("features").get("mcp").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("the source, kind and backtest tools answer through their services")
    void sourceKindAndBacktestTools() {
        quake("k", 5.2, T);
        McpSyncClient client = client(agent);
        JsonNode sources = structured(call(client, "list_sources", Map.of()));
        assertThat(sources.get("items").toString()).contains(STANDARD);
        JsonNode longSources = structured(call(client, "list_sources", Map.of("detail", "long")));
        assertThat(longSources.get("items").get(0).has("budget")).isTrue();
        JsonNode health = structured(call(client, "source_health", Map.of("source", STANDARD)));
        assertThat(health.get("items").get(0).get("sourceId").asString()).isEqualTo(STANDARD);
        JsonNode filtered = structured(call(client, "source_health", Map.of("status", List.of("failing"))));
        assertThat(filtered.get("items").toString()).doesNotContain("\"status\":\"healthy\"");
        JsonNode kinds = structured(call(client, "list_kinds", Map.of()));
        assertThat(kinds.get("items").toString()).contains(STANDARD_KIND);

        JsonNode watch = structured(call(client, "create_watch", Map.of("rule", Map.of("name", "Backtest",
                "sources", List.of(STANDARD), "conditions", List.of(Map.of("path", "mag", "op", ">=", "value", 5))))));
        JsonNode test = structured(call(client, "test_watch", Map.of("watchId", watch.get("id").asString(),
                "since", T.minusSeconds(3600).toString(), "until", T.plusSeconds(60).toString())));
        assertThat(test.get("matchCount").asLong()).isEqualTo(1);
        assertThat(test.get("items").get(0).get("id").asString()).isEqualTo(STANDARD + ":k");

        McpSchema.CallToolResult unknown = call(client, "get_record", Map.of("source", STANDARD, "externalId", "none"));
        assertThat(unknown.isError()).isTrue();
        assertThat(text(unknown)).contains("No record");
        McpSchema.CallToolResult invalid = call(client, "query_records", Map.of("limit", 500));
        assertThat(invalid.isError()).as("limit above 200").isTrue();
    }

    @Test
    @DisplayName("with the legacy flag off, /mcp/sse is 404")
    void legacyOff() throws Exception {
        assertThat(get("/mcp/sse", reader).statusCode()).isEqualTo(404);
    }
}
