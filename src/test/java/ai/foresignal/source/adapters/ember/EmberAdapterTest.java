package ai.foresignal.source.adapters.ember;

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
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@code ember-electricity} against a stub (prompt 093): the options short-circuit when the newest month is unchanged. */
@IntegrationTest
class EmberAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T06:00:00Z");
    static final String KEY = "ember-test-key-0123456789";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    EmberAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new EmberAdapter(baseUrl());
        harness.reset(adapter.id(), EmberAdapter.MONTH_STATE_PREFIX + EmberParser.GENERATION);
        harness.credentials.clear();
        harness.credentials.put(EmberAdapter.API_KEY, KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
        harness.credentials.clear();
    }

    private void serveOptions() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/v1/options/electricity-generation/monthly/date"))
                .willReturn(aResponse().withStatus(200)
                        .withBody(AdapterFixtures.bytes("ember-electricity", "options.json"))));
    }

    private void serveData() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/v1/electricity-generation/monthly"))
                .willReturn(aResponse().withStatus(200)
                        .withBody(AdapterFixtures.bytes("ember-electricity", "generation.json"))));
    }

    @Test
    @DisplayName("the first fetch reads the data; an unchanged options month short-circuits the next")
    void optionsShortCircuit() throws Exception {
        serveOptions();
        serveData();
        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).extracting(NormalizedRecord::externalId).contains("ember:USA:Wind:2026-08",
                "ember:USA:Wind:latest");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/v1/electricity-generation/monthly")));

        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/v1/electricity-generation/monthly")));
    }

    @Test
    @DisplayName("the dataset parameter selects carbon intensity and demand too")
    void datasets() throws Exception {
        serveOptions();
        serveData();
        FetchResult result = adapter.fetch(harness.context(adapter,
                Map.of(EmberAdapter.DATASETS_PARAM, List.of(EmberParser.GENERATION))));
        assertThat(result.records()).isNotEmpty();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/v1/electricity-generation/monthly"))
                .withQueryParam("api_key", com.github.tomakehurst.wiremock.client.WireMock.equalTo(KEY)));
    }
}
