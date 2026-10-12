package ai.foresignal.testsupport.trigger;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.trigger.RuleStormEvent;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

/**
 * The trigger test context (prompt 017): the read-API sources of {@link RecordsTestSources} (which
 * bring the ingest sources, the {@code MutableClock} and {@code TestRecords}), plus
 *
 * <ul>
 *   <li>{@value #SAMPLED}: SAMPLED with no distance test, no thresholds, a 1 h keyframe, {@code edgeMinInterval} 0 and
 *       significant {@code name} only, so a moving entity persists nothing between its first version
 *       and a keyframe unless the persist gate forces a {@code TRIGGER_EDGE};
 *   <li>{@value #EDGE}: the same with {@code edgeMinInterval} and {@code minInterval} 5 min, for flapping;
 *   <li>{@value #VOLATILE}: ALL with volatile {@code fetchedAt}.
 * </ul>
 *
 * <p>Every trigger integration test imports this one configuration, so they share one context.
 */
@TestConfiguration
@Import(RecordsTestSources.class)
public class TriggerTestSources {

    /** SAMPLED, gate-driven. */
    public static final String SAMPLED = "test-trigger-sampled";

    /** The kind of {@value #SAMPLED}. */
    public static final String SAMPLED_KIND = "test-vessel";

    /** SAMPLED with edgeMinInterval 5 min. */
    public static final String EDGE = "test-trigger-edge";

    /** The kind of {@value #EDGE}. */
    public static final String EDGE_KIND = "test-gauge";

    /** The edge interval of {@value #EDGE}. */
    public static final Duration EDGE_MIN_INTERVAL = Duration.ofMinutes(5);

    /** ALL with a volatile field. */
    public static final String VOLATILE = "test-trigger-volatile";

    /** The kind of {@value #VOLATILE}. */
    public static final String VOLATILE_KIND = "test-reading";

    /** {@value #SAMPLED}. */
    @Bean
    public TestPushAdapter testTriggerSampledAdapter() {
        return new TestPushAdapter(base(SAMPLED, SAMPLED_KIND)
                .significantFields("name")
                // edgeMinInterval is explicitly 0: left null it defaults to minInterval / 2 = 30 s
                // (Core-Data-Model §5.1), which held every edge of the 10 s steps these tests take
                // (prompt 022). EDGE below is the source that exercises a non-zero interval.
                .defaultIngestPolicy(new IngestPolicy.Sampled(
                        Duration.ofSeconds(60), Duration.ofHours(1), null, false, Duration.ZERO))
                .build());
    }

    /** {@value #EDGE}. */
    @Bean
    public TestPushAdapter testTriggerEdgeAdapter() {
        return new TestPushAdapter(base(EDGE, EDGE_KIND)
                .significantFields("name")
                // Core-Data-Model §5.1 requires edgeMinInterval <= minInterval, so minInterval is the
                // edge interval itself (it was 60 s, which the registry rejects at start).
                .defaultIngestPolicy(new IngestPolicy.Sampled(
                        EDGE_MIN_INTERVAL, Duration.ofHours(1), null, false, EDGE_MIN_INTERVAL))
                .build());
    }

    /** {@value #VOLATILE}. */
    @Bean
    public TestPushAdapter testTriggerVolatileAdapter() {
        return new TestPushAdapter(base(VOLATILE, VOLATILE_KIND).volatileFields("fetchedAt").build());
    }

    /** Records every {@link RuleStormEvent}, from whichever thread publishes it. */
    @Bean
    public StormRecorder stormRecorder() {
        return new StormRecorder();
    }

    /** Collects storm events for assertions; {@link #clear} between tests. */
    public static final class StormRecorder {

        private final List<RuleStormEvent> events = new CopyOnWriteArrayList<>();

        /** Records one event. */
        @EventListener
        public void on(RuleStormEvent event) {
            events.add(event);
        }

        /** The events so far. */
        public List<RuleStormEvent> events() {
            return List.copyOf(events);
        }

        /** Forgets them. */
        public void clear() {
            events.clear();
        }
    }

    private static SourceDescriptor.Builder base(String id, String kind) {
        return SourceDescriptor.builder(id)
                .name("Trigger test source " + id)
                .attribution("Attribution of " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }
}
