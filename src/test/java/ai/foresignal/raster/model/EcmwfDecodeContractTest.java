package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The ECMWF decode contract (prompt 105 §1 tests): {@link EcmwfRunIngest}'s band decoding on the 046 self-test
 * fixture {@code ecmwf-ifs-0p25-lsm-0h.grib2} (a real IFS 0.25° message, CCSDS template 5.42). The grid comes
 * from the GRIB header (1440 × 721, lo1 −180 as NetCDF-Java reports it), a product factor scales every value,
 * and a difference product is clamped at zero.
 */
class EcmwfDecodeContractTest {

    static final DecodeRequest LSM = DecodeRequest.grib2("t-lsm", "lsm", 2, 0, 0, null, null, null);

    @TempDir Path dir;

    Grib2Decoder decoder;
    EcmwfRunIngest ingest;

    @BeforeEach
    void setUp() {
        decoder = new Grib2Decoder(DecoderFixtures.safe(dir));
        ingest = new EcmwfRunIngest(decoder, null);
    }

    private static EcmwfDefinition model(EcmwfDefinition.Product product) {
        return new EcmwfDefinition("t", "T", "b", "{yyyyMMdd}/{HH}/{step}", List.of("0"), java.time.Duration.ofHours(12),
                java.time.Duration.ofHours(7), "0p25", List.of(product), product.productId());
    }

    private Path fixture() {
        return DecoderFixtures.selfTest("ecmwf-ifs-0p25-lsm-0h.grib2", dir);
    }

    @Test
    @DisplayName("the header gives the 0.25° global grid; values decode and the factor applies")
    void decodesWithFactor() {
        EcmwfDefinition.Product product = new EcmwfDefinition.Product("t-lsm",
                List.of(new EcmwfDefinition.Component("lsm", "sfc", null, LSM)), true, 100.0, 0, null);
        Path file = fixture();
        DecodedGrid header = decoder.decode(file, LSM.header());
        GridDefinition grid = header.gridDefinition();
        assertThat(grid.nx()).isEqualTo(1440);
        assertThat(grid.ny()).isEqualTo(721);
        DecodedBands bands = ingest.decodeBands(model(product), product, grid, List.of(file), false);
        float max = 0;
        for (float v : bands.physical().get(0)) {
            assertThat(v).isBetween(0f, 100f);
            max = Math.max(max, v);
        }
        assertThat(max).as("a land-sea mask of 1 times the factor 100").isEqualTo(100f);
    }

    @Test
    @DisplayName("a difference product subtracts the reference message and never goes below zero")
    void differenceIsClamped() {
        EcmwfDefinition.Product product = new EcmwfDefinition.Product("t-diff",
                List.of(new EcmwfDefinition.Component("lsm", "sfc", null, LSM)), false, 1.0, 3, s -> s >= 3);
        Path current = fixture();
        Path reference = DecoderFixtures.selfTest("ecmwf-ifs-0p25-lsm-0h.grib2", dir.resolve("ref"));
        GridDefinition grid = decoder.decode(current, LSM.header()).gridDefinition();
        DecodedBands bands = ingest.decodeBands(model(product), product, grid, List.of(current, reference), false);
        for (float v : bands.physical().get(0)) {
            assertThat(v).isEqualTo(0f);
        }
    }
}
