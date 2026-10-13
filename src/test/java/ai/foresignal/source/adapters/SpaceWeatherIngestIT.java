package ai.foresignal.source.adapters;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.adapters.space.swpc.SwpcAuroraAdapter;
import ai.foresignal.source.adapters.space.swpc.SwpcRtswAdapter;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 077 sources end to end through the real fetch path and ingest writer: every source registers disabled
 * and the retired {@code noaa-swpc} and the excluded {@code tinygs} do not; solar-wind values that round the same
 * across polls create no version; aurora records exist per place and the source is private.
 *
 * <p>{@code swpc-rtsw} and {@code swpc-aurora} are used by no other test; each test resets their adapter state.
 */
class SpaceWeatherIngestIT extends WeatherIngestSupport {

    static final String SWPC = "/services.swpc.noaa.gov";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired UpstreamClient client;
    @Autowired PlaceService places;
    @Autowired EgressPolicy egress;

    @AfterEach
    void cleanUp() {
        configure(SwpcRtswAdapter.ID, false, Map.of());
        configure(SwpcAuroraAdapter.ID, false, Map.of());
        for (String id : List.of("aurora-it-tromso", "aurora-it-fairbanks")) {
            if (places.find(id).isPresent()) {
                places.delete(id, true);
            }
        }
    }

    private void resetState(String sourceId, String... keys) {
        var state = client.stateStore(sourceId);
        for (String key : keys) {
            state.remove(key);
        }
    }

    private static void serve(String path, String body) {
        UPSTREAM.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    @DisplayName("every 077 source is registered and disabled; noaa-swpc and tinygs are not registered")
    void registered() {
        List<String> ids = List.of("swpc-alerts", "swpc-kp", "swpc-rtsw", "swpc-xray", "swpc-aurora", "nasa-donki",
                "jpl-fireballs", "jpl-close-approaches", "satnogs-stations", "sondehub", "local-sonde");
        assertThat(sources.list()).filteredOn(s -> ids.contains(s.id())).hasSize(ids.size())
                .allSatisfy(s -> assertThat(s.enabled()).as(s.id()).isFalse());
        assertThat(sources.list()).noneMatch(s -> s.id().equals("noaa-swpc") || s.id().equals("tinygs"));
    }

    @Test
    @DisplayName("swpc-rtsw: values that round the same five minutes later are a touch, not a version")
    void rtswRoundingMakesNoVersion() {
        resetState(SwpcRtswAdapter.ID, SwpcRtswAdapter.LAST_SPEED_KEY, SwpcRtswAdapter.LAST_FIELD_KEY,
                SwpcRtswAdapter.LAST_DENSITY_KEY, SwpcRtswAdapter.DENSITY_READ_AT_KEY,
                "etag:" + SwpcRtswAdapter.SPEED_KEY, "etag:" + SwpcRtswAdapter.FIELD_KEY,
                "etag:" + SwpcRtswAdapter.WIND_KEY);
        clock.setInstant(Instant.parse("2026-10-13T20:47:00Z"));
        configure(SwpcRtswAdapter.ID, true, Map.of());
        serve(SWPC + "/products/summary/solar-wind-speed.json",
                "[{\"proton_speed\": 638, \"time_tag\": \"2026-10-13T20:37:00Z\"}]");
        serve(SWPC + "/products/summary/solar-wind-mag-field.json",
                "[{\"bt\": 6, \"bz_gsm\": 2, \"time_tag\": \"2026-10-13T20:40:00Z\"}]");
        UPSTREAM.stubFor(get(urlPathEqualTo(SWPC + "/json/rtsw/rtsw_wind_1m.json")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("swpc-rtsw", "rtsw_wind_1m.json"))));
        assertThat(fetch(SwpcRtswAdapter.ID).success()).isTrue();
        assertThat(versions(SwpcRtswAdapter.ID, "l1")).isEqualTo(1);

        clock.advance(Duration.ofMinutes(5));
        UPSTREAM.resetAll();
        serve(SWPC + "/products/summary/solar-wind-speed.json",
                "[{\"proton_speed\": 641.4, \"time_tag\": \"2026-10-13T20:42:00Z\"}]");
        serve(SWPC + "/products/summary/solar-wind-mag-field.json",
                "[{\"bt\": 6.2, \"bz_gsm\": 1.8, \"time_tag\": \"2026-10-13T20:45:00Z\"}]");
        assertThat(fetch(SwpcRtswAdapter.ID).success()).isTrue();
        assertThat(versions(SwpcRtswAdapter.ID, "l1")).as("same rounded values: a touch").isEqualTo(1);
        assertThat(live("observed_at", SwpcRtswAdapter.ID, "l1")).isEqualTo(Instant.parse("2026-10-13T20:45:00Z"));
        UPSTREAM.verify(0, getRequestedFor(urlPathEqualTo(SWPC + "/json/rtsw/rtsw_wind_1m.json")));

        clock.advance(Duration.ofMinutes(5));
        UPSTREAM.resetAll();
        serve(SWPC + "/products/summary/solar-wind-speed.json",
                "[{\"proton_speed\": 655, \"time_tag\": \"2026-10-13T20:47:00Z\"}]");
        serve(SWPC + "/products/summary/solar-wind-mag-field.json",
                "[{\"bt\": 6.2, \"bz_gsm\": 1.8, \"time_tag\": \"2026-10-13T20:45:00Z\"}]");
        assertThat(fetch(SwpcRtswAdapter.ID).success()).isTrue();
        assertThat(versions(SwpcRtswAdapter.ID, "l1")).as("660 km/s: a new version").isEqualTo(2);
        assertThat(versions(SwpcRtswAdapter.ID, PersistReason.FIRST)).isEqualTo(1);
    }

