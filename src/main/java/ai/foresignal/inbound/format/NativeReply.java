package ai.foresignal.inbound.format;

import java.util.Objects;
import org.springframework.http.MediaType;

/**
 * The reply an app expects instead of Foresignal's {@code IngestOutcome} (prompt 039): OwnTracks wants
 * {@code []}, Overland {@code {"result":"ok"}} (or it retries), OsmAnd/Traccar Client and GPSLogger an
 * empty {@code 200}.
 *
 * @param status the HTTP status
 * @param contentType the body's media type, or null for an empty body
 * @param body the body, or an empty string
 */
public record NativeReply(int status, MediaType contentType, String body) {

    /** Requires a status; a null body becomes empty. */
    public NativeReply {
        body = body == null ? "" : body;
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("status");
        }
    }

    /** {@code 200} with an empty body. */
    public static NativeReply empty() {
        return new NativeReply(200, null, "");
    }

    /** {@code 200 application/json} with {@code json}. */
    public static NativeReply json(String json) {
        return new NativeReply(200, MediaType.APPLICATION_JSON, Objects.requireNonNull(json, "json"));
    }
}
