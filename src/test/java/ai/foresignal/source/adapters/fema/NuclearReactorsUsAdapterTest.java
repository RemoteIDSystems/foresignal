package ai.foresignal.source.adapters.fema;

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
import java.time.Duration;
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

/** {@code nuclear-reactors-us} against a stubbed FEMA FeatureServer (prompt 094). */
@IntegrationTest
class NuclearReactorsUsAdapterTest extends WireMockSupport {

    private static final String ID = NuclearReactorsUsAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private NuclearReactorsUsAdapter adapter;
    private final List<NormalizedRecord> collected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, NuclearReactorsUsAdapter.STATE_KEY);
        adapter = new NuclearReactorsUsAdapter(baseUrl());
        collected.clear();
    }

    @Test
    @DisplayName("a 200 parses the reactors, sends outSR=4326, and the descriptor validates")
    void fetch() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(URI.create(adapter.queryUri().toString()).getPath()))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes(ID, "reactors.geojson"))));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(collected).extracting(NormalizedRecord::externalId)
                .containsExactly("reactor:05000266", "reactor:05000528");
        WIREMOCK.verify(1, WireMock.getRequestedFor(
                        urlPathEqualTo(URI.create(adapter.queryUri().toString()).getPath()))
                .withQueryParam("outSR", WireMock.equalTo("4326"))
                .withQueryParam("f", WireMock.equalTo("geojson")));
        assertThat(adapter.descriptor().defaultInterval()).isEqualTo(Duration.ofHours(24));
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
    }
}
