package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.source.adapters.mrms.MrmsAdapter;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.testsupport.raster.SampleFixtures;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code derived-lightning-holdover} (prompt 099), driven tick by tick over a synthetic 24 h QPE frame: a
 * dry cell with flashes creates a candidate, a later FIRMS detection within 5 km confirms it with the
 * delay, a wet cell creates none, and re-reading the same cluster never adds its flashes twice.
 */
class LightningHoldoverTest {

    private static final Instant T0 = Instant.parse("2026-10-15T20:00:00Z");
    private static final double LON = -98.0;
    private static final double LAT = 30.5;

    @Test
    @DisplayName("candidates are keyed by res-6 cell and UTC day")
    void ids() {
        long cell = LightningHoldoverGenerator.cell(-95.5, 32.5);
        assertThat(LightningHoldoverGenerator.id(cell, "2026-10-15")).startsWith("h3:").endsWith(":2026-10-15");
        assertThat(LightningHoldoverGenerator.cell(-95.5, 32.5)).isEqualTo(cell);
    }

    /** A world whose 24 h QPE is {@code mm} everywhere on the synthetic Texas grid, with one flash cluster. */
    private static LayerHarness world(double mm) {
        LayerHarness harness = new LayerHarness(T0);
        ProductSpec spec = new MrmsAdapter(null, "").descriptor().products().stream()
                .filter(s -> s.id().equals(MrmsAdapter.QPE_24H)).findFirst().orElseThrow();
        int[] codes = new int[SampleFixtures.TEXAS.nx() * SampleFixtures.TEXAS.ny()];
        Arrays.fill(codes, (int) Math.round(mm * 10)); // 0.1 mm per code
        FrameView frame = SampleFixtures.codes(spec, SampleFixtures.TEXAS, codes,
                SampleFixtures.row(MrmsAdapter.QPE_24H, T0, null, null));
        harness.world.addFrame(MrmsAdapter.QPE_24H, frame);
        harness.put(DerivedSources.GOES_GLM, "cluster-1", "lightning-cluster", T0, LON, LAT, "flashes5m", 12);
        return harness;
    }

    @Test
    @DisplayName("a dry cell with flashes is a candidate; re-reading the cluster does not add its flashes again")
    void dryCellCreatesACandidate() {
        LayerHarness harness = world(1.0);
        LightningHoldoverGenerator generator = new LightningHoldoverGenerator(harness.registry());

        List<NormalizedRecord> first = harness.tick(generator, T0.plus(Duration.ofMinutes(10)));
        assertThat(first).singleElement().satisfies(record -> {
            assertThat(record.externalId()).startsWith("h3:").endsWith(":2026-10-15");
            assertThat(record.payload()).containsEntry("flashes", 12).containsEntry("ignitionConfirmed", false)
                    .containsEntry("severity", "medium");
        });

        harness.tick(generator, T0.plus(Duration.ofMinutes(20)));
        harness.tick(generator, T0.plus(Duration.ofMinutes(30)));
        NormalizedRecord latest = harness.world.emitted.get(harness.world.emitted.size() - 1);
        assertThat(latest.payload()).containsEntry("flashes", 12);
    }

    @Test
    @DisplayName("a FIRMS detection 3 km away five hours later confirms the ignition with its delay")
    void laterFireConfirms() {
        LayerHarness harness = world(1.0);
        LightningHoldoverGenerator generator = new LightningHoldoverGenerator(harness.registry());
        harness.tick(generator, T0.plus(Duration.ofMinutes(10)));

        Instant fireAt = T0.plus(Duration.ofHours(5));
        harness.put(DerivedSources.FIRMS, "fire-1", "fire-detection", fireAt, LON, LayerHarness.north(LAT, 3.0));
        List<NormalizedRecord> confirmed = harness.tick(generator, fireAt.plus(Duration.ofMinutes(10)));
        assertThat(confirmed).singleElement().satisfies(record -> assertThat(record.payload())
                .containsEntry("ignitionConfirmed", true).containsEntry("delayHours", 5.0));
    }

    @Test
    @DisplayName("a detection 6 km away does not confirm")
    void distantFireDoesNotConfirm() {
        LayerHarness harness = world(1.0);
        LightningHoldoverGenerator generator = new LightningHoldoverGenerator(harness.registry());
        harness.put(DerivedSources.FIRMS, "fire-1", "fire-detection", T0.plus(Duration.ofHours(2)), LON,
                LayerHarness.north(LAT, 6.0));
        List<NormalizedRecord> out = harness.tick(generator, T0.plus(Duration.ofHours(2)).plusSeconds(600));
        assertThat(out).singleElement().satisfies(record ->
                assertThat(record.payload()).containsEntry("ignitionConfirmed", false));
    }

    @Test
    @DisplayName("a wet cell (QPE 10 mm) creates no candidate")
    void wetCellCreatesNone() {
        LayerHarness harness = world(10.0);
        LightningHoldoverGenerator generator = new LightningHoldoverGenerator(harness.registry());
        assertThat(harness.tick(generator, T0.plus(Duration.ofMinutes(10)))).isEmpty();
    }

    @Test
    @DisplayName("without a QPE frame dryness is unknown, never assumed: no candidate")
    void unknownDrynessCreatesNone() {
        LayerHarness harness = new LayerHarness(T0);
        harness.put(DerivedSources.GOES_GLM, "cluster-1", "lightning-cluster", T0, LON, LAT, "flashes5m", 12);
        LightningHoldoverGenerator generator = new LightningHoldoverGenerator(harness.registry());
        assertThat(harness.tick(generator, T0.plus(Duration.ofMinutes(10)))).isEmpty();
    }
}
