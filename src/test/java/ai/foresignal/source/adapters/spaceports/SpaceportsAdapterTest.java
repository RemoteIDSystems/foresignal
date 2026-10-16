package ai.foresignal.source.adapters.spaceports;

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

/** {@code spaceports} against a stubbed GCAT host (prompt 094). */
@IntegrationTest
class SpaceportsAdapterTest extends WireMockSupport {

    private static final String ID = SpaceportsAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private SpaceportsAdapter adapter;
    private final List<NormalizedRecord> collected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, SpaceportsAdapter.SITES_STATE, SpaceportsAdapter.PADS_STATE);
        adapter = new SpaceportsAdapter(baseUrl());
        collected.clear();
    }

    private void serve(String url, String fixture) {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(URI.create(url).getPath()))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain")
                        .withBody(AdapterFixtures.bytes(ID, fixture))));
    }

    @Test
    @DisplayName("sites and pads parse and the run is complete")
    void fetch() throws Exception {
        serve(adapter.sitesUri().toString(), "sites.tsv");
        serve(adapter.padsUri().toString(), "lp.tsv");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(collected).extracting(NormalizedRecord::kind)
                .contains(GcatParser.SITE_KIND, GcatParser.PAD_KIND);
        assertThat(collected).hasSize(5);
    }

    @Test
    @DisplayName("a 304 on the sites table still reconciles the pads (2026-10 review)")
    void sitesUnchanged() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(URI.create(adapter.sitesUri().toString()).getPath()))
                .willReturn(aResponse().withStatus(304)));
        serve(adapter.padsUri().toString(), "lp.tsv");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(result.reconcileKinds()).containsExactly(GcatParser.PAD_KIND);
        assertThat(collected).extracting(NormalizedRecord::kind).containsOnly(GcatParser.PAD_KIND);
    }
}
