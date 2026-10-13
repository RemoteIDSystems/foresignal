/**
 * Atom (RFC 4287) and JSON Feed 1.1 for alerts and briefing runs (prompt 058), so feed readers and
 * automations subscribe without custom code: {@link ai.foresignal.feed.FeedService} selects the entries,
 * {@link ai.foresignal.feed.AtomFeedWriter} (StAX, with GeoRSS-Simple) and
 * {@link ai.foresignal.feed.JsonFeedWriter} (Jackson, with the {@code _foresignal} extension) write them,
 * and {@link ai.foresignal.feed.FeedSanitizer} turns markdown into allowlisted HTML. RSS 2.0 and WebSub
 * are not offered.
 */
package ai.foresignal.feed;
