package ai.foresignal.source.kit.feature;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
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
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.net.URI;
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
 * {@link OgcFeaturesClient} through the real upstream client against WireMock (prompt 073): {@code next} links in
 * cursor and offset styles, relative links, accepted and refused content types, and the stop conditions (no
 * {@code next}, an empty page, {@code maxPages}, {@code maxFeatures}, a {@code next} link to another server).
 */
@IntegrationTest
class OgcFeaturesClientTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T18:00:00Z");
    static final String ITEMS = "/ogc/collections/gauges/items";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    KitFeatureAdapter adapter;

    static String spec(String base, String extra) {
        return """
                {
                  "id": "feature-test-ogc",
                  "name": "Test OGC collection",
                  "kind": "gauge",
                  "protocol": "ogc",
                  "url": "%s",
                  "collection": "gauges",
                  "query": {"bbox": [-125, 24, -66, 50], "filters": {"siteType": "ST"}},
                  "idField": "siteId",
                  "fields": {"monitoring_location_id": {"target": "siteId", "type": "string"},
                             "value": {"target": "value", "type": "number"}},
                  "budget": {"perMinute": 60, "perDay": 1000},
                  "paging": {"pageSize": 2, "maxPages": 5%s},
                  "absenceTombstones": true,
                  "license": "Public domain",
                  "attribution": "Test",
                  "homepage": "https://example.org/"
                }
                """.formatted(base, extra);
    }

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        adapter = new KitFeatureAdapter(KitTestSpecs.feature(spec(url("/ogc"), "")), null);
        harness.reset(adapter.id(), AbsenceTombstoner.IDS_KEY, AbsenceTombstoner.MISSED_KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    DefaultFetchContext ctx() {
        return harness.context(adapter, Map.of());
    }

    static String page(String next, String... ids) {
        StringBuilder features = new StringBuilder();
        for (String id : ids) {
            if (!features.isEmpty()) {
                features.append(',');
            }
            features.append("{\"type\": \"Feature\", \"id\": \"").append(id).append("\", \"geometry\": {\"type\":"
                    + " \"Point\", \"coordinates\": [-100, 40]}, \"properties\": {\"monitoring_location_id\": \"")
                    .append(id).append("\", \"value\": 1.5}}");
        }
        String links = next == null ? "[]" : "[{\"rel\": \"next\", \"type\": \"application/geo+json\", \"href\": \""
                + next + "\"}, {\"rel\": \"self\", \"href\": \"x\"}]";
        return "{\"type\": \"FeatureCollection\", \"numberMatched\": 999, \"links\": " + links + ", \"features\": ["
                + features + "]}";
    }

    static void serve(String cursorParam, String cursor, String body, String contentType) {
        var mapping = get(urlPathEqualTo(ITEMS));
        mapping = cursor == null ? mapping.withQueryParam(cursorParam, absent())
                : mapping.withQueryParam(cursorParam, equalTo(cursor));
        WIREMOCK.stubFor(mapping.willReturn(aResponse().withStatus(200).withHeader("Content-Type", contentType)
                .withBody(body)));
    }

    @Test
    @DisplayName("cursor-style next links are followed until a page has none; the first page carries the query")
    void cursorPaging() throws Exception {
        serve("cursor", null, page(url(ITEMS) + "?f=json&limit=2&cursor=abc", "A", "B"), "application/geo+json");
        serve("cursor", "abc", page(url(ITEMS) + "?f=json&limit=2&cursor=def", "C", "D"), "application/geo+json");
        serve("cursor", "def", page(null, "E"), "application/json");

        FetchResult result = adapter.fetch(ctx());
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).extracting(r -> r.externalId()).containsExactly("A", "B", "C", "D", "E");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(ITEMS)).withQueryParam("f", equalTo("json"))
                .withQueryParam("limit", equalTo("2")).withQueryParam("bbox", equalTo("-125,24,-66,50"))
                .withQueryParam("siteType", equalTo("ST"))
                .withHeader("Accept", equalTo(OgcFeaturesClient.ACCEPT)));
    }

    @Test
    @DisplayName("offset-style relative next links resolve against the page; numberMatched is ignored")
    void offsetPaging() throws Exception {
        serve("offset", null, page("items?f=json&limit=2&offset=2", "A", "B"), "application/geo+json");
        serve("offset", "2", page("items?f=json&limit=2&offset=4", "C", "D"), "application/geo+json");
        serve("offset", "4", page("items?f=json&limit=2&offset=6"), "application/geo+json");

        FetchResult result = adapter.fetch(ctx());
        assertThat(result.complete()).as("an empty page ends paging").isTrue();
        assertThat(result.records()).hasSize(4);
    }

    @Test
    @DisplayName("a page that is not GeoJSON or JSON is refused")
    void contentTypes() {
        serve("cursor", null, page(null, "A"), "text/html; charset=utf-8");
        assertThatThrownBy(() -> adapter.fetch(ctx())).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("text/html");
        OgcFeaturesClient.checkType("application/geo+json; charset=utf-8");
        OgcFeaturesClient.checkType("application/vnd.ogc.fg+json");
        OgcFeaturesClient.checkType(null);
    }

    @Test
    @DisplayName("maxPages and maxFeatures cut a fetch short and make it incomplete")
    void caps() throws Exception {
        adapter = new KitFeatureAdapter(KitTestSpecs.feature(spec(url("/ogc"), ", \"maxFeatures\": 3")), null);
        serve("cursor", null, page(url(ITEMS) + "?cursor=abc", "A", "B"), "application/geo+json");
        serve("cursor", "abc", page(url(ITEMS) + "?cursor=def", "C", "D"), "application/geo+json");
        FetchResult capped = adapter.fetch(ctx());
        assertThat(capped.complete()).isFalse();
        assertThat(capped.records()).hasSize(3);

        adapter = new KitFeatureAdapter(KitTestSpecs.feature(spec(url("/ogc"), "").replace("\"maxPages\": 5",
                "\"maxPages\": 1")), null);
        FetchResult onePage = adapter.fetch(ctx());
        assertThat(onePage.complete()).isFalse();
        assertThat(onePage.records()).hasSize(2);
    }

    @Test
    @DisplayName("a next link to another server is refused")
    void offHostNext() {
        serve("cursor", null, page("https://elsewhere.example/ogc/collections/gauges/items?cursor=x", "A"),
                "application/geo+json");
        assertThatThrownBy(() -> adapter.fetch(ctx())).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("leaves the server");
    }

    @Test
    @DisplayName("the next link ignores non-JSON alternates and resolves relative hrefs")
    void nextLink() {
        var root = jsonMapper.readTree("{\"links\": [{\"rel\": \"next\", \"type\": \"text/html\", \"href\": \"h\"},"
                + " {\"rel\": \"next\", \"href\": \"items?page=2\"}]}");
        assertThat(OgcFeaturesClient.next(root, URI.create("https://a.example/c/items?f=json")))
                .isEqualTo(URI.create("https://a.example/c/items?page=2"));
        assertThat(OgcFeaturesClient.next(jsonMapper.readTree("{}"), URI.create("https://a.example/"))).isNull();
    }
}
