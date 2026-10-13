package ai.foresignal.source.kit.feature;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.kit.KitTestSpecs;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link WfsJsonClient} through the real upstream client against WireMock (prompt 073): {@code startIndex} paging
 * while {@code numberReturned == count}, the GetFeature parameters, the {@code axisOrder} setting, and the swap with
 * a single warning when the first points fit the spec's bbox only with the axes swapped.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class WfsJsonClientTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T18:00:00Z");
    static final String WFS = "/geoserver/wfs";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;

    static String spec(String url, String axisOrder, boolean bbox) {
        return """
                {
                  "id": "feature-test-wfs",
                  "name": "Test WFS layer",
                  "kind": "fire-perimeter",
                  "protocol": "wfs",
                  "url": "%s",
                  "typeName": "public:perimeters",
                  %s
                  "idField": "fireId",
                  "fields": {"fire_id": {"target": "fireId", "type": "string"}},
                  "budget": {"perMinute": 60, "perDay": 1000},
                  "paging": {"pageSize": 2, "maxPages": 5},
                  "geometry": {"axisOrder": "%s"},
                  "license": "Test license",
                  "attribution": "Test",
                  "homepage": "https://example.org/"
                }
                """.formatted(url, bbox ? "\"query\": {\"bbox\": [-141, 41, -52, 84]}," : "", axisOrder);
    }

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset("feature-test-wfs", AbsenceTombstoner.IDS_KEY, AbsenceTombstoner.MISSED_KEY);
        WfsJsonClient.resetWarnings();
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess("feature-test-wfs");
    }

    DefaultFetchContext ctx(KitFeatureAdapter adapter) {
        return harness.context(adapter, Map.of());
    }

    /** A page of points given as [first, second] coordinate pairs. */
    static String page(int numberReturned, double[]... points) {
        StringBuilder features = new StringBuilder();
        int i = 0;
        for (double[] p : points) {
            if (!features.isEmpty()) {
                features.append(',');
            }
            features.append("{\"type\": \"Feature\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [")
                    .append(p[0]).append(',').append(p[1]).append("]}, \"properties\": {\"fire_id\": \"F")
                    .append(p[0]).append('_').append(i++).append("\"}}");
        }
        return "{\"type\": \"FeatureCollection\", \"numberReturned\": " + numberReturned
                + ", \"crs\": {\"type\": \"name\", \"properties\": {\"name\": \"urn:ogc:def:crs:EPSG::4326\"}},"
                + " \"features\": [" + features + "]}";
    }

    static void serve(int startIndex, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(WFS)).withQueryParam("startIndex", equalTo(String.valueOf(startIndex)))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body)));
    }

    @Test
    @DisplayName("pages by startIndex while numberReturned equals count, with the GetFeature parameters")
    void paging() throws Exception {
        KitFeatureAdapter adapter = new KitFeatureAdapter(KitTestSpecs.feature(spec(url(WFS), "lonlat", false)),
                null);
        serve(0, page(2, new double[] {-120, 50}, new double[] {-119, 51}));
        serve(2, page(2, new double[] {-118, 52}, new double[] {-117, 53}));
        serve(4, page(1, new double[] {-116, 54}));

        FetchResult result = adapter.fetch(ctx(adapter));
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).hasSize(5);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(WFS)).withQueryParam("service", equalTo("WFS"))
                .withQueryParam("version", equalTo("2.0.0")).withQueryParam("request", equalTo("GetFeature"))
                .withQueryParam("typeNames", equalTo("public:perimeters"))
                .withQueryParam("outputFormat", equalTo("application/json"))
                .withQueryParam("count", equalTo("2"))
                .withQueryParam("srsName", equalTo("urn:ogc:def:crs:EPSG::4326")));
        WIREMOCK.verify(3, getRequestedFor(urlPathEqualTo(WFS)));
        assertThat(result.records().get(0).geometry().getCoordinate().x).isEqualTo(-120.0);
    }

    @Test
    @DisplayName("axisOrder latlon swaps every coordinate")
    void latLon() throws Exception {
        KitFeatureAdapter adapter = new KitFeatureAdapter(KitTestSpecs.feature(spec(url(WFS), "latlon", false)),
                null);
        serve(0, page(1, new double[] {50, -120}));
        FetchResult result = adapter.fetch(ctx(adapter));
        assertThat(result.records().get(0).geometry().getCoordinate().x).isEqualTo(-120.0);
        assertThat(result.records().get(0).geometry().getCoordinate().y).isEqualTo(50.0);
    }

    @Test
    @DisplayName("points that fit the bbox only when swapped are swapped, with one warning per source")
    void swapWarning(CapturedOutput output) throws Exception {
        KitFeatureAdapter adapter = new KitFeatureAdapter(KitTestSpecs.feature(spec(url(WFS), "lonlat", true)),
                null);
        // Latitude first although the spec says lonlat: (50, -120) is outside [-141, 41, -52, 84] as read.
        serve(0, page(2, new double[] {50, -120}, new double[] {51, -119}));
        serve(2, page(0));
        FetchResult result = adapter.fetch(ctx(adapter));
        assertThat(result.records()).hasSize(2);
        assertThat(result.records().get(0).geometry().getCoordinate().x).isEqualTo(-120.0);
        assertThat(result.records().get(1).geometry().getCoordinate().y).isEqualTo(51.0);
        adapter.fetch(ctx(adapter));
        assertThat(output.getAll().split("fit the spec bbox only with the axes swapped", -1)).hasSize(2);
    }

    @Test
    @DisplayName("the sanity check: no swap when the points fit, or when they fit neither way")
    void sanityCheck() {
        List<Double> bbox = List.of(-141.0, 41.0, -52.0, 84.0);
        var fits = jsonMapper.readTree(page(1, new double[] {-120, 50})).get("features");
        var swapped = jsonMapper.readTree(page(1, new double[] {50, -120})).get("features");
        var neither = jsonMapper.readTree(page(1, new double[] {10, 10})).get("features");
        assertThat(WfsJsonClient.onlyFitsSwapped(fits, bbox, false)).isFalse();
        assertThat(WfsJsonClient.onlyFitsSwapped(swapped, bbox, false)).isTrue();
        assertThat(WfsJsonClient.onlyFitsSwapped(neither, bbox, false)).isFalse();
        assertThat(WfsJsonClient.onlyFitsSwapped(fits, bbox, true)).as("read as latlon, they fit only swapped")
                .isTrue();
    }
}
