package ai.foresignal.source.adapters.conflict;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.acled.AcledAdapter;
import ai.foresignal.source.adapters.alertsinua.AlertsInUaAdapter;
import ai.foresignal.source.adapters.deepstate.DeepStateAdapter;
import ai.foresignal.source.adapters.uasiren.UaSirenAdapter;
import ai.foresignal.source.adapters.ucdp.UcdpGedAdapter;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The prompt 081 sources end to end through the real fetch path and ingest writer, against the shared stub server
 * ({@link WeatherIngestSupport}): all five register disabled and the keyed ones refuse to be enabled without their
 * credentials; {@code acled} reports {@code LOCAL_ONLY}; an alerts.in.ua alert absent from two complete lists is
 * tombstoned; a new DeepState map creates version 2 of {@code occupied-area}; a UCDP event missing from a newer
 * candidate inside the window is tombstoned while one whose {@code date_end} left the window is kept.
 */
class ConflictIngestIT extends WeatherIngestSupport {

    static final String ALERTS = "/api.alerts.in.ua/v1/alerts/active.json";
    static final String DEEPSTATE = "/raw.githubusercontent.com/cyterat/deepstate-map-data/main/data/";
    static final String UCDP = "/ucdpapi.pcr.uu.se/api/gedevents/26.0.8";
    static final List<String> IDS = List.of(DeepStateAdapter.ID, AlertsInUaAdapter.ID, UaSirenAdapter.ID,
            AcledAdapter.ID, UcdpGedAdapter.ID);

    @Autowired UpstreamClient client;

    @AfterEach
    void disable() {
        for (String id : IDS) {
            configure(id, false, Map.of());
        }
    }

    private void resetState(String id, String... keys) {
        var state = client.stateStore(id);
        for (String key : keys) {
            state.remove(key);
        }
    }

    @Test
    @DisplayName("all five register disabled; keyed sources need their credentials; acled is LOCAL_ONLY")
    void registered() {
        assertThat(sources.list()).filteredOn(s -> IDS.contains(s.id())).hasSize(IDS.size())
                .allSatisfy(s -> assertThat(s.enabled()).as(s.id()).isFalse());
        for (String id : List.of(AlertsInUaAdapter.ID, AcledAdapter.ID, UcdpGedAdapter.ID)) {
            sources.deleteCredentials(id);
            assertThatThrownBy(() -> sources.update(id, SourceSettingsUpdate.enabled(true))).as(id)
                    .isInstanceOf(ConflictException.class)
                    .satisfies(e -> assertThat(((ConflictException) e).getCode())
                            .isEqualTo(ConflictException.CREDENTIALS_REQUIRED));
        }
        assertThat(sources.list()).filteredOn(s -> s.id().equals(AcledAdapter.ID)).singleElement()
                .satisfies(s -> assertThat(s.descriptor().egress()).isEqualTo(Egress.LOCAL_ONLY));
        assertThat(sources.list()).filteredOn(s -> IDS.contains(s.id()) && !s.id().equals(AcledAdapter.ID))
                .allSatisfy(s -> assertThat(s.descriptor().egress()).as(s.id()).isEqualTo(Egress.OPEN));
    }

    private static String alerts(String... ids) {
        StringBuilder body = new StringBuilder("{\"alerts\":[");
        for (int i = 0; i < ids.length; i++) {
            body.append(i == 0 ? "" : ",").append("{\"id\":").append(ids[i]).append(",\"location_title\":\"м. Київ\","
                    + "\"location_title_en\":\"Kyiv City\",\"location_type\":\"city\",\"location_uid\":\"31\","
                    + "\"alert_type\":\"air_raid\",\"alert_level\":\"red\",\"started_at\":\"2026-10-13T22:40:00Z\","
                    + "\"updated_at\":\"2026-10-13T22:41:00Z\"}");
        }
        return body.append("]}").toString();
    }

