package ai.foresignal.benchmark.component;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkScorer;
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.EvidenceMatcher;
import ai.foresignal.evidence.ObservationFacts;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Benchmarks canonical-event linking ({@code canonical-linking}, the 116 {@link EvidenceMatcher} under the bundled
 * {@code seismic} rule) on the planted multi-source events of {@code duplicate-echo}. Observations are linked in seq
 * order the way the {@code EvidenceResolver} links them, in memory: each is matched against every member of every
 * canonical event so far, matches below {@code minLinkConfidence} are dropped, no match creates an event, and an
 * observation matching several events merges them into the oldest. Pairwise precision and recall against the planted
 * events, and spurious merges (canonical events mixing planted events), are reported.
 */
@Component
public class CanonicalLinkingBenchmark implements ComponentBenchmark {

    /** The component name. */
    public static final String NAME = "canonical-linking";

    private final CanonicalRules rules;

    /** Creates the benchmark over the canonical rules in force. */
    public CanonicalLinkingBenchmark(CanonicalRules rules) {
        this.rules = rules;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String version() {
        return "evidence-resolver " + EvidenceMatcher.RESOLVER_VERSION + " (canonical-rules " + rules.version() + ")";
    }

    @Override
    public List<ScenarioType> scenarios() {
        return List.of(ScenarioType.DUPLICATE_ECHO);
    }

    @Override
    public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
        int replicates = params.replicates();
        BenchmarkScorer.PairCounts pairs = new BenchmarkScorer.PairCounts(0, 0, 0);
        int spurious = 0;
        Map<String, Object> truth = null;
        CanonicalRules.FamilyRule rule = rules.family("seismic");
        for (int r = 0; r < replicates; r++) {
            monitor.checkpoint((double) r / replicates);
            ScenarioGenerator.LinkingScenario sc = ScenarioGenerator.canonicalEvents(params.seed(), r);
            if (truth == null) {
                truth = new LinkedHashMap<>(sc.truth());
            }
            List<String> assigned = link(rule, sc.observations(), rules.minLinkConfidence());
            pairs = pairs.plus(BenchmarkScorer.pairs(sc.truthEvent(), assigned));
            spurious += BenchmarkScorer.spuriousMerges(sc.truthEvent(), assigned);
        }
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("linkingPrecision", pairs.precision());
        m.put("linkingRecall", pairs.recall());
        m.put("spuriousMerges", (double) spurious);
        m.put("minLinkConfidence", rules.minLinkConfidence());
        if (truth != null) {
            truth.put("replicates", replicates);
        }
        return new ScenarioMetrics(scenario.wire(), null, params.seed(), m, truth, List.of("Linked in memory as the"
                + " EvidenceResolver links (best match per event, merge into the oldest); the bundled"
                + " minLinkConfidence is used, not the operator setting."));
    }

    /**
     * Links observations in order and returns each one's canonical event (as an index label).
     *
     * @param rule the family rule
     * @param observations the observations in seq order
     * @param minimum the minimum link confidence
     * @return the canonical event of each observation, aligned
     */
    static List<String> link(CanonicalRules.FamilyRule rule, List<ObservationFacts> observations, double minimum) {
        List<List<Integer>> events = new ArrayList<>();
        List<Integer> eventOf = new ArrayList<>();
        for (int i = 0; i < observations.size(); i++) {
            ObservationFacts facts = observations.get(i);
            List<Integer> matched = new ArrayList<>();
            for (int e = 0; e < events.size(); e++) {
                if (events.get(e) == null) {
                    continue;
                }
                double best = -1;
                for (int member : events.get(e)) {
                    Optional<EvidenceMatcher.Match> match = EvidenceMatcher.match(rule, facts, observations.get(member));
                    if (match.isPresent()) {
                        best = Math.max(best, match.get().confidence());
                    }
                }
                if (best >= minimum) {
                    matched.add(e);
                }
            }
            if (matched.isEmpty()) {
                List<Integer> created = new ArrayList<>();
                created.add(i);
                events.add(created);
                eventOf.add(events.size() - 1);
                continue;
            }
            int survivor = matched.get(0);
            for (int k = 1; k < matched.size(); k++) {
                int other = matched.get(k);
                for (int member : events.get(other)) {
                    events.get(survivor).add(member);
                    eventOf.set(member, survivor);
                }
                events.set(other, null);
            }
            events.get(survivor).add(i);
            eventOf.add(survivor);
        }
        List<String> out = new ArrayList<>();
        for (int e : eventOf) {
            out.add("canonical-" + e);
        }
        return out;
    }
}
