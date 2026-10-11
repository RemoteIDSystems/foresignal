package ai.foresignal.ingest;

import static ai.foresignal.ingest.IngestFixtures.T0;
import static ai.foresignal.ingest.IngestFixtures.aircraft;
import static ai.foresignal.ingest.IngestFixtures.aircraftRecord;
import static ai.foresignal.ingest.IngestFixtures.at;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Invariant ING-4 (Core-Data-Model I4): per entity, accepted {@code observed_at} never decreases;
 * future values are clamped and late ones dropped. Exercised end to end through the pure core:
 * normalize, sort, then fold through the decision table.
 */
class TimeContractTest {

    @Test
    @DisplayName("folding a shuffled batch accepts a non-decreasing observed_at sequence")
    void acceptedObservedAtNeverDecreases() {
        SourceIngestProfile profile = aircraft();
        // Received at T0+100; one future observation and several out of order.
        Instant received = at(100);
        List<IngestRecord> batch = new ArrayList<>();
        long[] offsets = {50, 10, 400, 30, 10, 70};
        for (int i = 0; i < offsets.length; i++) {
            batch.add(new IngestRecord(aircraftRecord(at(offsets[i])).put("gs", (double) i).build(), received, i));
        }

        NormalizedBatch normalized = new Normalizer().normalize(profile, batch);

        EntityState state = EntityState.absent();
        Instant lastAccepted = Instant.MIN;
        for (Observation obs : normalized.observations()) {
            assertThat(obs.observedAt()).isBeforeOrEqualTo(received);
            PreparedObservation prepared = IngestPolicyEngine.prepare(obs, state, profile);
            Decision decision = IngestPolicyEngine.decide(prepared, state, profile, false);
            if (decision.outcome() != Outcome.LATE) {
                assertThat(obs.observedAt()).isAfterOrEqualTo(lastAccepted);
                lastAccepted = obs.observedAt();
            }
            state = state.apply(decision, prepared, profile, 1);
        }
        assertThat(normalized.observations()).extracting(Observation::observedAt).isSorted();
    }

    @Test
    @DisplayName("an observation older than live state is LATE across batches")
    void lateAcrossBatches() {
        SourceIngestProfile profile = aircraft();
        Observation first = IngestFixtures.observe(profile, aircraftRecord(at(60)).build());
        EntityState state = IngestFixtures.persisted(profile, first);

        Observation older = IngestFixtures.observe(profile, aircraftRecord(T0).put("squawk", 7700L).build(), at(70));

        assertThat(IngestFixtures.decide(profile, state, older, false).outcome()).isEqualTo(Outcome.LATE);
    }
}
