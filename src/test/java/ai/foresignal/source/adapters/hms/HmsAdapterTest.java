package ai.foresignal.source.adapters.hms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.NormalizedRecord;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code hms-smoke} and {@code hms-fire} against WireMock (prompt 048): conditional GETs, the yesterday
 * re-fetch window after midnight, a 404 for today, tombstones only from complete smoke files, and no
 * tombstones (with validators forgotten) when a file fails.
 */
@IntegrationTest
class HmsAdapterTest extends WireMockSupport {

    static final String SMOKE_12 = "/pub/FIRE/web/HMS/Smoke_Polygons/KML/2026/10/hms_smoke20261012.kml";
    static final String SMOKE_13 = "/pub/FIRE/web/HMS/Smoke_Polygons/KML/2026/10/hms_smoke20261013.kml";
    static final String FIRE_12 = "/pub/FIRE/web/HMS/Fire_Points/Text/2026/10/hms_fire20261012.txt";
    static final String FIRE_13 = "/pub/FIRE/web/HMS/Fire_Points/Text/2026/10/hms_fire20261013.txt";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    WeatherAdapterHarness.MapState state;
    HmsSmokeAdapter smoke;
    HmsFireAdapter fire;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-12T18:00:00Z"));
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 10_000),
                null);
        state = new WeatherAdapterHarness.MapState();
        smoke = new HmsSmokeAdapter(baseUrl());
        fire = new HmsFireAdapter(baseUrl());
    }

    private WeatherAdapterHarness.Context context(SourceDescriptor d) {
        return new WeatherAdapterHarness.Context(d, upstream.client().forSource(d.id(), d), ValidatedParams.empty(), state,
                clock);
    }

    private static void stub(String path, String etag, byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).withHeader("If-None-Match", absent()).willReturn(aResponse()
                .withHeader("ETag", etag).withHeader("Last-Modified", "Mon, 12 Oct 2026 23:25:14 GMT").withBody(body)));
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).withHeader("If-None-Match", equalTo(etag)).atPriority(1)
                .willReturn(aResponse().withStatus(304).withHeader("ETag", etag)));
    }

    private static byte[] smokeFixture() {
        return AdapterFixtures.bytes("hms", "hms_smoke20261012.kml");
    }

    /** The captured KML without its first placemark. */
    private static byte[] smokeWithoutFirst() {
        String kml = new String(smokeFixture(), StandardCharsets.UTF_8);
        int start = kml.indexOf("<Placemark>");
        int end = kml.indexOf("</Placemark>", start) + "</Placemark>".length();
        return (kml.substring(0, start) + kml.substring(end)).getBytes(StandardCharsets.UTF_8);
    }

    private static List<String> tombstones(FetchResult result) {
        return result.records().stream().filter(NormalizedRecord::tombstone).map(NormalizedRecord::externalId).toList();
    }

    @Test
    @DisplayName("a conditional GET: 200 with validators, then 304 sends If-None-Match and changes nothing")
    void conditionalGet() throws Exception {
        stub(SMOKE_12, "\"1cdf5-65dad02904a80\"", smokeFixture());

        FetchResult first = smoke.fetch(context(smoke.descriptor()));
        assertThat(first.records()).hasSize(67).noneMatch(NormalizedRecord::tombstone);
        assertThat(state.get(SourceHttp.ETAG_STATE_PREFIX + "smoke:20261012")).contains("\"1cdf5-65dad02904a80\"");

        FetchResult second = smoke.fetch(context(smoke.descriptor()));
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(SMOKE_12)).withHeader("If-None-Match",
                equalTo("\"1cdf5-65dad02904a80\"")));
    }

    @Test
    @DisplayName("in the first 6 hours after midnight yesterday's file is fetched again; later only today's")
    void yesterdayWindow() throws Exception {
        clock.setInstant(Instant.parse("2026-10-13T03:00:00Z"));
        stub(SMOKE_12, "\"a\"", smokeFixture());
        WIREMOCK.stubFor(get(urlPathEqualTo(SMOKE_13)).willReturn(aResponse().withStatus(404)));

        FetchResult early = smoke.fetch(context(smoke.descriptor()));
        assertThat(early.records()).hasSize(67);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(SMOKE_12)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(SMOKE_13)));

        clock.setInstant(Instant.parse("2026-10-13T07:00:00Z"));
        assertThat(smoke.fetch(context(smoke.descriptor())).notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(SMOKE_12)));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(SMOKE_13)));

        assertThat(HmsDailyFiles.days(Instant.parse("2026-10-13T05:59:59Z")))
                .containsExactly(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 13));
        assertThat(HmsDailyFiles.days(Instant.parse("2026-10-13T06:00:00Z"))).containsExactly(LocalDate.of(2026, 10, 13));
    }

    @Test
    @DisplayName("a 404 for today before the first analysis is normal: unchanged, no error")
    void notPublishedYet() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(SMOKE_12)).willReturn(aResponse().withStatus(404)));
        WIREMOCK.stubFor(get(urlPathEqualTo(FIRE_12)).willReturn(aResponse().withStatus(404)));

        assertThat(smoke.fetch(context(smoke.descriptor())).notModified()).isTrue();
        assertThat(fire.fetch(context(fire.descriptor())).notModified()).isTrue();
    }

    @Test
    @DisplayName("a polygon missing from a complete new file is tombstoned; a truncated file tombstones nothing")
    void analystEdits() throws Exception {
        stub(SMOKE_12, "\"v1\"", smokeFixture());
        FetchResult first = smoke.fetch(context(smoke.descriptor()));
        String firstId = first.records().get(0).externalId();

        WIREMOCK.resetAll();
        stub(SMOKE_12, "\"v2\"", smokeWithoutFirst());
        clock.advance(Duration.ofMinutes(30));
        FetchResult second = smoke.fetch(context(smoke.descriptor()));

        assertThat(tombstones(second)).containsExactly(firstId);
        assertThat(second.records().stream().filter(r -> r.tombstone()).findFirst().orElseThrow().observedAt())
                .isEqualTo(clock.instant());
        assertThat(LiveIdSet.read(state, LiveIdSet.LIVE_PREFIX + "smoke:20261012")).hasSize(66);

        // A cut-off body: the run fails before anything is tombstoned, and the validators are forgotten.
        WIREMOCK.resetAll();
        byte[] whole = smokeFixture();
        WIREMOCK.stubFor(get(urlPathEqualTo(SMOKE_12)).willReturn(aResponse().withHeader("ETag", "\"v3\"")
                .withBody(java.util.Arrays.copyOf(whole, whole.length / 2))));
        assertThatThrownBy(() -> smoke.fetch(context(smoke.descriptor()))).isInstanceOf(UpstreamFormatException.class);
        assertThat(state.get(SourceHttp.ETAG_STATE_PREFIX + "smoke:20261012")).isEmpty();
        assertThat(LiveIdSet.read(state, LiveIdSet.LIVE_PREFIX + "smoke:20261012")).hasSize(66);
    }

    @Test
    @DisplayName("old live-id snapshots are dropped after three days")
    void forgetsOldDays() throws Exception {
        stub(SMOKE_12, "\"v1\"", smokeFixture());
        smoke.fetch(context(smoke.descriptor()));
        assertThat(state.get(HmsSmokeAdapter.DAYS_KEY)).contains("20261012");

        HmsSmokeAdapter.forgetOldDays(context(smoke.descriptor()), LocalDate.of(2026, 10, 16));
        assertThat(state.get(LiveIdSet.LIVE_PREFIX + "smoke:20261012")).isEmpty();
        assertThat(state.get(HmsSmokeAdapter.DAYS_KEY)).isEmpty();
    }

    @Test
    @DisplayName("fire points: every row a record; a failure after yesterday's file fails the run and forgets its validators")
    void firePoints() throws Exception {
        stub(FIRE_12, "\"18d54a-65dad02aecf00\"", AdapterFixtures.bytes("hms", "hms_fire20261012.txt"));
        FetchResult result = fire.fetch(context(fire.descriptor()));
        assertThat(result.records()).hasSize(16_777).noneMatch(NormalizedRecord::tombstone)
                .allMatch(r -> r.kind().equals("fire-detection"));

        clock.setInstant(Instant.parse("2026-10-13T02:00:00Z"));
        state.remove(SourceHttp.ETAG_STATE_PREFIX + "fire:20261012");
        state.remove(SourceHttp.LAST_MODIFIED_STATE_PREFIX + "fire:20261012");
        WIREMOCK.stubFor(get(urlPathEqualTo(FIRE_13)).willReturn(aResponse().withStatus(500)));
        assertThatThrownBy(() -> fire.fetch(context(fire.descriptor()))).isInstanceOf(AdapterException.class);
        assertThat(state.get(SourceHttp.ETAG_STATE_PREFIX + "fire:20261012")).isEmpty();
    }

    @Test
    @DisplayName("the descriptors match the prompt and validate")
    void descriptors() {
        for (SourceDescriptor d : List.of(smoke.descriptor(), fire.descriptor())) {
            assertThat(DescriptorValidator.problems(d, d.id().equals(HmsSmokeAdapter.ID) ? smoke : fire)).isEmpty();
            assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(30));
            assertThat(d.defaultBudget()).isEqualTo(Budget.of(4, 200));
            assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
            assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(24));
            assertThat(d.licence()).isEqualTo("US-PD");
            assertThat(d.termsUrl()).isEqualTo("https://www.ospo.noaa.gov/Products/land/hms.html");
        }
        assertThat(smoke.descriptor().kinds()).containsExactly("smoke-polygon");
        assertThat(smoke.descriptor().kindRetention()).containsEntry("smoke-polygon", Duration.ofDays(7));
        assertThat(fire.descriptor().kinds()).containsExactly("fire-detection");
        assertThat(fire.descriptor().kindRetention()).containsEntry("fire-detection", Duration.ofDays(7));
    }
}
