package ai.foresignal.source.adapters.ucdp;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
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
 * {@link UcdpGedAdapter} against a stubbed UCDP API (prompt 081): the month walk-back across the year boundary and its
 * daily cache, the {@code version} override, {@code TotalPages} iteration from the page number in {@code NextPageUrl},
 * probe errors counted as empty, a 401, and the {@code x-ucdp-access-token} header sent and redacted.
 */
@IntegrationTest
class UcdpVersionDiscoveryTest extends WireMockSupport {

    static final Instant JANUARY = Instant.parse("2027-01-10T12:00:00Z");
    static final String BASE = "/ucdpapi.pcr.uu.se/api/gedevents/";
    static final String TOKEN = "ucdp-token-0123456789-SECRET";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    UcdpGedAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(JANUARY),
                Budget.of(100, 1000));
        adapter = new UcdpGedAdapter(baseUrl(), new CountryCentroids());
        harness.reset(UcdpGedAdapter.ID, UcdpGedAdapter.VERSION_KEY, UcdpGedAdapter.VERSION_AT_KEY,
                UcdpGedAdapter.ENDS_KEY, AbsenceTombstoner.IDS_KEY, AbsenceTombstoner.MISSED_KEY);
        harness.credentials.clear();
        harness.credentials.put(UcdpGedAdapter.TOKEN, TOKEN);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(UcdpGedAdapter.ID);
    }

    static void probe(String version, String file) {
        WIREMOCK.stubFor(get(urlPathEqualTo(BASE + version)).withQueryParam("pagesize", equalTo("1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("ucdp-ged", file))));
    }

    static void pages(String version) {
        WIREMOCK.stubFor(get(urlPathEqualTo(BASE + version)).withQueryParam("pagesize", equalTo("1000"))
                .withQueryParam("page", absent()).willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("ucdp-ged", "page1.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(BASE + version)).withQueryParam("pagesize", equalTo("1000"))
                .withQueryParam("page", equalTo("2")).willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("ucdp-ged", "page2.json"))));
    }

    @Test
    @DisplayName("candidate version names are YY.0.M")
    void names() {
        assertThat(UcdpGedAdapter.candidateVersion(YearMonth.of(2026, 8))).isEqualTo("26.0.8");
        assertThat(UcdpGedAdapter.candidateVersion(YearMonth.of(2027, 1))).isEqualTo("27.0.1");
    }

    @Test
    @DisplayName("January 2027: 27.0.1 and 26.0.12 are empty, 26.0.11 has events; cached for a day")
    void walkBackAcrossTheYear() throws Exception {
        probe("27.0.1", "probe-empty.json");
        probe("26.0.12", "probe-empty.json");
        probe("26.0.11", "probe-hit.json");
        pages("26.0.11");

        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));

        List<String> paths = WIREMOCK.getAllServeEvents().reversed().stream()
                .map(e -> e.getRequest().getUrl()).toList();
        assertThat(paths.subList(0, 3)).extracting(u -> u.substring(0, u.indexOf('?')))
                .containsExactly(BASE + "27.0.1", BASE + "26.0.12", BASE + "26.0.11");
        assertThat(result.records()).filteredOn(r -> !r.tombstone()).hasSize(3)
                .allSatisfy(r -> assertThat(r.payload()).containsEntry("datasetVersion", "26.0.11"));
        var state = harness.client.stateStore(UcdpGedAdapter.ID);
        assertThat(state.get(UcdpGedAdapter.VERSION_KEY)).contains("26.0.11");

        WIREMOCK.resetRequests();
        harness.clock.setInstant(JANUARY.plus(Duration.ofHours(6)));
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(BASE + "27.0.1")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(BASE + "26.0.11")).withQueryParam("pagesize", equalTo("1")));

        WIREMOCK.resetRequests();
        harness.clock.setInstant(JANUARY.plus(Duration.ofHours(25)));
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(BASE + "27.0.1")).withQueryParam("pagesize", equalTo("1")));
    }

    @Test
    @DisplayName("the version parameter skips discovery")
    void versionOverride() throws Exception {
        pages("26.1");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of("version", "26.1")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(BASE + "27.0.1")));
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()).withQueryParam("pagesize", equalTo("1")));
        assertThat(result.records()).isNotEmpty()
                .allSatisfy(r -> assertThat(r.payload()).containsEntry("datasetVersion", "26.1"));
    }

    @Test
    @DisplayName("TotalPages 2: the first page without page, then page=2 from NextPageUrl, never a guessed page")
    void totalPagesIteration() throws Exception {
        pages("26.0.8");
        adapter.fetch(harness.context(adapter, Map.of("version", "26.0.8", "lookbackDays", 120)));

        List<LoggedRequest> requests = WIREMOCK.findAll(getRequestedFor(urlPathEqualTo(BASE + "26.0.8")));
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).queryParameter("page").isPresent()).isFalse();
        assertThat(requests.get(0).queryParameter("StartDate").firstValue()).isEqualTo("2026-09-12");
        assertThat(requests.get(0).queryParameter("EndDate").firstValue()).isEqualTo("2027-01-10");
        assertThat(requests.get(1).queryParameter("page").firstValue()).isEqualTo("2");
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(BASE + "26.0.8")).withQueryParam("page", equalTo("3")));
    }

    @Test
    @DisplayName("a probe that errors counts as empty; no release in 14 months is a format error with nothing cached")
    void probeErrors() {
        WIREMOCK.stubFor(get(urlPathEqualTo(BASE + "27.0.1")).willReturn(aResponse().withStatus(500)
                .withBody("Internal Server Error")));
        WIREMOCK.stubFor(get(anyUrl()).atPriority(10).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("ucdp-ged", "probe-empty.json"))));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("no UCDP candidate release");
        assertThat(WIREMOCK.findAll(anyRequestedFor(anyUrl()))).hasSize(UcdpGedAdapter.MAX_PROBES);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(BASE + "25.0.12")));
        assertThat(harness.client.stateStore(UcdpGedAdapter.ID).get(UcdpGedAdapter.VERSION_KEY)).isEmpty();
    }

    @Test
    @DisplayName("a 401 is CredentialsRejectedException; no token sends nothing")
    void unauthorized() {
        WIREMOCK.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(401)
                .withBody(AdapterFixtures.bytes("ucdp-ged", "unauthorized-401.txt"))));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of("version", "26.0.8"))))
                .isInstanceOf(CredentialsRejectedException.class);
        WIREMOCK.resetRequests();
        harness.credentials.clear();
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(CredentialsRejectedException.class);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("the token travels in x-ucdp-access-token and never reaches traces or recorded exchanges")
    void tokenHeader() throws Exception {
        pages("26.0.8");
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(adapter, Map.of("version", "26.0.8"), recorder);
        adapter.fetch(ctx);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(BASE + "26.0.8"))
                .withHeader(UcdpGedAdapter.TOKEN_HEADER, equalTo(TOKEN)));
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl).allSatisfy(u -> assertThat(u).doesNotContain(TOKEN));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(TOKEN);
        }
    }

    @Test
    @DisplayName("an event missing twice inside the window is tombstoned; one whose date_end left the window is not")
    void windowedAbsence() throws Exception {
        pages("26.0.8");
        Map<String, Object> params = Map.of("version", "26.0.8", "lookbackDays", 89);
        harness.clock.setInstant(Instant.parse("2026-10-13T12:00:00Z"));
        adapter.fetch(harness.context(adapter, params));

        // A newer read holds only the Ukraine event: 780002 (date_end 2026-08-31) and 780003 (2026-08-15) are missing.
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(BASE + "26.0.8")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("ucdp-ged", "revised.json"))));
        assertThat(adapter.fetch(harness.context(adapter, params)).records()).noneMatch(NormalizedRecord::tombstone);

        // A month on, the window starts 2026-08-16: 780003 has left it (forgotten), 780002 is still inside (tombstoned).
        harness.clock.setInstant(Instant.parse("2026-11-13T12:00:00Z"));
        FetchResult third = adapter.fetch(harness.context(adapter, params));
        assertThat(third.records()).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactly("780002");
        assertThat(third.records()).filteredOn(r -> !r.tombstone()).extracting(NormalizedRecord::externalId)
                .containsExactly("780001");
        assertThat(third.records().getFirst().payload()).containsEntry("deathsBest", 4L);
        assertThat(UcdpGedAdapter.readEnds(harness.client.stateStore(UcdpGedAdapter.ID))).containsOnlyKeys("780001");
    }
}
