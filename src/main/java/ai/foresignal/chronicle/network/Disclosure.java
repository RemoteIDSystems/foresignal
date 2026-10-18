package ai.foresignal.chronicle.network;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a viewer may learn about a relationship between one of the chronicles it can see and another chronicle
 * (prompt 134). Pure.
 *
 * <ul>
 *   <li>{@link Level#FULL}: the viewer can see the other chronicle too (owner, access list or admin): full details,
 *       including the shared canonical events and entities;</li>
 *   <li>{@link Level#OPAQUE}: the viewer cannot see it, but it is discoverable to the viewer (a local chronicle whose
 *       effective discoverability is {@code instance} or {@code network}, or a remote one, which peers only ever send
 *       when it is {@code network}): strength band, overlapping dimensions and time overlap, plus the title and owner
 *       handle only when its owner revealed them; never counts finer than the strength band;</li>
 *   <li>{@link Level#NONE}: nothing at all.</li>
 * </ul>
 */
public final class Disclosure {

    /** A disclosure level. */
    public enum Level {
        /** Both chronicles visible. */
        FULL,
        /** Discoverable, not visible. */
        OPAQUE,
        /** Not disclosed. */
        NONE
    }

    private Disclosure() {}

    /**
     * The disclosure level of the other side of a link.
     *
     * @param viewerSeesOther true when the viewer holds a role on the other (local) chronicle
     * @param otherRemote true when the other side is a received peer sketch
     * @param otherEffective the other local chronicle's discoverability after the TLP cap (ignored for remote)
     * @return the level
     */
    public static Level level(boolean viewerSeesOther, boolean otherRemote, DiscoverabilityLevel otherEffective) {
        if (otherRemote) {
            return Level.OPAQUE;
        }
        if (viewerSeesOther) {
            return Level.FULL;
        }
        return otherEffective != null && otherEffective.atLeast(DiscoverabilityLevel.INSTANCE) ? Level.OPAQUE
                : Level.NONE;
    }

    /**
     * An {@code OpaqueChronicleLink}.
     *
     * @param linkId the link
     * @param chronicleId the viewer's own chronicle in the link
     * @param strength the strength band
     * @param dimensions the overlapping dimensions
     * @param timeOverlap the capture-span overlap
     * @param remote true when the other side is on another instance
     * @param ownerHandle the other owner's handle when revealed, else null
     * @param title the other title when revealed, else null
     * @param accessRequestStatus the viewer's latest access request on the link, or null
     * @return the view
     */
    public static Map<String, Object> opaque(String linkId, String chronicleId, String strength,
            List<String> dimensions, String timeOverlap, boolean remote, String ownerHandle, String title,
            String accessRequestStatus) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("linkId", linkId);
        out.put("disclosure", NetworkVocabulary.OPAQUE);
        out.put("chronicleId", chronicleId);
        out.put("strength", strength);
        out.put("dimensions", dimensions == null ? List.of() : List.copyOf(dimensions));
        out.put("timeOverlap", timeOverlap == null ? NetworkVocabulary.OVERLAP_NONE : timeOverlap);
        out.put("remote", remote);
        out.put("ownerHandle", ownerHandle);
        out.put("title", title);
        out.put("accessRequestStatus", accessRequestStatus);
        return out;
    }

    /**
     * The capture-span overlap of two spans (an open end is now): none, partial, or full when one contains the other.
     *
     * @param aFrom the start of a, or null when it never captured
     * @param aTo the end of a, or null while it runs
     * @param bFrom the start of b, or null
     * @param bTo the end of b, or null
     * @param now the current time
     * @return {@code none}, {@code partial} or {@code full}
     */
    public static String timeOverlap(java.time.Instant aFrom, java.time.Instant aTo, java.time.Instant bFrom,
            java.time.Instant bTo, java.time.Instant now) {
        if (aFrom == null || bFrom == null) {
            return NetworkVocabulary.OVERLAP_NONE;
        }
        java.time.Instant ae = aTo == null ? now : aTo;
        java.time.Instant be = bTo == null ? now : bTo;
        if (ae.isBefore(bFrom) || be.isBefore(aFrom)) {
            return NetworkVocabulary.OVERLAP_NONE;
        }
        boolean aContainsB = !aFrom.isAfter(bFrom) && !ae.isBefore(be);
        boolean bContainsA = !bFrom.isAfter(aFrom) && !be.isBefore(ae);
        return aContainsB || bContainsA ? NetworkVocabulary.OVERLAP_FULL : NetworkVocabulary.OVERLAP_PARTIAL;
    }
}
