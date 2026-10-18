package ai.foresignal.chronicle;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Decides, per matching version, what a layer's capture mode writes (prompt 131). Pure apart from its own memory
 * ({@link State}), so {@code CaptureModeTest} drives it without a database:
 *
 * <ul>
 *   <li>{@code full}: insert every version;</li>
 *   <li>{@code downsampled}: insert an event version (FIRST, SIGNIFICANT, REAPPEAR, TOMBSTONE, TRIGGER_EDGE) always,
 *       and any other version once at least {@code interval} after the entity's last captured observation;</li>
 *   <li>{@code events-only}: insert event versions only;</li>
 *   <li>{@code latest-per-interval}: replace the entity's version in its {@code interval} bucket (epoch-aligned) with
 *       a newer one;</li>
 *   <li>{@code rollup-only}: count the version in its region and hour, write no record;</li>
 *   <li>{@code frames}: insert the first raster frame and every {@code everyNthFrame}-th after it.</li>
 * </ul>
 */
public final class CaptureDecider {

    /** What to do with one version. */
    public enum Action {
        /** Nothing. */
        SKIP,
        /** Insert the version. */
        INSERT,
        /** Replace the entity's version in the bucket. */
        REPLACE,
        /** Count it in the region-hour rollup. */
        ROLLUP
    }

    /**
     * A decision.
     *
     * @param action what to do
     * @param bucketStart the {@code latest-per-interval} bucket, or the rollup hour; else null
     */
    public record Decision(Action action, Instant bucketStart) {

        /** Skip. */
        public static final Decision SKIP = new Decision(Action.SKIP, null);

        /** Insert. */
        public static final Decision INSERT = new Decision(Action.INSERT, null);
    }

    /**
     * The decider's memory for one chronicle: the last captured observation per layer and entity (for
     * {@code downsampled}), and the frame counter per layer (for {@code frames}). A cache miss for an entity is filled
     * from storage by the capture loop before deciding, so a restart does not capture an extra sample.
     */
    public static final class State {

        private final Map<String, Instant> lastCaptured = new HashMap<>();
        private final Map<String, Long> frameCounters = new HashMap<>();

        /** The last captured observation of an entity in a layer, or null. */
        public Instant lastCaptured(String layerId, String entityKey) {
            return lastCaptured.get(layerId + '\u0001' + entityKey);
        }

        /** True when the state knows the entity's last capture in the layer (possibly that there was none). */
        public boolean knows(String layerId, String entityKey) {
            return lastCaptured.containsKey(layerId + '\u0001' + entityKey);
        }

        /** Records the entity's last captured observation (null: known to have none). */
        public void captured(String layerId, String entityKey, Instant observedAt) {
            lastCaptured.put(layerId + '\u0001' + entityKey, observedAt);
        }

        /** The frame counter of a layer (frames seen so far). */
        public long frames(String layerId) {
            return frameCounters.getOrDefault(layerId, 0L);
        }

        /** Sets the frame counter of a layer. */
        public void frames(String layerId, long value) {
            frameCounters.put(layerId, value);
        }

        /** Forgets everything (the memory is bounded by clearing it between large passes). */
        public void clear() {
            lastCaptured.clear();
        }

        /** How many entity entries are held. */
        public int size() {
            return lastCaptured.size();
        }
    }

    private CaptureDecider() {}

    /**
     * Decides what {@code mode} writes for {@code row} in {@code layerId}, updating {@code state} for a capture.
     *
     * @param mode the effective capture mode (after the cap fallback)
     * @param layerId the layer
     * @param row the matching version
     * @param state the chronicle's memory
     * @return the decision
     */
    public static Decision decide(CaptureMode mode, String layerId, CapturedRow row, State state) {
        boolean event = ChronicleVocabulary.EVENT_REASONS.contains(row.persistReason());
        return switch (mode.mode()) {
            case CaptureMode.FULL -> Decision.INSERT;
            case CaptureMode.EVENTS_ONLY -> event ? Decision.INSERT : Decision.SKIP;
            case CaptureMode.DOWNSAMPLED -> {
                Instant last = state.lastCaptured(layerId, row.entityKey());
                boolean due = last == null || !row.observedAt().isBefore(last.plus(mode.interval()));
                if (event || due) {
                    if (last == null || row.observedAt().isAfter(last)) {
                        state.captured(layerId, row.entityKey(), row.observedAt());
                    }
                    yield Decision.INSERT;
                }
                yield Decision.SKIP;
            }
            case CaptureMode.LATEST_PER_INTERVAL -> new Decision(Action.REPLACE, bucket(row.observedAt(),
                    mode.interval()));
            case CaptureMode.ROLLUP_ONLY -> new Decision(Action.ROLLUP, row.observedAt().truncatedTo(ChronoUnit.HOURS));
            case CaptureMode.FRAMES -> {
                if (!ChronicleVocabulary.RASTER_FRAME_KIND.equals(row.kind())) {
                    yield Decision.SKIP;
                }
                long seen = state.frames(layerId);
                state.frames(layerId, seen + 1);
                yield seen % mode.everyNthFrame() == 0 ? Decision.INSERT : Decision.SKIP;
            }
            default -> Decision.SKIP;
        };
    }

    /** The epoch-aligned start of the {@code interval} bucket holding {@code t}. */
    public static Instant bucket(Instant t, Duration interval) {
        long width = Math.max(1, interval.toMillis());
        long ms = t.toEpochMilli();
        return Instant.ofEpochMilli(Math.floorDiv(ms, width) * width);
    }
}
