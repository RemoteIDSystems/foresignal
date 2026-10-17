package ai.foresignal.outlook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.FitIndexTool;
import ai.foresignal.mcp.tools.GetIndexTool;
import ai.foresignal.mcp.tools.GetOutlookTool;
import ai.foresignal.mcp.tools.GetSeriesForecastTool;
import ai.foresignal.outlook.index.RegionalIndexService;
import ai.foresignal.outlook.regional.RegionalOutlookEntity;
import ai.foresignal.outlook.regional.RegionalOutlookRepository;
import ai.foresignal.outlook.series.SeriesForecastEntity;
import ai.foresignal.outlook.series.SeriesForecastModelEntity;
import ai.foresignal.outlook.series.SeriesForecastModelRepository;
import ai.foresignal.outlook.series.SeriesForecastRepository;
import ai.foresignal.outlook.series.SeriesForecastViews;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 126 endpoints over HTTP and the MCP tools: series forecasts and models (409 before a method exists;
 * standardized units for a {@code LOCAL_ONLY} series off local paths), outlooks (list, detail, 404), indices (list
 * with skill and weights origin, PUT 201/200, validation, 404, fit 409 while not ready, weights), the scopes
 * ({@code read} reads, {@code agent} changes) and the tool shapes.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class OutlookControllersIT extends AnalyticsItSupport {

    static final String SERIES = "series:" + AnalyticsTestSources.SERIES + "/oil";

    static final String LOCAL = "series:" + AnalyticsTestSources.LOCAL + "/secret";

    static final String INDEX = """
            {"name": "Unrest", "components": [
              {"key": "conflict", "ref": "rollup:day/*/conflict-event/{region}", "transform": "pctrank",
               "window": "P28D", "direction": 1},
              {"key": "outages", "ref": "rollup:day/*/internet-outage/{region}", "transform": "pctrank",
               "window": "P7D", "direction": 1}],
             "weights": {"conflict": 0.7, "outages": 0.3}, "target": "conflict:battles", "enabled": true}
            """;

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired RegionalIndexService indices;
    @Autowired RegionalOutlookRepository outlooks;
    @Autowired SeriesForecastRepository forecasts;
    @Autowired SeriesForecastModelRepository models;
    @Autowired GetSeriesForecastTool getSeriesForecast;
    @Autowired GetOutlookTool getOutlook;
    @Autowired GetIndexTool getIndex;
    @Autowired FitIndexTool fitIndex;

    String read;
    String agent;

    @BeforeEach
    void setUp() {
        read = TestTokens.bearer(TestTokens.create(tokens, "outlook-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "outlook-agent", Scope.AGENT));
        seedDaily(AnalyticsTestSources.SERIES, "oil", T0, constant(10, 70), SeriesMeta.of(SeriesMeta.PRICE,
                SeriesMeta.LOG_RETURN, 1));
        seedDaily(AnalyticsTestSources.LOCAL, "secret", T0, constant(10, 30), SeriesMeta.of(SeriesMeta.PRICE,
                SeriesMeta.LOG_RETURN, 1));
        indices.ensureBuiltIns();
    }

    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    void forecastRows(String ref, boolean localOnly) {
        Instant made = T0.plus(Duration.ofDays(11));
        SeriesForecastModelEntity model = new SeriesForecastModelEntity(ref, "ets-ann", made, "series-forecast 1.0.0");
        model.setParams(Map.of("alpha", 0.3, "l0", 30.0));
        model.setBacktest(Map.of("origins", 8, "spacing", "P7D", "selectionHorizon", 7, "results", List.of(Map.of(
                "method", "ets-ann", "horizon", 7, "meanPinballLoss", 0.4))));
        models.save(model);
        for (int h : new int[] {1, 7, 28}) {
            Instant start = T0.plus(Duration.ofDays(10 + h));
            SeriesForecastEntity row = new SeriesForecastEntity(Ulids.next(made), ref, made, 5, h, start,
                    start.plus(Duration.ofDays(1)), "ets-ann", "series-forecast 1.0.0");
            Map<String, Double> q = new LinkedHashMap<>();
            double[] v = {26, 27, 28.5, 30, 31.5, 33, 34};
            for (int i = 0; i < v.length; i++) {
                q.put(ai.foresignal.forecast.Ledger.QUANTILES.get(i), v[i]);
            }
            row.setQuantiles(q);
            row.setTails(Map.of("p01", 25.0, "p99", 35.0));
            row.setStandardization(Map.of("mean", 30.0, "sd", 2.0));
            row.setLocalOnly(localOnly);
            forecasts.save(row);
        }
    }

    @Test
    @DisplayName("series forecasts: 409 before a model, then three horizons; one horizon on request; 400 for others")
    void seriesForecast() throws Exception {
        MockHttpServletResponse none = call(read, get("/v1/analytics/series/forecast").param("ref", SERIES));
        assertThat(none.getStatus()).isEqualTo(409);
        assertThat(body(none).get("code").asString()).isEqualTo("FORECAST_MODEL_NOT_READY");
        assertThat(call(read, get("/v1/analytics/series/forecast/model").param("ref", SERIES)).getStatus())
                .isEqualTo(409);
        forecastRows(SERIES, false);
        JsonNode f = body(call(read, get("/v1/analytics/series/forecast").param("ref", SERIES)));
        assertThat(f.get("forecasts")).hasSize(3);
        assertThat(f.get("units").asString()).isEqualTo("native");
        assertThat(f.get("method").asString()).isEqualTo("ets-ann");
        assertThat(f.get("forecasts").get(0).get("quantiles").get("p50").asDouble()).isEqualTo(30);
        JsonNode one = body(call(read, get("/v1/analytics/series/forecast").param("ref", SERIES).param("horizon",
                "7")));
        assertThat(one.get("forecasts")).hasSize(1);
        assertThat(call(read, get("/v1/analytics/series/forecast").param("ref", SERIES).param("horizon", "3"))
                .getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/analytics/series/forecast").param("ref", "nonsense")).getStatus())
                .isEqualTo(400);
        assertThat(call(read, get("/v1/analytics/series/forecast").param("ref", "series:an-series/missing"))
                .getStatus()).isEqualTo(404);
        JsonNode model = body(call(read, get("/v1/analytics/series/forecast/model").param("ref", SERIES)));
        assertThat(model.get("method").asString()).isEqualTo("ets-ann");
        assertThat(model.get("backtest").get("results")).hasSize(1);
        assertThat(model.get("params").get("alpha").asDouble()).isEqualTo(0.3);
    }

    @Test
    @DisplayName("LOCAL_ONLY: API reads are local paths (native); off them the forecast is standardized")
    void localOnly() throws Exception {
        forecastRows(LOCAL, true);
        JsonNode local = body(call(read, get("/v1/analytics/series/forecast").param("ref", LOCAL)));
        assertThat(local.get("units").asString()).isEqualTo("native");
        Map<String, Object> standardized = SeriesForecastViews.forecast(forecasts.findAll().stream()
                .filter(r -> r.getRef().equals(LOCAL)).sorted(java.util.Comparator.comparingInt(
                        SeriesForecastEntity::getHorizon)).toList(), null, true, List.of());
        assertThat(standardized).containsEntry("units", "standardised").containsEntry("unit", null);
        @SuppressWarnings("unchecked")
        Map<String, Object> first = ((List<Map<String, Object>>) standardized.get("forecasts")).get(0);
        @SuppressWarnings("unchecked")
        Map<String, Double> q = (Map<String, Double>) first.get("quantiles");
        assertThat(q).containsEntry("p50", 0.0).containsEntry("p95", 2.0);
        assertThat(String.valueOf(standardized)).doesNotContain("33.0").contains("LOCAL_ONLY_WITHHELD");
        assertThat(SeriesForecastViews.model(models.findById(LOCAL).orElseThrow(), true).get("params"))
                .isEqualTo(Map.of("withheld", true));
    }

    @Test
    @DisplayName("indices: built-ins listed with skill and weights origin; PUT 201 then 200; validation; scopes")
    void indicesCrud() throws Exception {
        JsonNode list = body(call(read, get("/v1/indices")));
        List<String> ids = new ArrayList<>();
        list.get("items").forEach(i -> ids.add(i.get("id").asString()));
        assertThat(ids).contains("instability", "maritime-risk", "infrastructure-stress");
        JsonNode instability = list.get("items").get(ids.indexOf("instability"));
        assertThat(instability.get("enabled").asBoolean()).isFalse();
        assertThat(instability.get("builtIn").asBoolean()).isTrue();
        assertThat(instability.get("weightsOrigin").asString()).isEqualTo("default");
        assertThat(instability.has("skill")).isTrue();
        assertThat(instability.get("ready").asBoolean()).isFalse();
        assertThat(call(read, put("/v1/indices/unrest").contentType(MediaType.APPLICATION_JSON).content(INDEX))
                .getStatus()).isEqualTo(403);
        MockHttpServletResponse created = call(agent, put("/v1/indices/unrest")
                .contentType(MediaType.APPLICATION_JSON).content(INDEX));
        assertThat(created.getStatus()).isEqualTo(201);
        assertThat(body(created).get("weightsOrigin").asString()).isEqualTo("operator");
        assertThat(call(agent, put("/v1/indices/unrest").contentType(MediaType.APPLICATION_JSON).content(INDEX))
                .getStatus()).isEqualTo(200);
        MockHttpServletResponse invalid = call(agent, put("/v1/indices/unrest")
                .contentType(MediaType.APPLICATION_JSON).content(INDEX.replace("0.7", "0.9")));
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(body(invalid).get("code").asString()).isEqualTo("INDEX_INVALID");
        MockHttpServletResponse unknown = call(read, get("/v1/indices/nothing"));
        assertThat(unknown.getStatus()).isEqualTo(404);
        assertThat(body(unknown).get("code").asString()).isEqualTo("UNKNOWN_INDEX");
        JsonNode detail = body(call(read, get("/v1/indices/unrest").param("region", "YE").param("from", "2026-10-01")
                .param("to", "2026-10-10")));
        assertThat(detail.get("index").get("id").asString()).isEqualTo("unrest");
        assertThat(detail.get("values")).isEmpty();
        assertThat(call(read, get("/v1/indices/unrest").param("from", "yesterday")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("fit is 409 while the component series do not exist; weights: operator applied, unknown version 400")
    void fitAndWeights() throws Exception {
        call(agent, put("/v1/indices/unrest").contentType(MediaType.APPLICATION_JSON).content(INDEX));
        assertThat(call(read, post("/v1/indices/unrest/fit")).getStatus()).isEqualTo(403);
        MockHttpServletResponse notReady = call(agent, post("/v1/indices/unrest/fit"));
        assertThat(notReady.getStatus()).isEqualTo(409);
        assertThat(body(notReady).get("code").asString()).isEqualTo("FORECAST_MODEL_NOT_READY");
        assertThat(call(agent, post("/v1/indices/nothing/fit")).getStatus()).isEqualTo(404);
        MockHttpServletResponse operator = call(agent, post("/v1/indices/unrest/weights")
                .contentType(MediaType.APPLICATION_JSON).content(
                        "{\"source\":\"operator\",\"weights\":{\"conflict\":0.5,\"outages\":0.5},\"rationale\":\"r\"}"));
        assertThat(operator.getStatus()).isEqualTo(200);
        assertThat(body(operator).get("weightsVersion").asInt()).isEqualTo(2);
        assertThat(body(operator).get("weights").get("outages").asDouble()).isEqualTo(0.5);
        assertThat(call(agent, post("/v1/indices/unrest/weights").contentType(MediaType.APPLICATION_JSON)
                .content("{\"source\":\"fitted\",\"version\":7}")).getStatus()).isEqualTo(400);
        assertThat(call(read, post("/v1/indices/unrest/weights").contentType(MediaType.APPLICATION_JSON)
                .content("{\"source\":\"default\"}")).getStatus()).isEqualTo(403);
    }

    RegionalOutlookEntity outlook() {
        Instant at = T0.plus(Duration.ofDays(3));
        RegionalOutlookEntity o = new RegionalOutlookEntity(Ulids.next(at), "YE", "country", "conflict:battles",
                "conflict", at, "regional-outlook 1.0.0", 9);
        List<Map<String, Object>> windows = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Map<String, Object> w = new LinkedHashMap<>();
            w.put("index", i);
            w.put("windowStart", "2026-03-" + String.format("%02d", 2 + i));
            w.put("windowEnd", "2026-03-" + String.format("%02d", 2 + i));
            w.put("quantiles", Map.of("p05", 1.0, "p10", 2.0, "p25", 4.0, "p50", 6.0, "p75", 9.0, "p90", 12.0, "p95",
                    15.0));
            w.put("questionId", null);
            w.put("status", "open");
            windows.add(w);
        }
        o.setWindows(windows);
        o.setLatest(true);
        return outlooks.save(o);
    }

    @Test
    @DisplayName("outlooks: list, detail with skill and history, 404 for an unknown region or family")
    void outlookReads() throws Exception {
        MockHttpServletResponse missing = call(read, get("/v1/outlooks/YE/conflict:battles"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_OUTLOOK");
        outlook();
        JsonNode list = body(call(read, get("/v1/outlooks").param("family", "conflict:battles")));
        assertThat(list.get("items")).hasSize(1);
        assertThat(list.get("items").get(0).get("windows")).hasSize(6);
        assertThat(body(call(read, get("/v1/outlooks").param("family", "outage"))).get("items")).isEmpty();
        assertThat(call(read, get("/v1/outlooks").param("region", "yemen")).getStatus()).isEqualTo(400);
        JsonNode detail = body(call(read, get("/v1/outlooks/YE/conflict:battles")));
        assertThat(detail.get("windows")).hasSize(6);
        assertThat(detail.get("skill").get("label").asString()).isEqualTo("insufficient");
        assertThat(detail.has("history")).isTrue();
        assertThat(detail.get("recordRef").asString()).startsWith("record:foresignal-outlook/");
    }

    McpCaller caller(Scope scope) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "outlook-mcp", scope,
                TokenRestrictions.NONE, null, null, false, false);
        return new McpCaller(token, new ReadCaller(scope, true, false, ReadRestriction.NONE, false), Set.of(), null);
    }

    @Test
    @DisplayName("MCP: three read tools and one agent tool with the API shapes")
    void mcpTools() throws Exception {
        assertThat(getSeriesForecast.requiredScope()).isEqualTo(Scope.READ);
        assertThat(getOutlook.requiredScope()).isEqualTo(Scope.READ);
        assertThat(getIndex.requiredScope()).isEqualTo(Scope.READ);
        assertThat(fitIndex.requiredScope()).isEqualTo(Scope.AGENT);
        ToolContext ctx = new ToolContext(caller(Scope.AGENT), null, "test");
        forecastRows(SERIES, false);
        JsonNode f = getSeriesForecast.call(ctx, json.readTree("{\"ref\":\"" + SERIES + "\",\"horizon\":7}"))
                .structured();
        assertThat(f.get("forecasts")).hasSize(1);
        assertThat(f.get("model").get("method").asString()).isEqualTo("ets-ann");
        outlook();
        JsonNode o = getOutlook.call(ctx, json.readTree("{\"region\":\"YE\",\"family\":\"conflict:battles\"}"))
                .structured();
        assertThat(o.get("windows")).hasSize(6);
        assertThat(getOutlook.call(ctx, json.readTree("{}")).structured().get("items")).hasSize(1);
        JsonNode list = getIndex.call(ctx, json.readTree("{}")).structured();
        assertThat(list.get("items").size()).isGreaterThanOrEqualTo(3);
        JsonNode one = getIndex.call(ctx, json.readTree("{\"indexId\":\"instability\"}")).structured();
        assertThat(one.get("index").get("weightsOrigin").asString()).isEqualTo("default");
        assertThat(one.has("values")).isTrue();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> fitIndex.call(ctx,
                json.readTree("{\"indexId\":\"instability\"}"))).isInstanceOf(
                        ai.foresignal.api.error.ConflictException.class);
    }
}
