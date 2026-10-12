package ai.foresignal.notify;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A destination's response to one request.
 *
 * @param status the HTTP status (250 for an accepted SMTP message)
 * @param headers the response headers, names lowercase
 * @param body the response body as text, or empty
 */
public record ChannelResponse(int status, Map<String, List<String>> headers, String body) {

    /** Copies. */
    public ChannelResponse {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        body = body == null ? "" : body;
    }

    /** True for 2xx (and an accepted SMTP message). */
    public boolean success() {
        return (status >= 200 && status < 300) || status == 250;
    }

    /** The first value of a header (case-insensitive), if present. */
    public Optional<String> header(String name) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name) && !entry.getValue().isEmpty()) {
                return Optional.of(entry.getValue().getFirst());
            }
        }
        return Optional.empty();
    }
}
