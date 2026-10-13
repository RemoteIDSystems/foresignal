package ai.foresignal.source.adapters.radio;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Radio Browser mirror discovery and the crawl through the real upstream client against WireMock (prompt 075): the
 * host-pattern SSRF guard, the shuffle and the 6 h cache, failover to the next mirror within one fetch, the fallback
 * list when discovery fails, and the crawl cap.
 */
@IntegrationTest
class RadioMirrorDiscoveryTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T19:30:00Z");
    static final String DISCOVERY = "/all.api.radio-browser.info/json/servers";
    static final long SEED = 7;

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    RadioBrowserAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        adapter = new RadioBrowserAdapter(baseUrl(), new Random(SEED), () -> null);
        harness.reset(adapter.id(), RadioMirrors.MIRRORS_KEY, RadioMirrors.MIRRORS_AT_KEY, AbsenceTombstoner.IDS_KEY,
                AbsenceTombstoner.MISSED_KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    DefaultFetchContext ctx(Map<String, Object> params) {
        return harness.context(adapter, params);
    }

    static void discovery(String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(DISCOVERY)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }

    static void stations(String mirror, int status, byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + mirror + "/json/stations/search")).willReturn(aResponse()
                .withStatus(status).withHeader("Content-Type", "application/json").withBody(body)));
    }

    static byte[] page() {
        return AdapterFixtures.bytes("radio-browser", "stations-page.json");
    }

    /** The order {@link RadioMirrors} produces for {@code names} with the test seed. */
    static List<String> shuffled(List<String> names) {
        List<String> out = new ArrayList<>(names);
        Collections.shuffle(out, new Random(SEED));
        return out;
    }

    @Test
    @DisplayName("the descriptor validates: 6 h, no TTL, online volatile, maxStations 40,000")
    void descriptor() {
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().defaultInterval()).isEqualTo(Duration.ofHours(6));
        assertThat(adapter.descriptor().minInterval()).isEqualTo(Duration.ofHours(6));
        assertThat(adapter.descriptor().recordTtl()).isNull();
        assertThat(adapter.descriptor().volatileFields()).containsExactly("online");
        assertThat(adapter.descriptor().param(RadioBrowserAdapter.MAX_STATIONS_PARAM).defaultValue())
                .isEqualTo(RadioBrowserAdapter.DEFAULT_MAX);
    }

    @Test
    @DisplayName("host pattern: only *.api.radio-browser.info names survive; case and a trailing dot are normalized")
    void hostPattern() {
        String body = """
                [{"ip": "1.2.3.4", "name": "evil.example.com"},
                 {"ip": "1.2.3.4", "name": "de1.api.radio-browser.info."},
                 {"ip": "::1", "name": "DE2.API.RADIO-BROWSER.INFO"},
                 {"ip": "1.2.3.4", "name": "x.api.radio-browser.info.evil.com"},
                 {"ip": "1.2.3.4", "name": "127.0.0.1"},
                 {"ip": "1.2.3.4", "name": "a.b.api.radio-browser.info"},
                 {"ip": "1.2.3.4", "name": "de1.api.radio-browser.info"},
                 {"ip": "1.2.3.4"}, 7]""";
        assertThat(RadioMirrors.parse(body.getBytes(StandardCharsets.UTF_8)))
                .containsExactly("de1.api.radio-browser.info", "de2.api.radio-browser.info");
        assertThat(RadioMirrors.parse(AdapterFixtures.bytes("radio-browser", "servers.json")))
                .containsExactly("de1.api.radio-browser.info");
        assertThat(RadioMirrors.parse("{\"servers\": []}".getBytes(StandardCharsets.UTF_8))).isEmpty();
        assertThat(RadioMirrors.accepted("api.radio-browser.info")).isNull();
    }

    @Test
    @DisplayName("discovery is shuffled and cached for 6 h in the adapter state, then read again")
    void shuffleAndCache() throws Exception {
        List<String> names = List.of("de1.api.radio-browser.info", "de2.api.radio-browser.info",
                "nl1.api.radio-browser.info", "at1.api.radio-browser.info");
        discovery("[" + String.join(",", names.stream().map(n -> "{\"ip\": \"1.1.1.1\", \"name\": \"" + n + "\"}")
                .toList()) + "]");
        RadioMirrors mirrors = new RadioMirrors(java.net.URI.create(url(DISCOVERY)), new Random(SEED));
        List<String> first = mirrors.resolve(ctx(Map.of()));
        assertThat(first).containsExactlyElementsOf(shuffled(names));
        assertThat(ctx(Map.of()).state().get(RadioMirrors.MIRRORS_KEY)).contains(String.join(",", first));

        harness.clock.advance(Duration.ofHours(5));
        assertThat(mirrors.resolve(ctx(Map.of()))).isEqualTo(first);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DISCOVERY)));

        harness.clock.advance(Duration.ofHours(2));
        mirrors.resolve(ctx(Map.of()));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(DISCOVERY)));
    }

    @Test
    @DisplayName("a 5xx from the first mirror: the same page is read from the next one within the same fetch")
    void failover() throws Exception {
        List<String> names = List.of("de1.api.radio-browser.info", "nl1.api.radio-browser.info");
        discovery("[{\"name\": \"de1.api.radio-browser.info\"}, {\"name\": \"nl1.api.radio-browser.info\"}]");
        List<String> order = shuffled(names);
        stations(order.get(0), 500, "{\"error\": \"down\"}".getBytes(StandardCharsets.UTF_8));
        stations(order.get(1), 200, page());

        FetchResult result = adapter.fetch(ctx(Map.of()));
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).hasSize(25);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/" + order.get(0) + "/json/stations/search")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/" + order.get(1) + "/json/stations/search"))
                .withQueryParam("has_geo_info", equalTo("true")).withQueryParam("hidebroken", equalTo("true"))
                .withQueryParam("order", equalTo("name")).withQueryParam("limit", equalTo("1000"))
                .withQueryParam("offset", equalTo("0")).withHeader("User-Agent", matching("Foresignal/.*")));
    }

    @Test
    @DisplayName("a 4xx is not failed over: the run ends")
    void clientErrorEndsTheRun() {
        discovery("[{\"name\": \"de1.api.radio-browser.info\"}, {\"name\": \"nl1.api.radio-browser.info\"}]");
        List<String> order = shuffled(List.of("de1.api.radio-browser.info", "nl1.api.radio-browser.info"));
        stations(order.get(0), 404, "{}".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> adapter.fetch(ctx(Map.of()))).isInstanceOf(AdapterException.class);
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + order.get(1) + "/json/stations/search")));
    }

    @Test
    @DisplayName("discovery fails: the fallback de1, de2, nl1 is used in order and nothing is cached")
    void fallback() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(DISCOVERY)).willReturn(aResponse().withStatus(500)));
        stations("de1.api.radio-browser.info", 502, new byte[0]);
        stations("de2.api.radio-browser.info", 200, page());
        FetchResult result = adapter.fetch(ctx(Map.of()));
        assertThat(result.records()).hasSize(25);
        assertThat(ctx(Map.of()).state().get(RadioMirrors.MIRRORS_KEY)).isEmpty();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/nl1.api.radio-browser.info/json/stations/search")));

        discovery("[{\"name\": \"evil.example.com\"}]");
        assertThat(new RadioMirrors(java.net.URI.create(url(DISCOVERY)), new Random(SEED)).resolve(ctx(Map.of())))
                .as("nothing acceptable is a failed discovery").isEqualTo(RadioMirrors.FALLBACK);
    }

    @Test
    @DisplayName("the crawl cap: a full last page at maxStations is truncated and tombstones nothing")
    void crawlCap() throws Exception {
        discovery("[{\"name\": \"de1.api.radio-browser.info\"}]");
        stations("de1.api.radio-browser.info", 200, page());
        FetchResult capped = adapter.fetch(ctx(Map.of(RadioBrowserAdapter.MAX_STATIONS_PARAM, 25)));
        assertThat(capped.complete()).isFalse();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/de1.api.radio-browser.info/json/stations/search"))
                .withQueryParam("limit", equalTo("25")));
        assertThat(ctx(Map.of()).state().get(AbsenceTombstoner.IDS_KEY)).isEmpty();

        FetchResult complete = adapter.fetch(ctx(Map.of(RadioBrowserAdapter.MAX_STATIONS_PARAM, 30)));
        assertThat(complete.complete()).isTrue();
        assertThat(ctx(Map.of()).state().get(AbsenceTombstoner.IDS_KEY)).isPresent();
    }

    @Test
    @DisplayName("a station missing from two complete crawls is tombstoned once")
    void absence() throws Exception {
        discovery("[{\"name\": \"de1.api.radio-browser.info\"}]");
        stations("de1.api.radio-browser.info", 200, page());
        assertThat(adapter.fetch(ctx(Map.of())).records()).hasSize(25);
        String fewer = new String(page(), StandardCharsets.UTF_8).replace("db93a00f-9191-46ab-9e87-ec9b373b3eee",
                "not-a-uuid-any-more");
        stations("de1.api.radio-browser.info", 200, fewer.getBytes(StandardCharsets.UTF_8));
        assertThat(adapter.fetch(ctx(Map.of())).records()).noneMatch(NormalizedRecord::tombstone);
        List<NormalizedRecord> third = adapter.fetch(ctx(Map.of())).records();
        assertThat(third).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactly("db93a00f-9191-46ab-9e87-ec9b373b3eee");
    }

    @Test
    @DisplayName("retryable failures: a 5xx or an I/O cause, never a 4xx or a plain refusal")
    void retryable() {
        assertThat(RadioBrowserAdapter.retryable(new AdapterException("io", new java.io.IOException("reset"))))
                .isTrue();
        assertThat(RadioBrowserAdapter.retryable(new AdapterException("budget"))).isFalse();
        assertThat(RadioBrowserAdapter.retryable(new ai.foresignal.collect.UpstreamHttpException(503, ""))).isTrue();
        assertThat(RadioBrowserAdapter.retryable(new ai.foresignal.collect.UpstreamHttpException(429, ""))).isFalse();
    }
}
