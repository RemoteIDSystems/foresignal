package ai.foresignal.source.adapters.env;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
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
import ai.foresignal.source.adapters.env.airnow.AirNowHourlyAdapter;
import ai.foresignal.source.adapters.env.ndbc.NdbcBuoysAdapter;
import ai.foresignal.source.adapters.env.openaq.OpenAqAdapter;
import ai.foresignal.source.adapters.env.openaq.OpenAqParameterResolver;
import ai.foresignal.source.adapters.env.radnet.EpaRadnetAdapter;
import ai.foresignal.source.adapters.env.safecast.SafecastRadiationAdapter;
import ai.foresignal.source.adapters.env.sensorcommunity.SensorCommunityAdapter;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.nio.charset.StandardCharsets;
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
 * The prompt 078 polling adapters against stubbed upstreams through the real upstream client: the OpenAQ key header
 * and the AirNow key query parameter (both redacted from traces, recorded exchanges and the log), the OpenAQ
 * parameter cache, paging and {@code bbox} filter, NDBC's conditional GET and its daily station-list request,
 * Sensor.Community's {@code area} validation (and that no global dump is requested), RadNet's one request per
 * station, and Safecast's single request.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class EnvAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T21:35:00Z");
    static final String OPENAQ = "/api.openaq.org/v3/parameters";
    static final String AIRNOW = "/www.airnowapi.org/aq/data/";
    static final String NDBC = "/www.ndbc.noaa.gov";
    static final String SC = "/data.sensor.community";
    static final String RADNET = "/radnet.epa.gov/cdx-radnet-rest/api/rest/csv/2026/fixed/";
    static final String OPENAQ_KEY = "oaq-0123456789abcdefSECRET";
    static final String AIRNOW_KEY = "AN-0123-4567-89AB-SECRET";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    OpenAqAdapter openaq;
    AirNowHourlyAdapter airnow;
    NdbcBuoysAdapter ndbc;
    SensorCommunityAdapter sensorCommunity;
    EpaRadnetAdapter radnet;
    SafecastRadiationAdapter safecast;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        openaq = new OpenAqAdapter(baseUrl(), () -> null);
        airnow = new AirNowHourlyAdapter(baseUrl(), () -> null);
        ndbc = new NdbcBuoysAdapter(baseUrl(), () -> null);
        sensorCommunity = new SensorCommunityAdapter(baseUrl(), () -> null);
        radnet = new EpaRadnetAdapter(baseUrl(), () -> null);
        safecast = new SafecastRadiationAdapter(baseUrl(), () -> null);
        harness.reset(openaq.id(), OpenAqParameterResolver.CACHE_KEY, OpenAqParameterResolver.CACHED_AT_KEY);
        harness.reset(ndbc.id(), SourceHttp.ETAG_STATE_PREFIX + NdbcBuoysAdapter.OBS_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + NdbcBuoysAdapter.OBS_KEY, NdbcBuoysAdapter.STATIONS_KEY,
                NdbcBuoysAdapter.STATIONS_AT_KEY);
        harness.reset(radnet.id(), EpaRadnetAdapter.ROLLOVER_PREFIX + "CO/DENVER",
                EpaRadnetAdapter.ROLLOVER_PREFIX + "HI/HONOLULU");
        for (String id : List.of(airnow.id(), sensorCommunity.id(), safecast.id())) {
            harness.reset(id);
        }
        harness.credentials.clear();
    }

    @AfterEach
    void tearDown() {
        for (String id : List.of(openaq.id(), airnow.id(), ndbc.id(), sensorCommunity.id(), radnet.id(), safecast.id())) {
            harness.backoff.onSuccess(id);
        }
        harness.credentials.clear();
    }

    static void serve(String path, byte[] body, String contentType) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", contentType).withBody(body)));
    }

    /** A {@code latest} page of {@code n} results at distinct locations inside Berlin, measured at 21:00. */
    static byte[] latestPage(int n, int firstLocation) {
        StringBuilder s = new StringBuilder("{\"meta\": {\"page\": 1, \"limit\": 1000}, \"results\": [");
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                s.append(',');
            }
            s.append("{\"datetime\": {\"utc\": \"2026-10-13T21:00:00Z\", \"local\": \"2026-10-13T23:00:00+02:00\"},"
                    + " \"value\": 5.0, \"coordinates\": {\"latitude\": 52.5, \"longitude\": 13.4}, \"sensorsId\": ")
                    .append(100000 + firstLocation + i).append(", \"locationsId\": ").append(firstLocation + i)
                    .append('}');
        }
        return s.append("]}").toString().getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("openaq: no key, no request; the key in X-API-Key and never in a trace, exchange or log line")
    void openaqKey(CapturedOutput output) throws Exception {
        assertThatThrownBy(() -> openaq.fetch(harness.context(openaq, Map.of())))
                .isInstanceOf(AdapterException.class).hasMessageContaining("apiKey");
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));

        harness.credentials.put(OpenAqAdapter.API_KEY, OPENAQ_KEY);
        serve(OPENAQ, AdapterFixtures.bytes("openaq", "parameters.json"), "application/json");
        serve(OPENAQ + "/2/latest", AdapterFixtures.bytes("openaq", "latest-2-page1.json"), "application/json");
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(openaq, Map.of(), recorder);
        FetchResult result = openaq.fetch(ctx);
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("2178:pm25", "4761:pm25", "8118:pm25");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(OPENAQ)).withHeader("X-API-Key", equalTo(OPENAQ_KEY)));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(OPENAQ + "/2/latest"))
                .withHeader("X-API-Key", equalTo(OPENAQ_KEY))
                .withQueryParam("limit", equalTo("1000")).withQueryParam("page", equalTo("1"))
                .withQueryParam("datetime_min", equalTo("2026-10-13T15:35:00Z")));
        assertThat(ctx.traces()).isNotEmpty().extracting(FetchTrace::redactedUrl)
                .allSatisfy(url -> assertThat(url).doesNotContain(OPENAQ_KEY));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(OPENAQ_KEY);
        }
        assertThat(output.getAll()).doesNotContain(OPENAQ_KEY);

        // The parameter list is cached for 24 h: the second run asks only for the latest values.
        openaq.fetch(harness.context(openaq, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(OPENAQ)));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(OPENAQ + "/2/latest")));
        harness.clock.advance(Duration.ofHours(24));
        openaq.fetch(harness.context(openaq, Map.of()));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(OPENAQ)));
    }

    @Test
    @DisplayName("openaq: a full page asks for the next; bbox filters after parsing; two ids for o3")
    void openaqPagingAndBox() throws Exception {
        harness.credentials.put(OpenAqAdapter.API_KEY, OPENAQ_KEY);
        serve(OPENAQ, AdapterFixtures.bytes("openaq", "parameters.json"), "application/json");
        WIREMOCK.stubFor(get(urlPathEqualTo(OPENAQ + "/2/latest")).withQueryParam("page", equalTo("1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(latestPage(1000, 1))));
        WIREMOCK.stubFor(get(urlPathEqualTo(OPENAQ + "/2/latest")).withQueryParam("page", equalTo("2"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(latestPage(3, 5001))));
        assertThat(openaq.fetch(harness.context(openaq, Map.of())).records()).hasSize(1003);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(OPENAQ + "/2/latest")));

        WIREMOCK.resetAll();
        serve(OPENAQ + "/2/latest", AdapterFixtures.bytes("openaq", "latest-2-page1.json"), "application/json");
        Envelope berlin = new Envelope(13.0, 14.0, 52.0, 53.0);
        assertThat(openaq.fetch(harness.context(openaq, Map.of("bbox", berlin))).records())
                .extracting(NormalizedRecord::externalId).containsExactly("4761:pm25");

        serve(OPENAQ + "/3/latest", latestPage(1, 1), "application/json");
        serve(OPENAQ + "/10/latest", latestPage(1, 2), "application/json");
        assertThat(openaq.fetch(harness.context(openaq, Map.of("parameters", List.of("o3")))).records())
                .extracting(NormalizedRecord::externalId).containsExactly("1:o3", "2:o3");
    }

    @Test
    @DisplayName("airnow-hourly: key and bbox required; API_KEY in the query, never in a trace, exchange or log line")
    void airnowKey(CapturedOutput output) throws Exception {
        Envelope box = new Envelope(-105.5, -104.5, 39.5, 40.5);
        assertThatThrownBy(() -> airnow.fetch(harness.context(airnow, Map.of("bbox", box))))
                .isInstanceOf(AdapterException.class).hasMessageContaining("apiKey");
        harness.credentials.put(AirNowHourlyAdapter.API_KEY, AIRNOW_KEY);
        assertThatThrownBy(() -> airnow.fetch(harness.context(airnow, Map.of())))
                .isInstanceOf(AdapterException.class).hasMessageContaining("bbox");
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));

        serve(AIRNOW, AdapterFixtures.bytes("airnow-hourly", "data.json"), "application/json");
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(airnow, Map.of("bbox", box), recorder);
        assertThat(airnow.fetch(ctx).records()).hasSize(4);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(AIRNOW)).withQueryParam("API_KEY", equalTo(AIRNOW_KEY))
                .withQueryParam("startDate", equalTo("2026-10-13T20")).withQueryParam("endDate", equalTo("2026-10-13T20"))
                .withQueryParam("parameters", equalTo("PM25,OZONE"))
                .withQueryParam("BBOX", equalTo("-105.5,39.5,-104.5,40.5")));
        assertThat(ctx.traces()).isNotEmpty().extracting(FetchTrace::redactedUrl)
                .allSatisfy(url -> assertThat(url).doesNotContain(AIRNOW_KEY));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(AIRNOW_KEY);
        }
        assertThat(output.getAll()).doesNotContain(AIRNOW_KEY);
    }

    @Test
    @DisplayName("ndbc-buoys: conditional GET of latest_obs.txt; activestations.xml once per 24 h")
    void ndbcConditionalAndStations() throws Exception {
        String obs = NDBC + "/data/latest_obs/latest_obs.txt";
        String stations = NDBC + "/activestations.xml";
        WIREMOCK.stubFor(get(urlPathEqualTo(obs)).atPriority(5).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/plain; charset=ISO-8859-1").withHeader("ETag", "\"o1\"")
                .withBody(AdapterFixtures.bytes("ndbc-buoys", "latest_obs.txt"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(obs)).atPriority(1).withHeader("If-None-Match", equalTo("\"o1\""))
                .willReturn(aResponse().withStatus(304).withHeader("ETag", "\"o1\"")));
        serve(stations, AdapterFixtures.bytes("ndbc-buoys", "activestations.xml"), "text/xml");

        FetchResult first = ndbc.fetch(harness.context(ndbc, Map.of()));
        assertThat(first.records()).hasSize(38);
        assertThat(first.records()).filteredOn(r -> r.externalId().equals("41004")).singleElement()
                .satisfies(r -> assertThat(r.payload()).containsEntry("stationType", "buoy"));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(stations)));

        harness.clock.advance(Duration.ofMinutes(10));
        assertThat(ndbc.fetch(harness.context(ndbc, Map.of())).notModified()).isTrue();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(obs)).withHeader("If-None-Match", equalTo("\"o1\"")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(stations)));

        harness.clock.advance(Duration.ofHours(24));
        ndbc.fetch(harness.context(ndbc, Map.of()));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(stations)));
    }

    @Test
    @DisplayName("sensor-community: an invalid area makes no request; a valid one asks the filter API, never a dump")
    void sensorCommunityArea() throws Exception {
        assertThatThrownBy(() -> sensorCommunity.fetch(harness.context(sensorCommunity, Map.of("area", "box=0,0,20,20"))))
                .isInstanceOf(AdapterException.class).hasMessageContaining("10");
        assertThatThrownBy(() -> sensorCommunity.fetch(harness.context(sensorCommunity, Map.of())))
                .isInstanceOf(AdapterException.class);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));

        serve(SC + "/airrohr/v1/filter/box=52.3,13.0,52.7,13.8",
                AdapterFixtures.bytes("sensor-community", "filter-box.json"), "application/json");
        FetchResult result = sensorCommunity.fetch(harness.context(sensorCommunity,
                Map.of("area", "box=52.3, 13.0, 52.7, 13.8")));
        assertThat(result.records()).hasSize(7);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(SC + "/airrohr/v1/filter/box=52.3,13.0,52.7,13.8")));
        WIREMOCK.verify(0, getRequestedFor(urlPathMatching(SC + "/static/.*")));
    }

    @Test
    @DisplayName("epa-radnet: one request per station; a failing station is skipped; all failing fails the run")
    void radnetPerStation() throws Exception {
        serve(RADNET + "CO/DENVER", AdapterFixtures.bytes("epa-radnet", "CO_DENVER_2026.csv"), "text/text");
        serve(RADNET + "HI/HONOLULU", AdapterFixtures.bytes("epa-radnet", "HI_HONOLULU_2026.csv"), "text/text");
        Map<String, Object> params = Map.of("stations", List.of("CO/DENVER", "HI/HONOLULU"));
        assertThat(radnet.fetch(harness.context(radnet, params)).records()).extracting(NormalizedRecord::externalId)
                .containsExactly("CO/DENVER", "HI/HONOLULU");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(RADNET + "CO/DENVER")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(RADNET + "HI/HONOLULU")));
        WIREMOCK.verify(2, anyRequestedFor(anyUrl()));

        WIREMOCK.resetAll();
        serve(RADNET + "CO/DENVER", AdapterFixtures.bytes("epa-radnet", "CO_DENVER_2026.csv"), "text/text");
        WIREMOCK.stubFor(get(urlPathEqualTo(RADNET + "HI/HONOLULU")).willReturn(aResponse().withStatus(404)));
        assertThat(radnet.fetch(harness.context(radnet, params)).records()).extracting(NormalizedRecord::externalId)
                .containsExactly("CO/DENVER");

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathMatching(RADNET + ".*")).willReturn(aResponse().withStatus(404)));
        assertThatThrownBy(() -> radnet.fetch(harness.context(radnet, params))).isInstanceOf(AdapterException.class);
        assertThatThrownBy(() -> radnet.fetch(harness.context(radnet, Map.of()))).isInstanceOf(AdapterException.class);
    }

    @Test
    @DisplayName("epa-radnet on 1 January: the previous year's file is read once per station and year")
    void radnetRollover() throws Exception {
        harness.clock.setInstant(Instant.parse("2027-01-01T03:00:00Z"));
        String y2027 = "/radnet.epa.gov/cdx-radnet-rest/api/rest/csv/2027/fixed/CO/DENVER";
        serve(y2027, AdapterFixtures.bytes("epa-radnet", "GU_HAGATNA_2026.csv"), "text/text");
        serve(RADNET + "CO/DENVER", AdapterFixtures.bytes("epa-radnet", "CO_DENVER_2026.csv"), "text/text");
        Map<String, Object> params = Map.of("stations", List.of("CO/DENVER"), "maxAge", Duration.ofDays(120));
        assertThat(radnet.fetch(harness.context(radnet, params)).records()).singleElement()
                .satisfies(r -> assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T20:41:00Z")));
        radnet.fetch(harness.context(radnet, params));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(y2027)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(RADNET + "CO/DENVER")));
    }

    @Test
    @DisplayName("safecast-radiation: one request for the device list")
    void safecastSingleRequest() throws Exception {
        serve("/tt.safecast.org/devices", AdapterFixtures.bytes("safecast-radiation", "devices.json"),
                "application/json");
        assertThat(safecast.fetch(harness.context(safecast, Map.of())).records()).hasSize(12);
        WIREMOCK.verify(1, anyRequestedFor(anyUrl()));
    }
}
