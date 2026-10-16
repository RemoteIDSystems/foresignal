package ai.foresignal.source.adapters.cap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
 * {@link CapFeedAdapter} and {@link CapFeedAdapterFamily} (prompt 089): a conditional index GET, only new CAP
 * documents fetched, the per-run document cap (the budget less the index) and a clean stop on a budget refusal, a
 * 304 index, refused off-host links, non-{@code Actual} skipped (and read once {@code includeTests} is on), and the
 * reviewed family registering its sources.
 */
@IntegrationTest
class CapFeedAdapterFamilyTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final String PATH = "/cap/index.xml";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    CapFeedAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        CapFeed feed = new CapFeed("cap-test-xx", "XX", "Test Authority", URI.create(baseUrl() + PATH),
                "atom-index", List.of("en-GB"), "test licence", URI.create("https://example.org/terms"),
                "2026-10-14", "test feed");
        adapter = new CapFeedAdapter(feed);
        reset();
    }

    private void reset() {
        harness.reset(adapter.id(), "cap:index", "cap:seen", "cap:skippedTests", "cap:includeTests",
                SourceHttp.ETAG_STATE_PREFIX + "cap:index", SourceHttp.LAST_MODIFIED_STATE_PREFIX + "cap:index");
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    private static String doc(String name) {
        return new String(AdapterFixtures.bytes("cap-de-dwd", name), StandardCharsets.UTF_8);
    }

    private void serveIndex(String... entries) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?><feed xmlns=\"http://www.w3.org/2005/Atom\">")
                .append("<title>test</title>");
        int i = 0;
        for (String entry : entries) {
            xml.append("<entry><title>doc").append(i++).append("</title><id>doc").append(i)
                    .append("</id><updated>2026-10-14T10:00:00Z</updated><link href=\"")
                    .append(baseUrl()).append(entry).append("\"/></entry>");
        }
        xml.append("</feed>");
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200).withHeader("ETag", "\"v1\"")
                .withHeader("Content-Type", "application/atom+xml;charset=UTF-8").withBody(xml.toString())));
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withHeader("If-None-Match", equalTo("\"v1\""))
                .atPriority(1).willReturn(aResponse().withStatus(304).withHeader("ETag", "\"v1\"")));
    }

    private void serveDoc(String path, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/cap+xml;charset=UTF-8").withBody(body)));
    }

    private FetchResult fetch(Map<String, Object> params) throws Exception {
        return adapter.fetch(harness.context(adapter, params));
    }

    @Test
    @DisplayName("only new documents are fetched; a seen document is not fetched again")
    void onlyNewDocuments() throws Exception {
        serveIndex("/cap/a.xml", "/cap/b.xml");
        serveDoc("/cap/a.xml", doc("chain-alert.xml"));
        serveDoc("/cap/b.xml", doc("chain-update.xml"));

        FetchResult first = fetch(Map.of());
        assertThat(first.records()).hasSize(2).noneMatch(NormalizedRecord::tombstone);

        FetchResult second = fetch(Map.of());
        assertThat(second.records()).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/cap/a.xml")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/cap/b.xml")));
    }

    @Test
    @DisplayName("a non-Actual status is skipped unless includeTests, and enabling includeTests later still reads it")
    void nonActualSkipped() throws Exception {
        serveIndex("/cap/ex.xml");
        serveDoc("/cap/ex.xml", doc("chain-exercise.xml"));
        assertThat(fetch(Map.of()).records()).isEmpty();
        assertThat(fetch(Map.of()).records()).as("not fetched again while includeTests is off").isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/cap/ex.xml")));
        // The skipped document was not marked seen, so turning includeTests on picks it up with no reset.
        assertThat(fetch(Map.of("includeTests", true)).records()).hasSize(1);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo("/cap/ex.xml")));
    }

    @Test
    @DisplayName("the per-run cap: 7 new documents are read 5 then 2, even when the index answers 304 the second time")
    void perRunCap() throws Exception {
        String[] paths = new String[7];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = "/cap/d" + i + ".xml";
            serveDoc(paths[i], doc("chain-alert.xml"));
        }
        serveIndex(paths);

        FetchResult first = fetch(Map.of());
        assertThat(first.records()).hasSize(CapFeedAdapter.MAX_DOCS_PER_RUN);
        assertThat(harness.client.stateStore(adapter.id()).get(SourceHttp.ETAG_STATE_PREFIX + "cap:index"))
                .as("a backlog forgets the index validators").isEmpty();

        FetchResult second = fetch(Map.of());
        assertThat(second.records()).hasSize(2);
        for (String path : paths) {
            WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(path)));
        }
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(PATH)).withHeader("If-None-Match", equalTo("\"v1\"")));
    }

    @Test
    @DisplayName("a conditional index GET: a 304 is an unchanged run and fetches no document")
    void conditionalIndex() throws Exception {
        serveDoc("/cap/a.xml", doc("chain-alert.xml"));
        String xml = "<?xml version=\"1.0\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><title>t</title><entry>"
                + "<title>a</title><id>a</id><updated>2026-10-14T10:00:00Z</updated><link href=\"" + baseUrl()
                + "/cap/a.xml\"/></entry></feed>";
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200).withHeader("ETag", "\"v1\"")
                .withHeader("Content-Type", "application/atom+xml").withBody(xml)));
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withHeader("If-None-Match", equalTo("\"v1\""))
                .atPriority(1).willReturn(aResponse().withStatus(304).withHeader("ETag", "\"v1\"")));
        assertThat(fetch(Map.of()).records()).hasSize(1);
        FetchResult second = fetch(Map.of());
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(PATH)).withHeader("If-None-Match", equalTo("\"v1\"")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/cap/a.xml")));
    }

    @Test
    @DisplayName("a document link off the feed's host is refused and never fetched")
    void offHostRefused() throws Exception {
        String xml = "<?xml version=\"1.0\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><title>t</title><entry>"
                + "<title>x</title><id>x</id><updated>2026-10-14T10:00:00Z</updated>"
                + "<link href=\"https://elsewhere.example/cap/x.xml\"/></entry></feed>";
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/atom+xml").withBody(xml)));
        assertThat(fetch(Map.of()).records()).isEmpty();
        CapFeed feed = adapter.feed();
        assertThat(feed.allowsDocument(URI.create(baseUrl() + "/cap/a.xml"))).isTrue();
        assertThat(feed.allowsDocument(URI.create("https://elsewhere.example/cap/x.xml"))).isFalse();
        assertThat(feed.allowsDocument(URI.create("https://user@" + feed.feedUrl().getHost() + "/x"))).isFalse();
        CapFeed reviewed = new CapFeed("cap-xx-test", "XX", "Test", URI.create("https://feed.example/i.xml"),
                "atom-index", List.of("en"), "l", URI.create("https://feed.example/t"), "2026-10-16", null,
                List.of("Docs.Example"));
        assertThat(reviewed.allowsDocument(URI.create("https://docs.example/a.xml"))).isTrue();
        assertThat(reviewed.allowsDocument(URI.create("http://docs.example/a.xml"))).as("scheme").isFalse();
    }

    @Test
    @DisplayName("a budget refusal stops the document fetches cleanly; the rest are read on the next run")
    void budgetStopsCleanly() throws Exception {
        AdapterHttpHarness tight = new AdapterHttpHarness(jdbc, jsonMapper, properties, install,
                new MutableClock(NOW), Budget.of(3, 1_000));
        CapFeed feed = new CapFeed("cap-budget-xx", "XX", "Budget Authority", URI.create(baseUrl() + PATH),
                "atom-index", List.of("en-GB"), "test licence", URI.create("https://example.org/terms"),
                "2026-10-14", "test feed");
        CapFeedAdapter budgeted = new CapFeedAdapter(feed);
        tight.reset(budgeted.id(), "cap:index", "cap:seen", "cap:skippedTests", "cap:includeTests",
                SourceHttp.ETAG_STATE_PREFIX + "cap:index", SourceHttp.LAST_MODIFIED_STATE_PREFIX + "cap:index");
        serveIndex("/cap/b0.xml", "/cap/b1.xml", "/cap/b2.xml", "/cap/b3.xml");
        for (int i = 0; i < 4; i++) {
            serveDoc("/cap/b" + i + ".xml", doc("chain-alert.xml"));
        }
        FetchResult first = budgeted.fetch(tight.context(budgeted, Map.of()));
        assertThat(first.records()).as("the index and two documents fit 3 a minute").hasSize(2);
        tight.clock.advance(java.time.Duration.ofMinutes(5));
        FetchResult second = budgeted.fetch(tight.context(budgeted, Map.of()));
        assertThat(second.records()).hasSize(2);
        tight.backoff.onSuccess(budgeted.id());
    }

    @Test
    @DisplayName("the reviewed family registers every committed feed and its descriptors validate")
    void family() {
        CapFeedAdapterFamily family = new CapFeedAdapterFamily("", CapFeeds.load());
        assertThat(family.adapters()).hasSizeGreaterThanOrEqualTo(10);
        family.adapters().forEach(a ->
                assertThat(DescriptorValidator.problems(a.descriptor(), a)).as(a.id()).isEmpty());
    }
}
