package ai.foresignal.raster.geotiff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.testsupport.raster.GeoTiffFixtures;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link GeoTiffReader} (prompt 054): BigTIFF and oversize files refused from the header alone (no
 * pixel is decoded), the layouts read (8-bit RGB and RGBA, 16-bit grayscale), the alpha of RGB without
 * an alpha channel from the zero mask or {@code GDAL_NODATA}, and the SatDump 1.2.2 producer fixture.
 */
class GeoTiffReaderTest {

    static final String FIXTURE =
            "/fixtures/satdump/1.2.2/pass/2026-10-12_18-02_meteor_m2-x_lrpt_137.9 MHz/MSU-MR/"
                    + "rgb_MSU-MR_221_Composite_projected.tif";

    static byte[] fixture(String resource) {
        try {
            return Files.readAllBytes(Path.of(GeoTiffReaderTest.class.getResource(resource).toURI()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("BigTIFF is refused before anything else, with the health reason")
    void bigTiff() {
        assertThatThrownBy(() -> GeoTiffReader.read(GeoTiffFixtures.bigTiffHeader()))
                .isInstanceOfSatisfying(GeoTiffException.class, e -> {
                    assertThat(e.kind()).isEqualTo(GeoTiffException.Kind.BIGTIFF);
                    assertThat(e.getMessage()).isEqualTo("BigTIFF not supported");
                    assertThat(e.gdalCanHelp()).isTrue();
                });
    }

    @Test
    @DisplayName("over the image guard from the header only: the file has no pixel data, so a decode would fail differently")
    void oversizeFromHeaderOnly() {
        byte[] huge = GeoTiffFixtures.wgs84(-110, 42, 0.01).size(20_000, 20_000).withoutPixels().build();
        assertThatThrownBy(() -> GeoTiffReader.read(huge))
                .isInstanceOfSatisfying(GeoTiffException.class, e -> {
                    assertThat(e.kind()).isEqualTo(GeoTiffException.Kind.OVERSIZE);
                    assertThat(e.getMessage()).contains("megapixel limit");
                    assertThat(e.getCause()).isNull();
                });
        byte[] wide = GeoTiffFixtures.wgs84(-110, 42, 0.01).size(12_001, 10).withoutPixels().build();
        assertThatThrownBy(() -> GeoTiffReader.inspect(wide))
                .isInstanceOfSatisfying(GeoTiffException.class, e -> {
                    assertThat(e.kind()).isEqualTo(GeoTiffException.Kind.OVERSIZE);
                    assertThat(e.getMessage()).contains("12000 px limit");
                });
        // The header of a file within the limits but without pixels inspects fine; only the decode fails.
        byte[] empty = GeoTiffFixtures.wgs84(-110, 42, 0.01).size(100, 100).withoutPixels().build();
        assertThatThrownBy(() -> GeoTiffReader.read(empty))
                .isInstanceOfSatisfying(GeoTiffException.class,
                        e -> assertThat(e.kind()).isEqualTo(GeoTiffException.Kind.MALFORMED));
    }

    @Test
    @DisplayName("an unsupported CRS is refused before decoding")
    void crsBeforeDecode() {
        byte[] utm = GeoTiffFixtures.wgs84(0, 0, 1).geoKeys(1024, 1, 3072, 32614).withoutPixels().build();
        assertThatThrownBy(() -> GeoTiffReader.read(utm))
                .isInstanceOfSatisfying(GeoTiffException.class,
                        e -> assertThat(e.kind()).isEqualTo(GeoTiffException.Kind.UNSUPPORTED_CRS));
    }

    @Test
    @DisplayName("RGB without alpha: transparent where every band is zero")
    void rgbZeroMask() throws Exception {
        byte[] bytes = GeoTiffFixtures.wgs84(-100, 35, 0.5).size(4, 2)
                .pixels((x, y) -> x == 0 ? new int[] {0, 0, 0} : new int[] {x * 10, 0, 0}).build();
        GeoTiffReader.Image image = GeoTiffReader.read(bytes);
        assertThat(image.width()).isEqualTo(4);
        assertThat(image.rgba()[0]).isZero();
        assertThat(image.rgba()[1]).isEqualTo(0x0A0000FF);
        assertThat(image.validPixels()).isEqualTo(6);
    }

    @Test
    @DisplayName("RGB with GDAL_NODATA: transparent where every band equals it, zero is data")
    void rgbNodata() throws Exception {
        byte[] bytes = GeoTiffFixtures.wgs84(-100, 35, 0.5).size(2, 1).nodata("255")
                .pixels((x, y) -> x == 0 ? new int[] {255, 255, 255} : new int[] {0, 0, 0}).build();
        GeoTiffReader.Image image = GeoTiffReader.read(bytes);
        assertThat(image.rgba()[0] & 0xFF).isZero();
        assertThat(image.rgba()[1]).isEqualTo(0x000000FF);
    }

    @Test
    @DisplayName("RGBA keeps its alpha channel")
    void rgba() throws Exception {
        byte[] bytes = GeoTiffFixtures.wgs84(-100, 35, 0.5).size(2, 1).layout(8, 4)
                .pixels((x, y) -> new int[] {1, 2, 3, x == 0 ? 0 : 200}).build();
        GeoTiffReader.Image image = GeoTiffReader.read(bytes);
        assertThat(image.rgba()).containsExactly(0x01020300, 0x010203C8);
    }

    @Test
    @DisplayName("16-bit grayscale is scaled to 8 bits on all three color bands")
    void gray16() throws Exception {
        byte[] bytes = GeoTiffFixtures.wgs84(-100, 35, 0.5).size(3, 1).layout(16, 1)
                .pixels((x, y) -> new int[] {x * 0x4000}).build();
        GeoTiffReader.Header header = GeoTiffReader.inspect(bytes);
        assertThat(header.bitsPerSample()).isEqualTo(16);
        assertThat(header.samplesPerPixel()).isEqualTo(1);
        GeoTiffReader.Image image = GeoTiffReader.read(bytes);
        assertThat(image.rgba()[0]).as("zero is transparent").isZero();
        assertThat(image.rgba()[1]).isEqualTo(0x404040FF);
        assertThat(image.rgba()[2]).isEqualTo(0x808080FF);
    }

    @Test
    @DisplayName("other layouts are refused from the header")
    void layouts() {
        assertThatThrownBy(() -> GeoTiffReader.inspect(GeoTiffFixtures.wgs84(0, 0, 1).shorts(339, 3, 3, 3).build()))
                .isInstanceOf(GeoTiffException.class);
        assertThatThrownBy(() -> GeoTiffReader.read(new byte[] {'X', 'Y', 0, 0, 0, 0, 0, 0}))
                .isInstanceOfSatisfying(GeoTiffException.class,
                        e -> assertThat(e.kind()).isEqualTo(GeoTiffException.Kind.MALFORMED));
    }

    @Test
    @DisplayName("the SatDump 1.2.2 producer fixture: 120 × 160 EPSG:4326, a swath band and zero fill")
    void producerFixture() throws Exception {
        GeoTiffReader.Image image = GeoTiffReader.read(fixture(FIXTURE));
        assertThat(image.width()).isEqualTo(120);
        assertThat(image.height()).isEqualTo(160);
        assertThat(image.georeference().crs()).isEqualTo("EPSG:4326");
        assertThat(image.georeference().gt()).containsExactly(-110, 0.25, 0, 42, 0, -0.25);
        assertThat(image.rgba()[0]).isZero();
        assertThat(image.rgba()[36] & 0xFF).isEqualTo(255);
        assertThat(image.validPixels()).isBetween(6000L, 7000L);
    }
}
