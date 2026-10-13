package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.raster.SampleFixtures;
import ai.foresignal.testsupport.weather.PlaceWeatherSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/**
 * Weather-Imagery-Design §7.4, required integration test: <strong>an MRMS no-coverage frame does not clear the
 * radar section</strong> (prompt 052). After a frame with a 52 dBZ core near the place, a frame whose cells are
 * all MRMS code 0 sets {@code radar.status} to {@code no-coverage}, keeps the previous values with {@code asOf},
 * zeroes the verified helper, and fires no "echoes cleared" exit.
 */
class MrmsNoCoverageIT extends PlaceWeatherSupport {

    static final Instant T = Instant.parse("2026-11-03T15:00:00Z");

    @Test
    @DisplayName("a no-coverage frame keeps the radar values, sets no-coverage with asOf, and fires no all-clear")
    void noCoverageDoesNotClear() throws Exception {
        clock.setInstant(T);
        place("nocov-home", "NoCov Home");
        UUID near = rule("Heavy echoes near", "NoCov Home", "radar.nearest50Km10m", "<=", 20, "enter");
        UUID cleared = rule("Echoes cleared", "NoCov Home", "radar.verifiedNearest50Km10m", "<=", 20, "exit");
        enableWxPlaces();

        clock.setInstant(T.plusSeconds(30));
        reflectivity(T, coreAt(12));
        JsonNode ok = payload("nocov-home").get("radar");
        assertThat(ok.get("status").asString()).isEqualTo("ok");
        double nearest = ok.get("nearest50Km").asDouble();
        assertThat(nearest).isBetween(8.0, 10.0);
        assertThat(alerts(near)).isEqualTo(1);

        Instant t2 = T.plus(Duration.ofMinutes(2));
        clock.setInstant(t2.plusSeconds(30));
        reflectivity(t2, SampleFixtures.noCoverage(SampleFixtures.TEXAS));
        JsonNode radar = payload("nocov-home").get("radar");
        assertThat(radar.get("status").asString()).isEqualTo("no-coverage");
        assertThat(radar.get("asOf").asString()).isEqualTo(T.toString());
        assertThat(radar.get("nearest50Km").asDouble()).isEqualTo(nearest);
        assertThat(radar.get("verifiedNearest50Km10m").asDouble()).isZero();
        assertThat(radar.get("statusText").asString()).contains("not evidence of clear weather");
        assertThat(alerts(cleared)).as("unknown is not clear: no all-clear").isZero();
        assertThat(alerts(near)).isEqualTo(1);

        // Coverage returns with the core gone: now, and only now, the verified all-clear fires.
        Instant t3 = T.plus(Duration.ofMinutes(14));
        clock.setInstant(t3.plusSeconds(30));
        reflectivity(t3, coreAt(80));
        assertThat(payload("nocov-home").get("radar").get("status").asString()).isEqualTo("ok");
        assertThat(alerts(cleared)).isEqualTo(1);
    }
}
