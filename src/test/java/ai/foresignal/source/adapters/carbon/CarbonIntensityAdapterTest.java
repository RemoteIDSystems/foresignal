package ai.foresignal.source.adapters.carbon;

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
import tools.jackson.databind.json.JsonMapper;

/** {@code gb-carbon-intensity} against a stub (prompt 093): three endpoints plus the hourly pt24h re-read, no key. */
@IntegrationTest
class CarbonIntensityAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T06:00:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    CarbonIntensityAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new CarbonIntensityAdapter(baseUrl());
        harness.reset(adapter.id(), CarbonIntensityAdapter.PT24H_STATE_KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    private void serve(String path, String fixture) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("gb-carbon-intensity", fixture))));
    }

    @Test
    @DisplayName("national, regional and generation points, plus one pt24h re-read")
    void fetch() throws Exception {
        serve("/intensity", "intensity.json");
        serve("/regional", "regional.json");
        serve("/generation", "generation.json");
        serve("/intensity/pt24h", "intensity.json");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .contains("carbon:gb:national:2026-10-14T20:00:00Z", "carbon:gb:region:1:2026-10-14T20:00:00Z",
                        "genmix:gb:2026-10-14T20:00:00Z");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/intensity/pt24h")));

        // The hour is remembered, so the next poll in the same hour skips pt24h.
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/intensity/pt24h")));
    }
}
