package ai.foresignal.ingest;

import static ai.foresignal.ingest.IngestFixtures.T0;
import static ai.foresignal.ingest.IngestFixtures.aircraft;
import static ai.foresignal.ingest.IngestFixtures.aircraftRecord;
import static ai.foresignal.ingest.IngestFixtures.at;
import static ai.foresignal.ingest.IngestFixtures.observe;
import static ai.foresignal.ingest.IngestFixtures.persisted;
import static ai.foresignal.ingest.IngestFixtures.prepare;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Running-state transitions, Core-Data-Model §6.2 steps 10–12. */
class EntityStateTest {

    private static final SourceIngestProfile PROFILE = aircraft();

    /** Folds one observation: prepare, decide (no gate), apply. */
    private static EntityState fold(EntityState state, Observation obs, Outcome expected, long seq) {
        PreparedObservation prepared = prepare(PROFILE, obs, state);
        Decision decision = IngestPolicyEngine.decide(prepared, state, PROFILE, false);
        assertThat(decision.outcome()).as("outcome for %s", obs.observedAt()).isEqualTo(expected);
        return state.apply(decision, prepared, PROFILE, seq);
    }

    @Test
    @DisplayName("LIVE → PERSIST → LIVE keeps the merged payload correct when a key drops")
    void liveThenPersistThenLiveWithDroppedKey() {
        // Persist with an extra kinematic key "baro".
        EntityState state =
                persisted(PROFILE, observe(PROFILE, aircraftRecord(T0).put("baro", 1013L).build()));
        assertThat(state.live().kinematic()).containsOnlyKeys("gs", "track", "alt", "baro");

        // LIVE: gs changes and baro disappears; the dropped key is masked with a JSON null.
        state = fold(state, observe(PROFILE, aircraftRecord(at(10)).put("gs", 100.0).build()), Outcome.LIVE, 0);
        assertThat(state.live().dirty()).isTrue();
        assertThat(state.live().kinematic()).containsEntry("gs", 100.0).containsEntry("baro", null);
        assertThat(state.currentPayload()).doesNotContainKey("baro").containsEntry("gs", 100.0);

        // PERSIST (keyframe): the head takes the observation; kinematic resets to its subset, clean.
        state = fold(state, observe(PROFILE, aircraftRecord(at(240)).put("gs", 110.0).build()), Outcome.PERSIST, 2);
        assertThat(state.head().version()).isEqualTo(2);
        assertThat(state.head().seq()).isEqualTo(2);
        assertThat(state.live().dirty()).isFalse();
        assertThat(state.live().kinematic()).containsOnlyKeys("gs", "track", "alt").doesNotContainKey("baro");
        assertThat(state.head().persistedPayload()).doesNotContainKey("baro");

        // LIVE again: alt drops this time; baro must stay absent and alt must be masked.
        state = fold(state, observe(PROFILE, aircraftRecord(at(250)).put("gs", 120.0).build()), Outcome.LIVE, 0);
        Map<String, Object> withoutAlt = new java.util.LinkedHashMap<>(state.head().persistedPayload());
        state =
                fold(
                        state,
                        observe(PROFILE, NormalizedRecord.builder("a1b2c3", "aircraft")
                                .observedAt(at(260))
                                .point(IngestFixtures.LON, IngestFixtures.LAT)
                                .put("flight", "UAL1")
                                .put("squawk", 1200L)
                                .put("gs", 130.0)
                                .put("track", 90.0)
                                .build()),
                        Outcome.LIVE,
                        0);
        withoutAlt.remove("alt");
        withoutAlt.put("gs", 130.0);
        assertThat(state.live().kinematic()).containsEntry("alt", null).containsEntry("gs", 130.0);
        assertThat(state.currentPayload()).isEqualTo(withoutAlt);
    }

