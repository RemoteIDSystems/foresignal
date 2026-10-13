package ai.foresignal.source.adapters.nws;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.place.ForecastSeriesCache;
import ai.foresignal.place.PlaceService;
import ai.foresignal.raster.sample.ForecastSeriesService;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code nws-gridpoints} end to end (prompt 053): a real place through the real fetch path and ingest writer; the
 * {@code nws-forecast} record, its resolved retention, the {@code forecast_series_cache} row and the {@code nws}
 * series served from it; a 304 keeps the record; deleting the place deletes the cache row at once and tombstones the
 * record on the next run.
 */
class NwsForecastIngestIT extends WeatherIngestSupport {

    static final String ID = NwsGridpointsAdapter.ID;
    static final String PLACE = "nws-home";
    static final String LAST_MODIFIED = "Mon, 12 Oct 2026 19:46:56 GMT";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired PlaceService places;
    @Autowired ForecastSeriesCache cache;
    @Autowired ForecastSeriesService forecasts;
    @Autowired RetentionResolver retention;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-12T20:10:00Z"));
        configure(ID, false, Map.of());
        places.create(JSON.readTree("{\"id\":\"" + PLACE + "\",\"name\":\"NWS Home\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-117.3638,37.6856]},\"radiusKm\":20}"));
        UPSTREAM.stubFor(get(urlPathEqualTo("/points/37.6856,-117.3638")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json")
                .withBody(AdapterFixtures.bytes("nws-gridpoints", "points-VEF-55-177.json"))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/gridpoints/VEF/55,177")).withHeader("If-Modified-Since", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Last-Modified", LAST_MODIFIED)
                        .withBody(AdapterFixtures.bytes("nws-gridpoints", "gridpoints-VEF-55-177.json"))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/gridpoints/VEF/55,177")).withHeader("If-Modified-Since", equalTo(LAST_MODIFIED))
                .willReturn(aResponse().withStatus(304)));
    }

    @AfterEach
    void removePlace() {
        if (places.find(PLACE).isPresent()) {
            places.delete(PLACE, true);
        }
    }

    @Test
    @DisplayName("one nws-forecast record per place, kept 7 d, with its 72-hour series cached and served as provider nws")
    void ingest() {
        FetchOutcome first = fetch(ID);

        assertThat(first.success()).isTrue();
        assertThat(versions(ID, PersistReason.FIRST)).isEqualTo(1);
        assertThat(live("observed_at", ID, "place:" + PLACE)).isEqualTo(Instant.parse("2026-10-12T19:46:56Z"));
        String payload = jdbc.queryForObject("select payload::text from record_head where source_id = ? and external_id = ?",
                String.class, ID, "place:" + PLACE);
        assertThat(JSON.readTree(payload).get("wfo").asString()).isEqualTo("VEF");
        assertThat(JSON.readTree(payload).get("popMax24h").asInt()).isEqualTo(60);
        assertThat(retention.window(ID, NwsGridpointsAdapter.KIND)).isEqualTo(Duration.ofDays(7));

        ForecastSeriesCache.Entry entry = cache.find(PLACE, "nws").orElseThrow();
        assertThat(entry.items()).hasSize(72);
        assertThat(entry.updatedAt()).isEqualTo(Instant.parse("2026-10-12T19:46:56Z"));
        List<Map<String, Object>> served = forecasts.series(PLACE, -117.3638, 37.6856, java.util.Set.of());
        assertThat(served).anySatisfy(s -> {
            assertThat(s).containsEntry("provider", "nws").containsEntry("status", "ok");
            assertThat((List<?>) s.get("items")).isNotEmpty();
        });
        assertThat(forecasts.series(PLACE, -117.3638, 37.6856, java.util.Set.of(ID)))
                .noneSatisfy(s -> assertThat(s).containsEntry("provider", "nws"));
    }

    @Test
    @DisplayName("an hour later If-Modified-Since gets a 304: the record keeps one version")
    void notModifiedKeepsTheRecord() {
        fetch(ID);
        clock.setInstant(NwsGridpointsAdapter.nextDue(PLACE, clock.instant()));

        FetchOutcome second = fetch(ID);

        assertThat(second.success()).isTrue();
        assertThat(versions(ID, "place:" + PLACE)).isEqualTo(1);
        assertThat(tombstoned(ID, "place:" + PLACE)).isFalse();
    }

    @Test
    @DisplayName("deleting the place deletes its cache row at once and tombstones its record on the next run")
    void placeDelete() {
        fetch(ID);
        assertThat(cache.find(PLACE, "nws")).isPresent();

        places.delete(PLACE, false);

        assertThat(cache.find(PLACE, "nws")).isEmpty();
        clock.setInstant(clock.instant().plus(Duration.ofMinutes(1)));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(tombstoned(ID, "place:" + PLACE)).isTrue();
    }
}
