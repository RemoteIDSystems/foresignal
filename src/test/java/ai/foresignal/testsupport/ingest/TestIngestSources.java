package ai.foresignal.testsupport.ingest;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.PushAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.FakeAdapters;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The ingest test sources, registered alongside prompt 004's fakes, plus a {@link MutableClock} and
 * the {@link TestRecords} helper (Core-Data-Model §17).
 *
 * <p>Every integration test that seeds records imports this one configuration, so all of them share
 * one cached Spring context:
 *
 * <ul>
 *   <li>{@value #STANDARD}: ALL, STANDARD, no TTL (keyframe none, touchMin 1 h);
 *   <li>{@value #TTL}: ALL, STANDARD, TTL 10 min anchored on RECEIVED (default keyframe
 *       {@code 10m − 2s − 30s = 568 s}, touchMin 60 s);
 *   <li>{@value #TRACK}: TRACK, SAMPLED (minInterval 60 s, keyframe 240 s, minDeviation 1,000 m,
 *       thresholds on), significant {@code callsign, squawk}, thresholds {@code alt} 1,000 and
 *       angular {@code track} 20°, motion {@code gs} in knots with course {@code track}, TTL 5 min
 *       (touchMin 30 s);
 *   <li>{@value #LATEST}: LATEST with keyframe none, significant {@code name}, no TTL;
 *   <li>{@value #DYNAMIC}: a PUSH source registered at runtime with {@link #registerDynamic}, so a
 *       test can re-register it with different volatile and significant fields.
 * </ul>
 *
 * <p>All are PUSH sources, so their effective poll interval is the 2 s stream interval and the
 * scheduler never touches them.
 */
@TestConfiguration
@Import({FakeAdapters.class, MutableClock.TestConfig.class})
public class TestIngestSources {

    /** ALL, STANDARD, no TTL. */
    public static final String STANDARD = "test-standard";

    /** ALL, STANDARD, TTL 10 min, RECEIVED anchor. */
    public static final String TTL = "test-ttl";

    /** SAMPLED TRACK. */
    public static final String TRACK = "test-track";

    /** LATEST, significant {@code name}. */
    public static final String LATEST = "test-latest";

    /** Registered at runtime by tests. */
    public static final String DYNAMIC = "test-dynamic";

    /** The kind of {@value #STANDARD}. */
    public static final String STANDARD_KIND = "test-record";

    /** The kind of {@value #TTL}. */
    public static final String TTL_KIND = "test-event";

    /** The kind of {@value #TRACK}. */
    public static final String TRACK_KIND = "aircraft";

    /** The kind of {@value #LATEST}. */
    public static final String LATEST_KIND = "test-latest";

    /** The kind of {@value #DYNAMIC}. */
    public static final String DYNAMIC_KIND = "test-dyn";

    /** A PUSH adapter around a fixed descriptor. */
    public record TestPushAdapter(SourceDescriptor descriptor) implements PushAdapter {}

    /** {@value #STANDARD}. */
    @Bean
    public TestPushAdapter testStandardAdapter() {
        return new TestPushAdapter(base(STANDARD, STANDARD_KIND).build());
    }

    /** {@value #TTL}. */
    @Bean
    public TestPushAdapter testTtlAdapter() {
        return new TestPushAdapter(base(TTL, TTL_KIND).recordTtl(Duration.ofMinutes(10)).build());
    }

    /** {@value #TRACK}. */
    @Bean
    public TestPushAdapter testTrackAdapter() {
        return new TestPushAdapter(
                base(TRACK, TRACK_KIND)
                        .historyTier(HistoryTier.TRACK)
                        .recordTtl(Duration.ofMinutes(5))
                        .significantFields("callsign", "squawk")
                        .threshold(FieldThreshold.of("alt", 1000))
                        .threshold(FieldThreshold.angular("track", 20))
                        .motion(new MotionSpec("gs", MotionSpec.SpeedUnit.KNOTS, "track"))
                        .defaultIngestPolicy(
                                new IngestPolicy.Sampled(
                                        Duration.ofSeconds(60), Duration.ofSeconds(240), 1000.0, true, null))
                        .build());
    }

    /** {@value #LATEST}. */
    @Bean
    public TestPushAdapter testLatestAdapter() {
        return new TestPushAdapter(
                base(LATEST, LATEST_KIND)
                        .significantFields("name")
                        .defaultIngestPolicy(IngestPolicy.latest(null))
                        .build());
    }

    /** The seeding helper. */
    @Bean
    public TestRecords testRecords(IngestQueue queue, MutableClock clock) {
        return new TestRecords(queue, clock);
    }

    /**
     * (Re-)registers {@value #DYNAMIC} with the given field lists: unregisters it first when it is
     * already registered, so the ingest profile cache sees a registry change.
     */
    public static SourceDescriptor registerDynamic(
            SourceRegistry registry, List<String> volatileFields, List<String> significantFields) {
        unregisterDynamic(registry);
        SourceDescriptor descriptor =
                base(DYNAMIC, DYNAMIC_KIND)
                        .volatileFields(volatileFields.toArray(String[]::new))
                        .significantFields(significantFields.toArray(String[]::new))
                        .build();
        registry.registerDynamic(descriptor);
        return descriptor;
    }

    /** Unregisters {@value #DYNAMIC} when it is registered. */
    public static void unregisterDynamic(SourceRegistry registry) {
        if (registry.contains(DYNAMIC)) {
            registry.unregisterDynamic(DYNAMIC);
        }
    }

    private static SourceDescriptor.Builder base(String id, String kind) {
        return SourceDescriptor.builder(id)
                .name("Ingest test source " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }
}
