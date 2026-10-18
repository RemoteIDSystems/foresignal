package ai.foresignal.feed;

import java.util.List;
import java.util.Objects;

/**
 * A podcast feed (prompt 141): a {@link Feed} whose every entry has an audio {@link Enclosure}, written as RSS 2.0
 * by {@link RssFeedWriter}.
 *
 * @param feed the channel and its entries, newest first
 * @param enclosures the audio of each entry; {@code enclosures.get(i)} belongs to {@code feed.entries().get(i)}
 */
public record PodcastFeed(Feed feed, List<Enclosure> enclosures) {

    /** Requires one enclosure per entry. */
    public PodcastFeed {
        Objects.requireNonNull(feed, "feed");
        enclosures = enclosures == null ? List.of() : List.copyOf(enclosures);
        if (enclosures.size() != feed.entries().size()) {
            throw new IllegalArgumentException("a podcast feed needs exactly one enclosure per entry");
        }
    }

    /**
     * An RSS {@code <enclosure>}.
     *
     * @param url the absolute URL of the audio
     * @param length the size in bytes
     * @param type the media type, for example {@code audio/mpeg}
     */
    public record Enclosure(String url, long length, String type) {

        /** Requires the URL and type. */
        public Enclosure {
            Objects.requireNonNull(url, "url");
            Objects.requireNonNull(type, "type");
        }
    }
}
