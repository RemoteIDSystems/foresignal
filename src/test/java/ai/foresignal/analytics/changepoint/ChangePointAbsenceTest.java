package ai.foresignal.analytics.changepoint;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.finding.FindingSeverities;
import ai.foresignal.observability.AbsenceCheck;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Prompt 119 under OBS-1 (checkpoint 124, focus area 4): a downward change of counts that 116 detectability cannot
 * support is reported as {@code unknown} ({@code info}) with the failed check, never as a confirmed drop; a supported
 * decline, an upward change and a change of a non-count series are confirmed without (or regardless of) the check.
 */
class ChangePointAbsenceTest {

    static final Instant AT = Instant.parse("2026-03-01T00:00:00Z");

    static ChangePointDetector.Result confirmed(int direction) {
        return new ChangePointDetector.Result(ChangePointDetector.State.CONFIRMED, direction, 10, 12, AT,
                AT.plusSeconds(172_800), 20, 5, -0.75, 0.99, 4.0, "poisson", 1, 365);
    }

    static final AbsenceCheck LOW = new AbsenceCheck(false, 0.3, AbsenceCheck.LOW_DETECTABILITY);

    @Test
    @DisplayName("a downward count change with low detectability is unknown / info and carries the failed check")
    void lowDetectabilityDeclineIsUnknown() {
        ChangePointService.Classification c = ChangePointService.classify(confirmed(-1), true, true, 0.9, () -> LOW);
        assertThat(c.state()).isEqualTo("unknown");
        assertThat(c.confirmed()).isFalse();
        assertThat(c.severity()).isEqualTo(FindingSeverities.INFO);
        assertThat(c.failedDecline()).isSameAs(LOW);
        assertThat(c.coverageGap()).isFalse();
    }

    @Test
    @DisplayName("a supported decline is confirmed; rises and non-count series never ask for detectability")
    void otherwiseConfirmed() {
        ChangePointService.Classification ok = ChangePointService.classify(confirmed(-1), true, true, 0.9,
                () -> new AbsenceCheck(true, 0.95, null));
        assertThat(ok.state()).isEqualTo("confirmed");
        assertThat(ok.severity()).isEqualTo(FindingSeverities.SIGNIFICANT);
        AtomicInteger asked = new AtomicInteger();
        assertThat(ChangePointService.classify(confirmed(1), true, true, 0.9, () -> {
            asked.incrementAndGet();
            return LOW;
        }).state()).isEqualTo("confirmed");
        assertThat(ChangePointService.classify(confirmed(-1), false, true, 0.9, () -> {
            asked.incrementAndGet();
            return LOW;
        }).state()).isEqualTo("confirmed");
        assertThat(asked).hasValue(0);
    }
}
