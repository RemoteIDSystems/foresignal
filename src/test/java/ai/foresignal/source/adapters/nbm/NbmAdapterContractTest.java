package ai.foresignal.source.adapters.nbm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.model.GribIdx;
import ai.foresignal.raster.model.ModelCycleSelector;
import ai.foresignal.raster.model.ModelDefinition;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.GridStats;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The production NBM model ({@link NbmAdapter#MODEL}) against the core files captured 2026-10-17
 * (checkpoint 124; {@code src/test/resources/fixtures/nbm/README.md}): the object key of the 01z f012
 * step, each product's {@code .idx} selector on the real 01z and 12z inventories, and the f012
 * {@code GUST:10 m above ground} message decoded by {@link Grib2Decoder} with the adapter's request.
 */
class NbmAdapterContractTest {

    static final String DIR = "fixtures/nbm/";
    static final Instant RUN = Instant.parse("2026-10-17T01:00:00Z");

    @TempDir Path dir;

    static GribIdx idx(String name) {
        return GribIdx.parse(new String(DecoderFixtures.bytes(DIR + name), StandardCharsets.US_ASCII));
    }

    static ModelDefinition.Component component(String productId) {
        return NbmAdapter.MODEL.products().stream().filter(p -> p.productId().equals(productId)).findFirst()
                .orElseThrow().components().get(0);
    }

    static Optional<GribIdx.Message> find(GribIdx idx, String productId, int step) {
        return idx.find(component(productId).selector(step));
    }

    @Test
    @DisplayName("the 01z f012 object key is the captured one")
    void objectKey() {
        assertThat(ModelCycleSelector.objectKey(NbmAdapter.DEFAULT_TEMPLATE, RUN, 12))
                .isEqualTo("blend.20261017/01/core/blend.t01z.core.f012.co.grib2");
    }

    @Test
    @DisplayName("01z f012: TMP, WIND, GUST and TCDC:surface are found; the file has no 6-hour APCP bucket")
    void selectors01z() {
        GribIdx idx = idx("blend.t01z.core.f012.co.grib2.idx");
        assertThat(idx.all()).hasSize(156);
        assertThat(find(idx, NbmAdapter.T2M, 12).map(GribIdx.Message::number)).contains(133);
        assertThat(find(idx, NbmAdapter.WIND, 12).map(GribIdx.Message::number)).contains(154);
        assertThat(find(idx, NbmAdapter.SKY, 12).map(GribIdx.Message::number)).contains(70);
        GribIdx.Message gust = find(idx, NbmAdapter.GUST, 12).orElseThrow();
        assertThat(gust.number()).isEqualTo(150);
        assertThat(gust.offset()).isEqualTo(153_176_082L);
        assertThat(idx.next(gust).orElseThrow().offset()).isEqualTo(154_950_552L);
        // The 01z run lists only the 1-hour "11-12 hour acc" buckets at f012 (see the README).
        assertThat(find(idx, NbmAdapter.APCP6, 12)).isEmpty();
        assertThat(find(idx, NbmAdapter.POP6, 12)).isEmpty();
    }

    @Test
    @DisplayName("12z f012 (2026-10-16): the 6-12 hour APCP bucket and its probability are found")
    void selectors12z() {
        GribIdx idx = idx("blend.20261016.t12z.core.f012.co.grib2.idx");
        assertThat(find(idx, NbmAdapter.APCP6, 12).map(GribIdx.Message::number)).contains(100);
        assertThat(find(idx, NbmAdapter.POP6, 12).map(GribIdx.Message::number)).contains(78);
        assertThat(find(idx, NbmAdapter.SKY, 12).map(GribIdx.Message::number)).contains(102);
    }

    @Test
    @DisplayName("the f012 GUST message decodes with the nbm-gust request: 2345 x 1597 Lambert, m/s")
    void gust() throws Exception {
        Path file = dir.resolve("nbm-t01z-f012-gust.grib2");
        Files.write(file, DecoderFixtures.bytes(DIR + "nbm-t01z-f012-gust.grib2"));
        DecodeRequest request = component(NbmAdapter.GUST).request();
        DecodedGrid grid = new Grib2Decoder(DecoderFixtures.safe(dir)).decode(file, request);
        assertThat(grid.nx()).isEqualTo(2345);
        assertThat(grid.ny()).isEqualTo(1597);
        assertThat(grid.gridDefinition().type()).isEqualTo("lcc");
        assertThat(grid.gridDefinition().params()).containsEntry("dx", 2539.703).containsEntry("lov", -95.0)
                .containsEntry("lat1", 25.0);
        assertThat(grid.metadata()).containsEntry("dataTemplate", 3).containsEntry("gridTemplate", 30)
                .containsEntry("scanMode", 80);
        assertThat(grid.referenceTime()).isEqualTo(RUN);
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-17T13:00:00Z"));
        GridStats stats = GridStats.of(grid.floatData());
        assertThat(stats.nan()).isZero();
        assertThat(stats.min()).isZero();
        assertThat(stats.max()).isCloseTo(36.5, within(1e-3));
    }
}
