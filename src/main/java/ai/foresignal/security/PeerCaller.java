package ai.foresignal.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * The peer behind an accepted peer ingest key (prompt 113), set by the token filter as a request attribute on
 * {@link AccessRequirement.PeerKey} paths.
 *
 * @param peerId the peer whose key was presented
 */
public record PeerCaller(String peerId) {

    /** The request attribute. */
    public static final String ATTRIBUTE = "ai.foresignal.peerCaller";

    /** The caller of {@code request}, if the token filter accepted a peer key. */
    public static Optional<PeerCaller> of(HttpServletRequest request) {
        return Optional.ofNullable((PeerCaller) request.getAttribute(ATTRIBUTE));
    }
}
