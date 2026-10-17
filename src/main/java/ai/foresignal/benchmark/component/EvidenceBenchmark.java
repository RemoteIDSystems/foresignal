package ai.foresignal.benchmark.component;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkScorer;
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.evidence.EvidenceGrouper;
import ai.foresignal.evidence.EvidenceMatcher;
import ai.foresignal.evidence.OriginatorRegistry;
import ai.foresignal.intel.EventScorer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Benchmarks evidence independence ({@code evidence}, 116 {@link EvidenceGrouper} feeding the 101
 * {@link EventScorer}) on {@code duplicate-echo}: an event's original reports are grouped and scored, then grouped and
 * scored again with ten aggregator echoes added (citations and a wire service that declares it aggregates the
 * originator, resolved through a synthetic originator registry). Any increase in independent groups, confidence or
 * evidence quality is duplicate inflation, which must be 0.
 */
@Component
public class EvidenceBenchmark implements ComponentBenchmark {

    /** The component name. */
    public static final String NAME = "evidence";

    private final OriginatorRegistry registry;

    /** Creates the benchmark with the synthetic registry of the scenario. */
    public EvidenceBenchmark(JsonMapper json) {
        this.registry = OriginatorRegistry.of(ScenarioGenerator.echoRegistryDocument(), json);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String version() {
        return "evidence-grouper " + EvidenceMatcher.RESOLVER_VERSION + " + " + EventScorer.NAME + " "
                + EventScorer.VERSION;
    }

    @Override
    public List<ScenarioType> scenarios() {
        return List.of(ScenarioType.DUPLICATE_ECHO);
    }

    @Override
    public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
        int replicates = params.replicates();
        double inflation = 0;
        double groupsBefore = 0;
        double groupsAfter = 0;
        int wrongGroups = 0;
        Map<String, Object> truth = null;
        for (int r = 0; r < replicates; r++) {
            monitor.checkpoint((double) r / replicates);
            ScenarioGenerator.EchoScenario sc = ScenarioGenerator.duplicateEcho(params.seed(), r);
            if (truth == null) {
                truth = new LinkedHashMap<>(sc.truth());
            }
            Scored before = score(sc.originals());
            List<ScenarioGenerator.EchoMember> all = new ArrayList<>(sc.originals());
            all.addAll(sc.echoes());
            Scored after = score(all);
            groupsBefore += before.groups();
            groupsAfter += after.groups();
            if (after.groups() != ((Number) sc.truth().get("expectedGroups")).intValue()) {
                wrongGroups++;
            }
            inflation = Math.max(inflation, BenchmarkScorer.duplicateInflation(before.groups(), after.groups(),
                    before.score().confidence(), after.score().confidence(), before.score().evidenceQuality(),
                    after.score().evidenceQuality()));
        }
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("duplicateInflation", inflation);
        m.put("meanGroupsBefore", groupsBefore / replicates);
        m.put("meanGroupsAfter", groupsAfter / replicates);
        m.put("wrongGroupCountShare", BenchmarkScorer.rate(wrongGroups, replicates));
        if (truth != null) {
            truth.put("replicates", replicates);
        }
        return new ScenarioMetrics(scenario.wire(), null, params.seed(), m, truth, List.of("duplicateInflation is the"
                + " largest over replicates of the group increase plus one for a confidence rise and one for an"
                + " evidence-quality rise."));
    }

    /**
     * Groups and score of a member set.
     *
     * @param groups the independent evidence groups
     * @param score the event score
     */
    record Scored(int groups, EventScorer.Score score) {}

    /** Groups the members and scores them as the event scorer does, one member counted per group. */
    Scored score(List<ScenarioGenerator.EchoMember> members) {
        List<EvidenceGrouper.Member> grouperInput = new ArrayList<>();
        long seq = 1;
        for (ScenarioGenerator.EchoMember m : members) {
            grouperInput.add(new EvidenceGrouper.Member(m.key(), m.sourceId(), registry.resolve(m.sourceId()),
                    OriginatorRegistry.normalizeCitation(m.citation()), seq++));
        }
        List<EvidenceGrouper.Group> groups = EvidenceGrouper.group(grouperInput, registry);
        Map<String, String> groupOf = new HashMap<>();
        for (EvidenceGrouper.Group g : groups) {
            for (String key : g.members()) {
                groupOf.put(key, g.root());
            }
        }
        List<EventScorer.MemberView> views = new ArrayList<>();
        for (ScenarioGenerator.EchoMember m : members) {
            String root = groupOf.get(m.key());
            views.add(new EventScorer.MemberView(m.signalFamily(), 0.8, m.sourceId(), true, m.echo() ? 0.6 : 0.8,
                    root, root));
        }
        return new Scored(groups.size(), EventScorer.score(views, 0));
    }
}
