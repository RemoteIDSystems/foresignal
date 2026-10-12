package ai.foresignal.testsupport.timetravel;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.HistoryFidelity;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.tiles.TileTestSources;
import java.time.Duration;
import java.time.Instant;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * The time-travel test context (prompt 037): the tile sources of {@link TileTestSources} (and so
 * the read-API and ingest sources), plus
 *
 * <ul>
 *   <li>{@value #MOVER}: TRACK, policy ALL (every move is a version), TTL 5 min, kind
 *       {@value #VEHICLE}, title from {@code callsign}, tile properties {@code alt} and {@code gs};
 *   <li>{@value #MOVER_PRIVATE}: the same, private;
 *   <li>{@value #ZONE}: STANDARD without a TTL, kind {@value #ZONE_KIND}, tile property
 *       {@code level} (long-valid polygons);
 *   <li>a settable {@link StubHistoryFidelity} as the primary {@link HistoryFidelity}.
 * </ul>
 *
 * <p>Every time-travel integration test imports this one configuration, so they share one context.
 */
@TestConfiguration
@Import(TileTestSources.class)
public class TimeTravelTestSources {

    /** TRACK mover. */
    public static final String MOVER = "tt-mover";

    /** Private TRACK mover. */
    public static final String MOVER_PRIVATE = "tt-mover-private";

    /** STANDARD zones. */
    public static final String ZONE = "tt-zone";

    /** Kind of the movers. */
    public static final String VEHICLE = "tt-vehicle";

    /** Kind of the zones. */
    public static final String ZONE_KIND = "tt-zone-area";

    /** {@value #MOVER}. */
    @Bean
    public TestPushAdapter ttMoverAdapter() {
        return new TestPushAdapter(mover(MOVER).build());
    }

    /** {@value #MOVER_PRIVATE}. */
    @Bean
    public TestPushAdapter ttMoverPrivateAdapter() {
        return new TestPushAdapter(mover(MOVER_PRIVATE).privateSource(true).build());
    }

    /** {@value #ZONE}. */
    @Bean
    public TestPushAdapter ttZoneAdapter() {
        return new TestPushAdapter(base(ZONE, ZONE_KIND).tileProperties("level").titleField("name").build());
    }

    /** The settable fidelity. */
    @Bean
    @Primary
    public StubHistoryFidelity stubHistoryFidelity() {
        return new StubHistoryFidelity();
    }

    private static SourceDescriptor.Builder mover(String id) {
        return base(id, VEHICLE)
                .historyTier(HistoryTier.TRACK)
                .recordTtl(Duration.ofMinutes(5))
                .titleField("callsign")
                .tileProperties("alt", "gs");
    }

    private static SourceDescriptor.Builder base(String id, String kind) {
        return SourceDescriptor.builder(id)
                .name("Time travel test source " + id)
                .attribution("Attribution of " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }

    /** A {@link HistoryFidelity} tests can steer; {@link #reset} restores raw history. */
    public static final class StubHistoryFidelity implements HistoryFidelity {

        private volatile boolean downsampled;
        private volatile Duration widening = Duration.ZERO;
        private volatile Instant finalBefore = Instant.MAX;

        /** Reports every range as downsampled, or none. */
        public void setDownsampled(boolean value) {
            downsampled = value;
        }

        /** Sets the validity widening of every source. */
        public void setWidening(Duration value) {
            widening = value;
        }

        /** Sets the instant before which history is final. */
        public void setFinalBefore(Instant value) {
            finalBefore = value;
        }

        /** Raw history again. */
        public void reset() {
            downsampled = false;
            widening = Duration.ZERO;
            finalBefore = Instant.MAX;
        }

        @Override
        public boolean downsampled(HistoryTier tier, long fromSeq, long toSeq) {
            return downsampled;
        }

        @Override
        public Duration validityWidening(String sourceId) {
            return widening;
        }

        @Override
        public Instant finalBefore(HistoryTier tier) {
            return finalBefore;
        }
    }
}
