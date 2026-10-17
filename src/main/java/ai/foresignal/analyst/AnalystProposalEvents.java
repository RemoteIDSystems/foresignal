package ai.foresignal.analyst;

import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.stream.sse.AnalystTopic;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Announces analyst proposal changes as {@code analyst.proposal} events on the {@code analyst} SSE topic (prompt 102;
 * the publisher was missing until checkpoint 124). A change is persisted as a notification so the topic has a durable
 * cursor, but it is delivered to no channel: proposals are reviewed through the API, the MCP tools and the stream.
 *
 * <p>A private proposal (one from a run that saw private data) carries {@code data.sourceId} of a private source and
 * {@code data.private}, the marker the notification filter of the topic keys on, so it reaches only streams that may
 * see private sources.
 */
@Component
public class AnalystProposalEvents {

    private static final Logger log = LoggerFactory.getLogger(AnalystProposalEvents.class);

    /** The proposal was stored. */
    public static final String CREATED = "created";

    /** The proposal was executed. */
    public static final String EXECUTED = "executed";

    /** The proposal was rejected. */
    public static final String REJECTED = "rejected";

    /** The proposal expired unanswered. */
    public static final String EXPIRED = "expired";

    /** The proposal's execution failed. */
    public static final String FAILED = "failed";

    private final ObjectProvider<NotificationService> notifications;
    private final ObjectProvider<IntelVisibility> visibility;

    /** Creates the publisher. */
    public AnalystProposalEvents(ObjectProvider<NotificationService> notifications,
            ObjectProvider<IntelVisibility> visibility) {
        this.notifications = notifications;
        this.visibility = visibility;
    }

    /**
     * Publishes one change of {@code proposal}. A failure is logged and never fails the caller: the proposal itself
     * is already stored.
     *
     * @param proposal the proposal, in its new state
     * @param change created, executed, rejected, expired or failed
     */
    public void publish(AnalystProposalEntity proposal, String change) {
        NotificationService service = notifications.getIfAvailable();
        if (service == null || proposal == null) {
            return;
        }
        try {
            service.publish(message(proposal, change), channel -> false);
        } catch (RuntimeException e) {
            log.warn("Could not announce analyst proposal {} ({}): {}", proposal.getId(), change, e.toString());
        }
    }

    /** The notification message of one change. */
    NotificationMessage message(AnalystProposalEntity proposal, String change) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("proposalId", proposal.getId());
        data.put("profileId", proposal.getProfileId());
        data.put("change", change);
        if (proposal.isPrivateProposal()) {
            IntelVisibility v = visibility.getIfAvailable();
            String privateSource = v == null ? null : v.privateSourceIds().stream().findFirst().orElse(null);
            data.put("sourceId", privateSource != null ? privateSource : "private");
            data.put("private", true);
        }
        return NotificationMessage.builder(AnalystTopic.PROPOSAL, AnalystTopic.PROPOSAL,
                        "Analyst proposal " + change + ": " + proposal.getType(), "info")
                .url("/v1/analyst/proposals?status=" + proposal.getStatus())
                .data(data)
                .tags(List.of("analyst"))
                .build();
    }
}
