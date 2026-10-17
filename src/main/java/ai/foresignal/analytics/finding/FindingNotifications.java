package ai.foresignal.analytics.finding;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The 018 notification event type {@value #EVENT} (prompt 119): one message per new significant finding. It reaches
 * only channels whose {@code events} include it, so it is off by default. Private findings send none (a finding has
 * no single source to redact by).
 */
@Component
public class FindingNotifications {

    /** The event type. */
    public static final String EVENT = "finding.significant";

    private final NotificationService notifications;

    /** Creates the component. */
    public FindingNotifications(NotificationService notifications) {
        this.notifications = notifications;
    }

    /** Sends the notification of a significant finding. */
    public void send(AnalyticFindingEntity f) {
        if (f.isPrivateFinding()) {
            return;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("findingId", f.getId());
        data.put("findingType", f.getFindingType());
        data.put("severity", f.getSeverity());
        data.put("regionCodes", f.getRegionCodes());
        data.put("seriesRefs", f.getSeriesRefs());
        Object baseRate = f.getBaseRate().get("text");
        String body = (f.getExplanation() == null ? "" : f.getExplanation())
                + (baseRate == null ? "" : "\n\nBase rate: " + baseRate)
                + "\n\nAutomated statistical finding; it says how unusual a pattern is, not why. Verify before acting.";
        NotificationMessage message = NotificationMessage.builder(EVENT, EVENT, "Significant finding: " + f.getTitle(),
                        "warning")
                .body(body)
                .url("/v1/analytics/findings/" + f.getId())
                .data(data)
                .tags(List.of("finding", f.getFindingType()))
                .build();
        notifications.publish(message, null);
    }
}
