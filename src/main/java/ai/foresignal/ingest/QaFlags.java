package ai.foresignal.ingest;

/**
 * Bits of {@code qa_flags} on versions and live rows (Core-Data-Model §3).
 *
 * <p>The values are persisted and must never change.
 */
public final class QaFlags {

    /** {@code observed_at} was more than {@code skewTolerance} ahead of {@code received_at}. */
    public static final int FUTURE_CLAMPED = 1;

    /** The geometry was invalid and {@code GeometryFixer} repaired it. */
    public static final int GEOMETRY_FIXED = 2;

    /** An exact (0,0) point was removed. */
    public static final int NULL_ISLAND = 4;

    /** {@code observed_at} was set to {@code received_at} ({@code observedAtMode=RECEIVED}). */
    public static final int TIME_IMPUTED = 8;

    /** TRACK carry-forward: the geometry is the last known one, not this observation's. */
    public static final int POSITION_STALE = 16;

    private QaFlags() {}

    /** True when {@code flags} has {@code flag} set. */
    public static boolean has(int flags, int flag) {
        return (flags & flag) != 0;
    }
}
