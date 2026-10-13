package ai.foresignal.source.adapters.gbfs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
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
 * The GBFS adapter against a stubbed upstream through the real upstream client (prompt 071): information fetched
 * once and then only after max(its ttl, 1 h), status skipped while {@code last_updated + ttl} holds, a 302 refused
 * without requesting its target, a status-only station skipped and counted, and 304 handling (including after a
 * restart, when nothing is cached).
 */
@IntegrationTest
class GbfsAdapterTest extends WireMockSupport {

    /** Well after every captured {@code last_updated + ttl}, so no capture is skipped by its ttl. */
    static final Instant NOW = Instant.parse("2026-10-13T17:00:00Z");

    static final String CITI_INFO = "/gbfs.lyft.com/gbfs/2.3/bkn/en/station_information.json";
    static final String CITI_STATUS = "/gbfs.lyft.com/gbfs/2.3/bkn/en/station_status.json";
    static final String BOULDER_INFO = "/gbfs.bcycle.com/bcycle_boulder/station_information.json";
    static final String BOULDER_STATUS = "/gbfs.bcycle.com/bcycle_boulder/station_status.json";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    SimpleMeterRegistry meters;
    GbfsFamily family;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 1_000),
                null);
        meters = new SimpleMeterRegistry();
        family = new GbfsFamily(baseUrl(), () -> meters);
        for (SourceAdapter adapter : family.adapters()) {
            SourceHttp.forgetValidators(state(adapter), GbfsAdapter.INFORMATION_KEY);
            SourceHttp.forgetValidators(state(adapter), GbfsAdapter.STATUS_KEY);
        }
    }

    @AfterEach
    void tearDown() {
        for (SourceAdapter adapter : family.adapters()) {
            upstream.backoff().onSuccess(adapter.id());
        }
    }

    private GbfsAdapter adapter(String id) {
        return (GbfsAdapter) family.adapters().stream().filter(a -> a.id().equals(id)).findFirst().orElseThrow();
    }

    private WeatherAdapterHarness.Context context(SourceAdapter adapter) {
        return new WeatherAdapterHarness.Context(adapter.descriptor(),
                upstream.client().forSource(adapter.id(), adapter.descriptor()), ValidatedParams.of(Map.of()),
                state(adapter), clock);
    }

    private AdapterStateStore state(SourceAdapter adapter) {
        return upstream.client().stateStore(adapter.id());
    }

    private static void serve(String path, byte[] body, String etag) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withHeader("ETag", etag).withBody(body)));
    }

    private static byte[] fixture(String dir, String name) {
        return AdapterFixtures.bytes("gbfs/" + dir, name);
    }

    private void serveCitiBike() {
        serve(CITI_INFO, fixture("citibike", "station_information.json"), "\"i1\"");
        serve(CITI_STATUS, fixture("citibike", "station_status.json"), "\"s1\"");
    }

    private double counter(String name, String source) {
        var counter = meters.find(name).tag("source", source).counter();
        return counter == null ? 0 : counter.count();
    }

    @Test
    @DisplayName("information is fetched once, then again only after max(its ttl, 1 h); status every run")
    void informationCached() throws Exception {
        GbfsAdapter citi = adapter("gbfs-nyc-citibike");
        serveCitiBike();

        FetchResult first = citi.fetch(context(citi));
        assertThat(first.complete()).isTrue();
        assertThat(first.records()).hasSize(14);
        assertThat(counter(GbfsAdapter.UNTIMED_METRIC, "gbfs-nyc-citibike")).isEqualTo(11);

        clock.advance(Duration.ofSeconds(60));
        citi.fetch(context(citi));
        clock.advance(Duration.ofMinutes(58));
        citi.fetch(context(citi));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(CITI_INFO)));
        WIREMOCK.verify(3, getRequestedFor(urlEqualTo(CITI_STATUS)));

        clock.setInstant(NOW.plus(Duration.ofHours(1)));
        citi.fetch(context(citi));
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(CITI_INFO)));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(CITI_INFO)).withHeader("If-None-Match", equalTo("\"i1\"")));
    }

    @Test
    @DisplayName("an information ttl above 1 h is honored: no refresh at 1 h, a refresh at 2 h")
    void longInformationTtl() throws Exception {
        GbfsAdapter boulder = adapter("gbfs-boulder-bcycle");
        String info = AdapterFixtures.text("gbfs/synthetic", "information.json").replace("\"ttl\": 60", "\"ttl\": 7200");
        serve(BOULDER_INFO, info.getBytes(java.nio.charset.StandardCharsets.UTF_8), "\"i\"");
        serve(BOULDER_STATUS, fixture("synthetic", "status-only-station.json"), "\"s\"");

        boulder.fetch(context(boulder));
        clock.setInstant(NOW.plus(Duration.ofMinutes(61)));
        boulder.fetch(context(boulder));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(BOULDER_INFO)));
        clock.setInstant(NOW.plus(Duration.ofHours(2)));
        boulder.fetch(context(boulder));
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(BOULDER_INFO)));
    }

    @Test
    @DisplayName("status is skipped while last_updated + ttl is in the future and later than the next run")
    void statusTtlHonored() throws Exception {
        GbfsAdapter boulder = adapter("gbfs-boulder-bcycle");
        serve(BOULDER_INFO, fixture("synthetic", "information.json"), "\"i\"");
        serve(BOULDER_STATUS, fixture("synthetic", "ttl-future.json"), "\"s\"");
        // last_updated 16:46:40 + ttl 600 s = 16:56:40.
        clock.setInstant(Instant.parse("2026-10-13T16:47:00Z"));

        FetchResult first = boulder.fetch(context(boulder));
        assertThat(first.records()).extracting(NormalizedRecord::externalId).containsExactly("S1", "S2");

        clock.setInstant(Instant.parse("2026-10-13T16:48:00Z"));
        FetchResult skipped = boulder.fetch(context(boulder));
        assertThat(skipped.notModified()).isTrue();
        assertThat(skipped.records()).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(BOULDER_STATUS)));

        // 16:56:00: the ttl still holds now but not at the next run (16:57:00), so the status is requested.
        clock.setInstant(Instant.parse("2026-10-13T16:56:00Z"));
        assertThat(boulder.fetch(context(boulder)).records()).hasSize(2);
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(BOULDER_STATUS)));
    }

    @Test
    @DisplayName("ttl 0 means always refresh")
    void ttlZero() throws Exception {
        GbfsAdapter boulder = adapter("gbfs-boulder-bcycle");
        serve(BOULDER_INFO, fixture("synthetic", "information.json"), "\"i\"");
        String status = AdapterFixtures.text("gbfs/synthetic", "ttl-future.json").replace("\"ttl\": 600", "\"ttl\": 0");
        serve(BOULDER_STATUS, status.getBytes(java.nio.charset.StandardCharsets.UTF_8), "\"s\"");
        clock.setInstant(Instant.parse("2026-10-13T16:47:00Z"));
        boulder.fetch(context(boulder));
        clock.advance(Duration.ofSeconds(30));
        boulder.fetch(context(boulder));
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(BOULDER_STATUS)));
    }

    @Test
    @DisplayName("a 302 is an UpstreamFormatException and its target is never requested")
    void redirectRefused() throws Exception {
        GbfsAdapter citi = adapter("gbfs-nyc-citibike");
        String target = "/elsewhere.example/station_information.json";
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(CITI_INFO)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", baseUrl() + target)));
        serve(target, fixture("citibike", "station_information.json"), "\"x\"");

        assertThatThrownBy(() -> citi.fetch(context(citi))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageStartingWith("redirect refused: localhost");
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo(target)));
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo(CITI_STATUS)));
        assertThat(upstream.backoff().isInBackoff("gbfs-nyc-citibike")).as("a failure, not a backoff").isFalse();

        // The same on the status request, with information already cached.
        WIREMOCK.resetAll();
        serveCitiBike();
        citi.fetch(context(citi));
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(CITI_STATUS)).willReturn(aResponse().withStatus(301)
                .withHeader("Location", "https://evil.example/station_status.json")));
        clock.advance(Duration.ofMinutes(2));
        assertThatThrownBy(() -> citi.fetch(context(citi))).isInstanceOf(UpstreamFormatException.class)
                .hasMessage("redirect refused: localhost (to evil.example)");
        assertThat(GbfsAdapter.redirectHost(java.net.URI.create("https://a.example/x"), "/y")).isEqualTo("a.example");
        assertThat(GbfsAdapter.redirectHost(java.net.URI.create("https://a.example/x"), " ")).isNull();
    }

    @Test
    @DisplayName("a status-only station is skipped and counted until information lists it")
    void statusOnlyStation() throws Exception {
        GbfsAdapter boulder = adapter("gbfs-boulder-bcycle");
        serve(BOULDER_INFO, fixture("synthetic", "information.json"), "\"i\"");
        serve(BOULDER_STATUS, fixture("synthetic", "status-only-station.json"), "\"s\"");

        FetchResult result = boulder.fetch(context(boulder));
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("S1");
        assertThat(counter(GbfsAdapter.UNMATCHED_METRIC, "gbfs-boulder-bcycle")).isEqualTo(1);
        assertThat(result.records().get(0).payload()).containsEntry("system", "boulder-bcycle");
    }

    @Test
    @DisplayName("304s: status unchanged; information kept; after a restart the information validators are dropped")
    void notModified() throws Exception {
        GbfsAdapter citi = adapter("gbfs-nyc-citibike");
        serveCitiBike();
        citi.fetch(context(citi));
        assertThat(state(citi).get(SourceHttp.ETAG_STATE_PREFIX + GbfsAdapter.STATUS_KEY)).contains("\"s1\"");

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(CITI_STATUS)).withHeader("If-None-Match", equalTo("\"s1\""))
                .willReturn(aResponse().withStatus(304)));
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(CITI_INFO)).withHeader("If-None-Match", equalTo("\"i1\""))
                .willReturn(aResponse().withStatus(304)));
        clock.advance(Duration.ofMinutes(2));
        FetchResult unchanged = citi.fetch(context(citi));
        assertThat(unchanged.notModified()).isTrue();
        assertThat(unchanged.records()).isEmpty();

        // An hour on, information answers 304 and the cached stations are kept.
        clock.advance(Duration.ofHours(1));
        assertThat(citi.fetch(context(citi)).notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(CITI_INFO)).withHeader("If-None-Match", equalTo("\"i1\"")));

        // A restart: nothing cached, so information is requested without validators and the status merges again.
        citi.forgetCache();
        WIREMOCK.resetAll();
        serveCitiBike();
        assertThat(citi.fetch(context(citi)).records()).hasSize(14);
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(CITI_INFO)).withHeader("If-None-Match", absent()));
    }

    @Test
    @DisplayName("a body that is not GBFS is a format error and its validators are forgotten")
    void notGbfs() {
        GbfsAdapter citi = adapter("gbfs-nyc-citibike");
        serve(CITI_INFO, fixture("citibike", "station_information.json"), "\"i1\"");
        serve(CITI_STATUS, "<html>maintenance</html>".getBytes(), "\"bad\"");

        assertThatThrownBy(() -> citi.fetch(context(citi))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("station_status is not valid JSON");
        assertThat(state(citi).get(SourceHttp.ETAG_STATE_PREFIX + GbfsAdapter.STATUS_KEY)).isEmpty();
    }

    @Test
    @DisplayName("a 2xx other than 200 is a format error")
    void otherSuccess() {
        GbfsAdapter citi = adapter("gbfs-nyc-citibike");
        serve(CITI_INFO, fixture("citibike", "station_information.json"), "\"i1\"");
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(CITI_STATUS)).willReturn(aResponse().withStatus(204)));
        assertThatThrownBy(() -> citi.fetch(context(citi))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("HTTP 204");
    }
}
