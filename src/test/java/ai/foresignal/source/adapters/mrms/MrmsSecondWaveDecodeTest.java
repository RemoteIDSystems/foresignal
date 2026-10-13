package ai.foresignal.source.adapters.mrms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The four MRMS second-wave products (prompt 053) against real files, one per product: the GRIB2 field each
 * {@code Upstream} selects, its sentinels and their codes, the rotation grid's 2 × 2 reduction, and the FFG
 * percent quantization.
 *
 * <p>Fixtures captured 2026-10-13 (see {@code src/test/resources/fixtures/mrms/README.md}): rotation 04:32 UTC,
 * QPE 1 h 04:32 UTC and the FFG ratio 04:30 UTC are committed; the 4.7 MB QPE 24 h of 03:00 UTC is downloaded by
 * {@code ./gradlew downloadTestFixtures}, and its test is skipped without it.
 */
class MrmsSecondWaveDecodeTest {

    @TempDir Path dir;

    Grib2Decoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new Grib2Decoder(DecoderFixtures.safe(dir));
    }

    private static Map<String, ProductSpec> specs() {
        Map<String, ProductSpec> out = new java.util.HashMap<>();
        for (ProductSpec spec : new MrmsAdapter(null, "").descriptor().products()) {
            out.put(spec.id(), spec);
        }
        return out;
    }

    private static int count(int[] codes, int code) {
        int n = 0;
        for (int c : codes) {
            if (c == code) {
                n++;
            }
        }
        return n;
    }

    private static int max(int[] codes, int except) {
        int m = Integer.MIN_VALUE;
        for (int c : codes) {
            if (c != except && c > m) {
                m = c;
            }
        }
        return m;
    }

    @Test
    @DisplayName("rotation: 209/3/3 at 500 m on a 0.005° grid, reduced 2 x 2 by maximum to the reflectivity grid")
    void rotation() {
        Path file = DecoderFixtures.mrms(DecoderFixtures.MRMS_ROTATION, dir);
        DecodedGrid header = decoder.decode(file, MrmsAdapter.ROTATION_UPSTREAM.request().header());
        GridDefinition fine = header.gridDefinition();
        assertThat(fine.nx()).isEqualTo(14_000);
        assertThat(fine.ny()).isEqualTo(7_000);
        assertThat(fine.params()).containsEntry("dx", 0.005).containsEntry("la1", 54.9975).containsEntry("lo1", -129.9975);
        assertThat(header.validTime()).isEqualTo(Instant.parse("2026-10-13T04:32:00Z"));
        assertThat(MrmsAdapter.reduction(fine)).isEqualTo(2);

        Path reflectivity = DecoderFixtures.mrms(DecoderFixtures.MRMS_REFLECTIVITY, dir);
        GridDefinition shared = decoder.decode(reflectivity, MrmsAdapter.REFLECTIVITY_UPSTREAM.request().header())
                .gridDefinition();
        GridDefinition coarse = MrmsAdapter.coarsen(fine, 2);
        assertThat(coarse).as("the reduced grid is the 0.01° grid of the other products").isEqualTo(shared);
        assertThat(MrmsAdapter.reduction(shared)).isEqualTo(1);

        DecodedGrid full = decoder.decode(file, MrmsAdapter.ROTATION_UPSTREAM.request());
        int[] codes = MrmsCodec.rotationCoarsened(full.floatData(), full.nx(), full.ny(), 2);
        assertThat(codes).hasSize(DecoderFixtures.MRMS_CELLS);
        assertThat(count(codes, MrmsCodec.ROTATION_NO_COVERAGE)).as("the file marks no coverage nowhere").isZero();
        assertThat(count(codes, MrmsCodec.ROTATION_NONE)).isEqualTo(24_408_713);
        assertThat(DecoderFixtures.MRMS_CELLS - count(codes, MrmsCodec.ROTATION_NONE)).isEqualTo(91_287);
        assertThat(max(codes, -1)).as("49 x 0.001 s-1").isEqualTo(491);
        ProductSpec spec = specs().get(MrmsAdapter.ROTATION);
        assertThat(spec.physical(491)).isCloseTo(0.049, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(spec.physical(MrmsCodec.ROTATION_NONE)).isCloseTo(0.0, org.assertj.core.data.Offset.offset(1e-12));
        assertThat(spec.codes()).containsEntry(0, "noCoverage").containsEntry(1, "noEcho");
    }

    @Test
    @DisplayName("QPE 1 h: 209/6/2 at 0 m; -3 is no coverage (65535), tenths of a millimeter")
    void qpe1h() {
        Path file = DecoderFixtures.mrms(DecoderFixtures.MRMS_QPE_1H, dir);
        DecodedGrid grid = decoder.decode(file, MrmsAdapter.QPE_1H_UPSTREAM.request());
        assertThat(grid.nx()).isEqualTo(7000);
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-13T04:32:00Z"));
        int[] codes = MrmsCodec.qpe(grid.floatData());
        assertThat(count(codes, MrmsCodec.U16_MISSING)).isEqualTo(8_257_230);
        assertThat(count(codes, 0)).isEqualTo(15_190_723);
        assertThat(max(codes, MrmsCodec.U16_MISSING)).as("39.2 mm").isEqualTo(392);
        assertThat(specs().get(MrmsAdapter.QPE_1H).physical(392)).isCloseTo(39.2, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    @DisplayName("QPE 24 h: 209/6/41 at 0 m; -3 is no coverage (downloaded fixture)")
    void qpe24h() {
        Path file = DecoderFixtures.mrmsDownloadedOrSkip(DecoderFixtures.MRMS_QPE_24H, dir);
        DecodedGrid grid = decoder.decode(file, MrmsAdapter.QPE_24H_UPSTREAM.request());
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-13T03:00:00Z"));
        int[] codes = MrmsCodec.qpe(grid.floatData());
        assertThat(count(codes, MrmsCodec.U16_MISSING)).isEqualTo(1_512_918);
        assertThat(count(codes, 0)).isEqualTo(14_866_789);
        assertThat(max(codes, MrmsCodec.U16_MISSING)).as("385.5 mm").isEqualTo(3855);
    }

    @Test
    @DisplayName("FFG ratio: 209/12/26 at the surface; -999 is no coverage; percent in tenths round-trips")
    void ffgRatio() {
        Path file = DecoderFixtures.mrms(DecoderFixtures.MRMS_FFG, dir);
        DecodedGrid grid = decoder.decode(file, MrmsAdapter.FFG_UPSTREAM.request());
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-13T04:30:00Z"));
        assertThat(grid.gridDefinition().params()).as("the longitude is encoded a few micro-degrees off")
                .containsEntry("lo1", -129.994996);
        int[] codes = MrmsCodec.ffgPercent(grid.floatData());
        assertThat(count(codes, MrmsCodec.U16_MISSING)).isEqualTo(15_738_788);
        assertThat(count(codes, 0)).isEqualTo(8_542_783);
        assertThat(max(codes, MrmsCodec.U16_MISSING)).as("90 %").isEqualTo(900);
        ProductSpec spec = specs().get(MrmsAdapter.FFG_RATIO);
        assertThat(spec.units()).isEqualTo("%");
        for (float pct : new float[] {0f, 0.1f, 5f, 99.9f, 100f, 104.96f, 250.3f}) {
            int code = MrmsCodec.ffgPercent(pct);
            assertThat(spec.physical(code)).as("%s %%", pct)
                    .isCloseTo(Math.round(pct * 10.0) / 10.0, org.assertj.core.data.Offset.offset(1e-9));
        }
    }

    @Test
    @DisplayName("codec edges: rotation 0 and NaN, QPE missing, FFG sentinel and cap, block maximum")
    void codecEdges() {
        assertThat(MrmsCodec.rotation(Float.NaN)).isEqualTo(MrmsCodec.ROTATION_NO_COVERAGE);
        assertThat(MrmsCodec.rotation(-1f)).isEqualTo(MrmsCodec.ROTATION_NO_COVERAGE);
        assertThat(MrmsCodec.rotation(0f)).isEqualTo(MrmsCodec.ROTATION_NONE);
        assertThat(MrmsCodec.rotation(0.01f)).as("below the code step").isEqualTo(MrmsCodec.ROTATION_NONE);
        assertThat(MrmsCodec.rotation(3f)).isEqualTo(31);
        assertThat(MrmsCodec.rotation(1e9f)).isEqualTo(65_535);
        assertThat(MrmsCodec.qpe(-1f)).isEqualTo(MrmsCodec.U16_MISSING);
        assertThat(MrmsCodec.qpe(-3f)).isEqualTo(MrmsCodec.U16_MISSING);
        assertThat(MrmsCodec.qpe(12.34f)).isEqualTo(123);
        assertThat(MrmsCodec.qpe(1e6f)).isEqualTo(65_534);
        assertThat(MrmsCodec.ffgPercent(-999f)).isEqualTo(MrmsCodec.U16_MISSING);
        assertThat(MrmsCodec.ffgPercent(Float.NaN)).isEqualTo(MrmsCodec.U16_MISSING);
        assertThat(MrmsCodec.ffgPercent(1e6f)).isEqualTo(65_534);

        // 4 x 2 -> 2 x 1: each output keeps its block's largest code, and no coverage only when the block is all 0.
        int[] codes = {0, 0, 1, 31, 0, 0, 5, 2};
        assertThat(MrmsCodec.coarsenMax(codes, 4, 2, 2)).containsExactly(0, 31);
        float[] values = {Float.NaN, Float.NaN, 0f, 3f, Float.NaN, Float.NaN, 0.4f, 0f};
        assertThat(MrmsCodec.rotationCoarsened(values, 4, 2, 2)).containsExactly(0, 31);
        assertThat(MrmsCodec.coarsenMax(codes, 4, 2, 1)).isSameAs(codes);
        assertThatThrownBy(() -> MrmsCodec.coarsenMax(codes, 3, 2, 2)).isInstanceOf(IllegalArgumentException.class);
    }
}
