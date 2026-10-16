package ai.foresignal.backtest;

import ai.foresignal.trigger.window.AnomalyDetector;
import ai.foresignal.trigger.window.Baseline;
import ai.foresignal.trigger.window.WindowEngine;
import ai.foresignal.trigger.window.WindowSpec;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Replays an anomaly rule (prompt 100) over stored bucket counts, in bucket order, for a backtest
 * (prompt 103 §3 "Anomaly rules"). Pure.
 *
 * <p><strong>No look-ahead (INT-2).</strong> The baseline of bucket {@code b} is built only from
 * samples whose bucket starts before {@code b}: the same slot, within the 12-week lookback,
 * coverage-ok, and not flagged by an earlier fire <em>of this replay</em>. Buckets are evaluated
 * strictly in time order, so a sample at the end of the range cannot change any earlier verdict.
 * Samples before the range are history the baseline may use (they precede every evaluated bucket);
 * only buckets inside the range are evaluated and counted.
 *
 * <p>The counts are the per-bucket counts the live engine stored in {@code activity_sample}; a
 * variant may change the detector parameters (sensitivity, direction, minimum delta, minimum weeks)
 * but not the bucket or grouping, which would need the counts recomputed.
 */
public final class AnomalyReplay {

    /** The baseline lookback (prompt 100 §4: 12 weeks). */
    public static final Duration LOOKBACK = Duration.ofDays(84);

    /** The most sample verdicts kept in a result. */
    public static final int MAX_SAMPLES = 200;

    private AnomalyReplay() {}

    /**
     * One stored bucket.
     *
     * @param group the group key
     * @param bucketStart the bucket start
     * @param count the bucket count
     * @param coverageRatio the coverage ratio, or null (treated as 1)
     * @param coverageOk whether the coverage gate passed
     * @param flagged whether the live engine flagged it (used only for samples before the range; a
     *     bucket inside the range is flagged by the replay's own verdict)
     */
    public record Sample(String group, Instant bucketStart, int count, Double coverageRatio, boolean coverageOk,
            boolean flagged) {}

    /**
     * One evaluated bucket.
     *
     * @param group the group key
     * @param bucketStart the bucket start
     * @param observed the bucket count
     * @param expected the expected count
     * @param classification the detector's classification
     * @param fire true when the bucket would have alerted
     */
    public record Verdict(String group, Instant bucketStart, int observed, double expected, String classification,
            boolean fire) {}

    /**
     * The replay's outcome.
     *
     * @param verdicts every evaluated bucket in time order
     * @param byClassification counts per classification
     * @param wouldFire the buckets that would have alerted
     * @param learningBuckets the buckets spent learning (the warm-up consumed)
     * @param coverageDrops the {@code coverage-drop} buckets
     */
    public record Outcome(List<Verdict> verdicts, Map<String, Integer> byClassification, long wouldFire,
            long learningBuckets, long coverageDrops) {

        /**
         * The result map of a rule backtest of an anomaly rule, shaped as {@code RuleBacktestResult}.
         *
         * <p>A fired bucket is identified the way the live engine identifies the alert it would raise:
         * source {@value WindowEngine#SOURCE_ID}, external id {@code ruleId:groupKey}. Each sample's
         * {@code seq} is the watermark the live evaluation of that bucket read up to
         * ({@code anomaly_observation.watermark_seq}); a fired bucket without a stored watermark is left
         * out of the samples (never given an invented seq) but still counted everywhere else. Group keys
         * are not entities, so {@code topEntities[].entityId} is null.
         *
         * @param bucket the rule's bucket, for {@code warmupConsumed}
         * @param ruleId the rule, for the alert identity
         * @param watermarks stored watermark seqs keyed by {@link #key(String, Instant)}
         * @return the result map
         */
        public Map<String, Object> toMap(Duration bucket, UUID ruleId, Map<String, Long> watermarks) {
            Map<String, Integer> perDay = new TreeMap<>();
            Map<String, Integer> byGroup = new LinkedHashMap<>();
            List<Map<String, Object>> samples = new ArrayList<>();
            for (Verdict verdict : verdicts) {
                if (!verdict.fire()) {
                    continue;
                }
                perDay.merge(verdict.bucketStart().atZone(ZoneOffset.UTC).toLocalDate().toString(), 1, Integer::sum);
                byGroup.merge(verdict.group(), 1, Integer::sum);
                Long seq = watermarks == null ? null : watermarks.get(key(verdict.group(), verdict.bucketStart()));
                if (samples.size() < MAX_SAMPLES && seq != null) {
                    Map<String, Object> sample = new LinkedHashMap<>();
                    sample.put("seq", seq);
                    sample.put("source", WindowEngine.SOURCE_ID);
                    sample.put("externalId", alertExternalId(ruleId, verdict.group()));
                    sample.put("observedAt", verdict.bucketStart().toString());
                    sample.put("edge", "window");
                    samples.add(sample);
                }
            }
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ruleType", "anomaly");
            out.put("wouldFire", wouldFire);
            out.put("perDay", RuleBacktest.perDay(perDay));
            out.put("topEntities", byGroup.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(RuleBacktest.MAX_ENTITIES)
                    .map(e -> {
                        Map<String, Object> entity = new LinkedHashMap<>();
                        entity.put("sourceId", WindowEngine.SOURCE_ID);
                        entity.put("externalId", alertExternalId(ruleId, e.getKey()));
                        entity.put("entityId", null);
                        entity.put("count", e.getValue());
                        return entity;
                    })
                    .toList());
            out.put("samples", samples);
            Map<String, Object> anomaly = new LinkedHashMap<>();
            anomaly.put("warmupConsumed", (bucket == null ? Duration.ZERO : bucket.multipliedBy(learningBuckets))
                    .toString());
            anomaly.put("warmupBuckets", learningBuckets);
            anomaly.put("byClassification", byClassification);
            anomaly.put("coverageDrops", coverageDrops);
            out.put("anomaly", anomaly);
            out.put("bucketsEvaluated", verdicts.size());
            return out;
        }
    }

