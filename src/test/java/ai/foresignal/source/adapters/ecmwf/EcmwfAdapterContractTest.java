package ai.foresignal.source.adapters.ecmwf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.model.EcmwfDefinition;
import ai.foresignal.raster.model.EcmwfIndex;
import ai.foresignal.raster.model.EcmwfRunIngest;
import ai.foresignal.raster.model.ModelCycleSelector;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.GridStats;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The production ECMWF IFS model ({@link EcmwfAdapter#MODEL}) against the 2026-10-17 00z f024 step
 * captured from ECMWF open data (checkpoint 124; {@code src/test/resources/fixtures/ecmwf-ifs/README.md}):
 * the inventory key the engine requests, every product's component found in the real JSON
 * {@code .index}, and two CCSDS-packed messages decoded by {@link Grib2Decoder} with the adapter's own
 * decode requests.
 */
class EcmwfAdapterContractTest {

    static final Instant RUN = Instant.parse("2026-10-17T00:00:00Z");
    static final String DIR = "fixtures/ecmwf-ifs/";
    static final String INDEX = DIR + "20261017000000-24h-oper-fc.index";

    @TempDir Path dir;

    static DecodeRequest request(EcmwfDefinition model, String productId) {
        return model.products().stream().filter(p -> p.productId().equals(productId)).findFirst().orElseThrow()
                .components().get(0).request();
    }

    static Path copy(String resource, Path dir) throws Exception {
        Path file = dir.resolve(Path.of(resource).getFileName().toString());
        Files.write(file, DecoderFixtures.bytes(resource));
        return file;
    }

    @Test
    @DisplayName("the engine asks for the published inventory name, …-fc.index, not …-fc.grib2.index")
    void indexKey() {
        String key = ModelCycleSelector.objectKey(EcmwfAdapter.DEFAULT_TEMPLATE.replace("{res}", "0p25"), RUN, 24);
        assertThat(key).isEqualTo("20261017/00z/ifs/0p25/oper/20261017000000-24h-oper-fc.grib2");
        assertThat(EcmwfRunIngest.indexKey(key)).isEqualTo("20261017/00z/ifs/0p25/oper/20261017000000-24h-oper-fc.index");
    }

    @Test
    @DisplayName("the captured .index lists every component of every IFS product at sfc")
    void index() {
        EcmwfIndex index = EcmwfIndex.parse(new String(DecoderFixtures.bytes(INDEX), StandardCharsets.UTF_8));
        assertThat(index.all()).hasSize(184);
        for (EcmwfDefinition.Product product : EcmwfAdapter.MODEL.products()) {
            for (EcmwfDefinition.Component c : product.components()) {
                assertThat(index.find(c.param(), c.levtype(), c.levelist()))
                        .as(product.productId() + " " + c.param()).isPresent();
            }
        }
        EcmwfIndex.Entry msl = index.find("msl", "sfc", null).orElseThrow();
        assertThat(msl.offset()).isEqualTo(96_826_485L);
        assertThat(msl.length()).isEqualTo(521_465L);
        EcmwfIndex.Entry tp = index.find("tp", "sfc", null).orElseThrow();
        assertThat(tp.offset()).isZero();
        assertThat(tp.length()).isEqualTo(817_046L);
        assertThat(index.find("10fg", "sfc", null)).as("10fg at f024").isPresent();
    }

    @Test
    @DisplayName("msl decodes with the ifs-mslp request: 1440 x 721, Pa")
    void msl() throws Exception {
        Grib2Decoder decoder = new Grib2Decoder(DecoderFixtures.safe(dir));
        DecodedGrid grid = decoder.decode(copy(DIR + "ifs-24h-msl.grib2", dir), request(EcmwfAdapter.MODEL,
                EcmwfAdapter.MSLP));
        assertThat(grid.nx()).isEqualTo(1440);
        assertThat(grid.ny()).isEqualTo(721);
        assertThat(grid.gridDefinition().params()).containsEntry("dx", 0.25).containsEntry("la1", 90.0)
                .containsEntry("lo1", -180.0);
        assertThat(grid.metadata()).containsEntry("dataTemplate", 42).containsEntry("center", 98);
        assertThat(grid.referenceTime()).isEqualTo(RUN);
        assertThat(grid.validTime()).isEqualTo(Instant.parse("2026-10-18T00:00:00Z"));
        GridStats stats = GridStats.of(grid.floatData());
        assertThat(stats.nan()).isZero();
        assertThat(stats.min()).isCloseTo(94_572.375, within(0.01));
        assertThat(stats.max()).isCloseTo(103_928.375, within(0.01));
    }

    @Test
    @DisplayName("tp decodes with the ifs-precip request (discipline 0, category 1, number 193): meters since the run start")
    void tp() throws Exception {
        Grib2Decoder decoder = new Grib2Decoder(DecoderFixtures.safe(dir));
        DecodedGrid grid = decoder.decode(copy(DIR + "ifs-24h-tp.grib2", dir), request(EcmwfAdapter.MODEL,
                EcmwfAdapter.PRECIP_3H));
        assertThat(grid.nx()).isEqualTo(1440);
        assertThat(grid.ny()).isEqualTo(721);
        assertThat(grid.metadata()).containsEntry("number", 193).containsEntry("dataTemplate", 42);
        GridStats stats = GridStats.of(grid.floatData());
        assertThat(stats.nan()).isZero();
        assertThat(stats.min()).isZero();
        assertThat(stats.max()).as("0.291 m in the first 24 h").isCloseTo(0.29110, within(1e-4));
        double factor = EcmwfAdapter.MODEL.products().stream().filter(p -> p.productId().equals(EcmwfAdapter.PRECIP_3H))
                .findFirst().orElseThrow().factor();
        assertThat(stats.max() * factor).as("mm").isCloseTo(291.1, within(0.1));
    }
}
