package ai.foresignal.source.adapters.conflict;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.reference.ukr.UaRegionIndex;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.adapters.alertsinua.AlertsInUaAdapter;
import ai.foresignal.source.adapters.deepstate.DeepStateAdapter;
import ai.foresignal.source.adapters.uasiren.UaSirenAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 081 keyless and alert adapters against stubbed upstreams through the real upstream client: the
 * alerts.in.ua Bearer header (never in a URL, redacted everywhere), {@code If-Modified-Since} and a 304, a 401, a 429
 * backing off; siren.pp.ua conditional GET and its absence tombstones; DeepState's fallback to yesterday on a 404, its
 * conditional GET per file name, and a pinned commit fetched once.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class ConflictAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T23:00:00Z");
    static final String ALERTS = "/api.alerts.in.ua/v1/alerts/active.json";
    static final String SIREN = "/siren.pp.ua/api/v3/alerts";
    static final String DEEPSTATE = "/raw.githubusercontent.com/cyterat/deepstate-map-data/";
    static final String TOKEN = "alerts-token-0123456789-SECRET";
    static final String LAST_MODIFIED = "Tue, 13 Oct 2026 22:59:31 GMT";
    static final String SHA = "0123456789abcdef0123456789abcdef01234567";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    UaRegionIndex regions = new UaRegionIndex();
    AlertsInUaAdapter alerts;
    UaSirenAdapter siren;
    DeepStateAdapter deepstate;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        alerts = new AlertsInUaAdapter(baseUrl(), regions);
        siren = new UaSirenAdapter(baseUrl(), regions);
        deepstate = new DeepStateAdapter(baseUrl());
        String[] absence = {AbsenceTombstoner.IDS_KEY + ":air-raid-alert", AbsenceTombstoner.MISSED_KEY + ":air-raid-alert",
                AbsenceTombstoner.IDS_KEY + ":threat-alert", AbsenceTombstoner.MISSED_KEY + ":threat-alert"};
        harness.reset(alerts.id(), absence);
        harness.reset(alerts.id(), SourceHttp.ETAG_STATE_PREFIX + AlertsInUaAdapter.STATE_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + AlertsInUaAdapter.STATE_KEY);
        harness.reset(siren.id(), absence);
        harness.reset(siren.id(), SourceHttp.ETAG_STATE_PREFIX + UaSirenAdapter.STATE_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + UaSirenAdapter.STATE_KEY);
        harness.reset(deepstate.id(), DeepStateAdapter.CURRENT_KEY, DeepStateAdapter.PREVIOUS_KEY,
                DeepStateAdapter.PINNED_KEY);
        for (int day = 10; day <= 14; day++) {
            String key = DeepStateAdapter.stateKey(LocalDate.of(2026, 10, day));
            harness.reset(deepstate.id(), SourceHttp.ETAG_STATE_PREFIX + key, SourceHttp.LAST_MODIFIED_STATE_PREFIX + key);
        }
        harness.credentials.clear();
    }

    @AfterEach
    void tearDown() {
        for (String id : new String[] {alerts.id(), siren.id(), deepstate.id()}) {
            harness.backoff.onSuccess(id);
        }
    }

    @Test
    @DisplayName("alerts.in.ua: Bearer header, Last-Modified back as If-Modified-Since, a 304 unchanged, token redacted")
    void alertsConditional(CapturedOutput output) throws Exception {
        harness.credentials.put(AlertsInUaAdapter.TOKEN, TOKEN);
        WIREMOCK.stubFor(get(urlPathEqualTo(ALERTS)).withHeader("If-Modified-Since", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("Last-Modified", LAST_MODIFIED)
                        .withBody(AdapterFixtures.bytes("alerts-in-ua", "active.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(ALERTS)).withHeader("If-Modified-Since", equalTo(LAST_MODIFIED))
                .willReturn(aResponse().withStatus(304)));
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(alerts, Map.of(), recorder);

        FetchResult first = alerts.fetch(ctx);
        assertThat(first.complete()).isTrue();
        assertThat(first.records()).hasSize(7);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(ALERTS)).withHeader("Authorization", equalTo("Bearer " + TOKEN))
                .withQueryParam("token", absent()));
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl).allSatisfy(u -> assertThat(u).doesNotContain(TOKEN));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(TOKEN);
        }
        assertThat(output.getAll()).doesNotContain(TOKEN);

        // A recording fetch is probe-like and stores no conditional-GET validator (prompt 047); a
        // normal fetch does, so the run after it is a 304.
        assertThat(alerts.fetch(harness.context(alerts, Map.of())).complete()).isTrue();
        assertThat(alerts.fetch(harness.context(alerts, Map.of())).notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(ALERTS)).withHeader("If-Modified-Since", equalTo(LAST_MODIFIED)));
    }

    @Test
    @DisplayName("alerts.in.ua: an alert gone from two complete lists is tombstoned (finished alerts at once)")
    void alertsAbsence() throws Exception {
        harness.credentials.put(AlertsInUaAdapter.TOKEN, TOKEN);
        WIREMOCK.stubFor(get(urlPathEqualTo(ALERTS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("alerts-in-ua", "active.json"))));
        FetchResult first = alerts.fetch(harness.context(alerts, Map.of()));
        assertThat(first.records()).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactly("1006");

        String fewer = "{\"alerts\":[{\"id\":10,\"location_uid\":\"16\",\"alert_type\":\"air_raid\","
                + "\"updated_at\":\"2022-04-08T08:04:26.316Z\",\"started_at\":\"2022-04-04T16:45:39.000Z\"}]}";
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(ALERTS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(fewer)));
        assertThat(alerts.fetch(harness.context(alerts, Map.of())).records()).noneMatch(NormalizedRecord::tombstone);
        FetchResult third = alerts.fetch(harness.context(alerts, Map.of()));
        assertThat(third.records()).filteredOn(NormalizedRecord::tombstone)
                .extracting(r -> r.kind() + ":" + r.externalId()).containsExactlyInAnyOrder(
                        "air-raid-alert:1001", "air-raid-alert:1002", "air-raid-alert:1004", "threat-alert:1003",
                        "threat-alert:1005");
    }

    @Test
    @DisplayName("alerts.in.ua: no token sends nothing; 401 is a credentials error; 429 puts the source in backoff")
    void alertsErrors() {
        assertThatThrownBy(() -> alerts.fetch(harness.context(alerts, Map.of())))
                .isInstanceOf(CredentialsRejectedException.class).hasMessageContaining("token");
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));

        harness.credentials.put(AlertsInUaAdapter.TOKEN, TOKEN);
        WIREMOCK.stubFor(get(urlPathEqualTo(ALERTS)).willReturn(aResponse().withStatus(401)
                .withBody("{\"message\":\"API token required. Please contact api@alerts.in.ua for details.\"}")));
        assertThatThrownBy(() -> alerts.fetch(harness.context(alerts, Map.of())))
                .isInstanceOf(CredentialsRejectedException.class).hasMessageNotContaining(TOKEN);

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(ALERTS)).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "60").withBody("Too many requests")));
        assertThatThrownBy(() -> alerts.fetch(harness.context(alerts, Map.of())))
                .isInstanceOfSatisfying(UpstreamHttpException.class, e -> assertThat(e.getStatus()).isEqualTo(429));
        assertThat(harness.backoff.isInBackoff(alerts.id())).isTrue();
        WIREMOCK.resetRequests();
        assertThatThrownBy(() -> alerts.fetch(harness.context(alerts, Map.of())))
                .as("nothing is sent during the backoff").isInstanceOf(ai.foresignal.source.spi.AdapterException.class);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("siren.pp.ua: ETag conditional GET; an alert gone from two complete lists is tombstoned")
    void sirenConditionalAndAbsence() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(SIREN)).withHeader("If-None-Match", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("ETag", "\"s1\"").withBody(AdapterFixtures.bytes("ua-air-raid-siren", "alerts.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(SIREN)).withHeader("If-None-Match", equalTo("\"s1\""))
                .willReturn(aResponse().withStatus(304)));
        FetchResult first = siren.fetch(harness.context(siren, Map.of()));
        assertThat(first.records()).hasSize(50);
        assertThat(siren.fetch(harness.context(siren, Map.of())).notModified()).isTrue();

        WIREMOCK.resetAll();
        String one = "[{\"regionId\":\"16\",\"regionType\":\"State\",\"regionName\":\"Луганська область\","
                + "\"regionEngName\":\"Luhanska region\",\"lastUpdate\":\"2022-04-04T16:45:00Z\",\"activeAlerts\":["
                + "{\"regionId\":\"16\",\"regionType\":\"State\",\"type\":\"AIR\",\"lastUpdate\":\"2022-04-04T16:45:00Z\","
                + "\"activeAlertLevels\":[{\"alertLevel\":\"Red\",\"reason\":\"\",\"createdAt\":\"2022-04-04T16:45:00Z\"}]}]}]";
        WIREMOCK.stubFor(get(urlPathEqualTo(SIREN)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(one)));
        assertThat(siren.fetch(harness.context(siren, Map.of())).records()).noneMatch(NormalizedRecord::tombstone);
        FetchResult third = siren.fetch(harness.context(siren, Map.of()));
        assertThat(third.records()).filteredOn(NormalizedRecord::tombstone).hasSize(49)
                .extracting(NormalizedRecord::externalId).doesNotContain("16:AIR").contains("122:AIR", "1313:ARTILLERY");
    }

    @Test
    @DisplayName("DeepState: today's 404 falls back to yesterday; the next day's map carries the area change")
    void deepstateFallbackAndChange() throws Exception {
        harness.clock.setInstant(Instant.parse("2026-10-13T01:30:00Z"));
        WIREMOCK.stubFor(get(urlPathEqualTo(DEEPSTATE + "main/data/deepstatemap_data_20261013.geojson"))
                .willReturn(aResponse().withStatus(404).withBody("404: Not Found")));
        WIREMOCK.stubFor(get(urlPathEqualTo(DEEPSTATE + "main/data/deepstatemap_data_20261012.geojson"))
                .willReturn(aResponse().withStatus(200).withHeader("ETag", "\"d12\"")
                        .withBody(AdapterFixtures.bytes("deepstate-frontline", "deepstatemap_data_20261012.geojson"))));
        FetchResult first = deepstate.fetch(harness.context(deepstate, Map.of()));
        assertThat(first.records()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("occupied-area");
            assertThat(r.payload()).containsEntry("mapDate", "2026-10-12").containsEntry("areaKm2", 117_147.4)
                    .doesNotContainKey("areaChangeKm2");
        });
        assertThat(harness.client.stateStore(deepstate.id()).get(SourceHttp.ETAG_STATE_PREFIX + "deepstate-20261012"))
                .contains("\"d12\"");

        WIREMOCK.resetAll();
        harness.clock.setInstant(Instant.parse("2026-10-13T04:00:00Z"));
        WIREMOCK.stubFor(get(urlPathEqualTo(DEEPSTATE + "main/data/deepstatemap_data_20261013.geojson"))
                .willReturn(aResponse().withStatus(200).withHeader("ETag", "\"d13\"")
                        .withBody(AdapterFixtures.bytes("deepstate-frontline", "deepstatemap_data_20261013.geojson"))));
        FetchResult second = deepstate.fetch(harness.context(deepstate, Map.of()));
        assertThat(second.records().getFirst().payload()).containsEntry("mapDate", "2026-10-13")
                .containsEntry("areaChangeKm2", 7.5);
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(DEEPSTATE + "main/data/deepstatemap_data_20261012.geojson")));

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(DEEPSTATE + "main/data/deepstatemap_data_20261013.geojson"))
                .withHeader("If-None-Match", equalTo("\"d13\"")).willReturn(aResponse().withStatus(304)));
        assertThat(deepstate.fetch(harness.context(deepstate, Map.of())).notModified()).isTrue();
    }

    @Test
    @DisplayName("DeepState: both days 404 is an error")
    void deepstateNothing() {
        WIREMOCK.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(404).withBody("404: Not Found")));
        assertThatThrownBy(() -> deepstate.fetch(harness.context(deepstate, Map.of())))
                .isInstanceOfSatisfying(UpstreamHttpException.class, e -> assertThat(e.getStatus()).isEqualTo(404));
        WIREMOCK.verify(2, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("DeepState: a pinned commit is fetched once, then not-modified with no request; fileDate is required")
    void deepstatePinned() throws Exception {
        Map<String, Object> params = Map.of("ref", SHA, "fileDate", "2026-10-12");
        WIREMOCK.stubFor(get(urlPathEqualTo(DEEPSTATE + SHA + "/data/deepstatemap_data_20261012.geojson"))
                .willReturn(aResponse().withStatus(200)
                        .withBody(AdapterFixtures.bytes("deepstate-frontline", "deepstatemap_data_20261012.geojson"))));
        FetchResult first = deepstate.fetch(harness.context(deepstate, params));
        assertThat(first.records().getFirst().payload()).containsEntry("ref", SHA).containsEntry("mapDate", "2026-10-12");
        harness.clock.setInstant(NOW.plus(Duration.ofDays(3)));
        assertThat(deepstate.fetch(harness.context(deepstate, params)).notModified()).isTrue();
        assertThat(deepstate.fetch(harness.context(deepstate, params)).notModified()).isTrue();
        WIREMOCK.verify(1, anyRequestedFor(anyUrl()));

        assertThat(deepstate.checkParams(ValidatedParams.of(Map.of("ref", SHA))))
                .containsKey(DeepStateAdapter.FILE_DATE_PARAM);
        assertThat(deepstate.checkParams(ValidatedParams.of(Map.of("ref", "../etc")))).containsKey(DeepStateAdapter.REF_PARAM);
        assertThat(deepstate.checkParams(ValidatedParams.of(Map.of("fileDate", "13/10/2026"))))
                .containsKey(DeepStateAdapter.FILE_DATE_PARAM);
        assertThat(deepstate.checkParams(ValidatedParams.of(Map.of("ref", SHA, "fileDate", "20261012")))).isEmpty();
        assertThat(deepstate.checkParams(ValidatedParams.of(Map.of()))).isEmpty();
    }
}
