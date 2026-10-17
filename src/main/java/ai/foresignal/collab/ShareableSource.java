package ai.foresignal.collab;

import ai.foresignal.security.Tlp;
import java.util.Optional;

/**
 * The SPI through which a shareable type tells {@link AccessResolver} about its objects (prompt 130): one bean per
 * type in {@link ShareableTypes#ALL}. {@link #defaults} registers an object created before prompt 130 (or by code that
 * did not register it) the first time its access is asked for.
 */
public interface ShareableSource {

    /** The shareable type. */
    String type();

    /**
     * The registration defaults of object {@code id}.
     *
     * @param id the type-specific id (an investigation also by display id)
     * @return the defaults, or empty when no such object exists
     */
    Optional<Defaults> defaults(String id);

    /**
     * A role the object itself gives {@code principalId} besides its access list (a 129 workspace member), or null.
     */
    default AclRole intrinsicRole(String id, String principalId) {
        return null;
    }

    /**
     * Registration defaults.
     *
     * @param canonicalId the canonical id (an investigation's ULID for a display id)
     * @param owner the owner's principal id
     * @param visibility the visibility
     * @param tlp the TLP label, or null for the instance default
     */
    record Defaults(String canonicalId, String owner, Visibility visibility, Tlp tlp) {}
}
