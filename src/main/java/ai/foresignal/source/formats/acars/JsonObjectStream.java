package ai.foresignal.source.formats.acars;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Cuts a byte stream into top-level JSON objects (prompt 086), whether the sender ends each object with a newline or
 * writes them back to back: acars_router's TCP servers pass the decoders' JSON through, and the framing is confirmed
 * only at fixture capture, so the codec does not depend on it.
 *
 * <p>Tracks brace depth outside strings (with backslash escapes); bytes between objects (whitespace, newlines, stray
 * characters) are skipped. An object longer than {@value #MAX_OBJECT_BYTES} bytes is discarded and counted, never
 * delivered in pieces. The splitter never logs or exposes the bytes it discards. Not thread-safe; one per connection.
 */
public final class JsonObjectStream {

    /** The longest object delivered. */
    public static final int MAX_OBJECT_BYTES = 64 * 1024;

    private final ByteArrayOutputStream current = new ByteArrayOutputStream();
    private int depth;
    private boolean inString;
    private boolean escaped;
    private boolean overflow;
    private long discarded;

    /** Appends {@code chunk} and returns the objects it completes, in order. */
    public List<byte[]> feed(byte[] chunk) {
        List<byte[]> out = new ArrayList<>();
        if (chunk == null) {
            return out;
        }
        for (byte b : chunk) {
            if (depth == 0) {
                if (b == '{') {
                    depth = 1;
                    inString = false;
                    escaped = false;
                    overflow = false;
                    current.reset();
                    current.write(b);
                }
                continue;
            }
            if (!overflow) {
                if (current.size() >= MAX_OBJECT_BYTES) {
                    overflow = true;
                    current.reset();
                } else {
                    current.write(b);
                }
            }
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (b == '\\') {
                    escaped = true;
                } else if (b == '"') {
                    inString = false;
                }
                continue;
            }
            if (b == '"') {
                inString = true;
            } else if (b == '{') {
                depth++;
            } else if (b == '}') {
                depth--;
                if (depth == 0) {
                    if (overflow) {
                        discarded++;
                    } else {
                        out.add(current.toByteArray());
                    }
                    current.reset();
                }
            }
        }
        return out;
    }

    /** Objects discarded for their length so far. */
    public long discarded() {
        return discarded;
    }
}
