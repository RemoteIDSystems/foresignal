package ai.foresignal.source.adapters.hydro;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.headRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.adapters.hydro.coops.NoaaCoopsTidesAdapter;
import ai.foresignal.source.adapters.hydro.ea.EaFloodWarningsAdapter;
import ai.foresignal.source.adapters.hydro.eccc.EcccHydrometricAdapter;
import ai.foresignal.source.adapters.hydro.nwps.NwpsGaugesAdapter;
import ai.foresignal.source.adapters.hydro.snotel.SnotelAdapter;
import ai.foresignal.source.adapters.hydro.usdm.UsdmAdapter;
import ai.foresignal.source.adapters.hydro.usgswater.UsgsWaterAdapter;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 079 adapters against stubbed upstreams through the real upstream client: USGS cursor paging, the
 * {@code X-Api-Key} header (never in a trace, exchange or log line) and the daily name refresh; NWPS threshold details
 * at most once per gauge per day under a {@link MutableClock}; the USDM 304 path and the HEAD confirmation of the map
 * date; CO-OPS requests that always carry {@code application=Foresignal} and unknown stations; SNOTEL batches and its
 * daily station list; EA polygons fetched once and vanished warnings tombstoned; ECCC paging with the newest row per
 * station. No adapter calls {@code waterservices.usgs.gov}.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class HydroAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T22:15:00Z");
    static final String USGS = "/api.waterdata.usgs.gov/ogcapi/v1/collections/";
    static final String NWPS = "/api.water.noaa.gov/nwps/v1/gauges";
    static final String USDM = "/droughtmonitor.unl.edu/data/json/";
    static final String DATA = "/api.tidesandcurrents.noaa.gov/api/prod/datagetter";
    static final String MD = "/api.tidesandcurrents.noaa.gov/mdapi/prod/webapi";
    static final String AWDB = "/wcc.sc.egov.usda.gov/awdbRestApi/services/v1";
    static final String EA = "/environment.data.gov.uk/flood-monitoring/id/";
    static final String ECCC = "/api.weather.gc.ca/collections/hydrometric-realtime/items";
    static final String USGS_KEY = "usgs-0123456789abcdefSECRETKEY";
    static final String CURSOR = "MDUxNGViMGItY2EzMi00NzJkLWEzOTYtZjJhYWQwYWE2NzU0";
    static final Envelope DENVER = new Envelope(-105.5, -104.5, 39.5, 40.2);

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    UsgsWaterAdapter usgs;
    NwpsGaugesAdapter nwps;
    UsdmAdapter usdm;
    NoaaCoopsTidesAdapter coops;
    SnotelAdapter snotel;
    EaFloodWarningsAdapter ea;
    EcccHydrometricAdapter eccc;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        usgs = new UsgsWaterAdapter(baseUrl(), () -> null);
        nwps = new NwpsGaugesAdapter(baseUrl(), () -> null);
        usdm = new UsdmAdapter(baseUrl());
        coops = new NoaaCoopsTidesAdapter(baseUrl());
        snotel = new SnotelAdapter(baseUrl(), () -> null);
        ea = new EaFloodWarningsAdapter(baseUrl(), () -> null);
        eccc = new EcccHydrometricAdapter(baseUrl(), () -> null);
        harness.reset(usgs.id(), UsgsWaterAdapter.NAMES_KEY, UsgsWaterAdapter.NAMES_AT_KEY,
                UsgsWaterAdapter.RATE_LIMIT_KEY);
        harness.reset(nwps.id(), NwpsGaugesAdapter.INDEX_KEY, NwpsGaugesAdapter.DETAIL_PREFIX + "BBCC2",
                NwpsGaugesAdapter.DETAIL_PREFIX + "CGCC2", NwpsGaugesAdapter.DETAIL_PREFIX + "DNVC2",
                NwpsGaugesAdapter.DETAIL_PREFIX + "GSEC2");
        harness.reset(usdm.id(), SourceHttp.ETAG_STATE_PREFIX + UsdmAdapter.STATE_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + UsdmAdapter.STATE_KEY);
        harness.reset(coops.id(), NoaaCoopsTidesAdapter.STATIONS_KEY, NoaaCoopsTidesAdapter.STATIONS_AT_KEY,
                NoaaCoopsTidesAdapter.PRED_PREFIX + "8518750", NoaaCoopsTidesAdapter.PRED_AT_PREFIX + "8518750",
                NoaaCoopsTidesAdapter.FLOOD_PREFIX + "8518750", NoaaCoopsTidesAdapter.FLOOD_AT_PREFIX + "8518750",
                AdapterStateStore.DEGRADED_REASON_KEY);
        harness.reset(snotel.id(), SnotelAdapter.STATIONS_KEY, SnotelAdapter.STATIONS_AT_KEY, SnotelAdapter.QUERY_KEY);
        harness.reset(ea.id(), SourceHttp.ETAG_STATE_PREFIX + EaFloodWarningsAdapter.STATE_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + EaFloodWarningsAdapter.STATE_KEY,
                EaFloodWarningsAdapter.ACTIVE_KEY, EaFloodWarningsAdapter.POLYGON_INDEX_KEY,
                EaFloodWarningsAdapter.POLYGON_PREFIX + "054WACDV4A");
        harness.reset(eccc.id());
        harness.credentials.clear();
    }

    @AfterEach
    void tearDown() {
        for (String id : List.of(usgs.id(), nwps.id(), usdm.id(), coops.id(), snotel.id(), ea.id(), eccc.id())) {
            harness.backoff.onSuccess(id);
        }
        harness.credentials.clear();
    }

    static void serve(String path, byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }

    static byte[] rebased(String sourceId, String name, String host) {
        return AdapterFixtures.text(sourceId, name).replace("https://" + host, baseUrl() + "/" + host)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("usgs-water: cursor paging, X-Api-Key never logged or traced, names once a day, rate limit kept")
    void usgsPagingAndKey(CapturedOutput output) throws Exception {
        harness.credentials.put(UsgsWaterAdapter.API_KEY, USGS_KEY);
        WIREMOCK.stubFor(get(urlPathEqualTo(USGS + "latest-continuous/items")).withQueryParam("cursor", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("X-RateLimit-Remaining", "998")
                        .withBody(rebased("usgs-water", "latest-page1.json", "api.waterdata.usgs.gov"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(USGS + "latest-continuous/items")).withQueryParam("cursor", equalTo(CURSOR))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("X-RateLimit-Remaining", "997")
                        .withBody(AdapterFixtures.bytes("usgs-water", "latest-page2.json"))));
        serve(USGS + "monitoring-locations/items", AdapterFixtures.bytes("usgs-water", "monitoring-locations.json"));
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(usgs, Map.of("bbox", DENVER), recorder);
        FetchResult result = usgs.fetch(ctx);
        assertThat(result.records()).hasSize(8)
                .allSatisfy(r -> assertThat(r.payload()).containsKey("siteName"));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(USGS + "latest-continuous/items"))
                .withQueryParam("cursor", absent()).withQueryParam("f", equalTo("json"))
                .withQueryParam("limit", equalTo("1000")).withQueryParam("parameter_code", equalTo("00060,00065"))
                .withQueryParam("bbox", equalTo("-105.5,39.5,-104.5,40.2"))
                .withHeader("X-Api-Key", equalTo(USGS_KEY)));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(USGS + "latest-continuous/items"))
                .withQueryParam("cursor", equalTo(CURSOR)).withHeader("X-Api-Key", equalTo(USGS_KEY)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(USGS + "monitoring-locations/items"))
                .withQueryParam("skipGeometry", equalTo("true")).withHeader("X-Api-Key", equalTo(USGS_KEY)));
        WIREMOCK.verify(0, getRequestedFor(urlPathMatching(".*waterservices.*")));
        assertThat(harness.client.stateStore(usgs.id()).get(UsgsWaterAdapter.RATE_LIMIT_KEY)).contains("997");
        assertThat(ctx.traces()).isNotEmpty().extracting(FetchTrace::redactedUrl)
                .allSatisfy(url -> assertThat(url).doesNotContain(USGS_KEY));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(USGS_KEY);
        }
        assertThat(output.getAll()).doesNotContain(USGS_KEY);

        usgs.fetch(harness.context(usgs, Map.of("bbox", DENVER)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(USGS + "monitoring-locations/items")));
        harness.clock.advance(Duration.ofHours(24));
        usgs.fetch(harness.context(usgs, Map.of("bbox", DENVER)));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(USGS + "monitoring-locations/items")));
    }

    @Test
    @DisplayName("usgs-water: without a key no X-Api-Key; a site list replaces the box; no box fails before a request")
    void usgsSitesAndNoKey() throws Exception {
        assertThatThrownBy(() -> usgs.fetch(harness.context(usgs, Map.of()))).isInstanceOf(AdapterException.class)
                .hasMessageContaining("bbox");
        WIREMOCK.verify(0, getRequestedFor(anyUrl()));
        serve(USGS + "latest-continuous/items", AdapterFixtures.bytes("usgs-water", "latest-page2.json"));
        serve(USGS + "monitoring-locations/items", AdapterFixtures.bytes("usgs-water", "monitoring-locations.json"));
        assertThat(usgs.fetch(harness.context(usgs, Map.of("monitoringLocations",
                List.of("USGS-06711780", "USGS-06719840")))).records()).hasSize(4);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(USGS + "latest-continuous/items"))
                .withQueryParam("monitoring_location_id", equalTo("USGS-06711780,USGS-06719840"))
                .withQueryParam("bbox", absent()).withHeader("X-Api-Key", absent()));
    }

    @Test
    @DisplayName("nwps-gauges: each gauge's detail at most once per day; a 404 caches no thresholds")
    void nwpsDetailsOncePerDay() throws Exception {
        serve(NWPS, AdapterFixtures.bytes("nwps-gauges", "gauges-denver.json"));
        serve(NWPS + "/DNVC2", AdapterFixtures.bytes("nwps-gauges", "detail-DNVC2.json"));
        WIREMOCK.stubFor(get(urlPathMatching(NWPS + "/(BBCC2|CGCC2|GSEC2)")).willReturn(aResponse().withStatus(404)
                .withBody("{\"code\": 5, \"message\": \"not found\"}")));
        Map<String, Object> params = Map.of("bbox", DENVER);
        FetchResult first = nwps.fetch(harness.context(nwps, params));
        assertThat(first.records()).extracting(NormalizedRecord::externalId).containsExactly("BBCC2", "CGCC2", "DNVC2");
        assertThat(first.records().stream().filter(r -> r.externalId().equals("DNVC2")).findFirst().orElseThrow()
                .payload()).containsEntry("minorStage", 11.0).containsEntry("stageToMinor", 8.53);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(NWPS)).withQueryParam("bbox.xmin", equalTo("-105.5"))
                .withQueryParam("bbox.ymax", equalTo("40.2")).withQueryParam("srid", equalTo("EPSG_4326")));
        WIREMOCK.verify(4, getRequestedFor(urlPathMatching(NWPS + "/.+")));

        nwps.fetch(harness.context(nwps, params));
        harness.clock.advance(Duration.ofHours(23));
        nwps.fetch(harness.context(nwps, params));
        WIREMOCK.verify(4, getRequestedFor(urlPathMatching(NWPS + "/.+")));
        harness.clock.advance(Duration.ofHours(1));
        nwps.fetch(harness.context(nwps, params));
        WIREMOCK.verify(8, getRequestedFor(urlPathMatching(NWPS + "/.+")));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(NWPS + "/DNVC2")));
    }

    @Test
    @DisplayName("nwps-gauges: maxGauges keeps the first gauges by lid and their details only")
    void nwpsCap() throws Exception {
        serve(NWPS, AdapterFixtures.bytes("nwps-gauges", "gauges-denver.json"));
        WIREMOCK.stubFor(get(urlPathMatching(NWPS + "/.+")).willReturn(aResponse().withStatus(404)));
        FetchResult result = nwps.fetch(harness.context(nwps, Map.of("bbox", DENVER, "maxGauges", 2)));
        assertThat(result.complete()).isFalse();
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("BBCC2", "CGCC2");
        WIREMOCK.verify(2, getRequestedFor(urlPathMatching(NWPS + "/.+")));
    }

    @Test
    @DisplayName("usdm: 200 with a HEAD-confirmed map date and tombstones for absent categories; then a 304")
    void usdmConditionalAndHead() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(USDM + "usdm_current.json")).withHeader("If-None-Match", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("ETag", "\"7f7f5dbe57dd1:0\"")
                        .withHeader("Last-Modified", "Thu, 08 Oct 2026 10:16:12 GMT")
                        .withBody(AdapterFixtures.bytes("usdm", "usdm_current-d3d4.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(USDM + "usdm_current.json"))
                .withHeader("If-None-Match", equalTo("\"7f7f5dbe57dd1:0\"")).willReturn(aResponse().withStatus(304)));
        WIREMOCK.stubFor(head(urlPathEqualTo(USDM + "usdm_20261006.json")).willReturn(aResponse().withStatus(200)));
        FetchResult first = usdm.fetch(harness.context(usdm, Map.of()));
        assertThat(first.records()).filteredOn(r -> !r.tombstone()).extracting(NormalizedRecord::externalId)
                .containsExactly("D3", "D4");
        assertThat(first.records()).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactly("D0", "D1", "D2");
        assertThat(first.records()).filteredOn(r -> !r.tombstone())
                .allSatisfy(r -> assertThat(r.payload()).containsEntry("mapDate", "2026-10-06"));
        WIREMOCK.verify(1, headRequestedFor(urlPathEqualTo(USDM + "usdm_20261006.json")));

        FetchResult second = usdm.fetch(harness.context(usdm, Map.of()));
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(1, headRequestedFor(urlPathEqualTo(USDM + "usdm_20261006.json")));
    }

    @Test
    @DisplayName("usdm: a 404 for the computed date steps back one week")
    void usdmHeadMissing() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(USDM + "usdm_current.json")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("Last-Modified", "Thu, 08 Oct 2026 10:16:12 GMT")
                .withBody(AdapterFixtures.bytes("usdm", "usdm_current-d3d4.json"))));
        WIREMOCK.stubFor(head(urlPathEqualTo(USDM + "usdm_20261006.json")).willReturn(aResponse().withStatus(404)));
        FetchResult result = usdm.fetch(harness.context(usdm, Map.of()));
        assertThat(result.records()).filteredOn(r -> !r.tombstone())
                .allSatisfy(r -> assertThat(r.payload()).containsEntry("mapDate", "2026-09-29"));
    }

    @Test
    @DisplayName("noaa-coops-tides: application=Foresignal on every data request; unknown stations degrade the source")
    void coopsApplicationAndValidation() throws Exception {
        serve(MD + "/stations.json", AdapterFixtures.bytes("noaa-coops-tides", "stations-waterlevels.json"));
        WIREMOCK.stubFor(get(urlPathEqualTo(DATA)).withQueryParam("product", equalTo("water_level"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("noaa-coops-tides", "water_level-8518750.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(DATA)).withQueryParam("product", equalTo("predictions"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("noaa-coops-tides", "predictions-8518750.json"))));
        serve(MD + "/stations/8518750/floodlevels.json",
                AdapterFixtures.bytes("noaa-coops-tides", "floodlevels-8518750.json"));
        serve(MD + "/stations/8518750/datums.json", AdapterFixtures.bytes("noaa-coops-tides", "datums-8518750.json"));
        DefaultFetchContext ctx = harness.context(coops, Map.of("stations", List.of("8518750", "9063020")));
        FetchResult result = coops.fetch(ctx);
        assertThat(result.records()).singleElement().satisfies(r -> assertThat(r.payload())
                .containsEntry("floodCategory", "none").containsEntry("minorFloodM", 2.103)
                .containsEntry("nextHighTime", "2026-10-14T02:56:00Z"));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(DATA)).withQueryParam("application", equalTo("Foresignal"))
                .withQueryParam("station", equalTo("8518750")).withQueryParam("datum", equalTo("MLLW"))
                .withQueryParam("time_zone", equalTo("gmt")).withQueryParam("units", equalTo("metric")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(DATA)).withQueryParam("application", absent()));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(DATA)).withQueryParam("station", equalTo("9063020")));
        assertThat(harness.client.stateStore(coops.id()).get(AdapterStateStore.DEGRADED_REASON_KEY).orElseThrow())
                .contains("9063020");

        // Predictions every 6 h and thresholds daily: the next run only reads the water level.
        coops.fetch(harness.context(coops, Map.of("stations", List.of("8518750"))));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DATA)).withQueryParam("product", equalTo("predictions")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(MD + "/stations/8518750/floodlevels.json")));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(DATA)).withQueryParam("product", equalTo("water_level")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(MD + "/stations.json")));
        assertThat(harness.client.stateStore(coops.id()).get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
    }

    @Test
    @DisplayName("noaa-coops-tides: a CO-OPS error object for every station fails the run")
    void coopsErrorObject() {
        serve(MD + "/stations.json", AdapterFixtures.bytes("noaa-coops-tides", "stations-waterlevels.json"));
        serve(DATA, AdapterFixtures.bytes("noaa-coops-tides", "error-invalid-station.json"));
        WIREMOCK.stubFor(get(urlPathMatching(MD + "/stations/.+")).willReturn(aResponse().withStatus(404)));
        assertThatThrownBy(() -> coops.fetch(harness.context(coops, Map.of("stations", List.of("8518750")))))
                .isInstanceOf(AdapterException.class).hasMessageContaining("not a valid station");
    }

    @Test
    @DisplayName("snotel: the station list once a day, one data request per 50 stations")
    void snotelBatches() throws Exception {
        serve(AWDB + "/stations", AdapterFixtures.bytes("snotel", "stations.json"));
        serve(AWDB + "/data", AdapterFixtures.bytes("snotel", "data.json"));
        Map<String, Object> params = Map.of("states", List.of("CO"));
        assertThat(snotel.fetch(harness.context(snotel, params)).records()).hasSize(3);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(AWDB + "/stations"))
                .withQueryParam("stationTriplets", equalTo("*:CO:SNTL")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(AWDB + "/data"))
                .withQueryParam("stationTriplets", equalTo("1030:CO:SNTL,1344:CO:SNTL,335:CO:SNTL"))
                .withQueryParam("elements", equalTo("WTEQ,SNWD,PREC,TOBS")).withQueryParam("duration", equalTo("DAILY"))
                .withQueryParam("beginDate", equalTo("2026-10-11")).withQueryParam("endDate", equalTo("2026-10-13")));
        snotel.fetch(harness.context(snotel, params));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(AWDB + "/stations")));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(AWDB + "/data")));
        snotel.fetch(harness.context(snotel, Map.of("states", List.of("UT"))));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(AWDB + "/stations")));
    }

    @Test
    @DisplayName("ea-flood-warnings: each polygon fetched once; a warning that disappears is tombstoned")
    void eaPolygonsAndRetirement() throws Exception {
        serve(EA + "floods", AdapterFixtures.bytes("ea-flood-warnings", "floods.json"));
        serve(EA + "floodAreas/054WACDV4A/polygon",
                AdapterFixtures.bytes("ea-flood-warnings", "polygon-054WACDV4A.json"));
        WIREMOCK.stubFor(get(urlPathMatching(EA + "floodAreas/(?!054WACDV4A/).+/polygon"))
                .willReturn(aResponse().withStatus(404)));
        FetchResult first = ea.fetch(harness.context(ea, Map.of()));
        assertThat(first.records()).hasSize(6);
        assertThat(first.records().stream().filter(r -> r.externalId().equals("054WACDV4A")).findFirst()
                .orElseThrow().geometry()).isNotNull();
        WIREMOCK.verify(6, getRequestedFor(urlPathMatching(EA + "floodAreas/.+/polygon")));

        ea.fetch(harness.context(ea, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(EA + "floodAreas/054WACDV4A/polygon")));

        serve(EA + "floods", "{\"meta\": {}, \"items\": []}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        FetchResult gone = ea.fetch(harness.context(ea, Map.of()));
        assertThat(gone.records()).hasSize(6).allSatisfy(r -> assertThat(r.tombstone()).isTrue());
    }

    @Test
    @DisplayName("eccc-hydrometric: two pages, the box and window sent, the newest row per station kept")
    void ecccLatestPerStation() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(ECCC)).withQueryParam("offset", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(rebased("eccc-hydrometric", "items-page1.json", "api.weather.gc.ca"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(ECCC)).withQueryParam("offset", equalTo("5"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("eccc-hydrometric", "items-page2.json"))));
        FetchResult result = eccc.fetch(harness.context(eccc, Map.of("bbox", new Envelope(-76, -75, 45, 45.6))));
        assertThat(result.records()).hasSize(7);
        assertThat(result.records().stream().filter(r -> r.externalId().equals("02LB022")).findFirst().orElseThrow()
                .observedAt()).isEqualTo(Instant.parse("2026-10-13T21:45:00Z"));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(ECCC)).withQueryParam("offset", absent())
                .withQueryParam("bbox", equalTo("-76,45,-75,45.6"))
                .withQueryParam("datetime", equalTo("2026-10-13T20:15:00Z/.."))
                .withQueryParam("sortby", equalTo("-DATETIME")));
        assertThatThrownBy(() -> eccc.fetch(harness.context(eccc, Map.of()))).isInstanceOf(AdapterException.class);
    }
}
