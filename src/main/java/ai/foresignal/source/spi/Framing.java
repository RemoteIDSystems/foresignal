package ai.foresignal.source.spi;

/**
 * How a TCP byte stream is cut into frames for a {@link FrameHandler} (prompt 069).
 *
 * <ul>
 *   <li>{@link #LINES}: one frame per line, ended by LF or CRLF; the terminator is not part of the frame.
 *       A line longer than {@value #MAX_LINE_BYTES} bytes is dropped and counted, never delivered in
 *       pieces.
 *   <li>{@link #RAW}: whatever one read returned, for binary protocols that frame themselves (KISS in
 *       prompt 082).
 * </ul>
 */
public enum Framing {
    /** LF- or CRLF-terminated lines, at most {@value #MAX_LINE_BYTES} bytes each. */
    LINES,
    /** Read-sized chunks, delivered as they arrive. */
    RAW;

    /** The longest line {@link #LINES} delivers, in bytes, without its terminator. */
    public static final int MAX_LINE_BYTES = 64 * 1024;
}
