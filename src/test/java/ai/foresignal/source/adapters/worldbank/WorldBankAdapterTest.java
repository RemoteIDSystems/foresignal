package ai.foresignal.source.adapters.worldbank;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.PersistentAdapterStateStore;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
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
 * {@code worldbank-indicators} against a stub (prompt 117): aggregates skipped by {@code region.id}, ISO3 mapped to
 * the 040 ISO2 code, paging to the last page, the annual structural series and an unknown indicator skipped.
 */
@IntegrationTest
class WorldBankAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");
    static final String DATA = "/v2/country/all/indicator/SP.POP.TOTL";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    WorldBankIndicatorsAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new WorldBankIndicatorsAdapter(baseUrl());
        harness.reset(adapter.id());
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", adapter.id());
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    private static void serve(String path, String fixture) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("worldbank-indicators", fixture))));
    }

    private static void serveAll() {
        serve("/v2/country", "countries.json");
        serve("/v2/indicator/SP.POP.TOTL", "indicator-SP.POP.TOTL.json");
        WIREMOCK.stubFor(get(urlPathEqualTo(DATA)).withQueryParam("page", equalTo("1"))
                .willReturn(aResponse().withStatus(200)
                        .withBody(AdapterFixtures.bytes("worldbank-indicators", "data-page1.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(DATA)).withQueryParam("page", equalTo("2"))
                .willReturn(aResponse().withStatus(200)
                        .withBody(AdapterFixtures.bytes("worldbank-indicators", "data-page2.json"))));
    }

    private Map<String, Object> params(String... indicators) {
        return Map.of(WorldBankIndicatorsAdapter.INDICATORS_PARAM, List.of(indicators),
                WorldBankIndicatorsAdapter.FROM_YEAR_PARAM, 2022);
    }

    @Test
    @DisplayName("aggregates (Arab World, World) are skipped; countries map ISO3 to ISO2; both pages are read")
    void aggregatesAndPaging() throws Exception {
        serveAll();
        FetchResult result = adapter.fetch(harness.context(adapter, params("SP.POP.TOTL")));
        List<String> ids = result.records().stream().map(NormalizedRecord::externalId).toList();
        assertThat(ids).contains("wb.SP.POP.TOTL.DE:2023", "wb.SP.POP.TOTL.XK:2022", "wb.SP.POP.TOTL.US:2023",
                "wb.SP.POP.TOTL.YE:2022", "wb.SP.POP.TOTL.YE:latest");
        assertThat(ids).noneMatch(id -> id.contains(".1A") || id.contains(".1W") || id.contains(".ARB")
                || id.contains(".WLD"));
        assertThat(result.records().stream().filter(r -> r.kind().equals("series-point")).count()).isEqualTo(8);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DATA)).withQueryParam("page", equalTo("2"))
                .withQueryParam("per_page", equalTo("20000")).withQueryParam("date", equalTo("2022:2026")));
    }

    @Test
    @DisplayName("annual structural series with regionCode, a 180-day lag and the source organization")
    void metadata() throws Exception {
        serveAll();
        FetchResult result = adapter.fetch(harness.context(adapter, params("SP.POP.TOTL")));
        NormalizedRecord point = result.records().stream()
                .filter(r -> r.externalId().equals("wb.SP.POP.TOTL.YE:2023")).findFirst().orElseThrow();
        assertThat(point.payload()).containsEntry("frequency", "P1Y").containsEntry("area", "YE")
                .containsEntry("value", 39390799.0).containsEntry("period", "2023");
        assertThat(point.eventTime()).isEqualTo(Instant.parse("2023-01-01T00:00:00Z"));
        NormalizedRecord latest = result.records().stream()
                .filter(r -> r.externalId().equals("wb.SP.POP.TOTL.YE:latest")).findFirst().orElseThrow();
        assertThat(latest.payload()).containsEntry("family", "structural").containsEntry("regionCode", "YE")
                .containsEntry("releaseLagDays", 180.0).containsEntry("period", "2023");
        assertThat(String.valueOf(latest.payload().get("attribution"))).contains("World Population Prospects");
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), result.records());
    }

    @Test
    @DisplayName("an unknown indicator is skipped with a health warning; the others are read")
    void unknownIndicator() throws Exception {
        serveAll();
        serve("/v2/indicator/NOT.AN.INDICATOR", "invalid-indicator.json");
        FetchResult result = adapter.fetch(harness.context(adapter, params("NOT.AN.INDICATOR", "SP.POP.TOTL")));
        assertThat(result.records()).isNotEmpty();
        AdapterStateStore state = new PersistentAdapterStateStore(adapter.id(), jdbc, jsonMapper);
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValueSatisfying(
                reason -> assertThat(reason).contains("NOT.AN.INDICATOR"));
    }

    @Test
    @DisplayName("the parser: countries without aggregates, error bodies refused")
    void parser() {
        Map<String, String> countries = WorldBankParser.countries(WorldBankParser.page(
                AdapterFixtures.bytes("worldbank-indicators", "countries.json")).rows());
        assertThat(countries).containsEntry("USA", "US").containsEntry("XKX", "XK").containsEntry("YEM", "YE")
                .doesNotContainKeys("ARB", "WLD");
        assertThatThrownBy(() -> WorldBankParser.page(AdapterFixtures.bytes("worldbank-indicators",
                "invalid-indicator.json"))).isInstanceOf(UpstreamFormatException.class);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of(
                WorldBankIndicatorsAdapter.FROM_YEAR_PARAM, 1900)))).containsKey("fromYear");
    }
}
