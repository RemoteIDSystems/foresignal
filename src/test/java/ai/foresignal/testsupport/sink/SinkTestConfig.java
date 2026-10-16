package ai.foresignal.testsupport.sink;

import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkDeliveryException;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkKind;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.sink.SinkTestResult;
import ai.foresignal.sink.SinkType;
import ai.foresignal.sink.SinkTypeSpec;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The outbound-sink integration test context (prompt 110): two licensed test sources and two recording sink
 * types, so the supervisor's cursor, gate, resync and backoff behavior can be observed without a broker or a
 * network target.
 *
 * <ul>
 *   <li>{@value #OPEN}: a STANDARD source under {@code CC BY 4.0} (redistributable), kind {@value #KIND};
 *   <li>{@value #FORBIDDEN}: a STANDARD source under the OpenSky terms ({@code FORBIDDEN}), kind {@value #KIND};
 *   <li>{@value #FEED_TYPE}: a CHANGE_FEED PUSH type that records every delivery in {@link #FEED} and can be told
 *       to refuse;
 *   <li>{@value #MIRROR_TYPE}: a STATE_MIRROR SERVE type that records upserts, removals and resyncs in
 *       {@link #MIRROR}.
 * </ul>
 *
 * <p>Every sink integration test imports this one configuration, so they share one context.
 */
@TestConfiguration
@Import(RecordsTestSources.class)
public class SinkTestConfig {

    /** A redistributable source. */
    public static final String OPEN = "test-sink-open";

    /** A source whose license forbids redistribution. */
    public static final String FORBIDDEN = "test-sink-forbidden";

    /** The kind both sources emit. */
    public static final String KIND = "test-sink-point";

    /** The recording CHANGE_FEED type. */
    public static final String FEED_TYPE = "test-feed";

    /** The recording STATE_MIRROR type. */
    public static final String MIRROR_TYPE = "test-mirror";

    /** What the recording CHANGE_FEED type received. */
    public static final Recorder FEED = new Recorder();

    /** What the recording STATE_MIRROR type received. */
    public static final Recorder MIRROR = new Recorder();

    /** Every {@code system.*} notification event published while the sink tests run. */
    public static final List<ai.foresignal.notify.SystemEvent> SYSTEM_EVENTS = new CopyOnWriteArrayList<>();

    /** Collects {@link ai.foresignal.notify.SystemEvent}s into {@link #SYSTEM_EVENTS}. */
    @Bean
    public SystemEventCollector sinkSystemEventCollector() {
        return new SystemEventCollector();
    }

    /** The collector bean. */
    public static final class SystemEventCollector {
        /** Records one event. */
        @org.springframework.context.event.EventListener
        public void on(ai.foresignal.notify.SystemEvent event) {
            SYSTEM_EVENTS.add(event);
        }
    }

    /** {@value #OPEN}. */
    @Bean
    public TestPushAdapter testSinkOpenAdapter() {
        return new TestPushAdapter(base(OPEN, "CC BY 4.0").build());
    }

    /** {@value #FORBIDDEN}. */
    @Bean
    public TestPushAdapter testSinkForbiddenAdapter() {
        return new TestPushAdapter(base(FORBIDDEN, "OpenSky Network terms (non-commercial research)").build());
    }

    /** {@value #FEED_TYPE}. */
    @Bean
    public SinkType testFeedSinkType() {
        return new RecordingType(FEED_TYPE, SinkKind.CHANGE_FEED, SinkMode.PUSH, FEED);
    }

    /** {@value #MIRROR_TYPE}. */
    @Bean
    public SinkType testMirrorSinkType() {
        return new RecordingType(MIRROR_TYPE, SinkKind.STATE_MIRROR, SinkMode.SERVE, MIRROR);
    }

    private static SourceDescriptor.Builder base(String id, String licence) {
        return SourceDescriptor.builder(id)
                .name("Sink test source " + id)
                .attribution("Attribution of " + id)
                .licence(licence)
                .kind(KIND)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }

    /** The shared record of one recording type; {@link #reset} between tests. */
    public static final class Recorder {
        /** Every acknowledged CHANGE_FEED delivery and every mirror upsert, in order. */
        public final List<SinkRecord> received = new CopyOnWriteArrayList<>();
        /** Every mirror removal as {@code sourceId|externalId}. */
        public final List<String> removed = new CopyOnWriteArrayList<>();
        /** Mirror resyncs. */
        public final AtomicInteger resyncs = new AtomicInteger();
        /** Instances opened. */
        public final AtomicInteger opens = new AtomicInteger();
        /** While true, deliveries throw a retryable {@link SinkDeliveryException}. */
        public final AtomicBoolean refuse = new AtomicBoolean();
        /** The delivery of this external id throws once (a crash mid-page); null for none. */
        public final java.util.concurrent.atomic.AtomicReference<String> failOnceAt =
                new java.util.concurrent.atomic.AtomicReference<>();
        /** While true, opening the instance throws (a target that is down). */
        public final AtomicBoolean failOpen = new AtomicBoolean();

        /** Clears everything. */
        public void reset() {
            received.clear();
            removed.clear();
            resyncs.set(0);
            opens.set(0);
            refuse.set(false);
            failOnceAt.set(null);
            failOpen.set(false);
        }

        /** The external ids received, in order. */
        public List<String> externalIds() {
            return received.stream().map(SinkRecord::externalId).toList();
        }
    }

    /** A sink type that records what reaches it. */
    static final class RecordingType implements SinkType {
        private final SinkTypeSpec spec;
        private final Recorder recorder;

        RecordingType(String id, SinkKind kind, SinkMode mode, Recorder recorder) {
            this.spec = new SinkTypeSpec(id, id, "test", kind, Set.of(mode), Map.of("type", "object"), List.of(),
                    mode == SinkMode.PUSH ? 50 : null);
            this.recorder = recorder;
        }

        @Override
        public String id() {
            return spec.id();
        }

        @Override
        public SinkTypeSpec spec() {
            return spec;
        }

        @Override
        public SinkInstance open(SinkConfig config) {
            recorder.opens.incrementAndGet();
            if (recorder.failOpen.get()) {
                throw new IllegalStateException("the test target is down");
            }
            return new SinkInstance() {
                @Override
                public boolean deliver(SinkRecord record) {
                    if (recorder.refuse.get()) {
                        throw new SinkDeliveryException("refused by the test", 503, Duration.ofSeconds(1), false);
                    }
                    if (record.externalId().equals(recorder.failOnceAt.get())) {
                        recorder.failOnceAt.set(null);
                        throw new SinkDeliveryException("crash mid-page", null, null, false);
                    }
                    recorder.received.add(record);
                    return true;
                }

                @Override
                public void upsert(SinkRecord record) {
                    recorder.received.add(record);
                }

                @Override
                public void remove(String sourceId, String externalId) {
                    recorder.removed.add(sourceId + "|" + externalId);
                }

                @Override
                public void resync() {
                    recorder.resyncs.incrementAndGet();
                }

                @Override
                public SinkTestResult test(SinkRecord probe) {
                    return new SinkTestResult(true, "send", "recorded", 0L, Map.of());
                }
            };
        }
    }
}
