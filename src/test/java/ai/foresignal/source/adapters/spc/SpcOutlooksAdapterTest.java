package ai.foresignal.source.adapters.spc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.AdapterStateHealthRule;
import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.collect.HealthSnapshot;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code spc-outlooks} against a stubbed {@code www.spc.noaa.gov} (prompt 047): 17 conditional GETs,
 * the 304 path, per-file tombstones, and a 404 layer that degrades health and tombstones nothing.
 */
@IntegrationTest
class SpcOutlooksAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T00:05:00Z");
    static final String ETAG = "\"fd2-65daa04c9e660\"";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired AdapterStateHealthRule healthRule;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    SpcOutlooksAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 2_000), null);
        adapter = new SpcOutlooksAdapter(baseUrl());
    }

    private WeatherAdapterHarness.Context context() {
        SourceDescriptor d = adapter.descriptor();
        return new WeatherAdapterHarness.Context(d, upstream.client().forSource(SpcOutlooksAdapter.ID, d),
                ValidatedParams.empty(), state(), clock);
    }

    private AdapterStateStore state() {
        return upstream.client().stateStore(SpcOutlooksAdapter.ID);
    }

    private static String path(SpcOutlooksAdapter.Layer layer) {
        return "/products/outlook/" + layer.file();
    }

    /** Every layer answers 200 with its captured file and an ETag, and 304 to that ETag. */
    private static void serveAll() {
        for (SpcOutlooksAdapter.Layer layer : SpcOutlooksAdapter.LAYERS) {
            serve(layer, AdapterFixtures.bytes("spc", layer.file()));
        }
    }

    private static void serve(SpcOutlooksAdapter.Layer layer, byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path(layer))).withHeader("If-None-Match", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/geo+json")
                        .withHeader("ETag", ETAG).withBody(body)));
        WIREMOCK.stubFor(get(urlPathEqualTo(path(layer))).withHeader("If-None-Match", equalTo(ETAG))
                .willReturn(aResponse().withStatus(304).withHeader("ETag", ETAG)));
    }

    private HealthRuleContext health() {
        return new HealthRuleContext(adapter.descriptor(), HealthSnapshot.empty(SpcOutlooksAdapter.ID), NOW);
    }

    private static List<String> tombstones(FetchResult result) {
        return result.records().stream().filter(NormalizedRecord::tombstone).map(NormalizedRecord::externalId).toList();
    }

    @Test
    @DisplayName("the first run fetches 17 layers and keeps every live risk area; the second is all 304s")
    void firstRunThenNotModified() throws Exception {
        serveAll();

        FetchResult first = adapter.fetch(context());

        assertThat(first.records()).extracting(NormalizedRecord::externalId).containsExactlyInAnyOrder(
                "day1:cat:TSTM", "day2:cat:TSTM", "day2:cat:MRGL", "day2:cat:SLGT", "day2:torn:0.02", "day2:wind:0.05",
                "day2:wind:0.15", "day2:hail:0.05", "day2:hail:0.15", "day3:cat:TSTM", "day3:cat:MRGL", "day3:prob:0.05");
        WIREMOCK.verify(17, getRequestedFor(com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching(
                "/products/outlook/day[123]otlk_.*")));

        FetchResult second = adapter.fetch(context());

        assertThat(second.notModified()).isTrue();
        assertThat(second.records()).isEmpty();
        assertThat(healthRule.evaluate(health())).isEmpty();
    }

    @Test
    @DisplayName("a label absent from a re-fetched file is tombstoned for that day and type only")
    void perFileTombstones() throws Exception {
        serveAll();
        adapter.fetch(context());

        SpcOutlooksAdapter.Layer day2cat = new SpcOutlooksAdapter.Layer(2, "cat");
        String reissued = AdapterFixtures.text("spc", day2cat.file());
        // Drop the SLGT feature: keep the first two features of the three.
        var root = (tools.jackson.databind.node.ObjectNode) JsonMapper.builder().build().readTree(reissued);
        ((tools.jackson.databind.node.ArrayNode) root.get("features")).remove(2);
        WIREMOCK.resetAll();
        serveAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(path(day2cat))).atPriority(1)
                .willReturn(aResponse().withStatus(200).withHeader("ETag", "\"new\"")
                        .withBody(root.toString().getBytes(StandardCharsets.UTF_8))));

        FetchResult result = adapter.fetch(context());

        assertThat(tombstones(result)).containsExactly("day2:cat:SLGT");
        assertThat(result.records().stream().filter(r -> !r.tombstone()).map(NormalizedRecord::externalId))
                .containsExactlyInAnyOrder("day2:cat:TSTM", "day2:cat:MRGL");
    }

    @Test
    @DisplayName("a 404 layer degrades health with 'SPC layer {file} missing' and tombstones nothing")
    void missingLayer() throws Exception {
        serveAll();
        adapter.fetch(context());

        SpcOutlooksAdapter.Layer prob = new SpcOutlooksAdapter.Layer(3, "prob");
        WIREMOCK.stubFor(get(urlPathEqualTo(path(prob))).atPriority(1).willReturn(aResponse().withStatus(404)));

        FetchResult result = adapter.fetch(context());

        assertThat(tombstones(result)).isEmpty();
        var verdict = healthRule.evaluate(health()).orElseThrow();
        assertThat(verdict.status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(verdict.reason()).isEqualTo("SPC layer day3otlk_prob.lyr.geojson missing");

        // The layer comes back unchanged: the live set was kept, so nothing is tombstoned and health clears.
        WIREMOCK.resetAll();
        serveAll();
        FetchResult back = adapter.fetch(context());
        assertThat(tombstones(back)).isEmpty();
        assertThat(healthRule.evaluate(health())).isEmpty();
    }

    @Test
    @DisplayName("an unreadable layer forgets its validators, so the next run asks again unconditionally")
    void unreadableLayer() throws Exception {
        SpcOutlooksAdapter.Layer cat = new SpcOutlooksAdapter.Layer(1, "cat");
        serveAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(path(cat))).atPriority(1)
                .willReturn(aResponse().withStatus(200).withHeader("ETag", ETAG).withBody("<html>maintenance</html>")));

        adapter.fetch(context());

        assertThat(state().get("etag:" + "spc:day1:cat")).isEmpty();
        assertThat(healthRule.evaluate(health()).orElseThrow().reason()).contains("day1otlk_cat.lyr.geojson unreadable");
    }

    @Test
    @DisplayName("when every layer fails the run fails")
    void allFailed() {
        WIREMOCK.stubFor(get(com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching("/products/outlook/.*"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamHttpException.class);
    }

    @Test
    @DisplayName("the descriptor has the prompt's values and validates")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(20, 2_000));
        assertThat(d.kindRetention()).containsEntry("spc-outlook", Duration.ofDays(14));
        assertThat(d.titleField()).isEqualTo("title");
        assertThat(d.licence()).isEqualTo("US-PD");
    }
}
