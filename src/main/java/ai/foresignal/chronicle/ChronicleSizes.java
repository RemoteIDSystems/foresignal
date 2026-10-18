package ai.foresignal.chronicle;

/**
 * The deterministic storage estimates of chronicle capture (prompt 131). Bytes are counted per captured row rather
 * than read from the relation size, so a cap is enforced identically on every instance and an empty leaf (whose
 * index pages alone take tens of kilobytes) does not count against it.
 */
public final class ChronicleSizes {

    /** The fixed overhead of one captured version: tuple header, the fixed columns and index entries. */
    public static final long ROW_OVERHEAD = 160;

    /** One rollup row (inserted once per region and hour). */
    public static final long ROLLUP_ROW = 64;

    /** One captured series point. */
    public static final long SERIES_ROW = 128;

    /** One frame pin (the frame itself stays in the 042 frame store). */
    public static final long FRAME_ROW = 96;

    /** Bytes in a gigabyte, for the {@code GB} settings. */
    public static final long GB = 1024L * 1024L * 1024L;

    private ChronicleSizes() {}
}
