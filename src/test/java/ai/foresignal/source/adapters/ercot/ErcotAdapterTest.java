package ai.foresignal.source.adapters.ercot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
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
import java.time.Duration;
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

/**
 * {@code ercot-public} against a stub (prompt 093): the B2C token is fetched once and reused for 55 minutes, a 401
 * re-fetches it, and the report columns map to records by {@code fields[].name}.
 */
@IntegrationTest
class ErcotAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T06:00:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    ErcotAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new ErcotAdapter(baseUrl(), baseUrl() + "/token");
        harness.reset(adapter.id());
        harness.credentials.clear();
        harness.credentials.put(ErcotAdapter.USERNAME, "user");
        harness.credentials.put(ErcotAdapter.PASSWORD, "pass");
        harness.credentials.put(ErcotAdapter.SUBSCRIPTION_KEY, "sub-key");
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
        harness.credentials.clear();
    }

    private void serveToken() {
        WIREMOCK.stubFor(post(urlPathEqualTo("/token")).willReturn(aResponse().withStatus(200)
                .withBody("{\"id_token\":\"id-token-1\"}")));
    }

    private void serveReports() {
        WIREMOCK.stubFor(get(urlPathEqualTo(ErcotAdapter.LOAD_PATH)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("ercot-public", "load.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(ErcotAdapter.SPP_PATH)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("ercot-public", "spp.json"))));
    }

    private Map<String, Object> params() {
        return Map.of(ErcotAdapter.HUBS_PARAM, List.of("HB_HOUSTON"));
    }

    @Test
    @DisplayName("the token is fetched once and reused; the columns map to load and price points")
    void tokenReuse() throws Exception {
        serveToken();
        serveReports();
        FetchResult result = adapter.fetch(harness.context(adapter, params()));
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .contains("ercot:load:coast:2026-10-12T1", "ercot:spp:HB_HOUSTON:2026-10-12T1:1");
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo("/token")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(ErcotAdapter.LOAD_PATH))
                .withHeader("Ocp-Apim-Subscription-Key", equalTo("sub-key"))
                .withHeader("Authorization", equalTo("Bearer id-token-1")));

        adapter.fetch(harness.context(adapter, params()));
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo("/token")));

        harness.clock.advance(Duration.ofMinutes(56));
        adapter.fetch(harness.context(adapter, params()));
        WIREMOCK.verify(2, postRequestedFor(urlPathEqualTo("/token")));
    }

    @Test
    @DisplayName("a 401 re-fetches the token and retries once")
    void unauthorized() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(ErcotAdapter.LOAD_PATH)).inScenario("retry")
                .whenScenarioStateIs(STARTED).willReturn(aResponse().withStatus(401)).willSetStateTo("ok"));
        WIREMOCK.stubFor(get(urlPathEqualTo(ErcotAdapter.LOAD_PATH)).inScenario("retry")
                .whenScenarioStateIs("ok").willReturn(aResponse().withStatus(200)
                        .withBody(AdapterFixtures.bytes("ercot-public", "load.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(ErcotAdapter.SPP_PATH)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("ercot-public", "spp.json"))));
        serveToken();
        adapter.fetch(harness.context(adapter, params()));
        WIREMOCK.verify(2, postRequestedFor(urlPathEqualTo("/token")));
    }
}
