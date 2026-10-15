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

/** {@code gie-agsi} against a stub (prompt 093): the {@code x-key} header and the HTTP-200 access-denied body. */
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

    @Test
    @DisplayName("the countries parameter keeps the EU aggregate and the chosen countries")
    void countries() throws Exception {
        // The shipped fixture's EU aggregate is status N (no data), which the parser skips by design
        // (AgsiParserTest); give it data here so the "EU is always kept" filter rule is exercised.
        tools.jackson.databind.node.ObjectNode root = (tools.jackson.databind.node.ObjectNode)
                jsonMapper.readTree(AdapterFixtures.bytes("gie-agsi", "storage.json"));
        ((tools.jackson.databind.node.ObjectNode) root.get("data").get(2)).put("status", "C");
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).willReturn(aResponse().withStatus(200)
                .withBody(root.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8))));
        // DE is chosen; FR is not, and the EU row is always kept.
        List<NormalizedRecord> records = adapter.fetch(harness.context(adapter,
                Map.of(AgsiAdapter.COUNTRIES_PARAM, List.of("DE")))).records();
        Map<String, NormalizedRecord> byId = new java.util.HashMap<>();
        records.forEach(r -> byId.put(r.externalId(), r));
        assertThat(byId).containsKey("agsi:DE:2026-10-13").containsKey("agsi:EU:2026-10-13")
                .doesNotContainKey("agsi:FR:2026-10-13");
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
