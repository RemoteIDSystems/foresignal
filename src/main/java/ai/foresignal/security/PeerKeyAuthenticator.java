package ai.foresignal.security;

import java.util.Optional;

/**
 * Resolves a federation peer ingest key ({@code fsp_…}, prompt 113) presented as a Bearer token on an
 * {@link AccessRequirement.PeerKey} path. Implemented by the federation peer service; the token filter only asks.
 */
public interface PeerKeyAuthenticator {

    /** The prefix of a peer ingest key. */
    String PREFIX = "fsp_";

    /**
     * The peer whose current ingest key {@code key} is, or empty when no peer holds it (unknown, rotated away, or
     * the peer was deleted).
     *
     * @param key the presented plaintext key
     */
    Optional<String> authenticate(String key);
}
