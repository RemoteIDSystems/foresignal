package ai.foresignal.raster.decode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

import ai.foresignal.raster.GeosGrid;
import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * NetCDF4 decoding (prompt 046). The GOES band 13 cases need {@code ./gradlew downloadTestFixtures}
 * and skip with a logged reason without it; the derived-motion-winds cases use the bundled self-test
 * file and always run.
 */
class NetcdfDecoderTest {

    static final DecodeRequest CMI = DecodeRequest.netcdf("goes-east-ir", "CMI");

    @TempDir Path dir;

    NetcdfDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new NetcdfDecoder(DecoderFixtures.safe(dir));
    }

    @Test
    @DisplayName("CMI is enhanced to kelvin, fill becomes NaN, and the geos grid comes from the projection attributes")
    void cmiEnhancementAndGeos() {
        Path file = DecoderFixtures.goesOrSkip(dir);
        DecodedGrid grid = decoder.decode(file, CMI);

        assertThat(grid.nx()).isEqualTo(2500);
        assertThat(grid.ny()).isEqualTo(1500);
        int nan = 0;
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (float k : grid.floatData()) {
            if (Float.isNaN(k)) {
                nan++;
                continue;
            }
            min = Math.min(min, k);
            max = Math.max(max, k);
        }
        assertThat(nan).as("_FillValue samples (fixture README)").isEqualTo(47_163);
        assertThat(min).isCloseTo(190.526f, offset(0.01f));
        assertThat(max).isCloseTo(298.684f, offset(0.01f));

        GridDefinition def = grid.gridDefinition();
        assertThat(def.type()).isEqualTo(GridDefinition.GEOS);
        assertThat(def.params()).containsEntry("lon0", -75.0).containsEntry("sweep", "x")
                .containsEntry("h", 35_786_023.0).containsEntry("a", 6_378_137.0).containsEntry("b", 6_356_752.31414)
                .containsEntry("x0", -0.101332001).containsEntry("y0", 0.128212005)
                .containsEntry("dx", 5.6e-5).containsEntry("dy", -5.6e-5);
        assertThat(def.west()).isCloseTo(-150.23, offset(0.01));
        assertThat(def.east()).isCloseTo(-52.92, offset(0.01));
        assertThat(def.south()).isCloseTo(14.56, offset(0.01));
        assertThat(def.north()).isCloseTo(56.32, offset(0.01));
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-12T12:01:17Z"));
        assertThat(grid.metadata()).containsEntry("obsStart", "2026-10-12T12:01:17Z")
                .containsEntry("obsEnd", "2026-10-12T12:03:55.400Z").containsEntry("platform", "G19")
                .containsEntry("units", "K").containsEntry("projection", "goes_imager_projection");
    }

    @Test
    @DisplayName("DQF above the limit marks samples missing; a header-only decode reads no samples")
    void qualityAndHeader() {
        Path file = DecoderFixtures.goesOrSkip(dir);
        DecodedGrid strict = decoder.decode(file, CMI.withQuality("DQF", 1));
        int nan = 0;
        for (float k : strict.floatData()) {
            if (Float.isNaN(k)) {
                nan++;
            }
        }
        assertThat(nan).as("DQF fill samples coincide with the CMI fill").isEqualTo(47_163);
        DecodedGrid header = decoder.decode(file, CMI.header());
        assertThat(header.hasData()).isFalse();
        assertThat(header.gridDefinition()).isEqualTo(strict.gridDefinition());
    }

    @Test
    @DisplayName("a missing variable is a silent format change: 'variable CMI missing'")
    void missingVariable() {
        Path winds = DecoderFixtures.selfTest("goes19-abi-dmwvc-c08-20261012-0001.nc", dir);
        assertThatThrownBy(() -> decoder.decode(winds, CMI))
                .isInstanceOf(SilentFormatChangeException.class)
                .hasMessage("variable CMI missing");
        assertThatThrownBy(() -> decoder.decode(winds, DecodeRequest.netcdf("x", "wind_speed")))
                .as("a one-dimensional variable cannot be gridded")
                .isInstanceOf(RasterFormatException.class);
    }

    @Test
    @DisplayName("a gridless variable decodes its values with fill handling (the self-test path)")
    void valuesOnly() {
        Path winds = DecoderFixtures.selfTest("goes19-abi-dmwvc-c08-20261012-0001.nc", dir);
        DecodedGrid grid = decoder.decode(winds, DecodeRequest.netcdf("x", "wind_speed").valuesOnly());
        assertThat(grid.gridDefinition()).isNull();
        assertThat(grid.cells()).isEqualTo(712);
        assertThat(grid.floatData()[0]).isCloseTo(30.978f, offset(0.001f));
        for (float v : grid.floatData()) {
            assertThat(v).isBetween(3f, 155f);
        }
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-12T00:01:18.200Z"));
        assertThat(decoder.supports(CMI)).isTrue();
        assertThat(decoder.supports(Grib2DecoderTest.REFLECTIVITY)).isFalse();
    }

    @Test
    @DisplayName("the outline of a geos grid is its projected edge; an all-space grid is refused")
    void outline() {
        GeosGrid conus = new GeosGrid(2500, 1500, -75.0, 35_786_023.0, GeosGrid.GRS80_A, GeosGrid.GRS80_B, true,
                -0.101332, 5.6e-5, 0.128212, -5.6e-5, new double[] {-180, -90, 180, 90});
        double[] box = NetcdfDecoder.outline(conus, 2500, 1500);
        assertThat(box[0]).isCloseTo(-150.23, offset(0.01));
        assertThat(box[3]).isCloseTo(56.32, offset(0.01));
        GeosGrid space = new GeosGrid(10, 10, -75.0, 35_786_023.0, GeosGrid.GRS80_A, GeosGrid.GRS80_B, true,
                0.2, 0.001, 0.2, -0.001, new double[] {-180, -90, 180, 90});
        assertThatThrownBy(() -> NetcdfDecoder.outline(space, 10, 10)).isInstanceOf(RasterFormatException.class);
    }
}
