package ai.foresignal.store;

import java.util.List;

/** The {@code notification_delivery.deferred_reason} values (prompt 018). */
public final class DeferredReason {

    /** Inside the channel's quiet hours. */
    public static final String QUIET_HOURS = "quiet_hours";

    /** Notifications are paused globally. */
    public static final String PAUSED = "paused";

    /** Waiting for the alert's enrichment (030 brings it forward). */
    public static final String ENRICHMENT = "enrichment";

    /** Held for the channel's grouping window. */
    public static final String GROUPING = "grouping";

    /** Every value. */
    public static final List<String> ALL = List.of(QUIET_HOURS, PAUSED, ENRICHMENT, GROUPING);

    /** The reasons released as a digest when they end. */
    public static final List<String> RELEASED_AS_DIGEST = List.of(QUIET_HOURS, PAUSED);

    private DeferredReason() {}
}
