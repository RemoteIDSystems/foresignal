package ai.foresignal.source.adapters.wikidata;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.UpstreamHttpException;
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

/** {@code nuclear-plants-wikidata} against a stubbed SPARQL endpoint (prompt 094). */
@IntegrationTest
class NuclearPlantsWikidataAdapterTest extends WireMockSupport {

    private static final String ID = NuclearPlantsWikidataAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private NuclearPlantsWikidataAdapter adapter;
    private final List<NormalizedRecord> collected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID);
        adapter = new NuclearPlantsWikidataAdapter(baseUrl());
        collected.clear();
    }

    private String path() {
        return URI.create(adapter.endpoint().toString()).getPath();
    }

    @Test
    @DisplayName("a POST with the Accept header and Foresignal's User-Agent parses the bindings")
    void fetch() throws Exception {
        WIREMOCK.stubFor(WireMock.post(urlPathEqualTo(path())).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/sparql-results+json")
                .withBody(AdapterFixtures.bytes(ID, "sparql.json"))));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(collected).hasSize(2);
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo(path()))
                .withHeader("Accept", containing("sparql-results+json"))
                .withHeader("User-Agent", containing("Foresignal/")));
    }

    @Test
    @DisplayName("a 429 honors Retry-After and enters backoff")
    void throttled() {
        WIREMOCK.stubFor(WireMock.post(urlPathEqualTo(path())).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "30")));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(UpstreamHttpException.class);
        assertThat(harness.backoff.backoffUntil(ID)).isEqualTo(NOW.plus(Duration.ofSeconds(30)));
    }
}
