package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.benchmark.BenchmarkService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_benchmark_status} (read, prompt 123): the latest {@code pass | warn | fail} of every benchmarked
 * intelligence component and what it gates, as {@code GET /v1/benchmarks/status} returns it.
 */
@RestrictedRead("benchmark statuses are statistics over synthetic data and corpus cases, no record content")
@Component
public class GetBenchmarkStatusTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props());

    private final BenchmarkService benchmarks;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetBenchmarkStatusTool(BenchmarkService benchmarks, JsonMapper json) {
        this.benchmarks = benchmarks;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_benchmark_status";
    }

    @Override
    public String title() {
        return "Get analytic benchmark status";
    }

    @Override
    public String description() {
        return "Returns the latest analytic benchmark status (pass, warn, fail or unknown) of every intelligence"
                + " component: change points, event studies, co-occurrence, anomalies, evidence independence,"
                + " canonical linking, precursors, the situation linker and the forecasters, with what each gates."
                + " When a component you rely on is fail, say its output is less trustworthy; findings of a failing"
                + " detector carry the caveat BENCHMARK_FAIL.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The BenchmarkStatus object");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(json.valueToTree(benchmarks.status()));
    }
}
