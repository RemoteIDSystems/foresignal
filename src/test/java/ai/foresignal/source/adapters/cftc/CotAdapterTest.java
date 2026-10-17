package ai.foresignal.source.adapters.cftc;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.PersistentAdapterStateStore;
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
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
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
 * {@code cftc-cot} against a stub (prompt 117): the SoQL query, {@code $offset} paging, the app-token header, the
 * per-report cursor and no duplicate versions on re-poll.
 */
@IntegrationTest
class CotAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");
    static final String DISAGG = "/resource/72hh-3qpy.json";
    static final String LEGACY = "/resource/6dca-aqww.json";
    static final String TFF = "/resource/gpe5-46if.json";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    CftcCotAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new CftcCotAdapter(baseUrl());
        harness.reset(adapter.id());
        harness.credentials.clear();
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", adapter.id());
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
        harness.credentials.clear();
    }

    private static void serve(String path, String fixture) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(AdapterFixtures.bytes("cftc-cot", fixture))));
    }

    private static void serveEmpty(String path) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("[]")));
    }

    private Map<String, Object> params(String... reports) {
        return Map.of(CftcCotAdapter.REPORTS_PARAM, List.of(reports),
                CftcCotAdapter.CONTRACTS_PARAM, List.of("067651", "088691", "13874A", "043602"));
    }

    @Test
    @DisplayName("the SoQL query: cursor date, contract list, order, limit and offset")
    void soql() throws Exception {
        serve(DISAGG, "disaggregated.json");
        adapter.fetch(harness.context(adapter, params("disaggregated")));
        List<LoggedRequest> requests = WIREMOCK.findAll(getRequestedFor(urlPathEqualTo(DISAGG)));
        assertThat(requests).hasSize(1);
        LoggedRequest request = requests.get(0);
        assertThat(request.queryParameter("$where").firstValue())
                .isEqualTo("report_date_as_yyyy_mm_dd > '2025-10-16' AND cftc_contract_market_code in"
                        + " ('043602','067651','088691','13874A')");
        assertThat(request.queryParameter("$order").firstValue()).isEqualTo("report_date_as_yyyy_mm_dd,id");
        assertThat(request.queryParameter("$limit").firstValue()).isEqualTo("1000");
        assertThat(request.queryParameter("$offset").firstValue()).isEqualTo("0");
        assertThat(request.getHeader("X-App-Token")).isNull();
    }

    @Test
    @DisplayName("points and one series-latest per series with the 117 metadata")
    void records() throws Exception {
        serve(DISAGG, "disaggregated.json");
        FetchResult result = adapter.fetch(harness.context(adapter, params("disaggregated")));
        List<NormalizedRecord> latest = result.records().stream().filter(r -> r.kind().equals("series-latest"))
                .toList();
        assertThat(latest).hasSize(18);
        NormalizedRecord net = latest.stream()
                .filter(r -> r.externalId().equals("cot.067651.disaggregated.mm_net:latest")).findFirst().orElseThrow();
        assertThat(net.payload()).containsEntry("period", "2026-10-13").containsEntry("family", "position")
                .containsEntry("transform", "difference").containsEntry("releaseLagDays", 3.0)
                .containsEntry("egress", "OPEN");
        assertThat(result.complete()).isFalse();
    }

    @Test
    @DisplayName("a full page asks for the next one with $offset")
    void paging() throws Exception {
        StringBuilder page = new StringBuilder("[");
        for (int i = 0; i < CftcCotAdapter.PAGE_SIZE; i++) {
            if (i > 0) {
                page.append(',');
            }
            String date = java.time.LocalDate.parse("2026-01-06").plusWeeks(i % 40).toString();
            page.append("{\"report_date_as_yyyy_mm_dd\":\"").append(date)
                    .append("T00:00:00.000\",\"cftc_contract_market_code\":\"067651\",\"open_interest_all\":\"")
                    .append(1000 + i).append("\"}");
        }
        page.append(']');
        WIREMOCK.stubFor(get(urlPathEqualTo(LEGACY)).withQueryParam("$offset", equalTo("0"))
                .willReturn(aResponse().withStatus(200).withBody(page.toString())));
        WIREMOCK.stubFor(get(urlPathEqualTo(LEGACY)).withQueryParam("$offset", equalTo("1000"))
                .willReturn(aResponse().withStatus(200).withBody("[]")));
        adapter.fetch(harness.context(adapter, params("legacy")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(LEGACY)).withQueryParam("$offset", equalTo("0")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(LEGACY)).withQueryParam("$offset", equalTo("1000")));
    }

    @Test
    @DisplayName("the app token is sent as X-App-Token when set")
    void appToken() throws Exception {
        serveEmpty(TFF);
        harness.credentials.put(CftcCotAdapter.APP_TOKEN, "app-token-123");
        adapter.fetch(harness.context(adapter, params("tff")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(TFF)).withHeader("X-App-Token", equalTo("app-token-123")));
    }

    @Test
    @DisplayName("the futures-and-options datasets are read when futuresOnly is false")
    void combined() throws Exception {
        serveEmpty("/resource/kh3c-gbw2.json");
        adapter.fetch(harness.context(adapter, Map.of(CftcCotAdapter.REPORTS_PARAM, List.of("disaggregated"),
                CftcCotAdapter.FUTURES_ONLY_PARAM, false)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/resource/kh3c-gbw2.json")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(DISAGG)));
    }

    @Test
    @DisplayName("a re-poll asks only for reports after the cursor, so no version is duplicated")
    void rePoll() throws Exception {
        serve(DISAGG, "disaggregated.json");
        adapter.fetch(harness.context(adapter, params("disaggregated")));
        WIREMOCK.resetRequests();
        WIREMOCK.stubFor(get(urlPathEqualTo(DISAGG))
                .withQueryParam("$where", containing("> '2026-10-13'"))
                .willReturn(aResponse().withStatus(200).withBody("[]")));
        FetchResult second = adapter.fetch(harness.context(adapter, params("disaggregated")));
        assertThat(second.records()).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DISAGG))
                .withQueryParam("$where", containing("report_date_as_yyyy_mm_dd > '2026-10-13'")));
        Map<String, String> state = new PersistentAdapterStateStore(adapter.id(), jdbc, jsonMapper).all();
        assertThat(state.entrySet()).anySatisfy(e -> {
            assertThat(e.getKey()).startsWith("cursor.disaggregated.f.");
            assertThat(e.getValue()).isEqualTo("2026-10-13");
        });
    }

    @Test
    @DisplayName("contract codes and report names are checked before a request")
    void checkParams() {
        assertThat(adapter.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of(
                CftcCotAdapter.CONTRACTS_PARAM, List.of("067651') OR (1=1"))))).containsKey("contracts");
        assertThat(adapter.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of(
                CftcCotAdapter.REPORTS_PARAM, List.of("weekly"))))).containsKey("reports");
        assertThat(adapter.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of(
                CftcCotAdapter.CONTRACTS_PARAM, List.of("13874a"))))).isEmpty();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(DISAGG)).withHeader("X-App-Token", absent()));
    }
}
