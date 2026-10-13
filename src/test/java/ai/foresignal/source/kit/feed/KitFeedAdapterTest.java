package ai.foresignal.source.kit.feed;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.kit.KitTestSpecs;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
 * {@link KitFeedAdapter} through the real upstream client against WireMock (prompt 073): conditional GET and 304,
 * the item cap, the category filter, the fixed {@code observedAt} rule, the payload and descriptor, format errors
 * forgetting the validators, and no tombstones ever.
 */
@IntegrationTest
class KitFeedAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T18:00:00Z");
    static final String ID = "feed-test-adapter";
    static final String PATH = "/bulletins.example/feed.xml";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    SimpleMeterRegistry meters;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, SourceHttp.ETAG_STATE_PREFIX + KitFeedAdapter.STATE_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + KitFeedAdapter.STATE_KEY, KitFeedAdapter.LAST_POLL_KEY);
        meters = new SimpleMeterRegistry();
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(ID);
    }

    KitFeedAdapter adapter(Map<String, Object> overrides) {
        return new KitFeedAdapter(KitTestSpecs.feed(ID, url(PATH), overrides), null, () -> meters);
    }

    DefaultFetchContext ctx(KitFeedAdapter adapter) {
        return harness.context(adapter, Map.of());
    }

    static void serve(String fixture, String etag) {
        WIREMOCK.stubFor(get(urlEqualTo(PATH)).withHeader("If-None-Match", absent()).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/rss+xml; charset=utf-8")
                .withHeader("ETag", etag).withBody(AdapterFixtures.bytes("kit-feeds", fixture))));
    }

    @Test
    @DisplayName("conditional GET: the ETag is stored and sent; a 304 is notModified; Foresignal's User-Agent")
    void conditionalGet() throws Exception {
        KitFeedAdapter adapter = adapter(Map.of());
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        serve("ecdc-1307.xml", "\"v1\"");
        FetchResult first = adapter.fetch(ctx(adapter));
        assertThat(first.records()).hasSize(10);
        assertThat(first.complete()).as("feeds are windows").isFalse();
        WIREMOCK.verify(getRequestedFor(urlEqualTo(PATH)).withHeader("User-Agent", equalTo(install.userAgent())));

        WIREMOCK.stubFor(get(urlEqualTo(PATH)).withHeader("If-None-Match", equalTo("\"v1\""))
                .willReturn(aResponse().withStatus(304)));
        FetchResult second = adapter.fetch(ctx(adapter));
        assertThat(second.notModified()).isTrue();
        assertThat(ctx(adapter).state().get(KitFeedAdapter.LAST_POLL_KEY)).contains(NOW.toString());
    }

    @Test
    @DisplayName("the item cap keeps the first maxItems items in document order")
    void itemCap() throws Exception {
        KitFeedAdapter adapter = adapter(Map.of("maxItems", 3));
        serve("ecdc-1307.xml", "\"v2\"");
        List<NormalizedRecord> records = adapter.fetch(ctx(adapter)).records();
        assertThat(records).hasSize(3);
        assertThat(records.get(0).externalId())
                .isEqualTo("https://www.ecdc.europa.eu/en/ebola-outbreak-democratic-republic-congo-and-uganda");
        assertThat(meters.find(KitFeedAdapter.SKIPPED_METRIC).tag("reason", FeedRecords.SKIP_OVER_CAP).counter()
                .count()).isEqualTo(7.0);
    }

    @Test
    @DisplayName("the category filter: include keeps only matching items, exclude drops them, case-insensitively")
    void categoryFilter() throws Exception {
        serve("georss-simple.xml", "\"v3\"");
        KitFeedAdapter include = adapter(Map.of("categories", Map.of("include", List.of("A"))));
        assertThat(include.fetch(ctx(include)).records()).extracting(NormalizedRecord::externalId)
                .containsExactly("g-point");
        harness.reset(ID, SourceHttp.ETAG_STATE_PREFIX + KitFeedAdapter.STATE_KEY);
        KitFeedAdapter exclude = adapter(Map.of("categories", Map.of("exclude", List.of("b"))));
        assertThat(exclude.fetch(ctx(exclude)).records()).extracting(NormalizedRecord::externalId)
                .containsExactly("g-line", "g-polygon", "g-box", "g-bad", "g-encoded");
    }

    @Test
    @DisplayName("records: payload allowlist, eventTime = published, observedAt per spec, items without it skipped")
    void records() throws Exception {
        serve("odd-dates.xml", "\"v4\"");
        KitFeedAdapter published = adapter(Map.of("observedAt", "published"));
        assertThat(published.descriptor().observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        List<NormalizedRecord> records = published.fetch(ctx(published)).records();
        assertThat(records).extracting(NormalizedRecord::externalId).doesNotContain("d-4").hasSize(4);
        NormalizedRecord first = records.get(0);
        assertThat(first.kind()).isEqualTo("bulletin");
        assertThat(first.eventTime()).isEqualTo(Instant.parse("2026-10-13T17:17:38Z"));
        assertThat(first.observedAt()).isEqualTo(first.eventTime());
        assertThat(first.payload()).containsEntry("title", "Two-digit year EDT")
                .containsEntry("published", "2026-10-13T17:17:38Z").containsEntry("feedTitle", "Odd dates")
                .doesNotContainKeys("summary", "updated", "categories", "authors", "enclosures");
        assertThat(records).noneMatch(NormalizedRecord::tombstone);
        assertThat(meters.find(KitFeedAdapter.SKIPPED_METRIC).tag("reason", FeedRecords.SKIP_MISSING_TIME).counter()
                .count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("descriptor: STANDARD, ALL, TTL 30 d on receipt, RECEIVED by default, embed title and summary")
    void descriptor() {
        var d = adapter(Map.of()).descriptor();
        assertThat(d.recordTtl()).hasDays(30);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.embedFields()).containsExactly("title", "summary");
        assertThat(d.searchFields()).containsExactly("title", "summary", "categories");
        assertThat(d.titleField()).isEqualTo("title");
        assertThat(d.defaultInterval()).hasMinutes(15);
        assertThat(d.kinds()).containsExactly("bulletin");
    }

    @Test
    @DisplayName("an HTML page or an error status is a format error and forgets the stored validators")
    void formatErrors() {
        KitFeedAdapter adapter = adapter(Map.of());
        WIREMOCK.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/html").withHeader("ETag", "\"bad\"")
                .withBody("<html><body>Maintenance</body></html>")));
        assertThatThrownBy(() -> adapter.fetch(ctx(adapter))).isInstanceOf(UpstreamFormatException.class);
        assertThat(ctx(adapter).state().get(SourceHttp.ETAG_STATE_PREFIX + KitFeedAdapter.STATE_KEY)).isEmpty();
    }
}
