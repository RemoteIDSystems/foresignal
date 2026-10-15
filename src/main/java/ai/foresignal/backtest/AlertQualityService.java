package ai.foresignal.backtest;

import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Feedback;
import ai.foresignal.trigger.Severity;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Alert quality for one rule (prompt 103): maps the 017 feedback enum into positive / negative /
 * neutral classes, then reports precision with a Wilson interval, median time-to-ack and
 * time-to-resolve, flapping, duplicates and the severity distribution.
 */
@Service
public class AlertQualityService {

    /**
     * The feedback mapping: {@code useful} is positive; {@code noise} and {@code false_positive} are
     * negative; {@code late} is neutral (it arrived, just too late to act on).
     */
    public static final Map<String, String> FEEDBACK_CLASSES = Map.of(
            Feedback.USEFUL.wire(), "positive",
            Feedback.NOISE.wire(), "negative",
            Feedback.FALSE_POSITIVE.wire(), "negative",
            Feedback.LATE.wire(), "neutral");

    /** The labelled alerts below which precision is null. */
    public static final int MIN_LABELLED = 5;

    private final AlertStore alerts;

    /** Creates the service. */
    public AlertQualityService(AlertStore alerts) {
        this.alerts = alerts;
    }

    /** The quality report for a rule since {@code since}. */
    public Map<String, Object> forRule(UUID ruleId, Instant since) {
        AlertQuery query = new AlertQuery(java.util.Set.of(ruleId), null, null, since, null, null, null, null, null,
                true, java.util.Set.of(), ai.foresignal.security.restrict.ReadRestriction.NONE);
        List<AlertView> rows = alerts.feed(query, 5_000);
        long positive = 0;
        long negative = 0;
        long neutral = 0;
        long labelled = 0;
        List<Long> ackMinutes = new ArrayList<>();
        List<Long> resolveMinutes = new ArrayList<>();
        long flapping = 0;
        Map<String, Long> severity = new LinkedHashMap<>();
        Map<String, Instant> lastByEntity = new LinkedHashMap<>();
        long duplicates = 0;
        for (AlertView alert : rows) {
            String feedbackClass = alert.feedback() == null ? "unlabelled"
                    : FEEDBACK_CLASSES.getOrDefault(alert.feedback(), "neutral");
            switch (feedbackClass) {
                case "positive" -> {
                    positive++;
                    labelled++;
                }
                case "negative" -> {
                    negative++;
                    labelled++;
                }
                case "neutral" -> neutral++;
                default -> { }
            }
            severity.merge(alert.severity(), 1L, Long::sum);
            if (alert.acknowledgedAt() != null && alert.firedAt() != null) {
                ackMinutes.add(Duration.between(alert.firedAt(), alert.acknowledgedAt()).toMinutes());
            }
            if (alert.resolvedAt() != null && alert.firedAt() != null) {
                long minutes = Duration.between(alert.firedAt(), alert.resolvedAt()).toMinutes();
                resolveMinutes.add(minutes);
                if (minutes < 10) {
                    flapping++;
                }
            }
            String key = alert.sourceId() + ":" + alert.externalId();
            Instant last = lastByEntity.get(key);
            if (last != null && Duration.between(last, alert.firedAt()).abs().compareTo(Duration.ofMinutes(10)) < 0) {
                duplicates++;
            }
            lastByEntity.put(key, alert.firedAt());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ruleId", ruleId);
        out.put("alerts", rows.size());
        out.put("labelled", labelled);
        if (labelled == 0) {
            out.put("labelledShare", 0.0);
        } else {
            out.put("labelledShare", (double) labelled / rows.size());
        }
        if (labelled < MIN_LABELLED) {
            out.put("precision", null);
        } else {
            double precision = (double) positive / (positive + negative);
            out.put("precision", precision);
            out.put("precisionInterval", List.of(Wilson.interval(positive, positive + negative)[0],
                    Wilson.interval(positive, positive + negative)[1]));
        }
        out.put("positive", positive);
        out.put("negative", negative);
        out.put("neutral", neutral);
        out.put("medianTimeToAckMinutes", median(ackMinutes));
        out.put("medianTimeToResolveMinutes", median(resolveMinutes));
        out.put("flapping", flapping);
        out.put("duplicates", duplicates);
        out.put("severity", severity);
        return out;
    }

    /** The stored feedback classes, for the docs. */
    public static Map<String, String> mapping() {
        return FEEDBACK_CLASSES;
    }

    private static Double median(List<Long> values) {
        if (values.isEmpty()) {
            return null;
        }
        List<Long> sorted = new ArrayList<>(values);
        sorted.sort(Long::compareTo);
        int middle = sorted.size() / 2;
        return sorted.size() % 2 == 0 ? (sorted.get(middle - 1) + sorted.get(middle)) / 2.0 : sorted.get(middle) * 1.0;
    }
}
