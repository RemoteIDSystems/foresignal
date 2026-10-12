package ai.foresignal.source.adapters.mrms;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.ProductIngestSupport;
import ai.foresignal.testsupport.raster.RasterFixtures;
import ai.foresignal.testsupport.raster.RasterTestSources;
import java.awt.image.BufferedImage;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * {@code mrms} end to end (prompt 046): the committed reflectivity fixture, served by a stubbed S3,
 * becomes a {@code raster_frame} row, a {@code .fsg} file and a {@code raster-frame} record, and its
 * tile renders at z = 6 with echoes drawn and no-coverage cells transparent. Runs in 042's shared
 * raster context.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class MrmsIngestIT extends ProductIngestSupport {

    static final Instant VALID = Instant.parse("2026-10-12T12:00:37Z");

    /** A 0.5° block of the fixture where 2,125 of 2,500 cells are at least 30 dBZ. */
    static final double ECHO_LON = -67.245;
    static final double ECHO_LAT = 49.245;

    @Autowired Grib2Decoder decoder;

    @BeforeEach
    void clean() {
        clock.setInstant(Instant.parse("2026-10-12T12:05:00Z"));
        cleanFrames();
    }

    @AfterEach
    void cleanFrames() {
        for (String product : List.of(MrmsAdapter.REFLECTIVITY, MrmsAdapter.PRECIP_RATE, MrmsAdapter.MESH)) {
            RasterFixtures.deleteTree(paths.productFrames(product));
        }
    }

    @Test
    @DisplayName("fixture -> frame row, .fsg file and raster-frame record; the z=6 tile draws echoes")
    void reflectivityToTile() throws Exception {
        byte[] body = DecoderFixtures.mrmsBytes(DecoderFixtures.MRMS_REFLECTIVITY);
        String key = MrmsAdapter.REFLECTIVITY_UPSTREAM.key(VALID);
        stubAnyListing(MrmsAdapter.BUCKET);
        stubListing(MrmsAdapter.BUCKET, "CONUS/MergedReflectivityQCComposite_00.50/20261012/",
                Map.of(key, (long) body.length));
        stubObject(MrmsAdapter.BUCKET, key, body);
        MrmsAdapter adapter = new MrmsAdapter(decoder, S3.baseUrl());

        FetchResult result = adapter.fetch(context(adapter.descriptor()));

        assertThat(result.records()).singleElement().satisfies(record -> {
            assertThat(record.kind()).isEqualTo(ProductSpec.RECORD_KIND);
            assertThat(record.externalId()).isEqualTo(MrmsAdapter.REFLECTIVITY);
            assertThat(record.observedAt()).isEqualTo(VALID);
        });
        NormalizedRecord record = result.records().get(0);
        assertThat(record.payload()).containsEntry("productId", MrmsAdapter.REFLECTIVITY);

        List<RasterFrameRow> rows = frames.atValidTime(MrmsAdapter.REFLECTIVITY, VALID);
        assertThat(rows).hasSize(1);
        RasterFrameRow row = rows.get(0);
        assertThat(row.frameId()).isEqualTo("20261012T120037Z");
        assertThat(row.complete()).isTrue();
        assertThat(row.upstreamKey()).isEqualTo(key);
        assertThat(row.dtype()).isEqualTo("u8");
        @SuppressWarnings("unchecked")
        Map<String, Object> codeCounts = (Map<String, Object>) row.stats().get("codeCounts");
        assertThat(((Number) codeCounts.get("0")).longValue()).as("no coverage stays no coverage").isEqualTo(8_174_561L);
        assertThat(((Number) codeCounts.get("1")).longValue()).isGreaterThanOrEqualTo(14_293_209L);
        assertThat(Files.isRegularFile(paths.resolve(row.path()))).isTrue();

        int[] xy = tile(6, ECHO_LON, ECHO_LAT);
        HttpResponse<byte[]> tile = getBytes("/v1/products/" + MrmsAdapter.REFLECTIVITY + "/tiles/" + row.frameKey() + "/6/"
                + xy[0] + "/" + xy[1] + ".png?style=nws-reflectivity@1");
        assertThat(tile.statusCode()).isEqualTo(200);
        BufferedImage image = image(tile.body());
        int[] echo = pixel(6, xy[0], xy[1], 256, ECHO_LON, ECHO_LAT);
        assertThat(image.getRGB(echo[0], echo[1]) >>> 24).as("a strong echo is drawn").isEqualTo(0xFF);
        int opaque = 0;
        for (int py = 0; py < image.getHeight(); py++) {
            for (int px = 0; px < image.getWidth(); px++) {
                if ((image.getRGB(px, py) >>> 24) != 0) {
                    opaque++;
                }
            }
        }
        assertThat(opaque).as("the tile is not empty").isPositive();

        // The Gulf of Alaska is outside every radar: no coverage renders transparent, never as "no echo".
        int[] far = tile(6, -140.0, 50.0);
        HttpResponse<byte[]> outside = getBytes("/v1/products/" + MrmsAdapter.REFLECTIVITY + "/tiles/" + row.frameKey()
                + "/6/" + far[0] + "/" + far[1] + ".png?style=nws-reflectivity@1");
        assertThat(outside.statusCode()).isIn(200, 204);
        if (outside.statusCode() == 200) {
            BufferedImage empty = image(outside.body());
            int[] p = pixel(6, far[0], far[1], 256, -140.0, 50.0);
            assertThat(empty.getRGB(p[0], p[1]) >>> 24).isZero();
        }
    }
}
