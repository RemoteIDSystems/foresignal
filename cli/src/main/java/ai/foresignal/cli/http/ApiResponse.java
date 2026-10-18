package ai.foresignal.cli.http;

import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * A buffered HTTP response.
 *
 * @param status  the HTTP status
 * @param headers the response headers
 * @param body    the body bytes (empty when there was none)
 */
public record ApiResponse(int status, HttpHeaders headers, byte[] body) {

    /**
     * Whether the status is 2xx.
     *
     * @return true on success
     */
    public boolean ok() {
        return status >= 200 && status < 300;
    }

    /**
     * One header.
     *
     * @param name the header name (case-insensitive)
     * @return its first value
     */
    public Optional<String> header(String name) {
        return headers.firstValue(name);
    }

    /**
     * The body as UTF-8 text.
     *
     * @return the text
     */
    public String text() {
        return new String(body, StandardCharsets.UTF_8);
    }
}
