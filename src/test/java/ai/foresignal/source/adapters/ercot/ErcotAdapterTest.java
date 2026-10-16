package ai.foresignal.source.adapters.ercot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code ercot-public} against a stub (prompt 093): the B2C token is fetched once and reused for 55 minutes, a 401
 * re-fetches it, and the report columns map to records by {@code fields[].name}.
 */
@IntegrationTest
class ErcotAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T06:00:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    ErcotAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new ErcotAdapter(baseUrl(), baseUrl() + "/token");
        harness.reset(adapter.id(), ErcotAdapter.LOAD_AT_KEY);
        harness.credentials.clear();
        harness.credentials.put(ErcotAdapter.USERNAME, "user");
        harness.credentials.put(ErcotAdapter.PASSWORD, "pass");
        harness.credentials.put(ErcotAdapter.SUBSCRIPTION_KEY, "sub-key");
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
        harness.credentials.clear();
    }

    private void serveToken() {
        WIREMOCK.stubFor(post(urlPathEqualTo("/token")).willReturn(aResponse().withStatus(200)
                .withBody("{\"id_token\":\"id-token-1\"}")));
    }

    private void serveReports() {
        WIREMOCK.stubFor(get(urlPathEqualTo(ErcotAdapter.LOAD_PATH)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("ercot-public", "load.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(ErcotAdapter.SPP_PATH)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("ercot-public", "spp.json"))));
    }

    private Map<String, Object> params() {
        return Map.of(ErcotAdapter.HUBS_PARAM, List.of("HB_HOUSTON"));
    }

    @Test
    @DisplayName("the token is fetched once and reused; the columns map to load and price points")
    void tokenReuse() throws Exception {
        serveToken();
        serveReports();
        FetchResult result = adapter.fetch(harness.context(adapter, params()));
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .contains("ercot:load:coast:2026-10-12T1", "ercot:spp:HB_HOUSTON:2026-10-12T1:1");
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo("/token")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(ErcotAdapter.LOAD_PATH))
                .withHeader("Ocp-Apim-Subscription-Key", equalTo("sub-key"))
                .withHeader("Authorization", equalTo("Bearer id-token-1")));

        adapter.fetch(harness.context(adapter, params()));
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo("/token")));

        harness.clock.advance(Duration.ofMinutes(56));
        adapter.fetch(harness.context(adapter, params()));
        WIREMOCK.verify(2, postRequestedFor(urlPathEqualTo("/token")));
    }

    @Test
    @DisplayName("a 401 re-fetches the token and retries once")
    void unauthorized() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(ErcotAdapter.LOAD_PATH)).inScenario("retry")
                .whenScenarioStateIs(STARTED).willReturn(aResponse().withStatus(401)).willSetStateTo("ok"));
        WIREMOCK.stubFor(get(urlPathEqualTo(ErcotAdapter.LOAD_PATH)).inScenario("retry")
                .whenScenarioStateIs("ok").willReturn(aResponse().withStatus(200)
                        .withBody(AdapterFixtures.bytes("ercot-public", "load.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(ErcotAdapter.SPP_PATH)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("ercot-public", "spp.json"))));
        serveToken();
        adapter.fetch(harness.context(adapter, params()));
        WIREMOCK.verify(2, postRequestedFor(urlPathEqualTo("/token")));
    }

    private static final String LOAD_FIELDS = "[{'name':'operatingDay'},{'name':'hourEnding'},{'name':'coast'},"
            + "{'name':'total'},{'name':'DSTFlag'}]";
    private static final String SPP_FIELDS = "[{'name':'deliveryDate'},{'name':'deliveryHour'},"
            + "{'name':'deliveryInterval'},{'name':'settlementPoint'},{'name':'settlementPointPrice'},{'name':'DSTFlag'}]";

    private void serve(String path, String fields, String rows) {
        String body = ("{'_meta':{'totalPages':1},'fields':" + fields + ",'data':[" + rows + "]}").replace('\'', '"');
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200).withBody(body)));
    }

    private Map<String, NormalizedRecord> points(FetchResult result) {
        Map<String, NormalizedRecord> out = new java.util.HashMap<>();
        result.records().stream().filter(r -> r.kind().equals("series-point")).forEach(r -> out.put(r.externalId(), r));
        return out;
    }

    @Test
    @DisplayName("fall back: hour ending 02:00 twice, told apart by DSTFlag, in Central time and never colliding")
    void fallBack() throws Exception {
        serveToken();
        serve(ErcotAdapter.LOAD_PATH, LOAD_FIELDS, "['2026-11-01','01:00',1,10,false],"
                + "['2026-11-01','02:00',2,20,false],['2026-11-01','02:00',3,30,true],['2026-11-01','03:00',4,40,false]");
        serve(ErcotAdapter.SPP_PATH, SPP_FIELDS, "['2026-11-01',2,1,'HB_HOUSTON',20.5,false],"
                + "['2026-11-01',2,1,'HB_HOUSTON',21.5,true]");
        Map<String, NormalizedRecord> byId = points(adapter.fetch(harness.context(adapter, params())));
        // CDT is UTC-5 until 02:00 local, then CST is UTC-6: HE01 starts 05:00Z, the first HE02 06:00Z, the
        // repeated HE02 07:00Z and HE03 08:00Z.
        assertThat(byId.get("ercot:load:coast:2026-11-01T1").eventTime()).isEqualTo(Instant.parse("2026-11-01T05:00:00Z"));
        assertThat(byId.get("ercot:load:coast:2026-11-01T2").eventTime()).isEqualTo(Instant.parse("2026-11-01T06:00:00Z"));
        assertThat(byId.get("ercot:load:coast:2026-11-01T2-DST").eventTime())
                .isEqualTo(Instant.parse("2026-11-01T07:00:00Z"));
        assertThat(byId.get("ercot:load:coast:2026-11-01T3").eventTime()).isEqualTo(Instant.parse("2026-11-01T08:00:00Z"));
        assertThat(byId.get("ercot:spp:HB_HOUSTON:2026-11-01T2:1").eventTime())
                .isEqualTo(Instant.parse("2026-11-01T06:00:00Z"));
        assertThat(byId.get("ercot:spp:HB_HOUSTON:2026-11-01T2:1-DST").eventTime())
                .isEqualTo(Instant.parse("2026-11-01T07:00:00Z"));
        assertThat(byId.get("ercot:spp:HB_HOUSTON:2026-11-01T2:1-DST").payload()).containsEntry("value", 21.5)
                .containsEntry("frequency", "PT15M");
    }

    @Test
    @DisplayName("spring forward: the missing hour is skipped and the hours around it are an hour apart")
    void springForward() throws Exception {
        serveToken();
        serve(ErcotAdapter.LOAD_PATH, LOAD_FIELDS, "['2026-03-08','02:00',2,20,false],"
                + "['2026-03-08','03:00',3,30,false],['2026-03-08','04:00',4,40,false]");
        serve(ErcotAdapter.SPP_PATH, SPP_FIELDS, "['2026-03-08',3,1,'HB_HOUSTON',20.5,false]");
        Map<String, NormalizedRecord> byId = points(adapter.fetch(harness.context(adapter, params())));
        assertThat(byId.get("ercot:load:coast:2026-03-08T2").eventTime()).isEqualTo(Instant.parse("2026-03-08T07:00:00Z"));
        assertThat(byId).doesNotContainKey("ercot:load:coast:2026-03-08T3")
                .doesNotContainKey("ercot:spp:HB_HOUSTON:2026-03-08T3:1");
        assertThat(byId.get("ercot:load:coast:2026-03-08T4").eventTime()).isEqualTo(Instant.parse("2026-03-08T08:00:00Z"));
    }

    @Test
    @DisplayName("the hourly load report is read once an hour; prices every run")
    void loadHourly() throws Exception {
        serveToken();
        serveReports();
        adapter.fetch(harness.context(adapter, params()));
        harness.clock.advance(Duration.ofMinutes(15));
        FetchResult second = adapter.fetch(harness.context(adapter, params()));
        assertThat(second.complete()).as("a run without the load report is partial").isFalse();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(ErcotAdapter.LOAD_PATH)));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(ErcotAdapter.SPP_PATH)));
        harness.clock.advance(Duration.ofMinutes(45));
        adapter.fetch(harness.context(adapter, params()));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(ErcotAdapter.LOAD_PATH)));
        harness.clock.setInstant(NOW);
    }

    @Test
    @DisplayName("a day of default polling fits the default daily budget")
    void dailyBudget() {
        int runs = (int) (Duration.ofDays(1).toMinutes() / adapter.descriptor().defaultInterval().toMinutes());
        int reports = 24 + runs * ErcotAdapter.DEFAULT_HUBS.size();
        int tokens = (int) Math.ceil(Duration.ofDays(1).toMinutes() / (double) Duration.ofMinutes(55).toMinutes());
        assertThat(reports + tokens).isLessThanOrEqualTo(adapter.descriptor().defaultBudget().perDay());
        assertThat(1 + 1 + ErcotAdapter.DEFAULT_HUBS.size())
                .isLessThanOrEqualTo(adapter.descriptor().defaultBudget().perMinute());
    }

    @Test
    @DisplayName("hourEnding text and DSTFlag spellings are read")
    void helpers() {
        assertThat(ErcotAdapter.hourEnding("01:00")).isEqualTo(1L);
        assertThat(ErcotAdapter.hourEnding("24:00")).isEqualTo(24L);
        assertThat(ErcotAdapter.hourEnding(7.0)).isEqualTo(7L);
        assertThat(ErcotAdapter.hourEnding(null)).isNull();
        assertThat(ErcotAdapter.dstFlag("Y")).isTrue();
        assertThat(ErcotAdapter.dstFlag(Boolean.TRUE)).isTrue();
        assertThat(ErcotAdapter.dstFlag("N")).isFalse();
        assertThat(ErcotAdapter.dstFlag(null)).isFalse();
    }
}
