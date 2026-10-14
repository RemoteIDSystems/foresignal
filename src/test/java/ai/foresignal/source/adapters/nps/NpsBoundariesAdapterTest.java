package ai.foresignal.source.adapters.nps;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
import com.github.tomakehurst.wiremock.client.WireMock;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@code nps-boundaries} against a stubbed ArcGIS FeatureServer (prompt 094). */
@IntegrationTest
class NpsBoundariesAdapterTest extends WireMockSupport {

    private static final String ID = NpsBoundariesAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private NpsBoundariesAdapter adapter;
    private final List<NormalizedRecord> collected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, "arcgis:meta", "arcgis:lastEdit");
        adapter = new NpsBoundariesAdapter(baseUrl());
        collected.clear();
    }

    private void serveLayer() {
        String path = URI.create(adapter.layerUri().toString()).getPath();
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes(ID, "layer.json"))));
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(path + "/query")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes(ID, "boundaries.geojson"))));
    }

    @Test
    @DisplayName("a 200 queries the layer and parses the boundaries")
    void fetch() throws Exception {
        serveLayer();
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(collected).extracting(NormalizedRecord::externalId).containsExactly("YOSE", "GRCA");
    }

    @Test
    @DisplayName("an unchanged editingInfo short-circuits the query on the next run")
    void editingInfoShortCircuit() throws Exception {
        serveLayer();
        adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        collected.clear();
        FetchResult second = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(second.notModified()).isTrue();
        assertThat(collected).isEmpty();
        WIREMOCK.verify(1, WireMock.getRequestedFor(
                urlPathEqualTo(URI.create(adapter.layerUri().toString()).getPath() + "/query")));
    }
}
