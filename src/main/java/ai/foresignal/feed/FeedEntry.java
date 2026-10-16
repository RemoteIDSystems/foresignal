package ai.foresignal.feed;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * One entry of an Atom or JSON Feed (prompt 058), written by {@link AtomFeedWriter} and
 * {@link JsonFeedWriter}.
 *
 * @param id the entry id, a tag URI ({@code tag:foresignal,2026:{installId}/alert/{id}})
 * @param title the title (plain text)
 * @param link the alternate link
 * @param published Atom {@code published} / JSON Feed {@code date_published}
 * @param updated Atom {@code updated} / JSON Feed {@code date_modified}: the last lifecycle change
 * @param html the sanitized HTML content
 * @param text the plain-text content
 * @param categories Atom categories / JSON Feed tags (severity and rule tags)
 * @param geometry the geometry for GeoRSS, or null
 * @param extension the JSON Feed {@code _foresignal} object, or null
 * @param linkType the media type of the alternate link (Atom {@code type}), or null (prompt 112: CAP entries
 *     link {@code application/cap+xml})
 */
public record FeedEntry(
        String id,
        String title,
        String link,
        Instant published,
        Instant updated,
        String html,
        String text,
        List<Category> categories,
        Geometry geometry,
        Map<String, Object> extension,
        String linkType) {

    /** Copies the categories. */
    public FeedEntry {
        categories = categories == null ? List.of() : List.copyOf(categories);
    }

    /** An entry whose alternate link has no media type (prompt 058). */
    public FeedEntry(String id, String title, String link, Instant published, Instant updated, String html, String text,
            List<Category> categories, Geometry geometry, Map<String, Object> extension) {
        this(id, title, link, published, updated, html, text, categories, geometry, extension, null);
    }

    /**
     * One category.
     *
     * @param term the term, for example {@code critical}
     * @param scheme the scheme URI, for example {@code tag:foresignal,2026:severity}
     */
    public record Category(String term, String scheme) {}
}
