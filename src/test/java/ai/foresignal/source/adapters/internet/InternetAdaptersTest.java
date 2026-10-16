package ai.foresignal.source.adapters.internet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
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
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 092 adapter edge cases (WireMock, the real upstream client): the Bearer token sent and redacted,
 * Radar pagination stopping at {@code total_count}, a 429 with {@code Retry-After}, conditional-GET 304s (Atlas
 * dump, Statuspage), the Atlas intraday refresh, gpsjam manifest gating at its real default budget, the
 * resolved-incident follow-up and the incident.io shim.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class InternetAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final String TOKEN = "radar-secret-0123456789-TOKEN";
    static final String OUTAGES = "/client/v4/radar/annotations/outages";
    static final String HIJACKS = "/client/v4/radar/bgp/hijacks/events";
    static final String LEAKS = "/client/v4/radar/bgp/leaks/events";
    static final String DUMP = "/ripe/atlas/probes/archive/meta-latest";
    static final String PROBES = "/api/v2/probes/";

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
        harness.backoff.onSuccess(CloudflareBgpAdapter.ID);
        harness.backoff.onSuccess(GpsjamAdapter.ID);
        harness.backoff.onSuccess(AtlasProbesAdapter.ID);
        harness.backoff.onSuccess("status-test");
    }

    @Test
    @DisplayName("Cloudflare Radar is called with the Bearer token, which no trace, recording or log contains")
    void cloudflareBearerRedacted(CapturedOutput output) throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(OUTAGES))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("cloudflare-radar-outages", "outages.json"))));
        CloudflareOutageAdapter adapter = new CloudflareOutageAdapter(baseUrl(), new CountryCentroids());
        harness.credentials.put(CloudflareOutageAdapter.API_TOKEN, TOKEN);
        harness.reset(adapter.id());
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(adapter, Map.of(), recorder);
        FetchResult result = adapter.fetch(ctx);
        assertThat(result.records()).hasSize(2);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(OUTAGES))
                .withHeader("Authorization", equalTo("Bearer " + TOKEN)));
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl).allSatisfy(u -> assertThat(u).doesNotContain(TOKEN));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(TOKEN);
        }
        assertThat(output.getAll()).doesNotContain(TOKEN);
    }

    @Test
    @DisplayName("Cloudflare query values are encoded; one location goes upstream, several are filtered locally")
    void cloudflareLocations() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(OUTAGES))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("cloudflare-radar-outages", "outages.json"))));
        CloudflareOutageAdapter adapter = new CloudflareOutageAdapter(baseUrl(), new CountryCentroids());
        harness.credentials.put(CloudflareOutageAdapter.API_TOKEN, TOKEN);
        harness.reset(adapter.id());

        adapter.fetch(harness.context(adapter, Map.of("location", List.of("de"), "dateRange", "7d&x=1")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(OUTAGES)).withQueryParam("location", equalTo("DE"))
                .withQueryParam("dateRange", equalTo("7d&x=1")).withQueryParam("x", absent()));

        WIREMOCK.resetRequests();
        FetchResult several = adapter.fetch(harness.context(adapter, Map.of("location", List.of("US", "JP"))));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(OUTAGES)).withQueryParam("location", absent()));
        assertThat(several.records()).extracting(NormalizedRecord::externalId).containsExactly("cf-outage:1");
    }

    @Test
    @DisplayName("a 429 with Retry-After backs the source off and nothing is sent during the backoff")
    void cloudflareRetryAfter() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(OUTAGES)).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "120").withBody("Too many requests")));
        CloudflareOutageAdapter adapter = new CloudflareOutageAdapter(baseUrl(), new CountryCentroids());
        harness.credentials.put(CloudflareOutageAdapter.API_TOKEN, TOKEN);
        harness.reset(adapter.id());
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOfSatisfying(UpstreamHttpException.class, e -> assertThat(e.getStatus()).isEqualTo(429));
        assertThat(harness.backoff.isInBackoff(adapter.id())).isTrue();
        WIREMOCK.resetRequests();
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .as("nothing is sent during the backoff").isInstanceOf(AdapterException.class);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("Radar BGP paging stops at result_info.total_count; AS names come from asn_info")
    void bgpPagination() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(HIJACKS)).withQueryParam("page", equalTo("1"))
                .willReturn(json(hijacks(3, 1, 2))));
        WIREMOCK.stubFor(get(urlPathEqualTo(HIJACKS)).withQueryParam("page", equalTo("2"))
                .willReturn(json(hijacks(3, 3))));
        WIREMOCK.stubFor(get(urlPathEqualTo(HIJACKS)).withQueryParam("page", equalTo("3"))
                .willReturn(json(hijacks(3, 4))));
        WIREMOCK.stubFor(get(urlPathEqualTo(LEAKS)).willReturn(json(
                "{\"result\":{\"asn_info\":[],\"events\":[]},\"result_info\":{\"total_count\":0}}")));
        CloudflareBgpAdapter adapter = new CloudflareBgpAdapter(baseUrl(), new CountryCentroids());
        harness.credentials.put(CloudflareBgpAdapter.API_TOKEN, TOKEN);
        harness.reset(adapter.id());

        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("hijack:1", "hijack:2", "hijack:3");
        assertThat(result.records().get(0).payload()).containsEntry("hijacker_asn_name", "EXAMPLE-NET");
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(HIJACKS)).withQueryParam("page", equalTo("3")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(LEAKS)));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(HIJACKS)).withQueryParam("minConfidence", equalTo("8"))
                .withHeader("Authorization", equalTo("Bearer " + TOKEN)));
    }

    @Test
    @DisplayName("Radar BGP sends one involvedAsn upstream and filters several locally")
    void bgpInvolvedFilters() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(HIJACKS)).willReturn(json(hijacks(2, 1, 2))));
        WIREMOCK.stubFor(get(urlPathEqualTo(LEAKS)).willReturn(json(
                "{\"result\":{\"events\":[]},\"result_info\":{\"total_count\":0}}")));
        CloudflareBgpAdapter adapter = new CloudflareBgpAdapter(baseUrl(), new CountryCentroids());
        harness.credentials.put(CloudflareBgpAdapter.API_TOKEN, TOKEN);
        harness.reset(adapter.id());

        adapter.fetch(harness.context(adapter, Map.of("involvedAsn", List.of("64501"), "involvedCountry", List.of("de"))));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(HIJACKS)).withQueryParam("involvedAsn", equalTo("64501"))
                .withQueryParam("involvedCountry", equalTo("DE")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(LEAKS)).withQueryParam("involvedAsn", equalTo("64501")));

        WIREMOCK.resetRequests();
        FetchResult several = adapter.fetch(harness.context(adapter,
                Map.of("involvedAsn", List.of("AS64999", "64502"))));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(HIJACKS)).withQueryParam("involvedAsn", containing("6")));
        assertThat(several.records()).extracting(NormalizedRecord::externalId).containsExactly("hijack:2");
    }

    @Test
    @DisplayName("gpsjam ingests a new day at its real default budget and fetches nothing when the manifest has no new date")
    void gpsjamGatingAtDefaultBudget() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/data/manifest.csv")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/csv").withBody(AdapterFixtures.bytes("gpsjam-daily", "manifest.csv"))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/data/2026-10-13-h3_4.csv")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("gpsjam-daily", "day.csv"))));
        GpsjamAdapter adapter = new GpsjamAdapter(baseUrl());
        // The descriptor's own budget, not a generous test one: a run with a new day sends two requests at once,
        // which a one-per-minute bucket refused. A day of its own keeps other tests' usage out of the day bucket.
        AdapterHttpHarness real = new AdapterHttpHarness(jdbc, jsonMapper, properties, install,
                new MutableClock(Instant.parse("2026-11-20T06:00:00Z")), adapter.descriptor().defaultBudget());
        real.reset(adapter.id(), "gpsjam:lastDate", "gpsjam:ids");
        FetchResult first = adapter.fetch(real.context(adapter, Map.of()));
        assertThat(first.records()).isNotEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/data/manifest.csv")));

        real.clock.advance(Duration.ofHours(6));
        FetchResult second = adapter.fetch(real.context(adapter, Map.of()));
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/data/2026-10-13-h3_4.csv")));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo("/data/manifest.csv")));
        real.backoff.onSuccess(adapter.id());
    }

    @Test
    @DisplayName("the Atlas dump: a 304 is unchanged, and the dump is not re-checked within six hours")
    void atlasConditional() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(DUMP)).withHeader("If-None-Match", absent())
                .willReturn(aResponse().withStatus(200).withHeader("ETag", "\"d1\"")
                        .withBody(AdapterFixtures.bytes("ripe-atlas-probes", "meta-latest.bz2"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(DUMP)).withHeader("If-None-Match", equalTo("\"d1\""))
                .willReturn(aResponse().withStatus(304)));
        AtlasProbesAdapter adapter = new AtlasProbesAdapter(baseUrl(), baseUrl());
        harness.reset(adapter.id(), AtlasProbesAdapter.IDS_KEY, AtlasProbesAdapter.LAST_DAILY_KEY,
                "etag:" + AtlasProbesAdapter.DUMP_KEY, "lastmod:" + AtlasProbesAdapter.DUMP_KEY);

        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.complete()).isTrue();
        assertThat(first.records()).isNotEmpty();
        assertThat(first.records()).allMatch(r -> !r.payload().containsKey("address_v4")
                && !r.payload().containsKey("description"));

        FetchResult soon = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(soon.notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DUMP)));

        harness.clock.advance(AtlasProbesAdapter.DUMP_MIN_AGE);
        FetchResult later = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(later.notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DUMP)).withHeader("If-None-Match", equalTo("\"d1\"")));
        harness.clock.setInstant(NOW);
    }

    @Test
    @DisplayName("Atlas intraday: between dump checks the disconnected list is paged into whole probe records")
    void atlasIntraday() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(DUMP)).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("ripe-atlas-probes", "meta-latest.bz2"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(PROBES)).willReturn(json("{\"count\":1,\"next\":null,\"results\":[{"
                + "\"asn_v4\":50266,\"asn_v6\":null,\"country_code\":\"NL\",\"geometry\":{\"type\":\"Point\","
                + "\"coordinates\":[4.9575,52.3405]},\"id\":6,\"is_anchor\":false,\"is_public\":true,"
                + "\"last_connected\":1791530642,\"prefix_v4\":\"82.174.0.0/16\",\"prefix_v6\":null,"
                + "\"status\":{\"id\":2,\"name\":\"Disconnected\",\"since\":\"2026-10-09T07:24:02Z\"},"
                + "\"status_since\":1791530642,\"tags\":[{\"name\":\"Home\",\"slug\":\"home\"}],\"type\":\"Probe\"}]}")));
        AtlasProbesAdapter adapter = new AtlasProbesAdapter(baseUrl(), baseUrl());
        harness.reset(adapter.id(), AtlasProbesAdapter.IDS_KEY, AtlasProbesAdapter.LAST_DAILY_KEY,
                "etag:" + AtlasProbesAdapter.DUMP_KEY, "lastmod:" + AtlasProbesAdapter.DUMP_KEY);
        Map<String, Object> params = Map.of("intradayDisconnected", true);

        adapter.fetch(harness.context(adapter, params));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(PROBES)));

        harness.clock.advance(Duration.ofMinutes(30));
        FetchResult intraday = adapter.fetch(harness.context(adapter, params));
        assertThat(intraday.complete()).as("intraday is a partial update").isFalse();
        assertThat(intraday.records()).hasSize(1);
        NormalizedRecord probe = intraday.records().get(0);
        assertThat(probe.externalId()).isEqualTo("probe:6");
        assertThat(probe.geometry()).as("intraday keeps the location").isNotNull();
        assertThat(probe.payload()).containsEntry("status", 2).containsEntry("statusName", "disconnected")
                .containsEntry("asnV4", 50266).containsEntry("countryCode", "NL")
                .containsEntry("prefixV4", "82.174.0.0/16").containsEntry("tags", List.of("home"));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PROBES)).withQueryParam("status", equalTo("2"))
                .withQueryParam("fields", containing("geometry")).withQueryParam("fields", containing("asn_v4")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DUMP)));
        harness.clock.setInstant(NOW);
    }

    @Test
    @DisplayName("a resolved Statuspage incident gets a final version and a tombstone")
    void statusResolved() throws Exception {
        StatusPageAdapter adapter = statusAdapter();
        serveSummary(openSummary(), null);
        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).anyMatch(r -> r.externalId().equals("incident:INC1"));
        assertThat(first.records()).noneMatch(NormalizedRecord::tombstone);
        assertThat(first.records()).filteredOn(r -> r.kind().equals("service-status"))
                .allMatch(r -> "minor".equals(r.payload().get("indicator")));

        String closedSummary = "{\"page\":{\"id\":\"status-test\",\"updated_at\":\"2026-10-14T12:00:00Z\"},"
                + "\"status\":{\"indicator\":\"none\",\"description\":\"All Systems Operational\"},"
                + "\"components\":[],\"incidents\":[]}";
        serveSummary(closedSummary, null);
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v2/incidents.json")).willReturn(json(
                "{\"incidents\":[{\"id\":\"INC1\",\"name\":\"API latency\",\"status\":\"resolved\","
                        + "\"impact\":\"minor\",\"created_at\":\"2026-10-14T10:00:00Z\","
                        + "\"updated_at\":\"2026-10-14T11:30:00Z\",\"incident_updates\":[]}]}")));
        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.records()).filteredOn(r -> !r.tombstone() && r.externalId().equals("incident:INC1"))
                .singleElement().satisfies(r -> assertThat(r.payload()).containsEntry("status", "resolved"));
        assertThat(second.records()).filteredOn(NormalizedRecord::tombstone)
                .extracting(NormalizedRecord::externalId).contains("incident:INC1");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api/v2/incidents.json")));
    }

    @Test
    @DisplayName("a Statuspage 304 keeps the open set: no follow-up request, no tombstone")
    void statusNotModified() throws Exception {
        StatusPageAdapter adapter = statusAdapter();
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v2/summary.json")).withHeader("If-None-Match", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("ETag", "W/\"s1\"").withBody(openSummary())));
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v2/summary.json")).withHeader("If-None-Match", equalTo("W/\"s1\""))
                .willReturn(aResponse().withStatus(304)));
        adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(harness.client.stateStore(adapter.id()).get(StatusPageAdapter.OPEN_KEY)).contains("incident:INC1");

        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.notModified()).isTrue();
        assertThat(second.records()).isEmpty();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/api/v2/incidents.json")));
        assertThat(harness.client.stateStore(adapter.id()).get(StatusPageAdapter.OPEN_KEY)).contains("incident:INC1");
    }

    @Test
    @DisplayName("the incident.io shim: open incidents come from one incidents.json request, filtered locally")
    void statusIncidentIoShim() throws Exception {
        StatusPageAdapter adapter = statusAdapter();
        serveSummary("{\"page\":{\"id\":\"01JMDK\",\"updated_at\":\"2026-10-14T11:00:00Z\"},"
                + "\"status\":{\"indicator\":\"minor\",\"description\":\"Degraded\"},\"components\":[]}", null);
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v2/incidents.json")).willReturn(json("{\"incidents\":["
                + "{\"id\":\"01OPEN\",\"name\":\"Errors\",\"status\":\"investigating\",\"impact\":\"minor\","
                + "\"created_at\":\"2026-10-14T10:00:00Z\",\"updated_at\":\"2026-10-14T11:00:00Z\","
                + "\"incident_updates\":[{\"body\":\"Looking\"}]},"
                + "{\"id\":\"01DONE\",\"name\":\"Old\",\"status\":\"resolved\",\"impact\":\"minor\","
                + "\"created_at\":\"2026-10-10T10:00:00Z\",\"updated_at\":\"2026-10-10T11:00:00Z\","
                + "\"resolved_at\":\"2026-10-10T11:00:00Z\",\"incident_updates\":[]}]}")));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).filteredOn(r -> r.kind().equals("service-incident"))
                .extracting(NormalizedRecord::externalId).containsExactly("incident:01OPEN");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api/v2/incidents.json")));
    }

    private StatusPageAdapter statusAdapter() {
        StatusPageAdapter adapter = new StatusPageAdapter(
                new StatusPageFamily.Page("status-test", "example.com", "Test"), baseUrl());
        harness.reset(adapter.id(), StatusPageAdapter.SUMMARY_KEY, StatusPageAdapter.OPEN_KEY,
                "etag:" + StatusPageAdapter.SUMMARY_KEY, "lastmod:" + StatusPageAdapter.SUMMARY_KEY);
        return adapter;
    }

    private static String openSummary() {
        return "{\"page\":{\"id\":\"status-test\",\"updated_at\":\"2026-10-14T11:00:00Z\"},"
                + "\"status\":{\"indicator\":\"minor\",\"description\":\"Partially degraded\"},"
                + "\"components\":[{\"id\":\"c1\",\"name\":\"API\",\"status\":\"degraded_performance\"}],"
                + "\"incidents\":[{\"id\":\"INC1\",\"name\":\"API latency\",\"status\":\"investigating\","
                + "\"impact\":\"minor\",\"created_at\":\"2026-10-14T10:00:00Z\",\"updated_at\":\"2026-10-14T11:00:00Z\","
                + "\"incident_updates\":[{\"body\":\"Investigating\",\"status\":\"investigating\"}]}]}";
    }

    private static void serveSummary(String body, String etag) {
        var response = aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body);
        if (etag != null) {
            response = response.withHeader("ETag", etag);
        }
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v2/summary.json")).willReturn(response));
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder json(String body) {
        return aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body);
    }

    /** A hijacks page of {@code ids} with {@code total} events in all. */
    private static String hijacks(int total, int... ids) {
        StringBuilder events = new StringBuilder();
        for (int id : ids) {
            events.append(events.isEmpty() ? "" : ",").append("{\"id\":").append(id)
                    .append(",\"confidence_score\":9,\"duration\":60,\"event_type\":0,\"hijack_msgs_count\":3,")
                    .append("\"hijacker_asn\":6450").append(id).append(",\"hijacker_country\":\"US\",")
                    .append("\"is_stale\":false,\"max_hijack_ts\":\"2026-10-14T10:30:00Z\",")
                    .append("\"max_msg_ts\":\"2026-10-14T10:35:00Z\",\"min_hijack_ts\":\"2026-10-14T10:00:00Z\",")
                    .append("\"on_going_count\":1,\"peer_asns\":[1],\"peer_ip_count\":5,")
                    .append("\"prefixes\":[\"203.0.113.0/24\"],\"tags\":[],\"victim_asns\":[64499],")
                    .append("\"victim_countries\":[\"DE\"]}");
        }
        return "{\"success\":true,\"result\":{\"asn_info\":[{\"asn\":64501,\"country_code\":\"US\","
                + "\"org_name\":\"EXAMPLE-NET\"}],\"events\":[" + events + "],\"total_monitors\":10},"
                + "\"result_info\":{\"count\":" + ids.length + ",\"page\":1,\"per_page\":100,\"total_count\":" + total
                + "}}";
    }
}
