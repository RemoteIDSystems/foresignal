package ai.foresignal.integrations;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.inbound.InboundIt;
import ai.foresignal.testsupport.integrations.IntegrationDocs;
import ai.foresignal.testsupport.mcp.McpTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * What the agent cookbook and the OpenClaw skill (prompt 034) promise an <b>agent</b>-scope client,
 * proved over a real socket with the MCP SDK's streamable HTTP client: query, create a tagged
 * geofence watch, have a record pushed through a custom source (with an admin-created ingest key)
 * fire it, see the alert reach a signed webhook carrying capability {@code actions}, list it and
 * acknowledge it; create and run a briefing delivered to Slack. And nothing more: no MCP tool for
 * sources, credentials, channels, providers or tokens, and 403 for the REST equivalents.
 *
 * <p>Receivers are WireMock servers. The notification worker is driven with
 * {@link DeliveryWorker#runOnce()}, the clock moving a second per round so rate buckets refill, as
 * in {@code MilestoneE2EIT}. Every client and HTTP call has a timeout.
 */
@WebIntegrationTest
@Import(McpTestSources.class)
class AgentWorkflowIT {

    private static final Instant T = Instant.parse("2026-10-12T12:00:00Z");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String AGENT_NAME = "agent-workflow";
    private static final String HMAC_SECRET = "agent-it-secret";
    private static final String HARBOR = "{\"type\":\"Polygon\",\"coordinates\":[[[-95.03,29.30],[-94.70,29.30],"
            + "[-94.70,29.55],[-95.03,29.55],[-95.03,29.30]]]}";

    @RegisterExtension
    static final WireMockExtension WEBHOOK = receiver();

    @RegisterExtension
    static final WireMockExtension SLACK = receiver();

    private static WireMockExtension receiver() {
        return WireMockExtension.newInstance()
                .options(wireMockConfig().dynamicPort().gzipDisabled(true))
                .failOnUnmatchedRequests(true)
                .resetOnEachTest(true)
                .build();
    }

    @LocalServerPort int port;
    @Autowired MutableClock clock;
    @Autowired TokenService tokens;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired DeliveryWorker deliveryWorker;
    @Autowired ChannelRateLimiter limiter;
    @Autowired JdbcTemplate jdbc;

    private final List<McpSyncClient> clients = new ArrayList<>();
    private String admin;
    private String agent;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        settings.put(SettingKeys.SERVER, new ServerSettings("https://fs.example"));
        limiter.reset();
        admin = "Bearer " + adminTokens.rotate();
        agent = TestTokens.bearer(TestTokens.create(tokens, AGENT_NAME, Scope.AGENT));
        WEBHOOK.stubFor(post(urlEqualTo("/hook")).willReturn(aResponse().withStatus(200)));
        SLACK.stubFor(post(urlEqualTo("/slack/agent")).willReturn(aResponse().withStatus(200).withBody("ok")));
    }

    @AfterEach
    void tearDown() {
        for (McpSyncClient client : clients) {
            try {
                client.closeGracefully();
            } catch (RuntimeException e) {
                // the session may already be gone
            }
        }
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    @Test
    @DisplayName("agent: query → tagged geofence watch → ingest fires it → signed webhook with actions → list → acknowledge")
    void watchToAcknowledgement() throws Exception {
        String sourceId = InboundIt.newId("agent");
        String ingestKey = createSource(sourceId);
        createChannel("{\"name\":\"Agent IT webhook\",\"type\":\"webhook\",\"events\":[\"alert\"],"
                + "\"config\":{\"url\":\"" + WEBHOOK.url("/hook") + "\",\"actions\":{\"enabled\":true}},"
                + "\"secrets\":{\"hmacSecret\":\"" + HMAC_SECRET + "\"}}");
        McpSyncClient client = client(agent);

        // 1. query_records: nothing yet, with a watermark.
        JsonNode empty = structured(call(client, "query_records", Map.of("source", List.of(sourceId))));
        assertThat(empty.path("items").size()).isZero();
        assertThat(empty.path("watermarkSeq").isNumber()).isTrue();

        // 2. create_watch with tags and a geofence, mode enter.
        JsonNode watch = structured(call(client, "create_watch", Map.of("rule", JSON.readValue("{"
                + "\"name\":\"Harbor entry\",\"sources\":[\"" + sourceId + "\"],\"kinds\":[\"vessel\"],"
                + "\"mode\":\"enter\",\"tags\":[\"harbor\",\"agent-it\"],\"severity\":\"warning\","
                + "\"geofence\":" + HARBOR + "}", Map.class))));
        String watchId = watch.path("id").asString();
        assertThat(watchId).isNotBlank();
        assertThat(watch.path("tags").toString()).contains("harbor").contains("agent-it");

        // 3. A matching record through the custom source, with the admin-created ingest key as Bearer.
        HttpResponse<String> ingested = send("POST", "/v1/ingest/" + sourceId, "Bearer " + ingestKey,
                "{\"records\":[{\"externalId\":\"mv-test\",\"kind\":\"vessel\",\"observedAt\":\""
                        + T.minusSeconds(30) + "\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-94.85,29.40]},"
                        + "\"payload\":{\"name\":\"MV TEST\"}}]}");
        assertThat(ingested.statusCode()).as(ingested.body()).isEqualTo(200);
        assertThat(JSON.readTree(ingested.body()).path("persisted").asInt()).isEqualTo(1);

        // 4. list_alerts shows the firing alert of the watch.
        AtomicLong alertId = new AtomicLong();
        await().atMost(TIMEOUT).pollInterval(Duration.ofMillis(200)).until(() -> {
            JsonNode alerts = structured(call(client, "list_alerts", Map.of("status", List.of("firing"),
                    "ruleId", watchId)));
            for (JsonNode item : alerts.path("items")) {
                if ((sourceId + ":mv-test").equals(item.path("record").asString())) {
                    alertId.set(item.path("id").asLong());
                    return true;
                }
            }
            return false;
        });

        // 5. The alert reaches the webhook: HMAC over "<timestamp>.<body>", tags, and capability actions.
        deliverUntil(() -> !WEBHOOK.findAll(postRequestedFor(urlEqualTo("/hook"))
                .withRequestBody(containing("alert.fired"))).isEmpty());
        LoggedRequest hook = WEBHOOK.findAll(postRequestedFor(urlEqualTo("/hook"))
                .withRequestBody(containing("alert.fired"))).getFirst();
        String timestamp = hook.getHeader("X-Foresignal-Timestamp");
        assertThat(hook.getHeader("X-Foresignal-Signature")).isEqualTo("sha256=" + hmac(timestamp, hook.getBody()));
        JsonNode body = JSON.readTree(hook.getBodyAsString());
        assertThat(body.path("subtype").asString()).isEqualTo("alert.fired");
        assertThat(body.path("data").path("alertId").asLong()).isEqualTo(alertId.get());
        assertThat(body.path("data").path("tags").toString()).contains("harbor");
        List<String> actions = new ArrayList<>();
        for (JsonNode action : body.path("actions")) {
            actions.add(action.path("action").asString());
            assertThat(action.path("url").asString()).startsWith("https://fs.example/v1/actions/eac_");
        }
        assertThat(actions).contains("ack");

        // 6. acknowledge_alert: state and acknowledged_by.
        JsonNode acked = structured(call(client, "acknowledge_alert", Map.of("alertId", alertId.get(),
                "note", "handled by the agent")));
        assertThat(acked.path("state").asString()).isEqualTo("acknowledged");
        assertThat(acked.path("acknowledgedBy").asString()).isEqualTo("token:" + AGENT_NAME);
        assertThat(jdbc.queryForObject("SELECT acknowledged_by FROM alert WHERE id = ?", String.class, alertId.get()))
                .isEqualTo("token:" + AGENT_NAME);
        JsonNode rest = JSON.readTree(send("GET", "/v1/alerts/" + alertId.get(), agent, null).body());
        assertThat(rest.path("state").asString()).isEqualTo("acknowledged");
    }

    @Test
    @DisplayName("agent: create_briefing + run_briefing deliver to a Slack channel")
    void briefingToSlack() throws Exception {
        String sourceId = InboundIt.newId("brief");
        createSource(sourceId);
        String slack = createChannel("{\"name\":\"Agent IT Slack\",\"type\":\"slack\",\"events\":[\"briefing\"],"
                + "\"secrets\":{\"webhookUrl\":\"" + SLACK.url("/slack/agent") + "\"}}");
        McpSyncClient client = client(agent);

        JsonNode briefing = structured(call(client, "create_briefing", Map.of("briefing", JSON.readValue("{"
                + "\"name\":\"Agent brief\",\"schedule\":\"0 7 * * *\",\"timezone\":\"UTC\","
                + "\"scope\":{\"sources\":[\"" + sourceId + "\"]},\"mode\":\"digest\","
                + "\"channelIds\":[\"" + slack + "\"]}", Map.class))));
        String briefingId = briefing.path("id").asString();
        assertThat(briefingId).isNotBlank();

        JsonNode run = structured(call(client, "run_briefing", Map.of("briefingId", briefingId)));
        assertThat(run.path("briefingId").asString()).isEqualTo(briefingId);
        assertThat(run.path("status").asString()).isEqualTo("succeeded");

        deliverUntil(() -> !SLACK.findAll(postRequestedFor(urlEqualTo("/slack/agent"))
                .withRequestBody(containing("Agent brief"))).isEmpty());
    }

    @Test
    @DisplayName("agent: no MCP tools for sources, credentials, channels, providers or tokens; REST equivalents are 403")
    void nothingMore() throws Exception {
        String sourceId = InboundIt.newId("deny");
        createSource(sourceId);
        McpSyncClient client = client(agent);
        List<String> names = client.listTools().tools().stream().map(McpSchema.Tool::name).toList();

        assertThat(names).as("agent tools").contains("create_watch", "acknowledge_alert", "snooze_alert",
                "create_briefing", "run_briefing");
        assertThat(names).filteredOn(n -> n.contains("source")).containsOnly("list_sources", "source_health");
        assertThat(names).noneMatch(n -> n.matches(".*(credential|channel|provider|token|setting|ingest|llm|secret).*"));
        // Every tool the skill names is served to an agent token.
        assertThat(names).containsAll(IntegrationDocs.toolNames(
                IntegrationDocs.skill(IntegrationDocs.read(IntegrationDocs.SKILL)).body()));

        Map<String, String[]> forbidden = Map.of(
                "create a custom source", new String[] {"POST", "/v1/custom-sources", "{\"id\":\"custom-x\",\"name\":\"x\",\"kinds\":[\"x\"]}"},
                "change a source", new String[] {"PUT", "/v1/sources/" + sourceId, "{\"enabled\":false}"},
                "set credentials", new String[] {"PUT", "/v1/sources/" + sourceId + "/credentials", "{\"apiKey\":\"x\"}"},
                "create a channel", new String[] {"POST", "/v1/channels", "{\"name\":\"x\",\"type\":\"webhook\"}"},
                "list channels", new String[] {"GET", "/v1/channels", null},
                "list LLM providers", new String[] {"GET", "/v1/llm/providers", null},
                "create a token", new String[] {"POST", "/v1/tokens", "{\"name\":\"x\",\"scope\":\"admin\"}"},
                "list tokens", new String[] {"GET", "/v1/tokens", null},
                "rotate an ingest key", new String[] {"POST", "/v1/custom-sources/" + sourceId + "/ingest-key/rotate", null});
        forbidden.forEach((what, request) -> {
            try {
                HttpResponse<String> response = send(request[0], request[1], agent, request[2]);
                assertThat(response.statusCode()).as("%s with an agent token: %s", what, response.body()).isEqualTo(403);
            } catch (Exception e) {
                throw new AssertionError(what, e);
            }
        });
    }

    // ------------------------------------------------------------------ harness

    /** Creates a custom source with the admin token; returns its ingest key. */
    private String createSource(String sourceId) throws Exception {
        HttpResponse<String> created = send("POST", "/v1/custom-sources", admin, "{\"id\":\"" + sourceId + "\","
                + "\"name\":\"Agent IT\",\"kinds\":[\"vessel\"],\"titleField\":\"name\",\"observedAtMode\":\"UPSTREAM\"}");
        assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
        String key = JSON.readTree(created.body()).path("ingestKey").asString();
        assertThat(key).startsWith("eik_");
        return key;
    }

    /** Creates a channel with the admin token; returns its id. */
    private String createChannel(String json) throws Exception {
        HttpResponse<String> created = send("POST", "/v1/channels", admin, json);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
        return JSON.readTree(created.body()).path("id").asString();
    }

    /** Runs the delivery worker until {@code done} holds, a second of clock per round. */
    private void deliverUntil(Callable<Boolean> done) {
        await().atMost(TIMEOUT).pollInterval(Duration.ofMillis(200)).until(() -> {
            clock.advance(Duration.ofSeconds(1));
            deliveryWorker.runOnce();
            return done.call();
        });
    }

    private McpSyncClient client(String bearer) {
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port)
                .endpoint("/mcp")
                .connectTimeout(Duration.ofSeconds(5))
                .httpRequestCustomizer((builder, method, uri, body, context) -> builder.header("Authorization", bearer))
                .build();
        McpSyncClient client = McpClient.sync(transport).requestTimeout(TIMEOUT).initializationTimeout(TIMEOUT).build();
        clients.add(client);
        client.initialize();
        return client;
    }

    private static McpSchema.CallToolResult call(McpSyncClient client, String tool, Map<String, Object> args) {
        return client.callTool(new McpSchema.CallToolRequest(tool, args));
    }

    private static JsonNode structured(McpSchema.CallToolResult result) {
        assertThat(result.isError()).as(String.valueOf(result.content())).isNotEqualTo(Boolean.TRUE);
        return JSON.valueToTree(result.structuredContent());
    }

    private HttpResponse<String> send(String method, String path, String bearer, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(TIMEOUT);
        if (bearer != null) {
            request.header("Authorization", bearer);
        }
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    /** {@code hex(HMAC-SHA256(secret, timestamp + "." + body))}, computed independently of production code. */
    private static String hmac(String timestamp, byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(HMAC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(mac.doFinal(body));
    }
}
