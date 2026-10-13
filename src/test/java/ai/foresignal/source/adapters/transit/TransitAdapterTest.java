package ai.foresignal.source.adapters.transit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The transit adapters against a stubbed upstream through the real upstream client (prompt 070): the conditional
 * GET and a 304, protobuf garbage and an HTML 200 as {@link UpstreamFormatException}, a FULL_DATASET removal and a
 * DIFFERENTIAL {@code is_deleted} as tombstones, Entur's identification header and the untimed counter.
 */
@IntegrationTest
class TransitAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T16:05:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    SimpleMeterRegistry meters;
    TransitFamily family;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 1_000), null);
        meters = new SimpleMeterRegistry();
        family = new TransitFamily("", baseUrl(), new MobilityCatalog(MobilityCatalogTest.TEST_CATALOG),
                ai.foresignal.testsupport.net.TestOutbound.offlinePolicy(), install::installId, () -> meters);
    }

    @AfterEach
    void tearDown() {
        for (SourceAdapter adapter : family.adapters()) {
            upstream.backoff().onSuccess(adapter.id());
        }
    }

    private SourceAdapter adapter(String id) {
        return family.adapters().stream().filter(a -> a.id().equals(id)).findFirst().orElseThrow();
    }

    private WeatherAdapterHarness.Context context(SourceAdapter adapter, Map<String, Object> params) {
        return new WeatherAdapterHarness.Context(adapter.descriptor(),
                upstream.client().forSource(adapter.id(), adapter.descriptor()), ValidatedParams.of(params),
                state(adapter), clock);
    }

    private AdapterStateStore state(SourceAdapter adapter) {
        return upstream.client().stateStore(adapter.id());
    }

    private static void serve(String path, byte[] body, String etag) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/x-protobuf").withHeader("ETag", etag).withBody(body)));
    }

    private static byte[] synthetic(String name) {
        return AdapterFixtures.bytes("transit/synthetic", name);
    }

    private static List<String> tombstones(FetchResult result) {
        return result.records().stream().filter(NormalizedRecord::tombstone).map(NormalizedRecord::externalId).toList();
    }

    @Test
    @DisplayName("a conditional GET: the ETag is stored and sent back; a 304 is not modified and changes nothing")
    void conditionalGet() throws Exception {
        TransitVehicleAdapter mbta = (TransitVehicleAdapter) adapter("transit-mbta");
        String path = "/realtime/VehiclePositions.pb";
        serve(path, AdapterFixtures.bytes("transit/mbta", "VehiclePositions.pb"), "\"v1\"");

        FetchResult first = mbta.fetch(context(mbta, Map.of()));
        assertThat(first.records()).hasSize(30);
        assertThat(first.complete()).isTrue();
        assertThat(state(mbta).get(SourceHttp.ETAG_STATE_PREFIX + TransitVehicleAdapter.STATE_KEY)).contains("\"v1\"");

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(path)).withHeader("If-None-Match", equalTo("\"v1\""))
                .willReturn(aResponse().withStatus(304)));
        FetchResult second = mbta.fetch(context(mbta, Map.of()));

        assertThat(second.notModified()).isTrue();
        assertThat(second.records()).isEmpty();
    }

    @Test
    @DisplayName("protobuf garbage is an UpstreamFormatException, and the validators are forgotten")
    void garbage() {
        TransitVehicleAdapter mbta = (TransitVehicleAdapter) adapter("transit-mbta");
        serve("/realtime/VehiclePositions.pb", new byte[] {0x0A, 0x7F, 0x01, 0x02, (byte) 0xFF}, "\"bad\"");

        assertThatThrownBy(() -> mbta.fetch(context(mbta, Map.of()))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("not a GTFS-RT FeedMessage");
        assertThat(state(mbta).get(SourceHttp.ETAG_STATE_PREFIX + TransitVehicleAdapter.STATE_KEY)).isEmpty();
    }

    @Test
    @DisplayName("an HTML 200 is detected by its first byte before parsing; so is a JSON 200")
    void html() {
        TransitVehicleAdapter hsl = (TransitVehicleAdapter) adapter("transit-hsl-helsinki");
        WIREMOCK.stubFor(WireMock.get(urlEqualTo("/realtime/vehicle-positions/v2/hsl")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/html").withBody(synthetic("html-200.html"))));

        assertThatThrownBy(() -> hsl.fetch(context(hsl, Map.of()))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("HTML");
        assertThat(GtfsRt.textKind(" \n{\"alerts\":[]}".getBytes())).isEqualTo("JSON");
        assertThat(GtfsRt.textKind(new byte[] {0x0A, 0x0D, 0x0A, 0x03, '2', '.', '0'})).isNull();
    }

    @Test
    @DisplayName("FULL_DATASET: an alert missing from the next successful response gets one tombstone")
    void fullDatasetRemoval() throws Exception {
        TransitAlertsAdapter alerts = (TransitAlertsAdapter) adapter("transit-alerts-mbta");
        String path = "/realtime/Alerts.pb";
        serve(path, synthetic("alert-impact-period.pb"), "\"a1\"");
        FetchResult first = alerts.fetch(context(alerts, Map.of()));
        assertThat(first.records()).extracting(NormalizedRecord::externalId).containsExactly("A", "B");
        assertThat(tombstones(first)).isEmpty();

        serve(path, synthetic("full-dataset-alert-removed.pb"), "\"a2\"");
        FetchResult second = alerts.fetch(context(alerts, Map.of()));

        assertThat(tombstones(second)).containsExactly("B");
        NormalizedRecord tombstone = second.records().stream().filter(NormalizedRecord::tombstone).findFirst().orElseThrow();
        assertThat(tombstone.observedAt()).isEqualTo(Instant.ofEpochSecond(1_791_907_260L));
        assertThat(second.records()).filteredOn(r -> !r.tombstone()).extracting(NormalizedRecord::externalId)
                .containsExactly("A");
    }

    @Test
    @DisplayName("a 304 and a failed response tombstone nothing")
    void noTombstoneWithoutAFullResponse() throws Exception {
        TransitAlertsAdapter alerts = (TransitAlertsAdapter) adapter("transit-alerts-mbta");
        String path = "/realtime/Alerts.pb";
        serve(path, synthetic("alert-impact-period.pb"), "\"a1\"");
        alerts.fetch(context(alerts, Map.of()));

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(path)).willReturn(aResponse().withStatus(304)));
        assertThat(alerts.fetch(context(alerts, Map.of())).notModified()).isTrue();

        WIREMOCK.resetAll();
        serve(path, synthetic("html-200.html"), "\"err\"");
        assertThatThrownBy(() -> alerts.fetch(context(alerts, Map.of()))).isInstanceOf(UpstreamFormatException.class);

        serve(path, synthetic("alert-impact-period.pb"), "\"a1\"");
        assertThat(tombstones(alerts.fetch(context(alerts, Map.of())))).isEmpty();
    }

    @Test
    @DisplayName("DIFFERENTIAL: only is_deleted tombstones; absence means nothing")
    void differential() throws Exception {
        TransitAlertsAdapter alerts = (TransitAlertsAdapter) adapter("transit-alerts-metrotransit-msp");
        String path = "/mtgtfs/alerts.pb";
        serve(path, synthetic("alert-impact-period.pb"), "\"d1\"");
        alerts.fetch(context(alerts, Map.of()));

        serve(path, synthetic("differential-alert-deleted.pb"), "\"d2\"");
        FetchResult result = alerts.fetch(context(alerts, Map.of()));

        assertThat(tombstones(result)).containsExactly("B");
        assertThat(result.complete()).isFalse();
    }

    @Test
    @DisplayName("Entur requests carry ET-Client-Name: foresignal-{installId}; MBTA's carry none")
    void enturHeader() throws Exception {
        TransitVehicleAdapter entur = (TransitVehicleAdapter) adapter("transit-entur-norway");
        serve("/realtime/v1/gtfs-rt/vehicle-positions", AdapterFixtures.bytes("transit/entur-norway",
                "vehicle-positions.pb"), "\"e1\"");
        TransitVehicleAdapter mbta = (TransitVehicleAdapter) adapter("transit-mbta");
        serve("/realtime/VehiclePositions.pb", AdapterFixtures.bytes("transit/mbta", "VehiclePositions.pb"), "\"m1\"");

        assertThat(entur.fetch(context(entur, Map.of())).records()).isNotEmpty();
        mbta.fetch(context(mbta, Map.of()));

        WIREMOCK.verify(getRequestedFor(urlEqualTo("/realtime/v1/gtfs-rt/vehicle-positions"))
                .withHeader("ET-Client-Name", equalTo("foresignal-" + install.installId()))
                .withHeader("User-Agent", WireMock.containing("Foresignal/")));
        WIREMOCK.verify(getRequestedFor(urlEqualTo("/realtime/VehiclePositions.pb"))
                .withHeader("ET-Client-Name", absent()));
    }

    @Test
    @DisplayName("an untimed vehicle under VEHICLE is dropped and counted in foresignal.transit.untimed{source}")
    void untimedCounter() throws Exception {
        TransitVehicleAdapter mbta = (TransitVehicleAdapter) adapter("transit-mbta");
        serve("/realtime/VehiclePositions.pb", synthetic("untimed-vehicle.pb"), "\"u1\"");

        FetchResult result = mbta.fetch(context(mbta, Map.of()));

        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("v1");
        assertThat(meters.get(TransitVehicleAdapter.UNTIMED_METRIC).tag("source", "transit-mbta").counter().count())
                .isEqualTo(1.0);
    }

    @Test
    @DisplayName("the language parameter picks the translation of the alert texts")
    void language() throws Exception {
        TransitAlertsAdapter alerts = (TransitAlertsAdapter) adapter("transit-alerts-hsl-helsinki");
        serve("/realtime/service-alerts/v2/hsl", synthetic("alert-legacy-active-period.pb"), "\"l1\"");

        FetchResult result = alerts.fetch(context(alerts, Map.of(TransitDescriptors.LANGUAGE_PARAM, "fi")));

        assertThat(result.records().get(0).payload()).containsEntry("headerText", "Raitiolinja 6: poikkeusreitti");
    }
}
