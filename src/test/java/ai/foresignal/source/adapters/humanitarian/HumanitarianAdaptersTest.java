package ai.foresignal.source.adapters.humanitarian;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.reference.BundledCountryPolygons;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The prompt 090 adapter edge cases (WireMock): ReliefWeb's appname 403 and encoding, the GeoNet Accept header,
 * Copernicus fetching the detail only on change, the WHO DON query encoding, UNHCR's '-' rows in fetch(), and the
 * HANS coordinate cache surviving a failed refresh.
 */
@IntegrationTest
class HumanitarianAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        harness.credentials.put(ReliefWebDisastersAdapter.APPNAME, "test-app");
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(ReliefWebDisastersAdapter.ID);
        harness.backoff.onSuccess(GeoNetVolcanoAdapter.ID);
        harness.backoff.onSuccess(CopernicusEmsAdapter.ID);
        harness.backoff.onSuccess(WhoDonAdapter.ID);
        harness.backoff.onSuccess(UnhcrPopulationAdapter.ID);
        harness.backoff.onSuccess(UsgsHansVolcanoAdapter.ID);
    }

    @Test
    @DisplayName("ReliefWeb's appname 403 is APPNAME_NOT_APPROVED and is not retried")
    void reliefWeb403() {
        WIREMOCK.stubFor(post(urlPathEqualTo("/v2/disasters")).willReturn(aResponse().withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"You are not using an approved appname\"}")));
        ReliefWebDisastersAdapter adapter = new ReliefWebDisastersAdapter(baseUrl());
        harness.reset(adapter.id());
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(CredentialsRejectedException.class)
                .hasMessageContaining("APPNAME_NOT_APPROVED");
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo("/v2/disasters")));
    }

    @Test
    @DisplayName("GeoNet is fetched with the versioned GeoJSON Accept header")
    void geonetAccept() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/volcano/val")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json")
                .withBody(AdapterFixtures.bytes("geonet-volcanoes", "volcano-val.json"))));
        GeoNetVolcanoAdapter adapter = new GeoNetVolcanoAdapter(baseUrl());
        harness.reset(adapter.id());
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).isNotEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/volcano/val"))
                .withHeader("Accept", matching(".*geo\\+json.*")));
    }

    @Test
    @DisplayName("the Copernicus first run keeps every activation, and the detail is fetched only on change")
    void copernicusDetailOnChange() throws Exception {
        byte[] info = AdapterFixtures.bytes("copernicus-ems", "activations-info.json");
        int count = JSON.readTree(info).get("results").size();
        serveCopernicusInfo(info);
        WIREMOCK.stubFor(get(urlPathEqualTo("/backend/dashboard-api/public-activations/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("copernicus-ems", "detail.json"))));
        CopernicusEmsAdapter adapter = new CopernicusEmsAdapter(baseUrl());
        harness.reset(adapter.id(), CopernicusEmsAdapter.CURSOR_KEY);

        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).as("every activation of a newest-first page on the first run")
                .hasSize(count);
        assertThat(first.records()).anyMatch(r -> r.payload().get("reason") != null);
        WIREMOCK.verify(count, getRequestedFor(urlPathEqualTo("/backend/dashboard-api/public-activations/")));

        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.records()).isEmpty();
        WIREMOCK.verify(count, getRequestedFor(urlPathEqualTo("/backend/dashboard-api/public-activations/")));

        // one activation's lastUpdate moves forward: exactly that one is re-read with its detail
        ObjectNode root = (ObjectNode) JSON.readTree(info);
        ObjectNode bumped = (ObjectNode) root.get("results").get(count - 1);
        bumped.put("lastUpdate", "2030-01-01T00:00:00Z");
        serveCopernicusInfo(root.toString().getBytes(StandardCharsets.UTF_8));
        FetchResult third = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(third.records()).hasSize(1);
        assertThat(third.records().get(0).externalId()).isEqualTo(bumped.get("code").asString());
        WIREMOCK.verify(count + 1, getRequestedFor(urlPathEqualTo("/backend/dashboard-api/public-activations/")));
    }

    @Test
    @DisplayName("a Copernicus detail refused on budget keeps the run: every activation, summaries for the rest")
    void copernicusDetailBudget() throws Exception {
        byte[] info = AdapterFixtures.bytes("copernicus-ems", "activations-info.json");
        int count = JSON.readTree(info).get("results").size();
        serveCopernicusInfo(info);
        WIREMOCK.stubFor(get(urlPathEqualTo("/backend/dashboard-api/public-activations/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("copernicus-ems", "detail.json"))));
        AdapterHttpHarness tight = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(3, 300));
        CopernicusEmsAdapter adapter = new CopernicusEmsAdapter(baseUrl());
        tight.reset(adapter.id(), CopernicusEmsAdapter.CURSOR_KEY);

        FetchResult first = adapter.fetch(tight.context(adapter, Map.of()));

        assertThat(count).isGreaterThan(3);
        assertThat(first.records()).hasSize(count);
        // One list request, then details until the third request of the minute; none tried after the refusal.
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/backend/dashboard-api/public-activations-info/")));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo("/backend/dashboard-api/public-activations/")));
        assertThat(tight.client.stateStore(adapter.id()).get(CopernicusEmsAdapter.CURSOR_KEY)).isPresent();
    }

    private static void serveCopernicusInfo(byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/backend/dashboard-api/public-activations-info/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    @Test
    @DisplayName("OpenFEMA watermarks the last refresh into the next request")
    void openFemaWatermark() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/open/v2/DisasterDeclarationsSummaries"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("openfema-declarations", "declarations.json"))));
        OpenFemaDeclarationsAdapter adapter = new OpenFemaDeclarationsAdapter(baseUrl(), CountyGeometries.none());
        harness.reset(adapter.id(), OpenFemaDeclarationsAdapter.CURSOR_KEY);
        adapter.fetch(harness.context(adapter, Map.of()));
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api/open/v2/DisasterDeclarationsSummaries"))
                .withQueryParam("$filter", matching(".*lastRefresh.*")));
    }

    @Test
    @DisplayName("WHO DON fetch() sends the OData query percent-encoded and parses the result")
    void whoDonFetch() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/emergencies/diseaseoutbreaknews"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("who-don", "diseaseoutbreaknews.json"))));
        WhoDonAdapter adapter = new WhoDonAdapter(baseUrl(), new CountryCentroids());
        harness.reset(adapter.id());
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).isNotEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api/emergencies/diseaseoutbreaknews"))
                .withQueryParam("$orderby", equalTo("PublicationDate desc"))
                .withQueryParam("$top", equalTo("50")));
    }

    @Test
    @DisplayName("WHO DON counts a title country the gazetteer cannot resolve")
    void whoDonUnresolvedCountry() {
        UnresolvedCountries unresolved = new UnresolvedCountries();
        new WhoDonAdapter("", new CountryCentroids()).parse(("{\"value\":[{\"Id\":\"x\",\"Title\":"
                + "\"Cholera - Atlantis\"}]}").getBytes(StandardCharsets.UTF_8), unresolved);
        assertThat(unresolved.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("UNHCR fetch() skips the '-' country rows instead of failing on a null record")
    void unhcrFetchSkipsSentinels() throws Exception {
        ObjectNode body = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("unhcr-population", "population-2024.json"));
        body.put("maxPages", 1);
        WIREMOCK.stubFor(get(urlPathEqualTo("/population/v1/population/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(body.toString())));
        UnhcrPopulationAdapter adapter = new UnhcrPopulationAdapter(baseUrl(), new CountryCentroids());
        harness.reset(adapter.id());
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of("yearsBack", 1)));
        assertThat(result.records()).isNotEmpty().doesNotContainNull()
                .allMatch(r -> r.externalId().startsWith("coo:"), "the coa rows of this fixture are all '-'");
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo("/population/v1/population/")));
    }

    @Test
    @DisplayName("HANS uses the cached coordinates when the weekly coordinate refresh fails")
    void hansCoordinateRefreshFailure() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/hans-public/api/volcano/getMonitoredVolcanoes"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("usgs-hans-volcanoes", "monitored.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/hans-public/api/volcano/getUSVolcanoes"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("usgs-hans-volcanoes", "us-volcanoes.json"))));
        UsgsHansVolcanoAdapter adapter = new UsgsHansVolcanoAdapter(baseUrl());
        harness.reset(adapter.id(), UsgsHansVolcanoAdapter.COORDS_KEY, UsgsHansVolcanoAdapter.COORDS_AT_KEY);
        long placed = adapter.fetch(harness.context(adapter, Map.of())).records().stream()
                .filter(r -> r.geometry() != null).count();
        assertThat(placed).isPositive();

        // a week later the refresh is due, and the upstream answers 500: the client throws, the cache is used
        harness.clock.advance(UsgsHansVolcanoAdapter.COORDS_TTL.plusHours(1));
        WIREMOCK.stubFor(get(urlPathEqualTo("/hans-public/api/volcano/getUSVolcanoes"))
                .willReturn(aResponse().withStatus(500)));
        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.records().stream().filter(r -> r.geometry() != null).count()).isEqualTo(placed);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo("/hans-public/api/volcano/getUSVolcanoes")));
    }

    @Test
    @DisplayName("ReliefWeb sends the appname percent-encoded")
    void reliefWebAppnameEncoded() throws Exception {
        WIREMOCK.stubFor(post(urlPathEqualTo("/v2/disasters")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("reliefweb-disasters", "disasters.json"))));
        harness.credentials.put(ReliefWebDisastersAdapter.APPNAME, "my app&x=1");
        ReliefWebDisastersAdapter adapter = new ReliefWebDisastersAdapter(baseUrl());
        harness.reset(adapter.id());
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).isNotEmpty();
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo("/v2/disasters"))
                .withQueryParam("appname", equalTo("my app&x=1")));
    }
}
