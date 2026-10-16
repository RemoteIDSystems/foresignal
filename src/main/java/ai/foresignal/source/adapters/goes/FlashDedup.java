package ai.foresignal.source.adapters.goes;

import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.FlashSink;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * De-duplicates a {@code glm-local} GRB station's flashes against the shared ring (prompt 107): a flash
 * is dropped when a ring flash lies within ±{@value #TIME_MS} ms and ±{@value #POS_DEG}° of it. The ring
 * already holds the internet {@code goes-glm} flashes, so this drops the local copy of a flash the
 * internet feed saw first. Pure.
 */
final class FlashDedup {

    /** The time tolerance, milliseconds. */
    static final long TIME_MS = 1000;

    /** The position tolerance, degrees. */
    static final double POS_DEG = 0.05;

    private FlashDedup() {}

    /** The flashes of {@code batch} not already in the ring. */
    static FlashBatch dedup(FlashSink ring, FlashBatch batch) {
        if (batch.isEmpty()) {
            return batch;
        }
        List<Integer> keep = new ArrayList<>();
        for (int i = 0; i < batch.size(); i++) {
            if (!duplicate(ring, batch.timesMs()[i], batch.lat()[i], batch.lon()[i])) {
                keep.add(i);
            }
        }
        if (keep.size() == batch.size()) {
            return batch;
        }
        int[] indices = new int[keep.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = keep.get(i);
        }
        return batch.select(indices);
    }

    /** True when the ring already has a flash within the tolerances. */
    static boolean duplicate(FlashSink ring, long timeMs, float lat, float lon) {
        FlashBatch window = ring.between(Instant.ofEpochMilli(timeMs - TIME_MS), Instant.ofEpochMilli(timeMs + TIME_MS));
        for (int i = 0; i < window.size(); i++) {
            if (Math.abs(window.lat()[i] - lat) <= POS_DEG && Math.abs(window.lon()[i] - lon) <= POS_DEG) {
                return true;
            }
        }
        return false;
    }
}
