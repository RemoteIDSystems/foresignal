package ai.foresignal.source.kit.feed;

import ai.foresignal.source.spi.FetchContext;
import java.time.Instant;

/**
 * What a feed-kit adapter asks of WebSub (prompt 073). Implemented by the WebSub service; while
 * {@code foresignal.kits.websub.enabled=false} (the default) every answer is "no" and nothing is ever subscribed.
 *
 * <p>All hub requests go through the fetch context's budgeted client, so a subscription costs budget like any other
 * request and never opens its own connection.
 */
public interface WebSubCoordinator {

    /**
     * True when {@code sourceId} has a verified subscription whose lease has not expired, so the poll interval
     * stretches to the safety net.
     *
     * @param sourceId the source
     * @param now the current time
     */
    boolean active(String sourceId, Instant now);

    /**
     * Called after every real poll: subscribes when the spec asks for WebSub, WebSub is enabled, the public base URL
     * is https and the feed advertises a hub and a self link.
     *
     * @param ctx the fetch context (its client sends the hub request)
     * @param spec the source's spec
     * @param document the feed just parsed
     */
    void afterPoll(FetchContext ctx, FeedSourceSpec spec, FeedDocument document);

    /**
     * Called on every run, polled or not: renews an active subscription at 80 % of its lease. A failed renewal
     * returns the source to polling and records the health reason {@value #LEASE_LOST}.
     *
     * @param ctx the fetch context
     * @param spec the source's spec
     */
    void maintain(FetchContext ctx, FeedSourceSpec spec);

    /** The health reason after a lost lease. */
    String LEASE_LOST = "WebSub lease lost";

    /** A coordinator that never subscribes (WebSub disabled, or no WebSub service in this context). */
    WebSubCoordinator DISABLED = new WebSubCoordinator() {
        @Override
        public boolean active(String sourceId, Instant now) {
            return false;
        }

        @Override
        public void afterPoll(FetchContext ctx, FeedSourceSpec spec, FeedDocument document) {
            // WebSub is off.
        }

        @Override
        public void maintain(FetchContext ctx, FeedSourceSpec spec) {
            // WebSub is off.
        }
    };
}
