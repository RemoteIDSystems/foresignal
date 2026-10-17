package ai.foresignal.situation;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The 018 notification event types {@code situation.activated} and {@code situation.escalated} (prompt 118 §7).
 * They reach only the channels whose {@code events} include them, so they are off by default; the Operator Guide's
 * recipe adds them to a channel. A private situation's notification carries its private marker in
 * {@code data.sourceId}, so channels without the private payload grant receive only the redacted form.
 */
@Component
public class SituationNotifications {

    /** The event type of an activation. */
    public static final String ACTIVATED = "situation.activated";

    /** The event type of an escalation. */
    public static final String ESCALATED = "situation.escalated";

    private final NotificationService notifications;
    private final SituationMemberRepository members;

    /** Creates the component. */
    public SituationNotifications(NotificationService notifications, SituationMemberRepository members) {
        this.notifications = notifications;
        this.members = members;
    }

    /** Sends the notification of an activation or escalation event. */
    public void send(SituationEvent event) {
        String type = SituationEvent.ACTIVATED.equals(event.event()) ? ACTIVATED : ESCALATED;
        Map<String, Object> summary = event.situation();
        String title = String.valueOf(summary.get("title"));
        String severity = String.valueOf(summary.get("severity"));
        String heading = ACTIVATED.equals(type) ? "Situation active: " + title
                : "Situation escalated to " + severity + ": " + title;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("situationId", event.situationId());
        data.put("status", summary.get("status"));
        data.put("severity", severity);
        data.put("previousStatus", event.previousStatus());
        data.put("previousSeverity", event.previousSeverity());
        if (event.privateSituation()) {
            data.put("private", true);
            data.put("sourceId", privateSource(event.situationId()));
        }
        NotificationMessage message = NotificationMessage.builder(type, type, heading,
                        "critical".equals(severity) ? "critical" : "warning".equals(severity) ? "warning" : "info")
                .body(String.valueOf(summary.get("summary")) + "\n\nAutomated situation tracking; members are evidence,"
                        + " status and severity are derived. Verify before acting.")
                .url("/v1/situations/" + event.situationId())
                .data(data)
                .tags(List.of("situation"))
                .build();
        notifications.publish(message, null);
    }

    private String privateSource(String situationId) {
        for (SituationMemberEntity member : members.findBySituationIdOrderByAddedAtAscIdAsc(situationId)) {
            if (member.isPrivateMember() && member.getSourceId() != null) {
                return member.getSourceId();
            }
        }
        return "private";
    }
}
