package ai.foresignal.source.adapters.cap;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
 * @param licence the license, as stated by the authority
 * @param termsUrl the terms-of-use URL
 * @param reviewedOn the date the entry was last reviewed
 * @param notes what a reviewer should know
 * @param documentHosts further hosts the index may link CAP documents on, reviewed with the entry; the feed's own
 *     host is always allowed and needs no entry
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
        String notes,
        List<String> documentHosts) {

    /** Copies the lists and lower-cases the document hosts. */
    public CapFeed {
        languages = languages == null ? List.of() : List.copyOf(languages);
        List<String> hosts = new ArrayList<>();
        if (documentHosts != null) {
            for (String host : documentHosts) {
                if (host != null && !host.isBlank()) {
                    hosts.add(host.strip().toLowerCase(Locale.ROOT));
                }
            }
        }
        documentHosts = List.copyOf(hosts);
    }

    /**
     * A feed whose CAP documents live on the feed's own host.
     *
     * @param id the source id
     * @param country the country code
     * @param authority the alerting authority
     * @param feedUrl the index URL
     * @param format the index format
     * @param languages the preferred languages
     * @param licence the license
     * @param termsUrl the terms-of-use URL
     * @param reviewedOn the review date
     * @param notes reviewer notes
     */
    public CapFeed(String id, String country, String authority, URI feedUrl, String format, List<String> languages,
            String licence, URI termsUrl, String reviewedOn, String notes) {
        this(id, country, authority, feedUrl, format, languages, licence, termsUrl, reviewedOn, notes, List.of());
    }

    /**
     * True when the index may make the adapter fetch {@code link}: an absolute URL with the feed's scheme, no user
     * info, and the feed's host and port or one of the reviewed {@link #documentHosts} (default port). An index is
     * upstream data, so a link elsewhere is refused rather than followed.
     *
     * @param link the document link, possibly null
     * @return whether the link may be fetched
     */
    public boolean allowsDocument(URI link) {
        if (link == null || !link.isAbsolute() || link.getHost() == null || link.getRawUserInfo() != null
                || feedUrl == null || feedUrl.getScheme() == null
                || !feedUrl.getScheme().equalsIgnoreCase(link.getScheme())) {
            return false;
        }
        String host = link.getHost().toLowerCase(Locale.ROOT);
        if (host.equalsIgnoreCase(feedUrl.getHost())) {
            return link.getPort() == feedUrl.getPort();
        }
        return link.getPort() == -1 && documentHosts.contains(host);
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
        return new CapFeed(id, country, authority, moved, format, languages, licence, termsUrl, reviewedOn, notes,
                documentHosts);
    }
}
