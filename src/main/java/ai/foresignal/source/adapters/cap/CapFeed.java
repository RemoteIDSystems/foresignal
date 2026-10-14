package ai.foresignal.source.adapters.cap;

import java.net.URI;
import java.util.List;

/**
 * One reviewed CAP feed (prompt 089): an entry of the committed {@value CapFeeds#RESOURCE}, seeded from the
 * WMO Register of Alerting Authorities and reviewed by hand.
 *
 * @param id the source id ({@code cap-<iso2>-<authority>})
 * @param country the ISO 3166-1 alpha-2 country code
 * @param authority the alerting authority
 * @param feedUrl the index (or, for {@code cap-inline}, the single document) URL, HTTPS
 * @param format {@code atom-index}, {@code rss-index} or {@code cap-inline}
 * @param languages the preferred languages, best first, for the CAP {@code info} block
 * @param licence the licence, as stated by the authority
 * @param termsUrl the terms-of-use URL
 * @param reviewedOn the date the entry was last reviewed
 * @param notes what a reviewer should know
 */
public record CapFeed(
        String id,
        String country,
        String authority,
        URI feedUrl,
        String format,
        List<String> languages,
        String licence,
        URI termsUrl,
        String reviewedOn,
        String notes) {

    public CapFeed {
        languages = languages == null ? List.of() : List.copyOf(languages);
    }

    /** {@link #feedUrl()} moved under {@code base} (scheme and authority), keeping the path and query (tests). */
    public CapFeed rebased(String base) {
        if (base == null || base.isBlank()) {
            return this;
        }
        URI b = URI.create(base.strip());
        URI moved;
        try {
            moved = new URI(b.getScheme(), null, b.getHost(), b.getPort(), feedUrl.getRawPath(),
                    feedUrl.getRawQuery(), null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot rebase " + feedUrl + " under " + base, e);
        }
        return new CapFeed(id, country, authority, moved, format, languages, licence, termsUrl, reviewedOn, notes);
    }
}
