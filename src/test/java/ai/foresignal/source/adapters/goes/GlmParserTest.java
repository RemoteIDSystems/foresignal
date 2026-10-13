package ai.foresignal.source.adapters.goes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link GlmParser} on the captured granules (prompt 048): quality-0 flashes only, times from
 * {@code product_time} plus the first-event offset, energy in femtojoules, and a missing variable as a
 * silent format change.
 */
class GlmParserTest {

    static final String ACTIVE = "OR_GLM-L2-LCFA_G19_s20262852157200_e20262852157400_c20262852157421.nc";
    static final String QUIET = "OR_GLM-L2-LCFA_G19_s20262850359400_e20262850400000_c20262850400017.nc";
    static final String FDC = "OR_ABI-L2-FDCC-M6_G19_s20262852156170_e20262852158544_c20262852159041.nc";

    @TempDir Path dir;

    NetcdfDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new NetcdfDecoder(DecoderFixtures.safe(dir));
    }

    /** Copies a fixture into the decoder's allowed directory. */
    Path fixture(String source, String name) throws Exception {
        return Files.write(dir.resolve(name), AdapterFixtures.bytes(source, name));
    }

    @Test
    @DisplayName("the active granule: 450 flashes, 420 with quality flag 0, times around product_time")
    void active() throws Exception {
        GlmParser.Result r = GlmParser.parse(decoder, fixture("glm", ACTIVE), GoesGlmAdapter.ID);

        assertThat(r.total()).isEqualTo(450);
        assertThat(r.flashes().size()).isEqualTo(420);
        assertThat(r.dropped()).isEqualTo(30);
        assertThat(r.productTime()).isEqualTo(Instant.parse("2026-10-12T21:57:20Z"));
        FlashBatch f = r.flashes();
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        float eMin = Float.MAX_VALUE;
        float eMax = 0;
        for (int i = 0; i < f.size(); i++) {
            min = Math.min(min, f.timesMs()[i]);
            max = Math.max(max, f.timesMs()[i]);
            eMin = Math.min(eMin, f.energyFj()[i]);
            eMax = Math.max(eMax, f.energyFj()[i]);
            assertThat(f.lat()[i]).isBetween(-90f, 90f);
            assertThat(f.lon()[i]).isBetween(-180f, 180f);
        }
        // Offsets run from about -0.96 s (an event in the previous granule) to 19.4 s.
        assertThat(Instant.ofEpochMilli(min)).isBetween(Instant.parse("2026-10-12T21:57:19Z"),
                Instant.parse("2026-10-12T21:57:20Z"));
        assertThat(Instant.ofEpochMilli(max)).isBetween(Instant.parse("2026-10-12T21:57:39Z"),
                Instant.parse("2026-10-12T21:57:40Z"));
        assertThat(eMin).isBetween(1.2f, 1.4f);
        assertThat(eMax).isBetween(5_000f, 5_300f);
        assertThat(LightningClusterer.dbscan(f)).hasSize(37);
    }

    @Test
    @DisplayName("the quiet granule: 145 flashes, 134 with quality flag 0")
    void quiet() throws Exception {
        GlmParser.Result r = GlmParser.parse(decoder, fixture("glm", QUIET), GoesGlmAdapter.ID);

        assertThat(r.total()).isEqualTo(145);
        assertThat(r.flashes().size()).isEqualTo(134);
        assertThat(r.productTime()).isEqualTo(Instant.parse("2026-10-12T03:59:40Z"));
    }

    @Test
    @DisplayName("assemble: product_time + offset, joules to femtojoules, non-zero flags and bad positions dropped")
    void assemble() {
        GlmParser.Result r = GlmParser.assemble(845_114_240.0,
                new float[] {30f, 31f, Float.NaN, 95f, 32f},
                new float[] {-97f, -98f, -99f, -100f, -101f},
                new float[] {-0.5f, 1.25f, 2f, 3f, 4f},
                new float[] {2.5e-14f, 1e-12f, 1e-14f, 1e-14f, Float.NaN},
                new float[] {0f, 0f, 0f, 0f, 1f});

        assertThat(r.total()).isEqualTo(5);
        assertThat(r.flashes().size()).isEqualTo(2);
        assertThat(r.flashes().timesMs()).containsExactly(Instant.parse("2026-10-12T21:57:19.500Z").toEpochMilli(),
                Instant.parse("2026-10-12T21:57:21.250Z").toEpochMilli());
        assertThat(r.flashes().energyFj()[0]).isCloseTo(25f, org.assertj.core.data.Offset.offset(0.01f));
        assertThat(r.flashes().energyFj()[1]).isCloseTo(1000f, org.assertj.core.data.Offset.offset(0.1f));
        assertThatThrownBy(() -> GlmParser.assemble(0, new float[1], new float[2], new float[1], new float[1], new float[1]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(GlmParser.assemble(0, new float[0], new float[0], new float[0], new float[0], new float[0]).flashes())
                .isSameAs(FlashBatch.EMPTY);
    }

    @Test
    @DisplayName("a file without the flash variables is a silent format change naming the variable")
    void missingVariable() throws Exception {
        Path notGlm = fixture("fdc", FDC);
        assertThatThrownBy(() -> GlmParser.parse(decoder, notGlm, GoesGlmAdapter.ID))
                .isInstanceOf(SilentFormatChangeException.class).hasMessageContaining("variable flash_lat missing");
    }
}