    /**
     * The key of a stored watermark: the group and the bucket start.
     *
     * @param group the group key
     * @param bucketStart the bucket start
     * @return the key used by {@link Outcome#toMap(Duration, UUID, Map)}
     */
    public static String key(String group, Instant bucketStart) {
        return group + '\u0000' + bucketStart.getEpochSecond();
    }

    /**
     * The external id of the {@value WindowEngine#SOURCE_ID} record an alert of this rule and group
     * references (the live engine's {@code ruleId:groupKey}).
     *
     * @param ruleId the rule, or null
     * @param group the group key
     * @return the external id
     */
    public static String alertExternalId(UUID ruleId, String group) {
        return ruleId + ":" + group;
    }

    /**
     * Replays {@code samples} with {@code spec}, evaluating the buckets that start in
     * {@code [from, to)}.
     */
    public static Outcome replay(List<Sample> samples, WindowSpec spec, Instant from, Instant to) {
        List<Sample> sorted = new ArrayList<>(samples);
        sorted.sort(Comparator.comparing(Sample::bucketStart).thenComparing(Sample::group));
        Map<String, Map<Integer, Deque<Sample>>> history = new HashMap<>();
        List<Verdict> verdicts = new ArrayList<>();
        Map<String, Integer> byClassification = new TreeMap<>();
        long wouldFire = 0;
        long learning = 0;
        long coverageDrops = 0;
        for (Sample sample : sorted) {
            int slot = slotOf(sample.bucketStart(), spec.slotMode());
            Deque<Sample> slotHistory = history.computeIfAbsent(sample.group(), g -> new HashMap<>())
                    .computeIfAbsent(slot, s -> new ArrayDeque<>());
            Instant lookbackStart = sample.bucketStart().minus(LOOKBACK);
            while (!slotHistory.isEmpty() && slotHistory.peekFirst().bucketStart().isBefore(lookbackStart)) {
                slotHistory.pollFirst();
            }
            boolean flagged = sample.flagged();
            boolean inRange = !sample.bucketStart().isBefore(from) && sample.bucketStart().isBefore(to);
            if (inRange) {
                flagged = false;
                List<Integer> counts = new ArrayList<>();
                List<Double> coverages = new ArrayList<>();
                for (Sample earlier : slotHistory) {
                    counts.add(earlier.count());
                    coverages.add(earlier.coverageRatio() == null ? 1.0 : earlier.coverageRatio());
                }
                Baseline baseline = Baseline.of(counts, coverages, spec.slotMode(), spec.bucket());
                double coverageRatio = sample.coverageRatio() == null ? 1.0 : sample.coverageRatio();
                AnomalyDetector.Result result = AnomalyDetector.evaluate(sample.count(), baseline, coverageRatio,
                        sample.coverageOk(), spec);
                verdicts.add(new Verdict(sample.group(), sample.bucketStart(), sample.count(), result.expected(),
                        result.classification(), result.fire()));
                byClassification.merge(result.classification(), 1, Integer::sum);
                if (result.fire()) {
                    wouldFire++;
                    flagged = true;
                }
                if ("learning".equals(result.classification())) {
                    learning++;
                }
                if ("coverage-drop".equals(result.classification())) {
                    coverageDrops++;
                }
            }
            // Only coverage-ok, unflagged samples feed later baselines (the live engine's rule).
            if (sample.coverageOk() && !flagged) {
                slotHistory.addLast(sample);
            }
        }
        return new Outcome(verdicts, byClassification, wouldFire, learning, coverageDrops);
    }

    /** The baseline slot of a bucket: hour of day, or hour of week (the live engine's slots). */
    static int slotOf(Instant at, String slotMode) {
        var zoned = at.atZone(ZoneOffset.UTC);
        if ("hour-of-day".equals(slotMode)) {
            return zoned.getHour();
        }
        return zoned.getDayOfWeek().getValue() * 24 + zoned.getHour();
    }
}
