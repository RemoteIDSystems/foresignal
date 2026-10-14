package ai.foresignal.source.adapters.sanctions;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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

/**
 * The OFAC cheap change check (prompt 091): an unchanged publication path is not downloaded, a new one is,
 * and the pre-signed query is never part of the stored path.
 */
@IntegrationTest
class OfacChangeCheckTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final String EXPORT = "/api/PublicationPreview/exports/SDN.XML";
    static final String OBJECT = "/Published/abc/2026-10-14/abc/SDN.XML";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    OfacSdnAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new OfacSdnAdapter(baseUrl());
        harness.reset(adapter.id(), "ofac:path:SDN", "ofac:ids:SDN");
        WIREMOCK.stubFor(get(urlPathEqualTo(EXPORT)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", baseUrl() + OBJECT + "?X-Amz-Signature=secret")));
        WIREMOCK.stubFor(get(urlPathEqualTo(OBJECT)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml").withBody(AdapterFixtures.bytes("ofac-sdn", "sdn.xml"))));
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    @Test
    @DisplayName("a new publication downloads once; the same publication is not downloaded again")
    void changeCheck() throws Exception {
        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).isNotEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(OBJECT)));

        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.records()).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(OBJECT)));
    }
}
