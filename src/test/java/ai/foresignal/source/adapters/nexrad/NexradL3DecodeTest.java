package ai.foresignal.source.adapters.nexrad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.decode.SafeNetcdf;
import ai.foresignal.source.spi.S3Object;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * NEXRAD Level III decoding (prompt 106 task 4, post-review fix): the committed real N0B/N0G files
 * ({@code src/test/resources/fixtures/nexrad-l3/README.md}) through NetCDF-Java's NIDS IOSP, then the polar
 * to latlon resampling and u8 codes. The expected values are the ones the README records from opening the
 * files with cdm-radial 5.11.0.
 */
class NexradL3DecodeTest {

    static final String N0B = "FWS_N0B_2026_10_16_19_15_25";
    static final String N0G = "FWS_N0G_2026_10_16_19_01_16";

    @TempDir
    Path dir;

    private Path fixture(String name) throws IOException {
        Path target = dir.resolve("tmp").resolve(name);
        Files.createDirectories(target.getParent());
        Files.write(target, DecoderFixtures.bytes("fixtures/nexrad-l3/" + name));
        return target;
    }

    private SafeNetcdf safe() throws IOException {
        Path tmp = dir.resolve("tmp");
        Files.createDirectories(tmp);
        return DecoderFixtures.safe(tmp);
    }

