package ai.foresignal.source.adapters.submarinecables;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
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

/** {@code submarine-cables} against a stubbed map host (prompt 094). */
@IntegrationTest
class SubmarineCablesAdapterTest extends WireMockSupport {

    private static final String ID = SubmarineCablesAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private SubmarineCablesAdapter adapter;
    private final List<NormalizedRecord> collected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, SubmarineCablesAdapter.CABLE_STATE, SubmarineCablesAdapter.LANDING_STATE);
        adapter = new SubmarineCablesAdapter(baseUrl());
        collected.clear();
    }

    private void serve(String url, String fixture) {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(URI.create(url).getPath()))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes(ID, fixture))));
    }

    @Test
    @DisplayName("both files parse and the run is complete")
    void fetchFiles() throws Exception {
        serve(adapter.cableUri().toString(), "cable-geo.json");
        serve(adapter.landingUri().toString(), "landing-point-geo.json");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(collected).hasSize(4);
    }

    @Test
    @DisplayName("a 304 on either file makes the run incomplete so reconciliation is skipped")
    void partial() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(URI.create(adapter.cableUri().toString()).getPath()))
                .willReturn(aResponse().withStatus(304)));
        serve(adapter.landingUri().toString(), "landing-point-geo.json");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isFalse();
        assertThat(adapter.descriptor().egress()).isEqualTo(Egress.LOCAL_ONLY);
    }
}
