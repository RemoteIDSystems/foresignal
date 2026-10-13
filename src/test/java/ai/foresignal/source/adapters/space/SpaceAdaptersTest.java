package ai.foresignal.source.adapters.space;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.adapters.space.donki.NasaDonkiAdapter;
import ai.foresignal.source.adapters.space.jpl.JplCloseApproachesAdapter;
import ai.foresignal.source.adapters.space.jpl.JplFireballsAdapter;
import ai.foresignal.source.adapters.space.satnogs.SatnogsStationsAdapter;
import ai.foresignal.source.adapters.space.sondehub.SondehubAdapter;
import ai.foresignal.source.adapters.space.swpc.SwpcAlertsAdapter;
import ai.foresignal.source.adapters.space.swpc.SwpcAuroraAdapter;
import ai.foresignal.source.adapters.space.swpc.SwpcKpAdapter;
import ai.foresignal.source.adapters.space.swpc.SwpcRtswAdapter;
import ai.foresignal.source.adapters.space.swpc.SwpcXrayAdapter;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 077 polling adapters against stubbed upstreams through the real upstream client: a conditional GET on
 * every SWPC file, the RTSW wind file read at most once per {@code densityInterval} on a {@link MutableClock}, the
 * aurora sampled per place (and nothing fetched without places), one DONKI request per type, SatNOGS {@code Link}
 * paging with absence tombstones, and the JPL and SondeHub request URLs.
 */
