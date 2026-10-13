package ai.foresignal.source.adapters.wx;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthChangedEvent;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.adapters.goes.GoesGlmAdapter;
import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.weather.PlaceWeatherSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.JsonNode;

/**
 * Weather-Imagery-Design §7.4, required integration test: <strong>the GLM feed stops</strong> → no exit alert
 * fires, the place's {@code lightning} section goes {@code stale} (keeping its values, with a fixed {@code asOf}),
 * and the source-health system event fires for {@code goes-glm} (prompt 052; 048's {@code GlmFeedStopsIT} covers
 * the cluster records).
 */
@RecordApplicationEvents
class GlmStopsNoAllClearIT extends PlaceWeatherSupport {

    static final Instant T = Instant.parse("2026-11-01T18:00:00Z");

    @Autowired FlashStore flashes;
    @Autowired HealthService health;
    @Autowired UpstreamClient upstream;
    @Autowired ApplicationEvents events;

    @Test
    @DisplayName("GLM stops: lightning stale with its values kept, no all-clear, the health event fires")
    void glmStops() {
        clock.setInstant(T);
        place("glm-home", "Glm Home");
        UUID near = rule("Lightning near", "Glm Home", "lightning.nearestKm15m", "<=", 15, "enter");
        UUID allClear = rule("Lightning all-clear", "Glm Home", "lightning.verifiedNearestKm30m", "<=", 15, "exit");

        // A processed GLM file 20 s ago with one flash about 5 km east of the place.
        double lonEast = LON + 5.0 / (111.32 * Math.cos(Math.toRadians(LAT)));
        Instant fileTime = T.minusSeconds(20);
        flashes.append("G19", new FlashBatch(new long[] {T.minusSeconds(60).toEpochMilli()}, new float[] {(float) LAT},
                new float[] {(float) lonEast}, new float[] {100f}), fileTime);
        configure(GoesGlmAdapter.ID, true, Map.of());
        upstream.stateStore(GoesGlmAdapter.ID).recordInput(fileTime, GoesGlmAdapter.MAX_GAP);

        enableWxPlaces();
        tick();
        JsonNode ok = payload("glm-home").get("lightning");
        assertThat(ok.get("status").asString()).isEqualTo("ok");
        double nearest = ok.get("nearestKm15m").asDouble();
        assertThat(nearest).isBetween(4.5, 5.5);
        assertThat(ok.get("verifiedNearestKm30m").asDouble()).isEqualTo(ok.get("nearestKm30m").asDouble());
        assertThat(alerts(near)).isEqualTo(1);

        // The feed stops: no file for 5, then 35 minutes.
        clock.setInstant(T.plus(Duration.ofMinutes(5)));
        tick();
        JsonNode stale = payload("glm-home").get("lightning");
        assertThat(stale.get("status").asString()).isEqualTo("stale");
        assertThat(stale.get("asOf").asString()).isEqualTo(fileTime.toString());
        assertThat(stale.get("nearestKm15m").asDouble()).isEqualTo(nearest);
        assertThat(stale.get("verifiedNearestKm30m").asDouble()).isZero();
        assertThat(alerts(allClear)).as("a stopped feed never reads as clear").isZero();

        int versions = versions(WxPlacesGenerator.ID, "place:glm-home");
        clock.setInstant(T.plus(Duration.ofMinutes(35)));
        tick();
        assertThat(payload("glm-home").get("lightning").get("asOf").asString()).isEqualTo(fileTime.toString());
        assertThat(versions(WxPlacesGenerator.ID, "place:glm-home")).as("asOf stays fixed: no new version")
                .isEqualTo(versions);
        assertThat(alerts(allClear)).isZero();
        assertThat(alerts(near)).isEqualTo(1);

        // The goes-glm source itself shows degraded, and the source-health system event fires.
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + GoesGlmAdapter.BUCKET + "/")).atPriority(10)
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(GoesGlmAdapter.BUCKET, "", Map.of(), false))));
        assertThat(fetch(GoesGlmAdapter.ID).success()).isTrue();
        assertThat(health.health(GoesGlmAdapter.ID).status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(events.stream(SourceHealthChangedEvent.class)
                .filter(e -> e.sourceId().equals(GoesGlmAdapter.ID) && e.current() == HealthStatus.DEGRADED)).isNotEmpty();
        // Degraded is not failing: the section stays stale rather than unavailable.
        samples.invalidateHealth();
        tick();
        assertThat(payload("glm-home").get("lightning").get("status").asString()).isEqualTo("stale");
        configure(GoesGlmAdapter.ID, false, Map.of());
    }
}
