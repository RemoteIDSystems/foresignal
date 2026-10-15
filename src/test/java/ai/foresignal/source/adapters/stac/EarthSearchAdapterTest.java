package ai.foresignal.source.adapters.stac;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code stac-sentinel2} against a stubbed Earth Search (prompt 096). */
@IntegrationTest
class EarthSearchAdapterTest extends WireMockSupport {

    private static final Instant NOON = Instant.parse("2026-10-13T12:00:00Z");

    /**
     * The search path under the test base: the override replaces the production base
     * {@code https://earth-search.aws.element84.com/v1}, so the request is {@code POST /search} on WireMock.
     */
    private static final String PATH = "/search";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private EarthSearchAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOON),
                Budget.of(100, 10_000));
        harness.reset(StacSources.SENTINEL2, EarthSearchAdapter.LAST_ACQUISITION);
        harness.reset(StacSources.LANDSAT, EarthSearchAdapter.LAST_ACQUISITION);
        adapter = new EarthSearchAdapter(StacSources.reviewed(StacSources.SENTINEL2), baseUrl(), () -> null);
    }

    @Test
    @DisplayName("the descriptor validates")
    void descriptorValid() {
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
    }

    @Test
    @DisplayName("the POST body carries the collection, bbox, datetime, query cloud clause and sortby")
    void requestBody() throws Exception {
        serve(AdapterFixtures.text("stac-sentinel2", "search.json"));

        adapter.fetch(context());

        JsonNode body = requestBody(0);
        assertThat(body.get("collections").get(0).asString()).isEqualTo("sentinel-2-c1-l2a");
        assertThat(body.get("bbox").get(0).asDouble()).isEqualTo(-98.0);
        assertThat(body.get("datetime").asString()).endsWith("/..");
        assertThat(body.get("query").get("eo:cloud_cover").get("lt").asDouble()).isEqualTo(30.0);
        assertThat(body.get("sortby").get(0).get("field").asString()).isEqualTo("properties.datetime");
        assertThat(body.get("sortby").get(0).get("direction").asString()).isEqualTo("desc");
        assertThat(body.get("limit").asInt()).isEqualTo(EarthSearchAdapter.PAGE_LIMIT);
        assertThat(body.get("fields").get("include").size()).isEqualTo(EarthSearchAdapter.FIELD_INCLUDE.size());
    }

    @Test
    @DisplayName("the rel=next body token is followed verbatim and capped at maxPages")
    void pagination() throws Exception {
        String page = pageWithNext("TOKEN-123");
        serve(page);

        adapter.fetch(context(Map.of(EarthSearchAdapter.MAX_PAGES_PARAM, 2)));

        assertThat(WIREMOCK.getAllServeEvents()).hasSize(2);
        // getAllServeEvents() is newest-first, so index 0 is the second request (the rel=next body).
        assertThat(requestBody(0).get("next").asString()).isEqualTo("TOKEN-123");
        WIREMOCK.verify(2, postRequestedFor(urlPathEqualTo(PATH)));
    }

    @Test
    @DisplayName("an item older than the window ends the run without a second page")
    void earlyStop() throws Exception {
        // The fixture's older item (2026-10-07) precedes now - lookbackDays (2026-10-10T12:00Z).
        serve(AdapterFixtures.text("stac-sentinel2", "search.json"));
        assertThat(adapter.fetch(context()).records()).hasSize(1);
        assertThat(WIREMOCK.getAllServeEvents()).hasSize(1);
    }

    @Test
    @DisplayName("no request is made outside POST /v1/search")
    void onlySearchEndpoint() throws Exception {
        serve(AdapterFixtures.text("stac-sentinel2", "search.json"));
        adapter.fetch(context());
        assertThat(WIREMOCK.getAllServeEvents()).allSatisfy(event ->
                assertThat(event.getRequest().getUrl()).startsWith(PATH));
    }

    @Test
    @DisplayName("adapter state advances only after a successful run")
    void stateAdvancesOnlyOnSuccess() throws Exception {
        serve(AdapterFixtures.text("stac-sentinel2", "search.json"));
        adapter.fetch(context());
        assertThat(harness.client.stateStore(StacSources.SENTINEL2).get(EarthSearchAdapter.LAST_ACQUISITION))
                // Instant.toString() trims the trailing zero groups of the micros in the fixture.
                .contains("2026-10-10T17:25:25.181Z");

        harness.client.stateStore(StacSources.SENTINEL2).remove(EarthSearchAdapter.LAST_ACQUISITION);
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(post(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(500).withBody("boom")));
        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamHttpException.class);
        assertThat(harness.client.stateStore(StacSources.SENTINEL2).get(EarthSearchAdapter.LAST_ACQUISITION))
                .isEmpty();
    }

    @Test
    @DisplayName("a 429 with Retry-After puts the source in backoff for its duration")
    void throttled() {
        WIREMOCK.stubFor(post(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "120").withBody("Too Many Requests")));
        assertThatThrownBy(() -> adapter.fetch(context()))
                .isInstanceOfSatisfying(UpstreamHttpException.class, e -> assertThat(e.getStatus()).isEqualTo(429));
        assertThat(harness.backoff.isInBackoff(StacSources.SENTINEL2)).isTrue();
    }

    private DefaultFetchContext context() {
        return context(Map.of(EarthSearchAdapter.AOI_PARAM, "-98,30,-97,31"));
    }

    private DefaultFetchContext context(Map<String, Object> extra) {
        Map<String, Object> params = new java.util.LinkedHashMap<>(extra);
        params.putIfAbsent(EarthSearchAdapter.AOI_PARAM, "-98,30,-97,31");
        return harness.context(adapter, params);
    }

    private static void serve(String body) {
        WIREMOCK.stubFor(post(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json").withBody(body)));
    }

    private JsonNode requestBody(int index) throws Exception {
        return jsonMapper.readTree(WIREMOCK.getAllServeEvents().get(index).getRequest().getBodyAsString());
    }

    /** A one-feature page whose {@code rel=next} link carries an opaque token. */
    private static String pageWithNext(String token) {
        return "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"id\":\"P1\","
                + "\"collection\":\"sentinel-2-c1-l2a\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":"
                + "[[[-97.8,30.1],[-97.6,30.1],[-97.6,30.3],[-97.8,30.3],[-97.8,30.1]]]},"
                + "\"properties\":{\"datetime\":\"2026-10-12T00:00:00Z\",\"updated\":\"2026-10-12T01:00:00Z\","
                + "\"eo:cloud_cover\":5.0,\"platform\":\"sentinel-2c\"},"
                + "\"assets\":{\"thumbnail\":{\"href\":\"https://example.org/t.jpg\"}},"
                + "\"links\":[{\"rel\":\"self\",\"href\":\"https://example.org/items/P1\"}]}],"
                + "\"links\":[{\"rel\":\"next\",\"method\":\"POST\",\"href\":\"" + PATH + "\","
                + "\"body\":{\"next\":\"" + token + "\",\"collections\":[\"sentinel-2-c1-l2a\"]}}]}";
    }
}
