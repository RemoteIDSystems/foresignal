package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.util.OptionalInt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A probe's adapter state never reaches the persistent store (prompt 047). */
class ProbeStateOverlayTest {

    @Test
    @DisplayName("reads fall through, writes and removals stay in the overlay")
    void overlay() {
        var persistent = new WeatherAdapterHarness.MapState();
        persistent.put("live:national", "a\nb");
        persistent.put("lastKey", "k1");
        ProbeStateOverlay overlay = new ProbeStateOverlay(persistent);

        assertThat(overlay.get("live:national")).contains("a\nb");
        overlay.put("live:national", "a");
        overlay.remove("lastKey");
        overlay.put("new", "v");
        overlay.put("nulls", null);

        assertThat(overlay.get("live:national")).contains("a");
        assertThat(overlay.get("lastKey")).isEmpty();
        assertThat(overlay.get("new")).contains("v");
        assertThat(overlay.get("nulls")).isEmpty();
        assertThat(persistent.asMap()).containsEntry("live:national", "a\nb").containsEntry("lastKey", "k1")
                .doesNotContainKey("new");
        assertThat(overlay.writes()).containsKeys("live:national", "lastKey", "new");
    }

    @Test
    @DisplayName("UpstreamHttpException exposes its status through the SPI")
    void httpStatus() {
        assertThat(new UpstreamHttpException(404, "").httpStatus()).isEqualTo(OptionalInt.of(404));
    }
}
