package ai.foresignal.source.adapters.ebird;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.PlaceTimeZones;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@code ebird-notable} against a stubbed eBird (prompt 095). */
@IntegrationTest
class EbirdNotableAdapterTest extends WireMockSupport {

    private static final Instant NOON = Instant.parse("2026-10-13T12:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private EbirdNotableAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOON),
                Budget.of(100, 10_000));
        harness.reset(EbirdNotableAdapter.ID);
        harness.credentials.put(EbirdNotableAdapter.API_KEY, "test-key");
        adapter = new EbirdNotableAdapter(baseUrl(), () -> null, PlaceTimeZones.none());
    }

    @Test
    @DisplayName("the descriptor validates and is local-only")
    void descriptorValid() {
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().egress().name()).isEqualTo("LOCAL_ONLY");
    }

    @Test
    @DisplayName("the key is sent in the x-ebirdapitoken header and the notable path is used")
    void headerSent() throws Exception {
        serve(EbirdNotableAdapter.NOTABLE_PATH, 200, AdapterFixtures.text(EbirdNotableAdapter.ID, "notable.json"));
        adapter.fetch(harness.context(adapter, Map.of("lat", 30.27, "lng", -97.74)));
        WIREMOCK.verify(com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(
                        urlPathEqualTo(EbirdNotableAdapter.NOTABLE_PATH))
                .withHeader(EbirdNotableAdapter.TOKEN_HEADER,
                        com.github.tomakehurst.wiremock.client.WireMock.equalTo("test-key")));
    }

    @Test
    @DisplayName("a 403 is an upstream error")
    void forbidden() {
        serve(EbirdNotableAdapter.NOTABLE_PATH, 403, "forbidden");
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of("lat", 30.27, "lng", -97.74))))
                .isInstanceOf(AdapterException.class);
    }

    @Test
    @DisplayName("includeRecent also reads the recent feed")
    void includeRecent() throws Exception {
        serve(EbirdNotableAdapter.NOTABLE_PATH, 200, AdapterFixtures.text(EbirdNotableAdapter.ID, "notable.json"));
        serve(EbirdNotableAdapter.RECENT_PATH, 200, "[]");
        adapter.fetch(harness.context(adapter, Map.of("lat", 30.27, "lng", -97.74, "includeRecent", true)));
        assertThat(WIREMOCK.getAllServeEvents()).hasSize(2);
    }

    private static void serve(String path, int status, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }
}
