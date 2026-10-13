package ai.foresignal.source.adapters.awc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code awc-metars} against a stubbed {@code aviationweather.gov} (prompt 047): the conditional GET of
 * the gzip cache file, scopes, and the request budget.
 */
@IntegrationTest
class AwcMetarsAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T00:03:00Z");
    static final String ETAG = "\"6acd74c3-42239\"";
    static final String LAST_MODIFIED = "Tue, 13 Oct 2026 00:01:07 GMT";
    static final double[] TEXAS = {-106.65, 25.84, -93.51, 36.50};

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    AwcMetarsAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        adapter = new AwcMetarsAdapter(new WeatherAdapterHarness.TestAoi(TEXAS), baseUrl());
    }

    private WeatherAdapterHarness.Context context(Budget budget, Map<String, Object> params) {
        WeatherAdapterHarness.Upstream upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock,
                budget, null);
        SourceDescriptor d = adapter.descriptor();
        return new WeatherAdapterHarness.Context(d, upstream.client().forSource(AwcMetarsAdapter.ID, d),
                ValidatedParams.of(params), upstream.client().stateStore(AwcMetarsAdapter.ID), clock);
    }

    private static void serve() {
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcMetarsAdapter.PATH)).withHeader("If-None-Match", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/octet-stream")
                        .withHeader("ETag", ETAG).withHeader("Last-Modified", LAST_MODIFIED)
                        .withHeader("Cache-Control", "max-age=30")
                        .withBody(AdapterFixtures.bytes("awc", "metars.cache.csv.gz"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcMetarsAdapter.PATH)).withHeader("If-None-Match", equalTo(ETAG))
                .willReturn(aResponse().withStatus(304).withHeader("ETag", ETAG)));
    }

    @Test
    @DisplayName("a 200 is inflated and parsed for the AOI; the next request is conditional and a 304 is unchanged")
    void conditional() throws Exception {
        serve();
        WeatherAdapterHarness.Context ctx = context(Budget.of(10, 400), Map.of());

        FetchResult first = adapter.fetch(ctx);
        assertThat(first.records()).hasSize(291);
        assertThat(first.records()).noneMatch(NormalizedRecord::tombstone);

        FetchResult second = adapter.fetch(ctx);
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(AwcMetarsAdapter.PATH)).withHeader("If-None-Match", absent()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(AwcMetarsAdapter.PATH)).withHeader("If-None-Match", equalTo(ETAG))
                .withHeader("If-Modified-Since", equalTo(LAST_MODIFIED)));
    }

    @Test
    @DisplayName("scope conus and global, and a stations allowlist that overrides the scope")
    void scopes() throws Exception {
        serve();
        assertThat(adapter.fetch(context(Budget.of(10, 400), Map.of("scope", "conus"))).records()).hasSize(2582);
        assertThat(adapter.fetch(context(Budget.of(10, 400), Map.of("scope", "global"))).records()).hasSize(5022);
        assertThat(adapter.fetch(context(Budget.of(10, 400), Map.of("stations", List.of("kaus", "EGLL")))).records())
                .extracting(NormalizedRecord::externalId).containsExactlyInAnyOrder("KAUS", "EGLL");
    }

    @Test
    @DisplayName("a garbled body forgets the validators and fails the run")
    void garbled() {
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcMetarsAdapter.PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("ETag", ETAG).withBody(new byte[] {0x1f, (byte) 0x8b, 1, 2, 3})));
        WeatherAdapterHarness.Context ctx = context(Budget.of(10, 400), Map.of());

        assertThatThrownBy(() -> adapter.fetch(ctx)).isInstanceOf(UpstreamFormatException.class);
        AdapterStateStore state = ctx.state();
        assertThat(state.get("etag:" + AwcMetarsAdapter.STATE_KEY)).isEmpty();
    }

    @Test
    @DisplayName("the request budget is enforced: past perMinute the client refuses before sending")
    void budget() throws Exception {
        serve();
        WeatherAdapterHarness.Context ctx = context(Budget.of(2, 400), Map.of());
        adapter.fetch(ctx);
        adapter.fetch(ctx);

        assertThatThrownBy(() -> adapter.fetch(ctx)).isInstanceOf(BudgetExceededException.class);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(AwcMetarsAdapter.PATH)));
    }

    @Test
    @DisplayName("the descriptor: 5-minute poll within AWC's limits, budget 2/min 400/day, TTL 3 h on the observation")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(1));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(2, 400, 100));
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(3));
        assertThat(d.kindRetention()).containsEntry("metar", Duration.ofDays(7));
        assertThat(d.titleField()).isEqualTo("stationId");
        assertThat(d.description()).contains("1 request per minute per thread", "100 per minute overall");
    }
}
