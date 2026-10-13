package ai.foresignal.source.kit.feature;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.kit.KitTestSpecs;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Duration;
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
 * {@link ArcGisQueryClient} through the real upstream client against WireMock (prompt 073): pagination until
 * {@code exceededTransferLimit} is false, the {@code maxPages} cap marking the fetch incomplete, the
 * {@code lastEditDate} short-circuit, metadata reuse for a layer without {@code editingInfo}, and the Esri JSON
 * conversion including a polygon hole.
 */
@IntegrationTest
class ArcGisQueryClientTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T18:00:00Z");
    static final String LAYER = "/arcgis/rest/services/Test/FeatureServer/0";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    KitFeatureAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        adapter = new KitFeatureAdapter(KitTestSpecs.arcgis(url(LAYER)), null);
        harness.reset(adapter.id(), ArcGisQueryClient.META_KEY, ArcGisQueryClient.LAST_EDIT_KEY,
                AbsenceTombstoner.IDS_KEY, AbsenceTombstoner.MISSED_KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    DefaultFetchContext ctx() {
        return harness.context(adapter, Map.of());
    }

    static void layer(String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(LAYER)).withQueryParam("f", equalTo("json"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body)));
    }

    static void page(int offset, byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(LAYER + "/query")).withQueryParam("resultOffset", equalTo(
                String.valueOf(offset))).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json").withBody(body)));
    }

    static byte[] fixture(String name) {
        return AdapterFixtures.bytes("feature-wfigs-incidents", name);
    }

    @Test
    @DisplayName("pages while exceededTransferLimit is true, with the documented query, until a page says no more")
    void pagination() throws Exception {
        layer(AdapterFixtures.text("feature-wfigs-incidents", "layer.json"));
        page(0, fixture("query-page-1.json"));
        page(5, fixture("query-page-2.json"));

        FetchResult result = adapter.fetch(ctx());
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).hasSize(8);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(LAYER + "/query"))
                .withQueryParam("where", equalTo("1=1"))
                .withQueryParam("outFields", equalTo("IrwinID,IncidentName,IncidentSize,ModifiedOnDateTime_dt,"
                        + "FireDiscoveryDateTime"))
                .withQueryParam("returnGeometry", equalTo("true"))
                .withQueryParam("outSR", equalTo("4326"))
                .withQueryParam("f", equalTo("geojson"))
                .withQueryParam("resultOffset", equalTo("0"))
                .withQueryParam("resultRecordCount", equalTo("5"))
                .withQueryParam("orderByFields", equalTo("OBJECTID")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(LAYER + "/query")).withQueryParam("resultOffset",
                equalTo("5")));
    }

    @Test
    @DisplayName("the maxPages cap marks the fetch incomplete and tombstones nothing")
    void maxPages() throws Exception {
        adapter = new KitFeatureAdapter(KitTestSpecs.arcgis(url(LAYER), Map.of("paging",
                Map.of("pageSize", 5, "maxPages", 1))), null);
        layer(AdapterFixtures.text("feature-wfigs-incidents", "layer.json"));
        page(0, fixture("query-page-1.json"));

        FetchResult result = adapter.fetch(ctx());
        assertThat(result.complete()).isFalse();
        assertThat(result.records()).hasSize(5);
        assertThat(ctx().state().get(AbsenceTombstoner.IDS_KEY)).isEmpty();
        assertThat(ctx().state().get(ArcGisQueryClient.LAST_EDIT_KEY)).as("an incomplete fetch is not a baseline")
                .isEmpty();
    }

    @Test
    @DisplayName("an unchanged lastEditDate short-circuits: the layer document is read, no query is sent")
    void lastEditShortCircuit() throws Exception {
        layer(AdapterFixtures.text("feature-wfigs-incidents", "layer.json"));
        page(0, fixture("query-page-1.json"));
        page(5, fixture("query-page-2.json"));
        assertThat(adapter.fetch(ctx()).records()).hasSize(8);
        assertThat(ctx().state().get(ArcGisQueryClient.LAST_EDIT_KEY)).contains("1791913696835");

        harness.clock.advance(Duration.ofMinutes(10));
        FetchResult second = adapter.fetch(ctx());
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(LAYER)));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(LAYER + "/query")));

        // A new edit date queries again.
        WIREMOCK.resetAll();
        layer(AdapterFixtures.text("feature-wfigs-incidents", "layer.json").replace("1791913696835", "1791913999999"));
        page(0, fixture("query-page-1.json"));
        page(5, fixture("query-page-2.json"));
        harness.clock.advance(Duration.ofMinutes(10));
        assertThat(adapter.fetch(ctx()).records()).hasSize(8);
        assertThat(ctx().state().get(ArcGisQueryClient.LAST_EDIT_KEY)).contains("1791913999999");
    }

    @Test
    @DisplayName("a layer without editingInfo: metadata read once per 24 h, the query sent on every run")
    void metadataCache() throws Exception {
        layer("{\"maxRecordCount\": 1000, \"objectIdField\": \"FID\", \"advancedQueryCapabilities\":"
                + " {\"supportsPagination\": true}, \"fields\": [{\"name\": \"FID\", \"type\": \"esriFieldTypeOID\"}]}");
        page(0, fixture("query-page-2.json"));
        assertThat(adapter.fetch(ctx()).records()).hasSize(3);
        harness.clock.advance(Duration.ofHours(1));
        assertThat(adapter.fetch(ctx()).records()).hasSize(3);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(LAYER)));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(LAYER + "/query")).withQueryParam("orderByFields",
                equalTo("FID")));

        harness.clock.advance(Duration.ofHours(24));
        assertThat(adapter.fetch(ctx()).records()).hasSize(3);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(LAYER)));
        assertThat(ArcGisQueryClient.cached(ctx().state()).objectIdField()).isEqualTo("FID");
    }

    @Test
    @DisplayName("a layer without pagination is queried once; exceededTransferLimit then makes it incomplete")
    void noPagination() throws Exception {
        layer("{\"maxRecordCount\": 2, \"advancedQueryCapabilities\": {\"supportsPagination\": false}}");
        WIREMOCK.stubFor(get(urlPathEqualTo(LAYER + "/query")).willReturn(aResponse().withStatus(200)
                .withBody(fixture("query-page-1.json"))));
        FetchResult result = adapter.fetch(ctx());
        assertThat(result.complete()).isFalse();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(LAYER + "/query")).withoutQueryParam("resultOffset"));
    }

    @Test
    @DisplayName("format esrijson: f=json, points, paths and rings converted; a counter-clockwise ring is a hole")
    void esriJson() throws Exception {
        adapter = new KitFeatureAdapter(KitTestSpecs.arcgis(url(LAYER), Map.of("format", "esrijson")), null);
        layer("{\"maxRecordCount\": 100, \"advancedQueryCapabilities\": {\"supportsPagination\": true}}");
        String esri = """
                {"objectIdFieldName": "OBJECTID", "features": [
                  {"attributes": {"IrwinID": "ring", "ModifiedOnDateTime_dt": 1791913696835},
                   "geometry": {"rings": [
                     [[0, 0], [0, 10], [10, 10], [10, 0], [0, 0]],
                     [[2, 2], [8, 2], [8, 8], [2, 8], [2, 2]]]}},
                  {"attributes": {"IrwinID": "point", "ModifiedOnDateTime_dt": 1791913696835},
                   "geometry": {"x": -120.5, "y": 38.25}},
                  {"attributes": {"IrwinID": "path", "ModifiedOnDateTime_dt": 1791913696835},
                   "geometry": {"paths": [[[0, 0], [1, 1]], [[2, 2], [3, 3]]]}},
                  {"attributes": {"IrwinID": "nan", "ModifiedOnDateTime_dt": 1791913696835},
                   "geometry": {"x": "NaN", "y": 1}}
                ]}
                """;
        WIREMOCK.stubFor(get(urlPathEqualTo(LAYER + "/query")).withQueryParam("f", equalTo("json"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(esri)));
        FetchResult result = adapter.fetch(ctx());
        assertThat(result.records()).hasSize(4);
        var ring = result.records().stream().filter(r -> r.externalId().equals("ring")).findFirst().orElseThrow();
        assertThat(ring.geometry().getGeometryType()).isEqualTo("Polygon");
        assertThat(((org.locationtech.jts.geom.Polygon) ring.geometry()).getNumInteriorRing()).isEqualTo(1);
        assertThat(ring.geometry().getArea()).isEqualTo(64.0);
        assertThat(result.records().stream().filter(r -> r.externalId().equals("point")).findFirst().orElseThrow()
                .geometry().getCoordinate().x).isEqualTo(-120.5);
        assertThat(result.records().stream().filter(r -> r.externalId().equals("path")).findFirst().orElseThrow()
                .geometry().getGeometryType()).isEqualTo("MultiLineString");
        assertThat(result.records().stream().filter(r -> r.externalId().equals("nan")).findFirst().orElseThrow()
                .geometry()).isNull();
    }

    @Test
    @DisplayName("an ArcGIS error body, a redirect and a non-JSON page are format errors")
    void errors() {
        layer("{\"error\": {\"code\": 499, \"message\": \"Token Required\"}}");
        assertThatThrownBy(() -> adapter.fetch(ctx())).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("Token Required");
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(LAYER)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", "https://elsewhere.example/")));
        assertThatThrownBy(() -> adapter.fetch(ctx())).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("redirect refused");
        WIREMOCK.resetAll();
        layer("{\"maxRecordCount\": 100}");
        WIREMOCK.stubFor(get(urlPathEqualTo(LAYER + "/query")).willReturn(aResponse().withStatus(200)
                .withBody("<html>maintenance</html>")));
        assertThatThrownBy(() -> adapter.fetch(ctx())).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the converter's ring classification: clockwise outer, counter-clockwise hole")
    void ringOrientation() {
        double[][] clockwise = {{0, 0}, {0, 1}, {1, 1}, {1, 0}, {0, 0}};
        assertThat(EsriJsonConverter.signedArea(clockwise)).isNegative();
        assertThat(EsriJsonConverter.contains(clockwise, 0.5, 0.5)).isTrue();
        assertThat(EsriJsonConverter.contains(clockwise, 1.5, 0.5)).isFalse();
    }
}
