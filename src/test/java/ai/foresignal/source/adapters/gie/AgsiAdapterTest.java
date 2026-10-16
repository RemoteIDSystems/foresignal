package ai.foresignal.source.adapters.gie;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
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
 * {@code gie-agsi} against a stub (prompt 093): the {@code x-key} header, the per-country requests, the budget and the
 * HTTP-200 access-denied body.
 */
@IntegrationTest
class AgsiAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T06:00:00Z");
    static final String KEY = "agsi-test-key-0123456789";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    AgsiAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new AgsiAdapter(baseUrl());
        harness.reset(adapter.id());
        harness.credentials.clear();
        harness.credentials.put(AgsiAdapter.API_KEY, KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
        harness.credentials.clear();
    }

    @Test
    @DisplayName("the key is sent as x-key and the storage rows become points and latest records")
    void storage() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("gie-agsi", "storage.json"))));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .contains("agsi:DE:2026-10-13", "agsi:DE:latest", "agsi:FR:2026-10-13");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/api")).withQueryParam("type", equalTo("eu"))
                .withHeader("x-key", equalTo(KEY)));
    }

    private static String body(String code, String name, String full) {
        return ("{'last_page':1,'total':1,'dataset':'storage','data':[{'name':'" + name + "','code':'" + code
                + "','gasDayStart':'2026-10-13','gasInStorage':'1','full':'" + full + "','status':'C'}]}")
                .replace('\'', '"');
    }

    @Test
    @DisplayName("one type=eu request for the aggregate and one country= request per configured country")
    void countries() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).withQueryParam("type", equalTo("eu"))
                .willReturn(aResponse().withStatus(200).withBody(body("EU", "Europe", "90.1"))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).withQueryParam("country", equalTo("DE"))
                .willReturn(aResponse().withStatus(200).withBody(body("DE", "Germany", "92.3"))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).withQueryParam("country", equalTo("PL"))
                .willReturn(aResponse().withStatus(200).withBody(body("PL", "Poland", "88.0"))));
        List<NormalizedRecord> records = adapter.fetch(harness.context(adapter,
                Map.of(AgsiAdapter.COUNTRIES_PARAM, List.of("de", "PL", "EU")))).records();
        Map<String, NormalizedRecord> byId = new java.util.HashMap<>();
        records.forEach(r -> byId.put(r.externalId(), r));
        assertThat(byId).containsKey("agsi:DE:2026-10-13").containsKey("agsi:PL:2026-10-13")
                .containsKey("agsi:EU:2026-10-13").containsKey("agsi:PL:latest");
        WIREMOCK.verify(3, getRequestedFor(urlPathEqualTo("/api")).withHeader("x-key", equalTo(KEY)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api")).withQueryParam("country", equalTo("DE"))
                .withQueryParam("size", equalTo("30")));
    }

    @Test
    @DisplayName("the default countries fit the default per-minute budget in one run")
    void defaultBudget() {
        assertThat(AgsiAdapter.DEFAULT_REQUESTS_PER_RUN).isEqualTo(1 + AgsiAdapter.DEFAULT_COUNTRIES.size());
        assertThat(adapter.descriptor().defaultBudget().perMinute()).isGreaterThanOrEqualTo(
                AgsiAdapter.DEFAULT_REQUESTS_PER_RUN);
        long runsPerDay = Duration.ofDays(1).toMinutes() / adapter.descriptor().defaultInterval().toMinutes();
        assertThat(adapter.descriptor().defaultBudget().perDay()).isGreaterThanOrEqualTo(
                (int) runsPerDay * AgsiAdapter.DEFAULT_REQUESTS_PER_RUN);
    }

    @Test
    @DisplayName("the access-denied body is a rejected credential")
    void accessDenied() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("gie-agsi", "access-denied.json"))));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(CredentialsRejectedException.class);
    }
}
