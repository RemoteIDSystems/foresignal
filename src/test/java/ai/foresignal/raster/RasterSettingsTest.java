package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The two memory profiles and their per-value overrides (prompt 042, Weather-Imagery-Design §2.6). */
class RasterSettingsTest {

    static final long MB = 1024L * 1024L;

    @Test
    @DisplayName("small: 768/128/64 MB, render pool min(cores−2, 6), one decode; quota 200 GB")
    void small() {
        RasterSettings s = new RasterSettings(RasterProfile.SMALL, RasterProperties.defaults(), 16);
        assertThat(s.frameCacheBytes()).isEqualTo(768 * MB);
        assertThat(s.tileCacheBytes()).isEqualTo(128 * MB);
        assertThat(s.indexCacheBytes()).isEqualTo(64 * MB);
        assertThat(s.renderPool()).isEqualTo(6);
        assertThat(s.decodeConcurrency()).isEqualTo(1);
        assertThat(s.diskQuotaGb()).isEqualTo(200.0);
        assertThat(s.diskQuotaBytes()).isEqualTo(200L * 1024 * MB);
        assertThat(s.orphanGrace()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("large: 1536/256/128 MB, render pool cores−2, two decodes")
    void large() {
        RasterSettings s = new RasterSettings(RasterProfile.LARGE, RasterProperties.defaults(), 16);
        assertThat(s.frameCacheBytes()).isEqualTo(1536 * MB);
        assertThat(s.tileCacheBytes()).isEqualTo(256 * MB);
        assertThat(s.indexCacheBytes()).isEqualTo(128 * MB);
        assertThat(s.renderPool()).isEqualTo(14);
        assertThat(s.decodeConcurrency()).isEqualTo(2);
        assertThat(new RasterSettings(RasterProfile.SMALL, RasterProperties.defaults(), 2).renderPool()).isEqualTo(1);
    }

    @Test
    @DisplayName("each value can be overridden on its own")
    void overrides() {
        RasterSettings s = new RasterSettings(RasterProfile.SMALL,
                new RasterProperties(100, 10, 5, 3, 4, 50.0, Duration.ofMinutes(1)), 16);
        assertThat(s.frameCacheBytes()).isEqualTo(100 * MB);
        assertThat(s.tileCacheBytes()).isEqualTo(10 * MB);
        assertThat(s.indexCacheBytes()).isEqualTo(5 * MB);
        assertThat(s.renderPool()).isEqualTo(3);
        assertThat(s.decodeConcurrency()).isEqualTo(4);
        assertThat(s.diskQuotaGb()).isEqualTo(50.0);
        assertThat(s.orphanGrace()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    @DisplayName("FORESIGNAL_PROFILE parses small and large; blank is small; anything else fails")
    void parse() {
        assertThat(RasterProfile.parse(null)).isEqualTo(RasterProfile.SMALL);
        assertThat(RasterProfile.parse(" LARGE ")).isEqualTo(RasterProfile.LARGE);
        assertThatThrownBy(() -> RasterProfile.parse("huge")).isInstanceOf(IllegalArgumentException.class);
        assertThat(RasterSystemBlock.quotaPct(90, 100)).isEqualTo(90.0);
    }
}
