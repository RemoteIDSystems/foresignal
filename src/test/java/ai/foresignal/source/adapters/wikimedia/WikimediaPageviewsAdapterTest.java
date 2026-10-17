package ai.foresignal.source.adapters.wikimedia;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.notContaining;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.PersistentAdapterStateStore;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.formats.attention.TopicParams;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
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
 * {@code wikimedia-pageviews} against a stub (prompt 117): per-article URL encoding, {@code agent=user}, the 90-day
 * first run, the top-articles record, the User-Agent with and without the operator contact, the daily gate and the
 * 403 handling.
 */
@IntegrationTest
class WikimediaPageviewsAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-16T03:00:00Z");
    static final String PER_ARTICLE = "/api/rest_v1/metrics/pageviews/per-article/en.wikipedia/all-access/user/";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired AppSettingService settings;

    AdapterHttpHarness harness;
    MutableClock clock;
    WikimediaPageviewsAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, clock, Budget.of(1000, 100_000));
        adapter = new WikimediaPageviewsAdapter(baseUrl());
        harness.reset(adapter.id());
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", adapter.id());
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
        settings.put(SettingKeys.SERVER, ServerSettings.defaults());
    }

    private static Map<String, Object> topics(String... articles) {
        List<TopicParams.WikiArticle> list = new java.util.ArrayList<>();
        for (String article : articles) {
            list.add(new TopicParams.WikiArticle("en.wikipedia", article));
        }
        return Map.of(TopicParams.PARAM, List.of(TopicParams.encode(new TopicParams.WikiTopic("red-sea-shipping",
                list))));
    }

    private static void serveArticles() {
        WIREMOCK.stubFor(get(urlPathMatching("/api/rest_v1/metrics/pageviews/per-article/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("wikimedia-pageviews", "per-article.json"))));
    }

    private static void serveTop() {
        WIREMOCK.stubFor(get(urlPathMatching("/api/rest_v1/metrics/pageviews/top/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("wikimedia-pageviews", "top.json"))));
    }

    private AdapterStateStore state() {
        return new PersistentAdapterStateStore(adapter.id(), jdbc, jsonMapper);
    }

    @Test
    @DisplayName("first run: 90 days to yesterday, agent=user in the path, the article URL-encoded")
    void firstRun() throws Exception {
        serveArticles();
        serveTop();
        FetchResult result = adapter.fetch(harness.context(adapter, topics("Strait of Hormuz", "Côte d'Ivoire")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(PER_ARTICLE + "Strait_of_Hormuz/daily/20260718/20261015")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(PER_ARTICLE + "C%C3%B4te_d%27Ivoire/daily/20260718/20261015")));
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .contains("wiki.en.wikipedia.Strait_of_Hormuz:2026-10-05", "wiki.en.wikipedia.Strait_of_Hormuz:latest");
        NormalizedRecord latest = result.records().stream()
                .filter(r -> r.externalId().equals("wiki.en.wikipedia.Strait_of_Hormuz:latest")).findFirst()
                .orElseThrow();
        assertThat(latest.payload()).containsEntry("family", "attention").containsEntry("transform", "log-level")
                .containsEntry("releaseLagDays", 1.0).containsEntry("topicId", "red-sea-shipping")
                .containsEntry("value", 2144.0);
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), result.records());
    }

    @Test
    @DisplayName("later runs re-read from the last stored day minus 3 days")
    void reRead() throws Exception {
        serveArticles();
        serveTop();
        adapter.fetch(harness.context(adapter, topics("Strait_of_Hormuz")));
        clock.setInstant(NOW.plusSeconds(86_400));
        WIREMOCK.resetRequests();
        adapter.fetch(harness.context(adapter, topics("Strait_of_Hormuz")));
        // The fixture's newest day is 2026-10-08, so the window starts on 2026-10-05.
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(PER_ARTICLE + "Strait_of_Hormuz/daily/20261005/20261016")));
    }

    @Test
    @DisplayName("the top-articles record: attention-top, {project}:{date}, the top 100 with rank")
    void topArticles() throws Exception {
        serveArticles();
        serveTop();
        FetchResult result = adapter.fetch(harness.context(adapter, topics("Strait_of_Hormuz")));
        NormalizedRecord top = result.records().stream().filter(r -> r.kind().equals("attention-top")).findFirst()
                .orElseThrow();
        assertThat(top.externalId()).isEqualTo("en.wikipedia:2026-10-15");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(
                "/api/rest_v1/metrics/pageviews/top/en.wikipedia/all-access/2026/10/15")));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> articles = (List<Map<String, Object>>) top.payload().get("articles");
        assertThat(articles).hasSize(100);
        assertThat(articles.get(0)).containsEntry("article", "Main_Page").containsEntry("rank", 1L);
        assertThat(adapter.descriptor().kindRetention()).containsEntry("attention-top", java.time.Duration.ofDays(90));
    }

    @Test
    @DisplayName("the User-Agent is Foresignal's, with ; contact= only when server.contactEmail is set")
    void userAgent() throws Exception {
        serveArticles();
        serveTop();
        adapter.fetch(harness.context(adapter, topics("Strait_of_Hormuz")));
        WIREMOCK.verify(getRequestedFor(urlPathMatching(PER_ARTICLE + ".*"))
                .withHeader("User-Agent", containing("Foresignal/"))
                .withHeader("User-Agent", containing("+https://foresignal.ai"))
                .withHeader("User-Agent", notContaining("contact=")));

        settings.put(SettingKeys.SERVER, new ServerSettings(null, null, null, null, "ops@example.org"));
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", adapter.id());
        WIREMOCK.resetRequests();
        adapter.fetch(harness.context(adapter, topics("Strait_of_Hormuz")));
        WIREMOCK.verify(getRequestedFor(urlPathMatching(PER_ARTICLE + ".*"))
                .withHeader("User-Agent", containing("; contact=ops@example.org")));
    }

    @Test
    @DisplayName("a 403 marks the source failing with the User-Agent policy reason")
    void forbidden() {
        WIREMOCK.stubFor(get(urlPathMatching(PER_ARTICLE + ".*")).willReturn(aResponse().withStatus(403)
                .withBody("Please set a user-agent and respect our robot policy")));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, topics("Strait_of_Hormuz"))))
                .hasMessageContaining("User-Agent policy");
        assertThat(state().get(AdapterStateStore.FAILING_REASON_KEY)).hasValueSatisfying(
                reason -> assertThat(reason).contains("User-Agent policy").contains("server.contactEmail"));
    }

    @Test
    @DisplayName("an article without data (404) is skipped; before 02:30 UTC and after the day's run nothing is sent")
    void gates() throws Exception {
        WIREMOCK.stubFor(get(urlPathMatching(PER_ARTICLE + ".*")).willReturn(aResponse().withStatus(404)
                .withBody(AdapterFixtures.bytes("wikimedia-pageviews", "not-found.json"))));
        serveTop();
        FetchResult result = adapter.fetch(harness.context(adapter, topics("No_such_article_xyzzy_123")));
        assertThat(result.records()).extracting(NormalizedRecord::kind).containsExactly("attention-top");

        WIREMOCK.resetRequests();
        assertThat(adapter.fetch(harness.context(adapter, topics("No_such_article_xyzzy_123"))).records()).isEmpty();
        clock.setInstant(Instant.parse("2026-10-17T01:00:00Z"));
        assertThat(adapter.fetch(harness.context(adapter, topics("No_such_article_xyzzy_123"))).records()).isEmpty();
        WIREMOCK.verify(0, getRequestedFor(urlPathMatching("/api/.*")));
    }

    @Test
    @DisplayName("topic entries are validated by the parameter schema; the descriptor validates")
    void params() {
        assertThat(adapter.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of(TopicParams.PARAM,
                List.of("{\"id\":\"x\"}"))))).containsKey(TopicParams.PARAM);
        assertThat(adapter.checkParams(ai.foresignal.source.spi.ValidatedParams.of(topics("Strait_of_Hormuz"))))
                .isEmpty();
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(PageviewsParser.encodeArticle("A b/c")).isEqualTo("A_b%2Fc");
    }
}
