package ai.foresignal.source.adapters.elexon;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
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
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** The GB and Fingrid grid-frequency sources against a stub (prompt 093): no key, an {@code x-api-key} header and backoff. */
@IntegrationTest
class GridFrequencyAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T00:01:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    GridFrequencyGbAdapter gb;
    ai.foresignal.source.adapters.fingrid.GridFrequencyFingridAdapter fingrid;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        gb = new GridFrequencyGbAdapter(baseUrl());
        fingrid = new ai.foresignal.source.adapters.fingrid.GridFrequencyFingridAdapter(baseUrl());
        harness.reset(gb.id(), "freq:gb");
        harness.reset(fingrid.id(), "freq:fi");
        harness.credentials.clear();
        harness.credentials.put(ai.foresignal.source.adapters.fingrid.GridFrequencyFingridAdapter.API_KEY,
                "fingrid-test-key");
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(gb.id());
        harness.backoff.onSuccess(fingrid.id());
        harness.credentials.clear();
    }

    @Test
    @DisplayName("elexon: an in-band window emits only the state record, read without a key")
    void elexon() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/bmrs/api/v1/system/frequency")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("grid-frequency-gb", "frequency.json"))));
        FetchResult result = gb.fetch(harness.context(gb, Map.of()));
        List<NormalizedRecord> states = result.records().stream()
                .filter(r -> r.kind().equals("grid-frequency")).toList();
        assertThat(states).hasSize(1);
        assertThat(states.get(0).payload()).containsEntry("state", "normal");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/bmrs/api/v1/system/frequency")));
    }

    @Test
    @DisplayName("fingrid: the key is sent as x-api-key and the dip below 0.1 Hz is an excursion")
    void fingrid() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/datasets/177/data")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("grid-frequency-fingrid", "frequency.json"))));
        FetchResult result = fingrid.fetch(harness.context(fingrid, Map.of()));
        assertThat(result.records()).extracting(NormalizedRecord::kind).contains("frequency-excursion",
                "grid-frequency");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/api/datasets/177/data"))
                .withHeader("x-api-key", equalTo("fingrid-test-key")));
    }

    @Test
    @DisplayName("a 429 puts the source in backoff and nothing is sent while it lasts")
    void retryAfter() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/bmrs/api/v1/system/frequency")).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "120")));
        try {
            gb.fetch(harness.context(gb, Map.of()));
        } catch (Exception expected) {
            // a backoff is the expected outcome
        }
        assertThat(harness.backoff.isInBackoff(gb.id())).isTrue();
        WIREMOCK.resetRequests();
        try {
            gb.fetch(harness.context(gb, Map.of()));
        } catch (Exception expected) {
            // refused while backing off
        }
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }
}
