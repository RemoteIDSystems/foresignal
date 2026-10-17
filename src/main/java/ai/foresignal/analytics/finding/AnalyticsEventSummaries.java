package ai.foresignal.analytics.finding;

import java.util.Map;

/**
 * Resolves the payload of the prompt 126 events of the {@code analytics} SSE topic ({@code outlook.published},
 * {@code index.changed}, {@code index.weights_proposed}), so that {@link FindingService} can replay them from
 * {@code analytic_finding_log} without depending on the outlook package.
 */
public interface AnalyticsEventSummaries {

    /** True when this resolver handles {@code event}. */
    boolean handles(String event);

    /**
     * The event payload ({@code OutlookPublishedEvent}, {@code IndexChangedEvent} or
     * {@code IndexWeightsProposedEvent}).
     *
     * @param event the event name
     * @param objectId the logged object id (an outlook, an index value or an index fit)
     * @return the payload, or null when the object no longer exists
     */
    Map<String, Object> summary(String event, String objectId);
}
