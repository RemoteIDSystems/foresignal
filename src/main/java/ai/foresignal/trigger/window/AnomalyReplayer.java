package ai.foresignal.trigger.window;

import ai.foresignal.manifest.ManifestReplayCheck;
import ai.foresignal.manifest.ManifestReplayer;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.manifest.ReplayOutcome;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Replays a 100 anomaly evaluation from its manifest (prompt 116): reads the recorded activity samples (the
 * bucket's own, then the slot samples the baseline used), rebuilds the baseline, re-runs the detector with the
 * recorded window spec and detectability decision, and compares with the stored {@code anomaly_observation}.
 * A sample pruned by the 26-week retention makes the manifest {@code not-replayable} ({@code inputs-purged}).
 */
@Component
public class AnomalyReplayer implements ManifestReplayer {

    private final ActivitySampleRepository samples;
    private final JdbcTemplate jdbc;

    /** Creates the replayer. */
    public AnomalyReplayer(ActivitySampleRepository samples, JdbcTemplate jdbc) {
        this.samples = samples;
        this.jdbc = jdbc;
    }

    @Override
    public boolean supports(String component) {
        return WindowEngine.ANOMALY_COMPONENT.equals(component);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReplayOutcome replay(ManifestService.Manifest manifest) {
        if (!AnomalyDetector.VERSION.equals(manifest.componentVersion())) {
            return ReplayOutcome.notReplayable(ReplayOutcome.COMPONENT_REMOVED, "The detector version "
                    + manifest.componentVersion() + " is no longer installed (now " + AnomalyDetector.VERSION + ").");
        }
        List<String> refs = manifest.refs();
        if (refs.isEmpty()) {
            return ReplayOutcome.notReplayable(ReplayOutcome.INPUTS_PURGED, "The manifest lists no samples.");
        }
        List<ActivitySampleEntity> loaded = new ArrayList<>();
        for (String ref : refs) {
            Optional<ActivitySampleEntity> sample = parse(ref).flatMap(samples::findById);
            if (sample.isEmpty()) {
                return ReplayOutcome.notReplayable(ReplayOutcome.INPUTS_PURGED,
                        "The activity sample " + ref + " was purged by retention.");
            }
            loaded.add(sample.get());
        }
        WindowSpec spec = WindowSpec.fromStored((Map<String, Object>) manifest.config().get("window"));
        ActivitySampleEntity bucket = loaded.get(0);
        List<Integer> counts = new ArrayList<>();
        List<Double> coverages = new ArrayList<>();
        for (ActivitySampleEntity sample : loaded.subList(1, loaded.size())) {
            counts.add(sample.getCount());
            coverages.add(sample.getCoverageRatio() == null ? 1.0 : sample.getCoverageRatio());
        }
        Baseline baseline = Baseline.of(counts, coverages, spec.slotMode(), spec.bucket());
        double ratio = bucket.getCoverageRatio() == null ? 1.0 : bucket.getCoverageRatio();
        AnomalyDetector.Result result = AnomalyDetector.evaluate(bucket.getCount(), baseline, ratio,
                bucket.isCoverageOk(), spec);
        Object detectability = manifest.config().get("detectability");
        if (detectability instanceof Map<?, ?> d && Boolean.TRUE.equals(d.get("checked"))
                && Boolean.FALSE.equals(d.get("ok")) && "activity-down".equals(result.classification())) {
            result = WindowEngine.lowDetectability(result);
        }
        List<Map<String, Object>> stored = jdbc.queryForList("SELECT observed, expected, p_value, classification,"
                + " direction FROM anomaly_observation WHERE manifest_id = ? ORDER BY id LIMIT 1", manifest.id());
        if (stored.isEmpty()) {
            return ReplayOutcome.notReplayable(null, "No stored anomaly observation carries this manifest.");
        }
        Map<String, Object> row = stored.get(0);
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("observed", row.get("observed"));
        before.put("expected", row.get("expected"));
        before.put("pValue", row.get("p_value"));
        before.put("classification", row.get("classification"));
        before.put("direction", row.get("direction"));
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("observed", result.observed());
        after.put("expected", result.expected());
        after.put("pValue", result.pValue());
        after.put("classification", result.classification());
        after.put("direction", result.direction());
        return ManifestReplayCheck.compare(before, after);
    }

    /** The key of {@code activity-sample:{ruleId}/{group}/{bucketStart}}. */
    static Optional<ActivitySampleEntity.Key> parse(String ref) {
        String prefix = "activity-sample:";
        if (ref == null || !ref.startsWith(prefix)) {
            return Optional.empty();
        }
        String[] parts = ref.substring(prefix.length()).split("/", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            return Optional.of(new ActivitySampleEntity.Key(UUID.fromString(parts[0]),
                    URLDecoder.decode(parts[1], StandardCharsets.UTF_8), Instant.parse(parts[2])));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
