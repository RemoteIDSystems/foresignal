package ai.foresignal.source.adapters.gdelt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.formats.attention.TopicParams;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code gdelt-timelines} against a stub (prompt 117): both modes per topic, at least 5 seconds between request
 * starts (a fake monotonic clock), the share of all articles, the 3-day timespan cap and the round-robin.
 */
@IntegrationTest
class GdeltTimelineAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-16T01:00:00Z");
    static final String PATH = "/api/v2/doc/doc";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    MutableClock clock;
    AtomicLong nanos;
    List<Long> sleeps;
    GdeltTimelineAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, clock, Budget.of(1000, 100_000));
        nanos = new AtomicLong(1_000_000_000L);
        sleeps = new ArrayList<>();
        RequestSpacing spacing = new RequestSpacing(GdeltTimelineAdapter.SPACING, nanos::get, wait -> {
            sleeps.add(wait);
            nanos.addAndGet(wait);
        });
        adapter = new GdeltTimelineAdapter(baseUrl(), spacing);
        harness.reset(adapter.id());
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", adapter.id());
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    private static Map<String, Object> topics(String... ids) {
        List<String> entries = new ArrayList<>();
        for (String id : ids) {
            entries.add(TopicParams.encode(new TopicParams.GdeltTopic(id, "\"strait of hormuz\"")));
        }
        return Map.of(TopicParams.PARAM, entries);
    }

    private static void serve() {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("mode", equalTo("TimelineVolRaw"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("gdelt-timelines", "vol.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("mode", equalTo("TimelineTone"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("gdelt-timelines", "tone.json"))));
    }

    @Test
    @DisplayName("two requests per topic, TimelineVolRaw and TimelineTone, json, no smoothing, first run 7d")
    void bothModes() throws Exception {
        serve();
        FetchResult result = adapter.fetch(harness.context(adapter, topics("strait-of-hormuz")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("mode", equalTo("TimelineVolRaw"))
                .withQueryParam("query", equalTo("\"strait of hormuz\"")).withQueryParam("format", equalTo("json"))
                .withQueryParam("timespan", equalTo("7d")).withQueryParam("timelinesmooth", equalTo("0")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("mode", equalTo("TimelineTone")));
        assertThat(result.records()).extracting(NormalizedRecord::externalId).contains(
                "gdelt.strait-of-hormuz.vol:2026-10-16T00:00:00Z", "gdelt.strait-of-hormuz.tone:2026-10-16T00:45:00Z",
                "gdelt.strait-of-hormuz.vol:latest", "gdelt.strait-of-hormuz.tone:latest");
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), result.records());
    }

    @Test
    @DisplayName("requests start at least 5 s apart, across topics")
    void spacing() throws Exception {
        serve();
        adapter.fetch(harness.context(adapter, topics("topic-one", "topic-two")));
        WIREMOCK.verify(4, getRequestedFor(urlPathEqualTo(PATH)));
        assertThat(sleeps).hasSize(3);
        // Nothing else advances the fake clock, so every wait is the full spacing.
        assertThat(sleeps).allSatisfy(wait -> assertThat(wait).isEqualTo(Duration.ofSeconds(5).toNanos()));
    }

    @Test
    @DisplayName("volume: count, extra.total and extra.share = count ÷ total; a zero-count step has no tone")
    void share() throws Exception {
        serve();
        FetchResult result = adapter.fetch(harness.context(adapter, topics("strait-of-hormuz")));
        NormalizedRecord first = result.records().stream()
                .filter(r -> r.externalId().equals("gdelt.strait-of-hormuz.vol:2026-10-16T00:00:00Z")).findFirst()
                .orElseThrow();
        @SuppressWarnings("unchecked")
        Map<String, Object> extra = (Map<String, Object>) first.payload().get("extra");
        assertThat(first.payload()).containsEntry("value", 12.0).containsEntry("frequency", "PT15M");
        assertThat(extra).containsEntry("total", 48000.0).containsEntry("share", 0.00025)
                .containsEntry("resolution", "15m");
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .doesNotContain("gdelt.strait-of-hormuz.tone:2026-10-16T00:30:00Z");
        NormalizedRecord latest = result.records().stream()
                .filter(r -> r.externalId().equals("gdelt.strait-of-hormuz.tone:latest")).findFirst().orElseThrow();
        assertThat(latest.payload()).containsEntry("family", "tone").containsEntry("topicId", "strait-of-hormuz")
                .containsEntry("releaseLagDays", 0.0);
    }

    @Test
    @DisplayName("the timespan: since the last bucket plus a step, at least 15 min, capped at 3 days")
    void timespan() throws Exception {
        assertThat(GdeltTimelineAdapter.timespan(null, NOW)).isEqualTo("7d");
        assertThat(GdeltTimelineAdapter.timespan(NOW.minusSeconds(1800), NOW)).isEqualTo("45min");
        assertThat(GdeltTimelineAdapter.timespan(NOW, NOW)).isEqualTo("15min");
        assertThat(GdeltTimelineAdapter.timespan(NOW.minus(Duration.ofDays(10)), NOW)).isEqualTo("3d");
        serve();
        adapter.fetch(harness.context(adapter, topics("strait-of-hormuz")));
        WIREMOCK.resetRequests();
        clock.setInstant(Instant.parse("2026-10-25T00:00:00Z"));
        adapter.fetch(harness.context(adapter, topics("strait-of-hormuz")));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("timespan", equalTo("3d")));
    }

    @Test
    @DisplayName("more topics than one run covers are taken round-robin")
    void roundRobin() throws Exception {
        serve();
        String[] ids = {"topic-a", "topic-b", "topic-c", "topic-d", "topic-e", "topic-f", "topic-g"};
        FetchResult first = adapter.fetch(harness.context(adapter, topics(ids)));
        assertThat(first.records()).extracting(r -> String.valueOf(r.payload().get("series")))
                .contains("gdelt.topic-a.vol", "gdelt.topic-e.vol").doesNotContain("gdelt.topic-f.vol");
        FetchResult second = adapter.fetch(harness.context(adapter, topics(ids)));
        assertThat(second.records()).extracting(r -> String.valueOf(r.payload().get("series")))
                .contains("gdelt.topic-f.vol", "gdelt.topic-g.vol", "gdelt.topic-a.vol");
    }

    @Test
    @DisplayName("a plain-text answer (a malformed query) is a format error; the resolution is inferred")
    void parsing() {
        assertThatThrownBy(() -> GdeltTimelineParser.parse("The specified phrase is too short.".getBytes(
                java.nio.charset.StandardCharsets.UTF_8))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("too short");
        GdeltTimelineParser.Timeline hourly = GdeltTimelineParser.parse(
                AdapterFixtures.bytes("gdelt-timelines", "vol-hourly.json"));
        assertThat(hourly.frequency()).isEqualTo("PT1H");
        assertThat(hourly.resolutionLabel()).isEqualTo("1h");
        assertThat(GdeltTimelineParser.parse(new byte[0]).buckets()).isEmpty();
    }

    @Test
    @DisplayName("topic entries are validated; the descriptor validates; no topics, no request")
    void params() throws Exception {
        assertThat(adapter.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of(TopicParams.PARAM,
                List.of("{\"id\":\"ok-id\",\"query\":\"(unbalanced\"}"))))).containsKey(TopicParams.PARAM);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.fetch(harness.context(adapter, Map.of())).records()).isEmpty();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(PATH)));
    }
}
