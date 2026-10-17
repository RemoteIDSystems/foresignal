package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.benchmark.BenchmarkInputs;
import ai.foresignal.benchmark.BenchmarkRunRepository;
import ai.foresignal.benchmark.BenchmarkService;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.GetBenchmarkStatusTool;
import ai.foresignal.mcp.tools.RunBenchmarkTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 123 endpoints and MCP tools: starting runs (agent, 202, 409 {@code BENCHMARK_IN_PROGRESS}, validation),
 * listing and reading runs and the status per component (read), the historical corpus (agent: create with the source
 * by scope, list by source, delete, 404s), and the shapes and scopes of {@code get_benchmark_status} and
 * {@code run_benchmark}.
 */
@IntegrationTest
class BenchmarkControllersIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;
    @Autowired BenchmarkService service;
    @Autowired BenchmarkRunRepository runs;
    @Autowired GetBenchmarkStatusTool statusTool;
    @Autowired RunBenchmarkTool runTool;

    String read;
    String agent;
    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        settings.put(SettingKeys.BENCHMARK_THRESHOLDS, Map.of("suite", Map.of("replicates", 1)));
        read = TestTokens.bearer(TestTokens.create(tokens, "bench-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "bench-agent", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "bench-admin", Scope.ADMIN));
    }

    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    MockHttpServletResponse postJson(String bearer, String path, String body) throws Exception {
        return call(bearer, post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    /** Waits until no run is queued or running. */
    void awaitIdle() throws Exception {
        for (int i = 0; i < 1200 && !service.inProgress().isEmpty(); i++) {
            Thread.sleep(250);
        }
        assertThat(service.inProgress()).isEmpty();
    }

    @Test
    @DisplayName("runs: agent starts (202), a second start is 409, read lists and reads, validation and 404s")
    void runs() throws Exception {
        assertThat(postJson(read, "/v1/benchmarks/runs", "{\"suite\":\"synthetic\"}").getStatus()).isEqualTo(403);
        MockHttpServletResponse started = postJson(agent, "/v1/benchmarks/runs",
                "{\"suite\":\"synthetic\",\"components\":[\"change-point\"],\"seed\":7}");
        assertThat(started.getStatus()).isEqualTo(202);
        JsonNode run = body(started);
        String id = run.get("id").asString();
        assertThat(run.get("suite").asString()).isEqualTo("synthetic");
        assertThat(run.get("suiteVersion").asString()).isEqualTo("bench-scenarios 1.0.0");
        assertThat(run.get("seed").asLong()).isEqualTo(7);
        assertThat(run.get("components").get(0).asString()).isEqualTo("change-point");
        assertThat(run.get("jobStatus").asString()).isIn("queued", "running", "done");
        MockHttpServletResponse again = postJson(agent, "/v1/benchmarks/runs", "{\"suite\":\"synthetic\"}");
        if (again.getStatus() == 409) {
            assertThat(body(again).get("code").asString()).isEqualTo("BENCHMARK_IN_PROGRESS");
        } else {
            // The first run had already finished; the second is accepted.
            assertThat(again.getStatus()).isEqualTo(202);
        }
        awaitIdle();

        JsonNode done = body(call(read, get("/v1/benchmarks/runs/" + id)));
        assertThat(done.get("jobStatus").asString()).isEqualTo("done");
        assertThat(done.get("status").asString()).isIn("pass", "warn", "fail");
        assertThat(done.get("results").get(0).get("component").asString()).isEqualTo("change-point");
        assertThat(done.get("results").get(0).get("scenarios").size()).isEqualTo(5);
        assertThat(done.get("manifestId").isNull()).isFalse();
        JsonNode list = body(call(read, get("/v1/benchmarks/runs?suite=synthetic&limit=1")));
        assertThat(list.get("items").size()).isEqualTo(1);
        assertThat(call(read, get("/v1/benchmarks/runs?suite=nope")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/benchmarks/runs?status=maybe")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/benchmarks/runs?cursor=zzz")).getStatus()).isEqualTo(400);
        MockHttpServletResponse missing = call(read, get("/v1/benchmarks/runs/" + Ulids.next(NOW)));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_BENCHMARK_RUN");

        assertThat(postJson(agent, "/v1/benchmarks/runs", "{\"suite\":\"weekly\"}").getStatus()).isEqualTo(400);
        assertThat(postJson(agent, "/v1/benchmarks/runs", "{\"suite\":\"synthetic\",\"components\":[\"oracle\"]}")
                .getStatus()).isEqualTo(400);
        assertThat(postJson(agent, "/v1/benchmarks/runs", "{\"suite\":\"synthetic\",\"extra\":1}").getStatus())
                .isEqualTo(400);

        JsonNode status = body(call(read, get("/v1/benchmarks/status")));
        assertThat(status.has("generatedAt")).isTrue();
        assertThat(status.get("components").size()).isEqualTo(BenchmarkInputs.COMPONENTS.size());
        JsonNode first = status.get("components").get(0);
        assertThat(first.get("component").asString()).isEqualTo("change-point");
        assertThat(first.get("status").asString()).isIn("pass", "warn", "fail");
        assertThat(first.get("runId").asString()).isNotBlank();
        assertThat(first.get("gating").has("proposerAutoExecution")).isTrue();
    }

    @Test
    @DisplayName("cases: agent creates (source agent), admin creates (source operator), list by source, delete, 404s")
    void cases() throws Exception {
        String body = "{\"name\":\"Strait disruption\",\"windowFrom\":\"2024-01-01T00:00:00Z\","
                + "\"windowTo\":\"2024-02-01T00:00:00Z\",\"regionCodes\":[\"YE\"],\"expectations\":{\"findings\":"
                + "[{\"findingType\":\"change-point\",\"present\":true,\"seriesRef\":\"series:src/x\"}]}}";
        assertThat(call(read, get("/v1/benchmarks/cases")).getStatus()).isEqualTo(403);
        assertThat(postJson(read, "/v1/benchmarks/cases", body).getStatus()).isEqualTo(403);
        MockHttpServletResponse byAgent = postJson(agent, "/v1/benchmarks/cases", body);
        assertThat(byAgent.getStatus()).isEqualTo(201);
        JsonNode created = body(byAgent);
        assertThat(created.get("source").asString()).isEqualTo("agent");
        assertThat(created.get("regionCodes").get(0).asString()).isEqualTo("YE");
        assertThat(created.get("expectations").get("findings").get(0).get("present").asBoolean()).isTrue();
        MockHttpServletResponse byAdmin = postJson(admin, "/v1/benchmarks/cases", body);
        assertThat(body(byAdmin).get("source").asString()).isEqualTo("operator");
        assertThat(body(call(agent, get("/v1/benchmarks/cases"))).get("items").size()).isEqualTo(2);
        assertThat(body(call(agent, get("/v1/benchmarks/cases?source=operator"))).get("items").size()).isEqualTo(1);
        assertThat(call(agent, get("/v1/benchmarks/cases?source=robot")).getStatus()).isEqualTo(400);
        assertThat(postJson(agent, "/v1/benchmarks/cases", body.replace("2024-02-01", "2023-12-01")).getStatus())
                .isEqualTo(400);
        assertThat(postJson(agent, "/v1/benchmarks/cases", body.replace("\"YE\"", "\"Yemen\"")).getStatus())
                .isEqualTo(400);
        assertThat(postJson(agent, "/v1/benchmarks/cases", body.replace("\"present\":true,", "")).getStatus())
                .isEqualTo(400);
        String id = created.get("id").asString();
        assertThat(call(agent, delete("/v1/benchmarks/cases/" + id)).getStatus()).isEqualTo(204);
        MockHttpServletResponse gone = call(agent, delete("/v1/benchmarks/cases/" + id));
        assertThat(gone.getStatus()).isEqualTo(404);
        assertThat(body(gone).get("code").asString()).isEqualTo("UNKNOWN_BENCHMARK_CASE");
    }

    @Test
    @DisplayName("MCP: get_benchmark_status is a read tool, run_benchmark an agent tool, with the API shapes")
    void mcpTools() throws Exception {
        assertThat(statusTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(runTool.requiredScope()).isEqualTo(Scope.AGENT);
        ToolContext ctx = new ToolContext(caller(), null, "test");
        ToolResult status = statusTool.call(ctx, json.readTree("{}"));
        assertThat(status.structured().get("overall").asString()).isEqualTo("unknown");
        assertThat(status.structured().get("components").size()).isEqualTo(BenchmarkInputs.COMPONENTS.size());
        ToolResult queued = runTool.call(ctx, json.readTree("{\"suite\":\"synthetic\",\"components\":[\"evidence\","
                + "\"canonical-linking\"]}"));
        String id = queued.structured().get("id").asString();
        assertThat(queued.structured().get("components").size()).isEqualTo(2);
        assertThat(runs.findById(id)).isPresent();
        assertThat(runs.findById(id).orElseThrow().getTrigger()).isEqualTo("agent");
        if (!service.inProgress().isEmpty()) {
            assertThatThrownBy(() -> runTool.call(ctx, json.readTree("{\"suite\":\"synthetic\"}")))
                    .isInstanceOf(ConflictException.class);
        }
        awaitIdle();
        assertThat(statusTool.call(ctx, json.readTree("{}")).structured().get("overall").asString())
                .isIn("pass", "warn", "fail");
    }

    McpCaller caller() {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "bench-mcp", Scope.AGENT,
                TokenRestrictions.NONE, null, null, false, false);
        return new McpCaller(token, new ReadCaller(Scope.AGENT, true, false, ReadRestriction.NONE, false), Set.of(),
                null);
    }
}
