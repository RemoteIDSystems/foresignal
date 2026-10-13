package ai.foresignal.raster.sample;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.GridDef;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.testsupport.raster.SampleFixtures;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The place-weather performance budget (prompt 052; Weather-Imagery-Design §7.2): with each place's cells
 * within 100 km precomputed and sorted by distance, one pass over an MRMS-resolution frame for 50 places stays
 * under 5 ms. The assertion allows a generous CI margin (×4) over that budget; the best of several warmed-up
 * runs is measured, so a slow runner or a garbage collection does not fail it.
 */
class PlaceGridIndexBenchmarkTest {

    /** The design budget. */
    static final double BUDGET_MS = 5.0;

    /** The CI margin over the budget. */
    static final double CI_MARGIN = 4.0;

    @Test
    @DisplayName("50 places scan one 0.01° frame in under 5 ms (× the CI margin)")
    void fiftyPlacesUnderBudget() {
        // A 1,400 × 1,000 cell window of MRMS's 0.01° grid around Texas (MRMS itself is 7,000 × 3,500; the scan only
        // touches each place's 100 km cells, so the window size does not change the cost).
        GridDefinition definition = GridDefinition.latLon(1400, 1000, -106.0, 26.0, -92.0, 36.0);
        GridDef grid = GridDef.of(definition);
        int[] codes = new int[1400 * 1000];
        java.util.Arrays.fill(codes, 1);
        FrameView view = SampleFixtures.codes(SampleFixtures.reflectivity("mrms-reflectivity"), definition, codes, null);
        List<PlaceGridIndex.Entry> entries = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            double lon = -104.0 + (i % 10) * 1.0;
            double lat = 28.0 + (i / 10) * 1.5;
            entries.add(PlaceGridIndex.build(grid, lon, lat, PlaceGridIndex.RANGE_KM));
        }
        assertThat(entries.get(0).size()).as("about π·100² cells of ~1.07 km²").isBetween(25_000, 32_000);
        double[] thresholds = {40, 50, 60};
        long best = Long.MAX_VALUE;
        for (int run = 0; run < 30; run++) {
            long start = System.nanoTime();
            for (PlaceGridIndex.Entry entry : entries) {
                PlaceGridIndex.scan(view, entry, thresholds, 20);
            }
            best = Math.min(best, System.nanoTime() - start);
        }
        double ms = best / 1e6;
        assertThat(ms).as("best of 30 runs, ms").isLessThan(BUDGET_MS * CI_MARGIN);
    }

    @Test
    @DisplayName("the index is sorted by distance and a scan finds each threshold's nearest cell and the radius maximum")
    void scanFindsThresholds() {
        GridDefinition definition = SampleFixtures.TEXAS;
        FrameView view = SampleFixtures.codes(SampleFixtures.reflectivity("r"), definition,
                SampleFixtures.echoes(definition, -98.0, 30.5, 30, 2, 55), null);
        PlaceGridIndex.Entry entry = PlaceGridIndex.build(GridDef.of(definition), -98.0, 30.5, PlaceGridIndex.RANGE_KM);
        for (int i = 1; i < entry.size(); i++) {
            assertThat(entry.distKm()[i]).isGreaterThanOrEqualTo(entry.distKm()[i - 1]);
        }
        PlaceGridIndex.Scan scan = PlaceGridIndex.scan(view, entry, new double[] {40, 50, 60}, 20);
        assertThat(scan.nearestKm()[0]).isBetween(27.0, 29.5);
        assertThat(scan.nearestKm()[1]).isEqualTo(scan.nearestKm()[0]);
        assertThat(scan.nearestKm()[2]).isNaN();
        assertThat(scan.maxInRadius()).as("no echo within 20 km: code 1 is −32.5 dBZ").isEqualTo(-32.5);
        assertThat(scan.coveredInRadius()).isEqualTo(scan.radiusCells());
        assertThat(entry.within(20)).isEqualTo(scan.radiusCells());
    }
}
