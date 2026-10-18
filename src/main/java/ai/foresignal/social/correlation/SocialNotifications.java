package ai.foresignal.social.correlation;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The 018 notification event types of prompt 137, off by default (only channels that list them receive them):
 * {@value CorrelationVocabulary#NOTIFY_DIVERGENCE} when a comparison of a narrative above the velocity threshold
 * becomes {@code unobserved} or {@code contradicted}, and {@value CorrelationVocabulary#NOTIFY_GAP} for an
 * {@code attention-gap} finding. Wording reports what was and was not observed, never accusations.
 */
@Component
public class SocialNotifications {

    private final ObjectProvider<NotificationService> notifications;

    /** Creates the component. */
    public SocialNotifications(ObjectProvider<NotificationService> notifications) {
        this.notifications = notifications;
    }

    /** The message of a divergence. Pure. */
    public static NotificationMessage divergence(ComparisonStore.Comparison c, String narrativeTitle,
            Double velocity) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("comparisonId", c.id());
        data.put("claimId", c.claimId());
        data.put("narrativeId", c.narrativeId());
        data.put("status", c.status());
        data.put("signalFamily", c.signalFamily());
        data.put("velocity", velocity);
        String place = c.place() != null && c.place().get("name") != null ? c.place().get("name").toString()
                : c.regionCode();
        String what = CorrelationVocabulary.CONTRADICTED.equals(c.status())
                ? "an agency or sensor record, or the reuse of its image, contradicts it"
                : "no corresponding change was observed although coverage could have seen it";
        return NotificationMessage.builder(CorrelationVocabulary.NOTIFY_DIVERGENCE,
                        CorrelationVocabulary.NOTIFY_DIVERGENCE, "Social claim " + c.status() + ": " + c.claimType()
                                + (place == null ? "" : " at " + place), "info")
                .body("A fast-moving narrative (\"" + narrativeTitle + "\") reports " + c.claimType()
                        + (place == null ? "" : " at " + place) + "; " + what + ". This describes what people say"
                        + " versus what Foresignal observes, not anyone's intent.")
                .url("/v1/social/comparisons/" + c.id())
                .data(data)
                .tags(List.of("social"))
                .build();
    }

    /** The message of an attention gap. Pure. */
    public static NotificationMessage gap(String findingId, String title, String ref, double expected,
            double observed, List<String> platforms) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("findingId", findingId);
        data.put("ref", ref);
        data.put("expected", expected);
        data.put("observed", observed);
        data.put("platforms", platforms);
        return NotificationMessage.builder(CorrelationVocabulary.NOTIFY_GAP, CorrelationVocabulary.NOTIFY_GAP,
                        "Attention gap: " + title, "info")
                .body(String.format(java.util.Locale.ROOT, "Public discussion of %s was far below what similar past"
                        + " events drew (%.0f posts observed, %.0f expected, on %s), while social coverage of the"
                        + " region was adequate.", title, observed, expected, String.join(", ", platforms)))
                .url("/v1/findings/" + findingId)
                .data(data)
                .tags(List.of("social"))
                .build();
    }

    /** Sends a divergence. */
    public void sendDivergence(ComparisonStore.Comparison c, String narrativeTitle, Double velocity) {
        send(divergence(c, narrativeTitle, velocity));
    }

    /** Sends an attention gap. */
    public void sendGap(String findingId, String title, String ref, double expected, double observed,
            List<String> platforms) {
        send(gap(findingId, title, ref, expected, observed, platforms));
    }

    private void send(NotificationMessage message) {
        NotificationService service = notifications.getIfAvailable();
        if (service != null) {
            service.publish(message, null);
        }
    }
}
