package ai.foresignal.source.adapters.cyber;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
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
import ai.foresignal.source.adapters.abusech.FeodoAdapter;
import ai.foresignal.source.adapters.abusech.UrlhausAdapter;
import ai.foresignal.source.adapters.cisa.CisaKevAdapter;
import ai.foresignal.source.adapters.ct.CtDomainWatchAdapter;
import ai.foresignal.source.adapters.nvd.CveLedger;
import ai.foresignal.source.adapters.nvd.NvdCvesAdapter;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The prompt 076 adapters against stubbed upstreams through the real upstream client: KEV and Feodo conditional GET
 * (and KEV absence tombstones), the URLhaus {@code Auth-Key} header redacted everywhere and a 401, the NVD
 * {@code apiKey} header, the pause between requests without a key and {@code startIndex} paging, EPSS batches of at
 * most 100 ids, and Cert Spotter paging with {@code after}, round-robin domains and a {@code Retry-After} honored.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class CyberAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T20:00:00Z");
    static final String KEV = "/www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json";
    static final String FEODO = "/feodotracker.abuse.ch/downloads/ipblocklist.json";
    static final String FEODO_RECOMMENDED = "/feodotracker.abuse.ch/downloads/ipblocklist_recommended.json";
    static final String URLHAUS = "/urlhaus-api.abuse.ch/v1/urls/recent/limit/1000/";
    static final String NVD = "/services.nvd.nist.gov/rest/json/cves/2.0";
    static final String EPSS = "/api.first.org/data/v1/epss";
    static final String CT = "/api.certspotter.com/v1/issuances";
    static final String URLHAUS_KEY = "uhKey0123456789abcdefSECRET";
    static final String NVD_KEY = "nvd-0123-4567-89ab-cdefSECRET";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    List<Duration> pauses;
    CisaKevAdapter kev;
    FeodoAdapter feodo;
    UrlhausAdapter urlhaus;
    NvdCvesAdapter nvd;
    CtDomainWatchAdapter ct;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        pauses = new ArrayList<>();
        kev = new CisaKevAdapter(baseUrl());
        feodo = new FeodoAdapter(baseUrl(), new CountryCentroids());
        urlhaus = new UrlhausAdapter(baseUrl());
        nvd = new NvdCvesAdapter(baseUrl(), Duration.ofSeconds(6), pauses::add);
        ct = new CtDomainWatchAdapter(baseUrl(), Duration.ZERO, d -> { });
        harness.reset(kev.id(), SourceHttp.ETAG_STATE_PREFIX + CisaKevAdapter.STATE_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + CisaKevAdapter.STATE_KEY, AbsenceTombstoner.IDS_KEY,
                AbsenceTombstoner.MISSED_KEY);
        harness.reset(feodo.id(), SourceHttp.ETAG_STATE_PREFIX + FeodoAdapter.stateKey("standard"),
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + FeodoAdapter.stateKey("standard"),
                SourceHttp.ETAG_STATE_PREFIX + FeodoAdapter.stateKey("recommended"),
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + FeodoAdapter.stateKey("recommended"),
                AbsenceTombstoner.IDS_KEY, AbsenceTombstoner.MISSED_KEY);
        harness.reset(urlhaus.id());
        harness.reset(nvd.id(), NvdCvesAdapter.CURSOR_KEY, NvdCvesAdapter.WINDOW_KEY, CveLedger.STATE_KEY,
                NvdCvesAdapter.REFRESHED_ON_KEY, NvdCvesAdapter.REFRESH_FROM_KEY);
        harness.reset(ct.id(), CtDomainWatchAdapter.CURSOR_PREFIX + "example.com",
                CtDomainWatchAdapter.CURSOR_PREFIX + "example.org", CtDomainWatchAdapter.ROTATION_KEY);
        harness.credentials.clear();
    }

    @AfterEach
    void tearDown() {
        for (String id : List.of(kev.id(), feodo.id(), urlhaus.id(), nvd.id(), ct.id())) {
            harness.backoff.onSuccess(id);
        }
    }

    static void serve(String path, byte[] body, String etag) {
        var response = aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body);
        if (etag != null) {
            response = response.withHeader("ETag", etag);
        }
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(response));
    }

    @Test
    @DisplayName("KEV: a conditional GET; the ETag comes back and a 304 is unchanged; an entry missing twice is"
            + " tombstoned")
    void kevConditionalAndAbsence() throws Exception {
        serve(KEV, AdapterFixtures.bytes("cisa-kev", "catalog.json"), "\"k1\"");
        FetchResult first = kev.fetch(harness.context(kev, Map.of()));
        assertThat(first.complete()).isTrue();
        assertThat(first.records()).hasSize(25).noneMatch(NormalizedRecord::tombstone);

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(KEV)).withHeader("If-None-Match", equalTo("\"k1\""))
                .willReturn(aResponse().withStatus(304)));
        assertThat(kev.fetch(harness.context(kev, Map.of())).notModified()).isTrue();

        WIREMOCK.resetAll();
        ObjectNode fewer = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("cisa-kev", "catalog.json"));
        ((ArrayNode) fewer.get("vulnerabilities")).remove(0);
        serve(KEV, JSON.writeValueAsBytes(fewer), "\"k2\"");
        assertThat(kev.fetch(harness.context(kev, Map.of())).records()).noneMatch(NormalizedRecord::tombstone);
        WIREMOCK.resetAll();
        serve(KEV, JSON.writeValueAsBytes(fewer), "\"k3\"");
        assertThat(kev.fetch(harness.context(kev, Map.of())).records()).filteredOn(NormalizedRecord::tombstone)
                .extracting(NormalizedRecord::externalId).containsExactly("CVE-2026-104935");
    }

    @Test
    @DisplayName("Feodo: the list parameter picks the file; a conditional GET per list; an HTML body is a format error")
    void feodoConditional() throws Exception {
        serve(FEODO_RECOMMENDED, AdapterFixtures.bytes("abusech-feodo", "ipblocklist_recommended.json"), "\"r1\"");
        FetchResult first = feodo.fetch(harness.context(feodo, Map.of("list", "recommended")));
        assertThat(first.records()).extracting(NormalizedRecord::externalId).containsExactly("50.16.16.211:443");
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(FEODO)));

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(FEODO_RECOMMENDED)).withHeader("If-None-Match", equalTo("\"r1\""))
                .willReturn(aResponse().withStatus(304)));
        assertThat(feodo.fetch(harness.context(feodo, Map.of("list", "recommended"))).notModified()).isTrue();

        WIREMOCK.resetAll();
        serve(FEODO, AdapterFixtures.bytes("abusech-feodo", "empty.json"), "\"s1\"");
        FetchResult empty = feodo.fetch(harness.context(feodo, Map.of()));
        assertThat(empty.records()).isEmpty();
        assertThat(empty.complete()).isTrue();

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(FEODO)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/html").withBody("<html>maintenance</html>")));
        assertThatThrownBy(() -> feodo.fetch(harness.context(feodo, Map.of())))
                .isInstanceOf(ai.foresignal.source.spi.UpstreamFormatException.class);
    }

    @Test
    @DisplayName("URLhaus: no key, no request; the Auth-Key header is sent and redacted everywhere; 401 is an HTTP"
            + " error")
    void urlhausKey(CapturedOutput output) throws Exception {
        assertThatThrownBy(() -> urlhaus.fetch(harness.context(urlhaus, Map.of())))
                .isInstanceOf(AdapterException.class).hasMessageContaining("authKey");
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));

        harness.credentials.put(UrlhausAdapter.AUTH_KEY, URLHAUS_KEY);
        serve(URLHAUS, AdapterFixtures.bytes("abusech-urlhaus", "recent.json"), null);
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(urlhaus, Map.of(), recorder);
        FetchResult result = urlhaus.fetch(ctx);
        assertThat(result.records()).hasSize(3);
        assertThat(result.complete()).as("a window").isFalse();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(URLHAUS)).withHeader("Auth-Key", equalTo(URLHAUS_KEY))
                .withHeader("User-Agent", matching("Foresignal/.*")));
        assertThat(ctx.traces()).isNotEmpty().extracting(FetchTrace::redactedUrl)
                .allSatisfy(url -> assertThat(url).doesNotContain(URLHAUS_KEY));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(URLHAUS_KEY);
        }
        assertThat(output.getAll()).doesNotContain(URLHAUS_KEY);

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(URLHAUS)).willReturn(aResponse().withStatus(401)
                .withHeader("Content-Type", "application/json").withBody("{\"error\": \"Unauthorized\"}")));
        assertThatThrownBy(() -> urlhaus.fetch(harness.context(urlhaus, Map.of())))
                .isInstanceOfSatisfying(UpstreamHttpException.class, e -> assertThat(e.getStatus()).isEqualTo(401));
    }

    /** An NVD page of {@code total} results holding the fixture's first {@code n} CVEs. */
    static byte[] nvdPage(int startIndex, int total, int n) {
        ObjectNode root = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("nvd-cves", "cves-page.json"));
        ArrayNode vulns = (ArrayNode) root.get("vulnerabilities");
        while (vulns.size() > n) {
            vulns.remove(vulns.size() - 1);
        }
        root.put("startIndex", startIndex).put("totalResults", total).put("resultsPerPage", n);
        return JSON.writeValueAsBytes(root);
    }

    void serveNvdPages() {
        WIREMOCK.stubFor(get(urlPathEqualTo(NVD)).withQueryParam("startIndex", equalTo("0"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(nvdPage(0, 4, 2))));
        WIREMOCK.stubFor(get(urlPathEqualTo(NVD)).withQueryParam("startIndex", equalTo("2"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(nvdPage(2, 4, 2))));
        serve(EPSS, AdapterFixtures.bytes("nvd-cves", "epss.json"), null);
    }

    @Test
    @DisplayName("NVD without a key: startIndex paging, the window parameters, 6 s between requests, no apiKey header")
    void nvdPagingAndPause() throws Exception {
        serveNvdPages();
        FetchResult result = nvd.fetch(harness.context(nvd, Map.of()));
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("CVE-2025-8679");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(NVD))
                .withQueryParam("lastModStartDate", equalTo("2026-10-06T20:00:00.000Z"))
                .withQueryParam("lastModEndDate", equalTo("2026-10-13T20:00:00.000Z"))
                .withQueryParam("resultsPerPage", equalTo("2000"))
                .withQueryParam("startIndex", equalTo("0"))
                .withHeader("apiKey", absent()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(NVD)).withQueryParam("startIndex", equalTo("2")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(EPSS)).withQueryParam("cve", equalTo("CVE-2025-8679")));
        assertThat(pauses).as("before the second page and before the EPSS call").containsExactly(
                Duration.ofSeconds(6), Duration.ofSeconds(6));
        assertThat(harness.client.stateStore(nvd.id()).get(NvdCvesAdapter.CURSOR_KEY)).contains(NOW.toString());
        assertThat(harness.client.stateStore(nvd.id()).get(NvdCvesAdapter.REFRESHED_ON_KEY)).contains("2026-10-13");
    }

    @Test
    @DisplayName("NVD with a key: the apiKey header, no pause between NVD pages, the key never in a trace")
    void nvdApiKey(CapturedOutput output) throws Exception {
        harness.credentials.put(NvdCvesAdapter.API_KEY, NVD_KEY);
        serveNvdPages();
        DefaultFetchContext ctx = harness.context(nvd, Map.of(), new RequestRecorder());
        nvd.fetch(ctx);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(NVD)).withHeader("apiKey", equalTo(NVD_KEY)));
        assertThat(pauses).as("only the EPSS call is paced").containsExactly(Duration.ofSeconds(6));
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl).allSatisfy(url ->
                assertThat(url).doesNotContain(NVD_KEY));
        assertThat(output.getAll()).doesNotContain(NVD_KEY);
    }

    @Test
    @DisplayName("EPSS: 150 kept CVEs are scored in two requests of at most 100 ids, each URL under 2,000 characters")
    void epssBatching() throws Exception {
        StringBuilder vulns = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            vulns.append(i == 0 ? "" : ",").append("""
                    {"cve": {"id": "CVE-2026-%d", "vulnStatus": "Analyzed", "published": "2026-10-01T00:00:00.000",
                     "lastModified": "2026-10-12T00:00:00.000", "descriptions": [{"lang": "en", "value": "test"}],
                     "metrics": {"cvssMetricV31": [{"type": "Primary", "cvssData": {"version": "3.1",
                     "baseScore": 9.0, "baseSeverity": "CRITICAL", "vectorString": "CVSS:3.1/AV:N"}}]}}}"""
                    .formatted(100_000 + i));
        }
        String page = "{\"resultsPerPage\": 150, \"startIndex\": 0, \"totalResults\": 150, \"vulnerabilities\": ["
                + vulns + "]}";
        serve(NVD, page.getBytes(StandardCharsets.UTF_8), null);
        serve(EPSS, "{\"status\": \"OK\", \"data\": []}".getBytes(StandardCharsets.UTF_8), null);
        FetchResult result = nvd.fetch(harness.context(nvd, Map.of()));
        assertThat(result.records()).hasSize(150);
        List<LoggedRequest> epss = WIREMOCK.findAll(getRequestedFor(urlPathEqualTo(EPSS)));
        assertThat(epss).hasSize(2);
        assertThat(epss.get(0).queryParameter("cve").firstValue().split(",")).hasSize(100);
        assertThat(epss.get(1).queryParameter("cve").firstValue().split(",")).hasSize(50);
        assertThat(NvdCvesAdapter.epssBatches(java.util.stream.IntStream.range(0, 1000)
                .mapToObj(i -> "CVE-2026-" + (1_000_000 + i)).toList())).allSatisfy(batch -> {
                    assertThat(batch).hasSizeLessThanOrEqualTo(100);
                    assertThat((NvdCvesAdapter.EPSS_API + "?cve=" + String.join(",", batch)).length())
                            .isLessThan(NvdCvesAdapter.MAX_URL_LENGTH);
                });
    }

    @Test
    @DisplayName("Cert Spotter: pages with after until an empty array; a rate_limited 429 honors Retry-After and keeps"
            + " the records and cursors; the next run starts at the throttled domain")
    void certSpotterPagingAndRetryAfter() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(CT)).withQueryParam("domain", equalTo("example.com"))
                .withQueryParam("after", absent()).willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("ct-domain-watch", "issuances.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(CT)).withQueryParam("domain", equalTo("example.com"))
                .withQueryParam("after", equalTo("17433446883")).willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json").withHeader("Retry-After", "3600")
                        .withBody("[]")));
        WIREMOCK.stubFor(get(urlPathEqualTo(CT)).withQueryParam("domain", equalTo("example.org"))
                .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "120")
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\": \"rate_limited\", \"message\": \"Rate limit exceeded\"}")));
        Map<String, Object> params = Map.of("domains", List.of("example.com", "Example.org"),
                "expectedIssuers", List.of("sectigo.com"));
        FetchResult result = ct.fetch(harness.context(ct, params));
        assertThat(result.records()).hasSize(13);
        assertThat(result.records()).filteredOn(r -> Boolean.TRUE.equals(r.payload().get("unexpectedIssuer")))
                .hasSize(8);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(CT)).withQueryParam("domain", equalTo("example.com"))
                .withQueryParam("include_subdomains", equalTo("true"))
                .withQueryParam("match_wildcards", equalTo("true"))
                .withQueryParam("expand", equalTo("issuer.caa_domains"))
                .withHeader("Authorization", absent()));
        var state = harness.client.stateStore(ct.id());
        assertThat(state.get(CtDomainWatchAdapter.CURSOR_PREFIX + "example.com")).contains("17433446883");
        assertThat(state.get(CtDomainWatchAdapter.CURSOR_PREFIX + "example.org")).isEmpty();
        assertThat(state.get(CtDomainWatchAdapter.ROTATION_KEY)).contains("1");
        assertThat(harness.backoff.isInBackoff(ct.id())).isTrue();
        assertThat(harness.backoff.backoffUntil(ct.id())).isEqualTo(NOW.plusSeconds(120));
        assertThatThrownBy(() -> ct.fetch(harness.context(ct, params))).as("in backoff: nothing is sent")
                .isInstanceOf(AdapterException.class);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(CT)).withQueryParam("domain", equalTo("example.org")));
    }

    @Test
    @DisplayName("Cert Spotter: the API key goes in Authorization: Bearer and is redacted")
    void certSpotterKey() throws Exception {
        harness.credentials.put(CtDomainWatchAdapter.API_KEY, "cs_0123456789abcdefSECRET");
        WIREMOCK.stubFor(get(urlPathEqualTo(CT)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("[]")));
        RequestRecorder recorder = new RequestRecorder();
        ct.fetch(harness.context(ct, Map.of("domains", List.of("example.com")), recorder));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(CT)).withHeader("Authorization",
                equalTo("Bearer cs_0123456789abcdefSECRET")));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain("cs_0123456789abcdefSECRET");
        }
        assertThat(harness.client.stateStore(ct.id()).get(CtDomainWatchAdapter.ROTATION_KEY)).contains("0");
    }
}
