package ai.foresignal.cli.sse;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Parses a {@code text/event-stream} body (WHATWG HTML, "Server-sent events") into {@link SseEvent}s.
 *
 * <p>Unlike a browser's {@code EventSource}, each event reports only the id it carried itself: Foresignal sends ids
 * on lossless events only, and the resume position must never advance on an id-less event. Comment lines
 * ({@code :}) are skipped; {@code retry:} is remembered and reported with every later event.
 */
public final class SseReader implements Closeable {

    private final BufferedReader reader;
    private Long retry;

    /**
     * Wraps a stream body.
     *
     * @param in the response body (UTF-8)
     */
    public SseReader(InputStream in) {
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * Reads the next event.
     *
     * @return the event, or {@code null} at the end of the stream
     * @throws IOException when reading fails
     */
    public SseEvent next() throws IOException {
        String event = null;
        String id = null;
        StringBuilder data = null;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (data != null || event != null) {
                    return new SseEvent(event == null ? "message" : event, id, data == null ? "" : data.toString(),
                            retry);
                }
                id = null;
                continue;
            }
            if (line.startsWith(":")) {
                continue;
            }
            int colon = line.indexOf(':');
            String field = colon < 0 ? line : line.substring(0, colon);
            String value = colon < 0 ? "" : line.substring(colon + 1);
            if (value.startsWith(" ")) {
                value = value.substring(1);
            }
            switch (field) {
                case "event" -> event = value;
                case "data" -> {
                    if (data == null) {
                        data = new StringBuilder(value);
                    } else {
                        data.append('\n').append(value);
                    }
                }
                case "id" -> {
                    if (value.indexOf('\0') < 0) {
                        id = value;
                    }
                }
                case "retry" -> {
                    if (!value.isEmpty() && value.chars().allMatch(Character::isDigit)) {
                        try {
                            retry = Long.parseLong(value);
                        } catch (NumberFormatException e) {
                            // Too large to matter; ignore it as the specification says.
                        }
                    }
                }
                default -> {
                    // Unknown fields are ignored.
                }
            }
        }
        return null;
    }

    /**
     * The last {@code retry:} value.
     *
     * @return milliseconds, or {@code null}
     */
    public Long retry() {
        return retry;
    }

    /**
     * Closes the underlying stream.
     *
     * @throws IOException when closing fails
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }
}
