package ai.foresignal.source.adapters.celestrak;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamHaltException;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code celestrak-gp} against a stubbed CelesTrak through the real {@code UpstreamClient} (prompt 064): the
 * "not updated" 403, the halt on every other unexpected answer, the member-group schedule and serial requests.
 */
@IntegrationTest
class CelestrakGpAdapterTest extends WireMockSupport {

    private static final String ID = CelestrakGpAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-13T13:00:00Z");
    private static final String ACTIVE = "/NORAD/elements/gp.php?GROUP=active&FORMAT=csv";
    private static final String NOT_UPDATED = "GP data has not updated since your last successful download";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private CelestrakGpAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW), Budget.of(100, 1000));
        harness.reset(ID);
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", ID);
        adapter = new CelestrakGpAdapter(baseUrl());
    }

    private DefaultFetchContext context(Map<String, Object> params) {
        return harness.context(adapter, params);
    }

    private static String group(String g) {
        return "/NORAD/elements/gp.php?GROUP=" + g + "&FORMAT=csv";
    }

    private static void serve(String path, int status, String contentType, byte[] body) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(path)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", contentType).withBody(body)));
    }

    private static void serveCsv(String path) {
        serve(path, 200, "text/plain; charset=UTF-8", AdapterFixtures.bytes(ID, "stations.csv"));
    }

    @Test
    @DisplayName("the main group and stale member groups are fetched; memberships fill each record's groups")
    void membersMerged() throws Exception {
        serveCsv(ACTIVE);
        serveCsv(group("stations"));
        serveCsv(group("visual"));

        FetchResult result = adapter.fetch(context(Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of("stations", "visual"))));

        assertThat(result.complete()).isTrue();
        assertThat(result.records()).hasSize(21);
        NormalizedRecord iss = result.records().stream().filter(r -> r.externalId().equals("25544")).findFirst().orElseThrow();
        assertThat(iss.payload().get("groups")).isEqualTo(List.of("stations", "visual"));
        assertThat(iss.observedAt()).isEqualTo(Instant.parse("2026-10-13T02:41:20.970528Z"));
        assertThat(harness.client.stateStore(ID).get("members:stations")).isPresent();
        assertThat(harness.client.stateStore(ID).get("membersAt:visual")).contains(NOW.toString());
    }

    @Test
    @DisplayName("at most two member groups per run, oldest first; a day later the rest follow")
    void twoPerRun() throws Exception {
        serveCsv(ACTIVE);
        for (String g : List.of("stations", "visual", "weather")) {
            serveCsv(group(g));
        }
        Map<String, Object> params = Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of("stations", "visual", "weather"));

        adapter.fetch(context(params));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(group("stations"))));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(group("visual"))));
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo(group("weather"))));

        harness.clock.advance(Duration.ofHours(2));
        adapter.fetch(context(params));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(group("weather"))));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(group("stations"))));

        harness.clock.advance(Duration.ofHours(2));
        adapter.fetch(context(params));
        WIREMOCK.verify(3, getRequestedFor(urlEqualTo(ACTIVE)));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(group("stations"))));
    }

    @Test
    @DisplayName("requests are strictly serial: each starts after the previous one was answered")
    void serial() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo("/NORAD/elements/gp.php")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/plain").withFixedDelay(150).withBody(AdapterFixtures.bytes(ID, "stations.csv"))));

        adapter.fetch(context(Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of("stations", "visual"))));

        List<ServeEvent> events = WIREMOCK.getAllServeEvents().stream()
                .sorted(Comparator.comparing(e -> e.getRequest().getLoggedDate())).toList();
        assertThat(events).hasSize(3);
        assertThat(events.get(0).getRequest().getUrl()).isEqualTo(ACTIVE);
        for (int i = 1; i < events.size(); i++) {
            long previousStart = events.get(i - 1).getRequest().getLoggedDate().getTime();
            long start = events.get(i).getRequest().getLoggedDate().getTime();
            assertThat(start - previousStart).as("request %d waited for request %d", i, i - 1).isGreaterThanOrEqualTo(150);
        }
    }

    @Test
    @DisplayName("the 'not updated' 403 is not modified: no error, no backoff")
    void notUpdated() throws Exception {
        serve(ACTIVE, 403, "text/plain", NOT_UPDATED.getBytes());

        FetchResult result = adapter.fetch(context(Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of())));

        assertThat(result.notModified()).isTrue();
        assertThat(harness.backoff.isInBackoff(ID)).isFalse();
    }

    @Test
    @DisplayName("a member group's 'not updated' 403 refreshes its time and keeps its membership")
    void memberNotUpdated() throws Exception {
        serveCsv(ACTIVE);
        serve(group("stations"), 403, "text/plain", NOT_UPDATED.getBytes());
        Memberships.store(harness.client.stateStore(ID), "stations", List.of(25544L), NOW.minus(Duration.ofDays(2)));

        FetchResult result = adapter.fetch(context(Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of("stations"))));

        assertThat(result.records()).filteredOn(r -> r.externalId().equals("25544"))
                .singleElement().satisfies(r -> assertThat(r.payload().get("groups")).isEqualTo(List.of("stations")));
        assertThat(Memberships.fetchedAt(harness.client.stateStore(ID), "stations")).contains(NOW);
    }

    @Test
    @DisplayName("another 403, a 404, a 301 (not followed) and a 503 each halt the source for 24 h")
    void halts() {
        for (int status : List.of(403, 404, 301, 503)) {
            WIREMOCK.resetAll();
            harness.reset(ID);
            if (status == 301) {
                WIREMOCK.stubFor(WireMock.get(urlEqualTo(ACTIVE)).willReturn(aResponse().withStatus(301)
                        .withHeader("Location", baseUrl() + "/elsewhere")));
            } else {
                serve(ACTIVE, status, "text/html", "<html>Forbidden</html>".getBytes());
            }
            assertThatThrownBy(() -> adapter.fetch(context(Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of()))))
                    .as("HTTP %d", status)
                    .isInstanceOfSatisfying(UpstreamHaltException.class, e -> {
                        assertThat(e.status()).isEqualTo(status);
                        assertThat(e.hold()).isEqualTo(Duration.ofHours(24));
                        assertThat(e.getMessage()).isEqualTo("CelesTrak returned " + status
                                + "; automatic retries stopped per CelesTrak usage policy — investigate, then use fetch-now");
                    });
            WIREMOCK.verify(1, getRequestedFor(urlEqualTo(ACTIVE)));
            WIREMOCK.verify(0, getRequestedFor(urlEqualTo("/elsewhere")));
        }
    }

    @Test
    @DisplayName("a 200 that is not CSV halts too; a truncated or empty active body is a format error")
    void wrongBody() {
        serve(ACTIVE, 200, "text/html", "<html>maintenance</html>".getBytes());
        assertThatThrownBy(() -> adapter.fetch(context(Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of()))))
                .isInstanceOfSatisfying(UpstreamHaltException.class, e -> assertThat(e.status()).isZero());

        WIREMOCK.resetAll();
        serve(ACTIVE, 200, "text/plain", (String.join(",", ai.foresignal.space.OmmElements.KEYS) + "\r\n").getBytes());
        assertThatThrownBy(() -> adapter.fetch(context(Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of()))))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("JSON format requests FORMAT=json and parses the same records")
    void jsonFormat() throws Exception {
        serve("/NORAD/elements/gp.php?GROUP=active&FORMAT=json", 200, "application/json",
                AdapterFixtures.bytes(ID, "stations.json"));
        FetchResult result = adapter.fetch(context(Map.of(CelestrakGpAdapter.FORMAT_PARAM, "json",
                CelestrakGpAdapter.MEMBERS_PARAM, List.of())));
        assertThat(result.records()).hasSize(21);
    }

    @Test
    @DisplayName("the descriptor: 2 h interval, 3/min 48/day, 32 MB, 3-day TTL at receipt, no redirects")
    void descriptor() {
        var d = adapter.descriptor();
        assertThat(d.minInterval()).isEqualTo(Duration.ofHours(2));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofHours(2));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(3, 48));
        assertThat(d.maxResponseBytes()).isEqualTo(32 * 1024 * 1024);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofDays(3));
        assertThat(d.ttlAnchor()).isEqualTo(ai.foresignal.source.spi.TtlAnchor.RECEIVED);
        assertThat(d.followRedirects()).isFalse();
        assertThat(d.titleField()).isEqualTo("OBJECT_NAME");
        assertThat(d.searchFields()).containsExactly("OBJECT_NAME", "OBJECT_ID");
        assertThat(ai.foresignal.source.DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(adapter.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of("group", "../x"))))
                .containsKey("group");
        assertThat(adapter.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of("group", "stations"))))
                .isEmpty();
    }
}
