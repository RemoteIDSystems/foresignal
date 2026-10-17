package ai.foresignal.source.adapters.heatrisk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.geotiff.GeoTiffValues;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.FixtureHttp;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The production {@link HeatRiskAdapter} against the WPC data page and the day-1 Mercator GeoTIFF
 * captured 2026-10-17 (checkpoint 124; {@code src/test/resources/fixtures/wpc-heatrisk/README.md}): the
 * page's seven links, the palette indices read by {@link GeoTiffValues}, the category mapping (index 5,
 * the file's nodata, becomes 255) and the file's own EPSG:3857 affine grid.
 */
class HeatRiskAdapterContractTest {

    static final String PAGE = "fixtures/wpc-heatrisk/data.html";
    static final String DAY1 = "fixtures/wpc-heatrisk/HeatRisk_1_Mercator.tif";
    static final String DAY1_URL = "https://www.wpc.ncep.noaa.gov/heatrisk/data/HeatRisk_1_Mercator.tif";
    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    @TempDir Path dir;

    @Test
    @DisplayName("the captured data page links days 1-7")
    void links() {
        Map<Integer, String> links = HeatRiskAdapter.links(new String(DecoderFixtures.bytes(PAGE),
                java.nio.charset.StandardCharsets.UTF_8));
        assertThat(links).hasSize(7).containsEntry(1, DAY1_URL)
                .containsEntry(7, "https://www.wpc.ncep.noaa.gov/heatrisk/data/HeatRisk_7_Mercator.tif");
    }

    @Test
    @DisplayName("the captured day-1 GeoTIFF: 2866 x 1672 palette indices 0-3 and nodata 5, EPSG:3857")
    void geoTiff() throws Exception {
        GeoTiffValues.Values values = GeoTiffValues.read(DecoderFixtures.bytes(DAY1));
        assertThat(values.width()).isEqualTo(2866);
        assertThat(values.height()).isEqualTo(1672);
        assertThat(values.georeference().crs()).isEqualTo("EPSG:3857");
        assertThat(values.nodata()).isEqualTo(5.0);
        assertThat(histogram(values.samples())).containsExactlyEntriesOf(new TreeMap<>(Map.of(
                0, 1_819_633, 1, 227_771, 2, 6_567, 3, 798, 5, 2_737_183)));
    }

    @Test
    @DisplayName("a fetch commits day 1 as categories 0-3 plus 255 on the file's affine grid")
    void fetch() throws Exception {
        FixtureHttp http = new FixtureHttp(dir)
                .serve(HeatRiskAdapter.PAGE_URL, DecoderFixtures.bytes(PAGE))
                .serve(DAY1_URL, DecoderFixtures.bytes(DAY1));
        HeatRiskAdapter adapter = new HeatRiskAdapter("", "small");
        ProductAdapterHarness.RecordingSink sink = new ProductAdapterHarness.RecordingSink();

        FetchResult result = adapter.fetch(new ProductAdapterHarness.Context(adapter.descriptor(), http,
                Clock.fixed(NOW, ZoneOffset.UTC), new ProductAdapterHarness.MapState(), sink));

        // Days 2-7 were not captured: they answer 404 and are skipped with a warning.
        assertThat(http.requests()).contains("GET " + HeatRiskAdapter.PAGE_URL, "DOWNLOAD " + DAY1_URL)
                .hasSize(8);
        assertThat(sink.formatChanges()).isEmpty();
        assertThat(result.records()).hasSize(1);
        ProductAdapterHarness.Committed day1 = sink.committed("heatrisk-day1").get(0);
        assertThat(day1.frame().validTime()).isEqualTo(Instant.parse("2026-10-17T00:00:00Z"));
        GridDefinition grid = day1.frame().grid();
        assertThat(grid.type()).isEqualTo(GridDefinition.AFFINE);
        assertThat(grid.nx()).isEqualTo(2866);
        assertThat(grid.ny()).isEqualTo(1672);
        assertThat(grid.params()).containsEntry("crs", "EPSG:3857");
        assertThat((Double) grid.params().get("gt1")).isCloseTo(2539.703, within(1e-6));
        assertThat(grid.west()).isCloseTo(-128.693, within(0.01));
        assertThat(grid.east()).isCloseTo(-63.306, within(0.01));
        assertThat(grid.north()).isCloseTo(51.567, within(0.01));
        assertThat(grid.south()).isCloseTo(21.702, within(0.01));
        assertThat(histogram(day1.bands().codes().get(0))).containsExactlyEntriesOf(new TreeMap<>(Map.of(
                0, 1_819_633, 1, 227_771, 2, 6_567, 3, 798, HeatRiskAdapter.NO_DATA, 2_737_183)));
    }

    private static Map<Integer, Integer> histogram(int[] samples) {
        Map<Integer, Integer> out = new TreeMap<>();
        for (int s : samples) {
            out.merge(s, 1, Integer::sum);
        }
        return out;
    }
}
