package ai.foresignal.raster.geotiff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.testsupport.raster.GeoTiffFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link GeoKeyParser} (prompt 054): EPSG:4326 and EPSG:3857 from the GeoKeyDirectory, the corner
 * geotransform from a tie point and pixel scale or an axis-aligned ModelTransformation, the
 * PixelIsPoint half-pixel shift, and the refusals (rotation, another EPSG, user-defined, no keys).
 */
class GeoKeyParserTest {

    private static GeoReference parse(GeoTiffFixtures fixture) throws GeoTiffException {
        return GeoKeyParser.parse(TiffDirectory.parse(fixture.build()));
    }

    @Test
    @DisplayName("SatDump 1.2.2's equirectangular tags: EPSG:4326, corner transform from the tie point and scale")
    void wgs84() throws Exception {
        GeoReference ref = parse(GeoTiffFixtures.wgs84(-110, 42, 0.25));
        assertThat(ref.epsg()).isEqualTo(4326);
        assertThat(ref.crs()).isEqualTo("EPSG:4326");
        assertThat(ref.pixelIsPoint()).isFalse();
        assertThat(ref.gt()).containsExactly(-110, 0.25, 0, 42, 0, -0.25);
        assertThat(ref.lonLat(4, 2)).containsExactly(-109.0, 41.5);
    }

    @Test
    @DisplayName("a big-endian file reads the same")
    void bigEndian() throws Exception {
        GeoReference ref = parse(GeoTiffFixtures.wgs84(-110, 42, 0.25).bigEndian());
        assertThat(ref.gt()).containsExactly(-110, 0.25, 0, 42, 0, -0.25);
    }

    @Test
    @DisplayName("a tie point away from raster (0, 0) moves the origin")
    void offsetTiepoint() throws Exception {
        GeoReference ref = parse(GeoTiffFixtures.wgs84(0, 0, 0.5).tiepoint(10, 4, -100, 30));
        assertThat(ref.gt()[0]).isEqualTo(-105.0);
        assertThat(ref.gt()[3]).isEqualTo(32.0);
    }

    @Test
    @DisplayName("ProjectedCSTypeGeoKey 3857 (SatDump's WebMercator output also writes GeographicType 4326): EPSG:3857")
    void webMercator() throws Exception {
        GeoReference ref = parse(GeoTiffFixtures.tiff().pixelScale(1000, 1000).tiepoint(0, 0, -11_000_000, 4_000_000)
                .geoKeys(1024, 1, 3072, 3857, 2048, 4326));
        assertThat(ref.epsg()).isEqualTo(3857);
        assertThat(ref.mercator()).isTrue();
        double[] ll = ref.lonLat(0, 0);
        assertThat(ll[0]).isCloseTo(-98.81468, within(1e-4));
        assertThat(ll[1]).isCloseTo(33.78523, within(1e-4));
    }

    @Test
    @DisplayName("PixelIsPoint: the transform moves half a pixel up and left to the corner")
    void pixelIsPoint() throws Exception {
        GeoReference area = parse(GeoTiffFixtures.wgs84(-110, 42, 0.25).geoKeys(1024, 2, 1025, 1, 2048, 4326));
        GeoReference point = parse(GeoTiffFixtures.wgs84(-110, 42, 0.25).geoKeys(1024, 2, 1025, 2, 2048, 4326));
        assertThat(area.pixelIsPoint()).isFalse();
        assertThat(point.pixelIsPoint()).isTrue();
        assertThat(point.gt()[0]).isEqualTo(-110.125);
        assertThat(point.gt()[3]).isEqualTo(42.125);
        assertThat(area.gt()[0]).isEqualTo(-110.0);
    }

    @Test
    @DisplayName("an axis-aligned ModelTransformation is read; a rotated one is refused (GDAL can help)")
    void transformation() throws Exception {
        GeoReference ref = parse(GeoTiffFixtures.tiff().geoKeys(1024, 2, 2048, 4326)
                .transformation(0.1, 0, 0, -100, 0, -0.1, 0, 35, 0, 0, 0, 0, 0, 0, 0, 1));
        assertThat(ref.gt()).containsExactly(-100, 0.1, 0, 35, 0, -0.1);

        assertThatThrownBy(() -> parse(GeoTiffFixtures.tiff().geoKeys(1024, 2, 2048, 4326)
                .transformation(0.1, 0.02, 0, -100, 0.02, -0.1, 0, 35, 0, 0, 0, 0, 0, 0, 0, 1)))
                .isInstanceOfSatisfying(GeoTiffException.class, e -> {
                    assertThat(e.kind()).isEqualTo(GeoTiffException.Kind.ROTATED);
                    assertThat(e.gdalCanHelp()).isTrue();
                    assertThat(e.getMessage()).contains("rotated");
                });
    }

    @Test
    @DisplayName("another EPSG is refused with the health reason naming it")
    void unsupportedEpsg() {
        assertThatThrownBy(() -> parse(GeoTiffFixtures.wgs84(0, 0, 1).geoKeys(1024, 1, 3072, 32614)))
                .isInstanceOfSatisfying(GeoTiffException.class, e -> {
                    assertThat(e.kind()).isEqualTo(GeoTiffException.Kind.UNSUPPORTED_CRS);
                    assertThat(e.epsg()).isEqualTo(32614);
                    assertThat(e.getMessage())
                            .isEqualTo("GeoTIFF CRS EPSG:32614 not supported (build with WITH_GDAL=true)");
                });
        assertThatThrownBy(() -> parse(GeoTiffFixtures.wgs84(0, 0, 1).geoKeys(1024, 2, 2048, 4269)))
                .hasMessage("GeoTIFF CRS EPSG:4269 not supported (build with WITH_GDAL=true)");
        assertThatThrownBy(() -> parse(GeoTiffFixtures.wgs84(0, 0, 1).geoKeys(1024, 1, 3075, 15)))
                .hasMessage(GeoTiffException.crsReason(GeoKeyParser.USER_DEFINED));
    }

    @Test
    @DisplayName("no GeoKeys, no tie point, or tie points without a scale: no georeference")
    void noGeoreference() {
        assertThatThrownBy(() -> parse(GeoTiffFixtures.wgs84(0, 0, 1).without(34735)))
                .isInstanceOfSatisfying(GeoTiffException.class,
                        e -> assertThat(e.kind()).isEqualTo(GeoTiffException.Kind.NO_GEOREFERENCE));
        assertThatThrownBy(() -> parse(GeoTiffFixtures.wgs84(0, 0, 1).without(33922)))
                .hasMessageContaining("no tie point");
        assertThatThrownBy(() -> parse(GeoTiffFixtures.wgs84(0, 0, 1).without(33550)))
                .hasMessageContaining("without a pixel scale");
        assertThatThrownBy(() -> parse(GeoTiffFixtures.wgs84(0, 0, 1).geoKeys(1024, 2)))
                .hasMessageContaining("declares no geographic or projected CRS");
    }
}
