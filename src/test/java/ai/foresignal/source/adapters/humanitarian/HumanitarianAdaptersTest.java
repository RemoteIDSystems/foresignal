package ai.foresignal.source.adapters.humanitarian;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 090 adapter edge cases (WireMock): ReliefWeb's appname 403, the GeoNet Accept header, and
 * Copernicus fetching the detail only on change.
 */
@IntegrationTest
class HumanitarianAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

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
    @DisplayName("the Copernicus detail is fetched only when an activation's lastUpdate changes")
    void copernicusDetailOnChange() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/backend/dashboard-api/public-activations-info/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("copernicus-ems", "activations-info.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/backend/dashboard-api/public-activations/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("copernicus-ems", "detail.json"))));
        CopernicusEmsAdapter adapter = new CopernicusEmsAdapter(baseUrl());
        harness.reset(adapter.id(), CopernicusEmsAdapter.CURSOR_KEY);
        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).isNotEmpty();
        assertThat(first.records()).anyMatch(r -> r.payload().get("reason") != null);
        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.records()).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/backend/dashboard-api/public-activations/")));
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
}
