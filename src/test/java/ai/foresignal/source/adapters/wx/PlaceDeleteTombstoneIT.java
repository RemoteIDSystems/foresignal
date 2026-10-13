package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.testsupport.weather.PlaceWeatherSupport;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Deleting a place tombstones its {@code place-weather} record, and the tombstone fires the {@code exit} rule
 * whose condition held (prompt 052; Weather-Imagery-Design §7.3 note: a tombstone after a matching version is an
 * exit). This is the documented behavior, not an all-clear: the Operator Guide says so.
 */
class PlaceDeleteTombstoneIT extends PlaceWeatherSupport {

    static final Instant T = Instant.parse("2026-11-07T15:00:00Z");

    @Test
    @DisplayName("deleting the place tombstones place:{id} and fires the exit rule")
    void deleteFiresExit() throws Exception {
        clock.setInstant(T);
        place("del-home", "Del Home");
        UUID enter = rule("Heavy echoes near", "Del Home", "radar.nearest50Km10m", "<=", 20, "enter");
        UUID exit = rule("Heavy echoes gone", "Del Home", "radar.nearest50Km10m", "<=", 20, "exit");
        enableWxPlaces();
        clock.setInstant(T.plusSeconds(30));
        reflectivity(T, coreAt(12));
        assertThat(payload("del-home")).isNotNull();
        assertThat(alerts(enter)).isEqualTo(1);
        assertThat(alerts(exit)).isZero();

        places.delete("del-home", true);
        await().atMost(WAIT).until(() -> Boolean.TRUE.equals(tombstoned(WxPlacesGenerator.ID, "place:del-home")));
        idle();
        assertThat(payload("del-home")).isNull();
        assertThat(alerts(exit)).as("the tombstone is an exit").isEqualTo(1);
        assertThat(alerts(enter)).isEqualTo(1);
    }
}
