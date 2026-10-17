package ai.foresignal.hypothesis;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The 018 notification event types of rolling hypotheses (prompt 125): {@value #BAND_CHANGED} (on by default for sets
 * with a daily or weekly cadence: it appears in the inbox; rate-limited to one per set per 6 hours by the service,
 * the latest change winning), {@value #INDICATOR_OBSERVED} and {@value #ASSUMPTION_BROKEN} (off by default: only
 * channels that list them receive them). Private sets send none.
 */
@Component
public class HypothesisNotifications {

    /** A hypothesis crossed an ICD 203 band. */
    public static final String BAND_CHANGED = "hypothesis.band_changed";

    /** An indicator was observed. */
    public static final String INDICATOR_OBSERVED = "indicator.observed";

    /** A key assumption broke. */
    public static final String ASSUMPTION_BROKEN = "assumption.broken";

    private final ObjectProvider<NotificationService> notifications;

    /** Creates the component. */
    public HypothesisNotifications(ObjectProvider<NotificationService> notifications) {
        this.notifications = notifications;
    }

    /** True when band changes of a set with {@code cadence} notify by default (daily and weekly sets). */
    public static boolean bandChangeDefaultOn(String cadence) {
        return "daily".equals(cadence) || "weekly".equals(cadence);
    }

    /**
     * Sends {@value #BAND_CHANGED}.
     *
     * @param set the set
     * @param changes the band changes ({@code hypothesisId}, {@code label}, {@code fromTerm}, {@code toTerm},
     *     {@code probability})
     */
    public void bandChanged(HypothesisSetEntity set, List<Map<String, Object>> changes) {
        if (set.isPrivateSet() || changes == null || changes.isEmpty()) {
            return;
        }
        StringBuilder body = new StringBuilder();
        for (Map<String, Object> change : changes) {
            if (!body.isEmpty()) {
                body.append('\n');
            }
            body.append("- '").append(change.get("label")).append("' moved from ").append(change.get("fromTerm"))
                    .append(".\n  It is now ").append(change.get("toTerm")).append(" (")
                    .append(RevisionRationale.percent(((Number) change.get("probability")).doubleValue()))
                    .append(").");
        }
        body.append("\n\nThe revisions explain each change: GET /v1/hypotheses/sets/").append(set.getId())
                .append("/revisions.");
        Map<String, Object> data = data(set);
        data.put("bandChanges", changes);
        send(NotificationMessage.builder(BAND_CHANGED, BAND_CHANGED, "Hypothesis band change: " + set.getQuestion(),
                        "info")
                .body(body.toString())
                .url("/v1/hypotheses/sets/" + set.getId())
                .data(data)
                .tags(List.of("hypothesis"))
                .build());
    }

    /** Sends {@value #INDICATOR_OBSERVED}. */
    public void indicatorObserved(HypothesisSetEntity set, IndicatorEntity indicator) {
        if (set.isPrivateSet()) {
            return;
        }
        Map<String, Object> data = data(set);
        data.put("indicatorId", indicator.getId());
        data.put("signalFamily", indicator.getSignalFamily());
        send(NotificationMessage.builder(INDICATOR_OBSERVED, INDICATOR_OBSERVED, "Indicator observed: "
                        + indicator.getDescription(), "info")
                .body("An indicator of the hypothesis set \"" + set.getQuestion() + "\" was observed. The set was"
                        + " revised; see its revisions for the change and its drivers.")
                .url("/v1/hypotheses/sets/" + set.getId())
                .data(data)
                .tags(List.of("hypothesis"))
                .build());
    }

    /** Sends {@value #ASSUMPTION_BROKEN}. */
    public void assumptionBroken(HypothesisSetEntity set, KeyAssumptionEntity assumption) {
        if (set.isPrivateSet()) {
            return;
        }
        Map<String, Object> data = data(set);
        data.put("assumptionId", assumption.getId());
        data.put("linchpin", assumption.isLinchpin());
        send(NotificationMessage.builder(ASSUMPTION_BROKEN, ASSUMPTION_BROKEN, "Key assumption broken: "
                        + assumption.getText(), assumption.isLinchpin() ? "warning" : "info")
                .body((assumption.isLinchpin() ? "A linchpin assumption" : "A key assumption") + " of the hypothesis"
                        + " set \"" + set.getQuestion() + "\" is now broken."
                        + (assumption.isLinchpin() ? " The set is flagged for review and its analytic rating is one"
                                + " step lower." : ""))
                .url("/v1/hypotheses/sets/" + set.getId())
                .data(data)
                .tags(List.of("hypothesis"))
                .build());
    }

    private static Map<String, Object> data(HypothesisSetEntity set) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("setId", set.getId());
        data.put("question", set.getQuestion());
        data.put("cadence", set.getCadence());
        return data;
    }

    private void send(NotificationMessage message) {
        NotificationService service = notifications.getIfAvailable();
        if (service != null) {
            service.publish(message, null);
        }
    }
}
