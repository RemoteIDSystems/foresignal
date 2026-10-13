package ai.foresignal.source.adapters.nws;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code nws-alerts} against a stubbed {@code api.weather.gov} through the real upstream client
 * (prompt 047): the User-Agent contact, gzip, the {@code area} parameter, a 503 with
 * {@code Retry-After}, tombstones from complete responses and none from a truncated one.
 */
@IntegrationTest
class NwsAlertsAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T00:01:00Z");
    static final String PATH = "/alerts/active";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired AppSettingService settings;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    WeatherAdapterHarness.MapState state;
    NwsAlertsAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 1_000), null);
        state = new WeatherAdapterHarness.MapState();
        adapter = new NwsAlertsAdapter(CountyGeometries.none(), baseUrl());
    }

    @AfterEach
    void tearDown() {
        upstream.backoff().onSuccess(NwsAlertsAdapter.ID);
    }

    private WeatherAdapterHarness.Context context(Map<String, Object> params) {
        return context(params, upstream.client().forSource(NwsAlertsAdapter.ID, adapter.descriptor()));
    }

    private WeatherAdapterHarness.Context context(Map<String, Object> params, SourceHttp http) {
        return new WeatherAdapterHarness.Context(adapter.descriptor(), http, ValidatedParams.of(params), state, clock);
    }

    static byte[] gzip(byte[] body) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    private static void serve(String fixture) {
        serveBytes(AdapterFixtures.bytes("nws", fixture));
    }

    private static void serveBytes(byte[] body) {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json")
                .withHeader("Content-Encoding", "gzip")
                .withBody(gzip(body))));
    }

    /** {@code edge-cases.json} without the alert {@code id}. */
    private static byte[] without(String id) {
        var root = (tools.jackson.databind.node.ObjectNode) JsonMapper.builder().build()
                .readTree(AdapterFixtures.bytes("nws", "edge-cases.json"));
        var features = (tools.jackson.databind.node.ArrayNode) root.get("features");
        for (int i = features.size() - 1; i >= 0; i--) {
            if (id.equals(features.get(i).path("properties").path("id").asString())) {
                features.remove(i);
            }
        }
        return root.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static List<String> tombstones(FetchResult result) {
        return result.records().stream().filter(NormalizedRecord::tombstone).map(NormalizedRecord::externalId).toList();
    }

    @Test
    @DisplayName("the request asks for geo+json, accepts gzip, and the User-Agent carries the operator contact")
    void requestHeaders() throws Exception {
        settings.put(SettingKeys.SERVER, new ServerSettings(null, null, null, null, "ops@example.org"));
        try {
            serve("area-TX.json");

            FetchResult result = adapter.fetch(context(Map.of()));

            assertThat(result.records()).hasSize(2);
            WIREMOCK.verify(getRequestedFor(urlEqualTo(PATH + "?status=actual"))
                    .withHeader("Accept", equalTo("application/geo+json"))
                    .withHeader("Accept-Encoding", containing("gzip"))
                    .withHeader("User-Agent", containing("+https://foresignal.ai"))
                    .withHeader("User-Agent", containing("contact=ops@example.org")));
        } finally {
            settings.put(SettingKeys.SERVER, ServerSettings.defaults());
        }
    }

    @Test
    @DisplayName("area codes are upper-cased, sorted and sent as one area= list; a bad code is refused")
    void areaParam() throws Exception {
        serve("area-TX.json");

        adapter.fetch(context(Map.of(NwsAlertsAdapter.AREA_PARAM, List.of("tx", "OK", " "))));

        WIREMOCK.verify(getRequestedFor(urlEqualTo(PATH + "?status=actual&area=OK,TX")));
        assertThatThrownBy(() -> adapter.fetch(context(Map.of(NwsAlertsAdapter.AREA_PARAM, List.of("TX&x=1")))))
                .hasMessageContaining("not a state or marine area code");
    }

    @Test
    @DisplayName("an alert absent from the next complete response is tombstoned at the fetch time")
    void absentTombstoned() throws Exception {
        serve("edge-cases.json");
        FetchResult first = adapter.fetch(context(Map.of()));
        assertThat(tombstones(first)).isEmpty();
        assertThat(first.complete()).isTrue();

        clock.advance(Duration.ofMinutes(1));
        serveBytes(without("urn:oid:test.polygon.1"));
        FetchResult second = adapter.fetch(context(Map.of()));

        assertThat(tombstones(second)).containsExactly("urn:oid:test.polygon.1");
        NormalizedRecord tombstone = second.records().stream().filter(NormalizedRecord::tombstone).findFirst().orElseThrow();
        assertThat(tombstone.observedAt()).isEqualTo(NOW.plus(Duration.ofMinutes(1)));
    }

    @Test
    @DisplayName("a superseded alert seen earlier is tombstoned at the superseding message's sent time")
    void supersededTombstone() throws Exception {
        // First response: old.1 is live (no Update references it yet).
        serveBytes(without("urn:oid:test.update.1"));
        adapter.fetch(context(Map.of()));

        serve("edge-cases.json");
        FetchResult second = adapter.fetch(context(Map.of()));

        assertThat(tombstones(second)).contains("urn:oid:test.old.1").doesNotContain("urn:oid:test.old.2");
        NormalizedRecord tombstone = second.records().stream()
                .filter(r -> r.tombstone() && r.externalId().equals("urn:oid:test.old.1")).findFirst().orElseThrow();
        assertThat(tombstone.observedAt()).isEqualTo(Instant.parse("2026-10-12T15:35:00Z"));
    }

    @Test
    @DisplayName("a truncated body throws and tombstones nothing; the live set is untouched")
    void truncated() throws Exception {
        serve("edge-cases.json");
        adapter.fetch(context(Map.of()));
        Map<String, String> before = state.asMap();

        serve("area-TX.json");
        SourceHttp truncating = WeatherAdapterHarness.truncating(
                upstream.client().forSource(NwsAlertsAdapter.ID, adapter.descriptor()));
        assertThatThrownBy(() -> adapter.fetch(context(Map.of(), truncating))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("truncated");

        assertThat(state.asMap()).isEqualTo(before);
    }

    @Test
    @DisplayName("a 503 with Retry-After fails the run, starts the backoff and tombstones nothing")
    void throttled() throws Exception {
        serve("edge-cases.json");
        adapter.fetch(context(Map.of()));
        Map<String, String> before = state.asMap();
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).atPriority(1)
                .willReturn(aResponse().withStatus(503).withHeader("Retry-After", "120")));

        assertThatThrownBy(() -> adapter.fetch(context(Map.of()))).isInstanceOf(UpstreamHttpException.class)
                .extracting(e -> ((UpstreamHttpException) e).getStatus()).isEqualTo(503);

        assertThat(upstream.backoff().isInBackoff(NwsAlertsAdapter.ID)).isTrue();
        assertThat(upstream.backoff().backoffUntil(NwsAlertsAdapter.ID)).isAfterOrEqualTo(NOW.plusSeconds(120));
        assertThat(state.asMap()).isEqualTo(before);
    }

    @Test
    @DisplayName("an unparseable body is a format error before anything is tombstoned")
    void formatError() throws Exception {
        serve("edge-cases.json");
        adapter.fetch(context(Map.of()));
        Map<String, String> before = state.asMap();
        serveBytes("{\"type\":\"Feature\"}".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> adapter.fetch(context(Map.of()))).isInstanceOf(UpstreamFormatException.class);
        assertThat(state.asMap()).isEqualTo(before);
    }

    @Test
    @DisplayName("the descriptor has the values the prompt specifies (retention per Conventions) and validates")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("nws-alerts");
        assertThat(d.kinds()).containsExactly("nws-alert");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(60));
        assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(2, 1_600));
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.defaultIngestPolicy()).isEqualTo(IngestPolicy.all());
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.RECEIVED);
        assertThat(d.recordTtl()).isNull();
        assertThat(d.licence()).isEqualTo("US-PD");
        assertThat(d.termsUrl()).isEqualTo("https://www.weather.gov/disclaimer");
        assertThat(d.titleField()).isEqualTo("headline");
        assertThat(d.embedFields()).containsExactly("headline", "description", "instruction");
        assertThat(d.searchFields()).containsExactly("event", "headline", "areaDesc");
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.kindRetention()).containsEntry("nws-alert", Duration.ofDays(1825));
        assertThat(d.param("area")).isNotNull();
        assertThat(d.param("minSeverity").allowed()).containsExactly("Extreme", "Severe", "Moderate", "Minor", "Unknown");
        assertThat(d.fieldSpecs().get("maxHailSizeIn").unit()).isEqualTo("[in_i]");
        assertThat(d.fieldSpecs().get("maxWindGustMph").unit()).isEqualTo("[mi_i]/h");
    }
}
