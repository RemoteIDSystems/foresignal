package ai.foresignal.source.adapters.sst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.FixtureHttp;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Envelope;

/**
 * The production {@link SstAdapter} against the CoastWatch ERDDAP subset captured 2026-10-17
 * (checkpoint 124; {@code src/test/resources/fixtures/sst-geopolar/README.md}): the griddap URL it
 * builds for a bbox (coordinate bounds in parentheses), the NetCDF read through {@link NetcdfDecoder},
 * the analysis time, the row flip to north-to-south, the u16 coding (°C + 2) × 100 and 65535 over land.
 *
 * <p>The expected samples were checked against the same subset requested as {@code .csv} from ERDDAP.
 */
class SstAdapterContractTest {

    static final String FIXTURE = "fixtures/sst-geopolar/noaacwBLENDEDsstDNDaily-last-24.5N-27N-83W-80W.nc";
    static final String URL = "https://coastwatch.noaa.gov/erddap/griddap/noaacwBLENDEDsstDNDaily.nc"
            + "?analysed_sst[(last)][(24.5):1:(27.0)][(-83.0):1:(-80.0)]";
    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");
    static final int NX = 61;

    @TempDir Path dir;

    @Test
    @DisplayName("the subset URL puts the bbox bounds in parentheses (coordinates, not indices)")
    void subsetUrl() {
        SstAdapter adapter = new SstAdapter(null, "", "large", null);
        assertThat(adapter.subsetUrl(new double[] {-83.0, 24.5, -80.0, 27.0})).isEqualTo(URL);
    }

    @Test
    @DisplayName("the captured subset: 61 x 51 at 0.05°, 2026-10-15T12:00Z, 1115 land cells, 28.39-29.85 °C")
    void capturedSubset() throws Exception {
        Path downloads = java.nio.file.Files.createDirectories(dir.resolve("downloads"));
        FixtureHttp http = new FixtureHttp(downloads).serve(URL, DecoderFixtures.bytes(FIXTURE));
        SstAdapter adapter = new SstAdapter(new NetcdfDecoder(DecoderFixtures.safe(downloads)), "", "large", null);
        ProductAdapterHarness.MapState state = new ProductAdapterHarness.MapState();
        ProductAdapterHarness.RecordingSink sink = new ProductAdapterHarness.RecordingSink();
        ValidatedParams params = ValidatedParams.of(Map.of(SstAdapter.BBOX_PARAM, new Envelope(-83.0, -80.0, 24.5, 27.0)));

        FetchResult result = adapter.fetch(new ProductAdapterHarness.ParamContext(adapter.descriptor(), http,
                Clock.fixed(NOW, ZoneOffset.UTC), state, sink, params));

        assertThat(http.requests()).containsExactly("DOWNLOAD " + URL);
        assertThat(result.records()).hasSize(1);
        ProductAdapterHarness.Committed frame = sink.committed(SstAdapter.PRODUCT).get(0);
        assertThat(frame.frame().validTime()).isEqualTo(Instant.parse("2026-10-15T12:00:00Z"));
        assertThat(state.asMap()).containsEntry("lastTime", "2026-10-15T12:00:00Z");

        GridDefinition grid = frame.frame().grid();
        assertThat(grid.type()).isEqualTo(GridDefinition.LATLON);
        assertThat(grid.nx()).isEqualTo(NX);
        assertThat(grid.ny()).isEqualTo(51);
        assertThat((Double) grid.params().get("dx")).isCloseTo(0.05, within(1e-5));
        assertThat((Double) grid.params().get("la1")).isCloseTo(27.025, within(1e-4));
        assertThat((Double) grid.params().get("lo1")).isCloseTo(360 - 82.975, within(1e-4));
        assertThat(grid.west()).isCloseTo(-83.0, within(1e-4));
        assertThat(grid.east()).isCloseTo(-79.95, within(1e-4));
        assertThat(grid.north()).isCloseTo(27.05, within(1e-4));
        assertThat(grid.south()).isCloseTo(24.5, within(1e-4));

        int[] codes = frame.bands().codes().get(0);
        assertThat(codes).hasSize(NX * 51);
        assertThat(Arrays.stream(codes).filter(c -> c == 65535).count()).isEqualTo(1115);
        assertThat(Arrays.stream(codes).filter(c -> c != 65535).min().orElseThrow()).isEqualTo(3039);
        assertThat(Arrays.stream(codes).filter(c -> c != 65535).max().orElseThrow()).isEqualTo(3185);
        // Rows run north to south after the flip: row 0 is 27.025° N, row 50 is 24.525° N.
        assertThat(codes[0]).as("27.025 N 82.975 W, 29.229994 C").isEqualTo(3123);
        assertThat(codes[20 * NX + 39]).as("26.025 N 81.025 W, the Everglades").isEqualTo(65535);
        assertThat(codes[30 * NX + 58]).as("25.525 N 80.075 W, 29.669994 C").isEqualTo(3167);
        assertThat(codes[50 * NX + 27]).as("24.525 N 81.625 W, the maximum 29.849995 C").isEqualTo(3185);
        assertThat(codes[2 * NX + 17]).as("26.925 N 82.125 W, the minimum 28.389994 C").isEqualTo(3039);
    }
}
