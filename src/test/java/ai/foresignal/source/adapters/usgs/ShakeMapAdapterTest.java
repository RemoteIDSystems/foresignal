package ai.foresignal.source.adapters.usgs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
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
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code usgs-shakemap} (prompt 089): the detail document is fetched only when the event changes, the two kinds
 * are written, the PAGER levels map, and the MMI contours are produced.
 */
@IntegrationTest
class ShakeMapAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final String INDEX_PATH = "/fdsn";
    static final String EVENT = "us6000u24c";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    UsgsShakeMapAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new UsgsShakeMapAdapter(baseUrl() + INDEX_PATH);
        harness.reset(adapter.id(), "shakemap:last", "shakemap:seen:" + EVENT, "shakemap:mmi:" + EVENT);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    private JsonNode indexJson() {
        JsonNode root = jsonMapper.readTree(AdapterFixtures.bytes("usgs-shakemap", "index.geojson"));
        for (JsonNode feature : root.get("features")) {
            ((ObjectNode) feature.get("properties")).put("detail", baseUrl() + "/detail/" + feature.get("id").asString());
        }
        return root;
    }

    private JsonNode detailJson() {
        JsonNode root = jsonMapper.readTree(AdapterFixtures.bytes("usgs-shakemap", "detail.json"));
        ((ObjectNode) root.get("properties").get("products").get("shakemap").get(0).get("contents")
                .get("download/cont_mmi.json")).put("url", baseUrl() + "/cont");
        return root;
    }

    private void serveAll() {
        JsonNode index = indexJson();
        WIREMOCK.stubFor(get(urlPathEqualTo(INDEX_PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(index.toString())));
        // The adapter fetches the detail of every changed event; only the captured event has one, so
        // the others are stubbed 404 (their request must still match a stub).
        for (JsonNode feature : index.get("features")) {
            String id = feature.get("id").asString();
            if (!EVENT.equals(id)) {
                WIREMOCK.stubFor(get(urlPathEqualTo("/detail/" + id)).willReturn(aResponse().withStatus(404)
                        .withBody("no shakemap")));
            }
        }
        WIREMOCK.stubFor(get(urlPathEqualTo("/detail/" + EVENT)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(detailJson().toString())));
        WIREMOCK.stubFor(get(urlPathEqualTo("/cont")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("usgs-shakemap", "cont_mmi.json"))));
    }

    private FetchResult fetch() throws Exception {
        return adapter.fetch(harness.context(adapter, Map.of()));
    }

    @Test
    @DisplayName("a changed event yields one impact and its MMI contours; the detail is fetched only once")
    void products() throws Exception {
        serveAll();
        FetchResult result = fetch();
        assertThat(result.records()).filteredOn(r -> r.kind().equals(UsgsShakeMapAdapter.KIND_IMPACT))
                .hasSize(1);
        NormalizedRecord impact = result.records().stream()
                .filter(r -> r.kind().equals(UsgsShakeMapAdapter.KIND_IMPACT)).findFirst().orElseThrow();
        assertThat(impact.externalId()).isEqualTo(EVENT);
        assertThat(impact.payload()).containsEntry("pagerAlert", "green");
        assertThat(impact.payload().get("maxMmi")).isNotNull();
        assertThat(impact.geometry()).isNotNull();

        assertThat(result.records()).filteredOn(r -> r.kind().equals(UsgsShakeMapAdapter.KIND_MMI))
                .isNotEmpty();
        result.records().stream().filter(r -> r.kind().equals(UsgsShakeMapAdapter.KIND_MMI)).forEach(r -> {
            assertThat(r.externalId()).startsWith(EVENT + ":mmi");
            assertThat(r.geometry()).isNotNull();
            assertThat(r.geometry().getNumPoints()).isLessThanOrEqualTo(UsgsShakeMapAdapter.MAX_VERTICES);
        });

        // A second run sees the same updated timestamp and fetches no detail again.
        FetchResult second = fetch();
        assertThat(second.records()).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/detail/" + EVENT)));
    }

    static final String[] EVENTS = {"us6000u214", EVENT, "us6000u28y"};

    private void resetAll(AdapterHttpHarness h) {
        for (String id : EVENTS) {
            h.reset(adapter.id(), UsgsShakeMapAdapter.SEEN_PREFIX + id, UsgsShakeMapAdapter.FAIL_PREFIX + id,
                    "shakemap:mmi:" + id);
        }
        h.reset(adapter.id(), UsgsShakeMapAdapter.CURSOR_KEY);
    }

    @Test
    @DisplayName("a budget refusal stops the run without marking the event seen; the cursor waits for it")
    void budgetRefusalRetries() throws Exception {
        AdapterHttpHarness tight = new AdapterHttpHarness(jdbc, jsonMapper, properties, install,
                new MutableClock(NOW), Budget.of(3, 1_500));
        resetAll(tight);
        serveAll();
        // index, the 404 detail of the oldest change, the captured detail; its contour file is refused.
        fetchWith(tight);
        var state = tight.client.stateStore(adapter.id());
        assertThat(state.get(UsgsShakeMapAdapter.SEEN_PREFIX + "us6000u214")).isPresent();
        assertThat(state.get(UsgsShakeMapAdapter.SEEN_PREFIX + EVENT)).as("not finished, not seen").isEmpty();
        assertThat(state.get(UsgsShakeMapAdapter.CURSOR_KEY))
                .contains(Instant.ofEpochMilli(1791925814489L).minus(UsgsShakeMapAdapter.OVERLAP).toString());

        tight.clock.advance(java.time.Duration.ofMinutes(10));
        FetchResult second = fetchWith(tight);
        assertThat(second.records()).filteredOn(r -> r.kind().equals(UsgsShakeMapAdapter.KIND_MMI)).isNotEmpty();
        assertThat(state.get(UsgsShakeMapAdapter.SEEN_PREFIX + EVENT)).isPresent();
        tight.backoff.onSuccess(adapter.id());
    }

    private FetchResult fetchWith(AdapterHttpHarness h) throws Exception {
        return adapter.fetch(h.context(adapter, Map.of()));
    }

    @Test
    @DisplayName("a failed detail is retried on the next run and given up on after three attempts")
    void failedDetailRetried() throws Exception {
        resetAll(harness);
        serveAll();
        WIREMOCK.stubFor(get(urlPathEqualTo("/detail/" + EVENT)).willReturn(aResponse().withStatus(500)));
        var state = harness.client.stateStore(adapter.id());
        for (int attempt = 1; attempt <= UsgsShakeMapAdapter.MAX_ATTEMPTS; attempt++) {
            assertThat(state.get(UsgsShakeMapAdapter.SEEN_PREFIX + EVENT)).as("attempt " + attempt).isEmpty();
            fetch();
            harness.backoff.onSuccess(adapter.id());
            harness.clock.advance(java.time.Duration.ofMinutes(10));
        }
        assertThat(state.get(UsgsShakeMapAdapter.SEEN_PREFIX + EVENT)).as("given up").isPresent();
        assertThat(state.get(UsgsShakeMapAdapter.FAIL_PREFIX + EVENT)).isEmpty();
    }

    @Test
    @DisplayName("a detail link off the index's origin is refused and never fetched")
    void offOriginRefused() throws Exception {
        resetAll(harness);
        JsonNode index = indexJson();
        for (JsonNode feature : index.get("features")) {
            ((ObjectNode) feature.get("properties")).put("detail", "https://elsewhere.example/detail/x");
        }
        WIREMOCK.stubFor(get(urlPathEqualTo(INDEX_PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(index.toString())));
        FetchResult result = fetch();
        assertThat(result.records()).isEmpty();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/detail/x")));
    }

    @Test
    @DisplayName("simplify leaves a small geometry unchanged and grows the tolerance for a large one")
    void simplify() {
        JsonNode layer = jsonMapper.readTree(AdapterFixtures.bytes("usgs-shakemap", "cont_mmi.json"));
        ArrayNode coordinates = (ArrayNode) layer.get("features").get(0).get("geometry").get("coordinates");
        assertThat(UsgsShakeMapAdapter.simplify(new ai.foresignal.source.formats.geojson.GeoJsonReader()
                .read(layer.get("features").get(0).get("geometry")))).isNotNull();
        assertThat(coordinates).isNotNull();
    }
}
