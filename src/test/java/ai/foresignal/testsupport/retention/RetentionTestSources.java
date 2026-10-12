package ai.foresignal.testsupport.retention;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import java.time.Duration;
import java.util.OptionalLong;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The shared context of prompt 019's integration tests: prompt 010's collector sources (including
 * {@code collect-keyed}, which needs a credential) and prompt 009's ingest sources, plus
 *
 * <ul>
 *   <li>{@value #KINDS}: STANDARD, kinds {@value #SHORT_KIND} (descriptor default 7d) and
 *       {@value #LONG_KIND} ({@code kindRetention} 365d);
 *   <li>a settable {@link TestConsumer} registered with the consumer clamp (empty, so no clamp, by
 *       default);
 *   <li>a {@link BlockingStep} that holds a run open while armed, for the concurrent-run test.
 * </ul>
 *
 * <p>Every 019 IT imports exactly this, so they share one cached context.
 */
@TestConfiguration
@Import(CollectorTestSources.class)
public class RetentionTestSources {

    /** The per-kind source. */
    public static final String KINDS = "test-ret-kinds";

    /** Kind of {@value #KINDS} with the descriptor's 7d default. */
    public static final String SHORT_KIND = "ret-short";

    /** Kind of {@value #KINDS} with a 365d {@code kindRetention}. */
    public static final String LONG_KIND = "ret-long";

    /** {@value #KINDS}. */
    @Bean
    public TestPushAdapter testRetKindsAdapter() {
        return new TestPushAdapter(SourceDescriptor.builder(KINDS)
                .name("Retention kinds test source")
                .kinds(SHORT_KIND, LONG_KIND)
                .mode(Mode.PUSH)
                .historyTier(HistoryTier.STANDARD)
                .defaultBudget(Budget.of(60, 10_000))
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(LONG_KIND, Duration.ofDays(365))
                .build());
    }

    /** The settable consumer. */
    @Bean
    public TestConsumer testRetentionConsumer() {
        return new TestConsumer();
    }

    /** The blocking step. */
    @Bean
    public BlockingStep blockingRetentionStep() {
        return new BlockingStep();
    }

    /** A {@link RetentionConsumer} whose cursor a test sets; empty (no clamp) until then. */
    public static final class TestConsumer implements RetentionConsumer {

        /** Its name. */
        public static final String NAME = "test.cursor";

        private volatile OptionalLong cursor = OptionalLong.empty();

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public OptionalLong cursorSeq() {
            return cursor;
        }

        /** Sets the cursor. */
        public void set(long seq) {
            cursor = OptionalLong.of(seq);
        }

        /** Clears the cursor (no clamp). */
        public void clear() {
            cursor = OptionalLong.empty();
        }
    }

    /** A first step that, while armed, signals it started and waits for release. */
    public static final class BlockingStep implements RetentionStep {

        private volatile CountDownLatch started;
        private volatile CountDownLatch release;

        @Override
        public String name() {
            return "test-blocking";
        }

        @Override
        public int order() {
            return 1;
        }

        @Override
        public void run(RetentionRun run) {
            CountDownLatch gate = release;
            if (gate == null) {
                return;
            }
            started.countDown();
            try {
                gate.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /** Arms the step: the next run blocks until {@link #release()}. */
        public void arm() {
            started = new CountDownLatch(1);
            release = new CountDownLatch(1);
        }

        /** Waits until an armed run has started. */
        public boolean awaitStarted() throws InterruptedException {
            return started.await(30, TimeUnit.SECONDS);
        }

        /** Releases and disarms. */
        public void release() {
            CountDownLatch gate = release;
            release = null;
            if (gate != null) {
                gate.countDown();
            }
        }
    }
}
