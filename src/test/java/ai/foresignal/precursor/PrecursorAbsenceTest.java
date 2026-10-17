package ai.foresignal.precursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.backtest.BacktestCoverage;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.ObservabilityDeclarations;
import ai.foresignal.observability.ObservabilityService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Prompt 120 under OBS-1 (checkpoint 124, focus area 4): in precursor discovery and validation, an antecedent without
 * a consequent counts as a miss only when 116 detectability could have seen the consequent's absence; otherwise it is
 * excluded and counted under {@code LOW_DETECTABILITY}, so an unseen consequent is never read as "it did not happen".
 */
class PrecursorAbsenceTest {

    static final Instant DAY0 = Instant.parse("2026-01-05T12:00:00Z");

    @SuppressWarnings("unchecked")
    static PrecursorMiner miner(AbsenceCheck check) {
        PrecursorOccurrences occurrences = mock(PrecursorOccurrences.class);
        when(occurrences.signalFamily(any())).thenReturn("conflict");
        ObservabilityDeclarations declarations = mock(ObservabilityDeclarations.class);
        when(declarations.eventFamilies()).thenReturn(List.of("conflict"));
        ObservabilityService observability = mock(ObservabilityService.class);
        when(observability.canSeeAbsence(anyString(), any(), any(), any(), any())).thenReturn(check);
        ObjectProvider<ObservabilityDeclarations> declarationsProvider = mock(ObjectProvider.class);
        when(declarationsProvider.getIfAvailable()).thenReturn(declarations);
        ObjectProvider<ObservabilityService> observabilityProvider = mock(ObjectProvider.class);
        when(observabilityProvider.getIfAvailable()).thenReturn(observability);
        return new PrecursorMiner(occurrences, null, null, null, null, null, null, mock(BacktestCoverage.class), null,
                null, observabilityProvider, declarationsProvider, null, null, null, Clock.systemUTC());
    }

    static PrecursorOccurrences.Occurrence occurrence(Instant at, String id) {
        return new PrecursorOccurrences.Occurrence(at, "intel-event:" + id, "YE", null, "test", id, "warning");
    }

    static final Candidate CANDIDATE = new Candidate(Candidate.EVENT,
            Map.of("selector", Map.of("intelEventType", "airstrike", "regionCodes", List.of("YE")), "family",
                    "conflict"),
            Map.of("selector", Map.of("intelEventType", "armed-clash", "regionCodes", List.of("YE")), "family",
                    "conflict"),
            Map.of("rule", "none"), Duration.ZERO, Duration.ofDays(2), List.of("YE"), "auto", null, 10, 10, null);

    /** Ten antecedents a day apart; only the first three are followed by a consequent within a day. */
    static PrecursorMiner.EventRun run(AbsenceCheck check) {
        List<PrecursorOccurrences.Occurrence> ant = new ArrayList<>();
        List<PrecursorOccurrences.Occurrence> con = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Instant at = DAY0.plus(Duration.ofDays(3L * i));
            ant.add(occurrence(at, "a" + i));
            if (i < 3) {
                con.add(occurrence(at.plus(Duration.ofHours(6)), "c" + i));
            }
        }
        return miner(check).eventRun(CANDIDATE, ant, con, DAY0.minus(Duration.ofDays(1)),
                DAY0.plus(Duration.ofDays(60)), Set.of(), "absence-test");
    }

    @Test
    @DisplayName("with low detectability the antecedents without a consequent are excluded, never counted as misses")
    void lowDetectabilityExcludesMisses() {
        PrecursorMiner.EventRun low = run(new AbsenceCheck(false, 0.3, AbsenceCheck.LOW_DETECTABILITY));
        assertThat(low.lowDetectability()).isEqualTo(7);
        assertThat(low.result().nAntecedent()).isEqualTo(3);
        assertThat(low.result().hits()).isEqualTo(3);
    }

    @Test
    @DisplayName("with adequate detectability the same antecedents are misses")
    void adequateDetectabilityCountsMisses() {
        PrecursorMiner.EventRun ok = run(new AbsenceCheck(true, 0.95, null));
        assertThat(ok.lowDetectability()).isZero();
        assertThat(ok.result().nAntecedent()).isEqualTo(10);
        assertThat(ok.result().hits()).isEqualTo(3);
    }
}
