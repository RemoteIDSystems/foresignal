package ai.foresignal.benchmark.component;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkScorer;
import ai.foresignal.benchmark.scenario.Rng;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.social.correlation.AttentionGapDetector;
import ai.foresignal.social.correlation.CorrelationVocabulary;
import ai.foresignal.social.correlation.PhysicalEvidenceSource;
import ai.foresignal.social.correlation.SocialPhysicalComparator;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Benchmarks social–physical correlation ({@code social-physical}, prompt 137: {@link SocialPhysicalComparator} 1.0.0
 * and {@link AttentionGapDetector} 1.0.0) on planted scenarios, pooled over replicates:
 *
 * <ul>
 *   <li>{@code social-planted-covered}: a narrative with no physical event under adequate coverage must be
 *       {@code unobserved};</li>
 *   <li>{@code social-planted-thin}: the same under thin coverage must be {@code insufficient-coverage};</li>
 *   <li>{@code social-event-discussed}: a physical event with organic discussion must be {@code corroborated} with a
 *       positive lead and draw no attention gap;</li>
 *   <li>{@code social-event-silent}: a physical event with silence under adequate coverage must draw an attention
 *       gap;</li>
 *   <li>{@code social-reused-image}: a claim with a reused image must be {@code contradicted};</li>
 *   <li>{@code social-cue-loop}: a cue-found claim that matches only its own trigger must carry
 *       {@code CUE_TRIGGER_MATCH} (never independent confirmation), and social evidence never corroborates.</li>
 * </ul>
 */
@Component
public class SocialPhysicalBenchmark implements ComponentBenchmark {

    /** The component name. */
    public static final String NAME = "social-physical";

    private static final Instant T0 = Instant.parse("2026-01-15T12:00:00Z");

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String version() {
        return CorrelationVocabulary.COMPARATOR + " " + CorrelationVocabulary.COMPARATOR_VERSION + " + "
                + CorrelationVocabulary.GAP_DETECTOR + " " + CorrelationVocabulary.GAP_DETECTOR_VERSION;
    }

    @Override
    public List<ScenarioType> scenarios() {
        return ScenarioType.SOCIAL;
    }

