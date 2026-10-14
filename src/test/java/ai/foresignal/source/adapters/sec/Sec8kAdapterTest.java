package ai.foresignal.source.adapters.sec;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
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

/** The SEC 8-K contact User-Agent and paging (prompt 091). */
@IntegrationTest
class Sec8kAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final String PATH = "/cgi-bin/browse-edgar";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(Sec8kAdapter.ID);
    }

    @Test
    @DisplayName("the User-Agent carries the Foresignal identifier and the contact suffix")
    void userAgent() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/atom+xml")
                .withBody(AdapterFixtures.bytes("sec-8k", "current-8k.xml"))));
        Sec8kAdapter adapter = new Sec8kAdapter(baseUrl(), "ops@example.org");
        harness.reset(adapter.id());
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).isNotEmpty();
        assertThat(result.complete()).isFalse();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH))
                .withHeader("User-Agent", containing("Foresignal/"))
                .withHeader("User-Agent", containing("contact=ops@example.org")));
    }
}
