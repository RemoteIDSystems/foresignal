package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.testsupport.raster.RasterFixtures;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Adapter-supplied frame statistics (prompt 046: {@code stats.flagMissing}). */
class RasterPipelineStatsTest {

    static final Instant T = Instant.parse("2026-10-12T12:00:00Z");

    @Test
    @DisplayName("extra stats are merged into the computed ones and never override them")
    void merges() {
        FrameInput input = FrameInput.builder(SyntheticRasterAdapter.OBS).validTime(T).grid(RasterFixtures.GRID)
                .bands(DecodedBands.codes(RasterFixtures.codes(0))).stat("flagMissing", true).stat("coveragePct", -1)
                .build();
        EncodedFrame encoded = FrameEncoder.encode(SyntheticRasterAdapter.observationSpec(), input.grid(), input.bands(),
                false);

        Map<String, Object> stats = RasterPipeline.stats(encoded, input);

        assertThat(stats).containsEntry("flagMissing", true)
                .containsEntry("coveragePct", encoded.stats().get("coveragePct"));
        FrameInput plain = FrameInput.builder(SyntheticRasterAdapter.OBS).validTime(T).grid(RasterFixtures.GRID)
                .bands(DecodedBands.codes(RasterFixtures.codes(0))).build();
        assertThat(plain.extraStats()).isEmpty();
        assertThat(RasterPipeline.stats(encoded, plain)).isSameAs(encoded.stats());
    }
}
