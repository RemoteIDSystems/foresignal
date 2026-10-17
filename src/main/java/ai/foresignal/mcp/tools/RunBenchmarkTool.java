package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.benchmark.BenchmarkInputs;
import ai.foresignal.benchmark.BenchmarkRunEntity;
import ai.foresignal.benchmark.BenchmarkService;
import ai.foresignal.benchmark.BenchmarkViews;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code run_benchmark} (agent, prompt 123): queues a benchmark run as {@code POST /v1/benchmarks/runs} does; the
 * result is the queued {@code BenchmarkRun}. One run at a time ({@code BENCHMARK_IN_PROGRESS}).
 */
@RestrictedRead("a queued benchmark run holds no record content")
@Component
public class RunBenchmarkTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "suite", enumeration("The synthetic scenarios, the historical corpus, or both.", "synthetic",
                    BenchmarkInputs.SUITES.toArray(String[]::new)),
            "components", JsonSchemas.enumArray("Components to benchmark (default: all).",
                    BenchmarkInputs.COMPONENTS.toArray(String[]::new)),
            "seed", JsonSchemas.integer("Scenario seed (default: the suite's fixed seed).", Long.MIN_VALUE,
                    Long.MAX_VALUE, null)),
            "suite");

    private final BenchmarkService benchmarks;
    private final JsonMapper json;

    /** Creates the tool. */
    public RunBenchmarkTool(BenchmarkService benchmarks, JsonMapper json) {
        this.benchmarks = benchmarks;
        this.json = json;
    }

    @Override
    public String name() {
        return "run_benchmark";
    }

    @Override
    public String title() {
        return "Run analytic benchmark";
    }

    @Override
    public String description() {
        return "Queues an analytic benchmark run over the deterministic synthetic scenarios, the historical corpus or"
                + " both. It writes only benchmark results, never findings, events, situations or forecasts. Poll"
                + " get_benchmark_status (or GET /v1/benchmarks/runs/{id}) for the outcome; only one run at a time.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The queued BenchmarkRun");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        BenchmarkInputs.RunRequest request = BenchmarkInputs.run(args);
        BenchmarkRunEntity run = benchmarks.submit(request, context.actor(), "agent");
        Map<String, Object> out = new LinkedHashMap<>(BenchmarkViews.run(run, benchmarks.job(run.getId())));
        return ToolResult.json(json.valueToTree(out));
    }
}