    private static void serveAlerts(String body) {
        UPSTREAM.stubFor(get(urlPathEqualTo(ALERTS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    @DisplayName("alerts-in-ua: an alert absent from two complete lists is tombstoned, the other stays current")
    void alertAbsentTwice() {
        resetState(AlertsInUaAdapter.ID, AbsenceTombstoner.IDS_KEY + ":air-raid-alert",
                AbsenceTombstoner.MISSED_KEY + ":air-raid-alert", SourceHttp.LAST_MODIFIED_STATE_PREFIX
                        + AlertsInUaAdapter.STATE_KEY, SourceHttp.ETAG_STATE_PREFIX + AlertsInUaAdapter.STATE_KEY);
        sources.setCredentials(AlertsInUaAdapter.ID, Map.of(AlertsInUaAdapter.TOKEN, "it-alerts-token-0123456789"));
        configure(AlertsInUaAdapter.ID, false, Map.of());
        clock.setInstant(Instant.parse("2026-10-13T23:00:00Z"));

        serveAlerts(alerts("810001", "810002"));
        assertThat(fetch(AlertsInUaAdapter.ID).success()).isTrue();
        assertThat(tombstoned(AlertsInUaAdapter.ID, "810002")).isFalse();
        UPSTREAM.verify(anyRequestedFor(urlPathEqualTo(ALERTS))
                .withHeader("Authorization", equalTo("Bearer it-alerts-token-0123456789")));

        UPSTREAM.resetAll();
        serveAlerts(alerts("810001"));
        clock.setInstant(Instant.parse("2026-10-13T23:00:30Z"));
        assertThat(fetch(AlertsInUaAdapter.ID).success()).isTrue();
        assertThat(tombstoned(AlertsInUaAdapter.ID, "810002")).as("one miss").isFalse();
        clock.setInstant(Instant.parse("2026-10-13T23:01:00Z"));
        assertThat(fetch(AlertsInUaAdapter.ID).success()).isTrue();
        assertThat(tombstoned(AlertsInUaAdapter.ID, "810002")).as("two misses").isTrue();
        assertThat(tombstoned(AlertsInUaAdapter.ID, "810001")).isFalse();
        assertThat(jdbc.queryForObject("select payload->>'locationEn' from record_head where source_id = ? and"
                + " external_id = ?", String.class, AlertsInUaAdapter.ID, "810001")).isEqualTo("Kyiv City");
    }

    @Test
    @DisplayName("deepstate-frontline: the next day's map is version 2 of occupied-area, with the area change")
    void deepstateVersions() {
        resetState(DeepStateAdapter.ID, DeepStateAdapter.CURRENT_KEY, DeepStateAdapter.PREVIOUS_KEY,
                DeepStateAdapter.PINNED_KEY);
        for (int day = 11; day <= 13; day++) {
            String key = DeepStateAdapter.stateKey(LocalDate.of(2026, 10, day));
            resetState(DeepStateAdapter.ID, SourceHttp.ETAG_STATE_PREFIX + key, SourceHttp.LAST_MODIFIED_STATE_PREFIX + key);
        }
        configure(DeepStateAdapter.ID, false, Map.of());
        int before = versions(DeepStateAdapter.ID, "occupied-area");

        clock.setInstant(Instant.parse("2026-10-13T01:00:00Z"));
        UPSTREAM.stubFor(get(urlPathEqualTo(DEEPSTATE + "deepstatemap_data_20261013.geojson"))
                .willReturn(aResponse().withStatus(404).withBody("404: Not Found")));
        UPSTREAM.stubFor(get(urlPathEqualTo(DEEPSTATE + "deepstatemap_data_20261012.geojson"))
                .willReturn(aResponse().withStatus(200)
                        .withBody(AdapterFixtures.bytes("deepstate-frontline", "deepstatemap_data_20261012.geojson"))));
        assertThat(fetch(DeepStateAdapter.ID).success()).isTrue();
        assertThat(versions(DeepStateAdapter.ID, "occupied-area")).isEqualTo(before + 1);

        UPSTREAM.resetAll();
        clock.setInstant(Instant.parse("2026-10-13T04:00:00Z"));
        UPSTREAM.stubFor(get(urlPathEqualTo(DEEPSTATE + "deepstatemap_data_20261013.geojson"))
                .willReturn(aResponse().withStatus(200)
                        .withBody(AdapterFixtures.bytes("deepstate-frontline", "deepstatemap_data_20261013.geojson"))));
        assertThat(fetch(DeepStateAdapter.ID).success()).isTrue();
        assertThat(versions(DeepStateAdapter.ID, "occupied-area")).isEqualTo(before + 2);
        assertThat(jdbc.queryForObject("select payload->>'mapDate' from record_head where source_id = ? and"
                + " external_id = ?", String.class, DeepStateAdapter.ID, "occupied-area")).isEqualTo("2026-10-13");
        assertThat(jdbc.queryForObject("select (payload->>'areaChangeKm2')::float from record_head where source_id = ?"
                + " and external_id = ?", Double.class, DeepStateAdapter.ID, "occupied-area")).isEqualTo(7.5);
        assertThat(head("event_time", DeepStateAdapter.ID, "occupied-area"))
                .isEqualTo(Instant.parse("2026-10-13T03:00:00Z"));
    }

    @Test
    @DisplayName("ucdp-ged: missing inside the window is tombstoned; outside the window it is kept")
    void ucdpWindowedAbsence() {
        resetState(UcdpGedAdapter.ID, UcdpGedAdapter.ENDS_KEY, AbsenceTombstoner.IDS_KEY, AbsenceTombstoner.MISSED_KEY,
                UcdpGedAdapter.VERSION_KEY, UcdpGedAdapter.VERSION_AT_KEY);
        sources.setCredentials(UcdpGedAdapter.ID, Map.of(UcdpGedAdapter.TOKEN, "it-ucdp-token-0123456789"));
        Map<String, Object> params = Map.of("version", "26.0.8", "lookbackDays", 89);
        configure(UcdpGedAdapter.ID, false, params);

        clock.setInstant(Instant.parse("2026-10-13T12:00:00Z"));
        UPSTREAM.stubFor(get(urlPathEqualTo(UCDP)).withQueryParam("page", absent()).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("ucdp-ged", "page1.json"))));
        UPSTREAM.stubFor(get(urlPathEqualTo(UCDP)).withQueryParam("page", equalTo("2")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("ucdp-ged", "page2.json"))));
        assertThat(fetch(UcdpGedAdapter.ID).success()).isTrue();
        for (String id : List.of("780001", "780002", "780003")) {
            assertThat(tombstoned(UcdpGedAdapter.ID, id)).as(id).isFalse();
        }

        UPSTREAM.resetAll();
        UPSTREAM.stubFor(get(urlPathEqualTo(UCDP)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("ucdp-ged", "revised.json"))));
        clock.setInstant(Instant.parse("2026-10-13T18:00:00Z"));
        assertThat(fetch(UcdpGedAdapter.ID).success()).isTrue();
        assertThat(versions(UcdpGedAdapter.ID, "780001")).as("the revision is a new version").isEqualTo(2);

        // The window now starts 2026-08-16: 780003 (date_end 2026-08-15) left it; 780002 (2026-08-31) is inside.
        clock.setInstant(Instant.parse("2026-11-13T12:00:00Z"));
        assertThat(fetch(UcdpGedAdapter.ID).success()).isTrue();
        assertThat(tombstoned(UcdpGedAdapter.ID, "780002")).isTrue();
        assertThat(tombstoned(UcdpGedAdapter.ID, "780003")).isFalse();
        assertThat(tombstoned(UcdpGedAdapter.ID, "780001")).isFalse();
        UPSTREAM.verify(0, anyRequestedFor(anyUrl()).withQueryParam("pagesize", equalTo("1")));
    }
}
