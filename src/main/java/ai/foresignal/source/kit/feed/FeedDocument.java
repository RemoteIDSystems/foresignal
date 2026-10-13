package ai.foresignal.source.kit.feed;

import java.util.List;

/**
 * A parsed feed (prompt 073).
 *
 * @param format {@code rss2}, {@code rss1}, {@code atom} or {@code jsonfeed}
 * @param title the feed title, plain text, or null
 * @param link the feed's home page link, or null
 * @param self the feed's own URL ({@code <link rel="self">}, JSON Feed {@code feed_url}), or null
 * @param hubs the WebSub hubs it advertises ({@code <link rel="hub">}, JSON Feed {@code hubs}), possibly empty
 * @param language the feed language, or null
 * @param items the items, in document order
 * @param warnings parser warnings (an ISO-8601 date without a zone read as UTC, a geometry left out)
 */
public record FeedDocument(String format, String title, String link, String self, List<String> hubs, String language,
        List<FeedItem> items, List<String> warnings) {

    /** RSS 2.0 (and 0.9x). */
    public static final String RSS2 = "rss2";

    /** RSS 1.0 (RDF). */
    public static final String RSS1 = "rss1";

    /** Atom 1.0. */
    public static final String ATOM = "atom";

    /** JSON Feed 1.0 or 1.1. */
    public static final String JSON_FEED = "jsonfeed";

    /** Copies the lists. */
    public FeedDocument {
        hubs = hubs == null ? List.of() : List.copyOf(hubs);
        items = items == null ? List.of() : List.copyOf(items);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
