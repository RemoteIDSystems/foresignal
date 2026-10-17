package ai.foresignal.source.adapters.rtma;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.model.FieldSelector;
import ai.foresignal.raster.model.GribIdx;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.GridStats;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The production RTMA-RU fields ({@link RtmaAdapter#FIELDS}) against the 2026-10-17 11:00Z analysis
 * captured from {@code noaa-rtma-pds} (checkpoint 124; {@code src/test/resources/fixtures/rtma-ru/README.md}):
 * the object key, every field's selector on the real {@code .idx}, and the {@code GUST:10 m above ground}
 * message decoded by {@link Grib2Decoder} with the adapter's request.
 *
 * <p>Every RTMA-RU message is 5.6–7.5 MB (simple packing on the 2345 × 1597 grid), over the commit
 * limit, so the GUST message is fetched by {@code ./gradlew downloadTestFixtures} into
 * {@code build/test-fixtures/rtma-ru/}; its test skips with a logged reason when the file is absent.
 */
class RtmaAdapterContractTest {

    static final String IDX = "fixtures/rtma-ru/rtma2p5_ru.t1100z.2dvaranl_ndfd.grb2.idx";
    static final Path GUST_FILE = Path.of("build", "test-fixtures", "rtma-ru", "rtma-ru-t1100z-gust.grib2");

    @TempDir Path dir;

    @Test
    @DisplayName("the 11:00Z object key is the captured one")
    void objectKey() {
        assertThat(RtmaAdapter.ModelKeys.objectKey(RtmaAdapter.TEMPLATE, Instant.parse("2026-10-17T11:00:00Z")))
                .isEqualTo("rtma2p5_ru.20261017/rtma2p5_ru.t1100z.2dvaranl_ndfd.grb2");
    }

    @Test
    @DisplayName("every field the adapter reads is in the captured .idx; GUST is message 10, 5,617,636 bytes")
    void selectors() {
        GribIdx idx = GribIdx.parse(new String(DecoderFixtures.bytes(IDX), StandardCharsets.US_ASCII));
        assertThat(idx.all()).hasSize(13);
        for (RtmaAdapter.Field field : RtmaAdapter.FIELDS) {
            assertThat(idx.find(FieldSelector.of(field.variable(), field.level())))
                    .as(field.variable() + ":" + field.level()).isPresent();
        }
        GribIdx.Message gust = idx.find(FieldSelector.of("GUST", "10 m above ground")).orElseThrow();
        assertThat(gust.number()).isEqualTo(10);
        assertThat(gust.offset()).isEqualTo(57_580_533L);
        assertThat(idx.next(gust).orElseThrow().offset() - gust.offset()).isEqualTo(5_617_636L);
    }

    @Test
    @DisplayName("the GUST message decodes with the rtma-gust request: 2345 x 1597 Lambert, m/s")
    void gust() throws Exception {
        Assumptions.assumeTrue(Files.isRegularFile(GUST_FILE),
                "RTMA-RU fixture absent: run ./gradlew downloadTestFixtures");
        Path file = Files.copy(GUST_FILE, dir.resolve(GUST_FILE.getFileName()));
        RtmaAdapter.Field field = RtmaAdapter.FIELDS.stream().filter(f -> f.productId().equals(RtmaAdapter.GUST))
                .findFirst().orElseThrow();
        DecodedGrid grid = new Grib2Decoder(DecoderFixtures.safe(dir)).decode(file, field.request());
        assertThat(grid.nx()).isEqualTo(2345);
        assertThat(grid.ny()).isEqualTo(1597);
        assertThat(grid.gridDefinition().type()).isEqualTo("lcc");
        assertThat(grid.gridDefinition().params()).containsEntry("dx", 2539.703).containsEntry("lov", -95.0);
        assertThat(grid.metadata()).containsEntry("dataTemplate", 0).containsEntry("scanMode", 64)
                .containsEntry("gridRelativeWinds", true);
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-17T11:00:00Z"));
        GridStats stats = GridStats.of(grid.floatData());
        assertThat(stats.nan()).isZero();
        assertThat(stats.min()).isZero();
        assertThat(stats.max()).isCloseTo(32.37, within(1e-3));
    }
}
