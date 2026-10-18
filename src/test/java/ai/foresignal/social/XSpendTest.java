package ai.foresignal.social;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.adapters.x.XAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.social.SocialTestSupport;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Invariant SOC-4 (prompt 135): a paid social call is estimated before it is made, refused when the monthly cap would
 * be exceeded (no request is sent), and charged with its actual cost; counts are billed per request, search per post
 * returned.
 */
@IntegrationTest
class XSpendTest extends WireMockSupport {

    @Autowired XSpend spend;
    @Autowired SocialRuntime runtime;
    @Autowired SocialPlatformRepository platforms;
    @Autowired TermIndexService terms;
    @Autowired SocialVolumeStore volume;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    MutableClock clock;
    XAdapter adapter;

    @BeforeEach
    void setUp() {
        SocialTestSupport.clean(jdbc);
        clock = new MutableClock(Instant.now().truncatedTo(ChronoUnit.HOURS).plusSeconds(600));
        harness = new AdapterHttpHarness(jdbc, json, properties, install, clock, Budget.of(1000, 100_000));
        harness.credentials.put("bearerToken", "test-bearer");
        adapter = new XAdapter(baseUrl(), runtime);
        harness.reset(adapter.id());
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", adapter.id());
        terms.install(SocialTestSupport.index(Map.of("red-sea", SocialTestSupport.block(List.of("Bab el-Mandeb"),
                List.of("RedSea")))));
    }

    private void settings(String mode, BigDecimal cap) {
        SocialPlatformEntity row = new SocialPlatformEntity(SocialVocabulary.X_ADAPTER, Instant.now());
        row.setEnabled(true);
        row.setMode(mode);
        row.setDeletionSync(false);
        row.setMonthlyCapUsd(cap);
        row.setPrices(List.of(Map.of("item", "post-read", "usd", "0.005", "per", 1),
                Map.of("item", "counts-recent-request", "usd", "0.005", "per", 1)));
        row.setPricesAsOf("2026-10-17");
        row.setOptions(Map.of("standingCounts", true, "maxResults", 10));
        platforms.save(row);
    }

    @Test
    @DisplayName("estimates: search = max_results × post read, counts = one request, the monthly counts estimate")
    void estimates() {
        SocialPlatformSettings defaults = SocialPlatformSettings.defaults(SocialPlatformCatalog.get("x").orElseThrow());
        assertThat(XSpend.estimateSearch(defaults, 100)).isEqualByComparingTo("0.5");
        assertThat(XSpend.estimateCounts(defaults, false)).isEqualByComparingTo("0.005");
        assertThat(XSpend.estimateCounts(defaults, true)).isEqualByComparingTo("0.010");
        assertThat(XSpend.standingCountsMonthly(defaults, 3)).isEqualByComparingTo("10.95");
        assertThat(defaults.monthlyCapUsd()).isEqualByComparingTo("25");
    }

    @Test
    @DisplayName("reserve refuses at the cap and counts the refusal; charges add up; the API refusal names cap and spend")
    void reserveAndCharge() throws Exception {
        settings("counts", new BigDecimal("0.01"));
        SocialPlatformSettings s = runtime.settings("x");
        spend.reserve("x", s, new BigDecimal("0.005"));
        spend.charge("x", new BigDecimal("0.008"));
        assertThat(spend.month("x").spentUsd()).isEqualByComparingTo("0.008");
        assertThatThrownBy(() -> spend.reserve("x", s, new BigDecimal("0.005")))
                .isInstanceOf(BudgetExceededException.class);
        assertThat(spend.month("x").refused()).isEqualTo(1);
        ForesignalException problem = spend.refusal("x", s, new BigDecimal("0.005"));
        assertThat(problem.getCode()).isEqualTo(ErrorCodes.BUDGET_EXCEEDED);
        assertThat(problem.getStatus().value()).isEqualTo(429);
        assertThat(problem.getProperties()).containsKeys("monthlyCapUsd", "spentThisMonth", "estimateUsd");
        assertThat(spend.refusal("x", s, BigDecimal.ZERO)).isNull();
        assertThat(spend.spentUsdThisMonth("x")).isEqualTo(0.008);
        assertThat(spend.spentUsdThisMonth("reddit")).isNull();
    }

    @Test
    @DisplayName("counts: one request per standing topic, billed per request, written as the topic's hourly X volume")
    void countsBilledPerRequest() throws Exception {
        settings("counts", new BigDecimal("25"));
        Instant hour = clock.instant().truncatedTo(ChronoUnit.HOURS).minus(java.time.Duration.ofHours(1));
        WIREMOCK.stubFor(get(urlPathEqualTo("/2/tweets/counts/recent")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"data\":[{\"start\":\"" + hour
                        + "\",\"end\":\"" + hour.plusSeconds(3600) + "\",\"post_count\":7}],"
                        + "\"meta\":{\"total_post_count\":7}}")));
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/2/tweets/counts/recent"))
                .withQueryParam("granularity", equalTo("hour")));
        assertThat(spend.month("x").spentUsd()).isEqualByComparingTo("0.005");
        assertThat(spend.month("x").calls()).isEqualTo(1);
        assertThat(volume.volume("topic", "red-sea", "x", null, hour, hour.plusSeconds(3600)))
                .singleElement().satisfies(r -> {
                    assertThat(r.posts()).isEqualTo(7);
                    assertThat(r.collectionType()).isEqualTo("complete");
                });
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/2/tweets/counts/recent")));
    }

    @Test
    @DisplayName("at the cap no request is sent and the refusal is counted")
    void refusedAtCap() throws Exception {
        settings("counts", new BigDecimal("0.004"));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).isEmpty();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/2/tweets/counts/recent")));
        assertThat(spend.month("x").refused()).isEqualTo(1);
        assertThat(spend.month("x").spentUsd()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("search: estimated at max_results × post read, charged for the posts returned, posts are a sample")
    void searchChargedPerPost() throws Exception {
        settings("search", new BigDecimal("25"));
        WIREMOCK.stubFor(get(urlPathEqualTo("/2/tweets/search/recent")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("""
                        {"data":[
                          {"id":"1","text":"Tanker hit near Bab el-Mandeb","created_at":"2026-10-16T10:00:00Z",
                           "lang":"en","author_id":"u1","public_metrics":{"repost_count":2,"reply_count":1,
                           "like_count":5,"quote_count":0,"impression_count":100},
                           "entities":{"hashtags":[{"tag":"RedSea"}]}},
                          {"id":"2","text":"#RedSea update","created_at":"2026-10-16T10:01:00Z","author_id":"u2"},
                          {"id":"3","text":"Bab el-Mandeb closed?","created_at":"2026-10-16T10:02:00Z",
                           "author_id":"u1","referenced_posts":[{"type":"quoted","id":"1"}]}],
                         "includes":{"users":[{"id":"u1","username":"watcher","name":"Watcher"},
                                              {"id":"u2","username":"other"}]},
                         "meta":{"result_count":3,"newest_id":"3"}}""")));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/2/tweets/search/recent"))
                .withQueryParam("max_results", equalTo("10")));
        assertThat(spend.month("x").spentUsd()).isEqualByComparingTo("0.015");
        assertThat(result.records()).hasSize(3);
        assertThat(result.records()).allSatisfy(r -> assertThat(r.payload()).containsEntry("collection", "sample"));
        assertThat(result.records().get(2).payload().get("quoteOf")).isInstanceOf(Map.class);
    }
}
