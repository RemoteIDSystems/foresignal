package ai.foresignal.cycle;

import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.llm.LlmService;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.search.EmbeddingWorker;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Delivers a published assessment (prompt 128 §4): the 018 notification event {@code assessment.published} (stored,
 * so it reaches the 033 inbox; sent to the cycle's channels, or every subscribed channel when it names none, when
 * the cadence's priority — {@code normal} daily and on demand, {@code high} weekly and monthly — is at least the
 * cycle's {@code notify_min_priority}), and the 027 embedding of its markdown. A private assessment carries the
 * private source so channels and the inbox apply the private-payload gate, and is never embedded.
 */
@Component
public class AssessmentDelivery {

    private static final Logger log = LoggerFactory.getLogger(AssessmentDelivery.class);

    /** The notification event. */
    public static final String EVENT = "assessment.published";

    /** The 027 document type of assessments. */
    public static final String DOC_TYPE = "assessment";

    private final ObjectProvider<NotificationService> notifications;
    private final ObjectProvider<EmbeddingWorker> embeddings;
    private final ObjectProvider<LlmService> llm;
    private final IntelVisibility visibility;

    /** Creates the component. */
    public AssessmentDelivery(ObjectProvider<NotificationService> notifications,
            ObjectProvider<EmbeddingWorker> embeddings, ObjectProvider<LlmService> llm, IntelVisibility visibility) {
        this.notifications = notifications;
        this.embeddings = embeddings;
        this.llm = llm;
        this.visibility = visibility;
    }

    /**
     * Notifies and embeds {@code a}.
     *
     * @param a the assessment
     * @param channelIds the cycle's channels (empty: every subscribed channel)
     * @param minPriority the cycle's {@code notify_min_priority}
     */
    public void deliver(AssessmentEntity a, List<String> channelIds, String minPriority) {
        notify(a, channelIds, minPriority);
        if (!a.isPrivateAssessment()) {
            embed(a);
        }
    }

    private void notify(AssessmentEntity a, List<String> channelIds, String minPriority) {
        NotificationService service = notifications.getIfAvailable();
        if (service == null) {
            return;
        }
        try {
            String priority = CycleVocabulary.priority(a.getCadence());
            boolean toChannels = !CycleVocabulary.belowPriority(priority, minPriority);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("assessmentId", a.getId());
            data.put("cycleId", a.getCycleId());
            data.put("situationId", a.getSituationId());
            data.put("cadence", a.getCadence());
            data.put("status", a.getStatus());
            data.put("priority", priority);
            if (a.isPrivateAssessment()) {
                data.put("sourceId", visibility.privateSourceIds().stream().findFirst().orElse("private"));
                data.put("private", true);
            }
            String title = title(a);
            NotificationMessage message = NotificationMessage.builder(EVENT, EVENT, title,
                            "high".equals(priority) ? "warning" : "info")
                    .body((a.getHeadline() == null ? "" : a.getHeadline() + "\n\n") + CycleVocabulary.FOOTER)
                    .url("/v1/assessments/" + a.getId())
                    .data(data)
                    .tags(List.of("assessment", a.getCadence()))
                    .build();
            Set<String> ids = new LinkedHashSet<>(channelIds == null ? List.of() : channelIds);
            service.publish(message, channel -> toChannels && (ids.isEmpty() || ids.contains(channel.id())));
        } catch (RuntimeException e) {
            log.warn("Could not notify assessment {}: {}", a.getId(), e.toString());
        }
    }

    /** The notification title. */
    static String title(AssessmentEntity a) {
        String kind = switch (a.getCadence()) {
            case CycleVocabulary.DAILY -> "Daily assessment";
            case CycleVocabulary.WEEKLY -> "Weekly assessment";
            case CycleVocabulary.MONTHLY -> "Monthly assessment";
            default -> "Situation assessment";
        };
        return kind + " " + a.getPeriodEnd().toString().substring(0, 10);
    }

    private void embed(AssessmentEntity a) {
        try {
            EmbeddingWorker worker = embeddings.getIfAvailable();
            LlmService service = llm.getIfAvailable();
            if (worker != null && service != null && service.embeddingClient().isPresent() && a.getMarkdown() != null) {
                worker.embedDocument(DOC_TYPE, a.getId(), a.getMarkdown(), null, a.getCreatedAt(), null, null);
            }
        } catch (RuntimeException e) {
            log.debug("Could not embed assessment {}: {}", a.getId(), e.toString());
        }
    }
}
