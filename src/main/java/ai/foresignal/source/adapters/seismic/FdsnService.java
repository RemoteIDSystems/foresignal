package ai.foresignal.source.adapters.seismic;

import java.net.URI;

/**
 * One reviewed FDSN event service (prompt 089): {@value FdsnServices#RESOURCE}.
 *
 * @param id the service id; the source id is {@code fdsn-<id>}
 * @param name the display name
 * @param baseUrl the FDSN event query endpoint
 * @param supportsUpdatedAfter whether the service honors {@code updatedafter} (else {@code starttime} is used)
 * @param licence the license
 * @param termsUrl the terms-of-use URL
 * @param attribution the attribution
 * @param homepage the service home page
 * @param notes what a reviewer should know
 */
public record FdsnService(
        String id,
        String name,
        URI baseUrl,
        boolean supportsUpdatedAfter,
        String licence,
        URI termsUrl,
        String attribution,
        URI homepage,
        String notes) {

    /** The source id. */
    public String sourceId() {
        return "fdsn-" + id;
    }

    /** {@link #baseUrl()} moved under {@code base} (scheme and authority), keeping the path (tests). */
    public FdsnService rebased(String base) {
        if (base == null || base.isBlank()) {
            return this;
        }
        URI b = URI.create(base.strip());
        try {
            URI moved = new URI(b.getScheme(), null, b.getHost(), b.getPort(), baseUrl().getRawPath(),
                    baseUrl().getRawQuery(), null);
            return new FdsnService(id, name, moved, supportsUpdatedAfter, licence, termsUrl, attribution, homepage,
                    notes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot rebase " + baseUrl, e);
        }
    }
}
