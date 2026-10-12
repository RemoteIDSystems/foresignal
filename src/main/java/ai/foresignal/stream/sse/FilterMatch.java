package ai.foresignal.stream.sse;

/** How an event relates to a client's filter. */
public enum FilterMatch {
    /** Not delivered. */
    NONE,
    /** Delivered; the current box matches (or there is no spatial filter). */
    MATCH,
    /** Delivered with {@code leftFilter: true}: only a previous box matches, so the client removes the feature. */
    LEFT;

    /** True when the event is delivered. */
    public boolean delivered() {
        return this != NONE;
    }
}
