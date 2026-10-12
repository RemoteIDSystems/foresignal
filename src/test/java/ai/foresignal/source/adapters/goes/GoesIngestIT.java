package ai.foresignal.source.adapters.goes;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.FetchResult;
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
 * {@code goes-east-abi} end to end (prompt 046): the band 13 file (from {@code ./gradlew
 * downloadTestFixtures}; skipped with a logged reason without it), served by a stubbed S3, becomes a
 * {@code raster_frame} row on a {@code geos} grid, a {@code .fsg} file and a {@code raster-frame}
 * record, and its z = 6 tile over the central US renders. Runs in 042's shared raster context.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class GoesIngestIT extends ProductIngestSupport {

    static final Instant VALID = Instant.parse("2026-10-12T12:01:17Z");
    static final String KEY = "ABI-L2-CMIPC/2026/285/12/" + DecoderFixtures.GOES_C13;

    @Autowired NetcdfDecoder decoder;

    @BeforeEach
    void clean() {
        clock.setInstant(Instant.parse("2026-10-12T12:06:00Z"));
        cleanFrames();
    }

    @AfterEach
    void cleanFrames() {
        RasterFixtures.deleteTree(paths.productFrames(GoesAbiAdapter.IR));
    }

    @Test
    @DisplayName("fixture -> geos frame row, .fsg file and raster-frame record; the z=6 tile renders")
    void bandThirteenToTile() throws Exception {
        byte[] body = DecoderFixtures.goesBytesOrSkip();
        stubAnyListing(GoesAbiAdapter.BUCKET);
        stubListing(GoesAbiAdapter.BUCKET, "ABI-L2-CMIPC/2026/285/12/OR_ABI-L2-CMIPC-M", Map.of(KEY, (long) body.length));
        stubObject(GoesAbiAdapter.BUCKET, KEY, body);
        GoesAbiAdapter adapter = new GoesAbiAdapter(decoder, S3.baseUrl());

        FetchResult result = adapter.fetch(context(adapter.descriptor()));

        assertThat(result.records()).singleElement().satisfies(record -> {
            assertThat(record.kind()).isEqualTo(ProductSpec.RECORD_KIND);
            assertThat(record.externalId()).isEqualTo(GoesAbiAdapter.IR);
            assertThat(record.observedAt()).isEqualTo(VALID);
        });
        List<RasterFrameRow> rows = frames.atValidTime(GoesAbiAdapter.IR, VALID);
        assertThat(rows).hasSize(1);
        RasterFrameRow row = rows.get(0);
        assertThat(row.frameId()).isEqualTo("20261012T120117Z");
        assertThat(row.obsStart()).isEqualTo(VALID);
        assertThat(row.obsEnd()).isEqualTo(Instant.parse("2026-10-12T12:03:55.400Z"));
        assertThat(row.dtype()).isEqualTo("u16");
        assertThat(((Number) row.stats().get("min")).doubleValue()).isBetween(190.0, 191.0);
        assertThat(((Number) row.stats().get("max")).doubleValue()).isBetween(298.0, 299.0);
        assertThat(Files.isRegularFile(paths.resolve(row.path()))).isTrue();

        int[] xy = tile(6, -95.0, 35.0);
        HttpResponse<byte[]> tile = getBytes("/v1/products/" + GoesAbiAdapter.IR + "/tiles/" + row.frameKey() + "/6/"
                + xy[0] + "/" + xy[1] + ".png?style=ir-gray@1");
        assertThat(tile.statusCode()).isEqualTo(200);
        BufferedImage image = image(tile.body());
        int[] p = pixel(6, xy[0], xy[1], 256, -95.0, 35.0);
        assertThat(image.getRGB(p[0], p[1]) >>> 24).as("inside the CONUS scan").isEqualTo(0xFF);
    }
}
