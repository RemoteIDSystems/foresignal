package ai.foresignal.feed;

import java.time.Instant;
import java.util.List;

/**
 * A whole feed (prompt 058).
 *
 * @param id the feed id, a tag URI
 * @param title the feed title
 * @param description a one-line description
 * @param feedUrl the feed's own URL ({@code rel="self"}, {@code feed_url})
 * @param homeUrl the related page ({@code rel="alternate"}, {@code home_page_url})
 * @param updated the newest entry's {@code updated}, or the epoch for an empty feed
 * @param entries the entries, newest first
 */
public record Feed(String id, String title, String description, String feedUrl, String homeUrl, Instant updated,
        List<FeedEntry> entries) {

    /** The author of every feed and entry. */
    public static final String AUTHOR = "Foresignal";

    /** Copies the entries. */
    public Feed {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }
}
