package ai.foresignal.source.adapters.mrms;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The MRMS code spaces (prompt 046, Weather-Imagery-Design §2.3), including the real sentinels. */
class MrmsCodecTest {

    @TempDir Path dir;

    @Test
    @DisplayName("reflectivity: -999 is no coverage (0), -99 and below -32 dBZ are no echo (1), values round trip")
    void reflectivity() {
        assertThat(MrmsCodec.reflectivity(-999f)).isZero();
        assertThat(MrmsCodec.reflectivity(Float.NaN)).as("a bitmap-missing sample is no coverage").isZero();
        assertThat(MrmsCodec.reflectivity(-99f)).isEqualTo(1);
        assertThat(MrmsCodec.reflectivity(-32.5f)).isEqualTo(1);
        assertThat(MrmsCodec.reflectivity(-32f)).isEqualTo(2);
        assertThat(MrmsCodec.reflectivity(50f)).isEqualTo(166);
        assertThat(MrmsCodec.reflectivity(0f)).isEqualTo(66);
        assertThat(MrmsCodec.reflectivity(94.5f)).isEqualTo(255);
        assertThat(MrmsCodec.reflectivity(120f)).as("clamped at 255").isEqualTo(255);
        for (float dbz = -32f; dbz <= 94.5f; dbz += 0.5f) {
            int code = MrmsCodec.reflectivity(dbz);
            assertThat(code * 0.5 - 33.0).as("dBZ of code %d", code).isEqualTo(dbz);
        }
    }

    @Test
    @DisplayName("precipitation rate: tenths of mm/h in u16, -3 is 65535, capped at 65534")
    void precipRate() {
        assertThat(MrmsCodec.precipRate(-3f)).isEqualTo(65_535);
        assertThat(MrmsCodec.precipRate(Float.NaN)).isEqualTo(65_535);
        assertThat(MrmsCodec.precipRate(0f)).isZero();
        assertThat(MrmsCodec.precipRate(12.34f)).isEqualTo(123);
        assertThat(MrmsCodec.precipRate(125f)).isEqualTo(1250);
        assertThat(MrmsCodec.precipRate(1e6f)).isEqualTo(65_534);
    }

    @Test
    @DisplayName("precipitation flag: the flag value as given, missing is 65535")
    void precipFlag() {
        for (int flag : new int[] {0, 1, 3, 6, 7, 10, 91, 96}) {
            assertThat(MrmsCodec.precipFlag(flag)).isEqualTo(flag);
        }
        assertThat(MrmsCodec.precipFlag(-3f)).isEqualTo(65_535);
        assertThat(MrmsCodec.precipFlag(Float.NaN)).isEqualTo(65_535);
        assertThat(MrmsCodec.missingFlags(3)).containsExactly(65_535, 65_535, 65_535);
    }

    @Test
    @DisplayName("MESH: -3 is no coverage (0), -1 is 0 mm (1), mm + 1 clamped to 2..255")
    void mesh() {
        assertThat(MrmsCodec.mesh(-3f)).isZero();
        assertThat(MrmsCodec.mesh(Float.NaN)).isZero();
        assertThat(MrmsCodec.mesh(-1f)).isEqualTo(1);
        assertThat(MrmsCodec.mesh(0.2f)).as("a sliver of hail still reads as hail").isEqualTo(2);
        assertThat(MrmsCodec.mesh(16.5f)).isEqualTo(18);
        assertThat(MrmsCodec.mesh(25f)).isEqualTo(26);
        assertThat(MrmsCodec.mesh(400f)).isEqualTo(255);
    }

    @Test
    @DisplayName("the real reflectivity fixture: every -999 cell is no coverage, never no echo")
    void reflectivityFixtureSentinels() {
        Grib2Decoder decoder = new Grib2Decoder(DecoderFixtures.safe(dir));
        DecodedGrid grid = decoder.decode(DecoderFixtures.mrms(DecoderFixtures.MRMS_REFLECTIVITY, dir),
                MrmsAdapter.REFLECTIVITY_UPSTREAM.request());
        long noCoverageIn = 0;
        long noEchoIn = 0;
        for (float v : grid.floatData()) {
            if (v == -999f) {
                noCoverageIn++;
            } else if (v == -99f) {
                noEchoIn++;
            }
        }
        assertThat(noCoverageIn).as("MRMS -999 cells (fixture README)").isEqualTo(8_174_561L);
        assertThat(noEchoIn).as("MRMS -99 cells").isEqualTo(14_293_209L);
        int[] codes = MrmsCodec.reflectivity(grid.floatData());
        long code0 = 0;
        long code1 = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i] == 0) {
                code0++;
                assertThat(grid.floatData()[i]).isEqualTo(-999f);
            } else if (codes[i] == 1) {
                code1++;
            }
        }
        assertThat(code0).isEqualTo(noCoverageIn);
        assertThat(code1).as("no echo includes the few values below -32 dBZ").isGreaterThanOrEqualTo(noEchoIn);
    }

    @Test
    @DisplayName("the no-coverage-heavy Hawaii MESH sample: -3 maps to 0 and -1 to 1, nothing else")
    void hawaiiMeshSentinels() {
        Grib2Decoder decoder = new Grib2Decoder(DecoderFixtures.safe(dir));
        DecodedGrid grid = decoder.decode(DecoderFixtures.mrms(DecoderFixtures.MRMS_HAWAII_MESH, dir),
                MrmsAdapter.MESH_UPSTREAM.request());
        int[] codes = MrmsCodec.mesh(grid.floatData());
        long zero = 0;
        long one = 0;
        for (int code : codes) {
            assertThat(code).isBetween(0, 1);
            if (code == 0) {
                zero++;
            } else {
                one++;
            }
        }
        assertThat(zero).isEqualTo(2_365_802L);
        assertThat(one).isEqualTo(3_354_198L);
    }
}
