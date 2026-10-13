package ai.foresignal.collect.socket;

import ai.foresignal.source.spi.Framing;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

/**
 * Cuts a byte stream into lines for {@link Framing#LINES} (prompt 069).
 *
 * <p>A line ends at LF; a CR right before the LF is removed too, so LF and CRLF peers read the same. A line
 * longer than the limit is dropped whole, up to and including its terminator, and reported through the
 * overflow callback once; it is never delivered in pieces, because a truncated sentence would parse as a
 * different, wrong one. Bytes after the last terminator wait for the next {@link #feed}. Not thread-safe:
 * one framer per connection.
 */
public final class LineFramer {

    private final int maxLineBytes;
    private final Consumer<byte[]> lines;
    private final Runnable overflow;
    private final ByteArrayOutputStream pending = new ByteArrayOutputStream();
    private boolean discarding;

    /**
     * Creates a framer.
     *
     * @param maxLineBytes the longest line delivered, without its terminator
     * @param lines receives each complete line, without its terminator
     * @param overflow called once for each dropped line
     */
    public LineFramer(int maxLineBytes, Consumer<byte[]> lines, Runnable overflow) {
        if (maxLineBytes < 1) {
            throw new IllegalArgumentException("maxLineBytes must be positive");
        }
        this.maxLineBytes = maxLineBytes;
        this.lines = lines;
        this.overflow = overflow;
    }

    /**
     * Feeds {@code length} bytes of {@code buffer} from {@code offset}, delivering every line they complete.
     */
    public void feed(byte[] buffer, int offset, int length) {
        int start = offset;
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            if (buffer[i] != '\n') {
                continue;
            }
            append(buffer, start, i - start);
            if (discarding) {
                discarding = false;
            } else {
                byte[] line = pending.toByteArray();
                int size = line.length;
                if (size > 0 && line[size - 1] == '\r') {
                    size--;
                }
                if (size > maxLineBytes) {
                    overflow.run();
                } else {
                    lines.accept(size == line.length ? line : java.util.Arrays.copyOf(line, size));
                }
            }
            pending.reset();
            start = i + 1;
        }
        append(buffer, start, end - start);
    }

    /** The bytes held for an unfinished line. */
    public int pendingBytes() {
        return pending.size();
    }

    /** Appends to the unfinished line, switching to discarding once it is longer than the limit. */
    private void append(byte[] buffer, int offset, int length) {
        if (discarding || length <= 0) {
            return;
        }
        // One byte of slack for the CR of a CRLF terminator.
        if (pending.size() + length > maxLineBytes + 1) {
            pending.reset();
            discarding = true;
            overflow.run();
            return;
        }
        pending.write(buffer, offset, length);
    }
}
