package ai.foresignal.ingest;

import static ai.foresignal.ingest.IngestFixtures.LAT;
import static ai.foresignal.ingest.IngestFixtures.LON;
import static ai.foresignal.ingest.IngestFixtures.T0;
import static ai.foresignal.ingest.IngestFixtures.aircraft;
import static ai.foresignal.ingest.IngestFixtures.aircraftRecord;
import static ai.foresignal.ingest.IngestFixtures.aircraftWith;
import static ai.foresignal.ingest.IngestFixtures.at;
import static ai.foresignal.ingest.IngestFixtures.catalog;
import static ai.foresignal.ingest.IngestFixtures.catalogRecord;
import static ai.foresignal.ingest.IngestFixtures.decide;
import static ai.foresignal.ingest.IngestFixtures.eventRecord;
import static ai.foresignal.ingest.IngestFixtures.events;
import static ai.foresignal.ingest.IngestFixtures.observe;
import static ai.foresignal.ingest.IngestFixtures.persisted;
import static ai.foresignal.ingest.IngestFixtures.prepare;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The Core-Data-Model §5.2 decision table, row by row.
 *
 * <p>{@link #decisionTable} is the matrix the design asks for: each of the 12 rows has a firing
 * case, a boundary case and a near-miss that falls through to a later row. Test names start with
 * the row number.
 */
class IngestPolicyEngineTest {

    private static final Decision LATE = Decision.of(Outcome.LATE);
    private static final Decision SKIP = Decision.of(Outcome.SKIP);
    private static final Decision TOUCH = Decision.of(Outcome.TOUCH);
    private static final Decision THROTTLED = Decision.of(Outcome.TOUCH_THROTTLED);
    private static final Decision LIVE = Decision.of(Outcome.LIVE);

    private static Decision persist(PersistReason reason) {
        return Decision.persist(reason);
    }

    /** One matrix case: a profile, a starting state, an observation and the gate's answer. */
    private record Scenario(
            SourceIngestProfile profile, EntityState state, Observation observation, boolean gateFlip) {

        Decision decide() {
            return IngestFixtures.decide(profile, state, observation, gateFlip);
        }
    }

    private static Arguments row(int row, String name, Decision expected, Supplier<Scenario> scenario) {
        return Arguments.of("row " + row + ": " + name, expected, scenario);
    }

    // ---------- scenario helpers ----------

    /** An aircraft persisted at T0 (observed and received at T0). */
    private static EntityState aircraftAtT0() {
        return persisted(aircraft(), observe(aircraft(), aircraftRecord(T0).build()));
    }

    /** The persisted aircraft, then re-observed at {@code seconds} after T0 with {@code edit}. */
    private static Scenario aircraftThen(long seconds, java.util.function.UnaryOperator<NormalizedRecord.Builder> edit) {
        return aircraftThen(seconds, seconds, edit, false);
    }

    private static Scenario aircraftThen(
            long observedSeconds,
            long receivedSeconds,
            java.util.function.UnaryOperator<NormalizedRecord.Builder> edit,
            boolean gateFlip) {
        Observation obs = observe(aircraft(), edit.apply(aircraftRecord(at(observedSeconds))).build(), at(receivedSeconds));
        return new Scenario(aircraft(), aircraftAtT0(), obs, gateFlip);
    }

    /** An aircraft tombstone observed and received at {@code seconds} after T0. */
    private static Observation aircraftTombstone(long seconds) {
        return observe(aircraft(), NormalizedRecord.tombstone("a1b2c3", "aircraft", at(seconds)));
    }

    /** The state after persisting a tombstone at T0 on top of the aircraft. */
    private static EntityState tombstonedAircraft() {
        EntityState state = aircraftAtT0();
        PreparedObservation tombstone = prepare(aircraft(), aircraftTombstone(0), state);
        return state.afterPersist(tombstone, aircraft(), 2);
    }

    /** About {@code meters} north of (LON, LAT), in degrees of latitude. */
    private static double north(double meters) {
        return LAT + Math.toDegrees(meters / DeadReckoning.EARTH_RADIUS_METERS);
    }

    static Stream<Arguments> decisionTable() {
        List<Arguments> rows = new ArrayList<>();

        // Row 1: observed_at < l.observed_at -> LATE.
        rows.add(row(1, "older observation is LATE", LATE, () -> aircraftThen(-1, 0, b -> b.put("gs", 300.0), false)));
        rows.add(row(1, "boundary: equal observed_at is not late", THROTTLED, () -> aircraftThen(0, b -> b)));
        rows.add(row(1, "near-miss: 1 ms later is not late", THROTTLED, () -> {
            Observation obs = observe(aircraft(), aircraftRecord(T0.plusMillis(1)).build());
            return new Scenario(aircraft(), aircraftAtT0(), obs, false);
        }));

        // Row 2: tombstone with no head or a tombstoned head -> SKIP.
        rows.add(row(2, "tombstone without a head is skipped", SKIP,
                () -> new Scenario(aircraft(), EntityState.absent(), aircraftTombstone(10), false)));
        rows.add(row(2, "boundary: tombstone on a tombstoned head is skipped", SKIP,
                () -> new Scenario(aircraft(), tombstonedAircraft(), aircraftTombstone(10), false)));
        rows.add(row(2, "near-miss: tombstone on a live head persists", persist(PersistReason.TOMBSTONE),
                () -> new Scenario(aircraft(), aircraftAtT0(), aircraftTombstone(10), false)));

        // Row 3: tombstone -> TOMBSTONE.
        rows.add(row(3, "tombstone of a live entity", persist(PersistReason.TOMBSTONE),
                () -> new Scenario(aircraft(), aircraftAtT0(), aircraftTombstone(60), false)));
        rows.add(row(3, "boundary: tombstone at the same observed_at", persist(PersistReason.TOMBSTONE),
                () -> new Scenario(aircraft(), aircraftAtT0(), aircraftTombstone(0), false)));
        rows.add(row(3, "near-miss: a non-tombstone with no head is FIRST", persist(PersistReason.FIRST),
                () -> new Scenario(aircraft(), EntityState.absent(), observe(aircraft(), aircraftRecord(T0).build()), false)));

        // Row 4: no head -> FIRST.
        rows.add(row(4, "no head", persist(PersistReason.FIRST),
                () -> new Scenario(events(), EntityState.absent(), observe(events(), eventRecord(T0).build()), false)));
        rows.add(row(4, "boundary: no head, no geometry, empty payload", persist(PersistReason.FIRST), () -> {
            Observation obs = observe(events(), NormalizedRecord.builder("ev1", "earthquake").observedAt(T0).build());
            return new Scenario(events(), EntityState.absent(), obs, false);
        }));
        rows.add(row(4, "near-miss: a tombstoned head is REAPPEAR", persist(PersistReason.REAPPEAR),
                () -> new Scenario(aircraft(), tombstonedAircraft(), observe(aircraft(), aircraftRecord(at(10)).build()), false)));

        // Row 5: head tombstoned or expired -> REAPPEAR. TTL 5 min on the observed axis.
        rows.add(row(5, "expired live row", persist(PersistReason.REAPPEAR), () -> aircraftThen(301, b -> b)));
        rows.add(row(5, "boundary: anchor equal to valid_until is not expired", persist(PersistReason.HEARTBEAT),
                () -> aircraftThen(300, b -> b)));
        rows.add(row(5, "near-miss: 1 s before expiry", persist(PersistReason.HEARTBEAT), () -> aircraftThen(299, b -> b)));

        // Row 6: static hash differs -> SIGNIFICANT.
        rows.add(row(6, "significant field changed", persist(PersistReason.SIGNIFICANT),
                () -> aircraftThen(5, b -> b.put("squawk", 7700L))));
        rows.add(row(6, "boundary: 1200 and 1200.0 hash equal", THROTTLED,
                () -> aircraftThen(5, b -> b.put("squawk", 1200.0))));
        rows.add(row(6, "near-miss: only a kinematic field changed", LIVE, () -> aircraftThen(5, b -> b.put("gs", 120.0))));

        // Row 7: ALL and full hash differs -> CHANGE (the static hash only sees geom IS NULL).
        rows.add(row(7, "ALL, geometry moved", persist(PersistReason.CHANGE), () -> {
            EntityState state = persisted(events(), observe(events(), eventRecord(T0).build()));
            Observation obs = observe(events(), eventRecord(at(1)).point(LON + 0.01, LAT).build(), at(10));
            return new Scenario(events(), state, obs, false);
        }));
        rows.add(row(7, "boundary: a move below 1e-7 degrees hashes equal", TOUCH, () -> {
            EntityState state = persisted(events(), observe(events(), eventRecord(T0).build()));
            Observation obs = observe(events(), eventRecord(at(1)).point(LON + 4e-8, LAT).build(), at(60));
            return new Scenario(events(), state, obs, false);
        }));
        rows.add(row(7, "near-miss: the same move under SAMPLED is LIVE", LIVE,
                () -> aircraftThen(5, b -> b.point(LON + 0.001, LAT))));

        // Row 8: keyframeDue (keyframe 240 s on the observed axis).
        rows.add(row(8, "keyframe due, unchanged and clean", persist(PersistReason.HEARTBEAT), () -> aircraftThen(241, b -> b)));
        rows.add(row(8, "boundary: exactly keyframe", persist(PersistReason.HEARTBEAT), () -> aircraftThen(240, b -> b)));
        rows.add(row(8, "near-miss: keyframe minus 1 s touches", TOUCH, () -> aircraftThen(239, b -> b)));

        // Row 9: gate flip and Δt >= edgeMinInterval (30 s).
        rows.add(row(9, "gate flip after edgeMinInterval", persist(PersistReason.TRIGGER_EDGE),
                () -> aircraftThen(31, 31, b -> b.put("gs", 120.0), true)));
        rows.add(row(9, "boundary: Δt = edgeMinInterval", persist(PersistReason.TRIGGER_EDGE),
                () -> aircraftThen(30, 30, b -> b.put("gs", 120.0), true)));
        rows.add(row(9, "near-miss: Δt = edgeMinInterval − 1 s", LIVE,
                () -> aircraftThen(29, 29, b -> b.put("gs", 120.0), true)));

        // Row 10: SAMPLED, Δt >= minInterval (60 s), deviation >= 1,000 m.
        rows.add(row(10, "moved 2 km after 61 s", persist(PersistReason.SAMPLE),
                () -> aircraftThen(61, b -> b.point(LON, north(2000)))));
        rows.add(row(10, "boundary: Δt = minInterval", persist(PersistReason.SAMPLE),
                () -> aircraftThen(60, b -> b.point(LON, north(2000)))));
        rows.add(row(10, "near-miss: Δt = minInterval − 1 s", LIVE,
                () -> aircraftThen(59, b -> b.point(LON, north(2000)))));

        // Row 11: full hash equal -> TOUCH when touchMin (30 s) passed on either axis.
        rows.add(row(11, "unchanged after touchMin on the observed axis", TOUCH, () -> aircraftThen(31, b -> b)));
        rows.add(row(11, "boundary: exactly touchMin on the received axis", TOUCH, () -> aircraftThen(0, 30, b -> b, false)));
        rows.add(row(11, "near-miss: touchMin − 1 s is throttled", THROTTLED, () -> aircraftThen(29, b -> b)));

        // Row 12: otherwise -> LIVE.
        rows.add(row(12, "kinematic change, nothing due", LIVE, () -> aircraftThen(10, b -> b.put("alt", 30_500L))));
        rows.add(row(12, "boundary: Δt = minInterval but deviation just under 1,000 m", LIVE,
                () -> aircraftThen(60, b -> b.point(LON, north(990)))));
        rows.add(row(12, "near-miss: content back to persisted falls to row 11", THROTTLED, () -> aircraftThen(10, b -> b)));

        return rows.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("decisionTable")
    @DisplayName("§5.2 decision table")
    void decisionTable(String name, Decision expected, Supplier<Scenario> scenario) {
        assertThat(scenario.get().decide()).as(name).isEqualTo(expected);
    }

    @Test
    @DisplayName("the matrix covers all 12 rows, three cases each")
    void matrixCoversEveryRow() {
        Map<String, Long> perRow =
                decisionTable()
                        .map(a -> ((String) a.get()[0]).split(":")[0])
                        .collect(java.util.stream.Collectors.groupingBy(s -> s, java.util.stream.Collectors.counting()));
        assertThat(perRow).hasSize(12);
        assertThat(perRow.values()).allMatch(count -> count == 3);
    }

    // ---------- row 8 details ----------

    @Nested
    @DisplayName("row 8: HEARTBEAT vs KEYFRAME")
    class HeartbeatVersusKeyframe {

        private EntityState dirty() {
            EntityState state = aircraftAtT0();
            Observation live = observe(aircraft(), aircraftRecord(at(10)).put("gs", 150.0).build());
            PreparedObservation prepared = prepare(aircraft(), live, state);
            assertThat(IngestPolicyEngine.decide(prepared, state, aircraft(), false)).isEqualTo(LIVE);
            return state.afterLive(prepared);
        }

        @Test
        @DisplayName("row 8: live equal to the observation but dirty is a KEYFRAME")
        void dirtyEqualIsKeyframe() {
            Observation obs = observe(aircraft(), aircraftRecord(at(240)).put("gs", 150.0).build());
            assertThat(decide(aircraft(), dirty(), obs, false)).isEqualTo(persist(PersistReason.KEYFRAME));
        }

        @Test
        @DisplayName("row 8: content back to persisted while live is dirty is a KEYFRAME")
        void backToPersistedWhileDirtyIsKeyframe() {
            Observation obs = observe(aircraft(), aircraftRecord(at(240)).build());
            assertThat(decide(aircraft(), dirty(), obs, false)).isEqualTo(persist(PersistReason.KEYFRAME));
        }

        @Test
        @DisplayName("row 8: clean and equal is a HEARTBEAT")
        void cleanEqualIsHeartbeat() {
            Observation obs = observe(aircraft(), aircraftRecord(at(240)).build());
            assertThat(decide(aircraft(), aircraftAtT0(), obs, false)).isEqualTo(persist(PersistReason.HEARTBEAT));
        }

        @Test
        @DisplayName("row 8: clean but changed is a KEYFRAME")
        void cleanChangedIsKeyframe() {
            Observation obs = observe(aircraft(), aircraftRecord(at(240)).put("gs", 10.0).build());
            assertThat(decide(aircraft(), aircraftAtT0(), obs, false)).isEqualTo(persist(PersistReason.KEYFRAME));
        }
    }

    @Nested
    @DisplayName("row 8: retention keyframe and keyframe none")
    class RetentionKeyframe {

        private EntityState catalogAtT0() {
            return persisted(catalog(), observe(catalog(), catalogRecord(T0).build()));
        }

        @Test
        @DisplayName("keyframe none: no TTL means no anchor-axis keyframe")
        void keyframeNone() {
            assertThat(catalog().keyframe()).isNull();
            assertThat(catalog().retentionKeyframe()).isEqualTo(Duration.ofHours(42));
        }

        @Test
        @DisplayName("row 8: received exactly window/4 after the persisted version is a HEARTBEAT")
        void exactlyWindowQuarter() {
            Observation obs = observe(catalog(), catalogRecord(T0).build(), T0.plus(Duration.ofHours(42)));
            assertThat(decide(catalog(), catalogAtT0(), obs, false)).isEqualTo(persist(PersistReason.HEARTBEAT));
        }

        @Test
        @DisplayName("row 8 near-miss: window/4 − 1 s touches (fixed upstream observed_at)")
        void justBeforeWindowQuarter() {
            Observation obs =
                    observe(catalog(), catalogRecord(T0).build(), T0.plus(Duration.ofHours(42)).minusSeconds(1));
            assertThat(decide(catalog(), catalogAtT0(), obs, false)).isEqualTo(TOUCH);
        }

        @Test
        @DisplayName("row 8: the retention keyframe applies on TRACK too, on the received axis")
        void retentionKeyframeOnTrack() {
            SourceIngestProfile profile = aircraft();
            assertThat(profile.retentionKeyframe()).isEqualTo(Duration.ofHours(18));
            assertThat(profile.effectiveKeyframe()).isEqualTo(Duration.ofSeconds(240));
        }

        @Test
        @DisplayName("ALL default keyframe ttl − poll − 30 s on the received axis")
        void allDefaultKeyframe() {
            SourceIngestProfile profile = events();
            assertThat(profile.keyframe()).isEqualTo(Duration.ofSeconds(510));
            EntityState state = persisted(profile, observe(profile, eventRecord(T0).build()));
            Observation due = observe(profile, eventRecord(T0).build(), at(510));
            Observation notDue = observe(profile, eventRecord(T0).build(), at(509));
            assertThat(decide(profile, state, due, false)).isEqualTo(persist(PersistReason.HEARTBEAT));
            assertThat(decide(profile, state, notDue, false)).isEqualTo(TOUCH);
        }
    }

    // ---------- row 10 details ----------

    @Nested
    @DisplayName("row 10: SAMPLE, thresholds and dead reckoning")
    class Sampling {

        @Test
        @DisplayName("row 10: a threshold of exactly delta samples")
        void thresholdExactlyDelta() {
            assertThat(aircraftThen(60, b -> b.put("alt", 31_000L)).decide()).isEqualTo(persist(PersistReason.SAMPLE));
        }

        @Test
        @DisplayName("row 10 near-miss: just under the threshold is LIVE")
        void thresholdJustUnder() {
            assertThat(aircraftThen(60, b -> b.put("alt", 30_999L)).decide()).isEqualTo(LIVE);
        }

        @Test
        @DisplayName("row 10: angular thresholds wrap (350° to 10° is 20°)")
        void angularThresholdWraps() {
            EntityState state =
                    persisted(aircraft(), observe(aircraft(), aircraftRecord(T0).put("track", 350.0).build()));
            Observation obs = observe(aircraft(), aircraftRecord(at(60)).put("track", 10.0).build());
            assertThat(decide(aircraft(), state, obs, false)).isEqualTo(persist(PersistReason.SAMPLE));
        }

        @Test
        @DisplayName("row 10: thresholds=false ignores thresholds")
        void thresholdsOff() {
            SourceIngestProfile profile =
                    aircraftWith(new IngestPolicy.Sampled(
                            Duration.ofSeconds(60), Duration.ofSeconds(240), 1000.0, false, Duration.ofSeconds(30)));
            EntityState state = persisted(profile, observe(profile, aircraftRecord(T0).build()));
            Observation obs = observe(profile, aircraftRecord(at(60)).put("alt", 40_000L).build());
            assertThat(decide(profile, state, obs, false)).isEqualTo(LIVE);
        }

        @Test
        @DisplayName("row 10: a non-numeric threshold value on either side is not exceeded")
        void nonNumericThreshold() {
            assertThat(aircraftThen(60, b -> b.put("alt", "ground")).decide()).isEqualTo(LIVE);
        }

        @Test
        @DisplayName("row 10: an aircraft exactly where dead reckoning predicted is not sampled")
        void onPredictedTrackIsNotSampled() {
            EntityState state =
                    persisted(aircraft(), observe(aircraft(), aircraftRecord(T0).put("gs", 480.0).build()));
            var predicted =
                    DeadReckoning.destination(
                            new org.locationtech.jts.geom.Coordinate(LON, LAT), 90.0, 480 * 0.514444 * 60);
            Observation obs =
                    observe(aircraft(), aircraftRecord(at(60)).put("gs", 480.0).point(predicted.x, predicted.y).build());
            assertThat(decide(aircraft(), state, obs, false)).isEqualTo(LIVE);
        }

        @Test
        @DisplayName("row 10: an aircraft that stopped where it was predicted to move is sampled")
        void offPredictedTrackIsSampled() {
            EntityState state =
                    persisted(aircraft(), observe(aircraft(), aircraftRecord(T0).put("gs", 480.0).build()));
            Observation obs = observe(aircraft(), aircraftRecord(at(60)).put("gs", 480.0).build());
            assertThat(decide(aircraft(), state, obs, false)).isEqualTo(persist(PersistReason.SAMPLE));
        }

        @Test
        @DisplayName("LATEST never samples")
        void latestNeverSamples() {
            SourceIngestProfile profile = aircraftWith(IngestPolicy.latest(Duration.ofSeconds(240)));
            EntityState state = persisted(profile, observe(profile, aircraftRecord(T0).build()));
            Observation moved = observe(profile, aircraftRecord(at(120)).point(LON, north(50_000)).put("alt", 1L).build());
            assertThat(decide(profile, state, moved, false)).isEqualTo(LIVE);
        }

        @Test
        @DisplayName("LATEST still persists trigger edges and keyframes")
        void latestKeepsEdgesAndKeyframes() {
            SourceIngestProfile profile = aircraftWith(IngestPolicy.latest(Duration.ofSeconds(240)));
            EntityState state = persisted(profile, observe(profile, aircraftRecord(T0).build()));
            // LATEST's default edgeMinInterval is keyframe / 2 = 120 s.
            Observation edge = observe(profile, aircraftRecord(at(120)).put("gs", 5.0).build());
            Observation keyframe = observe(profile, aircraftRecord(at(240)).put("gs", 5.0).build());
            assertThat(decide(profile, state, edge, true)).isEqualTo(persist(PersistReason.TRIGGER_EDGE));
            assertThat(decide(profile, state, keyframe, false)).isEqualTo(persist(PersistReason.KEYFRAME));
        }

        @Test
        @DisplayName("without minDeviationMeters only thresholds can sample")
        void noDeviationTest() {
            SourceIngestProfile profile =
                    aircraftWith(new IngestPolicy.Sampled(
                            Duration.ofSeconds(60), Duration.ofSeconds(240), null, true, Duration.ofSeconds(30)));
            EntityState state = persisted(profile, observe(profile, aircraftRecord(T0).build()));
            Observation moved = observe(profile, aircraftRecord(at(60)).point(LON, north(5000)).build());
            assertThat(decide(profile, state, moved, false)).isEqualTo(LIVE);
        }

        @Test
        @DisplayName("deviation is 0 when either side has no geometry")
        void deviationWithoutGeometry() {
            SourceIngestProfile profile = events();
            EntityState noGeometry =
                    persisted(profile, observe(profile, NormalizedRecord.builder("x", "earthquake").observedAt(T0).build()));
            assertThat(IngestPolicyEngine.deviationMeters(
                            ai.foresignal.source.spi.Geo.point(1, 1), noGeometry, profile, Duration.ofSeconds(60)))
                    .isZero();
            assertThat(IngestPolicyEngine.deviationMeters(null, aircraftAtT0(), aircraft(), Duration.ofSeconds(60)))
                    .isZero();
        }
    }

    // ---------- carry-forward ----------

    @Nested
    @DisplayName("TRACK carry-forward")
    class CarryForward {

        @Test
        @DisplayName("a missing position inherits the running geometry and is flagged POSITION_STALE")
        void inheritsGeometry() {
            EntityState state = aircraftAtT0();
            Observation noPosition = observe(aircraft(), aircraftRecord(at(10)).geometry(null).build());

            PreparedObservation prepared = prepare(aircraft(), noPosition, state);

            assertThat(prepared.observation().geometry()).isEqualTo(state.runningGeometry());
            assertThat(prepared.observation().hasFlag(QaFlags.POSITION_STALE)).isTrue();
        }

        @Test
        @DisplayName("carry-forward does not flip the static hash")
        void staticHashUnchanged() {
            EntityState state = aircraftAtT0();
            Observation noPosition = observe(aircraft(), aircraftRecord(at(10)).geometry(null).build());

            PreparedObservation prepared = prepare(aircraft(), noPosition, state);

            assertThat(prepared.staticHash()).isEqualTo(state.head().staticHash());
            assertThat(prepared.fullHash()).isEqualTo(state.live().fullHash());
            assertThat(IngestPolicyEngine.decide(prepared, state, aircraft(), false)).isEqualTo(THROTTLED);
        }

        @Test
        @DisplayName("without carry-forward a lost position would be SIGNIFICANT (geom IS NULL is hashed)")
        void lostPositionOnStandardIsSignificant() {
            SourceIngestProfile profile = events();
            EntityState state = persisted(profile, observe(profile, eventRecord(T0).build()));
            Observation noPosition = observe(profile, eventRecord(at(10)).geometry(null).build(), at(10));

            PreparedObservation prepared = prepare(profile, noPosition, state);

            assertThat(prepared.observation().geometry()).isNull();
            assertThat(prepared.observation().hasFlag(QaFlags.POSITION_STALE)).isFalse();
            assertThat(IngestPolicyEngine.decide(prepared, state, profile, false))
                    .isEqualTo(persist(PersistReason.SIGNIFICANT));
        }

        @Test
        @DisplayName("a tombstone never inherits geometry")
        void tombstoneDoesNotInherit() {
            PreparedObservation prepared = prepare(aircraft(), aircraftTombstone(10), aircraftAtT0());
            assertThat(prepared.observation().geometry()).isNull();
            assertThat(prepared.kinematic()).isEmpty();
        }

        @Test
        @DisplayName("nothing to inherit without a running geometry")
        void nothingToInherit() {
            Observation noPosition = observe(aircraft(), aircraftRecord(T0).geometry(null).build());
            PreparedObservation prepared = prepare(aircraft(), noPosition, EntityState.absent());
            assertThat(prepared.observation().geometry()).isNull();
            assertThat(prepared.observation().hasFlag(QaFlags.POSITION_STALE)).isFalse();
        }
    }

    // ---------- gate ----------

    @Nested
    @DisplayName("PersistGate consultation")
    class Gate {

        /** A gate that records what it was asked and flips when altitude crosses 20,000. */
        private static final class RecordingGate implements GateSnapshot {
            GateState persisted;
            GateState candidate;

            @Override
            public GateSpec spec(String sourceId) {
                return new GateSpec(Set.of("alt", "missing.path"), true);
            }

            @Override
            public boolean flips(String sourceId, String kind, GateState persisted, GateState candidate) {
                this.persisted = persisted;
                this.candidate = candidate;
                return above(persisted) != above(candidate);
            }

            private static boolean above(GateState state) {
                Object alt = state.pathValues().get("alt");
                return alt instanceof Number n && n.doubleValue() > 20_000;
            }
        }

        @Test
        @DisplayName("compares persisted head state with the candidate")
        void comparesPersistedWithCandidate() {
            RecordingGate gate = new RecordingGate();
            EntityState state = aircraftAtT0();
            PreparedObservation prepared =
                    prepare(aircraft(), observe(aircraft(), aircraftRecord(at(40)).put("alt", 10_000L).build()), state);

            boolean flip = IngestPolicyEngine.gateFlip(prepared, state, aircraft(), gate);

            assertThat(flip).isTrue();
            assertThat(gate.persisted.pathValues()).containsEntry("alt", 30_000L).containsEntry("missing.path", null);
            assertThat(gate.candidate.pathValues()).containsEntry("alt", 10_000L);
            assertThat(gate.persisted.geometry()).isEqualTo(state.head().persistedGeometry());
            assertThat(gate.candidate.geometry()).isEqualTo(prepared.observation().geometry());
        }

        @Test
        @DisplayName("no head, no rules or the empty snapshot never flip")
        void neverFlipsWithoutHeadOrRules() {
            PreparedObservation prepared =
                    prepare(aircraft(), observe(aircraft(), aircraftRecord(T0).build()), EntityState.absent());
            assertThat(IngestPolicyEngine.gateFlip(prepared, EntityState.absent(), aircraft(), new RecordingGate()))
                    .isFalse();
            assertThat(IngestPolicyEngine.gateFlip(prepared, aircraftAtT0(), aircraft(), GateSnapshot.EMPTY)).isFalse();
            assertThat(IngestPolicyEngine.gateFlip(prepared, aircraftAtT0(), aircraft(), null)).isFalse();
            assertThat(new NoopPersistGate().snapshot()).isSameAs(GateSnapshot.EMPTY);
        }

        @Test
        @DisplayName("edgeMinInterval null counts as zero (ALL)")
        void nullEdgeMinIntervalIsZero() {
            SourceIngestProfile profile = events();
            assertThat(profile.policy().edgeMinInterval()).isNull();
            EntityState state = persisted(profile, observe(profile, eventRecord(T0).build()));
            // Same content, same observed_at: Δt = 0 still passes edgeMinInterval = 0.
            Observation obs = observe(profile, eventRecord(T0).build(), at(1));
            assertThat(decide(profile, state, obs, true)).isEqualTo(persist(PersistReason.TRIGGER_EDGE));
        }
    }

    @Test
    @DisplayName("Decision insists on a reason exactly for PERSIST")
    void decisionShape() {
        assertThat(Decision.persist(PersistReason.FIRST).persists()).isTrue();
        assertThat(Decision.of(Outcome.LIVE).persists()).isFalse();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new Decision(Outcome.PERSIST, null))
                .isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new Decision(Outcome.LIVE, PersistReason.FIRST))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("persist reason codes are the stored values 1..9")
    void persistReasonCodes() {
        assertThat(PersistReason.FIRST.code()).isEqualTo((short) 1);
        assertThat(PersistReason.TRIGGER_EDGE.code()).isEqualTo((short) 9);
        for (PersistReason reason : PersistReason.values()) {
            assertThat(PersistReason.fromCode(reason.code())).isSameAs(reason);
        }
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> PersistReason.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
