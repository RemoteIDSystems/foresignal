package ai.foresignal.source.adapters.awc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 087 AWC adapters against a stubbed aviationweather.gov through the real upstream client: Foresignal's
 * User-Agent on every request, the PIREP box always sent (lat-first, CONUS by default), a 429 backing the source off,
 * the two serial SIGMET requests with a tombstone for a canceled advisory, the TAF API in chunks and the TAF cache
 * file conditional GET, and descriptors that keep every AWC source inside the 100 requests/minute ceiling.
 */
@IntegrationTest
class AwcAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T04:35:00Z");
    static final String ETAG = "\"6acf0653-530ba\"";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    AwcSigmetsAdapter sigmets;
    AwcGairmetsAdapter gairmets;
    AwcCwasAdapter cwas;
    AwcPirepsAdapter pireps;
    AwcTafsAdapter tafs;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        sigmets = new AwcSigmetsAdapter(baseUrl());
        gairmets = new AwcGairmetsAdapter(baseUrl());
        cwas = new AwcCwasAdapter(baseUrl());
        pireps = new AwcPirepsAdapter(baseUrl());
        tafs = new AwcTafsAdapter((ai.foresignal.source.spi.AreaOfInterest) null, baseUrl());
        for (PollingAdapter a : adapters()) {
            harness.reset(a.id(), LiveIdSet.LIVE_PREFIX + AwcHazardParser.KIND, LiveIdSet.GONE_PREFIX
                    + AwcHazardParser.KIND, SourceHttp.ETAG_STATE_PREFIX + AwcTafsAdapter.STATE_KEY,
                    SourceHttp.LAST_MODIFIED_STATE_PREFIX + AwcTafsAdapter.STATE_KEY);
        }
    }

    @AfterEach
    void tearDown() {
        for (PollingAdapter a : adapters()) {
            harness.backoff.onSuccess(a.id());
        }
    }

    private List<PollingAdapter> adapters() {
        return List.of(sigmets, gairmets, cwas, pireps, tafs);
    }

    private static void serve(String path, String fixture) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json; charset=utf-8").withHeader("Cache-Control", "max-age=180")
                .withBody(AdapterFixtures.bytes("awc", fixture))));
    }

    @Test
    @DisplayName("every AWC request carries Foresignal's User-Agent")
    void userAgent() throws Exception {
        serve(AwcSigmetsAdapter.AIRSIGMET_PATH, "airsigmet.geojson");
        serve(AwcSigmetsAdapter.ISIGMET_PATH, "isigmet.geojson");
        serve(AwcGairmetsAdapter.PATH, "gairmet.geojson");
        serve(AwcCwasAdapter.PATH, "cwa.geojson");
        serve(AwcPirepsAdapter.PATH, "pirep.geojson");

        sigmets.fetch(harness.context(sigmets, Map.of()));
        gairmets.fetch(harness.context(gairmets, Map.of()));
        cwas.fetch(harness.context(cwas, Map.of()));
        pireps.fetch(harness.context(pireps, Map.of()));

        WIREMOCK.verify(5, anyRequestedFor(anyUrl()));
        WIREMOCK.verify(5, anyRequestedFor(anyUrl()).withHeader("User-Agent", matching("^Foresignal/.+")));
    }

    @Test
    @DisplayName("PIREPs: the box is always sent, lat-first; CONUS when the parameter is absent")
    void pirepBoxRequired() throws Exception {
        serve(AwcPirepsAdapter.PATH, "pirep.geojson");

        FetchResult conus = pireps.fetch(harness.context(pireps, Map.of()));
        assertThat(conus.records()).hasSize(88);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(AwcPirepsAdapter.PATH)).withQueryParam("bbox",
                equalTo("20,-130,55,-60")).withQueryParam("age", equalTo("2")).withQueryParam("format",
                equalTo("geojson")));

        WIREMOCK.resetRequests();
        pireps.fetch(harness.context(pireps, Map.of(AwcPirepsAdapter.BBOX_PARAM, new Envelope(-100, -94, 28, 33))));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(AwcPirepsAdapter.PATH)).withQueryParam("bbox",
                equalTo("28,-100,33,-94")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(AwcPirepsAdapter.PATH)).withQueryParam("bbox", absent()));
        assertThat(pireps.descriptor().param(AwcPirepsAdapter.BBOX_PARAM).defaultValue())
                .isEqualTo(AwcPirepsAdapter.DEFAULT_BBOX);
    }

    @Test
    @DisplayName("a 429 puts the source in backoff (Retry-After) and nothing is sent while it lasts")
    void tooManyRequests() {
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcGairmetsAdapter.PATH)).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "120").withBody("Too Many Requests")));

        assertThatThrownBy(() -> gairmets.fetch(harness.context(gairmets, Map.of())))
                .isInstanceOfSatisfying(UpstreamHttpException.class, e -> assertThat(e.getStatus()).isEqualTo(429));
        assertThat(harness.backoff.isInBackoff(gairmets.id())).isTrue();
        WIREMOCK.resetRequests();
        assertThatThrownBy(() -> gairmets.fetch(harness.context(gairmets, Map.of())))
                .isInstanceOf(AdapterException.class);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("SIGMETs: US then international, merged; a canceled advisory is tombstoned; a failed request"
            + " tombstones nothing")
    void sigmetsAndCancellation() throws Exception {
        serve(AwcSigmetsAdapter.AIRSIGMET_PATH, "airsigmet.geojson");
        serve(AwcSigmetsAdapter.ISIGMET_PATH, "isigmet.geojson");
        FetchResult first = sigmets.fetch(harness.context(sigmets, Map.of()));
        assertThat(first.complete()).isTrue();
        assertThat(first.records()).hasSize(28).noneMatch(NormalizedRecord::tombstone);

        tools.jackson.databind.node.ObjectNode collection = (tools.jackson.databind.node.ObjectNode) jsonMapper
                .readTree(AdapterFixtures.bytes("awc", "airsigmet.geojson"));
        tools.jackson.databind.node.ArrayNode features = (tools.jackson.databind.node.ArrayNode) collection
                .get("features");
        while (features.size() > 1) {
            features.remove(features.size() - 1);
        }
        String one = collection.toString();
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcSigmetsAdapter.ISIGMET_PATH)).willReturn(aResponse().withStatus(500)));
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcSigmetsAdapter.AIRSIGMET_PATH)).willReturn(aResponse().withStatus(200)
                .withBody(one)));
        assertThatThrownBy(() -> sigmets.fetch(harness.context(sigmets, Map.of())))
                .isInstanceOf(AdapterException.class);
        harness.backoff.onSuccess(sigmets.id());

        serve(AwcSigmetsAdapter.ISIGMET_PATH, "isigmet.geojson");
        FetchResult after = sigmets.fetch(harness.context(sigmets, Map.of()));
        List<NormalizedRecord> tombstones = new ArrayList<>(after.records().stream()
                .filter(NormalizedRecord::tombstone).toList());
        assertThat(tombstones).hasSize(6).allSatisfy(t -> assertThat(t.externalId()).startsWith("KKCI:"));
        assertThat(after.records()).filteredOn(r -> !r.tombstone()).hasSize(22);
    }

    @Test
    @DisplayName("CWAs: none in force is a normal empty result (a 204 too)")
    void emptyCwas() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcCwasAdapter.PATH)).willReturn(aResponse().withStatus(200)
                .withBody("{\"type\":\"FeatureCollection\",\"features\":[]}")));
        assertThat(cwas.fetch(harness.context(cwas, Map.of())).records()).isEmpty();
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcCwasAdapter.PATH)).willReturn(aResponse().withStatus(204)));
        assertThat(cwas.fetch(harness.context(cwas, Map.of())).records()).isEmpty();
        assertThat(cwas.descriptor().zeroResultsNormal()).isTrue();
    }

    @Test
    @DisplayName("TAFs with stations: the TAF API, at most 400 ids per request, one request after another")
    void tafApiChunks() throws Exception {
        serve(AwcTafsAdapter.API_PATH, "taf.json");
        List<String> stations = new ArrayList<>(List.of("KAUS", "KDFW", "KJFK", "EGLL"));
        for (int i = 0; i < 400; i++) {
            stations.add(String.format("K%03d", i));
        }

        FetchResult result = tafs.fetch(harness.context(tafs, Map.of(AwcTafsAdapter.STATIONS_PARAM, stations)));
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .containsExactlyInAnyOrder("KAUS", "KDFW", "KJFK", "EGLL");
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(AwcTafsAdapter.API_PATH)).withQueryParam("format",
                equalTo("json")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(AwcTafsAdapter.API_PATH)).withQueryParam("ids",
                matching("^KAUS,KDFW,KJFK,EGLL,K000,.*,K395$")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(AwcTafsAdapter.CACHE_PATH)));
    }

    @Test
    @DisplayName("TAFs by scope: the cache file, conditional; a 304 is unchanged; a garbled file forgets the ETag")
    void tafCache() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcTafsAdapter.CACHE_PATH)).withHeader("If-None-Match", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/octet-stream")
                        .withHeader("ETag", ETAG).withBody(AdapterFixtures.bytes("awc", "tafs.cache.xml.gz"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcTafsAdapter.CACHE_PATH)).withHeader("If-None-Match", equalTo(ETAG))
                .willReturn(aResponse().withStatus(304)));

        FetchResult texas = tafs.fetch(harness.context(tafs, Map.of()));
        assertThat(texas.records()).hasSize(86);
        assertThat(tafs.fetch(harness.context(tafs, Map.of())).notModified()).isTrue();

        harness.reset(tafs.id(), SourceHttp.ETAG_STATE_PREFIX + AwcTafsAdapter.STATE_KEY);
        assertThat(tafs.fetch(harness.context(tafs, Map.of(AwcTafsAdapter.SCOPE_PARAM, "global"))).records())
                .hasSize(90);

        WIREMOCK.resetAll();
        harness.reset(tafs.id(), SourceHttp.ETAG_STATE_PREFIX + AwcTafsAdapter.STATE_KEY);
        WIREMOCK.stubFor(get(urlPathEqualTo(AwcTafsAdapter.CACHE_PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("ETag", ETAG).withBody(new byte[] {0x1f, (byte) 0x8b, 1, 2, 3})));
        var ctx = harness.context(tafs, Map.of());
        assertThatThrownBy(() -> tafs.fetch(ctx)).isInstanceOf(ai.foresignal.source.spi.UpstreamFormatException.class);
        assertThat(ctx.state().get(SourceHttp.ETAG_STATE_PREFIX + AwcTafsAdapter.STATE_KEY)).isEmpty();
    }

    @Test
    @DisplayName("descriptors: valid, never under 2 minutes, budgets well inside AWC's 100 requests/minute")
    void descriptors() {
        int perMinute = new AwcMetarsAdapter((ai.foresignal.source.spi.AreaOfInterest) null, "").descriptor()
                .defaultBudget().perMinute();
        for (PollingAdapter a : adapters()) {
            SourceDescriptor d = a.descriptor();
            assertThat(DescriptorValidator.problems(d, a)).as(d.id()).isEmpty();
            assertThat(d.minInterval()).as(d.id()).isGreaterThanOrEqualTo(Duration.ofMinutes(2));
            assertThat(d.description()).as(d.id()).contains("100 requests per minute");
            perMinute += d.defaultBudget().perMinute();
        }
        assertThat(perMinute).isEqualTo(AwcSupport.BUDGET_SUM_PER_MINUTE).isLessThan(AwcSupport.RATE_LIMIT_PER_MINUTE);
        assertThat(sigmets.descriptor().defaultBudget()).isEqualTo(Budget.of(3, 1500));
        assertThat(gairmets.descriptor().defaultBudget()).isEqualTo(Budget.of(1, 500));
        assertThat(gairmets.descriptor().recordTtl()).isEqualTo(Duration.ofHours(3));
        assertThat(cwas.descriptor().defaultBudget()).isEqualTo(Budget.of(1, 500));
        assertThat(pireps.descriptor().defaultBudget()).isEqualTo(Budget.of(1, 500));
        assertThat(pireps.descriptor().recordTtl()).isEqualTo(Duration.ofHours(2));
        assertThat(tafs.descriptor().defaultBudget()).isEqualTo(Budget.of(2, 300));
        assertThat(tafs.descriptor().defaultInterval()).isEqualTo(Duration.ofMinutes(10));
    }
}
