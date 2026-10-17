package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.forecast.ForecastFilter;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastStatus;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.ForecastsTopic;
import ai.foresignal.forecast.resolve.ResolutionService;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.CreateQuestionTool;
import ai.foresignal.mcp.tools.GetQuestionTool;
import ai.foresignal.mcp.tools.GetScorecardTool;
import ai.foresignal.mcp.tools.ListQuestionsTool;
import ai.foresignal.mcp.tools.SubmitForecastTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.stream.sse.FifoEvent;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
 * The prompt 122 endpoints over HTTP, the SSE topic and the MCP tools: question lifecycle and dedup, scopes, the
 * operator forecaster per principal, forecast immutability (supersede, never edit; server-set times), the
 * immutability of criteria once forecast, annulment, manual resolution and scores, history, forecasters,
 * scorecards, calibration, comparison, the refit, the resolution job's closing and manual notification, the
 * {@code forecasts} topic and the tool shapes.
 */
@IntegrationTest
class ForecastControllersIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;
    @Autowired ForecasterRegistry registry;
    @Autowired ForecastService ledger;
    @Autowired ResolutionService resolution;
    @Autowired ForecastsTopic topic;
    @Autowired ListQuestionsTool listQuestions;
    @Autowired GetQuestionTool getQuestion;
    @Autowired GetScorecardTool getScorecard;
    @Autowired CreateQuestionTool createQuestion;
    @Autowired SubmitForecastTool submitForecast;

    String read;
    String agent;
    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        registry.registerBuiltIns();
        read = TestTokens.bearer(TestTokens.create(tokens, "fc-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "fc-agent", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "fc-admin", Scope.ADMIN));
    }

    static String manualQuestion(String dedup, String type, String outcomes) {
        return "{\"title\":\"Will the ceasefire hold?\",\"type\":\"" + type + "\"" + (outcomes == null ? ""
                : ",\"outcomes\":" + outcomes) + ",\"resolutionCriteria\":\"Yes if no party announces its end.\","
                + "\"resolver\":{\"type\":\"manual\"},\"closesAt\":\"2026-10-24T00:00:00Z\","
                + "\"resolveBy\":\"2026-10-31T00:00:00Z\",\"category\":\"conflict\",\"tags\":[\"test\"]"
                + (dedup == null ? "" : ",\"dedupKey\":\"" + dedup + "\"") + "}";
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

    String open(String dedup) throws Exception {
        MockHttpServletResponse created = postJson(agent, "/v1/forecasts/questions", manualQuestion(dedup, "binary",
                null));
        assertThat(created.getStatus()).isEqualTo(201);
        return body(created).get("id").asString();
    }

    @Test
    @DisplayName("questions: agent opens (201), the same dedupKey returns it (200), read cannot open, bad resolvers 400")
    void questions() throws Exception {
        String id = open("ceasefire-1");
        MockHttpServletResponse again = postJson(agent, "/v1/forecasts/questions", manualQuestion("ceasefire-1",
                "binary", null));
        assertThat(again.getStatus()).isEqualTo(200);
        assertThat(body(again).get("id").asString()).isEqualTo(id);
        assertThat(postJson(read, "/v1/forecasts/questions", manualQuestion(null, "binary", null)).getStatus())
                .isEqualTo(403);
        MockHttpServletResponse bad = postJson(agent, "/v1/forecasts/questions", manualQuestion(null, "binary", null)
                .replace("{\"type\":\"manual\"}", "{\"type\":\"event-exists\",\"selector\":{\"rule\":{}},"
                        + "\"window\":{\"start\":\"2026-10-18T00:00:00Z\"}}"));
        assertThat(bad.getStatus()).isEqualTo(400);
        assertThat(body(bad).get("code").asString()).isEqualTo("RESOLVER_INVALID");
        JsonNode q = body(call(read, get("/v1/forecasts/questions/" + id)));
        assertThat(q.get("origin").asString()).isEqualTo("agent");
        assertThat(q.get("resolutionCriteria").asString()).isEqualTo("Yes if no party announces its end.");
        assertThat(q.get("resolver").get("version").asString()).isEqualTo("1.0.0");
        assertThat(q.get("latestForecasts").isArray()).isTrue();
        JsonNode list = body(call(read, get("/v1/forecasts/questions?status=open&resolver=manual")));
        assertThat(list.get("items").size()).isEqualTo(1);
        assertThat(body(call(read, get("/v1/forecasts/questions?status=resolved"))).get("items").size()).isZero();
        assertThat(call(read, get("/v1/forecasts/questions?status=bogus")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/forecasts/questions/01JA8Q3V7S2K9W4T6Y1B5N0M3C")).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("forecasts: operator per principal, server-set times, supersede not edit, ensemble refreshed")
    void forecasts() throws Exception {
        String id = open(null);
        assertThat(postJson(null, "/v1/forecasts/questions/" + id + "/forecasts", "{\"probability\":0.6}")
                .getStatus()).isEqualTo(401);
        MockHttpServletResponse first = postJson(read, "/v1/forecasts/questions/" + id + "/forecasts",
                "{\"probability\":0.6,\"rationale\":\"Base rate 0.5, adjusted up.\"}");
        assertThat(first.getStatus()).isEqualTo(201);
        JsonNode f1 = body(first);
        String operator = f1.get("forecasterId").asString();
        assertThat(operator).startsWith("operator:");
        assertThat(f1.get("forecasterType").asString()).isEqualTo("operator");
        assertThat(f1.get("madeAt").asString()).isEqualTo(NOW.toString());
        assertThat(f1.get("estimativeTerm").asString()).isEqualTo("likely");
        assertThat(f1.get("manifestId").isNull()).isFalse();
        assertThat(f1.get("supersedesId").isNull()).isTrue();
        // A client-chosen time is not a member of ForecastInput.
        MockHttpServletResponse timed = postJson(read, "/v1/forecasts/questions/" + id + "/forecasts",
                "{\"probability\":0.6,\"madeAt\":\"2020-01-01T00:00:00Z\"}");
        assertThat(timed.getStatus()).isEqualTo(400);
        assertThat(body(timed).get("code").asString()).isEqualTo("FORECAST_INVALID");
        clock.setInstant(NOW.plus(Duration.ofHours(1)));
        JsonNode f2 = body(postJson(read, "/v1/forecasts/questions/" + id + "/forecasts", "{\"probability\":0.3}"));
        assertThat(f2.get("supersedesId").asString()).isEqualTo(f1.get("id").asString());
        assertThat(f2.get("forecasterId").asString()).isEqualTo(operator);
        JsonNode all = body(call(read, get("/v1/forecasts/questions/" + id + "/forecasts?forecaster=" + operator)));
        assertThat(all.get("items").size()).isEqualTo(2);
        assertThat(all.get("items").get(1).get("probability").asDouble()).isEqualTo(0.6);
        JsonNode q = body(call(read, get("/v1/forecasts/questions/" + id)));
        assertThat(q.get("ensemble").get("forecasterId").asString()).isEqualTo("ensemble");
        assertThat(q.get("ensembleProbability").asDouble()).isEqualTo(0.3, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(q.get("estimativeTerm").asString()).isEqualTo("unlikely");
        assertThat(body(call(read, get("/v1/forecasts/forecasters?type=operator"))).get("items").get(0).get("id")
                .asString()).isEqualTo(operator);
        // Read scope may not name another forecaster; agent may name only operator or external forecasters.
        assertThat(body(postJson(read, "/v1/forecasts/questions/" + id + "/forecasts",
                "{\"probability\":0.3,\"forecasterId\":\"operator:someone-else\"}")).get("code").asString())
                .isEqualTo("FORECAST_INVALID");
        assertThat(postJson(agent, "/v1/forecasts/questions/" + id + "/forecasts",
                "{\"probability\":0.3,\"forecasterId\":\"base-rate\"}").getStatus()).isEqualTo(404);
        assertThat(postJson(agent, "/v1/forecasts/questions/" + id + "/forecasts",
                "{\"probability\":0.3,\"forecasterId\":\"" + operator + "\"}").getStatus()).isEqualTo(201);
        // History: one point per day per forecaster.
        JsonNode history = body(call(read, get("/v1/forecasts/questions/" + id + "/history")));
        assertThat(history.get("step").asString()).isEqualTo("P1D");
        assertThat(history.get("series").size()).isGreaterThanOrEqualTo(2);
        assertThat(call(read, get("/v1/forecasts/questions/" + id + "/history?step=1h")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("criteria and resolver are immutable once forecast; title changes; annul then 409 everywhere")
    void immutabilityAndAnnulment() throws Exception {
        String id = open(null);
        assertThat(call(agent, patch("/v1/forecasts/questions/" + id).contentType("application/merge-patch+json")
                .content("{\"resolutionCriteria\":\"Changed before any forecast.\"}")).getStatus()).isEqualTo(200);
        postJson(read, "/v1/forecasts/questions/" + id + "/forecasts", "{\"probability\":0.5}");
        MockHttpServletResponse locked = call(agent, patch("/v1/forecasts/questions/" + id)
                .contentType("application/merge-patch+json").content("{\"resolutionCriteria\":\"Changed later.\"}"));
        assertThat(locked.getStatus()).isEqualTo(409);
        assertThat(body(locked).get("code").asString()).isEqualTo("QUESTION_IMMUTABLE");
        assertThat(body(call(agent, patch("/v1/forecasts/questions/" + id).contentType("application/merge-patch+json")
                .content("{\"title\":\"Will the truce hold?\"}"))).get("title").asString())
                .isEqualTo("Will the truce hold?");
        JsonNode annulled = body(postJson(agent, "/v1/forecasts/questions/" + id + "/annul",
                "{\"reason\":\"Duplicate question.\"}"));
        assertThat(annulled.get("status").asString()).isEqualTo("annulled");
        assertThat(annulled.get("annulment").get("code").asString()).isEqualTo("OPERATOR");
        assertThat(postJson(agent, "/v1/forecasts/questions/" + id + "/annul", "{\"reason\":\"Again.\"}").getStatus())
                .isEqualTo(409);
        MockHttpServletResponse closed = postJson(read, "/v1/forecasts/questions/" + id + "/forecasts",
                "{\"probability\":0.5}");
        assertThat(closed.getStatus()).isEqualTo(409);
        assertThat(body(closed).get("code").asString()).isEqualTo("QUESTION_CLOSED");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM forecast_score WHERE question_id = ?", Long.class, id))
                .isZero();
    }

    @Test
    @DisplayName("manual resolution scores every forecaster; scorecard, calibration, comparison and track records")
    void resolveAndScore() throws Exception {
        String id = open(null);
        postJson(read, "/v1/forecasts/questions/" + id + "/forecasts", "{\"probability\":0.8}");
        clock.setInstant(NOW.plus(Duration.ofDays(2)));
        MockHttpServletResponse invalid = postJson(agent, "/v1/forecasts/questions/" + id + "/resolve",
                "{\"outcome\":\"maybe\",\"evidence\":[\"alert:1\"]}");
        assertThat(body(invalid).get("code").asString()).isEqualTo("FORECAST_INVALID");
        assertThat(postJson(read, "/v1/forecasts/questions/" + id + "/resolve",
                "{\"outcome\":\"yes\",\"evidence\":[\"alert:1\"]}").getStatus()).isEqualTo(403);
        JsonNode resolved = body(postJson(agent, "/v1/forecasts/questions/" + id + "/resolve",
                "{\"outcome\":\"yes\",\"evidence\":[\"alert:1\"],\"note\":\"Confirmed.\"}"));
        assertThat(resolved.get("status").asString()).isEqualTo("resolved");
        assertThat(resolved.get("resolvedOutcome").asString()).isEqualTo("yes");
        assertThat(resolved.get("resolution").get("evidence").get(0).asString()).isEqualTo("alert:1");
        JsonNode scores = resolved.get("scores");
        assertThat(scores.size()).isGreaterThanOrEqualTo(1);
        JsonNode mine = null;
        for (JsonNode s : scores) {
            if (s.get("forecasterId").asString().startsWith("operator:")) {
                mine = s;
            }
        }
        assertThat(mine.get("brier").asDouble()).isEqualTo(0.04, org.assertj.core.data.Offset.offset(1e-12));
        assertThat(mine.get("scorerVersion").asString()).isEqualTo("1.0.0");
        String operator = mine.get("forecasterId").asString();
        JsonNode card = body(call(read, get("/v1/forecasts/scorecard?forecaster=" + operator)));
        assertThat(card.get("entries").get(0).get("nResolved").asInt()).isEqualTo(1);
        assertThat(card.get("entries").get(0).get("label").asString()).isEqualTo("insufficient");
        JsonNode cal = body(call(read, get("/v1/forecasts/calibration?forecaster=" + operator)));
        assertThat(cal.get("method").asString()).isEqualTo("bins");
        assertThat(cal.get("bins").size()).isEqualTo(10);
        JsonNode cmp = body(call(read, get("/v1/forecasts/compare?a=" + operator + "&b=ensemble")));
        assertThat(cmp.get("nShared").asInt()).isEqualTo(1);
        assertThat(call(read, get("/v1/forecasts/compare?a=nobody&b=ensemble")).getStatus()).isEqualTo(404);
        assertThat(call(read, get("/v1/forecasts/scorecard?forecaster=nobody")).getStatus()).isEqualTo(404);
        JsonNode forecasters = body(call(read, get("/v1/forecasts/forecasters?type=operator")));
        assertThat(forecasters.get("items").get(0).get("trackRecord").get("nResolved").asInt()).isEqualTo(1);
        assertThat(postJson(agent, "/v1/forecasts/questions/" + id + "/resolve",
                "{\"outcome\":\"no\",\"evidence\":[\"alert:1\"]}").getStatus()).isEqualTo(409);
        JsonNode refit = body(call(agent, post("/v1/forecasts/ensemble/refit")));
        assertThat(refit.get("extremizing").get("enabled").asBoolean()).isFalse();
        assertThat(refit.has("platt")).isTrue();
    }

    @Test
    @DisplayName("forecasters: built-ins listed; admin sets active and config, agent cannot")
    void forecasters() throws Exception {
        JsonNode list = body(call(read, get("/v1/forecasts/forecasters")));
        List<String> ids = new ArrayList<>();
        list.get("items").forEach(f -> ids.add(f.get("id").asString()));
        assertThat(ids).contains("base-rate", "climatology", "persistence", "precursor", "analog", "ensemble");
        String body = "{\"active\":false,\"config\":{\"maxAgeDays\":3}}";
        assertThat(call(agent, put("/v1/forecasts/forecasters/ensemble").contentType(MediaType.APPLICATION_JSON)
                .content(body)).getStatus()).isEqualTo(403);
        JsonNode updated = body(call(admin, put("/v1/forecasts/forecasters/ensemble")
                .contentType(MediaType.APPLICATION_JSON).content(body)));
        assertThat(updated.get("active").asBoolean()).isFalse();
        assertThat(updated.get("config").get("maxAgeDays").asInt()).isEqualTo(3);
        assertThat(call(admin, put("/v1/forecasts/forecasters/nobody").contentType(MediaType.APPLICATION_JSON)
                .content(body)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("the resolution job closes past-due questions and asks for manual resolutions once")
    void resolutionJob() throws Exception {
        String id = open(null);
        clock.setInstant(Instant.parse("2026-10-25T00:00:00Z"));
        ResolutionService.RunResult first = resolution.runOnce();
        assertThat(first.closed()).isEqualTo(1);
        assertThat(ledger.find(id).orElseThrow().getStatus()).isEqualTo(ForecastStatus.CLOSED);
        clock.setInstant(Instant.parse("2026-10-31T01:00:00Z"));
        assertThat(resolution.runOnce().notified()).isEqualTo(1);
        assertThat(resolution.runOnce().notified()).isZero();
        ForecastQuestionEntity q = ledger.find(id).orElseThrow();
        assertThat(q.getResolutionRequestedAt()).isNotNull();
        JsonNode due = body(call(read, get("/v1/forecasts/questions?status=closed&resolver=manual")));
        assertThat(due.get("items").get(0).get("id").asString()).isEqualTo(id);
    }

    @Test
    @DisplayName("the forecasts topic: lifecycle and forecast events in order; private questions need private access")
    void topic() throws Exception {
        String id = open(null);
        postJson(read, "/v1/forecasts/questions/" + id + "/forecasts", "{\"probability\":0.6}");
        MockHttpServletResponse hidden = postJson(admin, "/v1/forecasts/questions", manualQuestion(null, "binary",
                null).replace("\"tags\":[\"test\"]", "\"tags\":[\"test\"],\"private\":true"));
        String privateId = body(hidden).get("id").asString();
        List<FifoEvent> open = new ArrayList<>();
        topic.replay(new ReplayRequest(0, false), new ForecastFilter(false), e -> open.add(e));
        assertThat(open).extracting(FifoEvent::event).contains("question.opened", "forecast.submitted");
        assertThat(open).noneMatch(e -> String.valueOf(e.data()).contains(privateId));
        List<FifoEvent> all = new ArrayList<>();
        topic.replay(new ReplayRequest(0, false), new ForecastFilter(true), e -> all.add(e));
        assertThat(all).anyMatch(e -> String.valueOf(e.data()).contains(privateId));
        assertThat(topic.head()).isEqualTo(all.get(all.size() - 1).cursor());
        assertThat(call(read, get("/v1/forecasts/questions/" + privateId)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("MCP: list_questions, get_question and get_scorecard are read tools; create_question and"
            + " submit_forecast are agent tools with the API shapes")
    void mcpTools() throws Exception {
        assertThat(listQuestions.requiredScope()).isEqualTo(Scope.READ);
        assertThat(getQuestion.requiredScope()).isEqualTo(Scope.READ);
        assertThat(getScorecard.requiredScope()).isEqualTo(Scope.READ);
        assertThat(createQuestion.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(submitForecast.requiredScope()).isEqualTo(Scope.AGENT);
        ToolContext ctx = new ToolContext(caller(), null, "test");
        ToolResult created = createQuestion.call(ctx, json.readTree(manualQuestion("mcp-1", "binary", null)));
        String id = created.structured().get("id").asString();
        assertThat(created.structured().get("created").asBoolean()).isTrue();
        ToolResult forecast = submitForecast.call(ctx, json.readTree("{\"questionId\":\"" + id
                + "\",\"probability\":0.25}"));
        assertThat(forecast.structured().get("forecasterId").asString()).isEqualTo("operator:fc-mcp");
        assertThat(String.valueOf(listQuestions.call(ctx, json.readTree("{\"status\":[\"open\"]}")).structured()))
                .contains(id);
        assertThat(getQuestion.call(ctx, json.readTree("{\"questionId\":\"" + id + "\"}")).structured()
                .get("resolutionCriteria").asString()).isEqualTo("Yes if no party announces its end.");
        assertThat(getScorecard.call(ctx, json.readTree("{}")).structured().has("entries")).isTrue();
    }

    McpCaller caller() {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "fc-mcp", Scope.AGENT,
                TokenRestrictions.NONE, null, null, false, false);
        return new McpCaller(token, new ReadCaller(Scope.AGENT, true, false, ReadRestriction.NONE, false), Set.of(),
                null);
    }
}
