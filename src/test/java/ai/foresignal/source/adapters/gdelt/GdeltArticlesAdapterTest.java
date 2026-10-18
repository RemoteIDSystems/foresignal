package ai.foresignal.source.adapters.gdelt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.formats.attention.TopicParams;
import ai.foresignal.source.formats.news.ArticleUrls;
import ai.foresignal.source.formats.news.NewsArticles;
import ai.foresignal.source.formats.news.NewsOutletLookup;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code gdelt-articles} against a stub (prompt 133): ArtList parsing (a live capture and a synthetic file), URL
 * canonicalization and the SHA-256 external id, topics merged per article, the outlet lookup, host pacing shared with
 * {@code gdelt-timelines} on one {@code UpstreamClient}, and no request other than the ArtList call (no article body
 * or image is ever fetched).
 */
@IntegrationTest
@Timeout(value = 90, unit = TimeUnit.SECONDS)
class GdeltArticlesAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-16T13:00:00Z");
    static final String PATH = "/api/v2/doc/doc";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    MutableClock clock;
    GdeltArticlesAdapter adapter;
    final List<String> lookups = new ArrayList<>();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        clock = new MutableClock(NOW);
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, clock, Budget.of(1000, 100_000));
        lookups.clear();
        NewsOutletLookup lookup = (domain, country, language) -> {
            lookups.add(domain + "|" + country + "|" + language);
            return "outlet-" + domain;
        };
        ObjectProvider<NewsOutletLookup> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(lookup);
        adapter = new GdeltArticlesAdapter(baseUrl(), provider);
        harness.reset(adapter.id());
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", adapter.id());
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
        harness.backoff.onSuccess(GdeltTimelineAdapter.ID);
    }

    private static Map<String, Object> topics(String... ids) {
        List<String> entries = new ArrayList<>();
        for (String id : ids) {
            entries.add(TopicParams.encode(new TopicParams.GdeltTopic(id, "\"strait of hormuz\"")));
        }
        return Map.of(TopicParams.PARAM, entries);
    }

    private static void serve(String fixture) {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("mode", equalTo("ArtList"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("gdelt-articles", fixture))));
    }

    @Test
    @DisplayName("one ArtList request per topic: json, 250 records, newest first, first run one day")
    void request() throws Exception {
        serve("artlist.json");
        adapter.fetch(harness.context(adapter, topics("hormuz")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("mode", equalTo("ArtList"))
                .withQueryParam("format", equalTo("json")).withQueryParam("maxrecords", equalTo("250"))
                .withQueryParam("sort", equalTo("DateDesc")).withQueryParam("timespan", equalTo("1d"))
                .withQueryParam("query", equalTo("\"strait of hormuz\"")));
        clock.setInstant(NOW.plus(Duration.ofMinutes(40)));
        adapter.fetch(harness.context(adapter, topics("hormuz")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("timespan", equalTo("40min")));
        assertThat(GdeltArticlesAdapter.timespan(NOW, NOW.plusSeconds(60))).isEqualTo("15min");
        assertThat(GdeltArticlesAdapter.timespan(NOW, NOW.plus(Duration.ofDays(3)))).isEqualTo("1d");
    }

    @Test
    @DisplayName("parsing: canonical URLs, SHA-256 ids, domains, ISO country and language, unusable entries skipped")
    void parsing() throws Exception {
        serve("artlist.json");
        FetchResult result = adapter.fetch(harness.context(adapter, topics("hormuz")));
        assertThat(result.records()).hasSize(3);
        NormalizedRecord ap = result.records().get(0);
        assertThat(ap.kind()).isEqualTo(NewsArticles.KIND);
        assertThat(ap.payload()).containsEntry("url", "https://apnews.com/article/hormuz-tanker-seized-1a2b3c")
                .containsEntry("domain", "apnews.com").containsEntry("language", "en")
                .containsEntry("sourceCountry", "US").containsEntry("outletId", "outlet-apnews.com")
                .containsEntry("seenAt", "2026-10-16T12:00:00Z").containsEntry("imageUrl",
                        "https://apnews.com/img/tanker.jpg");
        assertThat(ap.externalId()).isEqualTo(ArticleUrls.sha256(
                "https://apnews.com/article/hormuz-tanker-seized-1a2b3c"));
        assertThat(ap.observedAt()).isEqualTo(Instant.parse("2026-10-16T12:00:00Z"));
        NormalizedRecord times = result.records().get(1);
        assertThat(times.payload()).containsEntry("url",
                "https://www.example-times.com/world/2026/10/16/iran-seizes-tanker.html")
                .containsEntry("domain", "example-times.com");
        NormalizedRecord herald = result.records().get(2);
        assertThat(herald.payload()).containsEntry("url", "https://www.example-herald.co.uk/news/tanker?id=42")
                .containsEntry("sourceCountry", "GB");
        assertThat(result.records()).allSatisfy(r -> assertThat(r.payload()).doesNotContainKeys("body", "authors",
                "author", "byline"));
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), result.records());
        assertThat(lookups).contains("apnews.com|US|en", "example-herald.co.uk|GB|en");
    }

    @Test
    @DisplayName("the live capture parses: Russia and India map to RU and IN")
    void liveCapture() throws Exception {
        List<GdeltArtListParser.Entry> entries = GdeltArtListParser.parse(AdapterFixtures.bytes("gdelt-articles",
                "artlist-live.json"));
        assertThat(entries).hasSize(3);
        assertThat(entries).extracting(GdeltArtListParser.Entry::sourceCountry).containsExactly("RU", "IN", "IN");
        assertThat(entries).extracting(GdeltArtListParser.Entry::language).containsOnly("en");
        assertThat(entries.get(0).seenAt()).isEqualTo(Instant.parse("2026-10-18T01:30:00Z"));
        assertThat(GdeltArtListParser.parse("{}".getBytes())).isEmpty();
        assertThatThrownBy(() -> GdeltArtListParser.parse("Please limit requests to one every 5 seconds".getBytes()))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("an article found for two topics is one record with both topics")
    void topicsMerged() throws Exception {
        serve("artlist.json");
        FetchResult result = adapter.fetch(harness.context(adapter, topics("hormuz", "tankers")));
        assertThat(result.records()).hasSize(3);
        assertThat(result.records().get(0).payload().get("topicIds")).isEqualTo(List.of("hormuz", "tankers"));
    }

    @Test
    @DisplayName("an HTTP error or a text body is an upstream format failure")
    void failures() {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/plain").withBody("Your search contained no valid terms.")));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, topics("hormuz"))))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("host pacing: both GDELT sources declare 5 s and share one gate, so their requests start 5 s apart")
    void sharedPacing() throws Exception {
        assertThat(adapter.descriptor().hostPacing()).isEqualTo(Duration.ofSeconds(5));
        GdeltTimelineAdapter timelines = new GdeltTimelineAdapter(baseUrl(), RequestSpacing.system(Duration.ZERO));
        assertThat(timelines.descriptor().hostPacing()).isEqualTo(adapter.descriptor().hostPacing());
        harness.reset(timelines.id());
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", timelines.id());
        serve("artlist.json");
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("mode", equalTo("TimelineVolRaw"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("gdelt-timelines", "vol.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("mode", equalTo("TimelineTone"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("gdelt-timelines", "tone.json"))));
        adapter.fetch(harness.context(adapter, topics("hormuz")));
        timelines.fetch(harness.context(timelines, topics("hormuz")));
        List<Long> starts = WIREMOCK.getAllServeEvents().stream().map(ServeEvent::getRequest)
                .map(r -> r.getLoggedDate().getTime()).sorted(Comparator.naturalOrder()).toList();
        assertThat(starts).hasSize(3);
        for (int i = 1; i < starts.size(); i++) {
            assertThat(starts.get(i) - starts.get(i - 1)).as("gap %d", i).isGreaterThanOrEqualTo(4_900L);
        }
        assertThat(WIREMOCK.getAllServeEvents()).allSatisfy(e -> assertThat(e.getRequest().getUrl())
                .startsWith(PATH));
    }
}
