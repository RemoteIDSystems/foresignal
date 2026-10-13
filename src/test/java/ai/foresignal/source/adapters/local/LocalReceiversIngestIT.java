package ai.foresignal.source.adapters.local;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The local receivers end to end through the real fetch path and ingest writer (prompt 069):
 * {@code local-adsb-1} is TRACK with the local SAMPLED defaults (a 400 m move after 12 s is a SAMPLE);
 * {@code station-readsb-1} turning {@code silent} is SIGNIFICANT; {@code local-gpsd-1} is private (hidden from
 * anonymous {@code openReads} callers and never embedded); every local source is {@code LOCAL_ONLY}, an
 * {@code OPEN} override is accepted only where the descriptor allows it ({@code EGRESS_FIXED} elsewhere), and
 * MLAT never leaves on a re-serving path.
 */
class LocalReceiversIngestIT extends WeatherIngestSupport {

    static final String ADSB = "local-adsb-1";
    static final String STATION = "station-readsb-1";
    static final String GPSD = "local-gpsd-1";

    @Autowired private EgressPolicy egress;
    @Autowired private RetentionResolver retention;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-13T16:00:00Z"));
        for (String id : List.of(ADSB, STATION)) {
            backoff.onSuccess(id);
        }
        configure(ADSB, false, Map.of(LocalAdsbAdapter.URL_PARAM, UPSTREAM.baseUrl() + "/data/aircraft.json"));
        configure(STATION, false, Map.of(StationReadsbAdapter.URL_PARAM, UPSTREAM.baseUrl() + "/data/stats.json"));
    }

    @AfterEach
    void revertEgress() {
        sources.update(ADSB, SourceSettingsUpdate.clearingEgress());
        for (String id : List.of(ADSB, STATION)) {
            backoff.onSuccess(id);
        }
    }

    private void serveAircraft(double lat, double lon) {
        String body = "{\"now\":" + clock.instant().getEpochSecond() + ",\"messages\":1,\"aircraft\":[{\"hex\":\"c0ffee\","
                + "\"type\":\"adsb_icao\",\"flight\":\"TEST1\",\"alt_baro\":5000,\"lat\":" + lat + ",\"lon\":" + lon
                + ",\"seen\":0,\"seen_pos\":0,\"rssi\":-10.0}]}";
        UPSTREAM.stubFor(get(urlPathEqualTo("/data/aircraft.json")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body.getBytes(StandardCharsets.UTF_8))));
    }

    private void serveStats(long messages) {
        long end = clock.instant().getEpochSecond();
        String body = "{\"last1min\":{\"start\":" + (end - 60) + ",\"end\":" + end + ",\"messages\":" + messages
                + ",\"tracks\":{\"all\":1}}}";
        UPSTREAM.stubFor(get(urlPathEqualTo("/data/stats.json")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body.getBytes(StandardCharsets.UTF_8))));
    }

    private List<Integer> reasons(String sourceId, String externalId) {
        return jdbc.queryForList("select persist_reason from record_version where source_id = ? and external_id = ?"
                + " order by seq", Integer.class, sourceId, externalId);
    }

    @Test
    @DisplayName("local-adsb: a 400 m move after 12 s is a SAMPLE under the local SAMPLED defaults")
    void trackSampling() {
        int before = versions(ADSB, "c0ffee");
        serveAircraft(40.0, -105.0);
        assertThat(fetch(ADSB).success()).isTrue();

        clock.advance(Duration.ofSeconds(12));
        serveAircraft(40.0036, -105.0);
        assertThat(fetch(ADSB).success()).isTrue();

        List<Integer> reasons = reasons(ADSB, "c0ffee");
        assertThat(versions(ADSB, "c0ffee") - before).isEqualTo(2);
        assertThat(reasons.get(reasons.size() - 1)).isEqualTo((int) PersistReason.SAMPLE.code());
    }

    @Test
    @DisplayName("station-readsb: ok then silent writes a SIGNIFICANT version; retention is the state class's 365 d")
    void stationSilent() {
        String key = STATION;
        int before = versions(STATION, key);
        serveStats(6000);
        assertThat(fetch(STATION).success()).isTrue();

        clock.advance(Duration.ofSeconds(60));
        serveStats(0);
        assertThat(fetch(STATION).success()).isTrue();

        assertThat(versions(STATION, key) - before).isEqualTo(2);
        List<Integer> reasons = reasons(STATION, key);
        assertThat(reasons.get(reasons.size() - 1)).isEqualTo((int) PersistReason.SIGNIFICANT.code());
        assertThat(jdbc.queryForObject("select payload->>'state' from record_head where source_id = ? and external_id = ?",
                String.class, STATION, key)).isEqualTo("silent");
        assertThat(retention.window(STATION, ReadsbStatsParser.KIND)).isEqualTo(Duration.ofDays(365));
    }

    @Test
    @DisplayName("local-gpsd is private: hidden from anonymous openReads callers and never embedded")
    void gpsdPrivate() {
        assertThat(egress.isPrivate(GPSD)).isTrue();
        assertThat(egress.hiddenSources(Viewer.anonymous())).contains(GPSD);
        assertThat(egress.visible(GPSD, Viewer.anonymous())).isFalse();
        assertThat(egress.allowsEmbedding(GPSD)).isFalse();
        assertThat(egress.allows(EgressPath.FEDERATION, GPSD, Viewer.admin())).isFalse();
    }

    @Test
    @DisplayName("LOCAL_ONLY by default; OPEN only where overridable (EGRESS_FIXED elsewhere); MLAT never leaves")
    void egressOverride() {
        assertThat(egress.isLocalOnly(ADSB)).isTrue();
        assertThat(egress.allows(ADSB, EgressPath.SINK)).isFalse();

        sources.update(ADSB, SourceSettingsUpdate.egress(Egress.OPEN));
        assertThat(sources.egressOverride(ADSB)).hasValue(Egress.OPEN);
        egress.onSourceSettingsChanged(new ai.foresignal.source.SourceSettingsChangedEvent(ADSB, null, null));
        assertThat(egress.isLocalOnly(ADSB)).isFalse();
        assertThat(egress.allows(EgressPath.SINK, ADSB, Map.of("posSource", "adsb"), null)).isTrue();
        assertThat(egress.allows(EgressPath.SINK, ADSB, Map.of("posSource", "mlat"), null)).isFalse();
        assertThat(egress.allows(EgressPath.FEED, ADSB, Map.of("posSource", "mlat"), null)).isFalse();

        assertThatThrownBy(() -> sources.update(ai.foresignal.source.adapters.ioda.IodaOutagesAdapter.ID,
                SourceSettingsUpdate.egress(Egress.OPEN)))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.EGRESS_FIXED));

        sources.update(ADSB, SourceSettingsUpdate.clearingEgress());
        egress.onSourceSettingsChanged(new ai.foresignal.source.SourceSettingsChangedEvent(ADSB, null, null));
        assertThat(egress.isLocalOnly(ADSB)).isTrue();
    }
}