    @Test
    @DisplayName("a key masked by LIVE stays masked through further LIVE updates")
    void maskSurvivesRepeatedLive() {
        EntityState state = persisted(PROFILE, observe(PROFILE, aircraftRecord(T0).put("baro", 1013L).build()));
        state = fold(state, observe(PROFILE, aircraftRecord(at(10)).put("gs", 100.0).build()), Outcome.LIVE, 0);
        state = fold(state, observe(PROFILE, aircraftRecord(at(20)).put("gs", 101.0).build()), Outcome.LIVE, 0);

        assertThat(state.live().kinematic()).containsEntry("baro", null);
        assertThat(state.currentPayload()).doesNotContainKey("baro");
    }

    @Test
    @DisplayName("a touch moves times and validity only")
    void touch() {
        EntityState state = persisted(PROFILE, observe(PROFILE, aircraftRecord(T0).build()));
        long fullHash = state.live().fullHash();

        state = fold(state, observe(PROFILE, aircraftRecord(at(40)).build(), at(41)), Outcome.TOUCH, 0);

        assertThat(state.live().observedAt()).isEqualTo(at(40));
        assertThat(state.live().lastReceivedAt()).isEqualTo(at(41));
        assertThat(state.live().validUntil()).isEqualTo(at(340));
        assertThat(state.live().fullHash()).isEqualTo(fullHash);
        assertThat(state.head().version()).isEqualTo(1);
    }

    @Test
    @DisplayName("late, skipped and throttled observations leave the state unchanged")
    void noOpOutcomes() {
        EntityState state = persisted(PROFILE, observe(PROFILE, aircraftRecord(T0).build()));

        assertThat(fold(state, observe(PROFILE, aircraftRecord(at(-5)).build(), T0), Outcome.LATE, 0)).isSameAs(state);
        assertThat(fold(state, observe(PROFILE, aircraftRecord(at(5)).build()), Outcome.TOUCH_THROTTLED, 0))
                .isSameAs(state);
        EntityState absent = EntityState.absent();
        assertThat(fold(absent, observe(PROFILE, NormalizedRecord.tombstone("a1b2c3", "aircraft", T0)), Outcome.SKIP, 0))
                .isSameAs(absent);
    }

    @Test
    @DisplayName("a tombstone clears geometry, payload and kinematic but keeps the kind")
    void tombstoneClears() {
        EntityState state = persisted(PROFILE, observe(PROFILE, aircraftRecord(T0).build()));

        state = fold(state, observe(PROFILE, NormalizedRecord.tombstone("a1b2c3", "aircraft", at(30))), Outcome.PERSIST, 2);

        assertThat(state.head().tombstone()).isTrue();
        assertThat(state.head().persistedGeometry()).isNull();
        assertThat(state.head().persistedCentroid()).isNull();
        assertThat(state.head().persistedPayload()).isEmpty();
        assertThat(state.live().geometry()).isNull();
        assertThat(state.live().kinematic()).isEmpty();
        assertThat(state.runningGeometry()).isNull();
    }

    @Test
    @DisplayName("persisted values come from loaded path values until a PERSIST supplies the payload")
    void persistedValueSources() {
        EntityState.Head loaded =
                new EntityState.Head(true, 7, 3, false, 42L, T0, T0, null, null, Map.of("gs", 250.0), null, null);
        EntityState state = new EntityState(loaded, new EntityState.Live(T0, T0, null, 1L, false, Map.of(), null, null));

        assertThat(state.persistedValue("gs")).isEqualTo(250.0);
        assertThat(state.persistedValue("alt")).isNull();
        assertThat(state.currentPayload()).isNull();
        assertThat(state.hasHead()).isTrue();
        assertThat(EntityState.absent().hasHead()).isFalse();
        assertThat(EntityState.absent().runningGeometry()).isNull();
    }

    @Test
    @DisplayName("last_received_at never moves backwards")
    void lastReceivedMonotonic() {
        EntityState state = persisted(PROFILE, observe(PROFILE, aircraftRecord(T0).build(), at(100)));
        state = fold(state, observe(PROFILE, aircraftRecord(at(10)).put("gs", 5.0).build(), at(50)), Outcome.LIVE, 0);
        assertThat(state.live().lastReceivedAt()).isEqualTo(at(100));
    }
}