    @Override
    public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
        int replicates = params.replicates();
        int correct = 0;
        int uncaveated = 0;
        int gaps = 0;
        double leadSum = 0;
        String expected = expectedStatus(scenario);
        for (int r = 0; r < replicates; r++) {
            monitor.checkpoint((double) r / replicates);
            Rng rng = Rng.of(params.seed(), NAME, scenario.wire(), Integer.toString(r));
            if (scenario == ScenarioType.SOCIAL_EVENT_SILENT || scenario == ScenarioType.SOCIAL_EVENT_DISCUSSED) {
                List<Double> past = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    past.add(Math.max(0, 3 + rng.normal() * 0.5));
                }
                double observed = scenario == ScenarioType.SOCIAL_EVENT_SILENT ? 0.05 * rng.uniform()
                        : 3 + rng.normal() * 0.3;
                if (AttentionGapDetector.gap(observed, AttentionGapDetector.expected(past, List.of(), 10))) {
                    gaps++;
                }
            }
            if (scenario == ScenarioType.SOCIAL_EVENT_SILENT) {
                continue;
            }
            SocialPhysicalComparator.Decision d = SocialPhysicalComparator.decide(inputs(scenario, rng));
            if (d.status().equals(expected)) {
                correct++;
            }
            if (scenario == ScenarioType.SOCIAL_CUE_LOOP && d.caveats().stream().noneMatch(c ->
                    CorrelationVocabulary.CUE_TRIGGER_MATCH.equals(c.get("code")))) {
                uncaveated++;
            }
            Object lead = d.leadTimes().get("leadOverPhysicalSeconds");
            if (lead instanceof Number n) {
                leadSum += n.doubleValue();
            }
        }
        Map<String, Double> m = new LinkedHashMap<>();
        if (scenario == ScenarioType.SOCIAL_EVENT_SILENT) {
            m.put("gapRecall", BenchmarkScorer.rate(gaps, replicates));
        } else {
            m.put("correctStatusShare", BenchmarkScorer.rate(correct, replicates));
        }
        if (scenario == ScenarioType.SOCIAL_EVENT_DISCUSSED) {
            m.put("falseGapShare", BenchmarkScorer.rate(gaps, replicates));
            m.put("meanLeadSeconds", leadSum / replicates);
        }
        if (scenario == ScenarioType.SOCIAL_CUE_LOOP) {
            m.put("uncaveatedTriggerMatches", (double) uncaveated);
        }
        Map<String, Object> truth = new LinkedHashMap<>();
        truth.put("expectedStatus", expected);
        truth.put("replicates", replicates);
        return new ScenarioMetrics(scenario.wire(), null, params.seed(), m, truth, List.of("Social evidence is planted"
                + " alongside the physical evidence in every replicate; it must never corroborate."));
    }

    /** The status a scenario must produce, or null for the attention-gap scenario. */
    static String expectedStatus(ScenarioType scenario) {
        return switch (scenario) {
            case SOCIAL_PLANTED_COVERED -> CorrelationVocabulary.UNOBSERVED;
            case SOCIAL_PLANTED_THIN -> CorrelationVocabulary.INSUFFICIENT;
            case SOCIAL_EVENT_DISCUSSED, SOCIAL_CUE_LOOP -> CorrelationVocabulary.CORROBORATED;
            case SOCIAL_REUSED_IMAGE -> CorrelationVocabulary.CONTRADICTED;
            default -> null;
        };
    }

    /** The comparator inputs of a scenario replicate. */
    static SocialPhysicalComparator.Inputs inputs(ScenarioType scenario, Rng rng) {
        Instant social = T0.plusSeconds(rng.nextInt(600));
        Instant start = T0.minus(Duration.ofHours(6));
        Instant end = T0.plus(Duration.ofHours(24));
        Instant now = end.plus(Duration.ofHours(25));
        List<PhysicalEvidenceSource.Evidence> evidence = new ArrayList<>();
        evidence.add(new PhysicalEvidenceSource.Evidence("narrative:planted", "social-narrative", "social:bluesky:a",
                "social", social, 0.0, false));
        double p = 0.9 + 0.09 * rng.uniform();
        SocialPhysicalComparator.Detectability covered = new SocialPhysicalComparator.Detectability(p, p - 0.05,
                Math.min(1, p + 0.05), List.of("declared"), true, null);
        SocialPhysicalComparator.Detectability thin = new SocialPhysicalComparator.Detectability(0.3, 0.2, 0.4,
                List.of("declared"), false, "LOW_DETECTABILITY");
        String reuse = null;
        String trigger = null;
        SocialPhysicalComparator.Detectability d = covered;
        switch (scenario) {
            case SOCIAL_PLANTED_THIN -> d = thin;
            case SOCIAL_EVENT_DISCUSSED -> evidence.add(new PhysicalEvidenceSource.Evidence("canonical-event:planted",
                    "conflict", "acled", "aggregator", social.plusSeconds(600 + rng.nextInt(3600)), 3.0, false));
            case SOCIAL_REUSED_IMAGE -> reuse = "finding:reuse";
            case SOCIAL_CUE_LOOP -> {
                trigger = "canonical-event:trigger";
                evidence.add(new PhysicalEvidenceSource.Evidence(trigger, "conflict", "acled", "aggregator",
                        social.minusSeconds(3600), 2.0, false));
            }
            default -> {
                // planted narrative only
            }
        }
        return new SocialPhysicalComparator.Inputs(start, end, end.plus(Duration.ofHours(24)), now, evidence, d, reuse,
                social, null, trigger);
    }
}
