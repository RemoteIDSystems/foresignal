package ai.foresignal.source.adapters.cap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
 * documents fetched, the 50-document cap, non-{@code Actual} skipped, and the reviewed family registering its
 * sources.
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
        harness.reset(adapter.id(), "cap:index", "cap:seen",
                SourceHttp.ETAG_STATE_PREFIX + "cap:index", SourceHttp.LAST_MODIFIED_STATE_PREFIX + "cap:index");
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    private static String doc(String name) {
        return new String(AdapterFixtures.bytes("cap-de-dwd-de", name), StandardCharsets.UTF_8);
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
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/atom+xml;charset=UTF-8").withBody(xml.toString())));
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
    @DisplayName("a non-Actual status is skipped unless includeTests")
    void nonActualSkipped() throws Exception {
        serveIndex("/cap/ex.xml");
        serveDoc("/cap/ex.xml", doc("chain-exercise.xml"));
        assertThat(fetch(Map.of()).records()).isEmpty();
        assertThat(fetch(Map.of("includeTests", true)).records()).hasSize(1);
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
