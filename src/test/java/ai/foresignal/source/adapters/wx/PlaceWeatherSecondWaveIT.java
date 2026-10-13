package ai.foresignal.source.adapters.wx;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.mrms.MrmsAdapter;
import ai.foresignal.source.adapters.nws.NwsGridpointsAdapter;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.raster.SampleFixtures;
import ai.foresignal.testsupport.weather.PlaceWeatherSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/**
 * The prompt 053 sections of {@code place-weather} on the real runtime: MRMS QPE 1 h, QPE 24 h and the FFG ratio
 * fill {@code precip} next to the rate, an {@code nws-gridpoints} fetch fills {@code forecast.nws}, the
 * "flash-flood guidance exceeded" recipe fires, and a forecast older than 6 hours turns {@code forecast.nws} stale
 * while keeping its values.
 */
class PlaceWeatherSecondWaveIT extends PlaceWeatherSupport {

    static final Instant T = Instant.parse("2026-10-12T21:00:00Z");

    @AfterEach
    void stopNws() {
        configure(NwsGridpointsAdapter.ID, false, Map.of());
        if (places.find("sw-home").isPresent()) {
            places.delete("sw-home", true);
        }
    }

    private static int[] filled(int code) {
        int[] codes = new int[SampleFixtures.TEXAS.nx() * SampleFixtures.TEXAS.ny()];
        Arrays.fill(codes, code);
        return codes;
    }

    private void commit(String productId, Instant t, int[]... bands) throws Exception {
        pipeline.commit(MrmsAdapter.ID, FrameInput.builder(productId).validTime(t).grid(SampleFixtures.TEXAS)
                .bands(DecodedBands.codes(bands)).upstreamKey("synthetic/" + productId + "/" + t).build());
        idle();
    }

    @Test
    @DisplayName("precip gains the QPE and FFG maxima, forecast.nws fills from the NWS record and goes stale after 6 h")
    void secondWaveSections() throws Exception {
        clock.setInstant(T);
        place("sw-home", "SW Home");
        UUID flood = rule("Flash-flood guidance exceeded", "SW Home", "precip.ffgRatio1hPctMax", ">=", 100, "enter");
        enableWxPlaces();

        clock.setInstant(T.plusSeconds(30));
        commit(MrmsAdapter.PRECIP_RATE, T, filled(50), filled(0));
        commit(MrmsAdapter.QPE_1H, T, filled(123));
        commit(MrmsAdapter.QPE_24H, T, filled(456));
        commit(MrmsAdapter.FFG_RATIO, T, filled(1050));

        JsonNode precip = payload("sw-home").get("precip");
        assertThat(precip.get("status").asString()).isEqualTo("ok");
        assertThat(precip.get("rateMmH").asDouble()).isEqualTo(5.0);
        assertThat(precip.get("qpe1hMmMax").asDouble()).isEqualTo(12.0);
        assertThat(precip.get("qpe24hMmMax").asDouble()).isEqualTo(46.0);
        assertThat(precip.get("ffgRatio1hPctMax").asDouble()).isEqualTo(105.0);
        assertThat(alerts(flood)).isEqualTo(1);

        JsonNode before = payload("sw-home").get("forecast").get("nws");
        assertThat(before.get("status").asString()).as("no NWS source yet: unknown").isEqualTo("unavailable");

        UPSTREAM.stubFor(get(urlPathEqualTo("/points/30.5,-98")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("nws-gridpoints", "points-VEF-55-177.json"))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/gridpoints/VEF/55,177")).willReturn(aResponse().withStatus(200)
                .withHeader("Last-Modified", "Mon, 12 Oct 2026 19:46:56 GMT")
                .withBody(AdapterFixtures.bytes("nws-gridpoints", "gridpoints-VEF-55-177.json"))));
        configure(NwsGridpointsAdapter.ID, true, Map.of());
        samples.invalidateHealth();
        assertThat(fetch(NwsGridpointsAdapter.ID).success()).isTrue();
        idle();
        tick();

        JsonNode nws = payload("sw-home").get("forecast").get("nws");
        assertThat(nws.get("status").asString()).isEqualTo("ok");
        assertThat(nws.get("popMax24h").asInt()).isEqualTo(60);
        assertThat(nws.get("qpf24hMm").asDouble()).isEqualTo(1.3);
        assertThat(nws.get("gustMaxMs24h").asDouble()).isEqualTo(19.5);
        assertThat(nws.has("asOf")).isFalse();

        // Seven hours later the forecast (updated 19:46:56) is stale: values kept, asOf = its update time.
        clock.setInstant(T.plus(Duration.ofHours(7)));
        samples.invalidateHealth();
        tick();

        JsonNode stale = payload("sw-home").get("forecast").get("nws");
        assertThat(stale.get("status").asString()).isEqualTo("stale");
        assertThat(stale.get("asOf").asString()).isEqualTo("2026-10-12T19:46:56Z");
        assertThat(stale.get("popMax24h").asInt()).isEqualTo(60);
        assertThat(stale.get("statusText").asString()).contains("not current");
        JsonNode stalePrecip = payload("sw-home").get("precip");
        assertThat(stalePrecip.get("status").asString()).isEqualTo("stale");
        assertThat(stalePrecip.get("asOf").asString()).isEqualTo(T.toString());
        assertThat(stalePrecip.get("ffgRatio1hPctMax").asDouble()).as("values kept").isEqualTo(105.0);
        assertThat(alerts(flood)).as("stale is not an exit, and no second enter").isEqualTo(1);
    }
}
