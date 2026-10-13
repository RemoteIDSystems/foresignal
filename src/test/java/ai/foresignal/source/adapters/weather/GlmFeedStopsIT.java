package ai.foresignal.source.adapters.weather;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthChangedEvent;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.place.PlaceService;
import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.adapters.goes.GoesGlmAdapter;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

/**
 * Weather-Imagery-Design §7.4, required integration test (the first of three; 052 adds the place-weather
 * ones): <strong>the GLM feed stops</strong> → no cluster tombstones, no exit alerts, the source goes
 * {@code degraded} and the source-health system event fires (prompt 048).
 *
 * <p>The captured active granule has its lightning over South America, so a place there puts its
 * clusters inside the AOI ring.
 */
@RecordApplicationEvents
class GlmFeedStopsIT extends WeatherIngestSupport {

    static final String ID = GoesGlmAdapter.ID;
    static final String BUCKET = GoesGlmAdapter.BUCKET;
    static final String HOUR_21 = "GLM-L2-LCFA/2026/285/21/";
    static final String KEY = HOUR_21 + "OR_GLM-L2-LCFA_G19_s20262852157200_e20262852157400_c20262852157421.nc";

    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired HealthService health;
    @Autowired PlaceService places;
    @Autowired FlashStore flashes;
    @Autowired ApplicationEvents events;

    private int alerts(UUID rule) {
        engine.catchUp();
        Integer count = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, rule);
        return count == null ? 0 : count;
    }

    @Test
    @DisplayName("the GLM feed stops: clusters freeze, no exit alert, health degraded, the health event fires")
    void feedStops() {
        clock.setInstant(Instant.parse("2026-10-12T21:58:00Z"));
        places.create(JsonMapper.builder().build().readTree("{\"id\":\"storm\",\"name\":\"Storm\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-64.95,-10.68]},\"radiusKm\":50}"));
        configure(ID, true, Map.of());
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(10)
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, "", Map.of(), false))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(1).withQueryParam("prefix", equalTo(HOUR_21))
                .withQueryParam("start-after", absent())
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, HOUR_21, Map.of(KEY, 432_989L), false))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + KEY))
                .willReturn(aResponse().withBody(AdapterFixtures.bytes("glm",
                        "OR_GLM-L2-LCFA_G19_s20262852157200_e20262852157400_c20262852157421.nc"))));

        FetchOutcome first = fetch(ID);
        assertThat(first.success()).isTrue();
        int clusters = versions(ID, PersistReason.FIRST);
        assertThat(clusters).as("clusters inside the storm AOI").isPositive();
        assertThat(flashes.newestFlashFileTime()).contains(Instant.parse("2026-10-12T21:57:40Z"));
        assertThat(health.health(ID).status()).isEqualTo(HealthStatus.HEALTHY);
        UUID rule = rules.create(JsonMapper.builder().build().readTree(
                "{\"name\":\"lightning cleared\",\"kinds\":[\"lightning-cluster\"],\"conditions\":[{\"path\":\"automated\","
                        + "\"op\":\"exists\"}],\"mode\":\"exit\"}")).rule().getId();
        assertThat(alerts(rule)).isZero();

        // The feed stops: the listing has nothing after the last key, for 15 and then 45 minutes.
        clock.advance(Duration.ofMinutes(15));
        FetchOutcome stalled = fetch(ID);
        assertThat(stalled.success()).isTrue();
        assertThat(versions(ID, PersistReason.TOMBSTONE)).isZero();
        assertThat(alerts(rule)).isZero();
        assertThat(health.health(ID).status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(health.health(ID).statusReason()).contains("newest input file");
        assertThat(events.stream(SourceHealthChangedEvent.class)
                .filter(e -> e.sourceId().equals(ID) && e.current() == HealthStatus.DEGRADED)).isNotEmpty();

        clock.advance(Duration.ofMinutes(30));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(versions(ID, PersistReason.TOMBSTONE)).isZero();
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(clusters);
        assertThat(alerts(rule)).isZero();
        configure(ID, false, Map.of());
    }
}
