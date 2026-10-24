package ai.foresignal.impact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.impact.rate.ImpactRateService;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.GetImpactRateTool;
import ai.foresignal.mcp.tools.ListProjectedImpactsTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 127 endpoints over HTTP and the MCP tools: {@code listProjectedImpacts} (grouped by hazard, highest
 * probability first, filters, cursor, 400s), {@code getProjectedImpact} (inputs, paths, labels, question; 404
 * {@code UNKNOWN_IMPACT}, private projections hidden), {@code listImpactRates} (insufficient pairs without history,
 * paging, 400s), the {@code read} scope, and the tool shapes.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class ImpactControllersIT extends AnalyticsItSupport {

    static final Instant NOW = Instant.parse("2026-09-10T16:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired FindingService findings;
    @Autowired ImpactRateService rates;
    @Autowired ListProjectedImpactsTool listTool;
    @Autowired GetImpactRateTool rateTool;
    @Autowired AppSettingService settings;

    String read;

    @BeforeEach
    void setUp() {
        read = TestTokens.bearer(TestTokens.create(tokens, "impact-read", Scope.READ));
        rates.clearCache();
        clock.setInstant(NOW);
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

    /** Records a projection as the projector would. */
    String projection(String hazardKey, String hazardType, String family, String admin1, double pImpact,
            boolean privateFinding) {
        Map<String, Object> hazard = new LinkedHashMap<>();
        hazard.put("hazardType", hazardType);
        hazard.put("hazardKey", hazardKey);
        hazard.put("sourceRef", "record:nhc-wind-probs/34kt:40-50@12");
        hazard.put("issuedAt", NOW.toString());
        hazard.put("validFrom", NOW.toString());
        hazard.put("validTo", NOW.plusSeconds(120 * 3600).toString());
        hazard.put("geometry", Map.of("type", "Polygon", "coordinates", List.of(List.of(List.of(-96, 28),
                List.of(-94, 28), List.of(-94, 30), List.of(-96, 28)))));
        hazard.put("probabilityField", List.of(Map.of("band", "40-50%", "pMin", 0.4, "pMax", 0.5, "geometry",
                Map.of("type", "Polygon", "coordinates", List.of(List.of(List.of(-96, 28), List.of(-94, 28),
                        List.of(-94, 30), List.of(-96, 28)))))));
        hazard.put("probabilityModel", "published-polygons");
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("hazardKey", hazardKey);
        stats.put("hazardType", hazardType);
        stats.put("sourceRef", "record:nhc-wind-probs/34kt:40-50@12");
        stats.put("impactFamily", family);
        stats.put("admin1", admin1);
        stats.put("admin1Name", "Texas");
        stats.put("regionClass", "continent:NA");
        stats.put("pHazard", 0.4);
        stats.put("pConditional", pImpact / 0.4);
        stats.put("pImpact", pImpact);
        stats.put("interval", Map.of("lo", pImpact / 2, "hi", Math.min(1, pImpact * 1.5)));
        stats.put("n", 34);
        stats.put("lagHours", 48);
        stats.put("hazard", hazard);
        stats.put("exposedAssets", List.of(Map.of("ref", "record:wri-power-plants/pp1", "title", "Plant", "kind",
                "power-plant", "pHazard", 0.4)));
        stats.put("dependencyPaths", List.of(Map.of("from", "record:wri-power-plants/pp1", "to",
                "record:osm-infrastructure/n11", "hops", List.of("record:osm-infrastructure/n10"),
                "weakestEdgeLabel", "possible")));
        stats.put("inputs", List.of(Map.of("name", "hazardProbability", "value", 0.4)));
        stats.put("skipped", List.of(Map.of("impactFamily", "ground-stop", "regionClass", "continent:NA", "n", 3,
                "reason", "insufficient-history")));
        stats.put("labels", List.of(ImpactProjector.LABEL));
        return findings.record(new FindingDraft("projected-impact", "projection " + family + " " + admin1,
                ImpactProjectionService.severity(pImpact), null, List.of(admin1), NOW, NOW.plusSeconds(120 * 3600),
                NOW, List.of(), List.of(), 12, stats, null, List.of(FindingCaveats.of(FindingCaveats.MODELED,
                        ImpactProjector.LABEL)), ImpactProjector.NAME, ImpactProjector.VERSION, Map.of(), "x",
                privateFinding, null, List.of(), List.of(), hazardKey + "|" + family + "|" + admin1)).finding()
                .getId();
    }

    @Test
    @DisplayName("list: grouped by hazard, highest probability first; filters; cursor; 400s; read scope")
    void list() throws Exception {
        String a = projection("nhc-wind-probs:34", "tropical-wind-34", "outage", "US-TX", 0.22, false);
        String b = projection("nhc-wind-probs:34", "tropical-wind-34", "ground-stop", "US-TX", 0.31, false);
        String c = projection("spc-outlooks:day1:torn", "tornado", "outage", "US-OK", 0.08, false);
        projection("spc-outlooks:day1:torn", "tornado", "tornado-warning", "US-KS", 0.4, true);

        JsonNode all = body(call(read, get("/v1/analytics/impacts")));
        assertThat(all.get("items")).extracting(i -> i.get("findingId").asString()).containsExactly(b, a, c);
        JsonNode first = all.get("items").get(0);
        assertThat(first.get("labels").get(0).asString()).isEqualTo(ImpactProjector.LABEL);
        assertThat(first.get("status").asString()).isEqualTo("active");
        assertThat(first.get("hazard").get("probabilityField").get(0).has("geometry")).isFalse();

        assertThat(body(call(read, get("/v1/analytics/impacts").param("hazardType", "tornado"))).get("items"))
                .hasSize(1);
        assertThat(body(call(read, get("/v1/analytics/impacts").param("region", "US"))).get("items")).hasSize(3);
        assertThat(body(call(read, get("/v1/analytics/impacts").param("region", "US-OK"))).get("items")).hasSize(1);
        assertThat(body(call(read, get("/v1/analytics/impacts").param("minProbability", "0.2"))).get("items"))
                .hasSize(2);
        assertThat(body(call(read, get("/v1/analytics/impacts").param("since", NOW.plusSeconds(60).toString())))
                .get("items")).isEmpty();

        JsonNode page1 = body(call(read, get("/v1/analytics/impacts").param("limit", "2")));
        assertThat(page1.get("items")).hasSize(2);
        String cursor = page1.get("nextCursor").asString();
        JsonNode page2 = body(call(read, get("/v1/analytics/impacts").param("cursor", cursor)));
        assertThat(page2.get("items")).extracting(i -> i.get("findingId").asString()).containsExactly(c);
        assertThat(page2.get("nextCursor").isNull()).isTrue();

        assertThat(call(read, get("/v1/analytics/impacts").param("hazardType", "volcano")).getStatus())
                .isEqualTo(400);
        assertThat(call(read, get("/v1/analytics/impacts").param("region", "texas")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/analytics/impacts").param("minProbability", "2")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/analytics/impacts").param("cursor", "%%%")).getStatus()).isEqualTo(400);
        // listProjectedImpacts is a read ([{}, bearerToken]): open while openReads is on (the default), a token
        // once it is off.
        assertThat(call(null, get("/v1/analytics/impacts")).getStatus()).isEqualTo(200);
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        assertThat(call(null, get("/v1/analytics/impacts")).getStatus()).isEqualTo(401);
        assertThat(call(read, get("/v1/analytics/impacts")).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("detail: hazard with geometries, inputs, paths, labels; 404 UNKNOWN_IMPACT; private hidden")
    void detail() throws Exception {
        String a = projection("nhc-wind-probs:34", "tropical-wind-34", "outage", "US-TX", 0.22, false);
        String hidden = projection("spc-outlooks:day1:torn", "tornado", "outage", "US-OK", 0.3, true);
        JsonNode d = body(call(read, get("/v1/analytics/impacts/" + a)));
        assertThat(d.get("hazard").get("probabilityField").get(0).has("geometry")).isTrue();
        assertThat(d.get("dependencyPaths").get(0).get("weakestEdgeLabel").asString()).isEqualTo("possible");
        assertThat(d.get("inputs")).isNotEmpty();
        assertThat(d.get("skipped").get(0).get("reason").asString()).isEqualTo("insufficient-history");
        assertThat(d.get("pImpact").asDouble()).isEqualTo(0.22);
        assertThat(d.get("version").asString()).isEqualTo("impact-projection 1.0.0");
        assertThat(d.get("manifestId").isNull()).isFalse();
        MockHttpServletResponse missing = call(read, get("/v1/analytics/impacts/01JZZZZZZZZZZZZZZZZZZZZZZZ"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_IMPACT");
        assertThat(call(read, get("/v1/analytics/impacts/" + hidden)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("rates: every built-in pair is insufficient without history; paging and 400s")
    void rates() throws Exception {
        JsonNode one = body(call(read, get("/v1/analytics/impact-rates").param("hazardClass", "tropical-wind-34")
                .param("impactFamily", "outage").param("regionClass", "continent:NA")));
        assertThat(one.get("items")).singleElement().satisfies(r -> {
            assertThat(r.get("status").asString()).isEqualTo("insufficient");
            assertThat(r.get("rate").isNull()).isTrue();
            assertThat(r.get("n").asLong()).isZero();
            assertThat(r.get("lagHours").asInt()).isEqualTo(48);
        });
        JsonNode page = body(call(read, get("/v1/analytics/impact-rates").param("hazardClass", "tornado")
                .param("limit", "5")));
        assertThat(page.get("items")).hasSize(5);
        JsonNode rest = body(call(read, get("/v1/analytics/impact-rates").param("hazardClass", "tornado")
                .param("cursor", page.get("nextCursor").asString())));
        assertThat(rest.get("items")).hasSize(2 * 7 - 5);
        assertThat(call(read, get("/v1/analytics/impact-rates").param("hazardClass", "volcano")).getStatus())
                .isEqualTo(400);
        assertThat(call(read, get("/v1/analytics/impact-rates").param("regionClass", "admin1:coastal")).getStatus())
                .isEqualTo(400);
    }

    McpCaller caller(Scope scope, boolean privateAccess) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "impact-mcp", scope,
                TokenRestrictions.NONE, null, null, false, false);
        return new McpCaller(token, new ReadCaller(scope, privateAccess, false, ReadRestriction.NONE, false), Set.of(),
                null);
    }

    @Test
    @DisplayName("MCP: list_projected_impacts and get_impact_rate (read) with the API shapes")
    void mcpTools() {
        assertThat(listTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(rateTool.requiredScope()).isEqualTo(Scope.READ);
        String a = projection("nhc-wind-probs:34", "tropical-wind-34", "outage", "US-TX", 0.22, false);
        ToolContext ctx = new ToolContext(caller(Scope.READ, false), null, "test");
        JsonNode list = listTool.call(ctx, json.readTree("{\"region\":\"US-TX\"}")).structured();
        assertThat(list.get("items")).singleElement().satisfies(i -> assertThat(i.get("findingId").asString())
                .isEqualTo(a));
        JsonNode one = listTool.call(ctx, json.readTree("{\"findingId\":\"" + a + "\"}")).structured();
        assertThat(one.get("labels").get(0).asString()).isEqualTo(ImpactProjector.LABEL);
        assertThatThrownBy(() -> listTool.call(ctx, json.readTree("{\"findingId\":\"01JZZZZZZZZZZZZZZZZZZZZZZZ\"}")))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> listTool.call(ctx, json.readTree("{\"hazardType\":\"volcano\"}")))
                .isInstanceOf(ValidationException.class);
        JsonNode rate = rateTool.call(ctx, json.readTree("{\"hazardClass\":\"tornado\",\"impactFamily\":\"outage\","
                + "\"regionClass\":\"continent:NA\"}")).structured();
        assertThat(rate.get("items")).singleElement().satisfies(r -> assertThat(r.get("status").asString())
                .isEqualTo("insufficient"));
        assertThatThrownBy(() -> rateTool.call(ctx, json.readTree("{\"hazardClass\":\"tornado\","
                + "\"impactFamily\":\"famine\"}"))).isInstanceOf(ValidationException.class);
    }
}
