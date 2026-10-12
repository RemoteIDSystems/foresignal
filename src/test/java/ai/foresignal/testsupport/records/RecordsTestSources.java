package ai.foresignal.testsupport.records;

import ai.foresignal.ingest.SourceIngestProfiles;
import ai.foresignal.retention.DefaultRetentionState;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * The read-API test context (prompt 014): prompt 010's collector sources and prompt 009's ingest
 * sources, plus
 *
 * <ul>
 *   <li>{@value #PRIVATE}: a private STANDARD source (kind {@value #PRIVATE_KIND}), hidden from
 *       anonymous reads;
 *   <li>{@value #RASTER}: STANDARD, kind {@code raster-frame}, whose changes always carry payloads;
 *   <li>{@value #SCHEMA_A} and {@value #SCHEMA_B}: both emit {@value #SCHEMA_KIND}; they agree on
 *       {@code depth} and disagree on the type of {@code mag}; {@value #SCHEMA_A} declares a
 *       {@code kindDisplay} and a {@code titleField};
 *   <li>a {@link StubRetentionState} as the primary {@link RetentionState}, so tests can set a purge
 *       seq and a window.
 * </ul>
 *
 * <p>Every read-API integration test imports this one configuration, so they share one context.
 */
@TestConfiguration
@Import(CollectorTestSources.class)
public class RecordsTestSources {

    /** Private source. */
    public static final String PRIVATE = "test-private";

    /** Kind of {@value #PRIVATE}. */
    public static final String PRIVATE_KIND = "test-secret";

    /** raster-frame source. */
    public static final String RASTER = "test-raster";

    /** First schema source. */
    public static final String SCHEMA_A = "test-schema-a";

    /** Second schema source. */
    public static final String SCHEMA_B = "test-schema-b";

    /** The kind both schema sources emit. */
    public static final String SCHEMA_KIND = "test-quake";

    /** A second kind of {@value #SCHEMA_B}, with its own field. */
    public static final String OTHER_KIND = "test-other";

    /** The display block {@value #SCHEMA_A} declares. */
    public static final KindDisplay SCHEMA_DISPLAY = new KindDisplay("M {mag}", "earthquake", "#D9480F", null);

    /** {@value #PRIVATE}. */
    @Bean
    public TestPushAdapter testPrivateAdapter() {
        return new TestPushAdapter(base(PRIVATE, PRIVATE_KIND).privateSource(true).build());
    }

    /** {@value #RASTER}. */
    @Bean
    public TestPushAdapter testRasterAdapter() {
        return new TestPushAdapter(base(RASTER, "raster-frame").build());
    }

    /** {@value #SCHEMA_A}. */
    @Bean
    public TestPushAdapter testSchemaAAdapter() {
        return new TestPushAdapter(base(SCHEMA_A, SCHEMA_KIND)
                .titleField("place")
                .fieldSpec("mag", new FieldSpec(FieldType.NUMBER, "1", "Magnitude", "Event magnitude", null, Set.of()))
                .fieldSpec("depth", new FieldSpec(FieldType.NUMBER, "km", "Depth", null, "WGS84", Set.of()))
                .fieldSpec("time", new FieldSpec(FieldType.TIMESTAMP, "ms", null, null, null, Set.of()))
                .kindDisplay(SCHEMA_KIND, SCHEMA_DISPLAY)
                .build());
    }

    /** {@value #SCHEMA_B}. */
    @Bean
    public TestPushAdapter testSchemaBAdapter() {
        return new TestPushAdapter(base(SCHEMA_B, SCHEMA_KIND)
                .kinds(SCHEMA_KIND, OTHER_KIND)
                .fieldSpec("mag", new FieldSpec(FieldType.STRING, null, "Magnitude text", null, null, Set.of()))
                .fieldSpec("depth", new FieldSpec(FieldType.NUMBER, "km", "Depth", null, "WGS84", Set.of()))
                .fieldSpec("other", new FieldSpec(FieldType.BOOLEAN, null, null, null, null, Set.of(OTHER_KIND)))
                .build());
    }

    /** The settable retention state. */
    @Bean
    @Primary
    public StubRetentionState stubRetentionState(SourceIngestProfiles profiles) {
        return new StubRetentionState(new DefaultRetentionState(profiles));
    }

    private static SourceDescriptor.Builder base(String id, String kind) {
        return SourceDescriptor.builder(id)
                .name("Records test source " + id)
                .attribution("Attribution of " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }

    /** A {@link RetentionState} whose purge seqs and windows tests may override; {@link #reset} clears them. */
    public static final class StubRetentionState implements RetentionState {

        private final RetentionState delegate;
        private final Map<String, Long> purgeSeqs = new ConcurrentHashMap<>();
        private final Map<String, Duration> windows = new ConcurrentHashMap<>();
        private volatile long lowestRetainedSeq = -1;

        StubRetentionState(RetentionState delegate) {
            this.delegate = delegate;
        }

        /** Sets the purge seq of {@code sourceId}. */
        public void setPurgeSeq(String sourceId, long seq) {
            purgeSeqs.put(sourceId, seq);
        }

        /** Overrides the window of {@code sourceId}. */
        public void setWindow(String sourceId, Duration window) {
            windows.put(sourceId, window);
        }

        /** Overrides {@link #lowestRetainedSeq} for every source set (prompt 015's resume floor). */
        public void setLowestRetainedSeq(long seq) {
            lowestRetainedSeq = seq;
        }

        /** Clears every override. */
        public void reset() {
            purgeSeqs.clear();
            windows.clear();
            lowestRetainedSeq = -1;
        }

        @Override
        public long purgeSeq(String sourceId) {
            return purgeSeqs.getOrDefault(sourceId, delegate.purgeSeq(sourceId));
        }

        @Override
        public long lowestRetainedSeq(Set<String> sources) {
            long override = lowestRetainedSeq;
            return override >= 0 ? override : delegate.lowestRetainedSeq(sources);
        }

        @Override
        public Duration window(String sourceId) {
            Duration window = windows.get(sourceId);
            return window != null ? window : delegate.window(sourceId);
        }
    }
}
