package ai.foresignal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.analytics.AnalyticsAccess;
import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticsTopic;
import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingViews;
import ai.foresignal.analytics.finding.ForesignalAnalyticsGenerator;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.derive.DerivedContext;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastsTopic;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.intel.ForesignalIntelGenerator;
import ai.foresignal.intel.IntelTopic;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.McpToolRegistry;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.situation.SituationsTopic;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.adapters.fred.FredSeriesAdapter;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.sse.EventTopic;
import ai.foresignal.stream.sse.FifoEvent;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.TopicParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stats.StatsTestSources;
import io.modelcontextprotocol.spec.McpSchema;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
 * Intelligence egress, first version (checkpoint 124, focus area 7; invariants EGR-1 and EGR-2) across the surfaces
 * of prompts 098–123, with one fixture: an intelligence event and a situation resting on the private
 * {@value StatsTestSources#SECRET}, a private finding, a private forecast question, and a finding over the
 * {@code LOCAL_ONLY} {@value AnalyticsTestSources#LOCAL} series with raw values.
 *
 * <ul>
 *   <li><b>openReads and tokens without the private grant</b> (anonymous, read, agent): no private item in any list
 *       and 404 on every direct read of findings, intelligence events, situations, analogs and forecast questions;
 *       admin sees them (the control).
 *   <li><b>MCP tool output</b> for an anonymous session: the same.
 *   <li><b>SSE payloads</b>: the analytics, intel, situations and forecasts topics replay nothing private to an
 *       anonymous principal.
 *   <li><b>Tiles, feeds and federation</b> read the derived {@code foresignal-analytics} and {@code foresignal-intel}
 *       records: neither a private item nor a raw {@code LOCAL_ONLY} value is ever emitted as one.
 *   <li><b>{@code LOCAL_ONLY} inputs</b> ({@code fred-series}, ACLED reference history, the test series): refused on
 *       every path that re-serves data to another party (share links, feeds, federation, sinks); a finding's raw
 *       values are withheld there, and the ACLED per-source counts of a base rate are not mixed into them.
 *   <li><b>LLM requests</b>: a private source never reaches a remote provider; the analyst and brief request
 *       fixtures are covered by {@code AnalystRunnerIT} and {@code LlmBriefingIT}, whose scripted LLM contexts
 *       cannot share this one.
 * </ul>
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class IntelligenceEgressTest extends AnalyticsItSupport {

    static final Instant NOW = Instant.parse("2026-03-10T12:00:00Z");
    static final String SECRET = StatsTestSources.SECRET;
    static final String RAW = "98765.4321";

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired TokenService tokens;
    @Autowired FindingService findings;
    @Autowired AnalyticsAccess access;
    @Autowired ForecastService ledger;
    @Autowired ForecasterRegistry forecasters;
    @Autowired McpToolRegistry mcp;
    @Autowired AnalyticsTopic analyticsTopic;
    @Autowired IntelTopic intelTopic;
    @Autowired SituationsTopic situationsTopic;
    @Autowired ForecastsTopic forecastsTopic;
    @Autowired ForesignalAnalyticsGenerator analyticsGenerator;
    @Autowired ForesignalIntelGenerator intelGenerator;
    @Autowired EgressPolicy egress;
    @Autowired SourceRegistry registry;
    @Autowired BaseRateService baseRates;

    String read;
    String agent;
    String admin;

    /** The private items: what must never leave to a caller without the grant. */
    final Map<String, String> secrets = new LinkedHashMap<>();
    String localFinding;
    String publicSituation;

    @BeforeEach
    void fixture() throws Exception {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        clock.setInstant(NOW);
        forecasters.registerBuiltIns();
        read = TestTokens.bearer(TestTokens.create(tokens, "egress-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "egress-agent", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "egress-admin", Scope.ADMIN));

        seed(SECRET, NOW, point("sec1", StatsTestSources.SECRET_KIND, 44.2, 15.3, NOW, 7));
        seed(StatsTestSources.POINTS, NOW, point("pub1", StatsTestSources.POINT_KIND, 44.3, 15.4, NOW, 1));
        long secretSeq = seqOf(SECRET, "sec1");
        long publicSeq = seqOf(StatsTestSources.POINTS, "pub1");
        String secretMember = member(SECRET, "sec1", secretSeq);
        String secretSituationMember = situationMember(SECRET, "sec1", secretSeq);
        String publicSituationMember = situationMember(StatsTestSources.POINTS, "pub1", publicSeq);

        secrets.put("intel event", created("/v1/intel/events", "{\"type\":\"custom\",\"title\":\"Egress secret event\","
                + "\"members\":[" + secretMember + "]}"));
        secrets.put("situation", created("/v1/situations", "{\"title\":\"Egress secret situation\",\"theme\":"
                + "\"mixed\",\"members\":[" + secretSituationMember + "]}"));
        publicSituation = created("/v1/situations", "{\"title\":\"Egress public situation\",\"theme\":\"mixed\","
                + "\"members\":[" + publicSituationMember + "]}");
        secrets.put("finding", findings.record(finding("Egress secret finding", true, List.of(), "egress-secret"))
                .finding().getId());
        localFinding = findings.record(finding("Egress local finding", false, List.of(AnalyticsTestSources.LOCAL),
                "egress-local")).finding().getId();
        ForecastInputs.QuestionDraft draft = new ForecastInputs.QuestionDraft("Egress secret question?",
                Ledger.BINARY, Map.of(), "Yes if it happens.", ResolverSpec.validate(Map.of("type", "manual"),
                        Ledger.BINARY), NOW, NOW.plus(Duration.ofDays(3)), NOW.plus(Duration.ofDays(4)), "conflict",
                List.of(), Map.of(), null, true);
        ForecastQuestionEntity question = ledger.openQuestion(draft, "agent", "test").question();
        secrets.put("question", question.getId());
    }

    @AfterEach
    void restore() {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    long seqOf(String source, String externalId) {
        return jdbc.queryForObject("SELECT max(seq) FROM record_version WHERE source_id = ? AND external_id = ?",
                Long.class, source, externalId);
    }

    static String member(String source, String externalId, long seq) {
        return "{\"memberType\":\"record\",\"sourceId\":\"" + source + "\",\"externalId\":\"" + externalId
                + "\",\"seq\":" + seq + "}";
    }

    static String situationMember(String source, String externalId, long seq) {
        return "{\"memberType\":\"record\",\"ref\":\"" + source + "/" + externalId + "\",\"seq\":" + seq + "}";
    }

    static FindingDraft finding(String title, boolean privateFinding, List<String> localOnly, String scope) {
        return new FindingDraft("change-point", title, "significant", null, List.of("YE"),
                NOW.minus(Duration.ofDays(2)), NOW, NOW, localOnly.isEmpty() ? List.of() : List.of("series:"
                        + AnalyticsTestSources.LOCAL + "/oil"), List.of(), 42, Map.of("effect", 0.6, "levelBefore",
                        Double.parseDouble(RAW), "levelAfter", 90.0), Map.of("value", 1.0, "unit", "alarms-per-year",
                        "method", "detector-false-alarm-rate", "text", "flagged once a year"),
                List.of(FindingCaveats.notGroundTruth()), "changepoint-cusum", "1.0.0", Map.of(), "explained",
                privateFinding, "markets", localOnly, localOnly.isEmpty() ? List.of() : List.of("levelBefore",
                        "levelAfter"), scope);
    }

    String created(String path, String body) throws Exception {
        MockHttpServletResponse r = call(admin, post(path).contentType(MediaType.APPLICATION_JSON).content(body));
        assertThat(r.getStatus()).as(path + " " + r.getContentAsString()).isEqualTo(201);
        return json.readTree(r.getContentAsString(StandardCharsets.UTF_8)).get("id").asString();
    }

    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    String text(String bearer, String path) throws Exception {
        MockHttpServletResponse r = call(bearer, get(path));
        assertThat(r.getStatus()).as(path + " " + r.getContentAsString()).isEqualTo(200);
        return r.getContentAsString(StandardCharsets.UTF_8);
    }

    /** The private ids and titles. */
    List<String> markers() {
        List<String> out = new ArrayList<>(secrets.values());
        out.addAll(List.of("Egress secret event", "Egress secret situation", "Egress secret finding",
                "Egress secret question", "sec1"));
        return out;
    }

    static final List<String> LISTS = List.of("/v1/analytics/findings", "/v1/intel/events", "/v1/situations",
            "/v1/forecasts/questions", "/v1/situations/{public}/analogs");

    Map<String, String> directs() {
        Map<String, String> out = new LinkedHashMap<>();
        out.put("finding", "/v1/analytics/findings/" + secrets.get("finding"));
        out.put("intel event", "/v1/intel/events/" + secrets.get("intel event"));
        out.put("situation", "/v1/situations/" + secrets.get("situation"));
        out.put("situation members", "/v1/situations/" + secrets.get("situation") + "/members");
        out.put("situation timeline", "/v1/situations/" + secrets.get("situation") + "/timeline");
        out.put("situation analogs", "/v1/situations/" + secrets.get("situation") + "/analogs");
        out.put("question", "/v1/forecasts/questions/" + secrets.get("question"));
        out.put("question forecasts", "/v1/forecasts/questions/" + secrets.get("question") + "/forecasts");
        return out;
    }

    @Test
    @DisplayName("openReads and tokens without the private grant: no private item listed, every direct read 404")
    void apiReads() throws Exception {
        for (String caller : new String[] {null, read, agent}) {
            String who = caller == null ? "anonymous" : caller.equals(read) ? "read" : "agent";
            for (String list : LISTS) {
                String body = text(caller, list.replace("{public}", publicSituation));
                for (String marker : markers()) {
                    assertThat(body).as(who + " " + list).doesNotContain(marker);
                }
            }
            for (Map.Entry<String, String> direct : directs().entrySet()) {
                assertThat(call(caller, get(direct.getValue())).getStatus()).as(who + " " + direct.getKey())
                        .isEqualTo(404);
            }
        }
        // The control: admin sees each of them.
        assertThat(text(admin, "/v1/analytics/findings")).contains(secrets.get("finding"));
        assertThat(text(admin, "/v1/intel/events")).contains(secrets.get("intel event"));
        assertThat(text(admin, "/v1/situations")).contains(secrets.get("situation"));
        assertThat(text(admin, "/v1/forecasts/questions")).contains(secrets.get("question"));
        for (Map.Entry<String, String> direct : directs().entrySet()) {
            assertThat(call(admin, get(direct.getValue())).getStatus()).as("admin " + direct.getKey()).isLessThan(400);
        }
    }

    @Test
    @DisplayName("MCP tool output for an anonymous session never carries a private item")
    void mcpTools() {
        ToolContext anonymous = new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(SECRET),
                "203.0.113.9"), "session", McpSessions.STREAMABLE);
        Map<String, Map<String, Object>> calls = new LinkedHashMap<>();
        calls.put("list_findings", Map.of());
        calls.put("list_intel_events", Map.of());
        calls.put("list_situations", Map.of());
        calls.put("list_questions", Map.of());
        calls.put("find_analogs", Map.of("situationId", publicSituation));
        calls.put("get_finding", Map.of("findingId", secrets.get("finding")));
        calls.put("get_intel_event", Map.of("id", secrets.get("intel event")));
        calls.put("get_situation", Map.of("id", secrets.get("situation")));
        calls.put("get_question", Map.of("questionId", secrets.get("question")));
        for (Map.Entry<String, Map<String, Object>> c : calls.entrySet()) {
            McpSchema.CallToolResult result = mcp.call(c.getKey(), c.getValue(), anonymous);
            String output = String.valueOf(result.structuredContent()) + result.content();
            if (c.getKey().startsWith("get_")) {
                // A direct read of a private item is "not found"; the error may echo the id the caller sent.
                assertThat(result.isError()).as(c.getKey() + " " + output).isTrue();
            }
            for (String marker : markers()) {
                if (!c.getValue().containsValue(marker)) {
                    assertThat(output).as(c.getKey()).doesNotContain(marker);
                }
            }
        }
    }

    @Test
    @DisplayName("SSE: the analytics, intel, situations and forecasts topics replay nothing private to anonymous")
    void ssePayloads() {
        StreamPrincipal anonymous = StreamPrincipal.anonymous("203.0.113.9");
        for (EventTopic<?> topic : List.<EventTopic<?>>of(analyticsTopic, intelTopic, situationsTopic,
                forecastsTopic)) {
            List<FifoEvent> events = replay(topic, anonymous);
            for (FifoEvent e : events) {
                for (String marker : markers()) {
                    assertThat(String.valueOf(e.data())).as(topic.name() + " " + e.event()).doesNotContain(marker);
                }
            }
        }
        // The control: a principal with the private grant receives the private finding's event.
        List<FifoEvent> granted = replay(analyticsTopic, StreamPrincipal.forToken(UUID.randomUUID(), Scope.READ,
                "127.0.0.1").withPrivateAccess(true));
        assertThat(granted).anyMatch(e -> String.valueOf(e.data()).contains(secrets.get("finding")));
    }

    static <F> List<FifoEvent> replay(EventTopic<F> topic, StreamPrincipal principal) {
        F filter = topic.parseQuery(new TopicParams(topic.name(), Map.of()), principal);
        List<FifoEvent> out = new ArrayList<>();
        topic.replay(new ReplayRequest(0, false), filter, out::add);
        return out;
    }

    @Test
    @DisplayName("derived records (what tiles, feeds and federation serve) carry no private item and no raw value")
    void derivedRecords() {
        List<NormalizedRecord> emitted = new ArrayList<>();
        for (ai.foresignal.derive.DerivedGenerator generator : List.of(analyticsGenerator, intelGenerator)) {
            DerivedContext ctx = mock(DerivedContext.class);
            generator.reconcile(List.of(), ctx);
            generator.onTick(NOW.plusSeconds(30), ctx);
            ArgumentCaptor<NormalizedRecord> captor = ArgumentCaptor.forClass(NormalizedRecord.class);
            Mockito.verify(ctx, Mockito.atLeast(0)).emit(captor.capture());
            emitted.addAll(captor.getAllValues());
        }
        assertThat(emitted).as("the LOCAL_ONLY finding is published, without its raw values")
                .anyMatch(r -> r.externalId().equals(localFinding));
        for (NormalizedRecord r : emitted) {
            String payload = String.valueOf(r.payload());
            for (String marker : markers()) {
                assertThat(payload).as(r.kind() + " " + r.externalId()).doesNotContain(marker);
            }
            assertThat(payload).as("raw LOCAL_ONLY value in " + r.externalId()).doesNotContain(RAW);
        }
    }

    @Test
    @DisplayName("LOCAL_ONLY inputs (FRED, ACLED history, a LOCAL_ONLY series) never leave on a re-serving path")
    void localOnlyInputs() {
        assertThat(registry.descriptor(FredSeriesAdapter.ID).egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(ReferenceFamilies.EGRESS.get(ReferenceFamilies.ACLED)).isEqualTo("LOCAL_ONLY");
        String acledSource = ReferenceFamilies.SOURCE_IDS.get(ReferenceFamilies.ACLED);
        List<EgressPath> reServing = List.of(EgressPath.SHARE, EgressPath.PUBLIC_SHARE, EgressPath.FEED,
                EgressPath.FEDERATION, EgressPath.SINK, EgressPath.SINK_PUSH);
        ReadCaller anonymous = new ReadCaller(null, false, true);
        ReadCaller operator = new ReadCaller(Scope.ADMIN, false, true, null, true);
        AnalyticFindingEntity local = findings.visible(localFinding, true);
        for (EgressPath path : reServing) {
            for (Viewer viewer : new Viewer[] {Viewer.anonymous(), Viewer.admin()}) {
                assertThat(egress.allows(path, FredSeriesAdapter.ID, viewer)).as("fred " + path).isFalse();
                assertThat(egress.allows(path, acledSource, viewer)).as("acled " + path).isFalse();
                assertThat(egress.allows(path, AnalyticsTestSources.LOCAL, viewer)).as("series " + path).isFalse();
            }
            assertThat(baseRates.mixAllowed(ReferenceFamilies.ACLED, path)).as("ACLED counts on " + path).isFalse();
            assertThat(access.withheld(local, anonymous, path)).as("raw values on " + path).isTrue();
            assertThat(access.withheld(local, operator, path)).as("raw values on " + path + " for admin").isTrue();
        }
        assertThat(String.valueOf(FindingViews.full(local, null, true))).doesNotContain(RAW).contains(
                "valuesWithheld=true");
        // A private source never reaches a remote LLM provider, whoever asks.
        assertThat(egress.allows(EgressPath.LLM_REMOTE, SECRET, Viewer.admin())).isFalse();
    }
}
