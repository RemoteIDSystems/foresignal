package ai.foresignal.source.adapters.roads;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Instant;
import java.util.ArrayList;
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
 * The road-event adapters against stubbed upstreams through the real upstream client (prompt 074): Open511 paging via
 * {@code next_url}, Digitraffic's one conditional request per situation type with {@code Digitraffic-User}, NDW's gzip
 * body and 304, and WZDx key placement redacted from traces.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class RoadEventAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T19:00:00Z");
    static final String OPEN511 = "/api.open511.gov.bc.ca/events";
    static final String OPEN511_QUERY = "?format=json&status=ACTIVE&limit=500";
    static final String DT_ROADWORKS = "/tie.digitraffic.fi/api/traffic-message/v2/roadworks";
    static final String DT_ANNOUNCEMENTS = "/tie.digitraffic.fi/api/traffic-message/v2/traffic-announcements";
    static final String DT_WEIGHT = "/tie.digitraffic.fi/api/traffic-message/v2/weight-restrictions";
    static final String NDW = "/opendata.ndw.nu/actueel_beeld.xml.gz";
    static final String WSDOT = "/wzdx.wsdot.wa.gov/api/v4/WorkZoneFeed";
    static final String CDOT = "/data.cotrip.org/api/v1/wzdx";
    static final String CDOT_KEY = "cdot0123456789abcdefKEY";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    List<SourceAdapter> adapters;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        adapters = new ArrayList<>();
        adapters.addAll(new Open511Family(baseUrl(), () -> null).adapters());
        adapters.addAll(new WzdxFamily(baseUrl(), () -> null).adapters());
        adapters.addAll(new NdwFamily(baseUrl(), () -> null).adapters());
        adapters.add(new DigitrafficRoadAdapter(baseUrl(), () -> "9.9.9", () -> null));
        for (SourceAdapter a : adapters) {
            List<String> keys = new ArrayList<>(List.of(AbsenceTombstoner.IDS_KEY, AbsenceTombstoner.MISSED_KEY));
            for (String state : List.of(WzdxAdapter.STATE_KEY, NdwSituationsAdapter.STATE_KEY,
                    "msg:TRAFFIC_ANNOUNCEMENT", "msg:ROAD_WORK", "msg:WEIGHT_RESTRICTION", "msg:EXEMPTED_TRANSPORT")) {
                keys.add(SourceHttp.ETAG_STATE_PREFIX + state);
                keys.add(SourceHttp.LAST_MODIFIED_STATE_PREFIX + state);
            }
            for (String type : DigitrafficRoadAdapter.ROUTES.keySet()) {
                keys.add(AbsenceTombstoner.IDS_KEY + ":" + type);
                keys.add(AbsenceTombstoner.MISSED_KEY + ":" + type);
            }
            harness.reset(a.id(), keys.toArray(String[]::new));
        }
    }

    @AfterEach
    void tearDown() {
        harness.credentials.clear();
        for (SourceAdapter a : adapters) {
            harness.backoff.onSuccess(a.id());
        }
    }

    SourceAdapter adapter(String id) {
        return adapters.stream().filter(a -> a.id().equals(id)).findFirst().orElseThrow();
    }

    FetchResult fetch(String id, Map<String, Object> params) throws AdapterException {
        SourceAdapter a = adapter(id);
        return ((ai.foresignal.source.spi.PollingAdapter) a).fetch(harness.context(a, params));
    }

    static void serve(String path, byte[] body, String contentType, String etag) {
        var response = aResponse().withStatus(200).withHeader("Content-Type", contentType).withBody(body);
        if (etag != null) {
            response = response.withHeader("ETag", etag);
        }
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(path)).willReturn(response));
    }

    @Test
    @DisplayName("every road-event descriptor validates, is disabled-by-default material and emits road-event")
    void descriptors() {
        assertThat(adapters).hasSize(34);
        for (SourceAdapter a : adapters) {
            assertThat(DescriptorValidator.problems(a.descriptor(), a)).as(a.id()).isEmpty();
            assertThat(a.descriptor().kinds()).as(a.id()).containsExactly("road-event");
            assertThat(a.descriptor().startField()).isEqualTo("startsAt");
            assertThat(a.descriptor().endField()).isEqualTo("endsAt");
            assertThat(a.descriptor().titleField()).isEqualTo("headline");
        }
    }

    @Test
    @DisplayName("Open511 follows pagination.next_url on the same server and combines the pages")
    void open511Paging() throws Exception {
        String page1 = """
                {"events": [{"id": "drivebc.ca/P-1", "status": "ACTIVE", "event_type": "INCIDENT", "severity": "MINOR",
                 "updated": "2026-10-13T10:00:00-07:00", "geography": {"type": "Point", "coordinates": [-123.1, 49.2]}}],
                 "pagination": {"offset": "0", "next_url": "%s"}}""".formatted(OPEN511 + OPEN511_QUERY + "&offset=1");
        String page2 = """
                {"events": [{"id": "drivebc.ca/P-2", "status": "ACTIVE", "event_type": "CONSTRUCTION",
                 "updated": "2026-10-13T11:00:00-07:00", "geography": {"type": "Point", "coordinates": [-123.2, 49.3]}},
                 {"id": "drivebc.ca/P-OLD", "status": "ARCHIVED", "updated": "2026-10-13T11:00:00-07:00"}],
                 "pagination": {"offset": "1"}}""";
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(OPEN511)).withQueryParam("offset", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(page1)));
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(OPEN511)).withQueryParam("offset", equalTo("1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(page2)));

        FetchResult result = fetch("open511-drivebc", Map.of());
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("drivebc.ca/P-1", "drivebc.ca/P-2", "drivebc.ca/P-OLD");
        assertThat(result.records().get(2).tombstone()).as("ARCHIVED is tombstoned at once").isTrue();
        WIREMOCK.verify(getRequestedFor(urlEqualTo(OPEN511 + OPEN511_QUERY))
                .withHeader("User-Agent", equalTo(install.userAgent())));
        WIREMOCK.verify(getRequestedFor(urlEqualTo(OPEN511 + OPEN511_QUERY + "&offset=1")));
    }

    @Test
    @DisplayName("Open511 stops after 10 pages with a partial set, and refuses a next_url to another server")
    void open511Limits() throws Exception {
        String endless = """
                {"events": [], "pagination": {"offset": "0", "next_url": "%s"}}"""
                .formatted(OPEN511 + OPEN511_QUERY + "&offset=1");
        serve(OPEN511, endless.getBytes(java.nio.charset.StandardCharsets.UTF_8), "application/json", null);
        FetchResult partial = fetch("open511-drivebc", Map.of());
        assertThat(partial.complete()).isFalse();
        WIREMOCK.verify(Open511Adapter.MAX_PAGES, getRequestedFor(urlPathEqualTo(OPEN511)));

        WIREMOCK.resetAll();
        String offHost = """
                {"events": [], "pagination": {"offset": "0", "next_url": "https://evil.example/events"}}""";
        serve(OPEN511, offHost.getBytes(java.nio.charset.StandardCharsets.UTF_8), "application/json", null);
        assertThatThrownBy(() -> fetch("open511-drivebc", Map.of())).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("leaves the server");
    }

    @Test
    @DisplayName("Digitraffic: one conditional GET per type with Digitraffic-User and gzip; all 304 is unchanged")
    void digitrafficPerType() throws Exception {
        serve(DT_ANNOUNCEMENTS, AdapterFixtures.bytes("digitraffic-road", "traffic-announcements.json"),
                "application/json;charset=UTF-8", "W/\"ta1\"");
        serve(DT_ROADWORKS, AdapterFixtures.bytes("digitraffic-road", "roadworks.json"),
                "application/json;charset=UTF-8", "W/\"rw1\"");
        FetchResult first = fetch(DigitrafficRoadAdapter.ID, Map.of());
        assertThat(first.complete()).isTrue();
        assertThat(first.records().stream().filter(r -> !r.tombstone())).hasSize(8 + 14);
        assertThat(first.records().stream().filter(NormalizedRecord::tombstone)).as("closed early").hasSize(3);
        for (String path : List.of(DT_ANNOUNCEMENTS, DT_ROADWORKS)) {
            WIREMOCK.verify(1, getRequestedFor(urlEqualTo(path)));
            WIREMOCK.verify(getRequestedFor(urlEqualTo(path)).withHeader("Digitraffic-User", equalTo("Foresignal/9.9.9"))
                    .withHeader("Accept-Encoding", matching(".*gzip.*"))
                    .withHeader("User-Agent", equalTo(install.userAgent())).withHeader("If-None-Match", absent()));
        }
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo(DT_WEIGHT)));

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(DT_ANNOUNCEMENTS)).withHeader("If-None-Match", equalTo("W/\"ta1\""))
                .willReturn(aResponse().withStatus(304)));
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(DT_ROADWORKS)).withHeader("If-None-Match", equalTo("W/\"rw1\""))
                .willReturn(aResponse().withStatus(304)));
        FetchResult second = fetch(DigitrafficRoadAdapter.ID, Map.of());
        assertThat(second.notModified()).isTrue();
        assertThat(second.records()).isEmpty();
    }

    @Test
    @DisplayName("Digitraffic: the situationTypes parameter picks the requests; one 304 makes the result partial")
    void digitrafficTypes() throws Exception {
        serve(DT_WEIGHT, AdapterFixtures.bytes("digitraffic-road", "weight-restrictions.json"),
                "application/json", "\"wr1\"");
        serve(DT_ROADWORKS, AdapterFixtures.bytes("digitraffic-road", "roadworks.json"), "application/json",
                "\"rw2\"");
        Map<String, Object> params = Map.of(DigitrafficRoadAdapter.TYPES_PARAM,
                List.of("ROAD_WORK", "WEIGHT_RESTRICTION"));
        assertThat(fetch(DigitrafficRoadAdapter.ID, params).complete()).isTrue();
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo(DT_ANNOUNCEMENTS)));

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(DT_WEIGHT)).willReturn(aResponse().withStatus(304)));
        serve(DT_ROADWORKS, AdapterFixtures.bytes("digitraffic-road", "roadworks.json"), "application/json",
                "\"rw3\"");
        FetchResult mixed = fetch(DigitrafficRoadAdapter.ID, params);
        assertThat(mixed.complete()).isFalse();
        assertThat(mixed.notModified()).isFalse();
    }

    @Test
    @DisplayName("NDW: the gzip body (application/xml, no Content-Encoding) is decompressed; 304 is unchanged")
    void ndwGzip() throws Exception {
        serve(NDW, AdapterFixtures.bytes("ndw-situations", "actueel_beeld.xml.gz"), "application/xml",
                "\"5502e-65dbd2fa98755\"");
        FetchResult first = fetch("ndw-situations", Map.of());
        assertThat(first.complete()).isTrue();
        assertThat(first.records()).hasSize(995);
        WIREMOCK.verify(getRequestedFor(urlEqualTo(NDW)).withHeader("If-None-Match", absent()));

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(NDW)).withHeader("If-None-Match", equalTo("\"5502e-65dbd2fa98755\""))
                .willReturn(aResponse().withStatus(304)));
        FetchResult second = fetch("ndw-situations", Map.of());
        assertThat(second.notModified()).isTrue();
    }

    @Test
    @DisplayName("NDW: an HTML error page forgets the validators and leaves the absence state alone")
    void ndwFormatError() throws Exception {
        serve(NDW, AdapterFixtures.bytes("ndw-situations", "actueel_beeld.xml"), "application/xml", "\"a\"");
        fetch("ndw-situations", Map.of());
        var before = harness.client.stateStore("ndw-situations").get(AbsenceTombstoner.IDS_KEY);
        WIREMOCK.resetAll();
        serve(NDW, "<html><body>maintenance".getBytes(java.nio.charset.StandardCharsets.UTF_8), "text/html", "\"b\"");
        assertThatThrownBy(() -> fetch("ndw-situations", Map.of())).isInstanceOf(UpstreamFormatException.class);
        assertThat(harness.client.stateStore("ndw-situations").get(SourceHttp.ETAG_STATE_PREFIX
                + NdwSituationsAdapter.STATE_KEY)).isEmpty();
        assertThat(harness.client.stateStore("ndw-situations").get(AbsenceTombstoner.IDS_KEY)).isEqualTo(before);
    }

    @Test
    @DisplayName("WZDx 4.2 feed parsed; a 3.1 document is a format error naming the version")
    void wzdxFeed() throws Exception {
        serve(WSDOT, AdapterFixtures.bytes("wzdx", "wsdot-4.2.json"), "application/geo+json", "\"w1\"");
        FetchResult result = fetch("wzdx-wsdot", Map.of());
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).hasSize(20);

        WIREMOCK.resetAll();
        serve(WSDOT, AdapterFixtures.bytes("wzdx", "quebec-3.1.json"), "application/geo+json", "\"w2\"");
        assertThatThrownBy(() -> fetch("wzdx-wsdot", Map.of())).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("unsupported WZDx version 3.1");
    }

    @Test
    @DisplayName("WZDx key: required before any request, sent as the registry's query parameter, redacted everywhere")
    void wzdxKeyRedacted(CapturedOutput output) throws Exception {
        SourceAdapter cdot = adapter("wzdx-cdot");
        assertThat(cdot.descriptor().requiresCredentials()).isTrue();
        assertThatThrownBy(() -> fetch("wzdx-cdot", Map.of())).isInstanceOf(AdapterException.class)
                .hasMessageContaining("apiKey");
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));

        harness.credentials.put(WzdxFeed.CREDENTIAL, CDOT_KEY);
        serve(CDOT, AdapterFixtures.bytes("wzdx", "wsdot-4.2.json"), "application/json", null);
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(cdot, Map.of(), recorder);
        FetchResult result = ((WzdxAdapter) cdot).fetch(ctx);
        assertThat(result.records()).hasSize(20);
        WIREMOCK.verify(getRequestedFor(urlEqualTo(CDOT + "?apiKey=" + CDOT_KEY)));
        assertThat(ctx.traces()).isNotEmpty();
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl)
                .allSatisfy(url -> assertThat(url).doesNotContain(CDOT_KEY).contains("apiKey=***"));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(CDOT_KEY);
        }
        assertThat(output.getAll()).doesNotContain(CDOT_KEY);
    }

    @Test
    @DisplayName("a header-placed key goes in the named header and never into the URL")
    void wzdxHeaderKey() throws Exception {
        WzdxFeed feed = new WzdxFeed("hdr_test", "Header DOT", "test", "4.2", "5m",
                java.net.URI.create(baseUrl() + "/hdr.example/wzdx"), new WzdxFeed.ApiKey("header", "X-Api-Key"),
                null);
        WzdxAdapter a = new WzdxAdapter(feed, null);
        harness.reset(a.id(), SourceHttp.ETAG_STATE_PREFIX + WzdxAdapter.STATE_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + WzdxAdapter.STATE_KEY, AbsenceTombstoner.IDS_KEY,
                AbsenceTombstoner.MISSED_KEY);
        harness.credentials.put(WzdxFeed.CREDENTIAL, CDOT_KEY);
        serve("/hdr.example/wzdx", AdapterFixtures.bytes("wzdx", "wsdot-4.2.json"), "application/json", null);
        DefaultFetchContext ctx = harness.context(a, Map.of());
        a.fetch(ctx);
        WIREMOCK.verify(getRequestedFor(urlEqualTo("/hdr.example/wzdx")).withHeader("X-Api-Key", equalTo(CDOT_KEY)));
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl).allSatisfy(url ->
                assertThat(url).doesNotContain(CDOT_KEY));
    }

    @Test
    @DisplayName("an Open511 event missing from two consecutive complete fetches is tombstoned once")
    void absence() throws Exception {
        String two = """
                {"events": [
                 {"id": "drivebc.ca/A-1", "status": "ACTIVE", "event_type": "INCIDENT", "updated": "2026-10-13T10:00:00-07:00",
                  "geography": {"type": "Point", "coordinates": [-123.1, 49.2]}},
                 {"id": "drivebc.ca/A-2", "status": "ACTIVE", "event_type": "INCIDENT", "updated": "2026-10-13T10:00:00-07:00",
                  "geography": {"type": "Point", "coordinates": [-123.2, 49.3]}}], "pagination": {"offset": "0"}}""";
        String one = """
                {"events": [
                 {"id": "drivebc.ca/A-1", "status": "ACTIVE", "event_type": "INCIDENT", "updated": "2026-10-13T10:00:00-07:00",
                  "geography": {"type": "Point", "coordinates": [-123.1, 49.2]}}], "pagination": {"offset": "0"}}""";
        serve(OPEN511, two.getBytes(java.nio.charset.StandardCharsets.UTF_8), "application/json", null);
        assertThat(fetch("open511-drivebc", Map.of()).records()).noneMatch(NormalizedRecord::tombstone);
        WIREMOCK.resetAll();
        serve(OPEN511, one.getBytes(java.nio.charset.StandardCharsets.UTF_8), "application/json", null);
        assertThat(fetch("open511-drivebc", Map.of()).records()).as("one miss").noneMatch(NormalizedRecord::tombstone);
        FetchResult third = fetch("open511-drivebc", Map.of());
        assertThat(third.records()).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactly("drivebc.ca/A-2");
        assertThat(fetch("open511-drivebc", Map.of()).records()).as("only once").noneMatch(NormalizedRecord::tombstone);
    }
}
