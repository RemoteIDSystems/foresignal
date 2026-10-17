package ai.foresignal.forecast;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The 018 notification event types of the ledger (prompt 122): {@value #RESOLUTION_NEEDED} when a manual (or
 * hypothesis-outcome) question reaches {@code resolve_by} — on by default: it appears in the operator's inbox — and
 * {@value #RESOLVED} when a question resolves — off by default: only channels that list it receive it. Private
 * questions send neither.
 */
@Component
public class ForecastNotifications {

    /** A question resolved. */
    public static final String RESOLVED = "forecast.resolved";

    /** A manual question needs resolving. */
    public static final String RESOLUTION_NEEDED = "forecast.resolution_needed";

    private final ObjectProvider<NotificationService> notifications;

    /** Creates the component. */
    public ForecastNotifications(ObjectProvider<NotificationService> notifications) {
        this.notifications = notifications;
    }

    /** Sends {@value #RESOLUTION_NEEDED}. */
    public void resolutionNeeded(ForecastQuestionEntity q) {
        if (q.isPrivateQuestion()) {
            return;
        }
        Map<String, Object> data = data(q);
        send(NotificationMessage.builder(RESOLUTION_NEEDED, RESOLUTION_NEEDED, "Resolve forecast question: "
                        + q.getTitle(), "info")
                .body("This question reached its resolution time and needs a manual resolution with evidence."
                        + "\n\nCriteria (verbatim): " + q.getResolutionCriteria()
                        + "\n\nResolve it with POST /v1/forecasts/questions/" + q.getId() + "/resolve.")
                .url("/v1/forecasts/questions/" + q.getId())
                .data(data)
                .tags(List.of("forecast"))
                .build());
    }

    /** Sends {@value #RESOLVED}. */
    public void resolved(ForecastQuestionEntity q) {
        if (q.isPrivateQuestion()) {
            return;
        }
        Map<String, Object> data = data(q);
        Map<String, Object> resolution = q.getResolution();
        Object outcome = resolution == null ? null : resolution.get("outcome") != null ? resolution.get("outcome")
                : resolution.get("value");
        data.put("outcome", outcome);
        send(NotificationMessage.builder(RESOLVED, RESOLVED, "Forecast question resolved: " + q.getTitle(), "info")
                .body("Resolved " + outcome + " by " + (resolution == null ? "?" : resolution.get("resolvedBy"))
                        + ". Scores are on the question.")
                .url("/v1/forecasts/questions/" + q.getId())
                .data(data)
                .tags(List.of("forecast"))
                .build());
    }

    private static Map<String, Object> data(ForecastQuestionEntity q) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("questionId", q.getId());
        data.put("category", q.getCategory());
        data.put("resolverType", q.getResolverType());
        data.put("resolveBy", q.getResolveBy().toString());
        return data;
    }

    private void send(NotificationMessage message) {
        NotificationService service = notifications.getIfAvailable();
        if (service != null) {
            service.publish(message, null);
        }
    }
}
