package ai.foresignal.source.adapters.ecmwf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.model.EcmwfDefinition;
import ai.foresignal.raster.model.EcmwfIndex;
import ai.foresignal.raster.model.EcmwfRunIngest;
import ai.foresignal.raster.model.ModelCycleSelector;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.GridStats;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The production AIFS Single model ({@link AifsAdapter#MODEL}) against the 2026-10-17 00z f024 step
 * captured from ECMWF open data (checkpoint 124; {@code src/test/resources/fixtures/aifs/README.md}):
 * the inventory key, every product's component in the real JSON {@code .index}, and two CCSDS-packed
 * messages decoded by {@link Grib2Decoder} with the adapter's own decode requests.
 */
class AifsAdapterContractTest {

    static final Instant RUN = Instant.parse("2026-10-17T00:00:00Z");
    static final String DIR = "fixtures/aifs/";
    static final String INDEX = DIR + "20261017000000-24h-oper-fc.index";

    @TempDir Path dir;

    @Test
    @DisplayName("the engine asks for the published inventory name, …-fc.index")
    void indexKey() {
        String key = ModelCycleSelector.objectKey(AifsAdapter.DEFAULT_TEMPLATE.replace("{res}", "0p25"), RUN, 24);
        assertThat(EcmwfRunIngest.indexKey(key))
                .isEqualTo("20261017/00z/aifs-single/0p25/oper/20261017000000-24h-oper-fc.index");
    }

    @Test
    @DisplayName("the captured .index lists every component of every AIFS product at sfc")
    void index() {
        EcmwfIndex index = EcmwfIndex.parse(new String(DecoderFixtures.bytes(INDEX), StandardCharsets.UTF_8));
        assertThat(index.all()).hasSize(122);
        for (EcmwfDefinition.Product product : AifsAdapter.MODEL.products()) {
            for (EcmwfDefinition.Component c : product.components()) {
                assertThat(index.find(c.param(), c.levtype(), c.levelist()))
                        .as(product.productId() + " " + c.param()).isPresent();
            }
        }
        EcmwfIndex.Entry t2m = index.find("2t", "sfc", null).orElseThrow();
        assertThat(t2m.offset()).isEqualTo(82_722_776L);
        assertThat(t2m.length()).isEqualTo(548_565L);
        EcmwfIndex.Entry tp = index.find("tp", "sfc", null).orElseThrow();
        assertThat(tp.offset()).isEqualTo(14_782_771L);
        assertThat(tp.length()).isEqualTo(787_124L);
    }

    @Test
    @DisplayName("2t decodes with the aifs-t2m request: 1440 x 721, K")
    void t2m() throws Exception {
        Grib2Decoder decoder = new Grib2Decoder(DecoderFixtures.safe(dir));
        DecodedGrid grid = decoder.decode(EcmwfAdapterContractTest.copy(DIR + "aifs-24h-2t.grib2", dir),
                EcmwfAdapterContractTest.request(AifsAdapter.MODEL, AifsAdapter.T2M));
        assertThat(grid.nx()).isEqualTo(1440);
        assertThat(grid.ny()).isEqualTo(721);
        assertThat(grid.metadata()).containsEntry("levelType", 103).containsEntry("levelValue", 2.0)
                .containsEntry("dataTemplate", 42);
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-18T00:00:00Z"));
        GridStats stats = GridStats.of(grid.floatData());
        assertThat(stats.nan()).isZero();
        assertThat(stats.min()).isCloseTo(212.2078, within(1e-3));
        assertThat(stats.max()).isCloseTo(308.8016, within(1e-3));
    }

    @Test
    @DisplayName("tp decodes with the aifs-tp request (discipline 0, category 1, number 52) in kg m-2, i.e. mm")
    void tp() throws Exception {
        Grib2Decoder decoder = new Grib2Decoder(DecoderFixtures.safe(dir));
        DecodedGrid grid = decoder.decode(EcmwfAdapterContractTest.copy(DIR + "aifs-24h-tp.grib2", dir),
                EcmwfAdapterContractTest.request(AifsAdapter.MODEL, AifsAdapter.TP));
        assertThat(grid.nx()).isEqualTo(1440);
        assertThat(grid.ny()).isEqualTo(721);
        assertThat(grid.metadata()).containsEntry("number", 52).containsEntry("dataTemplate", 42);
        GridStats stats = GridStats.of(grid.floatData());
        assertThat(stats.nan()).isZero();
        assertThat(stats.min()).isZero();
        // kg m-2, already mm: the IFS tp of the same run peaks at 0.291 m (EcmwfAdapterContractTest).
        assertThat(stats.max()).isCloseTo(205.0195, within(1e-3));
        double factor = AifsAdapter.MODEL.products().stream().filter(p -> p.productId().equals(AifsAdapter.TP))
                .findFirst().orElseThrow().factor();
        assertThat(stats.max() * factor).as("mm, inside the i16 range of the 0.1 mm product").isCloseTo(205.0, within(0.1));
    }
}
