package ai.foresignal.raster.decode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** GRIB2 decoding against the real fixtures (prompt 046). */
class Grib2DecoderTest {

    static final DecodeRequest REFLECTIVITY =
            DecodeRequest.grib2("mrms-reflectivity", "MergedReflectivityQCComposite", 209, 10, 0, 102, 500.0, "500 m");

    @TempDir Path dir;

    Grib2Decoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new Grib2Decoder(DecoderFixtures.safe(dir));
    }

    @Test
    @DisplayName("selects the field by discipline, category, number and level, with the grid from the GDS")
    void selectsByParameterAndLevel() throws IOException {
        Path file = DecoderFixtures.mrms(DecoderFixtures.MRMS_REFLECTIVITY, dir);
        DecodedGrid grid = decoder.decode(file, REFLECTIVITY);

        assertThat(grid.nx()).isEqualTo(7000);
        assertThat(grid.ny()).isEqualTo(3500);
        assertThat(grid.floatData()).hasSize(DecoderFixtures.MRMS_CELLS);
        GridDefinition def = grid.gridDefinition();
        assertThat(def.type()).isEqualTo(GridDefinition.LATLON);
        assertThat(def.west()).isEqualTo(-130.0);
        assertThat(def.east()).isEqualTo(-60.0);
        assertThat(def.north()).isEqualTo(55.0);
        assertThat(def.south()).isEqualTo(20.0);
        assertThat(def.params()).containsEntry("la1", 54.995).containsEntry("lo1", -129.995)
                .containsEntry("dx", 0.01).containsEntry("dy", 0.01);
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-12T12:00:37Z"));
        assertThat(grid.referenceTime()).isEqualTo(grid.validTime());
        assertThat(grid.metadata()).containsEntry("discipline", 209).containsEntry("category", 10)
                .containsEntry("number", 0).containsEntry("levelType", 102).containsEntry("dataTemplate", 41)
                .containsEntry("gridTemplate", 0).containsEntry("scanMode", 0);
        assertThat(grid.floatData()[0]).as("the north-west corner is outside radar coverage").isEqualTo(-999f);
        try (Stream<Path> files = Files.list(dir)) {
            assertThat(files).as("the inflated sibling is deleted").containsExactly(file);
        }
    }

    @Test
    @DisplayName("a missing field (or level) is a silent format change naming the field and level")
    void missingField() {
        Path mesh = DecoderFixtures.mrms(DecoderFixtures.MRMS_MESH, dir);
        assertThatThrownBy(() -> decoder.decode(mesh, REFLECTIVITY))
                .isInstanceOf(SilentFormatChangeException.class)
                .hasMessage("field MergedReflectivityQCComposite missing at level 500 m")
                .satisfies(e -> assertThat(((SilentFormatChangeException) e).productId()).isEqualTo("mrms-reflectivity"));

        Path reflectivity = DecoderFixtures.mrms(DecoderFixtures.MRMS_REFLECTIVITY, dir);
        DecodeRequest otherLevel =
                DecodeRequest.grib2("mrms-reflectivity", "MergedReflectivityQCComposite", 209, 10, 0, 102, 1000.0, "1000 m");
        assertThatThrownBy(() -> decoder.decode(reflectivity, otherLevel))
                .isInstanceOf(SilentFormatChangeException.class)
                .hasMessage("field MergedReflectivityQCComposite missing at level 1000 m");

        DecodeRequest anyLevel = DecodeRequest.grib2("x", "Other", 209, 99, 99, null, null, null);
        assertThatThrownBy(() -> decoder.decode(reflectivity, anyLevel)).hasMessage("field Other missing");
    }

    @Test
    @DisplayName("a header-only decode reads the grid and times but no samples")
    void headerOnly() {
        DecodedGrid grid = decoder.decode(DecoderFixtures.mrms(DecoderFixtures.MRMS_MESH, dir),
                DecodeRequest.grib2("mrms-mesh-60m", "MESH", 209, 3, 30, 102, 500.0, "500 m").header());
        assertThat(grid.hasData()).isFalse();
        assertThat(grid.nx()).isEqualTo(7000);
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
    }

    @Test
    @DisplayName("template 5.3 (GFS complex packing): a global 1-degree grid that wraps")
    void complexPacking() {
        DecodedGrid grid = decoder.decode(DecoderFixtures.selfTest("gfs-1p00-tmp2m-f000.grib2", dir),
                DecodeRequest.grib2("gfs", "TMP", 0, 0, 0, 103, 2.0, "2 m"));
        assertThat(grid.metadata()).containsEntry("dataTemplate", 3);
        assertThat(grid.cells()).isEqualTo(360 * 181);
        GridDefinition def = grid.gridDefinition();
        assertThat(def.west()).isEqualTo(-180.0);
        assertThat(def.east()).isEqualTo(180.0);
        assertThat(def.north()).isEqualTo(90.0);
        assertThat(def.params()).containsEntry("lo1", 0.0).containsEntry("la1", 90.0);
        for (float kelvin : grid.floatData()) {
            assertThat(kelvin).isBetween(200f, 330f);
        }
    }

    @Test
    @DisplayName("template 5.42 (CCSDS/AEC via libaec): the ECMWF land-sea mask decodes to 0..1")
    void ccsds() {
        DecodedGrid grid = decoder.decode(DecoderFixtures.selfTest("ecmwf-ifs-0p25-lsm-0h.grib2", dir),
                DecodeRequest.grib2("ifs", "lsm", 2, 0, 0, null, null, null));
        assertThat(grid.metadata()).containsEntry("dataTemplate", 42);
        assertThat(grid.cells()).isEqualTo(1440 * 721);
        assertThat(grid.gridDefinition().params()).containsEntry("lo1", -180.0);
        float max = 0;
        for (float v : grid.floatData()) {
            assertThat(v).isBetween(0f, 1f);
            max = Math.max(max, v);
        }
        assertThat(max).isEqualTo(1f);
    }

    @Test
    @DisplayName("a file that is not GRIB2 is a format error, not a format change")
    void notGrib() throws IOException {
        Path junk = Files.writeString(dir.resolve("junk.grib2"), "not a grib file at all, just text");
        assertThatThrownBy(() -> decoder.decode(junk, REFLECTIVITY)).isInstanceOf(RasterFormatException.class);
        assertThatThrownBy(() -> decoder.decode(junk, DecodeRequest.netcdf("x", "CMI")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(decoder.supports(REFLECTIVITY)).isTrue();
        assertThat(decoder.supports(DecodeRequest.netcdf("x", "CMI"))).isFalse();
    }

    @Test
    @DisplayName("scanning modes are normalized to rows north to south and columns west to east")
    void reorder() {
        float[] southUp = {7, 8, 9, 4, 5, 6, 1, 2, 3};
        Grib2Decoder.reorder(southUp, 3, 3, Grib2Decoder.SCAN_J_POSITIVE);
        assertThat(southUp).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
        float[] eastFirst = {3, 2, 1, 6, 5, 4};
        Grib2Decoder.reorder(eastFirst, 3, 2, Grib2Decoder.SCAN_I_NEGATIVE);
        assertThat(eastFirst).containsExactly(1, 2, 3, 4, 5, 6);
        float[] untouched = {1, 2, 3, 4};
        Grib2Decoder.reorder(untouched, 2, 2, 0);
        assertThat(untouched).containsExactly(1, 2, 3, 4);
    }

    @Test
    @DisplayName("GDS integers are sign-magnitude; the angle unit follows the basic angle")
    void rawOctets() {
        byte[] raw = new byte[72];
        raw[46] = (byte) 0x80;
        raw[49] = 0x05;
        assertThat(Grib2Decoder.signed32(raw, 47)).isEqualTo(-5);
        assertThat(Grib2Decoder.angleUnit(raw)).as("basic angle 0 means micro-degrees").isEqualTo(1e-6);
        // Basic angle 1 (octets 39-42), subdivisions 1,000,000 (octets 43-46), as MRMS writes them.
        raw[41] = 1;
        raw[42] = 0x00;
        raw[43] = 0x0f;
        raw[44] = 0x42;
        raw[45] = 0x40;
        assertThat(Grib2Decoder.angleUnit(raw)).isEqualTo(1e-6);
        raw[45] = 0x41;
        assertThat(Grib2Decoder.angleUnit(raw)).isEqualTo(1.0 / 1_000_001);
        assertThat(Grib2Decoder.angleUnit(new byte[10])).isZero();
        assertThat(Grib2Decoder.normalizeLon(230.005)).isCloseTo(-129.995, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(Grib2Decoder.normalizeLon(180.0)).isEqualTo(-180.0);
    }
}
