package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.forecast.score.Calibration;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** The intelligence calibration layer: outcomes fit a curve, scores map to probabilities (prompt 151). */
class IntelCalibrationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-22T00:00:00Z"), ZoneOffset.UTC);

    private static IntelOutcomeEntity outcome(double raw, double o) {
        return new IntelOutcomeEntity("id-" + System.nanoTime(), IntelCalibrationService.SCOPE_EVENT,
                IntelOutcomeEntity.KIND_EVENT, "e-" + System.nanoTime(), raw, o, CLOCK.instant());
    }

    /** A monotone set of outcomes: the higher the score, the more often the outcome happened. */
    private static List<IntelOutcomeEntity> monotone(int n) {
        List<IntelOutcomeEntity> rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double raw = (i % 10) / 10.0 + 0.05;
            double o = Math.random() < raw ? 1 : 0;
            rows.add(outcome(raw, o));
        }
        return rows;
    }

    @Test
    @DisplayName("interpolate clamps outside the curve and interpolates within it")
    void interpolate() {
        List<Map<String, Object>> curve = List.of(point(0.2, 0.1), point(0.6, 0.5), point(0.9, 0.95));
        assertThat(IntelCalibrationService.interpolate(curve, 0.0)).isEqualTo(0.1);
        assertThat(IntelCalibrationService.interpolate(curve, 1.0)).isEqualTo(0.95);
        // Halfway between forecast 0.2→0.6 maps halfway between calibrated 0.1→0.5.
        assertThat(IntelCalibrationService.interpolate(curve, 0.4)).isCloseTo(0.3, within(1e-12));
    }

    @Test
    @DisplayName("below the minimum sample size a scope is not calibrated; at or above it is")
    void flips() {
        assertThat(fit(Calibration.CORP_MIN - 1).isCalibrated()).isFalse();
        assertThat(fit(Calibration.CORP_MIN + 10).isCalibrated()).isTrue();
    }

    @Test
    @DisplayName("an uncalibrated scope maps no score; a calibrated one returns a probability")
    void maps() {
        IntelOutcomeRepository outcomes = mock(IntelOutcomeRepository.class);
        IntelCalibrationRepository calibrations = mock(IntelCalibrationRepository.class);
        IntelCalibrationService service = new IntelCalibrationService(outcomes, calibrations, CLOCK);

        when(calibrations.findById(anyString())).thenReturn(Optional.empty());
        assertThat(service.calibrate(IntelCalibrationService.SCOPE_EVENT, 0.5)).isEmpty();

        IntelCalibrationEntity row = new IntelCalibrationEntity(IntelCalibrationService.SCOPE_EVENT, CLOCK.instant());
        row.setFit(List.of(point(0.2, 0.1), point(0.8, 0.9)), List.of(), 0.1, Calibration.CORP_MIN, true,
                CLOCK.instant());
        when(calibrations.findById(IntelCalibrationService.SCOPE_EVENT)).thenReturn(Optional.of(row));
        assertThat(service.calibrate(IntelCalibrationService.SCOPE_EVENT, 0.5))
                .hasValueSatisfying(p -> assertThat(p).isCloseTo(0.5, within(1e-12)));
    }

    /** Runs a refit over {@code n} outcomes and returns the saved calibration entity. */
    private static IntelCalibrationEntity fit(int n) {
        IntelOutcomeRepository outcomes = mock(IntelOutcomeRepository.class);
        IntelCalibrationRepository calibrations = mock(IntelCalibrationRepository.class);
        when(outcomes.findByScopeOrderByResolvedAtAsc(anyString())).thenReturn(monotone(n));
        when(calibrations.findById(anyString())).thenReturn(Optional.empty());
        IntelCalibrationService service = new IntelCalibrationService(outcomes, calibrations, CLOCK);
        service.refit(IntelCalibrationService.SCOPE_EVENT);
        ArgumentCaptor<IntelCalibrationEntity> saved = ArgumentCaptor.forClass(IntelCalibrationEntity.class);
        org.mockito.Mockito.verify(calibrations).save(saved.capture());
        IntelCalibrationEntity row = saved.getValue();
        assertThat(row.getSampleN()).isEqualTo(n);
        return row;
    }

    private static Map<String, Object> point(double forecast, double calibrated) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("forecast", forecast);
        out.put("calibrated", calibrated);
        return out;
    }
}
