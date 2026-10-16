package ai.foresignal.source.adapters.eia;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.collect.UpstreamHttpException;
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
 * The EIA sources against a stub (prompt 093): the {@code api_key} query parameter is redacted in every trace, the
 * incomplete-return warning fails the fetch, the series map to records and a 429 backs the source off.
 */
@IntegrationTest
class EiaAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T06:00:00Z");
    static final String KEY = "eia-test-api-key-0123456789";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    EiaPetroleumAdapter petroleum;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        petroleum = new EiaPetroleumAdapter(baseUrl());
        harness.reset(petroleum.id());
        harness.credentials.clear();
        harness.credentials.put(EiaApi.API_KEY, KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(petroleum.id());
        harness.credentials.clear();
    }

    private void serve(String path, String series, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).withQueryParam("facets[series][]", equalTo(series))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    @Test
    @DisplayName("petroleum: the key is in the URL but redacted, and the rows become points and a latest")
    void petroleumRecords() throws Exception {
        serve(EiaApi.PETROLEUM_SPOT_ROUTE, "RWTC",
                new String(AdapterFixtures.bytes("eia-petroleum", "spot.json")));
        WIREMOCK.stubFor(get(urlPathEqualTo(EiaApi.PETROLEUM_STOCKS_ROUTE)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("eia-petroleum", "stock.json"))));
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(petroleum,
                Map.of(EiaPetroleumAdapter.SPOT_PARAM, List.of("RWTC"), EiaPetroleumAdapter.STOCK_PARAM,
                        List.of("WCESTUS1")),
                recorder);
        FetchResult result = petroleum.fetch(ctx);
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .contains("eia:RWTC:2026-10-09", "eia:RWTC:latest");
        NormalizedRecord point = result.records().stream()
                .filter(r -> r.externalId().equals("eia:RWTC:2026-10-09")).findFirst().orElseThrow();
        assertThat(point.payload()).containsEntry("value", 61.23).containsEntry("unit", "[USD]/[bbl_us]")
                .containsEntry("seriesName", "Cushing, OK WTI Spot Price FOB");
        // The key is sent in the query, and redacted from every trace and recorded exchange.
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(EiaApi.PETROLEUM_SPOT_ROUTE))
                .withQueryParam("api_key", equalTo(KEY)));
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl)
                .allSatisfy(url -> assertThat(url).doesNotContain(KEY));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(KEY);
        }
    }

    @Test
    @DisplayName("the incomplete-return warning fails the fetch instead of silently truncating")
    void incomplete() {
        serve(EiaApi.PETROLEUM_SPOT_ROUTE, "RWTC",
                new String(AdapterFixtures.bytes("eia-petroleum", "incomplete.json")));
        assertThatThrownBy(() -> petroleum.fetch(harness.context(petroleum,
                Map.of(EiaPetroleumAdapter.SPOT_PARAM, List.of("RWTC"), EiaPetroleumAdapter.STOCK_PARAM, List.of()))))
                .isInstanceOf(ai.foresignal.source.spi.UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a missing key is refused before any request")
    void missingKey() {
        harness.credentials.clear();
        assertThatThrownBy(() -> petroleum.fetch(harness.context(petroleum,
                Map.of(EiaPetroleumAdapter.SPOT_PARAM, List.of("RWTC"), EiaPetroleumAdapter.STOCK_PARAM, List.of()))))
                .isInstanceOf(ai.foresignal.source.spi.AdapterException.class);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("a 429 with Retry-After backs the source off and nothing is sent while it lasts")
    void retryAfter() {
        WIREMOCK.stubFor(get(urlPathEqualTo(EiaApi.PETROLEUM_SPOT_ROUTE)).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "600").withBody("Too Many Requests")));
        assertThatThrownBy(() -> petroleum.fetch(harness.context(petroleum,
                Map.of(EiaPetroleumAdapter.SPOT_PARAM, List.of("RWTC"), EiaPetroleumAdapter.STOCK_PARAM, List.of()))))
                .isInstanceOf(UpstreamHttpException.class);
        assertThat(harness.backoff.isInBackoff(petroleum.id())).isTrue();
        WIREMOCK.resetRequests();
        assertThatThrownBy(() -> petroleum.fetch(harness.context(petroleum,
                Map.of(EiaPetroleumAdapter.SPOT_PARAM, List.of("RWTC"), EiaPetroleumAdapter.STOCK_PARAM, List.of()))))
                .isInstanceOf(ai.foresignal.source.spi.AdapterException.class);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("eia-930 region and fuel rows become points and a latest, DF forecast")
    void eia930() throws Exception {
        Eia930Adapter adapter = new Eia930Adapter(baseUrl());
        WIREMOCK.stubFor(get(urlPathEqualTo(EiaApi.RTO_REGION_ROUTE)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("eia-930", "region.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(EiaApi.RTO_FUEL_ROUTE)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("eia-930", "fuel.json"))));
        harness.credentials.put(EiaApi.API_KEY, KEY);
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .contains("eia930:ERCO:D:2026-10-13T00", "eia930:ERCO:D:latest", "eia930:ERCO:fuel:WND:2026-10-13T00");
        NormalizedRecord forecast = result.records().stream()
                .filter(r -> r.externalId().equals("eia930:ERCO:DF:2026-10-13T00")).findFirst().orElseThrow();
        assertThat(forecast.payload()).containsEntry("status", "forecast");
        assertThat(result.records()).noneMatch(r -> r.externalId().equals("eia930:ERCO:DF:latest"));
    }

    /** An EIA-930 region page of {@code hours} ERCO demand rows starting at {@code firstHour}, claiming {@code total}. */
    private static String regionPage(int total, int firstHour, int hours) {
        StringBuilder rows = new StringBuilder();
        for (int h = firstHour; h < firstHour + hours; h++) {
            rows.append(rows.isEmpty() ? "" : ",").append("{\"period\":\"2026-10-13T")
                    .append(String.format("%02d", h)).append("\",\"respondent\":\"ERCO\",\"type\":\"D\",")
                    .append("\"value\":\"41000\",\"value-units\":\"megawatthours\"}");
        }
        return "{\"response\":{\"total\":\"" + total + "\",\"frequency\":\"hourly\",\"data\":[" + rows
                + "],\"warnings\":[]}}";
    }

    private static final String EMPTY_PAGE = "{\"response\":{\"total\":\"0\",\"data\":[],\"warnings\":[]}}";

    @Test
    @DisplayName("eia-930 pages with offset at the 5,000-row length until response.total rows have arrived")
    void eia930Paging() throws Exception {
        Eia930Adapter adapter = new Eia930Adapter(baseUrl());
        harness.reset(adapter.id());
        WIREMOCK.stubFor(get(urlPathEqualTo(EiaApi.RTO_REGION_ROUTE)).withQueryParam("offset", equalTo("0"))
                .willReturn(aResponse().withStatus(200).withBody(regionPage(5, 0, 3))));
        WIREMOCK.stubFor(get(urlPathEqualTo(EiaApi.RTO_REGION_ROUTE)).withQueryParam("offset", equalTo("3"))
                .willReturn(aResponse().withStatus(200).withBody(regionPage(5, 3, 2))));
        WIREMOCK.stubFor(get(urlPathEqualTo(EiaApi.RTO_FUEL_ROUTE))
                .willReturn(aResponse().withStatus(200).withBody(EMPTY_PAGE)));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).filteredOn(r -> r.kind().equals("series-point")).hasSize(5);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(EiaApi.RTO_REGION_ROUTE))
                .withQueryParam("length", equalTo("5000")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(EiaApi.RTO_REGION_ROUTE)).withQueryParam("offset", equalTo("3")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(EiaApi.RTO_FUEL_ROUTE)));
        harness.backoff.onSuccess(adapter.id());
    }

    @Test
    @DisplayName("eia-930 never accepts a silently truncated window: past two pages it is partial and degraded")
    void eia930Truncated() throws Exception {
        Eia930Adapter adapter = new Eia930Adapter(baseUrl());
        harness.reset(adapter.id(), "health.degraded");
        WIREMOCK.stubFor(get(urlPathEqualTo(EiaApi.RTO_REGION_ROUTE))
                .willReturn(aResponse().withStatus(200).withBody(regionPage(100, 0, 1))));
        WIREMOCK.stubFor(get(urlPathEqualTo(EiaApi.RTO_FUEL_ROUTE))
                .willReturn(aResponse().withStatus(200).withBody(EMPTY_PAGE)));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.complete()).isFalse();
        WIREMOCK.verify(Eia930Adapter.MAX_PAGES_PER_DATASET,
                getRequestedFor(urlPathEqualTo(EiaApi.RTO_REGION_ROUTE)));
        assertThat(harness.client.stateStore(adapter.id()).get("health.degraded"))
                .hasValueSatisfying(reason -> assertThat(reason).contains("region-data"));
        harness.backoff.onSuccess(adapter.id());
    }

    @Test
    @DisplayName("a weekly stock point is the week ending on its label, so observedAt is not a week in the future")
    void weeklyIsWeekEnding() throws Exception {
        serve(EiaApi.PETROLEUM_SPOT_ROUTE, "RWTC", new String(AdapterFixtures.bytes("eia-petroleum", "spot.json")));
        WIREMOCK.stubFor(get(urlPathEqualTo(EiaApi.PETROLEUM_STOCKS_ROUTE)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("eia-petroleum", "stock.json"))));
        FetchResult result = petroleum.fetch(harness.context(petroleum,
                Map.of(EiaPetroleumAdapter.SPOT_PARAM, List.of("RWTC"), EiaPetroleumAdapter.STOCK_PARAM,
                        List.of("WCESTUS1"))));
        NormalizedRecord week = result.records().stream()
                .filter(r -> r.externalId().equals("eia:WCESTUS1:2026-10-02")).findFirst().orElseThrow();
        assertThat(week.eventTime()).isEqualTo(Instant.parse("2026-09-26T00:00:00Z"));
        assertThat(week.observedAt()).isEqualTo(Instant.parse("2026-10-03T00:00:00Z"));
        assertThat(week.payload()).containsEntry("unit", "k[bbl_us]");
    }
}
