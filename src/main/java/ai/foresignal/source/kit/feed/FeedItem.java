package ai.foresignal.source.kit.feed;

import java.time.Instant;
import java.util.List;
import org.locationtech.jts.geom.Geometry;

/**
 * One feed item, normalized across RSS 2.0, RSS 1.0, Atom 1.0 and JSON Feed (prompt 073).
 *
 * <p>All text is plain text: HTML is stripped by {@link FeedText}, so nothing here is ever rendered as HTML. Item text
 * is upstream-controlled and, under prompt 028's rules, untrusted data that never becomes instructions.
 *
 * @param id the guid, Atom id or JSON Feed id; else the first 16 hex characters of SHA-256 of link and title
 * @param title the title, plain text, or null
 * @param summary the summary (else the content), plain text, at most 4 KB, or null
 * @param link the absolute http(s) link, or null
 * @param published when it was published, or null
 * @param updated when it was last updated, or null
 * @param authors author names, possibly empty
 * @param categories categories or tags, at most 20
 * @param enclosures attachments, at most 5
 * @param language the item's language, else the feed's, or null
 * @param geometry the GeoRSS, GML, Basic Geo or {@code _geo} geometry in SRID 4326, or null
 */
public record FeedItem(String id, String title, String summary, String link, Instant published, Instant updated,
        List<String> authors, List<String> categories, List<Enclosure> enclosures, String language,
        Geometry geometry) {

    /** The most categories kept. */
    public static final int MAX_CATEGORIES = 20;

    /** The most enclosures kept. */
    public static final int MAX_ENCLOSURES = 5;

    /** Copies the lists and applies the caps. */
    public FeedItem {
        authors = authors == null ? List.of() : List.copyOf(authors);
        categories = categories == null ? List.of()
                : List.copyOf(categories.subList(0, Math.min(MAX_CATEGORIES, categories.size())));
        enclosures = enclosures == null ? List.of()
                : List.copyOf(enclosures.subList(0, Math.min(MAX_ENCLOSURES, enclosures.size())));
    }

    /**
     * An attachment.
     *
     * @param url its absolute http(s) URL
     * @param type its media type, or null
     * @param length its size in bytes, or null
     */
    public record Enclosure(String url, String type, Long length) {}
}
