package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.baserate.BaseRateDefinition;
import ai.foresignal.baserate.BaseRateEntity;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.baserate.reference.ReferenceImportEntity;
import ai.foresignal.baserate.reference.ReferenceImportRepository;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.FindAnalogsTool;
import ai.foresignal.mcp.tools.GetBaseRateTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
 * The prompt 121 endpoints over HTTP and the MCP tools: base rates (read; store, delete and refresh are agent),
 * ad-hoc queries cached for 24 hours, reference imports (admin; skipped without ACLED credentials, 409 without a
 * UCDP file, rows removed on delete), analogs (read), the Problem codes and the tool shapes, and ACLED counts shown
 * as LOCAL_ONLY derived statistics.
 */
@IntegrationTest
class BaseRateControllersIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;
    @Autowired BaseRateService baseRates;
    @Autowired ReferenceImportRepository imports;
    @Autowired GetBaseRateTool getBaseRate;
    @Autowired FindAnalogsTool findAnalogs;

    String read;
    String agent;
    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        baseRates.clearCache();
        read = TestTokens.bearer(TestTokens.create(tokens, "baserate-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "baserate-agent", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "baserate-admin", Scope.ADMIN));
        seedHistory();
    }

    /** Two years of UCDP coverage (2023, 2024) with battles in Yemen every other week, and ACLED for Yemen in 2024. */
    void seedHistory() {
        ReferenceImportEntity ucdp = new ReferenceImportEntity(Ulids.next(NOW), "ucdp-ged", "k-ucdp", "test", NOW);
        ucdp.setStatus("done");
        ucdp.setLicense("CC-BY-4.0");
        ucdp.setEgress("OPEN");
        ucdp.setProgress(Map.of("done", List.of("2023", "2024")));
        imports.save(ucdp);
        ReferenceImportEntity acled = new ReferenceImportEntity(Ulids.next(NOW.plusSeconds(1)), "acled", "k-acled",
                "test", NOW);
        acled.setStatus("done");
        acled.setLicense("ACLED-EULA");
        acled.setEgress("LOCAL_ONLY");
        acled.setProgress(Map.of("done", List.of("YE:2024")));
        imports.save(acled);
        LocalDate start = ReferenceFamilies.yearChunk(2023)[0];
        LocalDate acledStart = ReferenceFamilies.yearChunk(2024)[0];
        for (LocalDate w = start; w.isBefore(ReferenceFamilies.yearChunk(2024)[1]); w = w.plusWeeks(2)) {
            boolean acledWeek = !w.isBefore(acledStart);
            insert(ReferenceFamilies.BATTLES, "YE", w, acledWeek ? ReferenceFamilies.ACLED : ReferenceFamilies.UCDP,
                    acledWeek ? 3 : 2, acledWeek ? acled.getId() : ucdp.getId());
            if (acledWeek) {
                // UCDP also covers these weeks; ACLED wins, so these never count.
                insert(ReferenceFamilies.BATTLES, "YE", w, ReferenceFamilies.UCDP, 9, ucdp.getId());
            }
        }
    }

    void insert(String family, String region, LocalDate week, String source, long count, String importId) {
        jdbc.update("INSERT INTO reference_history (family, region_code, week_start, source, count, fatalities,"
                + " license, egress, import_id) VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?)", family, region,
                Date.valueOf(week), source, count, ReferenceFamilies.LICENSES.get(source),
                ReferenceFamilies.EGRESS.get(source), importId);
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

    static final String QUERY = "{\"family\":\"conflict:battles\",\"regionCodes\":[\"YE\"],\"window\":\"P7D\"}";

    @Test
    @DisplayName("ad-hoc query: reference class, n, Jeffreys interval, one source per week, cached on repeat")
    void query() throws Exception {
        MockHttpServletResponse first = postJson(read, "/v1/base-rates/query", QUERY);
        assertThat(first.getStatus()).as(first.getContentAsString()).isEqualTo(200);
        JsonNode rate = body(first);
        assertThat(rate.get("id").isNull()).isTrue();
        assertThat(rate.get("referenceClass").asString()).isEqualTo("conflict:battles events in YE, per 7-day window");
        int windows = rate.get("nWindows").asInt();
        assertThat(windows).isGreaterThanOrEqualTo(104);
        assertThat(rate.get("pAtLeastOne").asDouble()).isBetween(0.45, 0.55);
        assertThat(rate.get("interval").get("method").asString()).isEqualTo("jeffreys");
        assertThat(rate.get("sourceMix").get("acled").asLong()).isPositive();
        assertThat(rate.get("sourceMix").get("ucdp").asLong()).isEqualTo(2L * 27);
        assertThat(rate.get("caveats").toString()).contains("MIXED_SOURCES");
        assertThat(rate.get("licences").toString()).contains("LOCAL_ONLY").contains("ACLED-EULA");
        assertThat(rate.get("computer").get("name").asString()).isEqualTo("baserate");
        assertThat(rate.get("manifestId").isNull()).isFalse();
        assertThat(rate.get("cached").asBoolean()).isFalse();

        assertThat(body(postJson(read, "/v1/base-rates/query", QUERY)).get("cached").asBoolean()).isTrue();

        MockHttpServletResponse invalid = postJson(read, "/v1/base-rates/query", "{\"family\":\"conflict:battles\","
                + "\"window\":\"P30D\"}");
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(body(invalid).get("code").asString()).isEqualTo("BASE_RATE_INVALID");
    }

    @Test
    @DisplayName("custom rates: agent stores (origin agent), admin stores (origin operator), read is refused")
    void customRates() throws Exception {
        String input = "{\"name\":\"Yemen battles weekly\",\"definition\":" + QUERY + "}";
        assertThat(postJson(read, "/v1/base-rates", input).getStatus()).isEqualTo(403);
        MockHttpServletResponse created = postJson(agent, "/v1/base-rates", input);
        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(201);
        String id = body(created).get("id").asString();
        assertThat(body(created).get("origin").asString()).isEqualTo("agent");
        assertThat(body(postJson(admin, "/v1/base-rates", input)).get("origin").asString()).isEqualTo("operator");

        MockHttpServletResponse one = call(read, get("/v1/base-rates/" + id));
        assertThat(one.getStatus()).isEqualTo(200);
        assertThat(body(one).get("name").asString()).isEqualTo("Yemen battles weekly");
        assertThat(body(call(read, get("/v1/base-rates").param("family", "conflict:battles").param("region", "YE")
                .param("window", "P7D"))).get("items")).hasSize(2);
        assertThat(body(call(read, get("/v1/base-rates").param("window", "P28D"))).get("items")).isEmpty();
        // a stored rate answers the ad-hoc query
        assertThat(body(postJson(read, "/v1/base-rates/query", QUERY)).get("id").isNull()).isFalse();

        MockHttpServletResponse missing = call(read, get("/v1/base-rates/01JA8Q3V7S2K9W4T6Y1B5N0M3C"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_BASE_RATE");

        assertThat(call(read, delete("/v1/base-rates/" + id)).getStatus()).isEqualTo(403);
        assertThat(call(agent, delete("/v1/base-rates/" + id)).getStatus()).isEqualTo(204);

        BaseRateDefinition definition = BaseRateDefinition.parse(Map.of("family", "conflict:battles", "regionCodes",
                List.of("YE"), "window", "P28D"));
        BaseRateEntity auto = baseRates.upsertAuto(definition, baseRates.compute(definition));
        MockHttpServletResponse notCustom = call(agent, delete("/v1/base-rates/" + auto.getId()));
        assertThat(notCustom.getStatus()).isEqualTo(409);
        assertThat(body(notCustom).get("code").asString()).isEqualTo("BASE_RATE_NOT_CUSTOM");
    }

    @Test
    @DisplayName("refresh: agent starts a base-rate-refresh job; a second one while it runs is 409")
    void refresh() throws Exception {
        assertThat(call(read, post("/v1/base-rates/refresh")).getStatus()).isEqualTo(403);
        MockHttpServletResponse accepted = call(agent, post("/v1/base-rates/refresh"));
        assertThat(accepted.getStatus()).as(accepted.getContentAsString()).isEqualTo(202);
        assertThat(body(accepted).get("type").asString()).isEqualTo("base-rate-refresh");
        jdbc.update("UPDATE backtest_job SET status = 'running' WHERE id = ?", body(accepted).get("id").asString());
        MockHttpServletResponse busy = call(agent, post("/v1/base-rates/refresh"));
        assertThat(busy.getStatus()).isEqualTo(409);
        assertThat(body(busy).get("code").asString()).isEqualTo("BACKTEST_IN_PROGRESS");

        Map<String, Object> result = baseRates.refreshAll(ai.foresignal.backtest.JobMonitor.NONE);
        assertThat(((Number) result.get("rates")).intValue()).isEqualTo(2);
        JsonNode items = body(call(read, get("/v1/base-rates").param("region", "YE"))).get("items");
        assertThat(items).isNotEmpty();
        items.forEach(item -> assertThat(item.get("origin").asString()).isEqualTo("auto"));
    }

    @Test
    @DisplayName("reference imports are admin: ACLED without credentials is skipped, a missing UCDP file is 409")
    void referenceImports() throws Exception {
        String acled = "{\"dataset\":\"acled\",\"params\":{\"countries\":[\"YE\"],\"yearFrom\":2023,\"yearTo\":2024}}";
        assertThat(postJson(read, "/v1/reference-imports", acled).getStatus()).isEqualTo(403);
        assertThat(postJson(agent, "/v1/reference-imports", acled).getStatus()).isEqualTo(403);
        MockHttpServletResponse skipped = postJson(admin, "/v1/reference-imports", acled);
        assertThat(skipped.getStatus()).as(skipped.getContentAsString()).isEqualTo(202);
        assertThat(body(skipped).get("status").asString()).isEqualTo("skipped");
        assertThat(body(skipped).get("statusReason").asString()).isEqualTo("credentials absent");
        assertThat(body(skipped).get("egress").asString()).isEqualTo("LOCAL_ONLY");
        assertThat(body(skipped).get("licence").asString()).isEqualTo("ACLED-EULA");
        // the same request reuses the same import
        assertThat(body(postJson(admin, "/v1/reference-imports", acled)).get("id").asString())
                .isEqualTo(body(skipped).get("id").asString());

        MockHttpServletResponse unavailable = postJson(admin, "/v1/reference-imports",
                "{\"dataset\":\"ucdp-ged\",\"params\":{\"file\":\"no-such-file.csv\"}}");
        assertThat(unavailable.getStatus()).isEqualTo(409);
        assertThat(body(unavailable).get("code").asString()).isEqualTo("IMPORT_DATASET_UNAVAILABLE");
        assertThat(postJson(admin, "/v1/reference-imports", "{\"dataset\":\"nope\"}").getStatus()).isEqualTo(400);

        assertThat(body(call(admin, get("/v1/reference-imports").param("dataset", "acled"))).get("items"))
                .hasSize(2);
        assertThat(call(read, get("/v1/reference-imports")).getStatus()).isEqualTo(403);

        String ucdpImport = jdbc.queryForObject("SELECT id FROM reference_import WHERE dataset = 'ucdp-ged'",
                String.class);
        assertThat(call(admin, get("/v1/reference-imports/" + ucdpImport)).getStatus()).isEqualTo(200);
        assertThat(call(admin, delete("/v1/reference-imports/" + ucdpImport)).getStatus()).isEqualTo(204);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM reference_history WHERE source = 'ucdp'", Long.class))
                .isZero();
        MockHttpServletResponse gone = call(admin, get("/v1/reference-imports/" + ucdpImport));
        assertThat(gone.getStatus()).isEqualTo(404);
        assertThat(body(gone).get("code").asString()).isEqualTo("UNKNOWN_IMPORT");
    }

    @Test
    @DisplayName("analogs: an unknown situation is 404; a region-period query is a read with feature-only ranking")
    void analogs() throws Exception {
        MockHttpServletResponse unknown = call(read, get("/v1/situations/01JA8Q3V7S2K9W4T6Y1B5N0M3C/analogs"));
        assertThat(unknown.getStatus()).isEqualTo(404);
        assertThat(body(unknown).get("code").asString()).isEqualTo("UNKNOWN_SITUATION");
        assertThat(call(read, get("/v1/situations/01JA8Q3V7S2K9W4T6Y1B5N0M3C/analogs").param("outcome", "bogus"))
                .getStatus()).isEqualTo(400);
        MockHttpServletResponse region = postJson(read, "/v1/analogs/query", "{\"regionCode\":\"YE\",\"from\":"
                + "\"2024-12-02T00:00:00Z\",\"to\":\"2024-12-16T00:00:00Z\",\"k\":5,\"outcome\":"
                + "\"conflict:battles/P30D\"}");
        assertThat(region.getStatus()).as(region.getContentAsString()).isEqualTo(200);
        JsonNode result = body(region);
        assertThat(result.get("method").asString()).isEqualTo("feature-only");
        assertThat(result.get("target").get("regionCode").asString()).isEqualTo("YE");
        assertThat(result.get("countFound").asInt()).isGreaterThanOrEqualTo(result.get("analogs").size());
        assertThat(result.get("detector").get("name").asString()).isEqualTo("analog-search");
        assertThat(result.has("outcomeDistribution")).isTrue();
        assertThat(postJson(read, "/v1/analogs/query", "{\"regionCode\":\"yemen\"}").getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("MCP: get_base_rate and find_analogs are read tools with the API shapes")
    void mcpTools() {
        assertThat(getBaseRate.name()).isEqualTo("get_base_rate");
        assertThat(getBaseRate.requiredScope()).isEqualTo(Scope.READ);
        assertThat(findAnalogs.name()).isEqualTo("find_analogs");
        assertThat(findAnalogs.requiredScope()).isEqualTo(Scope.READ);
        ToolContext reader = new ToolContext(caller(), null, "test");
        String rate = text(getBaseRate.call(reader, json.readTree(QUERY)));
        assertThat(rate).contains("pAtLeastOne").contains("interval").contains("referenceClass");
        String analog = text(findAnalogs.call(reader, json.readTree("{\"regionCode\":\"YE\",\"to\":"
                + "\"2024-12-16T00:00:00Z\"}")));
        assertThat(analog).contains("countFound").contains("feature-only");
    }

    static String text(ToolResult result) {
        return String.valueOf(result.structured());
    }

    McpCaller caller() {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "baserate-mcp", Scope.READ,
                TokenRestrictions.NONE, null, "baserate-mcp", false, false);
        return new McpCaller(token, new ReadCaller(Scope.READ, true, false, ReadRestriction.NONE, false), Set.of(),
                null);
    }
}
