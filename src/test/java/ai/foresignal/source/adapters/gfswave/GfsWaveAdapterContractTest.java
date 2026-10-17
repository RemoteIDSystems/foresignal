package ai.foresignal.source.adapters.gfswave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.model.GribIdx;
import ai.foresignal.raster.model.ModelCycleSelector;
import ai.foresignal.raster.model.ModelDefinition;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.GridStats;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The production GFS-Wave model ({@link GfsWaveAdapter#MODEL}) against the 2026-10-17 00z f006 step
 * captured from {@code noaa-gfs-bdp-pds} (checkpoint 124; {@code src/test/resources/fixtures/gfs-wave/README.md}):
 * the object key, every product's {@code .idx} selector, and the {@code HTSGW:surface} message decoded by
 * {@link Grib2Decoder} with the adapter's request.
 */
class GfsWaveAdapterContractTest {

    static final String DIR = "fixtures/gfs-wave/";
    static final Instant RUN = Instant.parse("2026-10-17T00:00:00Z");

    @TempDir Path dir;

    static ModelDefinition.Component component(String productId) {
        return GfsWaveAdapter.MODEL.products().stream().filter(p -> p.productId().equals(productId)).findFirst()
                .orElseThrow().components().get(0);
    }

    @Test
    @DisplayName("the 00z f006 object key is the captured one")
    void objectKey() {
        assertThat(ModelCycleSelector.objectKey(GfsWaveAdapter.DEFAULT_TEMPLATE, RUN, 6))
                .isEqualTo("gfs.20261017/00/wave/gridded/gfswave.t00z.global.0p25.f006.grib2");
    }

    @Test
    @DisplayName("HTSGW, PERPW and DIRPW are messages 5, 6 and 7 of the captured .idx")
    void selectors() {
        GribIdx idx = GribIdx.parse(new String(DecoderFixtures.bytes(DIR + "gfswave.t00z.global.0p25.f006.grib2.idx"),
                StandardCharsets.US_ASCII));
        assertThat(idx.all()).hasSize(19);
        GribIdx.Message htsgw = idx.find(component(GfsWaveAdapter.WAVE).selector(6)).orElseThrow();
        assertThat(htsgw.number()).isEqualTo(5);
        assertThat(htsgw.offset()).isEqualTo(3_011_229L);
        assertThat(idx.next(htsgw).orElseThrow().offset()).isEqualTo(3_443_044L);
        assertThat(idx.find(component(GfsWaveAdapter.PERIOD).selector(6)).map(GribIdx.Message::number)).contains(6);
        assertThat(idx.find(component(GfsWaveAdapter.DIRECTION).selector(6)).map(GribIdx.Message::number)).contains(7);
    }

    @Test
    @DisplayName("the HTSGW message decodes with the gfs-wave request: 1440 x 721, m, NaN over land")
    void htsgw() throws Exception {
        Path file = dir.resolve("gfswave-t00z-f006-htsgw.grib2");
        Files.write(file, DecoderFixtures.bytes(DIR + "gfswave-t00z-f006-htsgw.grib2"));
        DecodedGrid grid = new Grib2Decoder(DecoderFixtures.safe(dir)).decode(file,
                component(GfsWaveAdapter.WAVE).request());
        assertThat(grid.nx()).isEqualTo(1440);
        assertThat(grid.ny()).isEqualTo(721);
        assertThat(grid.gridDefinition().params()).containsEntry("dx", 0.25).containsEntry("la1", 90.0)
                .containsEntry("lo1", 0.0);
        // Template 5.40 (JPEG 2000), which the adapter's GRIB2_COMPLEX_PACKING requirement does not name.
        assertThat(grid.metadata()).containsEntry("discipline", 10).containsEntry("number", 3)
                .containsEntry("dataTemplate", 40);
        assertThat(grid.referenceTime()).isEqualTo(RUN);
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-17T06:00:00Z"));
        GridStats stats = GridStats.of(grid.floatData());
        assertThat(stats.nan()).as("land and ice cells").isEqualTo(470_952);
        assertThat(stats.min()).isZero();
        assertThat(stats.max()).isCloseTo(10.65, within(1e-3));
    }
}
