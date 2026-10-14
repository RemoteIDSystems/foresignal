package ai.foresignal.source.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * One open (not fully read) HTTP response body, for a streaming adapter that consumes a long-lived
 * HTTP response such as Server-Sent Events (prompt 095: the Wikimedia EventStreams
 * {@code recentchange} feed).
 *
 * <p>Unlike {@link UpstreamResponse}, the body is never buffered: the caller reads it and closes it.
 * The connection was opened by the budgeted client, so the connection attempt was charged against
 * the source's budget and carries Foresignal's own User-Agent; bytes read afterwards are reported by
 * the adapter through {@link StreamContext#reportBytes(long)}.
 *
 * @param status the HTTP status code
 * @param headers response headers, keys lowercased
 * @param body the response body stream; never null, and the caller must close it
 */
public record HttpStream(int status, Map<String, List<String>> headers, InputStream body) implements AutoCloseable {

    /** Copies the headers. */
    public HttpStream {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    /** The first value of a header, case-insensitively, or null. */
    public String header(String name) {
        List<String> values = headers.get(name.toLowerCase());
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    /** Closes the body stream, swallowing an error: it is already being torn down. */
    @Override
    public void close() {
        if (body == null) {
            return;
        }
        try {
            body.close();
        } catch (IOException ignored) {
            // The connection is being abandoned; there is nothing useful to do with a close error.
        }
    }
}