@IntegrationTest
class SpaceAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T20:47:00Z");
    static final String SWPC = "/services.swpc.noaa.gov";
    static final String DONKI = "/ccmc.gsfc.nasa.gov/DONKI-API/get/";
    static final String SATNOGS = "/network.satnogs.org/api/stations/";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    List<PlacePoints.Summary> places;
    SwpcAlertsAdapter alerts;
    SwpcKpAdapter kp;
    SwpcRtswAdapter rtsw;
    SwpcXrayAdapter xray;
    SwpcAuroraAdapter aurora;
    NasaDonkiAdapter donki;
    JplFireballsAdapter fireballs;
    JplCloseApproachesAdapter cad;
    SatnogsStationsAdapter satnogs;
    SondehubAdapter sondehub;

    /** Places as an adapter sees them, from {@link #places}. */
    PlacePoints placePoints() {
        return new PlacePoints() {
            @Override
            public Optional<double[]> point(String placeId) {
                return places.stream().filter(p -> p.id().equals(placeId)).findFirst()
                        .map(p -> new double[] {p.lon(), p.lat()});
            }

            @Override
            public List<PlacePoints.Summary> summaries() {
                return places;
            }
        };
    }

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        places = new ArrayList<>();
        alerts = new SwpcAlertsAdapter(baseUrl());
        kp = new SwpcKpAdapter(baseUrl());
        rtsw = new SwpcRtswAdapter(baseUrl());
        xray = new SwpcXrayAdapter(baseUrl());
        aurora = new SwpcAuroraAdapter(this::placePoints, baseUrl());
        donki = new NasaDonkiAdapter(baseUrl());
        fireballs = new JplFireballsAdapter(baseUrl());
        cad = new JplCloseApproachesAdapter(baseUrl());
        satnogs = new SatnogsStationsAdapter(baseUrl());
        sondehub = new SondehubAdapter(this::placePoints, baseUrl());
        reset(alerts.id(), SwpcAlertsAdapter.ALERTS_KEY, SwpcAlertsAdapter.SCALES_KEY);
        reset(kp.id(), SwpcKpAdapter.ESTIMATED_KEY, SwpcKpAdapter.OFFICIAL_KEY);
        reset(rtsw.id(), SwpcRtswAdapter.SPEED_KEY, SwpcRtswAdapter.FIELD_KEY, SwpcRtswAdapter.WIND_KEY);
        harness.reset(rtsw.id(), SwpcRtswAdapter.LAST_SPEED_KEY, SwpcRtswAdapter.LAST_FIELD_KEY,
                SwpcRtswAdapter.LAST_DENSITY_KEY, SwpcRtswAdapter.DENSITY_READ_AT_KEY);
        reset(xray.id(), SwpcXrayAdapter.FLARES_KEY);
        reset(aurora.id(), SwpcAuroraAdapter.OVATION_KEY);
        harness.reset(aurora.id(), LiveIdSet.LIVE_PREFIX + "places", LiveIdSet.GONE_PREFIX + "places", "aurora:places",
                AdapterStateStore.DEGRADED_REASON_KEY);
        harness.reset(satnogs.id(), AbsenceTombstoner.IDS_KEY, AbsenceTombstoner.MISSED_KEY);
        for (String id : List.of(donki.id(), fireballs.id(), cad.id(), sondehub.id())) {
            harness.reset(id);
        }
    }

    @AfterEach
    void tearDown() {
        for (String id : List.of(alerts.id(), kp.id(), rtsw.id(), xray.id(), aurora.id(), donki.id(), fireballs.id(),
                cad.id(), satnogs.id(), sondehub.id())) {
            harness.backoff.onSuccess(id);
        }
    }

    void reset(String sourceId, String... conditionalKeys) {
        for (String key : conditionalKeys) {
            harness.reset(sourceId, SourceHttp.ETAG_STATE_PREFIX + key, SourceHttp.LAST_MODIFIED_STATE_PREFIX + key);
        }
    }

    /** Serves {@code body} at {@code path} with an ETag, and a 304 to a request that presents it. */
    static void serveConditional(String path, byte[] body, String etag) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).atPriority(5).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withHeader("ETag", etag)
                .withHeader("Cache-Control", "max-age=60").withBody(body)));
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).atPriority(1).withHeader("If-None-Match", equalTo(etag))
                .willReturn(aResponse().withStatus(304).withHeader("ETag", etag)));
    }

    static void serve(String path, byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }

    @Test
    @DisplayName("swpc-alerts and swpc-kp: both files conditional; the validators come back and both 304 is unchanged")
    void alertsAndKpConditional() throws Exception {
        serveConditional(SWPC + "/products/alerts.json", AdapterFixtures.bytes("swpc-alerts", "alerts.json"), "\"a1\"");
        serveConditional(SWPC + "/products/noaa-scales.json", AdapterFixtures.bytes("swpc-alerts", "noaa-scales.json"),
                "\"s1\"");
        FetchResult first = alerts.fetch(harness.context(alerts, Map.of()));
        assertThat(first.records()).hasSize(83);
        assertThat(first.records()).filteredOn(r -> r.kind().equals("space-weather-scales")).hasSize(1);
        assertThat(alerts.fetch(harness.context(alerts, Map.of())).notModified()).isTrue();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(SWPC + "/products/alerts.json"))
                .withHeader("If-None-Match", equalTo("\"a1\"")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(SWPC + "/products/noaa-scales.json"))
                .withHeader("If-None-Match", equalTo("\"s1\"")));

        serveConditional(SWPC + "/json/planetary_k_index_1m.json",
                AdapterFixtures.bytes("swpc-kp", "planetary_k_index_1m.json"), "\"k1\"");
        serveConditional(SWPC + "/products/noaa-planetary-k-index.json",
                AdapterFixtures.bytes("swpc-kp", "noaa-planetary-k-index.json"), "\"k3\"");
        assertThat(kp.fetch(harness.context(kp, Map.of())).records()).extracting(NormalizedRecord::externalId)
                .containsExactly("kp-estimated", "kp-official");
        assertThat(kp.fetch(harness.context(kp, Map.of())).notModified()).isTrue();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(SWPC + "/json/planetary_k_index_1m.json"))
                .withHeader("If-None-Match", equalTo("\"k1\"")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(SWPC + "/products/noaa-planetary-k-index.json"))
                .withHeader("If-None-Match", equalTo("\"k3\"")));

        // Only the estimate changed: one record, the official file answers 304.
        WIREMOCK.resetAll();
        serveConditional(SWPC + "/json/planetary_k_index_1m.json",
                AdapterFixtures.bytes("swpc-kp", "planetary_k_index_1m.json"), "\"k2\"");
        serveConditional(SWPC + "/products/noaa-planetary-k-index.json",
                AdapterFixtures.bytes("swpc-kp", "noaa-planetary-k-index.json"), "\"k3\"");
        assertThat(kp.fetch(harness.context(kp, Map.of())).records()).extracting(NormalizedRecord::externalId)
                .containsExactly("kp-estimated");
    }

    @Test
    @DisplayName("swpc-xray: conditional; an unparsable file forgets the validators and fails")
    void xrayConditional() throws Exception {
        serveConditional(SWPC + "/json/goes/primary/xray-flares-latest.json",
                AdapterFixtures.bytes("swpc-xray", "xray-flares-latest.json"), "\"x1\"");
        assertThat(xray.fetch(harness.context(xray, Map.of())).records()).hasSize(2);
        assertThat(xray.fetch(harness.context(xray, Map.of())).notModified()).isTrue();

        WIREMOCK.resetAll();
        serveConditional(SWPC + "/json/goes/primary/xray-flares-latest.json",
                "<html>maintenance</html>".getBytes(StandardCharsets.UTF_8), "\"x2\"");
        assertThatThrownBy(() -> xray.fetch(harness.context(xray, Map.of())))
                .isInstanceOf(ai.foresignal.source.spi.UpstreamFormatException.class);
        assertThat(harness.client.stateStore(xray.id()).get(SourceHttp.ETAG_STATE_PREFIX + SwpcXrayAdapter.FLARES_KEY))
                .isEmpty();
    }

    @Test
    @DisplayName("swpc-rtsw: summaries every run, the 2.6 MB wind file at most once per densityInterval, all conditional")
    void rtswDensityCadence() throws Exception {
        String wind = SWPC + "/json/rtsw/rtsw_wind_1m.json";
        serveConditional(SWPC + "/products/summary/solar-wind-speed.json",
                AdapterFixtures.bytes("swpc-rtsw", "solar-wind-speed.json"), "\"v1\"");
        serveConditional(SWPC + "/products/summary/solar-wind-mag-field.json",
                AdapterFixtures.bytes("swpc-rtsw", "solar-wind-mag-field.json"), "\"m1\"");
        serveConditional(wind, AdapterFixtures.bytes("swpc-rtsw", "rtsw_wind_1m.json"), "\"w1\"");

        FetchResult first = rtsw.fetch(harness.context(rtsw, Map.of()));
        assertThat(first.records()).singleElement().satisfies(r -> assertThat(r.payload())
                .containsEntry("speedKms", 640L).containsEntry("densityCc", 4.0).containsEntry("spacecraft", "SOLAR1"));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(wind)));

        // Every 5 minutes for 55 minutes: the summaries are asked again (304), the wind file is not.
        for (int i = 0; i < 11; i++) {
            harness.clock.advance(Duration.ofMinutes(5));
            assertThat(rtsw.fetch(harness.context(rtsw, Map.of())).notModified()).isTrue();
        }
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(wind)));
        WIREMOCK.verify(12, getRequestedFor(urlPathEqualTo(SWPC + "/products/summary/solar-wind-speed.json")));

        // At 60 minutes the wind file is due: asked conditionally, 304, nothing else changed.
        harness.clock.advance(Duration.ofMinutes(5));
        assertThat(rtsw.fetch(harness.context(rtsw, Map.of())).notModified()).isTrue();
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(wind)));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(wind)).withHeader("If-None-Match", equalTo("\"w1\"")));

        // A new speed alone: a complete snapshot from the stored field and density.
        WIREMOCK.resetAll();
        serveConditional(SWPC + "/products/summary/solar-wind-speed.json",
                "[{\"proton_speed\": 702, \"time_tag\": \"2026-10-13T21:50:00Z\"}]".getBytes(StandardCharsets.UTF_8),
                "\"v2\"");
        serveConditional(SWPC + "/products/summary/solar-wind-mag-field.json",
                AdapterFixtures.bytes("swpc-rtsw", "solar-wind-mag-field.json"), "\"m1\"");
        harness.clock.advance(Duration.ofMinutes(5));
        FetchResult changed = rtsw.fetch(harness.context(rtsw, Map.of("densityInterval", Duration.ofMinutes(60))));
        assertThat(changed.records()).singleElement().satisfies(r -> {
            assertThat(r.payload()).containsEntry("speedKms", 700L).containsEntry("btNt", 6.0)
                    .containsEntry("densityCc", 4.0);
            assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T21:50:00Z"));
        });
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(wind)));
    }

    @Test
    @DisplayName("swpc-aurora: one record per place at the place point; without places nothing is fetched and the"
            + " health reason is 'no places'")
    void auroraPerPlace() throws Exception {
        String ovation = SWPC + "/json/ovation_aurora_latest.json";
        serveConditional(ovation, AdapterFixtures.bytes("swpc-aurora", "ovation_aurora_latest.json"), "\"o1\"");

        FetchResult none = aurora.fetch(harness.context(aurora, Map.of()));
        assertThat(none.records()).isEmpty();
        assertThat(none.complete()).isTrue();
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
        AdapterStateStore state = harness.client.stateStore(aurora.id());
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).contains(SwpcAuroraAdapter.NO_PLACES);

        places.add(new PlacePoints.Summary("tromso", "Tromso", 18.96, 69.65, List.of(), 1));
        places.add(new PlacePoints.Summary("yellowknife", "Yellowknife", -114.37, 62.45, List.of(), 1));
        FetchResult sampled = aurora.fetch(harness.context(aurora, Map.of()));
        assertThat(sampled.complete()).isTrue();
        assertThat(sampled.records()).extracting(NormalizedRecord::externalId).containsExactly("tromso", "yellowknife");
        assertThat(sampled.records().getFirst().geometry().getCoordinate().x).isEqualTo(18.96);
        assertThat(sampled.records().getFirst().payload()).containsEntry("placeName", "Tromso")
                .containsKeys("probability", "maxWithin300Km", "forecastTime");
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
        assertThat(aurora.fetch(harness.context(aurora, Map.of())).notModified()).isTrue();

        // A place is removed: tombstoned, and the grid read last time is sampled without a second request.
        places.removeIf(p -> p.id().equals("yellowknife"));
        FetchResult after = aurora.fetch(harness.context(aurora, Map.of()));
        assertThat(after.records()).extracting(NormalizedRecord::externalId, NormalizedRecord::tombstone)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("yellowknife", true),
                        org.assertj.core.groups.Tuple.tuple("tromso", false));
        WIREMOCK.verify(3, getRequestedFor(urlPathEqualTo(ovation)));

        // maxPlaces 1 keeps the first place by id and degrades.
        places.add(new PlacePoints.Summary("abisko", "Abisko", 18.83, 68.35, List.of(), 1));
        FetchResult capped = aurora.fetch(harness.context(aurora, Map.of("maxPlaces", 1)));
        assertThat(capped.records()).filteredOn(r -> !r.tombstone()).extracting(NormalizedRecord::externalId)
                .containsExactly("abisko");
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValueSatisfying(v ->
                assertThat(v).contains("maxPlaces"));
    }

    @Test
    @DisplayName("nasa-donki: one request per configured type over the look-back; a failing type does not stop the others")
    void donkiOneRequestPerType() throws Exception {
        for (String type : List.of("FLR", "CME", "GST", "IPS", "SEP")) {
            serve(DONKI + type, AdapterFixtures.bytes("nasa-donki", type + ".json"));
        }
        FetchResult result = donki.fetch(harness.context(donki, Map.of()));
        assertThat(result.records()).hasSize(7 + 25 + 1 + 5);
        for (String type : List.of("FLR", "CME", "GST", "IPS", "SEP")) {
            WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DONKI + type))
                    .withQueryParam("startDate", equalTo("2026-10-06")).withQueryParam("endDate", equalTo("2026-10-13")));
        }
        WIREMOCK.verify(5, anyRequestedFor(anyUrl()));

        WIREMOCK.resetAll();
        serve(DONKI + "notifications", AdapterFixtures.bytes("nasa-donki", "notifications.json"));
        WIREMOCK.stubFor(get(urlPathEqualTo(DONKI + "GST")).willReturn(aResponse().withStatus(400)
                .withBody("API Error: Date range cannot exceed 60 days. You requested 61 days.")));
        FetchResult partial = donki.fetch(harness.context(donki, Map.of("types", List.of("GST", "notifications"),
                "lookbackDays", 3)));
        assertThat(partial.records()).hasSize(21).allSatisfy(r -> assertThat(r.kind()).isEqualTo("space-weather-notice"));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(DONKI + "notifications"))
                .withQueryParam("startDate", equalTo("2026-10-10")));

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(DONKI + "FLR")).willReturn(aResponse().withStatus(200)
                .withBody("API Error: something")));
        assertThatThrownBy(() -> donki.fetch(harness.context(donki, Map.of("types", List.of("FLR")))))
                .isInstanceOf(ai.foresignal.source.spi.UpstreamFormatException.class);
    }

    @Test
    @DisplayName("satnogs-stations: follows Link rel=next on the same origin; absence tombstones after two complete lists")
    void satnogsLinkPaging() throws Exception {
        byte[] all = AdapterFixtures.bytes("satnogs-stations", "stations-online.json");
        var array = (tools.jackson.databind.node.ArrayNode) JSON.readTree(all);
        var page1 = JSON.createArrayNode();
        var page2 = JSON.createArrayNode();
        for (int i = 0; i < array.size(); i++) {
            (i < 12 ? page1 : page2).add(array.get(i));
        }
        String next = baseUrl() + SATNOGS + "?status=online&page=2";
        WIREMOCK.stubFor(get(urlPathEqualTo(SATNOGS)).withQueryParam("status", equalTo("online"))
                .atPriority(5).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("Link", "<" + next + ">; rel=\"next\"").withBody(JSON.writeValueAsBytes(page1))));
        WIREMOCK.stubFor(get(urlPathEqualTo(SATNOGS)).withQueryParam("page", equalTo("2"))
                .atPriority(1).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(JSON.writeValueAsBytes(page2))));
        FetchResult result = satnogs.fetch(harness.context(satnogs, Map.of()));
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).hasSize(20).noneMatch(NormalizedRecord::tombstone);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(SATNOGS)));

        // Station 26 is offline (missing) twice: tombstoned on the second complete list.
        WIREMOCK.resetAll();
        page1.remove(0);
        serve(SATNOGS, JSON.writeValueAsBytes(page1));
        assertThat(satnogs.fetch(harness.context(satnogs, Map.of())).records()).noneMatch(NormalizedRecord::tombstone);
        assertThat(satnogs.fetch(harness.context(satnogs, Map.of())).records()).filteredOn(NormalizedRecord::tombstone)
                .extracting(NormalizedRecord::externalId).contains("26");

        // A next link to another origin is not followed.
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(SATNOGS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("Link", "<https://elsewhere.example/api/stations/?page=2>; rel=\"next\"")
                .withBody(JSON.writeValueAsBytes(page2))));
        assertThat(satnogs.fetch(harness.context(satnogs, Map.of())).records()).hasSize(8);
        WIREMOCK.verify(1, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("JPL and SondeHub: the documented request parameters; SondeHub centers on the first place or fails")
    void requests() throws Exception {
        serve("/ssd-api.jpl.nasa.gov/fireball.api", AdapterFixtures.bytes("jpl-fireballs", "fireball.json"));
        assertThat(fireballs.fetch(harness.context(fireballs, Map.of())).records()).hasSize(18);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/ssd-api.jpl.nasa.gov/fireball.api"))
                .withQueryParam("date-min", equalTo("2026-09-13")).withQueryParam("req-loc", equalTo("true")));

        serve("/ssd-api.jpl.nasa.gov/cad.api", AdapterFixtures.bytes("jpl-close-approaches", "cad.json"));
        assertThat(cad.fetch(harness.context(cad, Map.of("distMaxAu", 0.02))).records()).hasSize(42);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/ssd-api.jpl.nasa.gov/cad.api"))
                .withQueryParam("date-min", equalTo("now")).withQueryParam("date-max", equalTo("+60"))
                .withQueryParam("dist-max", equalTo("0.02")).withQueryParam("fullname", equalTo("true")));

        assertThatThrownBy(() -> sondehub.fetch(harness.context(sondehub, Map.of())))
                .isInstanceOf(AdapterException.class).hasMessageContaining("center");
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/api.v2.sondehub.org/sondes")));
        serve("/api.v2.sondehub.org/sondes", AdapterFixtures.bytes("sondehub", "sondes.json"));
        places.add(new PlacePoints.Summary("b-cabin", "Cabin", 10.0, 50.0, List.of(), 1));
        places.add(new PlacePoints.Summary("a-home", "Home", 13.62, 52.26, List.of(), 1));
        assertThat(sondehub.fetch(harness.context(sondehub, Map.of())).records()).hasSize(11);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/api.v2.sondehub.org/sondes"))
                .withQueryParam("lat", equalTo("52.26")).withQueryParam("lon", equalTo("13.62"))
                .withQueryParam("distance", equalTo("500000")).withQueryParam("last", equalTo("600")));
        sondehub.fetch(harness.context(sondehub, Map.of("center", "47.1,9.7", "radiusKm", 100, "windowS", 1200)));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/api.v2.sondehub.org/sondes"))
                .withQueryParam("lat", equalTo("47.1")).withQueryParam("distance", equalTo("100000"))
                .withQueryParam("last", equalTo("1200")));
    }
}
