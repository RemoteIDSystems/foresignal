package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.HealthVerdict;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Frame-age health of product sources (prompt 042 on the 006 HealthRule SPI). */
class FrameAgeHealthRuleTest {

    static final Instant NOW = Instant.parse("2026-10-10T15:00:00Z");
    static final ProductRegistry.Registered OBS =
            new ProductRegistry.Registered(SyntheticRasterAdapter.observationSpec(), SyntheticRasterAdapter.SOURCE, false);
    static final ProductRegistry.Registered FCST =
            new ProductRegistry.Registered(SyntheticRasterAdapter.forecastSpec(), SyntheticRasterAdapter.SOURCE, false);

    final ProductStatusTracker tracker = new ProductStatusTracker(event -> { });

    @Test
    @DisplayName("a fresh frame is healthy and reports newestFrameAt")
    void fresh() {
        Optional<HealthVerdict> v = FrameAgeHealthRule.verdict(List.of(OBS), Map.of(OBS.id(), NOW.minusSeconds(120)),
                tracker, NOW);
        assertThat(v).isPresent();
        assertThat(v.get().status()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(v.get().newestFrameAt()).isEqualTo(NOW.minusSeconds(120));
    }

    @Test
    @DisplayName("a frame older than maxGap degrades the source, naming the product")
    void stale() {
        Optional<HealthVerdict> v = FrameAgeHealthRule.verdict(List.of(OBS, FCST),
                Map.of(OBS.id(), NOW.minusSeconds(600), FCST.id(), NOW.minusSeconds(60)), tracker, NOW);
        assertThat(v.get().status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(v.get().reason()).startsWith(OBS.id() + ": newest frame is 600s old");
        assertThat(v.get().newestFrameAt()).as("newest across products").isEqualTo(NOW.minusSeconds(60));
    }

    @Test
    @DisplayName("without maxGap the limit is three cadences")
    void cadenceRule() {
        ProductRegistry.Registered noGap = new ProductRegistry.Registered(ai.foresignal.source.spi.ProductSpec
                .builder("no-gap").category("radar").dtype(ai.foresignal.source.spi.RasterDtype.U8)
                .temporal(ai.foresignal.source.spi.ProductSpec.Temporal.observation(java.time.Duration.ofMinutes(5), null))
                .build(), "src", false);
        assertThat(FrameAgeHealthRule.verdict(List.of(noGap), Map.of("no-gap", NOW.minusSeconds(14 * 60)), tracker, NOW)
                .get().status()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(FrameAgeHealthRule.verdict(List.of(noGap), Map.of("no-gap", NOW.minusSeconds(16 * 60)), tracker, NOW)
                .get().status()).isEqualTo(HealthStatus.DEGRADED);
    }

    @Test
    @DisplayName("a silent format change degrades with the decoder's reason until cleared")
    void formatChange() {
        tracker.formatChanged(OBS.id(), "variable MergedReflectivityQCComposite not found", NOW);
        Optional<HealthVerdict> v = FrameAgeHealthRule.verdict(List.of(OBS), Map.of(OBS.id(), NOW.minusSeconds(60)),
                tracker, NOW);
        assertThat(v.get().status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(v.get().reason()).contains("upstream format changed").contains("MergedReflectivityQCComposite");
        tracker.clearFormatChange(OBS.id());
        assertThat(FrameAgeHealthRule.verdict(List.of(OBS), Map.of(OBS.id(), NOW.minusSeconds(60)), tracker, NOW)
                .get().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    @DisplayName("no frame yet gives no verdict")
    void noFrames() {
        assertThat(FrameAgeHealthRule.verdict(List.of(OBS), Map.of(), tracker, NOW)).isEmpty();
    }

    @Test
    @DisplayName("a product disabled by the decoder self-test degrades the source with its reason (046)")
    void disabledProduct() {
        tracker.disable(OBS.id(), OBS.sourceId(), "decoder self-test failed (GRIB2 template 5.41 (PNG): boom)", NOW);
        Optional<HealthVerdict> v = FrameAgeHealthRule.verdict(List.of(OBS), Map.of(OBS.id(), NOW.minusSeconds(60)),
                tracker, NOW);
        assertThat(v.get().status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(v.get().reason()).isEqualTo(OBS.id() + ": disabled: decoder self-test failed (GRIB2 template 5.41 (PNG): boom)");
        tracker.enable(OBS.id(), OBS.sourceId());
        assertThat(FrameAgeHealthRule.verdict(List.of(OBS), Map.of(OBS.id(), NOW.minusSeconds(60)), tracker, NOW)
                .get().status()).isEqualTo(HealthStatus.HEALTHY);
    }
}
