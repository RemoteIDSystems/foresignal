package ai.foresignal.source.adapters.goes;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.FlashSink;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalDouble;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/** The local-vs-internet flash dedup (prompt 107). */
class FlashDedupTest {

    /** A ring holding one internet flash. */
    private static final FlashBatch RING = new FlashBatch(new long[] {1_000_000}, new float[] {32.50f},
            new float[] {-97.30f}, new float[] {10f});

    private static FlashSink ring() {
        return new FlashSink() {
            @Override
            public void append(String satellite, FlashBatch flashes, Instant fileTime) {}

            @Override
            public FlashBatch between(Instant from, Instant to) {
                long f = from.toEpochMilli();
                long t = to.toEpochMilli();
                if (RING.timesMs()[0] >= f && RING.timesMs()[0] <= t) {
                    return RING;
                }
                return FlashBatch.EMPTY;
            }

            @Override
            public FlashBatch flashesWithin(Point point, double radiusKm, Instant since) {
                return FlashBatch.EMPTY;
            }

            @Override
            public int countWithin(Geometry geometry, Instant since) {
                return 0;
            }

            @Override
            public OptionalDouble nearestKm(Point point, Instant since) {
                return OptionalDouble.empty();
            }

            @Override
            public Optional<Instant> newestFlashFileTime() {
                return Optional.empty();
            }
        };
    }

    @Test
    @DisplayName("a local flash within 1 s and 0.05° is dropped")
    void dropsDuplicate() {
        FlashBatch local = new FlashBatch(new long[] {1_000_500}, new float[] {32.51f}, new float[] {-97.29f},
                new float[] {10f});
        assertThat(FlashDedup.dedup(ring(), local).size()).isZero();
    }

    @Test
    @DisplayName("a local flash that is far away is kept")
    void keepsDistinct() {
        FlashBatch local = new FlashBatch(new long[] {1_000_500}, new float[] {40.0f}, new float[] {-100.0f},
                new float[] {10f});
        assertThat(FlashDedup.dedup(ring(), local).size()).isEqualTo(1);
    }
}
