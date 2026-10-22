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
 *
 * <p>Prompt 115 extends the report, next to (never mixed into) the feedback precision, with the implicit
 * signal counts ({@code signals}), their weighted {@code signalScore} ({@link ai.foresignal.quality.AlertSignalType})
 * and the {@code daily[]} trend rows of {@code rule_quality_daily}. Exercise alerts are excluded everywhere: the
 * {@link AlertQuery} used here never includes them.
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

    /** The labeled alerts below which precision is null. */
    public static final int MIN_LABELED = 5;

    /** The most alerts read per report. */
    public static final int MAX_ALERTS = 5_000;

    /** The duplicate window of a rule without a cooldown. */
    public static final Duration DEFAULT_DUPLICATE_WINDOW = Duration.ofMinutes(10);

    private final AlertStore alerts;
    private final ai.foresignal.trigger.TriggerRuleService rules;

    /** The implicit signals (prompt 115); absent in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.quality.AlertSignalStore signals;

    /** The daily trend rows (prompt 115); absent in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.quality.RuleQualityDailyStore daily;

    /** Creates the service. */
    public AlertQualityService(AlertStore alerts, ai.foresignal.trigger.TriggerRuleService rules) {
        this.alerts = alerts;
        this.rules = rules;
    }

    /**
     * One labeled alert, for the threshold scan, the geofence check and (prompt 115) the active-window and
     * severity checks of {@link TuningSuggester}.
     *
     * @param feedbackClass {@code positive}, {@code negative} or {@code neutral}
     * @param recordSnapshot the alert's record snapshot as JSON text, or null
     * @param feedback the feedback wire value, or null when unknown
     * @param firedAt when the alert fired, or null when unknown
     */
    public record Labeled(String feedbackClass, String recordSnapshot, String feedback, Instant firedAt) {

        /** A labeled alert without its feedback value and time (the shape before prompt 115). */
        public Labeled(String feedbackClass, String recordSnapshot) {
            this(feedbackClass, recordSnapshot, null, null);
        }
    }

    /** The alerts of a rule since {@code since} that carry feedback, with their class and snapshot. */
    public List<Labeled> labeled(UUID ruleId, Instant since) {
        List<Labeled> out = new ArrayList<>();
        for (AlertView alert : alerts.feed(query(ruleId, since), MAX_ALERTS)) {
            if (alert.feedback() != null) {
                out.add(new Labeled(FEEDBACK_CLASSES.getOrDefault(alert.feedback(), "neutral"),
                        alert.recordSnapshot(), alert.feedback(), alert.firedAt()));
            }
        }
        return out;
    }

    private static AlertQuery query(UUID ruleId, Instant since) {
        return new AlertQuery(java.util.Set.of(ruleId), null, null, since, null, null, null, null, null,
                true, java.util.Set.of(), ai.foresignal.security.restrict.ReadRestriction.NONE);
    }

    /**
     * The duplicate window of a rule: twice its cooldown (prompt 103 section 6), or
     * {@link #DEFAULT_DUPLICATE_WINDOW} when it has none or cannot be read.
     */
    Duration duplicateWindow(UUID ruleId) {
        try {
            int cooldown = rules == null ? 0 : rules.load(ruleId).getCooldownSeconds();
            return cooldown > 0 ? Duration.ofSeconds(2L * cooldown) : DEFAULT_DUPLICATE_WINDOW;
        } catch (RuntimeException e) {
            return DEFAULT_DUPLICATE_WINDOW;
        }
    }

    /** The quality report for a rule since {@code since}. */
    public Map<String, Object> forRule(UUID ruleId, Instant since) {
        List<AlertView> rows = alerts.feed(query(ruleId, since), MAX_ALERTS);
        Duration duplicateWindow = duplicateWindow(ruleId);
        long positive = 0;
        long negative = 0;
        long neutral = 0;
        long labeled = 0;
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
                    labeled++;
                }
                case "negative" -> {
                    negative++;
                    labeled++;
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
            if (last != null && alert.firedAt() != null
                    && Duration.between(last, alert.firedAt()).abs().compareTo(duplicateWindow) < 0) {
                duplicates++;
            }
            lastByEntity.put(key, alert.firedAt());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ruleId", ruleId);
        out.put("alerts", rows.size());
        out.put("labelled", labeled);
        if (labeled == 0) {
            out.put("labelledShare", 0.0);
        } else {
            out.put("labelledShare", (double) labeled / rows.size());
        }
        if (labeled < MIN_LABELED) {
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
        out.put("duplicateWindow", duplicateWindow.toString());
        out.put("severity", severity);
        // Prompt 115: implicit signals and the daily trend, next to the feedback precision.
        Map<String, Long> signalCounts = signals == null ? ai.foresignal.quality.AlertSignalType.complete(Map.of())
                : signals.counts(ruleId, since);
        out.put("signals", signalCounts);
        out.put("signalScore", ai.foresignal.quality.AlertSignalType.score(signalCounts, rows.size()));
        out.put("signalWeights", ai.foresignal.quality.AlertSignalType.weights());
        out.put("daily", daily == null ? List.of() : daily.daily(ruleId, since));
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