    @Test
    @DisplayName("swpc-aurora: one record per place at the place point; the source is private")
    void auroraAtPlaces() {
        resetState(SwpcAuroraAdapter.ID, "etag:" + SwpcAuroraAdapter.OVATION_KEY,
                "lastmod:" + SwpcAuroraAdapter.OVATION_KEY, LiveIdSet.LIVE_PREFIX + "places",
                LiveIdSet.GONE_PREFIX + "places", "aurora:places");
        clock.setInstant(Instant.parse("2026-10-13T20:47:00Z"));
        places.create(JSON.readTree("{\"id\":\"aurora-it-tromso\",\"name\":\"Tromso\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[18.96,69.65]},\"radiusKm\":10}"));
        places.create(JSON.readTree("{\"id\":\"aurora-it-fairbanks\",\"name\":\"Fairbanks\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-147.72,64.84]},\"radiusKm\":10}"));
        configure(SwpcAuroraAdapter.ID, true, Map.of());
        UPSTREAM.stubFor(get(urlPathEqualTo(SWPC + "/json/ovation_aurora_latest.json")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("swpc-aurora", "ovation_aurora_latest.json"))));
        assertThat(fetch(SwpcAuroraAdapter.ID).success()).isTrue();

        for (String id : List.of("aurora-it-tromso", "aurora-it-fairbanks")) {
            assertThat(versions(SwpcAuroraAdapter.ID, id)).as(id).isEqualTo(1);
            assertThat(tombstoned(SwpcAuroraAdapter.ID, id)).isFalse();
        }
        assertThat(jdbc.queryForObject("select payload->>'placeName' from record_head where source_id = ? and"
                + " external_id = ?", String.class, SwpcAuroraAdapter.ID, "aurora-it-tromso")).isEqualTo("Tromso");
        assertThat(jdbc.queryForObject("select payload->>'probability' from record_head where source_id = ? and"
                + " external_id = ?", String.class, SwpcAuroraAdapter.ID, "aurora-it-tromso")).isEqualTo("14");
        assertThat(egress.isPrivate(SwpcAuroraAdapter.ID)).isTrue();
        assertThat(egress.visible(SwpcAuroraAdapter.ID, Viewer.anonymous())).isFalse();
        assertThat(egress.allowsEmbedding(SwpcAuroraAdapter.ID)).isFalse();
    }
}
