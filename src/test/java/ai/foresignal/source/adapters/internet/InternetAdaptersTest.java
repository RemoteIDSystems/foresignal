package ai.foresignal.source.adapters.internet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
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

/** The prompt 092 adapter edge cases (WireMock): Bearer token, manifest gating, resolved-incident follow-up. */
@IntegrationTest
class InternetAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

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
        harness.backoff.onSuccess(CloudflareOutageAdapter.ID);
        harness.backoff.onSuccess(GpsjamAdapter.ID);
        harness.backoff.onSuccess(StatusPageAdapter.KIND_STATUS);
    }

    @Test
    @DisplayName("Cloudflare Radar is called with the Bearer token")
    void cloudflareBearer() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/client/v4/radar/annotations/outages"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("cloudflare-radar-outages", "outages.json"))));
        CloudflareOutageAdapter adapter = new CloudflareOutageAdapter(baseUrl(), new CountryCentroids());
        harness.credentials.put(CloudflareOutageAdapter.API_TOKEN, "radar-secret");
        harness.reset(adapter.id());
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).hasSize(2);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/client/v4/radar/annotations/outages"))
                .withHeader("Authorization", containing("Bearer radar-secret")));
    }

    @Test
    @DisplayName("gpsjam fetches nothing when the manifest has no new date")
    void gpsjamGating() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/data/manifest.csv")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/csv").withBody(AdapterFixtures.bytes("gpsjam-daily", "manifest.csv"))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/data/2026-10-13-h3_4.csv")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("gpsjam-daily", "day.csv"))));
        GpsjamAdapter adapter = new GpsjamAdapter(baseUrl());
        harness.reset(adapter.id(), "gpsjam:lastDate", "gpsjam:ids");
        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).isNotEmpty();
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/data/2026-10-13-h3_4.csv")));
    }

    @Test
    @DisplayName("a resolved Statuspage incident gets a final version and a tombstone")
    void statusResolved() throws Exception {
        StatusPageAdapter adapter = new StatusPageAdapter(
                new StatusPageFamily.Page("status-test", "example.com", "Test"), baseUrl());
        harness.reset(adapter.id(), "status:summary", "status:open");
        String openSummary = "{\"page\":{\"id\":\"status-test\",\"updated_at\":\"2026-10-14T11:00:00Z\","
                + "\"status\":{\"indicator\":\"minor\",\"description\":\"Partially degraded\"}},"
                + "\"components\":[{\"id\":\"c1\",\"name\":\"API\",\"status\":\"degraded_performance\"}],"
                + "\"incidents\":[{\"id\":\"INC1\",\"name\":\"API latency\",\"status\":\"investigating\","
                + "\"impact\":\"minor\",\"created_at\":\"2026-10-14T10:00:00Z\",\"updated_at\":\"2026-10-14T11:00:00Z\","
                + "\"incident_updates\":[{\"body\":\"Investigating\",\"status\":\"investigating\"}]}]}";
        serveSummary(openSummary);
        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).anyMatch(r -> r.externalId().equals("incident:INC1"));
        assertThat(first.records()).noneMatch(NormalizedRecord::tombstone);

        String closedSummary = "{\"page\":{\"id\":\"status-test\",\"updated_at\":\"2026-10-14T12:00:00Z\","
                + "\"status\":{\"indicator\":\"none\",\"description\":\"All Systems Operational\"}},"
                + "\"components\":[],\"incidents\":[]}";
        serveSummary(closedSummary);
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v2/incidents.json")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"incidents\":[{\"id\":\"INC1\",\"name\":\"API latency\",\"status\":\"resolved\","
                        + "\"impact\":\"minor\",\"created_at\":\"2026-10-14T10:00:00Z\","
                        + "\"updated_at\":\"2026-10-14T11:30:00Z\",\"incident_updates\":[]}]}")));
        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.records()).filteredOn(NormalizedRecord::tombstone)
                .extracting(NormalizedRecord::externalId).contains("incident:INC1");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api/v2/incidents.json")));
    }

    private static void serveSummary(String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v2/summary.json")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }
}
