package ai.foresignal.raster.sample;

import ai.foresignal.raster.RasterFrameRow;

/**
 * The frame a sample time resolved to (prompt 052), with the status that already follows from the
 * choice: {@code unavailable} (no usable frame, the reason in {@code text}) or {@code stale} (the frame
 * is older than {@code maxGap} relative to the requested time); null when the frame is current.
 *
 * @param frame the frame, or null when {@code unavailable}
 * @param status {@link SampleStatus#UNAVAILABLE}, {@link SampleStatus#STALE} or null
 * @param text why, for a non-null status
 */
public record FrameChoice(RasterFrameRow frame, SampleStatus status, String text) {

    /** No usable frame. */
    public static FrameChoice unavailable(String reason) {
        return new FrameChoice(null, SampleStatus.UNAVAILABLE, reason);
    }

    /** A current frame. */
    public static FrameChoice current(RasterFrameRow frame) {
        return new FrameChoice(frame, null, null);
    }

    /** A frame older than {@code maxGap} relative to the requested time. */
    public static FrameChoice stale(RasterFrameRow frame, String reason) {
        return new FrameChoice(frame, SampleStatus.STALE, reason);
    }

    /** True when there is a frame to read. */
    public boolean hasFrame() {
        return frame != null;
    }
}
