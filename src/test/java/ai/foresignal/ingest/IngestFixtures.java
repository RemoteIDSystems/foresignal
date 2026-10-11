package ai.foresignal.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Shared descriptors, profiles and observations for the ingest core tests.
 *
 * <p>Three source shapes:
 *
 * <ul>
 *   <li><strong>aircraft</strong>: TRACK, TTL 5 min, poll 10 s, SAMPLED (minInterval 60 s,
 *       keyframe 240 s, minDeviation 1,000 m, thresholds on, edgeMinInterval 30 s), significant
 *       {@code flight, squawk}, motion {@code gs} (knots) / {@code track}, thresholds {@code alt}
 *       1,000 and {@code track} 20°. touchMin 30 s; window 3 d.
 *   <li><strong>events</strong>: STANDARD, TTL 10 min, poll 60 s, ALL (default keyframe
 *       {@code 10m − 60s − 30s = 510 s}), RECEIVED anchor. touchMin 60 s; window 7 d.
 *   <li><strong>catalog</strong>: STANDARD, no TTL, ALL with keyframe none. touchMin 1 h; window
 *       7 d, so the retention keyframe is 42 h.
 * </ul>
 */
final class IngestFixtures {

    static final Instant T0 = Instant.parse("2026-10-11T12:00:00Z");

    static final double LON = -122.0;
    static final double LAT = 37.0;

    private IngestFixtures() {}

    static Instant at(long seconds) {
        return T0.plusSeconds(seconds);
    }

    static IngestPolicy.Sampled aircraftPolicy() {
        return new IngestPolicy.Sampled(
                Duration.ofSeconds(60), Duration.ofSeconds(240), 1000.0, true, Duration.ofSeconds(30));
    }

    static SourceDescriptor.Builder aircraftDescriptor() {
        return SourceDescriptor.builder("test-aircraft")
                .kind("aircraft")
                .historyTier(HistoryTier.TRACK)
                .interval(Duration.ofSeconds(5), Duration.ofSeconds(10))
                .defaultBudget(60, 10_000)
                .recordTtl(Duration.ofMinutes(5))
                .significantFields("flight", "squawk")
                .motion(new MotionSpec("gs", MotionSpec.SpeedUnit.KNOTS, "track"))
                .threshold(FieldThreshold.of("alt", 1000))
                .threshold(FieldThreshold.angular("track", 20))
                .searchFields("flight")
                .defaultIngestPolicy(aircraftPolicy());
    }

    static SourceIngestProfile aircraft() {
        return SourceIngestProfile.forDescriptor(aircraftDescriptor().build());
    }

    static SourceIngestProfile aircraftWith(IngestPolicy policy) {
        return SourceIngestProfile.forDescriptor(aircraftDescriptor().defaultIngestPolicy(policy).build());
    }

    static SourceDescriptor.Builder eventsDescriptor() {
        return SourceDescriptor.builder("test-events")
                .kind("earthquake")
                .interval(Duration.ofSeconds(30), Duration.ofSeconds(60))
                .defaultBudget(60, 10_000)
                .recordTtl(Duration.ofMinutes(10));
    }

    static SourceIngestProfile events() {
        return SourceIngestProfile.forDescriptor(eventsDescriptor().build());
    }

    static SourceDescriptor.Builder catalogDescriptor() {
        return SourceDescriptor.builder("test-catalog")
                .kind("plant")
                .interval(Duration.ofSeconds(30), Duration.ofSeconds(60))
                .defaultBudget(60, 10_000);
    }

    static SourceIngestProfile catalog() {
        return SourceIngestProfile.forDescriptor(catalogDescriptor().build());
    }

    /** An aircraft record at (LON, LAT) with gs 0, track 90, alt 30,000, squawk 1200. */
    static NormalizedRecord.Builder aircraftRecord(Instant observedAt) {
        return NormalizedRecord.builder("a1b2c3", "aircraft")
                .observedAt(observedAt)
                .point(LON, LAT)
                .put("flight", "UAL1")
                .put("squawk", 1200L)
                .put("gs", 0.0)
                .put("track", 90.0)
                .put("alt", 30_000L);
    }

    /** An earthquake record at (LON, LAT). */
    static NormalizedRecord.Builder eventRecord(Instant observedAt) {
        return NormalizedRecord.builder("ev1", "earthquake")
                .observedAt(observedAt)
                .point(LON, LAT)
                .put("mag", 4.5)
                .put("place", "Somewhere");
    }

    /** A catalog record at (LON, LAT). */
    static NormalizedRecord.Builder catalogRecord(Instant observedAt) {
        return NormalizedRecord.builder("p1", "plant")
                .observedAt(observedAt)
                .point(LON, LAT)
                .put("name", "Plant One");
    }

    /** Normalizes one record received at {@code receivedAt}, asserting it is accepted. */
    static Observation observe(SourceIngestProfile profile, NormalizedRecord record, Instant receivedAt) {
        NormalizedBatch batch =
                new Normalizer().normalize(profile, List.of(new IngestRecord(record, receivedAt, 0)));
        assertThat(batch.rejections()).as("rejections").isEmpty();
        return batch.observations().getFirst();
    }

    /** Normalizes a record received when it was observed. */
    static Observation observe(SourceIngestProfile profile, NormalizedRecord record) {
        return observe(profile, record, record.observedAt());
    }

    static PreparedObservation prepare(SourceIngestProfile profile, Observation observation, EntityState state) {
        return IngestPolicyEngine.prepare(observation, state, profile);
    }

    /** The state after persisting {@code observation} as the entity's first version. */
    static EntityState persisted(SourceIngestProfile profile, Observation observation) {
        EntityState absent = EntityState.absent();
        return absent.afterPersist(prepare(profile, observation, absent), profile, 1);
    }

    /** Prepares and decides in one step. */
    static Decision decide(SourceIngestProfile profile, EntityState state, Observation observation, boolean gateFlip) {
        return IngestPolicyEngine.decide(prepare(profile, observation, state), state, profile, gateFlip);
    }
}
