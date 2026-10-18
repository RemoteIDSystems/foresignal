package ai.foresignal.notify;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A two-step audio send (prompt 141): {@link #upload()} is sent first, and {@link #then()} turns its response into
 * the request that posts the audio, or empty when the response carries nothing usable. Used by types whose audio
 * must first be uploaded to a media repository (Matrix {@code m.audio}).
 *
 * @param upload the upload request
 * @param then the follow-up built from the upload's response
 */
public record AudioUpload(PreparedRequest upload, Function<ChannelResponse, Optional<PreparedRequest>> then) {

    /** Requires both. */
    public AudioUpload {
        Objects.requireNonNull(upload, "upload");
        Objects.requireNonNull(then, "then");
    }
}
