package ai.foresignal.source.adapters.nhc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code nhc-cyclones} against a stubbed {@code www.nhc.noaa.gov} through the real upstream client (prompt 067): the
 * conditional GET, a 304, tombstones for storms that leave the complete set, none from a failed or rejected response,
 * and the empty off-season file.
 */
@IntegrationTest
class NhcAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T14:50:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    WeatherAdapterHarness.MapState state;
    NhcCyclonesAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 1_000), null);
        state = new WeatherAdapterHarness.MapState();
        adapter = new NhcCyclonesAdapter(baseUrl());
    }

    @AfterEach
    void tearDown() {
        upstream.backoff().onSuccess(NhcCyclonesAdapter.ID);
    }

    private WeatherAdapterHarness.Context context() {
        return new WeatherAdapterHarness.Context(adapter.descriptor(),
                upstream.client().forSource(NhcCyclonesAdapter.ID, adapter.descriptor()), ValidatedParams.empty(), state,
                clock);
    }

    private static void serve(byte[] body) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(NhcCyclonesAdapter.PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withHeader("ETag", "\"v1\"")
                .withHeader("Last-Modified", "Tue, 13 Oct 2026 14:34:00 GMT").withBody(body)));
    }

    private static void serve(String fixture) {
        serve(AdapterFixtures.bytes(NhcCyclonesAdapter.ID, fixture));
    }

    private static List<String> tombstones(FetchResult result) {
        return result.records().stream().filter(NormalizedRecord::tombstone).map(NormalizedRecord::externalId).toList();
    }

    @Test
    @DisplayName("the storms are returned complete and their ids remembered; the ETag is stored")
    void storms() throws Exception {
        serve("CurrentStorms.json");

        FetchResult result = adapter.fetch(context());

        assertThat(result.complete()).isTrue();
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("ep202026");
        assertThat(state.get("live:" + NhcCyclonesAdapter.ACTIVE_SCOPE)).contains("ep202026");
        assertThat(state.get(SourceHttp.ETAG_STATE_PREFIX + NhcCyclonesAdapter.STATE_KEY)).contains("\"v1\"");
    }

    @Test
    @DisplayName("a 304 is not modified and changes nothing")
    void notModified() throws Exception {
        serve("CurrentStorms.json");
        adapter.fetch(context());
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(NhcCyclonesAdapter.PATH)).withHeader("If-None-Match", equalTo("\"v1\""))
                .willReturn(aResponse().withStatus(304)));

        FetchResult result = adapter.fetch(context());

        assertThat(result.notModified()).isTrue();
        assertThat(result.records()).isEmpty();
        assertThat(state.get("live:" + NhcCyclonesAdapter.ACTIVE_SCOPE)).contains("ep202026");
    }

    @Test
    @DisplayName("a storm absent from a later successful response gets one explicit tombstone")
    void tombstone() throws Exception {
        serve("string-numbers.json");
        adapter.fetch(context());
        serve("CurrentStorms.json");
        clock.advance(Duration.ofMinutes(10));

        FetchResult result = adapter.fetch(context());

        assertThat(tombstones(result)).containsExactly("al092026", "cp012026", "ep142026");
        assertThat(result.records().stream().filter(NormalizedRecord::tombstone))
                .allSatisfy(r -> assertThat(r.observedAt()).isEqualTo(NOW.plus(Duration.ofMinutes(10))));
        assertThat(result.records()).filteredOn(r -> !r.tombstone()).extracting(NormalizedRecord::externalId)
                .containsExactly("ep202026");
    }

    @Test
    @DisplayName("an empty list after storms: zero storms, tombstones for the old ones, healthy")
    void emptyAfterStorms() throws Exception {
        serve("CurrentStorms.json");
        adapter.fetch(context());
        serve("empty.json");

        FetchResult result = adapter.fetch(context());

        assertThat(tombstones(result)).containsExactly("ep202026");
        assertThat(result.records()).hasSize(1);
    }

    @Test
    @DisplayName("an empty list from the start is zero records, a normal result (zeroResultsNormal)")
    void emptyIsNormal() throws Exception {
        serve("empty.json");

        FetchResult result = adapter.fetch(context());

        assertThat(result.records()).isEmpty();
        assertThat(result.notModified()).isFalse();
        assertThat(adapter.descriptor().zeroResultsNormal()).isTrue();
    }

    @Test
    @DisplayName("a rejected document tombstones nothing and forgets the validators")
    void rejectedTombstonesNothing() throws Exception {
        serve("CurrentStorms.json");
        adapter.fetch(context());
        serve("invalid-id.json");

        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamFormatException.class);

        assertThat(state.get("live:" + NhcCyclonesAdapter.ACTIVE_SCOPE)).contains("ep202026");
        assertThat(state.get(SourceHttp.ETAG_STATE_PREFIX + NhcCyclonesAdapter.STATE_KEY)).isEmpty();
    }

    @Test
    @DisplayName("an HTTP error tombstones nothing")
    void httpError() throws Exception {
        serve("CurrentStorms.json");
        adapter.fetch(context());
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(NhcCyclonesAdapter.PATH)).willReturn(aResponse().withStatus(500)
                .withBody("oops".getBytes(StandardCharsets.UTF_8))));

        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(Exception.class);

        assertThat(state.get("live:" + NhcCyclonesAdapter.ACTIVE_SCOPE)).contains("ep202026");
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(NhcCyclonesAdapter.PATH)));
    }

    @Test
    @DisplayName("the descriptor is valid and carries the prompt's values")
    void descriptor() {
        var d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("nhc-cyclones");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(2, 300));
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.recordTtl()).isNull();
        assertThat(d.licence()).isEqualTo("US-PD");
        assertThat(new NhcCyclonesAdapter("").stormsUri().toString()).isEqualTo("https://www.nhc.noaa.gov/CurrentStorms.json");
    }
}
