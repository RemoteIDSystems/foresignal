package ai.foresignal.source.kit.feed;

import ai.foresignal.source.formats.news.NewsArticles;
import ai.foresignal.source.formats.news.NewsNames;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.List;

/**
 * The reviewed {@code news-article} item mapper (prompt 133): an operator outlet feed of the {@code news-feed-*} family
 * maps each item to the same {@code news-article} record {@code gdelt-articles} writes ({@link NewsArticles}).
 *
 * <ul>
 *   <li>{@code externalId}: the SHA-256 of the item link's canonical URL, so an article seen by GDELT and by the
 *       outlet's feed is one record;
 *   <li>{@code seenAt}: the record's {@code observedAt} (the spec's {@code updated} or {@code published}), else the
 *       item's published or updated time; an item with none of them is skipped;
 *   <li>{@code language}: the item's, else the feed's, else the spec's; {@code sourceCountry} stays unknown (the
 *       outlet registry supplies it);
 *   <li>the feed summary (at most 500 characters) and the first image enclosure as a link; <strong>authors are dropped
 *       </strong> (NEWS-2: no bylines are stored).
 * </ul>
 * The outlet id is resolved later by the media registry from the domain (a mapper is pure and reads no store). Item
 * text is untrusted data.
 */
public final class NewsArticleItemMapper implements FeedItemMapper {

    /** The name a feed spec uses. */
    public static final String NAME = "news-article";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public NormalizedRecord map(FeedItem item, FeedDocument document, FeedSourceSpec spec, NormalizedRecord standard) {
        if (item.link() == null || item.title() == null) {
            return null;
        }
        Instant seen = standard.observedAt() != null ? standard.observedAt()
                : item.published() != null ? item.published() : item.updated();
        if (seen == null) {
            return null;
        }
        String language = NewsNames.languageCode(item.language() != null ? item.language()
                : document.language() != null ? document.language() : spec.language());
        String image = null;
        for (FeedItem.Enclosure e : item.enclosures()) {
            if (e.type() != null && e.type().startsWith("image/")) {
                image = e.url();
                break;
            }
        }
        return NewsArticles.record(new NewsArticles.Article(item.title(), item.link(), null, language, null, seen,
                item.published(), List.of(), item.summary(), image));
    }
}
