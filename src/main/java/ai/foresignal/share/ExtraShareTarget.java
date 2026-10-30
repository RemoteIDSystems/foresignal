package ai.foresignal.share;

import java.util.Set;

/**
 * A share-link target type contributed by another package (prompt 129: {@code workspace}, a read-only link to a
 * workspace context). {@link ShareLinkService} discovers these beans and accepts their {@link #type()} next to its own
 * target types.
 */
public interface ExtraShareTarget {

    /** The target type, e.g. {@code workspace}. */
    String type();

    /**
     * Checks that the target exists and may be shared by the caller.
     *
     * @param ref the target reference
     * @param caller the link's creator
     * @return the sources the target involves (none for a context)
     */
    Set<String> check(String ref, ShareLinkService.Caller caller);

    /**
     * Renders the target for a link.
     *
     * @param link the share link
     * @return what the link shows
     */
    ShareLinkService.Rendered render(ShareLinkEntity link);

    /**
     * The longest lifetime a link of this type may have; {@link ShareLinkService#MAX_TTL} unless the target serves
     * something meant to stay at one address (NP-005: an edition site).
     */
    default java.time.Duration maxTtl() {
        return ShareLinkService.MAX_TTL;
    }
}
