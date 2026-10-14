package ai.foresignal.source.adapters.gvp;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
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

/** {@code gvp-volcanoes} against a stubbed WFS host (prompt 094). */
@IntegrationTest
class GvpVolcanoesAdapterTest extends WireMockSupport {

    private static final String ID = GvpVolcanoesAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");
    private static final String LAST_MODIFIED = "Mon, 13 Oct 2026 00:00:00 GMT";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private GvpVolcanoesAdapter adapter;
    private final List<NormalizedRecord> collected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, GvpVolcanoesAdapter.STATE_KEY);
        adapter = new GvpVolcanoesAdapter(baseUrl());
        collected.clear();
    }

    private void serve() {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(URI.create(adapter.wfsUri().toString()).getPath()))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("Last-Modified", LAST_MODIFIED)
                        .withBody(AdapterFixtures.bytes(ID, "volcanoes.geojson"))));
    }

    @Test
    @DisplayName("a 200 parses volcanoes and reports the Last-Modified release")
    void fetch() throws Exception {
        serve();
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(result.release()).isEqualTo(LAST_MODIFIED);
        assertThat(collected).hasSize(3);
    }

    @Test
    @DisplayName("a 304 is unchanged")
    void notModified() throws Exception {
        serve();
        adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(URI.create(adapter.wfsUri().toString()).getPath()))
                .willReturn(aResponse().withStatus(304)));
        collected.clear();
        assertThat(adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll)).notModified())
                .isTrue();
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
    }
}
