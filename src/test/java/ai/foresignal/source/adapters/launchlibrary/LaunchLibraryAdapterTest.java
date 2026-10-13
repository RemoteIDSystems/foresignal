package ai.foresignal.source.adapters.launchlibrary;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/** {@code launch-library} against a stubbed Launch Library 2 (prompt 064). */
@IntegrationTest
class LaunchLibraryAdapterTest extends WireMockSupport {

    private static final String ID = LaunchLibraryAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-13T13:00:00Z");
    private static final String PATH = "/2.3.0/launches/";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private LaunchLibraryAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW), Budget.of(100, 1000));
        harness.reset(ID);
        adapter = new LaunchLibraryAdapter(baseUrl());
    }

    /** The captured page with {@code next} pointing at offset {@code nextOffset} on the stub, or null. */
    private byte[] page(Integer nextOffset) {
        ObjectNode root = (ObjectNode) jsonMapper.readTree(AdapterFixtures.bytes(ID, "launches-page1.json"));
        if (nextOffset == null) {
            root.putNull("next");
        } else {
            root.put("next", baseUrl() + PATH + "?limit=100&mode=normal&offset=" + nextOffset + "&ordering=net");
        }
        return jsonMapper.writeValueAsBytes(root);
    }

    private void servePages() {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).atPriority(5)
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(page(3))));
        for (int offset = 3; offset <= 12; offset += 3) {
            WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).withQueryParam("offset", equalTo(Integer.toString(offset)))
                    .atPriority(1).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody(page(offset + 3))));
        }
    }

    @Test
    @DisplayName("the window is now−7d to now+60d, and pagination stops after three pages")
    void paginationCapped() throws Exception {
        servePages();
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(3, getRequestedFor(urlPathEqualTo(PATH)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH))
                .withQueryParam("net__gte", equalTo(NOW.minus(Duration.ofDays(7)).toString()))
                .withQueryParam("net__lte", equalTo(NOW.plus(Duration.ofDays(60)).toString()))
                .withQueryParam("limit", equalTo("100")).withQueryParam("mode", equalTo("normal")));
        assertThat(result.complete()).isFalse();
        assertThat(result.records()).hasSize(9);
    }

    @Test
    @DisplayName("a last page without next makes a complete result")
    void complete() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(page(null))));
        assertThat(adapter.fetch(harness.context(adapter, Map.of())).complete()).isTrue();
    }

    @Test
    @DisplayName("the Token header is sent only with a credential")
    void tokenHeader() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(page(null))));
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withHeader("Authorization", absent()));
        harness.credentials.put(LaunchLibraryAdapter.TOKEN, "abc123");
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withHeader("Authorization", equalTo("Token abc123")));
    }

    @Test
    @DisplayName("a 429 without Retry-After backs off 30 minutes; with it, honors it")
    void throttled() {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(429)));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of()))).isInstanceOf(UpstreamHttpException.class);
        assertThat(harness.backoff.backoffUntil(ID)).isEqualTo(NOW.plus(Duration.ofMinutes(30)));

        harness.reset(ID);
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(429).withHeader("Retry-After", "120")));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of()))).isInstanceOf(UpstreamHttpException.class);
        assertThat(harness.backoff.backoffUntil(ID)).isEqualTo(NOW.plus(Duration.ofSeconds(120)));
    }

    @Test
    @DisplayName("records: id, net, last_updated, pad geometry from numbers or strings, the payload keys")
    void records() {
        LaunchLibraryParser.Page page = LaunchLibraryParser.parse(page(null));
        assertThat(page.records()).hasSize(3);
        NormalizedRecord nuri = page.records().get(0);
        assertThat(nuri.externalId()).isEqualTo("e0741415-6c63-4236-9736-24a6c04485bc");
        assertThat(nuri.eventTime()).isEqualTo(Instant.parse("2026-10-07T03:25:00Z"));
        assertThat(nuri.observedAt()).isEqualTo(Instant.parse("2026-10-09T01:02:13Z"));
        assertThat(((Point) nuri.geometry()).getX()).isEqualTo(127.535069);
        assertThat(((Point) nuri.geometry()).getY()).isEqualTo(34.431867);
        assertThat(nuri.payload()).containsEntry("name", "Nuri | NeonSat-2 to 6").containsEntry("status", "Success")
                .containsEntry("netPrecision", "Second").containsEntry("provider", "Korea Aerospace Research Institute")
                .containsEntry("rocket", "KSLV-2 Nuri").containsEntry("missionName", "NeonSat-2 to 6")
                .containsEntry("missionType", "Earth Science").containsEntry("orbit", "Sun-Synchronous Orbit")
                .containsEntry("padName", "LC-2").containsEntry("location", "Naro Space Center, South Korea")
                .containsEntry("webcastLive", false).containsKey("missionDescription").doesNotContainKey("probability");
        assertThat(page.records().get(2).payload()).containsEntry("probability", 80);

        ObjectNode root = (ObjectNode) jsonMapper.readTree(page(null));
        ObjectNode pad = (ObjectNode) root.get("results").get(0).get("pad");
        pad.put("latitude", "28.56194122");
        pad.put("longitude", "-80.57735736");
        NormalizedRecord strings = LaunchLibraryParser.parse(jsonMapper.writeValueAsBytes(root)).records().get(0);
        assertThat(((Point) strings.geometry()).getY()).isEqualTo(28.56194122);
        assertThat(((Point) strings.geometry()).getX()).isEqualTo(-80.57735736);
    }

    @Test
    @DisplayName("next pointing at another host is not followed; the descriptor validates")
    void sameHost() {
        assertThat(adapter.sameHost("https://evil.example/2.3.0/launches/?offset=3")).isNull();
        assertThat(adapter.sameHost(baseUrl() + PATH + "?offset=3")).isNotNull();
        assertThat(ai.foresignal.source.DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().embedFields()).containsExactly("missionDescription");
        assertThat(adapter.descriptor().defaultInterval()).isEqualTo(Duration.ofMinutes(15));
    }
}
