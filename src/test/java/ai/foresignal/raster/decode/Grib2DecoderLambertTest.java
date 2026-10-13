package ai.foresignal.raster.decode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.GridDef;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * GRIB2 grid template 3.30 (prompt 051) and the resolution and component flags, on the committed HRRR
 * REFC message and the GFS 1° wind messages of the 2026-10-12 12z runs.
 */
class Grib2DecoderLambertTest {

    static final DecodeRequest REFC = DecodeRequest.grib2("hrrr-refc", "REFC", 0, 16, 196, 10, null, "entire atmosphere");
    static final DecodeRequest GFS_U = DecodeRequest.grib2("gfs", "UGRD", 0, 2, 2, 103, 10.0, "10 m above ground");
    static final DecodeRequest GFS_V = DecodeRequest.grib2("gfs", "VGRD", 0, 2, 3, 103, 10.0, "10 m above ground");

    @TempDir Path dir;

    Grib2Decoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new Grib2Decoder(DecoderFixtures.safe(dir));
    }

    @Test
    @DisplayName("HRRR's template 3.30 becomes an lcc grid with its south-west first point; corners match prompt 045")
    void hrrrLambert() {
        Path file = DecoderFixtures.hrrr(DecoderFixtures.HRRR_REFC, dir);
        DecodedGrid grid = decoder.decode(file, REFC);
        GridDefinition def = grid.gridDefinition();
        assertThat(def.type()).isEqualTo(GridDefinition.LCC);
        assertThat(def.nx()).isEqualTo(1799);
        assertThat(def.ny()).isEqualTo(1059);
        assertThat(def.params()).containsEntry("lat1", 38.5).containsEntry("lat2", 38.5).containsEntry("lov", -97.5)
                .containsEntry("radius", 6_371_229.0).containsEntry("la1", 21.138123).containsEntry("lo1", -122.719528)
                .containsEntry("dx", 3000.0).containsEntry("dy", 3000.0);
        assertThat(def.west()).isCloseTo(-134.12, within(0.01));
        assertThat(def.east()).isCloseTo(-60.89, within(0.01));
        assertThat(def.south()).isCloseTo(21.12, within(0.01));
        assertThat(def.north()).as("the top edge bulges north at the central meridian").isCloseTo(52.63, within(0.01));
        GridDef g = GridDef.of(def);
        assertThat(g.indexToLonLat(0, 1058)[0]).isCloseTo(-122.7195, within(1e-4));
        assertThat(g.indexToLonLat(1798, 1058)[0]).isCloseTo(-72.2897, within(1e-4));
        assertThat(g.indexToLonLat(0, 0)[1]).isCloseTo(47.8386, within(1e-4));
        assertThat(g.indexToLonLat(1798, 0)[0]).isCloseTo(-60.9172, within(1e-4));
        assertThat(grid.metadata()).containsEntry("gridTemplate", 30).containsEntry("scanMode", 64)
                .containsEntry("componentFlags", 8).containsEntry("gridRelativeWinds", true)
                .containsEntry("dataTemplate", 3);
        assertThat(grid.floatData()).hasSize(1799 * 1059);
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        for (float f : grid.floatData()) {
            max = Math.max(max, f);
            min = Math.min(min, f);
        }
        assertThat(min).isEqualTo(-10.0);
        assertThat(max).isCloseTo(67.625, within(1e-3));
    }

    @Test
    @DisplayName("GFS components are earth-relative (flags 0x30) and both come from one coalesced range")
    void gfsFlags() {
        Path file = DecoderFixtures.gfs("gfs-1p00-f006-ugrd-vgrd.grib2", dir);
        DecodedGrid u = decoder.decode(file, GFS_U);
        DecodedGrid v = decoder.decode(file, GFS_V);
        assertThat(u.metadata()).containsEntry("componentFlags", 0x30).containsEntry("gridRelativeWinds", false)
                .containsEntry("message", 1);
        assertThat(v.metadata()).containsEntry("message", 2);
        assertThat(u.gridDefinition().params()).containsEntry("lo1", 0.0).containsEntry("la1", 90.0)
                .containsEntry("dx", 1.0);
        assertThat(u.nx()).isEqualTo(360);
        assertThat(u.ny()).isEqualTo(181);
        assertThat(u.validTime()).isEqualTo(java.time.Instant.parse("2026-10-12T18:00:00Z"));
    }

    @Test
    @DisplayName("a Lambert definition keeps the south-west cell as la1/lo1 and walks the outline for its box")
    void lambertDefinition() {
        Grib2Decoder.LambertBox sw = new Grib2Decoder.LambertBox(10, 5, 38.5, 38.5, -97.5, 6_371_229.0, 30.0, -100.0,
                3000.0, 3000.0, 8);
        GridDefinition def = sw.definition();
        GridDef grid = GridDef.of(def);
        double[] nw = grid.indexToLonLat(0, 0);
        assertThat(nw[1]).isGreaterThan(30.0);
        assertThat(def.params()).containsEntry("la1", 30.0).containsEntry("lo1", -100.0);
        for (double[] ij : new double[][] {{0, 0}, {9, 0}, {0, 4}, {9, 4}}) {
            double[] ll = grid.indexToLonLat(ij[0], ij[1]);
            assertThat(ll[0]).isBetween(def.west(), def.east());
            assertThat(ll[1]).isBetween(def.south(), def.north());
        }
    }
}
