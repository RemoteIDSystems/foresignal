package ai.foresignal.security.restrict;

import static ai.foresignal.testsupport.records.RecordsTestSources.OTHER_KIND;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE_KIND;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_A;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_B;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpToolRegistry;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.push.PushRestrictionGate;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenFilter;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.sse.AlertsTopic;
import ai.foresignal.stream.sse.ChangeFilter;
import ai.foresignal.stream.sse.ChangesTopic;
import ai.foresignal.stream.sse.FifoEvent;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.TopicParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import io.modelcontextprotocol.spec.McpSchema;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

/**
 * The leak matrix of prompt 035: {anonymous with a profile, restricted read token, feed key} × every
 * {@code @RestrictedRead} path. The fixture has two sources ({@code test-schema-a},
 * {@code test-schema-b}), two kinds ({@code test-quake}, {@code test-other}), records inside and
 * outside the area, and one private source. Every principal is restricted to
 * {@code sources=[test-schema-a], kinds=[test-quake], bbox=[0,0,10,10]}, so exactly one record
 * ({@code in-a1}) is visible; every other record's external id starts with {@code out-}, and the
 * test asserts no such id is ever returned or streamed.
 *
 * <p>HTTP paths run through the real filter chain ({@code PathKeyFilter}, {@code TokenFilter}) and
 * controllers. The SSE lanes are checked at the topics (replay through the real SQL, live matching
 * on change summaries, alert and notification filters), MCP through the tool registry with each
 * principal's caller, and push fan-out through {@link PushRestrictionGate}. Ask's tool loop is
 * covered by {@code AskRestrictionIT}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TriggerTestSources.class)
class RestrictionLeakMatrixIT {

    static final Instant T = Instant.parse("2026-06-01T12:00:00Z");
    static final List<Double> BOX = List.of(0.0, 0.0, 10.0, 10.0);
    static final Pattern OUT = Pattern.compile("out-[a-z0-9]+");
    static final JsonMapper JSON = JsonMapper.builder().build();

    /** The three restricted principals. */
    enum Who { ANONYMOUS, TOKEN, FEED_KEY }

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired FeedKeyService feedKeys;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired ChangeBus changeBus;
    @Autowired AlertStore alertStore;
    @Autowired AlertBus alertBus;
    @Autowired ChangesTopic changesTopic;
    @Autowired AlertsTopic alertsTopic;
    @Autowired McpToolRegistry mcp;
    @Autowired PushRestrictionGate push;

    TriggerFixture fixture;
    String admin;
    TokenService.CreatedToken kiosk;
    String feedKey;
    UUID rule;

    static TokenRestrictions restriction() {
        return new TokenRestrictions(List.of(SCHEMA_A), List.of(SCHEMA_KIND), BOX, null);
    }

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        admin = "Bearer " + adminTokens.rotate();
        settings.put(SettingKeys.SECURITY, new SecuritySettings(true, false, restriction(), true));
        kiosk = TestTokens.create(tokens, "kiosk", Scope.READ, restriction(), null);
        feedKey = feedKeys.create(JSON.readTree("{\"name\":\"wall map\",\"prefixes\":[\"/v1/records\",\"/v1/history\","
                + "\"/v1/changes\",\"/v1/kinds\",\"/v1/tiles\",\"/v1/alerts\",\"/v1/digest\"],\"restrictions\":"
                + "{\"sources\":[\"" + SCHEMA_A + "\"],\"kinds\":[\"" + SCHEMA_KIND + "\"],\"bbox\":[0,0,10,10]},"
                + "\"expiresAt\":\"" + T.plusSeconds(86_400) + "\"}")).key();
        fixture = new TriggerFixture(rules, engine, changeBus, alertStore, alertBus);
        rule = fixture.rule("{'name':'Everything','sources':['" + SCHEMA_A + "','" + SCHEMA_B + "'],"
                + "'conditions':[{'path':'mag','op':'>=','value':0}]}");
        seed();
        fixture.settle();
    }

    @AfterEach
    void tearDown() {
        fixture.close();
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private void seed() {
        records.ingestAt(SCHEMA_A, T, rec("in-a1", SCHEMA_KIND, 5, 5), rec("out-a2", SCHEMA_KIND, 50, 50));
        records.ingestAt(SCHEMA_B, T, rec("out-b1", SCHEMA_KIND, 5, 5), rec("out-b2", OTHER_KIND, 6, 6));
        records.ingestAt(PRIVATE, T, rec("out-p1", PRIVATE_KIND, 5, 5));
    }

    private static NormalizedRecord rec(String id, String kind, double lon, double lat) {
        return NormalizedRecord.builder(id, kind).observedAt(T).point(lon, lat).put("mag", 5.0)
                .put("place", "place " + id).build();
    }

    // ------------------------------------------------------------------------- helpers

    private MockHttpServletResponse read(Who who, String path) throws Exception {
        MockHttpServletRequestBuilder request = switch (who) {
            case ANONYMOUS -> get(path);
            case TOKEN -> get(path).header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(kiosk));
            case FEED_KEY -> get("/v1/k/" + feedKey + path.substring("/v1".length()));
        };
        return mvc.perform(request).andReturn().getResponse();
    }

    private static List<String> leaks(String body) {
        List<String> out = new ArrayList<>();
        Matcher m = OUT.matcher(body);
        while (m.find()) {
            out.add(m.group());
        }
        return out;
    }

    private static void assertNoLeak(String what, String body) {
        assertThat(leaks(body)).as(what + " leaked records outside the restriction").isEmpty();
    }

    private String body(Who who, String path) throws Exception {
        MockHttpServletResponse response = read(who, path);
        String body = response.getContentAsString(StandardCharsets.UTF_8);
        assertThat(response.getStatus()).as(who + " " + path + ": " + body).isEqualTo(200);
        return body;
    }

    /** The effective restriction each principal ends up with; all three must be the same subset. */
    private ReadRestriction restrictionOf(Who who) {
        return switch (who) {
            case ANONYMOUS -> ReadRestriction.of(
                    settings.getOrDefault(SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults)
                            .effectiveAnonymous(), true);
            case TOKEN -> TokenFilter.restrictionOf(AuthenticatedToken.of(kiosk.entity()));
            case FEED_KEY -> ((PathKeyResolver.Valid) feedKeys.resolve(feedKey)).principal().restriction();
        };
    }

    // ------------------------------------------------------------------------- HTTP read paths

    @ParameterizedTest(name = "{0}")
    @EnumSource(Who.class)
    @DisplayName("records in every format, history, changes and kinds show only the inside record")
    void listings(Who who) throws Exception {
        String json = body(who, "/v1/records?order=id");
        assertNoLeak("records", json);
        assertThat(json).contains("in-a1");
        for (String format : List.of("ndjson", "csv", "geojsonseq", "geojson")) {
            String text = body(who, "/v1/records?format=" + format);
            assertNoLeak("records " + format, text);
            assertThat(text).as(format).contains("in-a1");
        }
        String history = body(who, "/v1/history?since=" + T.minusSeconds(3600));
        assertNoLeak("history", history);
        assertThat(history).contains("in-a1");
        String changes = body(who, "/v1/changes?after=0");
        assertNoLeak("changes", changes);
        assertThat(changes).contains("in-a1");
        String kinds = body(who, "/v1/kinds");
        assertThat(kinds).contains(SCHEMA_KIND).doesNotContain(OTHER_KIND).doesNotContain(PRIVATE_KIND)
                .doesNotContain(SCHEMA_B);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Who.class)
    @DisplayName("single records and versions outside the restriction are 404, never 403")
    void singleReads(Who who) throws Exception {
        assertThat(read(who, "/v1/records/" + SCHEMA_A + "/in-a1").getStatus()).isEqualTo(200);
        assertThat(read(who, "/v1/records/" + SCHEMA_A + "/in-a1/versions").getStatus()).isEqualTo(200);
        for (String path : List.of("/v1/records/" + SCHEMA_A + "/out-a2", "/v1/records/" + SCHEMA_B + "/out-b1",
                "/v1/records/" + SCHEMA_A + "/out-a2/versions", "/v1/records/" + SCHEMA_B + "/out-b2/versions")) {
            assertThat(read(who, path).getStatus()).as(who + " " + path).isEqualTo(404);
        }
        assertThat(read(who, "/v1/kinds/" + OTHER_KIND + "/schema").getStatus()).isEqualTo(404);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Who.class)
    @DisplayName("a disjoint explicit filter yields an empty list")
    void disjointFilter(Who who) throws Exception {
        String json = body(who, "/v1/records?source=" + SCHEMA_B);
        assertThat(JSON.readTree(json).get("items")).isEmpty();
        assertThat(JSON.readTree(body(who, "/v1/records?kind=" + OTHER_KIND)).get("items")).isEmpty();
        assertThat(JSON.readTree(body(who, "/v1/records?bbox=40,40,60,60")).get("items")).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Who.class)
    @DisplayName("tiles filter inside the MVT SQL and are Cache-Control private; TileJSON lists allowed layers only")
    void tiles(Who who) throws Exception {
        MockHttpServletResponse tile = read(who, "/v1/tiles/" + SCHEMA_KIND + "/0/0/0.mvt?agg=none");
        assertThat(tile.getStatus()).isEqualTo(200);
        String bytes = new String(tile.getContentAsByteArray(), StandardCharsets.ISO_8859_1);
        assertNoLeak("tile", bytes);
        assertThat(bytes).contains("in-a1");
        assertThat(tile.getHeader(HttpHeaders.CACHE_CONTROL)).startsWith("private");
        assertThat(read(who, "/v1/tiles/" + SCHEMA_B + "/0/0/0.mvt").getStatus()).isEqualTo(404);
        assertThat(read(who, "/v1/tiles/" + OTHER_KIND + ".json").getStatus()).isEqualTo(404);
        String index = body(who, "/v1/tiles.json");
        assertThat(index).contains(SCHEMA_A).doesNotContain(SCHEMA_B).doesNotContain(OTHER_KIND)
                .doesNotContain(PRIVATE);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Who.class)
    @DisplayName("alerts and alert detail, and the digest, stay inside the restriction")
    void alertsAndDigest(Who who) throws Exception {
        List<AlertView> fired = fixture.alerts(rule);
        assertThat(fired).extracting(AlertView::externalId).contains("in-a1", "out-a2", "out-b1");
        String list = body(who, "/v1/alerts");
        assertNoLeak("alerts", list);
        assertThat(list).contains("in-a1");
        for (AlertView alert : fired) {
            int expected = alert.externalId().startsWith("in-") ? 200 : 404;
            assertThat(read(who, "/v1/alerts/" + alert.id()).getStatus()).as(alert.externalId()).isEqualTo(expected);
        }
        assertNoLeak("digest", body(who, "/v1/digest"));
    }

    @Test
    @DisplayName("search and the inbox (always authenticated) stay inside the token's restriction")
    void searchAndInbox() throws Exception {
        String search = mvc.perform(get("/v1/search").param("q", "place").param("mode", "text")
                        .header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(kiosk)))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertNoLeak("search", search);
        String inbox = mvc.perform(get("/v1/notifications").header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(kiosk))
                        .header("Foresignal-Device-Id", UUID.randomUUID().toString()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertNoLeak("inbox", inbox);
        String badge = mvc.perform(get("/v1/notifications/badge").header(HttpHeaders.AUTHORIZATION,
                        TestTokens.bearer(kiosk)).header("Foresignal-Device-Id", UUID.randomUUID().toString()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(JSON.readTree(badge).path("unread").asInt()).isLessThanOrEqualTo(1);
    }

    // ------------------------------------------------------------------------- the same subset

    @Test
    @DisplayName("the token, the feed key and the anonymous profile have the same effective data restriction")
    void sameSubset() {
        ReadRestriction token = restrictionOf(Who.TOKEN);
        for (Who who : Who.values()) {
            ReadRestriction r = restrictionOf(who);
            assertThat(r.sources()).as(who.name()).isEqualTo(token.sources());
            assertThat(r.kinds()).as(who.name()).isEqualTo(token.kinds());
            assertThat(r.region().equalsTopo(token.region())).as(who.name()).isTrue();
        }
    }

    // ------------------------------------------------------------------------- streams

    @ParameterizedTest(name = "{0}")
    @EnumSource(Who.class)
    @DisplayName("SSE change lane: replay and live matching never carry an outside record; PATCHed filters keep it")
    void changeStream(Who who) {
        StreamPrincipal principal = new StreamPrincipal("p:" + who, Scope.READ, "127.0.0.1", restrictionOf(who));
        ChangeFilter filter = changesTopic.parseQuery(new TopicParams("", Map.of()), principal);
        List<FifoEvent> replayed = new ArrayList<>();
        changesTopic.replay(new ReplayRequest(0, false), filter, event -> replayed.add(event));
        String text = JSON.writeValueAsString(replayed.stream().map(FifoEvent::data).toList());
        assertNoLeak("change replay", text);
        assertThat(text).contains("in-a1");

        ChangeFilter patched = changesTopic.parseJson(JSON.readTree("{\"source\":[\"" + SCHEMA_B + "\"]}"), principal);
        assertThat(patched.restriction()).isEqualTo(principal.restriction());

        assertThat(filter.matchChange(summary(SCHEMA_A, SCHEMA_KIND, 5, 5)))
                .isEqualTo(ai.foresignal.stream.sse.FilterMatch.MATCH);
        assertThat(filter.matchChange(summary(SCHEMA_A, SCHEMA_KIND, 50, 50)))
                .isEqualTo(ai.foresignal.stream.sse.FilterMatch.NONE);
        assertThat(filter.matchChange(summary(SCHEMA_B, SCHEMA_KIND, 5, 5)))
                .isEqualTo(ai.foresignal.stream.sse.FilterMatch.NONE);
        assertThat(filter.matchChange(summary(SCHEMA_A, OTHER_KIND, 5, 5)))
                .isEqualTo(ai.foresignal.stream.sse.FilterMatch.NONE);
        assertThat(patched.matchChange(summary(SCHEMA_B, SCHEMA_KIND, 5, 5)))
                .isEqualTo(ai.foresignal.stream.sse.FilterMatch.NONE);
    }

    private static ChangeSummary summary(String source, String kind, double lon, double lat) {
        ai.foresignal.stream.Bbox box = new ai.foresignal.stream.Bbox(lon, lat, lon, lat);
        return new ChangeSummary(1L, null, ai.foresignal.source.spi.HistoryTier.STANDARD, source, "x", kind, 1,
                ai.foresignal.ingest.PersistReason.FIRST, T, null, null, false, 0, box, null, null);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Who.class)
    @DisplayName("SSE alert and notification lanes: only alerts inside the restriction pass")
    void alertStream(Who who) {
        StreamPrincipal principal = new StreamPrincipal("p:" + who, Scope.READ, "127.0.0.1", restrictionOf(who));
        AlertQuery filter = alertsTopic.parseQuery(new TopicParams("", Map.of()), principal);
        for (AlertView alert : fixture.alerts(rule)) {
            assertThat(filter.matches(alert)).as(alert.externalId()).isEqualTo(alert.externalId().startsWith("in-"));
        }
        List<AlertView> replay = alertStore.afterRev(filter, 0, 100);
        assertThat(replay).extracting(AlertView::externalId).containsExactly("in-a1");
    }

    // ------------------------------------------------------------------------- MCP and push

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Who.class, names = {"TOKEN", "FEED_KEY", "ANONYMOUS"})
    @DisplayName("MCP query_records, search and list_alerts run under the caller's restriction")
    void mcpTools(Who who) {
        ReadRestriction r = restrictionOf(who);
        AuthenticatedToken token = AuthenticatedToken.of(kiosk.entity());
        McpCaller caller = new McpCaller(token, new ReadCaller(Scope.READ, false, true, r), java.util.Set.of(PRIVATE),
                "127.0.0.1");
        ToolContext context = new ToolContext(caller, "s-" + who, "streamable");
        for (String tool : List.of("query_records", "list_alerts", "search")) {
            Map<String, Object> args = "search".equals(tool) ? Map.of("q", "place", "mode", "text") : Map.of();
            McpSchema.CallToolResult result = mcp.call(tool, args, context);
            String text = ((McpSchema.TextContent) result.content().getFirst()).text();
            assertNoLeak("MCP " + tool, text);
        }
        McpSchema.CallToolResult outside = mcp.call("get_record",
                Map.of("source", SCHEMA_A, "externalId", "out-a2"), context);
        assertThat(outside.isError()).isTrue();
    }

    @Test
    @DisplayName("push fan-out: a restricted token's targets only receive alerts inside its restriction")
    void pushFanOut() {
        for (AlertView alert : fixture.alerts(rule)) {
            NotificationMessage message = NotificationMessage.builder(NotificationMessage.ALERT, "alert.fired",
                    "Alert", "warning").data(Map.of("alertId", alert.id(), "sourceId", alert.sourceId(),
                    "kind", alert.kind())).build();
            assertThat(push.allows(kiosk.entity().getId(), message)).as(alert.externalId())
                    .isEqualTo(alert.externalId().startsWith("in-"));
            assertThat(push.allows(null, message)).as("anonymous profile " + alert.externalId())
                    .isEqualTo(alert.externalId().startsWith("in-"));
        }
        NotificationMessage system = NotificationMessage.builder(NotificationMessage.SYSTEM, "system.test", "System",
                "info").data(Map.of()).build();
        assertThat(push.allows(kiosk.entity().getId(), system)).isFalse();
    }

    @Test
    @DisplayName("a feed key reaches no write, admin or always-authenticated operation")
    void feedKeyIsReadOnly() throws Exception {
        assertThat(mvc.perform(post("/v1/k/" + feedKey + "/records").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andReturn().getResponse().getStatus()).isEqualTo(405);
        assertThat(read(Who.FEED_KEY, "/v1/notifications").getStatus()).isEqualTo(403);
        assertThat(read(Who.FEED_KEY, "/v1/sources").getStatus()).as("outside the prefixes").isEqualTo(403);
    }
}
