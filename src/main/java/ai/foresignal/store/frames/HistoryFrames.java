package ai.foresignal.store.frames;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * A keyframe plus delta frames (the {@code HistoryFrames} schema). Applying frames {@code 1..i} to
 * the keyframe gives exactly the asOf state at {@code frames[i].t}.
 *
 * @param asOf the keyframe instant ({@code from})
 * @param step the frame step
 * @param watermarkSeq {@code S(to)}
 * @param horizon the retention horizon of the involved sources, or null
 * @param approximate a sampled source or a downsampled range is involved
 * @param keyframe the asOf state at {@code from}, ordered by id
 * @param frames the frames in time order
 */
public record HistoryFrames(Instant asOf, Duration step, long watermarkSeq, Instant horizon, boolean approximate,
        List<FrameItem> keyframe, List<Frame> frames) {

    /**
     * One frame: the changes in {@code (t_{i−1}, t_i]}.
     *
     * @param t the frame's end
     * @param upserts the newest version of each entity that appeared or changed, ordered by id
     * @param removals entities that left the state, ordered by id
     */
    public record Frame(Instant t, List<FrameItem> upserts, List<Removal> removals) {}

    /**
     * An entity leaving the state.
     *
     * @param id {@code <sourceId>:<externalId>}
     * @param reason {@code tombstone}, {@code expired} or {@code left} (left the bbox or filter)
     */
    public record Removal(String id, String reason) {

        /** True when the entity left the bbox or filter. */
        public boolean left() {
            return REASON_LEFT.equals(reason);
        }
    }

    /** Removal reason: a tombstone. */
    public static final String REASON_TOMBSTONE = "tombstone";

    /** Removal reason: validity ended inside the frame. */
    public static final String REASON_EXPIRED = "expired";

    /** Removal reason: the newest version lies outside the bbox or filter. */
    public static final String REASON_LEFT = "left";
}
