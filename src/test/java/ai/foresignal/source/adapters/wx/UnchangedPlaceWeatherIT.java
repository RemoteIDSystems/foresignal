package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.weather.PlaceWeatherSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Weather-Imagery-Design §7.4, required integration test: <strong>an unchanged place-weather over 30 minutes
 * produces no versions and no alerts</strong> (prompt 052). Fifteen identical reflectivity frames, two minutes
 * apart, with a staleness tick after each, write nothing after the first version, and the {@code enter} rule fires
 * once only. Every invocation still leaves its {@code fetch_log} row.
 */
class UnchangedPlaceWeatherIT extends PlaceWeatherSupport {

    static final Instant T = Instant.parse("2026-11-05T15:00:00Z");

    @Test
    @DisplayName("30 minutes of identical frames: no new versions, no new alerts")
    void steadyStateWritesNothing() throws Exception {
        clock.setInstant(T);
        place("steady-home", "Steady Home");
        UUID near = rule("Heavy echoes near", "Steady Home", "radar.nearest50Km10m", "<=", 20, "enter");
        enableWxPlaces();
        int[] codes = coreAt(12);
        clock.setInstant(T.plusSeconds(30));
        reflectivity(T, codes);
        tick();
        assertThat(payload("steady-home").get("radar").get("status").asString()).isEqualTo("ok");
        int versions = versions(WxPlacesGenerator.ID, "place:steady-home");
        assertThat(alerts(near)).isEqualTo(1);
        Integer rowsBefore = jdbc.queryForObject("select count(*) from fetch_log where source_id = ?", Integer.class,
                WxPlacesGenerator.ID);

        for (int i = 1; i <= 15; i++) {
            Instant t = T.plus(Duration.ofMinutes(2L * i));
            clock.setInstant(t.plusSeconds(30));
            reflectivity(t, codes);
            tick();
        }
        assertThat(versions(WxPlacesGenerator.ID, "place:steady-home")).as("versions after 30 minutes").isEqualTo(versions);
        assertThat(alerts(near)).isEqualTo(1);
        Integer rowsAfter = jdbc.queryForObject("select count(*) from fetch_log where source_id = ?", Integer.class,
                WxPlacesGenerator.ID);
        assertThat(rowsAfter - rowsBefore).as("one fetch_log row per invocation, records or not").isGreaterThanOrEqualTo(30);
    }
}