    @Test
    @DisplayName("N0B (product 153) decodes to 720 radials x 1840 gates of dBZ at the Dallas/Fort Worth radar")
    void decodesN0b() throws IOException {
        PolarScan scan = NidsRadialReader.read(safe(), fixture(N0B));
        assertThat(scan.variable()).isEqualTo("BaseReflectivityDR");
        assertThat(scan.units()).isEqualTo("dBz");
        assertThat(scan.radarLat()).isCloseTo(32.573, within(1e-3));
        assertThat(scan.radarLon()).isCloseTo(-97.303, within(1e-3));
        assertThat(scan.time()).isEqualTo(Instant.parse("2026-10-16T19:15:25Z"));
        assertThat(scan.azimuths()).hasSize(720);
        assertThat(scan.gateStarts()).hasSize(1840);
        assertThat(scan.gateWidth()).isCloseTo(249.75, within(0.01));
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        int nan = 0;
        for (float v : scan.values()) {
            if (Float.isNaN(v)) {
                nan++;
            } else {
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
        }
        assertThat(min).isEqualTo(-26.0f);
        assertThat(max).isEqualTo(49.0f);
        assertThat(nan).isEqualTo(979_317);
    }

    @Test
    @DisplayName("N0G (product 154) decodes to m/s velocity")
    void decodesN0g() throws IOException {
        PolarScan scan = NidsRadialReader.read(safe(), fixture(N0G));
        assertThat(scan.variable()).isEqualTo("BaseVelocityDV");
        assertThat(scan.units()).isEqualTo("m/s");
        assertThat(scan.gateStarts()).hasSize(1200);
        assertThat(scan.time()).isEqualTo(Instant.parse("2026-10-16T19:01:16Z"));
    }

    @Test
    @DisplayName("resampled to 0.005° within 230 km around the radar, NaN outside the range")
    void resamplesFixture() throws IOException {
        PolarScan scan = NidsRadialReader.read(safe(), fixture(N0B));
        PolarResampler.Resampled r = PolarResampler.resample(scan, 0.005, 230);
        assertThat(r.grid().nx()).isEqualTo(981);
        assertThat(r.grid().ny()).isEqualTo(827);
        assertThat(r.grid().west()).isCloseTo(-99.7555, within(1e-4));
        assertThat(r.grid().north()).isCloseTo(34.6405, within(1e-4));
        long valued = 0;
        float max = -Float.MAX_VALUE;
        for (float v : r.values()) {
            if (!Float.isNaN(v)) {
                valued++;
                max = Math.max(max, v);
            }
        }
        assertThat(valued).isEqualTo(189_220);
        assertThat(max).isEqualTo(45.5f);
        // The corners are beyond 230 km.
        assertThat(r.values()[0]).isNaN();
        assertThat(r.values()[r.values().length - 1]).isNaN();
    }

    /** A synthetic scan: 360 radials of 1°, 100 gates of 1 km, each sample = radial index. */
    static PolarScan synthetic() {
        float[] az = new float[360];
        for (int i = 0; i < 360; i++) {
            az[i] = (i + 90) % 360;
        }
        float[] gates = new float[100];
        for (int g = 0; g < 100; g++) {
            gates[g] = g * 1000f;
        }
        float[] values = new float[360 * 100];
        for (int i = 0; i < 360; i++) {
            for (int g = 0; g < 100; g++) {
                values[i * 100 + g] = az[i];
            }
        }
        return new PolarScan("Test", "deg", 35.0, -100.0, az, gates, values, null);
    }

    @Test
    @DisplayName("each cell takes the radial covering its bearing, whatever the file's radial order")
    void bearingPicksRadial() {
        PolarResampler.Resampled r = PolarResampler.resample(synthetic(), 0.01, 90);
        GridDef grid = GridDef.of(r.grid());
        // 50 km due east of the radar: bearing ~90°, the sample is the radial's azimuth.
        double[] east = grid.lonLatToIndex(-100.0 + 50 / (111.32 * Math.cos(Math.toRadians(35))), 35.0);
        float v = r.values()[(int) Math.round(east[1]) * r.grid().nx() + (int) Math.round(east[0])];
        assertThat(v).isCloseTo(90f, within(1.01f));
        double[] north = grid.lonLatToIndex(-100.0, 35.0 + 50 / 111.32);
        float n = r.values()[(int) Math.round(north[1]) * r.grid().nx() + (int) Math.round(north[0])];
        assertThat(Math.min(n, 360 - n)).isLessThanOrEqualTo(1.01f);
    }

    @Test
    @DisplayName("beyond the last gate is NaN even inside the requested range")
    void beyondLastGate() {
        PolarResampler.Resampled r = PolarResampler.resample(synthetic(), 0.01, 150);
        GridDef grid = GridDef.of(r.grid());
        double[] far = grid.lonLatToIndex(-100.0, 35.0 + 120 / 111.32);
        assertThat(r.values()[(int) Math.round(far[1]) * r.grid().nx() + (int) Math.round(far[0])]).isNaN();
    }

    @Test
    @DisplayName("the azimuth lookup covers every bin for a full 0.5° sweep")
    void lookupFull() {
        float[] az = new float[720];
        for (int i = 0; i < 720; i++) {
            az[i] = (float) ((69.0 + i * 0.5) % 360.0);
        }
        int[] lookup = PolarResampler.azimuthLookup(az);
        assertThat(Arrays.stream(lookup).filter(r -> r < 0).count()).isZero();
        assertThat(az[lookup[(int) (69.0 * PolarResampler.BINS_PER_DEGREE)]]).isCloseTo(69.0f, within(0.5f));
    }

    @Test
    @DisplayName("codes: 0.5 dBZ from −32 (N0B), 0.5 m/s from −64 (N0G), NaN is nodata 0, clamped to 1..255")
    void codes() {
        assertThat(PolarResampler.codes(new float[] {Float.NaN, -32f, -26f, 49f, 200f}, 0.5, -32))
                .containsExactly(0, 1, 12, 162, 255);
        assertThat(PolarResampler.codes(new float[] {-62f, 0f, 50f}, 0.5, -64)).containsExactly(4, 128, 228);
    }

    @Test
    @DisplayName("listing prefixes and keys follow the bucket's grammar")
    void keys() {
        assertThat(NexradL3Adapter.dayPrefix("FWS", "N0B", Instant.parse("2026-10-16T19:15:25Z")))
                .isEqualTo("FWS_N0B_2026_10_16_");
        var frame = NexradL3Adapter.frame(new S3Object(N0B, 249_815, null, null)).orElseThrow();
        assertThat(frame.site()).isEqualTo("FWS");
        assertThat(frame.product()).isEqualTo("N0B");
        assertThat(frame.time()).isEqualTo(Instant.parse("2026-10-16T19:15:25Z"));
        assertThat(NexradL3Adapter.frame(new S3Object("FWS_N0Q_2026_10_16_19_15_25", 1, null, null))).isEmpty();
        assertThat(NexradL3Adapter.code(NexradL3Adapter.N0G)).isEqualTo("N0G");
        assertThat(NexradL3Adapter.code(NexradL3Adapter.N0B)).isEqualTo("N0B");
    }
}
